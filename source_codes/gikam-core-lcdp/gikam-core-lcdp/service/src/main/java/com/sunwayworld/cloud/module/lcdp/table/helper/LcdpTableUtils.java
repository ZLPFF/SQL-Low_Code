package com.sunwayworld.cloud.module.lcdp.table.helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.sunwayworld.cloud.module.lcdp.table.helper.FieldType.clob;
import static com.sunwayworld.cloud.module.lcdp.table.helper.FieldType.date;
import static com.sunwayworld.cloud.module.lcdp.table.helper.FieldType.number;
import static com.sunwayworld.cloud.module.lcdp.table.helper.FieldType.varchar;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpAnalysisResultDTO;
import com.sunwayworld.cloud.module.lcdp.support.LcdpDatasourceAspect;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableIndexBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableIndexDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpDatabaseService;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.tenant.TenantContext;
import com.sunwayworld.framework.utils.BeanUtils;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.ConvertUtils;
import com.sunwayworld.framework.utils.NumberUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;

/**
 * @author yangsz@sunway.com 2022-11-09
 */
public abstract class LcdpTableUtils {

    //统一为一个汉字占3个varchar
    private static final String cnCharacterPattren = "[\u4e00-\u9fa5]";

    //保留字:oracle 12c mysql8.0 sqlserver postgresql
    private static final String[] oracleReservedWords = {"SPATIAL", "PRINT", "ELSEIF", "ROWCOUNT", "CREATE", "PURGE", "REVERT", "TRIGGER", "ROWGUIDCOL", "BETWEEN", "CLOSE", "MINUS", "CONVERT", "DENY", "EXPLAIN", "HOUR_MINUTE", "DEALLOCATE", "INNER", "SUBTYPE", "EACH", "UPDATETEXT", "OPTIONALLY", "LEFT", "SETUSER", "SIZE", "BIGINT", "RESTRICT", "RELEASE", "WHERE", "SQLWARNING", "IDENTITY", "AS", "AT", "DATABASE", "FULLTEXT", "VARCHAR", "THEN", "CONDITION", "XOR", "FREETEXTTABLE", "KEY", "UNLOCK", "ALTER", "CALL", "WAITFOR", "INTO", "SET", "REPEAT", "MERGE", "EXCEPTION", "CONSTRAINT", "RLIKE", "NOCOMPRESS", "PRECISION", "TEXTSIZE", "ASC", "CRASH", "GROUP", "DELETE", "COLAUTH", "BY", "STRAIGHT_JOIN", "RESTORE", "CHARACTER", "DUAL", "TINYINT", "RESOURCE", "LONG", "OPENDATASOURCE", "INT1", "INTERVAL", "INT2", "PROCEDURE", "STATISTICS", "INT3", "INT4", "SECOND_MICROSECOND", "ANALYZE", "INT8", "CLUSTERED", "OPEN", "CONNECTION", "CONTINUE", "DIV", "DELAYED", "NO_WRITE_TO_BINLOG", "PERCENT", "VARBINARY", "TO", "UNION", "TRUNCATE", "CURSOR", "INDEXES", "ADD", "TRY_CONVERT", "LOOP", "HIGH_PRIORITY", "VIEW", "DESC", "LINES", "VIEWS", "LOW_PRIORITY", "LABEL", "FREETEXT", "INDEX", "CURRENT_TIME", "ESCAPED", "REPLACE", "INTEGER", "VARYING", "OFFSETS", "SQL_BIG_RESULT", "LONGTEXT", "FOR", "UNIQUE", "DATABASES", "TRAILING", "TABAUTH", "ITERATE", "PIVOT", "FULL", "CURRENT", "USING", "EXEC", "NOT", "SPECIFIC", "END", "MINUTE_SECOND", "End", "FLOAT8", "HAVING", "RAISERROR", "FLOAT4", "SQLSTATE", "RECONFIGURE", "UNDO", "SEMANTICSIMILARITYTABLE", "LOAD", "TERMINATED", "BINARY", "LOCALTIME", "CONTAINS", "DROP", "RETURN", "REGEXP", "SOME", "FOREIGN", "SCHEMA", "TSEQUAL", "SQL_SMALL_RESULT", "LEAVE", "COLUMNS", "OUTER", "RULE", "SHOW", "SHUTDOWN", "INFILE", "RENAME", "EXCLUSIVE", "MOD", "EXISTS", "IDENTIFIED", "MEDIUMINT", "INTERSECT", "OPENXML", "WITH", "ESCAPE", "OVER", "GRANT", "ERRLVL", "START", "FALSE", "LINEAR", "DEFAULT", "NOCHECK", "LOCK", "JOIN", "SHARE", "TRAN", "PLAN", "BULK", "LOCALTIMESTAMP", "SESSION_USER", "NULLIF", "TABLE", "WHEN", "BREAK", "ELSE", "CLUSTER", "IF", "TYPE", "SEMANTICKEYPHRASETABLE", "DAY_MINUTE", "LONGBLOB", "NATIONAL", "IN", "DISTINCT", "OPTION", "IS", "ENCLOSED", "FUNCTION", "NOWAIT", "LEADING", "MODIFIES", "ASENSITIVE", "CASE", "OUT", "OPTIMIZE", "FORCE", "TOP", "TINYBLOB", "OVERLAPS", "CHECK", "PUBLIC", "UNPIVOT", "READTEXT", "EXIT", "WRITETEXT", "DBCC", "OPENROWSET", "GOTO", "IDENTITY_INSERT", "RAID0", "NONCLUSTERED", "DAY_HOUR", "UNSIGNED", "CASCADE", "CHAR", "CONNECT", "TRANSACTION", "BEGIN", "IGNORE", "MEDIUMTEXT", "SYSTEM_USER", "TABLESAMPLE", "OFF", "WRITE", "IDENTITYCOL", "ORDER", "MIDDLEINT", "SQL_CALC_FOUND_ROWS", "USAGE", "RIGHT", "UPDATE", "VALUES", "DOUBLE", "SAVE", "FILE", "DISTRIBUTED", "REQUIRE", "FILLFACTOR", "FETCH", "NUMERIC", "STARTING", "REVOKE", "COLLATE", "COMPUTE", "DISTINCTROW", "USE", "COPY", "SQLEXCEPTION", "LINENO", "SELECT", "EXECUTE", "OUTFILE", "PROC", "UTC_DATE", "VARCHARACTER", "CHECKPOINT", "KILL", "AUTHORIZATION", "ALL", "CURRENT_USER", "BLOB", "Null", "MODE", "COLUMN", "DECIMAL", "FROM", "SEMANTICSIMILARITYDETAILSTABLE", "X509", "COMPRESS", "BACKUP", "MINUTE_MICROSECOND", "COALESCE", "HOLDLOCK", "SECURITYAUDIT", "TINYTEXT", "DESCRIBE", "INSENSITIVE", "BOTH", "HOUR_MICROSECOND", "WITHINGROUP", "NULL", "SENSITIVE", "DAY_MICROSECOND", "SEPARATOR", "TRUE", "EXCEPT", "UTC_TIME", "SQL", "READ", "UTC_TIMESTAMP", "CLUSTERS", "LIKE", "ZEROFILL", "AND", "REAL", "INSERT", "YEAR_MONTH", "CURRENT_DATE", "DISK", "DAY_SECOND", "RANGE", "INOUT", "FLOAT", "BROWSE", "SCHEMAS", "CURRENT_TIMESTAMP", "LIMIT", "ANY", "INT", "ROLLBACK", "NATURAL", "EXTERNAL", "DUMP", "OF", "KEYS", "CHANGE", "READS", "ON", "OPENQUERY", "OR", "DEC", "COMMIT", "DETERMINISTIC", "PRIMARY", "USER", "SSL", "CONTAINSTABLE", "BEFORE", "DECLARE", "MEDIUMBLOB", "CROSS", "SMALLINT", "WHILE", "HOUR_SECOND", "MATCH", "REFERENCES"};

