package com.sunwayworld.cloud.module.lcdp.config.bean;

import java.util.List;

import com.sunwayworld.framework.support.domain.AbstractBaseData;

/**
 * @author yangsz@sunway.com 2023-02-22
 */
public class LcdpConfigCompareDTO extends AbstractBaseData {

    private static final long serialVersionUID = 3522477116015765499L;

    //当前版本资源历史数据
    private LcdpGlobalConfigBean currentVersionHistory;

    //上一版本资源历史数据
    private LcdpGlobalConfigBean previousVersionHistory;

    //资源所有版本
    private List<LcdpGlobalConfigBean> historyList;

    public LcdpGlobalConfigBean getCurrentVersionHistory() {
        return currentVersionHistory;
    }

    public void setCurrentVersionHistory(LcdpGlobalConfigBean currentVersionHistory) {
        this.currentVersionHistory = currentVersionHistory;
    }

    public LcdpGlobalConfigBean getPreviousVersionHistory() {
        return previousVersionHistory;
    }

    public void setPreviousVersionHistory(LcdpGlobalConfigBean previousVersionHistory) {
        this.previousVersionHistory = previousVersionHistory;
    }

    public List<LcdpGlobalConfigBean> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(List<LcdpGlobalConfigBean> historyList) {
        this.historyList = historyList;
    }
}
