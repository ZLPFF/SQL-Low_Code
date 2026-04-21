package com.sunwayworld.cloud.module.lcdp.apiintegration.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiFieldBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiReqBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiResBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.resource.LcdpApiIntegrationResource;
import com.sunwayworld.cloud.module.lcdp.apiintegration.service.LcdpApiIntegrationService;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.activatable.resource.AbstractGenericActivatableResource;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;

@LogModule("接口集成")
@RestController
@GikamBean
public class LcdpApiIntegrationResourceImpl implements LcdpApiIntegrationResource, AbstractGenericResource<LcdpApiIntegrationService, LcdpApiBean, Long>, AbstractGenericActivatableResource<LcdpApiIntegrationService, LcdpApiBean, Long> {

    @Autowired
    private LcdpApiIntegrationService lcdpApiIntegrationService;

    @Override
    public LcdpApiIntegrationService getService() {
        return lcdpApiIntegrationService;
    }

    @Log(value = "新增接口集成", type = LogType.INSERT)
    @Override
    @RequestMapping(method = RequestMethod.POST)
    public Long insert(RestJsonWrapperBean wrapper) {
        return getService().insert(wrapper);
    }

    @Override
    @Log(value = "根据接口集成ID查询接口响应字段映射", type = LogType.SELECT)
    @RequestMapping(value = "/{id}/fields/queries", method = RequestMethod.POST)
    public Page<LcdpApiFieldBean> selectFieldPaginationByApiIntegrationId(@PathVariable Long id, RestJsonWrapperBean wrapper) {
        return getService().selectFieldPaginationByApiIntegrationId(id, wrapper);
    }

    @Override
    @Log(value = "新增接口响应字段映射", type = LogType.INSERT)
    @RequestMapping(value = "/{id}/fields", method = RequestMethod.POST)
    public Long insertField(@PathVariable Long id, RestJsonWrapperBean wrapper) {
        return getService().insertField(id, wrapper);
    }

    @Override
    @Log(value = "删除接口响应字段映射", type = LogType.DELETE)
    @RequestMapping(value = "/{id}/fields", method = RequestMethod.DELETE)
    public void deleteField(@PathVariable Long id, RestJsonWrapperBean wrapper) {
        getService().deleteField(id, wrapper);
    }

    @Override
    @Log(value = "测试接口集成接口", type = LogType.SELECT)
    @RequestMapping(value = "/{apiCode}/action/test", method = RequestMethod.POST)
    public Object testApi(@PathVariable String apiCode, @RequestBody JSONObject json) {
        return getService().testApi(apiCode, json);
    }

    @Override
    @Log(value = "根据接口集成ID查询接口响应", type = LogType.SELECT)
    @RequestMapping(value = "/{id}/requests/queries", method = RequestMethod.POST)
    public Page<LcdpApiReqBean> selectRequestPaginationByApiIntegrationId(@PathVariable Long id, RestJsonWrapperBean wrapper) {
        return getService().selectRequestPaginationByApiIntegrationId(id, wrapper);
    }

    @Override
    @Log(value = "根据接口请求ID查询接口响应", type = LogType.SELECT)
    @RequestMapping(value = "/requests/{id}/responses/queries", method = RequestMethod.POST)
    public Page<LcdpApiResBean> selectResponsePaginationByApiRequestId(@PathVariable Long id, RestJsonWrapperBean wrapper) {
        return getService().selectResponsePaginationByApiRequestId(id, wrapper);
    }

    @Override
    @Log(value = "重试接口请求", type = LogType.UPDATE)
    @RequestMapping(value = "/requests/{id}/action/reset", method = RequestMethod.POST)
    public void resetRequest(@PathVariable Long id) {
        getService().resetRequest(id);
    }

    @Override
    @Log(value = "生成导出文件", type = LogType.EXPORT)
    @RequestMapping(value = "/action/generate-export", method = RequestMethod.POST)
    public String generateExportFile(RestJsonWrapperBean wrapper) {
        return getService().generateExportFile(wrapper);
    }

    @Override
    @Log(value = "导入", type = LogType.IMPORT)
    @RequestMapping(value = "/action/import/{fileId}", method = RequestMethod.POST)
    public void importByExportFile(@PathVariable Long fileId, RestJsonWrapperBean wrapper) {
        getService().importByExportFile(fileId, wrapper);
    }


    @Override
    @Log(value = "校验url", type = LogType.VALIDATE)
    @RequestMapping(value = "/{id}/action/validate-url", method = RequestMethod.POST)
    public RestValidationResultBean validateUrl(@PathVariable Long id, RestJsonWrapperBean wrapper) {
        return getService().validateUrl(id, wrapper);
    }

    @Override
    @Log(value = "重试接口请求", type = LogType.UPDATE)
    @RequestMapping(value = "/requests/{id}/action/editreset", method = RequestMethod.POST)
    public void editResetRequest(@PathVariable Long id,RestJsonWrapperBean wrapper) {
        getService().editResetRequest(id,wrapper);
    }
}
