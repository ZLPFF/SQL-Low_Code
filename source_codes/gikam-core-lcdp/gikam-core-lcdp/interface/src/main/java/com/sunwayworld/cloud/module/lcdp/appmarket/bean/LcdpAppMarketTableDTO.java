package com.sunwayworld.cloud.module.lcdp.appmarket.bean;

import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Transient;

import java.io.Serializable;

public class LcdpAppMarketTableDTO implements Serializable {

    @Transient
    private static final long serialVersionUID = -82556566618756043L;

    @Id
    private Long id;// 主键
    private Long funcId;// 功能ID
    private String tableName;// 表名称
    private String mergeFlag; // 应用时,表合并标识

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFuncId() {
        return funcId;
    }

    public void setFuncId(Long funcId) {
        this.funcId = funcId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getMergeFlag() {
        return mergeFlag;
    }

    public void setMergeFlag(String mergeFlag) {
        this.mergeFlag = mergeFlag;
    }
}
