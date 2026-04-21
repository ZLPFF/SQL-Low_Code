package com.sunwayworld.cloud.module.lcdp.resource.service.impl;

import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.LcdpHistroyCleanBackDao;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.framework.spring.annotation.GikamBean;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpHistroyCleanBackBean;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpHistroyCleanBackService;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

@Repository
@GikamBean
public class LcdpHistroyCleanBackServiceImpl implements LcdpHistroyCleanBackService {

    @Autowired
    private LcdpHistroyCleanBackDao lcdpHistroyCleanBackDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpHistroyCleanBackDao getDao() {
        return lcdpHistroyCleanBackDao;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        LcdpHistroyCleanBackBean lcdpHistroyCleanBack = jsonWrapper.parseUnique(LcdpHistroyCleanBackBean.class);
        lcdpHistroyCleanBack.setId(ApplicationContextHelper.getNextIdentity());
        getDao().insert(lcdpHistroyCleanBack);
        return lcdpHistroyCleanBack.getId();
    }

}
