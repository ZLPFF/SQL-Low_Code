package com.sunwayworld.cloud.module.lcdp.table.bean;

import java.io.Serializable;

public class LcdpViewDTO implements Serializable {
    private static final long serialVersionUID = 4155943428029354759L;
    
    private String viewName;// 视图名
    private String viewDesc; // 视图描述
    private String tableName;// 虚拟表名
    private String selectStatement;// 查询语句
    
    public String getViewName() {
        return viewName;
    }
    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
    public String getViewDesc() {
        return viewDesc;
    }
    public void setViewDesc(String viewDesc) {
        this.viewDesc = viewDesc;
    }
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public String getSelectStatement() {
        return selectStatement;
    }
    public void setSelectStatement(String selectStatement) {
        this.selectStatement = selectStatement;
    }
}
