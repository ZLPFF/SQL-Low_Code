package com.sunwayworld.cloud.module.lcdp.table.helper.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.cloud.module.lcdp.table.helper.SqlGenerateHelper;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;

/**
 * @author yangsz@sunway.com 2022-10-21
 */
@Component
@Profile("sqlserver")
public class SqlserverSqlGenereateHelper implements SqlGenerateHelper {

    /**
     * 修改表注释：EXEC sys.sp_updateextendedproperty @name=N'MS_Description', @value=N'表注释'@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE',@level1name=N'表名'
     */
    @Override
    public String generateTableCommentSql(LcdpTableBean table, LcdpTableDTO physicalTable) {
        if(StringUtils.isEmpty(physicalTable.getTableDesc())){
            return MessageFormat.format("EXEC sys.sp_addextendedproperty @name=N''MS_Description'', @value=N''{0}'',@level0type=N''SCHEMA'', @level0name=N''dbo'', @level1type=N''TABLE'',@level1name=N''{1}''"
                    , nullToEmpty(table.getTableDesc()), table.getTableName());
        }else {
            return MessageFormat.format("EXEC sys.sp_updateextendedproperty @name=N''MS_Description'', @value=N''{0}'',@level0type=N''SCHEMA'', @level0name=N''dbo'', @level1type=N''TABLE'',@level1name=N''{1}''"
                    , nullToEmpty(table.getTableDesc()), table.getTableName());
        }
    }

    /**
     * 删除表主键：ALTER TABLE 表名 DROP CONSTRAINT 索引名称;
     * 删除索引：DROP INDEX 索引名称 ON 表名;
     * 添加表主键：ALTER TABLE 表名 ADD CONSTRAINT 索引名称 PRIMARY KEY (主键1，主键2);
     * 添加索引：CREATE [UNIQUE|NONCLUSTERED] INDEX 索引名称 ON 表名(主键1，主键2);
     */
    @Override
    public String generateIndexSql(LcdpTableBean table, LcdpTableIndexBean index) {
        String tableName = table.getTableName();
        if (primarykey.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_DELETE.equalsIgnoreCase(index.getIndexOperationType())) {
            return MessageFormat.format("ALTER TABLE {0} DROP CONSTRAINT {1}", tableName, index.getIndexName());
        } else if (!primarykey.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_DELETE.equalsIgnoreCase(index.getIndexOperationType())) {
            return MessageFormat.format("DROP INDEX {0} ON {1}", index.getIndexName(), tableName);
        } else if (primarykey.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(index.getIndexOperationType())) {
            return MessageFormat.format("ALTER TABLE {0} ADD CONSTRAINT {1} PRIMARY KEY ({2})", tableName, index.getIndexName(), index.getIndexField());
        } else if (unique.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(index.getIndexOperationType())) {
            return MessageFormat.format("CREATE UNIQUE INDEX {0} ON {1}({2})", index.getIndexName(), tableName, index.getIndexField());
        } else if (regular.name().equalsIgnoreCase(index.getIndexType()) && LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(index.getIndexOperationType())) {
            return MessageFormat.format("CREATE NONCLUSTERED INDEX {0} ON {1}({2})", index.getIndexName(), tableName, index.getIndexField());
        }
        return "";
    }

