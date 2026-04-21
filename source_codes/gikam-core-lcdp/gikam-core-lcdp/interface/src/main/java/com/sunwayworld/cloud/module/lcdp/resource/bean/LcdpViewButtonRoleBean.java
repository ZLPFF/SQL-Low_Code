package com.sunwayworld.cloud.module.lcdp.resource.bean;

import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.Insertable;
import com.sunwayworld.framework.support.domain.AbstractInsertable;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * 页面按钮权限
 * 
 * @author liuxia@sunwayworld.com
 * @date 2025-04-15
 */
@Table("T_LCDP_VIEW_BUTTON_ROLE")
public class LcdpViewButtonRoleBean extends AbstractInsertable<Long> implements Insertable<Long> {

    @Transient
    private static final long serialVersionUID = -82566196618711043L;

    @Id
    private Long id;// 主键
    private String menuId;//菜单编码
    private Long resourceId;// 资源ID
    private Long resourceHistoryId;// 资源历史表ID
    private String resourceName;// 资源名称
    private Long effectVersion;// 生效版本
    private String buttonDataId;// 按钮数据ID
    private String buttonId;// 按钮ID
    private String buttonParentId;// 按钮父节点ID(如果遍历得到有tabpanel则是tablePanel)
    private String buttonParentName;// 按钮父节点ID(如果遍历得到有tabpanel则是tablePanel)
    private String visibleRoles;//
    private String buttonName;
    private String tablePanelName;
    private String createdById;// 制单人编码
    private String createdByName;// 制单人名称
    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;// 制单时间
    private String createdByOrgId;// 制单人单位编码
    private String createdByOrgName;// 制单人单位名称

    private Long parentId;//一个页面可能有多个父页面

    private String resourceDesc;// 资源描述

    private String path;// 资源路径



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getResourceHistoryId() {
        return resourceHistoryId;
    }

    public void setResourceHistoryId(Long resourceHistoryId) {
        this.resourceHistoryId = resourceHistoryId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public Long getEffectVersion() {
        return effectVersion;
    }

    public void setEffectVersion(Long effectVersion) {
        this.effectVersion = effectVersion;
    }

    public String getButtonDataId() {
        return buttonDataId;
    }

    public void setButtonDataId(String buttonDataId) {
        this.buttonDataId = buttonDataId;
    }

    public String getButtonId() {
        return buttonId;
    }

    public void setButtonId(String buttonId) {
        this.buttonId = buttonId;
    }

    public String getButtonParentId() {
        return buttonParentId;
    }

    public void setButtonParentId(String buttonParentId) {
        this.buttonParentId = buttonParentId;
    }

    public String getButtonParentName() {
        return buttonParentName;
    }

    public void setButtonParentName(String buttonParentName) {
        this.buttonParentName = buttonParentName;
    }

    public String getButtonName() {
        return buttonName;
    }

    public void setButtonName(String buttonName) {
        this.buttonName = buttonName;
    }

    public String getTablePanelName() {
        return tablePanelName;
    }

    public void setTablePanelName(String tablePanelName) {
        this.tablePanelName = tablePanelName;
    }

    public String getVisibleRoles() {
        return visibleRoles;
    }

    public void setVisibleRoles(String visibleRoles) {
        this.visibleRoles = visibleRoles;
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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getResourceDesc() {
        return resourceDesc;
    }

    public void setResourceDesc(String resourceDesc) {
        this.resourceDesc = resourceDesc;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
