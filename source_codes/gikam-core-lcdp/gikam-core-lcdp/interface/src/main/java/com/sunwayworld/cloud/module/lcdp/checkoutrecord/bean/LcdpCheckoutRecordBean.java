package com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean;

import com.sunwayworld.framework.data.annotation.Clob;
import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.Insertable;
import com.sunwayworld.framework.support.domain.AbstractInsertable;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * 迁出记录表
 * 
 * @author liuxia@sunwayworld.com
 * @date 2023-04-10
 */
@Table("T_LCDP_CHECKOUT_RECORD")
public class LcdpCheckoutRecordBean extends AbstractInsertable<Long> implements Insertable<Long> {

    @Transient
    private static final long serialVersionUID = -82566196618711043L;

    @Id
    private Long id;// 主键
    private String checkoutNo;// 迁出编号
    private String checkoutNote;// 迁出说明
    private Long checkinRecordId;//迁入记录ID  用于迁入再迁出的数据
    @Transient
    private String actionStatus; //迁出操作状态
    @Clob
    private String content;// 迁出内容
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

    public String getCheckoutNo() {
        return checkoutNo;
    }

    public void setCheckoutNo(String checkoutNo) {
        this.checkoutNo = checkoutNo;
    }

    public String getCheckoutNote() {
        return checkoutNote;
    }

    public void setCheckoutNote(String checkoutNote) {
        this.checkoutNote = checkoutNote;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCheckinRecordId() {
        return checkinRecordId;
    }

    public void setCheckinRecordId(Long checkinRecordId) {
        this.checkinRecordId = checkinRecordId;
    }

    public String getActionStatus() {
        return actionStatus;
    }

    public void setActionStatus(String actionStatus) {
        this.actionStatus = actionStatus;
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
