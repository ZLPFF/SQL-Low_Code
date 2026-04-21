package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.util.List;

import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractBaseData;

/*
* */
public class LcdpCheckoutDTO extends AbstractBaseData {

    @Transient
    private static final long serialVersionUID = 752618844753241623L;

    private String categoryCheckoutStatus;

    private String moduleCheckoutStatus;

    private List<Long> moduleIdList;
    private List<Long> categoryIdList;

    public static LcdpCheckoutDTO of(String categoryCheckoutStatus,String moduleCheckoutStatus) {
        LcdpCheckoutDTO checkoutDTO = new LcdpCheckoutDTO();
        checkoutDTO.setCategoryCheckoutStatus(categoryCheckoutStatus);
        checkoutDTO.setModuleCheckoutStatus(moduleCheckoutStatus);
        return checkoutDTO;
    }

    public String getCategoryCheckoutStatus() {
        return categoryCheckoutStatus;
    }

    public void setCategoryCheckoutStatus(String categoryCheckoutStatus) {
        this.categoryCheckoutStatus = categoryCheckoutStatus;
    }

    public String getModuleCheckoutStatus() {
        return moduleCheckoutStatus;
    }

    public void setModuleCheckoutStatus(String moduleCheckoutStatus) {
        this.moduleCheckoutStatus = moduleCheckoutStatus;
    }

    public List<Long> getModuleIdList() {
        return moduleIdList;
    }

    public void setModuleIdList(List<Long> moduleIdList) {
        this.moduleIdList = moduleIdList;
    }

    public List<Long> getCategoryIdList() {
        return categoryIdList;
    }

    public void setCategoryIdList(List<Long> categoryIdList) {
        this.categoryIdList = categoryIdList;
    }
}
