package com.sunwayworld.cloud.module.lcdp.table.helper;

import java.text.MessageFormat;
import java.util.List;
import java.util.StringJoiner;
import java.util.StringTokenizer;

import static com.sunwayworld.cloud.module.lcdp.table.helper.FieldType.clob;
import static com.sunwayworld.cloud.module.lcdp.table.helper.FieldType.varchar;

import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableIndexBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;

/**
 * @author yangsz@sunway.com 2022-10-21
 */
public interface SqlGenerateHelper {

    /**
     * 生成表注释修改语句
     */
    String generateTableCommentSql(LcdpTableBean table, LcdpTableDTO physicalTable);

    /**
     * 生成索引创建删除语句
     */
    String generateIndexSql(LcdpTableBean table, LcdpTableIndexBean index);

    /**
     * 生成表字段创建删除语句
     */
    List<String> generateColumnSql(LcdpTableBean table, LcdpTableFieldBean oldField, LcdpTableFieldBean field);

    /**
     * 生成创建表语句，包括表结构、注解、主键
     */
    List<String> generateCreateTableSql(LcdpTableBean table, List<LcdpTableFieldBean> fields, LcdpTableIndexBean primaryKey);

    /**
     * 生成删除表语句
     */
    default String generateDeleteSql(LcdpTableBean table) {
        return MessageFormat.format("DROP TABLE {0}", table.getTableName());
    }

    /**
     * 数据库字段类型转为平台字段类型
     */
    String databaseTypeToFieldType(String databaseType);

    /**
     * null输出为字符串
     */
    default String nullToEmpty(String source) {
        return StringUtils.isEmpty(source) ? "" : source;
    }

    /**
     * 解析是否为空
     * 0: NOT NULL
     * 1: NULL
     * 其他：空（默认或不修改）
     */
    default String parseIsNULL(LcdpTableFieldBean field) {
        if (LcdpConstant.FIELD_ALLOWNULL_YES.equals(field.getAllowNull())) {
            return "NULL";
        } else if (LcdpConstant.FIELD_ALLOWNULL_NO.equals(field.getAllowNull())) {
            return "NOT NULL";
        }
        return "";
    }

    /**
     * 解析默认值
     * 空：空
     * 非空：DEFAULT 值(字符串格式：'值'，数字格式：数字值)
     */
    default String parseDefaultValue(LcdpTableFieldBean field) {
        //字符类型默认值单引号包裹
        if (!StringUtils.isEmpty(field.getDefaultValue()) && (varchar.name().equals(field.getFieldType()) || clob.name().equals(field.getFieldType()))) {
            field.setDefaultValue("'" + field.getDefaultValue().trim().replaceAll("'", "") + "'");
        }
        return StringUtils.isEmpty(field.getDefaultValue()) ? "" : MessageFormat.format("DEFAULT {0}", field.getDefaultValue());
    }

    /**
     * 根据LcdpTableFieldBean生成字段类型
     * 平台类型：    varchar、date、clob、number
     * oracle:varchar2()、date、clob、number()
     * pgsql:varchar()、date、text、decimal()、numeric()
     * sqlserver:varchar()、datetime、text、decimal()、numeric()
     * mysql: varchar()、datetime、text、decimal()、bigint()
     */
    String parseFieldType(LcdpTableFieldBean field);

    /**
     * 将索引字段加上保留标识符（字段1,字段2 -> "字段1","字段2"）
     */
    default String parseIndexField(LcdpTableIndexBean index) {
        String indexField = index.getIndexField();

        if (ObjectUtils.isEmpty(indexField)) {
            return "";
        }

        String reservedIdentifier = getReservedIdentifier();

        StringTokenizer indexFieldTokenizer = new StringTokenizer(indexField, ",");

        StringJoiner stringJoiner = new StringJoiner(",");

        while (indexFieldTokenizer.hasMoreTokens()) {
            stringJoiner.add(reservedIdentifier + indexFieldTokenizer.nextToken() + reservedIdentifier);
        }

        return stringJoiner.toString();
    }

    /**
     * 保留标识符
     */
    default String getReservedIdentifier() {
        return "\"";
    }

    default String generateCreateViewSql(LcdpViewBean view) {
        return MessageFormat.format("CREATE VIEW {0} AS {1}", view.getTableName(), view.getSelectStatement());
    }

    default String generateAlterViewSql(LcdpViewBean view) {
        return MessageFormat.format("CREATE OR REPLACE VIEW {0} AS {1}", view.getTableName(), view.getSelectStatement());
    }

    default String generateDropViewSql(LcdpViewBean view) {
        return MessageFormat.format("DROP VIEW {0}", view.getViewName());
    }

    /**
     * 数据库sql异常对应国际化提示信息
     */
    String parseSQLException(String message);
}
