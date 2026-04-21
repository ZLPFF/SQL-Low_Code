package com.sunwayworld.cloud.module.lcdp.moduletmpl.persistent.dao.impl;

import com.sunwayworld.cloud.module.lcdp.moduletmpl.bean.LcdpCustomTmplPageCompBean;
import com.sunwayworld.cloud.module.lcdp.moduletmpl.persistent.dao.LcdpCustomTmplPageCompDao;
import com.sunwayworld.cloud.module.lcdp.moduletmpl.persistent.mapper.LcdpCustomTmplPageCompMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@GikamBean
public class LcdpCustomTmplPageCompDaoImpl extends MybatisDaoSupport<LcdpCustomTmplPageCompBean, String> implements LcdpCustomTmplPageCompDao {

    @Autowired
    private LcdpCustomTmplPageCompMapper lcdpCustomTmplPageCompMapper;

    @Override
    public LcdpCustomTmplPageCompMapper getMapper() {
        return lcdpCustomTmplPageCompMapper;
    }

}
