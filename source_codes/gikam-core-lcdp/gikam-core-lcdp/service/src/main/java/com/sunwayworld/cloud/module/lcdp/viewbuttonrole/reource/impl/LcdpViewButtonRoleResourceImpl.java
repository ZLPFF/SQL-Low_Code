package com.sunwayworld.cloud.module.lcdp.viewbuttonrole.reource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpViewButtonRoleBean;
import com.sunwayworld.cloud.module.lcdp.viewbuttonrole.resource.LcdpViewButtonRoleResource;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpViewButtonRoleService;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;
import com.sunwayworld.framework.support.choosable.resource.AbstractGenericChoosableResource;

@LogModule("页面按钮权限")
@RestController
@GikamBean
public class LcdpViewButtonRoleResourceImpl implements LcdpViewButtonRoleResource, AbstractGenericResource<LcdpViewButtonRoleService, LcdpViewButtonRoleBean, Long>, AbstractGenericChoosableResource<LcdpViewButtonRoleService, LcdpViewButtonRoleBean, Long> {

    @Autowired
    private LcdpViewButtonRoleService lcdpViewButtonRoleService;

    @Override
    public LcdpViewButtonRoleService getService() {
        return lcdpViewButtonRoleService;
    }

    @Log(value = "新增页面按钮权限", type = LogType.INSERT)
    @Override
    @RequestMapping(method = RequestMethod.POST)
    public Long insert(RestJsonWrapperBean wrapper) {
        return getService().insert(wrapper);
    }

    @Override
    @Log(value = "查询按钮", type = LogType.SELECT)
    public Page<LcdpViewButtonRoleBean> selectPaginationByViewInfo(RestJsonWrapperBean wrapper) {
        return getService().selectPaginationByViewInfo(wrapper);
    }

    @Override
    public Page<LcdpViewButtonRoleBean> selectDistinctRawPagination(RestJsonWrapperBean wrapper) {
        return getService().selectDistinctRawPagination(wrapper);
    }
}
