package com.sunwayworld.cloud.module.lcdp.config.resource.impl;

import com.sunwayworld.cloud.module.lcdp.config.resource.LcdpGlobalConfigOpenResource;
import com.sunwayworld.cloud.module.lcdp.config.service.LcdpGlobalConfigService;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@LogModule("低代码系统全局配置")
@RestController
@GikamBean
public class LcdpGlobalConfigOpenResourceImpl implements LcdpGlobalConfigOpenResource {

    @Autowired
    private LcdpGlobalConfigService lcdpGlobalConfigService;


    @Override
    public String selectSysClientCss() {
        return lcdpGlobalConfigService.selectSysClientCss();
    }
}
