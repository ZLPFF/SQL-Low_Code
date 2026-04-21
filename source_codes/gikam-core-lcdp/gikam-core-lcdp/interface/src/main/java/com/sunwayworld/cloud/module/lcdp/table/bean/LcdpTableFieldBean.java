package com.sunwayworld.cloud.module.lcdp.table.bean;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.alibaba.fastjson.annotation.JSONField;
import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractInsertable;
import com.sunwayworld.framework.support.domain.Insertable;

/**
 * 低代码平台表字段
 *
 * @author shixw@sunwayworld.com
 * @date 2022-10-21
 */
@Table("T_LCDP_TABLE_FIELD")
public class LcdpTableFieldBean extends AbstractInsertable<Long> implements Insertable<Long> {
    @Transient
    private static final long serialVersionUID = -3571294725230509239L;

    @Id
    private Long id;// 主键
    private Long tableId;// 表ID
    private String tableName;// 表名称
    private String fieldOperationType;// 字段操作类型(add、delete、update)
    private String fieldName;// 字段名
    private String fieldComment;// 字段注释
    private String fieldType;// 字段类型 (varchar字符串、number数字、clob大文本、date日期)
    private String fieldLength;// 字段长度
    private Integer precision;// 精度
    private Integer scale;// 刻度
    private String allowNull;// 是否可为空
    private String defaultValue;// 默认值
    private String createdById;// 制单人编码
    private String createdByName;// 制单人名称
    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;// 制单时间
    private String createdByOrgId;// 制单人单位编码
    private String createdByOrgName;// 制单人单位名称
    
    public static LcdpTableFieldBean of(String fieldName, String fieldComment,
            String fieldType, String fieldLength, Integer precision,
            Integer scale, String allowNull, String defaultValue) {
        LcdpTableFieldBean field = new LcdpTableFieldBean();
        field.setFieldName(fieldName);
        field.setFieldComment(fieldComment);
        field.setFieldType(fieldType);
        field.setFieldLength(fieldLength);
        field.setPrecision(precision);
        field.setScale(scale);
        field.setAllowNull(allowNull);
        field.setDefaultValue(defaultValue);
        
        return field;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

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

    public String getFieldOperationType() {
        return fieldOperationType;
    }

    public void setFieldOperationType(String fieldOperationType) {
        this.fieldOperationType = fieldOperationType;
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

    public String getCreatedById() {
        return createdById;
    }

    public void setCreatedById(String createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreatedByOrgId() {
        return createdByOrgId;
    }

    public void setCreatedByOrgId(String createdByOrgId) {
        this.createdByOrgId = createdByOrgId;
    }

    public String getCreatedByOrgName() {
        return createdByOrgName;
    }

    public void setCreatedByOrgName(String createdByOrgName) {
        this.createdByOrgName = createdByOrgName;
    }
}