    public static boolean isReservedWord(String target) {
        return Arrays.stream(oracleReservedWords).anyMatch(word -> word.equalsIgnoreCase(target));
    }

    /**
     * 校验表操作是否可以生效到数据库
     * 1命名是否用到了保留字，关键字（字段名 索引名不校验）
     * 2默认值是否符合字段类型、长度
     * 3存在数据的表无法修改字段类型或减小长度精度，无法删除字段，无法添加不允许为空的字段
     * 4添加的索引关联字段是否存在
     * 5大文本类型无法当成索引字段，大文本无法与其他类型相互转换
     * 6字段存在null数据，字段无法修改为不允许为空
     */
    public static LcdpAnalysisResultDTO validateTable(LcdpTableDTO physicalTable, LcdpTableDTO newTable, List<LcdpTableFieldBean> fieldOpsList, List<LcdpTableIndexBean> indexOpsList) {
        //结果记录对象
        LcdpAnalysisResultDTO analysisResult = new LcdpAnalysisResultDTO();
        List<String> errorRecList = new ArrayList<>();
        analysisResult.setAnalysisResultList(errorRecList);
        analysisResult.setEnable(true);

        LcdpDatabaseService lcdpDatabaseService = ApplicationContextHelper.getBean(LcdpDatabaseService.class);

        boolean isExistPhysicalTable = !ObjectUtils.isEmpty(physicalTable);

        boolean isExistData = false;

        if (isExistPhysicalTable) {
            //查询出物理表信息
            isExistData = lcdpDatabaseService.isExistData(physicalTable.getTableName());
        }

        //校验字段操作
        if (!fieldOpsList.isEmpty()) {

            for (LcdpTableFieldBean field : fieldOpsList) {

                //添加、修改时字段默认值校验
                if (!LcdpConstant.FIELD_INDEX_OPS_DELETE.equalsIgnoreCase(field.getFieldOperationType())) {

                    if (varchar.name().equals(field.getFieldType())) {
                        //varchar类型默认值校验，长度不超过字段长度(自动去掉'单引号)
                        if (!StringUtils.isEmpty(field.getDefaultValue())) {
                            if (field.getDefaultValue().replaceAll("'", "").replaceAll(cnCharacterPattren, "123").length() > ConvertUtils.convert(field.getFieldLength(), Integer.class, 1)) {
                                errorRecList.add(I18nHelper.getMessage("LCDP.MODULE.TABLES.DEFAULT_VALUE.TIP.INVALID", field.getFieldName()));
                                continue;
                            }
                        }
                    } else if (date.name().equals(field.getFieldType()) || clob.name().equals(field.getFieldType())) {
                        //日期、大文本不做处理
                    } else if (number.name().equals(field.getFieldType())) {
                        //number类型默认值校验
                        if (!StringUtils.isEmpty(field.getDefaultValue())) {
                            if (!NumberUtils.isNumber(StringUtils.trim(field.getDefaultValue()))) {
                                errorRecList.add(I18nHelper.getMessage("LCDP.MODULE.TABLES.DEFAULT_VALUE.TIP.INVALID", field.getFieldName()));
                                continue;
                            }
                            BigDecimal defatulValue = new BigDecimal(StringUtils.trim(field.getDefaultValue()));
                            if (defatulValue.precision() > (field.getPrecision() == 0 ? 19 : field.getPrecision()) || defatulValue.scale() > field.getScale() || defatulValue.precision() - defatulValue.scale() > (field.getPrecision() == 0 ? 19 : field.getPrecision()) - field.getScale()) {
                                errorRecList.add(I18nHelper.getMessage("LCDP.MODULE.TABLES.DEFAULT_VALUE.TIP.INVALID", field.getFieldName()));
                                continue;
                            }
                        }
                    } else {
                        errorRecList.add(I18nHelper.getMessage("LCDP.MODULE.TABLES.FIELD.TYPE.TIP.INVALID", field.getFieldName()));
                        continue;
                    }
                }

                //校验新增的字段
                if (LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(field.getFieldOperationType())) {
                    //表存在数据时添加字段不能不允许为空
                    if (isExistData) {
                        if (StringUtils.equals(LcdpConstant.FIELD_ALLOWNULL_NO, field.getAllowNull())) {
                            errorRecList.add(I18nHelper.getMessage("LCDP.MODULE.TABLES.FIELD.ALLOW_NULL.TIP.EXIST_DATA_CANNOT_NOT_NULL", field.getFieldName()));
                            continue;
                        }
                    }
                }

                //修改操作校验
                if (LcdpConstant.FIELD_INDEX_OPS_UPDATE.equalsIgnoreCase(field.getFieldOperationType())) {
                    //获取物理表字段定义
                    LcdpTableFieldDTO physicalField = physicalTable.getFieldList().stream().filter(f -> StringUtils.equals(f.getFieldName(), field.getFieldName())).findFirst().orElse(null);

                    //oracle clob类型无法与其他类型相互转换
                    if (!StringUtils.equals(physicalField.getFieldType(), field.getFieldType()) && (StringUtils.equals(field.getFieldType(), clob.name()) || StringUtils.equals(physicalField.getFieldType(), clob.name()))) {
                        errorRecList.add(I18nHelper.getMessage("LCDP.MODULE.TABLES.FIELD.TYPE.TIP.CANNOT_UPDATE_CLOB", field.getFieldName()));
                        continue;
                    }
                    //表存在数据没法修改字段类型
                    if (isExistData) {
                        if (!StringUtils.equals(physicalField.getFieldType(), field.getFieldType())) {
                            errorRecList.add(I18nHelper.getMessage("LCDP.MODULE.TABLES.FIELD.TYPE.TIP.EXIST_DATA_CANNOT_UPDATE", field.getFieldName()));
                            continue;
                        }

                        //修改字段不能为空，必须保证该列没有空值数据
                        if (!StringUtils.equals(physicalField.getAllowNull(), field.getAllowNull()) && StringUtils.equals(field.getAllowNull(), LcdpConstant.FIELD_ALLOWNULL_NO) && lcdpDatabaseService.isExistNullDataInColumn(newTable.getTableName(), field.getFieldName())) {
                            errorRecList.add(I18nHelper.getMessage("LCDP.MODULE.TABLES.FIELD.ALLOW_NULL.TIP.EXIST_NULL_DATA_CANNOT_NOT_NULL", field.getFieldName()));
                            continue;
                        }

                        //存在数据时，字符串无法减小长度
                        if (varchar.name().equals(field.getFieldType()) && ConvertUtils.convert(field.getFieldLength(), Integer.class, 1) < ConvertUtils.convert(physicalField.getFieldLength(), Integer.class, 1)) {
                            errorRecList.add(I18nHelper.getMessage("LCDP.MODULE.TABLES.FIELD.LENGTH.TIP.EXIST_DATA_CANNOT_DECREASE", field.getFieldName()));
                            continue;
                        }
                        //存在数据时，数字类型无法减小精度刻度
                        if (number.name().equals(field.getFieldType()) && (physicalField.getPrecision() > field.getPrecision() || physicalField.getScale() > field.getScale())) {
                            errorRecList.add(I18nHelper.getMessage("LCDP.MODULE.TABLES.FIELD.PRECISION.TIP.EXIST_DATA_CANNOT_DECREASE", field.getFieldName()));
                            continue;
                        }
                    }
                }

                //校验删除的字段(存在数据不能删、索引字段不能删)
                if (LcdpConstant.FIELD_INDEX_OPS_DELETE.equalsIgnoreCase(field.getFieldOperationType())) {
                    //表存在数据没法删除字段类型
                    if (lcdpDatabaseService.isExistNotNullDataInColumn(newTable.getTableName(), field.getFieldName())) {
                        errorRecList.add(I18nHelper.getMessage("LCDP.MODULE.TABLES.FIELD.TYPE.TIP.EXIST_DATA_CANNOT_DELETE", field.getFieldName()));
                        continue;
                    }
                    //索引字段无法删除
                    for (LcdpTableIndexDTO index : newTable.getIndexList()) {
                        if (!StringUtils.isEmpty(index.getIndexField())) {
                            String[] indexFieldList = index.getIndexField().split(",");
                            if (Arrays.stream(indexFieldList).anyMatch(indexField -> StringUtils.equals(indexField, field.getFieldName()))) {
                                errorRecList.add(I18nHelper.getMessage("LCDP.MODULE.TABLES.FIELD.TYPE.TIP.INDEX_COLUMN_CANNOT_DELETE", field.getFieldName()));
                            }
                        }
                    }
                }
            }
        }

        //校验索引操作
        if (!indexOpsList.isEmpty()) {
            for (LcdpTableIndexBean index : indexOpsList) {
                if (LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(index.getIndexOperationType())) {
                    //逻辑校验：添加的索引关联字段是否存在
                    if (!StringUtils.isEmpty(index.getIndexField())) {
                        String[] indexFieldList = index.getIndexField().split(",");
                        //遍历所有索引字段
                        for (String indexFieldName : indexFieldList) {
                            //在生效字段与新增字段里查询是否存在对应字段
                            LcdpTableFieldDTO indexFiled = newTable.getFieldList().stream().filter(oldField -> StringUtils.equals(indexFieldName, oldField.getFieldName())).findAny().orElse(null);
                            if (ObjectUtils.isEmpty(indexFiled)) {
                                errorRecList.add(I18nHelper.getMessage("LCDP.MODULE.TABLES.INDEX.FIELD.TIP.NOT_EXIST", index.getIndexName()));
                            } else if (StringUtils.equals(indexFiled.getFieldType(), clob.name())) {
                                errorRecList.add(I18nHelper.getMessage("LCDP.MODULE.TABLES.INDEX.FIELD.TIP.NOT_SUPPORT_CLOB", index.getIndexName()));
                            }
                        }
                    } else {
                        errorRecList.add(I18nHelper.getMessage("LCDP.MODULE.TABLES.INDEX.FIELD.TIP.NOT_EXIST", index.getIndexName()));
                    }
                }
            }
        }

        if (!errorRecList.isEmpty()) {
            analysisResult.setEnable(false);
        }

        return analysisResult;
    }

