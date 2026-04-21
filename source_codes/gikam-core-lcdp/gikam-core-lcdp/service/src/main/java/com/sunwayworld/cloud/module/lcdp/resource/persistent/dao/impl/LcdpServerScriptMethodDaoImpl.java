package com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpServerScriptMethodBean;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.LcdpServerScriptMethodDao;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.mapper.LcdpServerScriptMethodMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpServerScriptMethodDaoImpl extends MybatisDaoSupport<LcdpServerScriptMethodBean, Long> implements LcdpServerScriptMethodDao {

    @Autowired
    private LcdpServerScriptMethodMapper lcdpServerScriptMethodMapper;

    @Override
    public LcdpServerScriptMethodMapper getMapper() {
        return lcdpServerScriptMethodMapper;
    }

}
