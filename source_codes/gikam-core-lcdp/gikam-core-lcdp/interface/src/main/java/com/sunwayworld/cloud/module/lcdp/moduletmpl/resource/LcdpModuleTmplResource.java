package com.sunwayworld.cloud.module.lcdp.moduletmpl.resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.moduletmpl.bean.LcdpModuleTmplBean;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;

@RequestMapping(LcdpPathConstant.MODULE_TMPL_PATH)
public interface LcdpModuleTmplResource extends GenericCloudResource<LcdpModuleTmplBean, Long> {

    @RequestMapping(value = "/save-custom-template", method = RequestMethod.POST)
    void saveCustomTemplate(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/custom-template/queries", method = RequestMethod.POST)
    Page<LcdpModuleTmplBean> selectCustomTemplatePagination(RestJsonWrapperBean wrapper);
}
