package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.alibaba.fastjson.annotation.JSONField;
import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.support.domain.AbstractInsertable;
import com.sunwayworld.framework.support.domain.Insertable;

/**
 * 功能模块页面国际化表
 * 
 * @author liuxia@sunwayworld.com
 * @date 2023-07-27
 */
@Table("T_LCDP_MODULE_PAGE_I18N")
public class LcdpModulePageI18nBean extends AbstractInsertable<Long> implements Insertable<Long> {
    private static final long serialVersionUID = 6951785668930207965L;

    @Id
    private Long id;// 主键
    private Long modulePageId;// 页面资源主键
    private Long modulePageHistoryId;// 历史页面资源主键
    private String code;// 国际化编码
    private String i18nConfigId;// 国际化配置主键
    private String message;// 国际化信息
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

    public Long getModulePageId() {
        return modulePageId;
    }

    public void setModulePageId(Long modulePageId) {
        this.modulePageId = modulePageId;
    }

    public Long getModulePageHistoryId() {
        return modulePageHistoryId;
    }

    public void setModulePageHistoryId(Long modulePageHistoryId) {
        this.modulePageHistoryId = modulePageHistoryId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getI18nConfigId() {
        return i18nConfigId;
    }

    public void setI18nConfigId(String i18nConfigId) {
        this.i18nConfigId = i18nConfigId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
