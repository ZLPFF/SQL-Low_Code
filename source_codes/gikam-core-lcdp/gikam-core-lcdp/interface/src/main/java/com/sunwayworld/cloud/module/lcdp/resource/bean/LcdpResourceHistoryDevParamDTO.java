package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.io.Serializable;

import org.springframework.lang.Nullable;

public class LcdpResourceHistoryDevParamDTO implements Serializable {
    private static final long serialVersionUID = -7307073887757654689L;
    
    private String resourceName;
    private @Nullable String path;
    
    public String getResourceName() {
        return resourceName;
    }
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
}
