package com.sunwayworld.cloud.module.lcdp.checkoutconfig.resource;

import org.springframework.web.bind.annotation.RequestMapping;

import com.sunwayworld.cloud.module.lcdp.checkoutconfig.bean.LcdpCheckoutConfigBean;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;

@RequestMapping(LcdpPathConstant.CHECKOUT_CONFIG_PATH)
public interface LcdpCheckoutConfigResource extends GenericCloudResource<LcdpCheckoutConfigBean, Long> {
}
