package com.sunwayworld.cloud.module.lcdp.table.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableIndexBean;
import com.sunwayworld.cloud.module.lcdp.table.persistent.dao.LcdpTableIndexDao;
import com.sunwayworld.cloud.module.lcdp.table.persistent.mapper.LcdpTableIndexMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.TransactionUtils;

@Repository
@GikamBean
public class LcdpTableIndexDaoImpl extends MybatisDaoSupport<LcdpTableIndexBean, Long> implements LcdpTableIndexDao {

    @Autowired
    private LcdpTableIndexMapper lcdpTableIndexMapper;
    @Autowired
    private CacheManager cacheManager;

    @Override
    public LcdpTableIndexMapper getMapper() {
        return lcdpTableIndexMapper;
    }
    
    @Override
    public void cacheEvict(LcdpTableIndexBean oldItem, LcdpTableIndexBean newItem) {
        String tableName = (oldItem == null ? newItem.getTableName() : oldItem.getTableName());
        evictCache(tableName);
        
        TransactionUtils.runAfterCompletion(i -> evictCache(tableName));
    }
    
    private void evictCache(String tableName) {
        cacheManager.getCache("LCDP.TABLE_INFO").evict(tableName);
    }
}
