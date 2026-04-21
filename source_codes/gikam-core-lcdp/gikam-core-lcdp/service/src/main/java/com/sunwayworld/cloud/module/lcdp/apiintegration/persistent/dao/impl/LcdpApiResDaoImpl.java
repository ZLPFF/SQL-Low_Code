package com.sunwayworld.cloud.module.lcdp.apiintegration.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiResBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.persistent.dao.LcdpApiResDao;
import com.sunwayworld.cloud.module.lcdp.apiintegration.persistent.mapper.LcdpApiResMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpApiResDaoImpl extends MybatisDaoSupport<LcdpApiResBean, Long> implements LcdpApiResDao {

    @Autowired
    private LcdpApiResMapper lcdpApiResMapper;

    @Override
    public LcdpApiResMapper getMapper() {
        return lcdpApiResMapper;
    }

}
