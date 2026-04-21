package com.sunwayworld.cloud.module.lcdp.resource.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModulePageCompBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceConvertRecordBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModuleSourceConvertResultDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.base.annotation.Mapping;
import com.sunwayworld.cloud.module.lcdp.base.service.LcdpAuditService;
import com.sunwayworld.cloud.module.lcdp.base.service.LcdpBaseService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpModulePageCompService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpModulePageI18nService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpModuleSourceConvertService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceConvertRecordService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceHistoryService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldBean;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableFieldService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableService;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.database.context.ColumnContext;
import com.sunwayworld.framework.database.core.DatabaseManager;
import com.sunwayworld.framework.database.sql.Order;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.sys.i18n.bean.CoreI18nBean;
import com.sunwayworld.module.sys.i18n.bean.CoreI18nConfigBean;
import com.sunwayworld.module.sys.i18n.bean.CoreI18nMessageBean;
import com.sunwayworld.module.sys.i18n.service.CoreI18nConfigService;
import com.sunwayworld.module.sys.i18n.service.CoreI18nMessageService;
import com.sunwayworld.module.sys.i18n.service.CoreI18nService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Repository
@GikamBean
public class LcdpModuleSourceConvertServiceImpl implements LcdpModuleSourceConvertService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LcdpModuleSourceConvertServiceImpl.class);
    private static final String CONVERTED_BASE_PACKAGE = "com.sunwayworld.lcdp.converted";
    private static final Pattern INSERT_FIELDS_PATTERN = Pattern.compile("fields\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
    private static final Pattern QUOTED_VALUE_PATTERN = Pattern.compile("'([^']+)'|\"([^\"]+)\"");
    private static final Pattern MAPPER_NAMESPACE_PATTERN = Pattern.compile("<mapper\\s+namespace=\"([^\"]+)\"");
    private static final Pattern BASE_URL_PATTERN = Pattern.compile("(?m)\\b(?:var|let|const)\\s+baseUrl\\s*=\\s*['\"]([^'\"]+)['\"]");
    private static final Pattern SERVICE_PATH_PATTERN = Pattern.compile("([A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)*\\.server\\.[A-Za-z0-9_]+)");
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("getTable\\s*\\(\\s*\\)\\s*\\{[\\s\\S]*?return\\s+[\"']([^\"']+)[\"']");
    private static final Pattern SQL_ALIAS_PATTERN = Pattern.compile("(?i)\\bAS\\s+([A-Za-z_][A-Za-z0-9_]*)");
    private static final Pattern JAVA_PACKAGE_PATTERN = Pattern.compile("(?m)^\\s*package\\s+([A-Za-z0-9_.]+)\\s*;");
    private static final Pattern JAVA_IMPORT_PATTERN = Pattern.compile("(?m)^\\s*import\\s+([A-Za-z0-9_.*]+)\\s*;");
    private static final Pattern LCDP_PATH_IMPORT_PATTERN = Pattern.compile("@LcdpPathImport\\s*\\((.*?)\\)", Pattern.DOTALL);
    private static final Pattern JAVA_METHOD_PATTERN = Pattern.compile("(?s)\\bpublic\\s+((?:<[^>]+>\\s*)?[A-Za-z0-9_$.<>\\[\\], ?]+?)\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*(?:throws\\s+[^{]+)?\\{");
    private static final Pattern MAPPING_ANNOTATION_PATTERN = Pattern.compile("@Mapping\\s*\\((.*?)\\)", Pattern.DOTALL);
    private static final Pattern ANNOTATION_VALUE_PATTERN = Pattern.compile("\\bvalue\\s*=\\s*\"([^\"]*)\"");
    private static final Pattern ANNOTATION_TYPE_PATTERN = Pattern.compile("\\btype\\s*=\\s*(?:MappingType\\.)?([A-Z_]+)");
    private static final Pattern SIMPLE_JS_IDENTIFIER = Pattern.compile("^[A-Za-z_$][A-Za-z0-9_$]*$");
    private static final Pattern JS_URL_DOT_METHOD_PATTERN = Pattern.compile("(?s)(\\burl\\s*:\\s*[^,}]*?)(['\"])\\.([A-Za-z0-9_]+)\\2");
    private static final Set<String> LCDP_BASE_MAPPING_METHOD_NAME_SET = Collections.unmodifiableSet(resolveMappingMethodNameSet(LcdpBaseService.class));
    private static final Set<String> LCDP_AUDIT_MAPPING_METHOD_NAME_SET = Collections.unmodifiableSet(resolveMappingMethodNameSet(LcdpAuditService.class));
    private static final List<String> COMPACT_IDENTIFIER_TOKENS = Arrays.asList(
            "industrialization", "implementation", "significance", "orientation", "background", "achievements",
            "achievement", "estimated", "expected", "benefits", "benefit", "approval", "progress", "process",
            "project", "chinese", "english", "manager", "research", "current", "company", "content", "created",
            "updated", "modify", "method", "remark", "status", "budget", "parent", "objectives", "objective",
            "category", "version", "problem", "project", "owned", "stage", "level", "basic", "start",
            "total", "cycle", "cost", "time", "date", "name", "year", "month", "title", "order", "role", "user",
            "type", "mode", "plan", "proj", "org", "unit", "risk", "flag", "code", "info", "last", "first", "end",
            "tag", "day", "rd", "by", "id", "no");

    @Autowired
    private LcdpResourceService resourceService;
    @Autowired
    private LcdpResourceHistoryService resourceHistoryService;
    @Autowired
    private LcdpModulePageCompService modulePageCompService;
    @Autowired
    private LcdpTableService lcdpTableService;
    @Autowired
    private LcdpTableFieldService lcdpTableFieldService;
    @Autowired
    private LcdpModulePageI18nService lcdpModulePageI18nService;
    @Autowired
    private CoreI18nService coreI18nService;
    @Autowired
    private CoreI18nMessageService coreI18nMessageService;
    @Autowired
    private CoreI18nConfigService coreI18nConfigService;
    @Autowired
    private LcdpResourceConvertRecordService resourceConvertRecordService;

    @Override
    public LcdpModuleSourceConvertResultDTO convert(Long moduleId, RestJsonWrapperBean wrapper) {
        RestJsonWrapperBean actualWrapper = wrapper == null ? RestJsonWrapperBean.newEmpty() : wrapper;
        ConversionOptions options = ConversionOptions.of(actualWrapper);
        List<String> warnings = new ArrayList<>();
        LOGGER.info("LCDP 转源码开始，moduleId={}, outputRoot={}, targetModule={}, overwrite={}",
                moduleId, options.outputRoot, options.targetModule, options.overwrite);

        LcdpResourceBean module = resourceService.selectById(moduleId);
        if (module == null || module.getId() == null) {
            throw new ApplicationRuntimeException("未找到模块资源：" + moduleId);
        }
        if (!StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_MODULE, module.getResourceCategory())) {
            throw new ApplicationRuntimeException("资源不是模块：" + moduleId);
        }
        if (StringUtils.equals(Constant.YES, module.getDeleteFlag())) {
            throw new ApplicationRuntimeException("模块已删除：" + moduleId);
        }

        ModuleContext context = buildModuleContext(module, warnings);
        Path projectRoot = resolveProjectRoot(options);
        Path outputRoot = resolveTargetRoot(projectRoot, options);
        LOGGER.info("LCDP 转源码模块上下文已解析，moduleId={}, moduleCode={}, projectRoot={}, outputRoot={}",
                moduleId, context.moduleCode, projectRoot, outputRoot);
        validateTargetRoot(outputRoot);

        List<GeneratedFile> generatedFiles = generateFiles(context, outputRoot, warnings);
        cleanupLegacyControllerFiles(context, outputRoot);
        cleanupLegacyBackendFiles(context, outputRoot);
        cleanupLegacyFrontendFiles(context, outputRoot);
        writeFiles(context, generatedFiles, options.overwrite);
        persistConvertedResourceRecords(context, outputRoot);

        LcdpModuleSourceConvertResultDTO result = new LcdpModuleSourceConvertResultDTO();
        result.setModuleId(moduleId);
        result.setModuleName(context.moduleCode);
        result.setTargetModule(options.targetModule);
        result.setOutputRoot(outputRoot.toString());
        result.setOverwrite(options.overwrite);
        result.setReportFile(context.reportFile == null ? null : context.reportFile.toString());
        result.setGeneratedFiles(generatedFiles.stream().map(file -> file.path.toString()).collect(Collectors.toList()));
        result.setConvertedResources(buildConvertedResourceSummaryList(context));
        result.setSkippedResources(buildSkippedResourceSummaryList(context));
        result.setWarnings(warnings);
        LOGGER.info("LCDP 转源码完成，moduleId={}, moduleCode={}, generatedFileCount={}, outputRoot={}",
                moduleId, context.moduleCode, generatedFiles.size(), outputRoot);
        return result;
    }

    private ModuleContext buildModuleContext(LcdpResourceBean module, List<String> warnings) {
        ModuleContext context = new ModuleContext();
        context.moduleId = module.getId();
        context.moduleCode = module.getResourceName();
        context.className = toUpperCamel(context.moduleCode);
        context.lowerCamelName = toLowerCamel(context.className);
        context.modulePath = toRouteSegment(context.moduleCode);
        context.jsNamespace = context.lowerCamelName;
        context.serviceBeanName = context.lowerCamelName + "ServiceImpl";
        context.tableName = firstNotBlank(module.getDependentTable());
        context.childResources = resourceService.selectListByFilter(SearchFilter.instance()
                        .match("PARENTID", module.getId()).filter(MatchPattern.EQ)
                        .match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ),
                Order.asc("ORDERNO"), Order.asc("ID"));
        if (CollectionUtils.isEmpty(context.childResources)) {
            throw new ApplicationRuntimeException("模块下没有可转换的资源：" + module.getId());
        }

        context.effectiveHistoryMap = new LinkedHashMap<>();
        context.resourceConvertRecordMap = new LinkedHashMap<>();
        context.moduleGeneratedFiles = new ArrayList<>();
        context.javaHistoryList = new ArrayList<>();
        context.javaHistoryByName = new LinkedHashMap<>();
        context.javaHistoryByPath = new LinkedHashMap<>();
        context.mapperHistoryList = new ArrayList<>();
        context.viewHistoryList = new ArrayList<>();
        for (LcdpResourceBean child : context.childResources) {
            registerResourceConvertRecord(context, child);
            LcdpResourceHistoryBean history = selectEffectiveHistory(child);
            if (history == null) {
                warnings.add("资源未找到生效版本，已跳过：" + child.getResourceName());
                markResourceSkipped(context, child.getId(), "未找到生效版本");
                continue;
            }
            updateResourceRecordPath(context, child.getId(), firstNotBlank(history.getPath(), child.getPath()));
            context.effectiveHistoryMap.put(child.getId(), history);
            if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_MAPPER, child.getResourceCategory())) {
                context.mapperHistoryList.add(history);
            } else if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_VIEW, child.getResourceCategory())) {
                context.viewHistoryList.add(history);
            } else if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_JAVA, child.getResourceCategory())) {
                context.javaHistoryList.add(history);
                context.javaHistoryByName.put(history.getResourceName(), history);
                context.javaHistoryByPath.put(history.getPath(), history);
            } else {
                markResourceSkipped(context, child.getId(), "当前资源类型暂不支持自动转源码");
            }
        }

        context.primaryViewHistory = selectPrimaryViewHistory(context.viewHistoryList, context.moduleCode);
        context.pageI18nMap = loadPageI18n(context.primaryViewHistory);
        context.primaryServicePath = inferServicePath(context.primaryViewHistory);
        applyModulePathMetadata(context, resolveModulePathMetadata(module, context, warnings));
        context.primaryJavaHistory = findPrimaryJavaHistory(context);
        if (context.primaryJavaHistory != null) {
            context.tableName = firstNotBlank(context.tableName, inferTableNameFromJavaContent(context.primaryJavaHistory.getContent()));
        }
        if (StringUtils.isBlank(context.tableName)) {
            context.tableName = inferTableNameFromJavaList(context.javaHistoryList, warnings);
        }
        if (StringUtils.isBlank(context.tableName)) {
            throw new ApplicationRuntimeException("模块未配置关联表，且无法从主服务反推主表：" + module.getId());
        }

        context.lcdpMethodMetaList = buildLcdpMethodMetaList(context, warnings);
        context.fieldMetaList = resolveFieldMetaList(context, warnings);
        context.fieldMetaByLowerColumn = context.fieldMetaList.stream()
                .collect(Collectors.toMap(meta -> meta.columnName.toLowerCase(Locale.ENGLISH), meta -> meta, (left, right) -> left, LinkedHashMap::new));
        context.idField = context.fieldMetaList.stream().filter(FieldMeta::isId).findFirst().orElse(null);
        if (context.idField == null) {
            throw new ApplicationRuntimeException("表缺少主键字段 ID：" + context.tableName);
        }

        context.beanBaseType = resolveBeanBaseType(context.fieldMetaList);
        return context;
    }

    private List<FieldMeta> resolveFieldMetaList(ModuleContext context, List<String> warnings) {
        try {
            context.columns = DatabaseManager.getTableColumnContextList(context.tableName);
        } catch (Exception e) {
            warnings.add("物理表字段读取失败，已降级使用低代码元数据推断：" + context.tableName + "，" + e.getMessage());
        }
        if (!CollectionUtils.isEmpty(context.columns)) {
            return ensureIdField(buildFieldMetaList(context.columns), warnings);
        }

        List<FieldMeta> lcdpFieldMetaList = buildFieldMetaListFromLcdpTable(context.tableName, warnings);
        if (!CollectionUtils.isEmpty(lcdpFieldMetaList)) {
            return ensureIdField(lcdpFieldMetaList, warnings);
        }

        List<FieldMeta> inferredFieldMetaList = buildFieldMetaListFromPageAndMapper(context, warnings);
        if (!CollectionUtils.isEmpty(inferredFieldMetaList)) {
            return ensureIdField(inferredFieldMetaList, warnings);
        }
        throw new ApplicationRuntimeException("未读取到表字段，且无法从页面 / Mapper 推断字段：" + context.tableName);
    }

    private LcdpResourceHistoryBean selectEffectiveHistory(LcdpResourceBean resource) {
        if (resource.getEffectVersion() != null && resource.getEffectVersion() > 0) {
            return resourceHistoryService.selectFirstByFilter(SearchFilter.instance()
                    .match("RESOURCEID", resource.getId()).filter(MatchPattern.EQ)
                    .match("VERSION", resource.getEffectVersion()).filter(MatchPattern.EQ)
                    .match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ));
        }
        return resourceHistoryService.selectFirstByFilter(SearchFilter.instance()
                .match("RESOURCEID", resource.getId()).filter(MatchPattern.EQ)
                .match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.EQ)
                .match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ), Order.desc("VERSION"), Order.desc("ID"));
    }

    private static Set<String> resolveMappingMethodNameSet(Class<?> type) {
        return Arrays.stream(type.getMethods())
                .filter(method -> method.getAnnotation(Mapping.class) != null)
                .map(Method::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<FieldMeta> buildFieldMetaList(List<ColumnContext> columns) {
        List<FieldMeta> fieldMetaList = new ArrayList<>();
        for (ColumnContext column : columns) {
            FieldMeta meta = new FieldMeta();
            meta.columnName = column.getColumnName();
            meta.propertyName = toLowerCamel(column.getColumnName());
            meta.type = column.getType();
            meta.clob = column.isClob();
            meta.id = StringUtils.equalsIgnoreCase("ID", column.getColumnName());
            fieldMetaList.add(meta);
        }
        return fieldMetaList;
    }

    private List<FieldMeta> buildFieldMetaListFromLcdpTable(String tableName, List<String> warnings) {
        LcdpTableBean table = lcdpTableService.selectFirstByFilter(SearchFilter.instance()
                .match("TABLENAME", tableName).filter(MatchPattern.EQ)
                .match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_YES).filter(MatchPattern.EQ), Order.desc("VERSION"), Order.desc("ID"));
        if (table == null || table.getId() == null) {
            table = lcdpTableService.selectFirstByFilter(SearchFilter.instance()
                    .match("TABLENAME", tableName).filter(MatchPattern.EQ), Order.desc("VERSION"), Order.desc("ID"));
        }

        List<LcdpTableFieldBean> tableFieldList;
        if (table != null && table.getId() != null) {
            tableFieldList = lcdpTableFieldService.selectListByFilter(SearchFilter.instance()
                    .match("TABLEID", table.getId()).filter(MatchPattern.EQ), Order.asc("ID"));
        } else {
            tableFieldList = lcdpTableFieldService.selectListByFilter(SearchFilter.instance()
                    .match("TABLENAME", tableName).filter(MatchPattern.EQ), Order.asc("ID"));
        }

        if (CollectionUtils.isEmpty(tableFieldList)) {
            return new ArrayList<>();
        }
        warnings.add("物理表不存在，已使用 LCDP 表字段元数据生成：" + tableName);

        List<FieldMeta> fieldMetaList = new ArrayList<>();
        for (LcdpTableFieldBean field : tableFieldList) {
            String fieldName = field.getFieldName();
            if (StringUtils.isBlank(fieldName)) {
                continue;
            }
            FieldMeta meta = new FieldMeta();
            meta.columnName = fieldName.toUpperCase(Locale.ENGLISH);
            meta.propertyName = toLowerCamel(fieldName);
            meta.type = resolveJavaType(fieldName, field.getFieldType(), field.getPrecision(), field.getScale());
            meta.clob = isClobField(field);
            meta.id = StringUtils.equalsIgnoreCase("ID", fieldName);
            fieldMetaList.add(meta);
        }
        return deduplicateFieldMeta(fieldMetaList);
    }

    private List<LcdpMethodMeta> buildLcdpMethodMetaList(ModuleContext context, List<String> warnings) {
        List<LcdpMethodMeta> methodMetaList = new ArrayList<>();
        List<LcdpResourceHistoryBean> javaHistoryList = new ArrayList<>(context.javaHistoryList);
        javaHistoryList.sort(Comparator.comparing(LcdpResourceHistoryBean::getResourceName));
        for (LcdpResourceHistoryBean history : javaHistoryList) {
            List<LcdpMethodMeta> sourceMethodMetaList = parseMethodMetaFromSource(history);
            if (!CollectionUtils.isEmpty(sourceMethodMetaList)) {
                methodMetaList.addAll(sourceMethodMetaList);
                continue;
            }
            try {
                List<LcdpMethodMeta> runtimeMethodMetaList = parseMethodMetaFromRuntime(history);
                if (!CollectionUtils.isEmpty(runtimeMethodMetaList)) {
                    methodMetaList.addAll(runtimeMethodMetaList);
                    continue;
                }
            } catch (Exception e) {
                warnings.add("低代码服务方法读取失败，已跳过方法元数据：" + history.getPath() + "，" + e.getMessage());
            }
            warnings.add("低代码服务方法元数据解析为空：" + history.getPath());
        }
        methodMetaList.sort(Comparator
                .comparing((LcdpMethodMeta meta) -> meta.servicePath)
                .thenComparing(meta -> meta.methodName)
                .thenComparing(meta -> String.join(",", meta.parameterTypeNameList)));
        return methodMetaList;
    }

    private List<LcdpMethodMeta> parseMethodMetaFromRuntime(LcdpResourceHistoryBean history) {
        Class<?> clazz = resourceService.getActiveClassByPath(history.getPath(), false);
        if (clazz == null) {
            return Collections.emptyList();
        }
        List<LcdpMethodMeta> methodMetaList = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers()) || method.isSynthetic()) {
                continue;
            }
            LcdpMethodMeta meta = new LcdpMethodMeta();
            meta.servicePath = history.getPath();
            meta.serviceName = history.getResourceName();
            meta.methodName = method.getName();
            meta.returnTypeName = method.getReturnType().getName();
            meta.parameterTypeNameList = Arrays.stream(method.getParameterTypes())
                    .map(Class::getName)
                    .collect(Collectors.toList());
            Mapping mapping = method.getAnnotation(Mapping.class);
            if (mapping != null) {
                meta.mappingType = mapping.type().name();
                meta.mappingDesc = mapping.value();
            }
            methodMetaList.add(meta);
        }
        return methodMetaList;
    }

    private List<LcdpMethodMeta> parseMethodMetaFromSource(LcdpResourceHistoryBean history) {
        String content = ObjectUtils.toString(history.getContent());
        if (StringUtils.isBlank(content)) {
            return Collections.emptyList();
        }
        String packageName = parseJavaPackageName(content);
        Map<String, String> importMap = parseJavaImportMap(content);
        List<LcdpMethodMeta> methodMetaList = new ArrayList<>();
        Matcher matcher = JAVA_METHOD_PATTERN.matcher(content);
        while (matcher.find()) {
            String methodName = matcher.group(2);
            if (StringUtils.equals(methodName, history.getResourceName())) {
                continue;
            }
            LcdpMethodMeta meta = new LcdpMethodMeta();
            meta.servicePath = history.getPath();
            meta.serviceName = history.getResourceName();
            meta.methodName = methodName;
            meta.returnTypeName = resolveSourceTypeName(matcher.group(1), importMap, packageName);
            meta.parameterTypeNameList = parseParameterTypeNameList(matcher.group(3), importMap, packageName);
            fillMappingMeta(meta, content.substring(Math.max(0, matcher.start() - 800), matcher.start()));
            methodMetaList.add(meta);
        }
        return methodMetaList;
    }

    private String parseJavaPackageName(String content) {
        Matcher matcher = JAVA_PACKAGE_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1) : "";
    }

    private Map<String, String> parseJavaImportMap(String content) {
        Map<String, String> importMap = new LinkedHashMap<>();
        Matcher matcher = JAVA_IMPORT_PATTERN.matcher(content);
        while (matcher.find()) {
            String importName = matcher.group(1);
            if (StringUtils.isBlank(importName) || importName.endsWith(".*")) {
                continue;
            }
            importMap.put(importName.substring(importName.lastIndexOf('.') + 1), importName);
        }
        return importMap;
    }

    private void fillMappingMeta(LcdpMethodMeta meta, String methodPrefix) {
        int mappingIndex = methodPrefix.lastIndexOf("@Mapping");
        if (mappingIndex < 0) {
            return;
        }
        Matcher mappingMatcher = MAPPING_ANNOTATION_PATTERN.matcher(methodPrefix.substring(mappingIndex));
        if (!mappingMatcher.find()) {
            return;
        }
        String annotationContent = mappingMatcher.group(1);
        Matcher valueMatcher = ANNOTATION_VALUE_PATTERN.matcher(annotationContent);
        if (valueMatcher.find()) {
            meta.mappingDesc = valueMatcher.group(1);
        }
        Matcher typeMatcher = ANNOTATION_TYPE_PATTERN.matcher(annotationContent);
        if (typeMatcher.find()) {
            meta.mappingType = typeMatcher.group(1);
        }
    }

    private List<String> parseParameterTypeNameList(String parameterSection, Map<String, String> importMap, String packageName) {
        if (StringUtils.isBlank(parameterSection)) {
            return new ArrayList<>();
        }
        List<String> parameterTypeNameList = new ArrayList<>();
        for (String parameter : splitMethodParameters(parameterSection)) {
            String typeName = extractParameterTypeName(parameter);
            if (StringUtils.isBlank(typeName)) {
                continue;
            }
            parameterTypeNameList.add(resolveSourceTypeName(typeName, importMap, packageName));
        }
        return parameterTypeNameList;
    }

    private List<String> splitMethodParameters(String parameterSection) {
        List<String> parameterList = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        int genericDepth = 0;
        int annotationDepth = 0;
        for (int i = 0; i < parameterSection.length(); i++) {
            char current = parameterSection.charAt(i);
            if (current == '<') {
                genericDepth++;
            } else if (current == '>') {
                genericDepth = Math.max(0, genericDepth - 1);
            } else if (current == '(') {
                annotationDepth++;
            } else if (current == ')') {
                annotationDepth = Math.max(0, annotationDepth - 1);
            } else if (current == ',' && genericDepth == 0 && annotationDepth == 0) {
                parameterList.add(builder.toString());
                builder.setLength(0);
                continue;
            }
            builder.append(current);
        }
        if (builder.length() > 0) {
            parameterList.add(builder.toString());
        }
        return parameterList;
    }

    private String extractParameterTypeName(String parameter) {
        String normalized = ObjectUtils.toString(parameter).replace('\n', ' ').replace('\r', ' ').trim();
        while (normalized.startsWith("@")) {
            normalized = normalized.replaceFirst("^@[A-Za-z0-9_$.]+(?:\\([^)]*\\))?\\s*", "").trim();
        }
        normalized = normalized.replaceAll("\\bfinal\\b\\s+", "")
                .replaceAll("\\bvolatile\\b\\s+", "")
                .replaceAll("\\btransient\\b\\s+", "")
                .trim();
        int lastBlankIndex = normalized.lastIndexOf(' ');
        if (lastBlankIndex < 0) {
            return normalized;
        }
        return normalized.substring(0, lastBlankIndex).trim();
    }

    private String resolveSourceTypeName(String sourceTypeName, Map<String, String> importMap, String packageName) {
        String typeName = ObjectUtils.toString(sourceTypeName).trim();
        if (StringUtils.isBlank(typeName)) {
            return Object.class.getName();
        }
        typeName = eraseGenericType(typeName).replace("...", "[]").trim();
        StringBuilder arraySuffix = new StringBuilder();
        while (typeName.endsWith("[]")) {
            arraySuffix.append("[]");
            typeName = typeName.substring(0, typeName.length() - 2).trim();
        }
        if (isPrimitiveTypeName(typeName) || typeName.contains(".")) {
            return typeName + arraySuffix;
        }
        String importedType = importMap.get(typeName);
        if (importedType != null) {
            return importedType + arraySuffix;
        }
        String defaultQualifiedType = resolveDefaultQualifiedTypeName(typeName);
        if (defaultQualifiedType != null) {
            return defaultQualifiedType + arraySuffix;
        }
        if (isJavaLangTypeName(typeName)) {
            return "java.lang." + typeName + arraySuffix;
        }
        return StringUtils.isBlank(packageName) ? typeName + arraySuffix : packageName + "." + typeName + arraySuffix;
    }

    private String resolveDefaultQualifiedTypeName(String typeName) {
        if (equalsAnyIgnoreCase(typeName, "List", "Map", "Set", "Collection", "Collections", "ArrayList", "LinkedList",
                "HashMap", "LinkedHashMap", "TreeMap", "HashSet", "LinkedHashSet", "TreeSet", "Iterator")) {
            return "java.util." + typeName;
        }
        if (equalsAnyIgnoreCase(typeName, "BigDecimal", "BigInteger")) {
            return "java.math." + typeName;
        }
        if (equalsAnyIgnoreCase(typeName, "LocalDate", "LocalDateTime", "LocalTime")) {
            return "java.time." + typeName;
        }
        if (equalsAnyIgnoreCase(typeName, "Date")) {
            return "java.util.Date";
        }
        if (equalsAnyIgnoreCase(typeName, "JSONObject", "JSONArray")) {
            return "com.alibaba.fastjson." + typeName;
        }
        return null;
    }

    private String eraseGenericType(String typeName) {
        StringBuilder builder = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < typeName.length(); i++) {
            char current = typeName.charAt(i);
            if (current == '<') {
                depth++;
                continue;
            }
            if (current == '>') {
                depth = Math.max(0, depth - 1);
                continue;
            }
            if (depth == 0) {
                builder.append(current);
            }
        }
        return builder.toString().trim();
    }

    private boolean isPrimitiveTypeName(String typeName) {
        return equalsAnyIgnoreCase(typeName, "byte", "short", "int", "long", "float", "double", "boolean", "char", "void");
    }

    private boolean isJavaLangTypeName(String typeName) {
        return equalsAnyIgnoreCase(typeName, "String", "Object", "Long", "Integer", "Double", "Float", "Boolean",
                "Short", "Byte", "Character", "Void", "Math", "Override", "Exception", "RuntimeException",
                "StringBuilder", "StringBuffer");
    }

    private List<FieldMeta> buildFieldMetaListFromPageAndMapper(ModuleContext context, List<String> warnings) {
        LinkedHashMap<String, FieldMeta> fieldMetaMap = new LinkedHashMap<>();
        List<LcdpModulePageCompBean> pageCompList = loadPageCompList(context.primaryViewHistory, warnings);
        if (!CollectionUtils.isEmpty(pageCompList)) {
            for (LcdpModulePageCompBean comp : pageCompList) {
                JSONObject config = parseConfig(comp);
                String rawField = config.getString("field");
                if (StringUtils.isBlank(rawField)) {
                    continue;
                }
                mergeFieldMeta(fieldMetaMap, buildFieldMeta(rawField, comp.getType(), config.getString("gridColumnType")));
            }
        }

        for (LcdpResourceHistoryBean mapperHistory : context.mapperHistoryList) {
            Matcher aliasMatcher = SQL_ALIAS_PATTERN.matcher(ObjectUtils.toString(mapperHistory.getContent()));
            while (aliasMatcher.find()) {
                String alias = aliasMatcher.group(1);
                if (StringUtils.isBlank(alias)) {
                    continue;
                }
                mergeFieldMeta(fieldMetaMap, buildFieldMeta(alias, null, null));
            }
        }

        if (fieldMetaMap.isEmpty()) {
            return new ArrayList<>();
        }
        warnings.add("物理表和 LCDP 表元数据均不存在，已按页面组件和 Mapper SQL 推断字段：" + context.tableName);
        return ensureIdField(new ArrayList<>(fieldMetaMap.values()), warnings);
    }

    private List<FieldMeta> ensureIdField(List<FieldMeta> fieldMetaList, List<String> warnings) {
        if (CollectionUtils.isEmpty(fieldMetaList)) {
            return fieldMetaList;
        }
        boolean hasId = fieldMetaList.stream().anyMatch(FieldMeta::isId);
        if (!hasId) {
            FieldMeta idField = new FieldMeta();
            idField.columnName = "ID";
            idField.propertyName = "id";
            idField.type = Long.class;
            idField.id = true;
            fieldMetaList.add(0, idField);
            warnings.add("未识别到主键字段，已按默认 ID(Long) 补齐");
        }
        return deduplicateFieldMeta(fieldMetaList);
    }

    private List<FieldMeta> deduplicateFieldMeta(List<FieldMeta> fieldMetaList) {
        LinkedHashMap<String, FieldMeta> fieldMetaMap = new LinkedHashMap<>();
        for (FieldMeta meta : fieldMetaList) {
            if (meta == null || StringUtils.isBlank(meta.columnName)) {
                continue;
            }
            mergeFieldMeta(fieldMetaMap, meta);
        }
        return new ArrayList<>(fieldMetaMap.values());
    }

    private String buildMapperFileName(String resourceName) {
        String dialect = detectMapperDialect(resourceName);
        String baseName = resourceName;
        if (StringUtils.endsWithIgnoreCase(baseName, "Mapper")) {
            baseName = baseName.substring(0, baseName.length() - "Mapper".length());
        }
        if (StringUtils.endsWithIgnoreCase(baseName, "Mysql")
                || StringUtils.endsWithIgnoreCase(baseName, "Oracle")
                || StringUtils.endsWithIgnoreCase(baseName, "Postgresql")
                || StringUtils.endsWithIgnoreCase(baseName, "Sqlserver")) {
            baseName = baseName.substring(0, baseName.length() - dialect.length());
        }
        return toKebabCase(baseName) + "-" + dialect.toLowerCase(Locale.ENGLISH) + "-mapper.xml";
    }

    private GeneratedFile buildLcdpJavaServiceFile(ModuleContext context, Path javaRoot, LcdpResourceHistoryBean history) {
        String packageName = context.basePackage + ".server";
        String content = renderLcdpJavaService(context, history, packageName);
        if (StringUtils.isBlank(content)) {
            return null;
        }
        String javaPackagePath = packageName.replace('.', '/');
        Path path = javaRoot.resolve(javaPackagePath + "/" + history.getResourceName() + ".java");
        return new GeneratedFile(path, content);
    }

    private GeneratedFile buildLcdpJavaResourceInterfaceFile(ModuleContext context, Path javaRoot, LcdpResourceHistoryBean history) {
        GeneratedLcdpResourceSpec spec = resolveGeneratedLcdpResourceSpec(context, history);
        String content = renderLcdpJavaResourceInterface(context, spec);
        if (StringUtils.isBlank(content)) {
            return null;
        }
        String javaPackagePath = spec.resourcePackage.replace('.', '/');
        Path path = javaRoot.resolve(javaPackagePath + "/" + spec.resourceInterfaceName + ".java");
        return new GeneratedFile(path, content);
    }

    private GeneratedFile buildLcdpJavaResourceImplFile(ModuleContext context, Path javaRoot, LcdpResourceHistoryBean history) {
        GeneratedLcdpResourceSpec spec = resolveGeneratedLcdpResourceSpec(context, history);
        String content = renderLcdpJavaResourceImpl(context, spec);
        if (StringUtils.isBlank(content)) {
            return null;
        }
        String javaPackagePath = spec.resourceImplPackage.replace('.', '/');
        Path path = javaRoot.resolve(javaPackagePath + "/" + spec.resourceImplName + ".java");
        return new GeneratedFile(path, content);
    }

    private GeneratedLcdpResourceSpec resolveGeneratedLcdpResourceSpec(ModuleContext context, LcdpResourceHistoryBean history) {
        GeneratedLcdpResourceSpec spec = new GeneratedLcdpResourceSpec();
        spec.serviceName = history.getResourceName();
        spec.servicePath = history.getPath();
        spec.serviceFieldName = buildLcdpServiceFieldName(history.getResourceName());
        spec.resourceInterfaceName = buildLcdpResourceName(history.getResourceName());
        spec.resourceImplName = spec.resourceInterfaceName + "Impl";
        spec.servicePackage = context.basePackage + ".server";
        spec.resourcePackage = context.basePackage + ".resource";
        spec.resourceImplPackage = spec.resourcePackage + ".impl";
        spec.resourceRouteSegment = buildLcdpServiceRouteSegment(history.getResourceName());
        spec.requestPath = buildSecureRequestPath(history.getPath(), context.requestPath + "/services/" + spec.resourceRouteSegment);
        spec.baseType = resolveGeneratedLcdpResourceBaseType(history);
        spec.customMethodMetaList = resolveGeneratedCustomMethodMetaList(context, history, spec.baseType);
        return spec;
    }

    private GeneratedMapperSpec resolveGeneratedMapperSpec(ModuleContext context, LcdpResourceHistoryBean history, List<String> warnings) {
        GeneratedMapperSpec spec = new GeneratedMapperSpec();
        String namespace = extractMapperNamespace(history.getContent());
        String mapperName = stripMapperDialectSuffix(history.getResourceName());
        spec.packageName = context.basePackage + ".mapper";
        if (StringUtils.isBlank(namespace)) {
            spec.mapperName = defaultIfBlank(mapperName, context.className + "Mapper");
            warnings.add("Mapper 未解析到 namespace，已使用默认包名生成：" + history.getResourceName());
        } else {
            spec.mapperName = parseSimpleName(namespace, defaultIfBlank(mapperName, context.className + "Mapper"));
        }
        spec.namespace = spec.packageName + "." + spec.mapperName;
        spec.packagePath = spec.packageName.replace('.', '/');
        return spec;
    }

    private String extractMapperNamespace(String content) {
        Matcher matcher = MAPPER_NAMESPACE_PATTERN.matcher(defaultIfBlank(content, ""));
        return matcher.find() ? matcher.group(1) : null;
    }

    private String stripMapperDialectSuffix(String resourceName) {
        String baseName = defaultIfBlank(resourceName, "");
        if (StringUtils.endsWithIgnoreCase(baseName, "MysqlMapper")) {
            return baseName.substring(0, baseName.length() - "MysqlMapper".length()) + "Mapper";
        }
        if (StringUtils.endsWithIgnoreCase(baseName, "OracleMapper")) {
            return baseName.substring(0, baseName.length() - "OracleMapper".length()) + "Mapper";
        }
        if (StringUtils.endsWithIgnoreCase(baseName, "PostgresqlMapper")) {
            return baseName.substring(0, baseName.length() - "PostgresqlMapper".length()) + "Mapper";
        }
        if (StringUtils.endsWithIgnoreCase(baseName, "SqlserverMapper")) {
            return baseName.substring(0, baseName.length() - "SqlserverMapper".length()) + "Mapper";
        }
        return baseName;
    }

    private GeneratedLcdpResourceBaseType resolveGeneratedLcdpResourceBaseType(LcdpResourceHistoryBean history) {
        try {
            Class<?> runtimeClass = resourceService.getActiveClassByPath(history.getPath(), false);
            if (runtimeClass != null) {
                if (LcdpAuditService.class.isAssignableFrom(runtimeClass)) {
                    return GeneratedLcdpResourceBaseType.AUDIT;
                }
                if (LcdpBaseService.class.isAssignableFrom(runtimeClass)) {
                    return GeneratedLcdpResourceBaseType.BASE;
                }
            }
        } catch (Exception e) {
            return GeneratedLcdpResourceBaseType.METHOD;
        }
        return GeneratedLcdpResourceBaseType.METHOD;
    }

    private List<LcdpMethodMeta> resolveGeneratedCustomMethodMetaList(ModuleContext context, LcdpResourceHistoryBean history,
                                                                      GeneratedLcdpResourceBaseType baseType) {
        Map<String, List<LcdpMethodMeta>> methodMetaGroup = context.lcdpMethodMetaList.stream()
                .filter(meta -> StringUtils.equals(meta.servicePath, history.getPath()) || StringUtils.equals(meta.serviceName, history.getResourceName()))
                .filter(meta -> StringUtils.isNotBlank(meta.mappingType))
                .collect(Collectors.groupingBy(meta -> meta.methodName, LinkedHashMap::new, Collectors.toList()));

        List<LcdpMethodMeta> methodMetaList = new ArrayList<>();
        for (Map.Entry<String, List<LcdpMethodMeta>> entry : methodMetaGroup.entrySet()) {
            if (isAbstractedLcdpMethod(entry.getKey(), baseType)) {
                continue;
            }
            LcdpMethodMeta preferred = selectPreferredGeneratedMethodMeta(entry.getValue());
            if (preferred != null) {
                methodMetaList.add(preferred);
            }
        }
        methodMetaList.sort(Comparator.comparing(meta -> meta.methodName));
        return methodMetaList;
    }

    private boolean isAbstractedLcdpMethod(String methodName, GeneratedLcdpResourceBaseType baseType) {
        if (baseType == GeneratedLcdpResourceBaseType.AUDIT) {
            return LCDP_AUDIT_MAPPING_METHOD_NAME_SET.contains(methodName);
        }
        if (baseType == GeneratedLcdpResourceBaseType.BASE) {
            return LCDP_BASE_MAPPING_METHOD_NAME_SET.contains(methodName);
        }
        return false;
    }

    private LcdpMethodMeta selectPreferredGeneratedMethodMeta(List<LcdpMethodMeta> methodMetaList) {
        if (CollectionUtils.isEmpty(methodMetaList)) {
            return null;
        }
        return methodMetaList.stream()
                .sorted(Comparator.comparingInt(this::scoreGeneratedMethodMeta)
                        .thenComparing(meta -> String.join(",", meta.parameterTypeNameList)))
                .findFirst()
                .orElse(null);
    }

    private int scoreGeneratedMethodMeta(LcdpMethodMeta methodMeta) {
        if (isWrapperGeneratedMethodMeta(methodMeta)) {
            return 0;
        }
        if (isNoArgGeneratedMethodMeta(methodMeta)) {
            return 1;
        }
        if (isUploadGeneratedMethodMeta(methodMeta)) {
            return 2;
        }
        return 10 + methodMeta.parameterTypeNameList.size();
    }

    private boolean isWrapperGeneratedMethodMeta(LcdpMethodMeta methodMeta) {
        return methodMeta.parameterTypeNameList.size() == 1
                && StringUtils.equals(methodMeta.parameterTypeNameList.get(0), "com.sunwayworld.framework.restful.data.RestJsonWrapperBean");
    }

    private boolean isNoArgGeneratedMethodMeta(LcdpMethodMeta methodMeta) {
        return CollectionUtils.isEmpty(methodMeta.parameterTypeNameList);
    }

    private boolean isUploadGeneratedMethodMeta(LcdpMethodMeta methodMeta) {
        return methodMeta.parameterTypeNameList.size() == 2
                && StringUtils.equals(methodMeta.parameterTypeNameList.get(0), "com.sunwayworld.module.item.file.bean.CoreFileBean")
                && StringUtils.equals(methodMeta.parameterTypeNameList.get(1), "org.springframework.web.multipart.MultipartFile");
    }

    private String buildLcdpServiceRouteSegment(String serviceName) {
        String simpleName = StringUtils.removeStart(serviceName, "Lcdp");
        simpleName = StringUtils.removeEnd(simpleName, "Service");
        return defaultIfBlank(toRouteSegment(simpleName), toRouteSegment(serviceName));
    }

    private String buildLcdpResourceName(String serviceName) {
        String baseName = defaultIfBlank(serviceName, "");
        if (StringUtils.endsWith(baseName, "Service")) {
            baseName = baseName.substring(0, baseName.length() - "Service".length());
        }
        return baseName + "Resource";
    }

    private String renderLcdpJavaResourceInterface(ModuleContext context, GeneratedLcdpResourceSpec spec) {
        Set<String> imports = new LinkedHashSet<>();
        imports.add("org.springframework.web.bind.annotation.RequestMapping");
        imports.add("org.springframework.web.bind.annotation.RequestMethod");
        imports.add(spec.servicePackage + "." + spec.serviceName);
        switch (spec.baseType) {
            case AUDIT:
                imports.add("com.sunwayworld.cloud.module.lcdp.base.resource.LcdpAuditResource");
                break;
            case BASE:
                imports.add("com.sunwayworld.cloud.module.lcdp.base.resource.LcdpBaseResource");
                break;
            default:
                imports.add("com.sunwayworld.cloud.module.lcdp.base.resource.LcdpMethodResource");
                break;
        }
        collectGeneratedMethodImports(imports, spec.customMethodMetaList);

        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(spec.resourcePackage).append(";\n\n");
        appendImports(builder, imports);
        builder.append("\n");
        builder.append("@RequestMapping(\"").append(spec.requestPath).append("\")\n");
        builder.append("public interface ").append(spec.resourceInterfaceName).append(" extends ");
        builder.append(resolveGeneratedResourceParentType(spec)).append("<").append(spec.serviceName).append("> {\n\n");
        for (LcdpMethodMeta methodMeta : spec.customMethodMetaList) {
            appendGeneratedResourceInterfaceMethod(builder, methodMeta);
        }
        builder.append("}\n");
        return builder.toString();
    }

    private String renderLcdpJavaResourceImpl(ModuleContext context, GeneratedLcdpResourceSpec spec) {
        Set<String> imports = new LinkedHashSet<>();
        imports.add("org.springframework.beans.factory.annotation.Autowired");
        imports.add("org.springframework.web.bind.annotation.RequestMapping");
        imports.add("org.springframework.web.bind.annotation.RequestMethod");
        imports.add("org.springframework.web.bind.annotation.RestController");
        imports.add("com.sunwayworld.framework.log.annotation.Log");
        imports.add("com.sunwayworld.framework.log.annotation.LogModule");
        imports.add("com.sunwayworld.framework.log.annotation.LogType");
        imports.add("com.sunwayworld.framework.spring.annotation.GikamBean");
        imports.add(spec.resourcePackage + "." + spec.resourceInterfaceName);
        imports.add(spec.servicePackage + "." + spec.serviceName);
        switch (spec.baseType) {
            case AUDIT:
                imports.add("com.sunwayworld.cloud.module.lcdp.base.resource.impl.AbstractLcdpAuditResource");
                break;
            case BASE:
                imports.add("com.sunwayworld.cloud.module.lcdp.base.resource.impl.AbstractLcdpBaseResource");
                break;
            default:
                imports.add("com.sunwayworld.cloud.module.lcdp.base.resource.impl.AbstractLcdpMethodResource");
                break;
        }
        collectGeneratedMethodImports(imports, spec.customMethodMetaList);

        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(spec.resourceImplPackage).append(";\n\n");
        appendImports(builder, imports);
        builder.append("\n");
        builder.append("@GikamBean\n");
        builder.append("@RestController\n");
        builder.append("@RequestMapping(\"").append(spec.requestPath).append("\")\n");
        builder.append("@LogModule(value = \"").append(context.moduleCode).append("自动转换模块\")\n");
        builder.append("public class ").append(spec.resourceImplName).append(" implements ")
                .append(spec.resourceInterfaceName).append(", ")
                .append(resolveGeneratedResourceParentImplType(spec)).append("<").append(spec.serviceName).append("> {\n\n");
        builder.append("    @Autowired\n");
        builder.append("    private ").append(spec.serviceName).append(" ").append(spec.serviceFieldName).append(";\n\n");
        builder.append("    @Override\n");
        builder.append("    public ").append(spec.serviceName).append(" getService() {\n");
        builder.append("        return ").append(spec.serviceFieldName).append(";\n");
        builder.append("    }\n\n");
        for (LcdpMethodMeta methodMeta : spec.customMethodMetaList) {
            appendGeneratedResourceImplMethod(builder, methodMeta);
        }
        builder.append("}\n");
        return builder.toString();
    }

    private void collectGeneratedMethodImports(Set<String> imports, List<LcdpMethodMeta> methodMetaList) {
        for (LcdpMethodMeta methodMeta : methodMetaList) {
            addTypeImport(imports, methodMeta.returnTypeName);
            for (String parameterTypeName : methodMeta.parameterTypeNameList) {
                addTypeImport(imports, parameterTypeName);
            }
        }
    }

    private void addTypeImport(Set<String> imports, String typeName) {
        if (StringUtils.isBlank(typeName)) {
            return;
        }
        String normalizedTypeName = typeName.replace("[]", "");
        if (isPrimitiveTypeName(normalizedTypeName) || isJavaLangTypeName(normalizedTypeName) || !normalizedTypeName.contains(".")) {
            return;
        }
        imports.add(normalizedTypeName);
    }

    private String resolveGeneratedResourceParentType(GeneratedLcdpResourceSpec spec) {
        switch (spec.baseType) {
            case AUDIT:
                return "LcdpAuditResource";
            case BASE:
                return "LcdpBaseResource";
            default:
                return "LcdpMethodResource";
        }
    }

    private String resolveGeneratedResourceParentImplType(GeneratedLcdpResourceSpec spec) {
        switch (spec.baseType) {
            case AUDIT:
                return "AbstractLcdpAuditResource";
            case BASE:
                return "AbstractLcdpBaseResource";
            default:
                return "AbstractLcdpMethodResource";
        }
    }

    private void appendGeneratedResourceInterfaceMethod(StringBuilder builder, LcdpMethodMeta methodMeta) {
        builder.append("    @RequestMapping(value = \"/").append(methodMeta.methodName).append("\", method = ")
                .append(resolveGeneratedRequestMethodExpression(methodMeta)).append(")\n");
        builder.append("    ").append(resolveGeneratedTypeSimpleName(methodMeta.returnTypeName)).append(" ")
                .append(methodMeta.methodName).append("(").append(resolveGeneratedMethodParameters(methodMeta, false)).append(");\n\n");
    }

    private void appendGeneratedResourceImplMethod(StringBuilder builder, LcdpMethodMeta methodMeta) {
        builder.append("    @Override\n");
        builder.append("    @Log(value = \"").append(escapeJava(defaultIfBlank(methodMeta.mappingDesc, methodMeta.methodName))).append("\", type = ")
                .append(resolveGeneratedLogType(methodMeta)).append(")\n");
        builder.append("    @RequestMapping(value = \"/").append(methodMeta.methodName).append("\", method = ")
                .append(resolveGeneratedRequestMethodExpression(methodMeta)).append(")\n");
        if (!StringUtils.equals(methodMeta.returnTypeName, "void") && !isUploadGeneratedMethodMeta(methodMeta)) {
            builder.append("    @SuppressWarnings(\"unchecked\")\n");
        }
        builder.append("    public ").append(resolveGeneratedTypeSimpleName(methodMeta.returnTypeName)).append(" ")
                .append(methodMeta.methodName).append("(").append(resolveGeneratedMethodParameters(methodMeta, true)).append(") {\n");
        appendGeneratedResourceMethodBody(builder, methodMeta);
        builder.append("    }\n\n");
    }

    private void appendGeneratedResourceMethodBody(StringBuilder builder, LcdpMethodMeta methodMeta) {
        if (isUploadGeneratedMethodMeta(methodMeta)) {
            if (StringUtils.equals(methodMeta.returnTypeName, "void")) {
                builder.append("        getService().").append(methodMeta.methodName).append("(fileBean, file);\n");
            } else {
                builder.append("        return getService().").append(methodMeta.methodName).append("(fileBean, file);\n");
            }
            return;
        }

        String invokeArgument = isNoArgGeneratedMethodMeta(methodMeta) ? "null" : "wrapper";
        if (StringUtils.equals(methodMeta.returnTypeName, "void")) {
            builder.append("        invokeServiceMethod(\"").append(methodMeta.methodName).append("\", ").append(invokeArgument).append(");\n");
        } else {
            builder.append("        return (").append(resolveGeneratedTypeSimpleName(methodMeta.returnTypeName)).append(") ")
                    .append("invokeServiceMethod(\"").append(methodMeta.methodName).append("\", ").append(invokeArgument).append(");\n");
        }
    }

    private String resolveGeneratedMethodParameters(LcdpMethodMeta methodMeta, boolean implementation) {
        if (isUploadGeneratedMethodMeta(methodMeta)) {
            return "CoreFileBean fileBean, MultipartFile file";
        }
        if (isNoArgGeneratedMethodMeta(methodMeta)) {
            return "";
        }
        return "RestJsonWrapperBean wrapper";
    }

    private String resolveGeneratedRequestMethodExpression(LcdpMethodMeta methodMeta) {
        if (isUploadGeneratedMethodMeta(methodMeta)) {
            return "RequestMethod.POST";
        }
        return "{RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}";
    }

    private String resolveGeneratedLogType(LcdpMethodMeta methodMeta) {
        if (StringUtils.equalsIgnoreCase(methodMeta.mappingType, "INSERT")) {
            return "LogType.INSERT";
        }
        if (StringUtils.equalsIgnoreCase(methodMeta.mappingType, "DELETE")) {
            return "LogType.DELETE";
        }
        if (StringUtils.equalsIgnoreCase(methodMeta.mappingType, "UPDATE")) {
            return "LogType.UPDATE";
        }
        return "LogType.SELECT";
    }

    private String resolveGeneratedTypeSimpleName(String typeName) {
        if (StringUtils.isBlank(typeName)) {
            return "Object";
        }
        if (typeName.endsWith("[]")) {
            return resolveGeneratedTypeSimpleName(typeName.substring(0, typeName.length() - 2)) + "[]";
        }
        int index = typeName.lastIndexOf('.');
        return index < 0 ? typeName : typeName.substring(index + 1);
    }

    private String renderLcdpJavaService(ModuleContext context, LcdpResourceHistoryBean history, String packageName) {
        String source = history.getContent();
        if (StringUtils.isBlank(source)) {
            return null;
        }

        String content = source.replace("\r\n", "\n");
        content = content.replaceFirst("(?s)^\\s*package\\s+[^;]+;\\s*", "");
        String originalClassName = extractPublicClassName(content);
        if (StringUtils.isBlank(originalClassName)) {
            return null;
        }

        String targetClassName = history.getResourceName();
        content = content.replaceAll("\\b" + Pattern.quote(originalClassName) + "\\b", targetClassName);
        content = ensureImport(content, "com.sunwayworld.framework.spring.annotation.GikamBean");
        content = ensureImport(content, "org.springframework.stereotype.Repository");
        content = normalizeGeneratedServiceAnnotations(content, targetClassName);
        content = normalizeGeneratedPathImports(content);
        content = rewriteLegacyModulePackageReferences(content, context);
        content = sanitizeGeneratedJavaImports(content, packageName, targetClassName);
        return "package " + packageName + ";\n\n" + content.trim() + "\n";
    }

    private String rewriteLegacyModulePackageReferences(String content, ModuleContext context) {
        String normalized = defaultIfBlank(content, "");
        String legacyBasePackage = buildLegacyModuleBasePackage(context);
        if (StringUtils.isBlank(legacyBasePackage) || StringUtils.equals(legacyBasePackage, context.basePackage)) {
            return normalized;
        }
        return normalized.replace(legacyBasePackage + ".", context.basePackage + ".");
    }

    private String buildLegacyModuleBasePackage(ModuleContext context) {
        String categorySegment = sanitizePackageSegmentPreserveCase(context.moduleCategory);
        String moduleSegment = sanitizePackageSegmentPreserveCase(context.moduleName);
        if (StringUtils.isBlank(categorySegment) || StringUtils.isBlank(moduleSegment)) {
            return null;
        }
        return categorySegment + "." + moduleSegment;
    }

    private String buildPreserveCaseConvertedBasePackage(ModuleContext context) {
        String categorySegment = sanitizePackageSegmentPreserveCase(context.moduleCategory);
        String moduleSegment = sanitizePackageSegmentPreserveCase(context.moduleName);
        if (StringUtils.isBlank(categorySegment) || StringUtils.isBlank(moduleSegment)) {
            return null;
        }
        return CONVERTED_BASE_PACKAGE + "." + categorySegment + "." + moduleSegment;
    }

    private String normalizeGeneratedServiceAnnotations(String content, String targetClassName) {
        String normalized = defaultIfBlank(content, "");
        normalized = normalized.replaceAll("(?m)^\\s*@GikamBean\\s*\\n", "");
        if (normalized.contains("@Repository")) {
            normalized = normalized.replaceFirst("@Repository\\s*(\\([^)]*\\))?", "@Repository");
            normalized = normalized.replaceFirst("@Repository\\s*", "@Repository\n@GikamBean\n");
        } else {
            normalized = normalized.replaceFirst("(public\\s+class\\s+" + targetClassName + ")", "@Repository\n@GikamBean\n$1");
        }
        return normalized;
    }

    private String sanitizeGeneratedJavaImports(String content, String packageName, String className) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        Set<String> importSet = new LinkedHashSet<>();
        Matcher matcher = JAVA_IMPORT_PATTERN.matcher(content);
        while (matcher.find()) {
            String importName = matcher.group(1);
            if (StringUtils.isBlank(importName)) {
                continue;
            }
            if (StringUtils.startsWith(importName, packageName + ".")) {
                continue;
            }
            if (StringUtils.equals(importName, packageName + "." + className)) {
                continue;
            }
            importSet.add(importName);
        }
        String normalized = content.replaceAll("(?m)^\\s*import\\s+[A-Za-z0-9_.*]+\\s*;\\s*\\n?", "");
        StringBuilder importBuilder = new StringBuilder();
        for (String importName : importSet) {
            importBuilder.append("import ").append(importName).append(";\n");
        }
        if (importBuilder.length() == 0) {
            return normalized;
        }
        return importBuilder.append('\n').append(normalized.replaceFirst("^\\s*", "")).toString();
    }

    private String normalizeGeneratedPathImports(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        Set<String> pathImportSet = new LinkedHashSet<>();
        Matcher matcher = LCDP_PATH_IMPORT_PATTERN.matcher(content);
        while (matcher.find()) {
            String annotationContent = matcher.group(1);
            Matcher valueMatcher = QUOTED_VALUE_PATTERN.matcher(defaultIfBlank(annotationContent, ""));
            while (valueMatcher.find()) {
                String importName = firstNotBlank(valueMatcher.group(1), valueMatcher.group(2));
                if (StringUtils.isNotBlank(importName)) {
                    pathImportSet.add(importName.trim());
                }
            }
        }
        String normalized = content.replaceAll("(?m)^\\s*import\\s+com\\.sunwayworld\\.cloud\\.module\\.lcdp\\.base\\.annotation\\.LcdpPathImport\\s*;\\s*\\n?", "");
        normalized = LCDP_PATH_IMPORT_PATTERN.matcher(normalized).replaceAll("");
        for (String importName : pathImportSet) {
            normalized = ensureImport(normalized, importName);
        }
        return normalized;
    }

    private String ensureImport(String content, String importName) {
        if (content.contains("import " + importName + ";")) {
            return content;
        }
        int importIndex = content.lastIndexOf("import ");
        if (importIndex < 0) {
            return "import " + importName + ";\n" + content;
        }
        int insertIndex = content.indexOf('\n', importIndex);
        if (insertIndex < 0) {
            return content + "\nimport " + importName + ";\n";
        }
        return content.substring(0, insertIndex + 1) + "import " + importName + ";\n" + content.substring(insertIndex + 1);
    }

    private String extractPublicClassName(String content) {
        Matcher matcher = Pattern.compile("public\\s+class\\s+(\\w+)").matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String parsePackageNameFromPath(String resourcePath, String resourceName, String fallbackPackage) {
        if (StringUtils.isBlank(resourcePath)) {
            return fallbackPackage;
        }
        String[] segments = resourcePath.split("\\.");
        if (segments.length < 2) {
            return fallbackPackage;
        }
        int endIndex = segments.length;
        if (StringUtils.equals(segments[segments.length - 1], resourceName)) {
            endIndex--;
        }
        if (endIndex <= 0) {
            return fallbackPackage;
        }
        List<String> packageSegments = new ArrayList<>();
        for (int i = 0; i < endIndex; i++) {
            String packageSegment = sanitizePackageSegmentPreserveCase(segments[i]);
            if (StringUtils.isNotBlank(packageSegment)) {
                packageSegments.add(packageSegment);
            }
        }
        if (packageSegments.isEmpty()) {
            return fallbackPackage;
        }
        return String.join(".", packageSegments);
    }

    private String parsePackageNameFromQualifiedName(String qualifiedName, String fallbackPackage) {
        if (StringUtils.isBlank(qualifiedName) || !qualifiedName.contains(".")) {
            return fallbackPackage;
        }
        int index = qualifiedName.lastIndexOf('.');
        return parsePackageNameFromPath(qualifiedName.substring(0, index), null, fallbackPackage);
    }

    private String parseSimpleName(String qualifiedName, String fallbackName) {
        if (StringUtils.isBlank(qualifiedName)) {
            return fallbackName;
        }
        int index = qualifiedName.lastIndexOf('.');
        String simpleName = index < 0 ? qualifiedName : qualifiedName.substring(index + 1);
        return defaultIfBlank(sanitizeTypeName(simpleName), fallbackName);
    }

    private String buildModuleBasePackage(String categoryPackageSegment, String modulePackageSegment, String fallbackModuleCode) {
        List<String> segments = new ArrayList<>();
        if (StringUtils.isNotBlank(categoryPackageSegment)) {
            segments.add(categoryPackageSegment);
        }
        if (StringUtils.isNotBlank(modulePackageSegment)) {
            segments.add(modulePackageSegment);
        }
        if (segments.isEmpty()) {
            segments.add("item");
            segments.add(defaultIfBlank(toPackageSegment(fallbackModuleCode), "module"));
        }
        return CONVERTED_BASE_PACKAGE + "." + String.join(".", segments);
    }

    private String buildSecureRequestPath(String lcdpPath, String fallbackPath) {
        if (StringUtils.isBlank(lcdpPath)) {
            return fallbackPath;
        }
        String normalizedPath = lcdpPath.trim().replace('.', '/');
        normalizedPath = StringUtils.removeStart(normalizedPath, "/");
        if (StringUtils.startsWith(normalizedPath, "secure/")) {
            return "/" + normalizedPath;
        }
        return "/secure/" + normalizedPath;
    }

    private String resolvePrimaryServiceRequestPath(ModuleContext context) {
        String primaryServicePath = firstNotBlank(context.primaryServicePath,
                context.primaryJavaHistory == null ? null : context.primaryJavaHistory.getPath());
        return buildSecureRequestPath(primaryServicePath, context.requestPath);
    }

    private String sanitizePackageSegmentPreserveCase(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String sanitized = value.replaceAll("[^A-Za-z0-9_]", "");
        if (StringUtils.isBlank(sanitized)) {
            return null;
        }
        if (Character.isDigit(sanitized.charAt(0))) {
            sanitized = "p" + sanitized;
        }
        return sanitized;
    }

    private String sanitizeTypeName(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String sanitized = value.replaceAll("[^A-Za-z0-9_]", "");
        if (StringUtils.isBlank(sanitized)) {
            return null;
        }
        if (Character.isDigit(sanitized.charAt(0))) {
            sanitized = "T" + sanitized;
        }
        return sanitized;
    }

    private String loadLcdpRuntimeTemplate() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("lcdp-source-runtime.js")) {
            if (inputStream == null) {
                throw new ApplicationRuntimeException("未找到运行时模板：lcdp-source-runtime.js");
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > -1) {
                outputStream.write(buffer, 0, length);
            }
            return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ApplicationRuntimeException(e, "读取运行时模板失败");
        }
    }

    private String buildPageObjectName(ModuleContext context, String pageResourceName) {
        String pageName = StringUtils.removeEnd(pageResourceName, "Page");
        if (StringUtils.startsWithIgnoreCase(pageName, context.moduleCode)) {
            pageName = pageName.substring(context.moduleCode.length());
        }
        String result;
        if (StringUtils.isNotBlank(pageName) && pageName.matches("[A-Z][A-Za-z0-9]*")) {
            result = StringUtils.uncapitalize(pageName);
        } else {
            result = defaultIfBlank(toLowerCamel(pageName), "page");
        }
        return StringUtils.endsWithIgnoreCase(result, "Page") ? result : result + "Page";
    }

    private String buildControllerMethodName(String pageObjectName, boolean editListEntry) {
        if (editListEntry) {
            return "editListPage";
        }
        return pageObjectName;
    }

    private void mergeFieldMeta(Map<String, FieldMeta> fieldMetaMap, FieldMeta candidate) {
        if (candidate == null || StringUtils.isBlank(candidate.columnName)) {
            return;
        }
        String key = candidate.columnName.toLowerCase(Locale.ENGLISH);
        FieldMeta existing = fieldMetaMap.get(key);
        if (existing == null) {
            fieldMetaMap.put(key, candidate);
            return;
        }
        if (existing.type == null || String.class.equals(existing.type)) {
            existing.type = candidate.type;
        }
        existing.clob = existing.clob || candidate.clob;
        existing.id = existing.id || candidate.id;
        if (StringUtils.isBlank(existing.propertyName)) {
            existing.propertyName = candidate.propertyName;
        }
    }

    private FieldMeta buildFieldMeta(String rawField, String widgetType, String gridColumnType) {
        String columnName = rawField.toUpperCase(Locale.ENGLISH);
        FieldMeta meta = new FieldMeta();
        meta.columnName = columnName;
        meta.propertyName = toLowerCamel(rawField);
        meta.type = resolveJavaType(rawField, widgetType, gridColumnType);
        meta.clob = equalsAnyIgnoreCase(widgetType, "Textarea", "TextArea");
        meta.id = StringUtils.equalsIgnoreCase("ID", rawField);
        return meta;
    }

    private Class<?> resolveJavaType(String fieldName, String widgetType, String gridColumnType) {
        if (equalsAnyIgnoreCase(widgetType, "Date") || equalsAnyIgnoreCase(gridColumnType, "date", "datetime")) {
            return containsAnyIgnoreCase(fieldName, "time", "datetime") ? LocalDateTime.class : LocalDate.class;
        }
        if (containsAnyIgnoreCase(fieldName, "createdtime", "updatedtime", "modifytime", "plantime", "starttime", "endtime", "datetime")) {
            return LocalDateTime.class;
        }
        if (containsAnyIgnoreCase(fieldName, "date", "day")) {
            return LocalDate.class;
        }
        if (StringUtils.endsWithIgnoreCase(fieldName, "id")) {
            return Long.class;
        }
        if (containsAnyIgnoreCase(fieldName, "status", "type", "flag", "code", "name", "category", "mode", "method", "stage", "level", "tag", "title", "remark", "overview", "content")) {
            return String.class;
        }
        if (containsAnyIgnoreCase(fieldName, "version", "year", "month", "dayqty", "qty", "count", "index", "sort", "orderno", "ordernum", "level")) {
            return Integer.class;
        }
        if (containsAnyIgnoreCase(fieldName, "amount", "budget", "money", "price", "cost", "total", "rate", "score")) {
            return BigDecimal.class;
        }
        return String.class;
    }

    private Class<?> resolveJavaType(String fieldName, String fieldType, Integer precision, Integer scale) {
        String normalizedFieldType = ObjectUtils.toString(fieldType).toLowerCase(Locale.ENGLISH);
        if (containsAnyIgnoreCase(normalizedFieldType, "clob", "text", "longtext")) {
            return String.class;
        }
        if (containsAnyIgnoreCase(normalizedFieldType, "datetime", "timestamp")) {
            return LocalDateTime.class;
        }
        if (containsAnyIgnoreCase(normalizedFieldType, "date", "time")) {
            return containsAnyIgnoreCase(fieldName, "time", "datetime") ? LocalDateTime.class : LocalDate.class;
        }
        if (containsAnyIgnoreCase(normalizedFieldType, "number", "decimal", "numeric", "double", "float")) {
            if (scale != null && scale > 0) {
                return BigDecimal.class;
            }
            if (StringUtils.endsWithIgnoreCase(fieldName, "id")) {
                return Long.class;
            }
            return precision != null && precision <= 9 ? Integer.class : Long.class;
        }
        if (containsAnyIgnoreCase(normalizedFieldType, "int")) {
            return Integer.class;
        }
        return resolveJavaType(fieldName, null, null);
    }

    private boolean isClobField(LcdpTableFieldBean field) {
        if (field == null) {
            return false;
        }
        if (containsAnyIgnoreCase(field.getFieldType(), "clob", "text", "longtext")) {
            return true;
        }
        try {
            return Integer.parseInt(ObjectUtils.toString(field.getFieldLength())) > 4000;
        } catch (Exception e) {
            return false;
        }
    }

    private BeanBaseType resolveBeanBaseType(List<FieldMeta> fieldMetaList) {
        Set<String> columnSet = fieldMetaList.stream()
                .map(meta -> meta.columnName.toLowerCase(Locale.ENGLISH))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (columnSet.containsAll(Arrays.asList("createdbyid", "createdbyname", "createdtime", "createdbyorgid", "createdbyorgname", "processstatus", "version"))) {
            return BeanBaseType.AUDITABLE;
        }
        if (columnSet.containsAll(Arrays.asList("createdbyid", "createdbyname", "createdtime", "createdbyorgid", "createdbyorgname"))) {
            return BeanBaseType.INSERTABLE;
        }
        return BeanBaseType.PERSISTABLE;
    }

    private Path resolveProjectRoot(ConversionOptions options) {
        if (StringUtils.isNotBlank(options.outputRoot)) {
            return Paths.get(options.outputRoot).toAbsolutePath().normalize();
        }
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }

    private Path resolveTargetRoot(Path projectRoot, ConversionOptions options) {
        if (StringUtils.isBlank(options.targetModule)) {
            return projectRoot;
        }
        return projectRoot.resolve(options.targetModule).normalize();
    }

    private void validateTargetRoot(Path targetRoot) {
        if (!Files.isDirectory(targetRoot)) {
            throw new ApplicationRuntimeException("目标源码根目录不存在：" + targetRoot);
        }
        if (!Files.isDirectory(targetRoot.resolve("src/main/java")) || !Files.isDirectory(targetRoot.resolve("src/main/resources"))) {
            throw new ApplicationRuntimeException("目标目录不是标准源码目录：" + targetRoot);
        }
    }

    private List<GeneratedFile> generateFiles(ModuleContext context, Path targetRoot, List<String> warnings) {
        List<GeneratedFile> files = new ArrayList<>();
        Path javaRoot = targetRoot.resolve("src/main/java");
        Path resourcesRoot = targetRoot.resolve("src/main/resources");
        String basePackagePath = context.basePackage.replace('.', '/');
        String controllerPackagePath = context.controllerPackage.replace('.', '/');
        List<PageContext> pageContextList = buildPageContextList(context, warnings);
        syncPageI18nToCoreI18n(pageContextList, warnings);

        List<LcdpResourceHistoryBean> mapperHistoryList = new ArrayList<>(context.mapperHistoryList);
        mapperHistoryList.sort(Comparator.comparing(LcdpResourceHistoryBean::getResourceName));
        Set<String> generatedMapperInterfaceSet = new LinkedHashSet<>();
        for (LcdpResourceHistoryBean history : mapperHistoryList) {
            GeneratedMapperSpec mapperSpec = resolveGeneratedMapperSpec(context, history, warnings);
            if (generatedMapperInterfaceSet.add(mapperSpec.namespace)) {
                GeneratedFile mapperInterfaceFile = new GeneratedFile(javaRoot.resolve(mapperSpec.packagePath + "/" + mapperSpec.mapperName + ".java"),
                        renderMapperInterface(mapperSpec, context));
                files.add(mapperInterfaceFile);
                markResourceConverted(context, history.getResourceId(), mapperInterfaceFile.path);
            }
            String fileName = buildMapperFileName(history.getResourceName());
            GeneratedFile mapperXmlFile = new GeneratedFile(javaRoot.resolve(mapperSpec.packagePath + "/conf/" + fileName),
                    renderMapperXml(context, mapperSpec, history.getContent()));
            files.add(mapperXmlFile);
            markResourceConverted(context, history.getResourceId(), mapperXmlFile.path);
        }

        for (LcdpResourceHistoryBean history : context.javaHistoryList) {
            GeneratedFile serviceFile = buildLcdpJavaServiceFile(context, javaRoot, history);
            if (serviceFile != null) {
                files.add(serviceFile);
                markResourceConverted(context, history.getResourceId(), serviceFile.path);
            }
            GeneratedFile resourceInterfaceFile = buildLcdpJavaResourceInterfaceFile(context, javaRoot, history);
            if (resourceInterfaceFile != null) {
                files.add(resourceInterfaceFile);
                markResourceConverted(context, history.getResourceId(), resourceInterfaceFile.path);
            }
            GeneratedFile resourceImplFile = buildLcdpJavaResourceImplFile(context, javaRoot, history);
            if (resourceImplFile != null) {
                files.add(resourceImplFile);
                markResourceConverted(context, history.getResourceId(), resourceImplFile.path);
            }
            if (serviceFile == null && resourceInterfaceFile == null && resourceImplFile == null) {
                markResourceSkipped(context, history.getResourceId(), "未识别到可生成的 Service / Resource 代码");
            }
        }

        if (!CollectionUtils.isEmpty(pageContextList)) {
            GeneratedFile pageRouteMappingFile = new GeneratedFile(javaRoot.resolve(basePackagePath + "/lcdp-page-route-mapping.txt"),
                    renderPageRouteMappingText(context, pageContextList));
            files.add(pageRouteMappingFile);
            context.moduleGeneratedFiles.add(pageRouteMappingFile.path);
            GeneratedFile controllerInterfaceFile = new GeneratedFile(javaRoot.resolve(controllerPackagePath + "/" + context.className + "Controller.java"),
                    renderControllerInterface(context, pageContextList));
            files.add(controllerInterfaceFile);
            context.moduleGeneratedFiles.add(controllerInterfaceFile.path);
            GeneratedFile controllerImplFile = new GeneratedFile(javaRoot.resolve(controllerPackagePath + "/impl/" + context.className + "ControllerImpl.java"),
                    renderControllerImpl(context, pageContextList));
            files.add(controllerImplFile);
            context.moduleGeneratedFiles.add(controllerImplFile.path);
            for (PageContext pageContext : pageContextList) {
                GeneratedFile templateFile = new GeneratedFile(resourcesRoot.resolve("templates").resolve(context.moduleResourcePath)
                        .resolve(pageContext.templateName + ".html"), renderTemplate(context, pageContext));
                files.add(templateFile);
                markResourceConverted(context, pageContext.pageResourceId, templateFile.path);
                GeneratedFile pageJsFile = new GeneratedFile(resourcesRoot.resolve("static").resolve(context.moduleResourcePath)
                        .resolve(pageContext.templateName + ".js"), renderPageJs(context, pageContext, pageContextList));
                files.add(pageJsFile);
                markResourceConverted(context, pageContext.pageResourceId, pageJsFile.path);
            }
        }
        finalizePendingResourceConvertRecords(context);
        GeneratedFile reportFile = new GeneratedFile(javaRoot.resolve(basePackagePath + "/lcdp-convert-report.txt"),
                renderConvertReport(context, warnings));
        files.add(reportFile);
        context.reportFile = reportFile.path;
        context.moduleGeneratedFiles.add(reportFile.path);
        logGeneratedFiles(context, files);
        return files;
    }

    private void logGeneratedFiles(ModuleContext context, List<GeneratedFile> files) {
        for (GeneratedFile file : files) {
            LOGGER.info("LCDP 转源码准备生成文件，moduleId={}, moduleCode={}, file={}",
                    context.moduleId, context.moduleCode, file.path);
        }
    }

    private void registerResourceConvertRecord(ModuleContext context, LcdpResourceBean resource) {
        ResourceConvertRecord record = new ResourceConvertRecord();
        record.resourceId = resource.getId();
        record.resourceName = resource.getResourceName();
        record.resourceCategory = resource.getResourceCategory();
        record.resourcePath = resource.getPath();
        record.status = ResourceConvertStatus.PENDING;
        context.resourceConvertRecordMap.put(resource.getId(), record);
    }

    private void updateResourceRecordPath(ModuleContext context, Long resourceId, String path) {
        ResourceConvertRecord record = context.resourceConvertRecordMap.get(resourceId);
        if (record != null && StringUtils.isNotBlank(path)) {
            record.resourcePath = path;
        }
    }

    private void markResourceConverted(ModuleContext context, Long resourceId, Path generatedPath) {
        if (resourceId == null || generatedPath == null) {
            return;
        }
        ResourceConvertRecord record = context.resourceConvertRecordMap.get(resourceId);
        if (record == null) {
            return;
        }
        record.status = ResourceConvertStatus.CONVERTED;
        record.reason = null;
        record.generatedFiles.add(generatedPath.toString());
    }

    private void markResourceSkipped(ModuleContext context, Long resourceId, String reason) {
        if (resourceId == null) {
            return;
        }
        ResourceConvertRecord record = context.resourceConvertRecordMap.get(resourceId);
        if (record == null || record.status == ResourceConvertStatus.CONVERTED) {
            return;
        }
        record.status = ResourceConvertStatus.SKIPPED;
        record.reason = reason;
    }

    private void finalizePendingResourceConvertRecords(ModuleContext context) {
        for (ResourceConvertRecord record : context.resourceConvertRecordMap.values()) {
            if (record.status == ResourceConvertStatus.PENDING) {
                record.status = ResourceConvertStatus.SKIPPED;
                record.reason = "未生成任何源码文件";
            }
        }
    }

    private List<String> buildConvertedResourceSummaryList(ModuleContext context) {
        return context.resourceConvertRecordMap.values().stream()
                .filter(record -> record.status == ResourceConvertStatus.CONVERTED)
                .map(this::buildResourceRecordSummary)
                .collect(Collectors.toList());
    }

    private List<String> buildSkippedResourceSummaryList(ModuleContext context) {
        return context.resourceConvertRecordMap.values().stream()
                .filter(record -> record.status == ResourceConvertStatus.SKIPPED)
                .map(this::buildResourceRecordSummary)
                .collect(Collectors.toList());
    }

    private String buildResourceRecordSummary(ResourceConvertRecord record) {
        StringBuilder builder = new StringBuilder();
        builder.append("[").append(defaultIfBlank(record.resourceCategory, "")).append("] ");
        builder.append(defaultIfBlank(record.resourceName, ""));
        if (StringUtils.isNotBlank(record.resourcePath)) {
            builder.append(" (").append(record.resourcePath).append(")");
        }
        if (record.status == ResourceConvertStatus.SKIPPED && StringUtils.isNotBlank(record.reason)) {
            builder.append(" - ").append(record.reason);
        }
        if (record.status == ResourceConvertStatus.CONVERTED && !CollectionUtils.isEmpty(record.generatedFiles)) {
            builder.append(" -> ").append(String.join(", ", record.generatedFiles));
        }
        return builder.toString();
    }

    private String renderConvertReport(ModuleContext context, List<String> warnings) {
        StringBuilder builder = new StringBuilder();
        builder.append("moduleId=").append(context.moduleId == null ? "" : context.moduleId).append("\n");
        builder.append("moduleCode=").append(defaultIfBlank(context.moduleCode, "")).append("\n");
        builder.append("requestPath=").append(defaultIfBlank(context.requestPath, "")).append("\n");
        builder.append("\n");

        List<ResourceConvertRecord> convertedRecordList = context.resourceConvertRecordMap.values().stream()
                .filter(record -> record.status == ResourceConvertStatus.CONVERTED)
                .collect(Collectors.toList());
        List<ResourceConvertRecord> skippedRecordList = context.resourceConvertRecordMap.values().stream()
                .filter(record -> record.status == ResourceConvertStatus.SKIPPED)
                .collect(Collectors.toList());

        builder.append("[converted-resources]").append("\n");
        builder.append("count=").append(convertedRecordList.size()).append("\n");
        for (ResourceConvertRecord record : convertedRecordList) {
            builder.append(buildResourceReportBlock(record));
        }
        builder.append("\n");

        builder.append("[skipped-resources]").append("\n");
        builder.append("count=").append(skippedRecordList.size()).append("\n");
        for (ResourceConvertRecord record : skippedRecordList) {
            builder.append(buildResourceReportBlock(record));
        }
        builder.append("\n");

        builder.append("[module-generated-files]").append("\n");
        for (Path path : context.moduleGeneratedFiles) {
            builder.append(path).append("\n");
        }
        builder.append("\n");

        builder.append("[warnings]").append("\n");
        for (String warning : warnings) {
            builder.append(warning).append("\n");
        }
        return builder.toString();
    }

    private String buildResourceReportBlock(ResourceConvertRecord record) {
        StringBuilder builder = new StringBuilder();
        builder.append("resourceId=").append(record.resourceId == null ? "" : record.resourceId).append("\n");
        builder.append("resourceName=").append(defaultIfBlank(record.resourceName, "")).append("\n");
        builder.append("resourceCategory=").append(defaultIfBlank(record.resourceCategory, "")).append("\n");
        builder.append("resourcePath=").append(defaultIfBlank(record.resourcePath, "")).append("\n");
        builder.append("status=").append(record.status == null ? "" : record.status.name()).append("\n");
        builder.append("reason=").append(defaultIfBlank(record.reason, "")).append("\n");
        if (!CollectionUtils.isEmpty(record.generatedFiles)) {
            for (String generatedFile : record.generatedFiles) {
                builder.append("generatedFile=").append(generatedFile).append("\n");
            }
        }
        builder.append("\n");
        return builder.toString();
    }

    private void persistConvertedResourceRecords(ModuleContext context, Path outputRoot) {
        List<LcdpResourceConvertRecordBean> recordList = context.resourceConvertRecordMap.values().stream()
                .filter(record -> record.status == ResourceConvertStatus.CONVERTED)
                .map(record -> toResourceConvertRecordBean(context, outputRoot, record))
                .collect(Collectors.toList());
        resourceConvertRecordService.saveConvertedRecords(recordList);
    }

    private LcdpResourceConvertRecordBean toResourceConvertRecordBean(ModuleContext context, Path outputRoot, ResourceConvertRecord record) {
        LcdpResourceConvertRecordBean bean = new LcdpResourceConvertRecordBean();
        bean.setResourceId(record.resourceId);
        bean.setModuleId(context.moduleId);
        bean.setModuleName(context.moduleCode);
        bean.setResourceName(record.resourceName);
        bean.setResourceCategory(record.resourceCategory);
        bean.setResourcePath(record.resourcePath);
        bean.setOutputRoot(outputRoot == null ? null : outputRoot.toString());
        bean.setConvertStatus("已转换");
        bean.setGeneratedFiles(JSON.toJSONString(record.generatedFiles));
        return bean;
    }

    private List<PageContext> buildPageContextList(ModuleContext context, List<String> warnings) {
        if (CollectionUtils.isEmpty(context.viewHistoryList)) {
            warnings.add("模块下没有页面资源，页面文件未生成");
            return new ArrayList<>();
        }

        List<PageContext> pageContextList = new ArrayList<>();
        for (LcdpResourceHistoryBean viewHistory : context.viewHistoryList) {
            List<LcdpModulePageCompBean> compList = loadPageCompList(viewHistory, warnings);
            if (CollectionUtils.isEmpty(compList)) {
                warnings.add("页面缺少组件配置，已跳过：" + viewHistory.getResourceName());
                markResourceSkipped(context, viewHistory.getResourceId(), "页面缺少组件配置");
                continue;
            }

            PageContext pageContext = new PageContext();
            pageContext.pageResourceId = viewHistory.getResourceId();
            pageContext.pageResourceName = viewHistory.getResourceName();
            pageContext.pagePath = viewHistory.getPath();
            pageContext.pageObjectName = buildPageObjectName(context, viewHistory.getResourceName());
            pageContext.templateName = buildTemplateName(context.moduleCode, context.modulePath, viewHistory.getResourceName());
            pageContext.controllerPath = buildSecureRequestPath(pageContext.pagePath, context.requestPath);
            pageContext.serviceRequestPath = buildSecureRequestPath(firstNotBlank(inferServicePath(viewHistory), context.primaryServicePath),
                    resolvePrimaryServiceRequestPath(context));
            pageContext.routePath = ObjectUtils.equals(viewHistory.getId(), context.primaryViewHistory == null ? null : context.primaryViewHistory.getId())
                    ? "edit-list"
                    : pageContext.templateName;
            pageContext.methodName = buildControllerMethodName(pageContext.pageObjectName,
                    StringUtils.equals(pageContext.routePath, "edit-list"));
            pageContext.editListEntry = StringUtils.equals("edit-list", pageContext.routePath);
            pageContext.compList = compList;
            pageContext.pageI18nMap = loadPageI18n(viewHistory);
            pageContext.viewContent = ObjectUtils.toString(viewHistory.getContent());
            pageContext.widgetListJson = JSON.toJSONString(compList);
            pageContext.i18nJson = JSON.toJSONString(pageContext.pageI18nMap);
            pageContextList.add(pageContext);
        }
        if (!CollectionUtils.isEmpty(pageContextList)
                && pageContextList.stream().noneMatch(functionPage -> functionPage.editListEntry)) {
            PageContext firstPage = pageContextList.get(0);
            firstPage.editListEntry = true;
            firstPage.routePath = "edit-list";
            firstPage.methodName = "editListPage";
        }
        return pageContextList;
    }

    private String renderPageRouteMappingText(ModuleContext context, List<PageContext> pageContextList) {
        StringBuilder builder = new StringBuilder();
        for (PageContext pageContext : pageContextList) {
            builder.append("update t_core_menu set HREF = '")
                    .append(defaultIfBlank(pageContext.controllerPath, ""))
                    .append("', RESOURCEID = null, RESOURCENAME = null, RESOURCEPATH = null where RESOURCEID = '")
                    .append(pageContext.pageResourceId == null ? "" : pageContext.pageResourceId)
                    .append("' and RESOURCEPATH = '")
                    .append(defaultIfBlank(pageContext.pagePath, ""))
                    .append("';\n");
        }
        return builder.toString();
    }

    private PageContext buildEditListPageContext(ModuleContext context, List<String> warnings) {
        if (context.primaryViewHistory == null) {
            warnings.add("模块下没有页面资源，前端文件未生成");
            return null;
        }
        LcdpResourceHistoryBean viewHistory = context.primaryViewHistory;

        List<LcdpModulePageCompBean> compList = loadPageCompList(viewHistory, warnings);
        if (CollectionUtils.isEmpty(compList)) {
            warnings.add("页面缺少组件配置，前端文件未生成：" + viewHistory.getResourceName());
            return null;
        }

        PageContext pageContext = new PageContext();
        pageContext.pageResourceName = viewHistory.getResourceName();
        String pageName = StringUtils.removeEnd(viewHistory.getResourceName(), "Page");
        if (StringUtils.startsWithIgnoreCase(pageName, context.moduleCode)) {
            pageName = pageName.substring(context.moduleCode.length());
        }
        pageContext.pageObjectName = defaultIfBlank(toLowerCamel(pageName), "page");
        if (!StringUtils.endsWithIgnoreCase(pageContext.pageObjectName, "Page")) {
            pageContext.pageObjectName = pageContext.pageObjectName + "Page";
        }
        pageContext.templateName = buildTemplateName(context.moduleCode, context.modulePath, viewHistory.getResourceName());
        pageContext.grid = findFirstComp(compList, "Grid", "TreeGrid");
        pageContext.form = findFirstComp(compList, "Form");
        pageContext.viewContent = ObjectUtils.toString(viewHistory.getContent());

        if (pageContext.grid == null && pageContext.form == null) {
            warnings.add("页面缺少可识别的 Grid / Form 组件，前端文件未生成：" + viewHistory.getResourceName());
            return null;
        }
        if (pageContext.grid == null) {
            warnings.add("当前暂不支持仅 Form 详情页自动落源码，已跳过：" + viewHistory.getResourceName());
            return null;
        }

        if (pageContext.grid != null) {
            JSONObject gridConfig = parseConfig(pageContext.grid);
            pageContext.gridId = defaultIfBlank(gridConfig.getString("id"), context.modulePath + "-grid");
            pageContext.gridComponentType = StringUtils.equalsIgnoreCase("TreeGrid", pageContext.grid.getType()) ? "treeGrid" : "grid";
            pageContext.gridColumns = buildGridColumns(gridConfig, compList, context.fieldMetaByLowerColumn, context.pageI18nMap);
        } else {
            pageContext.gridId = context.modulePath + "-grid";
            pageContext.gridComponentType = "grid";
            pageContext.gridColumns = new ArrayList<>();
        }
        if (pageContext.form != null) {
            JSONObject formConfig = parseConfig(pageContext.form);
            pageContext.formId = defaultIfBlank(formConfig.getString("id"), context.modulePath + "-form");
            pageContext.formColumns = formConfig.getInteger("columns") == null ? 2 : formConfig.getInteger("columns");
            pageContext.formPanelTitle = findFormPanelTitle(compList, pageContext.form, context.pageI18nMap);
            pageContext.formFields = buildFormFields(formConfig, compList, context.fieldMetaByLowerColumn, context.pageI18nMap);
        } else {
            pageContext.formId = context.modulePath + "-form";
            pageContext.formColumns = 2;
            pageContext.formPanelTitle = "基本信息";
            pageContext.formFields = new ArrayList<>();
            warnings.add("页面未识别到 Form 组件，将按列表页生成：" + viewHistory.getResourceName());
        }
        pageContext.insertFields = parseInsertFields(pageContext.viewContent, context.fieldMetaByLowerColumn);
        if (CollectionUtils.isEmpty(pageContext.insertFields) && !CollectionUtils.isEmpty(pageContext.formFields)) {
            pageContext.insertFields = Collections.singletonList(pageContext.formFields.stream()
                    .filter(field -> !field.readonly)
                    .map(field -> field.field)
                    .findFirst()
                    .orElse(pageContext.formFields.get(0).field));
        }
        if (CollectionUtils.isEmpty(pageContext.insertFields)) {
            pageContext.insertFields = context.fieldMetaList.stream()
                    .filter(field -> !field.id)
                    .map(field -> field.propertyName)
                    .limit(1)
                    .collect(Collectors.toList());
        }
        pageContext.primaryDisplayField = pageContext.formFields.stream()
                .map(field -> field.field)
                .filter(field -> !StringUtils.equals(field, "id"))
                .findFirst()
                .orElseGet(() -> pageContext.gridColumns.stream()
                        .map(column -> column.field)
                        .filter(StringUtils::isNotBlank)
                        .filter(field -> !StringUtils.equals(field, "id"))
                        .findFirst()
                        .orElse("id"));

        if (CollectionUtils.isEmpty(pageContext.gridColumns) && pageContext.grid != null) {
            warnings.add("页面列表字段提取为空，前端文件未生成：" + viewHistory.getResourceName());
            return null;
        }
        if (pageContext.form != null && CollectionUtils.isEmpty(pageContext.formFields)) {
            warnings.add("页面表单字段提取为空，已降级为列表页：" + viewHistory.getResourceName());
            pageContext.form = null;
        }
        if (pageContext.grid == null && pageContext.form == null) {
            warnings.add("页面转换后无可用组件，前端文件未生成：" + viewHistory.getResourceName());
            return null;
        }
        return pageContext;
    }

    private LcdpModulePageCompBean findFirstComp(List<LcdpModulePageCompBean> compList, String... types) {
        return compList.stream().filter(comp -> Arrays.stream(types).anyMatch(type -> StringUtils.equals(type, comp.getType()))).findFirst().orElse(null);
    }

    private List<LcdpModulePageCompBean> loadPageCompList(LcdpResourceHistoryBean viewHistory, List<String> warnings) {
        if (viewHistory == null) {
            return new ArrayList<>();
        }
        List<LcdpModulePageCompBean> compList = modulePageCompService.selectByModulePageHistoryId(viewHistory.getId());
        if (!CollectionUtils.isEmpty(compList)) {
            return compList;
        }
        compList = modulePageCompService.selectByModulePageId(viewHistory.getResourceId());
        if (!CollectionUtils.isEmpty(compList)) {
            warnings.add("页面组件未命中历史版本，已按页面资源ID读取：" + viewHistory.getResourceName());
            return compList;
        }
        return new ArrayList<>();
    }

    private String findFormPanelTitle(List<LcdpModulePageCompBean> compList, LcdpModulePageCompBean formComp,
                                      Map<String, Map<String, String>> pageI18nMap) {
        Map<String, LcdpModulePageCompBean> compMap = compList.stream().collect(Collectors.toMap(LcdpModulePageCompBean::getId, comp -> comp, (left, right) -> left));
        String parentId = formComp.getParentId();
        while (StringUtils.isNotBlank(parentId)) {
            LcdpModulePageCompBean parent = compMap.get(parentId);
            if (parent == null) {
                break;
            }
            if (StringUtils.equals("TabPanel", parent.getType())) {
                String title = resolveTitle(parseConfig(parent).get("title"), pageI18nMap);
                return defaultIfBlank(title, "基本信息");
            }
            parentId = parent.getParentId();
        }
        return "基本信息";
    }

    private List<GridColumnMeta> buildGridColumns(JSONObject gridConfig, List<LcdpModulePageCompBean> compList,
                                                  Map<String, FieldMeta> fieldMetaByLowerColumn,
                                                  Map<String, Map<String, String>> pageI18nMap) {
        Map<String, LcdpModulePageCompBean> compMap = compList.stream().collect(Collectors.toMap(LcdpModulePageCompBean::getId, comp -> comp, (left, right) -> left));
        List<GridColumnMeta> columns = new ArrayList<>();
        JSONArray childIds = gridConfig.getJSONArray("childrenWidgetId");
        if (childIds == null) {
            return columns;
        }
        for (Object childIdObj : childIds) {
            LcdpModulePageCompBean child = compMap.get(ObjectUtils.toString(childIdObj));
            if (child == null || !(StringUtils.equals("GridColumn", child.getType()) || StringUtils.equals("TreeGridColumn", child.getType()))) {
                continue;
            }
            JSONObject columnConfig = parseConfig(child);
            GridColumnMeta meta = new GridColumnMeta();
            if (Boolean.TRUE.equals(columnConfig.getBoolean("checkbox"))) {
                meta.checkbox = true;
            } else if (Boolean.TRUE.equals(columnConfig.getBoolean("index"))) {
                meta.index = true;
            } else {
                String rawField = columnConfig.getString("field");
                meta.field = resolveFieldName(rawField, fieldMetaByLowerColumn);
                meta.title = defaultIfBlank(resolveTitle(columnConfig.get("title"), pageI18nMap), humanizeFieldTitle(rawField));
                meta.width = columnConfig.getInteger("width");
                String gridColumnType = firstNotBlank(columnConfig.getString("gridColumnType"), columnConfig.getString("type"));
                if (equalsAnyIgnoreCase(gridColumnType, "date", "datetime")) {
                    meta.type = "date";
                }
            }
            columns.add(meta);
        }
        return columns;
    }

    private LcdpResourceHistoryBean selectPrimaryViewHistory(List<LcdpResourceHistoryBean> viewHistoryList, String moduleCode) {
        if (CollectionUtils.isEmpty(viewHistoryList)) {
            return null;
        }
        String exactEditPage = moduleCode + "EditPage";
        LcdpResourceHistoryBean exactMatched = viewHistoryList.stream()
                .filter(item -> StringUtils.equalsIgnoreCase(item.getResourceName(), exactEditPage))
                .findFirst()
                .orElse(null);
        if (exactMatched != null) {
            return exactMatched;
        }
        LcdpResourceHistoryBean matchedEditPage = viewHistoryList.stream()
                .filter(item -> StringUtils.endsWithIgnoreCase(item.getResourceName(), "EditPage"))
                .findFirst()
                .orElse(null);
        if (matchedEditPage != null) {
            return matchedEditPage;
        }
        LcdpResourceHistoryBean exactPage = viewHistoryList.stream()
                .filter(item -> StringUtils.equalsIgnoreCase(item.getResourceName(), moduleCode + "Page"))
                .findFirst()
                .orElse(null);
        return exactPage == null ? viewHistoryList.get(0) : exactPage;
    }

    private String inferServicePath(LcdpResourceHistoryBean viewHistory) {
        if (viewHistory == null || StringUtils.isBlank(viewHistory.getContent())) {
            return null;
        }
        Matcher baseUrlMatcher = BASE_URL_PATTERN.matcher(viewHistory.getContent());
        if (baseUrlMatcher.find()) {
            return baseUrlMatcher.group(1);
        }
        Matcher servicePathMatcher = SERVICE_PATH_PATTERN.matcher(viewHistory.getContent());
        if (servicePathMatcher.find()) {
            return servicePathMatcher.group(1);
        }
        return null;
    }

    private LcdpResourceHistoryBean findPrimaryJavaHistory(ModuleContext context) {
        if (CollectionUtils.isEmpty(context.javaHistoryList)) {
            return null;
        }
        if (StringUtils.isNotBlank(context.primaryServicePath)) {
            LcdpResourceHistoryBean history = context.javaHistoryByPath.get(context.primaryServicePath);
            if (history != null) {
                return history;
            }
            String serviceName = context.primaryServicePath.substring(context.primaryServicePath.lastIndexOf('.') + 1);
            history = context.javaHistoryByName.get(serviceName);
            if (history != null) {
                return history;
            }
        }
        String expectedServiceName = context.className + "Service";
        LcdpResourceHistoryBean history = context.javaHistoryList.stream()
                .filter(item -> StringUtils.equalsIgnoreCase(item.getResourceName(), expectedServiceName))
                .findFirst()
                .orElse(null);
        if (history != null) {
            return history;
        }
        history = context.javaHistoryList.stream()
                .filter(item -> StringUtils.containsIgnoreCase(item.getResourceName(), context.className))
                .findFirst()
                .orElse(null);
        if (history != null) {
            return history;
        }
        return context.javaHistoryList.size() == 1 ? context.javaHistoryList.get(0) : null;
    }

    private List<LcdpResourceHistoryBean> filterMapperHistoryByService(List<LcdpResourceHistoryBean> mapperHistoryList, String serviceName) {
        if (CollectionUtils.isEmpty(mapperHistoryList) || StringUtils.isBlank(serviceName)) {
            return mapperHistoryList;
        }
        String mapperPrefix = StringUtils.removeEnd(serviceName, "Service");
        List<LcdpResourceHistoryBean> matchedList = mapperHistoryList.stream()
                .filter(item -> StringUtils.startsWithIgnoreCase(item.getResourceName(), mapperPrefix))
                .collect(Collectors.toList());
        return CollectionUtils.isEmpty(matchedList) ? mapperHistoryList : matchedList;
    }

    private String inferTableNameFromJavaContent(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        Matcher matcher = TABLE_NAME_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String inferTableNameFromJavaList(List<LcdpResourceHistoryBean> javaHistoryList, List<String> warnings) {
        if (CollectionUtils.isEmpty(javaHistoryList)) {
            return null;
        }
        Set<String> tableNameSet = javaHistoryList.stream()
                .map(LcdpResourceHistoryBean::getContent)
                .map(this::inferTableNameFromJavaContent)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (tableNameSet.size() == 1) {
            String tableName = tableNameSet.iterator().next();
            warnings.add("模块未配置关联表，已从服务脚本反推主表：" + tableName);
            return tableName;
        }
        return null;
    }

    private void applyModulePathMetadata(ModuleContext context, ModulePathMetadata metadata) {
        context.moduleCategory = metadata.categoryName;
        context.moduleName = metadata.moduleName;
        context.modulePath = metadata.moduleRouteSegment;
        context.moduleRoutePath = metadata.routePath;
        context.moduleResourcePath = "module/" + metadata.routePath;
        context.basePackage = metadata.basePackage;
        context.controllerPackage = context.basePackage + ".controller";
        context.requestPath = buildSecureRequestPath(metadata.categoryName + "." + metadata.moduleName, "/secure/" + metadata.routePath);
    }

    private ModulePathMetadata resolveModulePathMetadata(LcdpResourceBean module, ModuleContext context, List<String> warnings) {
        String modulePathSource = firstNotBlank(
                module.getPath(),
                context.primaryServicePath,
                context.primaryViewHistory == null ? null : context.primaryViewHistory.getPath(),
                firstHistoryPath(context.viewHistoryList),
                firstHistoryPath(context.javaHistoryList),
                firstHistoryPath(context.mapperHistoryList));
        if (StringUtils.isBlank(modulePathSource)) {
            warnings.add("模块未找到可用 path，已退回默认 item/" + context.modulePath);
            return buildFallbackModulePathMetadata(context.moduleCode, context.modulePath);
        }

        String[] pathParts = modulePathSource.split("\\.");
        if (pathParts.length < 2) {
            warnings.add("模块 path 不符合“分类.模块”格式，已退回默认 item/" + context.modulePath + "：" + modulePathSource);
            return buildFallbackModulePathMetadata(context.moduleCode, context.modulePath);
        }

        String categoryName = pathParts[0];
        String moduleName = pathParts[1];
        ModulePathMetadata metadata = new ModulePathMetadata();
        metadata.categoryName = categoryName;
        metadata.moduleName = moduleName;
        metadata.categoryRouteSegment = defaultIfBlank(toRouteSegment(categoryName), "item");
        metadata.moduleRouteSegment = defaultIfBlank(toRouteSegment(moduleName), context.modulePath);
        metadata.categoryPackageSegment = defaultIfBlank(toPackageSegment(categoryName), "item");
        metadata.modulePackageSegment = defaultIfBlank(toPackageSegment(moduleName), toPackageSegment(context.moduleCode));
        metadata.basePackage = buildModuleBasePackage(metadata.categoryPackageSegment, metadata.modulePackageSegment, context.moduleCode);
        metadata.routePath = metadata.categoryRouteSegment + "/" + metadata.moduleRouteSegment;
        return metadata;
    }

    private ModulePathMetadata buildFallbackModulePathMetadata(String moduleCode, String modulePath) {
        ModulePathMetadata metadata = new ModulePathMetadata();
        metadata.categoryName = "item";
        metadata.moduleName = moduleCode;
        metadata.categoryRouteSegment = "item";
        metadata.moduleRouteSegment = defaultIfBlank(modulePath, "module");
        metadata.categoryPackageSegment = "item";
        metadata.modulePackageSegment = defaultIfBlank(toPackageSegment(moduleCode), "module");
        metadata.basePackage = buildModuleBasePackage(metadata.categoryPackageSegment, metadata.modulePackageSegment, moduleCode);
        metadata.routePath = metadata.categoryRouteSegment + "/" + metadata.moduleRouteSegment;
        return metadata;
    }

    private String firstHistoryPath(List<LcdpResourceHistoryBean> historyList) {
        if (CollectionUtils.isEmpty(historyList)) {
            return null;
        }
        for (LcdpResourceHistoryBean history : historyList) {
            if (StringUtils.isNotBlank(history.getPath())) {
                return history.getPath();
            }
        }
        return null;
    }

    private String buildTemplateName(String moduleCode, String modulePath, String pageResourceName) {
        if (StringUtils.equalsIgnoreCase(pageResourceName, moduleCode + "EditPage")) {
            return modulePath + "-edit-list";
        }
        String pageName = StringUtils.removeEnd(pageResourceName, "Page");
        String kebabName = pageName.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase(Locale.ENGLISH);
        return defaultIfBlank(kebabName, modulePath + "-page");
    }

    private List<FormFieldMeta> buildFormFields(JSONObject formConfig, List<LcdpModulePageCompBean> compList,
                                                Map<String, FieldMeta> fieldMetaByLowerColumn,
                                                Map<String, Map<String, String>> pageI18nMap) {
        Map<String, LcdpModulePageCompBean> compMap = compList.stream().collect(Collectors.toMap(LcdpModulePageCompBean::getId, comp -> comp, (left, right) -> left));
        List<FormFieldMeta> fields = new ArrayList<>();
        appendFormFields(formConfig.getJSONArray("childrenWidgetId"), compMap, fieldMetaByLowerColumn, pageI18nMap, fields);
        return fields;
    }

    private void appendFormFields(JSONArray childIds, Map<String, LcdpModulePageCompBean> compMap,
                                  Map<String, FieldMeta> fieldMetaByLowerColumn,
                                  Map<String, Map<String, String>> pageI18nMap,
                                  List<FormFieldMeta> fields) {
        if (childIds == null) {
            return;
        }
        for (Object childIdObj : childIds) {
            LcdpModulePageCompBean child = compMap.get(ObjectUtils.toString(childIdObj));
            if (child == null) {
                continue;
            }
            JSONObject config = parseConfig(child);
            if (StringUtils.equals("FormPanel", child.getType())) {
                appendFormFields(config.getJSONArray("childrenWidgetId"), compMap, fieldMetaByLowerColumn, pageI18nMap, fields);
                continue;
            }
            String rawField = config.getString("field");
            if (StringUtils.isBlank(rawField)) {
                continue;
            }
            FormFieldMeta meta = new FormFieldMeta();
            meta.field = resolveFieldName(rawField, fieldMetaByLowerColumn);
            meta.title = defaultIfBlank(resolveTitle(config.get("title"), pageI18nMap), humanizeFieldTitle(rawField));
            meta.type = mapWidgetType(child.getType(), fieldMetaByLowerColumn.get(rawField.toLowerCase(Locale.ENGLISH)));
            meta.readonly = Boolean.TRUE.equals(config.getBoolean("readonly"));
            fields.add(meta);
        }
    }

    private List<String> parseInsertFields(String content, Map<String, FieldMeta> fieldMetaByLowerColumn) {
        if (StringUtils.isBlank(content)) {
            return new ArrayList<>();
        }
        Matcher matcher = INSERT_FIELDS_PATTERN.matcher(content);
        if (!matcher.find()) {
            return new ArrayList<>();
        }
        String fieldsContent = matcher.group(1);
        Matcher valueMatcher = QUOTED_VALUE_PATTERN.matcher(fieldsContent);
        List<String> fields = new ArrayList<>();
        while (valueMatcher.find()) {
            String rawValue = firstNotBlank(valueMatcher.group(1), valueMatcher.group(2));
            if (StringUtils.isNotBlank(rawValue)) {
                fields.add(resolveFieldName(rawValue, fieldMetaByLowerColumn));
            }
        }
        return fields;
    }

    private void writeFiles(ModuleContext context, List<GeneratedFile> generatedFiles, boolean overwrite) {
        for (GeneratedFile generatedFile : generatedFiles) {
            try {
                LOGGER.info("LCDP 转源码写入文件开始，moduleId={}, moduleCode={}, file={}",
                        context.moduleId, context.moduleCode, generatedFile.path);
                Files.createDirectories(generatedFile.path.getParent());
                if (Files.exists(generatedFile.path) && !overwrite) {
                    throw new ApplicationRuntimeException("目标文件已存在，请传 p.overwrite=true 覆盖：" + generatedFile.path);
                }
                Files.write(generatedFile.path, generatedFile.content.getBytes(StandardCharsets.UTF_8));
                LOGGER.info("LCDP 转源码写入文件完成，moduleId={}, moduleCode={}, file={}",
                        context.moduleId, context.moduleCode, generatedFile.path);
            } catch (IOException e) {
                throw new ApplicationRuntimeException(e, "写入文件失败：" + generatedFile.path);
            }
        }
    }

    private void cleanupLegacyControllerFiles(ModuleContext context, Path targetRoot) {
        Path javaRoot = targetRoot.resolve("src/main/java");
        Set<String> legacyControllerPackagePathSet = new LinkedHashSet<>(Arrays.asList(
                context.controllerPackage.replace('.', '/'),
                ("com.sunwayworld.cloud.module." + toPackageSegment(context.moduleCategory) + "." + toPackageSegment(context.moduleName) + ".controller").replace('.', '/'),
                ("com.sunwayworld.cloud.module.item.controller." + context.modulePath).replace('.', '/'),
                ("com.sunwayworld.cloud.module.item." + context.modulePath + ".controller").replace('.', '/')));
        String preserveCaseConvertedBasePackage = buildPreserveCaseConvertedBasePackage(context);
        if (StringUtils.isNotBlank(preserveCaseConvertedBasePackage)) {
            legacyControllerPackagePathSet.add((preserveCaseConvertedBasePackage + ".controller").replace('.', '/'));
        }
        List<Path> legacyFiles = new ArrayList<>();
        for (String legacyControllerPackagePath : legacyControllerPackagePathSet) {
            legacyFiles.add(javaRoot.resolve(legacyControllerPackagePath + "/" + context.className + "Controller.java"));
            legacyFiles.add(javaRoot.resolve(legacyControllerPackagePath + "/impl/" + context.className + "ControllerImpl.java"));
        }
        for (Path legacyFile : legacyFiles) {
            try {
                Files.deleteIfExists(legacyFile);
            } catch (IOException e) {
                throw new ApplicationRuntimeException(e, "删除旧版 Controller 文件失败：" + legacyFile);
            }
        }
    }

    private void cleanupLegacyBackendFiles(ModuleContext context, Path targetRoot) {
        Path javaRoot = targetRoot.resolve("src/main/java");
        Set<String> legacyBasePackageSet = new LinkedHashSet<>(Arrays.asList(
                context.basePackage,
                "com.sunwayworld.cloud.module." + toPackageSegment(context.moduleCategory) + "." + toPackageSegment(context.moduleName)));
        String preserveCaseConvertedBasePackage = buildPreserveCaseConvertedBasePackage(context);
        if (StringUtils.isNotBlank(preserveCaseConvertedBasePackage)) {
            legacyBasePackageSet.add(preserveCaseConvertedBasePackage);
        }
        String legacyModuleBasePackage = buildLegacyModuleBasePackage(context);
        if (StringUtils.isNotBlank(legacyModuleBasePackage)) {
            legacyBasePackageSet.add(legacyModuleBasePackage);
        }
        for (String legacyBasePackage : legacyBasePackageSet) {
            deleteIfExists(javaRoot.resolve(legacyBasePackage.replace('.', '/') + "/lcdp-page-route-mapping.txt"));
            deleteIfExists(javaRoot.resolve(legacyBasePackage.replace('.', '/') + "/lcdp-convert-report.txt"));
        }
        for (String legacyBasePackage : legacyBasePackageSet) {
            if (StringUtils.isBlank(legacyBasePackage)) {
                continue;
            }
            String javaPackagePath = legacyBasePackage.replace('.', '/');
            List<Path> legacyFiles = Arrays.asList(
                    javaRoot.resolve(javaPackagePath + "/bean/" + context.className + "Bean.java"),
                    javaRoot.resolve(javaPackagePath + "/persistent/dao/" + context.className + "Dao.java"),
                    javaRoot.resolve(javaPackagePath + "/persistent/dao/impl/" + context.className + "DaoImpl.java"),
                    javaRoot.resolve(javaPackagePath + "/persistent/mapper/" + context.className + "Mapper.java"),
                    javaRoot.resolve(javaPackagePath + "/service/" + context.className + "Service.java"),
                    javaRoot.resolve(javaPackagePath + "/service/impl/" + context.className + "ServiceImpl.java"),
                    javaRoot.resolve(javaPackagePath + "/resource/" + context.className + "Resource.java"),
                    javaRoot.resolve(javaPackagePath + "/resource/impl/" + context.className + "ResourceImpl.java"));
            for (Path legacyFile : legacyFiles) {
                deleteIfExists(legacyFile);
            }
            for (LcdpResourceHistoryBean history : context.mapperHistoryList) {
                deleteIfExists(javaRoot.resolve(javaPackagePath + "/mapper/" + stripMapperDialectSuffix(history.getResourceName()) + ".java"));
                deleteIfExists(javaRoot.resolve(javaPackagePath + "/mapper/conf/" + buildMapperFileName(history.getResourceName())));
                deleteIfExists(javaRoot.resolve(javaPackagePath + "/persistent/mapper/conf/" + buildMapperFileName(history.getResourceName())));
            }
            for (LcdpResourceHistoryBean history : context.javaHistoryList) {
                String newResourceName = buildLcdpResourceName(history.getResourceName());
                deleteIfExists(javaRoot.resolve(javaPackagePath + "/server/" + history.getResourceName() + ".java"));
                deleteIfExists(javaRoot.resolve(javaPackagePath + "/service/lcdp/" + history.getResourceName() + ".java"));
                deleteIfExists(javaRoot.resolve(javaPackagePath + "/resource/lcdp/" + history.getResourceName() + "Resource.java"));
                deleteIfExists(javaRoot.resolve(javaPackagePath + "/resource/lcdp/impl/" + history.getResourceName() + "ResourceImpl.java"));
                deleteIfExists(javaRoot.resolve(javaPackagePath + "/resource/" + history.getResourceName() + "Resource.java"));
                deleteIfExists(javaRoot.resolve(javaPackagePath + "/resource/impl/" + history.getResourceName() + "ResourceImpl.java"));
                deleteIfExists(javaRoot.resolve(javaPackagePath + "/resource/" + newResourceName + ".java"));
                deleteIfExists(javaRoot.resolve(javaPackagePath + "/resource/impl/" + newResourceName + "Impl.java"));
            }
        }
    }

    private void cleanupLegacyFrontendFiles(ModuleContext context, Path targetRoot) {
        List<Path> staticDirList = Arrays.asList(
                targetRoot.resolve("src/main/resources/static/module/item").resolve(context.modulePath),
                targetRoot.resolve("src/main/resources/static").resolve(context.moduleResourcePath));
        for (Path staticDir : staticDirList) {
            deleteIfExists(staticDir.resolve(context.modulePath + ".js"));
            deleteIfExists(staticDir.resolve(context.modulePath + "-lcdp-runtime.js"));
            deleteChildrenBySuffix(staticDir, ".meta.js");
            deleteChildrenBySuffix(staticDir, ".script.js");
        }
    }

    private void deleteChildrenBySuffix(Path dir, String suffix) {
        if (!Files.isDirectory(dir)) {
            return;
        }
        try {
            Files.list(dir)
                    .filter(path -> path.getFileName().toString().endsWith(suffix))
                    .forEach(this::deleteIfExists);
        } catch (IOException e) {
            throw new ApplicationRuntimeException(e, "清理历史前端文件失败：" + dir);
        }
    }

    private void deleteIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new ApplicationRuntimeException(e, "删除历史文件失败：" + path);
        }
    }

    private String renderBean(ModuleContext context) {
        Set<String> imports = new LinkedHashSet<>();
        imports.add("com.sunwayworld.framework.data.annotation.Id");
        imports.add("com.sunwayworld.framework.data.annotation.Table");
        imports.add("com.sunwayworld.framework.data.annotation.Transient");
        switch (context.beanBaseType) {
            case AUDITABLE:
                imports.add("com.sunwayworld.framework.support.domain.AbstractAuditable");
                imports.add("com.sunwayworld.framework.support.domain.Auditable");
                break;
            case INSERTABLE:
                imports.add("com.sunwayworld.framework.support.domain.AbstractInsertable");
                imports.add("com.sunwayworld.framework.support.domain.Insertable");
                break;
            default:
                imports.add("com.sunwayworld.framework.support.domain.AbstractPersistable");
                imports.add("com.sunwayworld.framework.support.domain.Persistable");
                break;
        }

        List<FieldMeta> explicitFields = context.fieldMetaList.stream()
                .filter(field -> !isInheritedField(context.beanBaseType, field))
                .collect(Collectors.toList());
        for (FieldMeta field : explicitFields) {
            collectFieldImports(field, imports);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(context.basePackage).append(".bean;\n\n");
        appendImports(builder, imports);
        builder.append("\n");
        builder.append("@Table(\"").append(context.tableName).append("\")\n");
        builder.append("public class ").append(context.className).append("Bean extends ")
                .append(context.beanBaseType.parentSimpleName).append("<").append(getSimpleTypeName(context.idField.type)).append("> implements ")
                .append(context.beanBaseType.interfaceSimpleName).append("<").append(getSimpleTypeName(context.idField.type)).append("> {\n\n");
        builder.append("    @Transient\n");
        builder.append("    private static final long serialVersionUID = 1L;\n\n");
        for (FieldMeta field : explicitFields) {
            appendField(builder, field);
        }
        for (FieldMeta field : explicitFields) {
            appendGetterSetter(builder, field);
        }
        builder.append("}\n");
        return builder.toString();
    }

    private String renderDaoInterface(ModuleContext context) {
        return "package " + context.basePackage + ".persistent.dao;\n\n"
                + "import " + context.basePackage + ".bean." + context.className + "Bean;\n"
                + "import com.sunwayworld.framework.support.base.dao.GenericDao;\n\n"
                + "public interface " + context.className + "Dao extends GenericDao<" + context.className + "Bean, " + getSimpleTypeName(context.idField.type) + "> {\n"
                + "}\n";
    }

    private String renderDaoImpl(ModuleContext context) {
        return "package " + context.basePackage + ".persistent.dao.impl;\n\n"
                + "import org.springframework.beans.factory.annotation.Autowired;\n"
                + "import org.springframework.stereotype.Repository;\n\n"
                + "import " + context.basePackage + ".bean." + context.className + "Bean;\n"
                + "import " + context.basePackage + ".persistent.dao." + context.className + "Dao;\n"
                + "import " + context.basePackage + ".persistent.mapper." + context.className + "Mapper;\n"
                + "import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;\n"
                + "import com.sunwayworld.framework.spring.annotation.GikamBean;\n\n"
                + "@Repository\n"
                + "@GikamBean\n"
                + "public class " + context.className + "DaoImpl extends MybatisDaoSupport<" + context.className + "Bean, " + getSimpleTypeName(context.idField.type) + "> implements " + context.className + "Dao {\n\n"
                + "    @Autowired\n"
                + "    private " + context.className + "Mapper " + context.lowerCamelName + "Mapper;\n\n"
                + "    @Override\n"
                + "    public " + context.className + "Mapper getMapper() {\n"
                + "        return " + context.lowerCamelName + "Mapper;\n"
                + "    }\n"
                + "}\n";
    }

    private String renderMapperInterface(GeneratedMapperSpec spec, ModuleContext context) {
        return "package " + spec.packageName + ";\n\n"
                + "import com.sunwayworld.framework.spring.annotation.GikamBean;\n"
                + "import com.sunwayworld.framework.support.base.mapper.GenericMapper;\n\n"
                + "@GikamBean\n"
                + "public interface " + spec.mapperName + " extends GenericMapper<" + getSimpleTypeName(context.idField.type) + "> {\n"
                + "}\n";
    }

    private String renderServiceInterface(ModuleContext context) {
        String idTypeName = getSimpleTypeName(context.idField.type);
        return "package " + context.basePackage + ".service;\n\n"
                + "import java.util.List;\n"
                + "import java.util.Map;\n"
                + "import com.sunwayworld.framework.data.page.Page;\n"
                + "import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;\n"
                + "import com.sunwayworld.framework.restful.data.RestValidationResultBean;\n\n"
                + "public interface " + context.className + "Service {\n"
                + "\n"
                + "    Map<String, Object> getLcdpServiceMap();\n"
                + "\n"
                + "    List<Map<String, Object>> getLcdpMethodList();\n"
                + "\n"
                + "    default Object getLcdpService(String servicePath) {\n"
                + "        return getLcdpServiceMap().get(servicePath);\n"
                + "    }\n"
                + "\n"
                + "    default Map<String, String> getLcdpServiceTypeMap() {\n"
                + "        Map<String, String> serviceTypeMap = new java.util.LinkedHashMap<>();\n"
                + "        getLcdpServiceMap().forEach((servicePath, serviceBean) -> serviceTypeMap.put(servicePath, serviceBean == null ? null : serviceBean.getClass().getName()));\n"
                + "        return serviceTypeMap;\n"
                + "    }\n"
                + "\n"
                + "    Page<Map<String, Object>> selectPagination(RestJsonWrapperBean wrapper);\n"
                + "\n"
                + "    Page<Map<String, Object>> selectRawPagination(RestJsonWrapperBean wrapper);\n"
                + "\n"
                + "    Map<String, Object> selectDetail(" + idTypeName + " id);\n"
                + "\n"
                + "    String selectColumnById(" + idTypeName + " id, String column);\n"
                + "\n"
                + "    void delete(RestJsonWrapperBean wrapper);\n"
                + "\n"
                + "    " + idTypeName + " insert(RestJsonWrapperBean wrapper);\n"
                + "\n"
                + "    void instantSave(RestJsonWrapperBean wrapper);\n"
                + "\n"
                + "    void save(" + idTypeName + " id, RestJsonWrapperBean wrapper);\n"
                + "\n"
                + "    Page<Map<String, Object>> selectSearchablePagination(RestJsonWrapperBean wrapper);\n"
                + "\n"
                + "    RestValidationResultBean validateUnique(" + idTypeName + " id, String columnName, String columnValue);\n"
                + "\n"
                + "    RestValidationResultBean validateUnique(RestJsonWrapperBean wrapper);\n"
                + "\n"
                + "    Object invokeLcdpMethod(RestJsonWrapperBean wrapper);\n"
                + "}\n";
    }

    private String renderServiceImpl(ModuleContext context) {
        LcdpResourceHistoryBean primaryHistory = context.primaryJavaHistory != null
                ? context.primaryJavaHistory
                : (CollectionUtils.isEmpty(context.javaHistoryList) ? null : context.javaHistoryList.get(0));
        Set<String> imports = new LinkedHashSet<>();
        imports.add("javax.annotation.PostConstruct");
        imports.add("java.lang.reflect.Method");
        imports.add("java.util.ArrayList");
        imports.add("java.util.Arrays");
        imports.add("java.util.Collections");
        imports.add("java.util.LinkedHashMap");
        imports.add("java.util.List");
        imports.add("java.util.Map");
        imports.add("org.springframework.beans.factory.annotation.Autowired");
        imports.add("org.springframework.stereotype.Repository");
        imports.add("org.springframework.transaction.annotation.Transactional");
        imports.add("com.sunwayworld.cloud.module.lcdp.base.service.LcdpBaseService");
        imports.add(context.basePackage + ".service." + context.className + "Service");
        imports.add("com.sunwayworld.framework.at.annotation.AuditTrailEntry");
        imports.add("com.sunwayworld.framework.at.annotation.AuditTrailType");
        imports.add("com.sunwayworld.framework.audit.aunnotation.Audit");
        imports.add("com.sunwayworld.framework.audit.constant.AuditConstant");
        imports.add("com.sunwayworld.framework.constant.Constant");
        imports.add("com.sunwayworld.framework.data.page.Page");
        imports.add("com.sunwayworld.framework.exception.core.ApplicationRuntimeException");
        imports.add("com.sunwayworld.framework.mybatis.mapper.MapperParameter");
        imports.add("com.sunwayworld.framework.mybatis.page.PageRowBounds");
        imports.add("com.sunwayworld.framework.restful.data.RestValidationResultBean");
        imports.add("com.sunwayworld.framework.restful.data.RestJsonWrapperBean");
        imports.add("com.sunwayworld.framework.spring.annotation.GikamBean");
        imports.add("com.sunwayworld.framework.utils.CollectionUtils");
        imports.add("com.sunwayworld.framework.utils.ClassUtils");
        imports.add("com.sunwayworld.framework.utils.ConvertUtils");
        imports.add("com.sunwayworld.framework.utils.ReflectionUtils");
        imports.add("com.sunwayworld.framework.utils.StringUtils");
        imports.add("com.sunwayworld.cloud.module.lcdp.resource.support.LcdpScriptUtils");
        imports.add("com.sunwayworld.cloud.module.lcdp.resource.support.LcdpWrapperParseUtils");
        List<LcdpResourceHistoryBean> javaHistoryList = new ArrayList<>(context.javaHistoryList);
        javaHistoryList.sort(Comparator.comparing(LcdpResourceHistoryBean::getResourceName));
        for (LcdpResourceHistoryBean history : javaHistoryList) {
            imports.add(context.basePackage + ".service.lcdp." + history.getResourceName());
        }

        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(context.basePackage).append(".service.impl;\n\n");
        appendImports(builder, imports);
        builder.append("\n");
        builder.append("@GikamBean\n");
        builder.append("@Repository\n");
        builder.append("public class ").append(context.className).append("ServiceImpl implements ").append(context.className).append("Service {\n\n");
        for (LcdpResourceHistoryBean history : javaHistoryList) {
            builder.append("    @Autowired\n");
            builder.append("    private ").append(history.getResourceName()).append(" ")
                    .append(buildLcdpServiceFieldName(history.getResourceName())).append(";\n\n");
        }
        builder.append("    private final Map<String, Object> lcdpServiceMap = new LinkedHashMap<>();\n\n");
        builder.append("    private final List<Map<String, Object>> lcdpMethodList = new ArrayList<>();\n\n");
        builder.append("    @PostConstruct\n");
        builder.append("    public void initLcdpServiceMap() {\n");
        builder.append("        lcdpServiceMap.clear();\n");
        builder.append("        lcdpMethodList.clear();\n");
        for (LcdpResourceHistoryBean history : javaHistoryList) {
            builder.append("        lcdpServiceMap.put(\"").append(escapeJava(history.getPath())).append("\", ")
                    .append(buildLcdpServiceFieldName(history.getResourceName())).append(");\n");
        }
        appendLcdpMethodInit(builder, context.lcdpMethodMetaList);
        builder.append("    }\n\n");
        builder.append("    @Override\n");
        builder.append("    public Map<String, Object> getLcdpServiceMap() {\n");
        builder.append("        return Collections.unmodifiableMap(lcdpServiceMap);\n");
        builder.append("    }\n\n");
        builder.append("    @Override\n");
        builder.append("    public List<Map<String, Object>> getLcdpMethodList() {\n");
        builder.append("        return Collections.unmodifiableList(lcdpMethodList);\n");
        builder.append("    }\n\n");
        builder.append("    private LcdpBaseService getPrimaryLcdpService() {\n");
        if (primaryHistory != null) {
            builder.append("        return ").append(buildLcdpServiceFieldName(primaryHistory.getResourceName())).append(";\n");
        } else {
            builder.append("        Object serviceBean = lcdpServiceMap.values().stream().findFirst().orElse(null);\n");
            builder.append("        if (!(serviceBean instanceof LcdpBaseService)) {\n");
            builder.append("            throw new ApplicationRuntimeException(\"未找到主低代码服务\");\n");
            builder.append("        }\n");
            builder.append("        return (LcdpBaseService) serviceBean;\n");
        }
        builder.append("    }\n\n");
        builder.append("    private String getPrimaryServicePath() {\n");
        if (StringUtils.isNotBlank(context.primaryServicePath)) {
            builder.append("        return \"").append(escapeJava(context.primaryServicePath)).append("\";\n");
        } else if (primaryHistory != null && StringUtils.isNotBlank(primaryHistory.getPath())) {
            builder.append("        return \"").append(escapeJava(primaryHistory.getPath())).append("\";\n");
        } else {
            builder.append("        return null;\n");
        }
        builder.append("    }\n\n");
        builder.append("    @Override\n");
        builder.append("    public Page<Map<String, Object>> selectPagination(RestJsonWrapperBean wrapper) {\n");
        builder.append("        return getPrimaryLcdpService().selectLcdpPagination(wrapper);\n");
        builder.append("    }\n\n");
        builder.append("    @Override\n");
        builder.append("    @SuppressWarnings(\"unchecked\")\n");
        builder.append("    public Page<Map<String, Object>> selectRawPagination(RestJsonWrapperBean wrapper) {\n");
        builder.append("        Object result = invokeNamedLcdpMethod(wrapper, \"selectRawPagination\");\n");
        builder.append("        if (result instanceof Page) {\n");
        builder.append("            return (Page<Map<String, Object>>) result;\n");
        builder.append("        }\n");
        builder.append("        MapperParameter parameter = wrapper.extractMapFilter();\n");
        builder.append("        PageRowBounds rowBounds = wrapper.extractPageRowBounds();\n");
        builder.append("        parameter.setRawQueries();\n");
        builder.append("        if (!Constant.NO.equals(wrapper.getParamValue(\"orgAuthority\"))) {\n");
        builder.append("            parameter.setOrgAuthority();\n");
        builder.append("        }\n");
        builder.append("        if (!wrapper.getAuthorityList().isEmpty()) {\n");
        builder.append("            wrapper.getAuthorityList().forEach(parameter::setAuthorityParameter);\n");
        builder.append("        }\n");
        builder.append("        parameter.setTableName(getPrimaryLcdpService().getTable());\n");
        builder.append("        return getPrimaryLcdpService().selectLcdpPagination(parameter, rowBounds);\n");
        builder.append("    }\n\n");
        builder.append("    @Override\n");
        builder.append("    public Map<String, Object> selectDetail(").append(getSimpleTypeName(context.idField.type)).append(" id) {\n");
        builder.append("        return getPrimaryLcdpService().selectById(id);\n");
        builder.append("    }\n\n");
        builder.append("    @Override\n");
        builder.append("    public String selectColumnById(").append(getSimpleTypeName(context.idField.type)).append(" id, String column) {\n");
        builder.append("        Map<String, Object> detail = selectDetail(id);\n");
        builder.append("        return detail == null ? null : ConvertUtils.convert(CollectionUtils.getValueIgnorecase(detail, column), String.class);\n");
        builder.append("    }\n\n");
        builder.append("    @Override\n");
        builder.append("    @Transactional\n");
        builder.append("    @AuditTrailEntry(AuditTrailType.DELETE)\n");
        builder.append("    @Audit(AuditConstant.DELETE)\n");
        builder.append("    public void delete(RestJsonWrapperBean wrapper) {\n");
        builder.append("        Object result = invokeMappedLcdpMethod(\"DELETE\", wrapper, \"deleteData\");\n");
        builder.append("        if (result == null) {\n");
        builder.append("            getPrimaryLcdpService().lcdpDelete(wrapper);\n");
        builder.append("        }\n");
        builder.append("    }\n\n");
        builder.append("    @Override\n");
        builder.append("    @Transactional\n");
        builder.append("    @AuditTrailEntry(AuditTrailType.INSERT)\n");
        builder.append("    @Audit(AuditConstant.INSERT)\n");
        builder.append("    public ").append(getSimpleTypeName(context.idField.type)).append(" insert(RestJsonWrapperBean wrapper) {\n");
        builder.append("        Object result = invokeMappedLcdpMethod(\"INSERT\", wrapper, \"insertData\");\n");
        builder.append("        if (result == null) {\n");
        builder.append("            result = getPrimaryLcdpService().lcdpInsert(LcdpWrapperParseUtils.parseMap(wrapper));\n");
        builder.append("        }\n");
        builder.append("        return ConvertUtils.convert(result, ").append(getSimpleTypeName(context.idField.type)).append(".class);\n");
        builder.append("    }\n\n");
        builder.append("    @Override\n");
        builder.append("    @Transactional\n");
        builder.append("    @AuditTrailEntry(AuditTrailType.SAVE)\n");
        builder.append("    @Audit(AuditConstant.INSTANT_SAVE)\n");
        builder.append("    public void instantSave(RestJsonWrapperBean wrapper) {\n");
        builder.append("        Object result = invokeMappedLcdpMethod(\"UPDATE\", wrapper, \"instantSave\", \"updateData\", \"saveData\");\n");
        builder.append("        if (result == null) {\n");
        builder.append("            getPrimaryLcdpService().lcdpUpdate(wrapper);\n");
        builder.append("        }\n");
        builder.append("    }\n\n");
        builder.append("    @Override\n");
        builder.append("    @Transactional\n");
        builder.append("    @AuditTrailEntry(AuditTrailType.SAVE)\n");
        builder.append("    @Audit(AuditConstant.SAVE)\n");
        builder.append("    public void save(").append(getSimpleTypeName(context.idField.type)).append(" id, RestJsonWrapperBean wrapper) {\n");
        builder.append("        Object result = invokeMappedLcdpMethod(\"UPDATE\", wrapper, \"save\", \"updateData\", \"saveData\");\n");
        builder.append("        if (result == null) {\n");
        builder.append("            getPrimaryLcdpService().lcdpUpdate(wrapper);\n");
        builder.append("        }\n");
        builder.append("    }\n\n");
        builder.append("    @Override\n");
        builder.append("    @SuppressWarnings(\"unchecked\")\n");
        builder.append("    public Page<Map<String, Object>> selectSearchablePagination(RestJsonWrapperBean wrapper) {\n");
        builder.append("        Object result = invokeNamedLcdpMethod(wrapper, \"selectSearchablePagination\");\n");
        builder.append("        if (result instanceof Page) {\n");
        builder.append("            return (Page<Map<String, Object>>) result;\n");
        builder.append("        }\n");
        builder.append("        MapperParameter parameter = wrapper.extractMapFilter();\n");
        builder.append("        PageRowBounds rowBounds = wrapper.extractPageRowBounds();\n");
        builder.append("        if (!Constant.NO.equals(wrapper.getParamValue(\"orgAuthority\"))) {\n");
        builder.append("            parameter.setOrgAuthority();\n");
        builder.append("        }\n");
        builder.append("        if (!wrapper.getAuthorityList().isEmpty()) {\n");
        builder.append("            wrapper.getAuthorityList().forEach(parameter::setAuthorityParameter);\n");
        builder.append("        }\n");
        builder.append("        parameter.setTableName(getPrimaryLcdpService().getTable());\n");
        builder.append("        return getPrimaryLcdpService().selectLcdpPagination(parameter, rowBounds);\n");
        builder.append("    }\n\n");
        builder.append("    @Override\n");
        builder.append("    public RestValidationResultBean validateUnique(").append(getSimpleTypeName(context.idField.type)).append(" id, String columnName, String columnValue) {\n");
        builder.append("        return getPrimaryLcdpService().validateUnique(id, columnName, columnValue);\n");
        builder.append("    }\n\n");
        builder.append("    @Override\n");
        builder.append("    public RestValidationResultBean validateUnique(RestJsonWrapperBean wrapper) {\n");
        builder.append("        String id = wrapper.getParamValue(\"id\");\n");
        builder.append("        if (StringUtils.isBlank(id)) {\n");
        builder.append("            id = wrapper.getFilterValue(\"id\");\n");
        builder.append("        }\n");
        builder.append("        String columnName = wrapper.getParamValue(\"columnName\");\n");
        builder.append("        if (StringUtils.isBlank(columnName)) {\n");
        builder.append("            columnName = wrapper.getFilterValue(\"columnName\");\n");
        builder.append("        }\n");
        builder.append("        String columnValue = wrapper.getParamValue(\"columnValue\");\n");
        builder.append("        if (StringUtils.isBlank(columnValue)) {\n");
        builder.append("            columnValue = wrapper.getFilterValue(\"columnValue\");\n");
        builder.append("        }\n");
        builder.append("        return validateUnique(ConvertUtils.convert(id, ").append(getSimpleTypeName(context.idField.type)).append(".class), columnName, columnValue);\n");
        builder.append("    }\n\n");
        builder.append("    @Override\n");
        builder.append("    public Object invokeLcdpMethod(RestJsonWrapperBean wrapper) {\n");
        builder.append("        String servicePath = ConvertUtils.convert(wrapper.getParamMap().get(\"servicePath\"), String.class);\n");
        builder.append("        String methodName = ConvertUtils.convert(wrapper.getParamMap().get(\"methodName\"), String.class);\n");
        builder.append("        methodName = normalizeLcdpMethodName(methodName);\n");
        builder.append("        Object serviceBean = resolveLcdpService(servicePath);\n");
        builder.append("        if (serviceBean == null) {\n");
        builder.append("            throw new ApplicationRuntimeException(\"未找到低代码服务：\" + servicePath);\n");
        builder.append("        }\n");
        builder.append("        if (methodName == null || methodName.trim().isEmpty()) {\n");
        builder.append("            throw new ApplicationRuntimeException(\"未指定低代码方法名\");\n");
        builder.append("        }\n");
        builder.append("        return invokeLcdpServiceMethod(serviceBean, methodName, wrapper);\n");
        builder.append("    }\n");
        appendLcdpInvokeHelpers(builder);
        builder.append("\n");
        builder.append("    private Object resolveLcdpService(String servicePath) {\n");
        builder.append("        Object serviceBean = lcdpServiceMap.get(servicePath);\n");
        builder.append("        if (serviceBean != null || StringUtils.isBlank(servicePath)) {\n");
        builder.append("            return serviceBean;\n");
        builder.append("        }\n");
        builder.append("        try {\n");
        builder.append("            return LcdpScriptUtils.getBean(servicePath);\n");
        builder.append("        } catch (Exception e) {\n");
        builder.append("            return null;\n");
        builder.append("        }\n");
        builder.append("    }\n\n");
        builder.append("    private String normalizeLcdpMethodName(String methodName) {\n");
        builder.append("        if (StringUtils.isBlank(methodName)) {\n");
        builder.append("            return methodName;\n");
        builder.append("        }\n");
        builder.append("        while (methodName.startsWith(\".\")) {\n");
        builder.append("            methodName = methodName.substring(1);\n");
        builder.append("        }\n");
        builder.append("        return methodName;\n");
        builder.append("    }\n\n");
        builder.append("    private Object invokeMappedLcdpMethod(String mappingType, RestJsonWrapperBean wrapper, String... fallbackMethodNames) {\n");
        builder.append("        Object primaryResult = invokeMappedLcdpMethod(getPrimaryServicePath(), mappingType, wrapper);\n");
        builder.append("        if (primaryResult != null) {\n");
        builder.append("            return primaryResult;\n");
        builder.append("        }\n");
        builder.append("        for (Map<String, Object> methodMeta : lcdpMethodList) {\n");
        builder.append("            String servicePath = ConvertUtils.convert(methodMeta.get(\"servicePath\"), String.class);\n");
        builder.append("            if (StringUtils.equals(servicePath, getPrimaryServicePath())) {\n");
        builder.append("                continue;\n");
        builder.append("            }\n");
        builder.append("            Object result = invokeMappedLcdpMethod(servicePath, mappingType, wrapper);\n");
        builder.append("            if (result != null) {\n");
        builder.append("                return result;\n");
        builder.append("            }\n");
        builder.append("        }\n");
        builder.append("        if (fallbackMethodNames != null) {\n");
        builder.append("            Object primaryService = getPrimaryLcdpService();\n");
        builder.append("            for (String fallbackMethodName : fallbackMethodNames) {\n");
        builder.append("                if (StringUtils.isBlank(fallbackMethodName) || !hasMethod(primaryService, fallbackMethodName)) {\n");
        builder.append("                    continue;\n");
        builder.append("                }\n");
        builder.append("                return invokeLcdpServiceMethod(primaryService, fallbackMethodName, wrapper);\n");
        builder.append("            }\n");
        builder.append("        }\n");
        builder.append("        return null;\n");
        builder.append("    }\n\n");
        builder.append("    private Object invokeNamedLcdpMethod(RestJsonWrapperBean wrapper, String... methodNames) {\n");
        builder.append("        if (methodNames == null || methodNames.length == 0) {\n");
        builder.append("            return null;\n");
        builder.append("        }\n");
        builder.append("        Object primaryService = getPrimaryLcdpService();\n");
        builder.append("        for (String methodName : methodNames) {\n");
        builder.append("            if (StringUtils.isBlank(methodName) || !hasMethod(primaryService, methodName)) {\n");
        builder.append("                continue;\n");
        builder.append("            }\n");
        builder.append("            return invokeLcdpServiceMethod(primaryService, methodName, wrapper);\n");
        builder.append("        }\n");
        builder.append("        for (Object serviceBean : lcdpServiceMap.values()) {\n");
        builder.append("            if (serviceBean == primaryService) {\n");
        builder.append("                continue;\n");
        builder.append("            }\n");
        builder.append("            for (String methodName : methodNames) {\n");
        builder.append("                if (StringUtils.isBlank(methodName) || !hasMethod(serviceBean, methodName)) {\n");
        builder.append("                    continue;\n");
        builder.append("                }\n");
        builder.append("                return invokeLcdpServiceMethod(serviceBean, methodName, wrapper);\n");
        builder.append("            }\n");
        builder.append("        }\n");
        builder.append("        return null;\n");
        builder.append("    }\n\n");
        builder.append("    private Object invokeMappedLcdpMethod(String servicePath, String mappingType, RestJsonWrapperBean wrapper) {\n");
        builder.append("        if (StringUtils.isBlank(servicePath) || StringUtils.isBlank(mappingType)) {\n");
        builder.append("            return null;\n");
        builder.append("        }\n");
        builder.append("        for (Map<String, Object> methodMeta : lcdpMethodList) {\n");
        builder.append("            String metaServicePath = ConvertUtils.convert(methodMeta.get(\"servicePath\"), String.class);\n");
        builder.append("            String metaMappingType = ConvertUtils.convert(methodMeta.get(\"mappingType\"), String.class);\n");
        builder.append("            if (!StringUtils.equals(metaServicePath, servicePath) || !StringUtils.equalsIgnoreCase(metaMappingType, mappingType)) {\n");
        builder.append("                continue;\n");
        builder.append("            }\n");
        builder.append("            Object serviceBean = lcdpServiceMap.get(metaServicePath);\n");
        builder.append("            String methodName = ConvertUtils.convert(methodMeta.get(\"methodName\"), String.class);\n");
        builder.append("            if (serviceBean != null && StringUtils.isNotBlank(methodName)) {\n");
        builder.append("                return invokeLcdpServiceMethod(serviceBean, methodName, wrapper);\n");
        builder.append("            }\n");
        builder.append("        }\n");
        builder.append("        return null;\n");
        builder.append("    }\n\n");
        builder.append("    private boolean hasMethod(Object serviceBean, String methodName) {\n");
        builder.append("        if (serviceBean == null || StringUtils.isBlank(methodName)) {\n");
        builder.append("            return false;\n");
        builder.append("        }\n");
        builder.append("        Class<?> targetClass = ClassUtils.getRawType(serviceBean.getClass());\n");
        builder.append("        for (Method method : targetClass.getMethods()) {\n");
        builder.append("            if (StringUtils.equals(method.getName(), methodName) && method.getDeclaringClass() != Object.class) {\n");
        builder.append("                return true;\n");
        builder.append("            }\n");
        builder.append("        }\n");
        builder.append("        return false;\n");
        builder.append("    }\n");
        builder.append("}\n");
        return builder.toString();
    }

    private String renderResourceInterface(ModuleContext context) {
        String idTypeName = getSimpleTypeName(context.idField.type);
        return "package " + context.basePackage + ".resource;\n\n"
                + "import java.util.List;\n"
                + "import java.util.Map;\n\n"
                + "import org.springframework.web.bind.annotation.PathVariable;\n"
                + "import org.springframework.web.bind.annotation.RequestMapping;\n"
                + "import org.springframework.web.bind.annotation.RequestMethod;\n\n"
                + "import org.springframework.web.bind.annotation.RequestParam;\n\n"
                + "import " + context.basePackage + ".service." + context.className + "Service;\n"
                + "import com.sunwayworld.framework.data.page.Page;\n"
                + "import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;\n"
                + "import com.sunwayworld.framework.restful.data.RestValidationResultBean;\n\n"
                + "@RequestMapping(\"" + context.requestPath + "\")\n"
                + "public interface " + context.className + "Resource {\n"
                + "\n"
                + "    " + context.className + "Service getService();\n"
                + "\n"
                + "    @RequestMapping(value = \"/queries\", method = RequestMethod.POST)\n"
                + "    Page<Map<String, Object>> selectPagination(RestJsonWrapperBean wrapper);\n"
                + "\n"
                + "    @RequestMapping(value = \"/queries/raw\", method = RequestMethod.POST)\n"
                + "    Page<Map<String, Object>> selectRawPagination(RestJsonWrapperBean wrapper);\n"
                + "\n"
                + "    @RequestMapping(value = \"/{id}\", method = RequestMethod.GET)\n"
                + "    Map<String, Object> selectDetail(@PathVariable " + idTypeName + " id);\n"
                + "\n"
                + "    @RequestMapping(value = \"/{id}/columns/{column}\", method = RequestMethod.GET)\n"
                + "    String selectColumnById(@PathVariable " + idTypeName + " id, @PathVariable String column);\n"
                + "\n"
                + "    @RequestMapping(method = RequestMethod.DELETE)\n"
                + "    void delete(RestJsonWrapperBean wrapper);\n"
                + "\n"
                + "    @RequestMapping(method = RequestMethod.POST)\n"
                + "    " + idTypeName + " insert(RestJsonWrapperBean wrapper);\n"
                + "\n"
                + "    @RequestMapping(value = \"/instant\", method = RequestMethod.PUT)\n"
                + "    void instantSave(RestJsonWrapperBean wrapper);\n"
                + "\n"
                + "    @RequestMapping(value = \"/{id}\", method = RequestMethod.PUT)\n"
                + "    void save(@PathVariable " + idTypeName + " id, RestJsonWrapperBean wrapper);\n"
                + "\n"
                + "    @RequestMapping(value = \"/queries/searchable\", method = RequestMethod.POST)\n"
                + "    Page<Map<String, Object>> selectSearchablePagination(RestJsonWrapperBean wrapper);\n"
                + "\n"
                + "    @RequestMapping(value = { \"/{id}/action/validate-unique\", \"/{id}/action/validate-unique/{columnName}/{columnValue}\", \"/action/validate-unique/{columnName}/{columnValue}\" }, method = RequestMethod.POST)\n"
                + "    RestValidationResultBean validateUnique(@PathVariable(required = false) " + idTypeName + " id, @PathVariable(required = false) String columnName, @PathVariable(required = false) String columnValue);\n"
                + "\n"
                + "    @RequestMapping(value = \"/action/validate-unique\", method = RequestMethod.POST)\n"
                + "    RestValidationResultBean validateUnique(RestJsonWrapperBean wrapper);\n"
                + "\n"
                + "    @RequestMapping(\"/action/lcdp-services\")\n"
                + "    Map<String, String> selectLcdpServices();\n"
                + "\n"
                + "    @RequestMapping(\"/action/lcdp-methods\")\n"
                + "    List<Map<String, Object>> selectLcdpMethods();\n"
                + "\n"
                + "    @RequestMapping(\"/action/lcdp-invoke\")\n"
                + "    Object invokeLcdpMethod(RestJsonWrapperBean wrapper);\n"
                + "\n"
                + "    @RequestMapping(value = \"/action/lcdp-call\", method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE })\n"
                + "    Object callLcdpMethod(@RequestParam String servicePath, @RequestParam String methodName, RestJsonWrapperBean wrapper);\n"
                + "}\n";
    }

    private String renderResourceImpl(ModuleContext context) {
        String idTypeName = getSimpleTypeName(context.idField.type);
        return "package " + context.basePackage + ".resource.impl;\n\n"
                + "import java.util.List;\n"
                + "import java.util.Map;\n\n"
                + "import org.springframework.beans.factory.annotation.Autowired;\n"
                + "import org.springframework.web.bind.annotation.PathVariable;\n"
                + "import org.springframework.web.bind.annotation.RequestMapping;\n"
                + "import org.springframework.web.bind.annotation.RequestMethod;\n"
                + "import org.springframework.web.bind.annotation.RequestParam;\n"
                + "import org.springframework.web.bind.annotation.RestController;\n\n"
                + "import " + context.basePackage + ".resource." + context.className + "Resource;\n"
                + "import " + context.basePackage + ".service." + context.className + "Service;\n"
                + "import com.sunwayworld.framework.data.page.Page;\n"
                + "import com.sunwayworld.framework.log.annotation.Log;\n"
                + "import com.sunwayworld.framework.log.annotation.LogModule;\n"
                + "import com.sunwayworld.framework.log.annotation.LogType;\n"
                + "import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;\n"
                + "import com.sunwayworld.framework.restful.data.RestValidationResultBean;\n"
                + "import com.sunwayworld.framework.spring.annotation.GikamBean;\n\n"
                + "@GikamBean\n"
                + "@RestController\n"
                + "@LogModule(value = \"" + context.moduleCode + "自动转换模块\")\n"
                + "public class " + context.className + "ResourceImpl implements " + context.className + "Resource {\n\n"
                + "    @Autowired\n"
                + "    private " + context.className + "Service " + context.lowerCamelName + "Service;\n\n"
                + "    @Override\n"
                + "    public " + context.className + "Service getService() {\n"
                + "        return " + context.lowerCamelName + "Service;\n"
                + "    }\n\n"
                + "    @Override\n"
                + "    @Log(value = \"通用查询\", type = LogType.SELECT)\n"
                + "    @RequestMapping(value = \"/queries\", method = RequestMethod.POST)\n"
                + "    public Page<Map<String, Object>> selectPagination(RestJsonWrapperBean wrapper) {\n"
                + "        return getService().selectPagination(wrapper);\n"
                + "    }\n\n"
                + "    @Override\n"
                + "    @Log(value = \"编辑页查询\", type = LogType.SELECT)\n"
                + "    @RequestMapping(value = \"/queries/raw\", method = RequestMethod.POST)\n"
                + "    public Page<Map<String, Object>> selectRawPagination(RestJsonWrapperBean wrapper) {\n"
                + "        return getService().selectRawPagination(wrapper);\n"
                + "    }\n\n"
                + "    @Override\n"
                + "    @Log(value = \"详细信息查询\", type = LogType.SELECT)\n"
                + "    @RequestMapping(value = \"/{id}\", method = RequestMethod.GET)\n"
                + "    public Map<String, Object> selectDetail(@PathVariable " + idTypeName + " id) {\n"
                + "        return getService().selectDetail(id);\n"
                + "    }\n\n"
                + "    @Override\n"
                + "    @Log(value = \"单个字段查询\", type = LogType.SELECT)\n"
                + "    @RequestMapping(value = \"/{id}/columns/{column}\", method = RequestMethod.GET)\n"
                + "    public String selectColumnById(@PathVariable " + idTypeName + " id, @PathVariable String column) {\n"
                + "        return getService().selectColumnById(id, column);\n"
                + "    }\n\n"
                + "    @Override\n"
                + "    @Log(value = \"删除\", type = LogType.DELETE)\n"
                + "    @RequestMapping(method = RequestMethod.DELETE)\n"
                + "    public void delete(RestJsonWrapperBean wrapper) {\n"
                + "        getService().delete(wrapper);\n"
                + "    }\n\n"
                + "    @Override\n"
                + "    @Log(value = \"新增\", type = LogType.INSERT)\n"
                + "    @RequestMapping(method = RequestMethod.POST)\n"
                + "    public " + idTypeName + " insert(RestJsonWrapperBean wrapper) {\n"
                + "        return getService().insert(wrapper);\n"
                + "    }\n\n"
                + "    @Override\n"
                + "    @Log(value = \"实时修改\", type = LogType.UPDATE)\n"
                + "    @RequestMapping(value = \"/instant\", method = RequestMethod.PUT)\n"
                + "    public void instantSave(RestJsonWrapperBean wrapper) {\n"
                + "        getService().instantSave(wrapper);\n"
                + "    }\n\n"
                + "    @Override\n"
                + "    @Log(value = \"修改\", type = LogType.UPDATE)\n"
                + "    @RequestMapping(value = \"/{id}\", method = RequestMethod.PUT)\n"
                + "    public void save(@PathVariable " + idTypeName + " id, RestJsonWrapperBean wrapper) {\n"
                + "        getService().save(id, wrapper);\n"
                + "    }\n\n"
                + "    @Override\n"
                + "    @Log(value = \"查询页查询\", type = LogType.SELECT)\n"
                + "    @RequestMapping(value = \"/queries/searchable\", method = RequestMethod.POST)\n"
                + "    public Page<Map<String, Object>> selectSearchablePagination(RestJsonWrapperBean wrapper) {\n"
                + "        return getService().selectSearchablePagination(wrapper);\n"
                + "    }\n\n"
                + "    @Override\n"
                + "    @Log(value = \"验证数据唯一性\", type = LogType.VALIDATE)\n"
                + "    @RequestMapping(value = { \"/{id}/action/validate-unique\", \"/{id}/action/validate-unique/{columnName}/{columnValue}\", \"/action/validate-unique/{columnName}/{columnValue}\" }, method = RequestMethod.POST)\n"
                + "    public RestValidationResultBean validateUnique(@PathVariable(required = false) " + idTypeName + " id, @PathVariable(required = false) String columnName, @PathVariable(required = false) String columnValue) {\n"
                + "        return getService().validateUnique(id, columnName, columnValue);\n"
                + "    }\n\n"
                + "    @Override\n"
                + "    @Log(value = \"验证数据唯一性\", type = LogType.VALIDATE)\n"
                + "    @RequestMapping(value = \"/action/validate-unique\", method = RequestMethod.POST)\n"
                + "    public RestValidationResultBean validateUnique(RestJsonWrapperBean wrapper) {\n"
                + "        return getService().validateUnique(wrapper);\n"
                + "    }\n\n"
                + "    @Override\n"
                + "    @Log(value = \"查询低代码服务\", type = LogType.SELECT)\n"
                + "    @RequestMapping(value = \"/action/lcdp-services\", method = RequestMethod.GET)\n"
                + "    public Map<String, String> selectLcdpServices() {\n"
                + "        return getService().getLcdpServiceTypeMap();\n"
                + "    }\n\n"
                + "    @Override\n"
                + "    @Log(value = \"查询低代码方法\", type = LogType.SELECT)\n"
                + "    @RequestMapping(value = \"/action/lcdp-methods\", method = RequestMethod.GET)\n"
                + "    public List<Map<String, Object>> selectLcdpMethods() {\n"
                + "        return getService().getLcdpMethodList();\n"
                + "    }\n\n"
                + "    @Override\n"
                + "    @Log(value = \"调用低代码方法\", type = LogType.SELECT)\n"
                + "    @RequestMapping(value = \"/action/lcdp-invoke\", method = RequestMethod.POST)\n"
                + "    public Object invokeLcdpMethod(RestJsonWrapperBean wrapper) {\n"
                + "        return getService().invokeLcdpMethod(wrapper);\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    @Log(value = \"桥接低代码方法\", type = LogType.SELECT)\n"
                + "    @RequestMapping(value = \"/action/lcdp-call\", method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE })\n"
                + "    public Object callLcdpMethod(@RequestParam String servicePath, @RequestParam String methodName, RestJsonWrapperBean wrapper) {\n"
                + "        RestJsonWrapperBean actualWrapper = wrapper == null ? new RestJsonWrapperBean() : wrapper;\n"
                + "        actualWrapper.getParamMap().put(\"servicePath\", servicePath);\n"
                + "        actualWrapper.getParamMap().put(\"methodName\", methodName);\n"
                + "        return getService().invokeLcdpMethod(actualWrapper);\n"
                + "    }\n"
                + "}\n";
    }

    private String buildLcdpServiceFieldName(String resourceName) {
        String serviceName = ObjectUtils.toString(resourceName);
        if (serviceName.startsWith("Lcdp") && serviceName.length() > 4) {
            serviceName = serviceName.substring(4);
        }
        serviceName = serviceName.replaceAll("([a-z0-9])([A-Z])", "$1 $2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2");
        return defaultIfBlank(toLowerCamel(serviceName), "lcdpService");
    }

    private void appendLcdpMethodInit(StringBuilder builder, List<LcdpMethodMeta> methodMetaList) {
        for (LcdpMethodMeta methodMeta : methodMetaList) {
            builder.append("        {\n");
            builder.append("            Map<String, Object> methodMeta = new LinkedHashMap<>();\n");
            builder.append("            methodMeta.put(\"servicePath\", \"").append(escapeJava(methodMeta.servicePath)).append("\");\n");
            builder.append("            methodMeta.put(\"serviceName\", \"").append(escapeJava(methodMeta.serviceName)).append("\");\n");
            builder.append("            methodMeta.put(\"methodName\", \"").append(escapeJava(methodMeta.methodName)).append("\");\n");
            builder.append("            methodMeta.put(\"mappingType\", ")
                    .append(methodMeta.mappingType == null ? "null" : "\"" + escapeJava(methodMeta.mappingType) + "\"")
                    .append(");\n");
            builder.append("            methodMeta.put(\"mappingDesc\", ")
                    .append(methodMeta.mappingDesc == null ? "null" : "\"" + escapeJava(methodMeta.mappingDesc) + "\"")
                    .append(");\n");
            builder.append("            methodMeta.put(\"returnType\", \"").append(escapeJava(methodMeta.returnTypeName)).append("\");\n");
            builder.append("            methodMeta.put(\"parameterTypes\", Arrays.asList(");
            for (int i = 0; i < methodMeta.parameterTypeNameList.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append("\"").append(escapeJava(methodMeta.parameterTypeNameList.get(i))).append("\"");
            }
            builder.append("));\n");
            builder.append("            lcdpMethodList.add(methodMeta);\n");
            builder.append("        }\n");
        }
    }

    private void appendLcdpInvokeHelpers(StringBuilder builder) {
        builder.append("\n");
        builder.append("    private Object invokeLcdpServiceMethod(Object serviceBean, String methodName, RestJsonWrapperBean wrapper) {\n");
        builder.append("        List<Object> rawArgs = extractInvokeArgs(wrapper.getParamMap().get(\"args\"));\n");
        builder.append("        Class<?> targetClass = ClassUtils.getRawType(serviceBean.getClass());\n");
        builder.append("        for (Method method : targetClass.getMethods()) {\n");
        builder.append("            if (!method.getName().equals(methodName) || method.getDeclaringClass() == Object.class) {\n");
        builder.append("                continue;\n");
        builder.append("            }\n");
        builder.append("            Object[] invokeArgs = buildInvokeArgs(method, rawArgs, wrapper);\n");
        builder.append("            if (invokeArgs == null) {\n");
        builder.append("                continue;\n");
        builder.append("            }\n");
        builder.append("            return ReflectionUtils.invokeMethod(method, serviceBean, invokeArgs);\n");
        builder.append("        }\n");
        builder.append("        throw new ApplicationRuntimeException(\"未找到匹配的低代码方法：\" + methodName);\n");
        builder.append("    }\n\n");
        builder.append("    private List<Object> extractInvokeArgs(Object rawArgs) {\n");
        builder.append("        if (rawArgs == null) {\n");
        builder.append("            return Collections.emptyList();\n");
        builder.append("        }\n");
        builder.append("        if (rawArgs instanceof List) {\n");
        builder.append("            return new ArrayList<>((List<Object>) rawArgs);\n");
        builder.append("        }\n");
        builder.append("        if (rawArgs.getClass().isArray()) {\n");
        builder.append("            int length = java.lang.reflect.Array.getLength(rawArgs);\n");
        builder.append("            List<Object> arrayArgs = new ArrayList<>(length);\n");
        builder.append("            for (int i = 0; i < length; i++) {\n");
        builder.append("                arrayArgs.add(java.lang.reflect.Array.get(rawArgs, i));\n");
        builder.append("            }\n");
        builder.append("            return arrayArgs;\n");
        builder.append("        }\n");
        builder.append("        return new ArrayList<>(Collections.singletonList(rawArgs));\n");
        builder.append("    }\n\n");
        builder.append("    private Object[] buildInvokeArgs(Method method, List<Object> rawArgs, RestJsonWrapperBean wrapper) {\n");
        builder.append("        Class<?>[] parameterTypes = method.getParameterTypes();\n");
        builder.append("        if (parameterTypes.length == 0) {\n");
        builder.append("            return rawArgs.isEmpty() ? new Object[0] : null;\n");
        builder.append("        }\n");
        builder.append("        if (parameterTypes.length == 1 && RestJsonWrapperBean.class.isAssignableFrom(parameterTypes[0])) {\n");
        builder.append("            return new Object[] { wrapper };\n");
        builder.append("        }\n");
        builder.append("        if (method.isVarArgs()) {\n");
        builder.append("            int fixedCount = parameterTypes.length - 1;\n");
        builder.append("            if (rawArgs.size() < fixedCount) {\n");
        builder.append("                return null;\n");
        builder.append("            }\n");
        builder.append("            Object[] invokeArgs = new Object[parameterTypes.length];\n");
        builder.append("            for (int i = 0; i < fixedCount; i++) {\n");
        builder.append("                Object converted = convertInvokeArg(rawArgs.get(i), parameterTypes[i]);\n");
        builder.append("                if (converted == INVALID_ARGUMENT) {\n");
        builder.append("                    return null;\n");
        builder.append("                }\n");
        builder.append("                invokeArgs[i] = converted;\n");
        builder.append("            }\n");
        builder.append("            Class<?> componentType = parameterTypes[parameterTypes.length - 1].getComponentType();\n");
        builder.append("            Object varArgValues = java.lang.reflect.Array.newInstance(componentType, rawArgs.size() - fixedCount);\n");
        builder.append("            for (int i = fixedCount; i < rawArgs.size(); i++) {\n");
        builder.append("                Object converted = convertInvokeArg(rawArgs.get(i), componentType);\n");
        builder.append("                if (converted == INVALID_ARGUMENT) {\n");
        builder.append("                    return null;\n");
        builder.append("                }\n");
        builder.append("                java.lang.reflect.Array.set(varArgValues, i - fixedCount, converted);\n");
        builder.append("            }\n");
        builder.append("            invokeArgs[parameterTypes.length - 1] = varArgValues;\n");
        builder.append("            return invokeArgs;\n");
        builder.append("        }\n");
        builder.append("        if (parameterTypes.length != rawArgs.size()) {\n");
        builder.append("            return null;\n");
        builder.append("        }\n");
        builder.append("        Object[] invokeArgs = new Object[parameterTypes.length];\n");
        builder.append("        for (int i = 0; i < parameterTypes.length; i++) {\n");
        builder.append("            Object converted = convertInvokeArg(rawArgs.get(i), parameterTypes[i]);\n");
        builder.append("            if (converted == INVALID_ARGUMENT) {\n");
        builder.append("                return null;\n");
        builder.append("            }\n");
        builder.append("            invokeArgs[i] = converted;\n");
        builder.append("        }\n");
        builder.append("        return invokeArgs;\n");
        builder.append("    }\n\n");
        builder.append("    private static final Object INVALID_ARGUMENT = new Object();\n\n");
        builder.append("    private Object convertInvokeArg(Object value, Class<?> targetType) {\n");
        builder.append("        if (value == null) {\n");
        builder.append("            return targetType.isPrimitive() ? ConvertUtils.convert(null, targetType) : null;\n");
        builder.append("        }\n");
        builder.append("        if (targetType == Object.class || targetType.isInstance(value)) {\n");
        builder.append("            return value;\n");
        builder.append("        }\n");
        builder.append("        if (targetType.isArray() && value instanceof List) {\n");
        builder.append("            List<?> itemList = (List<?>) value;\n");
        builder.append("            Class<?> componentType = targetType.getComponentType();\n");
        builder.append("            Object array = java.lang.reflect.Array.newInstance(componentType, itemList.size());\n");
        builder.append("            for (int i = 0; i < itemList.size(); i++) {\n");
        builder.append("                Object converted = convertInvokeArg(itemList.get(i), componentType);\n");
        builder.append("                if (converted == INVALID_ARGUMENT) {\n");
        builder.append("                    return INVALID_ARGUMENT;\n");
        builder.append("                }\n");
        builder.append("                java.lang.reflect.Array.set(array, i, converted);\n");
        builder.append("            }\n");
        builder.append("            return array;\n");
        builder.append("        }\n");
        builder.append("        if (List.class.isAssignableFrom(targetType) && value instanceof List) {\n");
        builder.append("            return value;\n");
        builder.append("        }\n");
        builder.append("        if (Map.class.isAssignableFrom(targetType) && value instanceof Map) {\n");
        builder.append("            return value;\n");
        builder.append("        }\n");
        builder.append("        try {\n");
        builder.append("            return ConvertUtils.convert(value, targetType);\n");
        builder.append("        } catch (Exception e) {\n");
        builder.append("            return INVALID_ARGUMENT;\n");
        builder.append("        }\n");
        builder.append("    }\n");
    }

    private String renderControllerInterface(ModuleContext context, List<PageContext> pageContextList) {
        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(context.controllerPackage).append(";\n\n");
        builder.append("import java.util.Map;\n");
        builder.append("import org.springframework.ui.Model;\n");
        builder.append("import org.springframework.web.bind.annotation.RequestMapping;\n\n");
        builder.append("@RequestMapping(\"").append(resolveControllerBasePath(context, pageContextList)).append("\")\n");
        builder.append("public interface ").append(context.className).append("Controller {\n\n");
        for (PageContext pageContext : pageContextList) {
            builder.append("    @RequestMapping(\"/").append(resolveControllerMethodPath(pageContext)).append("\")\n");
            builder.append("    String ").append(pageContext.methodName).append("(Map<String, String> param, Model model);\n\n");
        }
        builder.append("}\n");
        return builder.toString();
    }

    private String renderControllerImpl(ModuleContext context, List<PageContext> pageContextList) {
        PageContext editListPage = pageContextList.stream().filter(page -> page.editListEntry).findFirst().orElse(null);
        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(context.controllerPackage).append(".impl;\n\n");
        builder.append("import java.util.Map;\n");
        builder.append("import org.springframework.stereotype.Controller;\n");
        builder.append("import org.springframework.ui.Model;\n");
        builder.append("import org.springframework.web.bind.annotation.RequestParam;\n\n");
        builder.append("import ").append(context.controllerPackage).append(".").append(context.className).append("Controller;\n");
        builder.append("import com.sunwayworld.framework.log.annotation.Log;\n");
        builder.append("import com.sunwayworld.framework.log.annotation.LogType;\n");
        builder.append("import com.sunwayworld.framework.spring.annotation.GikamBean;\n\n");
        builder.append("@Controller\n");
        builder.append("@GikamBean\n");
        builder.append("public class ").append(context.className).append("ControllerImpl implements ").append(context.className).append("Controller {\n\n");
        if (editListPage != null) {
            builder.append("    @Log(value = \"").append(editListPage.pageResourceName).append("\", type = LogType.CONTROLLER)\n");
            builder.append("    @Override\n");
            builder.append("    public String editListPage(@RequestParam Map<String, String> param, Model model) {\n");
            builder.append("        model.addAttribute(\"param\", param);\n");
            builder.append("        return \"").append(context.moduleResourcePath).append("/").append(editListPage.templateName).append("\";\n");
            builder.append("    }\n\n");
        }
        for (PageContext pageContext : pageContextList) {
            if (pageContext.editListEntry) {
                continue;
            }
            builder.append("    @Log(value = \"").append(pageContext.pageResourceName).append("\", type = LogType.CONTROLLER)\n");
            builder.append("    @Override\n");
            builder.append("    public String ").append(pageContext.methodName).append("(@RequestParam Map<String, String> param, Model model) {\n");
            builder.append("        model.addAttribute(\"param\", param);\n");
            builder.append("        return \"").append(context.moduleResourcePath).append("/").append(pageContext.templateName).append("\";\n");
            builder.append("    }\n\n");
        }
        builder.append("}\n");
        return builder.toString();
    }

    private String renderTemplate(ModuleContext context, PageContext pageContext) {
        return "<script th:inline=\"javascript\">\n"
                + "/*<![CDATA[*/\n"
                + "    Gikam.requirejs([\n"
                + "        '/static/" + context.moduleResourcePath + "/" + pageContext.templateName + ".js'\n"
                + "    ]);\n"
                + "    " + buildPageVarName(context, pageContext) + ".init(Gikam.getElParam([[${param}]]));\n"
                + "/*]]>*/\n"
                + "</script>\n";
    }

    private String renderPageJs(ModuleContext context, PageContext pageContext, List<PageContext> pageContextList) {
        PageParseResult parseResult = parsePageContext(pageContext);
        normalizeGeneratedPageConfig(context, parseResult, pageContextList);
        GeneratedPageJsMeta pageJsMeta = buildGeneratedPageJsMeta(parseResult);
        pageJsMeta.methodsContent = rewriteGeneratedWorkspaceWindowSaveConfig(pageJsMeta.methodsContent);
        String pageVarName = buildPageVarName(context, pageContext);
        String viewContent = rewriteGeneratedViewContent(context, cleanupGeneratedViewContent(pageContext.viewContent), pageContextList);
        PageJsRuntimeUsage runtimeUsage = analyzePageJsRuntimeUsage(pageVarName, viewContent, pageContextList);
        if (StringUtils.contains(pageJsMeta.methodsContent, "getPageUrl(")) {
            runtimeUsage.useGetPageUrlHelper = true;
            runtimeUsage.referencedPagePathSet.addAll(pageContextList.stream().map(item -> item.pagePath).collect(Collectors.toList()));
        }
        StringBuilder builder = new StringBuilder();
        builder.append("//@ sourceURL=").append(pageContext.templateName).append(".js\n");
        builder.append("(function(window) {\n");
        builder.append("    var requestPath = IFM_CONTEXT + '").append(defaultIfBlank(pageContext.serviceRequestPath, resolvePrimaryServiceRequestPath(context))).append("';\n");
        builder.append("    var initPageParam = {};\n");
        if (runtimeUsage.useInvokeLcdpMethodHelper) {
            builder.append("\n");
            appendGeneratedInvokeUrlHelpers(builder, context);
        }
        if (runtimeUsage.usePagePathMap()) {
            builder.append("\n");
            builder.append("    var pagePathMap = ");
            appendJsValue(builder, buildPagePathMap(context, pageContextList, runtimeUsage.referencedPagePathSet), 1);
            builder.append(";\n");
        }
        if (runtimeUsage.hasAnyHelper()) {
            builder.append("\n");
            appendStandalonePageHelpers(builder, runtimeUsage);
        } else {
            builder.append("\n");
            appendCurrentWindowHelper(builder);
        }
        if (StringUtils.isNotBlank(viewContent)) {
            builder.append("\n");
            appendIndentedScript(builder, viewContent, 1);
        }
        builder.append("\n");
        builder.append("    var ").append(pageVarName).append(" = window.").append(pageVarName)
                .append(" = Gikam.getPageObject({\n");
        builder.append("        requestPath : requestPath,\n");
        builder.append("        baseUrl : requestPath,\n");
        builder.append("        pagePath : ").append(JSON.toJSONString(pageContext.pagePath)).append(",\n");
        builder.append("        templateName : ").append(JSON.toJSONString(pageContext.templateName)).append(",\n\n");
        builder.append("        getWindowToolbar : function() {\n");
        if (StringUtils.isNotBlank(pageJsMeta.windowToolbarMethodName)) {
            builder.append("            return this.applyToolbarPageConfig(this.").append(pageJsMeta.windowToolbarMethodName).append("());\n");
        } else {
            builder.append("            return null;\n");
        }
        builder.append("        },\n\n");
        builder.append("        getCurrentPageConfig : function() {\n");
        builder.append("            if (typeof onBeforePageConfig === 'function') {\n");
        builder.append("                return onBeforePageConfig() || null;\n");
        builder.append("            }\n");
        builder.append("            if (typeof onBeforePageConfigLoad === 'function') {\n");
        builder.append("                return onBeforePageConfigLoad() || null;\n");
        builder.append("            }\n");
        builder.append("            return null;\n");
        builder.append("        },\n\n");
        builder.append("        getWindowPageConfig : function() {\n");
        builder.append("            var currentWindow = getCurrentWindow();\n");
        builder.append("            if (!currentWindow || !currentWindow.getPageConfig) {\n");
        builder.append("                return null;\n");
        builder.append("            }\n");
        builder.append("            var pageConfig = currentWindow.getPageConfig();\n");
        builder.append("            if (!hasUsablePageConfig(pageConfig)) {\n");
        builder.append("                return null;\n");
        builder.append("            }\n");
        builder.append("            return Gikam.deepExtend ? Gikam.deepExtend({}, pageConfig) : Object.assign({}, pageConfig);\n");
        builder.append("        },\n\n");
        builder.append("        getRenderPageConfig : function() {\n");
        builder.append("            var pageConfig = this.getCurrentPageConfig();\n");
        builder.append("            var windowPageConfig = this.getWindowPageConfig();\n");
        builder.append("            var hasPageConfig = hasUsablePageConfig(pageConfig);\n");
        builder.append("            var hasWindowPageConfig = hasUsablePageConfig(windowPageConfig);\n");
        builder.append("            if (!hasPageConfig && !hasWindowPageConfig) {\n");
        builder.append("                return null;\n");
        builder.append("            }\n");
        builder.append("            if (!hasPageConfig) {\n");
        builder.append("                return windowPageConfig;\n");
        builder.append("            }\n");
        builder.append("            var renderPageConfig = Gikam.deepExtend ? Gikam.deepExtend({}, pageConfig) : Object.assign({}, pageConfig);\n");
        builder.append("            if (!hasWindowPageConfig) {\n");
        builder.append("                return renderPageConfig;\n");
        builder.append("            }\n");
        builder.append("            return Gikam.deepExtend ? Gikam.deepExtend(renderPageConfig, windowPageConfig) : Object.assign(renderPageConfig, windowPageConfig);\n");
        builder.append("        },\n\n");
        builder.append("        applyToolbarPageConfig : function(toolbar) {\n");
        builder.append("            if (!toolbar || !toolbar.items || !toolbar.items.length) {\n");
        builder.append("                return toolbar;\n");
        builder.append("            }\n");
        builder.append("            var pageConfig = this.getRenderPageConfig();\n");
        builder.append("            if (!pageConfig) {\n");
        builder.append("                return toolbar;\n");
        builder.append("            }\n");
        builder.append("            var buttonConfig = pageConfig.button || {};\n");
        builder.append("            var nextToolbar = Gikam.deepExtend ? Gikam.deepExtend({}, toolbar) : Object.assign({}, toolbar);\n");
        builder.append("            nextToolbar.items = (toolbar.items || []).map(function(item) {\n");
        builder.append("                var nextItem = Gikam.deepExtend ? Gikam.deepExtend({}, item) : Object.assign({}, item);\n");
        builder.append("                var currentButtonConfig = nextItem && nextItem.id ? buttonConfig[nextItem.id] : null;\n");
        builder.append("                var displayText = typeof nextItem.text === 'string' && Gikam.propI18N ? Gikam.propI18N(nextItem.text) : nextItem.text;\n");
        builder.append("                var trimmedText = typeof displayText === 'string' ? displayText.trim() : '';\n");
        builder.append("                if (trimmedText === '返回' || trimmedText === '审计跟踪' || nextItem.iconType === 'back') {\n");
        builder.append("                    nextItem.hidden = false;\n");
        builder.append("                    return nextItem;\n");
        builder.append("                }\n");
        builder.append("                if (pageConfig.readonly === true) {\n");
        builder.append("                    var hidden = currentButtonConfig ? currentButtonConfig.hidden : undefined;\n");
        builder.append("                    nextItem.hidden = typeof hidden === 'undefined' ? true : hidden;\n");
        builder.append("                    return nextItem;\n");
        builder.append("                }\n");
        builder.append("                if (!currentButtonConfig) {\n");
        builder.append("                    return nextItem;\n");
        builder.append("                }\n");
        builder.append("                return Gikam.deepExtend ? Gikam.deepExtend(nextItem, currentButtonConfig) : Object.assign(nextItem, currentButtonConfig);\n");
        builder.append("            });\n");
        builder.append("            return nextToolbar;\n");
        builder.append("        },\n\n");
        builder.append("        getRenderComponents : function() {\n");
        builder.append("            return [\n");
        for (int i = 0; i < pageJsMeta.rootComponentMethodNames.size(); i++) {
            builder.append("                this.").append(pageJsMeta.rootComponentMethodNames.get(i)).append("()");
            if (i < pageJsMeta.rootComponentMethodNames.size() - 1) {
                builder.append(",");
            }
            builder.append("\n");
        }
        builder.append("            ];\n");
        builder.append("        },\n\n");
        builder.append(pageJsMeta.methodsContent);
        builder.append("        renderRootComponent : function(component) {\n");
        builder.append("            if (!component) {\n");
        builder.append("                return;\n");
        builder.append("            }\n");
        builder.append("            var options = Gikam.deepExtend ? Gikam.deepExtend({}, component) : Object.assign({}, component);\n");
        builder.append("            var renderPageConfig = this.getRenderPageConfig();\n");
        builder.append("            if (renderPageConfig) {\n");
        builder.append("                options.config = renderPageConfig;\n");
        builder.append("            }\n");
        builder.append("            normalizeGeneratedRequestData(options);\n");
        builder.append("            var type = options.type;\n");
        builder.append("            delete options.type;\n");
        builder.append("            if (!options.renderTo && window.workspace && workspace.window) {\n");
        builder.append("                options.renderTo = workspace.window.$dom;\n");
        builder.append("            }\n");
        builder.append("            return Gikam.create(type, options);\n");
        builder.append("        },\n\n");
        builder.append("        doCreate : function() {\n");
        builder.append("            var toolbar = this.getWindowToolbar();\n");
        builder.append("            var currentWindow = getCurrentWindow();\n");
        builder.append("            if (toolbar && currentWindow) {\n");
        builder.append("                if (currentWindow.refreshToolbar) {\n");
        builder.append("                    currentWindow.refreshToolbar(toolbar);\n");
        builder.append("                } else if (currentWindow.setToolbar) {\n");
        builder.append("                    currentWindow.setToolbar(toolbar);\n");
        builder.append("                }\n");
        builder.append("            }\n");
        builder.append("            var components = this.getRenderComponents() || [];\n");
        builder.append("            if (!components.length) {\n");
        builder.append("                return;\n");
        builder.append("            }\n");
        builder.append("            if (components.length === 1) {\n");
        builder.append("                return this.renderRootComponent(components[0]);\n");
        builder.append("            }\n");
        builder.append("            var layoutOptions = {\n");
        builder.append("                renderTo : window.workspace && workspace.window ? workspace.window.$dom : null,\n");
        builder.append("                center : {\n");
        builder.append("                    items : components\n");
        builder.append("                }\n");
        builder.append("            };\n");
        builder.append("            var renderPageConfig = this.getRenderPageConfig();\n");
        builder.append("            if (renderPageConfig) {\n");
        builder.append("                layoutOptions.config = renderPageConfig;\n");
        builder.append("            }\n");
        builder.append("            return Gikam.create('layout', layoutOptions);\n");
        builder.append("        },\n\n");
        builder.append("        create : function() {\n");
        builder.append("            var self = this;\n");
        builder.append("            if (typeof onBeforePageRender === 'function') {\n");
        builder.append("                return onBeforePageRender(function() {\n");
        builder.append("                    self.doCreate();\n");
        builder.append("                });\n");
        builder.append("            }\n");
        builder.append("            return this.doCreate();\n");
        builder.append("        },\n\n");
        builder.append("        init : function(param) {\n");
        builder.append("            initPageParam = param || {};\n");
        builder.append("            syncPageParamReference(initPageParam);\n");
        builder.append("            this.create();\n");
        builder.append("        }\n");
        builder.append("    });\n");
        builder.append("})(window);\n");
        return builder.toString();
    }

    private GeneratedPageJsMeta buildGeneratedPageJsMeta(PageParseResult parseResult) {
        GeneratedPageJsContext generationContext = new GeneratedPageJsContext();
        for (int i = 0; i < parseResult.components.size(); i++) {
            Object component = parseResult.components.get(i);
            if (!(component instanceof JSONObject)) {
                continue;
            }
            String suffix = resolveConfigMethodSuffix((JSONObject) component, "Root" + (i + 1));
            String methodName = ensureUniqueMethodName("get" + suffix + "Config", generationContext.usedMethodNames);
            generationContext.rootComponentMethodNames.add(methodName);
            appendGeneratedObjectMethod(generationContext, methodName, component, suffix);
        }
        if (parseResult.windowToolbar instanceof JSONObject) {
            generationContext.windowToolbarMethodName = ensureUniqueMethodName("getWindowToolbarConfig", generationContext.usedMethodNames);
            appendGeneratedObjectMethod(generationContext, generationContext.windowToolbarMethodName, parseResult.windowToolbar, "WindowToolbar");
        }
        GeneratedPageJsMeta pageJsMeta = new GeneratedPageJsMeta();
        pageJsMeta.rootComponentMethodNames.addAll(generationContext.rootComponentMethodNames);
        pageJsMeta.windowToolbarMethodName = generationContext.windowToolbarMethodName;
        pageJsMeta.methodsContent = generationContext.methodsBuilder.toString();
        return pageJsMeta;
    }

    private void appendGeneratedObjectMethod(GeneratedPageJsContext generationContext, String methodName, Object value, String ownerSuffix) {
        StringBuilder methodBuilder = new StringBuilder();
        methodBuilder.append("        ").append(methodName).append(" : function() {\n");
        methodBuilder.append("            return ");
        appendGeneratedMethodValue(generationContext, methodBuilder, value, 3, ownerSuffix, null);
        methodBuilder.append(";\n");
        methodBuilder.append("        },\n\n");
        generationContext.methodsBuilder.append(methodBuilder);
    }

    private void appendGeneratedMethodValue(GeneratedPageJsContext generationContext, StringBuilder builder, Object value, int indentLevel, String ownerSuffix, String propertyName) {
        if (value instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) value;
            if (StringUtils.isNotBlank(propertyName) && isComponentConfig(jsonObject)) {
                String suffix = resolveConfigMethodSuffix(jsonObject, concatMethodSuffix(ownerSuffix, propertyName));
                String methodName = ensureUniqueMethodName("get" + suffix + "Config", generationContext.usedMethodNames);
                appendGeneratedObjectMethod(generationContext, methodName, jsonObject, suffix);
                builder.append("this.").append(methodName).append("()");
                return;
            }
            String nestedOwnerSuffix = StringUtils.isNotBlank(propertyName) ? concatMethodSuffix(ownerSuffix, propertyName) : ownerSuffix;
            appendGeneratedObjectLiteral(generationContext, builder, jsonObject, indentLevel, nestedOwnerSuffix);
            return;
        }
        if (value instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) value;
            if (StringUtils.isNotBlank(propertyName) && shouldExtractArrayMethod(propertyName, jsonArray)) {
                String suffix = concatMethodSuffix(ownerSuffix, propertyName);
                String methodName = ensureUniqueMethodName("get" + suffix, generationContext.usedMethodNames);
                appendGeneratedObjectMethod(generationContext, methodName, jsonArray, suffix);
                builder.append("this.").append(methodName).append("()");
                return;
            }
            appendGeneratedArrayLiteral(generationContext, builder, jsonArray, indentLevel, ownerSuffix, propertyName);
            return;
        }
        appendJsValue(builder, value, indentLevel);
    }

    private void appendGeneratedObjectLiteral(GeneratedPageJsContext generationContext, StringBuilder builder, JSONObject jsonObject, int indentLevel, String ownerSuffix) {
        builder.append("{");
        if (!jsonObject.isEmpty()) {
            builder.append("\n");
            int index = 0;
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                indent(builder, indentLevel + 1);
                builder.append(formatJsKey(entry.getKey())).append(" : ");
                appendGeneratedMethodValue(generationContext, builder, entry.getValue(), indentLevel + 1, ownerSuffix, entry.getKey());
                if (index++ < jsonObject.size() - 1) {
                    builder.append(",");
                }
                builder.append("\n");
            }
            indent(builder, indentLevel);
        }
        builder.append("}");
    }

    private void appendGeneratedArrayLiteral(GeneratedPageJsContext generationContext, StringBuilder builder, JSONArray jsonArray, int indentLevel, String ownerSuffix, String propertyName) {
        builder.append("[");
        if (!jsonArray.isEmpty()) {
            builder.append("\n");
            for (int i = 0; i < jsonArray.size(); i++) {
                indent(builder, indentLevel + 1);
                Object item = jsonArray.get(i);
                if (item instanceof JSONObject && isComponentConfig((JSONObject) item) && shouldExtractArrayItemComponent(propertyName)) {
                    JSONObject component = (JSONObject) item;
                    String suffix = resolveConfigMethodSuffix(component, concatMethodSuffix(ownerSuffix, String.valueOf(i + 1)));
                    String methodName = ensureUniqueMethodName("get" + suffix + "Config", generationContext.usedMethodNames);
                    appendGeneratedObjectMethod(generationContext, methodName, component, suffix);
                    builder.append("this.").append(methodName).append("()");
                } else {
                    appendGeneratedMethodValue(generationContext, builder, item, indentLevel + 1,
                            concatMethodSuffix(ownerSuffix, String.valueOf(i + 1)), null);
                }
                if (i < jsonArray.size() - 1) {
                    builder.append(",");
                }
                builder.append("\n");
            }
            indent(builder, indentLevel);
        }
        builder.append("]");
    }

    private boolean shouldExtractArrayMethod(String propertyName, JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.isEmpty()) {
            return false;
        }
        return Arrays.asList("columns").contains(propertyName);
    }

    private boolean shouldExtractArrayItemComponent(String propertyName) {
        return Arrays.asList("items").contains(propertyName);
    }

    private boolean isComponentConfig(JSONObject jsonObject) {
        return jsonObject != null && StringUtils.isNotBlank(jsonObject.getString("type"));
    }

    private String resolveConfigMethodSuffix(JSONObject jsonObject, String defaultSuffix) {
        if (jsonObject != null && StringUtils.isNotBlank(jsonObject.getString("id"))) {
            return toMethodSuffix(jsonObject.getString("id"));
        }
        if (jsonObject != null && StringUtils.isNotBlank(jsonObject.getString("type"))) {
            return toMethodSuffix(jsonObject.getString("type"));
        }
        return StringUtils.isNotBlank(defaultSuffix) ? defaultSuffix : "Component";
    }

    private String concatMethodSuffix(String ownerSuffix, String propertyName) {
        String suffix = toMethodSuffix(propertyName);
        return StringUtils.isBlank(ownerSuffix) ? suffix : ownerSuffix + suffix;
    }

    private String toMethodSuffix(String value) {
        return StringUtils.capitalize(defaultIfBlank(toLowerCamel(value), "component"));
    }

    private String ensureUniqueMethodName(String methodName, Set<String> usedMethodNames) {
        if (usedMethodNames.add(methodName)) {
            return methodName;
        }
        int index = 2;
        while (!usedMethodNames.add(methodName + index)) {
            index++;
        }
        return methodName + index;
    }

    private String buildPageVarName(ModuleContext context, PageContext pageContext) {
        return context.jsNamespace + StringUtils.capitalize(pageContext.pageObjectName);
    }

    private String resolveControllerBasePath(ModuleContext context, List<PageContext> pageContextList) {
        if (CollectionUtils.isEmpty(pageContextList)) {
            return context.requestPath + "/page";
        }
        String pagePath = pageContextList.get(0).pagePath;
        if (StringUtils.isBlank(pagePath) || !pagePath.contains(".")) {
            return context.requestPath + "/page";
        }
        int index = pagePath.lastIndexOf('.');
        return buildSecureRequestPath(pagePath.substring(0, index), context.requestPath + "/page");
    }

    private String resolveControllerMethodPath(PageContext pageContext) {
        if (pageContext == null || StringUtils.isBlank(pageContext.pagePath) || !pageContext.pagePath.contains(".")) {
            return defaultIfBlank(pageContext == null ? null : pageContext.pageResourceName, "");
        }
        int index = pageContext.pagePath.lastIndexOf('.');
        return index < 0 ? pageContext.pagePath : pageContext.pagePath.substring(index + 1);
    }

    private Map<String, Object> buildPagePathMap(ModuleContext context, List<PageContext> pageContextList, Set<String> referencedPagePathSet) {
        Map<String, Object> pagePathMap = new LinkedHashMap<>();
        for (PageContext item : pageContextList) {
            if (!CollectionUtils.isEmpty(referencedPagePathSet) && !referencedPagePathSet.contains(item.pagePath)) {
                continue;
            }
            pagePathMap.put(item.pagePath, new RawJs("IFM_CONTEXT + '" + defaultIfBlank(item.controllerPath, context.requestPath) + "'"));
        }
        return pagePathMap;
    }

    private Map<String, Object> buildGeneratedServicePathMap(ModuleContext context) {
        Map<String, Object> servicePathMap = new LinkedHashMap<>();
        List<LcdpResourceHistoryBean> javaHistoryList = new ArrayList<>(context.javaHistoryList);
        javaHistoryList.sort(Comparator.comparing(LcdpResourceHistoryBean::getResourceName));
        for (LcdpResourceHistoryBean history : javaHistoryList) {
            if (StringUtils.isBlank(history.getPath())) {
                continue;
            }
            servicePathMap.put(history.getPath(), new RawJs("IFM_CONTEXT + '" + buildSecureRequestPath(history.getPath(), context.requestPath) + "'"));
        }
        return servicePathMap;
    }

    private void appendGeneratedInvokeUrlHelpers(StringBuilder builder, ModuleContext context) {
        builder.append("    var lcdpServicePathMap = ");
        appendJsValue(builder, buildGeneratedServicePathMap(context), 1);
        builder.append(";\n\n");
        builder.append("    function getInvokeLcdpMethodUrl(servicePath, methodName) {\n");
        builder.append("        var resourcePath = (lcdpServicePathMap || {})[servicePath];\n");
        builder.append("        if (!resourcePath) {\n");
        builder.append("            return servicePath && methodName ? servicePath + '.' + methodName : servicePath;\n");
        builder.append("        }\n");
        builder.append("        return resourcePath + '/' + encodeURIComponent(methodName);\n");
        builder.append("    }\n");
    }

    private String buildGeneratedServiceBaseUrlExpression(ModuleContext context, String servicePath) {
        return "IFM_CONTEXT + " + JSON.toJSONString(buildSecureRequestPath(servicePath, context.requestPath));
    }

    private String buildGeneratedServiceMethodUrlExpression(ModuleContext context, String methodPath) {
        if (!isLcdpServiceMethodPath(methodPath)) {
            return JSON.toJSONString(methodPath);
        }
        int methodIndex = methodPath.lastIndexOf('.');
        String servicePath = methodPath.substring(0, methodIndex);
        String methodName = methodPath.substring(methodIndex + 1);
        return buildGeneratedServiceBaseUrlExpression(context, servicePath) + " + " + JSON.toJSONString("/" + methodName);
    }

    private String buildGeneratedValidatorRuleExpression(ModuleContext context, String ruleType, String methodPath) {
        return JSON.toJSONString(ruleType + "[") + " + " + buildGeneratedServiceMethodUrlExpression(context, methodPath) + " + ']'";
    }

    private String buildGeneratedPageUrlExpression(PageContext pageContext) {
        return "IFM_CONTEXT + " + JSON.toJSONString(defaultIfBlank(pageContext.controllerPath, pageContext.pagePath));
    }

    private void normalizeGeneratedPageConfig(ModuleContext context, PageParseResult parseResult, List<PageContext> pageContextList) {
        if (parseResult == null) {
            return;
        }
        List<Object> normalizedComponents = new ArrayList<>();
        for (Object component : parseResult.components) {
            normalizedComponents.add(normalizeGeneratedJsValue(context, component, null, pageContextList));
        }
        parseResult.components.clear();
        parseResult.components.addAll(normalizedComponents);
        parseResult.windowToolbar = normalizeGeneratedJsValue(context, parseResult.windowToolbar, null, pageContextList);
    }

    private Object normalizeGeneratedJsValue(ModuleContext context, Object value, String propertyName, List<PageContext> pageContextList) {
        if (value instanceof JSONObject) {
            JSONObject source = (JSONObject) value;
            JSONObject target = new JSONObject(true);
            for (Map.Entry<String, Object> entry : source.entrySet()) {
                target.put(entry.getKey(), normalizeGeneratedJsValue(context, entry.getValue(), entry.getKey(), pageContextList));
            }
            return target;
        }
        if (value instanceof JSONArray) {
            JSONArray source = (JSONArray) value;
            JSONArray target = new JSONArray();
            for (Object item : source) {
                target.add(normalizeGeneratedJsValue(context, item, propertyName, pageContextList));
            }
            return target;
        }
        if (!(value instanceof String)) {
            return value;
        }
        String text = (String) value;
        if (StringUtils.isBlank(text)) {
            return value;
        }
        if (isLcdpValidatorRule(text)) {
            Matcher matcher = Pattern.compile("^(unique|remote)\\[(.+)]$").matcher(text);
            if (matcher.find() && isLcdpServiceMethodPath(matcher.group(2))) {
                return new RawJs(buildGeneratedValidatorRuleExpression(context, matcher.group(1), matcher.group(2)));
            }
        }
        if (isLcdpServiceMethodProperty(propertyName) && isLcdpServiceMethodPath(text)) {
            return new RawJs(buildGeneratedServiceMethodUrlExpression(context, text));
        }
        if (StringUtils.equals(propertyName, "url") && isCurrentModulePagePath(text, pageContextList)) {
            PageContext matchedPage = pageContextList.stream().filter(item -> StringUtils.equals(item.pagePath, text)).findFirst().orElse(null);
            if (matchedPage != null) {
                return new RawJs(buildGeneratedPageUrlExpression(matchedPage));
            }
        }
        return value;
    }

    private boolean isLcdpServiceMethodProperty(String propertyName) {
        return Arrays.asList("url", "instantSavePath", "insertUrl", "updateUrl", "deleteUrl", "leftGridUrl",
                "rightGridUrl", "customDataSource", "autoCompleteUrl").contains(propertyName);
    }

    private boolean isLcdpValidatorRule(String value) {
        return value.startsWith("unique[") || value.startsWith("remote[");
    }

    private boolean isLcdpServiceMethodPath(String value) {
        return StringUtils.isNotBlank(value)
                && Pattern.compile("^[A-Za-z0-9_$]+(?:\\.[A-Za-z0-9_$]+)*\\.server\\.[A-Za-z0-9_$]+\\.[A-Za-z0-9_$]+$").matcher(value).matches();
    }

    private boolean isCurrentModulePagePath(String value, List<PageContext> pageContextList) {
        if (StringUtils.isBlank(value) || CollectionUtils.isEmpty(pageContextList)) {
            return false;
        }
        return pageContextList.stream().anyMatch(item -> StringUtils.equals(item.pagePath, value));
    }

    private String rewriteGeneratedViewContent(ModuleContext context, String viewContent, List<PageContext> pageContextList) {
        String rewritten = defaultIfBlank(viewContent, "");
        rewritten = rewriteGeneratedServiceMethodConcat(context, rewritten);
        rewritten = rewriteGeneratedServiceBaseVar(context, rewritten);
        rewritten = rewriteGeneratedBaseUrlConcat(rewritten);
        rewritten = rewriteGeneratedServiceMethodLiteral(context, rewritten);
        rewritten = rewriteGeneratedValidatorLiteral(context, rewritten);
        rewritten = rewriteGeneratedPreInsertConfig(context, rewritten, pageContextList);
        rewritten = rewriteGeneratedPageUrlLiteral(rewritten, pageContextList);
        rewritten = rewriteGeneratedCurrentWindowCall(rewritten);
        rewritten = rewriteGeneratedWorkflowInvokeConfig(rewritten);
        rewritten = rewriteGeneratedWorkspaceWindowSaveConfig(rewritten);
        rewritten = rewriteGeneratedUrlDotMethodPath(rewritten);
        rewritten = rewriteGeneratedRequestDataLiteral(rewritten);
        return rewritten;
    }

    private String rewriteGeneratedUrlDotMethodPath(String content) {
        String rewritten = defaultIfBlank(content, "");
        String previous;
        do {
            previous = rewritten;
            rewritten = JS_URL_DOT_METHOD_PATTERN.matcher(rewritten).replaceAll("$1$2/$3$2");
        } while (!StringUtils.equals(previous, rewritten));
        return rewritten;
    }

    private String rewriteGeneratedWorkflowInvokeConfig(String content) {
        String rewritten = defaultIfBlank(content, "");
        int searchStart = 0;
        while (searchStart >= 0 && searchStart < rewritten.length()) {
            int singleQuoteIndex = rewritten.indexOf("Gikam.create('workflow').", searchStart);
            int doubleQuoteIndex = rewritten.indexOf("Gikam.create(\"workflow\").", searchStart);
            int callIndex;
            if (singleQuoteIndex < 0) {
                callIndex = doubleQuoteIndex;
            } else if (doubleQuoteIndex < 0) {
                callIndex = singleQuoteIndex;
            } else {
                callIndex = Math.min(singleQuoteIndex, doubleQuoteIndex);
            }
            if (callIndex < 0) {
                break;
            }
            int leftParenthesisIndex = rewritten.indexOf('(', callIndex + "Gikam.create('workflow').".length());
            if (leftParenthesisIndex < 0) {
                break;
            }
            int objectStartIndex = findNextNonWhitespaceIndex(rewritten, leftParenthesisIndex + 1);
            if (objectStartIndex < 0 || rewritten.charAt(objectStartIndex) != '{') {
                searchStart = leftParenthesisIndex + 1;
                continue;
            }
            int objectEndIndex = findMatchingBracketIndex(rewritten, objectStartIndex, '{', '}');
            if (objectEndIndex < 0) {
                break;
            }
            String objectLiteral = rewritten.substring(objectStartIndex, objectEndIndex + 1);
            String convertedObjectLiteral = rewriteWorkflowInvokeObjectLiteral(objectLiteral);
            rewritten = rewritten.substring(0, objectStartIndex) + convertedObjectLiteral + rewritten.substring(objectEndIndex + 1);
            searchStart = objectStartIndex + convertedObjectLiteral.length();
        }
        return rewritten;
    }

    private String rewriteGeneratedWorkspaceWindowSaveConfig(String content) {
        String rewritten = defaultIfBlank(content, "");
        int searchStart = 0;
        while (searchStart >= 0 && searchStart < rewritten.length()) {
            int callIndex = rewritten.indexOf("workspace.window.save", searchStart);
            if (callIndex < 0) {
                break;
            }
            int leftParenthesisIndex = rewritten.indexOf('(', callIndex + "workspace.window.save".length());
            if (leftParenthesisIndex < 0) {
                break;
            }
            int objectStartIndex = findNextNonWhitespaceIndex(rewritten, leftParenthesisIndex + 1);
            if (objectStartIndex < 0 || rewritten.charAt(objectStartIndex) != '{') {
                searchStart = leftParenthesisIndex + 1;
                continue;
            }
            int objectEndIndex = findMatchingBracketIndex(rewritten, objectStartIndex, '{', '}');
            if (objectEndIndex < 0) {
                break;
            }
            String objectLiteral = rewritten.substring(objectStartIndex, objectEndIndex + 1);
            String convertedObjectLiteral = rewriteWorkspaceWindowSaveObjectLiteral(objectLiteral);
            rewritten = rewritten.substring(0, objectStartIndex) + convertedObjectLiteral + rewritten.substring(objectEndIndex + 1);
            searchStart = objectStartIndex + convertedObjectLiteral.length();
        }
        return rewritten;
    }

    private String rewriteWorkspaceWindowSaveObjectLiteral(String objectLiteral) {
        List<JsObjectProperty> propertyList = parseJsObjectPropertyList(objectLiteral);
        boolean hasUrl = propertyList.stream().anyMatch(property -> StringUtils.equals(property.name, "url"));
        if (hasUrl) {
            return objectLiteral;
        }
        if (StringUtils.isBlank(objectLiteral) || objectLiteral.length() < 2) {
            return "{ url : requestPath + '/manual-save' }";
        }
        String innerContent = objectLiteral.substring(1, objectLiteral.length() - 1);
        if (StringUtils.isBlank(innerContent)) {
            return "{ url : requestPath + '/manual-save' }";
        }
        return "{ url : requestPath + '/manual-save'," + innerContent + "}";
    }

    private String rewriteWorkflowInvokeObjectLiteral(String objectLiteral) {
        List<JsObjectProperty> propertyList = parseJsObjectPropertyList(objectLiteral);
        if (CollectionUtils.isEmpty(propertyList)) {
            return "{ isLcdpConverted : true }";
        }
        boolean exists = propertyList.stream().anyMatch(property -> StringUtils.equals(property.name, "isLcdpConverted"));
        if (exists) {
            return objectLiteral;
        }
        StringBuilder builder = new StringBuilder("{ isLcdpConverted : true");
        for (JsObjectProperty property : propertyList) {
            builder.append(", ").append(property.name).append(" : ").append(property.rawValue.trim());
        }
        builder.append(" }");
        return builder.toString();
    }

    private String rewriteGeneratedRequestDataLiteral(String content) {
        String rewritten = defaultIfBlank(content, "");
        rewritten = rewritten.replaceAll("\\bgetRequestData\\s*:", "requestData :");
        rewritten = rewritten.replaceAll(
                "requestData\\s*:\\s*\\(typeof\\s+([A-Za-z0-9_$]+)\\s*===\\s*'function'\\s*\\?\\s*\\1\\s*:\\s*function\\s*\\(\\)\\s*\\{\\s*\\}\\s*\\)",
                "requestData : (typeof $1 === 'function' ? $1() : {})");
        return rewritten;
    }

    private String rewriteGeneratedPreInsertConfig(ModuleContext context, String content, List<PageContext> pageContextList) {
        String rewritten = defaultIfBlank(content, "");
        int searchStart = 0;
        while (searchStart >= 0 && searchStart < rewritten.length()) {
            int callIndex = rewritten.indexOf("Gikam.preInsert", searchStart);
            if (callIndex < 0) {
                break;
            }
            int leftParenthesisIndex = rewritten.indexOf('(', callIndex);
            if (leftParenthesisIndex < 0) {
                break;
            }
            int objectStartIndex = findNextNonWhitespaceIndex(rewritten, leftParenthesisIndex + 1);
            if (objectStartIndex < 0 || rewritten.charAt(objectStartIndex) != '{') {
                searchStart = leftParenthesisIndex + 1;
                continue;
            }
            int objectEndIndex = findMatchingBracketIndex(rewritten, objectStartIndex, '{', '}');
            if (objectEndIndex < 0) {
                break;
            }
            String objectLiteral = rewritten.substring(objectStartIndex, objectEndIndex + 1);
            String convertedObjectLiteral = rewritePreInsertObjectLiteral(context, objectLiteral, pageContextList);
            rewritten = rewritten.substring(0, objectStartIndex) + convertedObjectLiteral + rewritten.substring(objectEndIndex + 1);
            searchStart = objectStartIndex + convertedObjectLiteral.length();
        }
        return rewritten;
    }

    private String rewritePreInsertObjectLiteral(ModuleContext context, String objectLiteral, List<PageContext> pageContextList) {
        List<JsObjectProperty> propertyList = parseJsObjectPropertyList(objectLiteral);
        if (CollectionUtils.isEmpty(propertyList)) {
            return objectLiteral;
        }
        String pagePath = null;
        String formId = null;
        List<String> fieldNameList = new ArrayList<>();
        for (JsObjectProperty property : propertyList) {
            if (StringUtils.equals(property.name, "page")) {
                pagePath = parseJsQuotedString(property.rawValue);
            } else if (StringUtils.equals(property.name, "formId")) {
                formId = parseJsQuotedString(property.rawValue);
            } else if (StringUtils.equals(property.name, "fields")) {
                fieldNameList = parseJsStringArray(property.rawValue);
            }
        }
        if (StringUtils.isBlank(pagePath) || StringUtils.isBlank(formId)) {
            return objectLiteral;
        }
        JSONArray preInsertFields = resolvePreInsertFields(context, pagePath, formId, fieldNameList, pageContextList);
        if (preInsertFields == null || preInsertFields.isEmpty()) {
            return objectLiteral;
        }

        String fieldsLiteral = toJsLiteral(preInsertFields);
        StringBuilder builder = new StringBuilder("{ ");
        boolean appended = false;
        boolean replacedFields = false;
        for (JsObjectProperty property : propertyList) {
            if (StringUtils.equals(property.name, "page") || StringUtils.equals(property.name, "formId")) {
                continue;
            }
            if (appended) {
                builder.append(", ");
            }
            builder.append(property.name).append(" : ");
            if (StringUtils.equals(property.name, "fields")) {
                builder.append(fieldsLiteral);
                replacedFields = true;
            } else {
                builder.append(property.rawValue.trim());
            }
            appended = true;
        }
        if (!replacedFields) {
            if (appended) {
                builder.append(", ");
            }
            builder.append("fields : ").append(fieldsLiteral);
        }
        builder.append(" }");
        return builder.toString();
    }

    private JSONArray resolvePreInsertFields(ModuleContext context, String pagePath, String formId, List<String> fieldNameList,
                                             List<PageContext> pageContextList) {
        if (CollectionUtils.isEmpty(pageContextList)) {
            return null;
        }
        PageContext targetPageContext = pageContextList.stream()
                .filter(item -> StringUtils.equals(item.pagePath, pagePath))
                .findFirst()
                .orElse(null);
        if (targetPageContext == null) {
            return null;
        }
        PageParseResult parseResult = parsePageContext(targetPageContext);
        normalizeGeneratedPageConfig(context, parseResult, pageContextList);
        JSONObject formConfig = findFormConfig(parseResult.components, formId);
        if (formConfig == null) {
            return null;
        }
        List<JSONObject> formFieldConfigList = collectFormFieldConfigList(formConfig);
        if (CollectionUtils.isEmpty(formFieldConfigList)) {
            return null;
        }

        Map<String, JSONObject> fieldConfigMap = new LinkedHashMap<>();
        for (JSONObject fieldConfig : formFieldConfigList) {
            String fieldName = fieldConfig.getString("field");
            if (StringUtils.isNotBlank(fieldName)) {
                fieldConfigMap.put(fieldName, fieldConfig);
            }
        }

        JSONArray result = new JSONArray();
        List<String> actualFieldNameList = CollectionUtils.isEmpty(fieldNameList)
                ? new ArrayList<>(fieldConfigMap.keySet()) : fieldNameList;
        for (String fieldName : actualFieldNameList) {
            JSONObject fieldConfig = fieldConfigMap.get(fieldName);
            if (fieldConfig == null) {
                continue;
            }
            JSONObject copiedFieldConfig = deepCopyJson(fieldConfig);
            copiedFieldConfig.put("preInsert", true);
            result.add(copiedFieldConfig);
        }
        return result;
    }

    private JSONObject findFormConfig(List<Object> componentList, String formId) {
        if (CollectionUtils.isEmpty(componentList)) {
            return null;
        }
        for (Object component : componentList) {
            JSONObject formConfig = findFormConfig(component, formId);
            if (formConfig != null) {
                return formConfig;
            }
        }
        return null;
    }

    private JSONObject findFormConfig(Object value, String formId) {
        if (value instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) value;
            if (StringUtils.equals("form", jsonObject.getString("type")) && StringUtils.equals(formId, jsonObject.getString("id"))) {
                return jsonObject;
            }
            for (Object nestedValue : jsonObject.values()) {
                JSONObject matched = findFormConfig(nestedValue, formId);
                if (matched != null) {
                    return matched;
                }
            }
            return null;
        }
        if (value instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) value;
            for (Object item : jsonArray) {
                JSONObject matched = findFormConfig(item, formId);
                if (matched != null) {
                    return matched;
                }
            }
        }
        return null;
    }

    private List<JSONObject> collectFormFieldConfigList(JSONObject formConfig) {
        List<JSONObject> fieldConfigList = new ArrayList<>();
        collectFieldConfigList(formConfig.getJSONArray("fields"), fieldConfigList);
        JSONArray panels = formConfig.getJSONArray("panels");
        if (panels != null) {
            for (Object panelObj : panels) {
                if (!(panelObj instanceof JSONObject)) {
                    continue;
                }
                collectFieldConfigList(((JSONObject) panelObj).getJSONArray("fields"), fieldConfigList);
            }
        }
        return fieldConfigList;
    }

    private void collectFieldConfigList(JSONArray fieldArray, List<JSONObject> fieldConfigList) {
        if (fieldArray == null) {
            return;
        }
        for (Object fieldObj : fieldArray) {
            if (fieldObj instanceof JSONObject) {
                fieldConfigList.add((JSONObject) fieldObj);
            }
        }
    }

    private List<JsObjectProperty> parseJsObjectPropertyList(String objectLiteral) {
        List<JsObjectProperty> propertyList = new ArrayList<>();
        if (StringUtils.isBlank(objectLiteral)) {
            return propertyList;
        }
        int index = findNextNonWhitespaceIndex(objectLiteral, 0);
        if (index < 0 || objectLiteral.charAt(index) != '{') {
            return propertyList;
        }
        index++;
        while (index < objectLiteral.length()) {
            index = skipJsWhitespaceAndComma(objectLiteral, index);
            if (index >= objectLiteral.length() || objectLiteral.charAt(index) == '}') {
                break;
            }
            int keyStartIndex = index;
            String propertyName;
            if (isQuotedChar(objectLiteral.charAt(index))) {
                int keyEndIndex = findStringLiteralEndIndex(objectLiteral, index);
                if (keyEndIndex < 0) {
                    break;
                }
                propertyName = parseJsQuotedString(objectLiteral.substring(index, keyEndIndex + 1));
                index = keyEndIndex + 1;
            } else {
                while (index < objectLiteral.length() && isJsPropertyNameChar(objectLiteral.charAt(index))) {
                    index++;
                }
                propertyName = objectLiteral.substring(keyStartIndex, index).trim();
            }
            if (StringUtils.isBlank(propertyName)) {
                break;
            }
            index = skipJsWhitespace(objectLiteral, index);
            if (index >= objectLiteral.length() || objectLiteral.charAt(index) != ':') {
                break;
            }
            index++;
            int valueStartIndex = index;
            int valueEndIndex = findJsPropertyValueEndIndex(objectLiteral, valueStartIndex);
            if (valueEndIndex < 0) {
                valueEndIndex = objectLiteral.length() - 1;
            }
            JsObjectProperty property = new JsObjectProperty();
            property.name = propertyName;
            property.rawValue = objectLiteral.substring(valueStartIndex, valueEndIndex).trim();
            propertyList.add(property);
            index = valueEndIndex;
            if (index < objectLiteral.length() && objectLiteral.charAt(index) == ',') {
                index++;
            }
        }
        return propertyList;
    }

    private String parseJsQuotedString(String rawValue) {
        if (StringUtils.isBlank(rawValue)) {
            return null;
        }
        String value = rawValue.trim();
        if (value.length() < 2 || !isQuotedChar(value.charAt(0)) || value.charAt(value.length() - 1) != value.charAt(0)) {
            return null;
        }
        return value.substring(1, value.length() - 1);
    }

    private List<String> parseJsStringArray(String rawValue) {
        if (StringUtils.isBlank(rawValue)) {
            return new ArrayList<>();
        }
        Matcher matcher = QUOTED_VALUE_PATTERN.matcher(rawValue);
        List<String> valueList = new ArrayList<>();
        while (matcher.find()) {
            String value = firstNotBlank(matcher.group(1), matcher.group(2));
            if (StringUtils.isNotBlank(value)) {
                valueList.add(value);
            }
        }
        return valueList;
    }

    private int findNextNonWhitespaceIndex(String content, int startIndex) {
        int index = Math.max(startIndex, 0);
        while (index < content.length() && Character.isWhitespace(content.charAt(index))) {
            index++;
        }
        return index >= content.length() ? -1 : index;
    }

    private int skipJsWhitespace(String content, int startIndex) {
        int index = Math.max(startIndex, 0);
        while (index < content.length() && Character.isWhitespace(content.charAt(index))) {
            index++;
        }
        return index;
    }

    private int skipJsWhitespaceAndComma(String content, int startIndex) {
        int index = Math.max(startIndex, 0);
        while (index < content.length()) {
            char current = content.charAt(index);
            if (Character.isWhitespace(current) || current == ',') {
                index++;
                continue;
            }
            break;
        }
        return index;
    }

    private boolean isQuotedChar(char value) {
        return value == '\'' || value == '"' || value == '`';
    }

    private boolean isJsPropertyNameChar(char value) {
        return Character.isLetterOrDigit(value) || value == '_' || value == '$';
    }

    private int findStringLiteralEndIndex(String content, int startIndex) {
        if (startIndex < 0 || startIndex >= content.length()) {
            return -1;
        }
        char quote = content.charAt(startIndex);
        if (!isQuotedChar(quote)) {
            return -1;
        }
        for (int index = startIndex + 1; index < content.length(); index++) {
            char current = content.charAt(index);
            if (current == '\\') {
                index++;
                continue;
            }
            if (current == quote) {
                return index;
            }
        }
        return -1;
    }

    private int findMatchingBracketIndex(String content, int startIndex, char openChar, char closeChar) {
        if (StringUtils.isBlank(content) || startIndex < 0 || startIndex >= content.length() || content.charAt(startIndex) != openChar) {
            return -1;
        }
        int depth = 0;
        for (int index = startIndex; index < content.length(); index++) {
            char current = content.charAt(index);
            if (isQuotedChar(current)) {
                index = findStringLiteralEndIndex(content, index);
                if (index < 0) {
                    return -1;
                }
                continue;
            }
            if (current == '/' && index + 1 < content.length()) {
                char next = content.charAt(index + 1);
                if (next == '/') {
                    index = skipLineComment(content, index + 2);
                    continue;
                }
                if (next == '*') {
                    index = skipBlockComment(content, index + 2);
                    continue;
                }
            }
            if (current == openChar) {
                depth++;
                continue;
            }
            if (current == closeChar) {
                depth--;
                if (depth == 0) {
                    return index;
                }
            }
        }
        return -1;
    }

    private int findJsPropertyValueEndIndex(String content, int startIndex) {
        int braceDepth = 0;
        int bracketDepth = 0;
        int parenthesisDepth = 0;
        for (int index = startIndex; index < content.length(); index++) {
            char current = content.charAt(index);
            if (isQuotedChar(current)) {
                index = findStringLiteralEndIndex(content, index);
                if (index < 0) {
                    return -1;
                }
                continue;
            }
            if (current == '/' && index + 1 < content.length()) {
                char next = content.charAt(index + 1);
                if (next == '/') {
                    index = skipLineComment(content, index + 2);
                    continue;
                }
                if (next == '*') {
                    index = skipBlockComment(content, index + 2);
                    continue;
                }
            }
            if (current == '{') {
                braceDepth++;
                continue;
            }
            if (current == '[') {
                bracketDepth++;
                continue;
            }
            if (current == '(') {
                parenthesisDepth++;
                continue;
            }
            if (current == '}') {
                if (braceDepth == 0 && bracketDepth == 0 && parenthesisDepth == 0) {
                    return index;
                }
                braceDepth--;
                continue;
            }
            if (current == ']') {
                bracketDepth--;
                continue;
            }
            if (current == ')') {
                parenthesisDepth--;
                continue;
            }
            if (current == ',' && braceDepth == 0 && bracketDepth == 0 && parenthesisDepth == 0) {
                return index;
            }
        }
        return -1;
    }

    private int skipLineComment(String content, int startIndex) {
        int index = Math.max(startIndex, 0);
        while (index < content.length() && content.charAt(index) != '\n') {
            index++;
        }
        return index;
    }

    private int skipBlockComment(String content, int startIndex) {
        int index = Math.max(startIndex, 0);
        while (index + 1 < content.length()) {
            if (content.charAt(index) == '*' && content.charAt(index + 1) == '/') {
                return index + 1;
            }
            index++;
        }
        return content.length() - 1;
    }

    private String toJsLiteral(Object value) {
        StringBuilder builder = new StringBuilder();
        appendJsValue(builder, value, 0);
        return builder.toString();
    }

    private String rewriteGeneratedServiceMethodConcat(ModuleContext context, String content) {
        Pattern pattern = Pattern.compile("(['\"])([A-Za-z0-9_$]+(?:\\.[A-Za-z0-9_$]+)*\\.server\\.[A-Za-z0-9_$]+)\\1\\s*\\+\\s*(['\"])\\.([A-Za-z0-9_$]+)\\3");
        Matcher matcher = pattern.matcher(content);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String replacement = buildGeneratedServiceMethodUrlExpression(context, matcher.group(2) + "." + matcher.group(4));
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String rewriteGeneratedServiceBaseVar(ModuleContext context, String content) {
        Pattern pattern = Pattern.compile("((?:var|let|const)\\s+[A-Za-z_$][A-Za-z0-9_$]*\\s*=\\s*)(['\"])([A-Za-z0-9_$]+(?:\\.[A-Za-z0-9_$]+)*\\.server\\.[A-Za-z0-9_$]+)\\2(\\s*;)");
        Matcher matcher = pattern.matcher(content);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String replacement = matcher.group(1) + buildGeneratedServiceBaseUrlExpression(context, matcher.group(3)) + matcher.group(4);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String rewriteGeneratedBaseUrlConcat(String content) {
        Pattern pattern = Pattern.compile("(\\bbaseUrl\\s*\\+\\s*['\"])\\.([^'\"]+)(['\"])");
        Matcher matcher = pattern.matcher(content);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String replacement = matcher.group(1) + "/" + matcher.group(2) + matcher.group(3);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String rewriteGeneratedServiceMethodLiteral(ModuleContext context, String content) {
        Pattern pattern = Pattern.compile("(['\"])([A-Za-z0-9_$]+(?:\\.[A-Za-z0-9_$]+)*\\.server\\.[A-Za-z0-9_$]+\\.[A-Za-z0-9_$]+)\\1");
        Matcher matcher = pattern.matcher(content);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String replacement = buildGeneratedServiceMethodUrlExpression(context, matcher.group(2));
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String rewriteGeneratedValidatorLiteral(ModuleContext context, String content) {
        Pattern pattern = Pattern.compile("(['\"])(unique|remote)\\[([A-Za-z0-9_$]+(?:\\.[A-Za-z0-9_$]+)*\\.server\\.[A-Za-z0-9_$]+\\.[A-Za-z0-9_$]+)]\\1");
        Matcher matcher = pattern.matcher(content);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String replacement = buildGeneratedValidatorRuleExpression(context, matcher.group(2), matcher.group(3));
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String rewriteGeneratedPageUrlLiteral(String content, List<PageContext> pageContextList) {
        String rewritten = content;
        for (PageContext pageContext : pageContextList) {
            Pattern pattern = Pattern.compile("(\\burl\\s*:\\s*)(['\"])" + Pattern.quote(pageContext.pagePath) + "\\2");
            Matcher matcher = pattern.matcher(rewritten);
            StringBuffer buffer = new StringBuffer();
            while (matcher.find()) {
                String replacement = matcher.group(1) + buildGeneratedPageUrlExpression(pageContext);
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(buffer);
            rewritten = buffer.toString();
        }
        return rewritten;
    }

    private String rewriteGeneratedCurrentWindowCall(String content) {
        String rewritten = defaultIfBlank(content, "");
        rewritten = rewritten.replaceAll("\\bworkspace\\.window\\.loadPage\\s*\\(", "CURRENT_WINDOW.loadPage(");
        rewritten = rewritten.replaceAll("\\bworkspace\\.window\\.goBack\\s*\\(", "CURRENT_WINDOW.goBack(");
        return rewritten;
    }

    private void appendStandalonePageHelpers(StringBuilder builder, PageJsRuntimeUsage runtimeUsage) {
        appendCurrentWindowHelper(builder);
        builder.append("\n");
        if (runtimeUsage.useGetPageUrlHelper) {
            builder.append("    function getPageUrl(pagePath, pageParam) {\n");
            builder.append("        var url = (pagePathMap || {})[pagePath] || pagePath;\n");
            builder.append("        if (!pageParam) {\n");
            builder.append("            return url;\n");
            builder.append("        }\n");
            builder.append("        return url + Gikam.param(pageParam);\n");
            builder.append("    }\n");
            if (runtimeUsage.hasHelperAfterGetPageUrl()) {
                builder.append("\n");
            }
        }
        if (runtimeUsage.useLoadPageHelper) {
            builder.append("    function loadPage(pagePath, pageParam) {\n");
            builder.append("        var currentWindow = getCurrentWindow();\n");
            builder.append("        var pageUrl = getPageUrl(pagePath, pageParam);\n");
            builder.append("        if (currentWindow && currentWindow.load) {\n");
            builder.append("            currentWindow.load(pageUrl);\n");
            builder.append("            return;\n");
            builder.append("        }\n");
            builder.append("        if (window.workspace && workspace.window && workspace.window.load) {\n");
            builder.append("            workspace.window.load(pageUrl);\n");
            builder.append("            return;\n");
            builder.append("        }\n");
            builder.append("        if (currentWindow && currentWindow.loadPage) {\n");
            builder.append("            currentWindow.loadPage(pagePath, pageParam);\n");
            builder.append("        }\n");
            builder.append("    }\n");
            if (runtimeUsage.hasHelperAfterLoadPage()) {
                builder.append("\n");
            }
        }
        if (runtimeUsage.useOpenPageHelper) {
            builder.append("    function openPage(pagePath, pageParam, options) {\n");
            builder.append("        var pageUrl = getPageUrl(pagePath, pageParam);\n");
            builder.append("        if (Gikam.isFunction(Gikam.create)) {\n");
            builder.append("            Gikam.create('modal', Object.assign({ url : pageUrl }, options || {}));\n");
            builder.append("        }\n");
            builder.append("    }\n");
            if (runtimeUsage.hasHelperAfterOpenPage()) {
                builder.append("\n");
            }
        }
        if (runtimeUsage.useInvokeLcdpMethodHelper) {
            builder.append("    function invokeLcdpMethod(servicePath, methodName, args, extParam, invokeUrl) {\n");
            builder.append("        var wrapper = Gikam.getJsonWrapper(Object.assign({ args : args || [] }, extParam || {}));\n");
            builder.append("        var actionUrl = invokeUrl || getInvokeLcdpMethodUrl(servicePath, methodName);\n");
            builder.append("        return Gikam.post(actionUrl, wrapper);\n");
            builder.append("    }\n");
            if (runtimeUsage.useRequireHelper || runtimeUsage.useCurrentWindow) {
                builder.append("\n");
            }
        }
        if (runtimeUsage.useRequireHelper) {
            builder.append("    var cacheRequire = new Map();\n");
            builder.append("    function require(url) {\n");
            builder.append("        if (cacheRequire.get(url)) {\n");
            builder.append("            return cacheRequire.get(url).exports;\n");
            builder.append("        }\n");
            builder.append("        var module = { exports : {} };\n");
            builder.append("        var script = '';\n");
            builder.append("        if (Gikam.isLcdpPath && Gikam.isLcdpPath(url)) {\n");
            builder.append("            var resourceUrl = Gikam.getContextUrl('/secure/core/module/lcdp/resources/search-by-path');\n");
            builder.append("            var json = Gikam.getJsonWrapper({ resourcePath : url });\n");
            builder.append("            Gikam.postSync(resourceUrl, json).done(function(resource) {\n");
            builder.append("                script = resource.content;\n");
            builder.append("            });\n");
            builder.append("        } else {\n");
            builder.append("            Gikam.getTextSync(url).done(function(content) {\n");
            builder.append("                script = content;\n");
            builder.append("            });\n");
            builder.append("        }\n");
            builder.append("        new Function('module', script)(module);\n");
            builder.append("        cacheRequire.set(url, module);\n");
            builder.append("        return module.exports;\n");
            builder.append("    }\n");
            if (runtimeUsage.useCurrentWindow) {
                builder.append("\n");
            }
        }
        if (runtimeUsage.useCurrentWindow) {
            builder.append("    var CURRENT_WINDOW = {\n");
            builder.append("        getPageParam : function() {\n");
            builder.append("            return resolvePageParam();\n");
            if (runtimeUsage.currentWindowHasLoadPage || runtimeUsage.currentWindowHasOpenPage || runtimeUsage.currentWindowHasGoBack) {
                builder.append("        },\n");
            } else {
                builder.append("        }\n");
            }
            if (runtimeUsage.currentWindowHasLoadPage) {
                builder.append("        loadPage : function(pagePath, pageParam) {\n");
                builder.append("            loadPage(pagePath, pageParam);\n");
                if (runtimeUsage.currentWindowHasOpenPage || runtimeUsage.currentWindowHasGoBack) {
                    builder.append("        },\n");
                } else {
                    builder.append("        }\n");
                }
            }
            if (runtimeUsage.currentWindowHasOpenPage) {
                builder.append("        openPage : function(pagePath, pageParam, options) {\n");
                builder.append("            openPage(pagePath, pageParam, options);\n");
                if (runtimeUsage.currentWindowHasGoBack) {
                    builder.append("        },\n");
                } else {
                    builder.append("        }\n");
                }
            }
            if (runtimeUsage.currentWindowHasGoBack) {
                builder.append("        goBack : function() {\n");
                builder.append("            var currentWindow = getCurrentWindow();\n");
                builder.append("            if (currentWindow && currentWindow.goBack) {\n");
                builder.append("                return currentWindow.goBack.apply(currentWindow, arguments);\n");
                builder.append("            }\n");
                builder.append("            var deferred = Gikam.getDeferred ? Gikam.getDeferred() : null;\n");
                builder.append("            if (deferred && deferred.resolve) {\n");
                builder.append("                deferred.resolve();\n");
                builder.append("                return deferred;\n");
                builder.append("            }\n");
                builder.append("            return {\n");
                builder.append("                done : function(callback) {\n");
                builder.append("                    if (callback) {\n");
                builder.append("                        callback();\n");
                builder.append("                    }\n");
                builder.append("                    return this;\n");
                builder.append("                },\n");
                builder.append("                fail : function() {\n");
                builder.append("                    return this;\n");
                builder.append("                },\n");
                builder.append("                always : function(callback) {\n");
                builder.append("                    if (callback) {\n");
                builder.append("                        callback();\n");
                builder.append("                    }\n");
                builder.append("                    return this;\n");
                builder.append("                }\n");
                builder.append("            };\n");
                builder.append("        }\n");
            }
            builder.append("    };\n");
        }
    }

    private void appendCurrentWindowHelper(StringBuilder builder) {
        builder.append("    function hasUsablePageParam(param) {\n");
        builder.append("        return !!(param && (typeof param !== 'object' || Array.isArray(param) || Object.keys(param).length > 0));\n");
        builder.append("    }\n");
        builder.append("\n");
        builder.append("    function hasUsablePageConfig(config) {\n");
        builder.append("        return !!(config && typeof config === 'object' && !Array.isArray(config));\n");
        builder.append("    }\n");
        builder.append("\n");
        builder.append("    function getCurrentWindow() {\n");
        builder.append("        var modalWindow = Gikam.getLastModal && Gikam.getLastModal() ? Gikam.getLastModal().window : null;\n");
        builder.append("        if (modalWindow && modalWindow.getPageParam && hasUsablePageParam(modalWindow.getPageParam())) {\n");
        builder.append("            return modalWindow;\n");
        builder.append("        }\n");
        builder.append("        if (window.workspace && workspace.window) {\n");
        builder.append("            return workspace.window;\n");
        builder.append("        }\n");
        builder.append("        if (modalWindow) {\n");
        builder.append("            return modalWindow;\n");
        builder.append("        }\n");
        builder.append("        return {\n");
        builder.append("            getPageParam : function() {\n");
        builder.append("                return initPageParam || {};\n");
        builder.append("            }\n");
        builder.append("        };\n");
        builder.append("    }\n");
        builder.append("\n");
        builder.append("    function resolvePageParam() {\n");
        builder.append("        var currentWindow = getCurrentWindow();\n");
        builder.append("        if (currentWindow && currentWindow.getPageParam) {\n");
        builder.append("            var pageParam = currentWindow.getPageParam();\n");
        builder.append("            if (hasUsablePageParam(pageParam)) {\n");
        builder.append("                return pageParam;\n");
        builder.append("            }\n");
        builder.append("        }\n");
        builder.append("        return initPageParam || {};\n");
        builder.append("    }\n");
        builder.append("\n");
        builder.append("    function syncPageParamReference(nextPageParam) {\n");
        builder.append("        [typeof pageParam !== 'undefined' ? pageParam : null, typeof param !== 'undefined' ? param : null].forEach(function(target) {\n");
        builder.append("            if (!target || typeof target !== 'object' || Array.isArray(target)) {\n");
        builder.append("                return;\n");
        builder.append("            }\n");
        builder.append("            Object.keys(target).forEach(function(key) {\n");
        builder.append("                delete target[key];\n");
        builder.append("            });\n");
        builder.append("            if (nextPageParam && typeof nextPageParam === 'object' && !Array.isArray(nextPageParam)) {\n");
        builder.append("                Object.keys(nextPageParam).forEach(function(key) {\n");
        builder.append("                    target[key] = nextPageParam[key];\n");
        builder.append("                });\n");
        builder.append("            }\n");
        builder.append("        });\n");
        builder.append("    }\n");
        builder.append("\n");
        builder.append("    function normalizeGeneratedRequestData(config) {\n");
        builder.append("        if (!config) {\n");
        builder.append("            return config;\n");
        builder.append("        }\n");
        builder.append("        if (Array.isArray(config)) {\n");
        builder.append("            config.forEach(function(item) {\n");
        builder.append("                normalizeGeneratedRequestData(item);\n");
        builder.append("            });\n");
        builder.append("            return config;\n");
        builder.append("        }\n");
        builder.append("        if (typeof config !== 'object') {\n");
        builder.append("            return config;\n");
        builder.append("        }\n");
        builder.append("        if (typeof config.requestData === 'function') {\n");
        builder.append("            config.requestData = config.requestData();\n");
        builder.append("        }\n");
        builder.append("        if (config.getRequestData !== undefined && config.requestData === undefined) {\n");
        builder.append("            config.requestData = typeof config.getRequestData === 'function' ? config.getRequestData() : config.getRequestData;\n");
        builder.append("            delete config.getRequestData;\n");
        builder.append("        }\n");
        builder.append("        Object.keys(config).forEach(function(key) {\n");
        builder.append("            normalizeGeneratedRequestData(config[key]);\n");
        builder.append("        });\n");
        builder.append("        return config;\n");
        builder.append("    }\n");
    }

    private String cleanupGeneratedViewContent(String viewContent) {
        if (StringUtils.isBlank(viewContent)) {
            return "";
        }
        String normalized = viewContent.replace("\r\n", "\n").replace('\r', '\n');
        List<String> lines = new ArrayList<>(Arrays.asList(normalized.split("\n", -1)));
        while (!lines.isEmpty() && StringUtils.isBlank(lines.get(0))) {
            lines.remove(0);
        }
        while (!lines.isEmpty() && StringUtils.isBlank(lines.get(lines.size() - 1))) {
            lines.remove(lines.size() - 1);
        }
        List<String> filteredLines = removeUnusedViewVariableLines(lines);
        StringBuilder builder = new StringBuilder();
        boolean prevBlank = false;
        for (String line : filteredLines) {
            String trimmedRight = line.replaceFirst("\\s+$", "");
            boolean blank = StringUtils.isBlank(trimmedRight);
            if (blank) {
                if (prevBlank) {
                    continue;
                }
                prevBlank = true;
            } else {
                prevBlank = false;
            }
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(trimmedRight);
        }
        return builder.toString();
    }

    private List<String> removeUnusedViewVariableLines(List<String> lines) {
        List<String> result = new ArrayList<>(lines);
        boolean removed;
        do {
            removed = false;
            String content = String.join("\n", result);
            for (int i = 0; i < result.size(); i++) {
                Matcher matcher = Pattern.compile("^\\s*(?:var|let|const)\\s+([A-Za-z_$][A-Za-z0-9_$]*)\\s*=.*;\\s*$").matcher(result.get(i));
                if (!matcher.matches()) {
                    continue;
                }
                String name = matcher.group(1);
                String withoutLine = joinLinesWithoutIndex(result, i);
                if (!Pattern.compile("\\b" + Pattern.quote(name) + "\\b").matcher(withoutLine).find()) {
                    result.remove(i);
                    removed = true;
                    break;
                }
            }
        } while (removed);
        return result;
    }

    private String joinLinesWithoutIndex(List<String> lines, int removeIndex) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i == removeIndex) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(lines.get(i));
        }
        return builder.toString();
    }

    private PageJsRuntimeUsage analyzePageJsRuntimeUsage(String pageVarName, String viewContent, List<PageContext> pageContextList) {
        PageJsRuntimeUsage usage = new PageJsRuntimeUsage();
        String content = defaultIfBlank(viewContent, "");
        usage.useRequireHelper = containsScriptCall(content, "require");
        usage.useInvokeLcdpMethodHelper = containsScriptCall(content, "invokeLcdpMethod");
        usage.useCurrentWindow = content.contains("CURRENT_WINDOW.");
        usage.currentWindowHasLoadPage = content.contains("CURRENT_WINDOW.loadPage");
        usage.currentWindowHasOpenPage = content.contains("CURRENT_WINDOW.openPage");
        usage.currentWindowHasGoBack = content.contains("CURRENT_WINDOW.goBack");
        usage.useLoadPageHelper = usage.currentWindowHasLoadPage || containsScriptCall(content, "loadPage");
        usage.useOpenPageHelper = usage.currentWindowHasOpenPage || containsScriptCall(content, "openPage");
        usage.useGetPageUrlHelper = containsScriptCall(content, "getPageUrl") || usage.useLoadPageHelper || usage.useOpenPageHelper;
        for (PageContext item : pageContextList) {
            if (StringUtils.contains(content, item.pagePath)) {
                usage.referencedPagePathSet.add(item.pagePath);
            }
        }
        if (usage.referencedPagePathSet.isEmpty() && usage.useGetPageUrlHelper) {
            usage.referencedPagePathSet.addAll(pageContextList.stream().map(item -> item.pagePath).collect(Collectors.toList()));
        }
        return usage;
    }

    private boolean containsScriptCall(String content, String functionName) {
        return Pattern.compile("(^|[^\\w$.])" + Pattern.quote(functionName) + "\\s*\\(", Pattern.MULTILINE).matcher(defaultIfBlank(content, "")).find();
    }

    private void appendIndentedScript(StringBuilder builder, String content, int indentLevel) {
        if (StringUtils.isBlank(content)) {
            return;
        }
        String[] lines = content.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        for (String line : lines) {
            if (StringUtils.isBlank(line)) {
                builder.append("\n");
                continue;
            }
            indent(builder, indentLevel);
            builder.append(line).append("\n");
        }
    }

    private PageParseResult parsePageContext(PageContext pageContext) {
        List<PageCompNode> nodeList = new ArrayList<>();
        Map<String, PageCompNode> nodeMap = new LinkedHashMap<>();
        Map<String, PageCompNode> configIdMap = new LinkedHashMap<>();
        for (LcdpModulePageCompBean comp : pageContext.compList) {
            PageCompNode node = new PageCompNode();
            node.id = comp.getId();
            node.parentId = comp.getParentId();
            node.type = comp.getType();
            node.config = parseConfig(comp);
            parseLocaleForGeneratedCode(node.config, pageContext.pageI18nMap, pageContext.pagePath, node.config.getString("id"));
            formatterFunctionForGeneratedCode(node.config);
            nodeList.add(node);
            nodeMap.put(node.id, node);
            if (StringUtils.isNotBlank(node.config.getString("id"))) {
                configIdMap.put(node.config.getString("id"), node);
            }
        }
        parseDependentWidgetForGeneratedCode(configIdMap);

        List<PageCompNode> rootNodeList = nodeList.stream()
                .filter(node -> StringUtils.isBlank(node.parentId))
                .collect(Collectors.toList());
        PageParseResult result = new PageParseResult();
        for (PageCompNode node : rootNodeList) {
            if (StringUtils.equalsIgnoreCase("WindowToolbar", node.type)) {
                result.windowToolbar = parseWindowToolbarForGeneratedCode(node, nodeMap);
                continue;
            }
            result.components.add(parseWidgetForGeneratedCode(node, nodeMap));
        }
        return result;
    }

    private Object parseWidgetForGeneratedCode(PageCompNode node, Map<String, PageCompNode> nodeMap) {
        if (node == null) {
            return null;
        }
        switch (ObjectUtils.toString(node.type)) {
            case "Layout":
                return parseLayoutForGeneratedCode(node, nodeMap);
            case "Grid":
            case "TreeGrid":
                return parseGridForGeneratedCode(node, nodeMap);
            case "Form":
                return parseFormForGeneratedCode(node, nodeMap);
            case "Tab":
                return parseTabForGeneratedCode(node, nodeMap);
            case "TabPanel":
                return parseTabPanelForGeneratedCode(node, nodeMap);
            case "DropdownMenu":
                return parseDropdownMenuForGeneratedCode(node, nodeMap);
            case "ButtonToolbar":
                return parseButtonToolbarForGeneratedCode(node, nodeMap);
            case "ButtonGroup":
                return parseButtonGroupForGeneratedCode(node, nodeMap);
            case "ShuttleFrame":
                return parseShuttleFrameForGeneratedCode(node, nodeMap);
            default:
                return parseNormalForGeneratedCode(node);
        }
    }

    private JSONObject parseNormalForGeneratedCode(PageCompNode node) {
        JSONObject config = deepCopyJson(node.config);
        config.put("type", toInitialLowerCase(ObjectUtils.toString(node.type)));
        cleanupGeneratedConfig(config);
        return config;
    }

    private Object parseGridColumnForGeneratedCode(PageCompNode node) {
        JSONObject config = deepCopyJson(node.config);
        if (Boolean.TRUE.equals(config.getBoolean("checkbox"))) {
            JSONObject checkboxColumn = new JSONObject(true);
            checkboxColumn.put("checkbox", true);
            return checkboxColumn;
        }
        if (Boolean.TRUE.equals(config.getBoolean("index"))) {
            JSONObject indexColumn = new JSONObject(true);
            indexColumn.put("index", true);
            return indexColumn;
        }
        if (StringUtils.equalsIgnoreCase(config.getString("type"), "processStatus")
                || StringUtils.equalsIgnoreCase(config.getString("gridColumnType"), "processStatus")) {
            String dbTable = config.getString("dbTable");
            if (StringUtils.isNotBlank(dbTable)) {
                return new RawJs(buildGeneratedBpmnColumnExpression(dbTable, config.getString("param"), config.get("width")));
            }
        }
        config.remove("id");
        cleanupGeneratedConfig(config);
        return config;
    }

    private String buildGeneratedBpmnColumnExpression(String dbTable, String param, Object width) {
        StringBuilder builder = new StringBuilder("Gikam.status.getBpmnColumn(");
        builder.append(JSON.toJSONString(dbTable));
        List<String> paramList = Arrays.stream((StringUtils.isBlank(param) ? "" : param).split(","))
                .map(StringUtils::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        if (!paramList.isEmpty()) {
            builder.append(", ").append(JSON.toJSONString(paramList.get(0)));
            if (width instanceof Number) {
                builder.append(", ").append(width);
            }
        }
        builder.append(")");
        return builder.toString();
    }

    private JSONObject parseDropdownMenuForGeneratedCode(PageCompNode node, Map<String, PageCompNode> nodeMap) {
        JSONObject config = deepCopyJson(node.config);
        JSONArray childIds = config.getJSONArray("childrenWidgetId");
        JSONArray items = new JSONArray();
        if (childIds != null) {
            for (Object childId : childIds) {
                items.add(parseNormalForGeneratedCode(nodeMap.get(ObjectUtils.toString(childId))));
            }
        }
        config.put("items", items);
        config.put("type", toInitialLowerCase(ObjectUtils.toString(node.type)));
        cleanupGeneratedConfig(config);
        return config;
    }

    private JSONObject parseButtonToolbarForGeneratedCode(PageCompNode node, Map<String, PageCompNode> nodeMap) {
        JSONObject config = deepCopyJson(node.config);
        config.put("type", toInitialLowerCase(ObjectUtils.toString(node.type)));
        config.put("items", parseChildrenToArray(config.getJSONArray("childrenWidgetId"), nodeMap));
        cleanupGeneratedConfig(config);
        return config;
    }

    private JSONObject parseButtonGroupForGeneratedCode(PageCompNode node, Map<String, PageCompNode> nodeMap) {
        JSONObject config = deepCopyJson(node.config);
        config.put("type", toInitialLowerCase(ObjectUtils.toString(node.type)));
        config.put("items", parseChildrenToArray(config.getJSONArray("childrenWidgetId"), nodeMap));
        if (StringUtils.isBlank(config.getString("field"))) {
            config.put("field", String.valueOf(System.currentTimeMillis()));
        }
        cleanupGeneratedConfig(config);
        return config;
    }

    private JSONObject parseWindowToolbarForGeneratedCode(PageCompNode node, Map<String, PageCompNode> nodeMap) {
        JSONObject config = deepCopyJson(node.config);
        config.put("items", parseChildrenToArray(config.getJSONArray("childrenWidgetId"), nodeMap));
        cleanupGeneratedConfig(config);
        return config;
    }

    private JSONObject parseShuttleFrameForGeneratedCode(PageCompNode node, Map<String, PageCompNode> nodeMap) {
        JSONObject config = deepCopyJson(node.config);
        config.put("type", toInitialLowerCase(ObjectUtils.toString(node.type)));
        for (String key : new ArrayList<>(config.keySet())) {
            if (!key.endsWith("_childrenWidgetId")) {
                continue;
            }
            JSONArray childIds = config.getJSONArray(key);
            JSONArray items = new JSONArray();
            if (childIds != null) {
                for (Object childId : childIds) {
                    PageCompNode childNode = nodeMap.get(ObjectUtils.toString(childId));
                    if (childNode == null) {
                        continue;
                    }
                    if (StringUtils.equals(key, "leftToolbar_childrenWidgetId") || StringUtils.equals(key, "rightToolbar_childrenWidgetId")) {
                        JSONArray toolbarIds = childNode.config.getJSONArray("childrenWidgetId");
                        if (toolbarIds != null) {
                            for (Object toolbarId : toolbarIds) {
                                items.add(parseWidgetForGeneratedCode(nodeMap.get(ObjectUtils.toString(toolbarId)), nodeMap));
                            }
                        }
                    } else {
                        items.add(parseWidgetForGeneratedCode(childNode, nodeMap));
                    }
                }
            }
            config.put(StringUtils.removeEnd(key, "_childrenWidgetId"), items);
            config.remove(key);
        }
        cleanupGeneratedConfig(config);
        return config;
    }

    private JSONObject parseGridForGeneratedCode(PageCompNode node, Map<String, PageCompNode> nodeMap) {
        JSONObject config = deepCopyJson(node.config);
        config.put("type", toInitialLowerCase(ObjectUtils.toString(node.type)));
        config.put("createdByLcdp", true);
        config.remove("readonly");
        JSONArray columns = new JSONArray();
        JSONArray columnIds = config.getJSONArray("childrenWidgetId");
        if (columnIds != null) {
            for (Object childId : columnIds) {
                PageCompNode childNode = nodeMap.get(ObjectUtils.toString(childId));
                if (childNode != null) {
                    columns.add(parseGridColumnForGeneratedCode(childNode));
                }
            }
        }
        config.put("columns", columns);
        for (String key : new ArrayList<>(config.keySet())) {
            if (!key.endsWith("_childrenWidgetId")) {
                continue;
            }
            JSONArray childIds = config.getJSONArray(key);
            String realKey = StringUtils.removeEnd(key, "_childrenWidgetId");
            if (StringUtils.equals("searchPanelOptions_childrenWidgetId", key)) {
                Object parsed = null;
                if (childIds != null && !childIds.isEmpty()) {
                    parsed = parseWidgetForGeneratedCode(nodeMap.get(ObjectUtils.toString(childIds.get(0))), nodeMap);
                }
                config.put(realKey, parsed);
            } else {
                JSONArray items = new JSONArray();
                if (childIds != null) {
                    for (Object childId : childIds) {
                        PageCompNode childNode = nodeMap.get(ObjectUtils.toString(childId));
                        if (childNode == null) {
                            continue;
                        }
                        if (StringUtils.equals("toolbar_childrenWidgetId", key)) {
                            JSONArray toolbarIds = childNode.config.getJSONArray("childrenWidgetId");
                            if (toolbarIds != null) {
                                for (Object toolbarId : toolbarIds) {
                                    items.add(parseWidgetForGeneratedCode(nodeMap.get(ObjectUtils.toString(toolbarId)), nodeMap));
                                }
                            }
                        } else {
                            items.add(parseWidgetForGeneratedCode(childNode, nodeMap));
                        }
                    }
                }
                config.put(realKey, items);
            }
            config.remove(key);
        }
        cleanupGeneratedConfig(config);
        return config;
    }

    private JSONObject parseLayoutForGeneratedCode(PageCompNode node, Map<String, PageCompNode> nodeMap) {
        JSONObject config = deepCopyJson(node.config);
        config.put("type", toInitialLowerCase(ObjectUtils.toString(node.type)));
        JSONArray childIds = config.getJSONArray("childrenWidgetId");
        if (childIds != null) {
            for (Object childId : childIds) {
                PageCompNode childNode = nodeMap.get(ObjectUtils.toString(childId));
                if (childNode == null) {
                    continue;
                }
                String region = ObjectUtils.toString(childNode.type).toLowerCase(Locale.ENGLISH).replace("layout", "");
                JSONObject regionConfig = deepCopyJson(childNode.config);
                regionConfig.put("items", parseChildrenToArray(regionConfig.getJSONArray("childrenWidgetId"), nodeMap));
                cleanupGeneratedConfig(regionConfig);
                config.put(region, regionConfig);
            }
        }
        cleanupGeneratedConfig(config);
        return config;
    }

    private JSONObject parseFormForGeneratedCode(PageCompNode node, Map<String, PageCompNode> nodeMap) {
        JSONObject config = deepCopyJson(node.config);
        config.put("type", toInitialLowerCase(ObjectUtils.toString(node.type)));
        config.put("isLcdpConverted", true);
        config.put("createdByLcdp", true);
        config.remove("panels");
        JSONArray panels = new JSONArray();
        JSONArray fields = new JSONArray();
        JSONArray childIds = config.getJSONArray("childrenWidgetId");
        if (childIds != null) {
            for (Object childId : childIds) {
                PageCompNode childNode = nodeMap.get(ObjectUtils.toString(childId));
                if (childNode == null) {
                    continue;
                }
                if (StringUtils.equals("FormPanel", childNode.type)) {
                    panels.add(parseFormPanelForGeneratedCode(childNode, nodeMap));
                } else {
                    fields.add(parseWidgetForGeneratedCode(childNode, nodeMap));
                }
            }
        }
        if (panels.isEmpty()) {
            config.put("fields", fields);
        } else {
            if (!fields.isEmpty()) {
                JSONObject normalPanel = new JSONObject(true);
                normalPanel.put("fields", fields);
                panels.add(0, normalPanel);
            }
            config.put("panels", panels);
        }
        if (StringUtils.isNotBlank(config.getString("caption"))) {
            JSONObject caption = new JSONObject(true);
            caption.put("text", config.getString("caption"));
            config.put("caption", caption);
        }
        cleanupGeneratedConfig(config);
        return config;
    }

    private JSONObject parseFormPanelForGeneratedCode(PageCompNode node, Map<String, PageCompNode> nodeMap) {
        JSONObject config = deepCopyJson(node.config);
        config.put("fields", parseChildrenToArray(config.getJSONArray("childrenWidgetId"), nodeMap));
        if (StringUtils.isNotBlank(config.getString("caption"))) {
            JSONObject caption = new JSONObject(true);
            caption.put("text", config.getString("caption"));
            config.put("caption", caption);
        }
        cleanupGeneratedConfig(config);
        return config;
    }

    private JSONObject parseTabForGeneratedCode(PageCompNode node, Map<String, PageCompNode> nodeMap) {
        JSONObject config = deepCopyJson(node.config);
        config.put("type", toInitialLowerCase(ObjectUtils.toString(node.type)));
        config.put("panels", parseChildrenToArray(config.getJSONArray("childrenWidgetId"), nodeMap));
        cleanupGeneratedConfig(config);
        return config;
    }

    private JSONObject parseTabPanelForGeneratedCode(PageCompNode node, Map<String, PageCompNode> nodeMap) {
        JSONObject config = deepCopyJson(node.config);
        config.put("items", parseChildrenToArray(config.getJSONArray("childrenWidgetId"), nodeMap));
        cleanupGeneratedConfig(config);
        return config;
    }

    private JSONArray parseChildrenToArray(JSONArray childIds, Map<String, PageCompNode> nodeMap) {
        JSONArray items = new JSONArray();
        if (childIds == null) {
            return items;
        }
        for (Object childId : childIds) {
            PageCompNode childNode = nodeMap.get(ObjectUtils.toString(childId));
            if (childNode != null) {
                items.add(parseWidgetForGeneratedCode(childNode, nodeMap));
            }
        }
        return items;
    }

    private void parseDependentWidgetForGeneratedCode(Map<String, PageCompNode> configIdMap) {
        Map<String, List<String>> dependentMap = new LinkedHashMap<>();
        for (PageCompNode node : configIdMap.values()) {
            JSONObject config = node.config;
            String configId = config.getString("id");
            String dependentWidgetId = config.getString("dependentWidgetId");
            if (StringUtils.isNotBlank(dependentWidgetId) && StringUtils.isNotBlank(configId)) {
                dependentMap.computeIfAbsent(dependentWidgetId, key -> new ArrayList<>()).add(configId);
                config.put("$dependentWidgetId", dependentWidgetId);
            }
            JSONArray childrenOptions = config.getJSONArray("childrenOptions");
            if (childrenOptions != null && !childrenOptions.isEmpty()) {
                JSONArray cascadeChildren = config.getJSONArray("$cascadeChildrenId");
                if (cascadeChildren == null) {
                    cascadeChildren = new JSONArray();
                    config.put("$cascadeChildrenId", cascadeChildren);
                }
                for (Object itemObj : childrenOptions) {
                    JSONObject item = (JSONObject) itemObj;
                    PageCompNode childNode = configIdMap.get(item.getString("compId"));
                    if (childNode == null) {
                        continue;
                    }
                    JSONObject childConfig = childNode.config;
                    childConfig.put("$dependentWidgetId", configId);
                    childConfig.put("$dependentQueryParams", item.get("requestParams"));
                    cascadeChildren.add(childConfig.getString("id"));
                }
            }
        }
        for (Map.Entry<String, List<String>> entry : dependentMap.entrySet()) {
            PageCompNode node = configIdMap.get(entry.getKey());
            if (node == null) {
                continue;
            }
            JSONArray cascadeChildren = node.config.getJSONArray("$cascadeChildrenId");
            if (cascadeChildren == null) {
                cascadeChildren = new JSONArray();
                node.config.put("$cascadeChildrenId", cascadeChildren);
            }
            cascadeChildren.addAll(entry.getValue());
        }
    }

    private void parseLocaleForGeneratedCode(Object configObject, Map<String, Map<String, String>> pageI18nMap, String pagePath,
                                             String currentComponentId) {
        if (configObject instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) configObject;
            String effectiveComponentId = firstNotBlank(jsonObject.getString("id"), currentComponentId);
            for (String key : new ArrayList<>(jsonObject.keySet())) {
                Object value = jsonObject.get(key);
                if (value instanceof JSONObject) {
                    JSONObject valueObject = (JSONObject) value;
                    if (valueObject.containsKey("zh-CN") || StringUtils.equals("i18n", valueObject.getString("type"))) {
                        String i18nCode = valueObject.getString("i18nCode");
                        if (StringUtils.isBlank(i18nCode)) {
                            if (isBlankGeneratedI18nValue(valueObject)) {
                                jsonObject.remove(key);
                            }
                            continue;
                        }
                        String message = firstNotBlank(resolveI18nMessage(i18nCode, pageI18nMap),
                                valueObject.getString("zh-CN"), valueObject.getString("en-US"), i18nCode);
                        jsonObject.put(key, new RawJs(buildGeneratedI18nExpression(i18nCode, message)));
                    } else {
                        parseLocaleForGeneratedCode(valueObject, pageI18nMap, pagePath, effectiveComponentId);
                    }
                } else if (isBlankGeneratedI18nValue(value)) {
                    jsonObject.remove(key);
                } else if (value instanceof JSONArray) {
                    parseLocaleForGeneratedCode(value, pageI18nMap, pagePath, effectiveComponentId);
                }
            }
        } else if (configObject instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) configObject;
            for (Object item : jsonArray) {
                parseLocaleForGeneratedCode(item, pageI18nMap, pagePath, currentComponentId);
            }
        }
    }

    private boolean isBlankGeneratedI18nValue(Object value) {
        if (value instanceof JSONObject) {
            return isBlankGeneratedI18nObject((JSONObject) value);
        }
        if (value instanceof String) {
            String text = ObjectUtils.toString(value).trim();
            if (!StringUtils.startsWith(text, "{") || !StringUtils.endsWith(text, "}")) {
                return false;
            }
            try {
                return isBlankGeneratedI18nObject(JSON.parseObject(text));
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private boolean isBlankGeneratedI18nObject(JSONObject valueObject) {
        if (valueObject == null) {
            return false;
        }
        if (!valueObject.containsKey("zh-CN") && !valueObject.containsKey("en-US")) {
            return false;
        }
        return StringUtils.isBlank(valueObject.getString("i18nCode"))
                && StringUtils.isBlank(valueObject.getString("zh-CN"))
                && StringUtils.isBlank(valueObject.getString("en-US"));
    }

    private String buildGeneratedI18nExpression(String i18nCode, String fallbackMessage) {
        String expression = "Gikam.propI18N(" + JSON.toJSONString(i18nCode) + ")";
        return "(" + expression + " || \"\")";
    }

    private void formatterFunctionForGeneratedCode(Object configObject) {
        if (configObject instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) configObject;
            for (String key : new ArrayList<>(jsonObject.keySet())) {
                Object value = jsonObject.get(key);
                if (key.endsWith("__$S$__")) {
                    String realKey = StringUtils.removeEnd(key, "__$S$__");
                    String functionExpression = ObjectUtils.toString(value);
                    if (StringUtils.isNotBlank(functionExpression)) {
                        jsonObject.put(realKey, new RawJs(buildGeneratedFunctionReferenceExpression(functionExpression)));
                    }
                    jsonObject.remove(key);
                    continue;
                }
                formatterFunctionForGeneratedCode(value);
            }
        } else if (configObject instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) configObject;
            for (Object item : jsonArray) {
                formatterFunctionForGeneratedCode(item);
            }
        }
    }

    private String buildGeneratedFunctionReferenceExpression(String functionExpression) {
        String trimmedExpression = defaultIfBlank(functionExpression, "").trim();
        if (SIMPLE_JS_IDENTIFIER.matcher(trimmedExpression).matches()) {
            return "(typeof " + trimmedExpression + " === 'function' ? " + trimmedExpression + " : function() {})";
        }
        return trimmedExpression;
    }

    private JSONObject deepCopyJson(JSONObject jsonObject) {
        JSONObject copy = new JSONObject(true);
        if (jsonObject == null) {
            return copy;
        }
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            copy.put(entry.getKey(), deepCopyValue(entry.getValue()));
        }
        return copy;
    }

    private Object deepCopyValue(Object value) {
        if (value instanceof JSONObject) {
            return deepCopyJson((JSONObject) value);
        }
        if (value instanceof JSONArray) {
            JSONArray copy = new JSONArray();
            for (Object item : (JSONArray) value) {
                copy.add(deepCopyValue(item));
            }
            return copy;
        }
        if (value instanceof RawJs) {
            return new RawJs(((RawJs) value).expression);
        }
        return value;
    }

    private void cleanupGeneratedConfig(JSONObject config) {
        if (config == null) {
            return;
        }
        if (StringUtils.equals("uploader", config.getString("type"))) {
            config.remove("server");
            config.remove("searchUrl");
            config.remove("deleteUrl");
        }
        for (String key : new ArrayList<>(config.keySet())) {
            if (StringUtils.equals("uuid", key)
                    || StringUtils.equals("createdByLcdp", key)
                    || key.startsWith("$_")) {
                config.remove(key);
                continue;
            }
            if (StringUtils.equals("childrenWidgetId", key) || key.endsWith("_childrenWidgetId")) {
                config.remove(key);
                continue;
            }
            Object value = config.get(key);
            if (StringUtils.equals("lcdpVisible", key) && Boolean.TRUE.equals(value)) {
                config.remove(key);
                continue;
            }
            if (value instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) value;
                cleanupGeneratedConfig(jsonObject);
                if (jsonObject.isEmpty()) {
                    config.remove(key);
                }
            } else if (value instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) value;
                for (Object item : jsonArray) {
                    if (item instanceof JSONObject) {
                        cleanupGeneratedConfig((JSONObject) item);
                    }
                }
                if (isMeaninglessArray(key, jsonArray)) {
                    config.remove(key);
                }
            } else if (value instanceof String && shouldRemoveBlankString(key, (String) value)) {
                config.remove(key);
            }
        }
    }

    private boolean isMeaninglessArray(String key, JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.isEmpty()) {
            return Arrays.asList("validators", "visibleRoles", "editRoles", "children").contains(key);
        }
        if (!StringUtils.equals("children", key)) {
            return false;
        }
        for (Object item : jsonArray) {
            if (!(item instanceof JSONObject) || !((JSONObject) item).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean shouldRemoveBlankString(String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            return false;
        }
        return Arrays.asList("autoCompleteUrl", "customDataSource", "textField", "textTargetField", "valueField", "value").contains(key);
    }

    private void appendJsValue(StringBuilder builder, Object value, int indentLevel) {
        if (value instanceof RawJs) {
            String expression = ((RawJs) value).expression;
            if (expression.startsWith("IFM_CONTEXT +")) {
                builder.append(expression);
            } else {
                builder.append(expression);
            }
            return;
        }
        if (value == null) {
            builder.append("null");
            return;
        }
        if (value instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) value;
            builder.append("{");
            if (!jsonObject.isEmpty()) {
                builder.append("\n");
                int index = 0;
                for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                    indent(builder, indentLevel + 1);
                    builder.append(formatJsKey(entry.getKey())).append(" : ");
                    appendJsValue(builder, entry.getValue(), indentLevel + 1);
                    if (index++ < jsonObject.size() - 1) {
                        builder.append(",");
                    }
                    builder.append("\n");
                }
                indent(builder, indentLevel);
            }
            builder.append("}");
            return;
        }
        if (value instanceof Map) {
            JSONObject jsonObject = new JSONObject(true);
            ((Map<?, ?>) value).forEach((key, item) -> jsonObject.put(ObjectUtils.toString(key), item));
            appendJsValue(builder, jsonObject, indentLevel);
            return;
        }
        if (value instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) value;
            builder.append("[");
            if (!jsonArray.isEmpty()) {
                builder.append("\n");
                for (int i = 0; i < jsonArray.size(); i++) {
                    indent(builder, indentLevel + 1);
                    appendJsValue(builder, jsonArray.get(i), indentLevel + 1);
                    if (i < jsonArray.size() - 1) {
                        builder.append(",");
                    }
                    builder.append("\n");
                }
                indent(builder, indentLevel);
            }
            builder.append("]");
            return;
        }
        if (value instanceof List) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll((List<?>) value);
            appendJsValue(builder, jsonArray, indentLevel);
            return;
        }
        if (value instanceof Number || value instanceof Boolean) {
            builder.append(value);
            return;
        }
        builder.append(JSON.toJSONString(value));
    }

    private String formatJsKey(String key) {
        if (key.matches("[A-Za-z_$][A-Za-z0-9_$]*")) {
            return key;
        }
        return JSON.toJSONString(key);
    }

    private void indent(StringBuilder builder, int indentLevel) {
        for (int i = 0; i < indentLevel; i++) {
            builder.append("    ");
        }
    }

    private String renderMapperXml(ModuleContext context, GeneratedMapperSpec spec, String rawContent) {
        String content = rewriteLegacyModulePackageReferences(ObjectUtils.toString(rawContent), context);
        if (StringUtils.isBlank(content)) {
            return content;
        }
        Matcher matcher = MAPPER_NAMESPACE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.replaceFirst("<mapper namespace=\"" + Matcher.quoteReplacement(spec.namespace) + "\"");
        }
        return content;
    }

    private String renderGridColumns(List<GridColumnMeta> columns) {
        StringBuilder builder = new StringBuilder("[ ");
        for (int i = 0; i < columns.size(); i++) {
            GridColumnMeta column = columns.get(i);
            if (i > 0) {
                builder.append(", ");
            }
            builder.append("{ ");
            if (column.checkbox) {
                builder.append("checkbox : true");
            } else if (column.index) {
                builder.append("index : true");
            } else {
                builder.append("field : '").append(column.field).append("', ");
                builder.append("title : '").append(escapeJs(column.title)).append("'");
                if (column.type != null) {
                    builder.append(", type : '").append(column.type).append("'");
                }
                if (column.width != null) {
                    builder.append(", width : ").append(column.width);
                }
            }
            builder.append(" }");
        }
        builder.append(" ]");
        return builder.toString();
    }

    private String renderFormFields(List<FormFieldMeta> fields) {
        StringBuilder builder = new StringBuilder("[ ");
        for (int i = 0; i < fields.size(); i++) {
            FormFieldMeta field = fields.get(i);
            if (i > 0) {
                builder.append(", ");
            }
            builder.append("{ ");
            builder.append("type : '").append(field.type).append("', ");
            builder.append("field : '").append(field.field).append("', ");
            builder.append("title : '").append(escapeJs(field.title)).append("'");
            if (field.readonly) {
                builder.append(", readonly : true");
            }
            builder.append(" }");
        }
        builder.append(" ]");
        return builder.toString();
    }

    private String renderInsertFields(List<String> insertFields, List<FormFieldMeta> formFields, List<GridColumnMeta> gridColumns) {
        List<String> actualFields = insertFields;
        if (CollectionUtils.isEmpty(actualFields) && !CollectionUtils.isEmpty(formFields)) {
            actualFields = Collections.singletonList(formFields.get(0).field);
        }
        StringBuilder builder = new StringBuilder("[ ");
        for (int i = 0; i < actualFields.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append("{ ");
            builder.append("type : 'text', ");
            builder.append("field : '").append(actualFields.get(i)).append("', ");
            builder.append("title : '").append(escapeJs(resolveFieldTitle(actualFields.get(i), formFields, gridColumns))).append("'");
            builder.append(" }");
        }
        builder.append(" ]");
        return builder.toString();
    }

    private String resolveFieldTitle(String fieldName, List<FormFieldMeta> formFields, List<GridColumnMeta> gridColumns) {
        String title = formFields.stream().filter(field -> StringUtils.equals(field.field, fieldName)).map(field -> field.title).findFirst().orElse(null);
        if (StringUtils.isNotBlank(title)) {
            return title;
        }
        title = gridColumns.stream().filter(column -> StringUtils.equals(column.field, fieldName)).map(column -> column.title).findFirst().orElse(null);
        return StringUtils.isNotBlank(title) ? title : fieldName;
    }

    private void appendField(StringBuilder builder, FieldMeta field) {
        if (field.id) {
            builder.append("    @Id\n");
        }
        if (field.clob) {
            builder.append("    @Clob\n");
        }
        if (LocalType.LOCAL_DATE == localTypeOf(field.type)) {
            builder.append("    @JSONField(format = \"yyyy-MM-dd\")\n");
            builder.append("    @DateTimeFormat(pattern = \"yyyy-MM-dd\")\n");
        } else if (LocalType.LOCAL_DATE_TIME == localTypeOf(field.type)) {
            builder.append("    @JSONField(format = \"yyyy-MM-dd\")\n");
            builder.append("    @DateTimeFormat(pattern = \"yyyy-MM-dd HH:mm:ss\")\n");
        }
        builder.append("    private ").append(getSimpleTypeName(field.type)).append(" ").append(field.propertyName).append(";\n\n");
    }

    private void appendGetterSetter(StringBuilder builder, FieldMeta field) {
        String methodSuffix = StringUtils.capitalize(field.propertyName);
        if (field.id) {
            builder.append("    @Override\n");
        }
        builder.append("    public ").append(getSimpleTypeName(field.type)).append(" get").append(methodSuffix).append("() {\n");
        builder.append("        return ").append(field.propertyName).append(";\n");
        builder.append("    }\n\n");
        if (field.id) {
            builder.append("    @Override\n");
        }
        builder.append("    public void set").append(methodSuffix).append("(").append(getSimpleTypeName(field.type)).append(" ").append(field.propertyName).append(") {\n");
        builder.append("        this.").append(field.propertyName).append(" = ").append(field.propertyName).append(";\n");
        builder.append("    }\n\n");
    }

    private void collectFieldImports(FieldMeta field, Set<String> imports) {
        if (field.clob) {
            imports.add("com.sunwayworld.framework.data.annotation.Clob");
        }
        if (LocalType.LOCAL_DATE == localTypeOf(field.type) || LocalType.LOCAL_DATE_TIME == localTypeOf(field.type)) {
            imports.add("com.alibaba.fastjson.annotation.JSONField");
            imports.add("org.springframework.format.annotation.DateTimeFormat");
        }
        if (field.type == null) {
            return;
        }
        String typeName = field.type.getName();
        if (typeName.startsWith("java.time.") || typeName.startsWith("java.math.")) {
            imports.add(typeName);
        }
    }

    private boolean isInheritedField(BeanBaseType baseType, FieldMeta field) {
        String columnName = field.columnName.toLowerCase(Locale.ENGLISH);
        if (baseType == BeanBaseType.AUDITABLE) {
            return Arrays.asList("createdbyid", "createdbyname", "createdtime", "createdbyorgid", "createdbyorgname", "processstatus", "version").contains(columnName);
        }
        if (baseType == BeanBaseType.INSERTABLE) {
            return Arrays.asList("createdbyid", "createdbyname", "createdtime", "createdbyorgid", "createdbyorgname").contains(columnName);
        }
        return false;
    }

    private JSONObject parseConfig(LcdpModulePageCompBean comp) {
        String config = ObjectUtils.toString(comp.getConfig());
        return JSON.parseObject(StringUtils.isBlank(config) ? "{}" : config);
    }

    private Map<String, Map<String, String>> loadPageI18n(LcdpResourceHistoryBean viewHistory) {
        if (viewHistory == null || viewHistory.getId() == null) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, String>> pageI18nMap = lcdpModulePageI18nService.selectPageI18nMessage(viewHistory.getId());
        return pageI18nMap == null ? Collections.emptyMap() : pageI18nMap;
    }

    private void syncPageI18nToCoreI18n(List<PageContext> pageContextList, List<String> warnings) {
        if (CollectionUtils.isEmpty(pageContextList)) {
            return;
        }

        List<CoreI18nConfigBean> coreI18nConfigList = coreI18nConfigService.selectAll();
        if (CollectionUtils.isEmpty(coreI18nConfigList)) {
            warnings.add("核心国际化配置为空，页面国际化未同步到核心国际化表");
            return;
        }

        Map<String, String> localeConfigMap = new LinkedHashMap<>();
        for (CoreI18nConfigBean config : coreI18nConfigList) {
            if (config != null && StringUtils.isNotBlank(config.getId())) {
                localeConfigMap.put(config.getId(), config.getId());
            }
        }
        if (localeConfigMap.isEmpty()) {
            warnings.add("核心国际化配置为空，页面国际化未同步到核心国际化表");
            return;
        }

        Map<String, Map<String, String>> mergedI18nMap = new LinkedHashMap<>();
        Map<String, String> sourcePageMap = new LinkedHashMap<>();
        for (PageContext pageContext : pageContextList) {
            mergePageI18nToCoreMap(pageContext, mergedI18nMap, sourcePageMap, warnings);
        }
        if (mergedI18nMap.isEmpty()) {
            return;
        }

        List<String> codeList = new ArrayList<>(mergedI18nMap.keySet());
        List<CoreI18nBean> existingCoreI18nList = coreI18nService.selectListByFilter(SearchFilter.instance()
                .match("CODE", codeList).filter(MatchPattern.OR));
        Map<String, CoreI18nBean> coreI18nByCode = new LinkedHashMap<>();
        if (!CollectionUtils.isEmpty(existingCoreI18nList)) {
            for (CoreI18nBean coreI18n : existingCoreI18nList) {
                if (coreI18n != null && StringUtils.isNotBlank(coreI18n.getCode())) {
                    coreI18nByCode.put(coreI18n.getCode(), coreI18n);
                }
            }
        }

        List<CoreI18nBean> insertCoreI18nList = new ArrayList<>();
        List<CoreI18nBean> updateCoreI18nList = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> entry : mergedI18nMap.entrySet()) {
            String code = entry.getKey();
            String defaultMessage = resolveCoreDefaultMessage(entry.getValue(), code);
            CoreI18nBean coreI18n = coreI18nByCode.get(code);
            if (coreI18n == null) {
                coreI18n = new CoreI18nBean();
                coreI18n.setId(ApplicationContextHelper.getNextIdentity());
                coreI18n.setCode(code);
                coreI18n.setDescription(defaultMessage);
                coreI18n.setDefaultMessage(defaultMessage);
                insertCoreI18nList.add(coreI18n);
                coreI18nByCode.put(code, coreI18n);
                continue;
            }

            boolean changed = false;
            if (StringUtils.isBlank(coreI18n.getDescription()) && StringUtils.isNotBlank(defaultMessage)) {
                coreI18n.setDescription(defaultMessage);
                changed = true;
            }
            if (StringUtils.isBlank(coreI18n.getDefaultMessage()) && StringUtils.isNotBlank(defaultMessage)) {
                coreI18n.setDefaultMessage(defaultMessage);
                changed = true;
            }
            if (changed) {
                updateCoreI18nList.add(coreI18n);
            }
        }
        if (!insertCoreI18nList.isEmpty()) {
            coreI18nService.getDao().insert(insertCoreI18nList);
        }
        if (!updateCoreI18nList.isEmpty()) {
            coreI18nService.updateIfChanged(updateCoreI18nList);
        }

        List<Long> i18nIdList = new ArrayList<>();
        for (CoreI18nBean coreI18n : coreI18nByCode.values()) {
            if (coreI18n != null && coreI18n.getId() != null) {
                i18nIdList.add(coreI18n.getId());
            }
        }

        List<CoreI18nMessageBean> existingMessageList = i18nIdList.isEmpty()
                ? new ArrayList<>()
                : coreI18nMessageService.selectListByFilter(SearchFilter.instance()
                .match("I18NID", i18nIdList).filter(MatchPattern.OR));
        Map<String, CoreI18nMessageBean> existingMessageMap = new LinkedHashMap<>();
        for (CoreI18nMessageBean message : existingMessageList) {
            if (message != null && message.getI18nId() != null && StringUtils.isNotBlank(message.getI18nConfigId())) {
                existingMessageMap.put(buildCoreI18nMessageKey(message.getI18nId(), message.getI18nConfigId()), message);
            }
        }

        List<CoreI18nMessageBean> insertMessageList = new ArrayList<>();
        List<CoreI18nMessageBean> updateMessageList = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> entry : mergedI18nMap.entrySet()) {
            CoreI18nBean coreI18n = coreI18nByCode.get(entry.getKey());
            if (coreI18n == null || coreI18n.getId() == null) {
                continue;
            }

            for (Map.Entry<String, String> localeEntry : entry.getValue().entrySet()) {
                String localeId = localeEntry.getKey();
                String message = localeEntry.getValue();
                if (StringUtils.isBlank(localeId) || StringUtils.isBlank(message)) {
                    continue;
                }
                if (!localeConfigMap.containsKey(localeId)) {
                    warnings.add("页面国际化语言配置不存在，已跳过：code=" + entry.getKey() + ", localeId=" + localeId);
                    continue;
                }

                String messageKey = buildCoreI18nMessageKey(coreI18n.getId(), localeId);
                CoreI18nMessageBean coreI18nMessage = existingMessageMap.get(messageKey);
                if (coreI18nMessage == null) {
                    coreI18nMessage = new CoreI18nMessageBean();
                    coreI18nMessage.setId(ApplicationContextHelper.getNextIdentity());
                    coreI18nMessage.setI18nId(coreI18n.getId());
                    coreI18nMessage.setI18nConfigId(localeId);
                    coreI18nMessage.setMessage(message);
                    insertMessageList.add(coreI18nMessage);
                    existingMessageMap.put(messageKey, coreI18nMessage);
                    continue;
                }

                if (!StringUtils.equals(coreI18nMessage.getMessage(), message)) {
                    coreI18nMessage.setMessage(message);
                    updateMessageList.add(coreI18nMessage);
                }
            }
        }
        if (!insertMessageList.isEmpty()) {
            coreI18nMessageService.getDao().insert(insertMessageList);
        }
        if (!updateMessageList.isEmpty()) {
            coreI18nMessageService.updateIfChanged(updateMessageList);
        }
    }

    private void mergePageI18nToCoreMap(PageContext pageContext,
                                        Map<String, Map<String, String>> mergedI18nMap,
                                        Map<String, String> sourcePageMap,
                                        List<String> warnings) {
        if (pageContext == null || pageContext.pageI18nMap == null || pageContext.pageI18nMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Map<String, String>> entry : pageContext.pageI18nMap.entrySet()) {
            String code = entry.getKey();
            if (StringUtils.isBlank(code) || entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }

            Map<String, String> localeMessageMap = mergedI18nMap.computeIfAbsent(code, key -> new LinkedHashMap<>());
            for (Map.Entry<String, String> localeEntry : entry.getValue().entrySet()) {
                String localeId = localeEntry.getKey();
                String message = localeEntry.getValue();
                if (StringUtils.isBlank(localeId) || StringUtils.isBlank(message)) {
                    continue;
                }

                String oldMessage = localeMessageMap.get(localeId);
                String messageKey = code + "::" + localeId;
                if (StringUtils.isBlank(oldMessage)) {
                    localeMessageMap.put(localeId, message);
                    sourcePageMap.put(messageKey, pageContext.pagePath);
                    continue;
                }

                if (!StringUtils.equals(oldMessage, message)) {
                    warnings.add("页面国际化存在冲突，已保留首个值：code=" + code
                            + ", localeId=" + localeId
                            + ", keptPage=" + defaultIfBlank(sourcePageMap.get(messageKey), "")
                            + ", currentPage=" + defaultIfBlank(pageContext.pagePath, ""));
                }
            }
        }
    }

    private String resolveCoreDefaultMessage(Map<String, String> localeMessageMap, String code) {
        if (localeMessageMap == null || localeMessageMap.isEmpty()) {
            return code;
        }
        return firstNotBlank(localeMessageMap.get("zh-CN"), localeMessageMap.get("en-US"),
                localeMessageMap.values().stream().filter(StringUtils::isNotBlank).findFirst().orElse(null), code);
    }

    private String buildCoreI18nMessageKey(Long i18nId, String localeId) {
        return i18nId + "::" + localeId;
    }

    private String resolveFieldName(String rawField, Map<String, FieldMeta> fieldMetaByLowerColumn) {
        if (StringUtils.isBlank(rawField)) {
            return rawField;
        }
        FieldMeta meta = fieldMetaByLowerColumn.get(rawField.toLowerCase(Locale.ENGLISH));
        return meta == null ? toLowerCamel(rawField) : meta.propertyName;
    }

    private String resolveTitle(Object titleObject, Map<String, Map<String, String>> pageI18nMap) {
        if (titleObject == null) {
            return "";
        }
        if (titleObject instanceof String) {
            String title = ObjectUtils.toString(titleObject);
            return firstNotBlank(resolveI18nMessage(title, pageI18nMap), title);
        }
        JSONObject jsonObject = (JSONObject) titleObject;
        String i18nCode = jsonObject.getString("i18nCode");
        return firstNotBlank(resolveI18nMessage(i18nCode, pageI18nMap), jsonObject.getString("zh-CN"), jsonObject.getString("en-US"), i18nCode);
    }

    private String resolveI18nMessage(String i18nCode, Map<String, Map<String, String>> pageI18nMap) {
        if (StringUtils.isBlank(i18nCode) || pageI18nMap == null) {
            return null;
        }
        Map<String, String> localeMap = pageI18nMap.get(i18nCode);
        if (localeMap == null) {
            return null;
        }
        return firstNotBlank(localeMap.get("zh-CN"), localeMap.get("en-US"));
    }

    private String humanizeFieldTitle(String rawField) {
        String fieldName = toLowerCamel(rawField);
        if (StringUtils.isBlank(fieldName)) {
            return "";
        }
        return StringUtils.capitalize(fieldName.replaceAll("([a-z0-9])([A-Z])", "$1 $2"));
    }

    private String mapWidgetType(String widgetType, FieldMeta fieldMeta) {
        if (StringUtils.equalsIgnoreCase("Date", widgetType)) {
            return "date";
        }
        if (equalsAnyIgnoreCase(widgetType, "Textarea", "TextArea")) {
            return "textarea";
        }
        if (fieldMeta != null && (LocalType.LOCAL_DATE == localTypeOf(fieldMeta.type) || LocalType.LOCAL_DATE_TIME == localTypeOf(fieldMeta.type))) {
            return "date";
        }
        return "text";
    }

    private String detectMapperDialect(String resourceName) {
        String lower = ObjectUtils.toString(resourceName).toLowerCase(Locale.ENGLISH);
        if (lower.contains("oracle")) {
            return "oracle";
        }
        if (lower.contains("postgresql")) {
            return "postgresql";
        }
        if (lower.contains("sqlserver")) {
            return "sqlserver";
        }
        return "mysql";
    }

    private String renderIdAssignStatement(String beanName, Class<?> idType) {
        if (Long.class.equals(idType)) {
            return beanName + ".setId(ApplicationContextHelper.getNextIdentity());";
        }
        if (Integer.class.equals(idType)) {
            return beanName + ".setId(ApplicationContextHelper.getNextIdentity().intValue());";
        }
        if (String.class.equals(idType)) {
            return beanName + ".setId(StringUtils.randomUUID(16));";
        }
        throw new ApplicationRuntimeException("暂不支持的主键类型：" + idType.getName());
    }

    private void appendImports(StringBuilder builder, Set<String> imports) {
        List<String> sortedImports = new ArrayList<>(imports);
        Collections.sort(sortedImports);
        for (String item : sortedImports) {
            builder.append("import ").append(item).append(";\n");
        }
    }

    private String getSimpleTypeName(Class<?> type) {
        return type == null ? "String" : type.getSimpleName();
    }

    private String toUpperCamel(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        String normalized = value.replaceAll("[^A-Za-z0-9]+", " ");
        StringBuilder builder = new StringBuilder();
        for (String part : normalized.split("\\s+")) {
            if (StringUtils.isBlank(part)) {
                continue;
            }
            builder.append(StringUtils.capitalize(part.toLowerCase(Locale.ENGLISH)));
        }
        return builder.length() == 0 ? StringUtils.capitalize(value) : builder.toString();
    }

    private String toLowerCamel(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        if (value.equals(value.toUpperCase(Locale.ENGLISH))) {
            String lowerValue = value.toLowerCase(Locale.ENGLISH);
            if (lowerValue.contains("_")) {
                return joinCamelParts(Arrays.asList(lowerValue.split("_")));
            }
            return joinCamelParts(splitCompactIdentifier(lowerValue));
        }
        if (!value.contains("_")
                && value.matches("[A-Za-z0-9]+")
                && value.equals(value.toLowerCase(Locale.ENGLISH))) {
            return joinCamelParts(splitCompactIdentifier(value));
        }
        String normalized = value.replaceAll("([a-z0-9])([A-Z])", "$1 $2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2")
                .replaceAll("[^A-Za-z0-9]+", " ");
        String[] parts = normalized.split("\\s+");
        String camel = joinCamelParts(Arrays.asList(parts));
        return StringUtils.isBlank(camel) ? StringUtils.uncapitalize(value) : camel;
    }

    private String toRouteSegment(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        String normalized = value.replaceAll("([a-z0-9])([A-Z])", "$1-$2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1-$2")
                .replaceAll("[^A-Za-z0-9]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "")
                .toLowerCase(Locale.ENGLISH);
        return defaultIfBlank(normalized, value.toLowerCase(Locale.ENGLISH));
    }

    private String toPackageSegment(String value) {
        String routeSegment = toRouteSegment(value);
        if (StringUtils.isBlank(routeSegment)) {
            return routeSegment;
        }
        String packageSegment = routeSegment.replace("-", "").replaceAll("[^a-z0-9_]", "");
        if (StringUtils.isBlank(packageSegment)) {
            return null;
        }
        if (Character.isDigit(packageSegment.charAt(0))) {
            packageSegment = "p" + packageSegment;
        }
        return packageSegment;
    }

    private List<String> splitCompactIdentifier(String value) {
        String lower = value.toLowerCase(Locale.ENGLISH);
        List<String> parts = new ArrayList<>();
        StringBuilder unknown = new StringBuilder();
        int index = 0;
        while (index < lower.length()) {
            char current = lower.charAt(index);
            if (Character.isDigit(current)) {
                flushCompactPart(parts, unknown);
                parts.add(String.valueOf(current));
                index++;
                continue;
            }
            String matched = matchCompactToken(lower, index);
            if (matched != null) {
                flushCompactPart(parts, unknown);
                parts.add(matched);
                index += matched.length();
                continue;
            }
            unknown.append(current);
            index++;
        }
        flushCompactPart(parts, unknown);
        return parts.isEmpty() ? Collections.singletonList(lower) : parts;
    }

    private String matchCompactToken(String value, int start) {
        for (String token : COMPACT_IDENTIFIER_TOKENS) {
            if (value.startsWith(token, start)) {
                return token;
            }
        }
        return null;
    }

    private void flushCompactPart(List<String> parts, StringBuilder unknown) {
        if (unknown.length() == 0) {
            return;
        }
        parts.add(unknown.toString());
        unknown.setLength(0);
    }

    private String joinCamelParts(List<String> parts) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            String part = parts.get(i);
            if (StringUtils.isBlank(part)) {
                continue;
            }
            if (i == 0) {
                builder.append(part.toLowerCase(Locale.ENGLISH));
            } else {
                builder.append(StringUtils.capitalize(part.toLowerCase(Locale.ENGLISH)));
            }
        }
        return builder.toString();
    }

    private String escapeJs(String value) {
        return ObjectUtils.toString(value).replace("\\", "\\\\").replace("'", "\\'");
    }

    private String escapeJava(String value) {
        return ObjectUtils.toString(value).replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String toJsTemplateLiteral(String value) {
        return "`" + ObjectUtils.toString(value)
                .replace("\\", "\\\\")
                .replace("`", "\\`")
                .replace("${", "\\${") + "`";
    }

    private String toInitialLowerCase(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private String toKebabCase(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        return value.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase(Locale.ENGLISH);
    }

    private static String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.isBlank(value) ? defaultValue : value;
    }

    private static boolean equalsAnyIgnoreCase(String value, String... candidates) {
        if (value == null) {
            return false;
        }
        for (String candidate : candidates) {
            if (StringUtils.equalsIgnoreCase(value, candidate)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsAnyIgnoreCase(String value, String... candidates) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        for (String candidate : candidates) {
            if (StringUtils.containsIgnoreCase(value, candidate)) {
                return true;
            }
        }
        return false;
    }

    private String firstNotBlank(String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private LocalType localTypeOf(Class<?> type) {
        if (type == null) {
            return LocalType.NONE;
        }
        String name = type.getName();
        if ("java.time.LocalDate".equals(name)) {
            return LocalType.LOCAL_DATE;
        }
        if ("java.time.LocalDateTime".equals(name)) {
            return LocalType.LOCAL_DATE_TIME;
        }
        return LocalType.NONE;
    }

    private enum BeanBaseType {
        AUDITABLE("AbstractAuditable", "Auditable"),
        INSERTABLE("AbstractInsertable", "Insertable"),
        PERSISTABLE("AbstractPersistable", "Persistable");

        private final String parentSimpleName;
        private final String interfaceSimpleName;

        BeanBaseType(String parentSimpleName, String interfaceSimpleName) {
            this.parentSimpleName = parentSimpleName;
            this.interfaceSimpleName = interfaceSimpleName;
        }
    }

    private enum LocalType {
        NONE,
        LOCAL_DATE,
        LOCAL_DATE_TIME
    }

    private static class ConversionOptions {
        private String outputRoot;
        private String targetModule;
        private boolean overwrite;

        private static ConversionOptions of(RestJsonWrapperBean wrapper) {
            ConversionOptions options = new ConversionOptions();
            options.outputRoot = wrapper.getParamValue("outputRoot");
            options.targetModule = wrapper.getParamValue("targetModule");
            options.overwrite = equalsAnyIgnoreCase(wrapper.getParamValue("overwrite"), "true", "1", "yes");
            return options;
        }
    }

    private static class GeneratedFile {
        private final Path path;
        private final String content;

        private GeneratedFile(Path path, String content) {
            this.path = path;
            this.content = content;
        }
    }

    private static class RawJs {
        private final String expression;

        private RawJs(String expression) {
            this.expression = expression;
        }
    }

    private static class GeneratedPageJsContext {
        private final StringBuilder methodsBuilder = new StringBuilder();
        private final Set<String> usedMethodNames = new LinkedHashSet<>();
        private final List<String> rootComponentMethodNames = new ArrayList<>();
        private String windowToolbarMethodName;
    }

    private static class GeneratedPageJsMeta {
        private final List<String> rootComponentMethodNames = new ArrayList<>();
        private String windowToolbarMethodName;
        private String methodsContent = "";
    }

    private static class JsObjectProperty {
        private String name;
        private String rawValue;
    }

    private static class PageJsRuntimeUsage {
        private boolean useGetPageUrlHelper;
        private boolean useLoadPageHelper;
        private boolean useOpenPageHelper;
        private boolean useInvokeLcdpMethodHelper;
        private boolean useRequireHelper;
        private boolean useCurrentWindow;
        private boolean currentWindowHasLoadPage;
        private boolean currentWindowHasOpenPage;
        private boolean currentWindowHasGoBack;
        private final Set<String> referencedPagePathSet = new LinkedHashSet<>();

        private boolean usePagePathMap() {
            return useGetPageUrlHelper && !CollectionUtils.isEmpty(referencedPagePathSet);
        }

        private boolean hasAnyHelper() {
            return useGetPageUrlHelper || useLoadPageHelper || useOpenPageHelper
                    || useInvokeLcdpMethodHelper || useRequireHelper || useCurrentWindow;
        }

        private boolean hasHelperAfterCurrentWindow() {
            return useGetPageUrlHelper || useLoadPageHelper || useOpenPageHelper
                    || useInvokeLcdpMethodHelper || useRequireHelper || useCurrentWindow;
        }

        private boolean hasHelperAfterGetPageUrl() {
            return useLoadPageHelper || useOpenPageHelper || useInvokeLcdpMethodHelper || useRequireHelper || useCurrentWindow;
        }

        private boolean hasHelperAfterLoadPage() {
            return useOpenPageHelper || useInvokeLcdpMethodHelper || useRequireHelper || useCurrentWindow;
        }

        private boolean hasHelperAfterOpenPage() {
            return useInvokeLcdpMethodHelper || useRequireHelper || useCurrentWindow;
        }
    }

    private static class PageParseResult {
        private final List<Object> components = new ArrayList<>();
        private Object windowToolbar;
    }

    private static class PageCompNode {
        private String id;
        private String parentId;
        private String type;
        private JSONObject config;
    }

    private static class ModuleContext {
        private Long moduleId;
        private String moduleCode;
        private String moduleCategory;
        private String moduleName;
        private String modulePath;
        private String moduleRoutePath;
        private String moduleResourcePath;
        private String className;
        private String lowerCamelName;
        private String jsNamespace;
        private String basePackage;
        private String controllerPackage;
        private String requestPath;
        private String serviceBeanName;
        private String tableName;
        private BeanBaseType beanBaseType;
        private FieldMeta idField;
        private List<ColumnContext> columns;
        private List<FieldMeta> fieldMetaList;
        private Map<String, FieldMeta> fieldMetaByLowerColumn;
        private List<LcdpResourceBean> childResources;
        private Map<Long, LcdpResourceHistoryBean> effectiveHistoryMap;
        private Map<Long, ResourceConvertRecord> resourceConvertRecordMap;
        private List<Path> moduleGeneratedFiles;
        private Path reportFile;
        private List<LcdpResourceHistoryBean> javaHistoryList;
        private Map<String, LcdpResourceHistoryBean> javaHistoryByName;
        private Map<String, LcdpResourceHistoryBean> javaHistoryByPath;
        private List<LcdpResourceHistoryBean> mapperHistoryList;
        private List<LcdpResourceHistoryBean> viewHistoryList;
        private List<LcdpMethodMeta> lcdpMethodMetaList;
        private LcdpResourceHistoryBean primaryViewHistory;
        private Map<String, Map<String, String>> pageI18nMap;
        private String primaryServicePath;
        private LcdpResourceHistoryBean primaryJavaHistory;
    }

    private enum ResourceConvertStatus {
        PENDING,
        CONVERTED,
        SKIPPED
    }

    private static class ResourceConvertRecord {
        private Long resourceId;
        private String resourceName;
        private String resourceCategory;
        private String resourcePath;
        private ResourceConvertStatus status;
        private String reason;
        private final List<String> generatedFiles = new ArrayList<>();
    }

    private static class ModulePathMetadata {
        private String categoryName;
        private String moduleName;
        private String categoryRouteSegment;
        private String moduleRouteSegment;
        private String categoryPackageSegment;
        private String modulePackageSegment;
        private String basePackage;
        private String routePath;
    }

    private enum GeneratedLcdpResourceBaseType {
        METHOD,
        BASE,
        AUDIT
    }

    private static class GeneratedLcdpResourceSpec {
        private String serviceName;
        private String servicePath;
        private String servicePackage;
        private String serviceFieldName;
        private String resourceInterfaceName;
        private String resourceImplName;
        private String resourcePackage;
        private String resourceImplPackage;
        private String resourceRouteSegment;
        private String requestPath;
        private GeneratedLcdpResourceBaseType baseType;
        private List<LcdpMethodMeta> customMethodMetaList = new ArrayList<>();
    }

    private static class GeneratedMapperSpec {
        private String namespace;
        private String packageName;
        private String packagePath;
        private String mapperName;
    }

    private static class LcdpMethodMeta {
        private String servicePath;
        private String serviceName;
        private String methodName;
        private String mappingType;
        private String mappingDesc;
        private String returnTypeName;
        private List<String> parameterTypeNameList;
    }

    private static class FieldMeta {
        private String columnName;
        private String propertyName;
        private Class<?> type;
        private boolean clob;
        private boolean id;

        private boolean isId() {
            return id;
        }
    }

    private static class GridColumnMeta {
        private boolean checkbox;
        private boolean index;
        private String field;
        private String title;
        private String type;
        private Integer width;
    }

    private static class FormFieldMeta {
        private String field;
        private String title;
        private String type;
        private boolean readonly;
    }

    private static class PageContext {
        private Long pageResourceId;
        private String pageResourceName;
        private String pagePath;
        private String controllerPath;
        private String serviceRequestPath;
        private String pageObjectName;
        private String templateName;
        private String routePath;
        private String methodName;
        private String gridComponentType;
        private String gridId;
        private String formId;
        private Integer formColumns;
        private String formPanelTitle;
        private String primaryDisplayField;
        private String viewContent;
        private String widgetListJson;
        private String i18nJson;
        private boolean editListEntry;
        private LcdpModulePageCompBean grid;
        private LcdpModulePageCompBean form;
        private List<LcdpModulePageCompBean> compList;
        private List<GridColumnMeta> gridColumns;
        private List<FormFieldMeta> formFields;
        private List<String> insertFields;
        private Map<String, Map<String, String>> pageI18nMap;

        private boolean hasForm() {
            return form != null && !CollectionUtils.isEmpty(formFields);
        }
    }
}
