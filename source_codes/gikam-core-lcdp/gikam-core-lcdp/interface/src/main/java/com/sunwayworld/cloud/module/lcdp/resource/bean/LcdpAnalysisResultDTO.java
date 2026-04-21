package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.util.List;

import com.sunwayworld.framework.support.domain.AbstractBaseData;

/**
 * 分析结果
 */
public class LcdpAnalysisResultDTO extends AbstractBaseData {

    private static final long serialVersionUID = 23885414354209415L;

    private Boolean enable; //分析结果是否有效

    private List<String> analysisResultList; //分析结果

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public List<String> getAnalysisResultList() {
        return analysisResultList;
    }

    public void setAnalysisResultList(List<String> analysisResultList) {
        this.analysisResultList = analysisResultList;
    }
}
