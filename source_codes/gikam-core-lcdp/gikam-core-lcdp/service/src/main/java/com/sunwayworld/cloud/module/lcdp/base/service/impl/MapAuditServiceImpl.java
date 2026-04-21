package com.sunwayworld.cloud.module.lcdp.base.service.impl;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.cloud.module.lcdp.base.LcdpAuditableDTO;
import com.sunwayworld.cloud.module.lcdp.base.service.MapAuditService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpWrapperParseUtils;
import com.sunwayworld.framework.beans.BeanPropertyDescriptor;
import com.sunwayworld.framework.beans.BeanPropertyHelper;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.data.Pair;
import com.sunwayworld.framework.database.context.TableContext;
import com.sunwayworld.framework.exception.checked.CheckedException;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.support.PersistableHelper;
import com.sunwayworld.framework.support.auditable.bean.CoreBpmnParameterDTO;
import com.sunwayworld.framework.support.auditable.bean.CoreBpmnTaskStatusDTO;
import com.sunwayworld.framework.support.base.dao.MapDao;
import com.sunwayworld.framework.utils.BeanUtils;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.ConvertUtils;
import com.sunwayworld.framework.utils.DateTimeUtils;
import com.sunwayworld.framework.utils.NumberUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.ReflectionUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.mdm.user.bean.CoreUserBean;
import com.sunwayworld.module.mdm.user.service.CoreUserService;
import com.sunwayworld.module.sys.bpmn.bean.CoreBpmnInstanceBean;
import com.sunwayworld.module.sys.bpmn.bean.CoreBpmnInstanceNextTaskElementDTO;
import com.sunwayworld.module.sys.bpmn.bean.CoreBpmnInstanceStatusDTO;
import com.sunwayworld.module.sys.bpmn.bean.CoreBpmnProcBean;
import com.sunwayworld.module.sys.bpmn.bean.CoreBpmnTaskCandidatorsDTO;
import com.sunwayworld.module.sys.bpmn.diagram.BpmnDiagramHelper;
import com.sunwayworld.module.sys.bpmn.diagram.ProcessStatus;
import com.sunwayworld.module.sys.bpmn.diagram.attribute.CandidatorFilterStrategy;
import com.sunwayworld.module.sys.bpmn.diagram.attribute.NextCandidatorOptStrategy;
import com.sunwayworld.module.sys.bpmn.engine.CoreBpmnRuntimeService;
import com.sunwayworld.module.sys.bpmn.engine.CoreBpmnRuntimeSource;
import com.sunwayworld.module.sys.bpmn.engine.cache.BpmnRuntimeCacheProvider;
import com.sunwayworld.module.sys.bpmn.exception.BpmnException;
import com.sunwayworld.module.sys.role.bean.CoreRoleUserBean;
import com.sunwayworld.module.sys.role.service.CoreRoleService;
import com.sunwayworld.module.sys.role.service.CoreRoleUserService;

@Repository
public class MapAuditServiceImpl implements MapAuditService {
    @Autowired
    private MapDao mapDao;
    @Autowired
    private CoreBpmnRuntimeService runtimeService;
    
    @Override
    @Transactional
    public List<CoreBpmnInstanceStatusDTO<String>> startProcess(String table,
            List<LcdpAuditableDTO> auditableItemList,
            CoreBpmnParameterDTO parameter) {
        if (auditableItemList.isEmpty()) {
            return CollectionUtils.emptyList();
        }
        
        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseSubmittableRuntimeSource(table,
                auditableItemList,
                parameter);

        List<CoreBpmnInstanceStatusDTO<String>> instanceStatusList = runtimeService.startProcess(runtimeSourceList);
        
        autoPass(table, runtimeSourceList, instanceStatusList);

        return instanceStatusList;
    }

