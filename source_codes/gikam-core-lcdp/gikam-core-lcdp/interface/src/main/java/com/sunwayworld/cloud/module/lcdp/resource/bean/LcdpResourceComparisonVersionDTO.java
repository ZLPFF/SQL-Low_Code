package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.io.Serializable;

/**
 * 资源比较的版本信息
 */
public class LcdpResourceComparisonVersionDTO implements Serializable {
    private static final long serialVersionUID = -2459469220065544449L;
    
    private Long id; // 历史数据主键（T_LCDP_RESOURCE_HISTORY.ID或T_LCDP_GLOBAL_CONFIG.ID或T_LCDP_TABLE.ID或T_LCDP_VIEW.ID）
    private Long version; // 版本
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getVersion() {
        return version;
    }
    public void setVersion(Long version) {
        this.version = version;
    }
}