    /**
     * 删除表字段自动生成的默认值约束：EXEC dbo.p_dropcolumnconstraint @tableName=N'表名', @columnName=N'段名'
     * 修改表字段：ALTER TABLE 表名 MODIFY 字段名 字段类型 默认值 是否为空;
     * ALTER TABLE 表名 ADD DEFAULT (0) FOR 字段名 WITH VALUES
     * 删除表字段：ALTER TABLE 表名称 DROP 字段名称;
     * 添加表字段：ALTER TABLE 表名 ADD 字段名称 字段类型 默认值 是否为空;
     * 修改注释：EXEC sys.sp_addextendedproperty|sp_updateextendedproperty|sp_dropextendedproperty @name=N'MS_Description', @value=N'表注释'@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE',@level1name=N'表名', @level2type=N'COLUMN',@level2name=N'字段名'
     */
    @Override
    public List<String> generateColumnSql(LcdpTableBean table, LcdpTableFieldBean oldField, LcdpTableFieldBean field) {
        String tableName = table.getTableName();
        List<String> sqlList = new ArrayList<>();
        //修改字段
        if (LcdpConstant.FIELD_INDEX_OPS_UPDATE.equalsIgnoreCase(field.getFieldOperationType())) {
            //修改字段类型与是否为空
            if (!StringUtils.equals(parseFieldType(oldField), parseFieldType(field)) || !StringUtils.equals(oldField.getAllowNull(), field.getAllowNull())) {
                sqlList.add(MessageFormat.format("ALTER TABLE {0} ALTER COLUMN {1} {2} {3}"
                        , tableName, field.getFieldName(), parseFieldType(field), parseIsNULL(field)));
            }

            //删除默认值
            if (!StringUtils.equals(nullToEmpty(oldField.getDefaultValue()), nullToEmpty(field.getDefaultValue()))) {
                if (!StringUtils.isEmpty(oldField.getDefaultValue())) {
                    //sqlserver会自动生成默认值约束，更新字段类型与删除字段需要先删除该约束,就是删除默认值
                    sqlList.add(MessageFormat.format("EXEC dbo.p_dropcolumnconstraint @tableName=N''{0}'', @columnName=N''{1}''"
                            , tableName, field.getFieldName()));
                }

                if (!StringUtils.isEmpty(field.getDefaultValue())) {
                    sqlList.add(MessageFormat.format("ALTER TABLE {0} ADD DEFAULT ({1}) FOR {2} WITH VALUES"
                            , tableName, field.getDefaultValue(), field.getFieldName()));
                }
            }
            //修改注释
            if (!StringUtils.equals(nullToEmpty(oldField.getFieldComment()), nullToEmpty(field.getFieldComment()))) {
                if(StringUtils.isEmpty(oldField.getFieldComment())){
                    sqlList.add(MessageFormat.format("EXEC sys.sp_addextendedproperty @name=N''MS_Description'', @value=N''{0}'',@level0type=N''SCHEMA'', @level0name=N''dbo'', @level1type=N''TABLE'',@level1name=N''{1}'', @level2type=N''COLUMN'',@level2name=N''{2}''"
                            , nullToEmpty(field.getFieldComment()), tableName, field.getFieldName()));
                }else {
                    sqlList.add(MessageFormat.format("EXEC sys.sp_updateextendedproperty @name=N''MS_Description'', @value=N''{0}'',@level0type=N''SCHEMA'', @level0name=N''dbo'', @level1type=N''TABLE'',@level1name=N''{1}'', @level2type=N''COLUMN'',@level2name=N''{2}''"
                            , nullToEmpty(field.getFieldComment()), tableName, field.getFieldName()));
                }
            }
            //删除字段
        } else if (LcdpConstant.FIELD_INDEX_OPS_DELETE.equalsIgnoreCase(field.getFieldOperationType())) {//删除字段
            if (!StringUtils.isEmpty(oldField.getDefaultValue())) {
                //sqlserver会自动生成默认值约束，更新字段类型与删除字段需要先删除该约束,就是删除默认值
                sqlList.add(MessageFormat.format("EXEC dbo.p_dropcolumnconstraint @tableName=N''{0}'', @columnName=N''{1}''"
                        , tableName, field.getFieldName()));
            }
            sqlList.add(MessageFormat.format("ALTER TABLE {0} DROP COLUMN {1}"
                    , tableName, field.getFieldName()));
            //添加字段
        } else if (LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(field.getFieldOperationType())) {//添加字段
            sqlList.add(MessageFormat.format("ALTER TABLE {0} ADD {1} {2} {3} {4}"
                    , tableName, field.getFieldName(), parseFieldType(field), parseDefaultValue(field), parseIsNULL(field)));
            if (!StringUtils.isEmpty(field.getFieldComment())) {
                sqlList.add(MessageFormat.format("EXEC sys.sp_addextendedproperty @name=N''MS_Description'', @value=N''{0}'',@level0type=N''SCHEMA'', @level0name=N''dbo'', @level1type=N''TABLE'',@level1name=N''{1}'', @level2type=N''COLUMN'',@level2name=N''{2}''"
                        , nullToEmpty(field.getFieldComment()), tableName, field.getFieldName()));
            }
        }
        return sqlList;
    }

