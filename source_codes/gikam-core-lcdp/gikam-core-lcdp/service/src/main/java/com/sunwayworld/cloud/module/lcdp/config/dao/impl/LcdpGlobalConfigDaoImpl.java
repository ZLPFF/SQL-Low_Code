package com.sunwayworld.cloud.module.lcdp.config.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpGlobalConfigBean;
import com.sunwayworld.cloud.module.lcdp.config.dao.LcdpGlobalConfigDao;
import com.sunwayworld.cloud.module.lcdp.config.mapper.LcdpGlobalConfigMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.framework.utils.TransactionUtils;

@Repository
@GikamBean
public class LcdpGlobalConfigDaoImpl extends MybatisDaoSupport<LcdpGlobalConfigBean, Long> implements LcdpGlobalConfigDao {

    @Autowired
    private LcdpGlobalConfigMapper lcdpGlobalConfigMapper;
    @Autowired
    private CacheManager cacheManager;

    @Override
    public LcdpGlobalConfigMapper getMapper() {
        return lcdpGlobalConfigMapper;
    }
    
    @Override
    public void cacheEvict(LcdpGlobalConfigBean oldItem, LcdpGlobalConfigBean newItem) {
        String configCode = (oldItem == null ? newItem.getConfigCode() : oldItem.getConfigCode());
        
        if (!StringUtils.isBlank(configCode)) {
            cacheManager.getCache("T_LCDP_GLOBAL_CONFIG.BY_CONFIGCODE").evict(configCode);
            
            TransactionUtils.runAfterCompletion(i -> {
                cacheManager.getCache("T_LCDP_GLOBAL_CONFIG.BY_CONFIGCODE").evict(configCode);
            });
        }
    }

}
