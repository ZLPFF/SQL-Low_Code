package com.sunwayworld.cloud.module.lcdp.submitlog.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.submitlog.bean.LcdpSubmitLogBean;
import com.sunwayworld.cloud.module.lcdp.submitlog.persistent.dao.LcdpSubmitLogDao;
import com.sunwayworld.cloud.module.lcdp.submitlog.persistent.mapper.LcdpSubmitLogMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpSubmitLogDaoImpl extends MybatisDaoSupport<LcdpSubmitLogBean, Long> implements LcdpSubmitLogDao {

    @Autowired
    private LcdpSubmitLogMapper lcdpSubmitLogMapper;

    @Override
    public LcdpSubmitLogMapper getMapper() {
        return lcdpSubmitLogMapper;
    }

}
