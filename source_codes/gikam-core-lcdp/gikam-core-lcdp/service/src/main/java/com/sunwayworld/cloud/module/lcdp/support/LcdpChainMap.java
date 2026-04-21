package com.sunwayworld.cloud.module.lcdp.support;

import java.util.*;
import java.util.function.*;

/**
 * 链式Map
 * @param <K>
 * @param <V>
 */
public class LcdpChainMap<K, V> extends AbstractCustomMap<K, V> {
    public LcdpChainMap() {
        super();
    }

    public LcdpChainMap(Map<K, V> map) {
        super(map);
    }

    public LcdpChainMap(Map.Entry<K, V> entry) {
        this(entry.getKey(), entry.getValue());
    }

    public LcdpChainMap(K key, V value) {
        this();
        put(key, value);
    }

    public LcdpChainMap(Map<K, V> map, K key, V value) {
        this(map);
        put(key, value);
    }

    /**
     * 快速构建
     *
     * @param key   键
     * @param value 值
     * @param <K>   键类型
     * @param <V>   值类型
     * @return FluentMap
     */
    public static <K, V> LcdpChainMap<K, V> of(K key, V value) {
        return new LcdpChainMap<K, V>().set(key, value);
    }


    /**
     * 增加节点
     *
     * @param key   键
     * @param value 值
     * @return {@link LcdpChainMap}
     */
    public LcdpChainMap<K, V> set(K key, V value) {
        return set(true, key, value);
    }


    /**
     * 增加节点
     *
     * @param condition 执行条件
     * @param key       键
     * @param value     值
     * @return {@link LcdpChainMap}
     */
    public LcdpChainMap<K, V> set(boolean condition, K key, V value) {
        if (condition) {
            put(key, value);
        }
        return this;
    }

    /**
     * 执行自定义逻辑
     *
     * @param function 自定义逻辑
     * @return FluentMap
     */
    public LcdpChainMap<K, V> func(Consumer<LcdpChainMap<K, V>> function) {
        return func(true, function);
    }

    /**
     * 执行自定义逻辑
     *
     * @param condition 执行条件
     * @param function  自定义逻辑
     * @return FluentMap
     */
    public LcdpChainMap<K, V> func(boolean condition, Consumer<LcdpChainMap<K, V>> function) {
        if (condition && function != null) {
            function.accept(this);
        }
        return this;
    }
    
}
