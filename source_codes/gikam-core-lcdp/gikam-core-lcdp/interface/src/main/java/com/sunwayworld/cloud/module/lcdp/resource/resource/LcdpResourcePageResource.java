package com.sunwayworld.cloud.module.lcdp.resource.resource;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.base.LcdpResultDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpPageServiceMethodDTO;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldDTO;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

@RequestMapping(LcdpPathConstant.PAGE_PATH)
public interface LcdpResourcePageResource {
    @RequestMapping(value = "/services", method = RequestMethod.POST)
    List<String> selectServiceList();
    
    @RequestMapping(value = "/action/validate-sql", method = RequestMethod.POST)
    LcdpResultDTO validateSql(RestJsonWrapperBean wrapper);
    
    @RequestMapping(value = "/services/mapping-methods", method = RequestMethod.POST)
    List<LcdpPageServiceMethodDTO> selectServiceMappingMethodList(RestJsonWrapperBean wrapper);
    
    @RequestMapping(value = "/services/columns", method = RequestMethod.POST)
    List<LcdpTableFieldDTO> selectServiceColumnList(RestJsonWrapperBean wrapper);
}
