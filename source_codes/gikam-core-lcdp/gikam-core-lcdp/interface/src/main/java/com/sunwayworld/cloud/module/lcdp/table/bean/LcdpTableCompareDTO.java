package com.sunwayworld.cloud.module.lcdp.table.bean;

import java.util.List;

import com.sunwayworld.framework.support.domain.AbstractBaseData;

/**
 * @author yangsz@sunway.com 2023-01-13
 */
public class LcdpTableCompareDTO<T> extends AbstractBaseData {

    private static final long serialVersionUID = -7126448101069059815L;

    private T currentTable;

    private T previousTable;

    private List<T> historyTableList;

    public T getCurrentTable() {
        return currentTable;
    }

    public void setCurrentTable(T currentTable) {
        this.currentTable = currentTable;
    }

    public T getPreviousTable() {
        return previousTable;
    }

    public void setPreviousTable(T previousTable) {
        this.previousTable = previousTable;
    }

    public List<T> getHistoryTableList() {
        return historyTableList;
    }

    public void setHistoryTableList(List<T> historyTableList) {
        this.historyTableList = historyTableList;
    }
}
