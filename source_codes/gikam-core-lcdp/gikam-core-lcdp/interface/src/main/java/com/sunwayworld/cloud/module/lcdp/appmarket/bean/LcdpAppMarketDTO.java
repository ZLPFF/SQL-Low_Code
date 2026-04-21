package com.sunwayworld.cloud.module.lcdp.appmarket.bean;

import com.sunwayworld.framework.data.annotation.Transient;

import java.io.Serializable;

/**
 * @author yuanhh@sunwayworld.com
 * @date 2025-03-07
 */
public class LcdpAppMarketDTO implements Serializable {

    @Transient
    private static final long serialVersionUID = -82566196618756043L;

    private String funcName;// 功能名称
    private String funcPath;// 路径
    private String category;// 类型(下拉：业务模块、公共模块)
    private String sector;// 所属行业(下拉框可手动录入)
    private Long version = 1l;// 版本

    public String getFuncName() {
        return funcName;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    public String getFuncPath() {
        return funcPath;
    }

    public void setFuncPath(String funcPath) {
        this.funcPath = funcPath;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
