package com.sunwayworld.cloud.module.lcdp.apiintegration.controller.impl;

import org.springframework.stereotype.Controller;

import com.sunwayworld.cloud.module.lcdp.apiintegration.controller.LcdpApiIntegrationController;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Controller
@GikamBean
public class LcdpApiIntegrationControllerImpl implements LcdpApiIntegrationController {

    @Override
    public String editListPage() {
        return "module/lcdp/api-integrations/lcdp-api-integration-edit-list";
    }

    @Override
    public String logPage() {
        return "module/lcdp/api-integrations/lcdp-api-integration-log-list";
    }

}
