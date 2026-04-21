package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.io.Serializable;

public class LcdpPageServiceMethodDTO implements Serializable {
    private static final long serialVersionUID = -3033793880187288749L;
    
    private String name; // 接口名称
    private String desc; // 接口描述
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
