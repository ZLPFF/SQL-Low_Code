package com.sunwayworld.cloud.module.lcdp.apiintegration.controller;

import org.springframework.web.bind.annotation.RequestMapping;

import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.controller.GenericEditListPageController;


@RequestMapping(LcdpPathConstant.API_NOTIFIER_PATH + "/page")
public interface LcdpApiNotifierController extends GenericEditListPageController {

}
