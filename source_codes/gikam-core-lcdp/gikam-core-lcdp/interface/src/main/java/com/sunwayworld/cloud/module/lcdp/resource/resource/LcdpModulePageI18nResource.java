package com.sunwayworld.cloud.module.lcdp.resource.resource;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

@RequestMapping(LcdpPathConstant.PAGE_I18N_PATH)
public interface LcdpModulePageI18nResource {

    @RequestMapping(value = "/search-by-code", method = RequestMethod.POST)
    Map<String,String> selectI18nMessageByCode(RestJsonWrapperBean jsonWrapper);

    @RequestMapping(value = "/queries", method = RequestMethod.POST)
    Page<Map<String, Object>> selectAllI18nMessage(RestJsonWrapperBean jsonWrapper);

    @RequestMapping(method = RequestMethod.PUT)
    void alterMessage(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/action/export", method = RequestMethod.POST)
    String export(RestJsonWrapperBean jsonWrapper);

    @RequestMapping(value = "/action/refresh", method = RequestMethod.POST)
    void refreshI18nMessage(RestJsonWrapperBean jsonWrapper);


}
