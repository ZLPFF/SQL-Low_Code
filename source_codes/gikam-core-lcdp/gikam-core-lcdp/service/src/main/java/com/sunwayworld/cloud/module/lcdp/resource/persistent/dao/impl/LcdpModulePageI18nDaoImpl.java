package com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModulePageI18nBean;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.LcdpModulePageI18nDao;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.mapper.LcdpModulePageI18nMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.TransactionUtils;

@Repository
@GikamBean
public class LcdpModulePageI18nDaoImpl extends MybatisDaoSupport<LcdpModulePageI18nBean, Long> implements LcdpModulePageI18nDao {

    @Autowired
    private LcdpModulePageI18nMapper lcdpModulePageI18nMapper;
    @Autowired
    private CacheManager cacheManager;

    @Override
    public LcdpModulePageI18nMapper getMapper() {
        return lcdpModulePageI18nMapper;
    }

    @Override
    public List<Map<String, Object>> selectAllI18nMessage(MapperParameter parameter) {
        return lcdpModulePageI18nMapper.selectAllI18nMessage(parameter);
    }

    @Override
    public List<Map<String, Object>> selectEffectiveByCondition(MapperParameter parameter) {
        return lcdpModulePageI18nMapper.selectEffectiveByCondition(parameter);
    }
    
    @Override
    public void cacheEvict(LcdpModulePageI18nBean oldItem, LcdpModulePageI18nBean newItem) {
        String modulePageHistoryId = "" + (oldItem == null ? newItem.getModulePageHistoryId() : oldItem.getModulePageHistoryId());
        
        cacheManager.getCache("T_LCDP_MODULE_PAGE_I18N.BY_MODULEPAGEHISTORYID").evict(modulePageHistoryId);
        
        TransactionUtils.runAfterCompletion(i -> {
            cacheManager.getCache("T_LCDP_MODULE_PAGE_I18N.BY_MODULEPAGEHISTORYID").evict(modulePageHistoryId);
        });
    }
}
