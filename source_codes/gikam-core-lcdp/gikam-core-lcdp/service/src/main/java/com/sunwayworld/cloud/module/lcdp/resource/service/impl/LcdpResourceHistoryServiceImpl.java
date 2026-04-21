package com.sunwayworld.cloud.module.lcdp.resource.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryDevParamDTO;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.LcdpResourceHistoryDao;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceHistoryService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpJavaCodeResolverUtils;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.database.sql.Order;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpResourceHistoryServiceImpl implements LcdpResourceHistoryService {

    @Autowired
    private LcdpResourceHistoryDao lcdpResourceHistoryDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpResourceHistoryDao getDao() {
        return lcdpResourceHistoryDao;
    }

    @Override
    @Cacheable(value = "T_LCDP_RESOURCE_HISTORY.DEV_SCRIPT", key = "#scriptType", unless="#result == null")
    public List<LcdpResourceHistoryBean> selectDevScriptList(String scriptType) {
        return selectListByFilter(SearchFilter.instance()
                .match("SCRIPTTYPE", scriptType).filter(MatchPattern.EQ)
                .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ)
                .match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.EQ));
    }
    
    @Override
    @Cacheable(value = "T_LCDP_RESOURCE_HISTORY.BY_RESOURCEID", key = "'' + #resourceId", unless="#result == null")
    public List<LcdpResourceHistoryBean> selectListByResourceId(Long resourceId) {
        LcdpResourceHistoryBean historyCondition = new LcdpResourceHistoryBean();
        historyCondition.setResourceId(resourceId);
        
        return getDao().selectListByOneColumnValue(resourceId, "RESOURCEID", Order.desc("ID"));
    }
    
    @Override
    public List<String> selectDevResourceNameList(List<LcdpResourceHistoryDevParamDTO> devParamList) {
        return getDao().selectDevResourceNameList(devParamList);
    }

    @Override
    @Cacheable(value = "T_LCDP_RESOURCE_HISTORY.EXISTS", key = "#path", unless="#result == null")
    public String isExists(String path) {
        LcdpResourceHistoryBean filter = new LcdpResourceHistoryBean();
        filter.setPath(path);
        
        if (getDao().countBy(filter) > 0) {
            return Constant.YES;
        }
        
        return Constant.NO;
    }
    
    @Override
    @Cacheable(value = "T_LCDP_RESOURCE_HISTORY.ACTIVE", key = "#path", unless="#result == null")
    public String isActive(String path) {
        LcdpResourceHistoryBean filter = new LcdpResourceHistoryBean();
        filter.setPath(path);
        
        LcdpResourceHistoryBean latestHistory = getDao().selectFirst(filter, Order.desc("ID"));
        
        // 最新的历史记录未删除
        if (LcdpConstant.RESOURCE_DELETED_YES.equals(latestHistory.getDeleteFlag())) {
            return Constant.NO;
        }
        
        return Constant.YES;
    }

    @Override
    @Transactional
    public void compile(Long id) {
        LcdpResourceHistoryBean history = getDao().selectById(id);
        
        // 编译版本和变更版本一致，更新编译版本
        if (!Objects.equals(history.getCompiledVersion(), history.getModifyVersion())) {
            history.setCompiledVersion(history.getModifyVersion());
            
            getDao().update(history, "COMPILEDVERSION");
        }
        
        LcdpJavaCodeResolverUtils.loadSourceCode(history);
    }

    @Override
    @Transactional
    public void updateCompiledVersionIfNecessary(List<Long> idList) {
        if (idList.isEmpty()) {
            return;
        }
        
        List<LcdpResourceHistoryBean> historyList = getDao().selectListByIds(idList, Arrays.asList("ID", "COMPILEDVERSION", "MODIFYVERSION"))
                .stream().filter(h -> !Objects.equals(h.getCompiledVersion(), h.getModifyVersion())).collect(Collectors.toList());
        
        if (!historyList.isEmpty()) {
            historyList.forEach(h -> h.setCompiledVersion(h.getModifyVersion()));
            
            getDao().update(historyList, "COMPILEDVERSION");
        }
    }

    @Override
    @Cacheable(value = "T_LCDP_RESOURCE_HISTORY.USER_UNSUBMITTED_BY_RESOURCEID", key = "#userId + '$' + #resourceId", unless="#result == null")
    public LcdpResourceHistoryBean selectUnsubmittedResourceHistory(String userId, Long resourceId) {
        LcdpResourceHistoryBean filter = new LcdpResourceHistoryBean();
        filter.setResourceId(resourceId);
        if (!LcdpConstant.SUPER_ADMIN_ID.equals(LocalContextHelper.getLoginUserId())) {
            filter.setCreatedById(LocalContextHelper.getLoginUserId());
        }
        filter.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_NO);
        
        return getDao().selectFirstIfPresent(filter);
    }

    @Override
    public List<LcdpResourceHistoryBean> selectLatestActivatedListByResourceIdList(List<Long> resourceIdList) {
        return getDao().selectLatestActivatedListByResourceIdList(resourceIdList);
    }

    @Override
    public List<LcdpResourceHistoryBean> selectMaxVersionListByResourceIdList(List<Long> resourceIdList) {
        return getDao().selectMaxVersionListByResourceIdList(resourceIdList);
    }
}
