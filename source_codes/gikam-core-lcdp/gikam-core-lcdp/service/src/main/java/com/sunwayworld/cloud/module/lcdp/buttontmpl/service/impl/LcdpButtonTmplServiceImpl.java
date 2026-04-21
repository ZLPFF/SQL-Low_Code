package com.sunwayworld.cloud.module.lcdp.buttontmpl.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.cloud.module.lcdp.buttontmpl.bean.LcdpButtonTmplBean;
import com.sunwayworld.cloud.module.lcdp.buttontmpl.persistent.dao.LcdpButtonTmplDao;
import com.sunwayworld.cloud.module.lcdp.buttontmpl.service.LcdpButtonTmplService;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpButtonTmplServiceImpl implements LcdpButtonTmplService {
    @Autowired
    private LcdpButtonTmplDao lcdpButtonTmplDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpButtonTmplDao getDao() {
        return lcdpButtonTmplDao;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long insert(RestJsonWrapperBean wrapper) {
        LcdpButtonTmplBean buttonTmplBean = wrapper.parseUnique(LcdpButtonTmplBean.class);
        buttonTmplBean.setId(ApplicationContextHelper.getNextIdentity());
        getDao().insert(buttonTmplBean);
        return buttonTmplBean.getId();
    }
}
