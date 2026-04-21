package com.sunwayworld.cloud.module.lcdp.table.helper.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Profile;
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
@Profile("mysql")
public class MysqlSqlGenereateHelper implements SqlGenerateHelper {

    /**
     * 修改表注释：ALTER TABLE `表名` COMMENT '注释'
     */
    @Override
    public String generateTableCommentSql(LcdpTableBean table, LcdpTableDTO physicalTable) {
        return MessageFormat.format("ALTER TABLE `{0}` COMMENT ''{1}''", table.getTableName(), nullToEmpty(table.getTableDesc()));
    }

    /**
     * 删除表主键：ALTER TABLE 表名 DROP PRIMARY KEY;
     * 删除索引：ALTER TABLE 表名 DROP INDEX 索引名称;
     * 添加表主键：ALTER TABLE 表名 ADD PRIMARY KEY (主键1，主键2);
     * 添加索引：CREATE [UNIQUE] INDEX index_name ON 表名(主键1，主键2);
     */
    @Override
    public String generateIndexSql(LcdpTableBean table, LcdpTableIndexBean index) {
        String tableName = table.getTableName();

        if (primarykey.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_DELETE.equalsIgnoreCase(index.getIndexOperationType())) {
            return MessageFormat.format("ALTER TABLE `{0}` DROP PRIMARY KEY", tableName);
        } else if (!primarykey.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_DELETE.equalsIgnoreCase(index.getIndexOperationType())) {
            return MessageFormat.format("ALTER TABLE `{0}` DROP INDEX `{1}`", tableName, index.getIndexName());
        } else if (primarykey.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(index.getIndexOperationType())) {
            return MessageFormat.format("ALTER TABLE `{0}` ADD  PRIMARY KEY ({1})", tableName, parseIndexField(index));
        } else if (unique.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(index.getIndexOperationType())) {
            return MessageFormat.format("CREATE UNIQUE INDEX `{0}` ON `{1}`({2})", index.getIndexName(), tableName, parseIndexField(index));
        } else if (regular.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(index.getIndexOperationType())) {
            return MessageFormat.format("CREATE INDEX `{0}` ON `{1}`({2})", index.getIndexName(), tableName, parseIndexField(index));
        }
        return "";
    }

    /**
     * 修改表字段：ALTER TABLE `表名` MODIFY `字段名` 字段类型 默认值 是否为空 注释;
     * 删除表字段：ALTER TABLE `表名` DROP `字段名`;
     * 添加表字段：ALTER TABLE `表名` ADD `字段名` 字段类型 默认值 是否为空 注释;
     */
    @Override
    public List<String> generateColumnSql(LcdpTableBean table, LcdpTableFieldBean oldField, LcdpTableFieldBean field) {
        String tableName = table.getTableName();
        List<String> sqlList = new ArrayList<>();
        if (LcdpConstant.FIELD_INDEX_OPS_UPDATE.equalsIgnoreCase(field.getFieldOperationType())) {
            sqlList.add(MessageFormat.format("ALTER TABLE `{0}` MODIFY `{1}` {2} {3} {4} COMMENT ''{5}''"
                    , tableName, field.getFieldName(), parseFieldType(field), parseDefaultValue(field), parseIsNULL(field), nullToEmpty(field.getFieldComment())));
        } else if (LcdpConstant.FIELD_INDEX_OPS_DELETE.equalsIgnoreCase(field.getFieldOperationType())) {
            sqlList.add(MessageFormat.format("ALTER TABLE `{0}` DROP `{1}`"
                    , tableName, field.getFieldName()));
        } else if (LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(field.getFieldOperationType())) {
            sqlList.add(MessageFormat.format("ALTER TABLE `{0}` ADD `{1}` {2} {3} {4} COMMENT ''{5}''"
                    , tableName, field.getFieldName(), parseFieldType(field), parseDefaultValue(field), parseIsNULL(field), nullToEmpty(field.getFieldComment())));
        }
        return sqlList;
    }

