package com.sunwayworld.cloud.module.lcdp.table.helper.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import static com.sunwayworld.cloud.module.lcdp.table.helper.FieldType.clob;
import static com.sunwayworld.cloud.module.lcdp.table.helper.FieldType.date;
import static com.sunwayworld.cloud.module.lcdp.table.helper.FieldType.number;
import static com.sunwayworld.cloud.module.lcdp.table.helper.FieldType.varchar;
import static com.sunwayworld.cloud.module.lcdp.table.helper.IndexType.primarykey;
import static com.sunwayworld.cloud.module.lcdp.table.helper.IndexType.regular;
import static com.sunwayworld.cloud.module.lcdp.table.helper.IndexType.unique;

import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableIndexBean;
import com.sunwayworld.cloud.module.lcdp.table.helper.SqlGenerateHelper;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;

/**
 * @author yangsz@sunway.com 2022-10-21
 */
@Component
@Conditional(LcdpPgsqlCondition.class)
public class PgsqlSqlGenereateHelper implements SqlGenerateHelper {

    /**
     * 修改表注释：COMMENT ON TABLE "表名" IS '注释'
     */
    @Override
    public String generateTableCommentSql(LcdpTableBean table, LcdpTableDTO physicalTable) {
        return MessageFormat.format("COMMENT ON TABLE \"{0}\" IS ''{1}''", table.getTableName().toLowerCase(), nullToEmpty(table.getTableDesc()));
    }

    /**
     * 删除表主键：ALTER TABLE "表名" DROP CONSTRAINT "主键名";
     * 删除索引：DROP INDEX "索引名称";
     * 添加表主键：ALTER TABLE "表名" ADD CONSTRAINT "主键名" PRIMARY KEY("主键1"，"主键2");
     * 添加索引：CREATE [UNIQUE] INDEX "索引名称" ON "表名"("主键1"，"主键2");
     */
    @Override
    public String generateIndexSql(LcdpTableBean table, LcdpTableIndexBean index) {
        String tableName = table.getTableName().toLowerCase();

        if (primarykey.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_DELETE.equalsIgnoreCase(index.getIndexOperationType())) {
            return MessageFormat.format("ALTER TABLE \"{0}\" DROP CONSTRAINT \"{1}\"", tableName, index.getIndexName().toLowerCase());
        } else if (!primarykey.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_DELETE.equalsIgnoreCase(index.getIndexOperationType())) {
            return MessageFormat.format("DROP INDEX \"{0}\"", index.getIndexName().toLowerCase());
        } else if (primarykey.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(index.getIndexOperationType())) {
            return MessageFormat.format("ALTER TABLE \"{0}\" ADD CONSTRAINT \"{1}\" PRIMARY KEY ({2})", tableName, index.getIndexName().toLowerCase(), parseIndexField(index).toLowerCase());
        } else if (unique.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(index.getIndexOperationType())) {
            return MessageFormat.format("CREATE UNIQUE INDEX \"{0}\" ON \"{1}\"({2})", index.getIndexName().toLowerCase(), tableName, parseIndexField(index).toLowerCase());
        } else if (regular.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(index.getIndexOperationType())) {
            return MessageFormat.format("CREATE INDEX \"{0}\" ON \"{1}\"({2})", index.getIndexName().toLowerCase(), tableName, parseIndexField(index).toLowerCase());
        }
        return "";
    }

