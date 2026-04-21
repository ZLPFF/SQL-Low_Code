package com.sunwayworld.cloud.module.lcdp.configparam.persistent.dao.impl;

import com.sunwayworld.framework.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;


import com.sunwayworld.cloud.module.lcdp.configparam.bean.LcdpConfigParamBean;
import com.sunwayworld.cloud.module.lcdp.configparam.persistent.dao.LcdpConfigParamDao;
import com.sunwayworld.cloud.module.lcdp.configparam.persistent.mapper.LcdpConfigParamMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.TransactionUtils;

@Repository
@GikamBean
public class LcdpConfigParamDaoImpl extends MybatisDaoSupport<LcdpConfigParamBean, Long> implements LcdpConfigParamDao {

    @Autowired
    private LcdpConfigParamMapper lcdpConfigParamMapper;
    @Autowired
    private CacheManager cacheManager;

    @Override
    public LcdpConfigParamMapper getMapper() {
        return lcdpConfigParamMapper;
    }

    @Override
    public void cacheEvict(LcdpConfigParamBean oldItem, LcdpConfigParamBean newItem) {
        String paramCode = (oldItem == null ? newItem.getParamCode() : oldItem.getParamCode());
        String userId = (oldItem == null ? newItem.getUserId() : oldItem.getUserId());
        
        if (!StringUtils.isBlank(paramCode)) {
            cacheManager.getCache("T_LCDP_CONFIG_PARAM.PARAM_VALUE").evict(paramCode);
        }
        
        if (!StringUtils.isBlank(userId)) {
            cacheManager.getCache("T_LCDP_CONFIG_PARAM.BY_LOGINUSER").evict(userId);
        }
        
        TransactionUtils.runAfterCompletion(i -> {
            if (!StringUtils.isBlank(paramCode)) {
                cacheManager.getCache("T_LCDP_CONFIG_PARAM.PARAM_VALUE").evict(paramCode);
            }
            
            if (!StringUtils.isBlank(userId)) {
                cacheManager.getCache("T_LCDP_CONFIG_PARAM.BY_LOGINUSER").evict(userId);
            }
        });
    }
}
