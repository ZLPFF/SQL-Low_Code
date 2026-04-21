package com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.impl;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceConvertRecordBean;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.LcdpResourceConvertRecordDao;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.mapper.LcdpResourceConvertRecordMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@GikamBean
public class LcdpResourceConvertRecordDaoImpl extends MybatisDaoSupport<LcdpResourceConvertRecordBean, Long> implements LcdpResourceConvertRecordDao {

    @Autowired
    private LcdpResourceConvertRecordMapper lcdpResourceConvertRecordMapper;

    @Override
    public LcdpResourceConvertRecordMapper getMapper() {
        return lcdpResourceConvertRecordMapper;
    }
}
