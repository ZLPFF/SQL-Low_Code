package com.sunwayworld.cloud.module.lcdp.table.resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableCompareDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;

@RequestMapping(LcdpPathConstant.VIEW_PATH)
public interface LcdpViewResource extends GenericCloudResource<LcdpViewBean, Long> {

    @RequestMapping(value = "/{viewName}/action/design", method = RequestMethod.POST)
    Long design(String viewName);

    @RequestMapping(value = "/{viewName}/action/checkout", method = RequestMethod.POST)
    void checkout(String viewName);

    @RequestMapping(value = "/action/compare", method = RequestMethod.POST)
    LcdpTableCompareDTO<LcdpViewBean> compare(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/{viewName}/info/physical", method = RequestMethod.GET)
    LcdpViewBean selectPhysicalViewInfo(String viewName);
}
