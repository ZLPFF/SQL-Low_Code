package com.sunwayworld.cloud.module.lcdp.base.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sunwayworld.cloud.module.lcdp.base.annotation.Mapping;
import com.sunwayworld.cloud.module.lcdp.base.annotation.MappingType;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpWrapperParseUtils;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.data.page.Pagination;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.auditable.bean.CoreBpmnTaskStatusDTO;
import com.sunwayworld.framework.support.base.bean.CoreServiceStatusDTO;
import com.sunwayworld.framework.support.base.dao.MapDao;
import com.sunwayworld.framework.utils.BeanUtils;
import com.sunwayworld.framework.utils.NumberUtils;
import com.sunwayworld.framework.utils.ReflectionUtils;
import com.sunwayworld.framework.utils.ServletUtils;
import com.sunwayworld.framework.utils.SpringUtils;
import com.sunwayworld.module.sys.bpmn.bean.CoreBpmnInstanceStatusDTO;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.transaction.annotation.Transactional;

/**
 * 核心ervice逻辑反射调用
 */
public interface GikamBaseService {

    default Object getService() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Mapping(value = "核心分页查询数据", type = MappingType.SELECT)
    default Page<Map<String, Object>> selectPagination(RestJsonWrapperBean wrapper) {
        Page<T> page = (Page<T>) SpringUtils.invoke(getService(), "selectPagination", wrapper);
        return this.toMapPage(page);
    }

    @SuppressWarnings("unchecked")
    @Mapping(value = "核心编制页面查询数据", type = MappingType.SELECT)
    default Page<Map<String, Object>> selectRawPagination(RestJsonWrapperBean wrapper) {
        Page<T> page = (Page<T>) SpringUtils.invoke(getService(), "selectRawPagination", wrapper);
        return this.toMapPage(page);
    }

    @SuppressWarnings("unchecked")
    @Mapping(value = "核心查询页面查询数据", type = MappingType.SELECT)
    default Page<Map<String, Object>> selectSearchablePagination(RestJsonWrapperBean wrapper) {
        Page<T> page = (Page<T>) SpringUtils.invoke(getService(), "selectSearchablePagination", wrapper);
        return this.toMapPage(page);
    }


    @Mapping(value = "获取状态信息", type = MappingType.SELECT)
    default CoreServiceStatusDTO selectStatus(RestJsonWrapperBean wrapper) {
        String id = wrapper.getParamValue("id");

        String currentStatusCode = ServletUtils.getCurrentRequestParamMap().get("currentStatusCode");

        return (CoreServiceStatusDTO) SpringUtils.invoke(getService(), "selectStatus", id, currentStatusCode);
    }


    @Mapping(value = "核心删除数据", type = MappingType.DELETE)
    @Transactional
    @Audit(AuditConstant.DELETE)
    default void delete(RestJsonWrapperBean wrapper) {
        SpringUtils.invoke(getService(), "delete", wrapper);
    }

    @Mapping(value = "核心新增数据", type = MappingType.INSERT)
    @Transactional
    @Audit(AuditConstant.INSERT)
    default Object insert(RestJsonWrapperBean wrapper) {
        return SpringUtils.invoke(getService(), "insert", wrapper);
    }

    @Mapping(value = "核心自动保存数据", type = MappingType.INSERT)
    @Transactional
    @Audit(AuditConstant.INSTANT_SAVE)
    default void instantSave(RestJsonWrapperBean wrapper) {
        SpringUtils.invoke(getService(), "instantSave", wrapper);
    }

    @SuppressWarnings("unchecked")
    @Mapping(value = "核心分页查询数据（工作流）", type = MappingType.SELECT)
    default Page<Map<String, Object>> selectAuditablePagination(RestJsonWrapperBean wrapper) {
        Page<T> page = (Page<T>) SpringUtils.invoke(getService(), "selectAuditablePagination", wrapper);
        return this.toMapPage(page);
    }

    @SuppressWarnings("unchecked")
    @Mapping(value = "核心撤回页查询", type = MappingType.SELECT)
    default Page<Map<String, Object>> selectWithdrawablePagination(RestJsonWrapperBean wrapper) {
        Page<T> page = (Page<T>) SpringUtils.invoke(getService(), "selectWithdrawablePagination", wrapper);
        return this.toMapPage(page);
    }

