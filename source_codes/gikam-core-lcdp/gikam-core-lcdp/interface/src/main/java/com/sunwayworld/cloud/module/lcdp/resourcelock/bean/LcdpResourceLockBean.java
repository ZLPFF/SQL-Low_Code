package com.sunwayworld.cloud.module.lcdp.resourcelock.bean;

import com.sunwayworld.framework.data.annotation.Id;
import com.sunwayworld.framework.data.annotation.Table;
import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.Persistable;
import com.sunwayworld.framework.support.domain.AbstractPersistable;

/**
 * 低代码平台资源锁定
 * 
 * @author liuxia@@sunwayworld.com
 * @date 2022-10-20
 */
@Table("T_LCDP_RESOURCE_LOCK")
public class LcdpResourceLockBean extends AbstractPersistable<Long> implements Persistable<Long> {

    @Transient
    private static final long serialVersionUID = -2727941379230207121L;

    @Id
    private Long id;// 主键
    private String resourceId;// 资源ID
    private String resourceCategory;// 资源类型
    private String lockUserId;// 锁定人员

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getLockUserId() {
        return lockUserId;
    }

    public void setLockUserId(String lockUserId) {
        this.lockUserId = lockUserId;
    }

}
