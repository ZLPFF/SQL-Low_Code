package com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean;

import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.Insertable;
import com.sunwayworld.framework.support.domain.AbstractInsertable;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * 迁出记录日志表
 * 
 * @author liuxia@sunwayworld.com
 * @date 2023-04-10
 */
@Table("T_LCDP_CRD_LOG")
public class LcdpCrdLogBean extends AbstractInsertable<Long> implements Insertable<Long> {

    @Transient
    private static final long serialVersionUID = -82566196618711043L;

    @Id
    private Long id;// 主键
    private Long checkoutRecordId;// 迁出记录表ID
    private Long checkoutConfigId;//迁出配置表ID
    private String checkoutTarget;// 迁出目的
    private String checkoutType;// 迁出类型 迁出 checkout  导出 export
    private String checkoutStatus;// 迁出状态 0是失败 1是成功

    private String systemName;//系统名称
    private String createdById;// 制单人编码
    private String createdByName;// 制单人名称
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
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

    public Long getCheckoutRecordId() {
        return checkoutRecordId;
    }

    public void setCheckoutRecordId(Long checkoutRecordId) {
        this.checkoutRecordId = checkoutRecordId;
    }

    public Long getCheckoutConfigId() {
        return checkoutConfigId;
    }

    public void setCheckoutConfigId(Long checkoutConfigId) {
        this.checkoutConfigId = checkoutConfigId;
    }

    public String getCheckoutTarget() {
        return checkoutTarget;
    }

    public void setCheckoutTarget(String checkoutTarget) {
        this.checkoutTarget = checkoutTarget;
    }

    public String getCheckoutType() {
        return checkoutType;
    }

    public void setCheckoutType(String checkoutType) {
        this.checkoutType = checkoutType;
    }

    public String getCheckoutStatus() {
        return checkoutStatus;
    }

    public void setCheckoutStatus(String checkoutStatus) {
        this.checkoutStatus = checkoutStatus;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
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
