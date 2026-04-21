package com.sunwayworld.cloud.module.lcdp.apiintegration.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiNotifierBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.persistent.dao.LcdpApiNotifierDao;
import com.sunwayworld.cloud.module.lcdp.apiintegration.persistent.mapper.LcdpApiNotifierMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpApiNotifierDaoImpl extends MybatisDaoSupport<LcdpApiNotifierBean, Long> implements LcdpApiNotifierDao {

    @Autowired
    private LcdpApiNotifierMapper lcdpApiNotifierMapper;

    @Override
    public LcdpApiNotifierMapper getMapper() {
        return lcdpApiNotifierMapper;
    }

}
