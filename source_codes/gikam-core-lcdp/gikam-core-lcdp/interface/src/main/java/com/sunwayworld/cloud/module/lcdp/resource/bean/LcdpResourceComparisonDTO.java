package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.util.List;

import com.sunwayworld.framework.support.domain.AbstractBaseData;

/**
 * 资源对比（新）
 */
public class LcdpResourceComparisonDTO extends AbstractBaseData {
    private static final long serialVersionUID = 7270427410582349717L;
    
    private String type; // 类型（global：全局 table：表 view：视图 page：页面 resource：资源） 
    
    // 当前资源
    private LcdpResourceComparisonItemDTO currentItem;
    // 之前的资源
    private LcdpResourceComparisonItemDTO previousItem;
    
    // 资源所有版本信息
    private List<LcdpResourceComparisonVersionDTO> versionList;
    
    // 当前的页面组件
    private List<LcdpModulePageCompBean> currentPageCompList;
    // 之前的页面组件
    private List<LcdpModulePageCompBean> previousPageCompList;
    
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public LcdpResourceComparisonItemDTO getCurrentItem() {
        return currentItem;
    }
    public void setCurrentItem(LcdpResourceComparisonItemDTO currentItem) {
        this.currentItem = currentItem;
    }
    public LcdpResourceComparisonItemDTO getPreviousItem() {
        return previousItem;
    }
    public void setPreviousItem(LcdpResourceComparisonItemDTO previousItem) {
        this.previousItem = previousItem;
    }
    public List<LcdpResourceComparisonVersionDTO> getVersionList() {
        return versionList;
    }
    public void setVersionList(List<LcdpResourceComparisonVersionDTO> versionList) {
        this.versionList = versionList;
    }
    public List<LcdpModulePageCompBean> getCurrentPageCompList() {
        return currentPageCompList;
    }
    public void setCurrentPageCompList(List<LcdpModulePageCompBean> currentPageCompList) {
        this.currentPageCompList = currentPageCompList;
    }
    public List<LcdpModulePageCompBean> getPreviousPageCompList() {
        return previousPageCompList;
    }
    public void setPreviousPageCompList(List<LcdpModulePageCompBean> previousPageCompList) {
        this.previousPageCompList = previousPageCompList;
    }
}
