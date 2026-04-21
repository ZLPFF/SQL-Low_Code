package com.sunwayworld.cloud.module.lcdp.configparam.resource;

import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.configparam.bean.LcdpConfigParamBean;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(LcdpPathConstant.CONFIG_PARAM_PATH)
public interface LcdpConfigParamResource extends GenericCloudResource<LcdpConfigParamBean, Long> {

    @RequestMapping(value = "/user-config", method = RequestMethod.GET)
    JSONObject selectUserConfigParam();

    @RequestMapping(value = "/submit-config", method = RequestMethod.GET)
    LcdpConfigParamBean selectSubmitConfigParam(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/action/hints-config", method = RequestMethod.PUT)
    void hintsConfig(RestJsonWrapperBean wrapper);
}
