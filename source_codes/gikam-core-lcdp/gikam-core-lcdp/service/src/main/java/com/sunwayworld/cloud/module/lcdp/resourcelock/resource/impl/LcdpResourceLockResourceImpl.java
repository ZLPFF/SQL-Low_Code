package com.sunwayworld.cloud.module.lcdp.resourcelock.resource.impl;

import com.sunwayworld.cloud.module.lcdp.resourcelock.bean.LcdpResourceLockBean;
import com.sunwayworld.cloud.module.lcdp.resourcelock.resource.LcdpResourceLockResource;
import com.sunwayworld.cloud.module.lcdp.resourcelock.service.LcdpResourceLockService;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@LogModule("低代码平台资源锁定")
@RestController
@GikamBean
public class LcdpResourceLockResourceImpl implements LcdpResourceLockResource,AbstractGenericResource<LcdpResourceLockService, LcdpResourceLockBean, Long> {

    @Autowired
    private LcdpResourceLockService lcdpResourceLockService;

    @Override
    public LcdpResourceLockService getService() {
        return lcdpResourceLockService;
    }


    @Override
    @Log(value = "校验资源是否可检出", type = LogType.SELECT)
    public RestValidationResultBean validateResourceEditable(String resourceId) {
        return getService().validateResourceEditable(resourceId);
    }
}
