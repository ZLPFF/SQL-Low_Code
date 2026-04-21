package com.sunwayworld.cloud.module.lcdp.message.sync.bean;

import java.io.Serializable;

import com.sunwayworld.framework.context.ApplicationContextHelper;

public class LcdpResourceSyncDTO implements Serializable {
    private static final long serialVersionUID = 8853730318228064468L;

    private String appId; // 触发的系统appId
    private Long resourceId; // 资源ID
    
    public static LcdpResourceSyncDTO of(Long resourceId) {
        LcdpResourceSyncDTO dto = new LcdpResourceSyncDTO();
        dto.setAppId(ApplicationContextHelper.getAppId());
        dto.setResourceId(resourceId);
        
        return dto;
    }
    
    public String getAppId() {
        return appId;
    }
    public void setAppId(String appId) {
        this.appId = appId;
    }
    public Long getResourceId() {
        return resourceId;
    }
    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
}
