package com.sunwayworld.cloud.module.lcdp.resource.controller;

import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(LcdpPathConstant.RESOURCE_PATH + "/page")
public interface LcdpResourceController {

    String pageChoosePage();

    String methodChoosePage();
}
