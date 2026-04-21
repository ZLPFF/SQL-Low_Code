package com.sunwayworld.cloud.module.lcdp.moduletmpl.bean;

import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.Persistable;
import com.sunwayworld.framework.support.domain.AbstractPersistable;

/**
 * 模板配置表
 * 
 * @author liuxia@sunwayworld.com
 * @date 2022-12-08
 */
@Table("T_LCDP_MODULE_TMPL_CONFIG")
public class LcdpModuleTmplConfigBean extends AbstractPersistable<Long> implements Persistable<Long> {

    @Transient
    private static final long serialVersionUID = -82566196618711043L;

    @Id
    private Long id;// 主键
    private Long moduleTmplId;// 模块模板ID
    private String bpFlag;// 是否带工作流
    private String uploaderFlag;// 是否带附件

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getModuleTmplId() {
        return moduleTmplId;
    }

    public void setModuleTmplId(Long moduleTmplId) {
        this.moduleTmplId = moduleTmplId;
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

}
