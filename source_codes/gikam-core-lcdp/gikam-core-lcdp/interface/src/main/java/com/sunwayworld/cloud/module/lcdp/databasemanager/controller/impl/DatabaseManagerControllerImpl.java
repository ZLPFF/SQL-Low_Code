package com.sunwayworld.cloud.module.lcdp.databasemanager.controller.impl;

import org.springframework.stereotype.Controller;

import com.sunwayworld.cloud.module.lcdp.databasemanager.controller.DatabaseManagerController;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Controller
@GikamBean
public class DatabaseManagerControllerImpl implements DatabaseManagerController {
    @Override
    public String detailPage() {
        return "module/lcdp/database-managers/lcdp-database-manager-detail";
    }
}