    /**
     * 校验不同数据库里表定义是否一致
     */
    public static boolean validateDatabaseConsistency(String tableName) {
        if (!ApplicationContextHelper.isProfileActivated("lcdp-tenant")) {
            return true;
        }
        LcdpDatabaseService lcdpDatabaseService = ApplicationContextHelper.getBean(LcdpDatabaseService.class);
        String tenant = TenantContext.getTenant();
        try {
            //生产库是否存在表
            TenantContext.removeTenant();
            boolean existPhysicalTable = lcdpDatabaseService.isExistPhysicalTable(tableName);
            //开发库是否存在表
            TenantContext.setTenant(LcdpDatasourceAspect.DEV_DATABASE_TENANT_NAME);

            LcdpTableDTO lcdpTableDTODev;

            boolean existPhysicalTableDev = lcdpDatabaseService.isExistPhysicalTable(tableName);

            if (existPhysicalTable != existPhysicalTableDev) {
                return false;
            }
            if (!existPhysicalTable) {
                return true;
            }
            //查询开发库表结构
            lcdpTableDTODev = lcdpDatabaseService.selectPhysicalTableInfo(tableName);
            //查询生产库表结构
            TenantContext.removeTenant();
            LcdpTableDTO lcdpTableDTO = lcdpDatabaseService.selectPhysicalTableInfo(tableName);

            //对比字段索引是否一致
            List<LcdpTableFieldBean> fieldOpsList = analyzeFieldOps(lcdpTableDTODev.getFieldList(), lcdpTableDTO.getFieldList());
            List<LcdpTableIndexBean> IndexOpsList = analyzeIndexOps(lcdpTableDTODev.getIndexList(), lcdpTableDTO.getIndexList());

            if (fieldOpsList.size() > 0 || IndexOpsList.size() > 0) {
                return false;
            }
            return true;
        } finally {
            TenantContext.setTenant(tenant);
        }
    }

