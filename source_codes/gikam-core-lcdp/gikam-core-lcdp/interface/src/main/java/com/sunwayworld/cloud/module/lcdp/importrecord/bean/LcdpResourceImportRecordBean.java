package com.sunwayworld.cloud.module.lcdp.importrecord.bean;

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
 * 导入记录表
 * 
 * @author liuixia@sunwayworld.com
 * @date 2022-11-14
 */
@Table("T_LCDP_RESOURCE_IMPORT_RECORD")
public class LcdpResourceImportRecordBean extends AbstractInsertable<Long> implements Insertable<Long> {

    @Transient
    private static final long serialVersionUID = -82566196618711043L;

    @Id
    private Long id;// 主键
    private String importLog;// 导入日志
    private String exportLog;// 导出日志
    @Clob
    private String importContent;// 导入内容（导入树的JSON）
    private String createdById;// 制单人编码
    private String createdByName;// 制单人名称
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;// 制单时间
    private String createdByOrgId;// 制单人单位编码
    private String createdByOrgName;// 制单人单位名称

    private String hasRollbackFlag;//回滚标记

    private String rollbackable;//是否可以回滚

    private Long sysClientCssVersion; //全局CSS版本

    private Long sysClientJsVersion;//全局JS版本
    
    private Long submitLogId; //提交日志ID

    private String cssOperation;
    private String jsOperation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImportLog() {
        return importLog;
    }

    public void setImportLog(String importLog) {
        this.importLog = importLog;
    }

    public String getExportLog() {
        return exportLog;
    }

    public void setExportLog(String exportLog) {
        this.exportLog = exportLog;
    }

    public String getImportContent() {
        return importContent;
    }

    public void setImportContent(String importContent) {
        this.importContent = importContent;
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

    public String getHasRollbackFlag() {
        return hasRollbackFlag;
    }

    public void setHasRollbackFlag(String hasRollbackFlag) {
        this.hasRollbackFlag = hasRollbackFlag;
    }

    public String getRollbackable() {
        return rollbackable;
    }

    public void setRollbackable(String rollbackable) {
        this.rollbackable = rollbackable;
    }

    public Long getSysClientCssVersion() {
        return sysClientCssVersion;
    }

    public void setSysClientCssVersion(Long sysClientCssVersion) {
        this.sysClientCssVersion = sysClientCssVersion;
    }

    public Long getSysClientJsVersion() {
        return sysClientJsVersion;
    }

    public void setSysClientJsVersion(Long sysClientJsVersion) {
        this.sysClientJsVersion = sysClientJsVersion;
    }

    public Long getSubmitLogId() {
        return submitLogId;
    }

    public void setSubmitLogId(Long submitLogId) {
        this.submitLogId = submitLogId;
    }

    public String getCssOperation() {
        return cssOperation;
    }

    public void setCssOperation(String cssOperation) {
        this.cssOperation = cssOperation;
    }

    public String getJsOperation() {
        return jsOperation;
    }

    public void setJsOperation(String jsOperation) {
        this.jsOperation = jsOperation;
    }
}
