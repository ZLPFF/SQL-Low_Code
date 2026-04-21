package com.sunwayworld.cloud.module.lcdp.designer.controller.impl;

import com.sunwayworld.cloud.module.lcdp.designer.controller.LcdpDesignerController;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@GikamBean
public class LcdpDesignerControllerImpl implements LcdpDesignerController {

    @Override
    @Log(value = "首页", type = LogType.CONTROLLER)
    @RequestMapping("/index")
    public String indexPage() {
        return "module/lcdp/designers/designer";
    }
    
    @Override
    @Log(value = "运行页面", type = LogType.CONTROLLER)
    @RequestMapping("/run")
    public String runPage() {
        return "module/lcdp/designers/designer-run";
    }
    
}
