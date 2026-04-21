package [(${initJavaScriptDTO.packageName})];

import com.sunwayworld.cloud.module.lcdp.base.annotation.MappingType;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpScriptUtils;
import com.sunwayworld.framework.spring.SpringCaller;
import com.sunwayworld.cloud.module.lcdp.base.annotation.Mapping;
import com.sunwayworld.cloud.module.lcdp.base.LcdpResultDTO;
import com.sunwayworld.cloud.module.lcdp.base.LcdpTreeDataDTO;
import com.sunwayworld.cloud.module.lcdp.base.service.LcdpBaseService;
import com.sunwayworld.module.workflow.support.auditable.lcdpservice.WorkFlowAuditableService;
import com.sunwayworld.module.workflow.constant.WorkFlowConstant;
import com.sunwayworld.cloud.module.lcdp.base.annotation.Mapping;
import com.sunwayworld.cloud.module.lcdp.base.annotation.MappingType;
import com.sunwayworld.cloud.module.lcdp.base.annotation.LcdpPath;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpWrapperParseUtils;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

import org.springframework.context.ApplicationEvent;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class [(${initJavaScriptDTO.className})] implements WorkFlowAuditableService {

    @Override
    public String getTable() {
        return "[(${initJavaScriptDTO.tableName})]";
    }

    @Override
    public String getQuerySqlMapperId() {
        return "[(${initJavaScriptDTO.mapperNamespace})].selectByCondition";
    }

    /**
     * 新增数据
     */
    @Mapping(value = "Lcdp新增数据",type = MappingType.INSERT)
    @Transactional
    @Audit(AuditConstant.INSERT)
    public String insertData(RestJsonWrapperBean wrapper) {
        List<Map<String, Object>> list = LcdpWrapperParseUtils.parseList(wrapper);

        String id = this.lcdpInsert(list);

        // TODO 各个业务模块，先在“业务工作流定义”中定义流程，再改成定义的流程编码
        this.initProcInst(list, "CODE_WORK_FLOW");

        return id;
    }

    /**
     * 业务扩展数据。指定key映射关系，用于工作流监控展示对应的业务信息
     *
     * @return
     */
    public Map<String, Object> getBusinessExtendedData() {
        Map<String, Object> data = new HashMap<>();

        // 业务数据
        data.put(WorkFlowConstant.WF_BUSINESS_OBJECT_ID, WorkFlowConstant.ID_KEY);
        data.put(WorkFlowConstant.WF_BUSINESS_OBJECT_NO, "ID"); // TODO 换成业务编码对应的字段
        data.put(WorkFlowConstant.WF_BUSINESS_OBJECT_NAME, "ID"); // TODO 换成业务名称对应的字段

        // 关联业务数据
        data.put(WorkFlowConstant.WF_REF_BUSINESS_OBJECT_ID, WorkFlowConstant.ID_KEY);
        data.put(WorkFlowConstant.WF_REF_BUSINESS_OBJECT_NO, "ID"); // TODO 换成关联业务编码对应的字段
        data.put(WorkFlowConstant.WF_REF_BUSINESS_OBJECT_NAME, "ID"); // TODO 换成关联业务名称对应的字段

        data.put(WorkFlowConstant.WF_ORG_ID, WorkFlowConstant.DEFAULT_WORKFLOW_ORG_ID);

        return data;
    }

}