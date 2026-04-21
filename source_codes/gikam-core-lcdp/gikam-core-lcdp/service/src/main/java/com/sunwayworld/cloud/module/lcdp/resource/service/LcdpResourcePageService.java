package com.sunwayworld.cloud.module.lcdp.resource.service;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.base.LcdpResultDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModulePageCompBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpPageServiceMethodDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldDTO;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

/**
 * 低代码页面配置相关的方法
 *
 * @author zhangjr@sunwayworld.com 2024年9月14日
 */
public interface LcdpResourcePageService {
    List<String> selectServiceList();
    
    LcdpResultDTO validateSql(RestJsonWrapperBean wrapper);
    
    List<LcdpPageServiceMethodDTO> selectServiceMappingMethodList(RestJsonWrapperBean wrapper);
    
    List<LcdpTableFieldDTO> selectServiceColumnList(RestJsonWrapperBean wrapper);
    
    List<LcdpResourceBean> postUpdate(LcdpResourceHistoryBean resourcePageHistory, List<LcdpModulePageCompBean> insertPageCompList);
}
