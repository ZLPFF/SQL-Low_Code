package com.sunwayworld.cloud.module.lcdp.resource.support.bean;

import org.springframework.lang.Nullable;

/**
 * 低代码的方法参数描述类
 *
 * @author zhangjr@sunwayworld.com 2024年8月13日
 */
public class LcdpParamDescriptor {
    private String name; // 方法名称
    private String typeName; // 类型名称（全路径）
    private @Nullable Class<?> type; // 类型
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getTypeName() {
        return typeName;
    }
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
    public Class<?> getType() {
        return type;
    }
    public void setType(Class<?> type) {
        this.type = type;
    }
}
