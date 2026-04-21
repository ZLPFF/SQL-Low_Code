package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.io.Serializable;

/**
 * 资源接收的DTO
 */
public class LcdpResourceInDTO implements Serializable {
    private static final long serialVersionUID = -2583511153184402757L;
    
    private String resourceId; // 资源ID、库表名、视图名
    private String resourceCategory; // 资源类型

    public static LcdpResourceInDTO  of(String resourceId,String resourceCategory) {
        LcdpResourceInDTO resource = new LcdpResourceInDTO();
        resource.setResourceId(resourceId);
        resource.setResourceCategory(resourceCategory);
        return resource;
    }
    
    public String getResourceId() {
        return resourceId;
    }
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    public String getResourceCategory() {
        return resourceCategory;
    }
    public void setResourceCategory(String resourceCategory) {
        this.resourceCategory = resourceCategory;
    }
}
