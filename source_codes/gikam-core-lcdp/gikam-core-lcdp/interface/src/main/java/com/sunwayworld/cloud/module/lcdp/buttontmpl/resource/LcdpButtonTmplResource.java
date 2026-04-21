package com.sunwayworld.cloud.module.lcdp.buttontmpl.resource;

import org.springframework.web.bind.annotation.RequestMapping;

import com.sunwayworld.cloud.module.lcdp.buttontmpl.bean.LcdpButtonTmplBean;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;

@RequestMapping(LcdpPathConstant.BUTTON_TMPL_PATH)
public interface LcdpButtonTmplResource extends GenericCloudResource<LcdpButtonTmplBean, Long> {

}
