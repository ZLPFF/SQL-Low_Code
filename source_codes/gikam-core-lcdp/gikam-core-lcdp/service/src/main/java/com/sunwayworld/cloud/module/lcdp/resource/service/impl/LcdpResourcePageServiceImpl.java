package com.sunwayworld.cloud.module.lcdp.resource.service.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.base.LcdpResultDTO;
import com.sunwayworld.cloud.module.lcdp.base.annotation.Mapping;
import com.sunwayworld.cloud.module.lcdp.base.sourcecode.LcdpSourceCodeManager;
import com.sunwayworld.cloud.module.lcdp.base.sourcecode.LcdpSourceCodeMethodType;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModulePageCompBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpPageFormConfigDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpPageGridConfigDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpPageServiceMethodDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpPageTreeGridConfigDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceClassInfoDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.ServerScriptType;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceHistoryService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourcePageService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpJavaCodeResolverUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpMapperUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpReflectionUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpScriptUtils;
import com.sunwayworld.cloud.module.lcdp.resourcelock.service.LcdpResourceLockService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldDTO;
import com.sunwayworld.cloud.module.lcdp.table.helper.LcdpTableFunction;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableService;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.data.Pair;
import com.sunwayworld.framework.database.dialect.DialectRepository;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.executor.manager.TaskExecutorManager;
import com.sunwayworld.framework.jdk.core.ClassManager;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.mybatis.MybatisHelper;
import com.sunwayworld.framework.mybatis.mapper.GlobalMapper;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.utils.ArrayUtils;
import com.sunwayworld.framework.utils.BeanUtils;
import com.sunwayworld.framework.utils.ClassUtils;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.ConvertUtils;
import com.sunwayworld.framework.utils.ReflectionUtils;
import com.sunwayworld.framework.utils.ReflectionUtils.FieldWrapper;
import com.sunwayworld.framework.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class LcdpResourcePageServiceImpl implements LcdpResourcePageService {
    private static final Logger logger = LoggerFactory.getLogger(LcdpResourcePageService.class);

    @Autowired
    @Lazy
    private LcdpResourceService resourceService;
    @Autowired
    private LcdpResourceHistoryService resourceHistoryService;
    @Autowired
    private LcdpResourceLockService resourceLockService;
    @Autowired
    private LcdpTableService tableService;
    @Autowired
    private GlobalMapper globalMapper;

    @Override
    @Log(value = "查询所有服务", type = LogType.SELECT)
    public List<String> selectServiceList() {
        List<LcdpResourceBean> serviceResourceList = resourceService.selectPageServiceList();

        return serviceResourceList.stream().map(r -> r.getPath()).collect(Collectors.toList());
    }

    @Override
    public LcdpResultDTO validateSql(RestJsonWrapperBean wrapper) {
        String sql = wrapper.getParamValue("sql");

        if (StringUtils.isBlank(sql)) {
            return LcdpResultDTO.fail();
        }

        try {
            globalMapper.selectOne(DialectRepository.getDialect().getSelectFirstSql(sql));

            return LcdpResultDTO.sucess();
        } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);

            return LcdpResultDTO.fail();
        }
    }

    @Override
    public List<LcdpPageServiceMethodDTO> selectServiceMappingMethodList(RestJsonWrapperBean wrapper) {
        String service = wrapper.getParamValue("service");

        LcdpResourceBean resource = resourceService.getLatestActivatedResourceByPath(service);

        if (resource == null) {
            return CollectionUtils.emptyList();
        }

        LcdpResourceClassInfoDTO classInfo = resourceService.getActiveClassInfoByPath(service);

        if (classInfo == null) {
            return CollectionUtils.emptyList();
        }

        if (classInfo.getClazz() != null) {
            List<Method> methodList = ReflectionUtils.getMethodList(classInfo.getClazz(), m -> m.isAnnotationPresent(Mapping.class));

            return methodList.stream().map(m -> {
                        LcdpPageServiceMethodDTO serviceMethod = new LcdpPageServiceMethodDTO();
                        serviceMethod.setName(m.getName());

                        Mapping mapping = m.getAnnotation(Mapping.class);
                        serviceMethod.setDesc(mapping.value());

                        return serviceMethod;
                    }).sorted((c1, c2) -> c1.getName().compareTo(c2.getName()))
                    .collect(Collectors.toList());
        }


        return getServiceMethodList(classInfo.getSourceCode());
    }

    @Override
    public List<LcdpTableFieldDTO> selectServiceColumnList(RestJsonWrapperBean wrapper) {
        String sql = wrapper.getParamValue("sql");
        List<LcdpTableFieldDTO> sqlFieldList = new ArrayList<>();
        if (!StringUtils.isBlank(sql)) {
            try {
                Map<String, Class<?>> columnTypeMap = MybatisHelper.getColumnTypeMap(sql);

                if (!columnTypeMap.isEmpty()) {
                    sqlFieldList.addAll(columnTypeMap.entrySet().stream()
                            .map(e -> parse(e))
                            .collect(Collectors.toList()));

                }
            } catch (Exception ex) {
                logger.warn(ex.getMessage(), ex);
            }
        }

        String table = wrapper.getParamValue("table");
        if (!StringUtils.isBlank(table)) {
            LcdpTableDTO lcdpTable = tableService.selectVirtualTableInfo(table.toUpperCase());

            if (sqlFieldList.isEmpty()) {
                return lcdpTable.getFieldList();
            }

            List<LcdpTableFieldDTO> returnFieldList = new ArrayList<>();
            for (LcdpTableFieldDTO sqlField : sqlFieldList) {
                LcdpTableFieldDTO tableField = lcdpTable.getFieldList().stream()
                        .filter(f -> StringUtils.equalsIgnoreCase(f.getFieldName(), sqlField.getFieldName()))
                        .findAny().orElse(null);

                if (tableField == null) {
                    returnFieldList.add(sqlField);
                } else {
                    returnFieldList.add(tableField);
                }
            }

            return returnFieldList;
        }

        return sqlFieldList;
    }

    @Override
    @Transactional
    public List<LcdpResourceBean> postUpdate(LcdpResourceHistoryBean resourcePageHistory, List<LcdpModulePageCompBean> insertPageCompList) {
        if (CollectionUtils.isEmpty(insertPageCompList)) {
            return CollectionUtils.emptyList();
        }

        // 用于新增
        // key为path
        Map<String, Map<String, String>> insertSourceCodeMap = new HashMap<>();
        Map<String, List<String>> insertMapperMap = new HashMap<>();

        // 用于更新
        // key为path，pair里的first是方法名second是方法
        Map<String, Map<String, Pair<String, String>>> updateSourceCodeMap = new HashMap<>();
        // key为path，pair里的first是mapperId,second是sql或表
        Map<String, List<Pair<String, String>>> updateMapperMap = new HashMap<>();

        for (LcdpModulePageCompBean insertPageComp : insertPageCompList) {
            if (!LcdpConstant.RESOURCE_COPY_COMP_TYPE_LIST.contains(insertPageComp.getType())
                    || StringUtils.isBlank(insertPageComp.getConfig())) {
                continue;
            }

            String config = insertPageComp.getConfig();

            JSONObject configJSONObject = JSONObject.parseObject(config);

            // 原来数据源配置，不做任何调整
            if (!StringUtils.isBlank(configJSONObject.getString("url"))) {
                continue;
            }

            // v12转低代码，不需要处理零代码相关逻辑
            if (!StringUtils.isBlank(configJSONObject.getString("service")) && !configJSONObject.getString("service").contains(".server.")) {
                continue;
            }


            if ("Grid".equals(insertPageComp.getType())) {
                LcdpPageGridConfigDTO gridConfig = parseConfig(configJSONObject, LcdpPageGridConfigDTO.class);

                if (StringUtils.isBlank(gridConfig.getService())) {
                    continue;
                }

                gridConfig.setUpdateMapper(Constant.YES.equals(insertPageComp.getExt$Item(LcdpConstant.LCDP_MAPPER_UPDATE_KEY))); // 是否需要更新mybatis
                gridConfig.setUpdateTable(Constant.YES.equals(insertPageComp.getExt$Item(LcdpConstant.LCDP_TABLE_UPDATE_KEY))); // 是否需要更新获取table方法
                gridConfig.setUpdateSetIdMethod(Constant.YES.equals(insertPageComp.getExt$Item(LcdpConstant.LCDP_SETID_UPDATE_KEY))); // 是否需要更新setId方法

                // 添加库表必需的字段
                tableService.insertDefaultFieldsIfMissing(gridConfig.getTable(), getTableFunctionList(gridConfig));

                // 获取默认服务名称，前后端逻辑要保持一致
                int lastDotPos = resourcePageHistory.getPath().lastIndexOf(".");
                String classSimpleName = resourcePageHistory.getPath().substring(lastDotPos + 1);
                if (!StringUtils.startsWith(classSimpleName, "Lcdp")) {
                    classSimpleName = "Lcdp" + classSimpleName;
                }
//                String defaultService = StringUtils.removeEnd(resourcePageHistory.getPath().substring(0, lastDotPos), ".page")
//                        + ".server." + StringUtils.removeEnd(classSimpleName, "Page") + "Service";
//                
//                // 和默认的数据服务一样
//                if (defaultService.equals(gridConfig.getService())) {
                LcdpResourceBean resource = resourceService.getByPath(gridConfig.getService());

                if (resource == null
                        || Constant.YES.equals(resource.getDeleteFlag())) { // 新增
                    createDataForInsertServiceByGrid(gridConfig, insertSourceCodeMap, insertMapperMap);
                } else {
                    createDataForUpdateServiceByGrid(resource, gridConfig, updateSourceCodeMap, updateMapperMap);
                }
//                }
            } else if ("Form".equals(insertPageComp.getType())) {
                LcdpPageFormConfigDTO formConfig = parseConfig(configJSONObject, LcdpPageFormConfigDTO.class);

                if (StringUtils.isBlank(formConfig.getService())) {
                    continue;
                }

                formConfig.setUpdateMapper(Constant.YES.equals(insertPageComp.getExt$Item(LcdpConstant.LCDP_MAPPER_UPDATE_KEY))); // 是否需要更新mybatis
                formConfig.setUpdateTable(Constant.YES.equals(insertPageComp.getExt$Item(LcdpConstant.LCDP_TABLE_UPDATE_KEY))); // 是否需要更新获取table方法
                formConfig.setUpdateSetIdMethod(Constant.YES.equals(insertPageComp.getExt$Item(LcdpConstant.LCDP_SETID_UPDATE_KEY))); // 是否需要更新setId方法

                // 添加库表必需的字段
                tableService.insertDefaultFieldsIfMissing(formConfig.getTable(), getTableFunctionList(formConfig));

                // 获取默认服务名称，前后端逻辑要保持一致
                int lastDotPos = resourcePageHistory.getPath().lastIndexOf(".");
                String classSimpleName = resourcePageHistory.getPath().substring(lastDotPos + 1);
                if (!StringUtils.startsWith(classSimpleName, "Lcdp")) {
                    classSimpleName = "Lcdp" + classSimpleName;
                }
//                String defaultService = StringUtils.removeEnd(resourcePageHistory.getPath().substring(0, lastDotPos), ".page")
//                        + ".server." + StringUtils.removeEnd(classSimpleName, "Page") + "Service";
//                
//                // 和默认的数据服务一样
//                if (defaultService.equals(formConfig.getService())) {
                LcdpResourceBean resource = resourceService.getByPath(formConfig.getService());

                if (resource == null
                        || Constant.YES.equals(resource.getDeleteFlag())) { // 新增
                    createDataForInsertServiceByForm(formConfig, insertSourceCodeMap, insertMapperMap);
                } else { // 更新
                    createDataForUpdateServiceByForm(resource, formConfig, updateSourceCodeMap, updateMapperMap);
                }
//                }
            } else if ("TreeGrid".equals(insertPageComp.getType())) {
                LcdpPageTreeGridConfigDTO treeGridConfig = parseConfig(configJSONObject, LcdpPageTreeGridConfigDTO.class);

                if (StringUtils.isBlank(treeGridConfig.getService())) {
                    continue;
                }

                treeGridConfig.setUpdateMapper(Constant.YES.equals(insertPageComp.getExt$Item(LcdpConstant.LCDP_MAPPER_UPDATE_KEY))); // 是否需要更新mybatis
                treeGridConfig.setUpdateTable(Constant.YES.equals(insertPageComp.getExt$Item(LcdpConstant.LCDP_TABLE_UPDATE_KEY))); // 是否需要更新获取table方法
                treeGridConfig.setUpdateSetIdMethod(Constant.YES.equals(insertPageComp.getExt$Item(LcdpConstant.LCDP_SETID_UPDATE_KEY))); // 是否需要更新setId方法

                // 添加库表必需的字段
                tableService.insertDefaultFieldsIfMissing(treeGridConfig.getTable(), getTableFunctionList(treeGridConfig));

                // 获取默认服务名称，前后端逻辑要保持一致
                int lastDotPos = resourcePageHistory.getPath().lastIndexOf(".");
                String classSimpleName = resourcePageHistory.getPath().substring(lastDotPos + 1);
                if (!StringUtils.startsWith(classSimpleName, "Lcdp")) {
                    classSimpleName = "Lcdp" + classSimpleName;
                }
//                String defaultService = StringUtils.removeEnd(resourcePageHistory.getPath().substring(0, lastDotPos), ".page")
//                        + ".server." + StringUtils.removeEnd(classSimpleName, "Page") + "Service";
//                
//                // 和默认的数据服务一样
//                if (defaultService.equals(treeGridConfig.getService())) {
                LcdpResourceBean resource = resourceService.getByPath(treeGridConfig.getService());

                if (resource == null
                        || Constant.YES.equals(resource.getDeleteFlag())) { // 新增
                    createDataForInsertServiceByTreeGrid(treeGridConfig, insertSourceCodeMap, insertMapperMap);
                } else { // 更新
                    createDataForUpdateServiceByTreeGrid(resource, treeGridConfig, updateSourceCodeMap, updateMapperMap);
                }
//                }
            }
        }

        List<LcdpResourceBean> insertResourceList = new ArrayList<>();
        List<LcdpResourceHistoryBean> insertResourceHistoryList = new ArrayList<>();

        List<LcdpResourceHistoryBean> updateResourceHistoryList = new ArrayList<>();

        String currentDatabaseMapperSuffix = LcdpConstant.PROFILE_DB_LIST.stream()
                .filter(profile -> ApplicationContextHelper.isProfileActivated(profile))
                .findFirst()
                .get() + "Mapper";

        //---------------------------------------------------------------------------------
        // 更新资源
        //---------------------------------------------------------------------------------
        // Map<String, Map<LcdpSourceCodeMethodType, Pair<String, String>>>
        updateSourceCodeMap.forEach((s, m) -> {
            LcdpResourceBean resource = resourceService.getByPath(s);

            List<LcdpResourceHistoryBean> resourceHistoryList = resourceHistoryService.selectListByResourceId(resource.getId());

            LcdpResourceHistoryBean resourceHistory = (CollectionUtils.isEmpty(resourceHistoryList) ? null : resourceHistoryList.get(0));

            // 源代码资源是自己checkout的
            boolean myResource = resourceHistory != null
                    && "1".equals(resourceLockService.getLockStatus(LocalContextHelper.getLoginUserId(), "" + resource.getId(), LcdpConstant.RESOURCE_CATEGORY_JAVA));

            String sourceCode = (resourceHistory == null ? resource.getContent() : resourceHistory.getContent());

            boolean updated = false;

            for (Entry<String, Pair<String, String>> entry : m.entrySet()) {
                LcdpSourceCodeMethodType methodType = LcdpSourceCodeMethodType.valueOf(entry.getKey().substring(entry.getKey().lastIndexOf("$") + 1));

                if (LcdpSourceCodeMethodType.COMPONNET_TABLE.equals(methodType)
                        || LcdpSourceCodeMethodType.GRID_SETID.equals(methodType)
                        || LcdpSourceCodeMethodType.FORM_SETID.equals(methodType)) {
                    sourceCode = LcdpSourceCodeManager.getReplaceSimpleMethodJavaCode(sourceCode,
                            methodType,
                            entry.getValue().getFirst(),
                            entry.getValue().getSecond());

                    updated = true;
                } else if (!isMappingMethodExists(sourceCode, entry.getValue().getFirst())) {
                    if (resourceHistory != null
                            && !myResource) {
                        throw new ApplicationRuntimeException("LCDP.MODULE.RESOUCES.TIP.UNABLE_UPDATE_OTHER_CHECKOUT_RESOURCE",
                                resource.getPath());
                    }

                    sourceCode = LcdpSourceCodeManager.getInsertMethodJavaCode(sourceCode, methodType, entry.getValue().getSecond());

                    updated = true;
                }
            }

            if (updated) {
                if (resourceHistory == null) {
                    resourceHistory = new LcdpResourceHistoryBean();
                    BeanUtils.copyProperties(resource, resourceHistory);

                    resourceHistory.setId(ApplicationContextHelper.getNextIdentity());
                    resourceHistory.setResourceId(resource.getId());

                    resourceService.initResourceHistory(resourceHistory);
                    resourceHistory.setCompiledVersion(resourceHistory.getModifyVersion());

                    String classContent = LcdpJavaCodeResolverUtils.getClassContent(sourceCode,
                            resourceHistory.getResourceName(),
                            resourceHistory.getVersion(),
                            resourceHistory.getModifyVersion(),
                            resourceHistory.getVersionOffset());
                    classContent = resourceService.replaceScriptContent(classContent);
                    resourceHistory.setClassContent(classContent);

                    insertResourceHistoryList.add(resourceHistory);
                } else {
                    resourceHistory.setModifyVersion(Optional.ofNullable(resourceHistory.getModifyVersion()).orElse(0L) + 1L);

                    resourceHistory.setContent(sourceCode);

                    String classContent = LcdpJavaCodeResolverUtils.getClassContent(sourceCode,
                            resourceHistory.getResourceName(),
                            resourceHistory.getVersion(),
                            resourceHistory.getModifyVersion(),
                            resourceHistory.getVersionOffset());
                    classContent = resourceService.replaceScriptContent(classContent);
                    resourceHistory.setClassContent(classContent);

                    updateResourceHistoryList.add(resourceHistory);
                }
            }
        });

        // key为path，pair里的first是mapperId,second是sql
        // Map<String, List<Pair<String, String>>>
        updateMapperMap.forEach((s, l) -> {
            String pathPrefix = StringUtils.removeEnd(s, "Mapper");

            List<LcdpResourceBean> resourceList = resourceService.selectListByFilter(SearchFilter.instance()
                    .match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_MAPPER).filter(MatchPattern.EQ)
                    .match("PATH", pathPrefix).filter(MatchPattern.SB)
                    .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));

            List<LcdpResourceHistoryBean> resourceHistoryList = resourceHistoryService.selectListByFilter(SearchFilter.instance()
                    .match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_MAPPER).filter(MatchPattern.EQ)
                    .match("PATH", pathPrefix).filter(MatchPattern.SB)
                    .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));

            LcdpConstant.MAPPER_TMPL_NAME_LIST.forEach(m -> {
                LcdpResourceBean resource = resourceList.stream().filter(r -> StringUtils.endsWith(r.getResourceName(), m)).findAny().orElse(null);

                if (resource == null) {
                    return;
                }

                LcdpResourceHistoryBean resourceHistory = resourceHistoryList.stream().filter(r -> StringUtils.endsWith(r.getResourceName(), m)).findAny().orElse(null);

                if (resourceHistory != null) {
                    // 源代码资源是自己checkout的
                    boolean myResource = "1".equals(resourceLockService.getLockStatus(LocalContextHelper.getLoginUserId(),
                            "" + resource.getId(), LcdpConstant.RESOURCE_CATEGORY_MAPPER));

                    if (!myResource) { // 源代码资源不是自己的
                        if (!StringUtils.endsWith(resource.getResourceName(), currentDatabaseMapperSuffix)) { // 该mapper不属于当前数据库的，可以忽略不更新
                            return;
                        }

                        throw new ApplicationRuntimeException("LCDP.MODULE.RESOUCES.TIP.UNABLE_UPDATE_OTHER_CHECKOUT_RESOURCE",
                                resource.getResourceName());
                    }
                }

                String mybatisXml = (resourceHistory == null ? resource.getContent() : resourceHistory.getContent());

                for (Pair<String, String> pair : l) {
                    if (isMybatisXmlMapperIdExists(mybatisXml, pair.getFirst())) {
                        mybatisXml = LcdpSourceCodeManager.getReplaceSelectSqlStatement(pair.getFirst(), mybatisXml, pair.getSecond());
                    } else {
                        String sql = null;
                        if (pair.getFirst().startsWith("selectDetailById_")) { // 表单
                            sql = StringUtils.startsWithIgnoreCase(pair.getSecond().trim(), "select")
                                    ? LcdpSourceCodeManager.getFormSelectSqlStatement(pair.getFirst(), pair.getSecond())
                                    : LcdpSourceCodeManager.getFormSelectTableStatement(pair.getFirst(), pair.getSecond());
                        } else if (pair.getFirst().startsWith("selectByCondition_")) { // 表格
                            sql = StringUtils.startsWithIgnoreCase(pair.getSecond().trim(), "select")
                                    ? LcdpSourceCodeManager.getGridSelectSqlStatement(pair.getFirst(), pair.getSecond())
                                    : LcdpSourceCodeManager.getGridSelectTableStatement(pair.getFirst(), pair.getSecond());
                        } else if (pair.getFirst().startsWith("selectTreeByCondition_")) { // 树表格
                            sql = StringUtils.startsWithIgnoreCase(pair.getSecond().trim(), "select")
                                    ? LcdpSourceCodeManager.getTreeGridSelectSqlStatement(pair.getFirst(), pair.getSecond())
                                    : LcdpSourceCodeManager.getTreeGridSelectTableStatement(pair.getFirst(), pair.getSecond());
                        }

                        mybatisXml = LcdpSourceCodeManager.getInsertSelectSqlStatement(pair.getFirst(), mybatisXml, sql);
                    }
                }

                if (resourceHistory == null) {
                    resourceHistory = new LcdpResourceHistoryBean();
                    BeanUtils.copyProperties(resource, resourceHistory);

                    resourceHistory.setId(ApplicationContextHelper.getNextIdentity());
                    resourceHistory.setResourceId(resource.getId());

                    resourceService.initResourceHistory(resourceHistory);
                    resourceHistory.setContent(mybatisXml);

                    insertResourceHistoryList.add(resourceHistory);
                } else {
                    resourceHistory.setContent(mybatisXml);

                    updateResourceHistoryList.add(resourceHistory);
                }
            });
        });

        //---------------------------------------------------------------------------------
        // 新增资源
        //---------------------------------------------------------------------------------
        LcdpResourceBean resourcePage = resourceService.getDao().selectColumnsByIdIfPresent(resourcePageHistory.getResourceId(), "ID", "RESOURCEDESC", "PARENTID");

        insertSourceCodeMap.forEach((s, m) -> {
            String sourceCode = LcdpSourceCodeManager.getJavaCode(s, m);

            LcdpResourceBean sourceCodeResource = new LcdpResourceBean();
            sourceCodeResource.setId(ApplicationContextHelper.getNextIdentity());
            sourceCodeResource.setResourceName(getResourceName(s));
            sourceCodeResource.setResourceDesc(resourcePage.getResourceDesc());
            sourceCodeResource.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_JAVA);
            sourceCodeResource.setParentId(resourcePage.getParentId());
            sourceCodeResource.setScriptType(ServerScriptType.service.name());
            sourceCodeResource.setDeleteFlag(LcdpConstant.RESOURCE_DELETED_NO);
            sourceCodeResource.setContent(sourceCode);

            resourceService.updatePath(sourceCodeResource);
            resourceService.updateVersionOffset(sourceCodeResource);

            insertResourceList.add(sourceCodeResource);

            LcdpResourceHistoryBean sourceCodeResourceHistory = new LcdpResourceHistoryBean();
            BeanUtils.copyProperties(sourceCodeResource, sourceCodeResourceHistory);

            sourceCodeResourceHistory.setId(ApplicationContextHelper.getNextIdentity());
            sourceCodeResourceHistory.setResourceId(sourceCodeResource.getId());

            resourceService.initResourceHistory(sourceCodeResourceHistory);
            sourceCodeResourceHistory.setCompiledVersion(sourceCodeResourceHistory.getModifyVersion());

            String classContent = LcdpJavaCodeResolverUtils.getClassContent(sourceCodeResourceHistory.getContent(),
                    sourceCodeResourceHistory.getResourceName(),
                    sourceCodeResourceHistory.getVersion(),
                    sourceCodeResourceHistory.getModifyVersion(),
                    sourceCodeResourceHistory.getVersionOffset());
            classContent = resourceService.replaceScriptContent(classContent);
            sourceCodeResourceHistory.setClassContent(classContent);

            insertResourceHistoryList.add(sourceCodeResourceHistory);
        });

        insertMapperMap.forEach((n, s) -> {
            LcdpConstant.MAPPER_TMPL_NAME_LIST.forEach(mapperName -> {
                if (LcdpScriptUtils.validateCurrentDBMybatisMapper()
                        && !StringUtils.equalsIgnoreCase(mapperName, currentDatabaseMapperSuffix)) {
                    return;
                }

                String path = StringUtils.removeEnd(n, "Mapper") + mapperName;

                LcdpResourceBean mapper = new LcdpResourceBean();
                mapper.setId(ApplicationContextHelper.getNextIdentity());
                mapper.setResourceName(getResourceName(path));
                mapper.setResourceDesc(resourcePage.getResourceDesc());
                mapper.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_MAPPER);
                mapper.setParentId(resourcePage.getParentId());
                mapper.setEffectVersion(0L);

                resourceService.updatePath(mapper);

                insertResourceList.add(mapper);

                LcdpResourceHistoryBean mapperHistory = new LcdpResourceHistoryBean();
                mapperHistory.setId(ApplicationContextHelper.getNextIdentity());
                mapperHistory.setResourceName(mapper.getResourceName());
                mapperHistory.setResourceDesc(mapper.getResourceDesc());
                mapperHistory.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_MAPPER);
                mapperHistory.setPath(mapper.getPath());
                mapperHistory.setResourceId(mapper.getId());

                resourceService.initResourceHistory(mapperHistory);

                mapperHistory.setContent(LcdpSourceCodeManager.getMapper(n, s));

                insertResourceHistoryList.add(mapperHistory);
            });
        });

        //---------------------------------------------------------------------------------
        // 处理数据
        //---------------------------------------------------------------------------------
        if (updateResourceHistoryList.isEmpty()
                && insertResourceList.isEmpty()
                && insertResourceHistoryList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        // 更新修改版本和类内容
        resourceHistoryService.getDao().update(updateResourceHistoryList, "MODIFYVERSION", "CONTENT", "CLASSCONTENT");

        // mapper文件需要加载
        List<LcdpResourceHistoryBean> mapperResourceList = new ArrayList<>();
        updateResourceHistoryList.stream()
                .filter(h -> LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(h.getResourceCategory()))
                .forEach(h -> mapperResourceList.add(h));

        // 新增资源历史表，没有新增资源时，重新加载mapper，新增资源返回后会加载mapper
        insertResourceHistoryList.stream()
                .filter(h -> LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(h.getResourceCategory())
                        && insertResourceList.stream().noneMatch(r -> Objects.equals(r.getId(), h.getResourceId())))
                .forEach(h -> mapperResourceList.add(h));

        if (!mapperResourceList.isEmpty()) {
            TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> {
                mapperResourceList.forEach(h -> LcdpMapperUtils.loadMapper(h.getPath(), false, h.getContent()));
            });
        }

        resourceService.getDao().fastInsert(insertResourceList);
        resourceHistoryService.getDao().fastInsert(insertResourceHistoryList);

        return insertResourceList;
    }

    //--------------------------------------------------------------------------------------------------------------
    // 私有方法
    //--------------------------------------------------------------------------------------------------------------
    private <T> T parseConfig(JSONObject configJSONObject, Class<T> configClass) {
        T item = ClassUtils.newInstance(configClass);
        List<FieldWrapper> fieldWrapperList = ReflectionUtils.getFieldWrapperList(configClass);
        for (FieldWrapper fieldWrapper : fieldWrapperList) {
            Field field = fieldWrapper.getField();

            if (Modifier.isPrivate(field.getModifiers())
                    && !Modifier.isStatic(field.getModifiers())) {
                String value = configJSONObject.getString(field.getName());

                ReflectionUtils.invokeWriteMethod(item, field.getName(), ConvertUtils.convert(value, field.getType()));
            }
        }

        return item;
    }


    // 获取Mapping注解的方法
    private static Pattern classMappingMethodPattern = Pattern.compile("@Mapping\\([[^\\)]*\\s]?value\\s*=\\s*\"(?<mappingValue>[^\"]+)\"[^\\)]*\\)[\\s\\S]*?(public|private|protected)\\s+((final|static)\\s+)*(?<returnType>[A-Za-z0-9_]+(\\<.*\\>)?)\\s+(?<methodName>[A-Za-z0-9_]+)\\((?<methodParam>[^\\)]+)?\\)");

    /**
     * 获取源代码中Mapping注解的方法，并返回方法名和Mapping中的描述
     */
    private final List<LcdpPageServiceMethodDTO> getServiceMethodList(String sourceCode) {
        // 删除注释
        sourceCode = LcdpJavaCodeResolverUtils.getNocommentJavaCode(sourceCode);

        List<LcdpPageServiceMethodDTO> serviceMethodList = new ArrayList<>();

        // 提取源代码中的方法
        Matcher methodMatcher = classMappingMethodPattern.matcher(sourceCode);
        while (methodMatcher.find()) {
            String methodName = methodMatcher.group("methodName");

            String mappingValue = methodMatcher.group("mappingValue");

            LcdpPageServiceMethodDTO serviceMethod = new LcdpPageServiceMethodDTO();
            serviceMethod.setName(methodName);
            serviceMethod.setDesc(mappingValue);
            serviceMethodList.add(serviceMethod);
        }

        // 提取父类中的成员变量
        String extendClassFullName = LcdpReflectionUtils.getSuperClassFullName(sourceCode);
        if (!StringUtils.isBlank(extendClassFullName)) {
            Class<?> clazz = ClassManager.getClassByFullName(extendClassFullName);

            if (clazz == null) {
                throw new ApplicationRuntimeException("LCDP.EXCEPTION.CLASS_NOT_EXISTS", extendClassFullName);
            }

            List<Method> methodList = ReflectionUtils.getMethodList(clazz, m -> m.isAnnotationPresent(Mapping.class) && !Modifier.isPrivate(m.getModifiers()));
            for (Method method : methodList) {
                String methodName = method.getName();

                if (serviceMethodList.stream().noneMatch(m -> m.getName().equals(methodName))) {
                    Mapping mapping = method.getAnnotation(Mapping.class);

                    LcdpPageServiceMethodDTO serviceMethod = new LcdpPageServiceMethodDTO();
                    serviceMethod.setName(methodName);
                    serviceMethod.setDesc(mapping.value());
                    serviceMethodList.add(serviceMethod);
                }
            }
        }

        // 提取接口类中的成员变量
        List<String> implementedClassFullNameList = LcdpReflectionUtils.getImplementedClassFullNameList(sourceCode);
        for (String implementedClassFullName : implementedClassFullNameList) {
            Class<?> clazz = ClassManager.getClassByFullName(implementedClassFullName);

            if (clazz == null) {
                throw new ApplicationRuntimeException("LCDP.EXCEPTION.CLASS_NOT_EXISTS", implementedClassFullName);
            }

            List<Method> methodList = ReflectionUtils.getMethodList(clazz, m -> m.isAnnotationPresent(Mapping.class) && !Modifier.isPrivate(m.getModifiers()));
            for (Method method : methodList) {
                String methodName = method.getName();

                if (serviceMethodList.stream().noneMatch(m -> m.getName().equals(methodName))) {
                    Mapping mapping = method.getAnnotation(Mapping.class);

                    LcdpPageServiceMethodDTO serviceMethod = new LcdpPageServiceMethodDTO();
                    serviceMethod.setName(methodName);
                    serviceMethod.setDesc(mapping.value());
                    serviceMethodList.add(serviceMethod);
                }
            }
        }

        return serviceMethodList;
    }

    private void createDataForInsertServiceByGrid(LcdpPageGridConfigDTO gridConfig,
                                                  Map<String, Map<String, String>> insertSourceCodeMap,
                                                  Map<String, List<String>> insertMapperMap) {
        Map<String, String> methodSourceCodeMap = new HashMap<>();

        // 默认的mapperId
        String defaultNamespace = getDefaultMapperNamespace(gridConfig.getService());
        String defaultMapperId = "selectByCondition_" + gridConfig.getFunctionSuffixId();

        if (!StringUtils.isBlank(gridConfig.getTable())) { // 组件的表
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.COMPONNET_TABLE,
                    LcdpSourceCodeManager.getComponentTableJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getTable()));
        }

        if (!StringUtils.isBlank(gridConfig.getSelectMethod())) { // 查询功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_PAGINATION,
                    LcdpSourceCodeManager.GridComponent.getGridSelectPaginationJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getSelectMethod(),
                            defaultNamespace + "." + defaultMapperId));
        }

        if (!StringUtils.isBlank(gridConfig.getInsertMethod())) { // 新增功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_SETID,
                    LcdpSourceCodeManager.GridComponent.getGridSetIdJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getIdAutoGen(), gridConfig.getIdGenSequence()));

            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_INSERT,
                    LcdpSourceCodeManager.GridComponent.getGridInsertJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getInsertMethod(), gridConfig.getIdAutoGen(), gridConfig.getIdGenSequence()));
        }

        if (!StringUtils.isBlank(gridConfig.getDeleteMethod())) { // 删除功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_DELETE,
                    LcdpSourceCodeManager.GridComponent.getGridDeleteJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getDeleteMethod()));
        }

        if (!StringUtils.isBlank(gridConfig.getUpdateMethod())
                || Boolean.TRUE.equals(gridConfig.getAutoSave())) { // 更新功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_UPDATE,
                    LcdpSourceCodeManager.GridComponent.getGridUpdateJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getUpdateMethod()));
        }

        if (!StringUtils.isBlank(gridConfig.getActivateMethod())) { // 启用功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_ACTIVATE,
                    LcdpSourceCodeManager.GridComponent.getGridActivateJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getActivateMethod()));
        }

        if (!StringUtils.isBlank(gridConfig.getDeactivateMethod())) { // 停用功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_DEACTIVATE,
                    LcdpSourceCodeManager.GridComponent.getGridDeactivateJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getDeactivateMethod()));
        }

        if (!StringUtils.isBlank(gridConfig.getStartProcessMethod())) { // 流程提交功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_STARTPROCESS,
                    LcdpSourceCodeManager.GridComponent.getGridStartProcessJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getStartProcessMethod()));
        }

        if (!StringUtils.isBlank(gridConfig.getWithdrawProcessMethod())) { // 流程撤回功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_WITHDRAWPROCESS,
                    LcdpSourceCodeManager.GridComponent.getGridWithdrawProcessJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getWithdrawProcessMethod()));
        }

        if (!StringUtils.isBlank(gridConfig.getCompleteTaskMethod())) { // 审核通过功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_COMPLETETASK,
                    LcdpSourceCodeManager.GridComponent.getGridCompleteTaskJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getCompleteTaskMethod()));
        }

        if (!StringUtils.isBlank(gridConfig.getRejectTaskMethod())) { // 审核拒绝功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_REJECTTASK,
                    LcdpSourceCodeManager.GridComponent.getGridRejectTaskJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getRejectTaskMethod()));
        }

        if (!StringUtils.isBlank(gridConfig.getStartProcessMethod())
                || !StringUtils.isBlank(gridConfig.getCompleteTaskMethod())) { // 流程任务节点状态功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_PROCESSTASKSTATUS,
                    LcdpSourceCodeManager.GridComponent.getGridProcessTaskStatus(gridConfig.getFunctionSuffixId()));
        }

        if (insertSourceCodeMap.containsKey(gridConfig.getService())) {
            insertSourceCodeMap.get(gridConfig.getService()).putAll(methodSourceCodeMap);
        } else {
            insertSourceCodeMap.put(gridConfig.getService(), methodSourceCodeMap);
        }

        if (!StringUtils.isBlank(gridConfig.getSelectMethod())) { // 查询功能
            if (!StringUtils.isBlank(gridConfig.getSql())) {
                if (insertMapperMap.containsKey(defaultNamespace)) {
                    insertMapperMap.get(defaultNamespace).add(LcdpSourceCodeManager.getGridSelectSqlStatement(defaultMapperId, gridConfig.getSql()));
                } else {
                    insertMapperMap.put(defaultNamespace, ArrayUtils.asList(LcdpSourceCodeManager.getGridSelectSqlStatement(defaultMapperId, gridConfig.getSql())));
                }
            } else if (!StringUtils.isBlank(gridConfig.getTable())) {
                if (insertMapperMap.containsKey(defaultNamespace)) {
                    insertMapperMap.get(defaultNamespace).add(LcdpSourceCodeManager.getGridSelectTableStatement(defaultMapperId, gridConfig.getTable()));
                } else {
                    insertMapperMap.put(defaultNamespace, ArrayUtils.asList(LcdpSourceCodeManager.getGridSelectTableStatement(defaultMapperId, gridConfig.getTable())));
                }
            }
        }
    }

    private void createDataForUpdateServiceByGrid(LcdpResourceBean serviceResource, LcdpPageGridConfigDTO gridConfig,
                                                  Map<String, Map<String, Pair<String, String>>> updateSourceCodeMap,
                                                  Map<String, List<Pair<String, String>>> updateMapperMap) {
        Map<String, Pair<String, String>> methodSourceCodeMap = new HashMap<>();

        // 默认的mapperId
        String defaultNamespace = getDefaultMapperNamespace(gridConfig.getService());
        String defaultMapperId = "selectByCondition_" + gridConfig.getFunctionSuffixId();

        if (gridConfig.isUpdateTable()
                && !StringUtils.isBlank(gridConfig.getTable())) { // 组件对应的表
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.COMPONNET_TABLE,
                    Pair.of("get" + gridConfig.getFunctionSuffixId() + "Table",
                            LcdpSourceCodeManager.getComponentTableJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getTable())));
        }

        if (!StringUtils.isBlank(gridConfig.getSelectMethod())) { // 查询功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_PAGINATION,
                    Pair.of(gridConfig.getSelectMethod(),
                            LcdpSourceCodeManager.GridComponent.getGridSelectPaginationJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getSelectMethod(),
                                    defaultNamespace + "." + defaultMapperId)));
        }

        if (!StringUtils.isBlank(gridConfig.getInsertMethod())) { // 新增功能
            if (gridConfig.isUpdateSetIdMethod()) {
                methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_SETID,
                        Pair.of("setId_" + gridConfig.getFunctionSuffixId(),
                                LcdpSourceCodeManager.GridComponent.getGridSetIdJavaCode(gridConfig.getFunctionSuffixId(),
                                        gridConfig.getIdAutoGen(), gridConfig.getIdGenSequence())));
            }

            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_INSERT,
                    Pair.of(gridConfig.getInsertMethod(),
                            LcdpSourceCodeManager.GridComponent.getGridInsertJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getInsertMethod(), gridConfig.getIdAutoGen(), gridConfig.getIdGenSequence())));
        }

        if (!StringUtils.isBlank(gridConfig.getDeleteMethod())) { // 删除功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_DELETE,
                    Pair.of(gridConfig.getDeleteMethod(),
                            LcdpSourceCodeManager.GridComponent.getGridDeleteJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getDeleteMethod())));
        }

        if (!StringUtils.isBlank(gridConfig.getUpdateMethod())
                || Boolean.TRUE.equals(gridConfig.getAutoSave())) { // 更新功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_UPDATE,
                    Pair.of(gridConfig.getUpdateMethod(),
                            LcdpSourceCodeManager.GridComponent.getGridUpdateJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getUpdateMethod())));
        }

        if (!StringUtils.isBlank(gridConfig.getActivateMethod())) { // 启用功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_ACTIVATE,
                    Pair.of(gridConfig.getActivateMethod(),
                            LcdpSourceCodeManager.GridComponent.getGridActivateJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getActivateMethod())));
        }

        if (!StringUtils.isBlank(gridConfig.getDeactivateMethod())) { // 停用功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_DEACTIVATE,
                    Pair.of(gridConfig.getDeactivateMethod(),
                            LcdpSourceCodeManager.GridComponent.getGridDeactivateJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getDeactivateMethod())));
        }

        if (!StringUtils.isBlank(gridConfig.getStartProcessMethod())) { // 流程提交功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_STARTPROCESS,
                    Pair.of(gridConfig.getStartProcessMethod(),
                            LcdpSourceCodeManager.GridComponent.getGridStartProcessJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getStartProcessMethod())));
        }

        if (!StringUtils.isBlank(gridConfig.getWithdrawProcessMethod())) { // 流程撤回功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_WITHDRAWPROCESS,
                    Pair.of(gridConfig.getWithdrawProcessMethod(),
                            LcdpSourceCodeManager.GridComponent.getGridWithdrawProcessJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getWithdrawProcessMethod())));
        }

        if (!StringUtils.isBlank(gridConfig.getCompleteTaskMethod())) { // 审核通过功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_COMPLETETASK,
                    Pair.of(gridConfig.getCompleteTaskMethod(),
                            LcdpSourceCodeManager.GridComponent.getGridCompleteTaskJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getCompleteTaskMethod())));
        }

        if (!StringUtils.isBlank(gridConfig.getRejectTaskMethod())) { // 审核拒绝功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_REJECTTASK,
                    Pair.of(gridConfig.getRejectTaskMethod(),
                            LcdpSourceCodeManager.GridComponent.getGridRejectTaskJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getRejectTaskMethod())));
        }

        if (!StringUtils.isBlank(gridConfig.getStartProcessMethod())
                || !StringUtils.isBlank(gridConfig.getCompleteTaskMethod())) { // 流程任务节点状态功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_PROCESSTASKSTATUS,
                    Pair.of(LcdpSourceCodeManager.getProcessTaskStatusMethod(gridConfig.getFunctionSuffixId()),
                            LcdpSourceCodeManager.GridComponent.getGridProcessTaskStatus(gridConfig.getFunctionSuffixId())));
        }

        if (updateSourceCodeMap.containsKey(gridConfig.getService())) {
            updateSourceCodeMap.get(gridConfig.getService()).putAll(methodSourceCodeMap);
        } else {
            updateSourceCodeMap.put(gridConfig.getService(), methodSourceCodeMap);
        }

        // 是否需要更新mybatis的mapper内容
        if (gridConfig.isUpdateMapper()
                && !StringUtils.isBlank(gridConfig.getSelectMethod())) {
            if (!StringUtils.isBlank(gridConfig.getSql())) {
                if (updateMapperMap.containsKey(defaultNamespace)) {
                    updateMapperMap.get(defaultNamespace).add(Pair.of(defaultMapperId, gridConfig.getSql()));
                } else {
                    updateMapperMap.put(defaultNamespace, ArrayUtils.asList(Pair.of(defaultMapperId, gridConfig.getSql())));
                }
            } else if (!StringUtils.isBlank(gridConfig.getTable())) {
                if (updateMapperMap.containsKey(defaultNamespace)) {
                    updateMapperMap.get(defaultNamespace).add(Pair.of(defaultMapperId, gridConfig.getTable()));
                } else {
                    updateMapperMap.put(defaultNamespace, ArrayUtils.asList(Pair.of(defaultMapperId, gridConfig.getTable())));
                }
            }
        }
    }

    private void createDataForInsertServiceByTreeGrid(LcdpPageTreeGridConfigDTO gridConfig,
                                                      Map<String, Map<String, String>> insertSourceCodeMap,
                                                      Map<String, List<String>> insertMapperMap) {
        Map<String, String> methodSourceCodeMap = new HashMap<>();

        // 默认的mapperId
        String defaultNamespace = getDefaultMapperNamespace(gridConfig.getService());
        String defaultMapperId = "selectTreeByCondition_" + gridConfig.getFunctionSuffixId();

        if (!StringUtils.isBlank(gridConfig.getTable())) { // 组件的表
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.COMPONNET_TABLE,
                    LcdpSourceCodeManager.getComponentTableJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getTable()));
        }

        if (!StringUtils.isBlank(gridConfig.getSelectMethod())) { // 查询功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_PAGINATION,
                    LcdpSourceCodeManager.TreeGridComponent.getTreeGridSelectPaginationJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getSelectMethod(),
                            defaultNamespace + "." + defaultMapperId));
        }

        if (!StringUtils.isBlank(gridConfig.getInsertMethod())) { // 新增功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_SETID,
                    LcdpSourceCodeManager.TreeGridComponent.getTreeGridSetIdJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getIdAutoGen(), gridConfig.getIdGenSequence()));

            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_INSERT,
                    LcdpSourceCodeManager.TreeGridComponent.getTreeGridInsertJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getInsertMethod(), gridConfig.getIdAutoGen(), gridConfig.getIdGenSequence()));
        }

        if (!StringUtils.isBlank(gridConfig.getDeleteMethod())) { // 删除功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_DELETE,
                    LcdpSourceCodeManager.TreeGridComponent.getTreeGridDeleteJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getDeleteMethod()));
        }

        if (!StringUtils.isBlank(gridConfig.getUpdateMethod())
                || Boolean.TRUE.equals(gridConfig.getAutoSave())) { // 更新功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_UPDATE,
                    LcdpSourceCodeManager.TreeGridComponent.getTreeGridUpdateJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getUpdateMethod()));
        }

        if (!StringUtils.isBlank(gridConfig.getMoveMethod())) { // 移动功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.TREEGRID_MOVE,
                    LcdpSourceCodeManager.TreeGridComponent.getTreeGridMoveJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getMoveMethod()));
        }

        if (!StringUtils.isBlank(gridConfig.getActivateMethod())) { // 启用功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_ACTIVATE,
                    LcdpSourceCodeManager.TreeGridComponent.getTreeGridActivateJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getActivateMethod()));
        }

        if (!StringUtils.isBlank(gridConfig.getDeactivateMethod())) { // 停用功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_DEACTIVATE,
                    LcdpSourceCodeManager.TreeGridComponent.getTreeGridDeactivateJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getDeactivateMethod()));
        }

        if (!StringUtils.isBlank(gridConfig.getStartProcessMethod())) { // 流程提交功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_STARTPROCESS,
                    LcdpSourceCodeManager.TreeGridComponent.getTreeGridStartProcessJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getStartProcessMethod()));
        }

        if (!StringUtils.isBlank(gridConfig.getWithdrawProcessMethod())) { // 流程撤回功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_WITHDRAWPROCESS,
                    LcdpSourceCodeManager.TreeGridComponent.getTreeGridWithdrawProcessJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getWithdrawProcessMethod()));
        }

        if (!StringUtils.isBlank(gridConfig.getCompleteTaskMethod())) { // 审核通过功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_COMPLETETASK,
                    LcdpSourceCodeManager.TreeGridComponent.getTreeGridCompleteTaskJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getCompleteTaskMethod()));
        }

        if (!StringUtils.isBlank(gridConfig.getRejectTaskMethod())) { // 审核拒绝功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_REJECTTASK,
                    LcdpSourceCodeManager.TreeGridComponent.getTreeGridRejectTaskJavaCode(gridConfig.getFunctionSuffixId(),
                            gridConfig.getRejectTaskMethod()));
        }

        if (!StringUtils.isBlank(gridConfig.getStartProcessMethod())
                || !StringUtils.isBlank(gridConfig.getCompleteTaskMethod())) { // 流程任务节点状态功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_PROCESSTASKSTATUS,
                    LcdpSourceCodeManager.TreeGridComponent.getTreeGridProcessTaskStatus(gridConfig.getFunctionSuffixId()));
        }

        if (insertSourceCodeMap.containsKey(gridConfig.getService())) {
            insertSourceCodeMap.get(gridConfig.getService()).putAll(methodSourceCodeMap);
        } else {
            insertSourceCodeMap.put(gridConfig.getService(), methodSourceCodeMap);
        }

        if (!StringUtils.isBlank(gridConfig.getSelectMethod())) { // 查询功能
            if (!StringUtils.isBlank(gridConfig.getSql())) {
                if (insertMapperMap.containsKey(defaultNamespace)) {
                    insertMapperMap.get(defaultNamespace).add(LcdpSourceCodeManager.getTreeGridSelectSqlStatement(defaultMapperId, gridConfig.getSql()));
                } else {
                    insertMapperMap.put(defaultNamespace, ArrayUtils.asList(LcdpSourceCodeManager.getTreeGridSelectSqlStatement(defaultMapperId, gridConfig.getSql())));
                }
            } else if (!StringUtils.isBlank(gridConfig.getTable())) {
                if (insertMapperMap.containsKey(defaultNamespace)) {
                    insertMapperMap.get(defaultNamespace).add(LcdpSourceCodeManager.getTreeGridSelectTableStatement(defaultMapperId, gridConfig.getTable()));
                } else {
                    insertMapperMap.put(defaultNamespace, ArrayUtils.asList(LcdpSourceCodeManager.getTreeGridSelectTableStatement(defaultMapperId, gridConfig.getTable())));
                }
            }
        }
    }

    private void createDataForUpdateServiceByTreeGrid(LcdpResourceBean serviceResource, LcdpPageTreeGridConfigDTO gridConfig,
                                                      Map<String, Map<String, Pair<String, String>>> updateSourceCodeMap,
                                                      Map<String, List<Pair<String, String>>> updateMapperMap) {
        Map<String, Pair<String, String>> methodSourceCodeMap = new HashMap<>();

        // 默认的mapperId
        String defaultNamespace = getDefaultMapperNamespace(gridConfig.getService());
        String defaultMapperId = "selectTreeByCondition_" + gridConfig.getFunctionSuffixId();

        if (gridConfig.isUpdateTable()
                && !StringUtils.isBlank(gridConfig.getTable())) { // 组件对应的表
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.COMPONNET_TABLE,
                    Pair.of("get" + gridConfig.getFunctionSuffixId() + "Table",
                            LcdpSourceCodeManager.getComponentTableJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getTable())));
        }

        if (!StringUtils.isBlank(gridConfig.getSelectMethod())) { // 查询功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_PAGINATION,
                    Pair.of(gridConfig.getSelectMethod(),
                            LcdpSourceCodeManager.TreeGridComponent.getTreeGridSelectPaginationJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getSelectMethod(),
                                    defaultNamespace + "." + defaultMapperId)));
        }

        if (!StringUtils.isBlank(gridConfig.getInsertMethod())) { // 新增功能
            if (gridConfig.isUpdateSetIdMethod()) {
                methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_SETID,
                        Pair.of("setId_" + gridConfig.getFunctionSuffixId(),
                                LcdpSourceCodeManager.TreeGridComponent.getTreeGridSetIdJavaCode(gridConfig.getFunctionSuffixId(),
                                        gridConfig.getIdAutoGen(), gridConfig.getIdGenSequence())));
            }

            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_INSERT,
                    Pair.of(gridConfig.getInsertMethod(),
                            LcdpSourceCodeManager.TreeGridComponent.getTreeGridInsertJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getInsertMethod(), gridConfig.getIdAutoGen(), gridConfig.getIdGenSequence())));
        }

        if (!StringUtils.isBlank(gridConfig.getDeleteMethod())) { // 删除功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_DELETE,
                    Pair.of(gridConfig.getDeleteMethod(),
                            LcdpSourceCodeManager.TreeGridComponent.getTreeGridDeleteJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getDeleteMethod())));
        }

        if (!StringUtils.isBlank(gridConfig.getUpdateMethod())
                || Boolean.TRUE.equals(gridConfig.getAutoSave())) { // 更新功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_UPDATE,
                    Pair.of(gridConfig.getUpdateMethod(),
                            LcdpSourceCodeManager.TreeGridComponent.getTreeGridUpdateJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getUpdateMethod())));
        }

        if (!StringUtils.isBlank(gridConfig.getMoveMethod())) { // 移动功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.TREEGRID_MOVE,
                    Pair.of(gridConfig.getMoveMethod(),
                            LcdpSourceCodeManager.TreeGridComponent.getTreeGridMoveJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getMoveMethod())));
        }

        if (!StringUtils.isBlank(gridConfig.getActivateMethod())) { // 启用功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_ACTIVATE,
                    Pair.of(gridConfig.getActivateMethod(),
                            LcdpSourceCodeManager.TreeGridComponent.getTreeGridActivateJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getActivateMethod())));
        }

        if (!StringUtils.isBlank(gridConfig.getDeactivateMethod())) { // 停用功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_DEACTIVATE,
                    Pair.of(gridConfig.getDeactivateMethod(),
                            LcdpSourceCodeManager.TreeGridComponent.getTreeGridDeactivateJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getDeactivateMethod())));
        }

        if (!StringUtils.isBlank(gridConfig.getStartProcessMethod())) { // 流程提交功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_STARTPROCESS,
                    Pair.of(gridConfig.getStartProcessMethod(),
                            LcdpSourceCodeManager.TreeGridComponent.getTreeGridStartProcessJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getStartProcessMethod())));
        }

        if (!StringUtils.isBlank(gridConfig.getWithdrawProcessMethod())) { // 流程撤回功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_WITHDRAWPROCESS,
                    Pair.of(gridConfig.getWithdrawProcessMethod(),
                            LcdpSourceCodeManager.TreeGridComponent.getTreeGridWithdrawProcessJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getWithdrawProcessMethod())));
        }

        if (!StringUtils.isBlank(gridConfig.getCompleteTaskMethod())) { // 审核通过功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_COMPLETETASK,
                    Pair.of(gridConfig.getCompleteTaskMethod(),
                            LcdpSourceCodeManager.TreeGridComponent.getTreeGridCompleteTaskJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getCompleteTaskMethod())));
        }

        if (!StringUtils.isBlank(gridConfig.getRejectTaskMethod())) { // 审核拒绝功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_REJECTTASK,
                    Pair.of(gridConfig.getRejectTaskMethod(),
                            LcdpSourceCodeManager.TreeGridComponent.getTreeGridRejectTaskJavaCode(gridConfig.getFunctionSuffixId(),
                                    gridConfig.getRejectTaskMethod())));
        }

        if (!StringUtils.isBlank(gridConfig.getStartProcessMethod())
                || !StringUtils.isBlank(gridConfig.getCompleteTaskMethod())) { // 流程任务节点状态功能
            methodSourceCodeMap.put(gridConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.GRID_PROCESSTASKSTATUS,
                    Pair.of(LcdpSourceCodeManager.getProcessTaskStatusMethod(gridConfig.getFunctionSuffixId()),
                            LcdpSourceCodeManager.TreeGridComponent.getTreeGridProcessTaskStatus(gridConfig.getFunctionSuffixId())));
        }

        if (updateSourceCodeMap.containsKey(gridConfig.getService())) {
            updateSourceCodeMap.get(gridConfig.getService()).putAll(methodSourceCodeMap);
        } else {
            updateSourceCodeMap.put(gridConfig.getService(), methodSourceCodeMap);
        }

        // 是否需要更新mybatis的mapper内容
        if (gridConfig.isUpdateMapper()
                && !StringUtils.isBlank(gridConfig.getSelectMethod())) {
            if (!StringUtils.isBlank(gridConfig.getSql())) {
                if (updateMapperMap.containsKey(defaultNamespace)) {
                    updateMapperMap.get(defaultNamespace).add(Pair.of(defaultMapperId, gridConfig.getSql()));
                } else {
                    updateMapperMap.put(defaultNamespace, ArrayUtils.asList(Pair.of(defaultMapperId, gridConfig.getSql())));
                }
            } else if (!StringUtils.isBlank(gridConfig.getTable())) {
                if (updateMapperMap.containsKey(defaultNamespace)) {
                    updateMapperMap.get(defaultNamespace).add(Pair.of(defaultMapperId, gridConfig.getTable()));
                } else {
                    updateMapperMap.put(defaultNamespace, ArrayUtils.asList(Pair.of(defaultMapperId, gridConfig.getTable())));
                }
            }
        }
    }

    private void createDataForInsertServiceByForm(LcdpPageFormConfigDTO formConfig,
                                                  Map<String, Map<String, String>> insertSourceCodeMap,
                                                  Map<String, List<String>> insertMapperMap) {
        Map<String, String> methodSourceCodeMap = new HashMap<>();

        // 默认的mapperId
        String defaultNamespace = getDefaultMapperNamespace(formConfig.getService());
        String defaultMapperId = "selectDetailById_" + formConfig.getFunctionSuffixId();

        if (formConfig.isUpdateTable()
                && !StringUtils.isBlank(formConfig.getTable())) { // 组件对应的表
            methodSourceCodeMap.put(formConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.COMPONNET_TABLE,
                    LcdpSourceCodeManager.getComponentTableJavaCode(formConfig.getFunctionSuffixId(),
                            formConfig.getTable()));
        }

        if (!StringUtils.isBlank(formConfig.getSelectMethod())) { // 查询功能
            methodSourceCodeMap.put(formConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.FORM_SELECT,
                    LcdpSourceCodeManager.FormComponent.getFormSelectJavaCode(formConfig.getFunctionSuffixId(),
                            formConfig.getSelectMethod(),
                            defaultNamespace + "." + defaultMapperId));
        }

        if (!StringUtils.isBlank(formConfig.getInsertMethod())) { // 新增功能
            methodSourceCodeMap.put(formConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.FORM_SETID,
                    LcdpSourceCodeManager.FormComponent.getFormSetIdJavaCode(formConfig.getFunctionSuffixId(),
                            formConfig.getIdAutoGen(), formConfig.getIdGenSequence()));

            methodSourceCodeMap.put(formConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.FORM_INSERT,
                    LcdpSourceCodeManager.FormComponent.getFormInsertJavaCode(formConfig.getFunctionSuffixId(),
                            formConfig.getInsertMethod(), formConfig.getIdAutoGen(), formConfig.getIdGenSequence()));
        }

        if (!StringUtils.isBlank(formConfig.getUpdateMethod())
                || Boolean.TRUE.equals(formConfig.getAutoSave())) { // 更新功能
            methodSourceCodeMap.put(formConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.FORM_UPDATE,
                    LcdpSourceCodeManager.FormComponent.getFormUpdateJavaCode(formConfig.getFunctionSuffixId(),
                            formConfig.getUpdateMethod()));
        }

        if (!StringUtils.isBlank(formConfig.getStartProcessMethod())) { // 流程提交功能
            methodSourceCodeMap.put(formConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.FORM_STARTPROCESS,
                    LcdpSourceCodeManager.FormComponent.getFormStartProcessJavaCode(formConfig.getFunctionSuffixId(),
                            formConfig.getStartProcessMethod()));
        }

        if (!StringUtils.isBlank(formConfig.getCompleteTaskMethod())) { // 审核通过功能
            methodSourceCodeMap.put(formConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.FORM_COMPLETETASK,
                    LcdpSourceCodeManager.FormComponent.getFormCompleteTaskJavaCode(formConfig.getFunctionSuffixId(),
                            formConfig.getCompleteTaskMethod()));
        }

        if (!StringUtils.isBlank(formConfig.getRejectTaskMethod())) { // 审核拒绝功能
            methodSourceCodeMap.put(formConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.FORM_REJECTTASK,
                    LcdpSourceCodeManager.FormComponent.getFormRejectTaskJavaCode(formConfig.getFunctionSuffixId(),
                            formConfig.getRejectTaskMethod()));
        }

        if (!StringUtils.isBlank(formConfig.getStartProcessMethod())
                || !StringUtils.isBlank(formConfig.getCompleteTaskMethod())) { // 流程任务节点状态功能
            methodSourceCodeMap.put(formConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.FORM_PROCESSTASKSTATUS,
                    LcdpSourceCodeManager.FormComponent.getFormProcessTaskStatus(formConfig.getFunctionSuffixId()));
        }

        if (insertSourceCodeMap.containsKey(formConfig.getService())) {
            insertSourceCodeMap.get(formConfig.getService()).putAll(methodSourceCodeMap);
        } else {
            insertSourceCodeMap.put(formConfig.getService(), methodSourceCodeMap);
        }

        if (!StringUtils.isBlank(formConfig.getSelectMethod())) { // 查询功能
            if (!StringUtils.isBlank(formConfig.getSql())) {
                if (insertMapperMap.containsKey(defaultNamespace)) {
                    insertMapperMap.get(defaultNamespace).add(LcdpSourceCodeManager.getFormSelectSqlStatement(defaultMapperId, formConfig.getSql()));
                } else {
                    insertMapperMap.put(defaultNamespace, ArrayUtils.asList(LcdpSourceCodeManager.getFormSelectSqlStatement(defaultMapperId, formConfig.getSql())));
                }
            } else if (!StringUtils.isBlank(formConfig.getTable())) {
                if (insertMapperMap.containsKey(defaultNamespace)) {
                    insertMapperMap.get(defaultNamespace).add(LcdpSourceCodeManager.getFormSelectTableStatement(defaultMapperId, formConfig.getTable()));
                } else {
                    insertMapperMap.put(defaultNamespace, ArrayUtils.asList(LcdpSourceCodeManager.getFormSelectTableStatement(defaultMapperId, formConfig.getTable())));
                }
            }
        }
    }

    private void createDataForUpdateServiceByForm(LcdpResourceBean serviceResource, LcdpPageFormConfigDTO formConfig,
                                                  Map<String, Map<String, Pair<String, String>>> updateSourceCodeMap,
                                                  Map<String, List<Pair<String, String>>> updateMapperMap) {
        Map<String, Pair<String, String>> methodSourceCodeMap = new HashMap<>();

        // 默认的mapperId
        String defaultNamespace = getDefaultMapperNamespace(formConfig.getService());
        String defaultMapperId = "selectDetailById_" + formConfig.getFunctionSuffixId();

        if (formConfig.isUpdateTable()
                && !StringUtils.isBlank(formConfig.getTable())) { // 组件对应的表
            methodSourceCodeMap.put(formConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.COMPONNET_TABLE,
                    Pair.of("get" + formConfig.getFunctionSuffixId() + "Table",
                            LcdpSourceCodeManager.getComponentTableJavaCode(formConfig.getFunctionSuffixId(),
                                    formConfig.getTable())));
        }

        if (!StringUtils.isBlank(formConfig.getSelectMethod())) { // 查询功能
            methodSourceCodeMap.put(formConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.FORM_SELECT,
                    Pair.of(formConfig.getSelectMethod(),
                            LcdpSourceCodeManager.FormComponent.getFormSelectJavaCode(formConfig.getFunctionSuffixId(),
                                    formConfig.getSelectMethod(),
                                    defaultNamespace + "." + defaultMapperId)));
        }

        if (!StringUtils.isBlank(formConfig.getInsertMethod())) { // 新增功能
            if (formConfig.isUpdateSetIdMethod()) {
                methodSourceCodeMap.put(formConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.FORM_SETID,
                        Pair.of("setId_" + formConfig.getFunctionSuffixId(),
                                LcdpSourceCodeManager.FormComponent.getFormSetIdJavaCode(formConfig.getFunctionSuffixId(),
                                        formConfig.getIdAutoGen(), formConfig.getIdGenSequence())));
            }

            methodSourceCodeMap.put(formConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.FORM_INSERT,
                    Pair.of(formConfig.getInsertMethod(),
                            LcdpSourceCodeManager.FormComponent.getFormInsertJavaCode(formConfig.getFunctionSuffixId(),
                                    formConfig.getInsertMethod(), formConfig.getIdAutoGen(), formConfig.getIdGenSequence())));
        }

        if (!StringUtils.isBlank(formConfig.getUpdateMethod())
                || Boolean.TRUE.equals(formConfig.getAutoSave())) { // 更新功能
            methodSourceCodeMap.put(formConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.FORM_UPDATE,
                    Pair.of(formConfig.getUpdateMethod(),
                            LcdpSourceCodeManager.FormComponent.getFormUpdateJavaCode(formConfig.getFunctionSuffixId(),
                                    formConfig.getUpdateMethod())));
        }

        if (!StringUtils.isBlank(formConfig.getStartProcessMethod())) { // 流程提交功能
            methodSourceCodeMap.put(formConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.FORM_STARTPROCESS,
                    Pair.of(formConfig.getStartProcessMethod(),
                            LcdpSourceCodeManager.FormComponent.getFormStartProcessJavaCode(formConfig.getFunctionSuffixId(),
                                    formConfig.getStartProcessMethod())));
        }

        if (!StringUtils.isBlank(formConfig.getCompleteTaskMethod())) { // 审核通过功能
            methodSourceCodeMap.put(formConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.FORM_COMPLETETASK,
                    Pair.of(formConfig.getCompleteTaskMethod(),
                            LcdpSourceCodeManager.FormComponent.getFormCompleteTaskJavaCode(formConfig.getFunctionSuffixId(),
                                    formConfig.getCompleteTaskMethod())));
        }

        if (!StringUtils.isBlank(formConfig.getRejectTaskMethod())) { // 审核拒绝功能
            methodSourceCodeMap.put(formConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.FORM_REJECTTASK,
                    Pair.of(formConfig.getRejectTaskMethod(),
                            LcdpSourceCodeManager.FormComponent.getFormRejectTaskJavaCode(formConfig.getFunctionSuffixId(),
                                    formConfig.getRejectTaskMethod())));
        }

        if (!StringUtils.isBlank(formConfig.getStartProcessMethod())
                || !StringUtils.isBlank(formConfig.getCompleteTaskMethod())) { // 流程任务节点状态功能
            methodSourceCodeMap.put(formConfig.getFunctionSuffixId() + "$" + LcdpSourceCodeMethodType.FORM_PROCESSTASKSTATUS,
                    Pair.of(LcdpSourceCodeManager.getProcessTaskStatusMethod(formConfig.getFunctionSuffixId()),
                            LcdpSourceCodeManager.FormComponent.getFormProcessTaskStatus(formConfig.getFunctionSuffixId())));
        }

        if (updateSourceCodeMap.containsKey(formConfig.getService())) {
            updateSourceCodeMap.get(formConfig.getService()).putAll(methodSourceCodeMap);
        } else {
            updateSourceCodeMap.put(formConfig.getService(), methodSourceCodeMap);
        }

        // 是否需要更新mybatis的mapper内容
        if (formConfig.isUpdateMapper()
                && !StringUtils.isBlank(formConfig.getSelectMethod())) {
            if (!StringUtils.isBlank(formConfig.getSql())) {
                if (updateMapperMap.containsKey(defaultNamespace)) {
                    updateMapperMap.get(defaultNamespace).add(Pair.of(defaultMapperId, formConfig.getSql()));
                } else {
                    updateMapperMap.put(defaultNamespace, ArrayUtils.asList(Pair.of(defaultMapperId, formConfig.getSql())));
                }
            } else if (!StringUtils.isBlank(formConfig.getTable())) {
                if (updateMapperMap.containsKey(defaultNamespace)) {
                    updateMapperMap.get(defaultNamespace).add(Pair.of(defaultMapperId, formConfig.getTable()));
                } else {
                    updateMapperMap.put(defaultNamespace, ArrayUtils.asList(Pair.of(defaultMapperId, formConfig.getTable())));
                }
            }
        }
    }


    private String getDefaultMapperNamespace(String classPath) {
        return StringUtils.removeEnd(classPath.replace(".server.", ".mapper."), "Service") + "Mapper";
    }

    private String getResourceName(String path) {
        return path.contains(".") ? path.substring(path.lastIndexOf(".") + 1) : path;
    }

    private boolean isMappingMethodExists(String sourceCode, String methodName) {
        sourceCode = LcdpJavaCodeResolverUtils.getNocommentJavaCode(sourceCode);

        Pattern pattern = Pattern.compile("@Mapping\\(.+\\)\\s*(\\s*@.+\\s*)*public\\s+[^\\r\\n\\(]+\\s+" + methodName + "\\(");

        return pattern.matcher(sourceCode).find();
    }

    private boolean isMybatisXmlMapperIdExists(String mybatisXml, String mapperId) {
        Pattern pattern = Pattern.compile("\\<select\\s+id=\"" + mapperId + "\"\\s+.+\\>\\s+SELECT\\s+T\\.\\*\\s+FROM\\s*[\\s\\S]+?\\sT\\s+\\<where\\>");

        return pattern.matcher(mybatisXml).find();
    }

    private LcdpTableFieldDTO parse(Entry<String, Class<?>> entry) {
        LcdpTableFieldDTO field = new LcdpTableFieldDTO();
        field.setFieldName(entry.getKey());
        switch (entry.getValue().getSimpleName()) {
            case "Long":
            case "Integer":
                field.setFieldType("number");
                break;
            case "Double":
            case "Float":
            case "Number":
                field.setFieldType("double");
                break;
            case "LocalDate":
                field.setFieldType("date");
                break;
            case "LocalDateTime":
            case "Date":
                field.setFieldType("dateTime");
                break;
            default:
                field.setFieldType("string");
        }
        return field;
    }

    private List<LcdpTableFunction> getTableFunctionList(LcdpPageGridConfigDTO gridConfig) {
        List<LcdpTableFunction> tableFunctionList = new ArrayList<>();
        if (StringUtils.isBlank(gridConfig.getService())) {
            return tableFunctionList;
        }

        if (!StringUtils.isBlank(gridConfig.getActivateMethod())
                || !StringUtils.isBlank(gridConfig.getDeactivateMethod())) {
            tableFunctionList.add(LcdpTableFunction.ACTIVATE);
        }

        if (!StringUtils.isBlank(gridConfig.getStartProcessMethod())
                || !StringUtils.isBlank(gridConfig.getCompleteTaskMethod())
                || !StringUtils.isBlank(gridConfig.getRejectTaskMethod())
                || !StringUtils.isBlank(gridConfig.getWithdrawProcessMethod())) {
            tableFunctionList.add(LcdpTableFunction.PROCESS);
        }

        return tableFunctionList;
    }

    private List<LcdpTableFunction> getTableFunctionList(LcdpPageFormConfigDTO formConfig) {
        List<LcdpTableFunction> tableFunctionList = new ArrayList<>();
        if (StringUtils.isBlank(formConfig.getService())) {
            return tableFunctionList;
        }

        if (!StringUtils.isBlank(formConfig.getStartProcessMethod())
                || !StringUtils.isBlank(formConfig.getCompleteTaskMethod())
                || !StringUtils.isBlank(formConfig.getRejectTaskMethod())) {
            tableFunctionList.add(LcdpTableFunction.PROCESS);
        }

        return tableFunctionList;
    }

    private List<LcdpTableFunction> getTableFunctionList(LcdpPageTreeGridConfigDTO treeGridConfig) {
        List<LcdpTableFunction> tableFunctionList = new ArrayList<>();
        if (StringUtils.isBlank(treeGridConfig.getService())) {
            return tableFunctionList;
        }

        tableFunctionList.add(LcdpTableFunction.TREEGRID);

        return tableFunctionList;
    }
}
