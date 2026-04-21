package com.sunwayworld.cloud.module.lcdp.resourcelock.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resourcelock.bean.LcdpResourceLockBean;
import com.sunwayworld.cloud.module.lcdp.resourcelock.persistent.dao.LcdpResourceLockDao;
import com.sunwayworld.cloud.module.lcdp.resourcelock.service.LcdpResourceLockService;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.context.concurrent.GikamConcurrentLocker;
import com.sunwayworld.framework.exception.checked.CheckedException;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;

@Repository
@GikamBean
public class LcdpResourceLockServiceImpl implements LcdpResourceLockService {

    @Autowired
    private LcdpResourceLockDao lcdpResourceLockDao;

    @Lazy
    @Autowired
    private LcdpResourceService lcdpResourceService;
    
    @Lazy
    @Autowired
    private LcdpResourceLockService proxyInstance;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpResourceLockDao getDao() {
        return lcdpResourceLockDao;
    }


    @Override
    public RestValidationResultBean validateResourceEditable(List<String> resourceIdList) {

        if(resourceIdList.isEmpty()){
            return new RestValidationResultBean(true);
        }

        List<LcdpResourceLockBean> resourceLockList = getDao().selectListByOneColumnValues(resourceIdList, "RESOURCEID");

        //没有资源锁记录，可以编辑
        if (resourceLockList.isEmpty()) {
            return new RestValidationResultBean(true);
        }

        List<LcdpResourceLockBean> lockList = new ArrayList<>();

        for (String resourceId : resourceIdList) {
            List<LcdpResourceLockBean> filterList = resourceLockList.stream().filter(f -> ObjectUtils.equals(resourceId, f.getResourceId())).collect(Collectors.toList());
            LcdpResourceLockBean resourceLockBean = filterList.get(0);

            //有记录但未上锁
            if (StringUtils.isEmpty(resourceLockBean.getLockUserId())) {
                continue;
            }

            //资源的锁定人员为当前登录人可以编制
            if (LocalContextHelper.getLoginUserId().equals(resourceLockBean.getLockUserId())) {
                continue;
            }

            lockList.add(resourceLockBean);
        }

        if (lockList.size() == 0) {
            return new RestValidationResultBean(true);
        } else {
            List<LcdpResourceBean> resourceList = lcdpResourceService.selectListByIds(resourceIdList.stream().map(e -> Long.parseLong(e)).collect(Collectors.toList()));
            StringBuilder sb = new StringBuilder();

            for (LcdpResourceLockBean e : lockList) {
                LcdpResourceBean resource = resourceList.stream().filter(f -> ObjectUtils.equals(Long.parseLong(e.getResourceId()), f.getId())).findFirst().orElse(new LcdpResourceBean());
                
                if (sb.length() > 0) {
                    sb.append("\r\n");
                }
                
                sb.append(I18nHelper.getMessage("LCDP.WARNING.UNABLE_CHECKOUT_UPDATING_RESOURCE", resource.getPath(), e.getLockUserId()));
            }

            return new RestValidationResultBean(false, sb.toString());
        }

    }


    @Override
    public RestValidationResultBean validateResourceEditable(String resourceId) {
        List<LcdpResourceLockBean> resourceLockList = selectListByFilter(SearchFilter.instance().match("RESOURCEID", resourceId).filter(MatchPattern.EQ));

        //没有资源锁记录，可以编辑
        if (resourceLockList.isEmpty()) {
            return new RestValidationResultBean(true);
        }

        LcdpResourceLockBean resourceLockBean = resourceLockList.get(0);
        //有记录但未上锁
        if (StringUtils.isEmpty(resourceLockBean.getLockUserId())) {
            return new RestValidationResultBean(true);
        }
        //资源的锁定人员为当前登录人可以编制
        if (LocalContextHelper.getLoginUserId().equals(resourceLockBean.getLockUserId())) {
            return new RestValidationResultBean(true);
        }

        return new RestValidationResultBean(false, resourceLockBean.getLockUserId() + I18nHelper.getMessage(String.format("LCDP.MODULE.RESOUCES.TIP.%s_EDITING", resourceLockBean.getResourceCategory().toUpperCase())));

    }

