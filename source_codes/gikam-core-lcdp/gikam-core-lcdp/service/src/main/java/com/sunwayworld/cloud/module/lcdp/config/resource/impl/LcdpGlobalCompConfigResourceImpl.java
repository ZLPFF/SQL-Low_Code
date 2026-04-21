package com.sunwayworld.cloud.module.lcdp.config.resource.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpGlobalCompConfigBean;
import com.sunwayworld.cloud.module.lcdp.config.resource.LcdpGlobalCompConfigResource;
import com.sunwayworld.cloud.module.lcdp.config.service.LcdpGlobalCompConfigService;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;

@LogModule("低代码系统全局组件配置")
@RestController
@GikamBean
public class LcdpGlobalCompConfigResourceImpl implements LcdpGlobalCompConfigResource, AbstractGenericResource<LcdpGlobalCompConfigService, LcdpGlobalCompConfigBean, Long> {

    @Autowired
    private LcdpGlobalCompConfigService lcdpGlobalCompConfigService;

    @Override
    public LcdpGlobalCompConfigService getService() {
        return lcdpGlobalCompConfigService;
    }

    @Log(value = "新增低代码系统全局组件配置", type = LogType.INSERT)
    @Override
    public Long insert(RestJsonWrapperBean wrapper) {
        return getService().insert(wrapper);
    }

    @Log(value = "查询低代码系统全局组件配置", type = LogType.SELECT)
    @Override
    public Map<String, List<LcdpGlobalCompConfigBean>> selectConfigList() {
        return getService().selectConfigList();
    }
}
