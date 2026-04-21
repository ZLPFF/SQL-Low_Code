package com.sunwayworld.cloud.module.lcdp.resourceversion.resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.resourceversion.bean.LcdpResourceVersionBean;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;


@RequestMapping(LcdpPathConstant.RESOURCE_VERSION_PATH)
public interface LcdpResourceVersionResource extends GenericCloudResource<LcdpResourceVersionBean, Long>{
    @RequestMapping(value = {"/action/deal-history-data"}, method = {RequestMethod.PUT})
    void dealHistoryData(RestJsonWrapperBean wrapper);
}
