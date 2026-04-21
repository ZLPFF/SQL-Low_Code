package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.io.Serializable;

import org.springframework.lang.Nullable;

/**
 * 资源对应的类信息
 * 
 * @author zhangjr@sunwayworld.com 2024年8月7日
 */
public class LcdpResourceClassInfoDTO implements Serializable {
    private static final long serialVersionUID = -1678460419007093662L;
    
    private Long resourceId; // 资源ID
    private String className; // 类名称
    private String classFullName; // 类全称
    private String classSourceCode; // 可编译的源代码
    private String sourceCode; // 原始的源代码
    private @Nullable Class<?> clazz; // 已编译的类
    private @Nullable Long resourceHistoryId; // 资源历史表的ID
    private @Nullable Long modifyVersion; // 资源历史表的变更版本
    private @Nullable Long compiledVersion; // 资源历史表的编译版本
    
    public Long getResourceId() {
        return resourceId;
    }
    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
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
    public String getClassSourceCode() {
        return classSourceCode;
    }
    public void setClassSourceCode(String classSourceCode) {
        this.classSourceCode = classSourceCode;
    }
    public String getSourceCode() {
        return sourceCode;
    }
    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
    public Class<?> getClazz() {
        return clazz;
    }
    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }
    public Long getResourceHistoryId() {
        return resourceHistoryId;
    }
    public void setResourceHistoryId(Long resourceHistoryId) {
        this.resourceHistoryId = resourceHistoryId;
    }
    public Long getModifyVersion() {
        return modifyVersion;
    }
    public void setModifyVersion(Long modifyVersion) {
        this.modifyVersion = modifyVersion;
    }
    public Long getCompiledVersion() {
        return compiledVersion;
    }
    public void setCompiledVersion(Long compiledVersion) {
        this.compiledVersion = compiledVersion;
    }
}
