package com.sunwayworld.cloud.module.lcdp.checkinconfig.resource;

import org.springframework.web.bind.annotation.RequestMapping;

import com.sunwayworld.cloud.module.lcdp.checkinconfig.bean.LcdpCheckinConfigBean;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;

@RequestMapping(LcdpPathConstant.CHECKIN_CONFIG_PATH)
public interface LcdpCheckinConfigResource extends GenericCloudResource<LcdpCheckinConfigBean, Long> {
}
