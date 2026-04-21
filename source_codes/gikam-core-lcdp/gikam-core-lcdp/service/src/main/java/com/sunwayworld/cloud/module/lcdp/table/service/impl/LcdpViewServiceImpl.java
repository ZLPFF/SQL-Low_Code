package com.sunwayworld.cloud.module.lcdp.table.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.sunwayworld.cloud.module.lcdp.importrecord.bean.LcdpCheckImportDataDTO;
import com.sunwayworld.cloud.module.lcdp.importrecord.service.LcdpResourceImportRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpAnalysisResultDTO;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceCheckoutRecordService;
import com.sunwayworld.cloud.module.lcdp.resourcelock.bean.LcdpResourceLockBean;
import com.sunwayworld.cloud.module.lcdp.resourcelock.service.LcdpResourceLockService;
import com.sunwayworld.cloud.module.lcdp.resourceversion.bean.LcdpResourceVersionBean;
import com.sunwayworld.cloud.module.lcdp.resourceversion.service.LcdpResourceVersionService;
import com.sunwayworld.cloud.module.lcdp.submitlog.bean.LcdpSubmitLogBean;
import com.sunwayworld.cloud.module.lcdp.submitlog.service.LcdpSubmitLogService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableCompareDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.cloud.module.lcdp.table.persistent.dao.LcdpViewDao;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpDatabaseService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpViewService;
import com.sunwayworld.cloud.module.lcdp.table.validator.LcdpViewDeleteDataValidator;
import com.sunwayworld.cloud.module.lcdp.table.validator.LcdpViewSaveDataValidator;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.cache.redis.RedisHelper;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.database.core.DatabaseManager;
import com.sunwayworld.framework.database.sql.Order;
import com.sunwayworld.framework.exception.checked.CheckedException;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.ArrayUtils;
import com.sunwayworld.framework.utils.BeanUtils;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.JsonUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.framework.validator.data.annotation.ValidateDataWith;

@Repository
@GikamBean
public class LcdpViewServiceImpl implements LcdpViewService {

    @Autowired
    private LcdpViewDao lcdpViewDao;

    @Autowired
    private LcdpDatabaseService lcdpDatabaseService;

    @Autowired
    private LcdpResourceLockService lcdpResourceLockService;

    @Autowired
    @Lazy
    private LcdpResourceVersionService lcdpResourceVersionService;

    @Autowired
    private LcdpResourceCheckoutRecordService resourceCheckoutRecordService;

    @Autowired
    @Lazy
    private LcdpSubmitLogService submitLogService;


    @Autowired
    @Lazy
    private LcdpResourceImportRecordService resourceImportRecordService;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpViewDao getDao() {
        return lcdpViewDao;
    }

