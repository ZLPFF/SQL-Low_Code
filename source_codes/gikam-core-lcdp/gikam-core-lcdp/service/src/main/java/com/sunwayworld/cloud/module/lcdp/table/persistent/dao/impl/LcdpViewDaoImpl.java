package com.sunwayworld.cloud.module.lcdp.table.persistent.dao.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.cloud.module.lcdp.table.persistent.dao.LcdpViewDao;
import com.sunwayworld.cloud.module.lcdp.table.persistent.mapper.LcdpViewMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.PersistableHelper;

@Repository
@GikamBean
public class LcdpViewDaoImpl extends MybatisDaoSupport<LcdpViewBean, Long> implements LcdpViewDao {

    @Autowired
    private LcdpViewMapper lcdpViewMapper;

    @Override
    public LcdpViewMapper getMapper() {
        return lcdpViewMapper;
    }

    @Override
    public List<LcdpViewBean> selectLatestBriefList(MapperParameter parameter) {
        return getMapper().selectLatestBriefList(parameter).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpViewBean.class))
                .collect(Collectors.toList());
    }

}