    /**
     * 创建表：CREATE TABLE 表名 (
     * 字段名1 字段类型 默认值 是否为空,
     * 字段名2 字段类型 默认值 是否为空,
     * .....
     * CONSTRAINT 主键名 PRIMARY KEY(字段1,字段2)
     * )
     * 修改表注释：EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'表注释'@level0type=N'SCHEMA', @level0name=N'dbo', @level1type=N'TABLE',@level1name=N'表名'
     * 修改字段注释：EXEC sys.sp_addextendedproperty @name=N''MS_Description'', @value=N''字段注释'',@level0type=N''SCHEMA'', @level0name=N''dbo'', @level1type=N''TABLE'',@level1name=N''表名'', @level2type=N''COLUMN'',@level2name=N''字段名''
     */
    @Override
    public List<String> generateCreateTableSql(LcdpTableBean table, List<LcdpTableFieldBean> fields, LcdpTableIndexBean primaryKey) {
        String tableName = table.getTableName();
        String tableDesc = table.getTableDesc();
        List<String> sqlList = new ArrayList<>();

        //创建表sql
        StringBuilder createSqlAppend = new StringBuilder(MessageFormat.format("CREATE TABLE {0} (\n", tableName));
        for (int j = 0; j < fields.size(); j++) {
            LcdpTableFieldBean field = fields.get(j);
            createSqlAppend.append(MessageFormat.format(" {0} {1} {2} {3} ", field.getFieldName(), parseFieldType(field), parseDefaultValue(field), parseIsNULL(field)));

            if (j == fields.size() - 1) {
                //添加主键
                if (!ObjectUtils.isEmpty(primaryKey)) {
                    createSqlAppend.append(",\n");
                    createSqlAppend.append(MessageFormat.format("CONSTRAINT \"{0}\" PRIMARY KEY ({1})\n", primaryKey.getIndexName(), primaryKey.getIndexField()));
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
            sqlList.add(MessageFormat.format("EXEC sys.sp_addextendedproperty @name=N''MS_Description'', @value=N''{0}'',@level0type=N''SCHEMA'', @level0name=N''dbo'', @level1type=N''TABLE'',@level1name=N''{1}''"
                    , nullToEmpty(table.getTableDesc()), table.getTableName()));
        }
        Optional.of(fields).orElse(Collections.emptyList()).stream().filter(field -> LcdpConstant.FIELD_INDEX_OPS_ADD.equalsIgnoreCase(field.getFieldOperationType()))
                .forEach(field -> {
                            if (!StringUtils.isEmpty(field.getFieldComment())) {
                                sqlList.add(MessageFormat.format("EXEC sys.sp_addextendedproperty @name=N''MS_Description'', @value=N''{0}'',@level0type=N''SCHEMA'', @level0name=N''dbo'', @level1type=N''TABLE'',@level1name=N''{1}'', @level2type=N''COLUMN'',@level2name=N''{2}''"
                                        , nullToEmpty(field.getFieldComment()), tableName, field.getFieldName()));
                            }
                        }
                );


        return sqlList;
    }

    @Override
    public String databaseTypeToFieldType(String databaseType) {
        switch (databaseType.toUpperCase()) {
            case "VARCHAR":
            case "NVARCHAR":
                return varchar.name();
            case "DATETIME":
            case "DATETIME2":
                return date.name();
            case "TEXT":
            case "LONGTEXT":
                return clob.name();
            case "DECIMAL":
            case "BIGINT":
            case "NUMERIC":
            case "INT":
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
            fieldType = "TEXT";
        } else if (number.name().equalsIgnoreCase(field.getFieldType())) {
            if (field.getPrecision() > 0 && field.getScale() > 0) {
                field.setFieldLength(field.getPrecision() + "," + field.getScale());
                fieldLength = "(" + field.getPrecision() + "," + field.getScale() + ")";
                fieldType = "DECIMAL" + fieldLength;
            } else if (field.getPrecision() > 0 && field.getScale() <= 0) {
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
    public String generateAlterViewSql(LcdpViewBean view) {
        return MessageFormat.format("ALTER VIEW {0} AS {1}", view.getTableName(), view.getSelectStatement());
    }

    @Override
    public String parseSQLException(String message) {
        if(StringUtils.containsIgnoreCase(message,"permission denied")){
            return I18nHelper.getMessage("LCDP.MODULE.DATABASE.TIP.INSUFFICIENT_PRIVILEGES");
        }
        return message;
    }
}