    @Override
    public List<LcdpViewBean> selectPhysicalViewInfoList(RestJsonWrapperBean jsonWrapper) {
        return lcdpDatabaseService.selectPhysicalViewInfoList(jsonWrapper.extractMapFilter());
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    @ValidateDataWith(LcdpViewSaveDataValidator.class)
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        LcdpViewBean lcdpView = jsonWrapper.parseUnique(LcdpViewBean.class);
        //锁定资源
        lcdpResourceLockService.lock(lcdpView.getViewName(), LcdpConstant.RESOURCE_CATEGORY_DB_VIEW);
        //填充默认值
        lcdpView.setId(ApplicationContextHelper.getNextIdentity());
        lcdpView.setTableName(lcdpView.getViewName());
        lcdpView.setSubmitFlag(LcdpConstant.EFFECT_FLAG_NO);
        lcdpView.setVersion(1L);

        getDao().insert(lcdpView);
        resourceCheckoutRecordService.checkoutTableOrView(lcdpView.getViewName(), lcdpView.getViewName(), LcdpConstant.RESOURCE_CATEGORY_DB_VIEW);
        return lcdpView.getId();
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long design(String viewName) {
        //是否存在正编辑版本
        LcdpViewBean maxVersionTable = selectFirstByFilter(SearchFilter.instance().match("TABLENAME", viewName).filter(MatchPattern.EQ), Order.desc("VERSION"));
        if (ObjectUtils.isEmpty(maxVersionTable) || LcdpConstant.EFFECT_FLAG_YES.equals(maxVersionTable.getSubmitFlag())) {
            throw new CheckedException("LCDP.MODULE.TABLES.TABLE.EXCEPTION.LOCKED");
        }
        return maxVersionTable.getId();
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public void checkout(String viewName) {
        //资源是否可用并锁定
        lcdpResourceLockService.lock(viewName, LcdpConstant.RESOURCE_CATEGORY_DB_VIEW);

        //最新版本表
        LcdpViewBean maxVersionView = selectFirstByFilter(SearchFilter.instance().match("VIEWNAME", viewName).filter(MatchPattern.SEQ), Order.desc("VERSION"));

        LcdpViewBean lcdpViewBean = lcdpDatabaseService.selectPhysicalViewInfo(viewName);

        LcdpViewBean newVersionView = new LcdpViewBean();
        if (maxVersionView == null) {
            newVersionView.setTableName(viewName);
            newVersionView.setViewName(viewName);

            newVersionView.setVersion(2L);
        } else {
            BeanUtils.copyProperties(maxVersionView, newVersionView);
            newVersionView.setVersion(Optional.ofNullable(maxVersionView.getVersion()).orElse(1l) + 1);
        }

        newVersionView.setId(ApplicationContextHelper.getNextIdentity());
        newVersionView.setSubmitFlag(LcdpConstant.EFFECT_FLAG_NO);
        newVersionView.setSelectStatement(lcdpViewBean.getSelectStatement());
        
        resourceCheckoutRecordService.checkoutTableOrView(viewName, newVersionView.getViewName(), LcdpConstant.RESOURCE_CATEGORY_DB_VIEW);

        getDao().insert(newVersionView);
        LcdpCheckImportDataDTO checkImportDataDTO = new LcdpCheckImportDataDTO();
        checkImportDataDTO.setViewList(Arrays.asList(newVersionView));
        checkImportDataDTO.setOperation("checkout");
        resourceImportRecordService.checkImportRecord(checkImportDataDTO);
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public void submit(List<LcdpViewBean> viewList) {
        if (viewList.isEmpty()) {
            return;
        }
        List<LcdpViewBean> physicalViewList = lcdpDatabaseService.selectPhysicalViewInfoList(viewList.stream().map(LcdpViewBean::getViewName).collect(Collectors.toList()));

        List<LcdpViewBean> submitViewList = selectListByFilter(SearchFilter.instance().match("ID", viewList.stream().map(LcdpViewBean::getId).collect(Collectors.toList())).filter(MatchPattern.OR));

        viewList.forEach(view -> {
            LcdpViewBean matchPhysicalView = physicalViewList.stream().filter(physicalView -> StringUtils.equalsIgnoreCase(view.getViewName(), physicalView.getViewName())).findFirst().orElse(null);

            LcdpViewBean matchSubmitView = submitViewList.stream().filter(submitView -> StringUtils.equalsIgnoreCase(view.getViewName(), submitView.getViewName())).findFirst().orElse(null);

            if (ObjectUtils.isEmpty(matchSubmitView)) {
                return;
            }

            //清除TableContext缓存
            RedisHelper.evict(DatabaseManager.TABLE_CONTEXT_CACHE_NAME, view.getViewName());
            RedisHelper.evict(DatabaseManager.ENTITY_CONTEXT_CACHE_NAME, view.getViewName());

            lcdpDatabaseService.testSelectStatement(view.getSelectStatement());

            if (ObjectUtils.isEmpty(matchPhysicalView)) {
                lcdpDatabaseService.createPhysicalView(matchSubmitView);
            } else {
                lcdpDatabaseService.alterPhysicalView(matchSubmitView);
            }

            view.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_YES);
        });
        resourceCheckoutRecordService.removeCheckoutTableOrView(viewList.stream().map(LcdpViewBean::getViewName).collect(Collectors.toList()));

        getDao().update(viewList, "SUBMITFLAG");
    }

    @Override
    @Transactional
    @Audit(AuditConstant.DELETE)
    public List<String> revert(List<String> viewNameList) {
        //最新未提交版本表，新建表无法撤销检出
        List<LcdpViewBean> revertableViewList = selectListByFilter(SearchFilter.instance().match("VIEWNAME", viewNameList).filter(MatchPattern.OR)
                .match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.SEQ)
                .match("CREATEDBYID", LocalContextHelper.getLoginUserId())
                .filter(MatchPattern.SEQ).match("VERSION", 1).filter(MatchPattern.DIFFER));
        getDao().deleteBy(revertableViewList);
        
        List<String> revertedViewNameList = revertableViewList.stream().map(LcdpViewBean::getViewName).collect(Collectors.toList());
        
        resourceCheckoutRecordService.removeCheckoutTableOrView(revertedViewNameList);

        return revertedViewNameList;
    }

    @Override
    public LcdpTableCompareDTO<LcdpViewBean> compare(RestJsonWrapperBean wrapper) {
        String tableName = wrapper.getParamValue("TABLENAME");
        Long version = Optional.ofNullable(wrapper.getParamValue("VERSION")).map(Long::valueOf).orElse(1l);

        LcdpTableCompareDTO<LcdpViewBean> differ = new LcdpTableCompareDTO<LcdpViewBean>();

        LcdpViewBean currentView = new LcdpViewBean();

        LcdpViewBean previousView = new LcdpViewBean();
        //查出全部历史版本表
        List<LcdpViewBean> historyViewList = selectListByFilter(SearchFilter.instance().match("VIEWNAME", tableName).filter(MatchPattern.SEQ).match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_YES).filter(MatchPattern.SEQ));
        //当前版本表
        LcdpViewBean currentViewBean = historyViewList.stream().filter(table -> version == table.getVersion()).findAny().orElse(null);
        //上一个版本表
        LcdpViewBean previousViewBean = historyViewList.stream().filter(table -> version == table.getVersion() + 1).findAny().orElse(null);
        BeanUtils.copyProperties(currentViewBean, currentView);
        BeanUtils.copyProperties(previousViewBean, previousView);

        differ.setHistoryTableList(historyViewList);
        differ.setCurrentTable(currentView);
        differ.setPreviousTable(previousView);

        return differ;
    }

