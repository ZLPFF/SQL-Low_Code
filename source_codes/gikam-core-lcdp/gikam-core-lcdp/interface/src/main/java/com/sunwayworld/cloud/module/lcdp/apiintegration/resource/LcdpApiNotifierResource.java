package com.sunwayworld.cloud.module.lcdp.apiintegration.resource;

import org.springframework.web.bind.annotation.RequestMapping;

import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiNotifierBean;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;

@RequestMapping("/secure/cloud/module/lcdp/api-notifiers")
public interface LcdpApiNotifierResource extends GenericCloudResource<LcdpApiNotifierBean, Long> {

}