    /**
     * 校验不同数据库里表定义是否一致
     */
    public static boolean validateViewConsistency(String viewName) {
        if (!ApplicationContextHelper.isProfileActivated("lcdp-tenant")) {
            return true;
        }
        LcdpDatabaseService lcdpDatabaseService = ApplicationContextHelper.getBean(LcdpDatabaseService.class);
        String tenant = TenantContext.getTenant();
        try {
            //生产库是否存在表
            TenantContext.removeTenant();
            boolean existPhysicalTable = lcdpDatabaseService.isExistPhysicalView(viewName);
            //开发库是否存在表
            TenantContext.setTenant(LcdpDatasourceAspect.DEV_DATABASE_TENANT_NAME);

            boolean existPhysicalTableDev = lcdpDatabaseService.isExistPhysicalView(viewName);

            if (existPhysicalTable != existPhysicalTableDev) {
                return false;
            }
            if (!existPhysicalTable) {
                return true;
            }
            //查询开发库表结构
            LcdpViewBean lcdpViewDev = lcdpDatabaseService.selectPhysicalViewInfo(viewName);
            //查询生产库表结构
            TenantContext.removeTenant();
            LcdpViewBean lcdpViewPro = lcdpDatabaseService.selectPhysicalViewInfo(viewName);

            return StringUtils.equalsIgnoreCase(lcdpViewDev.getSelectStatement(), lcdpViewPro.getSelectStatement());
        } finally {
            TenantContext.setTenant(tenant);
        }
    }

