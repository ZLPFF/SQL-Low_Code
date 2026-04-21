package com.sunwayworld.cloud.module.lcdp.moduletmpl.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.moduletmpl.persistent.dao.LcdpCustomTmplPageCompDao;
import com.sunwayworld.cloud.module.lcdp.moduletmpl.service.LcdpCustomTmplPageCompService;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpCustomTmplPageCompServiceImpl implements LcdpCustomTmplPageCompService {

    @Autowired
    private LcdpCustomTmplPageCompDao lcdpCustomTmplPageCompDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpCustomTmplPageCompDao getDao() {
        return lcdpCustomTmplPageCompDao;
    }
}
