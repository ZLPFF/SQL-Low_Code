package com.sunwayworld.cloud.module.lcdp.resource.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceCheckoutRecordBean;
import com.sunwayworld.cloud.module.lcdp.resource.resource.LcdpResourceCheckoutRecordResource;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceCheckoutRecordService;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;

@LogModule("低代码平台资源检出记录表")
@RestController
@GikamBean
public class LcdpResourceCheckoutRecordResourceImpl implements LcdpResourceCheckoutRecordResource, AbstractGenericResource<LcdpResourceCheckoutRecordService, LcdpResourceCheckoutRecordBean, Long> {

    @Autowired
    private LcdpResourceCheckoutRecordService lcdpResourceCheckoutRecordService;

    @Override
    public LcdpResourceCheckoutRecordService getService() {
        return lcdpResourceCheckoutRecordService;
    }

}
