package com.sunwayworld.cloud.module.lcdp.pagetmpl.bean;

import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.Persistable;
import com.sunwayworld.framework.support.domain.AbstractPersistable;

/**
 * 页面模板页面组件表
 * 
 * @author liuxia@sunwayworld.com
 * @date 2023-07-04
 */
@Table("T_LCDP_PAGE_TMPL_COMP")
public class LcdpPageTmplCompBean extends AbstractPersistable<String> implements Persistable<String> {

    @Transient
    private static final long serialVersionUID = -82566196618711043L;

    @Id
    private String id;// 主键
    private Long tmplConfigId;// 模板配置ID
    private String compId;// 组件ID
    private String type;// 组件类型
    private String parentId;// 父组件ID
    private Long previousId;// 前一组件id
    private String visible;// 是否可见
    private String editable;// 是否可编辑
    private String config;// 组件配置
    private String configExtend;// 组件配置扩展
    private String compTag;// 页面组件标记 记录组件作用

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTmplConfigId() {
        return tmplConfigId;
    }

    public void setTmplConfigId(Long tmplConfigId) {
        this.tmplConfigId = tmplConfigId;
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

}
