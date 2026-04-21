package com.sunwayworld.cloud.module.lcdp.resource.support.bean;

import java.util.Map;

/**
 * 低代码的类描述类
 *
 * @author zhangjr@sunwayworld.com 2024年8月13日
 */
public class LcdpClassDescriptor {
    private String className; // 类
    private String classFullName; // 类的全称
    private boolean lcdpClass; // 是否是低代码开发的类
    
    private Map<String, LcdpClassDescriptor> genericMap; // 对于泛型类对那个的泛型类型的Map 
    
    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public String getClassFullName() {
        return classFullName;
    }
    public void setClassFullName(String classFullName) {
        this.classFullName = classFullName;
    }
    public boolean isLcdpClass() {
        return lcdpClass;
    }
    public void setLcdpClass(boolean lcdpClass) {
        this.lcdpClass = lcdpClass;
    }
    public Map<String, LcdpClassDescriptor> getGenericMap() {
        return genericMap;
    }
    public void setGenericMap(Map<String, LcdpClassDescriptor> genericMap) {
        this.genericMap = genericMap;
    }
}
