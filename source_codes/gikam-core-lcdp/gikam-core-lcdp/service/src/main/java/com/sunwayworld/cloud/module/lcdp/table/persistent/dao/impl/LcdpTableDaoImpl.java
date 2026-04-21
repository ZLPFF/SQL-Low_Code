package com.sunwayworld.cloud.module.lcdp.table.persistent.dao.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.persistent.dao.LcdpTableDao;
import com.sunwayworld.cloud.module.lcdp.table.persistent.mapper.LcdpTableMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.PersistableHelper;
import com.sunwayworld.framework.utils.TransactionUtils;

@Repository
@GikamBean
public class LcdpTableDaoImpl extends MybatisDaoSupport<LcdpTableBean, Long> implements LcdpTableDao {

    @Autowired
    private LcdpTableMapper lcdpTableMapper;
    @Autowired
    private CacheManager cacheManager;

    @Override
    public LcdpTableMapper getMapper() {
        return lcdpTableMapper;
    }
    
    @Override
    public List<LcdpTableBean> selectLatestBriefList(MapperParameter parameter) {
        return getMapper().selectLatestBriefList(parameter).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpTableBean.class))
                .collect(Collectors.toList());
    }
    
    @Override
    public void cacheEvict(LcdpTableBean oldItem, LcdpTableBean newItem) {
        String tableName = (oldItem == null ? newItem.getTableName() : oldItem.getTableName());
        cacheManager.getCache("LCDP.TABLE_INFO").evict(tableName);
        
        TransactionUtils.runAfterCompletion(i -> cacheManager.getCache("LCDP.TABLE_INFO").evict(tableName));
    }
}
