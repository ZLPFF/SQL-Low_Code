package com.sunwayworld.cloud.module.lcdp.table.bean;

import java.util.List;

import com.sunwayworld.framework.support.domain.AbstractBaseData;
import com.sunwayworld.framework.utils.CollectionUtils;

/**
 * @author yangsz@sunway.com 2022-10-26
 */
public class LcdpTableDTO extends AbstractBaseData {

    private static final long serialVersionUID = -8283270919270879941L;

    private String tableName;// 表名
    private String tableDesc;// 表注释
    private String masterTableName;// 主表名
    private String referColumn;// 关联字段
    private List<LcdpTableFieldDTO> fieldList = CollectionUtils.emptyList(); // 表字段
    private List<LcdpTableIndexDTO> indexList = CollectionUtils.emptyList(); // 表索引

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableDesc() {
        return tableDesc;
    }

    public void setTableDesc(String tableDesc) {
        this.tableDesc = tableDesc;
    }

    public String getMasterTableName() {
        return masterTableName;
    }

    public void setMasterTableName(String masterTableName) {
        this.masterTableName = masterTableName;
    }

    public String getReferColumn() {
        return referColumn;
    }

    public void setReferColumn(String referColumn) {
        this.referColumn = referColumn;
    }

    public List<LcdpTableFieldDTO> getFieldList() {
        return fieldList;
    }

    public void setFieldList(List<LcdpTableFieldDTO> fieldList) {
        this.fieldList = fieldList;
    }

    public List<LcdpTableIndexDTO> getIndexList() {
        return indexList;
    }

    public void setIndexList(List<LcdpTableIndexDTO> indexList) {
        this.indexList = indexList;
    }
}
