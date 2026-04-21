package com.sunwayworld.cloud.module.lcdp.table.bean;

import java.time.LocalDateTime;

import com.sunwayworld.framework.support.tree.bean.AbstractTreeNode;

public class LcdpTableViewTreeNodeDTO extends AbstractTreeNode {
    private static final long serialVersionUID = -8958793654973894766L;

    private String id;

    private String name; // 名称

    private String desc; // 描述

    private String type; // 类型（同资源的 resourceCategory，只有文件夹）
    
    private Long version; // 版本
    
    private String checkOutFlag; // 当前登录人检出状态 （0-否 1-是）
    
    private String otherUserCheckOutFlag; // 被其他人检出状态（0-否 1-是）
    
    private String checkoutUserId; // 检出用户编码
    private String checkoutUserName;//检出人名称

    private LocalDateTime checkoutTime;//检出时间

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getCheckOutFlag() {
        return checkOutFlag;
    }

    public void setCheckOutFlag(String checkOutFlag) {
        this.checkOutFlag = checkOutFlag;
    }

    public String getOtherUserCheckOutFlag() {
        return otherUserCheckOutFlag;
    }

    public void setOtherUserCheckOutFlag(String otherUserCheckOutFlag) {
        this.otherUserCheckOutFlag = otherUserCheckOutFlag;
    }

    public String getCheckoutUserId() {
        return checkoutUserId;
    }

    public void setCheckoutUserId(String checkoutUserId) {
        this.checkoutUserId = checkoutUserId;
    }

    public String getCheckoutUserName() {
        return checkoutUserName;
    }

    public void setCheckoutUserName(String checkoutUserName) {
        this.checkoutUserName = checkoutUserName;
    }

    public LocalDateTime getCheckoutTime() {
        return checkoutTime;
    }

    public void setCheckoutTime(LocalDateTime checkoutTime) {
        this.checkoutTime = checkoutTime;
    }
}
