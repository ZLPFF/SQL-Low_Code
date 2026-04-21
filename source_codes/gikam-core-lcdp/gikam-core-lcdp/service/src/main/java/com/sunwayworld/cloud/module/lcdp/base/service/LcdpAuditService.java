package com.sunwayworld.cloud.module.lcdp.base.service;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.sunwayworld.cloud.module.lcdp.base.LcdpAuditableDTO;
import com.sunwayworld.cloud.module.lcdp.base.annotation.Mapping;
import com.sunwayworld.cloud.module.lcdp.base.annotation.MappingType;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.beans.BeanPropertyDescriptor;
import com.sunwayworld.framework.beans.BeanPropertyHelper;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.context.concurrent.GikamConcurrentLocker;
import com.sunwayworld.framework.data.Pair;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.database.context.TableContext;
import com.sunwayworld.framework.database.sql.Order;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.mybatis.page.PageRowBounds;
import com.sunwayworld.framework.progress.core.ProgressTask;
import com.sunwayworld.framework.progress.service.ProgressService;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.PersistableHelper;
import com.sunwayworld.framework.support.auditable.bean.CoreBpmnParameterDTO;
import com.sunwayworld.framework.support.auditable.bean.CoreBpmnTaskDTO;
import com.sunwayworld.framework.support.auditable.bean.CoreBpmnTaskStatusDTO;
import com.sunwayworld.framework.support.base.bean.CoreServiceStatusDTO;
import com.sunwayworld.framework.utils.BeanUtils;
import com.sunwayworld.framework.utils.ClassUtils;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.ConvertUtils;
import com.sunwayworld.framework.utils.NumberUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.mdm.user.bean.CoreUserBean;
import com.sunwayworld.module.mdm.user.service.CoreUserService;
import com.sunwayworld.module.sys.bpmn.CoreBpmnHelper;
import com.sunwayworld.module.sys.bpmn.bean.CoreBpmnCommentBean;
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
import com.sunwayworld.module.sys.bpmn.service.CoreBpmnCommentService;
import com.sunwayworld.module.sys.role.bean.CoreRoleUserBean;
import com.sunwayworld.module.sys.role.service.CoreRoleService;
import com.sunwayworld.module.sys.role.service.CoreRoleUserService;
import org.dom4j.Element;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

public interface LcdpAuditService extends LcdpBaseService {
    @Mapping(value = "核心编制页面查询数据（工作流）", type = MappingType.SELECT)
    default Page<Map<String, Object>> selectRawPagination(RestJsonWrapperBean wrapper) {
        MapperParameter parameter = wrapper.extractMapFilter();
        PageRowBounds rowBounds = wrapper.extractPageRowBounds();

        parameter.setProcessStatus("PROCESSSTATUS", ProcessStatus.DRAFT);
        parameter.setRawQueries();
        if (!Constant.NO.equals(wrapper.getParamValue("orgAuthority"))) {
            parameter.setOrgAuthority();
        }

        if (!wrapper.getAuthorityList().isEmpty()) {
            wrapper.getAuthorityList().forEach(a -> parameter.setAuthorityParameter(a));
        }

        parameter.setTableName(getTable());


        return this.selectLcdpPagination(parameter, rowBounds);
    }

    @Mapping(value = "核心审核页面查询数据（工作流）", type = MappingType.SELECT)
    default Page<Map<String, Object>> selectAuditablePagination(RestJsonWrapperBean wrapper) {
        MapperParameter parameter = wrapper.extractMapFilter();
        PageRowBounds rowBounds = wrapper.extractPageRowBounds();

        parameter.setProcessStatus("PROCESSSTATUS", ProcessStatus.APPROVE);
        parameter.setAuditAuthority();
        if (!Constant.NO.equals(wrapper.getParamValue("orgAuthority"))) {
            parameter.setOrgAuthority();
        }
        parameter.setAuditableQueries();

        if (!wrapper.getAuthorityList().isEmpty()) {
            wrapper.getAuthorityList().forEach(a -> parameter.setAuthorityParameter(a));
        }

        parameter.setTableName(getTable());


        return this.selectLcdpPagination(parameter, rowBounds);
    }

    @Mapping(value = "核心查询页面查询数据", type = MappingType.SELECT)
    default Page<Map<String, Object>> selectSearchablePagination(RestJsonWrapperBean wrapper) {
        MapperParameter parameter = wrapper.extractMapFilter();
        PageRowBounds rowBounds = wrapper.extractPageRowBounds();

        parameter.setProcessStatus("PROCESSSTATUS", ProcessStatus.APPROVE_OR_DONE);
        if (!Constant.NO.equals(wrapper.getParamValue("orgAuthority"))) {
            parameter.setOrgAuthority();
        }

        if (!wrapper.getAuthorityList().isEmpty()) {
            wrapper.getAuthorityList().forEach(a -> parameter.setAuthorityParameter(a));
        }

        parameter.setTableName(getTable());


        return this.selectLcdpPagination(parameter, rowBounds);
    }

    @Mapping(value = "核心撤回页面查询数据", type = MappingType.SELECT)
    default Page<Map<String, Object>> selectWithdrawablePagination(RestJsonWrapperBean wrapper) {
        MapperParameter parameter = wrapper.extractMapFilter();
        PageRowBounds rowBounds = wrapper.extractPageRowBounds();

        parameter.setProcessStatus("PROCESSSTATUS", ProcessStatus.APPROVE);

        if (!Constant.NO.equals(wrapper.getParamValue("orgAuthority"))) {
            parameter.setOrgAuthority();
        }
        parameter.setWithdrawableQueries();

        if (!wrapper.getAuthorityList().isEmpty()) {
            wrapper.getAuthorityList().forEach(a -> parameter.setAuthorityParameter(a));
        }

        parameter.setTableName(getTable());


        return this.selectLcdpPagination(parameter, rowBounds);
    }

    @Mapping(value = "回退页查询", type = MappingType.SELECT)
    default Page<Map<String, Object>> selectUndoablePagination(RestJsonWrapperBean wrapper) {
        MapperParameter parameter = wrapper.extractMapFilter();
        PageRowBounds rowBounds = wrapper.extractPageRowBounds();

        parameter.setProcessStatus("PROCESSSTATUS", ProcessStatus.DONE);

        if (!Constant.NO.equals(wrapper.getParamValue("orgAuthority"))) {
            parameter.setOrgAuthority();
        }
        parameter.setUndoableQueries();

        if (!wrapper.getAuthorityList().isEmpty()) {
            wrapper.getAuthorityList().forEach(a -> parameter.setAuthorityParameter(a));
        }

        parameter.setTableName(getTable());


        return this.selectLcdpPagination(parameter, rowBounds);
    }

