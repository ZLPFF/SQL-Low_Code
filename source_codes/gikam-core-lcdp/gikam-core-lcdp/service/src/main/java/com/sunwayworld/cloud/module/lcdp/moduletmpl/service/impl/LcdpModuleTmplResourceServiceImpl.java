package com.sunwayworld.cloud.module.lcdp.moduletmpl.service.impl;

import com.sunwayworld.cloud.module.lcdp.moduletmpl.persistent.dao.LcdpModuleTmplResourceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.framework.at.annotation.AuditTrailType;
import com.sunwayworld.framework.at.annotation.AuditTrailEntry;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.spring.annotation.GikamBean;

import com.sunwayworld.cloud.module.lcdp.moduletmpl.bean.LcdpModuleTmplResourceBean;
import com.sunwayworld.cloud.module.lcdp.moduletmpl.service.LcdpModuleTmplResourceService;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

@Repository
@GikamBean
public class LcdpModuleTmplResourceServiceImpl implements LcdpModuleTmplResourceService {

    @Autowired
    private LcdpModuleTmplResourceDao lcdpModuleTmplResourceDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpModuleTmplResourceDao getDao() {
        return lcdpModuleTmplResourceDao;
    }

    @Override
    @Transactional
    @AuditTrailEntry(AuditTrailType.INSERT)
    @Audit(AuditConstant.INSERT)
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        LcdpModuleTmplResourceBean lcdpModuleTmplResource = jsonWrapper.parseUnique(LcdpModuleTmplResourceBean.class);
        lcdpModuleTmplResource.setId(ApplicationContextHelper.getNextIdentity());
        getDao().insert(lcdpModuleTmplResource);
        return lcdpModuleTmplResource.getId();
    }

}