    @Override
    @Transactional
    public void lock(String resourceId, String resourceCategory) {
        GikamConcurrentLocker.isolated("RESOURCE_CHECK_OUT$" + resourceId);
        
        if (selectListByFilter(SearchFilter.instance()
                .match("RESOURCEID", resourceId).filter(MatchPattern.EQ)
                .match("LOCKUSERID", LocalContextHelper.getLoginUserId()).filter(MatchPattern.NEQ)).size() > 0) {
            throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.RESOURCES.TIP.CURRENT_RESOURCE_HAS_BEEN_CHECKOUT"));
        }
        
        if (selectListByFilter(SearchFilter.instance()
                .match("RESOURCEID", resourceId).filter(MatchPattern.EQ)
                .match("LOCKUSERID", LocalContextHelper.getLoginUserId()).filter(MatchPattern.EQ)).isEmpty()) {
            LcdpResourceLockBean insertResourceLock = new LcdpResourceLockBean();
            insertResourceLock.setId(ApplicationContextHelper.getNextIdentity());
            insertResourceLock.setResourceId(resourceId);
            insertResourceLock.setResourceCategory(resourceCategory);
            insertResourceLock.setLockUserId(LocalContextHelper.getLoginUserId());
            
            getDao().insert(insertResourceLock);
        }
    }

    @Override
    @Transactional
    public void unLock(List<String> resourceIdList) {
        if (resourceIdList.isEmpty()) {
            return;
        }

        List<LcdpResourceLockBean> resourceLockList = selectListByFilter(SearchFilter.instance().match("RESOURCEID", resourceIdList).filter(MatchPattern.OR));

        getDao().deleteByIdList(resourceLockList.stream().map(LcdpResourceLockBean::getId).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public void lockResource(List<LcdpResourceBean> resourceList) {
        if (ObjectUtils.isEmpty(resourceList)) return;
        List<LcdpResourceLockBean> lockList = resourceList.stream().map(resource -> {
            LcdpResourceLockBean lock = new LcdpResourceLockBean();
            lock.setId(ApplicationContextHelper.getNextIdentity());
            lock.setLockUserId(LocalContextHelper.getLoginUserId());
            lock.setResourceId(resource.getId().toString());
            lock.setResourceCategory(resource.getResourceCategory());
            return lock;
        }).collect(Collectors.toList());

        getDao().fastInsert(lockList);
    }

    @Override
    @Cacheable(value = "T_LCDP_RESOURCE_LOCK.BY_CATEGORY", key = "#lockUserId + '-' + #resourceCategory", unless="#result == null")
    public List<LcdpResourceLockBean> selectResourceLockListByCategory(String lockUserId, String resourceCategory) {
        return selectListByFilter(SearchFilter.instance()
                .match("LOCKUSERID", lockUserId).filter(MatchPattern.EQ)
                .match("RESOURCECATEGORY", resourceCategory).filter(MatchPattern.EQ));
    }
    
    @Override
    @Cacheable(value = "T_LCDP_RESOURCE_LOCK.RESOURCE_LOCK", key = "#resourceId + '-' + #resourceCategory", unless="#result == null")
    public List<LcdpResourceLockBean> selectResourceLockList(String resourceId, String resourceCategory) {
        return selectListByFilter(SearchFilter.instance()
                .match("RESOURCEID", resourceId).filter(MatchPattern.EQ)
                .match("RESOURCECATEGORY", resourceCategory).filter(MatchPattern.EQ));
    }

    @Override
    public String getLockStatus(String userId, String resourceId, String resourceCategory) {
        List<LcdpResourceLockBean> lockList = proxyInstance.selectResourceLockList(resourceId, resourceCategory);
        
        if (lockList.isEmpty()) {
            return "0";
        }
        
        if (lockList.stream().anyMatch(l -> Objects.equals(userId, l.getLockUserId()))) {
            return "1";
        }
        
        return "2";
    }
}

