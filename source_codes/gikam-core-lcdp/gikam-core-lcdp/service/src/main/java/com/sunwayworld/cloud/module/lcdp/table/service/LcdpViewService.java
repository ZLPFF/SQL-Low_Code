package com.sunwayworld.cloud.module.lcdp.table.service;

import java.util.List;
import java.util.Map;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpAnalysisResultDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableCompareDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpViewService extends GenericService<LcdpViewBean, Long> {

    List<LcdpViewBean> selectPhysicalViewInfoList(RestJsonWrapperBean jsonWrapper);

    Long design(String viewName);

    void checkout(String viewName);

    void submit(List<LcdpViewBean> viewList);

    List<String> revert(List<String> viewName);

    LcdpTableCompareDTO<LcdpViewBean> compare(RestJsonWrapperBean wrapper);

    Map<String, LcdpAnalysisResultDTO> analysisViewInfo(List<String> viewNameList, Map<String, String> fileMap);

    LcdpViewBean selectPhysicalViewInfo(String viewName);
    
    /**
     * 根据条件查询最新的视图概要信息
     */
    List<LcdpViewBean> selectLatestBriefList(MapperParameter parameter);
}