    /**
     * 修改表字段：ALTER TABLE "表名" ALTER COLUMN "字段名" TYPE 字段类型;
     * ：ALTER TABLE "表名" ALTER COLUMN "字段名" SET DEFAULT 值;
     * ：ALTER TABLE "表名" ALTER COLUMN "字段名" DROP DEFAULT;
     * ：ALTER TABLE "表名" ALTER COLUMN "字段名" SET NOT NULL;
     * ：ALTER TABLE "表名" ALTER COLUMN "字段名" DROP NOT NULL;
     * 删除表字段：ALTER TABLE "表名" DROP "字段名称";
     * 添加表字段：ALTER TABLE "表名" ADD "字段名称" 字段类型 默认值 是否为空 ;
     */
    @Override
    public List<String> generateColumnSql(LcdpTableBean table, LcdpTableFieldBean oldField, LcdpTableFieldBean field) {
        String tableName = table.getTableName().toLowerCase();
        List<String> sqlList = new ArrayList<>();
        //修改字段
        if (LcdpConstant.FIELD_INDEX_OPS_UPDATE.equalsIgnoreCase(field.getFieldOperationType())) {
            //修改类型
            if (!StringUtils.equals(parseFieldType(oldField), parseFieldType(field))) {
                sqlList.add(MessageFormat.format("ALTER TABLE \"{0}\" ALTER COLUMN \"{1}\" TYPE {2}"
                        , tableName, field.getFieldName().toLowerCase(), parseFieldType(field)));
            }
            //修改默认值
            if (!StringUtils.equals(nullToEmpty(oldField.getDefaultValue()), nullToEmpty(field.getDefaultValue()))) {
                if (!StringUtils.isEmpty(field.getDefaultValue())) {
                    sqlList.add(MessageFormat.format("ALTER TABLE \"{0}\" ALTER COLUMN \"{1}\" SET {2}"
                            , tableName, field.getFieldName().toLowerCase(), parseDefaultValue(field)));
                } else {
                    sqlList.add(MessageFormat.format("ALTER TABLE \"{0}\" ALTER COLUMN \"{1}\" DROP DEFAULT"
                            , tableName, field.getFieldName().toLowerCase()));
                }
            }
            //修改是否为空
            if (!StringUtils.equals(oldField.getAllowNull(), field.getAllowNull())) {
                if (LcdpConstant.FIELD_ALLOWNULL_YES.equals(field.getAllowNull())) {
                    sqlList.add(MessageFormat.format("ALTER TABLE \"{0}\" ALTER COLUMN \"{1}\" DROP NOT NULL"
                            , tableName, field.getFieldName().toLowerCase()));
                } else if (LcdpConstant.FIELD_ALLOWNULL_NO.equals(field.getAllowNull())) {
                    sqlList.add(MessageFormat.format("ALTER TABLE \"{0}\" ALTER COLUMN \"{1}\" SET NOT NULL"
                            , tableName, field.getFieldName().toLowerCase()));
                }
            }
            if (!StringUtils.equals(nullToEmpty(oldField.getFieldComment()), nullToEmpty(field.getFieldComment()))) {
                sqlList.add(MessageFormat.format("COMMENT ON COLUMN \"{0}\".\"{1}\" IS ''{2}''"
                        , tableName, field.getFieldName().toLowerCase(), nullToEmpty(field.getFieldComment())));
            }
            //删除字段
        } else if (LcdpConstant.FIELD_INDEX_OPS_DELETE.equalsIgnoreCase(field.getFieldOperationType())) {
            sqlList.add(MessageFormat.format("ALTER TABLE \"{0}\" DROP COLUMN \"{1}\""
                    , tableName, field.getFieldName().toLowerCase()));
            //添加字段
        } else if (LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(field.getFieldOperationType())) {
            sqlList.add(MessageFormat.format("ALTER TABLE \"{0}\" ADD \"{1}\" {2} {3} {4}"
                    , tableName, field.getFieldName().toLowerCase(), parseFieldType(field), parseDefaultValue(field), parseIsNULL(field)));
            //非删除操作更新注释
            if (!StringUtils.isEmpty(field.getFieldComment())) {
                sqlList.add(MessageFormat.format("COMMENT ON COLUMN \"{0}\".\"{1}\" IS ''{2}''"
                        , tableName, field.getFieldName().toLowerCase(), nullToEmpty(field.getFieldComment())));
            }
        }

        return sqlList;
    }

