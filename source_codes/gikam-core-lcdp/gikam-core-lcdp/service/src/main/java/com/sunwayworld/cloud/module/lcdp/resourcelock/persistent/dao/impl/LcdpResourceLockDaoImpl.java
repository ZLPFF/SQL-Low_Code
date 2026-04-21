package com.sunwayworld.cloud.module.lcdp.resourcelock.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.resourcelock.bean.LcdpResourceLockBean;
import com.sunwayworld.cloud.module.lcdp.resourcelock.persistent.dao.LcdpResourceLockDao;
import com.sunwayworld.cloud.module.lcdp.resourcelock.persistent.mapper.LcdpResourceLockMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.TransactionUtils;

@Repository
@GikamBean
public class LcdpResourceLockDaoImpl extends MybatisDaoSupport<LcdpResourceLockBean, Long> implements LcdpResourceLockDao {

    @Autowired
    private LcdpResourceLockMapper lcdpResourceLockMapper;
    @Autowired
    private CacheManager cacheManager;

    @Override
    public LcdpResourceLockMapper getMapper() {
        return lcdpResourceLockMapper;
    }
    
    @Override
    public void cacheEvict(LcdpResourceLockBean oldItem, LcdpResourceLockBean newItem) {
        String lockUserId = (oldItem == null ? newItem.getLockUserId() : oldItem.getLockUserId());
        String resourceCategory = (oldItem == null ? newItem.getResourceCategory() : oldItem.getResourceCategory());
        String resourceId = (oldItem == null ? newItem.getResourceId() : oldItem.getResourceId());
        
        cacheManager.getCache("T_LCDP_RESOURCE_LOCK.BY_CATEGORY").evict(lockUserId + "-" + resourceCategory);
        cacheManager.getCache("T_LCDP_RESOURCE_LOCK.RESOURCE_LOCK").evict(resourceId + "-" + resourceCategory);
        
        TransactionUtils.runAfterCompletion(i -> {
            cacheManager.getCache("T_LCDP_RESOURCE_LOCK.BY_CATEGORY").evict(lockUserId + "-" + resourceCategory);
            cacheManager.getCache("T_LCDP_RESOURCE_LOCK.RESOURCE_LOCK").evict(resourceId + "-" + resourceCategory);
        });
    }
    
    @Override
    public String[] getForCacheEvictColumns() {
        return new String[] {"ID", "LOCKUSERID", "RESOURCECATEGORY", "RESOURCEID"};
    }
}
