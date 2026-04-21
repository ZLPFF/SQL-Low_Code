package com.sunwayworld.cloud.module.lcdp.moduletmpl.bean;

import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractPersistable;
import com.sunwayworld.framework.support.domain.Persistable;

/**
 * 模块模板页面组件表
 * 
 * @author liuxia@sunwayworld.com
 * @date 2022-12-08
 */
@Table("T_LCDP_MODULE_TMPL_PAGE_COMP")
public class LcdpModuleTmplPageCompBean extends AbstractPersistable<String> implements Persistable<String> {

    @Transient
    private static final long serialVersionUID = -82566196618711043L;

    @Id
    private String id;// 主键
    private Long moduleTmplPageId;// 模块模板页面ID
    private String compId;// 组件ID
    private String type;// 组件类型
    private String parentId;// 父组件ID
    private Long previousId;// 前一组件id
    private String visible;// 是否可见
    private String editable;// 是否可编辑
    private String config;// 组件配置
    private String configExtend;// 组件配置扩展
    private String compTag;// 页面组件标记 记录组件作用

    //只接收查询的pageType
    @Transient
    private String pageType;// 页面类型

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getModuleTmplPageId() {
        return moduleTmplPageId;
    }

    public void setModuleTmplPageId(Long moduleTmplPageId) {
        this.moduleTmplPageId = moduleTmplPageId;
    }

    public String getCompId() {
        return compId;
    }

    public void setCompId(String compId) {
        this.compId = compId;
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

    public Long getPreviousId() {
        return previousId;
    }

    public void setPreviousId(Long previousId) {
        this.previousId = previousId;
    }

    public String getVisible() {
        return visible;
    }

    public void setVisible(String visible) {
        this.visible = visible;
    }

    public String getEditable() {
        return editable;
    }

    public void setEditable(String editable) {
        this.editable = editable;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getConfigExtend() {
        return configExtend;
    }

    public void setConfigExtend(String configExtend) {
        this.configExtend = configExtend;
    }

    public String getCompTag() {
        return compTag;
    }

    public void setCompTag(String compTag) {
        this.compTag = compTag;
    }

    public String getPageType() {
        return pageType;
    }

    public void setPageType(String pageType) {
        this.pageType = pageType;
    }
}
