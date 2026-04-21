package com.sunwayworld.cloud.module.lcdp.config.resource;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpConfigCompareDTO;
import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpGlobalConfigBean;
import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpGlobalConfigEditDTO;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;

@RequestMapping(LcdpPathConstant.GLOBAL_CONFIG_PATH)
public interface LcdpGlobalConfigResource extends GenericCloudResource<LcdpGlobalConfigBean, Long> {

    @RequestMapping(value = "/{configCode}/content", method = RequestMethod.GET)
    LcdpGlobalConfigBean selectConfigContent(@PathVariable String configCode);

    @RequestMapping(value = "/action/submit", method = RequestMethod.POST)
    void submit(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/action/activate", method = RequestMethod.POST)
    void activate(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/action/compare", method = RequestMethod.POST)
    LcdpConfigCompareDTO compare(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/action/revert", method = RequestMethod.POST)
    void revert(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/config-codes/{configCode}", method = RequestMethod.POST)
    Long saveEditData(@PathVariable String configCode, RestJsonWrapperBean wrapper);
    
    @RequestMapping(value = "/config-codes/{configCode}/edit-content", method = RequestMethod.GET)
    LcdpGlobalConfigEditDTO selectEditContent(@PathVariable String configCode);
}
