package com.sunwayworld.cloud.module.lcdp.checkoutrecord.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCrdLogBean;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.persistent.dao.LcdpCrdLogDao;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.service.LcdpCrdLogService;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpCrdLogServiceImpl implements LcdpCrdLogService {

    @Autowired
    private LcdpCrdLogDao lcdpCrdLogDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpCrdLogDao getDao() {
        return lcdpCrdLogDao;
    }

    @Override
    @Transactional
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        LcdpCrdLogBean lcdpCrdLog = jsonWrapper.parseUnique(LcdpCrdLogBean.class);
        lcdpCrdLog.setId(ApplicationContextHelper.getNextIdentity());
        getDao().insert(lcdpCrdLog);
        return lcdpCrdLog.getId();
    }

}
