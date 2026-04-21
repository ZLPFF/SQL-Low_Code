package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.NotNull;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractPersistable;
import com.sunwayworld.framework.support.domain.Persistable;

/**
 * 低代码平台后端脚本方法表
 * 
 * @author liuxia@sunwayworld.com
 * @date 2022-11-02
 */
@Table("T_LCDP_SERVER_SCRIPT_METHOD")
public class LcdpServerScriptMethodBean extends AbstractPersistable<Long> implements Persistable<Long> {

    @Transient
    private static final long serialVersionUID = -82566196618711043L;

    @Id
    private Long id;// 主键
    private Long serverScriptId;// 服务端脚本ID(对应资源类型为server的资源ID)
    private String methodPath;// 方法路径
    private String methodName;//方法名称
    @NotNull(defaultValue = "0")
    private String deleteFlag;//删除标记
    private String deletedById;//删除人编码
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deleteTime;// 删除时间
    private String methodDesc;//方法描述
    private String mappingType;//方法映射类型

    private String methodCreatedById;//方法创建人
    private String methodFlag;//方法标记（新增，正常）
    private String todoFlag;//待办方法标记（是，否）

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServerScriptId() {
        return serverScriptId;
    }

    public void setServerScriptId(Long serverScriptId) {
        this.serverScriptId = serverScriptId;
    }

    public String getMethodPath() {
        return methodPath;
    }

    public void setMethodPath(String methodPath) {
        this.methodPath = methodPath;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(String deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public String getDeletedById() {
        return deletedById;
    }

    public void setDeletedById(String deletedById) {
        this.deletedById = deletedById;
    }

    public LocalDateTime getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(LocalDateTime deleteTime) {
        this.deleteTime = deleteTime;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }

    public String getMappingType() {
        return mappingType;
    }

    public void setMappingType(String mappingType) {
        this.mappingType = mappingType;
    }

    public String getMethodCreatedById() {
        return methodCreatedById;
    }

    public void setMethodCreatedById(String methodCreatedById) {
        this.methodCreatedById = methodCreatedById;
    }

    public String getMethodFlag() {
        return methodFlag;
    }

    public void setMethodFlag(String methodFlag) {
        this.methodFlag = methodFlag;
    }

    public String getTodoFlag() {
        return todoFlag;
    }

    public void setTodoFlag(String todoFlag) {
        this.todoFlag = todoFlag;
    }
}
