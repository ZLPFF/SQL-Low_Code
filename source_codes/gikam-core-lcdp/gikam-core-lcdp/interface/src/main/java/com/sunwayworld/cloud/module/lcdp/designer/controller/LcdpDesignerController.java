package com.sunwayworld.cloud.module.lcdp.designer.controller;

import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/secure/core/module/lcdp/designers/page")
public interface LcdpDesignerController {
    public String indexPage();
    
    public String runPage();
}
