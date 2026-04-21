package com.sunwayworld.cloud.module.lcdp.hints.core;

public enum CodeHintsCallType {
    STATIC, // static成员变量或方法
    THIS, // this.
    SUPER, // super.
    SUPER_DEFAULT, // ClassName.super.
    REGULAR;
    
    public boolean is(CodeHintsCallType callType) {
        return this.equals(callType);
    }
}
