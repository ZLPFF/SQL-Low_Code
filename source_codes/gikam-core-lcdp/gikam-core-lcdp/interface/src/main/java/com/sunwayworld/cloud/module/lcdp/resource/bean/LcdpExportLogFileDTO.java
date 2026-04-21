package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.resourcefile.bean.LcdpResourceFileBean;
import com.sunwayworld.cloud.module.lcdp.scriptblock.bean.LcdpScriptBlockTreeNodeDTO;
import com.sunwayworld.framework.support.domain.AbstractBaseData;

/**
 * 导出文件记录DTO 含文件树  导出日志  文件导入后的文件ID  是否可导入标志
 */
public class LcdpExportLogFileDTO extends AbstractBaseData {


    private static final long serialVersionUID = -5415110072520731263L;

    private List<LcdpResourceTreeNodeDTO> treeNodeDTOList; //文件树

    private String log; //导出日志

    private Long fileId; //文件导入后的文件ID

    private Boolean allowImport; //是否允许导入

    private String checkoutRecordNo;//检出编号

    private LcdpResourceCheckoutConfigDTO resourceCheckoutConfigDTO;

    private List<LcdpResourceFileBean> resourceFileList;


    private List<LcdpScriptBlockTreeNodeDTO> scriptBlockTreeNodeDTOList;

    public List<LcdpResourceTreeNodeDTO> getTreeNodeDTOList() {
        return treeNodeDTOList;
    }


    public void setTreeNodeDTOList(List<LcdpResourceTreeNodeDTO> treeNodeDTOList) {
        this.treeNodeDTOList = treeNodeDTOList;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Boolean getAllowImport() {
        return allowImport;
    }

    public void setAllowImport(Boolean allowImport) {
        this.allowImport = allowImport;
    }

    public String getCheckoutRecordNo() {
        return checkoutRecordNo;
    }

    public void setCheckoutRecordNo(String checkoutRecordNo) {
        this.checkoutRecordNo = checkoutRecordNo;
    }

    public LcdpResourceCheckoutConfigDTO getResourceCheckoutConfigDTO() {
        return resourceCheckoutConfigDTO;
    }

    public void setResourceCheckoutConfigDTO(LcdpResourceCheckoutConfigDTO resourceCheckoutConfigDTO) {
        this.resourceCheckoutConfigDTO = resourceCheckoutConfigDTO;
    }

    public List<LcdpResourceFileBean> getResourceFileList() {
        return resourceFileList;
    }

    public void setResourceFileList(List<LcdpResourceFileBean> resourceFileList) {
        this.resourceFileList = resourceFileList;
    }

    public List<LcdpScriptBlockTreeNodeDTO> getScriptBlockTreeNodeDTOList() {
        return scriptBlockTreeNodeDTOList;
    }

    public void setScriptBlockTreeNodeDTOList(List<LcdpScriptBlockTreeNodeDTO> scriptBlockTreeNodeDTOList) {
        this.scriptBlockTreeNodeDTOList = scriptBlockTreeNodeDTOList;
    }

}
