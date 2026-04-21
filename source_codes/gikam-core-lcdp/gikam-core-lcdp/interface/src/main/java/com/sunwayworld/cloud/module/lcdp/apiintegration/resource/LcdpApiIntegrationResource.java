package com.sunwayworld.cloud.module.lcdp.apiintegration.resource;

import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiFieldBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiReqBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiResBean;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.support.activatable.resource.GenericActivatableCloudResource;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;


@RequestMapping(LcdpPathConstant.API_INTEGRATION_PATH)
public interface LcdpApiIntegrationResource extends GenericCloudResource<LcdpApiBean, Long>, GenericActivatableCloudResource<LcdpApiBean, Long> {

    Page<LcdpApiFieldBean> selectFieldPaginationByApiIntegrationId(Long id, RestJsonWrapperBean wrapper);

    Long insertField(Long id, RestJsonWrapperBean wrapper);

    void deleteField(Long id, RestJsonWrapperBean wrapper);

    Object testApi(String apiCode, JSONObject json);

    Page<LcdpApiReqBean> selectRequestPaginationByApiIntegrationId(Long id, RestJsonWrapperBean wrapper);

    Page<LcdpApiResBean> selectResponsePaginationByApiRequestId(Long id, RestJsonWrapperBean wrapper);

    void resetRequest(Long id);

    String generateExportFile(RestJsonWrapperBean wrapper);

    void importByExportFile(Long fileId, RestJsonWrapperBean wrapper);

    RestValidationResultBean validateUrl(Long id, RestJsonWrapperBean wrapper);

    void editResetRequest(Long id, RestJsonWrapperBean wrapper);
}
