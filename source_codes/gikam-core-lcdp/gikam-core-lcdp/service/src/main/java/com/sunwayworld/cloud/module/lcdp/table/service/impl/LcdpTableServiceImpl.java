package com.sunwayworld.cloud.module.lcdp.table.service.impl;

import static com.sunwayworld.cloud.module.lcdp.table.helper.IndexType.primarykey;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sunwayworld.cloud.module.lcdp.importrecord.bean.LcdpCheckImportDataDTO;
import com.sunwayworld.cloud.module.lcdp.importrecord.service.LcdpResourceImportRecordService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.framework.tenant.TenantContext;
import com.sunwayworld.framework.tenant.TenantManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpDynamicPagination;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableCompareDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableIndexBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableViewDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.cloud.module.lcdp.table.helper.LcdpTableFunction;
import com.sunwayworld.cloud.module.lcdp.table.helper.LcdpTableUtils;
import com.sunwayworld.cloud.module.lcdp.table.helper.SqlGenerateHelper;
import com.sunwayworld.cloud.module.lcdp.table.persistent.dao.LcdpDatabaseDao;
import com.sunwayworld.cloud.module.lcdp.table.persistent.dao.LcdpTableDao;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpDatabaseService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableFieldService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableIndexService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpViewService;
import com.sunwayworld.cloud.module.lcdp.table.validator.LcdpTableDeleteDataValidator;
import com.sunwayworld.cloud.module.lcdp.table.validator.LcdpTableSaveDataValidator;
import com.sunwayworld.cloud.module.lcdp.table.validator.LcdpTableShieldUtils;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.cache.redis.RedisHelper;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.context.SunwayAopContext;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.database.context.ColumnContext;
import com.sunwayworld.framework.database.core.DatabaseManager;
import com.sunwayworld.framework.database.dialect.DialectRepository;
import com.sunwayworld.framework.database.sql.Order;
import com.sunwayworld.framework.exception.checked.CheckedException;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.exception.core.ApplicationWarningException;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.mybatis.mapper.GlobalMapper;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.tree.bean.AbstractTreeNode;
import com.sunwayworld.framework.utils.ArrayUtils;
import com.sunwayworld.framework.utils.BeanUtils;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.ExceptionUtils;
import com.sunwayworld.framework.utils.JsonUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.ServletUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.framework.utils.TransactionUtils;
import com.sunwayworld.framework.validator.data.annotation.ValidateDataWith;
import com.sunwayworld.module.sys.audit.bean.CoreAuditConfigBean;
import com.sunwayworld.module.sys.audit.bean.CoreAuditConfigColumnBean;
import com.sunwayworld.module.sys.audit.service.CoreAuditConfigColumnService;
import com.sunwayworld.module.sys.audit.service.CoreAuditConfigService;
import com.sunwayworld.module.sys.i18n.bean.CoreI18nBean;
import com.sunwayworld.module.sys.i18n.bean.CoreI18nMessageBean;
import com.sunwayworld.module.sys.i18n.service.CoreI18nMessageService;
import com.sunwayworld.module.sys.i18n.service.CoreI18nService;
import com.sunwayworld.module.sys.menu.bean.CoreMenuExtBean;
import com.sunwayworld.module.sys.menu.service.CoreMenuExtService;
import com.sunwayworld.module.sys.metadata.bean.CoreTableBean;
import com.sunwayworld.module.sys.metadata.bean.CoreTableHierarchyBean;
import com.sunwayworld.module.sys.metadata.dao.CoreTableHierarchyDao;
import com.sunwayworld.module.sys.metadata.service.CoreTableService;

@Repository
@GikamBean
public class LcdpTableServiceImpl implements LcdpTableService {

    @Value("${sunway.lcdp.table.auditable:false}")
    private Boolean auditable;// 初始化审计配置时是否开启审计跟踪

    @Autowired
    private LcdpTableDao lcdpTableDao;

    @Autowired
    private LcdpTableFieldService lcdpTableFieldService;

    @Autowired
    private LcdpTableIndexService lcdpTableIndexService;

    @Autowired
    private LcdpViewService lcdpViewService;

    @Autowired
    private LcdpDatabaseDao lcdpDatabaseDao;

    @Autowired
    private LcdpDatabaseService lcdpDatabaseService;

    @Autowired
    private SqlGenerateHelper sqlGenerateHelper;

    @Autowired
    private LcdpResourceLockService lcdpResourceLockService;

    @Autowired
    @Lazy
    private LcdpResourceVersionService lcdpResourceVersionService;

    @Autowired
    private CoreI18nService coreI18nService;

    @Autowired
    private CoreI18nMessageService coreI18nMessageService;

    @Autowired
    private CoreAuditConfigService coreAuditConfigService;

    @Autowired
    private CoreAuditConfigColumnService coreAuditConfigColumnService;

    @Autowired
    private CoreMenuExtService coreMenuExtService;

    @Autowired
    private CoreTableService coreTableService;

    @Autowired
    private CoreTableHierarchyDao coreTableHierarchyDao;

    @Autowired
    private LcdpResourceCheckoutRecordService resourceCheckoutRecordService;

    @Autowired
    @Lazy
    private LcdpSubmitLogService submitLogService;

    @Autowired
    private GlobalMapper globalMapper;

    @Autowired
    private LcdpTableShieldUtils lcdpTableShieldUtils;


    @Autowired
    private TenantManager tenantManager;

    @Autowired
    @Lazy
    private LcdpResourceImportRecordService resourceImportRecordService;

    private static final boolean designCenterTenantFlag = ApplicationContextHelper.getEnvironment().getProperty("sunway.design-center-tenant.enabled", Boolean.class, false)&&ApplicationContextHelper.isProfileActivated("tenant");



    @Override
    @SuppressWarnings("unchecked")
    public LcdpTableDao getDao() {
        return lcdpTableDao;
    }

    @Override
    public List<LcdpTableTreeNodeDTO> selectTableTree(RestJsonWrapperBean wrapper) {
        //表管理表、视图资源树
        List<LcdpTableTreeNodeDTO> lcdpTableTreeNodeDTOList = new ArrayList<>();

        //固定的TABLES父节点
        LcdpTableTreeNodeDTO tablesTreeNodeDTO = new LcdpTableTreeNodeDTO();
        tablesTreeNodeDTO.setId(LcdpConstant.RESOURCE_TABLE_TREE_ROOT_NAME);
        tablesTreeNodeDTO.setName(LcdpConstant.RESOURCE_TABLE_TREE_ROOT_NAME);
        tablesTreeNodeDTO.setType(LcdpConstant.RESOURCE_CATEGORY_FOLDER);

        //固定的VIEWS父节点
        LcdpTableTreeNodeDTO viewsTreeNodeDTO = new LcdpTableTreeNodeDTO();
        viewsTreeNodeDTO.setId(LcdpConstant.RESOURCE_VIEW_TREE_ROOT_NAME);
        viewsTreeNodeDTO.setName(LcdpConstant.RESOURCE_VIEW_TREE_ROOT_NAME);
        viewsTreeNodeDTO.setType(LcdpConstant.RESOURCE_CATEGORY_FOLDER);


        MapperParameter parameter = wrapper.extractMapFilter();

        //查询物理表
        List<LcdpTableBean> tableList = lcdpDatabaseService.selectPhysicalTableNameList(parameter)
                .stream().filter(table -> !lcdpTableShieldUtils.contains(String.valueOf(table.getTableName()))).collect(Collectors.toList());
        //查询物理视图
        List<LcdpViewBean> viewList = lcdpDatabaseService.selectPhysicalViewInfoList(parameter);

        //查出该用户新增的未创建的表
        String checkoutFlag = wrapper.getParamValue("checkoutFlag");
        List<LcdpTableBean> newTableList = new ArrayList<>();
        List<LcdpViewBean> newViewList = new ArrayList<>();
        if (StringUtils.isEmpty(checkoutFlag)) {
            parameter.setFilter(SearchFilter.instance().match("VERSION", 1).filter(MatchPattern.EQ).match("CREATEDBYID", LocalContextHelper.getLoginUserId()).filter(MatchPattern.SEQ).match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.SEQ));
            newTableList = selectList(parameter);
            newViewList = lcdpViewService.selectList(parameter);
        }


