package com.sunwayworld.cloud.module.lcdp.resource.resource;

import org.springframework.web.bind.annotation.RequestMapping;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceCheckoutRecordBean;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;

@RequestMapping(LcdpPathConstant.RESOURCE_CHECKOUT_RECORD_PATH)
public interface LcdpResourceCheckoutRecordResource  extends GenericCloudResource<LcdpResourceCheckoutRecordBean, Long> {

}
