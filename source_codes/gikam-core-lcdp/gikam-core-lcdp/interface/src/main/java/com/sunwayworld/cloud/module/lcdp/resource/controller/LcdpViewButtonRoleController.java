package com.sunwayworld.cloud.module.lcdp.resource.controller;

import org.springframework.web.bind.annotation.RequestMapping;

import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.controller.GenericChoosePageController;
import com.sunwayworld.framework.controller.GenericEditListPageController;

@RequestMapping(LcdpPathConstant.VIEW_BUTTON_ROLES+"/page")
public interface LcdpViewButtonRoleController  extends GenericEditListPageController, GenericChoosePageController {
}
