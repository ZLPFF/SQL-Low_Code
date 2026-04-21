package com.sunwayworld.cloud.module.lcdp.table.persistent.dao;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldGroupBean;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.support.base.dao.GenericDao;

public interface LcdpTableFieldGroupDao extends GenericDao<LcdpTableFieldGroupBean, Long> {
    List<LcdpTableFieldGroupBean> selectTreeNodeList(MapperParameter parameter);
}
