package com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryDevParamDTO;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.LcdpResourceHistoryDao;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.mapper.LcdpResourceHistoryMapper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.PersistableHelper;
import com.sunwayworld.framework.utils.TransactionUtils;

@Repository
@GikamBean
public class LcdpResourceHistoryDaoImpl extends MybatisDaoSupport<LcdpResourceHistoryBean, Long> implements LcdpResourceHistoryDao {

    @Autowired
    private LcdpResourceHistoryMapper lcdpResourceHistoryMapper;
    @Autowired
    private CacheManager cacheManager;
    
    @Override
    public LcdpResourceHistoryMapper getMapper() {
        return lcdpResourceHistoryMapper;
    }
    
    @Override
    public List<LcdpResourceHistoryBean> selectLatestActivatedListByResourceIdList(List<Long> resourceIdList) {
        return getMapper().selectLatestActivatedListByResourceIdList(resourceIdList).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceHistoryBean.class))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<LcdpResourceHistoryBean> selectMaxVersionListByResourceIdList(List<Long> resourceIdList) {
        return getMapper().selectMaxVersionListByResourceIdList(resourceIdList).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceHistoryBean.class))
                .collect(Collectors.toList());
    }
    
    @Override
    public void cacheEvict(LcdpResourceHistoryBean oldItem, LcdpResourceHistoryBean newItem) {
        String scriptType = (oldItem == null ? newItem.getScriptType() : oldItem.getScriptType());
        String path = (oldItem == null ? newItem.getPath() : oldItem.getPath());
        String resourceId = "" + (oldItem == null ? newItem.getResourceId() : oldItem.getResourceId());
        String createdById = oldItem == null ? newItem.getCreatedById() : oldItem.getCreatedById();
        
        cacheManager.getCache("T_LCDP_RESOURCE_HISTORY.DEV_SCRIPT").evict(scriptType);
        cacheManager.getCache("T_LCDP_RESOURCE_HISTORY.BY_RESOURCEID").evict(resourceId);
        cacheManager.getCache("T_LCDP_RESOURCE_HISTORY.EXISTS").evict(path);
        cacheManager.getCache("T_LCDP_RESOURCE_HISTORY.ACTIVE").evict(path);
        cacheManager.getCache("T_LCDP_RESOURCE_HISTORY.USER_UNSUBMITTED_BY_RESOURCEID").evict(createdById + "$" + resourceId);
        
        TransactionUtils.runAfterCompletion(i -> {
            cacheManager.getCache("T_LCDP_RESOURCE_HISTORY.DEV_SCRIPT").evict(scriptType);
            cacheManager.getCache("T_LCDP_RESOURCE_HISTORY.BY_RESOURCEID").evict(resourceId);
            cacheManager.getCache("T_LCDP_RESOURCE_HISTORY.EXISTS").evict(path);
            cacheManager.getCache("T_LCDP_RESOURCE_HISTORY.ACTIVE").evict(path);
            cacheManager.getCache("T_LCDP_RESOURCE_HISTORY.USER_UNSUBMITTED_BY_RESOURCEID").evict(createdById + "$" + resourceId);
        });
    }
    
    @Override
    public List<String> selectDevResourceNameList(List<LcdpResourceHistoryDevParamDTO> devParamList) {
        return getMapper().selectDevResourceNameList(LocalContextHelper.getLoginUserId(), devParamList);
    }
    
    @Override
    public String[] getForCacheEvictColumns() {
        return new String[] {"ID", "SCRIPTTYPE", "PATH", "RESOURCEID", "CREATEDBYID"};
    }
}