    @SuppressWarnings("unchecked")
    @Mapping(value = "回退页查询", type = MappingType.SELECT)
    default Page<Map<String, Object>> selectUndoablePagination(RestJsonWrapperBean wrapper) {
        Page<T> page = (Page<T>) SpringUtils.invoke(getService(), "selectUndoablePagination", wrapper);
        return this.toMapPage(page);
    }

    @SuppressWarnings("unchecked")
    @Mapping(value = "核心工作流启动", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_START)
    default List<CoreBpmnInstanceStatusDTO<String>> startProcess(RestJsonWrapperBean wrapper) {
        return (List<CoreBpmnInstanceStatusDTO<String>>) SpringUtils.invoke(getService(), "startProcess", wrapper);
    }

    @Mapping(value = "核心工作流启动（进度监控)", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_START)
    default String startProcessWithProgress(RestJsonWrapperBean wrapper) {
        return (String) SpringUtils.invoke(getService(), "startProcessWithProgress", wrapper);
    }

    @SuppressWarnings("unchecked")
    @Mapping(value = "核心工作流撤回", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_WITHDRAW)
    default List<CoreBpmnInstanceStatusDTO<String>> withdrawProcess(RestJsonWrapperBean wrapper) {
        return (List<CoreBpmnInstanceStatusDTO<String>>) SpringUtils.invoke(getService(), "withdrawProcess", wrapper);
    }

