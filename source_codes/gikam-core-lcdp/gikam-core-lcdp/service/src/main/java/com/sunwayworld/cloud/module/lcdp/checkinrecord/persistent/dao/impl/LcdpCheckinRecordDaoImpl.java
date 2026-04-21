package com.sunwayworld.cloud.module.lcdp.checkinrecord.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.checkinrecord.bean.LcdpCheckinRecordBean;
import com.sunwayworld.cloud.module.lcdp.checkinrecord.persistent.dao.LcdpCheckinRecordDao;
import com.sunwayworld.cloud.module.lcdp.checkinrecord.persistent.mapper.LcdpCheckinRecordMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpCheckinRecordDaoImpl extends MybatisDaoSupport<LcdpCheckinRecordBean, Long> implements LcdpCheckinRecordDao {

    @Autowired
    private LcdpCheckinRecordMapper lcdpCheckinRecordMapper;

    @Override
    public LcdpCheckinRecordMapper getMapper() {
        return lcdpCheckinRecordMapper;
    }

}
