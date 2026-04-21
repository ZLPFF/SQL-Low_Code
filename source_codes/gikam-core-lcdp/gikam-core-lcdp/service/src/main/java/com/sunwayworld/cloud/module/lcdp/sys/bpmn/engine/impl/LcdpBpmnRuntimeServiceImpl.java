package com.sunwayworld.cloud.module.lcdp.sys.bpmn.engine.impl;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sunwayworld.cloud.module.lcdp.base.LcdpAuditableDTO;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.support.PersistableMetadataHelper;
import com.sunwayworld.framework.support.domain.Auditable;
import com.sunwayworld.framework.utils.BeanUtils;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.sys.bpmn.CoreBpmnHelper;
import com.sunwayworld.module.sys.bpmn.bean.CoreBpmnInstanceBean;
import com.sunwayworld.module.sys.bpmn.bean.CoreBpmnProcBean;
import com.sunwayworld.module.sys.bpmn.diagram.ProcessStatus;
import com.sunwayworld.module.sys.bpmn.engine.impl.CoreBpmnRuntimeServiceImpl;

import com.sunwayworld.module.sys.bpmn.exception.BpmnException;
import com.sunwayworld.module.sys.bpmn.service.CoreBpmnInstanceService;
import com.sunwayworld.module.sys.bpmn.service.CoreBpmnProcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.framework.spring.annotation.GikamBean;

@GikamBean
@Repository
public class LcdpBpmnRuntimeServiceImpl extends CoreBpmnRuntimeServiceImpl {

    @Autowired
    private CoreBpmnProcService procService;

    @Autowired
    private CoreBpmnInstanceService instanceService;

    @Override
    public <T extends Auditable<ID>, ID extends Serializable> Map<ID, CoreBpmnProcBean> selectRuntimeBpmnProcList(List<T> itemList) {
        if (itemList.isEmpty()) {
            return CollectionUtils.emptyMap();
        }

        List<T> rawItemList = new ArrayList<>();
        List<T> approveItemList = new ArrayList<>();

        for (T item : itemList) {
            if (StringUtils.startsWithIgnoreCase(item.getProcessStatus(), ProcessStatus.DRAFT.name())) {
                rawItemList.add(item);
            } else if (StringUtils.startsWithIgnoreCase(item.getProcessStatus(), ProcessStatus.APPROVE.name())) {
                approveItemList.add(item);
            }
        }

        Map<ID, CoreBpmnProcBean> runtimeMap = new HashMap<>();

        if (!rawItemList.isEmpty()) {
            String tableName = null;

            if (LcdpAuditableDTO.class.equals(itemList.get(0).getClass())) {
                tableName = ((LcdpAuditableDTO) itemList.get(0)).getTableName();
            } else {
                tableName = PersistableMetadataHelper.getTableName(itemList.get(0).getClass());

            }


            List<CoreBpmnProcBean> procList = procService.selectRuntimeBpmnProcList(tableName, LocalContextHelper.getLoginOrgId());


            if (procList.isEmpty()) {
                throw new BpmnException("CORE.MODULE.SYS.T_CORE_BPMN_PROC.ENGINE.PROCESS_NOT_EXISTS", I18nHelper.getMessage(tableName + ".SERVICE_NAME"));
            } else {
                for (T rawItem : rawItemList) {
                    List<CoreBpmnProcBean> matchProcList = new ArrayList<>();

                    for (CoreBpmnProcBean proc : procList) {
                        if (StringUtils.isBlank(proc.getExpression())) {
                            matchProcList.add(proc);
                        } else {
                            Map<String, Object> vars = BeanUtils.deeplyToMap(rawItem);

                            if (CoreBpmnHelper.evalExpression(proc.getExpression(), vars)) {
                                if (!matchProcList.isEmpty()
                                        && !StringUtils.isBlank(matchProcList.get(0).getExpression())) {
                                    logMultipleMatchedProc(tableName, rawItem.getId(), matchProcList, "multiple expression processes matched");
                                    throw new BpmnException("CORE.MODULE.SYS.T_CORE_BPMN_PROC.DIAGRAM.MATCH_MULTIPLE_PROC");
                                }

                                matchProcList.add(0, proc);
                            }
                        }
                    }

                    if (matchProcList.isEmpty()) {
                        throw new BpmnException("CORE.MODULE.SYS.T_CORE_BPMN_PROC.ENGINE.PROCESS_NOT_EXISTS", I18nHelper.getMessage(tableName + ".SERVICE_NAME"));
                    } else {
                        if (matchProcList.size() == 1) {
                            runtimeMap.put(rawItem.getId(), matchProcList.get(0));
                        } else {
                            if (!StringUtils.isBlank(matchProcList.get(0).getExpression())) {
                                runtimeMap.put(rawItem.getId(), matchProcList.get(0));
                            } else {
                                // 判断是否有唯一非默认流程
                                for (CoreBpmnProcBean proc : matchProcList) {
                                    // 避免返回的数量转换字符串时有小数点的问题，判断是否0开头
                                    if (!StringUtils.startsWith(proc.getExt$Item("orgqty"), "0")) {
                                        if (runtimeMap.get(rawItem.getId()) == null) {
                                            runtimeMap.put(rawItem.getId(), proc);
                                        } else {
                                            logMultipleMatchedProc(tableName, rawItem.getId(), matchProcList, "multiple org-specific processes matched");
                                            throw new BpmnException("CORE.MODULE.SYS.T_CORE_BPMN_PROC.DIAGRAM.MATCH_MULTIPLE_PROC");
                                        }
                                    }
                                }

                                if (runtimeMap.get(rawItem.getId()) == null) {
                                    logMultipleMatchedProc(tableName, rawItem.getId(), matchProcList, "multiple default processes matched but none selected");
                                    throw new BpmnException("CORE.MODULE.SYS.T_CORE_BPMN_PROC.DIAGRAM.MATCH_MULTIPLE_PROC");
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!approveItemList.isEmpty()) {
            String tableName = PersistableMetadataHelper.getTableName(itemList.get(0).getClass());

            List<String> targetIdList = approveItemList.stream().map(i -> tableName + "$" + i.getId()).collect(Collectors.toList());
            List<CoreBpmnInstanceBean> instanceList = instanceService.selectListByFilter(SearchFilter.instance().match("TARGETID", targetIdList).filter(MatchPattern.OR));

            if (instanceList != null && instanceList.size() > 0) {
                approveItemList.forEach(a -> runtimeMap.put(a.getId(), procService.selectById(instanceList.stream().filter(i -> i.getTargetId().equals(tableName + "$" + a.getId())).findAny().get().getProcId())));
            }
        }

        return runtimeMap;
    }


}