    @Override
    @Transactional
    public List<CoreBpmnInstanceStatusDTO<String>> completeTask(String table,
            List<LcdpAuditableDTO> auditableItemList,
            CoreBpmnParameterDTO parameter) {
        if (auditableItemList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(table,
                auditableItemList,
                parameter);
        
        List<CoreBpmnInstanceStatusDTO<String>> instanceStatusList = runtimeService.completeTask(runtimeSourceList);

        autoPass(table, runtimeSourceList, instanceStatusList);

        return instanceStatusList;
    
    }
    
    @Override
    @Transactional
    public List<CoreBpmnInstanceStatusDTO<String>> rejectTask(String table,
            List<LcdpAuditableDTO> auditableItemList,
            CoreBpmnParameterDTO parameter) {
        if (auditableItemList.isEmpty()) {
            return CollectionUtils.emptyList();
        }
        
        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(table,
                auditableItemList,
                parameter);
        
        return runtimeService.rejectTask(runtimeSourceList);
    }
    
    @Override
    @Transactional
    public List<CoreBpmnInstanceStatusDTO<String>> withdrawProcess(String table, List<LcdpAuditableDTO> auditableItemList, CoreBpmnParameterDTO parameter) {
        if (auditableItemList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseWithdrawableRuntimeSource(table,
                auditableItemList,
                parameter);

        return runtimeService.withdrawProcess(runtimeSourceList);
    }
    
    @Override
    public <ID> List<LcdpAuditableDTO> getAuditableItemList(String table, List<ID> idList) {
        TableContext tableContext = TableContext.of(table);
        
        // 根据实际的ID类型做转换
        List<?> selectIdList = idList.stream().map(i -> tableContext.isNumberId() ? NumberUtils.parseLong(i) : i.toString()).collect(Collectors.toList());
        
        List<Map<String, Object>> mapList = mapDao.selectListByIdList(tableContext, selectIdList);

        List<LcdpAuditableDTO> list = new ArrayList<>();
        mapList.stream().forEach(e -> {
            LcdpAuditableDTO auditableDTO = BeanUtils.parse(e, LcdpAuditableDTO.class);

            auditableDTO.setTableName(table);
            
            auditableDTO.setVars(getBpmnVars(auditableDTO));

            list.add(auditableDTO);
        });
        
        return list;
    }

    @Override
    public Map<String, Object> selectTaskStatus(String table, List<LcdpAuditableDTO> auditableItemList, CoreBpmnParameterDTO parameter) {
        if (auditableItemList.stream().anyMatch(a -> ProcessStatus.DONE.name().equalsIgnoreCase(a.getProcessStatus()))) {
            return CollectionUtils.emptyMap();
        }
        
        List<LcdpAuditableDTO> draftList = auditableItemList.stream()
                .filter(a -> StringUtils.startsWithIgnoreCase(a.getProcessStatus(), ProcessStatus.DRAFT.name()))
                .collect(Collectors.toList());
        List<LcdpAuditableDTO> approveList = auditableItemList.stream()
                .filter(a -> StringUtils.startsWithIgnoreCase(a.getProcessStatus(), ProcessStatus.APPROVE.name()))
                .collect(Collectors.toList());
        
        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> sourceList = new ArrayList<>();
        if (!draftList.isEmpty()) {
            sourceList.addAll(parseSubmittableRuntimeSource(table, draftList, parameter));
        }
        if (!approveList.isEmpty()) {
            sourceList.addAll(parseAuditableRuntimeSource(table, approveList, parameter));
        }
        
        CoreBpmnTaskStatusDTO taskStatus = null;
        try {
            // 初始化缓存
            BpmnRuntimeCacheProvider.init(sourceList);

            for (CoreBpmnRuntimeSource<LcdpAuditableDTO, String> source : sourceList) {
                CoreBpmnTaskStatusDTO sourceStatus = new CoreBpmnTaskStatusDTO();

                Element currentElement = BpmnRuntimeCacheProvider.getBpmnRuntimeTaskElement(source);

                sourceStatus.setAttachmentStrategy(BpmnDiagramHelper.getAttachmentStrategy(currentElement));
                sourceStatus.setCommentRequired(BpmnDiagramHelper.isCommentRequiredTask(currentElement));
                sourceStatus.setAuthRequired(BpmnDiagramHelper.isAuthRequiredUserTask(currentElement));
                sourceStatus.setTransfer(BpmnDiagramHelper.isTransferTask(currentElement));

                // 当前节点的最后一个审核人时，才去判断下一级审核人信息
                if (BpmnRuntimeCacheProvider.isLastTaskCandidator(source)) {
                    sourceStatus.setNextCandidatorOptStrategy(BpmnDiagramHelper.getNextCandidatorOptStrategy(currentElement));

                    if (NextCandidatorOptStrategy.assigned.equals(sourceStatus.getNextCandidatorOptStrategy())
                            || NextCandidatorOptStrategy.assignedRole.equals(sourceStatus.getNextCandidatorOptStrategy())) {
                        CoreBpmnInstanceNextTaskElementDTO<String> nextTaskElement = BpmnRuntimeCacheProvider.getNextTask(source);

                        if (NextCandidatorOptStrategy.assigned.equals(sourceStatus.getNextCandidatorOptStrategy())) {
                            List<Element> nextTaskElementList = nextTaskElement.getNextTaskElementList();

                            List<String> candidatorIdList = new ArrayList<>();
                            List<Long> candidateRoleIdList = new ArrayList<>();

                            CoreBpmnInstanceBean instance = BpmnRuntimeCacheProvider.getBpmnRuntimeInstance(source);

                            // 提交人
                            String initiator = (instance == null ? LocalContextHelper.getLoginUserId() : instance.getInitiator());
                            for (Element taskElement : nextTaskElementList) {
                                CoreBpmnTaskCandidatorsDTO candidators = BpmnDiagramHelper.getUserTaskCandidators(source, taskElement, initiator);

                                if (candidators.getCandidatorIdList() != null) {
                                    candidatorIdList.addAll(candidators.getCandidatorIdList());
                                }

                                if (candidators.getCandidateRoleIdList() != null) {
                                    candidateRoleIdList.addAll(candidators.getCandidateRoleIdList());
                                }
                            }

                            if (!candidateRoleIdList.isEmpty()) {
                                CoreRoleUserService roleUserService = ApplicationContextHelper.getBean(CoreRoleUserService.class);

                                List<CoreRoleUserBean> roleUserList = roleUserService.getDao().selectListByOneColumnValues(candidateRoleIdList.stream().distinct().collect(Collectors.toList()), "ROLEID");

                                roleUserList.forEach(r -> candidatorIdList.add(r.getUserId()));
                            }

                            if (!candidatorIdList.isEmpty()) {
                                CoreUserService userService = ApplicationContextHelper.getBean(CoreUserService.class);

                                List<CoreUserBean> candidatorList = userService.selectListByIds(candidatorIdList.stream().distinct().collect(Collectors.toList())).stream().filter(u -> !"deprecated".equals(u.getStatus())).collect(Collectors.toList());

                                // 过滤相同部门的待审人员
                                if (CandidatorFilterStrategy.sameDept.equals(BpmnDiagramHelper.getCandidatorFilterStrategy(currentElement))) {
                                    String createdByOrgId = (String) PersistableHelper.getPropertyValue(source.getOldItem(), "createdByOrgId");

                                    if (!StringUtils.isBlank(createdByOrgId)) {
                                        candidatorList.removeIf(c -> !c.getOrgId().equals(createdByOrgId));
                                    }
                                }

                                sourceStatus.setCandidatorList(candidatorList);
                            }
                        } else if (NextCandidatorOptStrategy.assignedRole.equals(sourceStatus.getNextCandidatorOptStrategy())) {
                            List<Element> nextTaskElementList = nextTaskElement.getNextTaskElementList();

                            List<Long> candidateRoleIdList = new ArrayList<>();

                            CoreBpmnInstanceBean instance = BpmnRuntimeCacheProvider.getBpmnRuntimeInstance(source);

                            // 提交人
                            String initiator = (instance == null ? LocalContextHelper.getLoginUserId() : instance.getInitiator());
                            for (Element taskElement : nextTaskElementList) {
                                CoreBpmnTaskCandidatorsDTO candidators = BpmnDiagramHelper.getUserTaskCandidators(source, taskElement, initiator);

                                if (candidators.getCandidateRoleIdList() != null) {
                                    candidateRoleIdList.addAll(candidators.getCandidateRoleIdList());
                                }
                            }

                            if (!candidateRoleIdList.isEmpty()) {
                                CoreRoleService roleService = ApplicationContextHelper.getBean(CoreRoleService.class);

                                sourceStatus.setCandidateRoleList(roleService.selectListByIds(candidateRoleIdList.stream().distinct().collect(Collectors.toList())));

                            }
                        }
                    }
                }

                if (taskStatus == null) {
                    taskStatus = sourceStatus;
                } else {
                    checkAndMerge(taskStatus, sourceStatus);
                }
            }
        } finally {
            BpmnRuntimeCacheProvider.remove();
        }

        if (NextCandidatorOptStrategy.assigned.equals(taskStatus.getNextCandidatorOptStrategy())) {
            if (CollectionUtils.isEmpty(taskStatus.getCandidatorList())) {
                throw new BpmnException("CORE.MODULE.SYS.T_CORE_BPMN_PROC.ENGINE.TIP.ASSIGNED_STRATEGY_REQUIRES_CANDIDATORS");
            }
        }

        if (NextCandidatorOptStrategy.assignedRole.equals(taskStatus.getNextCandidatorOptStrategy())) {
            if (CollectionUtils.isEmpty(taskStatus.getCandidateRoleList())) {
                throw new BpmnException("CORE.MODULE.SYS.T_CORE_BPMN_PROC.ENGINE.TIP.ASSIGNED_ROLE_STRATEGY_REQUIRES_CANDIDATEROLES");
            }
        }

        return toMap(taskStatus);
        
    }
    //---------------------------------------------------------------------------------------------
    // 私有方法
    //---------------------------------------------------------------------------------------------
    private void autoPass(String table, List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList, List<CoreBpmnInstanceStatusDTO<String>> instanceStatusList) {
        List<LcdpAuditableDTO> autoPassIdList = new ArrayList<>();

        for (int i = 0, j = runtimeSourceList.size(); i < j; i++) {
            CoreBpmnRuntimeSource<LcdpAuditableDTO, String> runtimeSource = runtimeSourceList.get(i);

            if (runtimeSource.isAutoPass()) {
                instanceStatusList.removeIf(s -> s.getId().equals(runtimeSource.getId()));

                autoPassIdList.add(runtimeSource.getOldItem());
            }
        }

        if (!autoPassIdList.isEmpty()) {
            CoreBpmnParameterDTO bpmnParameter = new CoreBpmnParameterDTO();
            bpmnParameter.setComment(I18nHelper.getMessage("GIKAM.BPMN.COMMENT.AUTO_PASS"));

            instanceStatusList.addAll(completeTask(table, autoPassIdList, bpmnParameter));
        }
    }
    
    private List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> parseSubmittableRuntimeSource(String table,
            List<LcdpAuditableDTO> auditableItemList,
            CoreBpmnParameterDTO parameter) {
        List<Pair<String, Long>> procIdList = getBpmnProcIdList(auditableItemList);

        if (procIdList.isEmpty()) {
            throw new BpmnException("CORE.MODULE.SYS.T_CORE_BPMN_PROC.ENGINE.PROCESS_NOT_EXISTS", I18nHelper.getMessage(table + ".SERVICE_NAME"));
        }

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> sourceList = new ArrayList<>();
        for (LcdpAuditableDTO item : auditableItemList) {
            CoreBpmnRuntimeSource<LcdpAuditableDTO, String> source = CoreBpmnRuntimeSource.of(table,
                    item, parameter.getComment(), item.getVars());

            Pair<String, Long> pair = procIdList.stream().filter(p -> item.getId().equals(p.getFirst())).findAny().orElse(null);

            if (pair == null) {
                throw new ApplicationRuntimeException("CORE.MODULE.SYS.T_CORE_BPMN_PROC.ENGINE.PROCESS_NOT_EXISTS", I18nHelper.getMessage(table + ".SERVICE_NAME"));
            }

            source.setProcId(pair.getSecond());
            source.setDescSupplier(item.getDescSupplier());

            String attachment = parameter.getAttachment();
            if (!StringUtils.isBlank(attachment)) {
                source.setAttachmentId(NumberUtils.parseLong(attachment));
            }

            // 指定下一级审核人
            for (String nextCandidator : parameter.getNextCandidators()) {
                source.addNextCandidator(nextCandidator);
            }

            // 指定下一级审核角色
            for (String nextCandidateRole : parameter.getNextCandidateRoles()) {
                source.addNextCandidatorRole(NumberUtils.parseLong(nextCandidateRole));
            }

            sourceList.add(source);
        }

        return sourceList;
    }
    
    private List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> parseAuditableRuntimeSource(String table,
            List<LcdpAuditableDTO> auditableItemList,
            CoreBpmnParameterDTO parameter) {
        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> sourceList = new ArrayList<>();
        for (LcdpAuditableDTO item : auditableItemList) {
            CoreBpmnRuntimeSource<LcdpAuditableDTO, String> source = CoreBpmnRuntimeSource.of(table,
                    item, parameter.getComment(), item.getVars());

            source.setDescSupplier(item.getDescSupplier());
            
            // 当前流程节点的编号（可为空）
            source.setCurrentStatusCode(parameter.getCurrentStatusCode());

            // 审核拒绝时的目标节点（可为空）
            source.setTargetStatusCode(parameter.getTargetStatusCode());

            String attachment = parameter.getAttachment();
            if (!StringUtils.isBlank(attachment)) {
                source.setAttachmentId(NumberUtils.parseLong(attachment));
            }

            // 指定下一级审核人
            for (String nextCandidator : parameter.getNextCandidators()) {
                source.addNextCandidator(nextCandidator);
            }

            // 指定下一级审核角色
            for (String nextCandidateRole : parameter.getNextCandidateRoles()) {
                source.addNextCandidatorRole(NumberUtils.parseLong(nextCandidateRole));
            }

            // 转办人
            if (!StringUtils.isBlank(parameter.getTransferCandidator())) {
                source.setTransferCandidator(parameter.getTransferCandidator());
            }

            source.setDescSupplier(item.getDescSupplier());

            sourceList.add(source);
        }

        return sourceList;
    }
    
    private List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> parseWithdrawableRuntimeSource(String table,
            List<LcdpAuditableDTO> auditableItemList, CoreBpmnParameterDTO parameter) {
        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> sourceList = new ArrayList<>();
        for (LcdpAuditableDTO item : auditableItemList) {
            CoreBpmnRuntimeSource<LcdpAuditableDTO, String> source = CoreBpmnRuntimeSource.of(table,
                    item, parameter.getComment(), item.getVars());
            
            source.setDescSupplier(item.getDescSupplier());

            sourceList.add(source);
        }

        return sourceList;
    }
    
    private List<Pair<String, Long>> getBpmnProcIdList(List<LcdpAuditableDTO> itemList) {
        Map<String, CoreBpmnProcBean> map = runtimeService.selectRuntimeBpmnProcList(itemList);

        return map.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue().getId())).collect(Collectors.toList());
    }
    
