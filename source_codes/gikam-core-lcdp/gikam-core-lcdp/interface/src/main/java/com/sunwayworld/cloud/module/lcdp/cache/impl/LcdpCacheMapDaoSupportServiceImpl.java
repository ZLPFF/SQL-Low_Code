package com.sunwayworld.cloud.module.lcdp.cache.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.cache.LcdpCacheManager;
import com.sunwayworld.framework.database.context.TableContext;
import com.sunwayworld.framework.database.context.instance.TableContextInstance;
import com.sunwayworld.framework.executor.manager.TaskExecutorManager;
import com.sunwayworld.framework.mybatis.service.impl.MapDaoSupportServiceImpl;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@GikamBean
@Repository
public class LcdpCacheMapDaoSupportServiceImpl extends MapDaoSupportServiceImpl {
    @Override
    public void preInsert(TableContextInstance instance) {
        super.preInsert(instance);
        
        if (!LcdpCacheManager.isCached(instance.getContext().getTableName()) ) {
            return;
        }
        
        // 清缓存
        TaskExecutorManager.getDefaultRunner().submitAfterCommitTransaction(() -> LcdpCacheManager.removeByTable(instance.getContext().getTableName()));
    }
    
    @Override
    public void preInsertList(TableContext context, List<TableContextInstance> instanceList) {
        super.preInsertList(context, instanceList);
        
        if (!LcdpCacheManager.isCached(context.getTableName()) ) {
            return;
        }
        
        // 清缓存
        TaskExecutorManager.getDefaultRunner().submitAfterCommitTransaction(() -> LcdpCacheManager.removeByTable(context.getTableName()));
    }
    
    @Override
    public void preUpdateById(TableContextInstance instance) {
        super.preUpdateById(instance);
        
        if (!LcdpCacheManager.isCached(instance.getContext().getTableName()) ) {
            return;
        }
        
        // 清缓存
        TaskExecutorManager.getDefaultRunner().submitAfterCommitTransaction(() -> LcdpCacheManager.removeByTable(instance.getContext().getTableName()));
    }
    
    @Override
    public <ID> void preUpdateByIdList(TableContextInstance instance, List<ID> idList) {
        super.preUpdateByIdList(instance, idList);
        
        if (!LcdpCacheManager.isCached(instance.getContext().getTableName()) ) {
            return;
        }
        
        // 清缓存
        TaskExecutorManager.getDefaultRunner().submitAfterCommitTransaction(() -> LcdpCacheManager.removeByTable(instance.getContext().getTableName()));
    }
    
    @Override
    public void preUpdateByInstanceList(TableContext context, List<TableContextInstance> instanceList, List<String> updateColumnList) {
        super.preUpdateByInstanceList(context, instanceList, updateColumnList);
        
        if (!LcdpCacheManager.isCached(context.getTableName()) ) {
            return;
        }
        
        // 清缓存
        TaskExecutorManager.getDefaultRunner().submitAfterCommitTransaction(() -> LcdpCacheManager.removeByTable(context.getTableName()));
    }
    
    @Override
    public <ID> void preDeleteById(TableContext context, ID id) {
        super.preDeleteById(context, id);
        
        if (!LcdpCacheManager.isCached(context.getTableName()) ) {
            return;
        }
        
        // 清缓存
        TaskExecutorManager.getDefaultRunner().submitAfterCommitTransaction(() -> LcdpCacheManager.removeByTable(context.getTableName()));
    }

    @Override
    public <ID> void preDeleteByIdList(TableContext context, List<ID> idList) {
        super.preDeleteByIdList(context, idList);
        
        if (!LcdpCacheManager.isCached(context.getTableName()) ) {
            return;
        }
        
        // 清缓存
        TaskExecutorManager.getDefaultRunner().submitAfterCommitTransaction(() -> LcdpCacheManager.removeByTable(context.getTableName()));
    }
}
