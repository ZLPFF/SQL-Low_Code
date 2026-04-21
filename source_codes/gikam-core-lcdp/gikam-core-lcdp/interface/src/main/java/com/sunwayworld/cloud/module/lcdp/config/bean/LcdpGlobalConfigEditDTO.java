package com.sunwayworld.cloud.module.lcdp.config.bean;

import java.io.Serializable;

import org.springframework.lang.Nullable;

/**
 * 全局配置编辑
 * 
 * @author zhangjr@sunwayworld.com 2024年10月31日
 */
public class LcdpGlobalConfigEditDTO implements Serializable {
    private static final long serialVersionUID = 2004546301598184812L;
    
    private @Nullable Long effectId;// 生效的主键
    private String effectContent;// 生效的配置内容
    
    private @Nullable Long editId;// 编辑中的主键
    private String editContent;// 编辑中的配置内容
    
    public Long getEffectId() {
        return effectId;
    }
    public void setEffectId(Long effectId) {
        this.effectId = effectId;
    }
    public String getEffectContent() {
        return effectContent;
    }
    public void setEffectContent(String effectContent) {
        this.effectContent = effectContent;
    }
    public Long getEditId() {
        return editId;
    }
    public void setEditId(Long editId) {
        this.editId = editId;
    }
    public String getEditContent() {
        return editContent;
    }
    public void setEditContent(String editContent) {
        this.editContent = editContent;
    }
}
