package com.sunwayworld.cloud.module.lcdp.config.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpGlobalCompConfigBean;
import com.sunwayworld.cloud.module.lcdp.config.dao.LcdpGlobalCompConfigDao;
import com.sunwayworld.cloud.module.lcdp.config.service.LcdpGlobalCompConfigService;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@Repository
@GikamBean
public class LcdpGlobalCompConfigServiceImpl implements LcdpGlobalCompConfigService {

    @Autowired
    private LcdpGlobalCompConfigDao lcdpGlobalCompConfigDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpGlobalCompConfigDao getDao() {
        return lcdpGlobalCompConfigDao;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        List<LcdpGlobalCompConfigBean> lcdpGlobalCompConfigList = jsonWrapper.parse(LcdpGlobalCompConfigBean.class);
        //删除重新添加
        List<LcdpGlobalCompConfigBean> allCompConfigList = selectAll();
        getDao().deleteBy(allCompConfigList);
        lcdpGlobalCompConfigList.forEach(config -> config.setId(ApplicationContextHelper.getNextIdentity()));
        getDao().insert(lcdpGlobalCompConfigList);
        return lcdpGlobalCompConfigList.get(0).getId();
    }

    @Override
    @Cacheable(value = "T_LCDP_GLOBAL_COMP_CONFIG", key = "'ALL'", unless="#result == null")
    public Map<String, List<LcdpGlobalCompConfigBean>> selectConfigList() {
        List<LcdpGlobalCompConfigBean> lcdpGlobalCompConfigList = selectAll();
        return lcdpGlobalCompConfigList.stream().collect(Collectors.groupingBy(LcdpGlobalCompConfigBean::getCompCategory));
    }
}
