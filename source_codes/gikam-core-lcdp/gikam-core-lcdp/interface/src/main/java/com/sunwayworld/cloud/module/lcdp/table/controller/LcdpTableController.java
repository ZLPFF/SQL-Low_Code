package com.sunwayworld.cloud.module.lcdp.table.controller;

import org.springframework.web.bind.annotation.RequestMapping;

import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.controller.GenericChoosePageController;

@RequestMapping(LcdpPathConstant.TABLE_PATH + "/page")
public interface LcdpTableController extends GenericChoosePageController {
}
