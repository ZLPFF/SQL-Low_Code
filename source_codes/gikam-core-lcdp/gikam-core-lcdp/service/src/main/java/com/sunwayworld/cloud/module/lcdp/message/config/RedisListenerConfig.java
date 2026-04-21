package com.sunwayworld.cloud.module.lcdp.message.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import com.sunwayworld.cloud.module.lcdp.message.log.LcdpScriptLogListener;
import com.sunwayworld.cloud.module.lcdp.message.sync.LcdpResourceSyncListener;

@Configuration
public class RedisListenerConfig {
    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // 前端输出日志
        container.addMessageListener(new LcdpScriptLogListener(), new PatternTopic(LcdpScriptLogListener.LCDP_SCRIPT_LOG_TOPIC));
        // 同步资源（含历史资源）
        container.addMessageListener(new LcdpResourceSyncListener(), new PatternTopic(LcdpResourceSyncListener.LCDP_SYNC_RESOURCE_TOPIC));
        return container;
    }
}