    @Mapping(value = "核心工作流启动", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_START)
    default List<CoreBpmnInstanceStatusDTO<String>> startProcess(RestJsonWrapperBean wrapper) {
        List<LcdpAuditableDTO> itemList = wrapper.parse(LcdpAuditableDTO.class);

        if (itemList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        // 防止并发
        itemList.forEach(i -> GikamConcurrentLocker.isolated(CoreBpmnHelper.getTargetId(getTable(), i.getId())));

        checkAndUpdateVersion(itemList);

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<String> itemIdList = itemList.stream().map(i -> i.getId()).collect(Collectors.toList());

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseSubmittableRuntimeSource(itemIdList,
                CoreBpmnParameterDTO.of(wrapper), getBpmnVars(wrapper, selectItemList));

        List<CoreBpmnInstanceStatusDTO<String>> instanceStatusList = runtimeService.startProcess(runtimeSourceList);

        autoPass(runtimeSourceList, instanceStatusList);

        return instanceStatusList;

    }

    @Mapping(value = "核心工作流启动（进度监控)", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_START)
    default String startProcessWithProgress(List<String> itemIdList, CoreBpmnParameterDTO parameter) {
        if (itemIdList.isEmpty()) {
            return null;
        }

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseSubmittableRuntimeSource(itemIdList,
                parameter, getBpmnVars(null, selectItemList));

        // 修改单据的ProcessStatus，添加前缀表示数据在执行中
        prependStatusCode(runtimeSourceList, CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG);

        List<ProgressTask> taskList = new ArrayList<>();
        for (CoreBpmnRuntimeSource<LcdpAuditableDTO, String> runtimeSource : runtimeSourceList) {
            ProgressTask runnable = new ProgressTask(getDescSupplier(runtimeSource).get()) {
                public void run() {
                    List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> innerRuntimeSourceList = Arrays.asList(runtimeSource);

                    List<CoreBpmnInstanceStatusDTO<String>> innerInstanceStatusList = runtimeService.startProcess(innerRuntimeSourceList);

                    autoPass(innerRuntimeSourceList, innerInstanceStatusList);
                }

                public void rollback() {
                    LcdpAuditableDTO item = runtimeSource.getOldItem();

                    if (StringUtils.startsWith(item.getProcessStatus(), CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG)) {
                        item.setProcessStatus(StringUtils.removeStart(item.getProcessStatus(), CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG));

                        getMapDao().update(getTable(), toMap(item), "PROCESSSTATUS");
                    }
                }
            };

            taskList.add(runnable);
        }

        String progressId = StringUtils.randomUUID();

        ProgressService progressService = ApplicationContextHelper.getBean(ProgressService.class);
        progressService.start(progressId,
                I18nHelper.getMessage("GIKAM.BPMN.TASK.SUBMIT_NAME", I18nHelper.getMessage(getTable() + ".SERVICE_NAME")),
                taskList);

        return progressId;
    }

    @Mapping(value = "核心工作流启动（进度监控)", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_START)
    default String startProcessWithProgress(RestJsonWrapperBean wrapper) {
        List<LcdpAuditableDTO> itemList = wrapper.parse(LcdpAuditableDTO.class);

        if (itemList.isEmpty()) {
            return null;
        }

        checkAndUpdateVersion(itemList);

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<String> itemIdList = itemList.stream().map(i -> i.getId()).collect(Collectors.toList());

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseSubmittableRuntimeSource(itemIdList,
                CoreBpmnParameterDTO.of(wrapper), getBpmnVars(wrapper, selectItemList));

        // 修改单据的ProcessStatus，添加前缀表示数据在执行中
        prependStatusCode(runtimeSourceList, CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG);

        List<ProgressTask> taskList = new ArrayList<>();
        for (CoreBpmnRuntimeSource<LcdpAuditableDTO, String> runtimeSource : runtimeSourceList) {
            ProgressTask runnable = new ProgressTask(getDescSupplier(runtimeSource).get()) {
                public void run() {
                    List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> innerRuntimeSourceList = Arrays.asList(runtimeSource);

                    List<CoreBpmnInstanceStatusDTO<String>> innerInstanceStatusList = runtimeService.startProcess(innerRuntimeSourceList);

                    autoPass(innerRuntimeSourceList, innerInstanceStatusList);
                }

                public void rollback() {
                    LcdpAuditableDTO item = runtimeSource.getOldItem();

                    if (StringUtils.startsWith(item.getProcessStatus(), CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG)) {
                        item.setProcessStatus(StringUtils.removeStart(item.getProcessStatus(), CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG));

                        getMapDao().update(getTable(), toMap(item), "PROCESSSTATUS");
                    }
                }
            };

            taskList.add(runnable);
        }

        String progressId = StringUtils.randomUUID();

        ProgressService progressService = ApplicationContextHelper.getBean(ProgressService.class);
        progressService.start(progressId,
                I18nHelper.getMessage("GIKAM.BPMN.TASK.SUBMIT_NAME", I18nHelper.getMessage(getTable() + ".SERVICE_NAME")),
                taskList);

        return progressId;
    }

    @Mapping(value = "核心工作流启动", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_START)
    default List<CoreBpmnInstanceStatusDTO<String>> startProcess(List<String> itemIdList) {
        return startProcess(itemIdList, (CoreBpmnParameterDTO) null);
    }

    @Mapping(value = "核心工作流启动", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_START)
    default List<CoreBpmnInstanceStatusDTO<String>> startProcess(List<String> itemIdList, CoreBpmnParameterDTO parameter) {
        if (itemIdList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<LcdpAuditableDTO> itemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseSubmittableRuntimeSource(itemIdList,
                parameter, getBpmnVars(null, itemList));

        List<CoreBpmnInstanceStatusDTO<String>> instanceStatusList = runtimeService.startProcess(runtimeSourceList);

        autoPass(runtimeSourceList, instanceStatusList);

        return instanceStatusList;
    }

    @Mapping(value = "核心工作流启动", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_START)
    default List<CoreBpmnInstanceStatusDTO<String>> startProcess(List<String> itemIdList, String comment) {
        CoreBpmnParameterDTO parameter = new CoreBpmnParameterDTO();
        parameter.setComment(comment);
        return startProcess(itemIdList, parameter);
    }

    @Mapping(value = "核心工作流撤回", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_WITHDRAW)
    default List<CoreBpmnInstanceStatusDTO<String>> withdrawProcess(RestJsonWrapperBean wrapper) {
        List<LcdpAuditableDTO> itemList = wrapper.parse(LcdpAuditableDTO.class);

        if (itemList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        checkAndUpdateVersion(itemList);

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<String> itemIdList = itemList.stream().map(i -> i.getId()).collect(Collectors.toList());

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseWithdrawableRuntimeSource(itemIdList,
                CoreBpmnParameterDTO.of(wrapper), getBpmnVars(wrapper, selectItemList));


        List<CoreBpmnInstanceStatusDTO<String>> instanceStatusList = runtimeService.withdrawProcess(runtimeSourceList);


        return instanceStatusList;
    }

    @Mapping(value = "核心工作流撤回（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_WITHDRAW)
    default String withdrawProcessWithProgress(RestJsonWrapperBean wrapper) {
        List<LcdpAuditableDTO> itemList = wrapper.parse(LcdpAuditableDTO.class);

        if (itemList.isEmpty()) {
            return null;
        }

        checkAndUpdateVersion(itemList);

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<String> itemIdList = itemList.stream().map(i -> i.getId()).collect(Collectors.toList());

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseWithdrawableRuntimeSource(itemIdList,
                CoreBpmnParameterDTO.of(wrapper), getBpmnVars(wrapper, selectItemList));

        // 修改单据的ProcessStatus，添加前缀表示数据在执行中
        prependStatusCode(runtimeSourceList, CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG);

        List<ProgressTask> taskList = new ArrayList<>();
        for (CoreBpmnRuntimeSource<LcdpAuditableDTO, String> runtimeSource : runtimeSourceList) {
            ProgressTask task = new ProgressTask(getDescSupplier(runtimeSource).get()) {
                public void run() {
                    List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> innerRuntimeSourceList = Arrays.asList(runtimeSource);

                    runtimeService.withdrawProcess(innerRuntimeSourceList);
                }

                public void rollback() {
                    rollbackTask(runtimeSource);
                }
            };

            taskList.add(task);
        }

        String progressId = StringUtils.randomUUID();

        ProgressService progressService = ApplicationContextHelper.getBean(ProgressService.class);
        progressService.start(progressId,
                I18nHelper.getMessage("GIKAM.BPMN.TASK.WITHDRAW_NAME", I18nHelper.getMessage(getTable() + ".SERVICE_NAME")),
                taskList);

        return progressId;
    }


    @Mapping(value = "核心工作流撤回（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_WITHDRAW)
    default String withdrawProcessWithProgress(List<String> itemIdList, CoreBpmnParameterDTO parameter) {
        if (itemIdList.isEmpty()) {
            return null;
        }

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseWithdrawableRuntimeSource(itemIdList,
                parameter, getBpmnVars(null, selectItemList));

        // 修改单据的ProcessStatus，添加前缀表示数据在执行中
        prependStatusCode(runtimeSourceList, CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG);

        List<ProgressTask> taskList = new ArrayList<>();
        for (CoreBpmnRuntimeSource<LcdpAuditableDTO, String> runtimeSource : runtimeSourceList) {
            ProgressTask task = new ProgressTask(getDescSupplier(runtimeSource).get()) {
                public void run() {
                    List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> innerRuntimeSourceList = Arrays.asList(runtimeSource);

                    runtimeService.withdrawProcess(innerRuntimeSourceList);
                }

                public void rollback() {
                    rollbackTask(runtimeSource);
                }
            };

            taskList.add(task);
        }

        String progressId = StringUtils.randomUUID();

        ProgressService progressService = ApplicationContextHelper.getBean(ProgressService.class);
        progressService.start(progressId,
                I18nHelper.getMessage("GIKAM.BPMN.TASK.WITHDRAW_NAME", I18nHelper.getMessage(getTable() + ".SERVICE_NAME")),
                taskList);

        return progressId;
    }

    @Mapping(value = "核心工作流撤回", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_WITHDRAW)
    default List<CoreBpmnInstanceStatusDTO<String>> withdrawProcess(List<String> itemIdList, CoreBpmnParameterDTO parameter) {
        if (itemIdList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<LcdpAuditableDTO> itemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseWithdrawableRuntimeSource(itemIdList,
                parameter, getBpmnVars(null, itemList));

        return runtimeService.withdrawProcess(runtimeSourceList);
    }

    @Mapping(value = "核心工作流撤回", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_WITHDRAW)
    default List<CoreBpmnInstanceStatusDTO<String>> withdrawProcess(List<String> itemIdList, String comment) {
        CoreBpmnParameterDTO parameter = new CoreBpmnParameterDTO();
        parameter.setComment(comment);
        return withdrawProcess(itemIdList, parameter);
    }

    @Mapping(value = "核心工作流审核通过", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_COMPLETE)
    default List<CoreBpmnInstanceStatusDTO<String>> completeTask(RestJsonWrapperBean wrapper) {
        List<LcdpAuditableDTO> itemList = wrapper.parse(LcdpAuditableDTO.class);

        if (itemList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        // 防止并发
        itemList.forEach(i -> GikamConcurrentLocker.isolated(CoreBpmnHelper.getTargetId(getTable(), i.getId())));

        checkAndUpdateVersion(itemList);

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<String> itemIdList = itemList.stream().map(i -> i.getId()).collect(Collectors.toList());

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(itemIdList,
                CoreBpmnParameterDTO.of(wrapper), getBpmnVars(wrapper, selectItemList));

        // 当前流程已被审核
        boolean flag = runtimeSourceList.stream().anyMatch(e -> !StringUtils.startsWithIgnoreCase(e.getOldItem().getProcessStatus(), ProcessStatus.APPROVE.name()));
        if (flag) {
            throw new BpmnException("CORE.MODULE.SYS.T_CORE_BPMN_PROC.ENGINE.PROCESS_STATUS_UPDATED");
        }

        List<CoreBpmnInstanceStatusDTO<String>> instanceStatusList = runtimeService.completeTask(runtimeSourceList);

        autoPass(runtimeSourceList, instanceStatusList);

        return instanceStatusList;

    }


    @Mapping(value = "核心工作流审核通过（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_COMPLETE)
    default String completeTaskWithProgress(RestJsonWrapperBean wrapper) {
        List<LcdpAuditableDTO> itemList = wrapper.parse(LcdpAuditableDTO.class);

        if (itemList.isEmpty()) {
            return null;
        }

        checkAndUpdateVersion(itemList);

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<String> itemIdList = itemList.stream().map(i -> i.getId()).collect(Collectors.toList());

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(itemIdList,
                CoreBpmnParameterDTO.of(wrapper), getBpmnVars(wrapper, selectItemList));

        // 修改单据的ProcessStatus，添加前缀表示数据在执行中
        prependStatusCode(runtimeSourceList, CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG);

        List<ProgressTask> taskList = new ArrayList<>();
        for (CoreBpmnRuntimeSource<LcdpAuditableDTO, String> runtimeSource : runtimeSourceList) {
            ProgressTask task = new ProgressTask(getDescSupplier(runtimeSource).get()) {
                public void run() {
                    List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> innerRuntimeSourceList = Arrays.asList(runtimeSource);

                    List<CoreBpmnInstanceStatusDTO<String>> innerInstanceStatusList = runtimeService.completeTask(innerRuntimeSourceList);

                    autoPass(innerRuntimeSourceList, innerInstanceStatusList);
                }

                public void rollback() {
                    rollbackTask(runtimeSource);
                }
            };

            taskList.add(task);
        }

        String progressId = StringUtils.randomUUID();

        ProgressService progressService = ApplicationContextHelper.getBean(ProgressService.class);
        progressService.start(progressId,
                I18nHelper.getMessage("GIKAM.BPMN.TASK.COMPLETE_NAME", I18nHelper.getMessage(getTable() + ".SERVICE_NAME")),
                taskList);

        return progressId;
    }

    @Mapping(value = "核心工作流审核通过（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_COMPLETE)
    default String completeTaskWithProgress(List<String> itemIdList, CoreBpmnParameterDTO parameter) {
        if (itemIdList.isEmpty()) {
            return null;
        }

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(itemIdList,
                parameter, getBpmnVars(null, selectItemList));

        // 修改单据的ProcessStatus，添加前缀表示数据在执行中
        prependStatusCode(runtimeSourceList, CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG);

        List<ProgressTask> taskList = new ArrayList<>();
        for (CoreBpmnRuntimeSource<LcdpAuditableDTO, String> runtimeSource : runtimeSourceList) {
            ProgressTask task = new ProgressTask(getDescSupplier(runtimeSource).get()) {
                public void run() {
                    List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> innerRuntimeSourceList = Arrays.asList(runtimeSource);

                    List<CoreBpmnInstanceStatusDTO<String>> innerInstanceStatusList = runtimeService.completeTask(innerRuntimeSourceList);

                    autoPass(innerRuntimeSourceList, innerInstanceStatusList);
                }

                public void rollback() {
                    rollbackTask(runtimeSource);
                }
            };

            taskList.add(task);
        }

        String progressId = StringUtils.randomUUID();

        ProgressService progressService = ApplicationContextHelper.getBean(ProgressService.class);
        progressService.start(progressId,
                I18nHelper.getMessage("GIKAM.BPMN.TASK.COMPLETE_NAME", I18nHelper.getMessage(getTable() + ".SERVICE_NAME")),
                taskList);

        return progressId;
    }

    @Mapping(value = "核心工作流审核通过", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_COMPLETE)
    default List<CoreBpmnInstanceStatusDTO<String>> completeTask(List<String> itemIdList) {
        return completeTask(itemIdList, (CoreBpmnParameterDTO) null);
    }

    @Mapping(value = "核心工作流审核通过", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_COMPLETE)
    default List<CoreBpmnInstanceStatusDTO<String>> completeTask(List<String> itemIdList, CoreBpmnParameterDTO parameter) {
        if (itemIdList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<LcdpAuditableDTO> itemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(itemIdList,
                parameter, getBpmnVars(null, itemList));

        List<CoreBpmnInstanceStatusDTO<String>> instanceStatusList = runtimeService.completeTask(runtimeSourceList);

        autoPass(runtimeSourceList, instanceStatusList);

        return instanceStatusList;
    }

    @Mapping(value = "核心工作流审核通过", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_COMPLETE)
    default List<CoreBpmnInstanceStatusDTO<String>> completeTask(List<String> itemIdList, String comment) {
        CoreBpmnParameterDTO parameter = new CoreBpmnParameterDTO();
        parameter.setComment(comment);
        return completeTask(itemIdList, parameter);
    }

    @Mapping(value = "核心工作流转办", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_TRANSFER)
    default List<CoreBpmnInstanceStatusDTO<String>> transferTask(RestJsonWrapperBean wrapper) {
        List<LcdpAuditableDTO> itemList = wrapper.parse(LcdpAuditableDTO.class);

        if (itemList.isEmpty()) {
            return null;
        }

        checkAndUpdateVersion(itemList);

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<String> itemIdList = itemList.stream().map(i -> i.getId()).collect(Collectors.toList());

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(itemIdList,
                CoreBpmnParameterDTO.of(wrapper), getBpmnVars(wrapper, selectItemList));


        return runtimeService.transferTask(runtimeSourceList);
    }

    @Mapping(value = "核心工作流转办（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_TRANSFER)
    default String transferTaskWithProgress(RestJsonWrapperBean wrapper) {
        List<LcdpAuditableDTO> itemList = wrapper.parse(LcdpAuditableDTO.class);

        if (itemList.isEmpty()) {
            return null;
        }

        checkAndUpdateVersion(itemList);

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<String> itemIdList = itemList.stream().map(i -> i.getId()).collect(Collectors.toList());

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(itemIdList,
                CoreBpmnParameterDTO.of(wrapper), CollectionUtils.emptyMap());

        // 修改单据的ProcessStatus，添加前缀表示数据在执行中
        prependStatusCode(runtimeSourceList, CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG);

        List<ProgressTask> taskList = new ArrayList<>();
        for (CoreBpmnRuntimeSource<LcdpAuditableDTO, String> runtimeSource : runtimeSourceList) {
            ProgressTask task = new ProgressTask(getDescSupplier(runtimeSource).get()) {
                public void run() {
                    List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> innerRuntimeSourceList = Arrays.asList(runtimeSource);

                    runtimeService.transferTask(innerRuntimeSourceList);
                }

                public void rollback() {
                    rollbackTask(runtimeSource);
                }
            };

            taskList.add(task);
        }

        String progressId = StringUtils.randomUUID();

        ProgressService progressService = ApplicationContextHelper.getBean(ProgressService.class);
        progressService.start(progressId,
                I18nHelper.getMessage("GIKAM.BPMN.TASK.TRANSFER_NAME", I18nHelper.getMessage(getTable() + ".SERVICE_NAME")),
                taskList);

        return progressId;
    }

    @Mapping(value = "核心工作流结束", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_END)
    default List<CoreBpmnInstanceStatusDTO<String>> endTask(RestJsonWrapperBean wrapper) {
        List<LcdpAuditableDTO> itemList = wrapper.parse(LcdpAuditableDTO.class);

        if (itemList.isEmpty()) {
            return null;
        }

        checkAndUpdateVersion(itemList);

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<String> itemIdList = itemList.stream().map(i -> i.getId()).collect(Collectors.toList());

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(itemIdList,
                CoreBpmnParameterDTO.of(wrapper), getBpmnVars(wrapper, selectItemList));

        return runtimeService.endTask(runtimeSourceList);
    }

    @Mapping(value = "核心工作流结束（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_END)
    default String endTaskWithProgress(RestJsonWrapperBean wrapper) {
        List<LcdpAuditableDTO> itemList = wrapper.parse(LcdpAuditableDTO.class);

        if (itemList.isEmpty()) {
            return null;
        }

        checkAndUpdateVersion(itemList);

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<String> itemIdList = itemList.stream().map(i -> i.getId()).collect(Collectors.toList());

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(itemIdList,
                CoreBpmnParameterDTO.of(wrapper), getBpmnVars(wrapper, selectItemList));

        // 修改单据的ProcessStatus，添加前缀表示数据在执行中
        prependStatusCode(runtimeSourceList, CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG);

        List<ProgressTask> taskList = new ArrayList<>();
        for (CoreBpmnRuntimeSource<LcdpAuditableDTO, String> runtimeSource : runtimeSourceList) {
            ProgressTask task = new ProgressTask(getDescSupplier(runtimeSource).get()) {
                public void run() {
                    List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> innerRuntimeSourceList = Arrays.asList(runtimeSource);

                    runtimeService.endTask(innerRuntimeSourceList);
                }

                public void rollback() {
                    rollbackTask(runtimeSource);
                }
            };

            taskList.add(task);
        }

        String progressId = StringUtils.randomUUID();

        ProgressService progressService = ApplicationContextHelper.getBean(ProgressService.class);
        progressService.start(progressId,
                I18nHelper.getMessage("GIKAM.BPMN.TASK.END_NAME", I18nHelper.getMessage(getTable() + ".SERVICE_NAME")),
                taskList);

        return progressId;
    }

    @Mapping(value = "核心工作流结束（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_END)
    default String endTaskWithProgress(List<String> itemIdList, CoreBpmnParameterDTO parameter) {
        if (itemIdList.isEmpty()) {
            return null;
        }

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(itemIdList,
                parameter, getBpmnVars(null, selectItemList));

        // 修改单据的ProcessStatus，添加前缀表示数据在执行中
        prependStatusCode(runtimeSourceList, CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG);

        List<ProgressTask> taskList = new ArrayList<>();
        for (CoreBpmnRuntimeSource<LcdpAuditableDTO, String> runtimeSource : runtimeSourceList) {
            ProgressTask task = new ProgressTask(getDescSupplier(runtimeSource).get()) {
                public void run() {
                    List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> innerRuntimeSourceList = Arrays.asList(runtimeSource);

                    runtimeService.endTask(innerRuntimeSourceList);
                }

                public void rollback() {
                    rollbackTask(runtimeSource);
                }
            };

            taskList.add(task);
        }

        String progressId = StringUtils.randomUUID();

        ProgressService progressService = ApplicationContextHelper.getBean(ProgressService.class);
        progressService.start(progressId,
                I18nHelper.getMessage("GIKAM.BPMN.TASK.END_NAME", I18nHelper.getMessage(getTable() + ".SERVICE_NAME")),
                taskList);

        return progressId;
    }

    @Mapping(value = "核心工作流结束", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_END)
    default List<CoreBpmnInstanceStatusDTO<String>> endTask(List<String> itemIdList, CoreBpmnParameterDTO parameter) {
        if (itemIdList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<LcdpAuditableDTO> itemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(itemIdList,
                parameter, getBpmnVars(null, itemList));

        return runtimeService.endTask(runtimeSourceList);
    }

    @Mapping(value = "核心工作流结束", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_END)
    default List<CoreBpmnInstanceStatusDTO<String>> endTask(List<String> itemIdList, String comment) {
        CoreBpmnParameterDTO parameter = new CoreBpmnParameterDTO();
        parameter.setComment(comment);
        return endTask(itemIdList, parameter);
    }

    @Mapping(value = "核心工作流审核拒绝", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_REJECT)
    default List<CoreBpmnInstanceStatusDTO<String>> rejectTask(RestJsonWrapperBean wrapper) {
        List<LcdpAuditableDTO> itemList = wrapper.parse(LcdpAuditableDTO.class);

        if (itemList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        // 防止并发
        itemList.forEach(i -> GikamConcurrentLocker.isolated(CoreBpmnHelper.getTargetId(getTable(), i.getId())));

        checkAndUpdateVersion(itemList);

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<String> itemIdList = itemList.stream().map(i -> i.getId()).collect(Collectors.toList());

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(itemIdList,
                CoreBpmnParameterDTO.of(wrapper), getBpmnVars(wrapper, selectItemList));

        return runtimeService.rejectTask(runtimeSourceList);
    }

    @Mapping(value = "核心工作流审核拒绝（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_REJECT)
    default String rejectTaskWithProgress(RestJsonWrapperBean wrapper) {
        List<LcdpAuditableDTO> itemList = wrapper.parse(LcdpAuditableDTO.class);

        if (itemList.isEmpty()) {
            return null;
        }

        checkAndUpdateVersion(itemList);

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<String> itemIdList = itemList.stream().map(i -> i.getId()).collect(Collectors.toList());

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(itemIdList,
                CoreBpmnParameterDTO.of(wrapper), getBpmnVars(wrapper, selectItemList));

        // 修改单据的ProcessStatus，添加前缀表示数据在执行中
        prependStatusCode(runtimeSourceList, CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG);

        List<ProgressTask> taskList = new ArrayList<>();
        for (CoreBpmnRuntimeSource<LcdpAuditableDTO, String> runtimeSource : runtimeSourceList) {
            ProgressTask task = new ProgressTask(getDescSupplier(runtimeSource).get()) {
                public void run() {
                    List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> innerRuntimeSourceList = Arrays.asList(runtimeSource);

                    runtimeService.rejectTask(innerRuntimeSourceList);
                }

                public void rollback() {
                    rollbackTask(runtimeSource);
                }
            };

            taskList.add(task);
        }

        String progressId = StringUtils.randomUUID();

        ProgressService progressService = ApplicationContextHelper.getBean(ProgressService.class);
        progressService.start(progressId,
                I18nHelper.getMessage("GIKAM.BPMN.TASK.REJECT_NAME", I18nHelper.getMessage(getTable() + ".SERVICE_NAME")),
                taskList);

        return progressId;
    }

    @Mapping(value = "核心工作流审核拒绝（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_REJECT)
    default String rejectTaskWithProgress(List<String> itemIdList, CoreBpmnParameterDTO parameter) {
        if (itemIdList.isEmpty()) {
            return null;
        }

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(itemIdList,
                parameter, getBpmnVars(null, selectItemList));

        // 修改单据的ProcessStatus，添加前缀表示数据在执行中
        prependStatusCode(runtimeSourceList, CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG);

        List<ProgressTask> taskList = new ArrayList<>();
        for (CoreBpmnRuntimeSource<LcdpAuditableDTO, String> runtimeSource : runtimeSourceList) {
            ProgressTask task = new ProgressTask(getDescSupplier(runtimeSource).get()) {
                public void run() {
                    List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> innerRuntimeSourceList = Arrays.asList(runtimeSource);

                    runtimeService.rejectTask(innerRuntimeSourceList);
                }

                public void rollback() {
                    rollbackTask(runtimeSource);
                }
            };

            taskList.add(task);
        }

        String progressId = StringUtils.randomUUID();

        ProgressService progressService = ApplicationContextHelper.getBean(ProgressService.class);
        progressService.start(progressId,
                I18nHelper.getMessage("GIKAM.BPMN.TASK.REJECT_NAME", I18nHelper.getMessage(getTable() + ".SERVICE_NAME")),
                taskList);

        return progressId;
    }

    @Mapping(value = "核心工作流审核拒绝", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_REJECT)
    default List<CoreBpmnInstanceStatusDTO<String>> rejectTask(List<String> itemIdList, CoreBpmnParameterDTO parameter) {
        if (itemIdList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        List<LcdpAuditableDTO> itemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(itemIdList,
                parameter, getBpmnVars(null, itemList));

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        return runtimeService.rejectTask(runtimeSourceList);
    }

    @Mapping(value = "核心工作流审核拒绝", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_REJECT)
    default List<CoreBpmnInstanceStatusDTO<String>> rejectTask(List<String> itemIdList, String comment) {
        CoreBpmnParameterDTO parameter = new CoreBpmnParameterDTO();
        parameter.setComment(comment);
        return rejectTask(itemIdList, parameter);
    }

    @Mapping(value = "核心工作流异常拒绝", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_ODD_REJECT)
    default List<CoreBpmnInstanceStatusDTO<String>> oddRejectTask(RestJsonWrapperBean wrapper) {
        List<LcdpAuditableDTO> itemList = wrapper.parse(LcdpAuditableDTO.class);

        if (itemList.isEmpty()) {
            return null;
        }

        checkAndUpdateVersion(itemList);

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<String> itemIdList = itemList.stream().map(i -> i.getId()).collect(Collectors.toList());

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(itemIdList,
                CoreBpmnParameterDTO.of(wrapper), getBpmnVars(wrapper, selectItemList));

        return runtimeService.oddRejectTask(runtimeSourceList);
    }

    @Mapping(value = "核心工作流异常拒绝（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_ODD_REJECT)
    default String oddRejectTaskWithProgress(RestJsonWrapperBean wrapper) {
        List<LcdpAuditableDTO> itemList = wrapper.parse(LcdpAuditableDTO.class);

        if (itemList.isEmpty()) {
            return null;
        }

        checkAndUpdateVersion(itemList);

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<String> itemIdList = itemList.stream().map(i -> i.getId()).collect(Collectors.toList());

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(itemIdList,
                CoreBpmnParameterDTO.of(wrapper), getBpmnVars(wrapper, selectItemList));

        // 修改单据的ProcessStatus，添加前缀表示数据在执行中
        prependStatusCode(runtimeSourceList, CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG);

        List<ProgressTask> taskList = new ArrayList<>();
        for (CoreBpmnRuntimeSource<LcdpAuditableDTO, String> runtimeSource : runtimeSourceList) {
            ProgressTask task = new ProgressTask(getDescSupplier(runtimeSource).get()) {
                public void run() {
                    List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> innerRuntimeSourceList = Arrays.asList(runtimeSource);

                    runtimeService.oddRejectTask(innerRuntimeSourceList);
                }

                public void rollback() {
                    rollbackTask(runtimeSource);
                }
            };

            taskList.add(task);
        }

        String progressId = StringUtils.randomUUID();

        ProgressService progressService = ApplicationContextHelper.getBean(ProgressService.class);
        progressService.start(progressId,
                I18nHelper.getMessage("GIKAM.BPMN.TASK.ODD_REJECT_NAME", I18nHelper.getMessage(getTable() + ".SERVICE_NAME")),
                taskList);

        return progressId;
    }

    @Mapping(value = "核心工作流异常拒oddRejectTaskWithProgress绝（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_ODD_REJECT)
    default String oddRejectTaskWithProgress(List<String> itemIdList, CoreBpmnParameterDTO parameter) {
        if (itemIdList.isEmpty()) {
            return null;
        }

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(itemIdList,
                parameter, getBpmnVars(null, selectItemList));

        // 修改单据的ProcessStatus，添加前缀表示数据在执行中
        prependStatusCode(runtimeSourceList, CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG);

        List<ProgressTask> taskList = new ArrayList<>();
        for (CoreBpmnRuntimeSource<LcdpAuditableDTO, String> runtimeSource : runtimeSourceList) {
            ProgressTask task = new ProgressTask(getDescSupplier(runtimeSource).get()) {
                public void run() {
                    List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> innerRuntimeSourceList = Arrays.asList(runtimeSource);

                    runtimeService.oddRejectTask(innerRuntimeSourceList);
                }

                public void rollback() {
                    rollbackTask(runtimeSource);
                }
            };

            taskList.add(task);
        }

        String progressId = StringUtils.randomUUID();

        ProgressService progressService = ApplicationContextHelper.getBean(ProgressService.class);
        progressService.start(progressId,
                I18nHelper.getMessage("GIKAM.BPMN.TASK.ODD_REJECT_NAME", I18nHelper.getMessage(getTable() + ".SERVICE_NAME")),
                taskList);

        return progressId;
    }

    @Mapping(value = "核心工作流异常拒绝", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_ODD_REJECT)
    default List<CoreBpmnInstanceStatusDTO<String>> oddRejectTask(List<String> itemIdList, CoreBpmnParameterDTO parameter) {
        if (itemIdList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        List<LcdpAuditableDTO> itemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseAuditableRuntimeSource(itemIdList,
                parameter, getBpmnVars(null, itemList));

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);
        return runtimeService.oddRejectTask(runtimeSourceList);
    }

    @Mapping(value = "核心工作流异常拒绝", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_ODD_REJECT)
    default List<CoreBpmnInstanceStatusDTO<String>> oddRejectTask(List<String> itemIdList, String comment) {
        CoreBpmnParameterDTO parameter = new CoreBpmnParameterDTO();
        parameter.setComment(comment);
        return oddRejectTask(itemIdList, parameter);
    }

    @Mapping(value = "核心工作流回退", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_UNDO)
    default List<CoreBpmnInstanceStatusDTO<String>> undo(RestJsonWrapperBean wrapper) {
        List<LcdpAuditableDTO> itemList = wrapper.parse(LcdpAuditableDTO.class);

        if (itemList.isEmpty()) {
            return null;
        }

        checkAndUpdateVersion(itemList);

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<String> itemIdList = itemList.stream().map(i -> i.getId()).collect(Collectors.toList());

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseUndoableRuntimeSource(itemIdList,
                CoreBpmnParameterDTO.of(wrapper), getBpmnVars(wrapper, selectItemList));

        return runtimeService.undo(runtimeSourceList);
    }

    @Mapping(value = "核心工作流回退（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_UNDO)
    default String undoWithProgress(RestJsonWrapperBean wrapper) {
        List<LcdpAuditableDTO> itemList = wrapper.parse(LcdpAuditableDTO.class);

        if (itemList.isEmpty()) {
            return null;
        }

        checkAndUpdateVersion(itemList);

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<String> itemIdList = itemList.stream().map(i -> i.getId()).collect(Collectors.toList());

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseUndoableRuntimeSource(itemIdList,
                CoreBpmnParameterDTO.of(wrapper), getBpmnVars(wrapper, selectItemList));

        // 修改单据的ProcessStatus，添加前缀表示数据在执行中
        prependStatusCode(runtimeSourceList, CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG);

        List<ProgressTask> taskList = new ArrayList<>();
        for (CoreBpmnRuntimeSource<LcdpAuditableDTO, String> runtimeSource : runtimeSourceList) {
            ProgressTask task = new ProgressTask(getDescSupplier(runtimeSource).get()) {
                public void run() {
                    List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> innerRuntimeSourceList = Arrays.asList(runtimeSource);

                    runtimeService.undo(innerRuntimeSourceList);
                }

                public void rollback() {
                    rollbackTask(runtimeSource);
                }
            };

            taskList.add(task);
        }

        String progressId = StringUtils.randomUUID();

        ProgressService progressService = ApplicationContextHelper.getBean(ProgressService.class);
        progressService.start(progressId,
                I18nHelper.getMessage("GIKAM.BPMN.TASK.UNDO_NAME", I18nHelper.getMessage(getTable() + ".SERVICE_NAME")),
                taskList);

        return progressId;
    }

    @Mapping(value = "核心工作流回退（进度监控）", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_UNDO)
    default String undoWithProgress(List<String> itemIdList, CoreBpmnParameterDTO parameter) {
        if (itemIdList.isEmpty()) {
            return null;
        }

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        List<LcdpAuditableDTO> selectItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseUndoableRuntimeSource(itemIdList,
                parameter, getBpmnVars(null, selectItemList));

        // 修改单据的ProcessStatus，添加前缀表示数据在执行中
        prependStatusCode(runtimeSourceList, CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG);

        List<ProgressTask> taskList = new ArrayList<>();
        for (CoreBpmnRuntimeSource<LcdpAuditableDTO, String> runtimeSource : runtimeSourceList) {
            ProgressTask task = new ProgressTask(getDescSupplier(runtimeSource).get()) {
                public void run() {
                    List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> innerRuntimeSourceList = Arrays.asList(runtimeSource);

                    runtimeService.undo(innerRuntimeSourceList);
                }

                public void rollback() {
                    rollbackTask(runtimeSource);
                }
            };

            taskList.add(task);
        }

        String progressId = StringUtils.randomUUID();

        ProgressService progressService = ApplicationContextHelper.getBean(ProgressService.class);
        progressService.start(progressId,
                I18nHelper.getMessage("GIKAM.BPMN.TASK.UNDO_NAME", I18nHelper.getMessage(getTable() + ".SERVICE_NAME")),
                taskList);

        return progressId;
    }

    @Mapping(value = "核心工作流回退", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_UNDO)
    default List<CoreBpmnInstanceStatusDTO<String>> undo(List<String> itemIdList, CoreBpmnParameterDTO parameter) {
        if (itemIdList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        List<LcdpAuditableDTO> itemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList = parseUndoableRuntimeSource(itemIdList,
                parameter, getBpmnVars(null, itemList));

        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);
        return runtimeService.undo(runtimeSourceList);
    }

    @Mapping(value = "核心工作流回退", type = MappingType.UPDATE)
    @Transactional
    @Audit(AuditConstant.PROCESS_UNDO)
    default List<CoreBpmnInstanceStatusDTO<String>> undo(List<String> itemIdList, String comment) {
        CoreBpmnParameterDTO parameter = new CoreBpmnParameterDTO();
        parameter.setComment(comment);
        return undo(itemIdList, parameter);
    }

    default CoreBpmnTaskDTO getPreviousTask(String id) {
        Map<String, Object> map = selectById(TableContext.of(getTable()), id);

        // 如果是在提交节点，没有前一个任务节点
        if (StringUtils.startsWithIgnoreCase((String) map.get("PROCESSSTATUS"), ProcessStatus.DRAFT.name())) {
            return null;
        }

        CoreBpmnCommentService commentService = ApplicationContextHelper.getBean(CoreBpmnCommentService.class);

        CoreBpmnCommentBean latestComment = commentService.selectFirstByFilter(SearchFilter.instance().match("TARGETID", CoreBpmnHelper.getTargetId(getTable(), id)).filter(MatchPattern.EQ), Order.desc("ID"));

        if (latestComment == null) {
            return null;
        }

        CoreBpmnTaskDTO task = new CoreBpmnTaskDTO();
        task.setName(latestComment.getTaskName());
        task.setStatusCode(latestComment.getStatusCode());

        return task;
    }


    @Mapping(value = "获取状态信息", type = MappingType.SELECT)
    default CoreServiceStatusDTO selectStatus(RestJsonWrapperBean wrapper) {
        return new CoreServiceStatusDTO();
    }

    @Mapping(value = "核心获取工作流任务节点的状态", type = MappingType.SELECT)
    default Map<String, Object> selectBpmnTaskStatus(RestJsonWrapperBean wrapper) {
        List<String> idList = wrapper.parseId(String.class);

        if (idList.isEmpty()) {
            return new HashMap<>();
        }

        // 是否是提交或审核通过操作，提交或审核通过操作才会去考虑下一级审核人员的策略
        String pass = wrapper.getParamValue("bpmn_pass");

        if ("-1".equals(pass)) { // 撤回
            return new HashMap<>();
        }

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> sourceList = this.parseRuntimeSource(idList, wrapper);

        // 单据流程结束时直接返回
        if (sourceList.stream().anyMatch(s -> ProcessStatus.DONE.name().equalsIgnoreCase(s.getOldItem().getProcessStatus()))) {
            return new HashMap<>();
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

                // 审核通过
                if ("1".equals(pass)) {
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

    default List<LcdpAuditableDTO> getBpmnVarsItemList(List<String> idList) {
        List<Map<String, Object>> mapList = selectListByIdList(TableContext.of(getTable()), idList);

        List<LcdpAuditableDTO> list = new ArrayList<>();
        mapList.stream().forEach(e -> {
            LcdpAuditableDTO auditableDTO = BeanUtils.parse(e, LcdpAuditableDTO.class);

            auditableDTO.setTableName(getTable());

            list.add(auditableDTO);
        });
        return list;
    }

    default Map<String, Map<String, Object>> getBpmnVars(@Nullable RestJsonWrapperBean wrapper, List<LcdpAuditableDTO> itemList) {
        Map<String, Map<String, Object>> map = new HashMap<>();

        List<BeanPropertyDescriptor> propertyDescriptorList = BeanPropertyHelper.getBeanPropertyDescriptorList(LcdpAuditableDTO.class);

        for (LcdpAuditableDTO item : itemList) {
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

            map.put(item.getId(), vars);
        }

        return map;
    }

    /**
     * 获取当前业务描述的{@link Supplier}，该方法必需继承
     */
    default Supplier<String> getDescSupplier(CoreBpmnRuntimeSource<LcdpAuditableDTO, String> source) {
        return () -> ObjectUtils.toString(source.getId());
    }

    //-----------------------------------------------------------
    // 默认方法
    //-----------------------------------------------------------
    default List<String> getChosenAuditorList(RestJsonWrapperBean wrapper) {
        return JSON.parseArray(wrapper.getParamValue("auditor"), String.class);
    }

    default List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> parseRuntimeSource(List<String> itemIdList, RestJsonWrapperBean wrapper) {
        List<LcdpAuditableDTO> selectedItemList = getBpmnVarsItemList(itemIdList);

        Map<String, Map<String, Object>> map = getBpmnVars(wrapper, selectedItemList);

        // 获取工作流的参数
        CoreBpmnParameterDTO parameter = CoreBpmnParameterDTO.of(wrapper);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> sourceList = new ArrayList<>();
        for (LcdpAuditableDTO item : selectedItemList) {


            CoreBpmnRuntimeSource<LcdpAuditableDTO, String> source = CoreBpmnRuntimeSource.of(getTable(),
                    item, parameter.getComment(), map.get(item.getId()));

            source.setDescSupplier(getDescSupplier(source));

            sourceList.add(source);
        }

        boolean draft = selectedItemList.stream().anyMatch(i -> StringUtils.startsWithIgnoreCase(i.getProcessStatus(), ProcessStatus.DRAFT.name()));

        if (draft) {
            List<Pair<String, Long>> procIdList = getBpmnProcIdList(selectedItemList);

            sourceList.forEach(s -> s.setProcId(procIdList.stream().filter(p -> s.getId().equals(p.getFirst())).findAny().get().getSecond()));
        }

        return sourceList;
    }

    default List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> parseSubmittableRuntimeSource(List<String> itemIdList, CoreBpmnParameterDTO parameter, Map<String, Map<String, Object>> replacementMap) {
        List<LcdpAuditableDTO> selectedItemList = getBpmnVarsItemList(itemIdList);

        List<Pair<String, Long>> procIdList = getBpmnProcIdList(selectedItemList);
        String tableName = selectedItemList.get(0).getTableName();

        if (procIdList.isEmpty()) {
            throw new BpmnException("CORE.MODULE.SYS.T_CORE_BPMN_PROC.ENGINE.PROCESS_NOT_EXISTS", I18nHelper.getMessage(tableName + ".SERVICE_NAME"));
        }

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> sourceList = new ArrayList<>();
        for (LcdpAuditableDTO item : selectedItemList) {
            CoreBpmnRuntimeSource<LcdpAuditableDTO, String> source = CoreBpmnRuntimeSource.of(getTable(),
                    item, parameter.getComment(), replacementMap.get(item.getId()));

            Pair<String, Long> pair = procIdList.stream().filter(p -> item.getId().equals(p.getFirst())).findAny().orElse(null);

            if (pair == null) {
                throw new ApplicationRuntimeException("CORE.MODULE.SYS.T_CORE_BPMN_PROC.ENGINE.PROCESS_NOT_EXISTS", I18nHelper.getMessage(tableName + ".SERVICE_NAME"));
            }

            source.setProcId(pair.getSecond());
            source.setDescSupplier(getDescSupplier(source));

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

    default List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> parseAuditableRuntimeSource(List<String> itemIdList, CoreBpmnParameterDTO parameter, Map<String, Map<String, Object>> replacementMap) {
        List<LcdpAuditableDTO> selectedItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> sourceList = new ArrayList<>();
        for (LcdpAuditableDTO item : selectedItemList) {
            CoreBpmnRuntimeSource<LcdpAuditableDTO, String> source = CoreBpmnRuntimeSource.of(getTable(),
                    item, parameter.getComment(), replacementMap.get(item.getId()));

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

            source.setDescSupplier(getDescSupplier(source));

            sourceList.add(source);
        }

        return sourceList;
    }

    default List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> parseUndoableRuntimeSource(List<String> itemIdList, CoreBpmnParameterDTO parameter, Map<String, Map<String, Object>> replacementMap) {
        List<LcdpAuditableDTO> selectedItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> sourceList = new ArrayList<>();
        for (LcdpAuditableDTO item : selectedItemList) {
            CoreBpmnRuntimeSource<LcdpAuditableDTO, String> source = CoreBpmnRuntimeSource.of(getTable(),
                    item, parameter.getComment(), replacementMap.get(item.getId()));

            source.setDescSupplier(getDescSupplier(source));

            sourceList.add(source);
        }

        return sourceList;
    }

    default List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> parseWithdrawableRuntimeSource(List<String> itemIdList, CoreBpmnParameterDTO parameter, Map<String, Map<String, Object>> replacementMap) {
        List<LcdpAuditableDTO> selectedItemList = getBpmnVarsItemList(itemIdList);

        List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> sourceList = new ArrayList<>();
        for (LcdpAuditableDTO item : selectedItemList) {
            CoreBpmnRuntimeSource<LcdpAuditableDTO, String> source = CoreBpmnRuntimeSource.of(getTable(),
                    item, parameter.getComment(), replacementMap.get(item.getId()));

            source.setDescSupplier(getDescSupplier(source));

            sourceList.add(source);
        }

        return sourceList;
    }

    default List<Pair<String, Long>> getBpmnProcIdList(List<LcdpAuditableDTO> itemList) {
        CoreBpmnRuntimeService runtimeService = ApplicationContextHelper.getBean(CoreBpmnRuntimeService.class);

        Map<String, CoreBpmnProcBean> map = runtimeService.selectRuntimeBpmnProcList(itemList);

        return map.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue().getId())).collect(Collectors.toList());
    }

    //-------------------------------------------------------------------------
    // 工具方法
    //-------------------------------------------------------------------------
    default void checkAndMerge(CoreBpmnTaskStatusDTO taskStatus, CoreBpmnTaskStatusDTO newTaskStatus) {
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

    default void checkAndUpdateVersion(List<LcdpAuditableDTO> itemList) {
        //todo  版本校验

    }

    default void autoPass(List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList, List<CoreBpmnInstanceStatusDTO<String>> instanceStatusList) {
        List<String> autoPassIdList = new ArrayList<>();

        for (int i = 0, j = runtimeSourceList.size(); i < j; i++) {
            CoreBpmnRuntimeSource<LcdpAuditableDTO, String> runtimeSource = runtimeSourceList.get(i);

            if (runtimeSource.isAutoPass()) {
                instanceStatusList.removeIf(s -> s.getId().equals(runtimeSource.getId()));

                autoPassIdList.add(runtimeSource.getId());
            }
        }

        if (!autoPassIdList.isEmpty()) {
            CoreBpmnParameterDTO bpmnParameter = new CoreBpmnParameterDTO();
            bpmnParameter.setComment(I18nHelper.getMessage("GIKAM.BPMN.COMMENT.AUTO_PASS"));

            instanceStatusList.addAll(completeTask(autoPassIdList, bpmnParameter));
        }
    }

    default void prependStatusCode(List<CoreBpmnRuntimeSource<LcdpAuditableDTO, String>> runtimeSourceList, String prefix) {
        List<LcdpAuditableDTO> updateItemList = new ArrayList<>();
        for (CoreBpmnRuntimeSource<LcdpAuditableDTO, String> runtimeSource : runtimeSourceList) {
            LcdpAuditableDTO item = ClassUtils.newInstance(LcdpAuditableDTO.class);
            item.setId(runtimeSource.getId());
            item.setProcessStatus(runtimeSource.getOldItem().getProcessStatus());
            item.setProcessStatus(prefix + item.getProcessStatus());

            updateItemList.add(item);
        }

        update(getTable(), toMapList(updateItemList), "PROCESSSTATUS");
    }

    default void rollbackTask(CoreBpmnRuntimeSource<LcdpAuditableDTO, String> runtimeSource) {
        LcdpAuditableDTO item = runtimeSource.getOldItem();

        if (StringUtils.startsWith(item.getProcessStatus(), CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG)) {
            item.setProcessStatus(StringUtils.removeStart(item.getProcessStatus(), CoreBpmnHelper.PROCESS_STATUS_RUNNING_FLAG));

            getMapDao().update(getTable(), toMap(item), "PROCESSSTATUS");
        }
    }

}
