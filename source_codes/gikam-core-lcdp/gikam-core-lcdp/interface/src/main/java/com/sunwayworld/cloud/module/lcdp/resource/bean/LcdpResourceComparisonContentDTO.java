package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 资源比较的内容
 */
public class LcdpResourceComparisonContentDTO implements Serializable {
    private static final long serialVersionUID = -4903650302879342113L;
    
    private String content; // 内容
    private List<LcdpModulePageCompBean> pageCompList; // 页面的组件
    
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public List<LcdpModulePageCompBean> getPageCompList() {
        return pageCompList;
    }
    public void setPageCompList(List<LcdpModulePageCompBean> pageCompList) {
        this.pageCompList = pageCompList;
    }
}
