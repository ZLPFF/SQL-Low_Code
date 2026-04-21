package com.sunwayworld.cloud.module.lcdp.resource.bean;

import java.io.Serializable;

/**
 * 对应Grid的配置
 *
 * @author zhangjr@sunwayworld.com 2024年9月15日
 */
public class LcdpPageGridConfigDTO implements Serializable {
    private static final long serialVersionUID = -2946195626039480089L;
    
    private String functionSuffixId; // 功能名称的后缀
    private String service; // 数据服务
    private String table; // 绑定库表
    private String sql; // 查询SQL
    private Boolean autoSave; // 是否自动保存
    private String idAutoGen; // ID是否自动生成（1-自动 0-手动）
    private String idGenSequence; // ID生成的序列
    private String selectMethod; // 查询功能
    private String insertMethod; // 新增功能
    private String deleteMethod; // 删除功能
    private String updateMethod; // 更新功能
    private String activateMethod; // 启用功能
    private String deactivateMethod; // 停用功能
    private String startProcessMethod; // 流程提交功能
    private String withdrawProcessMethod; // 流程撤回功能
    private String completeTaskMethod; // 审核通过功能
    private String rejectTaskMethod; // 审核拒绝功能
    
    private boolean updateMapper = false; // 是否需要更新mapper文件
    private boolean updateTable = false; // 是否需要更新获取table方法
    private boolean updateSetIdMethod = false; // 是否更新setId方法
    
    public String getFunctionSuffixId() {
        return functionSuffixId;
    }
    public void setFunctionSuffixId(String functionSuffixId) {
        this.functionSuffixId = functionSuffixId;
    }
    public String getService() {
        return service;
    }
    public void setService(String service) {
        this.service = service;
    }
    public String getTable() {
        return table;
    }
    public void setTable(String table) {
        this.table = table;
    }
    public String getSql() {
        return sql;
    }
    public void setSql(String sql) {
        this.sql = sql;
    }
    public Boolean getAutoSave() {
        return autoSave;
    }
    public void setAutoSave(Boolean autoSave) {
        this.autoSave = autoSave;
    }
    public String getIdAutoGen() {
        return idAutoGen;
    }
    public void setIdAutoGen(String idAutoGen) {
        this.idAutoGen = idAutoGen;
    }
    public String getIdGenSequence() {
        return idGenSequence;
    }
    public void setIdGenSequence(String idGenSequence) {
        this.idGenSequence = idGenSequence;
    }
    public String getSelectMethod() {
        return selectMethod;
    }
    public void setSelectMethod(String selectMethod) {
        this.selectMethod = selectMethod;
    }
    public String getInsertMethod() {
        return insertMethod;
    }
    public void setInsertMethod(String insertMethod) {
        this.insertMethod = insertMethod;
    }
    public String getDeleteMethod() {
        return deleteMethod;
    }
    public void setDeleteMethod(String deleteMethod) {
        this.deleteMethod = deleteMethod;
    }
    public String getUpdateMethod() {
        return updateMethod;
    }
    public void setUpdateMethod(String updateMethod) {
        this.updateMethod = updateMethod;
    }
    public String getActivateMethod() {
        return activateMethod;
    }
    public void setActivateMethod(String activateMethod) {
        this.activateMethod = activateMethod;
    }
    public String getDeactivateMethod() {
        return deactivateMethod;
    }
    public void setDeactivateMethod(String deactivateMethod) {
        this.deactivateMethod = deactivateMethod;
    }
    public String getStartProcessMethod() {
        return startProcessMethod;
    }
    public void setStartProcessMethod(String startProcessMethod) {
        this.startProcessMethod = startProcessMethod;
    }
    public String getWithdrawProcessMethod() {
        return withdrawProcessMethod;
    }
    public void setWithdrawProcessMethod(String withdrawProcessMethod) {
        this.withdrawProcessMethod = withdrawProcessMethod;
    }
    public String getCompleteTaskMethod() {
        return completeTaskMethod;
    }
    public void setCompleteTaskMethod(String completeTaskMethod) {
        this.completeTaskMethod = completeTaskMethod;
    }
    public String getRejectTaskMethod() {
        return rejectTaskMethod;
    }
    public void setRejectTaskMethod(String rejectTaskMethod) {
        this.rejectTaskMethod = rejectTaskMethod;
    }
    public boolean isUpdateMapper() {
        return updateMapper;
    }
    public void setUpdateMapper(boolean updateMapper) {
        this.updateMapper = updateMapper;
    }
    public boolean isUpdateTable() {
        return updateTable;
    }
    public void setUpdateTable(boolean updateTable) {
        this.updateTable = updateTable;
    }
    public boolean isUpdateSetIdMethod() {
        return updateSetIdMethod;
    }
    public void setUpdateSetIdMethod(boolean updateSetIdMethod) {
        this.updateSetIdMethod = updateSetIdMethod;
    }
}
