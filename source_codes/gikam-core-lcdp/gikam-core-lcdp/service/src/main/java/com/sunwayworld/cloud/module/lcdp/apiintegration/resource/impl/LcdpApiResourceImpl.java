package com.sunwayworld.cloud.module.lcdp.apiintegration.resource.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunwayworld.cloud.module.lcdp.apiintegration.resource.LcdpApiResource;
import com.sunwayworld.cloud.module.lcdp.apiintegration.service.LcdpApiIntegrationService;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.ServletUtils;

/**
 * @author yangsz@sunway.com 2023-05-23
 */
@Controller
@GikamBean
public class LcdpApiResourceImpl implements LcdpApiResource {

    @Autowired
    private LcdpApiIntegrationService lcdpApiIntegrationService;

    @Override
    @Log("低代码接口集成restful开放接口")
    @RequestMapping(path = "/api/**", method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Object openApi(@RequestBody(required = false) String requestBody, HttpServletRequest request) {
        return lcdpApiIntegrationService.callRestfulApi(ServletUtils.getRequestUri(request), requestBody);
    }

    @Override
    @Log("低代码接口集成webservice开放接口")
    @RequestMapping(path = "/ws/**", method = {RequestMethod.GET, RequestMethod.POST})
    public void openSoap(HttpServletRequest request, HttpServletResponse response) throws Exception {
        lcdpApiIntegrationService.callSoapApi(request, response);
    }
}
