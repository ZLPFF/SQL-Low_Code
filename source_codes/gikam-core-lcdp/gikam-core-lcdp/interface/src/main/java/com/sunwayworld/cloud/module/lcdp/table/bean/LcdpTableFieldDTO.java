package com.sunwayworld.cloud.module.lcdp.table.bean;

import com.sunwayworld.framework.support.domain.AbstractBaseData;

public class LcdpTableFieldDTO extends AbstractBaseData {

    private static final long serialVersionUID = 2441958273054731547L;

    private String tableName;//表名
    private String fieldName;// 字段名
    private String fieldComment = "";// 字段注释
    private String fieldType;// 字段类型 (varchar字符串、number数字、clob大文本、date日期)
    private String fieldLength = "";// 字段长度
    private Integer precision;// 精度
    private Integer scale;// 刻度
    private String allowNull;// 是否可为空
    private String defaultValue = "";// 默认值


    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldComment() {
        return fieldComment;
    }

    public void setFieldComment(String fieldComment) {
        this.fieldComment = fieldComment;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldLength() {
        return fieldLength;
    }

    public void setFieldLength(String fieldLength) {
        this.fieldLength = fieldLength;
    }

    public String getAllowNull() {
        return allowNull;
    }

    public void setAllowNull(String allowNull) {
        this.allowNull = allowNull;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }
}
