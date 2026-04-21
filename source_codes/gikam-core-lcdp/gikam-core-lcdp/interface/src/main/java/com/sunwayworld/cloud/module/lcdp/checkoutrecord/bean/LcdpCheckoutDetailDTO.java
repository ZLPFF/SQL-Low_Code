package com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean;

import com.sunwayworld.framework.support.domain.AbstractBaseData;

public class LcdpCheckoutDetailDTO extends AbstractBaseData {

    private static final long serialVersionUID = -4937963776731685631L;

    private String systemName; //系统名称

    private String checkoutStatus; //迁出状态 0是失败 1 是成功

    private String message; //返回信息


    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getCheckoutStatus() {
        return checkoutStatus;
    }

    public void setCheckoutStatus(String checkoutStatus) {
        this.checkoutStatus = checkoutStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
