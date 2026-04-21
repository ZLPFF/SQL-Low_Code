package com.sunwayworld.cloud.module.lcdp.apiintegration.controller.impl;

import org.springframework.stereotype.Controller;

import com.sunwayworld.cloud.module.lcdp.apiintegration.controller.LcdpApiNotifierController;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Controller
@GikamBean
public class LcdpApiNotifierControllerImpl implements LcdpApiNotifierController {

    @Override
    public String editListPage() {
        return "module/lcdp/api-integrations/lcdp-api-notifier-edit-list";
    }
}
