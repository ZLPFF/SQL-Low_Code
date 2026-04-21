package com.sunwayworld.cloud.module.lcdp.resource.bean;

import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractBaseData;

/* Tree查询叶子节点数据辅助DTO
* */
public class LcdpTreeAuxiliaryDTO extends AbstractBaseData {

    @Transient
    private static final long serialVersionUID = 5418080377234894147L;

    private Long parentId; //父ID

    private Long count; //子节点数量

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
