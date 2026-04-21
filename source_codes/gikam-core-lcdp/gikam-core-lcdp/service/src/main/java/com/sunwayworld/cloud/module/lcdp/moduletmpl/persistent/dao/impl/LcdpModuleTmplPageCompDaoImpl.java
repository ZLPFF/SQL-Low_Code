package com.sunwayworld.cloud.module.lcdp.moduletmpl.persistent.dao.impl;

import com.sunwayworld.cloud.module.lcdp.moduletmpl.persistent.dao.LcdpModuleTmplPageCompDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.moduletmpl.bean.LcdpModuleTmplPageCompBean;
import com.sunwayworld.cloud.module.lcdp.moduletmpl.persistent.mapper.LcdpModuleTmplPageCompMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpModuleTmplPageCompDaoImpl extends MybatisDaoSupport<LcdpModuleTmplPageCompBean, String> implements LcdpModuleTmplPageCompDao {

    @Autowired
    private LcdpModuleTmplPageCompMapper lcdpModuleTmplPageCompMapper;

    @Override
    public LcdpModuleTmplPageCompMapper getMapper() {
        return lcdpModuleTmplPageCompMapper;
    }

}