    @Mapping(value = "核心工作流撤回（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_WITHDRAW)
    default String withdrawProcessWithProgress(RestJsonWrapperBean wrapper) {
        return (String) SpringUtils.invoke(getService(), "withdrawProcessWithProgress", wrapper);
    }

    @SuppressWarnings("unchecked")
    @Mapping(value = "核心工作流审核通过", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_COMPLETE)
    default List<CoreBpmnInstanceStatusDTO<String>> completeTask(RestJsonWrapperBean wrapper) {
        return (List<CoreBpmnInstanceStatusDTO<String>>) SpringUtils.invoke(getService(), "completeTask", wrapper);
    }

    @Mapping(value = "核心工作流审核通过（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_COMPLETE)
    default String completeTaskWithProgress(RestJsonWrapperBean wrapper) {
        return (String) SpringUtils.invoke(getService(), "completeTaskWithProgress", wrapper);
    }

    @Mapping(value = "核心工作流转办", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_TRANSFER)
    default List<CoreBpmnInstanceStatusDTO<String>> transferTask(RestJsonWrapperBean wrapper) {
        return (List<CoreBpmnInstanceStatusDTO<String>>) SpringUtils.invoke(getService(), "transferTask", wrapper);
    }

    @Mapping(value = "核心工作流转办（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_TRANSFER)
    default String transferTaskWithProgress(RestJsonWrapperBean wrapper) {
        return (String) SpringUtils.invoke(getService(), "transferTaskWithProgress", wrapper);
    }

    @SuppressWarnings("unchecked")
    @Mapping(value = "核心工作流结束", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_END)
    default List<CoreBpmnInstanceStatusDTO<String>> endTask(RestJsonWrapperBean wrapper) {
        return (List<CoreBpmnInstanceStatusDTO<String>>) SpringUtils.invoke(getService(), "endTask", wrapper);
    }

    @Mapping(value = "核心工作流结束（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_END)
    default String endTaskWithProgress(RestJsonWrapperBean wrapper) {
        return (String) SpringUtils.invoke(getService(), "endTaskWithProgress", wrapper);
    }

    @SuppressWarnings("unchecked")
    @Mapping(value = "核心工作流审核拒绝", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_REJECT)
    default List<CoreBpmnInstanceStatusDTO<String>> rejectTask(RestJsonWrapperBean wrapper) {
        return (List<CoreBpmnInstanceStatusDTO<String>>) SpringUtils.invoke(getService(), "rejectTask", wrapper);
    }

    @Mapping(value = "核心工作流审核拒绝（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_REJECT)
    default String rejectTaskWithProgress(RestJsonWrapperBean wrapper) {
        return (String) SpringUtils.invoke(getService(), "rejectTaskWithProgress", wrapper);
    }

    @SuppressWarnings("unchecked")
    @Mapping(value = "核心工作流异常拒绝", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_ODD_REJECT)
    default List<CoreBpmnInstanceStatusDTO<String>> oddRejectTask(RestJsonWrapperBean wrapper) {
        return (List<CoreBpmnInstanceStatusDTO<String>>) SpringUtils.invoke(getService(), "oddRejectTask", wrapper);
    }

    @Mapping(value = "核心工作流异常拒绝（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_ODD_REJECT)
    default String oddRejectTaskWithProgress(RestJsonWrapperBean wrapper) {
        return (String) SpringUtils.invoke(getService(), "oddRejectTaskWithProgress", wrapper);
    }

    @SuppressWarnings("unchecked")
    @Mapping(value = "核心工作流回退", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_UNDO)
    default List<CoreBpmnInstanceStatusDTO<String>> undo(RestJsonWrapperBean wrapper) {
        return (List<CoreBpmnInstanceStatusDTO<String>>) SpringUtils.invoke(getService(), "undo", wrapper);
    }

    @Mapping(value = "核心工作流回退（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_UNDO)
    default String undoWithProgress(RestJsonWrapperBean wrapper) {
        return (String) SpringUtils.invoke(getService(), "undoWithProgress", wrapper);
    }

    @Mapping(value = "核心获取工作流任务节点的状态", type = MappingType.SELECT)
    default Map<String, Object> selectBpmnTaskStatus(RestJsonWrapperBean wrapper) {
        CoreBpmnTaskStatusDTO bpmnTaskStatusDTO = (CoreBpmnTaskStatusDTO) SpringUtils.invoke(getService(), "selectBpmnTaskStatus", wrapper);
        return this.toMap(bpmnTaskStatusDTO);
    }


    @Mapping(value = "根据id和字段名查询字段值", type = MappingType.SELECT)
    default String selectColumnById(RestJsonWrapperBean wrapper) {
        String id = wrapper.getParamValue("id");
        String column = wrapper.getParamValue("column");

        return (String) SpringUtils.invoke(getService(), "selectColumnById", (NumberUtils.isNumber(id) ? Long.parseLong(id) : id), column);
    }

    @Mapping(value = "核心选择页查询", type = MappingType.SELECT)
    default Page<Map<String, Object>> selectChoosablePagination(RestJsonWrapperBean wrapper) {
        Page<?> page = (Page<?>) SpringUtils.invoke(getService(), "selectChoosablePagination", wrapper);
        return this.toMapPage(page);
    }


    //---------------------------------------------------------------
    // 工具方法
    //---------------------------------------------------------------
    @SuppressWarnings("unchecked")
    default Map<String, Object> toMap(Object bean) {
        if (bean == null) {
            return new HashMap<>();
        }

        if (Map.class.isAssignableFrom(bean.getClass())) {
            return (Map<String, Object>) bean;
        }

        Map<String, Object> map = BeanUtils.toMap(bean);
        //ext$数据特殊处理
        Method getExt$Method = ReflectionUtils.getUniqueMethodByName(bean.getClass(), "getExt$");
        if (getExt$Method != null) {
            Map<String, String> ext$Map = (Map<String, String>) ReflectionUtils.invokeMethod(getExt$Method, bean);
            ext$Map.forEach((k, v) -> map.put(k, v));

        }

        //树形结构数据特殊处理
        Method getChildrenMethod = ReflectionUtils.getUniqueMethodByName(bean.getClass(), "getChildren");
        if (getChildrenMethod != null) {
            List<Object> childrenList = (List<Object>) ReflectionUtils.invokeMethod(getChildrenMethod, bean);

            if (childrenList != null && childrenList.size() > 0) {
                List<Map<String, Object>> childrenMapList = this.toMapList(childrenList);
                map.put("children", childrenMapList);
            }
        }

        MapDao.correctMap(map);

        return LcdpWrapperParseUtils.lowerCaseKey(map);
    }

    default List<Map<String, Object>> toMapList(List<?> beanList) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (beanList == null) {
            return mapList;
        }

        for (Object bean : beanList) {
            mapList.add(this.toMap(bean));
        }
        return mapList;
    }

    default Page<Map<String, Object>> toMapPage(Page<?> beanPage) {
        if (beanPage == null) {
            return new Pagination<>();
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<?> dataList = beanPage.getRows();
        if (!dataList.isEmpty()) {
            for (Object bean : dataList) {
                mapList.add(this.toMap(bean));
            }
        }

        return new Pagination<>(beanPage.getTotal(), beanPage.getPageSize(), beanPage.getPageNumber(), mapList);
    }

    default void dealMapData(Map<String, Object> map) {
        MapDao.correctMap(map);
    }

}