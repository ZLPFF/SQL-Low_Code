package com.sunwayworld.cloud.module.lcdp.pagetmpl.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.pagetmpl.bean.LcdpPageTmplBean;

import com.sunwayworld.cloud.module.lcdp.pagetmpl.persistent.dao.LcdpPageTmplDao;
import com.sunwayworld.cloud.module.lcdp.pagetmpl.persistent.mapper.LcdpPageTmplMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpPageTmplDaoImpl extends MybatisDaoSupport<LcdpPageTmplBean, Long> implements LcdpPageTmplDao {

    @Autowired
    private LcdpPageTmplMapper lcdpPageTmplMapper;

    @Override
    public LcdpPageTmplMapper getMapper() {
        return lcdpPageTmplMapper;
    }

}
