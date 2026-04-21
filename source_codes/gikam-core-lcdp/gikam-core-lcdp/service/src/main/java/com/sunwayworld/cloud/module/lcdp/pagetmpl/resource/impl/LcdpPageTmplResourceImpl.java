package com.sunwayworld.cloud.module.lcdp.pagetmpl.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.pagetmpl.bean.LcdpPageTmplBean;
import com.sunwayworld.cloud.module.lcdp.pagetmpl.resource.LcdpPageTmplResource;
import com.sunwayworld.cloud.module.lcdp.pagetmpl.service.LcdpPageTmplService;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;

@LogModule("页面模板表")
@RestController
@GikamBean
public class LcdpPageTmplResourceImpl implements LcdpPageTmplResource,
        AbstractGenericResource<LcdpPageTmplService, LcdpPageTmplBean, Long> {

    @Autowired
    private LcdpPageTmplService lcdpPageTmplService;

    @Override
    public LcdpPageTmplService getService() {
        return lcdpPageTmplService;
    }

    @Log(value = "新增页面模板表", type = LogType.INSERT)
    @Override
    @RequestMapping(method = RequestMethod.POST)
    public Long insert(RestJsonWrapperBean wrapper) {
        return getService().insert(wrapper);
    }



}
