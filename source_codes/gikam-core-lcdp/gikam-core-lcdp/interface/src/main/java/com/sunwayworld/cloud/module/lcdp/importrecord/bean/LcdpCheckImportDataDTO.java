package com.sunwayworld.cloud.module.lcdp.importrecord.bean;

import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpGlobalConfigBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;

import java.io.Serializable;
import java.util.List;

public class LcdpCheckImportDataDTO implements Serializable {
    private String operation;
    private List<LcdpResourceBean> resourceList;
    private List<LcdpTableBean> tableList;
    private List<LcdpViewBean>viewList;
    private String cssOperation;
    private String jsOperation;

    private Long sysClientCssVersion; //全局CSS版本

    private Long sysClientJsVersion;//全局JS版本

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public List<LcdpResourceBean> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<LcdpResourceBean> resourceList) {
        this.resourceList = resourceList;
    }

    public List<LcdpTableBean> getTableList() {
        return tableList;
    }

    public void setTableList(List<LcdpTableBean> tableList) {
        this.tableList = tableList;
    }

    public List<LcdpViewBean> getViewList() {
        return viewList;
    }

    public void setViewList(List<LcdpViewBean> viewList) {
        this.viewList = viewList;
    }

    public String getCssOperation() {
        return cssOperation;
    }

    public void setCssOperation(String cssOperation) {
        this.cssOperation = cssOperation;
    }

    public String getJsOperation() {
        return jsOperation;
    }

    public void setJsOperation(String jsOperation) {
        this.jsOperation = jsOperation;
    }

    public Long getSysClientCssVersion() {
        return sysClientCssVersion;
    }

    public void setSysClientCssVersion(Long sysClientCssVersion) {
        this.sysClientCssVersion = sysClientCssVersion;
    }

    public Long getSysClientJsVersion() {
        return sysClientJsVersion;
    }

    public void setSysClientJsVersion(Long sysClientJsVersion) {
        this.sysClientJsVersion = sysClientJsVersion;
    }
}
