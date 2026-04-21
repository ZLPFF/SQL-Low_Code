package com.sunwayworld.cloud.module.lcdp.index.homepage.controller.impl;

import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.module.index.homepage.controller.impl.CoreHomepageControllerImpl;
import org.springframework.stereotype.Controller;

@Controller
@GikamBean
public class LcdpHomepageControllerImpl extends CoreHomepageControllerImpl {

    @Override
    @Log(value = "首页", type = LogType.CONTROLLER)
    public String indexPage() {
        return "module/lcdp/index/homepage/index";
    }

}
