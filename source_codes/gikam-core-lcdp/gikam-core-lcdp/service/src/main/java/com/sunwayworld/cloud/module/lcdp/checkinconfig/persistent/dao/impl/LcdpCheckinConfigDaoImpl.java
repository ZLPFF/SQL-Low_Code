package com.sunwayworld.cloud.module.lcdp.checkinconfig.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.checkinconfig.bean.LcdpCheckinConfigBean;
import com.sunwayworld.cloud.module.lcdp.checkinconfig.persistent.dao.LcdpCheckinConfigDao;
import com.sunwayworld.cloud.module.lcdp.checkinconfig.persistent.mapper.LcdpCheckinConfigMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpCheckinConfigDaoImpl extends MybatisDaoSupport<LcdpCheckinConfigBean, Long> implements LcdpCheckinConfigDao {

    @Autowired
    private LcdpCheckinConfigMapper lcdpCheckinConfigMapper;

    @Override
    public LcdpCheckinConfigMapper getMapper() {
        return lcdpCheckinConfigMapper;
    }

}