    @Override
    public Map<String, LcdpAnalysisResultDTO> analysisViewInfo(List<String> viewNameList, Map<String, String> fileMap) {

        Map<String, LcdpAnalysisResultDTO> viewResult = new HashMap<>();

        viewNameList.forEach(viewName -> {

            LcdpAnalysisResultDTO lcdpAnalysisResultDTO = new LcdpAnalysisResultDTO();
            lcdpAnalysisResultDTO.setEnable(true);
            lcdpAnalysisResultDTO.setAnalysisResultList(CollectionUtils.emptyList());

            LcdpViewBean view = JsonUtils.parse(fileMap.get(viewName), LcdpViewBean.class);

            try {
                lcdpDatabaseService.testSelectStatement(view.getSelectStatement());
            } catch (CheckedException ex) {
                lcdpAnalysisResultDTO.setEnable(false);
                lcdpAnalysisResultDTO.getAnalysisResultList().add(ex.getMessage());
            }
            viewResult.put(viewName, lcdpAnalysisResultDTO);
        });

        return viewResult;
    }

    @Override
    public LcdpViewBean selectPhysicalViewInfo(String viewName) {
        LcdpViewBean view = lcdpDatabaseService.selectPhysicalViewInfo(viewName);
        
        if (StringUtils.isBlank(view.getViewName())) {
            return null;
        }
        
        LcdpViewBean latestView = selectFirstByFilter(SearchFilter.instance().match("TABLENAME", viewName)
                .filter(MatchPattern.EQ), Order.desc("VERSION"));
        
        if (latestView != null) {
            latestView.setSelectStatement(view.getSelectStatement());
            
            return latestView;
        }
        
        return view;
    }

