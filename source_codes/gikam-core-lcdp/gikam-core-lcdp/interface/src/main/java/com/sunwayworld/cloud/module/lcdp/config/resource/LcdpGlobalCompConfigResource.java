package com.sunwayworld.cloud.module.lcdp.config.resource;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpGlobalCompConfigBean;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;

@RequestMapping(LcdpPathConstant.GLOBAL_CONFIG_COMP_PATH)
public interface LcdpGlobalCompConfigResource extends GenericCloudResource<LcdpGlobalCompConfigBean, Long> {

    @RequestMapping(method = RequestMethod.GET)
    Map<String, List<LcdpGlobalCompConfigBean>> selectConfigList();

}
