package com.sunwayworld.cloud.module.lcdp.apiintegration.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.persistent.dao.LcdpApiDao;
import com.sunwayworld.cloud.module.lcdp.apiintegration.persistent.mapper.LcdpApiMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.TransactionUtils;

@Repository
@GikamBean
public class LcdpApiDaoImpl extends MybatisDaoSupport<LcdpApiBean, Long> implements LcdpApiDao {
    @Autowired
    private LcdpApiMapper lcdpApiMapper;
    @Autowired
    private CacheManager cacheManager;

    @Override
    public LcdpApiMapper getMapper() {
        return lcdpApiMapper;
    }

    @Override
    public void cacheEvict(LcdpApiBean oldItem, LcdpApiBean newItem) {
        String apiCode = (oldItem == null ? newItem.getApiCode() : oldItem.getApiCode());
        String apiUrl = (oldItem == null ? newItem.getApiUrl() : oldItem.getApiUrl());
        
        cacheManager.getCache("T_LCDP_API").evict(apiCode);
        cacheManager.getCache("T_LCDP_API_URL").evict(apiUrl);
        
        TransactionUtils.runAfterCompletion(i -> {
            cacheManager.getCache("T_LCDP_API").evict(apiCode);
            cacheManager.getCache("T_LCDP_API_URL").evict(apiUrl);
        });
    }

}
