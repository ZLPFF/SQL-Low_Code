package com.sunwayworld.cloud.module.lcdp.config.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpGlobalCompConfigBean;
import com.sunwayworld.cloud.module.lcdp.config.dao.LcdpGlobalCompConfigDao;
import com.sunwayworld.cloud.module.lcdp.config.mapper.LcdpGlobalCompConfigMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.TransactionUtils;

@Repository
@GikamBean
public class LcdpGlobalCompConfigDaoImpl extends MybatisDaoSupport<LcdpGlobalCompConfigBean, Long> implements LcdpGlobalCompConfigDao {

    @Autowired
    private LcdpGlobalCompConfigMapper lcdpGlobalCompConfigMapper;
    @Autowired
    private CacheManager cacheManager;

    @Override
    public LcdpGlobalCompConfigMapper getMapper() {
        return lcdpGlobalCompConfigMapper;
    }
    
    @Override
    public void cacheEvict(LcdpGlobalCompConfigBean oldItem, LcdpGlobalCompConfigBean newItem) {
        cacheManager.getCache("T_LCDP_GLOBAL_COMP_CONFIG").evict("ALL");
        
        TransactionUtils.runAfterCompletion(i -> {
            cacheManager.getCache("T_LCDP_GLOBAL_COMP_CONFIG").evict("ALL");
        });
    }

}
