package com.sunwayworld.cloud.module.lcdp.table.persistent.dao;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.support.base.dao.GenericDao;

public interface LcdpTableDao extends GenericDao<LcdpTableBean, Long> {
    List<LcdpTableBean> selectLatestBriefList(MapperParameter parameter);
}
