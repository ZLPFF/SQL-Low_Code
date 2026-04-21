package com.sunwayworld.cloud.module.lcdp.hints.bean;

import java.io.Serializable;

public class CodeSourceCodeDTO implements Serializable {

    private static final long serialVersionUID = 6427955316379922636L;

    private int line; // 行数

    private String sourceCode; // 源码


    public static CodeSourceCodeDTO of(int line, String sourceCode) {
        CodeSourceCodeDTO instance = new CodeSourceCodeDTO();
        instance.setLine(line);
        instance.setSourceCode(sourceCode);

        return instance;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
}
