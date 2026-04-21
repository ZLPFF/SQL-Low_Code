package com.sunwayworld.cloud.module.lcdp.table.helper.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableIndexBean;
import static com.sunwayworld.cloud.module.lcdp.table.helper.FieldType.clob;
import static com.sunwayworld.cloud.module.lcdp.table.helper.FieldType.date;
import static com.sunwayworld.cloud.module.lcdp.table.helper.FieldType.number;
import static com.sunwayworld.cloud.module.lcdp.table.helper.FieldType.varchar;
import static com.sunwayworld.cloud.module.lcdp.table.helper.IndexType.primarykey;
import static com.sunwayworld.cloud.module.lcdp.table.helper.IndexType.regular;
import static com.sunwayworld.cloud.module.lcdp.table.helper.IndexType.unique;
import com.sunwayworld.cloud.module.lcdp.table.helper.SqlGenerateHelper;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.database.dialect.DialectRepository;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(LcdpOracleCondition.class)
public class OracleSqlGenereateHelper implements SqlGenerateHelper {

    /**
     * 修改表注释：COMMENT ON TABLE "表名" IS '注释'
     */
    @Override
    public String generateTableCommentSql(LcdpTableBean table, LcdpTableDTO physicalTable) {
        return MessageFormat.format("COMMENT ON TABLE \"{0}\" IS ''{1}''", table.getTableName(), nullToEmpty(table.getTableDesc()));
    }

    /**
     * 删除表主键：ALTER TABLE "表名" DROP CONSTRAINT "主键名";
     * 删除索引：DROP INDEX "索引名称";
     * 添加表主键：ALTER TABLE "表名" ADD CONSTRAINT "主键名" PRIMARY KEY("主键1"，"主键2");
     * 添加索引：CREATE [UNIQUE] INDEX "索引名称" ON "表名"("主键1"，"主键2");
     */
    @Override
    public String generateIndexSql(LcdpTableBean table, LcdpTableIndexBean index) {
        String tableName = table.getTableName();

        if (primarykey.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_DELETE.equalsIgnoreCase(index.getIndexOperationType())) {
            return MessageFormat.format("ALTER TABLE \"{0}\" DROP CONSTRAINT \"{1}\"", tableName, index.getIndexName());
        } else if (!primarykey.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_DELETE.equalsIgnoreCase(index.getIndexOperationType())) {
            return MessageFormat.format("DROP INDEX \"{0}\"", index.getIndexName());
        } else if (primarykey.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(index.getIndexOperationType())) {
            String primaryKey = parseIndexField(index);
            String primaryName = index.getIndexName();
            //处理mysql 主键默认名导入到oracle问题
            if (StringUtils.equalsIgnoreCase("primary", primaryName)) {
                primaryName = "PK_" + tableName;
            }
            return MessageFormat.format("ALTER TABLE \"{0}\" ADD CONSTRAINT \"{1}\" PRIMARY KEY ({2})", tableName, primaryName, primaryKey);
        } else if (unique.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(index.getIndexOperationType())) {
            return MessageFormat.format("CREATE UNIQUE INDEX \"{0}\" ON \"{1}\"({2})", "IDX_" + ApplicationContextHelper.getNextIdentity(), tableName, parseIndexField(index));
        } else if (regular.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(index.getIndexOperationType())) {
            return MessageFormat.format("CREATE INDEX \"{0}\" ON \"{1}\"({2})", "IDX_" + ApplicationContextHelper.getNextIdentity(), tableName, parseIndexField(index));
        }
        return "";

    }

