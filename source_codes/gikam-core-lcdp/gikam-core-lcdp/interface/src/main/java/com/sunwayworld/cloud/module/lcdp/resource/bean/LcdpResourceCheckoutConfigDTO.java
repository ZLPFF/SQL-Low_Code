package com.sunwayworld.cloud.module.lcdp.resource.bean;

import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractBaseData;

public class LcdpResourceCheckoutConfigDTO extends AbstractBaseData {
    @Transient
    private static final long serialVersionUID = 5994051573862896104L;

    private String exportEnvironmentDBType;// 导出环境数据库类型

    private String exportSysClientJsFlag; //是否导出全局脚本

    private String exportSysClientCssFlag; //是否导出全局样式

    public String getExportEnvironmentDBType() {
        return exportEnvironmentDBType;
    }
    public void setExportEnvironmentDBType(String exportEnvironmentDBType) {
        this.exportEnvironmentDBType = exportEnvironmentDBType;
    }
    public String getExportSysClientJsFlag() {
        return exportSysClientJsFlag;
    }
    public void setExportSysClientJsFlag(String exportSysClientJsFlag) {
        this.exportSysClientJsFlag = exportSysClientJsFlag;
    }
    public String getExportSysClientCssFlag() {
        return exportSysClientCssFlag;
    }
    public void setExportSysClientCssFlag(String exportSysClientCssFlag) {
        this.exportSysClientCssFlag = exportSysClientCssFlag;
    }
}
