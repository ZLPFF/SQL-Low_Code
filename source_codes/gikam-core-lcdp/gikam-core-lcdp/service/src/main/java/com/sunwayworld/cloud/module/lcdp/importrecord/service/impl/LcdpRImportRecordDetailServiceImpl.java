package com.sunwayworld.cloud.module.lcdp.importrecord.service.impl;

import com.sunwayworld.cloud.module.lcdp.importrecord.persistent.dao.LcdpRImportRecordDetailDao;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.framework.spring.annotation.GikamBean;

import com.sunwayworld.cloud.module.lcdp.importrecord.bean.LcdpRImportRecordDetailBean;
import com.sunwayworld.cloud.module.lcdp.importrecord.service.LcdpRImportRecordDetailService;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

@Repository
@GikamBean
public class LcdpRImportRecordDetailServiceImpl implements LcdpRImportRecordDetailService {

    @Autowired
    private LcdpRImportRecordDetailDao lcdpRImportRecordDetailDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpRImportRecordDetailDao getDao() {
        return lcdpRImportRecordDetailDao;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        LcdpRImportRecordDetailBean lcdpRImportRecordDetail = jsonWrapper.parseUnique(LcdpRImportRecordDetailBean.class);
        lcdpRImportRecordDetail.setId(ApplicationContextHelper.getNextIdentity());
        getDao().insert(lcdpRImportRecordDetail);
        return lcdpRImportRecordDetail.getId();
    }

}
