package com.sunwayworld.cloud.module.lcdp.message.log.bean;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.alibaba.fastjson.annotation.JSONField;
import com.sunwayworld.framework.context.ApplicationContextHelper;

public class LcdpScriptLogDTO implements Serializable {
    private static final long serialVersionUID = 3521984962649808171L;

    private Long id;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime executionTime;

    private String type;

    private String threadName;

    private String log;

    private String status;// 在线状态

    private String user; // 用户标识

    public LocalDateTime getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public static LcdpScriptLogDTO of(String type, String log){
        LcdpScriptLogDTO scriptLog = new LcdpScriptLogDTO();

        scriptLog.setId(ApplicationContextHelper.getNextIdentity());
        scriptLog.setExecutionTime(LocalDateTime.now());
        scriptLog.setThreadName(Thread.currentThread().getName());
        scriptLog.setLog(log);
        scriptLog.setType(type);
        scriptLog.setStatus("1");
        return scriptLog;
    }

    public static LcdpScriptLogDTO ofOffline(String type, String log){
        LcdpScriptLogDTO scriptLogInfo = of(type, log);
        scriptLogInfo.setStatus("0");
        return scriptLogInfo;
    }
}
