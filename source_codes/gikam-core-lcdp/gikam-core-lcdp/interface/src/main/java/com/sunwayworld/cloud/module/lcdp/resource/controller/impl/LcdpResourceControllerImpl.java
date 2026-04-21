package com.sunwayworld.cloud.module.lcdp.resource.controller.impl;

import com.sunwayworld.cloud.module.lcdp.resource.controller.LcdpResourceController;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@GikamBean
public class LcdpResourceControllerImpl implements LcdpResourceController {

    @Log(value = "低代码平台页面选择列表页", type = LogType.CONTROLLER)
    @Override
    @RequestMapping("/page-choose-list")
    public String pageChoosePage() {
        return "module/lcdp/resources/lcdp-resource-page-choose-list";
    }

    @Log(value = "低代码平台脚本接口方法选择列表页", type = LogType.CONTROLLER)
    @Override
    @RequestMapping("/method-choose-list")
    public String methodChoosePage() {
        return "module/lcdp/resources/lcdp-resource-method-choose-list";
    }
}
