package [(${initJavaScriptDTO.packageName})];

import com.sunwayworld.module.workflow.annotation.WorkflowProcess;
import com.sunwayworld.module.workflow.api.Processor;
import com.sunwayworld.module.workflow.constant.enumtype.ProcessType;
import com.sunwayworld.module.workflow.constant.enumtype.WorkFlowScopeTypeEnum;
import com.sunwayworld.module.workflow.data.context.WorkFlowProcessContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @WorkflowProcess 参数说明
 *      scope：作用范围 (proc-流程动作,node-节点动作)
 *      value：动作类型 (FORWARD_PRE-提交前,FORWARD_POST-提交后,BACKWARD_PRE-拒绝前,BACKWARD_POST-拒绝后,INIT_PRE-初始化前,INIT_POST-初始化后)
 *      comment：动作名称
 *      isCommon：是否是核心动作
 */
@WorkflowProcess(
        scope = WorkFlowScopeTypeEnum.PROCESS_ACTION,
        value = {ProcessType.FORWARD_PRE},
        comment = "业务工作流动作名称",
        isCommon = true)
@Service
public class [(${initJavaScriptDTO.className})] implements Processor {

    @Override
    public void execute(WorkFlowProcessContext context) {
        // TODO 写各业务自己的逻辑
        List<Map<String, Object>> dataList = context.getDataList();

    }

    @Override
    public int order() {
        return -1;
    }

}