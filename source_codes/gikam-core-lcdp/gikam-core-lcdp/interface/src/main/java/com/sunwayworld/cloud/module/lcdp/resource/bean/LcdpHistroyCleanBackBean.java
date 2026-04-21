package com.sunwayworld.cloud.module.lcdp.resource.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.sunwayworld.framework.data.annotation.Clob;
import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractInsertable;
import com.sunwayworld.framework.support.domain.Insertable;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 历史表瘦身备份表
 * 
 * @author liuxia@sunwayworld.com
 * @date 2025-08-13
 */
@Table("T_LCDP_HISTROY_CLEAN_BACK")
public class LcdpHistroyCleanBackBean extends AbstractInsertable<Long> implements Insertable<Long> {

    @Transient
    private static final long serialVersionUID = -82566196618711043L;

    @Id
    private Long id;// 主键
    private Long resourceId;// 资源ID
    private String resourceName;// 资源名称
    private String resourceCategory;// 资源类型
    @Clob
    private String content;// 内容
    @Clob
    private String classContent;// 类内容
    private Long version;// 版本
    private String submitFlag;// 是否提交
    private String effectFlag;// 是否生效
    private String path;// 资源路径
    private Long modifyVersion;// 修改版本
    private Long compiledVersion;// 已编译版本
    private String deleteFlag;// 删除标记
    private String deletedById;// 删除人编码
    private LocalDateTime deleteTime;// 删除时间
    private String scriptType;// 脚本类型
    private String eventName;// 事件名称
    private String lcdpFilePath;// 低代码文件关联路径
    private Long versionOffset;// 版本偏差
    private String createdById;// 制单人编码
    private String createdByName;// 制单人名称
    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;// 制单时间
    private String createdByOrgId;// 制单人单位编码
    private String createdByOrgName;// 制单人单位名称

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceCategory() {
        return resourceCategory;
    }

    public void setResourceCategory(String resourceCategory) {
        this.resourceCategory = resourceCategory;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getSubmitFlag() {
        return submitFlag;
    }

    public void setSubmitFlag(String submitFlag) {
        this.submitFlag = submitFlag;
    }

    public String getEffectFlag() {
        return effectFlag;
    }

    public void setEffectFlag(String effectFlag) {
        this.effectFlag = effectFlag;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getModifyVersion() {
        return modifyVersion;
    }

    public void setModifyVersion(Long modifyVersion) {
        this.modifyVersion = modifyVersion;
    }

    public Long getCompiledVersion() {
        return compiledVersion;
    }

    public void setCompiledVersion(Long compiledVersion) {
        this.compiledVersion = compiledVersion;
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

    public String getScriptType() {
        return scriptType;
    }

    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
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

    public Long getVersionOffset() {
        return versionOffset;
    }

    public void setVersionOffset(Long versionOffset) {
        this.versionOffset = versionOffset;
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
