package com.sunwayworld.cloud.module.lcdp.hints.bean;

import java.io.Serializable;
import java.util.Objects;

import com.sunwayworld.framework.utils.StringUtils;

public class CodeHintsTextDTO implements Serializable {
    private static final long serialVersionUID = -8035326543280508805L;
    
    private String name; // 名称（成员变量或方法）
    
    private String hintsText; // 用于代码提醒的字符串
    
    private boolean field; // 是否是成员变量
    
    private boolean staticModifier; // 是否是静态变量或静态方法

    private Long resourceId; // 源代码资源ID
    
    public static CodeHintsTextDTO of(String name, String hintsText, boolean field, boolean staticModifier) {
        CodeHintsTextDTO instance = new CodeHintsTextDTO();
        instance.setName(name);
        instance.setHintsText(hintsText);
        instance.setField(field);
        instance.setStaticModifier(staticModifier);
        
        return instance;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getHintsText() {
        return hintsText;
    }
    public void setHintsText(String hintsText) {
        this.hintsText = hintsText;
    }
    public boolean isField() {
        return field;
    }
    public void setField(boolean field) {
        this.field = field;
    }
    public boolean isStaticModifier() {
        return staticModifier;
    }
    public void setStaticModifier(boolean staticModifier) {
        this.staticModifier = staticModifier;
    }
    public Long getResourceId() {
        return resourceId;
    }
    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public boolean equals(Object target) {
        if (target == null) {
            return false;
        }
        
        if (!(target instanceof CodeHintsTextDTO)) {
            return false;
        }
        
        CodeHintsTextDTO instance = (CodeHintsTextDTO) target;
        
        if (isField()) {
            return Objects.equals(instance.getName(), getName())
                    && Objects.equals(instance.isField(), field);
        } else {
            return Objects.equals(instance.getName(), getName())
                    && Objects.equals(instance.isField(), field)
                    && StringUtils.contains(instance.getHintsText(), ":")
                    && StringUtils.contains(getHintsText(), ":")
                    && instance.getHintsText().substring(0, instance.getHintsText().indexOf(":")).equals(getHintsText().substring(0, getHintsText().indexOf(":")));
        }
    }
}