    /**
     * 分析字段：对比字段，生成字段操作bean
     */
    public static List<LcdpTableFieldBean> analyzeFieldOps(List<LcdpTableFieldDTO> fieldList, List<LcdpTableFieldDTO> physicalFieldList) {
        List<LcdpTableFieldBean> fieldOpsList = new ArrayList<>();

        SqlGenerateHelper sqlGenerateHelper = ApplicationContextHelper.getBean(SqlGenerateHelper.class);

        //修改对比，找出更新字段
        Optional.ofNullable(fieldList).orElse(Collections.emptyList()).forEach(field -> {
            LcdpTableFieldBean fieldOps = new LcdpTableFieldBean();
            BeanUtils.copyProperties(field, fieldOps);

            Optional.ofNullable(physicalFieldList).orElse(Collections.emptyList()).forEach(currentField -> {

                LcdpTableFieldBean currentFieldBean = new LcdpTableFieldBean();
                BeanUtils.copyProperties(currentField, currentFieldBean);

                //相同字段判断是否修改
                if (StringUtils.equals(currentField.getFieldName(), field.getFieldName())) {
                    if (!StringUtils.equals(sqlGenerateHelper.parseFieldType(fieldOps), sqlGenerateHelper.parseFieldType(currentFieldBean)) || !StringUtils.equals(fieldOps.getAllowNull(), currentFieldBean.getAllowNull()) || !StringUtils.equals(sqlGenerateHelper.parseDefaultValue(fieldOps), sqlGenerateHelper.parseDefaultValue(currentFieldBean)) || !StringUtils.equals(sqlGenerateHelper.nullToEmpty(fieldOps.getFieldComment()), sqlGenerateHelper.nullToEmpty(currentFieldBean.getFieldComment()))) {
                        fieldOps.setFieldOperationType(LcdpConstant.FIELD_INDEX_OPS_UPDATE);
                        fieldOpsList.add(fieldOps);
                    }
                }
            });
        });

        //找出添加字段
        Optional.ofNullable(fieldList).orElse(Collections.emptyList()).stream()
                .filter(field -> Optional.ofNullable(physicalFieldList).orElse(Collections.emptyList()).stream()
                        .noneMatch(currentField -> StringUtils.equals(field.getFieldName(), currentField.getFieldName())))
                .forEach(field -> {
                    LcdpTableFieldBean fieldOps = new LcdpTableFieldBean();
                    BeanUtils.copyProperties(field, fieldOps);
                    fieldOps.setFieldOperationType(LcdpConstant.FIELD_INDEX_OPS_ADD);
                    fieldOpsList.add(fieldOps);
                });

        //找出删除字段
        Optional.ofNullable(physicalFieldList).orElse(Collections.emptyList()).stream()
                .filter(physicalField -> Optional.ofNullable(fieldList).orElse(Collections.emptyList()).stream()
                        .noneMatch(field -> StringUtils.equals(field.getFieldName(), physicalField.getFieldName())))
                .forEach(physicalField -> {
                    LcdpTableFieldBean fieldOps = new LcdpTableFieldBean();
                    BeanUtils.copyProperties(physicalField, fieldOps);
                    fieldOps.setFieldOperationType(LcdpConstant.FIELD_INDEX_OPS_DELETE);
                    fieldOpsList.add(fieldOps);
                });


        return fieldOpsList;
    }