    /**
     * 修改表字段：ALTER TABLE "表名" MODIFY "字段名" 字段类型 是否为空;
     * 字段默认值单独修改：ALTER TABLE "表名" MODIFY "字段名" 默认值;
     * 删除表字段：ALTER TABLE "表名" DROP COLUMN "字段名";
     * 添加表字段：ALTER TABLE "表名" ADD "字段名" 字段类型 默认值 是否为空;
     */
    @Override
    public List<String> generateColumnSql(LcdpTableBean table, LcdpTableFieldBean currentField, LcdpTableFieldBean field) {
        String tableName = table.getTableName();
        List<String> sqlList = new ArrayList<>();
        //修改字段
        if (LcdpConstant.FIELD_INDEX_OPS_UPDATE.equalsIgnoreCase(field.getFieldOperationType())) {
            //修改字段类型或是否为空
            if (!StringUtils.equals(parseFieldType(currentField), parseFieldType(field)) || !StringUtils.equals(currentField.getAllowNull(), field.getAllowNull())) {
                sqlList.add(MessageFormat.format("ALTER TABLE \"{0}\" MODIFY \"{1}\" {2} {3}"
                        , tableName, field.getFieldName()
                        , StringUtils.equals(parseFieldType(currentField), parseFieldType(field)) ? "" : parseFieldType(field)
                        , StringUtils.equals(currentField.getAllowNull(), field.getAllowNull()) ? "" : parseIsNULL(field)));
            }
            //修改字段默认值
            if (!StringUtils.equals(nullToEmpty(currentField.getDefaultValue()), nullToEmpty(field.getDefaultValue()))) {
                if (StringUtils.isEmpty(field.getDefaultValue())) {
                    sqlList.add(MessageFormat.format("ALTER TABLE \"{0}\" MODIFY \"{1}\" DEFAULT NULL"
                            , tableName, field.getFieldName()));
                } else {
                    sqlList.add(MessageFormat.format("ALTER TABLE \"{0}\" MODIFY \"{1}\" {2}"
                            , tableName, field.getFieldName(), parseDefaultValue(field)));
                }
            }
            //修改字段注释
            if (!StringUtils.equals(nullToEmpty(currentField.getFieldComment()), nullToEmpty(field.getFieldComment()))) {
                sqlList.add(MessageFormat.format("COMMENT ON COLUMN \"{0}\".\"{1}\" IS ''{2}''"
                        , tableName, field.getFieldName(), nullToEmpty(field.getFieldComment())));
            }
            //删除字段
        } else if (LcdpConstant.FIELD_INDEX_OPS_DELETE.equalsIgnoreCase(field.getFieldOperationType())) {
            sqlList.add(MessageFormat.format("ALTER TABLE \"{0}\" DROP COLUMN \"{1}\""
                    , tableName, field.getFieldName()));
            //添加字段
        } else if (LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(field.getFieldOperationType())) {
            sqlList.add(MessageFormat.format("ALTER TABLE \"{0}\" ADD \"{1}\" {2} {3} {4}"
                    , tableName, field.getFieldName(), parseFieldType(field), parseDefaultValue(field), parseIsNULL(field)));
            if (!StringUtils.isEmpty(field.getFieldComment())) {
                sqlList.add(MessageFormat.format("COMMENT ON COLUMN \"{0}\".\"{1}\" IS ''{2}''"
                        , tableName, field.getFieldName(), nullToEmpty(field.getFieldComment())));
            }
        }
        return sqlList;

    }

