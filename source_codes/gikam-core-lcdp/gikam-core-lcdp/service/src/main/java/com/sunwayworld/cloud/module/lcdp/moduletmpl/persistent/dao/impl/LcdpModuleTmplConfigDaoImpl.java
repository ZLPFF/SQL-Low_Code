package com.sunwayworld.cloud.module.lcdp.moduletmpl.persistent.dao.impl;

import com.sunwayworld.cloud.module.lcdp.moduletmpl.persistent.dao.LcdpModuleTmplConfigDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.moduletmpl.bean.LcdpModuleTmplConfigBean;
import com.sunwayworld.cloud.module.lcdp.moduletmpl.persistent.mapper.LcdpModuleTmplConfigMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpModuleTmplConfigDaoImpl extends MybatisDaoSupport<LcdpModuleTmplConfigBean, Long> implements LcdpModuleTmplConfigDao {

    @Autowired
    private LcdpModuleTmplConfigMapper lcdpModuleTmplConfigMapper;

    @Override
    public LcdpModuleTmplConfigMapper getMapper() {
        return lcdpModuleTmplConfigMapper;
    }

}
