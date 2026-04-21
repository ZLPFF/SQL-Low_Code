package com.sunwayworld.cloud.module.lcdp.scriptsource.resource;

import com.sunwayworld.cloud.module.lcdp.scriptsource.bean.LcdpScriptSourceBean;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(LcdpPathConstant.SCRIPT_SOURCE_PATH)
public interface LcdpScriptSourceResource extends GenericCloudResource<LcdpScriptSourceBean, Long> {

    @GetMapping
    LcdpScriptSourceBean selectByFullName(@RequestParam("fullName") String fullName);

}
