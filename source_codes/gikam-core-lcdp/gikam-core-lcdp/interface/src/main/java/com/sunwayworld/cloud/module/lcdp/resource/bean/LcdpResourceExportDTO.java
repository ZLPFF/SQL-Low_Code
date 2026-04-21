package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.resourcefile.bean.LcdpResourceFileBean;
import com.sunwayworld.cloud.module.lcdp.scriptblock.bean.LcdpScriptBlockBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.framework.support.domain.AbstractBaseData;
import com.sunwayworld.module.sys.code.bean.CoreCodeBean;
import com.sunwayworld.module.sys.code.bean.CoreCodeCategoryBean;

/*
 * */
public class LcdpResourceExportDTO extends AbstractBaseData {


    private static final long serialVersionUID = 4041717488401902182L;

    //导出日志
    private String exportLog;

    //导出资源树
    List<LcdpResourceTreeNodeDTO> treeNodeDtoList;

    //导出资源集合
    private List<LcdpResourceBean> exportResourceList;

    //导出表集合
    private List<LcdpTableDTO> tableDTOList;

    //导出视图集合
    private List<LcdpViewBean> viewList;

    //导出系统编码分类集合
    private List<CoreCodeCategoryBean> coreCodeCategoryList;

    //导出系统编码集合
    private List<CoreCodeBean> coreCodeList;

    private List<LcdpResourceBean> deleteResourceList;

    private List<LcdpResourceFileBean> lcdpResourceFileList;

    //导出代码块集合
    private List<LcdpScriptBlockBean> lcdpScriptBlockList;

    public String getExportLog() {
        return exportLog;
    }

    public void setExportLog(String exportLog) {
        this.exportLog = exportLog;
    }

    public List<LcdpResourceTreeNodeDTO> getTreeNodeDtoList() {
        return treeNodeDtoList;
    }

    public void setTreeNodeDtoList(List<LcdpResourceTreeNodeDTO> treeNodeDtoList) {
        this.treeNodeDtoList = treeNodeDtoList;
    }

    public List<LcdpResourceBean> getExportResourceList() {
        return exportResourceList;
    }

    public void setExportResourceList(List<LcdpResourceBean> exportResourceList) {
        this.exportResourceList = exportResourceList;
    }

    public List<LcdpTableDTO> getTableDTOList() {
        return tableDTOList;
    }

    public void setTableDTOList(List<LcdpTableDTO> tableDTOList) {
        this.tableDTOList = tableDTOList;
    }

    public List<LcdpViewBean> getViewList() {
        return viewList;
    }

    public void setViewList(List<LcdpViewBean> viewList) {
        this.viewList = viewList;
    }

    public List<CoreCodeCategoryBean> getCoreCodeCategoryList() {
        return coreCodeCategoryList;
    }

    public void setCoreCodeCategoryList(List<CoreCodeCategoryBean> coreCodeCategoryList) {
        this.coreCodeCategoryList = coreCodeCategoryList;
    }

    public List<CoreCodeBean> getCoreCodeList() {
        return coreCodeList;
    }

    public void setCoreCodeList(List<CoreCodeBean> coreCodeList) {
        this.coreCodeList = coreCodeList;
    }


    public List<LcdpResourceBean> getDeleteResourceList() {
        return deleteResourceList;
    }

    public void setDeleteResourceList(List<LcdpResourceBean> deleteResourceList) {
        this.deleteResourceList = deleteResourceList;
    }

    public List<LcdpResourceFileBean> getLcdpResourceFileList() {
        return lcdpResourceFileList;
    }

    public void setLcdpResourceFileList(List<LcdpResourceFileBean> lcdpResourceFileList) {
        this.lcdpResourceFileList = lcdpResourceFileList;
    }


    public List<LcdpScriptBlockBean> getLcdpScriptBlockList() {
        return lcdpScriptBlockList;
    }

    public void setLcdpScriptBlockList(List<LcdpScriptBlockBean> lcdpScriptBlockList) {
        this.lcdpScriptBlockList = lcdpScriptBlockList;
    }

}
