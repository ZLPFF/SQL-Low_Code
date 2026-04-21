package com.sunwayworld.cloud.module.lcdp.checkoutrecord.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCrdLogBean;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.persistent.dao.LcdpCrdLogDao;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.persistent.mapper.LcdpCrdLogMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpCrdLogDaoImpl extends MybatisDaoSupport<LcdpCrdLogBean, Long> implements LcdpCrdLogDao {

    @Autowired
    private LcdpCrdLogMapper lcdpCrdLogMapper;

    @Override
    public LcdpCrdLogMapper getMapper() {
        return lcdpCrdLogMapper;
    }

}
