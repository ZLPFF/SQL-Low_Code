package com.sunwayworld.cloud.module.lcdp.configparam.resource.impl;

import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.configparam.bean.LcdpConfigParamBean;
import com.sunwayworld.cloud.module.lcdp.configparam.resource.LcdpConfigParamResource;
import com.sunwayworld.cloud.module.lcdp.configparam.service.LcdpConfigParamService;
import com.sunwayworld.cloud.module.lcdp.configparam.validator.LcdpConfigParamSaveValidator;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;
import com.sunwayworld.framework.validator.data.annotation.ValidateDataWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@LogModule("配置参数表")
@RestController
@GikamBean
public class LcdpConfigParamResourceImpl implements LcdpConfigParamResource, AbstractGenericResource<LcdpConfigParamService, LcdpConfigParamBean, Long> {

    @Autowired
    private LcdpConfigParamService lcdpConfigParamService;

    @Override
    public LcdpConfigParamService getService() {
        return lcdpConfigParamService;
    }

    @Log(value = "新增配置参数表", type = LogType.INSERT)
    @Override
    @RequestMapping(method = RequestMethod.POST)
    public Long insert(RestJsonWrapperBean wrapper) {
        return getService().insert(wrapper);
    }

    @Log(value = "查询用户的配置参数", type = LogType.SELECT)
    @Override
    public JSONObject selectUserConfigParam() {
        return getService().selectUserConfigParam();
    }

    @Log(value = "查询提交模板的配置参数", type = LogType.SELECT)
    @Override
    public LcdpConfigParamBean selectSubmitConfigParam(RestJsonWrapperBean wrapper) {
        return getService().selectSubmitConfigParam(wrapper);
    }

    @Log(value = "在线配置自动导包", type = LogType.UPDATE)
    @Override
    public void hintsConfig(RestJsonWrapperBean wrapper) {
        getService().hintsConfig(wrapper);
    }

    @Log(value = "配置参数实时修改", type = LogType.UPDATE)
    @Override
    @ValidateDataWith(LcdpConfigParamSaveValidator.class)
    public void instantSave(RestJsonWrapperBean wrapper) {
        getService().instantSave(wrapper);
    }
}