    /**
     * 创建表：CREATE TABLE `表名` (
     * `字段名1` 字段类型 默认值 是否为空 注释,
     * `字段名2` 字段类型 默认值 是否为空 注释,
     * .....
     * PRIMARY KEY (`字段1`,`字段2`)
     * )ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT = 表注释
     */
    @Override
    public List<String> generateCreateTableSql(LcdpTableBean table, List<LcdpTableFieldBean> fields, LcdpTableIndexBean primaryKey) {
        String tableName = table.getTableName();
        String tableDesc = table.getTableDesc();
        List<String> sqlList = new ArrayList<>();

        //创建表sql
        StringBuilder createSqlAppend = new StringBuilder(MessageFormat.format("CREATE TABLE `{0}` (\n", tableName));
        for (int j = 0; j < fields.size(); j++) {
            LcdpTableFieldBean field = fields.get(j);
            createSqlAppend.append(MessageFormat.format(" `{0}` {1} {2} {3} COMMENT ''{4}''", field.getFieldName(), parseFieldType(field), parseDefaultValue(field), parseIsNULL(field), nullToEmpty(field.getFieldComment())));

            if (j == fields.size() - 1) {
                //添加主键
                if (!ObjectUtils.isEmpty(primaryKey)) {
                    createSqlAppend.append(",\n");
                    createSqlAppend.append(MessageFormat.format("PRIMARY KEY ({0})\n", parseIndexField(primaryKey)));
                } else {
                    createSqlAppend.append("\n");
                }
            } else {
                createSqlAppend.append(",\n");
            }
        }
        createSqlAppend.append(MessageFormat.format(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT=''{0}''", nullToEmpty(tableDesc)));
        sqlList.add(createSqlAppend.toString());

        return sqlList;
    }


    @Override
    public String databaseTypeToFieldType(String databaseType) {
        switch (databaseType.toUpperCase()) {
            case "VARCHAR":
            case "CHAR":
            case "BIT":
                return varchar.name();
            case "DATETIME":
            case "TIMESTAMP":
            case "DATE":
            case "YEAR":
            case "TIME":
                return date.name();
            case "TEXT":
            case "LONGTEXT":
            case "LONGBLOB":
            case "MEDIUMTEXT":
                return clob.name();
            case "DECIMAL":
            case "BIGINT":
            case "INT":
            case "DOUBLE":
            case "FLOAT":
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
            fieldType = "DATETIME";
        } else if (clob.name().equalsIgnoreCase(field.getFieldType())) {
            fieldType = "LONGTEXT";
        } else if (number.name().equalsIgnoreCase(field.getFieldType())) {
            if (field.getPrecision() > 0 && field.getScale() == null) {
                field.setFieldLength(field.getPrecision() + "," + field.getPrecision()/2);
                fieldLength = "(" + field.getPrecision() + "," + field.getPrecision()/2 + ")";
                fieldType = "DECIMAL" + fieldLength;
            }else if (field.getPrecision() > 0 && field.getScale() > 0) {
                field.setFieldLength(field.getPrecision() + "," + field.getScale());
                fieldLength = "(" + field.getPrecision() + "," + field.getScale() + ")";
                fieldType = "DECIMAL" + fieldLength;
            } else if (field.getPrecision() > 0 && field.getScale() <= 0) {
                field.setFieldLength(String.valueOf(field.getPrecision()));
                fieldLength = "(" + field.getPrecision() + ")";
                fieldType = "BIGINT" + fieldLength;
            } else {
                fieldType = "BIGINT";
            }
        }

        return fieldType.toUpperCase();

    }

    /**
     * 保留标识符
     */
    @Override
    public String getReservedIdentifier() {
        return "`";
    }

    @Override
    public String parseSQLException(String message) {
        if(StringUtils.containsIgnoreCase(message,"ERROR 1142 (42000)")){
            return I18nHelper.getMessage("LCDP.MODULE.DATABASE.TIP.INSUFFICIENT_PRIVILEGES");
        }
        return message;
    }

}
