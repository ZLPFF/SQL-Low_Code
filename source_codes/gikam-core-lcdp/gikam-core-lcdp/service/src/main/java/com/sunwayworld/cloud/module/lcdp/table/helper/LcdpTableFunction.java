package com.sunwayworld.cloud.module.lcdp.table.helper;

import java.util.Arrays;
import java.util.List;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldBean;
import com.sunwayworld.framework.constant.Constant;

public enum LcdpTableFunction {
    ACTIVATE(Arrays.asList(LcdpTableFieldBean.of("ACTIVATEDFLAG", "启用标志", "varchar", "2", 0, 0, Constant.NO, Constant.NO),
            LcdpTableFieldBean.of("ACTIVATEDBYID", "启停人编码", "varchar", "32", 0, 0, Constant.YES, null),
            LcdpTableFieldBean.of("ACTIVATEDBYNAME", "启停人名称", "varchar", "96", 0, 0, Constant.YES, null),
            LcdpTableFieldBean.of("ACTIVATEDTIME", "启停时间", "date", null, 0, 0, Constant.YES, null))),
    PROCESS(Arrays.asList(LcdpTableFieldBean.of("PROCESSSTATUS", "流程状态", "varchar", "36", 0, 0, Constant.NO, "draft"))),
    ORDER(Arrays.asList(LcdpTableFieldBean.of("ORDERNO", "排序码", "number", null, 18, 0, Constant.NO, "1"))),
    TREEGRID(Arrays.asList(LcdpTableFieldBean.of("SW_LVLCODE", "层次码", "varchar", "40", 0, 0, Constant.YES, null),
            LcdpTableFieldBean.of("SW_PLVLCODE", "父级层次码", "varchar", "40", 0, 0, Constant.YES, null),
            LcdpTableFieldBean.of("SW_FPLVLCODE", "父级层次码补零", "varchar", "40", 0, 0, Constant.YES, null)));
    
    private List<LcdpTableFieldBean> defaultFieldList;
    
    private LcdpTableFunction(List<LcdpTableFieldBean> defaultFieldList) {
        this.defaultFieldList = defaultFieldList;
    }
    
    public List<LcdpTableFieldBean> getDefaultFieldList() {
        return defaultFieldList;
    }
}
