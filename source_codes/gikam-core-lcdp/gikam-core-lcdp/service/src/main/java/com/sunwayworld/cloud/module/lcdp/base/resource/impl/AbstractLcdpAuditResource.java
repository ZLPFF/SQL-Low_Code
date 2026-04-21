package com.sunwayworld.cloud.module.lcdp.base.resource.impl;

import java.util.List;
import java.util.Map;

import com.sunwayworld.cloud.module.lcdp.base.resource.LcdpAuditResource;
import com.sunwayworld.cloud.module.lcdp.base.service.LcdpAuditService;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.bean.CoreServiceStatusDTO;
import com.sunwayworld.module.sys.bpmn.bean.CoreBpmnInstanceStatusDTO;

public interface AbstractLcdpAuditResource<S extends LcdpAuditService> extends AbstractLcdpBaseResource<S>, LcdpAuditResource<S> {
    @Override
    default Page<Map<String, Object>> selectRawPagination(RestJsonWrapperBean wrapper) {
        return getService().selectRawPagination(wrapper);
    }

    @Override
    default Page<Map<String, Object>> selectAuditablePagination(RestJsonWrapperBean wrapper) {
        return getService().selectAuditablePagination(wrapper);
    }

    @Override
    default Page<Map<String, Object>> selectSearchablePagination(RestJsonWrapperBean wrapper) {
        return getService().selectSearchablePagination(wrapper);
    }

    @Override
    default Page<Map<String, Object>> selectWithdrawablePagination(RestJsonWrapperBean wrapper) {
        return getService().selectWithdrawablePagination(wrapper);
    }

    @Override
    default Page<Map<String, Object>> selectUndoablePagination(RestJsonWrapperBean wrapper) {
        return getService().selectUndoablePagination(wrapper);
    }

    @Override
    default List<CoreBpmnInstanceStatusDTO<String>> startProcess(RestJsonWrapperBean wrapper) {
        return getService().startProcess(wrapper);
    }

    @Override
    default String startProcessWithProgress(RestJsonWrapperBean wrapper) {
        return getService().startProcessWithProgress(wrapper);
    }

    @Override
    default List<CoreBpmnInstanceStatusDTO<String>> withdrawProcess(RestJsonWrapperBean wrapper) {
        return getService().withdrawProcess(wrapper);
    }

    @Override
    default String withdrawProcessWithProgress(RestJsonWrapperBean wrapper) {
        return getService().withdrawProcessWithProgress(wrapper);
    }

    @Override
    default List<CoreBpmnInstanceStatusDTO<String>> completeTask(RestJsonWrapperBean wrapper) {
        return getService().completeTask(wrapper);
    }

    @Override
    default String completeTaskWithProgress(RestJsonWrapperBean wrapper) {
        return getService().completeTaskWithProgress(wrapper);
    }

    @Override
    default List<CoreBpmnInstanceStatusDTO<String>> transferTask(RestJsonWrapperBean wrapper) {
        return getService().transferTask(wrapper);
    }

    @Override
    default String transferTaskWithProgress(RestJsonWrapperBean wrapper) {
        return getService().transferTaskWithProgress(wrapper);
    }

    @Override
    default List<CoreBpmnInstanceStatusDTO<String>> endTask(RestJsonWrapperBean wrapper) {
        return getService().endTask(wrapper);
    }

    @Override
    default String endTaskWithProgress(RestJsonWrapperBean wrapper) {
        return getService().endTaskWithProgress(wrapper);
    }

    @Override
    default List<CoreBpmnInstanceStatusDTO<String>> rejectTask(RestJsonWrapperBean wrapper) {
        return getService().rejectTask(wrapper);
    }

    @Override
    default String rejectTaskWithProgress(RestJsonWrapperBean wrapper) {
        return getService().rejectTaskWithProgress(wrapper);
    }

    @Override
    default List<CoreBpmnInstanceStatusDTO<String>> oddRejectTask(RestJsonWrapperBean wrapper) {
        return getService().oddRejectTask(wrapper);
    }

    @Override
    default String oddRejectTaskWithProgress(RestJsonWrapperBean wrapper) {
        return getService().oddRejectTaskWithProgress(wrapper);
    }

    @Override
    default List<CoreBpmnInstanceStatusDTO<String>> undo(RestJsonWrapperBean wrapper) {
        return getService().undo(wrapper);
    }

    @Override
    default String undoWithProgress(RestJsonWrapperBean wrapper) {
        return getService().undoWithProgress(wrapper);
    }

    @Override
    default CoreServiceStatusDTO selectStatus(RestJsonWrapperBean wrapper) {
        return getService().selectStatus(wrapper);
    }

    @Override
    default Map<String, Object> selectBpmnTaskStatus(RestJsonWrapperBean wrapper) {
        return getService().selectBpmnTaskStatus(wrapper);
    }
}
