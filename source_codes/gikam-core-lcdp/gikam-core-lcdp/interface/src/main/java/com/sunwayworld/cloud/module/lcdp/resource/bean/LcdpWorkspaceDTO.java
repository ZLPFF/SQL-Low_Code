package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.util.List;

import com.sunwayworld.framework.data.annotation.Transient;
import com.sunwayworld.framework.support.domain.AbstractBaseData;

/**
 * 工作区ID
 */
public class LcdpWorkspaceDTO extends AbstractBaseData {


    @Transient
    private static final long serialVersionUID = -3761256809560894048L;

    private List<String> moduleIdList; //模块Id集合

    private List<String> pageAndScriptIdList; //页面及组件ID集合

    public List<String> getModuleIdList() {
        return moduleIdList;
    }

    public void setModuleIdList(List<String> moduleIdList) {
        this.moduleIdList = moduleIdList;
    }

    public List<String> getPageAndScriptIdList() {
        return pageAndScriptIdList;
    }

    public void setPageAndScriptIdList(List<String> pageAndScriptIdList) {
        this.pageAndScriptIdList = pageAndScriptIdList;
    }
}
