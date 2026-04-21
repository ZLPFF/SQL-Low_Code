package com.sunwayworld.cloud.module.lcdp.checkoutconfig.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.checkoutconfig.bean.LcdpCheckoutConfigBean;
import com.sunwayworld.cloud.module.lcdp.checkoutconfig.resource.LcdpCheckoutConfigResource;
import com.sunwayworld.cloud.module.lcdp.checkoutconfig.service.LcdpCheckoutConfigService;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.activatable.resource.AbstractGenericActivatableResource;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;

@LogModule("迁出配置表")
@RestController
@GikamBean
public class LcdpCheckoutConfigResourceImpl implements LcdpCheckoutConfigResource, AbstractGenericResource<LcdpCheckoutConfigService, LcdpCheckoutConfigBean, Long>, AbstractGenericActivatableResource<LcdpCheckoutConfigService, LcdpCheckoutConfigBean, Long> {

    @Autowired
    private LcdpCheckoutConfigService lcdpCheckoutConfigService;

    @Override
    public LcdpCheckoutConfigService getService() {
        return lcdpCheckoutConfigService;
    }

    @Log(value = "新增迁出配置表", type = LogType.INSERT)
    @Override
    @RequestMapping(method = RequestMethod.POST)
    public Long insert(RestJsonWrapperBean wrapper) {
        return getService().insert(wrapper);
    }

}
