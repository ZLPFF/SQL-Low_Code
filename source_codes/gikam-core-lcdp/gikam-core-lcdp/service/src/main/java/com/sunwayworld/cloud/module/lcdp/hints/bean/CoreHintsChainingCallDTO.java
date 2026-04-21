package com.sunwayworld.cloud.module.lcdp.hints.bean;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

import com.sunwayworld.cloud.module.lcdp.hints.core.CodeHintsCallType;

/**
 * 链式调用，如果低代码开始时，会提供源代码，否则提供类
 *
 * @author zhangjr@sunwayworld.com 2024年8月16日
 */
public class CoreHintsChainingCallDTO {
    private @Nullable ResolvableType resolvableType; // 当前调用类
    private @Nullable String sourceCode; // 当前调用类的源代码（要用content，而不是classContent），调用类和源代码二选一
    
    private int level = 0; // 调用层级
    private Queue<String> chaining; // 链式调用队列
    private CodeHintsCallType callType = CodeHintsCallType.REGULAR; // 调用类型 
    
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
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public Queue<String> getChaining() {
        return chaining;
    }
    public void setChaining(Queue<String> chaining) {
        this.chaining = chaining;
    }
    public void addNextChain(String next) {
        if (chaining == null) {
            chaining = new LinkedBlockingQueue<>();
        }
        
        chaining.add(next);
    }
    public CodeHintsCallType getCallType() {
        return callType;
    }
    public void setCallType(CodeHintsCallType callType) {
        this.callType = callType;
    }
}
