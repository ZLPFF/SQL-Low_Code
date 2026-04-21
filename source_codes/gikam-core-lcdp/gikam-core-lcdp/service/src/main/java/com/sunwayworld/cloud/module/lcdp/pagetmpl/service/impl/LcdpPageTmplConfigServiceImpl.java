package com.sunwayworld.cloud.module.lcdp.pagetmpl.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.cloud.module.lcdp.pagetmpl.bean.LcdpPageTmplConfigBean;
import com.sunwayworld.cloud.module.lcdp.pagetmpl.persistent.dao.LcdpPageTmplConfigDao;
import com.sunwayworld.cloud.module.lcdp.pagetmpl.service.LcdpPageTmplConfigService;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpPageTmplConfigServiceImpl implements LcdpPageTmplConfigService {

    @Autowired
    private LcdpPageTmplConfigDao lcdpPageTmplConfigDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpPageTmplConfigDao getDao() {
        return lcdpPageTmplConfigDao;
    }

    @Override
    @Transactional
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        LcdpPageTmplConfigBean lcdpPageTmplConfig = jsonWrapper.parseUnique(LcdpPageTmplConfigBean.class);
        lcdpPageTmplConfig.setId(ApplicationContextHelper.getNextIdentity());
        getDao().insert(lcdpPageTmplConfig);
        return lcdpPageTmplConfig.getId();
    }

}
