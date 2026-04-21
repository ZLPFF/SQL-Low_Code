package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.alibaba.fastjson.annotation.JSONField;
import com.sunwayworld.framework.data.annotation.Clob;
import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractInsertable;
import com.sunwayworld.framework.support.domain.Insertable;

/**
 * 功能模块页面组件表
 * 
 * @author liuxia@@sunwayworld.com
 * @date 2022-10-17
 */
@Table("T_LCDP_MODULE_PAGE_COMP")
public class LcdpModulePageCompBean extends AbstractInsertable<String> implements Insertable<String> {

    @Transient
    private static final long serialVersionUID = 265400983079225347L;
    @Id
    private String id;// 前端组件UUID
    private Long modulePageHistoryId;// 模块页面历史ID
    private Long modulePageId;// 模块页面ID
    private Long modulePageVersion;// 模块页面版本
    
    private String type;// 组件类型
    private String parentId;// 父组件ID
    
    @Clob
    private String config;// 组件配置
    private String createdById;// 制单人编码
    private String createdByName;// 制单人名称
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;// 制单时间
    private String createdByOrgId;// 制单人单位编码
    private String createdByOrgName;// 制单人单位名称

    @Transient
    private String configExtend;// 组件配置拓展字段(兼容历史版本导入)

    private String compareColumn;//对比字段

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getModulePageHistoryId() {
        return modulePageHistoryId;
    }

    public void setModulePageHistoryId(Long modulePageHistoryId) {
        this.modulePageHistoryId = modulePageHistoryId;
    }

    public Long getModulePageId() {
        return modulePageId;
    }

    public void setModulePageId(Long modulePageId) {
        this.modulePageId = modulePageId;
    }

    public Long getModulePageVersion() {
        return modulePageVersion;
    }

    public void setModulePageVersion(Long modulePageVersion) {
        this.modulePageVersion = modulePageVersion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
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

    public String getConfigExtend() {
        return configExtend;
    }

    public void setConfigExtend(String configExtend) {
        this.configExtend = configExtend;
    }

    public String getCompareColumn() {
        return compareColumn;
    }

    public void setCompareColumn(String compareColumn) {
        this.compareColumn = compareColumn;
    }
}
