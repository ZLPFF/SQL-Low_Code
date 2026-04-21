package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.util.List;
import java.util.Map;

import com.sunwayworld.framework.support.domain.AbstractBaseData;

public class LcdpResourceDTO extends AbstractBaseData {

    private static final long serialVersionUID = -6472221143398378503L;

    private Long id;//资源ID

    private Long resourceId;//资源ID

    private String name;//资源名称

    private String type;//资源类型

    private String content;//资源内容

    private Boolean editable;//是否可编辑

    private List<LcdpModulePageCompBean> components;//页面组件

    private String path; //资源路径

    private String clientScript;// 客户端脚本

    private String resourceStatus;//资源状态 新增数据:new 有效数据:valid

    private Map<String, Map<String, String>> i18n;//页面国际化

    private List<String> dependentI18n;//页面系统国际化依赖

    private String pageConfig;//高级查询及页面配置
    
    private Long effectVersion; // 生效版本

    public static LcdpResourceDTO of(LcdpResourceHistoryBean resourceHistory) {
        LcdpResourceDTO resourceDTO = new LcdpResourceDTO();
        resourceDTO.setId(resourceHistory.getId());
        resourceDTO.setResourceId(resourceHistory.getResourceId());
        resourceDTO.setName(resourceHistory.getResourceName());
        resourceDTO.setType(resourceHistory.getResourceCategory());
        resourceDTO.setContent(resourceHistory.getContent());
        resourceDTO.setPath(resourceHistory.getPath());
        return resourceDTO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public List<LcdpModulePageCompBean> getComponents() {
        return components;
    }

    public void setComponents(List<LcdpModulePageCompBean> components) {
        this.components = components;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getClientScript() {
        return clientScript;
    }

    public void setClientScript(String clientScript) {
        this.clientScript = clientScript;
    }

    public String getResourceStatus() {
        return resourceStatus;
    }

    public void setResourceStatus(String resourceStatus) {
        this.resourceStatus = resourceStatus;
    }

    public List<String> getDependentI18n() {
        return dependentI18n;
    }

    public void setDependentI18n(List<String> dependentI18n) {
        this.dependentI18n = dependentI18n;
    }

    public Map<String, Map<String, String>> getI18n() {
        return i18n;
    }

    public void setI18n(Map<String, Map<String, String>> i18n) {
        this.i18n = i18n;
    }

    public String getPageConfig() {
        return pageConfig;
    }

    public void setPageConfig(String pageConfig) {
        this.pageConfig = pageConfig;
    }

    public Long getEffectVersion() {
        return effectVersion;
    }

    public void setEffectVersion(Long effectVersion) {
        this.effectVersion = effectVersion;
    }
}