    private Map<String, Object> getBpmnVars(LcdpAuditableDTO item) {
        List<BeanPropertyDescriptor> propertyDescriptorList = BeanPropertyHelper.getBeanPropertyDescriptorList(LcdpAuditableDTO.class);

        Map<String, Object> vars = new HashMap<>();

        propertyDescriptorList.forEach(pd -> {
            // 参数只支持如下几种类型
            if (String.class.equals(pd.getPropertyType())
                    || Date.class.isAssignableFrom(pd.getPropertyType())
                    || Temporal.class.isAssignableFrom(pd.getPropertyType())
                    || Number.class.isAssignableFrom(pd.getPropertyType())) {
                // 日期格式转换成Date
                if (Temporal.class.isAssignableFrom(pd.getPropertyType())) {
                    vars.put(pd.getName().toLowerCase(), ConvertUtils.convert(pd.getPropertyValue(item), Date.class));
                } else {
                    vars.put(pd.getName().toLowerCase(), pd.getPropertyValue(item));
                }
            }
        });

        item.getExt$().forEach((k, v) -> vars.put(k.toLowerCase(), v));
        
        return vars;
    }
    
    private void checkAndMerge(CoreBpmnTaskStatusDTO taskStatus, CoreBpmnTaskStatusDTO newTaskStatus) {
        if (taskStatus == null) {
            return;
        }

        if (taskStatus.getAuthRequired() != newTaskStatus.getAuthRequired()) {
            throw new BpmnException("CORE.MODULE.SYS.T_CORE_BPMN_PROC.ENGINE.TIP.AUTH_REQUIRED_DIFFER");
        }

        if (!ObjectUtils.equals(taskStatus.getAttachmentStrategy(), newTaskStatus.getAttachmentStrategy())) {
            throw new BpmnException("CORE.MODULE.SYS.T_CORE_BPMN_PROC.ENGINE.TIP.ATTACHMENT_STRATEGY_DIFFER");
        }

        if (!ObjectUtils.equals(taskStatus.getNextCandidatorOptStrategy(), newTaskStatus.getNextCandidatorOptStrategy())) {
            throw new BpmnException("CORE.MODULE.SYS.T_CORE_BPMN_PROC.ENGINE.TIP.NEXT_CANDIDATOR_OPT_STRATEGY_DIFFER");
        }

        if (taskStatus.isTransfer() != newTaskStatus.isTransfer()) {
            taskStatus.setTransfer(false);
        }

        if (NextCandidatorOptStrategy.assigned.equals(taskStatus.getNextCandidatorOptStrategy())) {
            if (taskStatus.getCandidatorList() != null) {
                if (newTaskStatus.getCandidatorList() == null) {
                    taskStatus.setCandidatorList(null);
                } else {
                    taskStatus.getCandidatorList().removeIf(c -> newTaskStatus.getCandidatorList().stream().noneMatch(n -> n.getId().equals(c.getId())));
                }
            }
        }

        if (NextCandidatorOptStrategy.assignedRole.equals(taskStatus.getNextCandidatorOptStrategy())) {
            if (taskStatus.getCandidateRoleList() != null) {
                if (newTaskStatus.getCandidateRoleList() == null) {
                    taskStatus.setCandidateRoleList(null);
                } else {
                    taskStatus.getCandidateRoleList().removeIf(c -> newTaskStatus.getCandidateRoleList().stream().noneMatch(n -> n.getId().equals(c.getId())));
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object bean) {
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
                List<Map<String, Object>> childrenMapList = toMapList(childrenList);
                map.put("children", childrenMapList);
            }
        }

        correctMapData(map);

        return LcdpWrapperParseUtils.lowerCaseKey(map);
    }
    
    private List<Map<String, Object>> toMapList(List<?> beanList) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (beanList == null) {
            return mapList;
        }

        for (Object bean : beanList) {
            mapList.add(toMap(bean));
        }
        return mapList;
    }
    
    private void correctMapData(Map<String, Object> map) {
        if (map == null) {
            return;
        }

        map.forEach((key, value) -> {
            //特殊处理日期和时间格式,去掉毫秒，纳秒
            if (LocalDate.class.isInstance(value)) {
                try {
                    value = DateTimeUtils.formatLocalDate(DateTimeUtils.parseLocalDate(value.toString()));
                    map.put(key, value);
                } catch (Exception ex) {
                    throw new CheckedException(ex);
                }
            } else if (LocalDateTime.class.isInstance(value)) {
                try {
                    value = DateTimeUtils.formatLocalDateTime(DateTimeUtils.parseLocalDateTime(value.toString()));
                    map.put(key, value);
                } catch (Exception ex) {
                    throw new CheckedException(ex);
                }
            } else if (Timestamp.class.isInstance(value)) {
                String dateValue = value.toString();

                //过滤毫秒或纳秒
                if (StringUtils.contains(dateValue, ".")) {
                    dateValue = dateValue.split("\\.")[0];
                }

                map.put(key, dateValue);
            }
        });
    }
}
