package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.format.annotation.DateTimeFormat;

import com.alibaba.fastjson.annotation.JSONField;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.framework.support.tree.bean.AbstractTreeNode;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;

/**
 * 左侧资源树，左侧资源树每个节点的状态如下：<br>
 * 1、提交的编译报错：<br>
 * <blockquote><pre>
 *    submittedCompiledFail = true
 * </pre></blockquote>
 * 2、自己检出编译未报错：<br>
 * <blockquote><pre>
 *    checkOutFlag = "1"
 *    compiledSuccess = true
 * </pre></blockquote>
 * 3、自己检出编译报错：<br>
 * <blockquote><pre>
 *    checkOutFlag = "1"
 *    compiledSuccess = false
 * </pre></blockquote>
 * 4、其它人检出（只在根节点展示状态）：<br>
 * <blockquote><pre>
 *    otherUserCheckOutFlag = "1"
 * </pre></blockquote>
 * 
 * 右键菜单中按钮是否显示的判断如下：<br>
 * 1、提交：<br>
 * <blockquote><pre>
 *    checkOutFlag = "1"
 * </pre></blockquote>
 * 2、检出：<br>
 * <blockquote><pre>
 *    checkoutable = true
 * </pre></blockquote>
 * 2、撤销检出：<br>
 * <blockquote><pre>
 *    revertable = true
 * </pre></blockquote>
 *
 * @author zhangjr@sunwayworld.com 2024年9月23日
 */
public class LcdpResourceTreeNodeDTO extends AbstractTreeNode  {
    private static final long serialVersionUID = -567548463312442378L;

    private String id; //资源ID

    private String name;//资源名称

    private String desc;//资源描述

    private String type; //资源类型

    private String parentId; // 父Id

    private Long orderNo; // 排序码

    private Long version;//版本

    private String path; //资源路径

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime editTime;// 开始编辑时间

    private String analysisResult;//资源导入时资源分析结果

    private Boolean allowImport; //是否允许导入

    private String checkOutFlag; //检出状态 （T_LCDP_RS_CHECKOUT_RECORD表里是否存在）
    
    private Boolean compiledSuccess; // 编译是否成功

    private Boolean leafFlag; //叶子节点  没有的话 叶子节点为true 有子节点则为false

    @Deprecated
    private String resourceStatus;//资源状态 新增数据:new 有效数据:valid

    private String dataType; //资源数据类型

    private String checkoutUser;//检出人（T_LCDP_RS_CHECKOUT_RECORD表里的checkoutUserId）

    private String otherUserCheckOutFlag; //被其他人检出状态
    
    private String checkoutUserId; // 检出用户编码
    private String checkoutUserName;//检出人名称
    
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkoutTime;//检出时间

    private String resourceTag; //资源标记

    private String createdByName;// 制单人名称
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;// 制单时间

    private Boolean checkoutable = false; // 是否可以检出

    private Boolean submittedCompiledFail = false; // 提交的是否编译失败

    private Boolean revertable = false; // 是否可以撤销检出

    private String convertStatus; // 转换状态：converted/partial/none

    private String convertOutputRoot; // 转换输出目录

    private List<String> convertGeneratedFiles; // 转换生成文件

    private String convertLastUpdatedByName; // 最近转换人

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime convertLastUpdatedTime; // 最近转换时间

    private Integer convertResourceCount; // 已转换资源数

    private Integer convertTotalResourceCount; // 可转换资源总数
    
