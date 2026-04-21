package com.sunwayworld.cloud.module.lcdp.base.resource;

import java.util.List;
import java.util.Map;

import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.bean.CoreServiceStatusDTO;
import com.sunwayworld.module.sys.bpmn.bean.CoreBpmnInstanceStatusDTO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public interface LcdpAuditResource<S> extends LcdpBaseResource<S> {
    @RequestMapping(value = "/selectRawPagination", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    Page<Map<String, Object>> selectRawPagination(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/selectAuditablePagination", "/queries/tasks"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    Page<Map<String, Object>> selectAuditablePagination(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/selectSearchablePagination", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    Page<Map<String, Object>> selectSearchablePagination(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/selectWithdrawablePagination", "/queries/withdrawable"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    Page<Map<String, Object>> selectWithdrawablePagination(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/selectUndoablePagination", "/queries/undoable"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    Page<Map<String, Object>> selectUndoablePagination(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/startProcess", "/action/start-process"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    List<CoreBpmnInstanceStatusDTO<String>> startProcess(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/startProcessWithProgress", "/action/start-process-with-progress"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    String startProcessWithProgress(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/withdrawProcess", "/action/withdraw-process"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    List<CoreBpmnInstanceStatusDTO<String>> withdrawProcess(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/withdrawProcessWithProgress", "/action/withdraw-process-with-progress"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    String withdrawProcessWithProgress(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/completeTask", "/action/complete-task"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    List<CoreBpmnInstanceStatusDTO<String>> completeTask(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/completeTaskWithProgress", "/action/complete-task-with-progress"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    String completeTaskWithProgress(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/transferTask", "/action/transfer-task"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    List<CoreBpmnInstanceStatusDTO<String>> transferTask(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/transferTaskWithProgress", "/action/transfer-task-with-progress"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    String transferTaskWithProgress(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/endTask", "/action/end-task"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    List<CoreBpmnInstanceStatusDTO<String>> endTask(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/endTaskWithProgress", "/action/end-task-with-progress"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    String endTaskWithProgress(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/rejectTask", "/action/reject-task"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    List<CoreBpmnInstanceStatusDTO<String>> rejectTask(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/rejectTaskWithProgress", "/action/reject-task-with-progress"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    String rejectTaskWithProgress(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/oddRejectTask", "/action/odd-reject-task"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    List<CoreBpmnInstanceStatusDTO<String>> oddRejectTask(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/oddRejectTaskWithProgress", "/action/odd-reject-task-with-progress"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    String oddRejectTaskWithProgress(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/undo", "/action/undo"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    List<CoreBpmnInstanceStatusDTO<String>> undo(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/undoWithProgress", "/action/undo-with-progress"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    String undoWithProgress(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/selectStatus", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    CoreServiceStatusDTO selectStatus(RestJsonWrapperBean wrapper);

    @RequestMapping(value = {"/selectBpmnTaskStatus","/bpmn-task-status"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    Map<String, Object> selectBpmnTaskStatus(RestJsonWrapperBean wrapper);
}
