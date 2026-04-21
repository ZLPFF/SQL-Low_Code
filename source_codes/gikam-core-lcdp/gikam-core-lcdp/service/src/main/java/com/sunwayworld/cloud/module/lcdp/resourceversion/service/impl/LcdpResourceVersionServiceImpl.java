package com.sunwayworld.cloud.module.lcdp.resourceversion.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sunwayworld.framework.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceHistoryService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resourceversion.bean.LcdpResourceVersionBean;
import com.sunwayworld.cloud.module.lcdp.resourceversion.persistent.dao.LcdpResourceVersionDao;
import com.sunwayworld.cloud.module.lcdp.resourceversion.service.LcdpResourceVersionService;
import com.sunwayworld.cloud.module.lcdp.submitlog.bean.LcdpSubmitLogBean;
import com.sunwayworld.cloud.module.lcdp.submitlog.service.LcdpSubmitLogService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.database.context.instance.EntityHelper;
import com.sunwayworld.framework.database.sql.Order;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpResourceVersionServiceImpl implements LcdpResourceVersionService {
    @Autowired
    private LcdpResourceVersionDao resourceVersionDao;
    
    @Autowired
    @Lazy
    private LcdpResourceService resourceService;
    @Autowired
    private LcdpResourceHistoryService resourceHistoryService;
    @Lazy
    @Autowired
    private LcdpSubmitLogService submitLogService;


    @Override
    @SuppressWarnings("unchecked")
    public LcdpResourceVersionDao getDao() {
        return resourceVersionDao;
    }


    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public void insertByHistoryResourceList(List<LcdpResourceHistoryBean> resourceHistoryList, LcdpSubmitLogBean submitLog) {
        if (resourceHistoryList.isEmpty()) {
            return;
        }
        List<LcdpResourceVersionBean> insertVersionList = resourceHistoryList.stream().map(history -> {
            LcdpResourceVersionBean resourceVersion = new LcdpResourceVersionBean();
            resourceVersion.setId(ApplicationContextHelper.getNextIdentity());
            resourceVersion.setLogId(submitLog.getId());
            resourceVersion.setResourceId(history.getResourceId().toString());
            resourceVersion.setResourceName(history.getResourceName());
            resourceVersion.setResourceCategory(history.getResourceCategory());
            resourceVersion.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
            resourceVersion.setVersion(history.getVersion());
            resourceVersion.setCommit(submitLog.getCommit());
            resourceVersion.setEditTime(history.getCreatedTime());
            String[] path = history.getPath().split("\\.");
            resourceVersion.setCategoryName(path[0]);
            resourceVersion.setModuleName(path[1]);
            resourceVersion.setResourceAction(history.getVersion() == 1 ? LcdpConstant.RESOURCE_SUBMIT_ACTION_NEW : LcdpConstant.RESOURCE_SUBMIT_ACTION_UPDATE);
            resourceVersion.setResourcePath(history.getPath().replaceAll("\\.", "/"));
            EntityHelper.assignCreatedElement(resourceVersion);
            return resourceVersion;
        }).collect(Collectors.toList());
        getDao().insert(insertVersionList);
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public void insertByTableList(List<LcdpTableBean> tableList, LcdpSubmitLogBean submitLog) {
        if (tableList.isEmpty()) {
            return;
        }
        List<LcdpResourceVersionBean> insertVersionList = tableList.stream().map(table -> {
            LcdpResourceVersionBean resourceVersion = new LcdpResourceVersionBean();
            resourceVersion.setId(ApplicationContextHelper.getNextIdentity());
            resourceVersion.setLogId(submitLog.getId());
            resourceVersion.setResourceId(table.getTableName());
            resourceVersion.setResourceName(table.getTableName());
            resourceVersion.setResourcePath(table.getTableName());
            resourceVersion.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_TABLE);
            resourceVersion.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
            resourceVersion.setVersion(table.getVersion());
            resourceVersion.setCommit(submitLog.getCommit());
            resourceVersion.setEditTime(table.getCreatedTime());
            resourceVersion.setResourcePath(table.getTableName());
            EntityHelper.assignCreatedElement(resourceVersion);
            return resourceVersion;
        }).collect(Collectors.toList());
        getDao().insert(insertVersionList);
    }

    @Override
    @Transactional
    @Audit(AuditConstant.SAVE)
    public void dealHistoryData(RestJsonWrapperBean wrapper) {
        List<LcdpSubmitLogBean> submitLogList = new ArrayList<>();
        List<LcdpResourceVersionBean> historyVersionList = selectListByFilter(SearchFilter.instance().match("LogId", null).filter(MatchPattern.EQ));
        List<Long> resourceIdList = historyVersionList.stream().filter(version -> LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(version.getResourceCategory())).map(LcdpResourceVersionBean::getResourceId).map(Long::valueOf).distinct().collect(Collectors.toList());
        List<LcdpResourceBean> resourceList = resourceService.selectListByIds(resourceIdList);
        Map<Long, LcdpResourceBean> resourceMap = resourceList.stream().collect(Collectors.toMap(LcdpResourceBean::getId, Function.identity()));
        historyVersionList.forEach(version -> {
            if (version.getResourceCategory().equals(LcdpConstant.RESOURCE_CATEGORY_TABLE)) {
                version.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_TABLE);
                version.setResourcePath(LcdpConstant.RESOURCE_CATEGORY_TABLE);
            } else {
                LcdpResourceBean resource = resourceMap.get(Long.valueOf(version.getResourceId()));
                
                if (resource == null) {
                    return;
                }
                
                
                version.setResourceDeleteFlag(resource.getDeleteFlag());
                version.setResourcePath(resource.getPath().replaceAll("\\.", "/"));
                version.setResourceCategory(resource.getResourceCategory());
            }
            version.setLogId(ApplicationContextHelper.getNextIdentity());

            LcdpSubmitLogBean submitLog = new LcdpSubmitLogBean();
            submitLog.setId(version.getLogId());
            submitLog.setCommit(version.getCommit());
            submitLog.setCreatedByOrgName("sourceDealData");
            submitLogList.add(submitLog);
        });
        getDao().update(historyVersionList, "RESOURCEDELETEFLAG", "RESOURCEPATH", "RESOURCECATEGORY", "LOGID");
        submitLogService.getDao().insert(submitLogList);


    }

    @Override
    public void insertByDeleteResourceList(List<LcdpResourceHistoryBean> toDeleteHistoryList, LcdpSubmitLogBean submitLog) {
        if (toDeleteHistoryList.isEmpty()) {
            return;
        }
        List<LcdpResourceVersionBean> insertVersionList = toDeleteHistoryList.stream().filter(history -> StringUtils.equals(history.getEffectFlag(), LcdpConstant.EFFECT_FLAG_YES)).map(history -> {
            LcdpResourceVersionBean resourceVersion = new LcdpResourceVersionBean();
            resourceVersion.setId(ApplicationContextHelper.getNextIdentity());
            resourceVersion.setLogId(submitLog.getId());
            resourceVersion.setResourceId(history.getResourceId().toString());
            resourceVersion.setResourceName(history.getResourceName());
            resourceVersion.setResourceCategory(history.getResourceCategory());
            resourceVersion.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
            resourceVersion.setResourceDeleteFlag(LcdpConstant.RESOURCE_DELETED_YES);
            resourceVersion.setVersion(history.getVersion());
            resourceVersion.setCommit(submitLog.getCommit());
            resourceVersion.setEditTime(history.getCreatedTime());
            String[] path = history.getPath().split("\\.");
            resourceVersion.setCategoryName(path[0]);
            resourceVersion.setModuleName(path[1]);
            resourceVersion.setResourceAction(LcdpConstant.RESOURCE_SUBMIT_ACTION_DELETE);
            resourceVersion.setResourcePath(history.getPath().replaceAll("\\.", "/"));
            EntityHelper.assignCreatedElement(resourceVersion);
            return resourceVersion;
        }).collect(Collectors.toList());
        getDao().insert(insertVersionList);
    }


    @Override
    public Page<LcdpResourceVersionBean> selectPagination(RestJsonWrapperBean wrapper) {
        Page<LcdpResourceVersionBean> resourceVersionPage = LcdpResourceVersionService.super.selectPagination(wrapper);
        List<LcdpResourceVersionBean> resourceVersionList = resourceVersionPage.getRows();
        String resourceId = wrapper.getFilterValue("resourceId_EQ");
        List<LcdpResourceHistoryBean> resourceHistoryList = resourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", resourceId).filter(MatchPattern.EQ).match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.EQ), Order.desc("VERSION"));
        if (!resourceHistoryList.isEmpty()) {
            LcdpResourceHistoryBean resourceHistory = resourceHistoryList.get(0);
            LcdpResourceVersionBean version = new LcdpResourceVersionBean();
            version.setResourceName(resourceHistory.getResourceName());
            version.setResourceId(resourceHistory.getResourceId().toString());
            version.setVersion(resourceHistory.getVersion());
            version.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);
            version.setCreatedById(resourceHistory.getCreatedById());
            version.setCreatedByName(resourceHistory.getCreatedByName());
            version.setEditTime(resourceHistory.getCreatedTime());
            resourceVersionList.add(0, version);
        }
        resourceVersionPage.setRows(resourceVersionList);
        return resourceVersionPage;
    }
}
