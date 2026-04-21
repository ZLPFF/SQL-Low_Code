package com.sunwayworld.cloud.module.lcdp.pagetmpl.bean;

import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.Persistable;
import com.sunwayworld.framework.support.domain.AbstractPersistable;

/**
 * 页面模板配置表
 * 
 * @author liuxia@sunwayworld.com
 * @date 2023-07-04
 */
@Table("T_LCDP_PAGE_TMPL_CONFIG")
public class LcdpPageTmplConfigBean extends AbstractPersistable<Long> implements Persistable<Long> {

    @Transient
    private static final long serialVersionUID = -82566196618711043L;

    @Id
    private Long id;// 主键
    private Long pageTmplId;// 模板id
    private String uploaderFlag;// 是否带附件

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPageTmplId() {
        return pageTmplId;
    }

    public void setPageTmplId(Long pageTmplId) {
        this.pageTmplId = pageTmplId;
    }

    public String getUploaderFlag() {
        return uploaderFlag;
    }

    public void setUploaderFlag(String uploaderFlag) {
        this.uploaderFlag = uploaderFlag;
    }

}