    /**
     * 分析索引：对比索引字段，生成索引操作bean
     */
    public static List<LcdpTableIndexBean> analyzeIndexOps(List<LcdpTableIndexDTO> indexList, List<LcdpTableIndexDTO> physicalIndexList) {
        List<LcdpTableIndexBean> indexOpsList = new ArrayList<>();

        //通过索引字段找出将要添加的索引
        Optional.ofNullable(indexList).orElse(Collections.emptyList()).stream()
                .filter(index -> !IndexType.primarykey.name().equalsIgnoreCase(index.getIndexType()) && Optional.ofNullable(physicalIndexList).orElse(Collections.emptyList()).stream()
                        .noneMatch(physicalIndex -> StringUtils.equals(index.getIndexField(), physicalIndex.getIndexField()))
                        || (IndexType.primarykey.name().equalsIgnoreCase(index.getIndexType()) && Optional.ofNullable(physicalIndexList).orElse(Collections.emptyList()).stream()
                        .noneMatch(physicalIndex -> IndexType.primarykey.name().equalsIgnoreCase(index.getIndexType()))))
                .forEach(index -> {
                    LcdpTableIndexBean indexOps = new LcdpTableIndexBean();
                    BeanUtils.copyProperties(index, indexOps);
                    indexOps.setIndexOperationType(LcdpConstant.FIELD_INDEX_OPS_ADD);
                    indexOpsList.add(indexOps);
                });

        //通过索引字段找出需要删除的索引
        Optional.ofNullable(physicalIndexList).orElse(Collections.emptyList()).stream()
                .filter(physicalIndex -> !IndexType.primarykey.name().equalsIgnoreCase(physicalIndex.getIndexType()) && Optional.ofNullable(indexList).orElse(Collections.emptyList()).stream()
                        .noneMatch(index -> StringUtils.equals(index.getIndexField(), physicalIndex.getIndexField())))
                .forEach(physicalIndex -> {
                    LcdpTableIndexBean indexOps = new LcdpTableIndexBean();
                    BeanUtils.copyProperties(physicalIndex, indexOps);
                    indexOps.setIndexOperationType(LcdpConstant.FIELD_INDEX_OPS_DELETE);
                    indexOpsList.add(indexOps);
                });

        return indexOpsList;
    }

