package com.sunwayworld.cloud.module.lcdp.resource.service.impl;

import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.LcdpCompCleanBackDao;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.framework.spring.annotation.GikamBean;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpCompCleanBackBean;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpCompCleanBackService;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

@Repository
@GikamBean
public class LcdpCompCleanBackServiceImpl implements LcdpCompCleanBackService {

    @Autowired
    private LcdpCompCleanBackDao lcdpCompCleanBackDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpCompCleanBackDao getDao() {
        return lcdpCompCleanBackDao;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public String insert(RestJsonWrapperBean jsonWrapper) {
        LcdpCompCleanBackBean lcdpCompCleanBack = jsonWrapper.parseUnique(LcdpCompCleanBackBean.class);
        lcdpCompCleanBack.setId(ApplicationContextHelper.getNextSequence(getDao().getTable()));
        getDao().insert(lcdpCompCleanBack);
        return lcdpCompCleanBack.getId();
    }

}
