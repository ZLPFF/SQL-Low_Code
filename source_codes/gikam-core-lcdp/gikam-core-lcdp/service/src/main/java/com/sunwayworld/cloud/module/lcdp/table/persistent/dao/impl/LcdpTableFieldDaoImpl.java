package com.sunwayworld.cloud.module.lcdp.table.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldBean;
import com.sunwayworld.cloud.module.lcdp.table.persistent.dao.LcdpTableFieldDao;
import com.sunwayworld.cloud.module.lcdp.table.persistent.mapper.LcdpTableFieldMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.TransactionUtils;

@Repository
@GikamBean
public class LcdpTableFieldDaoImpl extends MybatisDaoSupport<LcdpTableFieldBean, Long> implements LcdpTableFieldDao {

    @Autowired
    private LcdpTableFieldMapper lcdpTableFieldMapper;
    @Autowired
    private CacheManager cacheManager;

    @Override
    public LcdpTableFieldMapper getMapper() {
        return lcdpTableFieldMapper;
    }
    
    @Override
    public void cacheEvict(LcdpTableFieldBean oldItem, LcdpTableFieldBean newItem) {
        String tableName = (oldItem == null ? newItem.getTableName() : oldItem.getTableName());
        evictCache(tableName);
        
        TransactionUtils.runAfterCompletion(i -> evictCache(tableName));
    }
    
    private void evictCache(String tableName) {
        cacheManager.getCache("LCDP.TABLE_INFO").evict(tableName);
    }
}
