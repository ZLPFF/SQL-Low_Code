package com.sunwayworld.cloud.module.lcdp.pagetmpl.bean;

import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.Persistable;
import com.sunwayworld.framework.support.domain.AbstractPersistable;

/**
 * 页面模板表
 * 
 * @author liuxia@sunwayworld.com
 * @date 2023-07-04
 */
@Table("T_LCDP_PAGE_TMPL")
public class LcdpPageTmplBean extends AbstractPersistable<Long> implements Persistable<Long> {

    @Transient
    private static final long serialVersionUID = -82566196618711043L;

    @Id
    private Long id;// 主键
    private String pageTmplName;// 模板名称
    private String childTableFlag;// 是否需要子表

    private String i18nCode;//模板i18n


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPageTmplName() {
        return pageTmplName;
    }

    public void setPageTmplName(String pageTmplName) {
        this.pageTmplName = pageTmplName;
    }

    public String getChildTableFlag() {
        return childTableFlag;
    }

    public void setChildTableFlag(String childTableFlag) {
        this.childTableFlag = childTableFlag;
    }

    public String getI18nCode() {
        return i18nCode;
    }

    public void setI18nCode(String i18nCode) {
        this.i18nCode = i18nCode;
    }

}
