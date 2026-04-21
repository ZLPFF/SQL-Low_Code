package com.sunwayworld.cloud.module.lcdp.buttontmpl.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.buttontmpl.bean.LcdpButtonTmplBean;
import com.sunwayworld.cloud.module.lcdp.buttontmpl.resource.LcdpButtonTmplResource;
import com.sunwayworld.cloud.module.lcdp.buttontmpl.service.LcdpButtonTmplService;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;

@LogModule("按钮模板表")
@RestController
@GikamBean
public class LcdpButtonTmplResourceImpl implements LcdpButtonTmplResource, AbstractGenericResource<LcdpButtonTmplService, LcdpButtonTmplBean, Long> {

    @Autowired
    private LcdpButtonTmplService lcdpButtonTmplService;

    @Override
    public LcdpButtonTmplService getService() {
        return lcdpButtonTmplService;
    }

    @Log(value = "新增按钮模板表", type = LogType.INSERT)
    @Override
    @RequestMapping(method = RequestMethod.POST)
    public Long insert(RestJsonWrapperBean wrapper) {
        return getService().insert(wrapper);
    }
}
