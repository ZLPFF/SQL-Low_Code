package com.sunwayworld.cloud.module.lcdp.resource.persistent.dao;

import java.util.List;
import java.util.Map;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModulePageI18nBean;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.support.base.dao.GenericDao;

public interface LcdpModulePageI18nDao extends GenericDao<LcdpModulePageI18nBean, Long> {

    List<Map<String, Object>> selectAllI18nMessage(MapperParameter parameter);

    List<Map<String, Object>> selectEffectiveByCondition(MapperParameter parameter);
}
