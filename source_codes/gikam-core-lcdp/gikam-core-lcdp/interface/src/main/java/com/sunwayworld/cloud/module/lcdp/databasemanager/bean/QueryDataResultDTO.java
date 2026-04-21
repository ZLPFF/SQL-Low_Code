package com.sunwayworld.cloud.module.lcdp.databasemanager.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class QueryDataResultDTO implements Serializable {

    private static final long serialVersionUID = -6115517106806961353L;

    private int pageNumber;
    private int pageSize;
    private int total;
    private int totalPages;

    List<ColumnMetaData> columnList;

    List<Map<String, Object>> rows;

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<ColumnMetaData> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<ColumnMetaData> columnList) {
        this.columnList = columnList;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }
}
