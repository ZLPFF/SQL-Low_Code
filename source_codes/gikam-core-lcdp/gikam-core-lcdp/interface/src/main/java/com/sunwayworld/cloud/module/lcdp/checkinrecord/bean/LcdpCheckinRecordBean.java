package com.sunwayworld.cloud.module.lcdp.checkinrecord.bean;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.alibaba.fastjson.annotation.JSONField;
import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractInsertable;
import com.sunwayworld.framework.support.domain.Insertable;

/**
 * 迁入记录表
 *
 * @author liuxia@sunwayworld.com
 * @date 2023-04-10
 */
@Table("T_LCDP_CHECKIN_RECORD")
public class LcdpCheckinRecordBean extends AbstractInsertable<Long> implements Insertable<Long> {

    @Transient
    private static final long serialVersionUID = -82566196618711043L;

    @Id
    private Long id;// 主键
    private String checkinNo;// 迁入编号
    private String checkinNote;// 迁入说明
    private String checkinStatus;// 迁入状态
    private String checkinWay;// 迁入方式
    private Long checkoutRecordId;// 迁出记录ID
    private String checkoutRecordNo;// 迁出记录编号
    private String checkoutSysName;//迁出系统名称
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkinTime;// 迁入时间
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

    public String getCheckinNo() {
        return checkinNo;
    }

    public void setCheckinNo(String checkinNo) {
        this.checkinNo = checkinNo;
    }

    public String getCheckinNote() {
        return checkinNote;
    }

    public void setCheckinNote(String checkinNote) {
        this.checkinNote = checkinNote;
    }

    public String getCheckinStatus() {
        return checkinStatus;
    }

    public void setCheckinStatus(String checkinStatus) {
        this.checkinStatus = checkinStatus;
    }

    public String getCheckinWay() {
        return checkinWay;
    }

    public void setCheckinWay(String checkinWay) {
        this.checkinWay = checkinWay;
    }

    public Long getCheckoutRecordId() {
        return checkoutRecordId;
    }

    public void setCheckoutRecordId(Long checkoutRecordId) {
        this.checkoutRecordId = checkoutRecordId;
    }

    public String getCheckoutRecordNo() {
        return checkoutRecordNo;
    }

    public void setCheckoutRecordNo(String checkoutRecordNo) {
        this.checkoutRecordNo = checkoutRecordNo;
    }

    public String getCheckoutSysName() {
        return checkoutSysName;
    }

    public void setCheckoutSysName(String checkoutSysName) {
        this.checkoutSysName = checkoutSysName;
    }

    public LocalDateTime getCheckinTime() {
        return checkinTime;
    }

    public void setCheckinTime(LocalDateTime checkinTime) {
        this.checkinTime = checkinTime;
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
