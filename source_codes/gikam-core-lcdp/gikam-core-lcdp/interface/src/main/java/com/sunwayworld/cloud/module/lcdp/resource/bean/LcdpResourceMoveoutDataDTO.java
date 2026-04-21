package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.io.Serializable;
import java.util.List;

import com.sunwayworld.cloud.module.lcdp.resourcefile.bean.LcdpResourceFileBean;

/**
 * 导出的数据
 * 
 *  @author zhangjr@sunwayworld.com 2024年10月30日
 */
public class LcdpResourceMoveoutDataDTO implements Serializable {
    private static final long serialVersionUID = 4054215100192314155L;

    private List<LcdpResourceTreeNodeDTO> resourceTree;

    private List<LcdpResourceFileBean> fileList;

    public List<LcdpResourceTreeNodeDTO> getResourceTree() {
        return resourceTree;
    }
    public void setResourceTree(List<LcdpResourceTreeNodeDTO> resourceTree) {
        this.resourceTree = resourceTree;
    }
    public List<LcdpResourceFileBean> getFileList() {
        return fileList;
    }
    public void setFileList(List<LcdpResourceFileBean> fileList) {
        this.fileList = fileList;
    }
}
