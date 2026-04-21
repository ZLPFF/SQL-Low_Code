package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.resourcefile.bean.LcdpResourceFileBean;
import com.sunwayworld.framework.support.tree.bean.AbstractTreeNode;

public class LcdpExportTreeDTO extends AbstractTreeNode {

    private static final long serialVersionUID = 7327533379139157640L;

    private List<LcdpResourceTreeNodeDTO> exportTree;

    private List<LcdpResourceFileBean> resourceFileList;

    public List<LcdpResourceTreeNodeDTO> getExportTree() {
        return exportTree;
    }

    public void setExportTree(List<LcdpResourceTreeNodeDTO> exportTree) {
        this.exportTree = exportTree;
    }

    public List<LcdpResourceFileBean> getResourceFileList() {
        return resourceFileList;
    }

    public void setResourceFileList(List<LcdpResourceFileBean> resourceFileList) {
        this.resourceFileList = resourceFileList;
    }
}
