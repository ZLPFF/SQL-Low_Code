package com.sunwayworld.cloud.module.lcdp.importrecord.persistent.dao.impl;

import com.sunwayworld.cloud.module.lcdp.importrecord.persistent.dao.LcdpRImportRecordDetailDao;
import com.sunwayworld.cloud.module.lcdp.importrecord.persistent.mapper.LcdpRImportRecordDetailMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.importrecord.bean.LcdpRImportRecordDetailBean;

import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpRImportRecordDetailDaoImpl extends MybatisDaoSupport<LcdpRImportRecordDetailBean, Long> implements LcdpRImportRecordDetailDao {

    @Autowired
    private LcdpRImportRecordDetailMapper lcdpRImportRecordDetailMapper;

    @Override
    public LcdpRImportRecordDetailMapper getMapper() {
        return lcdpRImportRecordDetailMapper;
    }

}
