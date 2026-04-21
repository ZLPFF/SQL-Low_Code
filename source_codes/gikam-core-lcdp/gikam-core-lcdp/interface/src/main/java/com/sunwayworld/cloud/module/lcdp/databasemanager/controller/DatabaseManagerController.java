package com.sunwayworld.cloud.module.lcdp.databasemanager.controller;

import org.springframework.web.bind.annotation.RequestMapping;

import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;

@RequestMapping(LcdpPathConstant.DATABASE_MANAGER_PATH+"/page")
public interface DatabaseManagerController {
    @RequestMapping("/detail")
    public String detailPage();
}