    /**
     * 创建表：CREATE TABLE "表名" (
     * "字段名1" 字段类型 默认值 是否为空 ,
     * "字段名2" 字段类型 默认值 是否为空 ,
     * .....
     * PRIMARY KEY ("字段1","字段2")
     * )
     * 修改表注释：COMMENT ON TABLE "表名" IS '注释'
     * 修改字段注释：COMMENT ON COLUMN "表名"."列名" IS '注释'
     */
    @Override
    public List<String> generateCreateTableSql(LcdpTableBean table, List<LcdpTableFieldBean> fields, LcdpTableIndexBean primaryKey) {
        String tableName = table.getTableName().toLowerCase();
        String tableDesc = table.getTableDesc();
        List<String> sqlList = new ArrayList<>();

        //创建表sql
        StringBuilder createSqlAppend = new StringBuilder(MessageFormat.format("CREATE TABLE \"{0}\" (\n", tableName));
        for (int j = 0; j < fields.size(); j++) {
            LcdpTableFieldBean field = fields.get(j);
            createSqlAppend.append(MessageFormat.format(" \"{0}\" {1} {2} {3} ", field.getFieldName().toLowerCase(), parseFieldType(field), parseDefaultValue(field), parseIsNULL(field), nullToEmpty(field.getFieldComment())));

            if (j == fields.size() - 1) {
                //添加主键
                if (!ObjectUtils.isEmpty(primaryKey)) {
                    createSqlAppend.append(",\n");
                    createSqlAppend.append(MessageFormat.format("PRIMARY KEY ({0})\n", parseIndexField(primaryKey).toLowerCase()));
                } else {
                    createSqlAppend.append("\n");
                }
            } else {
                createSqlAppend.append(",\n");
            }
        }
        createSqlAppend.append(MessageFormat.format(")", nullToEmpty(tableDesc)));
        sqlList.add(createSqlAppend.toString());

        //注释sql
        if (!StringUtils.isEmpty(tableDesc)) {
            sqlList.add(MessageFormat.format("COMMENT ON TABLE \"{0}\" IS ''{1}''", tableName, nullToEmpty(tableDesc)));
        }
        Optional.of(fields).orElse(Collections.emptyList()).stream().filter(field -> LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(field.getFieldOperationType()))
                .forEach(field -> {
                            if (!StringUtils.isEmpty(field.getFieldComment())) {
                                sqlList.add(MessageFormat.format("COMMENT ON COLUMN \"{0}\".\"{1}\" IS ''{2}''"
                                        , tableName, field.getFieldName().toLowerCase(), nullToEmpty(field.getFieldComment())));
                            }
                        }
                );

        return sqlList;
    }

    @Override
    public String databaseTypeToFieldType(String databaseType) {
        switch (databaseType.toUpperCase()) {
            case "VARCHAR":
                return varchar.name();
            case "DATE":
            case "TIMESTAMP":
                return date.name();
            case "TEXT":
            case "LONGTEXT":
                return clob.name();
            case "DECIMAL":
            case "INT8":
            case "INT4":
            case "NUMERIC":
                return number.name();
            default:
                return databaseType;
        }
    }

    @Override
    public String parseFieldType(LcdpTableFieldBean field) {
        String fieldType = "";
        String fieldLength = "";

        if (varchar.name().equalsIgnoreCase(field.getFieldType())) {
            if (!StringUtils.isEmpty(field.getFieldLength())) {
                fieldLength = "(" + field.getFieldLength() + ")";
            }
            fieldType = "VARCHAR" + fieldLength;
        } else if (date.name().equalsIgnoreCase(field.getFieldType())) {
            fieldType = "TIMESTAMP";
        } else if (clob.name().equalsIgnoreCase(field.getFieldType())) {
            fieldType = "TEXT";
        } else if (number.name().equalsIgnoreCase(field.getFieldType())) {
            if (!ObjectUtils.isEmpty(field.getPrecision()) && !ObjectUtils.isEmpty(field.getScale()) && field.getPrecision() > 0 && field.getScale() > 0) {
                field.setFieldLength(field.getPrecision() + "," + field.getScale());
                fieldLength = "(" + field.getPrecision() + "," + field.getScale() + ")";
                fieldType = "DECIMAL" + fieldLength;
            } else if (!ObjectUtils.isEmpty(field.getPrecision()) && field.getPrecision() > 0 && (ObjectUtils.isEmpty(field.getScale()) || field.getScale() <= 0)) {
                field.setFieldLength(String.valueOf(field.getPrecision()));
                fieldLength = "(" + field.getPrecision() + ")";
                fieldType = "NUMERIC" + fieldLength;
            } else {
                fieldType = "BIGINT";
            }
        }

        return fieldType.toUpperCase();
    }

    @Override
    public String parseSQLException(String message) {
        if(StringUtils.containsIgnoreCase(message,"permission denied")){
            return I18nHelper.getMessage("LCDP.MODULE.DATABASE.TIP.INSUFFICIENT_PRIVILEGES");
        }
        return message;
    }

}
