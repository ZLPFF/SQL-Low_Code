package com.sunwayworld.cloud.module.lcdp.resource.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceCheckoutRecordBean;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.LcdpResourceCheckoutRecordDao;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceCheckoutRecordService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resourcelock.bean.LcdpResourceLockBean;
import com.sunwayworld.cloud.module.lcdp.resourcelock.service.LcdpResourceLockService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpViewService;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.mdm.user.bean.CoreUserBean;
import com.sunwayworld.module.mdm.user.service.CoreUserService;
import com.sunwayworld.module.sys.constant.bean.CoreConstantBean;
import com.sunwayworld.module.sys.constant.service.CoreConstantService;

@Repository
@GikamBean
public class LcdpResourceCheckoutRecordServiceImpl implements LcdpResourceCheckoutRecordService {

    @Autowired
    private LcdpResourceCheckoutRecordDao lcdpResourceCheckoutRecordDao;

    @Autowired
    @Lazy
    private LcdpResourceService resourceService;

    @Autowired
    @Lazy
    private LcdpResourceLockService resourceLockService;

    @Autowired
    @Lazy
    private LcdpTableService tableService;

    @Autowired
    @Lazy
    private LcdpViewService viewService;

    @Autowired
    private CoreConstantService coreConstantService;

    @Autowired
    private CoreUserService coreUserService;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpResourceCheckoutRecordDao getDao() {
        return lcdpResourceCheckoutRecordDao;
    }


    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public void checkoutResource(List<LcdpResourceBean> resourceList) {
        if (resourceList.isEmpty()) {
            return;
        }
        
        List<LcdpResourceBean> resourceCopyList = new ArrayList<>(resourceList);
        
        List<LcdpResourceCheckoutRecordBean> existCategoryModuleList = selectListByFilter(SearchFilter.instance()
                .match("CHECKOUTUSERID", LocalContextHelper.getLoginUserId()).filter(MatchPattern.EQ)
                .match("RESOURCECATEGORY", LcdpConstant.CATEGORY_MODULE_CATEGORY_LIST).filter(MatchPattern.OR));

        List<Long> moduleIdList = resourceCopyList.stream().map(LcdpResourceBean::getParentId).collect(Collectors.toList());
        List<LcdpResourceCheckoutRecordBean> checkoutModuleList =
                existCategoryModuleList.stream().filter(rc -> LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(rc.getResourceCategory())).collect(Collectors.toList());
        List<Long> exitsModuleIdList = checkoutModuleList.stream().map(LcdpResourceCheckoutRecordBean::getResourceId).collect(Collectors.toList());

        List<LcdpResourceCheckoutRecordBean> checkoutCategoryList =
                existCategoryModuleList.stream().filter(rc -> LcdpConstant.RESOURCE_CATEGORY_CATEGORY.equals(rc.getResourceCategory())).collect(Collectors.toList());
        List<Long> exitsCategoryIdList = checkoutCategoryList.stream().map(LcdpResourceCheckoutRecordBean::getResourceId).collect(Collectors.toList());
        
        moduleIdList.removeAll(exitsModuleIdList);
        
        List<LcdpResourceBean> moduleList = resourceService.getDao().selectListByIds(moduleIdList,
                Arrays.asList("ID", "RESOURCENAME", "RESOURCEDESC", "RESOURCECATEGORY", "PARENTID"));

        List<Long> categoryIdList = moduleList.stream().map(LcdpResourceBean::getParentId).collect(Collectors.toList());
        categoryIdList.removeAll(exitsCategoryIdList);
        List<LcdpResourceBean> categoryList = resourceService.getDao().selectListByIds(categoryIdList,
                Arrays.asList("ID", "RESOURCENAME", "RESOURCEDESC", "RESOURCECATEGORY", "PARENTID"));

        resourceCopyList.addAll(moduleList);
        resourceCopyList.addAll(categoryList);
        
        List<LcdpResourceCheckoutRecordBean> resourceCheckoutRecordList = resourceCopyList.stream().map(resource -> {
            LcdpResourceCheckoutRecordBean resourceCheckoutRecord = new LcdpResourceCheckoutRecordBean();
            BeanUtils.copyProperties(resource, resourceCheckoutRecord);
            resourceCheckoutRecord.setId(ApplicationContextHelper.getNextIdentity());
            resourceCheckoutRecord.setResourceId(resource.getId());
            resourceCheckoutRecord.setCheckoutTime(LocalDateTime.now());
            resourceCheckoutRecord.setCheckoutUserName(LocalContextHelper.getLoginUserName());
            resourceCheckoutRecord.setCheckoutUserId(LocalContextHelper.getLoginUserId());
            return resourceCheckoutRecord;
        }).collect(Collectors.toList());
        getDao().fastInsert(resourceCheckoutRecordList);
        
        LocalDateTime now = LocalDateTime.now();
        // 更新检出人编码
        resourceCopyList.forEach(r -> {
            r.setCheckoutUserId(LocalContextHelper.getLoginUserId());
            r.setCheckoutTime(now);
        });
        resourceService.getDao().update(resourceCopyList, "CHECKOUTUSERID", "CHECKOUTTIME");
    }

