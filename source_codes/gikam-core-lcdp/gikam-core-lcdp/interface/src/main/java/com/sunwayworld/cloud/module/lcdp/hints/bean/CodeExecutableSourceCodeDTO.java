package com.sunwayworld.cloud.module.lcdp.hints.bean;

import java.io.Serializable;

public class CodeExecutableSourceCodeDTO implements Serializable {

    private static final long serialVersionUID = 707425453103332687L;

    private String errorLog;// 编译报错日志

    private String executableSourceCode; // 可执行源码


    public String getErrorLog() {
        return errorLog;
    }

    public void setErrorLog(String errorLog) {
        this.errorLog = errorLog;
    }

    public String getExecutableSourceCode() {
        return executableSourceCode;
    }

    public void setExecutableSourceCode(String executableSourceCode) {
        this.executableSourceCode = executableSourceCode;
    }
}
