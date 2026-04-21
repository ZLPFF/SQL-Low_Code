package com.sunwayworld.cloud.module.lcdp.resourceversion.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.resourceversion.bean.LcdpResourceVersionBean;
import com.sunwayworld.cloud.module.lcdp.resourceversion.persistent.dao.LcdpResourceVersionDao;
import com.sunwayworld.cloud.module.lcdp.resourceversion.persistent.mapper.LcdpResourceVersionMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpResourceVersionDaoImpl extends MybatisDaoSupport<LcdpResourceVersionBean, Long> implements LcdpResourceVersionDao {

    @Autowired
    private LcdpResourceVersionMapper lcdpResourceVersionMapper;

    @Override
    public LcdpResourceVersionMapper getMapper() {
        return lcdpResourceVersionMapper;
    }

}
