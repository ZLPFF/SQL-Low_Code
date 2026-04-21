package com.sunwayworld.cloud.module.lcdp.checkoutconfig.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.checkoutconfig.bean.LcdpCheckoutConfigBean;
import com.sunwayworld.cloud.module.lcdp.checkoutconfig.persistent.dao.LcdpCheckoutConfigDao;
import com.sunwayworld.cloud.module.lcdp.checkoutconfig.persistent.mapper.LcdpCheckoutConfigMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpCheckoutConfigDaoImpl extends MybatisDaoSupport<LcdpCheckoutConfigBean, Long> implements LcdpCheckoutConfigDao {

    @Autowired
    private LcdpCheckoutConfigMapper lcdpCheckoutConfigMapper;

    @Override
    public LcdpCheckoutConfigMapper getMapper() {
        return lcdpCheckoutConfigMapper;
    }

}
