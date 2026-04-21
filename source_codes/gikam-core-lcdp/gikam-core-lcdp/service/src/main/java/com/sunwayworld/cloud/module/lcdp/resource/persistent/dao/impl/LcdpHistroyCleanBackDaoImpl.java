package com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.impl;

import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.LcdpHistroyCleanBackDao;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.mapper.LcdpHistroyCleanBackMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpHistroyCleanBackBean;

import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpHistroyCleanBackDaoImpl extends MybatisDaoSupport<LcdpHistroyCleanBackBean, Long> implements LcdpHistroyCleanBackDao {

    @Autowired
    private LcdpHistroyCleanBackMapper lcdpHistroyCleanBackMapper;

    @Override
    public LcdpHistroyCleanBackMapper getMapper() {
        return lcdpHistroyCleanBackMapper;
    }

}
