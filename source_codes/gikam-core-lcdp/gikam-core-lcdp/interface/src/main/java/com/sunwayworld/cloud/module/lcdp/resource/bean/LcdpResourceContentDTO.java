package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.io.Serializable;

public class LcdpResourceContentDTO implements Serializable {
    private static final long serialVersionUID = 4224099364765756357L;
    
    private String content;// 内容
    private String classContent;// 类内容(只有当资源类型为后端脚本时存储真正的java类代码)
    
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getClassContent() {
        return classContent;
    }
    public void setClassContent(String classContent) {
        this.classContent = classContent;
    }
}
