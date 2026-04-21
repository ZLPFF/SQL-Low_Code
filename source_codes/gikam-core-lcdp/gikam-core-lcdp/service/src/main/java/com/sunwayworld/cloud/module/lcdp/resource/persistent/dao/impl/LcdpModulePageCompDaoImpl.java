package com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModulePageCompBean;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.LcdpModulePageCompDao;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.mapper.LcdpModulePageCompMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.TransactionUtils;

@Repository
@GikamBean
public class LcdpModulePageCompDaoImpl extends MybatisDaoSupport<LcdpModulePageCompBean, String> implements LcdpModulePageCompDao {

    @Autowired
    private LcdpModulePageCompMapper lcdpModulePageCompMapper;
    @Autowired
    private CacheManager cacheManager;

    @Override
    public LcdpModulePageCompMapper getMapper() {
        return lcdpModulePageCompMapper;
    }

    @Override
    public void cacheEvict(LcdpModulePageCompBean oldItem, LcdpModulePageCompBean newItem) {
        Long modulePageId = (oldItem == null ? newItem.getModulePageId() : oldItem.getModulePageId());
        Long modulePageHistoryId = (oldItem == null ? newItem.getModulePageHistoryId() : oldItem.getModulePageHistoryId());
        
        if (modulePageId != null) {
            cacheManager.getCache("T_LCDP_MODULE_PAGE_COMP.BY_MODULEPAGEID").evict("" + modulePageId);
        }
        if (modulePageHistoryId != null) {
            cacheManager.getCache("T_LCDP_MODULE_PAGE_COMP.BY_MODULEPAGEHISTORYID").evict("" + modulePageHistoryId);
        }
        
        TransactionUtils.runAfterCompletion(i -> {
            if (modulePageId != null) {
                cacheManager.getCache("T_LCDP_MODULE_PAGE_COMP.BY_MODULEPAGEID").evict("" + modulePageId);
            }
            if (modulePageHistoryId != null) {
                cacheManager.getCache("T_LCDP_MODULE_PAGE_COMP.BY_MODULEPAGEHISTORYID").evict("" + modulePageHistoryId);
            }
        });
    }
    
    @Override
    public String[] getForCacheEvictColumns() {
        return new String[] {"ID", "MODULEPAGEID", "MODULEPAGEHISTORYID"};
    }
}
