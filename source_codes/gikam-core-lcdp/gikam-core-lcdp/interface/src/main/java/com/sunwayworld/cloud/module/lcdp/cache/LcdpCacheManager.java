package com.sunwayworld.cloud.module.lcdp.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.sunwayworld.framework.cache.redis.RedisHelper;
import com.sunwayworld.framework.cache.redis.aspect.RedisAspect;

public class LcdpCacheManager {
    private static Map<String, String> CACHE_KEY_HOLDER = new ConcurrentHashMap<>();
    
    public static <V> V get(String table, String key, Supplier<V> supplier) {
        table = table.toUpperCase();
        
        String finalKey = table + ":" + key.toUpperCase();
        
        V value = RedisHelper.get(RedisAspect.BIGKEY_CACHE_NAME, finalKey);
        
        if (value == null) {
            value = supplier.get();
            
            RedisHelper.put(RedisAspect.BIGKEY_CACHE_NAME, finalKey, value);
            
            CACHE_KEY_HOLDER.put(finalKey, table);
        }
        
        return value;
    }
    
    public static void removeByTable(String table) {
        String upperTable = table.toUpperCase();
        
        CACHE_KEY_HOLDER.entrySet().stream().filter(e -> upperTable.equals(e.getValue())).forEach(e -> RedisHelper.evict(RedisAspect.BIGKEY_CACHE_NAME, e.getKey()));
    }
    
    public static boolean isCached(String table) {
        String upperTable = table.toUpperCase();
        
        return CACHE_KEY_HOLDER.values().contains(upperTable);
    }
}
