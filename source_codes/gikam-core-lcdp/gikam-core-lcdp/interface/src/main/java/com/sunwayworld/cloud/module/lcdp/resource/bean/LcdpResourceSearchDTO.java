package com.sunwayworld.cloud.module.lcdp.resource.bean;

import com.sunwayworld.framework.support.domain.AbstractBaseData;

/**
 * 检索的查询结果
 */
public class LcdpResourceSearchDTO extends AbstractBaseData {
    private static final long serialVersionUID = -4171103260730820245L;
    
    private String id;// 历史资源或页面组件的主键
    private Long resourceId; // 资源主键
    private String resourceName;// 资源名称
    private String resourceCategory; // 资源类型
    private String path;// 资源路径
    private Long version;// 版本
    
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public Long getResourceId() {
        return resourceId;
    }
    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
    public String getResourceName() {
        return resourceName;
    }
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    public String getResourceCategory() {
        return resourceCategory;
    }
    public void setResourceCategory(String resourceCategory) {
        this.resourceCategory = resourceCategory;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public Long getVersion() {
        return version;
    }
    public void setVersion(Long version) {
        this.version = version;
    }
}
