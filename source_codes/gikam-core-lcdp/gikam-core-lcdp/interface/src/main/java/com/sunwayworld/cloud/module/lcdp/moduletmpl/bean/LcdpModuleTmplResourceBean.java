package com.sunwayworld.cloud.module.lcdp.moduletmpl.bean;

import com.sunwayworld.framework.data.annotation.Clob;
import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.Persistable;
import com.sunwayworld.framework.support.domain.AbstractPersistable;

/**
 * 模块模板资源
 * 
 * @author liuxia@sunwayworld.com
 * @date 2022-12-08
 */
@Table("T_LCDP_MODULE_TMPL_RESOURCE")
public class LcdpModuleTmplResourceBean extends AbstractPersistable<Long> implements Persistable<Long> {

    @Transient
    private static final long serialVersionUID = -82566196618711043L;

    @Id
    private Long id;// 主键
    private Long moduleTmplConfigId;// 模块模板配置ID
    private String resourceCategory;// 资源类型页面、前端脚本、后端脚本
    @Clob
    private String content;// 内容
    private String pageType;// 页面类型

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getModuleTmplConfigId() {
        return moduleTmplConfigId;
    }

    public void setModuleTmplConfigId(Long moduleTmplConfigId) {
        this.moduleTmplConfigId = moduleTmplConfigId;
    }

    public String getResourceCategory() {
        return resourceCategory;
    }

    public void setResourceCategory(String resourceCategory) {
        this.resourceCategory = resourceCategory;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPageType() {
        return pageType;
    }

    public void setPageType(String pageType) {
        this.pageType = pageType;
    }
}
