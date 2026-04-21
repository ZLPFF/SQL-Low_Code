package com.sunwayworld.cloud.module.lcdp.moduletmpl.service.impl;

import com.sunwayworld.cloud.module.lcdp.moduletmpl.persistent.dao.LcdpModuleTmplPageCompDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.framework.at.annotation.AuditTrailType;
import com.sunwayworld.framework.at.annotation.AuditTrailEntry;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.spring.annotation.GikamBean;

import com.sunwayworld.cloud.module.lcdp.moduletmpl.bean.LcdpModuleTmplPageCompBean;
import com.sunwayworld.cloud.module.lcdp.moduletmpl.service.LcdpModuleTmplPageCompService;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

@Repository
@GikamBean
public class LcdpModuleTmplPageCompServiceImpl implements LcdpModuleTmplPageCompService {

    @Autowired
    private LcdpModuleTmplPageCompDao lcdpModuleTmplPageCompDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpModuleTmplPageCompDao getDao() {
        return lcdpModuleTmplPageCompDao;
    }

    @Override
    @Transactional
    @AuditTrailEntry(AuditTrailType.INSERT)
    @Audit(AuditConstant.INSERT)
    public String insert(RestJsonWrapperBean jsonWrapper) {
        LcdpModuleTmplPageCompBean lcdpModuleTmplPageComp = jsonWrapper.parseUnique(LcdpModuleTmplPageCompBean.class);
        lcdpModuleTmplPageComp.setId(ApplicationContextHelper.getNextSequence(getDao().getTable()));
        getDao().insert(lcdpModuleTmplPageComp);
        return lcdpModuleTmplPageComp.getId();
    }

}
