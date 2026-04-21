package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractPersistable;
import com.sunwayworld.framework.support.domain.Persistable;

/**
 * 低代码平台资源检出记录表
 * 
 * @author liuxia@sunwayworld.com
 * @date 2023-03-01
 */
@Table("T_LCDP_RS_CHECKOUT_RECORD")
public class LcdpResourceCheckoutRecordBean extends AbstractPersistable<Long> implements Persistable<Long> {

    @Transient
    private static final long serialVersionUID = -82566196618711043L;

    @Id
    private Long id;// 主键
    private String checkoutUserId;// 检出用户ID
    private String checkoutUserName;// 检出用户名称
    private Long resourceId;// 资源Id
    private String resourceName;// 资源名称
    private String resourceDesc;// 资源描述
    private String resourceCategory;// 资源类型(模块分类、模块、页面、前端脚本、后端脚本)
    private Long parentId;//父ID
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkoutTime;//检出时间

    private String tableName;//表名
    private String tableDesc;//表描述



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getResourceDesc() {
        return resourceDesc;
    }

    public void setResourceDesc(String resourceDesc) {
        this.resourceDesc = resourceDesc;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getResourceCategory() {
        return resourceCategory;
    }

    public void setResourceCategory(String resourceCategory) {
        this.resourceCategory = resourceCategory;
    }

    public LocalDateTime getCheckoutTime() {
        return checkoutTime;
    }

    public void setCheckoutTime(LocalDateTime checkoutTime) {
        this.checkoutTime = checkoutTime;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableDesc() {
        return tableDesc;
    }

    public void setTableDesc(String tableDesc) {
        this.tableDesc = tableDesc;
    }
}
