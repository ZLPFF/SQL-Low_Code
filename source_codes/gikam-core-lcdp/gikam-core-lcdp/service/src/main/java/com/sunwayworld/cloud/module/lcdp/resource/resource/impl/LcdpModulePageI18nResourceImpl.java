package com.sunwayworld.cloud.module.lcdp.resource.resource.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.resource.resource.LcdpModulePageI18nResource;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpModulePageI18nService;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@LogModule("低代码平台资源检出记录表")
@RestController
@GikamBean
public class LcdpModulePageI18nResourceImpl implements LcdpModulePageI18nResource {

    @Autowired
    private LcdpModulePageI18nService lcdpModulePageI18nService;

    public LcdpModulePageI18nService getService() {
        return lcdpModulePageI18nService;
    }

    @Override
    @Log(value = "根据国际化编码查询国际化", type = LogType.SELECT)
    public Map<String, String> selectI18nMessageByCode(RestJsonWrapperBean jsonWrapper) {
        return getService().selectI18nMessageByCode(jsonWrapper);
    }

    @Override
    @Log(value = "根据国际化编码查询国际化", type = LogType.SELECT)
    public Page<Map<String, Object>> selectAllI18nMessage(RestJsonWrapperBean jsonWrapper) {
        return getService().selectAllI18nMessage(jsonWrapper);
    }

    @Override
    @Log(value = "变更国际化信息", type = LogType.UPDATE)
    public void alterMessage(RestJsonWrapperBean wrapper) {
        getService().alterMessage(wrapper);
    }

    @Override
    @Log(value = "导出全部国际化信息", type = LogType.EXPORT)
    public String export(RestJsonWrapperBean jsonWrapper) {
        return getService().export(jsonWrapper);
    }

    @Override
    @Log(value = "刷新国际化信息", type = LogType.INSERT)
    public void refreshI18nMessage(RestJsonWrapperBean jsonWrapper) {
        getService().refreshI18nMessage(jsonWrapper);
    }

}
