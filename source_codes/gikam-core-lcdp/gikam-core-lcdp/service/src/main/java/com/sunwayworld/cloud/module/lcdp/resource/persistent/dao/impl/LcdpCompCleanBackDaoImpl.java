package com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.impl;

import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.LcdpCompCleanBackDao;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.mapper.LcdpCompCleanBackMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpCompCleanBackBean;

import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpCompCleanBackDaoImpl extends MybatisDaoSupport<LcdpCompCleanBackBean, String> implements LcdpCompCleanBackDao {

    @Autowired
    private LcdpCompCleanBackMapper lcdpCompCleanBackMapper;

    @Override
    public LcdpCompCleanBackMapper getMapper() {
        return lcdpCompCleanBackMapper;
    }

}
