package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;

import com.alibaba.fastjson.annotation.JSONField;
import com.sunwayworld.framework.data.annotation.Clob;
import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.NotNull;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractInsertable;
import com.sunwayworld.framework.support.domain.Insertable;

/**
 * 低代码平台资源表
 *
 * @author liuxia@@sunwayworld.com
 * @date 2022-10-17
 */
@Table("T_LCDP_RESOURCE")
public class LcdpResourceBean extends AbstractInsertable<Long> implements Insertable<Long> {

    @Transient
    private static final long serialVersionUID = -5161075318706878251L;
    @Id
    private Long id;// 主键
    private String resourceName;// 资源名称
    private String resourceDesc;// 资源描述
    private String resourceCategory;// 资源类型(模块分类、模块、页面、前端脚本、后端脚本)
    private Long parentId;// 父ID
    @NotNull(defaultValue = "1")
    private Long orderNo;// 排序码
    @Clob
    private String content;// 内容
    @Clob
    private String classContent;// 类内容(只有当资源类型为后端脚本时存储真正的java类代码)
    private Long effectVersion;// 生效版本
    private String path;// 资源路径
    private String className; //类名
    @NotNull(defaultValue = "0")
    private String deleteFlag;//删除标记
    private String deletedById;//删除人编码
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deleteTime;// 删除时间
    private String dataType;//资源数据类型

    private String checkoutUserId; // 检出用户编码
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkoutTime;//检出时间

    @Transient
    private String checkoutUserName;//检出人名称

    @Transient
    private String scriptSourceType;//java脚本创建条件 table  view

    private String scriptType;//java脚本类型 service utils
    private Long moduleId;//模块ID
    private Long categoryId;//分类ID
    private String createdById;// 制单人编码
    private String createdByName;// 制单人名称
    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;// 制单时间
    private String createdByOrgId;// 制单人单位编码
    private String createdByOrgName;// 制单人单位名称
    
    @Transient
    private List<LcdpModulePageCompBean> components; // 页面组件
    @Transient
    private Map<String, Map<String, String>> i18n; // 页面国际化
    @Transient
    private List<String> dependentI18n; // 页面系统国际化依赖
    
    private String dependentTable; // 模块关联表
    private String eventName;//事件名称

    private String lcdpFilePath;//低代码文件关联路径
    
    private Long modifyVersion; // 反写历史表中的修改版本

    @NotNull(defaultValue = "0")
    private Long versionOffset; // 版本偏差（删除java源代码后再新增时赋值）

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceDesc() {
        return resourceDesc;
    }

    public void setResourceDesc(String resourceDesc) {
        this.resourceDesc = resourceDesc;
    }

    public String getResourceCategory() {
        return resourceCategory;
    }

    public void setResourceCategory(String resourceCategory) {
        this.resourceCategory = resourceCategory;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Long orderNo) {
        this.orderNo = orderNo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getClassContent() {
        return classContent;
    }

    public void setClassContent(String classContent) {
        this.classContent = classContent;
    }

    public Long getEffectVersion() {
        return effectVersion;
    }

    public void setEffectVersion(Long effectVersion) {
        this.effectVersion = effectVersion;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(String deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public String getDeletedById() {
        return deletedById;
    }

    public void setDeletedById(String deletedById) {
        this.deletedById = deletedById;
    }

    public LocalDateTime getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(LocalDateTime deleteTime) {
        this.deleteTime = deleteTime;
    }

    public LocalDateTime getCheckoutTime() {
        return checkoutTime;
    }

    public void setCheckoutTime(LocalDateTime checkoutTime) {
        this.checkoutTime = checkoutTime;
    }

    public String getCheckoutUserName() {
        return checkoutUserName;
    }

    public void setCheckoutUserName(String checkoutUserName) {
        this.checkoutUserName = checkoutUserName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getScriptType() {
        return scriptType;
    }

    public String getScriptSourceType() {
        return scriptSourceType;
    }

    public void setScriptSourceType(String scriptSourceType) {
        this.scriptSourceType = scriptSourceType;
    }

    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
    }

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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

    public List<LcdpModulePageCompBean> getComponents() {
        return components;
    }

    public void setComponents(List<LcdpModulePageCompBean> components) {
        this.components = components;
    }

    public Map<String, Map<String, String>> getI18n() {
        return i18n;
    }

    public void setI18n(Map<String, Map<String, String>> i18n) {
        this.i18n = i18n;
    }

    public List<String> getDependentI18n() {
        return dependentI18n;
    }

    public void setDependentI18n(List<String> dependentI18n) {
        this.dependentI18n = dependentI18n;
    }


    public String getDependentTable() {
        return dependentTable;
    }

    public void setDependentTable(String dependentTable) {
        this.dependentTable = dependentTable;
    }
    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getLcdpFilePath() {
        return lcdpFilePath;
    }

    public void setLcdpFilePath(String lcdpFilePath) {
        this.lcdpFilePath = lcdpFilePath;
    }

    public String getCheckoutUserId() {
        return checkoutUserId;
    }

    public void setCheckoutUserId(String checkoutUserId) {
        this.checkoutUserId = checkoutUserId;
    }

    public Long getModifyVersion() {
        return modifyVersion;
    }

    public void setModifyVersion(Long modifyVersion) {
        this.modifyVersion = modifyVersion;
    }

    public Long getVersionOffset() {
        return versionOffset;
    }

    public void setVersionOffset(Long versionOffset) {
        this.versionOffset = versionOffset;
    }
}
