package com.sunwayworld.cloud.module.lcdp.message.sync;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.alibaba.fastjson.JSON;
import com.sunwayworld.cloud.module.lcdp.message.sync.bean.LcdpResourceSyncDTO;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.executor.manager.TaskExecutorManager;

/**
 * 同步资源
 */
public abstract class LcdpResourceSyncManager {
    private static StringRedisTemplate redisTemplate;
    
    public static void sync(Long resourceId) {
        TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> {
            getRedisTemplate().convertAndSend(LcdpResourceSyncListener.LCDP_SYNC_RESOURCE_TOPIC, JSON.toJSONString(LcdpResourceSyncDTO.of(resourceId)));
        });
    }
    
    //------------------------------------------------------------------------
    // 私有方法
    //------------------------------------------------------------------------
    private static StringRedisTemplate getRedisTemplate() {
        if (redisTemplate == null) {
            redisTemplate = ApplicationContextHelper.getBean(StringRedisTemplate.class);
        }
        
        return redisTemplate;
    }
}