    /**
     * 创建表：CREATE TABLE "表名" (
     * "字段名1" 字段类型 默认值 是否为空,
     * "字段名2" 字段类型 默认值 是否为空,
     * .....
     * CONSTRAINT "主键名" PRIMARY KEY ("主键1"，"主键2")
     * )
     * 修改表注释：COMMENT ON TABLE "表名" IS '注释'
     * 修改字段注释：COMMENT ON COLUMN "表名"."列名" IS '注释'
     */
    @Override
    public List<String> generateCreateTableSql(LcdpTableBean table, List<LcdpTableFieldBean> fields, LcdpTableIndexBean primaryKey) {
        String tableName = table.getTableName();
        String tableDesc = table.getTableDesc();
        List<String> sqlList = new ArrayList<>();

        //创建表sql
        StringBuilder createSqlAppend = new StringBuilder(MessageFormat.format("CREATE TABLE \"{0}\" (\n", tableName));
        for (int j = 0; j < fields.size(); j++) {
            LcdpTableFieldBean field = fields.get(j);
            createSqlAppend.append(MessageFormat.format(" \"{0}\" {1} {2} {3} ", field.getFieldName(), parseFieldType(field), parseDefaultValue(field), parseIsNULL(field)));

            if (j == fields.size() - 1) {
                //添加主键
                if (!ObjectUtils.isEmpty(primaryKey)) {
                    createSqlAppend.append(",\n");

                    String primaryName = primaryKey.getIndexName();
                    //处理mysql 主键默认名导入到oracle问题
                    if (StringUtils.equalsIgnoreCase("primary", primaryName)) {
                        primaryName = "PK_" + tableName;
                    }

                    if (DialectRepository.isDameng()) {
                        createSqlAppend.append(MessageFormat.format("PRIMARY KEY ({0})\n", parseIndexField(primaryKey)));
                    } else {
                        createSqlAppend.append(MessageFormat.format("CONSTRAINT \"{0}\" PRIMARY KEY ({1})\n", primaryName, parseIndexField(primaryKey)));
                    }


                } else {
                    createSqlAppend.append("\n");
                }
            } else {
                createSqlAppend.append(",\n");
            }
        }
        createSqlAppend.append(")");
        sqlList.add(createSqlAppend.toString());

        //注释sql
        if (!StringUtils.isEmpty(tableDesc)) {
            sqlList.add(MessageFormat.format("COMMENT ON TABLE \"{0}\" IS ''{1}''", tableName, nullToEmpty(tableDesc)));
        }
        Optional.of(fields).orElse(Collections.emptyList()).stream().filter(field -> LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(field.getFieldOperationType()))
                .forEach(field -> {
                            if (!StringUtils.isEmpty(field.getFieldComment())) {
                                sqlList.add(MessageFormat.format("COMMENT ON COLUMN \"{0}\".\"{1}\" IS ''{2}''"
                                        , tableName, field.getFieldName(), nullToEmpty(field.getFieldComment())));
                            }
                        }
                );

        return sqlList;

    }

    @Override
    public String databaseTypeToFieldType(String databaseType) {
        switch (databaseType.toUpperCase()) {
            case "VARCHAR2":
            case "VARCHAR":
            case "NVARCHAR2":
            case "UROWID":
            case "CHAR":
            case "RAW":
            case "BLOB":
                return varchar.name();
            case "DATE":
            case "TIMESTAMP":
            case "DATETIME":
                return date.name();
            case "CLOB":
            case "TEXT":
                return clob.name();
            case "NUMBER":
            case "NUMERIC":
            case "FLOAT":
            case "BIGINT":
            case "DECIMAL":
            case "INT":
                return number.name();

            default:
                if (StringUtils.startsWithIgnoreCase(databaseType, "TIMESTAMP")) {
                    return date.name();
                }
                return databaseType;
        }
    }

    /**
     * 根据LcdpTableFieldBean生成字段类型
     * 类型：    varchar、date、clob、number
     * oracle: varchar2()、date、clob、number()
     */
    @Override
    public String parseFieldType(LcdpTableFieldBean field) {
        String fieldType = "";
        String fieldLength = "";

        if (varchar.name().equalsIgnoreCase(field.getFieldType())) {
            if (!StringUtils.isEmpty(field.getFieldLength())) {
                fieldLength = "(" + field.getFieldLength() + ")";
            }
            fieldType = "VARCHAR2" + fieldLength;
        } else if (date.name().equalsIgnoreCase(field.getFieldType())) {
            if (DialectRepository.isDameng()) {
                fieldType = "DATETIME";
            } else {
                fieldType = "DATE";
            }
        } else if (clob.name().equalsIgnoreCase(field.getFieldType())) {
            fieldType = "CLOB";
        } else if (number.name().equalsIgnoreCase(field.getFieldType())) {
            Integer precision = field.getPrecision() == null ? 0 : field.getPrecision();
            Integer scale = field.getScale() == null ? 0 : field.getScale();

            if (precision > 0 && scale > 0) {
                field.setFieldLength(precision + "," + scale);
                fieldLength = "(" + precision + "," + scale + ")";
            } else if (precision > 0 && scale <= 0) {
                field.setFieldLength(String.valueOf(precision));
                fieldLength = "(" + precision + ")";
            }
            fieldType = "NUMBER" + fieldLength;
        }

        return fieldType.toUpperCase();
    }

    @Override
    public String parseSQLException(String message) {
        if (StringUtils.containsIgnoreCase(message, "ORA-01031")) {
            return I18nHelper.getMessage("LCDP.MODULE.DATABASE.TIP.INSUFFICIENT_PRIVILEGES");
        }
        return message;
    }

}