    public static LcdpResourceTreeNodeDTO of(LcdpResourceBean resource) {
        LcdpResourceTreeNodeDTO instance = new LcdpResourceTreeNodeDTO();

        instance.setId(ObjectUtils.isEmpty(resource.getId())?null:resource.getId().toString());
        instance.setName(resource.getResourceName());
        instance.setDesc(resource.getResourceDesc());
        instance.setType(resource.getResourceCategory());
        instance.setParentId(ObjectUtils.isEmpty(resource.getParentId())?null:resource.getParentId().toString());
        instance.setOrderNo(resource.getOrderNo());
        instance.setVersion(resource.getEffectVersion());
        instance.setEditTime(StringUtils.isEmpty(resource.getExt$Item("edittime"))?null:LocalDateTime.parse(resource.getExt$Item("edittime"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        instance.setPath(resource.getPath());
        instance.setDataType(resource.getDataType());
        instance.setCheckoutUserId(resource.getCheckoutUserId());
        instance.setCheckoutUserName(resource.getCheckoutUserName());
        instance.setCheckoutTime(resource.getCheckoutTime());
        instance.setCreatedByName(resource.getCreatedByName());
        instance.setCreatedTime(resource.getCreatedTime());
        return instance;
    }

    public static LcdpResourceTreeNodeDTO of(LcdpTableBean table) {
        LcdpResourceTreeNodeDTO instance = new LcdpResourceTreeNodeDTO();
        instance.setId(table.getTableName());
        instance.setName(table.getTableName());
        instance.setDesc(table.getTableDesc());
        instance.setOrderNo(1L);
        instance.setVersion(table.getVersion());
        instance.setEditTime(table.getCreatedTime());
        return instance;
    }

    public static LcdpResourceTreeNodeDTO of(LcdpViewBean view) {
        LcdpResourceTreeNodeDTO instance = new LcdpResourceTreeNodeDTO();
        instance.setId(view.getViewName());
        instance.setName(view.getViewName());
        instance.setDesc(view.getViewName());
        instance.setOrderNo(1L);
        instance.setVersion(view.getVersion());
        instance.setEditTime(view.getCreatedTime());
        return instance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Long getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Long orderNo) {
        this.orderNo = orderNo;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getEditTime() {
        return editTime;
    }

    public void setEditTime(LocalDateTime editTime) {
        this.editTime = editTime;
    }

    public String getAnalysisResult() {
        return analysisResult;
    }

    public void setAnalysisResult(String analysisResult) {
        this.analysisResult = analysisResult;
    }

    public Boolean getAllowImport() {
        return allowImport;
    }

    public void setAllowImport(Boolean allowImport) {
        this.allowImport = allowImport;
    }

    public String getCheckOutFlag() {
        return checkOutFlag;
    }

    public void setCheckOutFlag(String checkOutFlag) {
        this.checkOutFlag = checkOutFlag;
    }

    public Boolean getLeafFlag() {
        return leafFlag;
    }

    public void setLeafFlag(Boolean leafFlag) {
        this.leafFlag = leafFlag;
    }

    public String getResourceStatus() {
        return resourceStatus;
    }

    public void setResourceStatus(String resourceStatus) {
        this.resourceStatus = resourceStatus;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getCheckoutUser() {
        return checkoutUser;
    }

    public void setCheckoutUser(String checkoutUser) {
        this.checkoutUser = checkoutUser;
    }

    public String getOtherUserCheckOutFlag() {
        return otherUserCheckOutFlag;
    }

    public void setOtherUserCheckOutFlag(String otherUserCheckOutFlag) {
        this.otherUserCheckOutFlag = otherUserCheckOutFlag;
    }

    public LocalDateTime getCheckoutTime() {
        return checkoutTime;
    }

    public void setCheckoutTime(LocalDateTime checkoutTime) {
        this.checkoutTime = checkoutTime;
    }

    public String getResourceTag() {
        return resourceTag;
    }

    public void setResourceTag(String resourceTag) {
        this.resourceTag = resourceTag;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public Boolean getCompiledSuccess() {
        return compiledSuccess;
    }

    public void setCompiledSuccess(Boolean compiledSuccess) {
        this.compiledSuccess = compiledSuccess;
    }

    public Boolean getSubmittedCompiledFail() {
        return submittedCompiledFail;
    }

    public void setSubmittedCompiledFail(Boolean submittedCompiledFail) {
        this.submittedCompiledFail = submittedCompiledFail;
    }

    public Boolean getRevertable() {
        return revertable;
    }

    public void setRevertable(Boolean revertable) {
        this.revertable = revertable;
    }

    public Boolean getCheckoutable() {
        return checkoutable;
    }

    public void setCheckoutable(Boolean checkoutable) {
        this.checkoutable = checkoutable;
    }

    public String getCheckoutUserId() {
        return checkoutUserId;
    }

    public void setCheckoutUserId(String checkoutUserId) {
        this.checkoutUserId = checkoutUserId;
    }

    public String getCheckoutUserName() {
        return checkoutUserName;
    }

    public void setCheckoutUserName(String checkoutUserName) {
        this.checkoutUserName = checkoutUserName;
    }

    public String getConvertStatus() {
        return convertStatus;
    }

    public void setConvertStatus(String convertStatus) {
        this.convertStatus = convertStatus;
    }

    public String getConvertOutputRoot() {
        return convertOutputRoot;
    }

    public void setConvertOutputRoot(String convertOutputRoot) {
        this.convertOutputRoot = convertOutputRoot;
    }

    public List<String> getConvertGeneratedFiles() {
        return convertGeneratedFiles;
    }

    public void setConvertGeneratedFiles(List<String> convertGeneratedFiles) {
        this.convertGeneratedFiles = convertGeneratedFiles;
    }

    public String getConvertLastUpdatedByName() {
        return convertLastUpdatedByName;
    }

    public void setConvertLastUpdatedByName(String convertLastUpdatedByName) {
        this.convertLastUpdatedByName = convertLastUpdatedByName;
    }

    public LocalDateTime getConvertLastUpdatedTime() {
        return convertLastUpdatedTime;
    }

    public void setConvertLastUpdatedTime(LocalDateTime convertLastUpdatedTime) {
        this.convertLastUpdatedTime = convertLastUpdatedTime;
    }

    public Integer getConvertResourceCount() {
        return convertResourceCount;
    }

    public void setConvertResourceCount(Integer convertResourceCount) {
        this.convertResourceCount = convertResourceCount;
    }

    public Integer getConvertTotalResourceCount() {
        return convertTotalResourceCount;
    }

    public void setConvertTotalResourceCount(Integer convertTotalResourceCount) {
        this.convertTotalResourceCount = convertTotalResourceCount;
    }
}
