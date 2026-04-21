package com.sunwayworld.cloud.module.lcdp.table.bean;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.alibaba.fastjson.annotation.JSONField;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.tree.bean.AbstractTreeNode;

public class LcdpTableFieldGroupDTO extends AbstractTreeNode {
    @Transient
    private static final long serialVersionUID = 4330758545933077524L;

    private String id;// 主键
    private String parentId;// 父级ID
    private String fieldName;// 字段名称
    private String fieldComment;// 字段注释
    private String fieldType;// 字段类型 (varchar字符串、number数字、clob大文本、date日期)
    private String fieldLength;// 字段长度
    private Long precision;// 精度
    private Long scale;// 刻度
    private String allowNull;// 是否可为空
    private String defaultValue;// 默认值
    private String fieldStatus;// 字段状态:sys/other
    private Long orderNo;// 排序码
    private Long groupFlag;// 是否为分组0/1
    private String i18nCode;// 国际化编码
    private String createdById;// 制单人编码
    private String createdByName;// 制单人名称
    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;// 制单时间
    private String createdByOrgId;// 制单人单位编码
    private String createdByOrgName;// 制单人单位名称
    private String groupType;// 分组类型
    
    private String groupName;// 用来区分那个分组的
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getParentId() {
        return parentId;
    }
    public void setParentId(String parentId) {
        this.parentId = parentId;
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
    public Long getPrecision() {
        return precision;
    }
    public void setPrecision(Long precision) {
        this.precision = precision;
    }
    public Long getScale() {
        return scale;
    }
    public void setScale(Long scale) {
        this.scale = scale;
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
    
    public String getFieldStatus() {
        return fieldStatus;
    }
    public void setFieldStatus(String fieldStatus) {
        this.fieldStatus = fieldStatus;
    }
    public Long getOrderNo() {
        return orderNo;
    }
    public void setOrderNo(Long orderNo) {
        this.orderNo = orderNo;
    }
    
    public Long getGroupFlag() {
        return groupFlag;
    }
    public void setGroupFlag(Long groupFlag) {
        this.groupFlag = groupFlag;
    }
    
    public String getI18nCode() {
        return i18nCode;
    }
    public void setI18nCode(String i18nCode) {
        this.i18nCode = i18nCode;
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
    public String getGroupType() {
        return groupType;
    }
    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }
    public String getGroupName() {
        return groupName;
    }
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

}