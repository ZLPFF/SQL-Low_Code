package com.sunwayworld.cloud.module.lcdp.importrecord.bean;

import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.Insertable;
import com.sunwayworld.framework.support.domain.AbstractInsertable;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * 导入记录详细表
 * 
 * @author liuxia@sunwayworld.com
 * @date 2025-09-15
 */
@Table("T_LCDP_R_IMPORT_RECORD_DETAIL")
public class LcdpRImportRecordDetailBean extends AbstractInsertable<Long> implements Insertable<Long> {

    @Transient
    private static final long serialVersionUID = -82566196618711043L;

    @Id
    private Long id;// 主键
    private Long recordId;// 记录表ID
    private String resourceId;// 迁入后资源Id（资源或者表）
    private Long resourceVersion;// 迁入后资源版本
    private String resourceCategory;// 资源类型
    private String deleteFlag;// 是否执行的是删除操作
    private String createdById;// 制单人编码
    private String createdByName;// 制单人名称
    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;// 制单时间
    private String createdByOrgId;// 制单人单位编码
    private String createdByOrgName;// 制单人单位名称

    private String rollbackable;//是否可回滚

    private String resourceOperations;//资源操作


    @Transient
    private LocalDateTime recordTime;//记录时间

    private Long operationVersion;//操作的版本






    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public Long getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(Long resourceVersion) {
        this.resourceVersion = resourceVersion;
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

    public String getResourceCategory() {
        return resourceCategory;
    }

    public void setResourceCategory(String resourceCategory) {
        this.resourceCategory = resourceCategory;
    }

    public String getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(String deleteFlag) {
        this.deleteFlag = deleteFlag;
    }


    public String getRollbackable() {
        return rollbackable;
    }

    public void setRollbackable(String rollbackable) {
        this.rollbackable = rollbackable;
    }

    public String getResourceOperations() {
        return resourceOperations;
    }

    public void setResourceOperations(String resourceOperations) {
        this.resourceOperations = resourceOperations;
    }

    public LocalDateTime getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
    }

    public Long getOperationVersion() {
        return operationVersion;
    }

    public void setOperationVersion(Long operationVersion) {
        this.operationVersion = operationVersion;
    }
}
