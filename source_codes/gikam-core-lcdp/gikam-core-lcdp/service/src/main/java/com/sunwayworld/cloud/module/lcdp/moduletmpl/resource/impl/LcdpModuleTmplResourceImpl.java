package com.sunwayworld.cloud.module.lcdp.moduletmpl.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.moduletmpl.bean.LcdpModuleTmplBean;
import com.sunwayworld.cloud.module.lcdp.moduletmpl.resource.LcdpModuleTmplResource;
import com.sunwayworld.cloud.module.lcdp.moduletmpl.service.LcdpModuleTmplService;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;

@LogModule("模块模板")
@RestController
@GikamBean
public class LcdpModuleTmplResourceImpl implements LcdpModuleTmplResource,
        AbstractGenericResource<LcdpModuleTmplService, LcdpModuleTmplBean, Long> {

    @Autowired
    private LcdpModuleTmplService lcdpModuleTmplService;

    @Override
    public LcdpModuleTmplService getService() {
        return lcdpModuleTmplService;
    }

    @Log(value = "新增模块模板", type = LogType.INSERT)
    @Override
    @RequestMapping(method = RequestMethod.POST)
    public Long insert(RestJsonWrapperBean wrapper) {
        return getService().insert(wrapper);
    }


    @Override
    public void saveCustomTemplate(RestJsonWrapperBean wrapper) {
        getService().saveCustomTemplate(wrapper);
    }

    @Override
    public Page<LcdpModuleTmplBean> selectCustomTemplatePagination(RestJsonWrapperBean wrapper) {
        return getService().selectCustomTemplatePagination(wrapper);
    }
}
