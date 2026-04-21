package com.sunwayworld.cloud.module.lcdp.moduletmpl.persistent.dao.impl;

import com.sunwayworld.cloud.module.lcdp.moduletmpl.bean.LcdpModuleTmplResourceBean;
import com.sunwayworld.cloud.module.lcdp.moduletmpl.persistent.dao.LcdpModuleTmplResourceDao;
import com.sunwayworld.cloud.module.lcdp.moduletmpl.persistent.mapper.LcdpModuleTmplResourceMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@GikamBean
public class LcdpModuleTmplResourceDaoImpl extends MybatisDaoSupport<LcdpModuleTmplResourceBean, Long> implements LcdpModuleTmplResourceDao {

    @Autowired
    private LcdpModuleTmplResourceMapper lcdpModuleTmplResourceMapper;

    @Override
    public LcdpModuleTmplResourceMapper getMapper() {
        return lcdpModuleTmplResourceMapper;
    }

}
