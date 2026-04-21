package com.sunwayworld.cloud.module.lcdp.resource.persistent.mapper;

import java.util.List;
import java.util.Map;

import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.mapper.GenericMapper;

@GikamBean
public interface LcdpModulePageI18nMapper extends GenericMapper<Long> {

    List<Map<String, Object>> selectAllI18nMessage(MapperParameter parameter);

    List<Map<String, Object>> selectEffectiveByCondition(MapperParameter parameter);
}
