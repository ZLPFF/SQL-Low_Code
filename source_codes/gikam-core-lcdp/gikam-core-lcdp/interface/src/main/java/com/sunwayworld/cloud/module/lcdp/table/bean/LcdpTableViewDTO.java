package com.sunwayworld.cloud.module.lcdp.table.bean;

import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractBaseData;

/**
 * 低代码平台表和视图集合 用于创建脚本时选择
 *
 * @author liuxia
 * @date 2023-05-30
 */
public class LcdpTableViewDTO extends AbstractBaseData {

    @Transient
    private static final long serialVersionUID = 8936490317045748554L;

    private String tableOrViewName;//表或视图名称
    private String dbStructureType; //类型 table  view

    public String getTableOrViewName() {
        return tableOrViewName;
    }

    public void setTableOrViewName(String tableOrViewName) {
        this.tableOrViewName = tableOrViewName;
    }

    public String getDbStructureType() {
        return dbStructureType;
    }

    public void setDbStructureType(String dbStructureType) {
        this.dbStructureType = dbStructureType;
    }
}
