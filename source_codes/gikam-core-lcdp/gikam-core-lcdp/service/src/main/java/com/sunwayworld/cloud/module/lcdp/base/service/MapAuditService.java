package com.sunwayworld.cloud.module.lcdp.base.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.sunwayworld.cloud.module.lcdp.base.LcdpAuditableDTO;
import com.sunwayworld.framework.support.auditable.bean.CoreBpmnParameterDTO;
import com.sunwayworld.framework.utils.ArrayUtils;
import com.sunwayworld.module.sys.bpmn.bean.CoreBpmnInstanceStatusDTO;

public interface MapAuditService {
    /**
     * 获取执行工作流的数据
     */
    <ID> List<LcdpAuditableDTO> getAuditableItemList(String table, List<ID> idList);
    
    /**
     * 获取执行工作流的数据
     */
    default <ID> LcdpAuditableDTO getAuditableItem(String table, ID id) {
        List<LcdpAuditableDTO> auditableList = getAuditableItemList(table, ArrayUtils.asList(id));
        
        if (auditableList.isEmpty()) {
            return null;
        }
        
        return auditableList.get(0);
    }
    
    /**
     * 工作流启动多条
     */
    List<CoreBpmnInstanceStatusDTO<String>> startProcess(String table, List<LcdpAuditableDTO> auditableItemList, CoreBpmnParameterDTO parameter);
    
    /**
     * 工作流启动单条
     */
    default CoreBpmnInstanceStatusDTO<String> startProcess(String table, LcdpAuditableDTO auditableItem, CoreBpmnParameterDTO parameter) {
        List<CoreBpmnInstanceStatusDTO<String>> statusList = startProcess(table, ArrayUtils.asList(auditableItem), parameter);
        
        if (statusList.isEmpty()) {
            return null;
        }
        
        return statusList.get(0);
    }
    
    /**
     * 工作流审核多条
     */
    List<CoreBpmnInstanceStatusDTO<String>> completeTask(String table, List<LcdpAuditableDTO> auditableItemList, CoreBpmnParameterDTO parameter);
    
    /**
     * 工作流审核单条
     */
    default CoreBpmnInstanceStatusDTO<String> completeTask(String table, LcdpAuditableDTO auditableItem, CoreBpmnParameterDTO parameter) {
        List<CoreBpmnInstanceStatusDTO<String>> statusList = completeTask(table, ArrayUtils.asList(auditableItem), parameter);
        
        if (statusList.isEmpty()) {
            return null;
        }
        
        return statusList.get(0);
    }
    
    /**
     * 工作流拒绝多条
     */
    List<CoreBpmnInstanceStatusDTO<String>> rejectTask(String table, List<LcdpAuditableDTO> auditableItemList, CoreBpmnParameterDTO parameter);
    
    /**
     * 工作流审核单条
     */
    default CoreBpmnInstanceStatusDTO<String> rejectTask(String table, LcdpAuditableDTO auditableItem, CoreBpmnParameterDTO parameter) {
        List<CoreBpmnInstanceStatusDTO<String>> statusList = rejectTask(table, ArrayUtils.asList(auditableItem), parameter);
        
        if (statusList.isEmpty()) {
            return null;
        }
        
        return statusList.get(0);
    }
    
    /**
     * 工作流撤回
     */
    List<CoreBpmnInstanceStatusDTO<String>> withdrawProcess(String table, List<LcdpAuditableDTO> auditableItemList, CoreBpmnParameterDTO parameter);
    
    /**
     * 工作流任务多个节点状态
     */
    Map<String, Object> selectTaskStatus(String table, List<LcdpAuditableDTO> auditableItemList, CoreBpmnParameterDTO parameter);
    
    /**
     * 工作流任务单个节点状态
     */
    default Map<String, Object> selectTaskStatus(String table, LcdpAuditableDTO auditableItem, CoreBpmnParameterDTO parameter) {
        return selectTaskStatus(table, Arrays.asList(auditableItem), parameter);
    }
}
