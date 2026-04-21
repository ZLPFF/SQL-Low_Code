package com.sunwayworld.cloud.module.lcdp.moduletmpl.bean;

import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractInsertable;
import com.sunwayworld.framework.support.domain.Persistable;

/**
 * 模块模板
 * 
 * @author liuxia@sunwayworld.com
 * @date 2022-12-08
 */
@Table("T_LCDP_MODULE_TMPL")
public class LcdpModuleTmplBean extends AbstractInsertable<Long> implements Persistable<Long> {

    @Transient
    private static final long serialVersionUID = -82566196618711043L;

    @Id
    private Long id;// 主键
    private String moduleTmplName;// 模板名称
    private String tmplClassId;//模板所属类别 系统sys 自定义biz
    private String tmplType;//模板类型 模块  页面
    private String i18nCode;//模板i18n


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModuleTmplName() {
        return moduleTmplName;
    }

    public void setModuleTmplName(String moduleTmplName) {
        this.moduleTmplName = moduleTmplName;
    }

    public String getTmplClassId() {
        return tmplClassId;
    }

    public void setTmplClassId(String tmplClassId) {
        this.tmplClassId = tmplClassId;
    }

    public String getTmplType() {
        return tmplType;
    }

    public void setTmplType(String tmplType) {
        this.tmplType = tmplType;
    }

    public String getI18nCode() {
        return i18nCode;
    }

    public void setI18nCode(String i18nCode) {
        this.i18nCode = i18nCode;
    }
}
