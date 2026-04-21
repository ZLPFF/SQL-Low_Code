package com.sunwayworld.cloud.module.lcdp.apiintegration.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiResBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.persistent.dao.LcdpApiResDao;
import com.sunwayworld.cloud.module.lcdp.apiintegration.service.LcdpApiResService;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpApiResServiceImpl implements LcdpApiResService {

    @Autowired
    private LcdpApiResDao lcdpApiResDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpApiResDao getDao() {
        return lcdpApiResDao;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        LcdpApiResBean lcdpApiRes = jsonWrapper.parseUnique(LcdpApiResBean.class);
        lcdpApiRes.setId(ApplicationContextHelper.getNextIdentity());
        getDao().insert(lcdpApiRes);
        return lcdpApiRes.getId();
    }

}
