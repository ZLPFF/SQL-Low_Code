package com.sunwayworld.cloud.module.lcdp.table.controller.impl;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sunwayworld.cloud.module.lcdp.table.controller.LcdpTableController;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Controller
@GikamBean
public class LcdpTableControllerImpl implements LcdpTableController {

    @Log(value = "低代码平台表选择列表页", type = LogType.CONTROLLER)
    @Override
    @RequestMapping("/page-choose-list")
    public String choosePage() {
        return "module/lcdp/tables/lcdp-table-choose-list";
    }

}