    /**
     * 将bean转化为DTO对象
     */
    public static List<LcdpTableDTO> parseTableBeanToDTO(List<LcdpTableBean> tableList, List<LcdpTableFieldBean> fieldList, List<LcdpTableIndexBean> indexList) {
        if (ObjectUtils.isEmpty(tableList) || tableList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        Map<String, List<LcdpTableFieldBean>> fieldGroupMap = Optional.ofNullable(fieldList).orElse(Collections.emptyList()).stream().collect(Collectors.groupingBy(LcdpTableFieldBean::getTableName));

        Map<String, List<LcdpTableIndexBean>> indexGroupMap = Optional.ofNullable(indexList).orElse(Collections.emptyList()).stream().collect(Collectors.groupingBy(LcdpTableIndexBean::getTableName));

        return tableList.stream().map(lcdpTable -> {
            LcdpTableDTO table = new LcdpTableDTO();
            BeanUtils.copyProperties(lcdpTable, table);

            table.setFieldList(Optional.ofNullable(fieldGroupMap.get(lcdpTable.getTableName())).orElse(Collections.emptyList()).stream().map(lcdpField -> {
                LcdpTableFieldDTO field = new LcdpTableFieldDTO();
                BeanUtils.copyProperties(lcdpField, field);
                return field;
            }).collect(Collectors.toList()));

            table.setIndexList(Optional.ofNullable(indexGroupMap.get(lcdpTable.getTableName())).orElse(Collections.emptyList()).stream().map(lcdpIndex -> {
                LcdpTableIndexDTO index = new LcdpTableIndexDTO();
                BeanUtils.copyProperties(lcdpIndex, index);
                return index;
            }).collect(Collectors.toList()));

            return table;
        }).collect(Collectors.toList());
    }

    /**
     * 生成表、表字段对应国际化CODE
     */
    public static String i18nCode(String tableName, String fieldName) {
        return tableName.toUpperCase() + "." + (StringUtils.isEmpty(fieldName) ? "SERVICE_NAME" : fieldName.toUpperCase());
    }
}
