package com.sunwayworld.cloud.module.lcdp.apiintegration.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiNotifierBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.resource.LcdpApiNotifierResource;
import com.sunwayworld.cloud.module.lcdp.apiintegration.service.LcdpApiNotifierService;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;

@LogModule("接口通知人")
@RestController
@GikamBean
public class LcdpApiNotifierResourceImpl implements LcdpApiNotifierResource, AbstractGenericResource<LcdpApiNotifierService, LcdpApiNotifierBean, Long> {

    @Autowired
    private LcdpApiNotifierService lcdpApiNotifierService;

    @Override
    public LcdpApiNotifierService getService() {
        return lcdpApiNotifierService;
    }

    @Log(value = "新增接口通知人", type = LogType.INSERT)
    @Override
    @RequestMapping(method = RequestMethod.POST)
    public Long insert(RestJsonWrapperBean wrapper) {
        return getService().insert(wrapper);
    }

}