    @Override
    @Transactional
    @Audit(AuditConstant.DELETE)
    public void removeCheckout(List<LcdpResourceBean> resourceList) {
        List<LcdpResourceBean> uncheckoutResourceList = new ArrayList<>(resourceList);
        
        // 如果有分类，查询分类下的所有子项
        List<LcdpResourceBean> categoryResourceList = resourceList.stream()
                .filter(r -> LcdpConstant.RESOURCE_CATEGORY_CATEGORY.equals(r.getResourceCategory()))
                .collect(Collectors.toList());
        if (!categoryResourceList.isEmpty()) {
            List<LcdpResourceBean> moduleResourceList = resourceService.getDao().selectListByOneColumnValues(categoryResourceList
                    .stream().map(LcdpResourceBean::getId).collect(Collectors.toList()),
                    "PARENTID", Arrays.asList("ID", "PARENTID", "RESOURCECATEGORY"));
            
            List<LcdpResourceBean> scriptResourceList = resourceService.getDao().selectListByOneColumnValues(categoryResourceList
                    .stream().map(LcdpResourceBean::getId).collect(Collectors.toList()),
                    "CATEGORYID", Arrays.asList("ID", "PARENTID", "RESOURCECATEGORY"));
            
            moduleResourceList.forEach(m -> {
                if (uncheckoutResourceList.stream().noneMatch(r -> r.getId().equals(m.getId()))) {
                    uncheckoutResourceList.add(m);
                }
            });
            
            scriptResourceList.forEach(s -> {
                if (uncheckoutResourceList.stream().noneMatch(r -> r.getId().equals(s.getId()))) {
                    uncheckoutResourceList.add(s);
                }
            });
        }
        // 如果有模块，查询模块下的所有子项
        List<LcdpResourceBean> moduleResourceList = resourceList.stream()
                .filter(r -> Objects.equals(r.getResourceCategory(), LcdpConstant.RESOURCE_CATEGORY_MODULE)
                        && categoryResourceList.stream().noneMatch(c -> c.getId().equals(r.getParentId())))
                .collect(Collectors.toList());
        if (!moduleResourceList.isEmpty()) {
            List<LcdpResourceBean> scriptResourceList = resourceService.getDao().selectListByOneColumnValues(moduleResourceList
                    .stream().map(LcdpResourceBean::getId).collect(Collectors.toList()),
                    "MODULEID", Arrays.asList("ID", "PARENTID", "RESOURCECATEGORY"));
            
            scriptResourceList.forEach(s -> {
                if (uncheckoutResourceList.stream().noneMatch(r -> r.getId().equals(s.getId()))) {
                    uncheckoutResourceList.add(s);
                }
            });
        }
        
        // 自己检出的记录
        SearchFilter searchFilter = SearchFilter.instance()
                .match("RESOURCEID", uncheckoutResourceList.stream()
                        .map(LcdpResourceBean::getId).collect(Collectors.toList())).filter(MatchPattern.OR);
        if (!StringUtils.equals(LcdpConstant.SUPER_ADMIN_ID, LocalContextHelper.getLoginUserId())
                && !LocalContextHelper.isAdminLogin()) {
            searchFilter.match("CHECKOUTUSERID", LocalContextHelper.getLoginUserId()).filter(MatchPattern.SEQ);
        }
        List<LcdpResourceCheckoutRecordBean> checkoutRecordList = selectListByFilter(searchFilter);
        
        getDao().deleteBy(checkoutRecordList);
        
        // 更新检出人编码
        uncheckoutResourceList.forEach(r -> {
            r.setCheckoutUserId(null);
            r.setCheckoutTime(null);
        });
        resourceService.getDao().update(uncheckoutResourceList, "CHECKOUTUSERID", "CHECKOUTTIME");
    }


    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public void checkoutTableOrView(String name, String desc, String category) {
        LcdpResourceCheckoutRecordBean resourceCheckoutRecord = new LcdpResourceCheckoutRecordBean();
        resourceCheckoutRecord.setId(ApplicationContextHelper.getNextIdentity());
        resourceCheckoutRecord.setResourceCategory(category);
        resourceCheckoutRecord.setTableName(name);
        resourceCheckoutRecord.setResourceDesc(desc);
        resourceCheckoutRecord.setCheckoutUserId(LocalContextHelper.getLoginUserId());
        resourceCheckoutRecord.setCheckoutUserName(LocalContextHelper.getLoginUserName());
        resourceCheckoutRecord.setCheckoutTime(LocalDateTime.now());
        getDao().insert(resourceCheckoutRecord);
    }

