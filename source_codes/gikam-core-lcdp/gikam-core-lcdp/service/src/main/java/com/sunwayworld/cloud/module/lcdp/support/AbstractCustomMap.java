package com.sunwayworld.cloud.module.lcdp.support;

import java.util.*;

public abstract class AbstractCustomMap<K, V> implements Map<K, V> {
    /**
     * 静态代理对象
     * <p>
     * 真正实现接口的对象
     */
    private final Map<K, V> map;

    protected AbstractCustomMap() {
        this.map = new HashMap<>();
    }

    protected AbstractCustomMap(Map<K, V> map) {
        this.map = map == null ? new HashMap<>() : map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        return map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }
}
