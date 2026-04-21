package com.sunwayworld.cloud.module.lcdp.pagetmpl.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.pagetmpl.bean.LcdpPageTmplCompBean;

import com.sunwayworld.cloud.module.lcdp.pagetmpl.persistent.dao.LcdpPageTmplCompDao;
import com.sunwayworld.cloud.module.lcdp.pagetmpl.persistent.mapper.LcdpPageTmplCompMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpPageTmplCompDaoImpl extends MybatisDaoSupport<LcdpPageTmplCompBean, String> implements LcdpPageTmplCompDao {

    @Autowired
    private LcdpPageTmplCompMapper lcdpPageTmplCompMapper;

    @Override
    public LcdpPageTmplCompMapper getMapper() {
        return lcdpPageTmplCompMapper;
    }

}