    @Override
    @Transactional
    @Audit(AuditConstant.DELETE)
    public void removeCheckoutTableOrView(List<String> nameList) {
        List<LcdpResourceCheckoutRecordBean> checkoutRecordList = selectListByFilter(SearchFilter.instance().match("tableName", nameList).filter(MatchPattern.OR));
        List<Long> deleteIdList = checkoutRecordList.stream().map(LcdpResourceCheckoutRecordBean::getId).collect(Collectors.toList());
        getDao().deleteByIdList(deleteIdList);
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public void dealResourceCheckoutRecord() {
        CoreConstantBean queryCoreConstant = coreConstantService.selectByIdIfPresent("LCDP_RS_CHECKOUT");
        if (queryCoreConstant != null) {
            return;
        }
        List<LcdpResourceCheckoutRecordBean> allCheckoutRecordList = new ArrayList<>();
        List<LcdpResourceLockBean> lockList = resourceLockService.selectListByFilter(SearchFilter.instance().match("lockUserId", null).filter(MatchPattern.DIFFER));
        List<String> checkUserIdList = lockList.stream().map(LcdpResourceLockBean::getLockUserId).collect(Collectors.toList());
        checkUserIdList = checkUserIdList.stream().distinct().collect(Collectors.toList());
        List<CoreUserBean> coreUserList = coreUserService.selectListByIds(checkUserIdList);
        Map<String, String> userId2UserNameMap = coreUserList.stream().collect(Collectors.toMap(CoreUserBean::getId, CoreUserBean::getUserName));
        dealCheckoutResouce(allCheckoutRecordList, lockList, userId2UserNameMap);

        List<LcdpResourceLockBean> checkoutTableList = lockList.stream().filter(lock -> LcdpConstant.RESOURCE_CATEGORY_TABLE.equals(lock.getResourceCategory())).collect(Collectors.toList());
        List<String> tableNameList = checkoutTableList.stream().map(LcdpResourceLockBean::getResourceId).collect(Collectors.toList());
        List<LcdpTableBean> tableList = tableService.selectListByFilter(SearchFilter.instance().match("tableName", tableNameList).filter(MatchPattern.OR).match("submitFlag", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.EQ));
        tableList.forEach(table -> {
            LcdpResourceCheckoutRecordBean resourceCheckoutRecord = new LcdpResourceCheckoutRecordBean();
            resourceCheckoutRecord.setId(ApplicationContextHelper.getNextIdentity());
            resourceCheckoutRecord.setTableName(table.getTableName());
            resourceCheckoutRecord.setTableDesc(table.getTableDesc());
            resourceCheckoutRecord.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_TABLE);
            resourceCheckoutRecord.setCheckoutTime(table.getCreatedTime());
            resourceCheckoutRecord.setCheckoutUserName(table.getCreatedByName());
            resourceCheckoutRecord.setCheckoutUserId(table.getCreatedById());
            allCheckoutRecordList.add(resourceCheckoutRecord);
        });

        List<LcdpResourceLockBean> checkoutDBViewList = lockList.stream().filter(lock -> LcdpConstant.RESOURCE_CATEGORY_DB_VIEW.equals(lock.getResourceCategory())).collect(Collectors.toList());
        List<String> viewNameList = checkoutDBViewList.stream().map(LcdpResourceLockBean::getResourceId).collect(Collectors.toList());
        List<LcdpViewBean> viewList = viewService.selectListByFilter(SearchFilter.instance().match("viewName", viewNameList).filter(MatchPattern.OR).match("submitFlag", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.EQ));
        viewList.forEach(view -> {
            LcdpResourceCheckoutRecordBean resourceCheckoutRecord = new LcdpResourceCheckoutRecordBean();
            resourceCheckoutRecord.setId(ApplicationContextHelper.getNextIdentity());
            resourceCheckoutRecord.setTableName(view.getViewName());
            resourceCheckoutRecord.setTableDesc(view.getViewName());
            resourceCheckoutRecord.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_DB_VIEW);
            resourceCheckoutRecord.setCheckoutTime(view.getCreatedTime());
            resourceCheckoutRecord.setCheckoutUserName(view.getCreatedByName());
            resourceCheckoutRecord.setCheckoutUserId(view.getCreatedById());
            allCheckoutRecordList.add(resourceCheckoutRecord);
        });
        getDao().insert(allCheckoutRecordList);
        CoreConstantBean coreConstant = new CoreConstantBean();
        coreConstant.setId("LCDP_RS_CHECKOUT");
        coreConstant.setConstantValue("1");
        coreConstant.setConstantDesc("低代码平台检出数据处理");
        coreConstant.setClassfication("系统");
        coreConstantService.getDao().insert(coreConstant);
    }

    private void dealCheckoutResouce(List<LcdpResourceCheckoutRecordBean> allCheckoutRecordList, List<LcdpResourceLockBean> lockList, Map<String, String> userId2UserNameMap) {
        List<LcdpResourceLockBean> pageAndScriptLockTempList = lockList.stream().filter(lock -> LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(lock.getResourceCategory())).collect(Collectors.toList());
        List<Long> pageAndScripIdList = pageAndScriptLockTempList.stream().map(LcdpResourceLockBean::getResourceId).map(Long::valueOf).collect(Collectors.toList());
        List<LcdpResourceBean> pageAndScriptList = resourceService.selectListByFilter(SearchFilter.instance().match("id", pageAndScripIdList).filter(MatchPattern.OR).match("deleteFlag", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));
        if (pageAndScriptList.isEmpty()) return;
        List<Long> resourceIdList = pageAndScriptList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
        List<Long> moduleIdList = pageAndScriptList.stream().map(LcdpResourceBean::getParentId).collect(Collectors.toList());
        List<LcdpResourceBean> moduleList = resourceService.selectListByIds(moduleIdList);
        List<Long> categoryIdList = moduleList.stream().map(LcdpResourceBean::getParentId).collect(Collectors.toList());
        List<LcdpResourceBean> categoryList = resourceService.selectListByIds(categoryIdList);
        Map<String, List<LcdpResourceLockBean>> checkUser2LockListMap = pageAndScriptLockTempList.stream().filter(lock -> resourceIdList.contains(Long.valueOf(lock.getResourceId()).longValue())).collect(Collectors.groupingBy(LcdpResourceLockBean::getLockUserId));

        checkUser2LockListMap.forEach((checkUser, LockList) -> {
            List<LcdpResourceBean> checkUserCheckoutList = new ArrayList<>();
            List<String> checkUserCheckoutIdList = LockList.stream().map(LcdpResourceLockBean::getResourceId).collect(Collectors.toList());
            List<LcdpResourceBean> checkUserCheckResourceList = pageAndScriptList.stream().filter(r -> checkUserCheckoutIdList.contains(r.getId().toString())).collect(Collectors.toList());
            checkUserCheckoutList.addAll(checkUserCheckResourceList);

            List<Long> checkUserCheckModuleIdList = checkUserCheckResourceList.stream().map(LcdpResourceBean::getParentId).collect(Collectors.toList());
            List<LcdpResourceBean> checkUserCheckModuleList = moduleList.stream().filter(m -> checkUserCheckModuleIdList.contains(m.getId().longValue())).collect(Collectors.toList());
            checkUserCheckModuleList = checkUserCheckModuleList.stream().collect(
                    Collectors.collectingAndThen(
                            Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(a -> a.getId()))),
                            ArrayList::new
                    )
            );
            checkUserCheckoutList.addAll(checkUserCheckModuleList);

            List<Long> checkUserCheckCategoryIdList = checkUserCheckModuleList.stream().map(LcdpResourceBean::getParentId).collect(Collectors.toList());
            List<LcdpResourceBean> checkUserCheckCategoryList = categoryList.stream().filter(m -> checkUserCheckCategoryIdList.contains(m.getId().longValue())).collect(Collectors.toList());
            checkUserCheckCategoryList = checkUserCheckCategoryList.stream().collect(
                    Collectors.collectingAndThen(
                            Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(a -> a.getId()))),
                            ArrayList::new
                    )
            );
            checkUserCheckoutList.addAll(checkUserCheckCategoryList);

            List<LcdpResourceCheckoutRecordBean> checkoutRecordList = checkUserCheckoutList.stream().map(resource -> {
                LcdpResourceCheckoutRecordBean resourceCheckoutRecord = new LcdpResourceCheckoutRecordBean();
                resourceCheckoutRecord.setId(ApplicationContextHelper.getNextIdentity());
                resourceCheckoutRecord.setResourceId(resource.getId());
                resourceCheckoutRecord.setResourceDesc(resource.getResourceDesc());
                resourceCheckoutRecord.setResourceName(resource.getResourceName());
                resourceCheckoutRecord.setResourceCategory(resource.getResourceCategory());
                resourceCheckoutRecord.setCheckoutUserId(checkUser);
                resourceCheckoutRecord.setCheckoutUserName(userId2UserNameMap.get(checkUser));
                resourceCheckoutRecord.setParentId(resource.getParentId());
                return resourceCheckoutRecord;
            }).collect(Collectors.toList());
            allCheckoutRecordList.addAll(checkoutRecordList);
        });
    }
}
