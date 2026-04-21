package com.sunwayworld.cloud.module.lcdp.table.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.table.persistent.dao.LcdpTableFieldDao;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableFieldService;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpTableFieldServiceImpl implements LcdpTableFieldService {

    @Autowired
    private LcdpTableFieldDao lcdpTableFieldDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpTableFieldDao getDao() {
        return lcdpTableFieldDao;
    }

}
