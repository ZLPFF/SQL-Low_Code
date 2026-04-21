package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.io.Serializable;

import org.springframework.lang.Nullable;

public class LcdpPageServiceColumnDTO implements Serializable {
    private static final long serialVersionUID = 7395382305612571154L;
    
    private String name; // 列名称
    private String type; // 列类型
    private @Nullable String desc; // 列描述
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
