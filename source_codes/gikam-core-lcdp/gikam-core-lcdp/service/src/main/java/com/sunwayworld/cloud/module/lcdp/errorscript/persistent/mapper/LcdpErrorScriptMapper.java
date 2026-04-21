package com.sunwayworld.cloud.module.lcdp.errorscript.persistent.mapper;

import java.util.List;
import java.util.Map;

import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.mapper.GenericMapper;

@GikamBean
public interface LcdpErrorScriptMapper extends GenericMapper<Long> {
    List<Map<String, Object>> selectWarningListByCondition(MapperParameter parameter);
    
    List<Long> selectAbnormalIdList();
}
