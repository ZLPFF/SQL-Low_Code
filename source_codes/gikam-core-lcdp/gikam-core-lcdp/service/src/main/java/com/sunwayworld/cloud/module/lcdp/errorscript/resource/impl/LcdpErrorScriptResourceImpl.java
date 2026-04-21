package com.sunwayworld.cloud.module.lcdp.errorscript.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.errorscript.bean.LcdpErrorScriptBean;
import com.sunwayworld.cloud.module.lcdp.errorscript.resource.LcdpErrorScriptResource;
import com.sunwayworld.cloud.module.lcdp.errorscript.service.LcdpErrorScriptService;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;

@LogModule("java脚本加载错误记录表")
@RestController
@GikamBean
public class LcdpErrorScriptResourceImpl implements LcdpErrorScriptResource, AbstractGenericResource<LcdpErrorScriptService, LcdpErrorScriptBean, Long> {

    @Autowired
    private LcdpErrorScriptService lcdpErrorScriptService;

    @Override
    public LcdpErrorScriptService getService() {
        return lcdpErrorScriptService;
    }

    @Log(value = "新增java脚本加载错误记录表", type = LogType.INSERT)
    @Override
    @RequestMapping(method = RequestMethod.POST)
    public Long insert(RestJsonWrapperBean wrapper) {
        return getService().insert(wrapper);
    }

    @Override
    @RequestMapping(value = "/queries/warning", method = RequestMethod.POST)
    @Log(value = "查询编译错误记录表", type = LogType.SELECT)
    public Page<LcdpErrorScriptBean> selectWarningPagination(RestJsonWrapperBean wrapper) {
        return getService().selectWarningPagination(wrapper);
    }
    
    @Override
    @RequestMapping(value = "/number-of-warnings", method = RequestMethod.GET)
    @Log(value = "查询编译错误记录数量", type = LogType.SELECT)
    public Long selectNumberOfWarnings() {
        return getService().selectNumberOfWarnings();
    }
}
