package com.sunwayworld.cloud.module.lcdp.resource.resource;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceSearchDTO;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

@RequestMapping(LcdpPathConstant.RESOURCE_SEARCH_PATH)
public interface LcdpResourceSearchResource {
    @RequestMapping(value = "/queries", method = RequestMethod.POST)
    Page<LcdpResourceSearchDTO> selectPagination(RestJsonWrapperBean jsonWrapper);
    
    @RequestMapping(value = "/resources/{id}/contents", method = RequestMethod.GET)
    String selectContent(@PathVariable String id);
}
