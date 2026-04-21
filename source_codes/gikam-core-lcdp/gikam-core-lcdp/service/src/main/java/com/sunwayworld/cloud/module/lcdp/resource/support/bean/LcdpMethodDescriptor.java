package com.sunwayworld.cloud.module.lcdp.resource.support.bean;

import java.util.List;
import java.util.Objects;

import org.springframework.lang.Nullable;

import com.sunwayworld.framework.utils.CollectionUtils;

/**
 * 低代码的方法描述类
 *
 * @author zhangjr@sunwayworld.com 2024年8月13日
 */
public class LcdpMethodDescriptor {
    private String name; // 方法名称
    private String returnTypeName; // 返回类型名称（全路径）
    private int modifiers; // 方法描述
    
    private LcdpClassDescriptor returnType; // 返回类型
    private @Nullable List<LcdpParamDescriptor> paramList; // 方法参数

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getReturnTypeName() {
        return returnTypeName;
    }
    public void setReturnTypeName(String returnTypeName) {
        this.returnTypeName = returnTypeName;
    }
    public List<LcdpParamDescriptor> getParamList() {
        return paramList;
    }
    public void setParamList(List<LcdpParamDescriptor> paramList) {
        this.paramList = paramList;
    }
    public int getModifiers() {
        return modifiers;
    }
    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }
    
    public LcdpClassDescriptor getReturnType() {
        return returnType;
    }
    public void setReturnType(LcdpClassDescriptor returnType) {
        this.returnType = returnType;
    }
    @Override
    public boolean equals(Object target) {
        if (target == null) {
            return false;
        }
        
        if (!(target instanceof LcdpMethodDescriptor)) {
            return false;
        }
        
        if (this == target) {
            return true;
        }
        
        LcdpMethodDescriptor descriptor = (LcdpMethodDescriptor) target;
        
        if (!Objects.equals(getName(), descriptor.getName())
                || !Objects.equals(getReturnTypeName(), descriptor.getReturnTypeName())) {
            return false;
        }
        
        if (CollectionUtils.isEmpty(getParamList())) {
            if (CollectionUtils.isEmpty(descriptor.getParamList())) {
                return true;
            }
            
            return false;
        }
        
        if (CollectionUtils.isEmpty(descriptor.getParamList())) {
            return false;
        }
        
        if (getParamList().size() != descriptor.getParamList().size()) {
            return false;
        }
        
        for (int i = 0, j = getParamList().size(); i < j; i++) {
            LcdpParamDescriptor param1 = getParamList().get(i);
            LcdpParamDescriptor param2 = descriptor.getParamList().get(i);
            
            if (!Objects.equals(param1.getName(), param2.getName())
                    || !Objects.equals(param1.getTypeName(), param2.getTypeName())) {
                return false;
            }
        }
        
        return true;
    }
}
