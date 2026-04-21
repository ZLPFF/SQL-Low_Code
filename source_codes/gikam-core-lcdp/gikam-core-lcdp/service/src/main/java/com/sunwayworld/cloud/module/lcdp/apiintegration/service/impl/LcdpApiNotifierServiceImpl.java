package com.sunwayworld.cloud.module.lcdp.apiintegration.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiNotifierBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.persistent.dao.LcdpApiNotifierDao;
import com.sunwayworld.cloud.module.lcdp.apiintegration.service.LcdpApiNotifierService;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpApiNotifierServiceImpl implements LcdpApiNotifierService {

    @Autowired
    private LcdpApiNotifierDao lcdpApiNotifierDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpApiNotifierDao getDao() {
        return lcdpApiNotifierDao;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        List<LcdpApiNotifierBean> lcdpApiNotifierList = jsonWrapper.parse(LcdpApiNotifierBean.class);
        lcdpApiNotifierList.forEach(lcdpApiNotifier -> {
            lcdpApiNotifier.setId(ApplicationContextHelper.getNextIdentity());
        });
        getDao().insert(lcdpApiNotifierList);
        return lcdpApiNotifierList.get(0).getId();
    }

}
