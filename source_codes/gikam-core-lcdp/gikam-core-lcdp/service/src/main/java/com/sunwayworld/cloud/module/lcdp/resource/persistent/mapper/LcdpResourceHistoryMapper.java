package com.sunwayworld.cloud.module.lcdp.resource.persistent.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryDevParamDTO;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.mapper.GenericMapper;

@GikamBean
public interface LcdpResourceHistoryMapper extends GenericMapper<Long> {
    List<String> selectDevResourceNameList(@Param("userId") String userId, @Param("devParamList") List<LcdpResourceHistoryDevParamDTO> devParamList);
    
    List<Map<String, Object>> selectLatestActivatedListByResourceIdList(@Param("resourceIdList") List<Long> resourceIdList);
    
    List<Map<String, Object>> selectMaxVersionListByResourceIdList(@Param("resourceIdList") List<Long> resourceIdList);
}
