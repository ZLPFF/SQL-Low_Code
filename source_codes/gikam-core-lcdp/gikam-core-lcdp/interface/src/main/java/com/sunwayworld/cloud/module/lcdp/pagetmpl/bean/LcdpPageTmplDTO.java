package com.sunwayworld.cloud.module.lcdp.pagetmpl.bean;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.moduletmpl.bean.LcdpModuleTmplChildTableDTO;
import com.sunwayworld.framework.support.domain.AbstractBaseData;

/**
 * 页面模板
 *
 * @author liuxia@sunwayworld.com
 * @date 2022-12-05
 */
public class LcdpPageTmplDTO extends AbstractBaseData {


    private static final long serialVersionUID = -5337848429570664980L;

    private Long pageTmplId;// 模板ID
    private String pageTmplName;// 模板名称
    private String masterTableName; //主表
    private List<LcdpModuleTmplChildTableDTO> childTableList;//子表集合
    private String uploaderFlag;//是否需要附件

    private String resourceName;// 页面名称
    private String resourceDesc;// 页面描述
    private Long parentId;// 父节点Id

    private String modulePath;//模块路径
    private Long viewId;// 页面Id
    private String viewPath;//页面路径

    private String moduleName;

    private String dependentWidgetId;
    private String masterScriptPath;


    private String bpFlag;//是否审核
    private String preInsertFlag;//是否预新增

    public Long getPageTmplId() {
        return pageTmplId;
    }

    public void setPageTmplId(Long pageTmplId) {
        this.pageTmplId = pageTmplId;
    }

    public String getPageTmplName() {
        return pageTmplName;
    }

    public void setPageTmplName(String pageTmplName) {
        this.pageTmplName = pageTmplName;
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

    public String getMasterTableName() {
        return masterTableName;
    }

    public void setMasterTableName(String masterTableName) {
        this.masterTableName = masterTableName;
    }

    public List<LcdpModuleTmplChildTableDTO> getChildTableList() {
        return childTableList;
    }

    public void setChildTableList(List<LcdpModuleTmplChildTableDTO> childTableList) {
        this.childTableList = childTableList;
    }

    public String getUploaderFlag() {
        return uploaderFlag;
    }

    public void setUploaderFlag(String uploaderFlag) {
        this.uploaderFlag = uploaderFlag;
    }

    public String getModulePath() {
        return modulePath;
    }

    public void setModulePath(String modulePath) {
        this.modulePath = modulePath;
    }

    public Long getViewId() {
        return viewId;
    }

    public void setViewId(Long viewId) {
        this.viewId = viewId;
    }

    public String getViewPath() {
        return viewPath;
    }

    public void setViewPath(String viewPath) {
        this.viewPath = viewPath;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getDependentWidgetId() {
        return dependentWidgetId;
    }

    public void setDependentWidgetId(String dependentWidgetId) {
        this.dependentWidgetId = dependentWidgetId;
    }

    public String getMasterScriptPath() {
        return masterScriptPath;
    }

    public void setMasterScriptPath(String masterScriptPath) {
        this.masterScriptPath = masterScriptPath;
    }

    public String getBpFlag() {
        return bpFlag;
    }

    public void setBpFlag(String bpFlag) {
        this.bpFlag = bpFlag;
    }

    public String getPreInsertFlag() {
        return preInsertFlag;
    }

    public void setPreInsertFlag(String preInsertFlag) {
        this.preInsertFlag = preInsertFlag;
    }
}
