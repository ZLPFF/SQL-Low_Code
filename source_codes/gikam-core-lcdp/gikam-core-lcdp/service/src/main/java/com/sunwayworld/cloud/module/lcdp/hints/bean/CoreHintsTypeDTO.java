package com.sunwayworld.cloud.module.lcdp.hints.bean;

import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * 对于类型，返回实际类或源代码（没有编译或编译失败）
 *
 * @author zhangjr@sunwayworld.com 2024年9月25日
 */
public class CoreHintsTypeDTO {
    private @Nullable ResolvableType resolvableType; // 当前调用类
    private @Nullable String sourceCode; // 当前调用类的源代码（要用content，而不是classContent），调用类和源代码二选一
    
    public ResolvableType getResolvableType() {
        return resolvableType;
    }
    public void setResolvableType(ResolvableType resolvableType) {
        this.resolvableType = resolvableType;
    }
    public String getSourceCode() {
        return sourceCode;
    }
    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
}
