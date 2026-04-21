package com.sunwayworld.cloud.module.lcdp.importrecord.persistent.dao.impl;

import com.sunwayworld.cloud.module.lcdp.importrecord.bean.LcdpResourceImportRecordBean;
import com.sunwayworld.cloud.module.lcdp.importrecord.persistent.dao.LcdpResourceImportRecordDao;
import com.sunwayworld.cloud.module.lcdp.importrecord.persistent.mapper.LcdpResourceImportRecordMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@GikamBean
public class LcdpResourceImportRecordDaoImpl extends MybatisDaoSupport<LcdpResourceImportRecordBean, Long> implements LcdpResourceImportRecordDao {

    @Autowired
    private LcdpResourceImportRecordMapper lcdpResourceImportRecordMapper;

    @Override
    public LcdpResourceImportRecordMapper getMapper() {
        return lcdpResourceImportRecordMapper;
    }

}
