package com.sunwayworld.cloud.module.lcdp.config.resource;

import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(LcdpPathConstant.GLOBAL_CONFIG_OPEN_PATH)
public interface LcdpGlobalConfigOpenResource {

    @RequestMapping(value = "/sys-client-css", method = RequestMethod.GET)
    String selectSysClientCss();
}
