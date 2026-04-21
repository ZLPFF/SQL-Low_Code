package com.sunwayworld.cloud.module.lcdp.moduletmpl.bean;

import com.sunwayworld.framework.support.domain.AbstractBaseData;

import java.util.List;

/**
 * 模块模板
 *
 * @author liuxia@sunwayworld.com
 * @date 2022-12-05
 */
public class LcdpModuleTmplDTO extends AbstractBaseData {


    private static final long serialVersionUID = 1201601684826467219L;

    private Long moduleTmplId;// 模板ID
    private String moduleTmplName;// 模板名称
    private String masterTableName; //主表
    private List<LcdpModuleTmplChildTableDTO> childTableList;//子表集合
    private String bpFlag;//是否审核
    private String uploaderFlag;//是否需要附件
    private String chooseFlag;//是否需要选择页

    //编码规则
    private String prefix; //前缀
    private String pattern; //规则
    private String column; //字段

    private String moduleName; //模块名称
    private String categoryName;//分类名称
    private String modulePath;//模块路径
    private String masterScriptPath;//主表对应的java脚本路径
    private String detailPagePath;//详情页面路径

    private String preInsertFlag;//是否预新增

    //模块信息
    private Long resourceId;//模块ID
    private Long parentId;// 父ID
    private String resourceName;// 资源名称
    private String resourceDesc;// 资源描述
    private String resourceCategory;// 资源类型(模块分类、模块、页面、前端脚本、后端脚本)

    private String tmplClassId; //模板所属类别 系统sys 自定义biz

    private List<LcdpModuleCustomReplaceDTO> customReplaceList;//自定义替换集合

    private String dependentWidgetId; //依赖组件ID

    private List<String> tableNameList; //表名集合 自定义模板时选择的表存储的集合


    public Long getModuleTmplId() {
        return moduleTmplId;
    }

    public void setModuleTmplId(Long moduleTmplId) {
        this.moduleTmplId = moduleTmplId;
    }

    public String getModuleTmplName() {
        return moduleTmplName;
    }

    public void setModuleTmplName(String moduleTmplName) {
        this.moduleTmplName = moduleTmplName;
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

    public String getBpFlag() {
        return bpFlag;
    }

    public void setBpFlag(String bpFlag) {
        this.bpFlag = bpFlag;
    }

    public String getUploaderFlag() {
        return uploaderFlag;
    }

    public void setUploaderFlag(String uploaderFlag) {
        this.uploaderFlag = uploaderFlag;
    }

    public String getPreInsertFlag() {
        return preInsertFlag;
    }

    public void setPreInsertFlag(String preInsertFlag) {
        this.preInsertFlag = preInsertFlag;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getModulePath() {
        return modulePath;
    }

    public void setModulePath(String modulePath) {
        this.modulePath = modulePath;
    }

    public String getMasterScriptPath() {
        return masterScriptPath;
    }

    public void setMasterScriptPath(String masterScriptPath) {
        this.masterScriptPath = masterScriptPath;
    }

    public String getDetailPagePath() {
        return detailPagePath;
    }

    public void setDetailPagePath(String detailPagePath) {
        this.detailPagePath = detailPagePath;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
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

    public String getResourceCategory() {
        return resourceCategory;
    }

    public void setResourceCategory(String resourceCategory) {
        this.resourceCategory = resourceCategory;
    }

    public String getTmplClassId() {
        return tmplClassId;
    }

    public void setTmplClassId(String tmplClassId) {
        this.tmplClassId = tmplClassId;
    }

    public List<LcdpModuleCustomReplaceDTO> getCustomReplaceList() {
        return customReplaceList;
    }

    public void setCustomReplaceList(List<LcdpModuleCustomReplaceDTO> customReplaceList) {
        this.customReplaceList = customReplaceList;
    }

    public String getDependentWidgetId() {
        return dependentWidgetId;
    }

    public void setDependentWidgetId(String dependentWidgetId) {
        this.dependentWidgetId = dependentWidgetId;
    }

    public List<String> getTableNameList() {
        return tableNameList;
    }

    public void setTableNameList(List<String> tableNameList) {
        this.tableNameList = tableNameList;
    }

    public String getChooseFlag() {
        return chooseFlag;
    }

    public void setChooseFlag(String chooseFlag) {
        this.chooseFlag = chooseFlag;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }
}