        //查询出锁定的表资源
        List<LcdpResourceLockBean> lockedResources = lcdpResourceLockService.selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", ArrayUtils.asList(LcdpConstant.RESOURCE_CATEGORY_TABLE, LcdpConstant.RESOURCE_CATEGORY_DB_VIEW))
                .filter(MatchPattern.OR).match("LOCKUSERID", null).filter(MatchPattern.DIFFER)
                .match("RESOURCEID", null).filter(MatchPattern.DIFFER));
        //表锁定状态映射
        Map<String, String> tableLockStatus = lockedResources.stream().collect(Collectors.toMap(LcdpResourceLockBean::getResourceId, LcdpResourceLockBean::getLockUserId, (v1, v2) -> v1));

        //生成表树并设置表的状态
        List<AbstractTreeNode> tableTreeNodeDTOList = new ArrayList<>();
        //新建表节点
        newTableList.forEach(table -> {

            LcdpTableTreeNodeDTO tableTreeNodeDTO = createTableTreeNodeDTO(table.getTableName(),
                    LcdpConstant.RESOURCE_CATEGORY_TABLE, LcdpConstant.RESOURCE_TABLE_TREE_ROOT_NAME,
                    CollectionUtils.containsIgnoreCase(tableLockStatus.keySet(), table.getTableName()) && ObjectUtils.equals(CollectionUtils.getValueIgnorecase(tableLockStatus, table.getTableName()), LocalContextHelper.getLoginUserId()) ? LcdpConstant.RESOURCE_STATUS_CHECKOUT : LcdpConstant.RESOURCE_STATUS_CHECKIN,
                    CollectionUtils.containsIgnoreCase(tableLockStatus.keySet(), table.getTableName()) && !ObjectUtils.equals(CollectionUtils.getValueIgnorecase(tableLockStatus, table.getTableName()), LocalContextHelper.getLoginUserId()) ? LcdpConstant.RESOURCE_STATUS_CHECKOUT : LcdpConstant.RESOURCE_STATUS_CHECKIN,
                    LcdpConstant.RESOURCE_STATUS_NEW);

            tableTreeNodeDTOList.add(tableTreeNodeDTO);
        });
        //物理表节点
        tableList.forEach(table -> {

            LcdpTableTreeNodeDTO tableTreeNodeDTO = createTableTreeNodeDTO(table.getTableName(),
                    LcdpConstant.RESOURCE_CATEGORY_TABLE,
                    LcdpConstant.RESOURCE_TABLE_TREE_ROOT_NAME,
                    CollectionUtils.containsIgnoreCase(tableLockStatus.keySet(), table.getTableName()) && ObjectUtils.equals(CollectionUtils.getValueIgnorecase(tableLockStatus, table.getTableName()), LocalContextHelper.getLoginUserId()) ? LcdpConstant.RESOURCE_STATUS_CHECKOUT : LcdpConstant.RESOURCE_STATUS_CHECKIN,
                    CollectionUtils.containsIgnoreCase(tableLockStatus.keySet(), table.getTableName()) && !ObjectUtils.equals(CollectionUtils.getValueIgnorecase(tableLockStatus, table.getTableName()), LocalContextHelper.getLoginUserId()) ? LcdpConstant.RESOURCE_STATUS_CHECKOUT : LcdpConstant.RESOURCE_STATUS_CHECKIN,
                    LcdpConstant.RESOURCE_STATUS_VALID);

            tableTreeNodeDTOList.add(tableTreeNodeDTO);
        });

        //生成表树并设置表的状态
        List<AbstractTreeNode> viewTreeNodeDTOList = new ArrayList<>();
        //新建视图节点
        newViewList.forEach(view -> {

            LcdpTableTreeNodeDTO tableTreeNodeDTO = createTableTreeNodeDTO(view.getViewName(),
                    LcdpConstant.RESOURCE_CATEGORY_DB_VIEW,
                    LcdpConstant.RESOURCE_VIEW_TREE_ROOT_NAME,
                    CollectionUtils.containsIgnoreCase(tableLockStatus.keySet(), view.getViewName()) && ObjectUtils.equals(CollectionUtils.getValueIgnorecase(tableLockStatus, view.getViewName()), LocalContextHelper.getLoginUserId()) ? LcdpConstant.RESOURCE_STATUS_CHECKOUT : LcdpConstant.RESOURCE_STATUS_CHECKIN,
                    CollectionUtils.containsIgnoreCase(tableLockStatus.keySet(), view.getViewName()) && !ObjectUtils.equals(CollectionUtils.getValueIgnorecase(tableLockStatus, view.getViewName()), LocalContextHelper.getLoginUserId()) ? LcdpConstant.RESOURCE_STATUS_CHECKOUT : LcdpConstant.RESOURCE_STATUS_CHECKIN,
                    LcdpConstant.RESOURCE_STATUS_NEW);

            viewTreeNodeDTOList.add(tableTreeNodeDTO);
        });
        //物理视图节点
        viewList.forEach(view -> {

            LcdpTableTreeNodeDTO tableTreeNodeDTO = createTableTreeNodeDTO(view.getViewName(),
                    LcdpConstant.RESOURCE_CATEGORY_DB_VIEW,
                    LcdpConstant.RESOURCE_VIEW_TREE_ROOT_NAME,
                    CollectionUtils.containsIgnoreCase(tableLockStatus.keySet(), view.getViewName()) && ObjectUtils.equals(CollectionUtils.getValueIgnorecase(tableLockStatus, view.getViewName()), LocalContextHelper.getLoginUserId()) ? LcdpConstant.RESOURCE_STATUS_CHECKOUT : LcdpConstant.RESOURCE_STATUS_CHECKIN,
                    CollectionUtils.containsIgnoreCase(tableLockStatus.keySet(), view.getViewName()) && !ObjectUtils.equals(CollectionUtils.getValueIgnorecase(tableLockStatus, view.getViewName()), LocalContextHelper.getLoginUserId()) ? LcdpConstant.RESOURCE_STATUS_CHECKOUT : LcdpConstant.RESOURCE_STATUS_CHECKIN,
                    LcdpConstant.RESOURCE_STATUS_VALID);

            viewTreeNodeDTOList.add(tableTreeNodeDTO);
        });

        tablesTreeNodeDTO.setChildren(tableTreeNodeDTOList);
        viewsTreeNodeDTO.setChildren(viewTreeNodeDTOList);

        lcdpTableTreeNodeDTOList.add(tablesTreeNodeDTO);
        lcdpTableTreeNodeDTOList.add(viewsTreeNodeDTO);

        return lcdpTableTreeNodeDTOList;
    }

    @Override
    public LcdpTableDTO selectTableInfo(Long id) {
        List<LcdpTableDTO> lcdpTableDTOS = selectTableInfoList(ArrayUtils.asList(id));

        if (lcdpTableDTOS.isEmpty()) {
            return null;
        }
        return lcdpTableDTOS.get(0);
    }

    @Override
    public List<LcdpTableDTO> selectTableInfoList(List<Long> idList) {
        List<LcdpTableBean> lcdpTableList = selectListByIds(idList);

        if (lcdpTableList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        List<LcdpTableFieldBean> fieldBeanList = lcdpTableFieldService.selectListByFilter(SearchFilter.instance().match("TABLEID", idList).filter(MatchPattern.OR), Order.asc("ID"));

        List<LcdpTableIndexBean> indexBeanList = lcdpTableIndexService.selectListByFilter(SearchFilter.instance().match("TABLEID", idList).filter(MatchPattern.OR), Order.asc("ID"));

        return LcdpTableUtils.parseTableBeanToDTO(lcdpTableList, fieldBeanList, indexBeanList);
    }

    @Override
    public LcdpTableDTO selectPhysicalTableInfo(String tableName) {
        return lcdpDatabaseService.selectPhysicalTableInfo(tableName);
    }

    @Override
    public List<LcdpTableDTO> selectPhysicalTableInfoList(List<String> tableNameList) {
        return lcdpDatabaseService.selectPhysicalTableInfoList(tableNameList);
    }

    @Override
    public LcdpTableDTO selectVirtualTableInfo(String tableName) {
        LcdpTableBean maxVersionTable = selectFirstByFilter(SearchFilter.instance().match("TABLENAME", tableName).filter(MatchPattern.EQ), Order.desc("VERSION"));
        if (ObjectUtils.isEmpty(maxVersionTable) || StringUtils.equals(maxVersionTable.getSubmitFlag(), LcdpConstant.SUBMIT_FLAG_YES)) {
            return selectPhysicalTableInfo(tableName);
        }
        return selectTableInfo(maxVersionTable.getId());
    }

    @Override
    public List<LcdpTableBean> selectPhysicalTableNameList(RestJsonWrapperBean wrapper) {
        //查询物理表
        MapperParameter parameter = wrapper.extractMapFilter();
        List<LcdpTableBean> lcdpTableNameList = lcdpDatabaseService.selectPhysicalTableNameList(parameter);
        //为了前端下拉框性能，只使用前100条
        return lcdpTableNameList.stream().limit(100l).collect(Collectors.toList());

    }

    @Override
    public List<LcdpTableBean> selectVirtualTableNameList(RestJsonWrapperBean wrapper) {
        //查询物理表
        MapperParameter parameter = wrapper.extractMapFilter();
        List<LcdpTableBean> physicalTableNameList = lcdpDatabaseService.selectPhysicalTableNameList(parameter);

        //查询新建未提交的表
        parameter.setFilter(SearchFilter.instance().match("VERSION", 1).filter(MatchPattern.EQ).match("CREATEDBYID", LocalContextHelper.getLoginUserId()).filter(MatchPattern.SEQ).match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.SEQ));
        List<LcdpTableBean> lcdpTableNameList = selectList(parameter);

        lcdpTableNameList.addAll(physicalTableNameList);

        //为了前端下拉框性能，只使用前100条
        return lcdpTableNameList.stream().limit(100l).collect(Collectors.toList());
    }

    @Override
    public List<LcdpTableViewDTO> selectPhysicalTableViewNameList(RestJsonWrapperBean wrapper) {
        //查询物理表
        MapperParameter parameter = wrapper.extractMapFilter();
        List<LcdpTableViewDTO> lcdpTableViewNameList = lcdpDatabaseService.selectPhysicalTableOrViewNameList(parameter);
        //为了前端下拉框性能，只使用前100条
        return lcdpTableViewNameList.stream().limit(300l).collect(Collectors.toList());

    }

    @Override
    public Page<LcdpTableBean> selectChoosablePagination(RestJsonWrapperBean wrapper) {
        return selectPagination(() -> lcdpDatabaseService.selectPhysicalTableInfoList(wrapper.extractMapFilter()), wrapper.extractPageRowBounds());
    }

    @Override
    public List<LcdpTableFieldBean> selectPhysicalFieldList(RestJsonWrapperBean wrapper) {
        return lcdpDatabaseService.selectPhysicalFieldList(wrapper.extractMapFilter());
    }

    @Override
    @Audit(AuditConstant.INSERT)
    @Transactional
    @ValidateDataWith(LcdpTableSaveDataValidator.class)
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        //解析创建信息
        LcdpTableBean table = jsonWrapper.parseUnique(LcdpTableBean.class);
        List<LcdpTableFieldBean> fieldList = jsonWrapper.parse(LcdpTableFieldBean.class);
        List<LcdpTableIndexBean> indexList = jsonWrapper.parse(LcdpTableIndexBean.class);

        //新增版本并锁定资源
        table.setId(ApplicationContextHelper.getNextIdentity());
        table.setSubmitFlag(LcdpConstant.EFFECT_FLAG_NO);//创建后需要手动提交
        table.setVersion(1l);//初始化版本为1
        lcdpResourceLockService.lock(table.getTableName(), LcdpConstant.RESOURCE_CATEGORY_TABLE);

        getDao().insert(table);

        //保存创建记录
        fieldList.forEach(e -> {
            e.setId(ApplicationContextHelper.getNextIdentity());
            e.setTableName(table.getTableName());
            e.setTableId(table.getId());
        });
        indexList.forEach(e -> {
            e.setId(ApplicationContextHelper.getNextIdentity());
            e.setTableName(table.getTableName());
            e.setTableId(table.getId());
        });
        lcdpTableIndexService.getDao().insert(indexList);
        lcdpTableFieldService.getDao().insert(fieldList);
        resourceCheckoutRecordService.checkoutTableOrView(table.getTableName(), table.getTableDesc(), LcdpConstant.RESOURCE_CATEGORY_TABLE);
        return table.getId();
    }


    @Override
    public Long design(String tableName) {
        //是否存在正编辑版本
        LcdpTableBean maxVersionTable = selectFirstByFilter(SearchFilter.instance().match("TABLENAME", tableName).filter(MatchPattern.EQ), Order.desc("VERSION"));
        if (ObjectUtils.isEmpty(maxVersionTable) || LcdpConstant.EFFECT_FLAG_YES.equals(maxVersionTable.getSubmitFlag())) {
            throw new CheckedException("LCDP.MODULE.TABLES.TABLE.EXCEPTION.LOCKED");
        }
        return maxVersionTable.getId();
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long checkout(String tableName) {
        //资源是否可用并锁定
        lcdpResourceLockService.lock(tableName, LcdpConstant.RESOURCE_CATEGORY_TABLE);

        //最新版本表
        LcdpTableBean maxVersionTable = selectFirstByFilter(SearchFilter.instance().match("TABLENAME", tableName).filter(MatchPattern.SEQ), Order.desc("VERSION"));

        //新增编辑版本
        LcdpTableBean newVersionTable = new LcdpTableBean();
        List<LcdpTableFieldBean> fieldList = new ArrayList<>();
        List<LcdpTableIndexBean> indexList = new ArrayList<>();

        if (maxVersionTable != null) {//非系统表存在版本记录,版本加一
            newVersionTable.setTableDesc(maxVersionTable.getTableDesc());
            newVersionTable.setMasterTableName(maxVersionTable.getMasterTableName());
            newVersionTable.setReferColumn(maxVersionTable.getReferColumn());
            newVersionTable.setVersion(Optional.ofNullable(maxVersionTable.getVersion()).orElse(1l) + 1);
        } else {//初次编辑系统表时库内无版本记录，查出物理表信息
            LcdpTableDTO lcdpTableDTO = lcdpDatabaseService.selectPhysicalTableInfo(tableName);

            newVersionTable.setTableName(tableName);
            newVersionTable.setTableDesc(lcdpTableDTO.getTableDesc());
            newVersionTable.setVersion(2l);
        }

        // 复制物理表字段、索引定义
        List<LcdpTableFieldBean> physicalFieldList = lcdpDatabaseService.selectPhysicalFieldList(tableName);
        List<LcdpTableIndexBean> physicalIndexList = lcdpDatabaseService.selectPhysicalIndexList(tableName);
        
        if (maxVersionTable != null) {
            LcdpTableFieldBean fieldFilter = new LcdpTableFieldBean();
            fieldFilter.setTableId(maxVersionTable.getId());
            List<LcdpTableFieldBean> selectFieldList = lcdpTableFieldService.selectList(fieldFilter);
            // 顺序不变
            for (LcdpTableFieldBean selectField : selectFieldList) {
                LcdpTableFieldBean physicalField = physicalFieldList.stream().filter(f -> StringUtils.equalsIgnoreCase(f.getFieldName(), selectField.getFieldName()))
                        .findAny().orElse(null);
                
                if (physicalField != null) {
                    fieldList.add(physicalField);
                }
            }
            for (LcdpTableFieldBean physicalField : physicalFieldList) {
                if (fieldList.stream().noneMatch(f -> StringUtils.equalsIgnoreCase(f.getFieldName(), physicalField.getFieldName()))) {
                    fieldList.add(physicalField);
                }
            }
            

            LcdpTableIndexBean indexFilter = new LcdpTableIndexBean();
            indexFilter.setTableId(maxVersionTable.getId());
            List<LcdpTableIndexBean> selectIndexList = lcdpTableIndexService.selectList(indexFilter);
            // 顺序不变
            for (LcdpTableIndexBean selectIndex : selectIndexList) {
                LcdpTableIndexBean physicalIndex = physicalIndexList.stream().filter(f -> StringUtils.equalsIgnoreCase(f.getIndexName(), selectIndex.getIndexName()))
                        .findAny().orElse(null);
                
                if (physicalIndex != null) {
                    indexList.add(physicalIndex);
                }
            }
            for (LcdpTableIndexBean physicalIndex : physicalIndexList) {
                if (indexList.stream().noneMatch(f -> StringUtils.equalsIgnoreCase(f.getIndexName(), physicalIndex.getIndexName()))) {
                    indexList.add(physicalIndex);
                }
            }
        } else {
            fieldList.addAll(physicalFieldList);
            indexList.addAll(physicalIndexList);
        }

        newVersionTable.setTableName(tableName);
        newVersionTable.setSubmitFlag(LcdpConstant.EFFECT_FLAG_NO);
        newVersionTable.setId(ApplicationContextHelper.getNextIdentity());

        getDao().insert(newVersionTable);

        fieldList.forEach(e -> {
            e.setId(ApplicationContextHelper.getNextIdentity());
            e.setTableId(newVersionTable.getId());
            e.setTableName(tableName);
        });
        indexList.forEach(e -> {
            e.setId(ApplicationContextHelper.getNextIdentity());
            e.setTableId(newVersionTable.getId());
            e.setTableName(tableName);
        });
        lcdpTableIndexService.getDao().insert(indexList);
        lcdpTableFieldService.getDao().insert(fieldList);
        resourceCheckoutRecordService.checkoutTableOrView(tableName, newVersionTable.getTableDesc(), LcdpConstant.RESOURCE_CATEGORY_TABLE);

        LcdpCheckImportDataDTO checkImportDataDTO = new LcdpCheckImportDataDTO();
        checkImportDataDTO.setTableList(Arrays.asList(newVersionTable));
        checkImportDataDTO.setOperation("checkout");
        resourceImportRecordService.checkImportRecord(checkImportDataDTO);
        return newVersionTable.getId();
    }

    @Override
    @Transactional
    @Audit(AuditConstant.SAVE)
    public void submit(List<LcdpTableBean> tableList) {
        if (tableList.isEmpty()) {
            return;
        }

        //查出当前系统对应物理表信息
        List<LcdpTableDTO> physicalTableList = lcdpDatabaseService.selectPhysicalTableInfoList(tableList.stream().map(LcdpTableBean::getTableName).collect(Collectors.toList()));

        //查出对应待提交版本表信息
        List<LcdpTableDTO> submitTableList = selectTableInfoList(tableList.stream().map(LcdpTableBean::getId).collect(Collectors.toList()));
        if(designCenterTenantFlag && StringUtils.isNotEmpty(TenantContext.getTenant())){
            tenantManager.call("T_LCDP_TABLE_SUBMIT",()->{
                tableList.forEach(table -> {

                    LcdpTableDTO physicalTable = physicalTableList.stream().filter(currentLcdpTable -> StringUtils.equals(currentLcdpTable.getTableName(), table.getTableName())).findAny().orElse(null);

                    LcdpTableDTO submitTable = submitTableList.stream().filter(submitLcdpTable -> StringUtils.equals(submitLcdpTable.getTableName(), table.getTableName())).findFirst().orElse(null);

                    if (ObjectUtils.isEmpty(submitTable)) {
                        return;
                    }

                    String sql = "";

                    //分析出字段操作
                    List<LcdpTableFieldBean> fieldOpsList = LcdpTableUtils.analyzeFieldOps(submitTable.getFieldList(), ObjectUtils.isEmpty(physicalTable) ? null : physicalTable.getFieldList());

                    //分析出索引操作
                    List<LcdpTableIndexBean> indexOpsList = LcdpTableUtils.analyzeIndexOps(submitTable.getIndexList(), ObjectUtils.isEmpty(physicalTable) ? null : physicalTable.getIndexList());

                    LcdpAnalysisResultDTO validateResult = LcdpTableUtils.validateTable(physicalTable, submitTable, fieldOpsList, indexOpsList);

                    if (!validateResult.getEnable()) {
                        throw new ApplicationWarningException(table.getTableName() + " : " + validateResult.getAnalysisResultList().stream().collect(Collectors.joining(",")));
                    }

                    //清除TableContext缓存
                    RedisHelper.evict(DatabaseManager.TABLE_CONTEXT_CACHE_NAME, table.getTableName());
                    RedisHelper.evict(DatabaseManager.ENTITY_CONTEXT_CACHE_NAME, table.getTableName());

                    if (ObjectUtils.isEmpty(physicalTable)) {
                        //创建表
                        sql = ((LcdpTableServiceImpl) SunwayAopContext.currentProxy()).createPhysicalTable(table, fieldOpsList, indexOpsList);
                    } else {
                        // 更改表
                        ((LcdpTableServiceImpl) SunwayAopContext.currentProxy()).alterPhysicalTable(physicalTable, table, fieldOpsList, indexOpsList);
                        // 版本表生成建表sql
                        sql = generateCreateSql(submitTable);
                    }

                    table.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_YES);

                    table.setSql(sql);
                });
            });
        }else {
            tableList.forEach(table -> {

                LcdpTableDTO physicalTable = physicalTableList.stream().filter(currentLcdpTable -> StringUtils.equals(currentLcdpTable.getTableName(), table.getTableName())).findAny().orElse(null);

                LcdpTableDTO submitTable = submitTableList.stream().filter(submitLcdpTable -> StringUtils.equals(submitLcdpTable.getTableName(), table.getTableName())).findFirst().orElse(null);

                if (ObjectUtils.isEmpty(submitTable)) {
                    return;
                }

                String sql = "";

                //分析出字段操作
                List<LcdpTableFieldBean> fieldOpsList = LcdpTableUtils.analyzeFieldOps(submitTable.getFieldList(), ObjectUtils.isEmpty(physicalTable) ? null : physicalTable.getFieldList());

                //分析出索引操作
                List<LcdpTableIndexBean> indexOpsList = LcdpTableUtils.analyzeIndexOps(submitTable.getIndexList(), ObjectUtils.isEmpty(physicalTable) ? null : physicalTable.getIndexList());

                LcdpAnalysisResultDTO validateResult = LcdpTableUtils.validateTable(physicalTable, submitTable, fieldOpsList, indexOpsList);

                if (!validateResult.getEnable()) {
                    throw new ApplicationWarningException(table.getTableName() + " : " + validateResult.getAnalysisResultList().stream().collect(Collectors.joining(",")));
                }

                //清除TableContext缓存
                RedisHelper.evict(DatabaseManager.TABLE_CONTEXT_CACHE_NAME, table.getTableName());
                RedisHelper.evict(DatabaseManager.ENTITY_CONTEXT_CACHE_NAME, table.getTableName());

                if (ObjectUtils.isEmpty(physicalTable)) {
                    //创建表
                    sql = ((LcdpTableServiceImpl) SunwayAopContext.currentProxy()).createPhysicalTable(table, fieldOpsList, indexOpsList);
                } else {
                    // 更改表
                    ((LcdpTableServiceImpl) SunwayAopContext.currentProxy()).alterPhysicalTable(physicalTable, table, fieldOpsList, indexOpsList);
                    // 版本表生成建表sql
                    sql = generateCreateSql(submitTable);
                }

                table.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_YES);

                table.setSql(sql);
            });
        }

            resourceCheckoutRecordService.removeCheckoutTableOrView(tableList.stream().map(LcdpTableBean::getTableName).collect(Collectors.toList()));

            getDao().update(tableList, "SUBMITFLAG", "SQL");


    }

    @Override
    public List<String> revert(List<String> tableNameList) {
        //最新未提交版本表，新建表无法撤销检出
        List<LcdpTableBean> revertableTableList = selectListByFilter(SearchFilter.instance().match("TABLENAME", tableNameList).filter(MatchPattern.OR)
                .match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_NO)
                .filter(MatchPattern.SEQ).match("CREATEDBYID", LocalContextHelper.getLoginUserId()).filter(MatchPattern.SEQ)
                .match("VERSION", 1).filter(MatchPattern.DIFFER));
        revertableTableList.stream().map(LcdpTableBean::getId).forEach(id -> deleteCascadeById(id));
        
        List<String> revertedTableNameList = revertableTableList.stream().map(LcdpTableBean::getTableName).collect(Collectors.toList());
        
        resourceCheckoutRecordService.removeCheckoutTableOrView(revertedTableNameList);

        return revertedTableNameList;
    }


    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public LcdpTableBean generateLcdpTableInfo(String tableName) {

        LcdpTableBean lcdpTable = new LcdpTableBean();
        List<LcdpTableFieldBean> lcdpTableFieldList = new ArrayList<>();
        List<LcdpTableIndexBean> lcdpTableIndexList = new ArrayList<>();


        LcdpTableDTO lcdpTableDTO = lcdpDatabaseDao.selectTableInfoByTableName(tableName);

        lcdpTable.setId(ApplicationContextHelper.getNextIdentity());
        lcdpTable.setTableName(tableName);
        lcdpTable.setTableDesc(lcdpTableDTO.getTableDesc());
        lcdpTable.setVersion(1l);
        lcdpTable.setSubmitFlag(LcdpConstant.EFFECT_FLAG_NO);

        getDao().insert(lcdpTable);


        lcdpTableFieldList.addAll(lcdpDatabaseService.selectPhysicalFieldList(tableName));

        lcdpTableFieldList.forEach(e -> {
            e.setId(ApplicationContextHelper.getNextIdentity());
            e.setTableId(lcdpTable.getId());
            e.setTableName(tableName);
        });

        lcdpTableFieldService.getDao().insert(lcdpTableFieldList);


        lcdpTableIndexList.addAll(lcdpDatabaseService.selectPhysicalIndexList(tableName));

        lcdpTableIndexList.forEach(e -> {
            e.setId(ApplicationContextHelper.getNextIdentity());
            e.setTableId(lcdpTable.getId());
            e.setTableName(tableName);
        });

        lcdpTableIndexService.getDao().insert(lcdpTableIndexList);
        
        return lcdpTable;
    }


    @Override
    public LcdpTableCompareDTO<LcdpTableBean> compare(RestJsonWrapperBean wrapper) {
        String tableName = wrapper.getParamValue("TABLENAME");
        Long version = Optional.ofNullable(wrapper.getParamValue("VERSION")).map(Long::valueOf).orElse(1l);

        LcdpTableCompareDTO<LcdpTableBean> differ = new LcdpTableCompareDTO<LcdpTableBean>();

        LcdpTableBean currentTable = new LcdpTableBean();

        LcdpTableBean previousTable = new LcdpTableBean();
        //查出全部历史版本表
        List<LcdpTableBean> historyTableList = selectListByFilter(SearchFilter.instance().match("TABLENAME", tableName).filter(MatchPattern.SEQ).match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_YES).filter(MatchPattern.SEQ));
        //当前版本表
        LcdpTableBean currentTableBean = historyTableList.stream().filter(table -> version == table.getVersion()).findAny().orElse(null);
        //上一个版本表
        LcdpTableBean previousTableBean = historyTableList.stream().filter(table -> version == table.getVersion() + 1).findAny().orElse(null);
        BeanUtils.copyProperties(currentTableBean, currentTable);
        BeanUtils.copyProperties(previousTableBean, previousTable);

        differ.setHistoryTableList(historyTableList);
        differ.setCurrentTable(currentTable);
        differ.setPreviousTable(previousTable);

        return differ;
    }


    @Override
    @Audit(AuditConstant.SAVE)
    @Transactional
    @ValidateDataWith(LcdpTableSaveDataValidator.class)
    public void save(Long id, RestJsonWrapperBean wrapper) {
        //解析创建信息
        LcdpTableBean table = wrapper.parseUnique(LcdpTableBean.class);
        List<LcdpTableFieldBean> fieldList = wrapper.parse(LcdpTableFieldBean.class);
        List<LcdpTableIndexBean> indexList = wrapper.parse(LcdpTableIndexBean.class);

        LcdpTableBean lcdpTableBean = selectById(id);
        lcdpTableBean.setTableDesc(table.getTableDesc());
        lcdpTableBean.setMasterTableName(table.getMasterTableName());
        lcdpTableBean.setReferColumn(table.getReferColumn());
        //更新表描述
        getDao().update(lcdpTableBean, "TABLEDESC", "MASTERTABLENAME", "REFERCOLUMN");

        //字段、索引先删除再新增
        fieldList.forEach(e -> {
            e.setId(ApplicationContextHelper.getNextIdentity());
            e.setTableName(table.getTableName());
            e.setTableId(id);
        });
        indexList.forEach(e -> {
            e.setId(ApplicationContextHelper.getNextIdentity());
            e.setTableName(table.getTableName());
            e.setTableId(id);
        });

        lcdpTableFieldService.getDao().deleteBy(fieldList, "TABLEID");
        lcdpTableIndexService.getDao().deleteBy(indexList, "TABLEID");

        lcdpTableFieldService.getDao().insert(fieldList);
        lcdpTableIndexService.getDao().insert(indexList);
    }

    @Override
    @Transactional
    @Audit(AuditConstant.DELETE)
    @ValidateDataWith(LcdpTableDeleteDataValidator.class)
    public void delete(RestJsonWrapperBean wrapper) {
        //待删除的表
        LcdpTableBean toDeleteTable = wrapper.parseUnique(getDao().getType());

        LcdpTableBean queryTable = selectFirstByFilter(SearchFilter.instance().match("TABLENAME", toDeleteTable.getTableName()).filter(MatchPattern.EQ).match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_YES).filter(MatchPattern.EQ));

        List<LcdpResourceLockBean> LockList = lcdpResourceLockService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", toDeleteTable.getTableName()).filter(MatchPattern.SEQ).match("LOCKUSERID", LocalContextHelper.getLoginUserId()).filter(MatchPattern.SEQ));
        if (LockList.isEmpty()) {
            throw new CheckedException("LCDP.MODULE.RESOURCES.TIP.UNCHECKOUT_CANNOT_DELETE");
        }

        //删除该表的相关版本记录
        List<Long> tableIdList = getDao().selectIdList(toDeleteTable, ArrayUtils.asList("TABLENAME"));

        tableIdList.stream().forEach(id -> deleteCascadeById(id));

        //删除资源版本信息
        List<LcdpResourceVersionBean> versionList = lcdpResourceVersionService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", toDeleteTable.getTableName()).filter(MatchPattern.OR));
        versionList.forEach(version -> {
            version.setResourceDeleteFlag(LcdpConstant.RESOURCE_DELETED_YES);
        });

        lcdpResourceVersionService.getDao().update(versionList, "RESOURCEDELETEFLAG");


        //表资源解锁
        lcdpResourceLockService.unLock(ArrayUtils.asList(toDeleteTable.getTableName()));

        //清除TableContext缓存
        RedisHelper.evict(DatabaseManager.TABLE_CONTEXT_CACHE_NAME, toDeleteTable.getTableName());
        RedisHelper.evict(DatabaseManager.ENTITY_CONTEXT_CACHE_NAME, toDeleteTable.getTableName());

        //删除物理表与记录
        if (lcdpDatabaseService.isExistPhysicalTable(toDeleteTable.getTableName())) {
            ((LcdpTableServiceImpl) SunwayAopContext.currentProxy()).dropPhysicalTable(toDeleteTable);
        }

        if (queryTable != null) {
            LcdpSubmitLogBean submitLog = new LcdpSubmitLogBean();
            submitLog.setId(ApplicationContextHelper.getNextIdentity());
            submitLog.setCommit(I18nHelper.getMessage("LCDP.RESOURCE.DELETED"));
            submitLogService.getDao().insert(submitLog);

            LcdpResourceVersionBean resourceVersion = new LcdpResourceVersionBean();
            resourceVersion.setId(ApplicationContextHelper.getNextIdentity());
            resourceVersion.setLogId(submitLog.getId());
            resourceVersion.setResourcePath(queryTable.getTableName());
            resourceVersion.setResourceDeleteFlag(LcdpConstant.RESOURCE_DELETED_YES);
            resourceVersion.setResourceAction(LcdpConstant.RESOURCE_SUBMIT_ACTION_DELETE);
            resourceVersion.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_TABLE);
            lcdpResourceVersionService.getDao().insert(resourceVersion);
        }
        //删除表对应国际化信息
        List<CoreI18nBean> tableI18nList = coreI18nService.selectListByFilter(SearchFilter.instance().match("CODE", toDeleteTable.getTableName() + ".").filter(MatchPattern.CISB));
        coreI18nService.getDao().deleteBy(tableI18nList, "ID");
        resourceCheckoutRecordService.removeCheckoutTableOrView(ArrayUtils.asList(toDeleteTable.getTableName()));


    }

    @Override
    public RestValidationResultBean validateTableName(String name) {
        if (StringUtils.equals("oracle", DialectRepository.getDialect().getDatabase()) && !StringUtils.isEmpty(name) && name.length() > 30) {
            return new RestValidationResultBean(false, I18nHelper.getMessage("LCDP.MODULE.TABLES.NAME.TIP.MAX_LENGTH", "30"));
        } else if (!StringUtils.isEmpty(name) && name.length() > 60) {
            return new RestValidationResultBean(false, I18nHelper.getMessage("LCDP.MODULE.TABLES.NAME.TIP.MAX_LENGTH", "60"));
        }
        SearchFilter filter = SearchFilter.instance().match("TABLENAME", name).filter(MatchPattern.SEQ);
        List<LcdpTableBean> tableList = selectListByFilter(filter);
        List<LcdpViewBean> viewList = lcdpViewService.selectListByFilter(filter);
        if (tableList.isEmpty() && viewList.isEmpty() && !lcdpDatabaseService.isExistPhysicalTable(name) && !lcdpDatabaseService.isExistPhysicalView(name)) {
            return new RestValidationResultBean(true);
        }
        return new RestValidationResultBean(false, I18nHelper.getMessage("LCDP.MODULE.TABLES.NAME.TIP.EXIST"));
    }

    @Override
    public RestValidationResultBean validateIndexName(String name) {
        int count = lcdpDatabaseDao.countByIndexName(name);
        if (count > 0) {
            return new RestValidationResultBean(false, I18nHelper.getMessage("LCDP.MODULE.TABLES.NAME.TIP.EXIST"));
        }
        List<LcdpTableIndexBean> indexList = lcdpTableIndexService.selectListByFilter(SearchFilter.instance().match("INDEXNAME", name).filter(MatchPattern.SEQ));
        if (!indexList.isEmpty()) {
            List<Long> tableIdList = indexList.stream().map(LcdpTableIndexBean::getTableId).collect(Collectors.toList());
            List<LcdpTableBean> tableList = selectListByFilter(SearchFilter.instance().match("ID", tableIdList).filter(MatchPattern.OR).match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.SEQ));

            if (!tableList.isEmpty()) {
                return new RestValidationResultBean(false, I18nHelper.getMessage("LCDP.MODULE.TABLES.NAME.TIP.EXIST"));
            }
        }
        return new RestValidationResultBean(true);
    }

    /**
     * 分析表数据返回是否可更新表
     *
     * @param tableNameList 待导入的表名集合
     * @param dataMap       全部的表及对应的tableDtoJson串
     * @return 返回结果 表名 分析结果
     */
    @Override
    public Map<String, LcdpAnalysisResultDTO> analysisTableInfo(List<String> tableNameList, Map<String, String> dataMap) {

        Map<String, LcdpAnalysisResultDTO> result = new HashMap<>();

        //查出当前系统对应表信息
        List<LcdpTableDTO> physicalTableList = lcdpDatabaseService.selectPhysicalTableInfoList(tableNameList);

        //对比生成表操作bean并校验
        tableNameList.forEach(tableName -> {

            String tableJson = dataMap.get(tableName);

            if (StringUtils.isEmpty(tableJson)) {
                return;
            }

            //解析导入表结构
            LcdpTableDTO importTable = JsonUtils.parse(dataMap.get(tableName), LcdpTableDTO.class);

            //查出物理表结构
            LcdpTableDTO physicalTable = physicalTableList.stream().filter(currentLcdpTableDTO -> StringUtils.equals(tableName, currentLcdpTableDTO.getTableName())).findAny().orElse(null);

            //分析出字段操作
            List<LcdpTableFieldBean> analyzedFieldList = LcdpTableUtils.analyzeFieldOps(importTable.getFieldList(), !ObjectUtils.isEmpty(physicalTable) ? physicalTable.getFieldList() : null);

            //分析出索引操作
            List<LcdpTableIndexBean> analyzedIndexList = LcdpTableUtils.analyzeIndexOps(importTable.getIndexList(), !ObjectUtils.isEmpty(physicalTable) ? physicalTable.getIndexList() : null);

            //校验是否可以导入
            LcdpAnalysisResultDTO analysisResult = LcdpTableUtils.validateTable(physicalTable, importTable, analyzedFieldList, analyzedIndexList);

            //开启多租户要校验 不同数据库表的一致性
            if (!LcdpTableUtils.validateDatabaseConsistency(tableName)) {
                analysisResult.setEnable(false);
                analysisResult.getAnalysisResultList().add(I18nHelper.getMessage("LCDP.MODULE.TABLES.TABLE.EXCEPTION.DATABASE_NON_CONSISTENCY", tableName));
            }


            result.put(tableName, analysisResult);
        });

        return result;
    }

    /**
     * 修改物理表和记录sql
     */
    @Override
    @Transactional
    public String alterPhysicalTable(LcdpTableDTO physicalTable, LcdpTableBean tableOps, List<LcdpTableFieldBean> fieldOpsList, List<LcdpTableIndexBean> indexOpsList) {
        //记录sql
        String sql = lcdpDatabaseService.alterPhysicalTable(physicalTable, tableOps, fieldOpsList, indexOpsList);

        //字段国际化更新
        initTableI18n(tableOps.getTableName(), tableOps.getTableDesc(), fieldOpsList);
        //初始化审计跟踪配置
        initAuditConfig(tableOps.getTableName(), tableOps.getTableDesc());
        //初始化业务配置
        initTableService(tableOps.getTableName(), tableOps.getTableDesc());
        //初始化表层级关系
        initTableHierarchy(tableOps.getTableName(), tableOps.getMasterTableName(), tableOps.getReferColumn());

        return sql;
    }

    @Override
    @Transactional
    public String dropPhysicalTable(LcdpTableBean oldTable) {
        //删除并记录sql
        String sql = lcdpDatabaseService.dropPhysicalTable(oldTable);

        //不删除国际化
        //删除审计跟踪配置
        deleteAuditConfig(oldTable.getTableName());
        //删除业务配置
        deleteTableService(oldTable.getTableName());
        //删除表层级关系
        deleteTableHierarchy(oldTable.getTableName());

        return sql;
    }

    /**
     * 创建物理表和记录sql
     */
    @Override
    @Transactional
    public String createPhysicalTable(LcdpTableBean table, List<LcdpTableFieldBean> fieldOpsList, List<LcdpTableIndexBean> indexOpsList) {
        //创建表并记录sql
        String sql = lcdpDatabaseService.createPhysicalTable(table, fieldOpsList, indexOpsList);
        //字段国际化更新
        initTableI18n(table.getTableName(), table.getTableDesc(), fieldOpsList);
        //初始化审计跟踪配置
        initAuditConfig(table.getTableName(), table.getTableDesc());
        //初始化业务配置
        initTableService(table.getTableName(), table.getTableDesc());
        //初始化表层级关系
        initTableHierarchy(table.getTableName(), table.getMasterTableName(), table.getReferColumn());

        return sql;
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<LcdpTableFieldBean> selectPhysicalFieldSelectableList() {

        MapperParameter parameter = new MapperParameter();

        Map<String, String> requestParamMap = ServletUtils.getCurrentRequestParamMap();

        if (!requestParamMap.isEmpty()) {
            SearchFilter filter = SearchFilter.instance();

            requestParamMap.forEach((k, v) -> {

                MatchPattern match = Arrays.stream(MatchPattern.values()).filter(m -> StringUtils.endsWith(k, "_" + m.name())).findAny().orElse(null);

                String key = k;

                if (match == null) {
                    match = MatchPattern.EQ;
                } else {
                    key = StringUtils.removeEnd(key, "_" + match.name());

                    if (MatchPattern.IN.equals(match)) {
                        match = MatchPattern.OR;
                    }
                }

                if (StringUtils.contains(v, ",")) {
                    filter.match(key, Arrays.asList(StringUtils.split(v, ","))).filter(match);
                } else {
                    filter.match(key, v).filter(match);
                }

            });

            parameter.setFilter(filter);
        }

        return lcdpDatabaseService.selectPhysicalFieldList(parameter);
    }


    @Override
    @Transactional
    public LcdpTableDTO executeSql(RestJsonWrapperBean wrapper) {
        String sqlText = wrapper.getParamValue("sql");
        String filterKey = " IF\\s+NOT\\s+EXISTS";

        if (!StringUtils.isEmpty(sqlText)) {
            sqlText = sqlText.toUpperCase().replaceFirst(filterKey, "");
            sqlText = sqlText.toUpperCase().replaceFirst("\\s+DBO.", " ");
        }

        String[] sqls = sqlText.split(";");
        try {
            for (String sql : sqls) {
                if (!StringUtils.isBlank(sql)) {
                    globalMapper.update(sql);
                }
            }

            //返回数据库表设计数据
            String tableName = "";
            Pattern pattern = Pattern.compile("CREATE\\s+TABLE\\s+'?\"?`?(\\w+)'?\"?`?");
            Matcher matcher = pattern.matcher(sqlText.toUpperCase());
            if (matcher.find()) {
                tableName = matcher.group().replaceAll(" ", "").replace("CREATETABLE", "");
                tableName = tableName.replaceAll("\"", "").replaceAll("`", "").replaceAll("'", "");
            }
            if (!StringUtils.isBlank(tableName)) {
                LcdpTableDTO tableDTO = selectPhysicalTableInfo(tableName);
                globalMapper.update("DROP TABLE " + tableName);//删除表
                return tableDTO;
            }

        } catch (Exception ex) {
            SQLException sqlException = ExceptionUtils.getCause(ex, SQLException.class);
            if (ObjectUtils.isEmpty(sqlException)) {
                throw new CheckedException(ex.getMessage(), ex);
            }
            String exceptionMessage = sqlGenerateHelper.parseSQLException(sqlException.getMessage());
            throw new CheckedException(exceptionMessage, sqlException);
        }

        return null;
    }

    @Override
    public LcdpDynamicPagination<Map<String, Object>> dynamicQuery(RestJsonWrapperBean wrapper) {
        return lcdpDatabaseService.dynamicQuery(wrapper);
    }

    @Override
    public String generateCreateSql(LcdpTableDTO tableDTO) {
        //记录sql
        StringBuilder recorder = new StringBuilder();

        LcdpTableIndexBean primaryKey = tableDTO.getIndexList().stream().filter(index -> primarykey.name().equalsIgnoreCase(index.getIndexType())).map(index -> {
            LcdpTableIndexBean indexBean = new LcdpTableIndexBean();
            BeanUtils.copyProperties(index, indexBean);
            indexBean.setIndexOperationType(LcdpConstant.FIELD_INDEX_OPS_ADD);
            return indexBean;
        }).findAny().orElse(null);

        List<LcdpTableIndexBean> indexOpsList = tableDTO.getIndexList().stream().filter(index -> !primarykey.name().equalsIgnoreCase(index.getIndexType())).map(index -> {
            LcdpTableIndexBean indexBean = new LcdpTableIndexBean();
            BeanUtils.copyProperties(index, indexBean);
            indexBean.setIndexOperationType(LcdpConstant.FIELD_INDEX_OPS_ADD);
            return indexBean;
        }).collect(Collectors.toList());

        List<LcdpTableFieldBean> fieldOpsList = tableDTO.getFieldList().stream().map(field -> {
            LcdpTableFieldBean fieldBean = new LcdpTableFieldBean();
            BeanUtils.copyProperties(field, fieldBean);
            fieldBean.setFieldOperationType(LcdpConstant.FIELD_INDEX_OPS_ADD);
            return fieldBean;
        }).collect(Collectors.toList());

        LcdpTableBean table = new LcdpTableBean();
        BeanUtils.copyProperties(tableDTO, table);

        List<String> createTableSqlList = sqlGenerateHelper.generateCreateTableSql(table, fieldOpsList, primaryKey);
        createTableSqlList.forEach(sql -> recorder.append(sql + ";\r\n"));
        indexOpsList.forEach(indexOps -> recorder.append(sqlGenerateHelper.generateIndexSql(table, indexOps) + ";\r\n"));
        return recorder.toString();
    }

    @Override
    public void insertDefaultFieldsIfMissing(String table, List<LcdpTableFunction> functionList) {
        if (StringUtils.isBlank(table)) {
            return;
        }
        
        table = table.toUpperCase();
        
        List<LcdpTableFieldBean> insertFieldList = new ArrayList<>();
        
        List<LcdpTableFieldBean> physicalFieldList = lcdpDatabaseService.selectPhysicalFieldList(table);
        
        LcdpTableFieldBean fieldFilter = new LcdpTableFieldBean();
        fieldFilter.setTableName(table);
        List<LcdpTableFieldBean> tableFieldList = lcdpTableFieldService.selectList(fieldFilter);
        
        for (LcdpTableFunction function : functionList) {
            function.getDefaultFieldList().forEach(f -> {
                if (physicalFieldList.stream().noneMatch(e -> e.getFieldName().equalsIgnoreCase(f.getFieldName()))
                        && tableFieldList.stream().noneMatch(e -> e.getFieldName().equalsIgnoreCase(f.getFieldName()))) {
                    insertFieldList.add(f);
                }
            });
        }
        
        if (!insertFieldList.isEmpty()) {
            LcdpTableBean filterTable = new LcdpTableBean();
            filterTable.setTableName(table);
            
            LcdpTableBean latestLcdpTable = getDao().selectFirstIfPresent(filterTable, Order.desc("VERSION"));
            
            if (latestLcdpTable != null) { // 表已存在
                String lockStatus = lcdpResourceLockService.getLockStatus(LocalContextHelper.getLoginUserId(), table, LcdpConstant.RESOURCE_CATEGORY_TABLE);
                
                if ("0".equals(lockStatus)) { // 没有锁定
                    Long newId = checkout(table); // 检出
                    
                    insertFieldList.forEach(f -> {
                        f.setId(ApplicationContextHelper.getNextIdentity());
                        f.setTableId(newId);
                        f.setTableName(filterTable.getTableName());
                        f.setFieldOperationType("add");
                    });
                    
                    lcdpTableFieldService.getDao().fastInsert(insertFieldList);
                } else if ("1".equals(lockStatus)) { // 已被自己锁定
                    insertFieldList.forEach(f -> {
                        f.setId(ApplicationContextHelper.getNextIdentity());
                        f.setTableId(latestLcdpTable.getId());
                        f.setTableName(filterTable.getTableName());
                        f.setFieldOperationType("add");
                    });
                    
                    lcdpTableFieldService.getDao().fastInsert(insertFieldList);
                } else { // 已被其它人锁定
                    throw new ApplicationRuntimeException("LCDP.MODULE.RESOUCES.TIP.UNABLE_UPDATE_TABLE_DUETO_LOCK_BY_OTHERUSER", table);
                }
            } else { // 表不存在
                LcdpTableBean lcdpTable = generateLcdpTableInfo(table);
                
                insertFieldList.forEach(f -> {
                    f.setId(ApplicationContextHelper.getNextIdentity());
                    f.setTableId(lcdpTable.getId());
                    f.setTableName(filterTable.getTableName());
                    f.setFieldOperationType("add");
                });
                
                lcdpTableFieldService.getDao().fastInsert(insertFieldList);
                
                lcdpResourceLockService.lock(table, LcdpConstant.RESOURCE_CATEGORY_TABLE);
            }
            
            String columns = insertFieldList.stream().map(LcdpTableFieldBean::getFieldName).collect(Collectors.joining(","));
            
            String msg = SunwayAopContext.getCache(LcdpConstant.LCDP_AUTOMATIC_INSERT_COLIMNS_MSG_KEY);
            if (msg != null) {
                msg += "\n" + I18nHelper.getMessage("LCDP.MODULE.TABLES.REQUIRE_SUBMIT_AUTOMATIC_INSERT_COLUMNS",
                        table, columns);
            } else {
                msg = I18nHelper.getMessage("LCDP.MODULE.TABLES.REQUIRE_SUBMIT_AUTOMATIC_INSERT_COLUMNS",
                        table, columns);
            }
            SunwayAopContext.putCache(LcdpConstant.LCDP_AUTOMATIC_INSERT_COLIMNS_MSG_KEY, msg);
        }
    }
    
    @Override
    public List<LcdpTableBean> selectLatestBriefList(MapperParameter parameter) {
        return getDao().selectLatestBriefList(parameter);
    }
    //-----------------------------------------------------------------------------------------------------------------------
    // 私有方法
    //-----------------------------------------------------------------------------------------------------------------------
    /***
     * 生成表树节点类
     */
    private LcdpTableTreeNodeDTO createTableTreeNodeDTO(String name, String type, String root, String checkoutFlag, String otherCheckoutFlag, String newFlag) {
        LcdpTableTreeNodeDTO tableTreeNodeDTO = new LcdpTableTreeNodeDTO();

        tableTreeNodeDTO.setId(name);
        tableTreeNodeDTO.setName(name);
        tableTreeNodeDTO.setType(type);
        tableTreeNodeDTO.setParentId(root);
        tableTreeNodeDTO.setCheckOutFlag(checkoutFlag);
        tableTreeNodeDTO.setOtherUserCheckOutFlag(otherCheckoutFlag);
        tableTreeNodeDTO.setResourceStatus(newFlag);

        return tableTreeNodeDTO;
    }

    /**
     * 根据表原国际化集,修改或插入当前国际化
     *
     * @param tableName 表名
     * @param fieldList 字段
     */
    private void initTableI18n(String tableName, String tableDesc, List<LcdpTableFieldBean> fieldList) {
        //查询表国际化信息
        List<CoreI18nBean> tableI18nList = coreI18nService.selectListByFilter(SearchFilter.instance().match("CODE", tableName + ".").filter(MatchPattern.CISB));
        List<Long> i18nIdList = tableI18nList.stream().map(CoreI18nBean::getId).collect(Collectors.toList());
        List<CoreI18nMessageBean> tableI18nMessageList = coreI18nMessageService.selectListByFilter(SearchFilter.instance().match("I18NID", i18nIdList).filter(MatchPattern.OR));

        //添加国际化信息
        List<CoreI18nBean> insertI18nList = new ArrayList<>();
        List<CoreI18nMessageBean> insertI18nMessageList = new ArrayList<>();

        //{tableName}.SERVICE_NAME是否已存在国际化
        LcdpTableFieldBean tableServiceI18n = new LcdpTableFieldBean();
        tableServiceI18n.setFieldName("SERVICE_NAME");
        tableServiceI18n.setFieldComment(tableDesc);
        fieldList.add(tableServiceI18n);

        fieldList.forEach(field -> {
            //根据表名与字段名获取到国际化Code
            String i18nCode = LcdpTableUtils.i18nCode(tableName, field.getFieldName());

            String message = field.getFieldComment();
            //是否已存在国际化
            CoreI18nBean tableI18n = tableI18nList.stream().filter(i18n -> StringUtils.equals(i18n.getCode(), i18nCode)).findFirst().orElse(null);

            if (ObjectUtils.isEmpty(tableI18n)) {//不存在插入新的国际化信息
                CoreI18nBean i18n = new CoreI18nBean();
                i18n.setId(ApplicationContextHelper.getNextIdentity());
                i18n.setCode(i18nCode);
                i18n.setDefaultMessage(message);
                i18n.setDescription(message);

                CoreI18nMessageBean zh = new CoreI18nMessageBean();
                zh.setId(ApplicationContextHelper.getNextIdentity());
                zh.setI18nConfigId("zh-CN");
                zh.setI18nId(i18n.getId());
                zh.setMessage(message);

                insertI18nList.add(i18n);
                insertI18nMessageList.add(zh);
            } else {//更新国际化信息
                tableI18n.setDefaultMessage(message);
                tableI18n.setDescription(message);
                tableI18nMessageList.stream().filter(i18nMessage -> i18nMessage.getI18nId().longValue() == tableI18n.getId().longValue()).forEach(i18nMessage -> i18nMessage.setMessage(message));
            }
        });

        //持久化国际化信息
        coreI18nService.updateIfChanged(tableI18nList);
        coreI18nMessageService.updateIfChanged(tableI18nMessageList);
        coreI18nService.getDao().insert(insertI18nList);
        coreI18nMessageService.getDao().insert(insertI18nMessageList);
    }


    /**
     * 初始化表业务配置
     */
    private void initTableService(String tableName, String tableDesc) {
        //提交后在初始化业务配置，防止pgsql、sqlserver事务内创建表无法查到导致报错
        TransactionUtils.runAfterCommit(() -> {

            //判断是否存在审核流字段
            List<ColumnContext> tableColumnContextList = DatabaseManager.getTableColumnContextList(tableName);
            boolean processStatusFlag = tableColumnContextList.stream().anyMatch(columnContext -> StringUtils.equalsIgnoreCase(columnContext.getColumnName(), "PROCESSSTATUS"));

            //coreTable工作流、附件配置
            CoreTableBean coreTableBean = coreTableService.selectTable(tableName);
            if (ObjectUtils.isEmpty(coreTableBean)) {
                CoreTableBean coreTable = new CoreTableBean();
                coreTable.setId(tableName);
                coreTable.setAuditable(processStatusFlag ? Constant.YES : Constant.NO);
                coreTable.setAttachment(Constant.NO);
                coreTableService.getDao().insert(coreTable);
            } else {
                coreTableBean.setAuditable(processStatusFlag ? Constant.YES : Constant.NO);
                coreTableService.update(coreTableBean);
            }
            //menuExt业务配置
            List<CoreMenuExtBean> menuExtList = coreMenuExtService.selectListByFilter(SearchFilter.instance().match("TABLENAME", tableName).filter(MatchPattern.SEQ));

            if (processStatusFlag) {

                if (!menuExtList.isEmpty()) {//存在，更新业务名称
                    menuExtList.forEach(service -> service.setServiceName(tableDesc));
                    coreMenuExtService.updateIfChanged(menuExtList);
                    return;
                }
                //不存在，添加业务配置
                CoreMenuExtBean menuExtBean = new CoreMenuExtBean();
                menuExtBean.setId(ApplicationContextHelper.getNextIdentity().toString());
                menuExtBean.setServiceName(tableDesc);
                menuExtBean.setTableName(tableName);
                menuExtBean.setEnable(Constant.ENABLE_YES);
                menuExtBean.setType("audit_def");

                coreMenuExtService.getDao().insert(menuExtBean);
            } else if (!menuExtList.isEmpty()) {
                coreMenuExtService.getDao().deleteBy(menuExtList);
            }
        });
    }

    /**
     * 初始化表层级关系
     */
    private void initTableHierarchy(String tableName, String masterTableName, String referColumn) {
        CoreTableHierarchyBean newTableHirarchy = new CoreTableHierarchyBean();
        newTableHirarchy.setTableName(tableName);
        CoreTableHierarchyBean tableHierarchy = coreTableHierarchyDao.selectFirstIfPresent(newTableHirarchy);

        if (!StringUtils.isEmpty(masterTableName) && !StringUtils.isEmpty(referColumn)) {
            //清除缓存
            RedisHelper.evict("T_CORE_TABLE_HIERARCHY.MASTERTABLE", masterTableName);
            RedisHelper.evict("T_CORE_TABLE_HIERARCHY.TABLENAME", tableName);

            if (ObjectUtils.isEmpty(tableHierarchy)) {
                newTableHirarchy.setId(ApplicationContextHelper.getNextIdentity());
                newTableHirarchy.setReferColumn(referColumn);
                newTableHirarchy.setMasterTableName(masterTableName);
                coreTableHierarchyDao.insert(newTableHirarchy);
            } else {
                tableHierarchy.setReferColumn(referColumn);
                tableHierarchy.setMasterTableName(masterTableName);
                coreTableHierarchyDao.updateIfChanged(tableHierarchy);
            }
        } else if (!ObjectUtils.isEmpty(tableHierarchy)) {
            deleteTableHierarchy(tableName);
        }
    }

    /**
     * 删除表的业务配置
     */
    private void deleteTableHierarchy(String tableName) {
        CoreTableHierarchyBean filter = new CoreTableHierarchyBean();
        filter.setTableName(tableName);
        //清除缓存
        RedisHelper.evict("T_CORE_TABLE_HIERARCHY.TABLENAME", tableName);
        coreTableHierarchyDao.deleteBy(filter, "TABLENAME");
    }

    /**
     * 删除表的业务配置
     */
    private void deleteTableService(String tableName) {
        CoreMenuExtBean condition = new CoreMenuExtBean();
        condition.setTableName(tableName);
        coreMenuExtService.getDao().deleteBy(condition, "TABLENAME");
    }

    /**
     * 初始化表审计跟踪配置
     */
    private void initAuditConfig(String tableName, String tableDesc) {
        //提交后在更新审计配置，防止pgsql、sqlserver事务内创建表无法查到导致报错
        TransactionUtils.runAfterCommit(() -> {
            CoreAuditConfigBean coreAuditConfigBean = coreAuditConfigService.selectDetail(tableName);
            //若不存在则新增审计跟踪配置
            if (ObjectUtils.isEmpty(coreAuditConfigBean)) {
                CoreAuditConfigBean config = new CoreAuditConfigBean();
                config.setId(tableName);
                if (StringUtils.startsWith(tableName, "T_CORE_")) { // 核心默认是系统数据
                    config.setCategory("system");
                } else {
                    config.setCategory("service"); // 非核心默认是业务数据
                }
                config.setName(tableDesc);
                config.setAuditable(auditable ? Constant.YES : Constant.NO); // 默认不审计
                coreAuditConfigService.getDao().insert(config);
            }

            //查出原配置字段
            List<CoreAuditConfigColumnBean> configColumnList = coreAuditConfigColumnService.selectListByConfigId(tableName);

            List<ColumnContext> columnList = DatabaseManager.getTableColumnContextList(tableName);
            //找出需要删除的配置字段
            List<Long> deleteConfigColumnIdList = configColumnList.stream().filter(cc -> columnList.stream().noneMatch(c -> c.getColumnName().equalsIgnoreCase(cc.getColumn())))
                    .map(CoreAuditConfigColumnBean::getId).collect(Collectors.toList());
            //找出需要新增的配置字段
            List<CoreAuditConfigColumnBean> insertConfigColumnList = columnList.stream().filter(c -> configColumnList.stream().noneMatch(cc -> cc.getColumn().equalsIgnoreCase(c.getColumnName())))
                    .map(c -> {
                        CoreAuditConfigColumnBean column = new CoreAuditConfigColumnBean();
                        column.setId(ApplicationContextHelper.getNextIdentity());
                        column.setConfigId(tableName);
                        column.setColumn(c.getColumnName().toUpperCase());
                        column.setAuditable(auditable ? Constant.YES : Constant.NO); // 新增时默认不审计

                        return column;
                    }).collect(Collectors.toList());

            coreAuditConfigColumnService.getDao().deleteByIdList(deleteConfigColumnIdList);

            coreAuditConfigColumnService.getDao().insert(insertConfigColumnList);
        });
    }

    /**
     * 删除表的审计跟踪配置
     */
    private void deleteAuditConfig(String tableName) {
        coreAuditConfigService.getDao().delete(tableName);
        List<CoreAuditConfigColumnBean> coreAuditConfigColumnBeans = coreAuditConfigColumnService.selectListByConfigId(tableName);
        coreAuditConfigColumnService.getDao().deleteBy(coreAuditConfigColumnBeans);
    }
}
