package com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean;

import java.util.List;

import com.sunwayworld.framework.support.domain.AbstractBaseData;

public class LcdpCheckOutDTO extends AbstractBaseData {

    private static final long serialVersionUID = -4921361708968023308L;

    private List<LcdpCheckoutDetailDTO> checkoutDetailList;

    private String downLoadUrl; //文件下载地址 操作中选了导出或者迁出失败时这里会赋值提供下载路径


    public String getDownLoadUrl() {
        return downLoadUrl;
    }

    public void setDownLoadUrl(String downLoadUrl) {
        this.downLoadUrl = downLoadUrl;
    }

    public List<LcdpCheckoutDetailDTO> getCheckoutDetailList() {
        return checkoutDetailList;
    }

    public void setCheckoutDetailList(List<LcdpCheckoutDetailDTO> checkoutDetailList) {
        this.checkoutDetailList = checkoutDetailList;
    }
}
