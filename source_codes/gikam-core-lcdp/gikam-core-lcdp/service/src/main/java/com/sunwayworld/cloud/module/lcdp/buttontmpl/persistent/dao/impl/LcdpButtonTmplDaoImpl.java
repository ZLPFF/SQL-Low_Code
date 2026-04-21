package com.sunwayworld.cloud.module.lcdp.buttontmpl.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.buttontmpl.bean.LcdpButtonTmplBean;
import com.sunwayworld.cloud.module.lcdp.buttontmpl.persistent.dao.LcdpButtonTmplDao;
import com.sunwayworld.cloud.module.lcdp.buttontmpl.persistent.mapper.LcdpButtonTmplMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpButtonTmplDaoImpl extends MybatisDaoSupport<LcdpButtonTmplBean, Long> implements LcdpButtonTmplDao {

    @Autowired
    private LcdpButtonTmplMapper LcdpButtonTmplMapper;

    @Override
    public LcdpButtonTmplMapper getMapper() {
        return LcdpButtonTmplMapper;
    }

}
