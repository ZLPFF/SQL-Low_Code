package com.sunwayworld.cloud.module.lcdp.apiintegration.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiFieldBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiReqBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiResBean;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.support.activatable.service.GenericActivatableService;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpApiIntegrationService extends GenericService<LcdpApiBean, Long>, GenericActivatableService<LcdpApiBean, Long> {

    Page<LcdpApiFieldBean> selectFieldPaginationByApiIntegrationId(Long id, RestJsonWrapperBean wrapper);

    Page<LcdpApiReqBean> selectRequestPaginationByApiIntegrationId(Long id, RestJsonWrapperBean wrapper);

    Page<LcdpApiResBean> selectResponsePaginationByApiRequestId(Long id, RestJsonWrapperBean wrapper);

    Long insertField(Long id, RestJsonWrapperBean wrapper);

    void deleteField(Long id, RestJsonWrapperBean wrapper);

    LcdpApiBean selectApiByCode(String apiCode);

    LcdpApiBean selectActivatedApiByUrl(String url);

    List<LcdpApiFieldBean> selectCacheableApiFieldList(Long apiId);

    Object testApi(String apiCode, JSONObject wrapper);

    Object callRestfulApi(String url, String requestBody);

    void resetRequest(Long id);

    void callSoapApi(HttpServletRequest request, HttpServletResponse response) throws Exception;

    Object callApiScript(String apiCode, Object... args);

    String generateExportFile(RestJsonWrapperBean wrapper);

    RestValidationResultBean validateUrl(Long id, RestJsonWrapperBean wrapper);

    void importByExportFile(Long fileId, RestJsonWrapperBean wrapper);

    void editResetRequest(Long id, RestJsonWrapperBean wrapper);
}
