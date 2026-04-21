package com.sunwayworld.cloud.module.lcdp.apiintegration.resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;

import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;

/**
 * @author yangsz@sunway.com 2023-05-26
 */
@RequestMapping(LcdpPathConstant.API_OPEN_PATH)
public interface LcdpApiResource {

    Object openApi(String requestBody, HttpServletRequest request);

    void openSoap(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
