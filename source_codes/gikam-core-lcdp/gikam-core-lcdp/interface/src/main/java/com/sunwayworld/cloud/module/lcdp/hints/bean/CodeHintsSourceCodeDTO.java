package com.sunwayworld.cloud.module.lcdp.hints.bean;

import java.io.Serializable;

import org.springframework.lang.Nullable;

/**
 * 查看源代码，可以是当前低代码中开发的源代码或非低代码开发的
 *
 * @author zhangjr@sunwayworld.com 2024年9月23日
 */
public class CodeHintsSourceCodeDTO implements Serializable {
    private static final long serialVersionUID = 6912572210910597174L;

    private @Nullable Long resourceId; // 源代码资源ID

    private @Nullable String sourceCode; // 源码

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
}
