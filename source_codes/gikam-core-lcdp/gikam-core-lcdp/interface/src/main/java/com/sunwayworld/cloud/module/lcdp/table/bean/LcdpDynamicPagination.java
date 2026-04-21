package com.sunwayworld.cloud.module.lcdp.table.bean;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.data.page.Pageable;
import com.sunwayworld.framework.mybatis.page.PageRowBounds;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.NumberUtils;

public class LcdpDynamicPagination<T> implements Serializable {

    private static final long serialVersionUID = -3762826401234226920L;

    private int total;
    private int pageSize;
    private int pageNumber;
    private List<T> rows;
    private Map<String, Number> totalMap;
    private final Map<String, String> columnTypeMap = new LinkedHashMap<>();
    private final Map<String, String> columnDescMap = new LinkedHashMap<>();
    private List<String> optionsList; // 指定下拉列表的数据

    public LcdpDynamicPagination() {
        this.total = 0;
        this.pageSize = 0;
        this.pageNumber = 0;
        this.rows = CollectionUtils.emptyList();
        this.totalMap = CollectionUtils.emptyMap();
    }

    public LcdpDynamicPagination(List<T> rows) {
        this.total = rows.size();
        this.pageSize = rows.size();
        this.pageNumber = 1;
        this.rows = Objects.requireNonNull(rows);
        this.totalMap = CollectionUtils.emptyMap();
    }

    public LcdpDynamicPagination(int total, int pageSize, int pageNumber, List<T> rows) {
        this.total = total;
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.rows = Objects.requireNonNull(rows);
        this.totalMap = CollectionUtils.emptyMap();
    }

    public LcdpDynamicPagination(int total, int pageSize, int pageNumber, List<T> rows, Map<String, Number> totalMap) {
        this.total = total;
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.rows = Objects.requireNonNull(rows);
        this.totalMap = Objects.requireNonNull(totalMap);
    }

    public LcdpDynamicPagination(int pageSize, int pageNumber, org.springframework.data.domain.Page<T> page) {
        this.total = Long.valueOf(page.getTotalElements()).intValue();
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.rows = page.getContent();
        this.totalMap = CollectionUtils.emptyMap();
    }

    public LcdpDynamicPagination(Page<?> otherPagination, List<T> rows) {
        this.total = NumberUtils.max(otherPagination.getTotal(), rows.size());
        this.pageSize = otherPagination.getPageSize();
        this.pageNumber = otherPagination.getPageNumber();
        this.totalMap = otherPagination.getTotalMap();
        this.rows = Objects.requireNonNull(rows);
        this.columnTypeMap.putAll(otherPagination.getColumnTypeMap());
        this.optionsList = otherPagination.getOptionsList();
    }

    public LcdpDynamicPagination(PageRowBounds rowBounds, List<T> rows) {
        this(NumberUtils.max(rowBounds.getTotal(), rows.size()), rowBounds.getLimit(), rowBounds.getPageNumber(), rows, rowBounds.getTotalMap());
    }

    public int getTotalPages() {
        return Double.valueOf(Math.ceil((double) total / getPageSize())).intValue();
    }

    public int getTotal() {
        return total;
    }

    public int getNumberOfElements() {
        return rows.size();
    }

    public boolean isFirst() {
        return pageNumber == Pageable.DEFAULT_FIRST_PAGE_NUMBER;
    }

    public boolean isLast() {
        return !hasNext();
    }

    public boolean hasNext() {
        return pageNumber < getTotalPages();
    }

    public boolean hasPrevious() {
        return pageNumber > Pageable.DEFAULT_FIRST_PAGE_NUMBER;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public List<T> getRows() {
        return rows;
    }

    public Map<String, Number> getTotalMap() {
        return totalMap;
    }

    public Map<String, String> getColumnTypeMap() {
        return columnTypeMap;
    }

    public void setColumnTypeMap(Map<String, String> columnTypeMap) {
        if (columnTypeMap != null) {
            this.columnTypeMap.putAll(columnTypeMap);
        }
    }

    public Map<String, String> getColumnDescMap() {
        return columnDescMap;
    }

    public void setColumnDescMap(Map<String, String> columnDescMap) {
        if (columnDescMap != null) {
            this.columnDescMap.putAll(columnDescMap);
        }
    }

    public List<String> getOptionsList() {
        return optionsList;
    }

    public void setOptionsList(List<String> optionsList) {
        this.optionsList = optionsList;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public void setTotalMap(Map<String, Number> totalMap) {
        this.totalMap = totalMap;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
