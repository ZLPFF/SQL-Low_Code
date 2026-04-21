package com.sunwayworld.cloud.module.lcdp.viewbuttonrole.resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpViewButtonRoleBean;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;
import com.sunwayworld.framework.support.choosable.resource.GenericChoosableCloudResource;

@RequestMapping(LcdpPathConstant.VIEW_BUTTON_ROLES)
public interface LcdpViewButtonRoleResource extends GenericCloudResource<LcdpViewButtonRoleBean, Long>, GenericChoosableCloudResource<LcdpViewButtonRoleBean, Long> {
    @RequestMapping(value = "/btn/action/queries", method = RequestMethod.POST)
    Page<LcdpViewButtonRoleBean> selectPaginationByViewInfo(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/queries/distinct/raw", method = RequestMethod.POST)
    Page<LcdpViewButtonRoleBean> selectDistinctRawPagination(RestJsonWrapperBean wrapper);

}
