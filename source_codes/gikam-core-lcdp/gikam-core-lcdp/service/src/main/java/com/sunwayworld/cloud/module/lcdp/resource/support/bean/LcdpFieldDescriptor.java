package com.sunwayworld.cloud.module.lcdp.resource.support.bean;

import org.springframework.lang.Nullable;

/**
 * 低代码的成员变量描述类
 *
 * @author zhangjr@sunwayworld.com 2024年8月13日
 */
public class LcdpFieldDescriptor {
    private String name; // 方法名称
    private String typeName; // 类型名称（全路径）
    private int modifiers; // 方法描述
    
    private @Nullable LcdpClassDescriptor type; // 返回类型
    
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
    public LcdpClassDescriptor getType() {
        return type;
    }
    public void setType(LcdpClassDescriptor type) {
        this.type = type;
    }
    public int getModifiers() {
        return modifiers;
    }
    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }
}
