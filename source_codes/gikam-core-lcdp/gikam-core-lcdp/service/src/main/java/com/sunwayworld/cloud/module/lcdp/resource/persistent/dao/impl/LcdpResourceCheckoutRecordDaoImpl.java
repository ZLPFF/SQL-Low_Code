package com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.impl;

import com.sunwayworld.cloud.module.lcdp.resource.bean.*;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.*;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.mapper.*;
import com.sunwayworld.framework.mybatis.dao.*;
import com.sunwayworld.framework.spring.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@GikamBean
public class LcdpResourceCheckoutRecordDaoImpl extends MybatisDaoSupport<LcdpResourceCheckoutRecordBean, Long> implements LcdpResourceCheckoutRecordDao {

    @Autowired
    private LcdpResourceCheckoutRecordMapper lcdpResourceCheckoutRecordMapper;

    @Override
    public LcdpResourceCheckoutRecordMapper getMapper() {
        return lcdpResourceCheckoutRecordMapper;
    }

}
