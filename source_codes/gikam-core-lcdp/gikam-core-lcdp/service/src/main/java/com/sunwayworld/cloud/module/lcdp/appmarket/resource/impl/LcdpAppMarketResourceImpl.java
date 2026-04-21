package com.sunwayworld.cloud.module.lcdp.appmarket.resource.impl;

import com.sunwayworld.cloud.module.lcdp.appmarket.resource.LcdpAppMarketResource;
import com.sunwayworld.cloud.module.lcdp.appmarket.service.LcdpAppMarketService;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@GikamBean
@RestController
@LogModule("应用超市")
public class LcdpAppMarketResourceImpl implements LcdpAppMarketResource {
    @Autowired
    private LcdpAppMarketService appMarketService;

    @Override
    @Log(value = "查询系统编码", type = LogType.SELECT)
    @RequestMapping(value = "/codes/{codeCategoryId}/queries", method = RequestMethod.GET)
    public Object getCodeList(@PathVariable String codeCategoryId) {
        return appMarketService.getCodeList(codeCategoryId);
    }

    @Override
    @Log(value = "通用查询", type = LogType.SELECT)
    @RequestMapping(value = {"/queries"}, method = {RequestMethod.POST})
    public Object selectPagination(RestJsonWrapperBean wrapper) {
        return appMarketService.selectPagination(wrapper);
    }

    @Override
    @Log(value = "功能发布", type = LogType.INSERT)
    @RequestMapping(value = "/action/publish-func", method = RequestMethod.POST)
    public Object publishFunc(RestJsonWrapperBean wrapper) {
        return appMarketService.publishFunc(wrapper);
    }

    @Override
    @Log(value = "功能应用", type = LogType.INSERT)
    @RequestMapping(value = "/action/apply-func", method = RequestMethod.POST)
    public Object applyFunc(RestJsonWrapperBean wrapper) {
        return appMarketService.applyFunc(wrapper);
    }

    @Override
    @Log(value = "获取发布功能的版本", type = LogType.SELECT)
    @RequestMapping(value = "/func-version/queries", method = RequestMethod.POST)
    public Object getFuncVersion(RestJsonWrapperBean wrapper) {
        return appMarketService.getFuncVersion(wrapper);
    }

    @Override
    @Log(value = "功能删除", type = LogType.DELETE)
    @RequestMapping(value = "/action/delete-func", method = RequestMethod.POST)
    public Object deleteFunc(RestJsonWrapperBean wrapper) {
        return appMarketService.deleteFunc(wrapper);
    }

    @Override
    @Log(value = "获取应用超市项目配置", type = LogType.SELECT)
    @RequestMapping(value = "/func-projects/queries", method = RequestMethod.GET)
    public Object getFuncProject() {
        return appMarketService.getFuncProject();
    }

    @Override
    @Log(value = "获取发布功能的版本", type = LogType.SELECT)
    @RequestMapping(value = "/func-exist-tables/queries", method = RequestMethod.POST)
    public Object getExistFuncTableList(RestJsonWrapperBean wrapper) {
        return appMarketService.getExistFuncTableList(wrapper);
    }

    @Override
    @Log(value = "获取功能page资源列表", type = LogType.SELECT)
    @RequestMapping(value = "/func-resources/queries", method = RequestMethod.POST)
    public Object getFuncPageResourceList(RestJsonWrapperBean wrapper) {
        return appMarketService.getFuncPageResourceList(wrapper);
    }
}
