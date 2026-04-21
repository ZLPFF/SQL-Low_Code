package com.sunwayworld.cloud.module.lcdp.submitlog.resource;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.resourceversion.bean.LcdpResourceVersionBean;
import com.sunwayworld.cloud.module.lcdp.submitlog.bean.LcdpSubmitLogBean;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;

@RequestMapping(LcdpPathConstant.SUBMIT_LOG_PATH)
public interface LcdpSubmitLogResource extends GenericCloudResource<LcdpSubmitLogBean, Long> {
    @RequestMapping(value = "/{id}/versions/queries", method = RequestMethod.POST)
    Page<LcdpResourceVersionBean> selectVersionPaginationByLogId(@PathVariable Long id, RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/resources/action/view"}, method = {RequestMethod.POST})
    Page<LcdpResourceVersionBean> viewResource(RestJsonWrapperBean wrapper);



}
