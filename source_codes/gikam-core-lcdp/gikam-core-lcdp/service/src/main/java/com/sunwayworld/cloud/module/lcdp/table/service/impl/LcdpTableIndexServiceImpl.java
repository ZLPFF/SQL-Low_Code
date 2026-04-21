package com.sunwayworld.cloud.module.lcdp.table.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.table.persistent.dao.LcdpTableIndexDao;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableIndexService;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpTableIndexServiceImpl implements LcdpTableIndexService {

    @Autowired
    private LcdpTableIndexDao lcdpTableIndexDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpTableIndexDao getDao() {
        return lcdpTableIndexDao;
    }

}
