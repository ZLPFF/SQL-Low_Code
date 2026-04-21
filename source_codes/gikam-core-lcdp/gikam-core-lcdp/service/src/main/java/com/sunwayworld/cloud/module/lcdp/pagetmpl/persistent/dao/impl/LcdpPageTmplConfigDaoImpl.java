package com.sunwayworld.cloud.module.lcdp.pagetmpl.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.pagetmpl.bean.LcdpPageTmplConfigBean;

import com.sunwayworld.cloud.module.lcdp.pagetmpl.persistent.dao.LcdpPageTmplConfigDao;
import com.sunwayworld.cloud.module.lcdp.pagetmpl.persistent.mapper.LcdpPageTmplConfigMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpPageTmplConfigDaoImpl extends MybatisDaoSupport<LcdpPageTmplConfigBean, Long> implements LcdpPageTmplConfigDao {

    @Autowired
    private LcdpPageTmplConfigMapper lcdpPageTmplConfigMapper;

    @Override
    public LcdpPageTmplConfigMapper getMapper() {
        return lcdpPageTmplConfigMapper;
    }

}
