package com.sunwayworld.cloud.module.lcdp.apiintegration.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiFieldBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.persistent.dao.LcdpApiFieldDao;
import com.sunwayworld.cloud.module.lcdp.apiintegration.persistent.mapper.LcdpApiFieldMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.TransactionUtils;

@Repository
@GikamBean
public class LcdpApiFieldDaoImpl extends MybatisDaoSupport<LcdpApiFieldBean, Long> implements LcdpApiFieldDao {

    @Autowired
    private LcdpApiFieldMapper lcdpApiFieldMapper;
    @Autowired
    private CacheManager cacheManager;

    @Override
    public LcdpApiFieldMapper getMapper() {
        return lcdpApiFieldMapper;
    }

    @Override
    public void cacheEvict(LcdpApiFieldBean oldItem, LcdpApiFieldBean newItem) {
        String apiId = "" + (oldItem == null ? newItem.getApiId() : oldItem.getApiId());
        
        cacheManager.getCache("T_LCDP_API_FIELD").evict(apiId);
        
        TransactionUtils.runAfterCompletion(i -> {
            cacheManager.getCache("T_LCDP_API_FIELD").evict(apiId);
        });
    }

}
