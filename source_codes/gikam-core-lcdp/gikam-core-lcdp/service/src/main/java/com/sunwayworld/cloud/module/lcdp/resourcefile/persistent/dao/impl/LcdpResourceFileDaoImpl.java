package com.sunwayworld.cloud.module.lcdp.resourcefile.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.resourcefile.bean.LcdpResourceFileBean;
import com.sunwayworld.cloud.module.lcdp.resourcefile.persistent.dao.LcdpResourceFileDao;
import com.sunwayworld.cloud.module.lcdp.resourcefile.persistent.mapper.LcdpResourceFileMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpResourceFileDaoImpl extends MybatisDaoSupport<LcdpResourceFileBean, Long> implements LcdpResourceFileDao {

    @Autowired
    private LcdpResourceFileMapper lcdpResourceFileMapper;

    @Override
    public LcdpResourceFileMapper getMapper() {
        return lcdpResourceFileMapper;
    }

}
