package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpGlobalConfigBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.framework.support.domain.AbstractBaseData;

public class LcdpResourceCompareDTO extends AbstractBaseData {


    private static final long serialVersionUID = 7270427410582349717L;

    //当前版本资源历史数据
    private LcdpResourceHistoryBean currentVersionHistory;

    private List<LcdpModulePageCompBean> currentModulePageCompList;

    //上一版本资源历史数据
    private LcdpResourceHistoryBean previousVersionHistory;

    private List<LcdpModulePageCompBean> previousModulePageCompList;

    //资源所有版本
    private List<LcdpResourceHistoryBean> historyList;

    private LcdpTableBean currentTable;

    private LcdpTableBean previousTable;

    private LcdpViewBean currentView;

    private LcdpViewBean previousView;

    //当前版本全局脚本资源历史数据
    private LcdpGlobalConfigBean currentGlobalConfigHistory;

    //上一版本全局脚本资源历史数据
    private LcdpGlobalConfigBean previousGlobalConfigHistory;



    public LcdpResourceHistoryBean getCurrentVersionHistory() {
        return currentVersionHistory;
    }

    public void setCurrentVersionHistory(LcdpResourceHistoryBean currentVersionHistory) {
        this.currentVersionHistory = currentVersionHistory;
    }

    public LcdpResourceHistoryBean getPreviousVersionHistory() {
        return previousVersionHistory;
    }

    public void setPreviousVersionHistory(LcdpResourceHistoryBean previousVersionHistory) {
        this.previousVersionHistory = previousVersionHistory;
    }

    public List<LcdpModulePageCompBean> getCurrentModulePageCompList() {
        return currentModulePageCompList;
    }

    public void setCurrentModulePageCompList(List<LcdpModulePageCompBean> currentModulePageCompList) {
        this.currentModulePageCompList = currentModulePageCompList;
    }

    public List<LcdpModulePageCompBean> getPreviousModulePageCompList() {
        return previousModulePageCompList;
    }

    public void setPreviousModulePageCompList(List<LcdpModulePageCompBean> previousModulePageCompList) {
        this.previousModulePageCompList = previousModulePageCompList;
    }

    public List<LcdpResourceHistoryBean> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(List<LcdpResourceHistoryBean> historyList) {
        this.historyList = historyList;
    }

    public LcdpTableBean getCurrentTable() {
        return currentTable;
    }

    public void setCurrentTable(LcdpTableBean currentTable) {
        this.currentTable = currentTable;
    }

    public LcdpTableBean getPreviousTable() {
        return previousTable;
    }

    public void setPreviousTable(LcdpTableBean previousTable) {
        this.previousTable = previousTable;
    }

    public LcdpViewBean getCurrentView() {
        return currentView;
    }

    public void setCurrentView(LcdpViewBean currentView) {
        this.currentView = currentView;
    }

    public LcdpViewBean getPreviousView() {
        return previousView;
    }

    public void setPreviousView(LcdpViewBean previousView) {
        this.previousView = previousView;
    }

    public LcdpGlobalConfigBean getCurrentGlobalConfigHistory() {
        return currentGlobalConfigHistory;
    }

    public void setCurrentGlobalConfigHistory(LcdpGlobalConfigBean currentGlobalConfigHistory) {
        this.currentGlobalConfigHistory = currentGlobalConfigHistory;
    }

    public LcdpGlobalConfigBean getPreviousGlobalConfigHistory() {
        return previousGlobalConfigHistory;
    }

    public void setPreviousGlobalConfigHistory(LcdpGlobalConfigBean previousGlobalConfigHistory) {
        this.previousGlobalConfigHistory = previousGlobalConfigHistory;
    }
}
