package com.sunwayworld.cloud.module.lcdp.resource.service;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceSearchDTO;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

/**
 * 资源建设
 */
public interface LcdpResourceSearchService {
    Page<LcdpResourceSearchDTO> selectPagination(RestJsonWrapperBean wrapper);
    
    String selectContent(String id);
}
