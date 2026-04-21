package com.sunwayworld.cloud.module.lcdp.moduletmpl.persistent.dao.impl;

import com.sunwayworld.cloud.module.lcdp.moduletmpl.persistent.dao.LcdpModuleTmplDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.moduletmpl.bean.LcdpModuleTmplBean;
import com.sunwayworld.cloud.module.lcdp.moduletmpl.persistent.mapper.LcdpModuleTmplMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpModuleTmplDaoImpl extends MybatisDaoSupport<LcdpModuleTmplBean, Long> implements LcdpModuleTmplDao {

    @Autowired
    private LcdpModuleTmplMapper lcdpModuleTmplMapper;

    @Override
    public LcdpModuleTmplMapper getMapper() {
        return lcdpModuleTmplMapper;
    }

}
