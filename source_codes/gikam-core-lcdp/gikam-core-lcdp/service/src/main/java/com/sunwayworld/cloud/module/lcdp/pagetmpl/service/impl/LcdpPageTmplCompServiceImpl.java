package com.sunwayworld.cloud.module.lcdp.pagetmpl.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.cloud.module.lcdp.pagetmpl.bean.LcdpPageTmplCompBean;
import com.sunwayworld.cloud.module.lcdp.pagetmpl.persistent.dao.LcdpPageTmplCompDao;
import com.sunwayworld.cloud.module.lcdp.pagetmpl.service.LcdpPageTmplCompService;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpPageTmplCompServiceImpl implements LcdpPageTmplCompService {

    @Autowired
    private LcdpPageTmplCompDao lcdpPageTmplCompDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpPageTmplCompDao getDao() {
        return lcdpPageTmplCompDao;
    }

    @Override
    @Transactional
    public String insert(RestJsonWrapperBean jsonWrapper) {
        LcdpPageTmplCompBean lcdpPageTmplComp = jsonWrapper.parseUnique(LcdpPageTmplCompBean.class);
        lcdpPageTmplComp.setId(ApplicationContextHelper.getNextSequence(getDao().getTable()));
        getDao().insert(lcdpPageTmplComp);
        return lcdpPageTmplComp.getId();
    }

}
