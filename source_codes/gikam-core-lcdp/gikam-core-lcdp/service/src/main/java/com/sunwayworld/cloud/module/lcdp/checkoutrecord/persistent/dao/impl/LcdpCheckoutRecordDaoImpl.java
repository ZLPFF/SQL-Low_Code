package com.sunwayworld.cloud.module.lcdp.checkoutrecord.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCheckoutRecordBean;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.persistent.dao.LcdpCheckoutRecordDao;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.persistent.mapper.LcdpCheckoutRecordMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpCheckoutRecordDaoImpl extends MybatisDaoSupport<LcdpCheckoutRecordBean, Long> implements LcdpCheckoutRecordDao {

    @Autowired
    private LcdpCheckoutRecordMapper lcdpCheckoutRecordMapper;

    @Override
    public LcdpCheckoutRecordMapper getMapper() {
        return lcdpCheckoutRecordMapper;
    }

}
