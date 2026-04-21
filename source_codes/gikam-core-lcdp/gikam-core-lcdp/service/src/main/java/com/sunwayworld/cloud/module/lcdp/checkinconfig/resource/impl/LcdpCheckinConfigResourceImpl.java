package com.sunwayworld.cloud.module.lcdp.checkinconfig.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.checkinconfig.bean.LcdpCheckinConfigBean;
import com.sunwayworld.cloud.module.lcdp.checkinconfig.resource.LcdpCheckinConfigResource;
import com.sunwayworld.cloud.module.lcdp.checkinconfig.service.LcdpCheckinConfigService;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.activatable.resource.AbstractGenericActivatableResource;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;

@LogModule("迁入配置表")
@RestController
@GikamBean
public class LcdpCheckinConfigResourceImpl implements LcdpCheckinConfigResource, AbstractGenericResource<LcdpCheckinConfigService, LcdpCheckinConfigBean, Long>, AbstractGenericActivatableResource<LcdpCheckinConfigService, LcdpCheckinConfigBean, Long> {

    @Autowired
    private LcdpCheckinConfigService lcdpCheckinConfigService;

    @Override
    public LcdpCheckinConfigService getService() {
        return lcdpCheckinConfigService;
    }

    @Log(value = "新增迁入配置表", type = LogType.INSERT)
    @Override
    @RequestMapping(method = RequestMethod.POST)
    public Long insert(RestJsonWrapperBean wrapper) {
        return getService().insert(wrapper);
    }

}
