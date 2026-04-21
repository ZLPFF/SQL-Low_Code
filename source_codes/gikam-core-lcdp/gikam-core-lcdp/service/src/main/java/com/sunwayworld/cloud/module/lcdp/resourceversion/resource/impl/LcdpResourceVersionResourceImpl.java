package com.sunwayworld.cloud.module.lcdp.resourceversion.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.resourceversion.bean.LcdpResourceVersionBean;
import com.sunwayworld.cloud.module.lcdp.resourceversion.resource.LcdpResourceVersionResource;
import com.sunwayworld.cloud.module.lcdp.resourceversion.service.LcdpResourceVersionService;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;

@LogModule("低代码平台资源版本")
@RestController
@GikamBean
public class LcdpResourceVersionResourceImpl implements LcdpResourceVersionResource,AbstractGenericResource<LcdpResourceVersionService, LcdpResourceVersionBean, Long> {

    @Autowired
    private LcdpResourceVersionService lcdpResourceVersionService;

    @Override
    public LcdpResourceVersionService getService() {
        return lcdpResourceVersionService;
    }

    @Override
    @Log(value = "历史数据处理(补全版本表更新后部分字段值缺失问题)", type = LogType.UPDATE)
    public void dealHistoryData(RestJsonWrapperBean wrapper) {
        getService().dealHistoryData(wrapper);
    }
}
