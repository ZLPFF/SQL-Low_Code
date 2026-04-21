package com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpViewButtonRoleBean;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.LcdpViewButtonRoleDao;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.mapper.LcdpViewButtonRoleMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpViewButtonRoleDaoImpl extends MybatisDaoSupport<LcdpViewButtonRoleBean, Long> implements LcdpViewButtonRoleDao {

    @Autowired
    private LcdpViewButtonRoleMapper lcdpViewButtonRoleMapper;

    @Override
    public LcdpViewButtonRoleMapper getMapper() {
        return lcdpViewButtonRoleMapper;
    }

}
