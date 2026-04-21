package com.sunwayworld.cloud.module.lcdp.config.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpConfigCompareDTO;
import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpGlobalConfigBean;
import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpGlobalConfigEditDTO;
import com.sunwayworld.cloud.module.lcdp.config.resource.LcdpGlobalConfigResource;
import com.sunwayworld.cloud.module.lcdp.config.service.LcdpGlobalConfigService;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;

@LogModule("低代码系统全局配置")
@RestController
@GikamBean
public class LcdpGlobalConfigResourceImpl implements LcdpGlobalConfigResource, AbstractGenericResource<LcdpGlobalConfigService, LcdpGlobalConfigBean, Long> {

    @Autowired
    private LcdpGlobalConfigService lcdpGlobalConfigService;

    @Override
    public LcdpGlobalConfigService getService() {
        return lcdpGlobalConfigService;
    }

    @Override
    @Log(value = "根据配置编码查询激活的配置信息", type = LogType.SELECT)
    public LcdpGlobalConfigBean selectConfigContent(String configCode) {
        return getService().selectConfigContent(configCode);
    }

    @Override
    @Log(value = "提交并激活配置", type = LogType.INSERT)
    public void submit(RestJsonWrapperBean wrapper) {
        getService().submit(wrapper);
    }

    @Override
    @Log(value = "激活配置", type = LogType.ACTIVATE)
    public void activate(RestJsonWrapperBean wrapper) {
        getService().activate(wrapper);
    }

    @Override
    @Log(value = "对比配置", type = LogType.SELECT)
    public LcdpConfigCompareDTO compare(RestJsonWrapperBean wrapper) {
        return getService().compare(wrapper);
    }

    @Override
    @Log(value = "回滚配置", type = LogType.ACTIVATE)
    public void revert(RestJsonWrapperBean wrapper) {
        getService().revert(wrapper);
    }

    @Override
    @Log(value = "编辑配置内容", type = LogType.ACTIVATE)
    public LcdpGlobalConfigEditDTO selectEditContent(@PathVariable String configCode) {
        return getService().selectEditContent(configCode);
    }
    
    @Log(value = "新增或更新指定配置类型下编辑中的数据", type = LogType.INSERT)
    public Long saveEditData(@PathVariable String configCode, RestJsonWrapperBean wrapper) {
        return getService().saveEditData(configCode, wrapper);
    }
}
