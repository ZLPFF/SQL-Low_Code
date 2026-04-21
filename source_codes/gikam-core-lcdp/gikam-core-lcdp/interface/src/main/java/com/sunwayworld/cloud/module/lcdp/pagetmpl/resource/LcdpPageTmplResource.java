package com.sunwayworld.cloud.module.lcdp.pagetmpl.resource;

import org.springframework.web.bind.annotation.RequestMapping;

import com.sunwayworld.cloud.module.lcdp.pagetmpl.bean.LcdpPageTmplBean;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;

@RequestMapping(LcdpPathConstant.PAGE_TMPL_PATH)
public interface LcdpPageTmplResource extends GenericCloudResource<LcdpPageTmplBean, Long> {

}
