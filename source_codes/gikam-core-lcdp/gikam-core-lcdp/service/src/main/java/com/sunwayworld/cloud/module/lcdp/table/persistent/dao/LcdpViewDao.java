package com.sunwayworld.cloud.module.lcdp.table.persistent.dao;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.support.base.dao.GenericDao;

public interface LcdpViewDao extends GenericDao<LcdpViewBean, Long> {
    List<LcdpViewBean> selectLatestBriefList(MapperParameter parameter);
}
