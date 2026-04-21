package com.sunwayworld.cloud.module.lcdp.table.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableCompareDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.cloud.module.lcdp.table.resource.LcdpViewResource;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpViewService;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;

@LogModule("低代码平台视图")
@RestController
@GikamBean
public class LcdpViewResourceImpl implements LcdpViewResource, AbstractGenericResource<LcdpViewService, LcdpViewBean, Long> {

    @Autowired
    private LcdpViewService lcdpViewService;

    @Override
    public LcdpViewService getService() {
        return lcdpViewService;
    }

    @Log(value = "设计视图", type = LogType.INSERT)
    @Override
    public Long design(@PathVariable String viewName) {
        return getService().design(viewName);
    }

    @Log(value = "检出视图", type = LogType.INSERT)
    @Override
    public void checkout(@PathVariable String viewName) {
        getService().checkout(viewName);
    }

    @Log(value = "视图对比", type = LogType.SELECT)
    @Override
    public LcdpTableCompareDTO<LcdpViewBean> compare(RestJsonWrapperBean wrapper) {
        return getService().compare(wrapper);
    }

    @Log(value = "根据视图名查询视图信息", type = LogType.SELECT)
    @Override
    public LcdpViewBean selectPhysicalViewInfo(@PathVariable String viewName) {
        return getService().selectPhysicalViewInfo(viewName);
    }
}
