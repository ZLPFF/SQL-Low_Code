package com.sunwayworld.cloud.module.lcdp.apiintegration.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiFieldBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.persistent.dao.LcdpApiFieldDao;
import com.sunwayworld.cloud.module.lcdp.apiintegration.service.LcdpApiFieldService;
import com.sunwayworld.framework.at.annotation.AuditTrailEntry;
import com.sunwayworld.framework.at.annotation.AuditTrailType;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpApiFieldServiceImpl implements LcdpApiFieldService {

    @Autowired
    private LcdpApiFieldDao lcdpApiFieldDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpApiFieldDao getDao() {
        return lcdpApiFieldDao;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        LcdpApiFieldBean lcdpApiField = jsonWrapper.parseUnique(LcdpApiFieldBean.class);
        lcdpApiField.setId(ApplicationContextHelper.getNextIdentity());
        getDao().insert(lcdpApiField);
        return lcdpApiField.getId();
    }

}