    @Override
    @Audit(AuditConstant.SAVE)
    @Transactional
    @ValidateDataWith(LcdpViewSaveDataValidator.class)
    public void save(Long id, RestJsonWrapperBean wrapper) {
        LcdpViewService.super.save(id, wrapper);
    }

    @Override
    @Transactional
    @Audit(AuditConstant.DELETE)
    @ValidateDataWith(LcdpViewDeleteDataValidator.class)
    public void delete(RestJsonWrapperBean wrapper) {
        //待删除的表
        LcdpViewBean toDeleteView = wrapper.parseUnique(getDao().getType());

        LcdpViewBean queryView = selectFirstByFilter(SearchFilter.instance().match("VIEWNAME", toDeleteView.getViewName()).filter(MatchPattern.EQ).match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_YES).filter(MatchPattern.EQ));

        List<LcdpResourceLockBean> LockList = lcdpResourceLockService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", toDeleteView.getViewName()).filter(MatchPattern.SEQ).match("LOCKUSERID", LocalContextHelper.getLoginUserId()).filter(MatchPattern.SEQ));
        if (LockList.isEmpty()) {
            throw new CheckedException("LCDP.MODULE.RESOURCES.TIP.UNCHECKOUT_CANNOT_DELETE");
        }

        //删除该表的相关版本记录
        List<Long> tableIdList = getDao().selectIdList(toDeleteView, ArrayUtils.asList("VIEWNAME"));

        getDao().deleteByIdList(tableIdList);
        //删除资源版本信息
        LcdpResourceVersionBean lcdpResourceVersionBean = new LcdpResourceVersionBean();
        lcdpResourceVersionBean.setResourceId(toDeleteView.getViewName());

        lcdpResourceVersionService.getDao().deleteBy(lcdpResourceVersionBean, "RESOURCEID");

        //删除资源版本信息
        List<LcdpResourceVersionBean> versionList = lcdpResourceVersionService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", toDeleteView.getViewName()).filter(MatchPattern.OR));
        versionList.forEach(version -> {
            version.setResourceDeleteFlag(LcdpConstant.RESOURCE_DELETED_YES);
        });
        lcdpResourceVersionService.getDao().update(versionList, "RESOURCEDELETEFLAG");

        //表资源解锁
        lcdpResourceLockService.unLock(ArrayUtils.asList(toDeleteView.getViewName()));

        //清除TableContext缓存
        RedisHelper.evict(DatabaseManager.TABLE_CONTEXT_CACHE_NAME, toDeleteView.getViewName());
        RedisHelper.evict(DatabaseManager.ENTITY_CONTEXT_CACHE_NAME, toDeleteView.getViewName());

        //删除物理表与记录
        if (lcdpDatabaseService.isExistPhysicalView(toDeleteView.getViewName())) {
            lcdpDatabaseService.dropPhysicalView(toDeleteView);
        }

        if (queryView != null) {
            LcdpSubmitLogBean submitLog = new LcdpSubmitLogBean();
            submitLog.setId(ApplicationContextHelper.getNextIdentity());
            submitLog.setCommit(I18nHelper.getMessage("LCDP.RESOURCE.DELETED"));
            submitLogService.getDao().insert(submitLog);

            LcdpResourceVersionBean resourceVersion = new LcdpResourceVersionBean();
            resourceVersion.setId(ApplicationContextHelper.getNextIdentity());
            resourceVersion.setLogId(submitLog.getId());
            resourceVersion.setResourcePath(queryView.getViewName());
            resourceVersion.setResourceDeleteFlag(LcdpConstant.RESOURCE_DELETED_YES);
            resourceVersion.setResourceAction(LcdpConstant.RESOURCE_SUBMIT_ACTION_DELETE);
            resourceVersion.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_DB_VIEW);
            lcdpResourceVersionService.getDao().insert(resourceVersion);
        }
        resourceCheckoutRecordService.removeCheckoutTableOrView(ArrayUtils.asList(toDeleteView.getViewName()));

    }

    @Override
    public List<LcdpViewBean> selectLatestBriefList(MapperParameter parameter) {
        return getDao().selectLatestBriefList(parameter);
    }
}
