package com.sunwayworld.cloud.module.lcdp.table.bean;

import com.sunwayworld.framework.support.tree.bean.AbstractTreeNode;

/**
 * @author yangsz@sunway.com 2022-10-19
 */
public class LcdpTableTreeNodeDTO extends AbstractTreeNode {

    private static final long serialVersionUID = -8648024492068214686L;

    private String id;

    private String name;

    private String type;

    private String checkOutFlag; //当前用户检出状态 1是0否

    private String resourceStatus;//资源状态 新增数据:new 有效数据:valid

    private String otherUserCheckOutFlag;//其他用户检出标志 1是0否

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCheckOutFlag() {
        return checkOutFlag;
    }

    public void setCheckOutFlag(String checkOutFlag) {
        this.checkOutFlag = checkOutFlag;
    }

    public String getResourceStatus() {
        return resourceStatus;
    }

    public void setResourceStatus(String resourceStatus) {
        this.resourceStatus = resourceStatus;
    }

    public String getOtherUserCheckOutFlag() {
        return otherUserCheckOutFlag;
    }

    public void setOtherUserCheckOutFlag(String otherUserCheckOutFlag) {
        this.otherUserCheckOutFlag = otherUserCheckOutFlag;
    }
}
