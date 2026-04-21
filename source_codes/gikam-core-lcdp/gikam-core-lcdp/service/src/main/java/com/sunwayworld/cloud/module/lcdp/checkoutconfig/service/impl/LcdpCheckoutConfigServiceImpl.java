package com.sunwayworld.cloud.module.lcdp.checkoutconfig.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.framework.at.annotation.AuditTrailType;
import com.sunwayworld.framework.at.annotation.AuditTrailEntry;
import com.sunwayworld.framework.spring.annotation.GikamBean;

import com.sunwayworld.cloud.module.lcdp.checkoutconfig.persistent.dao.LcdpCheckoutConfigDao;
import com.sunwayworld.cloud.module.lcdp.checkoutconfig.bean.LcdpCheckoutConfigBean;
import com.sunwayworld.cloud.module.lcdp.checkoutconfig.service.LcdpCheckoutConfigService;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

@Repository
@GikamBean
public class LcdpCheckoutConfigServiceImpl implements LcdpCheckoutConfigService {

    @Autowired
    private LcdpCheckoutConfigDao lcdpCheckoutConfigDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpCheckoutConfigDao getDao() {
        return lcdpCheckoutConfigDao;
    }

    @Override
    @Transactional
    @AuditTrailEntry(AuditTrailType.INSERT)
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        LcdpCheckoutConfigBean checkoutConfig = jsonWrapper.parseUnique(LcdpCheckoutConfigBean.class);
        checkoutConfig.setId(ApplicationContextHelper.getNextIdentity());

        getDao().insert(checkoutConfig);
        return checkoutConfig.getId();
    }

}
