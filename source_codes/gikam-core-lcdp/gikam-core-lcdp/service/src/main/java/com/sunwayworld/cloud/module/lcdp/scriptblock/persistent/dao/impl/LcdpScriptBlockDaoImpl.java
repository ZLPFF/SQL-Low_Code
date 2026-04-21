package com.sunwayworld.cloud.module.lcdp.scriptblock.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.scriptblock.bean.LcdpScriptBlockBean;
import com.sunwayworld.cloud.module.lcdp.scriptblock.persistent.dao.LcdpScriptBlockDao;
import com.sunwayworld.cloud.module.lcdp.scriptblock.persistent.mapper.LcdpScriptBlockMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpScriptBlockDaoImpl extends MybatisDaoSupport<LcdpScriptBlockBean, Long> implements LcdpScriptBlockDao {

    @Autowired
    private LcdpScriptBlockMapper lcdpScriptBlockMapper;

    @Override
    public LcdpScriptBlockMapper getMapper() {
        return lcdpScriptBlockMapper;
    }

}
