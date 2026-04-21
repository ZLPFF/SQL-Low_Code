package com.sunwayworld.cloud.module.lcdp.resource.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.base.LcdpResultDTO;
import com.sunwayworld.cloud.module.lcdp.base.service.LcdpBaseService;
import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpGlobalConfigBean;
import com.sunwayworld.cloud.module.lcdp.config.service.LcdpGlobalConfigService;
import com.sunwayworld.cloud.module.lcdp.errorscript.service.LcdpErrorScriptService;
import com.sunwayworld.cloud.module.lcdp.importrecord.bean.LcdpCheckImportDataDTO;
import com.sunwayworld.cloud.module.lcdp.importrecord.service.LcdpRImportRecordDetailService;
import com.sunwayworld.cloud.module.lcdp.importrecord.service.LcdpResourceImportRecordService;
import com.sunwayworld.cloud.module.lcdp.message.sync.LcdpResourceSyncManager;
import com.sunwayworld.cloud.module.lcdp.moduletmpl.service.LcdpModuleTmplService;
import com.sunwayworld.cloud.module.lcdp.pagetmpl.service.LcdpPageTmplService;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpAnalysisResultDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpCompCleanBackBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpHistroyCleanBackBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModulePageCompBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModulePageI18nBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModuleSourceConvertResultDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpPageI18nCodeBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceConvertRecordBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceCheckoutRecordBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpServerScriptMethodBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.ServerScriptType;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpCompCleanBackService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpHistroyCleanBackService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpModuleSourceConvertService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpModulePageCompService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpModulePageI18nService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpPageI18nCodeService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceConvertRecordService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceCheckoutRecordService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceHistoryService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceManageService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourcePageService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceTreeService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpServerScriptMethodService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpViewButtonRoleService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpJavaCodeResolverUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpMapperUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpScriptUtils;
import com.sunwayworld.cloud.module.lcdp.resourcelock.bean.LcdpResourceLockBean;
import com.sunwayworld.cloud.module.lcdp.resourcelock.service.LcdpResourceLockService;
import com.sunwayworld.cloud.module.lcdp.resourceversion.bean.LcdpResourceVersionBean;
import com.sunwayworld.cloud.module.lcdp.resourceversion.service.LcdpResourceVersionService;
import com.sunwayworld.cloud.module.lcdp.submitlog.bean.LcdpSubmitLogBean;
import com.sunwayworld.cloud.module.lcdp.submitlog.service.LcdpSubmitLogService;
import com.sunwayworld.cloud.module.lcdp.support.LcdpScriptTemplateHelper;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableIndexBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.cloud.module.lcdp.table.helper.LcdpTableUtils;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpDatabaseService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableFieldService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableIndexService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpViewService;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.cache.redis.RedisHelper;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.context.SunwayAopContext;
import com.sunwayworld.framework.context.concurrent.GikamConcurrentLocker;
import com.sunwayworld.framework.data.ListChunkIterator;
import com.sunwayworld.framework.data.ResultEntity;
import com.sunwayworld.framework.database.context.instance.EntityHelper;
import com.sunwayworld.framework.database.core.DatabaseManager;
import com.sunwayworld.framework.database.dialect.Dialect;
import com.sunwayworld.framework.database.sql.Order;
import com.sunwayworld.framework.exception.checked.CheckedException;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.exception.core.ApplicationWarningException;
import com.sunwayworld.framework.executor.manager.TaskExecutorManager;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.jdk.core.ClassManager;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.security.bean.LoginUser;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.PersistableHelper;
import com.sunwayworld.framework.support.tree.bean.AbstractTreeNode;
import com.sunwayworld.framework.tenant.TenantContext;
import com.sunwayworld.framework.tenant.TenantManager;
import com.sunwayworld.framework.utils.BeanUtils;
import com.sunwayworld.framework.utils.ClassUtils;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.GzipUtils;
import com.sunwayworld.framework.utils.NumberUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.SpringUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.framework.utils.TransactionUtils;
import com.sunwayworld.module.mdm.user.bean.CoreUserBean;
import com.sunwayworld.module.mdm.user.service.CoreUserService;
import com.sunwayworld.module.design.studio.bean.CoreStudioGridDTO;
import com.sunwayworld.module.sys.page.service.CorePageGridFieldConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@GikamBean
@Repository
public class LcdpResourceManageServiceImpl implements LcdpResourceManageService {
    private static final Logger log = LoggerFactory.getLogger(LcdpResourceManageService.class);

    private static final Pattern FILE_PATTERN = Pattern.compile("'([^']*?/files/lcdp/[^']*?)'");

    @Autowired
    private LcdpViewButtonRoleService viewButtonRoleService;

    @Autowired
    private LcdpResourceService resourceService;
    @Autowired
    private LcdpResourceHistoryService resourceHistoryService;
    @Autowired
    private LcdpResourceCheckoutRecordService checkoutRecordService;
    @Autowired
    private LcdpResourceLockService resourceLockService;
    @Autowired
    private LcdpErrorScriptService errorScriptService;
    @Autowired
    private LcdpServerScriptMethodService scriptMethodService;
    @Autowired
    private LcdpModulePageCompService pageCompService;
    @Autowired
    private LcdpResourceVersionService resourceVersionService;
    @Autowired
    private LcdpSubmitLogService submitLogService;
    @Autowired
    private LcdpModuleTmplService moduleTmplService;
    @Autowired
    private LcdpPageTmplService pageTmplService;
    @Autowired
    private LcdpResourcePageService resourcePageService;
    @Autowired
    private LcdpModulePageI18nService pageI18nService;
    @Autowired
    private LcdpPageI18nCodeService pageI18nCodeService;
    @Autowired
    private LcdpTableService tableService;
    @Autowired
    private LcdpTableFieldService tableFieldService;
    @Autowired
    private LcdpTableIndexService tableIndexService;
    @Autowired
    private LcdpViewService viewService;
    @Autowired
    private LcdpDatabaseService databaseService;
    @Autowired
    private LcdpGlobalConfigService globalConfigService;
    @Autowired
    private CoreUserService coreUserService;
    @Autowired
    private CorePageGridFieldConfigService pageGridFieldConfigService;


    @Autowired
    private TenantManager tenantManager;


    @Autowired
    private LcdpHistroyCleanBackService histroyCleanBackService;

    @Autowired
    private LcdpCompCleanBackService compCleanBackService;

    @Autowired
    private LcdpResourceImportRecordService resourceImportRecordService;

    @Autowired
    private LcdpRImportRecordDetailService resourceImportRecordDetailService;

    @Autowired
    private LcdpModuleSourceConvertService moduleSourceConvertService;
    @Autowired
    private LcdpResourceConvertRecordService resourceConvertRecordService;
    @Autowired
    private LcdpResourceTreeService resourceTreeService;

    private static final boolean designCenterTenantFlag = ApplicationContextHelper.getEnvironment().getProperty("sunway.design-center-tenant.enabled", Boolean.class, false) && ApplicationContextHelper.isProfileActivated("tenant");

    // 获取当前生效的数据库类型
    private String database = ClassUtils.getPredicatedClasses("com.sunwayworld", c -> !c.isInterface() && Dialect.class.isAssignableFrom(c))
            .stream()
            .map(c -> ((Dialect) ClassUtils.newInstance(c)).getDatabase())
            .filter(d -> ApplicationContextHelper.isProfileActivated(d))
            .findAny()
            .get();

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long insert(RestJsonWrapperBean wrapper) {
        LcdpResourceBean resource = wrapper.parseUnique(LcdpResourceBean.class);
        resource.setId(ApplicationContextHelper.getNextIdentity());

        List<LcdpResourceBean> resourceList = new ArrayList<>();

        List<LcdpResourceHistoryBean> resourceHistoryList = new ArrayList<>();

        if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_MAPPER, resource.getResourceCategory())) { // 新增mapper文件
            String tableName = Objects.requireNonNull(resource.getExt$Item("tablename"));
            String resourceName = getResourceName(resource);
            resource.setResourceName(resourceName);
            LcdpConstant.MAPPER_TMPL_NAME_LIST.forEach(mapperName -> {
                // 只生成自己的数据库对应的mapper信息
                if (LcdpScriptUtils.validateCurrentDBMybatisMapper()
                        && !StringUtils.containsIgnoreCase(mapperName, database)) {
                    return;
                }

                LcdpResourceBean mapper = new LcdpResourceBean();
                mapper.setId(ApplicationContextHelper.getNextIdentity());
                mapper.setResourceName(resource.getResourceName() + mapperName);
                mapper.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_MAPPER);
                mapper.setResourceDesc(resource.getResourceDesc());
                mapper.setParentId(resource.getParentId());

                resourceService.updatePath(mapper);

                resourceList.add(mapper);

                LcdpResourceHistoryBean mapperHistory = new LcdpResourceHistoryBean();
                mapperHistory.setId(ApplicationContextHelper.getNextIdentity());
                mapperHistory.setResourceName(mapper.getResourceName());
                mapperHistory.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_MAPPER);
                mapperHistory.setResourceDesc(resource.getResourceDesc());
                mapperHistory.setPath(mapper.getPath());
                mapperHistory.setResourceId(mapper.getId());
                mapperHistory.setContent(LcdpScriptTemplateHelper.generateMapper(mapper.getPath(), tableName, mapperName, null));

                resourceService.initResourceHistory(mapperHistory);

                resourceHistoryList.add(mapperHistory);
            });

            resourceHistoryService.getDao().fastInsert(resourceHistoryList);
        } else {
            resourceList.add(resource);
        }

        // 处理view、js、java源代码
        if (LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(resource.getResourceCategory())
                && !StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_MAPPER, resource.getResourceCategory())) {
            Long moduleId = resource.getParentId();

            // 当新增页面及前后端脚本时更新处理资源路径
            resourceService.updatePath(resource);

            LcdpResourceHistoryBean resourceHistory = new LcdpResourceHistoryBean();
            //复制主表数据到历史表
            BeanUtils.copyProperties(resource, resourceHistory);
            resourceHistory.setId(ApplicationContextHelper.getNextIdentity());
            resourceHistory.setResourceId(resource.getId());

            resourceService.initResourceHistory(resourceHistory);

            // 后端脚本初始化
            if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(resource.getResourceCategory())) {
                String scriptType = resource.getScriptType();
                String scriptSourceType = resource.getScriptSourceType();

                if (StringUtils.equals(ServerScriptType.service.name(), scriptType)) {
                    String resourceName = getResourceName(resource);
                    resource.setResourceName(resourceName);
                    resourceHistory.setResourceName(resourceName);
                }
                resource.setScriptType(scriptType);

                // 更新版本偏差
                resourceService.updateVersionOffset(resource, resourceHistory);

                resourceHistory.setScriptType(scriptType);
                resourceHistory.setPath(resource.getPath());

                String content = null;
                if (StringUtils.equals(ServerScriptType.service.name(), scriptType)) {
                    String tableName = Objects.requireNonNull(resource.getExt$Item("tablename"));

                    LcdpConstant.MAPPER_TMPL_NAME_LIST.forEach(mapperName -> {
                        // 只生成自己的数据库对应的mapper信息
                        if (LcdpScriptUtils.validateCurrentDBMybatisMapper()
                                && !StringUtils.containsIgnoreCase(mapperName, database)) {
                            return;
                        }

                        LcdpResourceBean mapper = new LcdpResourceBean();
                        mapper.setId(ApplicationContextHelper.getNextIdentity());
                        mapper.setResourceName(StringUtils.replaceLast(resource.getResourceName(), "Service", "") + mapperName);
                        mapper.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_MAPPER);
                        mapper.setResourceDesc(resource.getResourceDesc());
                        mapper.setParentId(moduleId);

                        resourceService.updatePath(mapper);

                        resourceList.add(mapper);

                        LcdpResourceHistoryBean mapperHistory = new LcdpResourceHistoryBean();
                        mapperHistory.setId(ApplicationContextHelper.getNextIdentity());
                        mapperHistory.setResourceName(mapper.getResourceName());
                        mapperHistory.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_MAPPER);
                        mapperHistory.setPath(mapper.getPath());
                        mapperHistory.setResourceId(mapper.getId());
                        mapperHistory.setResourceDesc(resource.getResourceDesc());

                        resourceService.initResourceHistory(mapperHistory);

                        mapperHistory.setContent(LcdpScriptTemplateHelper.generateMapper(mapper.getPath(), tableName, mapperName, null));

                        resourceHistoryList.add(mapperHistory);
                    });

                    content = StringUtils.equals("table", scriptSourceType) ?
                            LcdpScriptTemplateHelper.generateJavaScript(resourceHistory.getPath(),
                                    resourceHistory.getResourceName(),
                                    tableName,
                                    StringUtils.replaceLast(resourceHistory.getPath().replace("server.", "mapper."), "Service", "Mapper"),
                                    null)
                            : LcdpScriptTemplateHelper.generateViewJavaScript(resourceHistory.getPath(),
                            resourceHistory.getResourceName(),
                            tableName,
                            StringUtils.replaceLast(resourceHistory.getPath().replace("server.", "mapper."), "Service",
                                    "Mapper"));


                    resourceHistory.setContent(content);
                } else {
                    content = LcdpScriptTemplateHelper.generateCompJavaScript(resourceHistory.getPath(), scriptType, resourceHistory.getResourceName(), null);

                    resourceHistory.setContent(content);
                }

                //替换脚本类名
                String classContent = LcdpJavaCodeResolverUtils.getClassContent(content,
                        resourceHistory.getResourceName(),
                        1L, 1L,
                        resourceHistory.getVersionOffset());

                resourceHistory.setClassContent(classContent);
            }
            resourceHistoryList.add(resourceHistory);

            resourceHistoryService.getDao().fastInsert(resourceHistoryList);
        }

        resourceService.getDao().fastInsert(resourceList);

        postInsertResource(resourceList);

        return resourceList.get(0).getId();
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long insertByModuleTmpl(RestJsonWrapperBean wrapper) {
        List<LcdpResourceBean> resourceList = moduleTmplService.insertByModuleTmpl(wrapper);

        postInsertResource(resourceList);

        LcdpResourceBean moduleResource = resourceList.stream().filter(r -> LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(r.getResourceCategory())).findAny().get();

        return moduleResource.getId();
    }


    @Override
    @Transactional
    @Audit(AuditConstant.SAVE)
    public LcdpResultDTO convertPage(Long id, RestJsonWrapperBean wrapper) {
        LcdpResourceBean resource = wrapper.parseUnique(LcdpResourceBean.class);
        resource.setContent(GzipUtils.decompress(resource.getContent()));

        LcdpResourceHistoryBean resourceHistory = resourceHistoryService.selectFirstByFilter(SearchFilter.instance()
                .match("RESOURCEID", resource.getId()).filter(MatchPattern.EQ)
                .match("CREATEDBYID", LocalContextHelper.getLoginUserId()).filter(MatchPattern.SEQ)
                .match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.SEQ));

        //后端脚本保存，保存版本+1
        Long newModifyVersion = resourceHistory.getModifyVersion() + 1;
        resourceHistory.setContent(resource.getContent());
        resourceHistory.setModifyVersion(newModifyVersion);

        List<LcdpModulePageCompBean> insertPageCompList = savePageComps(resourceHistory, resource.getComponents());

        // 保存页面国际化
        if (resource.getI18n() != null) {
            savePageI18n(resourceHistory, resource.getI18n());
        }
        // 保存页面与系统国际化依赖关系
        if (resource.getDependentI18n() != null) {
            savePageDependentI18n(resourceHistory, resource.getDependentI18n());
        }

        resourceHistoryService.getDao().update(resourceHistory, "CONTENT", "MODIFYVERSION", "CLASSCONTENT", "LCDPFILEPATH");

        LcdpResourceSyncManager.sync(resource.getId());

        LcdpResultDTO result = LcdpResultDTO.sucess();

        String msg = SunwayAopContext.getCache(LcdpConstant.LCDP_AUTOMATIC_INSERT_COLIMNS_MSG_KEY);
        if (!StringUtils.isBlank(msg)) {
            result.setMessage(msg);
        }

        return result;


    }

    @Override
    public void dataClean(RestJsonWrapperBean wrapper) {

        //resource  history pagecomp i18n version  submitlog
        List<LcdpResourceBean> allResourceList = resourceService.selectEffectIdList();

        ListChunkIterator<LcdpResourceBean> chunkIterator = ListChunkIterator.of(allResourceList, 200);

        while (chunkIterator.hasNext()) {
            List<Long> resourceIdList = chunkIterator.nextChunk().stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
            List<LcdpResourceBean> resourceList = resourceService.selectListByIds(resourceIdList);
            resourceList.forEach(resource -> {
                resource.setEffectVersion(1L);
            });

            List<LcdpResourceHistoryBean> historyList = resourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", resourceIdList).filter(MatchPattern.OR));

            List<LcdpResourceHistoryBean> effectHistoryList = historyList.stream().filter(h -> StringUtils.equals(h.getEffectFlag(), LcdpConstant.EFFECT_FLAG_YES)).collect(Collectors.toList());

            effectHistoryList.forEach(h -> {
                h.setVersion(1L);
            });
            resourceService.getDao().update(resourceList, "EFFECTVERSION");
            resourceHistoryService.getDao().update(effectHistoryList, "VERSION");

            List<LcdpResourceBean> javaList = resourceList.stream().filter(r -> r.getResourceCategory().equals(LcdpConstant.RESOURCE_CATEGORY_JAVA)).collect(Collectors.toList());
            dealJavaListForDataClean(javaList);


            List<LcdpResourceHistoryBean> deleteHistoryList = historyList.stream().filter(h -> StringUtils.equals(h.getEffectFlag(), LcdpConstant.EFFECT_FLAG_NO)).collect(Collectors.toList());
            List<LcdpHistroyCleanBackBean> historyBackList = deleteHistoryList.stream().map(hist -> {
                LcdpHistroyCleanBackBean histroyCleanBack = new LcdpHistroyCleanBackBean();
                BeanUtils.copyProperties(hist, histroyCleanBack);
                return histroyCleanBack;
            }).collect(Collectors.toList());
            ListChunkIterator<LcdpHistroyCleanBackBean> hisChunkIterator = ListChunkIterator.of(historyBackList, 100);
            while (hisChunkIterator.hasNext()) {
                List<LcdpHistroyCleanBackBean> hisCleanList = hisChunkIterator.nextChunk();
                histroyCleanBackService.getDao().insert(hisCleanList);
            }


            List<LcdpResourceHistoryBean> viewHistoryList = deleteHistoryList.stream().filter(h -> h.getResourceCategory().equals(LcdpConstant.RESOURCE_CATEGORY_VIEW)).collect(Collectors.toList());
            if (!viewHistoryList.isEmpty()) {
                List<Long> viewHistoryIdList = viewHistoryList.stream().map(LcdpResourceHistoryBean::getId).collect(Collectors.toList());
                List<LcdpModulePageCompBean> pageCompList = pageCompService.selectListByFilter(SearchFilter.instance().match("modulePageHistoryId", viewHistoryIdList).filter(MatchPattern.OR));
                List<LcdpCompCleanBackBean> compBackList = pageCompList.stream().map(comp -> {
                    LcdpCompCleanBackBean compCleanBack = new LcdpCompCleanBackBean();
                    BeanUtils.copyProperties(comp, compCleanBack);
                    return compCleanBack;
                }).collect(Collectors.toList());
                ListChunkIterator<LcdpCompCleanBackBean> compChunkIterator = ListChunkIterator.of(compBackList, 100);
                while (compChunkIterator.hasNext()) {
                    List<LcdpCompCleanBackBean> compCleanList = compChunkIterator.nextChunk();
                    compCleanBackService.getDao().insert(compCleanList);
                }
                pageCompService.getDao().deleteByIdList(pageCompList.stream().map(LcdpModulePageCompBean::getId).collect(Collectors.toList()));
                List<LcdpModulePageI18nBean> pageI18nList = pageI18nService.selectListByFilter(SearchFilter.instance().match("modulePageHistoryId", viewHistoryIdList).filter(MatchPattern.OR));
                pageI18nService.getDao().deleteByIdList(pageI18nList.stream().map(LcdpModulePageI18nBean::getId).collect(Collectors.toList()));
                List<LcdpPageI18nCodeBean> I18nCodeList = pageI18nCodeService.selectListByFilter(SearchFilter.instance().match("modulePageHistoryId", viewHistoryIdList).filter(MatchPattern.OR));
                pageI18nCodeService.getDao().deleteByIdList(I18nCodeList.stream().map(LcdpPageI18nCodeBean::getId).collect(Collectors.toList()));
            }
            resourceHistoryService.getDao().deleteByIdList(deleteHistoryList.stream().map(LcdpResourceHistoryBean::getId).collect(Collectors.toList()));
        }

        //清除表的记录
        List<LcdpTableBean> tableList = tableService.selectAll();
        List<Long> tableIdList = tableList.stream().map(LcdpTableBean::getId).collect(Collectors.toList());
        tableService.getDao().deleteByIdList(tableIdList);
        //索引
        List<LcdpTableIndexBean> indexList = tableIndexService.selectAll();
        List<Long> indexIdList = indexList.stream().map(LcdpTableIndexBean::getId).collect(Collectors.toList());
        tableIndexService.getDao().deleteByIdList(indexIdList);
        //字段
        List<LcdpTableFieldBean> fieldList = tableFieldService.selectAll();
        List<Long> fieldIdList = fieldList.stream().map(LcdpTableFieldBean::getId).collect(Collectors.toList());
        tableFieldService.getDao().deleteByIdList(fieldIdList);

        List<LcdpResourceVersionBean> versionList = resourceVersionService.selectAll();
        List<Long> versionIdList = versionList.stream().map(LcdpResourceVersionBean::getId).collect(Collectors.toList());
        resourceVersionService.getDao().deleteByIdList(versionIdList);

        List<LcdpSubmitLogBean> submitList = submitLogService.selectAll();
        List<Long> submitIdList = submitList.stream().map(LcdpSubmitLogBean::getId).collect(Collectors.toList());
        submitLogService.getDao().deleteByIdList(submitIdList);

        List<LcdpViewBean> viewList = viewService.selectAll();
        List<Long> viewIdList = viewList.stream().map(LcdpViewBean::getId).collect(Collectors.toList());
        viewService.getDao().deleteByIdList(viewIdList);


    }


    @Override
    public ResultEntity validateDataClean(RestJsonWrapperBean wrapper) {
        List<LcdpResourceCheckoutRecordBean> rsCheckoutList = checkoutRecordService.selectListByFilter(SearchFilter.instance().match("resourceCategory", Arrays.asList("view", "java", "mapper", "js", "table", "view")).filter(MatchPattern.OR));

        int count = rsCheckoutList.size();
        if (count > 0) {
            List<String> checkoutUserIdList = rsCheckoutList.stream().map(e -> e.getCheckoutUserId()).distinct().filter(e -> !StringUtils.isEmpty(e)).collect(Collectors.toList());

            return ResultEntity.fail(I18nHelper.getMessage("LCDP.TIP.EXIST_CHECKOUT_USER_DATA") + ":" + StringUtils.join(checkoutUserIdList, ","));
        }
        return ResultEntity.success();
    }

    @Override
    public LcdpModuleSourceConvertResultDTO convertModuleSource(Long moduleId, RestJsonWrapperBean wrapper) {
        return moduleSourceConvertService.convert(moduleId, wrapper);
    }

    @Override
    public List<LcdpResourceTreeNodeDTO> selectConvertRecordTree(Long moduleId, RestJsonWrapperBean wrapper) {
        LcdpResourceBean module = resourceService.selectById(moduleId);
        if (module == null || !StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_MODULE, module.getResourceCategory())) {
            throw new ApplicationRuntimeException("模块不存在或不是模块资源，moduleId=" + moduleId);
        }

        return buildConvertRecordTree(moduleId, wrapper, null, true);
    }

    @Override
    public List<LcdpResourceTreeNodeDTO> selectConvertRecordOverviewTree(RestJsonWrapperBean wrapper) {
        List<LcdpResourceTreeNodeDTO> rootTreeNodeList = resourceTreeService.selectTree("root",
                wrapper == null ? RestJsonWrapperBean.newEmpty() : wrapper);
        if (CollectionUtils.isEmpty(rootTreeNodeList)) {
            return CollectionUtils.emptyList();
        }

        List<LcdpResourceConvertRecordBean> recordList = resourceConvertRecordService.selectListByFilter(SearchFilter.instance()
                .match("RESOURCEID", null).filter(MatchPattern.DIFFER));
        Map<Long, LcdpResourceConvertRecordBean> recordMap = CollectionUtils.isEmpty(recordList)
                ? new LinkedHashMap<>()
                : recordList.stream()
                        .filter(item -> item.getResourceId() != null)
                        .collect(Collectors.toMap(LcdpResourceConvertRecordBean::getResourceId, item -> item, (left, right) -> left, LinkedHashMap::new));

        List<LcdpResourceTreeNodeDTO> overviewTreeNodeList = new ArrayList<>();
        for (LcdpResourceTreeNodeDTO rootNode : rootTreeNodeList) {
            if (!NumberUtils.isNumber(rootNode.getId())) {
                overviewTreeNodeList.add(rootNode);
                continue;
            }

            List<LcdpResourceTreeNodeDTO> categoryTreeNodeList = buildConvertRecordTree(NumberUtils.parseLong(rootNode.getId()), wrapper, recordMap, false);
            if (CollectionUtils.isEmpty(categoryTreeNodeList)) {
                overviewTreeNodeList.add(rootNode);
                continue;
            }
            overviewTreeNodeList.addAll(categoryTreeNodeList);
        }
        return overviewTreeNodeList;
    }

    private List<LcdpResourceTreeNodeDTO> buildConvertRecordTree(Long rootResourceId, RestJsonWrapperBean wrapper,
            Map<Long, LcdpResourceConvertRecordBean> recordMap, boolean filterConvertedOnly) {
        RestJsonWrapperBean actualWrapper = wrapper == null ? RestJsonWrapperBean.newEmpty() : wrapper;
        actualWrapper.setParamValue("allDownward", Constant.YES);
        List<LcdpResourceTreeNodeDTO> treeNodeList = resourceTreeService.selectTree(String.valueOf(rootResourceId), actualWrapper);
        if (CollectionUtils.isEmpty(treeNodeList)) {
            return CollectionUtils.emptyList();
        }

        List<Long> resourceIdList = new ArrayList<>();
        collectConvertTreeResourceIds(treeNodeList, resourceIdList);
        Map<Long, LcdpResourceConvertRecordBean> actualRecordMap = recordMap;
        if (actualRecordMap == null) {
            actualRecordMap = selectConvertRecordMap(resourceIdList);
        }
        if (filterConvertedOnly && CollectionUtils.isEmpty(actualRecordMap)) {
            return CollectionUtils.emptyList();
        }

        fillConvertRecordInfo(treeNodeList, actualRecordMap);
        treeNodeList.forEach(this::updateConvertSummary);
        return filterConvertedOnly ? filterConvertRecordTree(treeNodeList) : treeNodeList;
    }

    private Map<Long, LcdpResourceConvertRecordBean> selectConvertRecordMap(List<Long> resourceIdList) {
        if (CollectionUtils.isEmpty(resourceIdList)) {
            return new HashMap<>();
        }
        return resourceConvertRecordService.selectListByFilter(SearchFilter.instance()
                        .match("RESOURCEID", resourceIdList).filter(MatchPattern.OR))
                .stream()
                .collect(Collectors.toMap(LcdpResourceConvertRecordBean::getResourceId, item -> item, (left, right) -> left, HashMap::new));
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long insertByPageTmpl(RestJsonWrapperBean wrapper) {
        LcdpResourceBean resource = pageTmplService.insertByPageTmpl(wrapper);

        postInsertResource(Arrays.asList(resource));

        return resource.getId();
    }

    private void collectConvertTreeResourceIds(List<LcdpResourceTreeNodeDTO> treeNodeList, List<Long> resourceIdList) {
        if (CollectionUtils.isEmpty(treeNodeList)) {
            return;
        }

        for (LcdpResourceTreeNodeDTO node : treeNodeList) {
            if (NumberUtils.isNumber(node.getId())) {
                resourceIdList.add(NumberUtils.parseLong(node.getId()));
            }
            collectConvertTreeResourceIds(getConvertTreeChildren(node), resourceIdList);
        }
    }

    private void fillConvertRecordInfo(List<LcdpResourceTreeNodeDTO> treeNodeList, Map<Long, LcdpResourceConvertRecordBean> recordMap) {
        if (CollectionUtils.isEmpty(treeNodeList)) {
            return;
        }

        for (LcdpResourceTreeNodeDTO node : treeNodeList) {
            if (NumberUtils.isNumber(node.getId())) {
                LcdpResourceConvertRecordBean record = recordMap.get(NumberUtils.parseLong(node.getId()));
                if (record != null) {
                    node.setConvertStatus("converted");
                    node.setConvertOutputRoot(record.getOutputRoot());
                    node.setConvertGeneratedFiles(JSON.parseArray(record.getGeneratedFiles(), String.class));
                    node.setConvertLastUpdatedByName(record.getLastUpdatedByName());
                    node.setConvertLastUpdatedTime(record.getLastUpdatedTime());
                }
            }
            fillConvertRecordInfo(getConvertTreeChildren(node), recordMap);
        }
    }

    private int[] updateConvertSummary(LcdpResourceTreeNodeDTO node) {
        List<LcdpResourceTreeNodeDTO> children = getConvertTreeChildren(node);
        if (CollectionUtils.isEmpty(children)) {
            int totalCount = isConvertibleResource(node) ? 1 : 0;
            int convertedCount = StringUtils.equals(node.getConvertStatus(), "converted") ? 1 : 0;
            if (totalCount > 0) {
                node.setConvertStatus(convertedCount > 0 ? "converted" : "none");
            }
            node.setConvertResourceCount(convertedCount);
            node.setConvertTotalResourceCount(totalCount);
            return new int[] {convertedCount, totalCount};
        }

        int convertedCount = 0;
        int totalCount = 0;
        for (LcdpResourceTreeNodeDTO child : children) {
            int[] childSummary = updateConvertSummary(child);
            convertedCount += childSummary[0];
            totalCount += childSummary[1];
        }

        node.setConvertResourceCount(convertedCount);
        node.setConvertTotalResourceCount(totalCount);
        if (totalCount > 0) {
            if (convertedCount == 0) {
                node.setConvertStatus("none");
            } else if (convertedCount == totalCount) {
                node.setConvertStatus("converted");
            } else {
                node.setConvertStatus("partial");
            }
        }
        return new int[] {convertedCount, totalCount};
    }

    private List<LcdpResourceTreeNodeDTO> filterConvertRecordTree(List<LcdpResourceTreeNodeDTO> treeNodeList) {
        if (CollectionUtils.isEmpty(treeNodeList)) {
            return CollectionUtils.emptyList();
        }

        List<LcdpResourceTreeNodeDTO> filteredNodeList = new ArrayList<>();
        for (LcdpResourceTreeNodeDTO node : treeNodeList) {
            List<LcdpResourceTreeNodeDTO> filteredChildren = filterConvertRecordTree(getConvertTreeChildren(node));
            node.setChildren(filteredChildren.stream()
                    .map(AbstractTreeNode.class::cast)
                    .collect(Collectors.toList()));

            if (ObjectUtils.defaultIfNull(node.getConvertResourceCount(), 0) > 0 || CollectionUtils.isNotEmpty(filteredChildren)) {
                filteredNodeList.add(node);
            }
        }
        return filteredNodeList;
    }

    private boolean isConvertibleResource(LcdpResourceTreeNodeDTO node) {
        return LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(node.getType());
    }

    private List<LcdpResourceTreeNodeDTO> getConvertTreeChildren(LcdpResourceTreeNodeDTO node) {
        if (node == null || CollectionUtils.isEmpty(node.getChildren())) {
            return CollectionUtils.emptyList();
        }
        return node.getChildren().stream()
                .map(AbstractTreeNode.class::cast)
                .map(LcdpResourceTreeNodeDTO.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Audit(AuditConstant.SAVE)
    public LcdpResultDTO save(Long id, RestJsonWrapperBean wrapper) {
        String resourceCategory = resourceService.selectColumnById(id, "RESOURCECATEGORY");

        LcdpResourceBean resource = wrapper.parseUnique(LcdpResourceBean.class);

        if (resource.getEffectVersion() != null) {
            Long effectVersion = resourceService.selectColumnById(resource.getId(), "EFFECTVERSION", Long.class);
            if (!Objects.equals(resource.getEffectVersion(), effectVersion)) {
                throw new ApplicationRuntimeException("LCDP.EXCEPTION.CONCURRENT_MODIFICATION");
            }
        }

        if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(resourceCategory)) {
            // 是否编译
            boolean compile = !StringUtils.isBlank(wrapper.getParamValue("compile"));

            return javaSave(resource, compile);
        } else {
            return noJavaSave(resource);
        }
    }

    @Override
    @Transactional
    @Audit(AuditConstant.DELETE)
    public void delete(RestJsonWrapperBean wrapper) {
        List<Long> deleteResourceIdList = wrapper.parseId(Long.class);

        if (deleteResourceIdList.isEmpty()) {
            return;
        }

        List<LcdpResourceBean> resourceList = resourceService.selectListByIds(deleteResourceIdList);

        if (resourceList.isEmpty()) {
            return;
        }

        List<String> selectColumnList = Arrays.asList("ID", "PARENTID", "RESOURCECATEGORY", "CHECKOUTUSERID", "DELETEFLAG", "CATEGORYID", "MODULEID", "EFFECTVERSION", "PATH");

        // 删除有分类时，查询所有子项
        List<LcdpResourceBean> categoryResourceList = resourceList.stream()
                .filter(r -> Objects.equals(r.getResourceCategory(), LcdpConstant.RESOURCE_CATEGORY_CATEGORY))
                .collect(Collectors.toList());
        if (!categoryResourceList.isEmpty()) {
            List<LcdpResourceBean> moduleResourceList = resourceService.getDao().selectListByOneColumnValues(categoryResourceList
                            .stream().map(LcdpResourceBean::getId).collect(Collectors.toList()),
                    "PARENTID", selectColumnList);

            moduleResourceList.forEach(m -> {
                if (LcdpConstant.RESOURCE_DELETED_NO.equals(m.getDeleteFlag())
                        && resourceList.stream().noneMatch(r -> r.getId().equals(m.getId()))) {
                    resourceList.add(m);
                }
            });
        }

        // 删除有模块时，查询所有子项，排除上面已查询的分类下的模块
        List<LcdpResourceBean> moduleResourceList = resourceList.stream()
                .filter(r -> Objects.equals(r.getResourceCategory(), LcdpConstant.RESOURCE_CATEGORY_MODULE))
                .collect(Collectors.toList());
        if (!moduleResourceList.isEmpty()) {
            List<LcdpResourceBean> scriptResourceList = resourceService.getDao().selectListByOneColumnValues(moduleResourceList
                            .stream().map(LcdpResourceBean::getId).collect(Collectors.toList()),
                    "MODULEID", selectColumnList);

            scriptResourceList.forEach(s -> {
                if (LcdpConstant.RESOURCE_DELETED_NO.equals(s.getDeleteFlag())
                        && resourceList.stream().noneMatch(r -> r.getId().equals(s.getId()))) {
                    resourceList.add(s);
                }
            });
        }

        // 非超级管理员，不能删除其他人检出的资源
        String loginUserId = LocalContextHelper.getLoginUserId();
        if (!LcdpConstant.SUPER_ADMIN_ID.equals(loginUserId)) {
            LcdpResourceBean tipResource = resourceList.stream().filter(f -> ObjectUtils.equals(f.getResourceCategory(), LcdpConstant.RESOURCE_CATEGORY_CATEGORY)).findFirst().orElse(null);
            if (tipResource == null) {
                tipResource = resourceList.stream().filter(f -> ObjectUtils.equals(f.getResourceCategory(), LcdpConstant.RESOURCE_CATEGORY_MODULE)).findFirst().orElse(null);
            }

            for (LcdpResourceBean resource : resourceList) {
                if (LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(resource.getResourceCategory())) {
                    if (!StringUtils.isBlank(resource.getCheckoutUserId())
                            && !Objects.equals(loginUserId, resource.getCheckoutUserId())) {
                        if (tipResource == null) {
                            tipResource = resource;
                        }

                        if (resource.getEffectVersion() != null) {
                            throw new ApplicationRuntimeException(I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.NOT_ALLOWED_DELETE_OTHER_CHECKOUT"));
                        } else {
                            throw new ApplicationRuntimeException(I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.NOT_ALLOWED_DELETE_OTHER_CREATE",
                                    tipResource.getResourceName(), resource.getCheckoutUserId()));
                        }
                    }
                }
            }
        }

        // 更新删除标记
        LocalDateTime now = LocalDateTime.now();
        resourceList.forEach(r -> {
            r.setDeleteFlag(LcdpConstant.RESOURCE_DELETED_YES);
            r.setDeletedById(LocalContextHelper.getLoginUserId());
            r.setDeleteTime(now);
        });
        resourceService.getDao().update(resourceList, "DELETEFLAG", "DELETEDBYID", "DELETETIME");

        List<Long> scriptIdList = resourceList.stream()
                .filter(r -> LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(r.getResourceCategory()))
                .map(r -> r.getId())
                .collect(Collectors.toList());

        // 获取所有检出的历史资源
        List<LcdpResourceHistoryBean> resourceHistoryList = resourceHistoryService.selectListByFilter(SearchFilter.instance()
                .match("RESOURCEID", scriptIdList).filter(MatchPattern.OR)
                .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));


        // 删除已加载的java类和mapper
        for (LcdpResourceBean resource : resourceList) {
            LcdpResourceHistoryBean resourceHistory = resourceHistoryList.stream().filter(h -> Objects.equals(h.getResourceId(), resource.getId()) && h.getVersion() == resource.getEffectVersion())
                    .findAny().orElse(null);

            if (resourceHistory == null) {
                continue;
            }

            if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(resource.getResourceCategory())) { // java类
                TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> {
                    LcdpJavaCodeResolverUtils.removeLoadedDevClass(resource);
                    LcdpJavaCodeResolverUtils.removeLoadedProClass(resource);
                });
            } else if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_MAPPER, resource.getResourceCategory())) {
                TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> {
                    LcdpMapperUtils.unloadMapper(resourceHistory.getPath(), false);
                    LcdpMapperUtils.unloadMapper(resourceHistory.getPath(), true);
                });
            }
        }

        // 更新删除标记
        resourceHistoryList.forEach(h -> {
            h.setDeleteFlag(LcdpConstant.RESOURCE_DELETED_YES);
            h.setDeletedById(LocalContextHelper.getLoginUserId());
            h.setDeleteTime(now);
        });
        resourceHistoryService.getDao().update(resourceHistoryList, "DELETEFLAG", "DELETEDBYID", "DELETETIME");

        List<Long> javaScriptIdList = resourceList.stream()
                .filter(r -> Objects.equals(r.getResourceCategory(), LcdpConstant.RESOURCE_CATEGORY_JAVA))
                .map(r -> r.getId())
                .collect(Collectors.toList());

        // java源代码不为空
        if (!javaScriptIdList.isEmpty()) {
            // 删除脚本方法表
            List<LcdpServerScriptMethodBean> scriptMethodList = scriptMethodService.selectListByFilter(SearchFilter.instance()
                    .match("SERVERSCRIPTID", javaScriptIdList).filter(MatchPattern.OR));
            if (!scriptMethodList.isEmpty()) {
                scriptMethodService.getDao().deleteByIdList(scriptMethodList.stream()
                        .map(LcdpServerScriptMethodBean::getId).collect(Collectors.toList()));
            }

            // 删除编译错误记录
            errorScriptService.deleteByScriptIdList(javaScriptIdList);
        }

        // 删除锁定记录
        List<LcdpResourceLockBean> resourceLockList = resourceLockService.selectListByFilter(SearchFilter.instance()
                .match("RESOURCEID", scriptIdList.stream().map(i -> i.toString()).collect(Collectors.toList())).filter(MatchPattern.OR));
        resourceLockService.getDao().deleteByIdList(resourceLockList.stream().map(LcdpResourceLockBean::getId).collect(Collectors.toList()));

        // 删除检出记录
        checkoutRecordService.removeCheckout(resourceList);

        // 版本表设置资源已被删除的标记
        List<LcdpResourceVersionBean> versionList = resourceVersionService.selectListByFilter(SearchFilter.instance()
                .match("RESOURCEID", scriptIdList.stream().map(i -> i.toString()).collect(Collectors.toList())).filter(MatchPattern.OR));
        versionList.forEach(version -> version.setResourceDeleteFlag(LcdpConstant.RESOURCE_DELETED_YES));
        resourceVersionService.getDao().update(versionList, "RESOURCEDELETEFLAG");

        // 添加提交记录
        String commit = wrapper.getParamValue("commit");
        LcdpSubmitLogBean submitLog = new LcdpSubmitLogBean();
        submitLog.setId(ApplicationContextHelper.getNextIdentity());
        submitLog.setCommit(Optional.ofNullable(commit).orElse(I18nHelper.getMessage("LCDP.RESOURCE.DELETED")));
        List<LcdpResourceVersionBean> resourceVersionList = resourceList.stream()
                .filter(re -> LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(re.getResourceCategory())
                        && re.getEffectVersion() != null
                        && re.getEffectVersion() > 0)
                .map(re -> {
                    LcdpResourceVersionBean resourceVersion = new LcdpResourceVersionBean();
                    resourceVersion.setId(ApplicationContextHelper.getNextIdentity());
                    resourceVersion.setLogId(submitLog.getId());
                    resourceVersion.setResourceId(re.getId().toString());
                    String[] path = re.getPath().split("\\.");
                    resourceVersion.setCategoryName(path[0]);
                    resourceVersion.setModuleName(path[1]);
                    resourceVersion.setResourceName(re.getResourceName());
                    resourceVersion.setVersion(re.getEffectVersion());
                    resourceVersion.setResourcePath(re.getPath().replaceAll("\\.", "/"));
                    resourceVersion.setResourceDeleteFlag(LcdpConstant.RESOURCE_DELETED_YES);
                    resourceVersion.setResourceAction(LcdpConstant.RESOURCE_SUBMIT_ACTION_DELETE);
                    resourceVersion.setResourceCategory(re.getResourceCategory());
                    return resourceVersion;
                }).collect(Collectors.toList());

        if (!resourceVersionList.isEmpty()) {
            submitLogService.getDao().insert(submitLog);
            resourceVersionService.getDao().fastInsert(resourceVersionList);
        }

        // 同步数据
        resourceList.forEach(r -> LcdpResourceSyncManager.sync(r.getId()));


        List<LcdpResourceBean> resourceBeanList = resourceList.stream().filter(r -> LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(r.getResourceCategory()) && r.getEffectVersion() != null).collect(Collectors.toList());

        if (!resourceBeanList.isEmpty()) {
            LcdpCheckImportDataDTO dto = new LcdpCheckImportDataDTO();
            dto.setOperation("delete");
            dto.setResourceList(resourceBeanList);
            resourceImportRecordService.checkImportRecord(dto);
        }
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long copyModule(Long moduleId, RestJsonWrapperBean wrapper) {
        Map<String, String> paramMap = wrapper.getParamMap();

        String copyName = paramMap.get("copyName"); // 新的模块名称
        String desc = paramMap.get("desc");
        String parentIdStr = paramMap.get("parentId"); // 目标分类的ID

        List<LcdpResourceBean> resourceList = new ArrayList<>(); // 所有要新增的资源

        Long targetParentId = Long.valueOf(parentIdStr);

        LcdpResourceBean moduleResource = resourceService.selectById(moduleId);

        // 查询所有模块下的脚本资源
        List<LcdpResourceBean> childList = resourceService.selectListByFilter(SearchFilter.instance()
                .match("PARENTID", moduleId).filter(MatchPattern.SEQ)
                .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));

        // 待复制的资源列表，排除不可见的资源
        List<LcdpResourceBean> toCopyChildList = childList.stream().filter(child -> !isInvisible(child)).collect(Collectors.toList());

        // 复制module
        LcdpResourceBean newResource = new LcdpResourceBean();
        BeanUtils.copyProperties(moduleResource, newResource, LcdpConstant.COPY_IGNORE_PROPERTIES);
        newResource.setId(ApplicationContextHelper.getNextIdentity());
        newResource.setParentId(targetParentId);
        newResource.setResourceDesc(Optional.ofNullable(desc).orElse(moduleResource.getResourceDesc()));
        newResource.setResourceName(copyName);
        EntityHelper.assignCreatedElement(newResource);

        resourceService.updatePath(newResource);
        if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(newResource.getResourceCategory())) {
            resourceService.updateVersionOffset(newResource);
        }

        resourceList.add(newResource);

        resourceService.getDao().insert(newResource);

        // 复制脚本资源
        if (!toCopyChildList.isEmpty()) {
            // 新旧资源Id（包括历史资源）的对造表
            Map<Long, Long> resourceIdMapping = new HashMap<>();
            // 新旧path的对造表
            Map<String, String> pathMapping = new HashMap<>();

            List<LcdpResourceBean> newChildList = toCopyChildList.stream().map(oldChild -> {
                LcdpResourceBean newChild = new LcdpResourceBean();
                BeanUtils.copyProperties(oldChild, newChild, "EFFECTVERSION", "CONTENT", "CLASSCONTENT");
                newChild.setId(ApplicationContextHelper.getNextIdentity());
                newChild.setParentId(newResource.getId());

                EntityHelper.assignCreatedElement(newChild);

                resourceIdMapping.put(oldChild.getId(), newChild.getId());

                resourceService.updatePath(newChild);
                if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(newChild.getResourceCategory())) {
                    resourceService.updateVersionOffset(newChild);
                }

                ModuleOperator.updateContent(newChild, oldChild);
                newChild.setClassContent(null);

                pathMapping.put(oldChild.getPath(), newChild.getPath());

                return newChild;
            }).collect(Collectors.toList());

            resourceList.addAll(newChildList);

            resourceService.getDao().fastInsert(newChildList);

            //获取资源路径映射
            Map<Long, String> resourcePathMap = newChildList.stream().collect(Collectors.toMap(LcdpResourceBean::getId, LcdpResourceBean::getPath, (v1, v2) -> v2));

            //准备history表中需要复制的资源
            //1.创建集合来装载需要复制的数据
            List<Long> toCopyChildIdList = toCopyChildList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());

            // 获取最新的历史资源
            List<LcdpResourceHistoryBean> toCopyLatestHistoryList = resourceHistoryService.selectLatestActivatedListByResourceIdList(toCopyChildIdList);

            List<LcdpResourceHistoryBean> insertHistoryList = toCopyLatestHistoryList.stream().map(r -> {
                LcdpResourceHistoryBean insertHistory = new LcdpResourceHistoryBean();
                BeanUtils.copyProperties(r, insertHistory, LcdpConstant.COPY_IGNORE_PROPERTIES);

                insertHistory.setId(ApplicationContextHelper.getNextIdentity());
                insertHistory.setResourceId(resourceIdMapping.get(r.getResourceId()));
                insertHistory.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);
                insertHistory.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_NO);
                insertHistory.setVersion(1L);
                insertHistory.setPath(resourcePathMap.get(resourceIdMapping.get(r.getResourceId())));
                insertHistory.setModifyVersion(r.getModifyVersion());
                ModuleOperator.updateContent(insertHistory, r);
                ModuleOperator.updateClassContent(insertHistory, r);

                if (LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(r.getResourceCategory())) {
                    if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(r.getResourceCategory())) {
                        String classContent = LcdpJavaCodeResolverUtils.getClassContent(insertHistory.getClassContent(),
                                insertHistory.getResourceName(),
                                insertHistory.getVersion(),
                                insertHistory.getModifyVersion(),
                                insertHistory.getVersionOffset());

                        insertHistory.setClassContent(classContent);
                    } else {
                        insertHistory.setClassContent(null);
                    }
                }

                resourceIdMapping.put(r.getId(), insertHistory.getId());

                return insertHistory;
            }).collect(Collectors.toList());

            resourceHistoryService.getDao().fastInsert(insertHistoryList);

            List<LcdpResourceHistoryBean> pageList = toCopyLatestHistoryList.stream()
                    .filter(r -> LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(r.getResourceCategory()))
                    .collect(Collectors.toList());

            if (!pageList.isEmpty()) {
                resourceService.copyPageComps(pageList, resourceIdMapping, pathMapping, false);
            }
        }

        postInsertResource(resourceList);

        //复制子节点
        return newResource.getId();
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long copyScript(Long id, RestJsonWrapperBean wrapper) {
        Map<String, String> paramMap = wrapper.getParamMap();
        String copyName = paramMap.get("copyName");
        String desc = paramMap.get("desc");
        String parentIdStr = paramMap.get("parentId");
        Long targetParentId = Long.valueOf(parentIdStr);

        LcdpResourceBean oldResource = resourceService.selectById(id);

        if (LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(oldResource.getResourceCategory())) {
            if (LcdpConstant.MAPPER_TMPL_NAME_LIST.stream().noneMatch(n -> StringUtils.endsWith(copyName, n))) {
                throw new ApplicationRuntimeException("LCDP.EXCEPTION.INVALID_MAPPER_SUFFIX", copyName);
            }
        }

        LcdpResourceBean newResource = new LcdpResourceBean();
        BeanUtils.copyProperties(oldResource, newResource, LcdpConstant.COPY_IGNORE_PROPERTIES);
        newResource.setId(ApplicationContextHelper.getNextIdentity());
        newResource.setParentId(targetParentId);
        newResource.setResourceName(copyName);
        newResource.setResourceDesc(desc);
        newResource.setEffectVersion(null);

        resourceService.updatePath(newResource);
        if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(newResource.getResourceCategory())) {
            resourceService.updateVersionOffset(newResource);
        }

        ScriptOperator.updateContent(newResource, oldResource);
        newResource.setClassContent(null);
        EntityHelper.assignCreatedElement(newResource);

        resourceService.getDao().insert(newResource);

        copyHistoryResource(id, newResource.getId(), newResource.getPath(), copyName, desc, true);

        postInsertResource(Arrays.asList(newResource));

        return newResource.getId();
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public LcdpResourceDTO checkoutScript(Long id) {
        LcdpResourceDTO resourceDTO = new LcdpResourceDTO();

        LcdpResourceBean resource = null;
        // 先查主表的数据准备复制
        if (ApplicationContextHelper.getEnvironment().getProperty("sunway.lcdp.resource.cache.enabled", Boolean.class, true)) {
            resource = resourceService.selectById(id);
        } else {
            resource = resourceService.getDao().selectListByIds(Arrays.asList(id)).get(0);
        }


        resourceDTO.setType(resource.getResourceCategory());

        LcdpResourceHistoryBean filter = new LcdpResourceHistoryBean();
        filter.setResourceId(id);
        LcdpResourceHistoryBean latestHistory = resourceHistoryService.getDao().selectFirst(filter,
                Arrays.asList("ID", "VERSION"), Order.desc("VERSION"));

        LcdpResourceHistoryBean insertResourceHistory = new LcdpResourceHistoryBean();
        //复制主表数据到历史表
        BeanUtils.copyProperties(resource, insertResourceHistory, LcdpConstant.COPY_IGNORE_PROPERTIES);
        insertResourceHistory.setId(ApplicationContextHelper.getNextIdentity());
        insertResourceHistory.setResourceId(resource.getId());
        insertResourceHistory.setVersion(Optional.ofNullable(latestHistory.getVersion()).orElse(0L) + 1L);
        insertResourceHistory.setModifyVersion(Optional.ofNullable(resource.getModifyVersion()).orElse(0L) + 1L);
        insertResourceHistory.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_NO);
        insertResourceHistory.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);

        if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_JAVA, insertResourceHistory.getResourceCategory())) {
            insertResourceHistory.setModifyVersion(Optional.ofNullable(resource.getModifyVersion()).orElse(0L) + 1L);

            String content = insertResourceHistory.getClassContent();

            //替换脚本类名
            String classContent = LcdpJavaCodeResolverUtils.getClassContent(content,
                    insertResourceHistory.getResourceName(),
                    insertResourceHistory.getVersion(),
                    insertResourceHistory.getModifyVersion(),
                    insertResourceHistory.getVersionOffset());

            insertResourceHistory.setClassContent(classContent);
        }

        resourceHistoryService.getDao().insert(insertResourceHistory);

        LcdpResourceHistoryBean resourceHistory = resourceHistoryService.selectFirstByFilter(SearchFilter.instance()
                .match("RESOURCEID", id).filter(MatchPattern.SEQ)
                .match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.EQ));

        // 判断是不是页面类型 页面类型还需插入页面组件表
        if (LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(resourceHistory.getResourceCategory())) {
            resourceDTO = LcdpResourceDTO.of(resourceHistory);

            Map<Long, Long> resourceIdMapping = new HashMap<>();
            resourceIdMapping.put(resourceHistory.getId(), insertResourceHistory.getId());

            // 复制页面组件
            pageCompService.copy(resourceIdMapping);

            // 复制页面国际化
            pageI18nService.copy(resourceIdMapping);

            // 复制页面系统国际化依赖
            pageI18nCodeService.copy(resourceIdMapping);
        }

        // 设置检出信息
        resource.setCheckoutUserId(LocalContextHelper.getLoginUserId());
        resource.setCheckoutTime(LocalDateTime.now());
        resourceService.getDao().update(resource, "CHECKOUTUSERID", "CHECKOUTTIME");

        // 资源锁定
        resourceLockService.lockResource(Arrays.asList(resource));

        // 添加检出记录表
        checkoutRecordService.checkoutResource(Arrays.asList(resource));

        /**
         * 检出时：
         * 1、Java类，需要编译一次
         * 2、Mapper文件，需要加载
         */
        if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(resource.getResourceCategory())) { // java类，新增时一定要编译一次
            try {
                //检出编译报错影响检出 检出编译报错 后台打印错误日志 不影响检出
                LcdpJavaCodeResolverUtils.loadAndRegisterSourceCode(insertResourceHistory);
            } catch (Exception e) {
                log.error("从历史表中编译资源ID={}, 源代码，异常：{}", resourceHistory.getResourceId(), e.getMessage());
            }
        } else if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_MAPPER, resource.getResourceCategory())) {
            TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> {
                LcdpMapperUtils.loadMapper(insertResourceHistory.getPath(), false, insertResourceHistory.getContent());
            });
        }

        // 同步数据
        LcdpResourceSyncManager.sync(resource.getId());

        LcdpCheckImportDataDTO checkImportRecord = new LcdpCheckImportDataDTO();
        checkImportRecord.setOperation("checkout");
        checkImportRecord.setResourceList(Arrays.asList(resource));
        resourceImportRecordService.checkImportRecord(checkImportRecord);

        return resourceDTO;
    }


    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public void checkoutModule(Long id) {
        GikamConcurrentLocker.isolated("RESOURCE_CHECK_OUT$" + id);

        List<LcdpResourceBean> checkoutableResourceList = resourceService.selectCheckoutableResourceList(Arrays.asList(id));

        // 只检出自己的数据库对应的mapper信息
        if (LcdpScriptUtils.requiredMapperOnly()) {
            checkoutableResourceList.removeIf(r -> LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(r.getResourceCategory())
                    && !StringUtils.endsWithIgnoreCase(r.getResourceName(), database + "Mapper"));
        }

        List<Long> checkoutableResourceIdList = checkoutableResourceList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());

        // 获取已生效的历时资源
        List<LcdpResourceHistoryBean> resourceHistoryList = resourceHistoryService.selectListByFilter(SearchFilter.instance()
                .match("RESOURCEID", checkoutableResourceIdList).filter(MatchPattern.OR)
                .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ)
                .match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.EQ));

        // 获取最大版本
        List<LcdpResourceHistoryBean> maxVersionHistoryList = resourceHistoryService.selectMaxVersionListByResourceIdList(checkoutableResourceIdList);
        Map<Long, Long> historyMaxVersionMap = maxVersionHistoryList.stream().collect(Collectors.toMap(h -> h.getResourceId(), h -> h.getVersion()));

        Map<Long, Long> viewIdMapping = new HashMap<>(); // 页面资源新老ID映射

        //开始准备数据插库
        List<LcdpResourceHistoryBean> insertResourceHistoryList = new ArrayList<>();
        resourceHistoryList.forEach(history -> {
            LcdpResourceHistoryBean insertResourceHistory = new LcdpResourceHistoryBean();
            BeanUtils.copyProperties(history, insertResourceHistory, LcdpConstant.COPY_IGNORE_PROPERTIES);
            insertResourceHistory.setId(ApplicationContextHelper.getNextIdentity());
            insertResourceHistory.setVersion(historyMaxVersionMap.get(history.getResourceId()) + 1L);
            insertResourceHistory.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_NO);
            insertResourceHistory.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);
            if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_JAVA, insertResourceHistory.getResourceCategory())) {
                LcdpResourceBean resource = checkoutableResourceList.stream().filter(r -> r.getId().equals(history.getResourceId())).findAny().get();

                insertResourceHistory.setModifyVersion(Optional.ofNullable(resource.getModifyVersion()).orElse(0L) + 1L);

                String content = insertResourceHistory.getClassContent();

                //替换脚本类名
                String classContent = LcdpJavaCodeResolverUtils.getClassContent(content,
                        insertResourceHistory.getResourceName(),
                        insertResourceHistory.getVersion(),
                        insertResourceHistory.getModifyVersion(),
                        insertResourceHistory.getVersionOffset());
                insertResourceHistory.setClassContent(classContent);
            }

            if (LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(history.getResourceCategory())) {
                viewIdMapping.put(history.getId(), insertResourceHistory.getId());
            }

            insertResourceHistoryList.add(insertResourceHistory);
        });

        resourceHistoryService.getDao().insert(insertResourceHistoryList);

        if (!viewIdMapping.isEmpty()) {
            pageCompService.copy(viewIdMapping);

            // 复制页面国际化
            pageI18nService.copy(viewIdMapping);

            // 复制页面系统国际化依赖
            pageI18nCodeService.copy(viewIdMapping);
        }

        resourceLockService.lockResource(checkoutableResourceList);

        checkoutRecordService.checkoutResource(checkoutableResourceList);

        // 编译并注册到spring容器
        List<LcdpResourceHistoryBean> javaHistoryList = insertResourceHistoryList.stream()
                .filter(h -> LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(h.getResourceCategory()))
                .collect(Collectors.toList());
        LcdpJavaCodeResolverUtils.loadAndRegisterResourceHistoryList(javaHistoryList);

        // 加载mapper文件
        List<LcdpResourceHistoryBean> mapperHistoryList = insertResourceHistoryList.stream()
                .filter(h -> LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(h.getResourceCategory()))
                .collect(Collectors.toList());
        if (!mapperHistoryList.isEmpty()) {
            TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> {
                mapperHistoryList.forEach(h -> LcdpMapperUtils.loadMapper(h.getPath(), false, h.getContent()));
            });
        }

        // 同步数据
        insertResourceHistoryList.forEach(h -> LcdpResourceSyncManager.sync(h.getResourceId()));

        LcdpCheckImportDataDTO checkImportRecord = new LcdpCheckImportDataDTO();
        checkImportRecord.setOperation("checkout");
        checkImportRecord.setResourceList(checkoutableResourceList);
        resourceImportRecordService.checkImportRecord(checkImportRecord);

    }

    @Override
    @Transactional
    public void cancelCheckoutModule(Long id) {
        if (!LocalContextHelper.isUserLogin()) {
            return;
        }

        LcdpResourceBean filter = new LcdpResourceBean();
        filter.setParentId(id);
        filter.setDeleteFlag(LcdpConstant.RESOURCE_DELETED_NO);
        filter.setCheckoutUserId(LocalContextHelper.getLoginUserId());

        List<Long> cancelIdList = resourceService.getDao().selectIdList(filter);

        cancelCheckout(cancelIdList, null, null, false);
        clearModuleCheckoutIfNoCheckedOutChildren(Arrays.asList(id));
    }

    @Override
    @Transactional
    public void cancelCheckout(RestJsonWrapperBean wrapper) {
        List<LcdpResourceLockBean> resourceLockList = wrapper.parse(LcdpResourceLockBean.class);

        List<String> lockResourceIdList = resourceLockList.stream()
                .map(LcdpResourceLockBean::getResourceId)
                .distinct()
                .collect(Collectors.toList());

        List<LcdpResourceLockBean> selectedLockList = resourceLockService.getDao()
                .selectListByOneColumnValues(lockResourceIdList, "RESOURCEID", Arrays.asList("ID", "RESOURCEID", "RESOURCECATEGORY"));


        //资源与表资源分开处理
        List<Long> resourceIdList = new ArrayList<>();
        List<String> tableNameList = new ArrayList<>();
        List<String> viewNameList = new ArrayList<>();

        selectedLockList.forEach(l -> {
            if (LcdpConstant.RESOURCE_CATEGORY_TABLE.equals(l.getResourceCategory())) {
                tableNameList.add(l.getResourceId());
            } else if (LcdpConstant.RESOURCE_CATEGORY_DB_VIEW.equals(l.getResourceCategory())) {
                viewNameList.add(l.getResourceId());
            } else {
                resourceIdList.add(Long.valueOf(l.getResourceId()));
            }
        });

        // 是否来源于检出概览
        boolean overview = !StringUtils.isBlank(wrapper.getParamValue("overview"));

        cancelCheckout(resourceIdList, tableNameList, viewNameList, overview);
    }

    @Override
    @Transactional
    public void submitResource(RestJsonWrapperBean wrapper) {
        List<LcdpResourceVersionBean> resourceVersionList = wrapper.parse(LcdpResourceVersionBean.class);

        if (resourceVersionList.isEmpty()) {
            return;
        }

        // 已提交版本中的数据，全部设置为不生效
        List<String> versionResourceIdList = resourceVersionList.stream().map(LcdpResourceVersionBean::getResourceId)
                .collect(Collectors.toList());

        // 已生效的历史版本
        SearchFilter searchFilter = SearchFilter.instance()
                .match("RESOURCEID", versionResourceIdList).filter(MatchPattern.OR)
                .match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.EQ);
        List<LcdpResourceVersionBean> effectedResourceVersionList = resourceVersionService.selectListByFilter(searchFilter);

        // 已生效的历史版本更新为未生效
        effectedResourceVersionList.forEach(v -> v.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO));
        resourceVersionService.getDao().update(effectedResourceVersionList, "EFFECTFLAG");

        LcdpCheckImportDataDTO checkImportRecord = new LcdpCheckImportDataDTO();

        // 划分待提交的资源、库表、视图
        List<Long> submitResourceIdList = new ArrayList<>();
        List<String> submitGlobalConfigCodeList = new ArrayList<>();
        List<String> submitTableNameList = new ArrayList<>();
        List<String> submitViewNameList = new ArrayList<>();
        resourceVersionList.forEach(version -> {
            if (LcdpConstant.RESOURCE_CATEGORY_TABLE.equals(version.getResourceCategory())) {
                submitTableNameList.add(version.getResourceId());
            } else if (LcdpConstant.RESOURCE_CATEGORY_DB_VIEW.equals(version.getResourceCategory())) {
                submitViewNameList.add(version.getResourceId());
            } else if (LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_JS.equals(version.getResourceCategory())
                    || LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_CSS.equals(version.getResourceCategory())) {
                // 全局js脚本和css样式，必需编辑后才能提交
                LcdpGlobalConfigBean configFilter = new LcdpGlobalConfigBean();
                configFilter.setConfigCode(version.getResourceCategory());
                LcdpGlobalConfigBean latestGlobalConfig = globalConfigService.getDao().selectFirst(configFilter, Arrays.asList("ID", "EFFECTFLAG"), Order.desc("VERSION"));
                if (latestGlobalConfig == null
                        || LcdpConstant.EFFECT_FLAG_YES.equals(latestGlobalConfig.getEffectFlag())) {
                    throw new ApplicationRuntimeException("LCDP.EXCEPTION.UNABLE_SUBMIT_BEFORE_MODIFIED");
                }

                submitGlobalConfigCodeList.add(version.getResourceId());
            } else {
                submitResourceIdList.add(NumberUtils.parseLong(version.getResourceId()));
            }
        });

        // 获取待提交的历史资源
        List<LcdpResourceHistoryBean> submitHistoryList = new ArrayList<>();
        if (!submitResourceIdList.isEmpty()) {
            SearchFilter resourceFilter = SearchFilter.instance().match("RESOURCEID", submitResourceIdList).filter(MatchPattern.OR)
                    .match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.EQ)
                    .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ);
            if (!StringUtils.equals(LcdpConstant.SUPER_ADMIN_ID, LocalContextHelper.getLoginUserId())
                    && !LocalContextHelper.isAdminLogin()) {
                resourceFilter.match("CREATEDBYID", LocalContextHelper.getLoginUserId()).filter(MatchPattern.SEQ);
            }
            submitHistoryList.addAll(resourceHistoryService.selectListByFilter(resourceFilter));
        }

        // 获取待提交的库表
        List<LcdpTableBean> submitTableList = new ArrayList<>();
        if (!submitTableNameList.isEmpty()) {
            SearchFilter tableFilter = SearchFilter.instance()
                    .match("TABLENAME", submitTableNameList).filter(MatchPattern.OR)
                    .match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.EQ);
            if (!StringUtils.equals(LcdpConstant.SUPER_ADMIN_ID, LocalContextHelper.getLoginUserId())
                    && !LocalContextHelper.isAdminLogin()) {
                tableFilter.match("CREATEDBYID", LocalContextHelper.getLoginUserId()).filter(MatchPattern.SEQ);
            }
            submitTableList.addAll(tableService.selectListByFilter(tableFilter));

            checkImportRecord.setTableList(submitTableList);
        }

        // 获取待提交的视图
        List<LcdpViewBean> submitViewList = new ArrayList<>();
        if (!submitViewNameList.isEmpty()) {
            SearchFilter viewFilter = SearchFilter.instance()
                    .match("VIEWNAME", submitViewNameList).filter(MatchPattern.OR)
                    .match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.EQ);
            if (!StringUtils.equals(LcdpConstant.SUPER_ADMIN_ID, LocalContextHelper.getLoginUserId())
                    && !LocalContextHelper.isAdminLogin()) {
                viewFilter.match("CREATEDBYID", LocalContextHelper.getLoginUserId()).filter(MatchPattern.SEQ);
            }
            submitViewList.addAll(viewService.selectListByFilter(viewFilter));
            checkImportRecord.setViewList(submitViewList);
        }

        // 获取待提交的全局配置
        List<LcdpGlobalConfigBean> submitGlobalConfigList = new ArrayList<>();
        if (!submitGlobalConfigCodeList.isEmpty()) {
            for (String configCode : submitGlobalConfigCodeList) {
                SearchFilter globalConfigFilter = SearchFilter.instance()
                        .match("CONFIGCODE", configCode).filter(MatchPattern.EQ)
                        .match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_NO).filter(MatchPattern.EQ);
                submitGlobalConfigList.add(globalConfigService.selectFirstByFilter(globalConfigFilter, Order.desc("VERSION")));

            }
        }

        LcdpSubmitLogBean submitLog = new LcdpSubmitLogBean();
        submitLog.setId(ApplicationContextHelper.getNextIdentity());
        submitLog.setCommit(Optional.ofNullable(wrapper.getParamValue("commit")).orElse(""));

        List<LcdpResourceVersionBean> insertVersionList = new ArrayList<>();

        for (LcdpResourceHistoryBean history : submitHistoryList) {
            insertVersionList.add(historyToVersion(submitLog, history));
        }

        for (LcdpTableBean table : submitTableList) {
            insertVersionList.add(tableToVersion(submitLog, table));
        }

        for (LcdpViewBean view : submitViewList) {
            insertVersionList.add(viewToVersion(submitLog, view));
        }

        for (LcdpGlobalConfigBean globalConfig : submitGlobalConfigList) {
            insertVersionList.add(globalConfigToVersion(submitLog, globalConfig));
        }

        resourceVersionService.getDao().insert(insertVersionList);

        // 视图提交
        if (!submitViewList.isEmpty()) {
            List<LcdpViewBean> physicalViewList = databaseService.selectPhysicalViewInfoList(submitViewList.stream()
                    .map(LcdpViewBean::getViewName).collect(Collectors.toList()));

            submitViewList.forEach(view -> {
                LcdpViewBean matchSubmitView = submitViewList.stream().filter(v -> StringUtils.equalsIgnoreCase(view.getViewName(), v.getViewName()))
                        .findFirst().orElse(null);

                if (matchSubmitView == null) {
                    return;
                }

                LcdpViewBean matchPhysicalView = physicalViewList.stream().filter(v -> StringUtils.equalsIgnoreCase(view.getViewName(), v.getViewName()))
                        .findFirst().orElse(null);

                databaseService.testSelectStatement(view.getSelectStatement());

                if (ObjectUtils.isEmpty(matchPhysicalView)) {
                    databaseService.createPhysicalView(matchSubmitView);
                } else {
                    databaseService.alterPhysicalView(matchSubmitView);
                }

                view.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_YES);
            });

            viewService.getDao().update(submitViewList, "SUBMITFLAG");
        }

        // 表提交
        if (!submitTableList.isEmpty()) {
            //查出当前系统对应物理表信息
            List<LcdpTableDTO> physicalTableList = databaseService.selectPhysicalTableInfoList(submitTableList.stream()
                    .map(LcdpTableBean::getTableName).collect(Collectors.toList()));

            //查出对应待提交版本表信息
            List<LcdpTableDTO> submitTableDTOList = tableService.selectTableInfoList(submitTableList.stream().map(LcdpTableBean::getId).collect(Collectors.toList()));

            submitTableList.forEach(table -> {
                LcdpTableDTO submitTable = submitTableDTOList.stream().filter(t -> StringUtils.equals(t.getTableName(), table.getTableName()))
                        .findFirst().orElse(null);

                if (ObjectUtils.isEmpty(submitTable)) {
                    return;
                }

                LcdpTableDTO physicalTable = physicalTableList.stream().filter(t -> StringUtils.equals(t.getTableName(), table.getTableName()))
                        .findAny().orElse(null);
                if (designCenterTenantFlag && StringUtils.isNotEmpty(TenantContext.getTenant())) {
                    tenantManager.call("submitTable", () -> {
                        String sql = "";

                        List<LcdpTableFieldBean> fieldOpsList = LcdpTableUtils.analyzeFieldOps(submitTable.getFieldList(), ObjectUtils.isEmpty(physicalTable) ? null : physicalTable.getFieldList());

                        // 分析出索引操作
                        List<LcdpTableIndexBean> indexOpsList = LcdpTableUtils.analyzeIndexOps(submitTable.getIndexList(), ObjectUtils.isEmpty(physicalTable) ? null : physicalTable.getIndexList());

                        LcdpAnalysisResultDTO validateResult = LcdpTableUtils.validateTable(physicalTable, submitTable, fieldOpsList, indexOpsList);

                        if (!validateResult.getEnable()) {
                            throw new ApplicationWarningException(table.getTableName() + " : " + validateResult.getAnalysisResultList().stream().collect(Collectors.joining(",")));
                        }

                        if (ObjectUtils.isEmpty(physicalTable)) {
                            //创建表
                            sql = tableService.createPhysicalTable(table, fieldOpsList, indexOpsList);
                        } else {
                            // 更改表
                            tableService.alterPhysicalTable(physicalTable, table, fieldOpsList, indexOpsList);

                            // 版本表生成建表sql
                            sql = tableService.generateCreateSql(submitTable);
                        }

                        table.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_YES);
                        table.setSql(sql);
                    });
                } else {
                    String sql = "";

                    List<LcdpTableFieldBean> fieldOpsList = LcdpTableUtils.analyzeFieldOps(submitTable.getFieldList(), ObjectUtils.isEmpty(physicalTable) ? null : physicalTable.getFieldList());

                    // 分析出索引操作
                    List<LcdpTableIndexBean> indexOpsList = LcdpTableUtils.analyzeIndexOps(submitTable.getIndexList(), ObjectUtils.isEmpty(physicalTable) ? null : physicalTable.getIndexList());

                    LcdpAnalysisResultDTO validateResult = LcdpTableUtils.validateTable(physicalTable, submitTable, fieldOpsList, indexOpsList);

                    if (!validateResult.getEnable()) {
                        throw new ApplicationWarningException(table.getTableName() + " : " + validateResult.getAnalysisResultList().stream().collect(Collectors.joining(",")));
                    }

                    if (ObjectUtils.isEmpty(physicalTable)) {
                        //创建表
                        sql = tableService.createPhysicalTable(table, fieldOpsList, indexOpsList);
                    } else {
                        // 更改表
                        tableService.alterPhysicalTable(physicalTable, table, fieldOpsList, indexOpsList);

                        // 版本表生成建表sql
                        sql = tableService.generateCreateSql(submitTable);
                    }

                    table.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_YES);
                    table.setSql(sql);
                }

                // 分析出字段操作

            });

            tableService.getDao().update(submitTableList, "SUBMITFLAG", "SQL");
        }

        if (!submitViewList.isEmpty()
                || !submitTableList.isEmpty()) {
            List<String> nameList = new ArrayList<>();
            submitViewList.forEach(v -> nameList.add(v.getViewName()));
            submitTableList.forEach(t -> nameList.add(t.getTableName()));

            checkoutRecordService.removeCheckoutTableOrView(nameList);

            TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> {
                nameList.forEach(n -> {
                    RedisHelper.evict(DatabaseManager.TABLE_CONTEXT_CACHE_NAME, n);
                    RedisHelper.evict(DatabaseManager.ENTITY_CONTEXT_CACHE_NAME, n);
                });
            });
        }
        List<LcdpResourceBean> pageList = new ArrayList<>();
        submitScriptHistoryList(submitHistoryList, checkImportRecord, pageList);

        // 全局配置
        if (!submitGlobalConfigList.isEmpty()) {
            List<String> configCodeList = submitGlobalConfigList.stream()
                    .map(LcdpGlobalConfigBean::getConfigCode).collect(Collectors.toList());

            // 把已生效的设置为未生效
            SearchFilter effectedFilter = SearchFilter.instance()
                    .match("CONFIGCODE", configCodeList).filter(MatchPattern.OR)
                    .match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.EQ);
            List<LcdpGlobalConfigBean> effectedGlobalConfigList = globalConfigService.selectListByFilter(effectedFilter);
            if (!effectedGlobalConfigList.isEmpty()) {
                effectedGlobalConfigList.forEach(c -> c.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO));
                globalConfigService.getDao().update(effectedGlobalConfigList, "EFFECTFLAG");
            }

            submitGlobalConfigList.forEach(c -> {
                c.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
                c.setCommitLog(submitLog.getCommit());
            });
            LcdpGlobalConfigBean jsConfig = submitGlobalConfigList.stream().filter(c -> StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_JS, c.getConfigCode())).findFirst().orElse(null);
            LcdpGlobalConfigBean cssConfig = submitGlobalConfigList.stream().filter(c -> StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_CSS, c.getConfigCode())).findFirst().orElse(null);
            if (jsConfig != null) {
                checkImportRecord.setJsOperation("submit");
                checkImportRecord.setSysClientJsVersion(jsConfig.getVersion());
            }
            if (cssConfig != null) {
                checkImportRecord.setCssOperation("submit");
                checkImportRecord.setSysClientCssVersion(cssConfig.getVersion());
            }

            globalConfigService.getDao().update(submitGlobalConfigList, "EFFECTFLAG", "COMMITLOG");
        }

        // 解锁（排除全局配置，全局配置没有锁）
        resourceLockService.unLock(insertVersionList.stream()
                .filter(v -> !LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_JS.equals(v.getResourceCategory())
                        && !LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_CSS.equals(v.getResourceCategory()))
                .map(LcdpResourceVersionBean::getResourceId).collect(Collectors.toList()));

        // 添加提交日志
        submitLogService.getDao().insert(submitLog);
        if (!pageList.isEmpty()) {
            try {
                viewButtonRoleService.updatePageInfo(pageList);
            } catch (Exception e) {
                log.error("更新页面信息失败", e);
            }
        }
        checkImportRecord.setOperation("submit");
        resourceImportRecordService.checkImportRecord(checkImportRecord);

    }

    @Override
    @Transactional
    public void submitResourceByHistory(RestJsonWrapperBean wrapper) {
        String userId = wrapper.getParamValue("userId");
        if (StringUtils.isBlank(userId)) {
            throw new ApplicationWarningException("userId不能为空");
        }

        LoginUser loginUser = buildLoginUser(userId);
        try {
            LocalContextHelper.setUserLogin(loginUser);

            List<Long> resourceHistoryIdList = wrapper.parse(LcdpResourceHistoryBean.class).stream()
                    .map(LcdpResourceHistoryBean::getId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (resourceHistoryIdList.isEmpty()) {
                return;
            }

            Map<Long, LcdpResourceHistoryBean> historyIdMap = resourceHistoryService.selectListByIds(resourceHistoryIdList).stream()
                    .collect(Collectors.toMap(LcdpResourceHistoryBean::getId, h -> h, (a, b) -> a));

            List<LcdpResourceHistoryBean> submitHistoryList = resourceHistoryIdList.stream()
                    .map(historyIdMap::get)
                    .filter(Objects::nonNull)
                    .filter(h -> StringUtils.equals(LcdpConstant.SUBMIT_FLAG_NO, h.getSubmitFlag()))
                    .filter(h -> StringUtils.equals(LcdpConstant.RESOURCE_DELETED_NO, h.getDeleteFlag()))
                    .filter(h -> StringUtils.equals(LcdpConstant.SUPER_ADMIN_ID, userId)
                            || StringUtils.equals(userId, h.getCreatedById()))
                    .sorted((a, b) -> {
                        int resourceCompare = a.getResourceId().compareTo(b.getResourceId());
                        if (resourceCompare != 0) {
                            return resourceCompare;
                        }

                        int versionCompare = a.getVersion().compareTo(b.getVersion());
                        if (versionCompare != 0) {
                            return versionCompare;
                        }

                        return a.getId().compareTo(b.getId());
                    })
                    .collect(Collectors.toList());
            if (submitHistoryList.isEmpty()) {
                return;
            }

            List<String> versionResourceIdList = submitHistoryList.stream()
                    .map(h -> h.getResourceId().toString())
                    .distinct()
                    .collect(Collectors.toList());
            List<LcdpResourceVersionBean> effectedResourceVersionList = resourceVersionService.selectListByFilter(SearchFilter.instance()
                    .match("RESOURCEID", versionResourceIdList).filter(MatchPattern.OR)
                    .match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.EQ));
            effectedResourceVersionList.forEach(v -> v.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO));
            resourceVersionService.getDao().update(effectedResourceVersionList, "EFFECTFLAG");

            LcdpCheckImportDataDTO checkImportRecord = new LcdpCheckImportDataDTO();
            LcdpSubmitLogBean submitLog = new LcdpSubmitLogBean();
            submitLog.setId(ApplicationContextHelper.getNextIdentity());
            submitLog.setCommit(Optional.ofNullable(wrapper.getParamValue("commit")).orElse(""));

            List<LcdpResourceVersionBean> insertVersionList = submitHistoryList.stream()
                    .map(history -> historyToVersion(submitLog, history))
                    .collect(Collectors.toList());
            resourceVersionService.getDao().insert(insertVersionList);

            List<LcdpResourceBean> pageList = new ArrayList<>();
            submitScriptHistoryList(submitHistoryList, checkImportRecord, pageList);

            resourceLockService.unLock(insertVersionList.stream()
                    .map(LcdpResourceVersionBean::getResourceId)
                    .collect(Collectors.toList()));

            submitLogService.getDao().insert(submitLog);
            if (!pageList.isEmpty()) {
                try {
                    viewButtonRoleService.updatePageInfo(pageList);
                } catch (Exception e) {
                    log.error("更新页面信息失败", e);
                }
            }
            checkImportRecord.setOperation("submit");
            resourceImportRecordService.checkImportRecord(checkImportRecord);
        } finally {
            LocalContextHelper.removeUserLogin();
        }
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public String createTableServer(LcdpTableDTO tableDTO) {
        LcdpTableBean table = new LcdpTableBean();
        BeanUtils.copyProperties(tableDTO, table);
        table.setId(ApplicationContextHelper.getNextIdentity());
        table.setSubmitFlag(LcdpConstant.EFFECT_FLAG_NO);//创建后需要手动提交
        table.setVersion(1l);//初始化版本为1
        tableService.getDao().insert(table);

        List<LcdpTableIndexBean> indexList = tableDTO.getIndexList().stream().map(e -> {
            LcdpTableIndexBean indexBean = new LcdpTableIndexBean();
            BeanUtils.copyProperties(e, indexBean);
            indexBean.setIndexOperationType(LcdpConstant.FIELD_INDEX_OPS_ADD);
            indexBean.setId(ApplicationContextHelper.getNextIdentity());
            indexBean.setTableName(table.getTableName());
            indexBean.setTableId(table.getId());
            return indexBean;
        }).collect(Collectors.toList());
        tableIndexService.getDao().insert(indexList);

        List<LcdpTableFieldBean> fieldList = tableDTO.getFieldList().stream().map(e -> {
            LcdpTableFieldBean fieldBean = new LcdpTableFieldBean();
            BeanUtils.copyProperties(e, fieldBean);
            fieldBean.setFieldOperationType(LcdpConstant.FIELD_INDEX_OPS_ADD);
            fieldBean.setId(ApplicationContextHelper.getNextIdentity());
            fieldBean.setTableName(table.getTableName());
            fieldBean.setTableId(table.getId());
            return fieldBean;
        }).collect(Collectors.toList());
        tableFieldService.getDao().insert(fieldList);


        try {
            String sql = tableService.createPhysicalTable(table, fieldList, indexList);
            table.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_YES);
            table.setSql(sql);
            tableService.getDao().update(table, "SUBMITFLAG", "SQL");
        } catch (Exception e) {
            return e.getMessage();
        }

        JSONObject successObj = new JSONObject();
        successObj.put("status", "200");
        successObj.put("message", "success");
        return successObj.toJSONString();
    }

    @Override
    @Transactional
    public void revertResource(RestJsonWrapperBean wrapper) {
        List<Long> resourceVersionIdList = wrapper.parseId(Long.class);

        if (resourceVersionIdList.isEmpty()) {
            return;
        }

        LcdpCheckImportDataDTO checkImportDataDTO = new LcdpCheckImportDataDTO();

        List<LcdpResourceVersionBean> resourceVersionList = resourceVersionService.getDao().selectListByIds(resourceVersionIdList);

        List<String> resourceIdStrList = resourceVersionList.stream().map(LcdpResourceVersionBean::getResourceId).collect(Collectors.toList());

        // 生效的历史版本
        List<LcdpResourceVersionBean> effectedResourceVersionList = resourceVersionService.selectListByFilter(SearchFilter.instance()
                .match("RESOURCEID", resourceIdStrList).filter(MatchPattern.OR)
                .match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.EQ));

        // 回滚时，不能回滚到已生效版本
        List<String> unableRevertResourceNameList = resourceVersionList.stream()
                .filter(r -> effectedResourceVersionList.stream().anyMatch(e -> r.getId().equals(e.getId())))
                .map(LcdpResourceVersionBean::getResourceName)
                .collect(Collectors.toList());
        if (!unableRevertResourceNameList.isEmpty()) {
            throw new ApplicationRuntimeException("LCDP.EXCEPTION.UNABLE_REVERT_RESOURCE_TO_EFFECT_VERSION",
                    unableRevertResourceNameList.stream().collect(Collectors.joining(",")));
        }


        // 已生效的历史版本更新为未生效
        effectedResourceVersionList.forEach(v -> v.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO));
        resourceVersionService.getDao().update(effectedResourceVersionList, "EFFECTFLAG");

        String commit = wrapper.getParamValue("commit"); // 回滚意见
        LocalDateTime now = LocalDateTime.now();

        LcdpSubmitLogBean submitLog = new LcdpSubmitLogBean();
        submitLog.setId(ApplicationContextHelper.getNextIdentity());
        submitLog.setCommit(Optional.ofNullable(commit).orElse("") + "(" + I18nHelper.getMessage("LCDP.SUBMIT_LOG.DEFAULT_REVERT_COMMIT_LOG") + ")");
        submitLogService.getDao().insert(submitLog);

        List<LcdpResourceVersionBean> insertVersionList = new ArrayList<>();

        // 脚本
        List<LcdpResourceHistoryBean> historyFilterList = resourceVersionList.stream()
                .filter(v -> LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(v.getResourceCategory()))
                .map(v -> {
                    LcdpResourceHistoryBean filter = new LcdpResourceHistoryBean();
                    filter.setResourceId(NumberUtils.parseLong(v.getResourceId()));
                    filter.setVersion(NumberUtils.parseLong(v.getVersion()));
                    return filter;
                })
                .collect(Collectors.toList());
        if (!historyFilterList.isEmpty()) {
            List<LcdpResourceHistoryBean> resourceHistoryList = resourceHistoryService.getDao()
                    .selectList(historyFilterList, Arrays.asList("RESOURCEID", "VERSION"), CollectionUtils.emptyList());

            List<Long> resourceIdList = resourceHistoryList.stream()
                    .map(LcdpResourceHistoryBean::getResourceId).collect(Collectors.toList());

            List<LcdpResourceBean> resourceList = resourceService.getDao().selectListByIds(resourceIdList);

            // 当有已检出数据时，不允许回滚
            if (resourceList.stream().anyMatch(r -> !StringUtils.isBlank(r.getCheckoutUserId()))) {
                String resourceDesc = resourceList.stream().filter(r -> !StringUtils.isBlank(r.getCheckoutUserId()))
                        .map(r -> r.getResourceDesc())
                        .sorted().collect(Collectors.joining(","));

                throw new ApplicationRuntimeException("LCDP.WARNING.UNABLE_REVERT_CHECKOUTED_RESOURCE", resourceDesc);
            }

            // 获取最大版本
            List<LcdpResourceHistoryBean> maxVersionHistoryList = resourceHistoryService.selectMaxVersionListByResourceIdList(resourceIdList);
            Map<Long, Long> historyMaxVersionMap = maxVersionHistoryList.stream().collect(Collectors.toMap(h -> h.getResourceId(), h -> h.getVersion()));

            // 把所有已生效的改为未生效
            List<LcdpResourceHistoryBean> activatedHistoryList = resourceHistoryService.selectLatestActivatedListByResourceIdList(resourceIdList);
            activatedHistoryList.forEach(h -> h.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO));
            resourceHistoryService.getDao().update(activatedHistoryList, "EFFECTFLAG");

            Map<Long, Long> pageResourceIdMapping = new HashMap<>();

            List<LcdpResourceHistoryBean> oldPageHistoryList = new ArrayList<>();

            List<LcdpResourceHistoryBean> insertHistoryList = new ArrayList<>();

            for (LcdpResourceHistoryBean history : resourceHistoryList) {
                LcdpResourceBean resource = resourceList.stream().filter(r -> r.getId().equals(history.getResourceId())).findAny().get();
                resource.setEffectVersion(Optional.ofNullable(historyMaxVersionMap.get(history.getResourceId())).orElse(0L) + 1L);

                if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(history.getResourceCategory())) {
                    resource.setModifyVersion(Math.max(resource.getModifyVersion(), history.getModifyVersion()) + 1);
                }

                LcdpResourceVersionBean resourceVersion = resourceVersionList.stream()
                        .filter(v -> history.getResourceId().toString().equals(v.getResourceId()))
                        .findAny().get();

                LcdpResourceHistoryBean insertHistory = new LcdpResourceHistoryBean();
                BeanUtils.copyProperties(history, insertHistory, LcdpConstant.COPY_IGNORE_PROPERTIES);
                insertHistory.setId(ApplicationContextHelper.getNextIdentity());
                insertHistory.setVersion(resource.getEffectVersion());
                insertHistory.setCompiledVersion(0L);
                insertHistory.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);

                if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(history.getResourceCategory())) {
                    insertHistory.setModifyVersion(Math.max(resource.getModifyVersion(), history.getModifyVersion()) + 1);

                    // 只需要替换类名，不需要替换脚本内容，因为脚本内容是被替换好了的
                    String classContent = LcdpJavaCodeResolverUtils.getClassContent(history.getContent(),
                            history.getResourceName(),
                            history.getVersion(),
                            history.getModifyVersion(),
                            history.getVersionOffset());
                    insertHistory.setClassContent(classContent);
                }

                insertHistoryList.add(insertHistory);

                LcdpResourceVersionBean insertVersion = new LcdpResourceVersionBean();
                BeanUtils.copyProperties(resourceVersion, insertVersion, LcdpConstant.COPY_IGNORE_PROPERTIES);
                insertVersion.setId(ApplicationContextHelper.getNextIdentity());
                insertVersion.setVersion(resource.getEffectVersion());
                insertVersion.setLogId(submitLog.getId());
                insertVersion.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
                insertVersion.setSubmitTime(now);
                insertVersion.setEditTime(now);
                insertVersion.setResourceAction(LcdpConstant.RESOURCE_SUBMIT_ACTION_REVERT); // 回滚
                insertVersion.setCommit(Optional.ofNullable(commit).orElse("")
                        + "(" + I18nHelper.getMessage("LCDP.VERSION_LOG.DEFAULT_REVERT_COMMIT_LOG",
                        Optional.ofNullable(history.getVersion()).orElse(0L).toString()) + ")");
                insertVersionList.add(insertVersion);

                resource.setContent(insertHistory.getContent());
                if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(history.getResourceCategory())) {
                    String classContent = LcdpJavaCodeResolverUtils.getClassContent(history.getContent(),
                            resource.getResourceName(),
                            resource.getEffectVersion(),
                            null,
                            resource.getVersionOffset());

                    resource.setClassContent(classContent);
                }
                resource.setCheckoutUserId(null);
                resource.setCheckoutTime(null);

                if (LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(history.getResourceCategory())) {
                    oldPageHistoryList.add(history);

                    pageResourceIdMapping.put(history.getResourceId(), history.getResourceId());
                    pageResourceIdMapping.put(history.getId(), insertHistory.getId());
                }
            }

            // 复制页面的组件表
            resourceService.copyPageComps(oldPageHistoryList, pageResourceIdMapping, null, false);

            resourceService.getDao().update(resourceList, "EFFECTVERSION", "MODIFYVERSION",
                    "CONTENT", "CLASSCONTENT", "CHECKOUTUSERID", "CHECKOUTTIME");

            resourceHistoryService.getDao().fastInsert(insertHistoryList);

            checkImportDataDTO.setResourceList(resourceList);
            // 删除之前已生效的类，加载新的类并注册到Spring
            List<LcdpResourceBean> javaSourceList = resourceList.stream()
                    .filter(r -> LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(r.getResourceCategory()))
                    .collect(Collectors.toList());
            javaSourceList.forEach(r -> {
                // 删除开发中的类
                LcdpJavaCodeResolverUtils.removeLoadedDevClass(r);
                // 删除加载的正式的类
                LcdpJavaCodeResolverUtils.removeLoadedProClass(r);
            });

            LcdpJavaCodeResolverUtils.loadAndRegisterResourceist(javaSourceList);

            // Mapper文件更新
            List<LcdpResourceBean> mapperResourceList = resourceList.stream()
                    .filter(r -> LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(r.getResourceCategory())
                            && StringUtils.endsWithIgnoreCase(r.getResourceName(), database + "mapper"))
                    .collect(Collectors.toList());
            if (!mapperResourceList.isEmpty()) {
                TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> mapperResourceList.forEach(r -> {
                    //加载生成mapper
                    LcdpMapperUtils.loadMapper(r.getPath(), true, r.getContent());
                }));
            }

            // 同步
            resourceList.forEach(r -> LcdpResourceSyncManager.sync(r.getId()));
        }

        // 数据库表
        List<LcdpTableBean> tableFilterList = resourceVersionList.stream()
                .filter(v -> LcdpConstant.RESOURCE_CATEGORY_TABLE.contains(v.getResourceCategory()))
                .map(v -> {
                    LcdpTableBean filter = new LcdpTableBean();
                    filter.setTableName(v.getResourceId());
                    filter.setVersion(NumberUtils.parseLong(v.getVersion()));
                    return filter;
                })
                .collect(Collectors.toList());
        if (!tableFilterList.isEmpty()) {
            List<LcdpTableBean> tableList = tableService.getDao()
                    .selectList(tableFilterList, Arrays.asList("TABLENAME", "VERSION"), CollectionUtils.emptyList());

            List<LcdpTableBean> insertTableList = new ArrayList<>();
            List<LcdpTableFieldBean> insertTableFieldList = new ArrayList<>();
            List<LcdpTableIndexBean> insertTableIndexList = new ArrayList<>();
            for (LcdpTableBean table : tableList) {
                LcdpTableBean filter = new LcdpTableBean();
                filter.setTableName(table.getTableName());

                LcdpTableBean latestTable = tableService.getDao().selectFirst(filter, Order.desc("VERSION"));

                if (LcdpConstant.SUBMIT_FLAG_NO.equals(latestTable.getSubmitFlag())) {
                    throw new ApplicationRuntimeException("LCDP.WARNING.UNABLE_REVERT_CHECKOUTED_RESOURCE", latestTable.getTableDesc());
                }

                LcdpTableFieldBean fieldFilter = new LcdpTableFieldBean();
                fieldFilter.setTableId(table.getId());
                List<LcdpTableFieldBean> fieldList = tableFieldService.selectList(fieldFilter);

                LcdpTableIndexBean indexFilter = new LcdpTableIndexBean();
                indexFilter.setTableId(table.getId());
                List<LcdpTableIndexBean> indexList = tableIndexService.selectList(indexFilter);

                LcdpTableBean insertTable = new LcdpTableBean();
                BeanUtils.copyProperties(table, insertTable, LcdpConstant.COPY_IGNORE_PROPERTIES);
                insertTable.setId(ApplicationContextHelper.getNextIdentity());
                insertTable.setVersion(Optional.ofNullable(latestTable.getVersion()).orElse(0L) + 1L);
                insertTable.setSubmitFlag(LcdpConstant.EFFECT_FLAG_YES);

                insertTableList.add(insertTable);

                fieldList.forEach(f -> {
                    f.setId(ApplicationContextHelper.getNextIdentity());
                    f.setTableId(insertTable.getId());
                    PersistableHelper.removeCreatedUserData(f);

                    insertTableFieldList.add(f);
                });

                indexList.forEach(f -> {
                    f.setId(ApplicationContextHelper.getNextIdentity());
                    f.setTableId(insertTable.getId());
                    PersistableHelper.removeCreatedUserData(f);

                    insertTableIndexList.add(f);
                });

                LcdpResourceVersionBean tableVersion = resourceVersionList.stream()
                        .filter(v -> table.getTableName().toString().equals(v.getResourceId()))
                        .findAny().get();

                LcdpResourceVersionBean insertVersion = new LcdpResourceVersionBean();
                BeanUtils.copyProperties(tableVersion, insertVersion, LcdpConstant.COPY_IGNORE_PROPERTIES);
                insertVersion.setId(ApplicationContextHelper.getNextIdentity());
                insertVersion.setVersion(insertTable.getVersion());
                insertVersion.setLogId(submitLog.getId());
                insertVersion.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
                insertVersion.setSubmitTime(now);
                insertVersion.setEditTime(now);
                insertVersion.setResourceAction(LcdpConstant.RESOURCE_SUBMIT_ACTION_REVERT); // 回滚
                insertVersion.setCommit(Optional.ofNullable(commit).orElse("") + "(" + I18nHelper.getMessage("LCDP.VERSION_LOG.DEFAULT_REVERT_COMMIT_LOG",
                        Optional.ofNullable(table.getVersion()).orElse(0L).toString()) + ")");
                insertVersionList.add(insertVersion);
            }

            tableService.getDao().fastInsert(insertTableList);
            tableFieldService.getDao().fastInsert(insertTableFieldList);
            tableIndexService.getDao().fastInsert(insertTableIndexList);

            checkImportDataDTO.setTableList(insertTableList);

        }

        // 数据库视图
        List<LcdpViewBean> viewFilterList = resourceVersionList.stream()
                .filter(v -> LcdpConstant.RESOURCE_CATEGORY_VIEW.contains(v.getResourceCategory()))
                .map(v -> {
                    LcdpViewBean filter = new LcdpViewBean();
                    filter.setTableName(v.getResourceId());
                    filter.setVersion(NumberUtils.parseLong(v.getVersion()));
                    return filter;
                })
                .collect(Collectors.toList());
        if (!tableFilterList.isEmpty()) {
            List<LcdpViewBean> viewList = viewService.getDao()
                    .selectList(viewFilterList, Arrays.asList("TABLENAME", "VERSION"), CollectionUtils.emptyList());

            List<LcdpViewBean> insertViewList = new ArrayList<>();
            for (LcdpViewBean view : viewList) {
                LcdpViewBean filter = new LcdpViewBean();
                filter.setTableName(view.getTableName());

                LcdpViewBean latestView = viewService.getDao().selectFirst(filter, Order.desc("VERSION"));

                if (LcdpConstant.SUBMIT_FLAG_NO.equals(latestView.getSubmitFlag())) {
                    throw new ApplicationRuntimeException("LCDP.WARNING.UNABLE_REVERT_CHECKOUTED_RESOURCE", latestView.getViewName());
                }

                LcdpViewBean insertView = new LcdpViewBean();
                BeanUtils.copyProperties(view, insertView, LcdpConstant.COPY_IGNORE_PROPERTIES);
                insertView.setId(ApplicationContextHelper.getNextIdentity());
                insertView.setVersion(Optional.ofNullable(latestView.getVersion()).orElse(0L) + 1L);
                insertView.setSubmitFlag(LcdpConstant.EFFECT_FLAG_YES);

                insertViewList.add(insertView);

                LcdpResourceVersionBean viewVersion = resourceVersionList.stream()
                        .filter(v -> view.getTableName().equals(v.getResourceId()))
                        .findAny().get();

                LcdpResourceVersionBean insertVersion = new LcdpResourceVersionBean();
                BeanUtils.copyProperties(viewVersion, insertVersion, LcdpConstant.COPY_IGNORE_PROPERTIES);
                insertVersion.setId(ApplicationContextHelper.getNextIdentity());
                insertVersion.setVersion(insertView.getVersion());
                insertVersion.setLogId(submitLog.getId());
                insertVersion.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
                insertVersion.setSubmitTime(now);
                insertVersion.setEditTime(now);
                insertVersion.setResourceAction(LcdpConstant.RESOURCE_SUBMIT_ACTION_REVERT); // 回滚
                insertVersion.setCommit(Optional.ofNullable(commit).orElse("") + "(" + I18nHelper.getMessage("LCDP.VERSION_LOG.DEFAULT_REVERT_COMMIT_LOG",
                        Optional.ofNullable(view.getVersion()).orElse(0L).toString()) + ")");
                insertVersionList.add(insertVersion);
            }

            viewService.getDao().fastInsert(insertViewList);
            checkImportDataDTO.setViewList(insertViewList);
        }

        // 全局JS配置
        LcdpResourceVersionBean configJsResourceVersion = resourceVersionList.stream()
                .filter(v -> LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_JS.equals(v.getResourceCategory()))
                .findAny().orElse(null);
        if (configJsResourceVersion != null) {
            LcdpGlobalConfigBean filter = new LcdpGlobalConfigBean();
            filter.setConfigCode(LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_JS);
            filter.setVersion(configJsResourceVersion.getVersion());
            checkImportDataDTO.setJsOperation("revert");
            LcdpGlobalConfigBean toRevertConfigJs = globalConfigService.getDao().selectFirst(filter);

            LcdpGlobalConfigBean insertConfigJs = new LcdpGlobalConfigBean();
            BeanUtils.copyProperties(toRevertConfigJs, insertConfigJs, LcdpConstant.COPY_IGNORE_PROPERTIES);
            insertConfigJs.setId(ApplicationContextHelper.getNextIdentity());

            // 删除最新未提交的数据
            LcdpGlobalConfigBean latestConfigJs = globalConfigService.selectFirstByFilter(SearchFilter.instance()
                            .match("CONFIGCODE", LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_JS).filter(MatchPattern.SEQ),
                    Order.desc("VERSION"));
            if (LcdpConstant.EFFECT_FLAG_NO.equals(latestConfigJs.getEffectFlag())) {
                globalConfigService.getDao().delete(latestConfigJs.getId());

                insertConfigJs.setVersion(latestConfigJs.getVersion());
            } else {
                insertConfigJs.setVersion(latestConfigJs.getVersion() + 1);
            }

            // 新增
            globalConfigService.getDao().insert(insertConfigJs);

            LcdpResourceVersionBean insertVersion = new LcdpResourceVersionBean();
            insertVersion.setId(ApplicationContextHelper.getNextIdentity());
            insertVersion.setLogId(submitLog.getId());
            insertVersion.setResourceId(insertConfigJs.getConfigCode());
            insertVersion.setResourceName(I18nHelper.getMessage("T_LCDP_GLOBAL_CONFIG." + insertConfigJs.getConfigCode() + ".NAME"));
            insertVersion.setResourcePath(insertVersion.getResourceName());
            insertVersion.setResourceCategory(insertConfigJs.getConfigCode());
            insertVersion.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
            insertVersion.setVersion(insertConfigJs.getVersion());
            insertVersion.setCommit(submitLog.getCommit());
            insertVersion.setEditTime(now);
            insertVersion.setSubmitTime(now);
            insertVersion.setResourceAction(LcdpConstant.RESOURCE_SUBMIT_ACTION_REVERT); // 回滚
            insertVersion.setCommit(Optional.ofNullable(commit).orElse("") + "(" + I18nHelper.getMessage("LCDP.VERSION_LOG.DEFAULT_REVERT_COMMIT_LOG",
                    Optional.ofNullable(toRevertConfigJs.getVersion()).orElse(0L).toString()) + ")");

            insertVersionList.add(insertVersion);
        }

        // 全局CSS配置
        LcdpResourceVersionBean configCssResourceVersion = resourceVersionList.stream()
                .filter(v -> LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_CSS.equals(v.getResourceCategory()))
                .findAny().orElse(null);
        if (configCssResourceVersion != null) {
            LcdpGlobalConfigBean filter = new LcdpGlobalConfigBean();
            filter.setConfigCode(LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_CSS);
            filter.setVersion(configCssResourceVersion.getVersion());
            checkImportDataDTO.setCssOperation("revert");

            LcdpGlobalConfigBean toRevertConfigCss = globalConfigService.getDao().selectFirst(filter);

            LcdpGlobalConfigBean insertConfigCss = new LcdpGlobalConfigBean();
            BeanUtils.copyProperties(toRevertConfigCss, insertConfigCss, LcdpConstant.COPY_IGNORE_PROPERTIES);
            insertConfigCss.setId(ApplicationContextHelper.getNextIdentity());

            // 删除最新未提交的数据
            LcdpGlobalConfigBean latestConfigCss = globalConfigService.selectFirstByFilter(SearchFilter.instance()
                            .match("CONFIGCODE", LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_CSS).filter(MatchPattern.SEQ),
                    Order.desc("VERSION"));
            if (LcdpConstant.EFFECT_FLAG_NO.equals(latestConfigCss.getEffectFlag())) {
                globalConfigService.getDao().delete(latestConfigCss.getId());

                insertConfigCss.setVersion(latestConfigCss.getVersion());
            } else {
                insertConfigCss.setVersion(latestConfigCss.getVersion() + 1);
            }

            // 新增
            globalConfigService.getDao().insert(insertConfigCss);

            LcdpResourceVersionBean insertVersion = new LcdpResourceVersionBean();
            insertVersion.setId(ApplicationContextHelper.getNextIdentity());
            insertVersion.setLogId(submitLog.getId());
            insertVersion.setResourceId(insertConfigCss.getConfigCode());
            insertVersion.setResourceName(I18nHelper.getMessage("T_LCDP_GLOBAL_CONFIG." + insertConfigCss.getConfigCode() + ".NAME"));
            insertVersion.setResourcePath(insertVersion.getResourceName());
            insertVersion.setResourceCategory(insertConfigCss.getConfigCode());
            insertVersion.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
            insertVersion.setVersion(insertConfigCss.getVersion());
            insertVersion.setCommit(submitLog.getCommit());
            insertVersion.setEditTime(now);
            insertVersion.setSubmitTime(now);
            insertVersion.setResourceAction(LcdpConstant.RESOURCE_SUBMIT_ACTION_REVERT); // 回滚
            insertVersion.setCommit(Optional.ofNullable(commit).orElse("") + "(" + I18nHelper.getMessage("LCDP.VERSION_LOG.DEFAULT_REVERT_COMMIT_LOG",
                    Optional.ofNullable(toRevertConfigCss.getVersion()).orElse(0L).toString()) + ")");

            insertVersionList.add(insertVersion);
        }

        resourceVersionService.getDao().fastInsert(insertVersionList);
        checkImportDataDTO.setOperation("revert");
        resourceImportRecordService.checkImportRecord(checkImportDataDTO);
    }

    //--------------------------------------------------------------------------------------------------------------------
    // 私有方法
    //--------------------------------------------------------------------------------------------------------------------
    private String getResourceName(LcdpResourceBean resource) {
        String resourceName = resource.getResourceName();
        if (!StringUtils.startsWithIgnoreCase(resourceName, "lcdp")) {
            resourceName = "Lcdp" + resourceName;
        }

        return resourceName;
    }

    private LcdpResultDTO noJavaSave(LcdpResourceBean resource) {
        resource.setContent(GzipUtils.decompress(resource.getContent()));
        String resourceCategory = resourceService.getDao().selectColumnById(resource.getId(), "RESOURCECATEGORY", String.class);

        LcdpResourceHistoryBean resourceHistory = resourceHistoryService.selectFirstByFilter(SearchFilter.instance()
                .match("RESOURCEID", resource.getId()).filter(MatchPattern.EQ)
                .match("CREATEDBYID", LocalContextHelper.getLoginUserId()).filter(MatchPattern.SEQ)
                .match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.SEQ));

        //后端脚本保存，保存版本+1
        Long newModifyVersion = resourceHistory.getModifyVersion() + 1;
        resourceHistory.setContent(resource.getContent());
        resourceHistory.setModifyVersion(newModifyVersion);

        if (LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(resourceCategory)) {
            TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> {
                LcdpMapperUtils.loadMapper(resourceHistory.getPath(), false, resource.getContent());
            });
        }

        // 保存页面组件
        if (LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(resourceCategory)) {
            List<LcdpModulePageCompBean> insertPageCompList = savePageComps(resourceHistory, resource.getComponents());

            if (!CollectionUtils.isEmpty(insertPageCompList)) {
                List<LcdpResourceBean> insertResourceList = resourcePageService.postUpdate(resourceHistory, insertPageCompList);

                postInsertResource(insertResourceList);
            }
        }

        // 保存页面国际化
        if (resource.getI18n() != null) {
            savePageI18n(resourceHistory, resource.getI18n());
        }
        // 保存页面与系统国际化依赖关系
        if (resource.getDependentI18n() != null) {
            savePageDependentI18n(resourceHistory, resource.getDependentI18n());
        }

        resourceHistoryService.getDao().update(resourceHistory, "CONTENT", "MODIFYVERSION", "CLASSCONTENT", "LCDPFILEPATH");

        if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_MAPPER, resource.getResourceCategory())) {
            TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> {
                LcdpMapperUtils.loadMapper(resourceHistory.getPath(), false, resourceHistory.getContent());
            });
        }

        // 同步数据
        LcdpResourceSyncManager.sync(resource.getId());

        LcdpResultDTO result = LcdpResultDTO.sucess();

        String msg = SunwayAopContext.getCache(LcdpConstant.LCDP_AUTOMATIC_INSERT_COLIMNS_MSG_KEY);
        if (!StringUtils.isBlank(msg)) {
            result.setMessage(msg);
        }

        return result;
    }

    private void postInsertResource(List<LcdpResourceBean> resourceList) {
        List<Long> resourceIdList = resourceList.stream()
                .filter(r -> LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(r.getResourceCategory()))
                .map(LcdpResourceBean::getId).collect(Collectors.toList());

        if (!resourceIdList.isEmpty()) {
            List<LcdpResourceHistoryBean> resourceHistoryList = resourceHistoryService.selectListByFilter(SearchFilter.instance()
                    .match("RESOURCEID", resourceIdList).filter(MatchPattern.OR));

            for (LcdpResourceBean resource : resourceList) {
                if (!LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(resource.getResourceCategory())) {
                    continue;
                }

                LcdpResourceHistoryBean resourceHistory = resourceHistoryList.stream().filter(h -> Objects.equals(h.getResourceId(), resource.getId()))
                        .findAny().get();

                if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(resource.getResourceCategory())) { // java类，新增时一定要编译一次
                    LcdpJavaCodeResolverUtils.loadSourceCode(resourceHistory);
                } else if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_MAPPER, resource.getResourceCategory())) {
                    TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> {
                        LcdpMapperUtils.loadMapper(resourceHistory.getPath(), false, resourceHistory.getContent());
                    });
                }
            }
        }

        List<LcdpResourceBean> scriptResourceList = resourceList.stream()
                .filter(r -> LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(r.getResourceCategory())).collect(Collectors.toList());

        // 资源锁定
        resourceLockService.lockResource(scriptResourceList);

        // 添加检出记录表
        checkoutRecordService.checkoutResource(scriptResourceList);

        // 同步数据
        resourceIdList.forEach(LcdpResourceSyncManager::sync);
    }

//    private void postUpdateResource(LcdpResourceBean resource, LcdpResourceHistoryBean resourceHistory) {
//        if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(resource.getResourceCategory())) { // java类，新增时一定要编译一次
//            LcdpJavaCodeResolverUtils.loadSourceCode(resourceHistory);
//        } else if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_MAPPER, resource.getResourceCategory())) {
//            TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> {
//                LcdpMapperUtils.loadMapper(resourceHistory.getPath(), false, resourceHistory.getContent());
//            });
//        }
//
//        // 同步数据
//        LcdpResourceSyncManager.sync(resource.getId());
//    }

    private List<LcdpModulePageCompBean> savePageComps(LcdpResourceHistoryBean resourceHistory, List<LcdpModulePageCompBean> modulePageCompList) {
        String content = resourceHistory.getContent();
        List<String> filePathList = new ArrayList<>();

        Matcher filePathMatcher = FILE_PATTERN.matcher(content);
        while (filePathMatcher.find()) {
            filePathList.add(filePathMatcher.group(1));
        }

        List<String> deleteIdList = new ArrayList<>();
        List<LcdpModulePageCompBean> insertList = new ArrayList<>();

        List<LcdpModulePageCompBean> selectedModulePageCompList = pageCompService.selectListByFilter(SearchFilter.instance()
                .match("MODULEPAGEHISTORYID", resourceHistory.getId()).filter(MatchPattern.SEQ));

        if (CollectionUtils.isEmpty(modulePageCompList)) {
            deleteIdList.addAll(selectedModulePageCompList.stream().map(LcdpModulePageCompBean::getId).collect(Collectors.toList()));
        } else {
            // 赋值资源ID
            modulePageCompList.forEach(comp -> {
                comp.setModulePageId(resourceHistory.getResourceId());
                comp.setModulePageHistoryId(resourceHistory.getId());
                comp.setModulePageVersion(resourceHistory.getVersion());
                EntityHelper.assignCreatedElement(comp);
            });

            for (LcdpModulePageCompBean pageComp : modulePageCompList) {
                String config = pageComp.getConfig();
                Matcher filePathConfigMatcher = FILE_PATTERN.matcher(config);
                while (filePathConfigMatcher.find()) {
                    filePathList.add(filePathConfigMatcher.group(1));
                }
                pageComp.setConfig(config);
            }

            for (LcdpModulePageCompBean pageComp : selectedModulePageCompList) {
                if (modulePageCompList.stream().noneMatch(c -> equalsPageComp(pageComp, c))) {
                    deleteIdList.add(pageComp.getId());
                }
            }

            // 删除有parentId，但是不存在对应父节点组件的
            modulePageCompList.forEach(c -> {
                if (!StringUtils.isBlank(c.getParentId())) {
                    if (modulePageCompList.stream().noneMatch(lc -> lc.getId().equals(c.getParentId()))) {
                        if (!deleteIdList.contains(c.getId())) {
                            deleteIdList.add(c.getId());
                        }
                    }
                }
            });

            for (LcdpModulePageCompBean pageComp : modulePageCompList) {
                LcdpModulePageCompBean selectedModulePageComp = selectedModulePageCompList.stream().filter(c -> c.getId().equals(pageComp.getId())).findAny().orElse(null);

                if (selectedModulePageComp == null) {
                    // 需要更新mapper
                    pageComp.setExt$Item(LcdpConstant.LCDP_MAPPER_UPDATE_KEY, Constant.YES);
                    // 需要更新table
                    pageComp.setExt$Item(LcdpConstant.LCDP_TABLE_UPDATE_KEY, Constant.YES);
                    // 需要更新setId方法
                    pageComp.setExt$Item(LcdpConstant.LCDP_SETID_UPDATE_KEY, Constant.YES);

                    insertList.add(pageComp);
                } else {
                    // 有数据更新
                    if (!equalsPageComp(pageComp, selectedModulePageComp)) {
                        insertList.add(pageComp);

                        // 判断是否需要更新mapper
                        String config = Optional.ofNullable(pageComp.getConfig()).orElse("");

                        JSONObject configJSONObject = JSONObject.parseObject(config);

                        String selectedConfig = Optional.ofNullable(selectedModulePageComp.getConfig()).orElse("");

                        JSONObject selectedConfigJSONObject = JSONObject.parseObject(selectedConfig);

                        if (!StringUtils.equals(Optional.ofNullable(configJSONObject.getString("sql")).orElse(""),
                                Optional.ofNullable(selectedConfigJSONObject.getString("sql")).orElse(""))) { // sql有变化，需要更新mapper
                            pageComp.setExt$Item(LcdpConstant.LCDP_MAPPER_UPDATE_KEY, Constant.YES);
                        } else if (StringUtils.isBlank(configJSONObject.getString("sql"))) { // sql没有变化，只有当sql为空时考虑表是否有修改，来判断是否需要更新mapper
                            if (!StringUtils.equals(Optional.ofNullable(configJSONObject.getString("table")).orElse(""),
                                    Optional.ofNullable(selectedConfigJSONObject.getString("table")).orElse(""))) {
                                pageComp.setExt$Item(LcdpConstant.LCDP_MAPPER_UPDATE_KEY, Constant.YES);
                            }
                        }

                        if (!StringUtils.equals(Optional.ofNullable(configJSONObject.getString("table")).orElse(""),
                                Optional.ofNullable(selectedConfigJSONObject.getString("table")).orElse(""))) {
                            // 需要更新table
                            pageComp.setExt$Item(LcdpConstant.LCDP_TABLE_UPDATE_KEY, Constant.YES);
                        }

                        if (!StringUtils.equals(Optional.ofNullable(configJSONObject.getString("idAutoGen")).orElse(""),
                                Optional.ofNullable(selectedConfigJSONObject.getString("idAutoGen")).orElse(""))
                                || !StringUtils.equals(Optional.ofNullable(configJSONObject.getString("idGenSequence")).orElse(""),
                                Optional.ofNullable(selectedConfigJSONObject.getString("idGenSequence")).orElse(""))) {
                            // 需要更新setId方法
                            pageComp.setExt$Item(LcdpConstant.LCDP_SETID_UPDATE_KEY, Constant.YES);
                        }
                    }
                }
            }
        }

        List<LcdpModulePageCompBean> currentModulePageCompList = Optional.ofNullable(modulePageCompList)
                .orElse(CollectionUtils.emptyList())
                .stream()
                .filter(comp -> !deleteIdList.contains(comp.getId()))
                .collect(Collectors.toList());

        List<CoreStudioGridDTO> lastPageGridList = getPageGridList(resourceHistory.getPath(), selectedModulePageCompList);
        List<CoreStudioGridDTO> pageGridList = getPageGridList(resourceHistory.getPath(), currentModulePageCompList);
        TransactionUtils.runAfterCommit(() -> pageGridFieldConfigService.resetPageGridConfig(lastPageGridList, pageGridList));

        if (!filePathList.isEmpty()) {
            filePathList = filePathList.stream().distinct().collect(Collectors.toList());
            String filePath = JSONObject.toJSONString(filePathList);
            resourceHistory.setLcdpFilePath(filePath);
        }

        pageCompService.getDao().deleteByIdList(deleteIdList);
        insertList.forEach(comp -> {
            JSONObject configObject = JSON.parseObject(comp.getConfig());
            if (!configObject.containsKey("versionCompare")) {
                configObject.put("versionCompare", StringUtils.randomUUID(18));
                comp.setConfig(configObject.toJSONString());
            }

        });
        pageCompService.getDao().fastInsert(insertList);

        return insertList;
    }

    private List<CoreStudioGridDTO> getPageGridList(String pageId, List<LcdpModulePageCompBean> pageCompList) {
        if (StringUtils.isBlank(pageId) || CollectionUtils.isEmpty(pageCompList)) {
            return CollectionUtils.emptyList();
        }

        List<CoreStudioGridDTO> gridList = new ArrayList<>();
        for (LcdpModulePageCompBean pageComp : pageCompList) {
            if ((!StringUtils.equalsIgnoreCase(pageComp.getType(), "Grid")
                    && !StringUtils.equalsIgnoreCase(pageComp.getType(), "TreeGrid"))
                    || StringUtils.isBlank(pageComp.getConfig())) {
                continue;
            }

            JSONObject configObject = JSONObject.parseObject(pageComp.getConfig());
            if (configObject == null) {
                continue;
            }

            CoreStudioGridDTO grid = new CoreStudioGridDTO();
            grid.setPageId(pageId);
            grid.setGridId(pageComp.getId());

            addGridColumns(grid, configObject.getJSONArray("columns"), Constant.NO);
            addGridColumns(grid, configObject.getJSONArray("hiddenFields"), Constant.YES);
            gridList.add(grid);
        }
        return gridList;
    }

    private void addGridColumns(CoreStudioGridDTO grid, JSONArray columnArray, String hidden) {
        if (CollectionUtils.isEmpty(columnArray)) {
            return;
        }

        for (int i = 0, j = columnArray.size(); i < j; i++) {
            JSONObject column = columnArray.getJSONObject(i);
            if (column == null || StringUtils.isBlank(column.getString("field"))) {
                continue;
            }

            String field = column.getString("field");
            String title = Optional.ofNullable(column.getString("title")).orElse(field);
            grid.addColumn(CoreStudioGridDTO.CoreStudioGridColumnDTO.of(field, title, hidden));
        }
    }

    private void savePageI18n(LcdpResourceHistoryBean resourceHistory, Map<String, Map<String, String>> modulePageI18n) {
        List<LcdpModulePageI18nBean> modulePageI18nBeanList = new ArrayList<>();

        //赋值资源ID
        modulePageI18n.forEach((code, i18Message) -> {
            i18Message.forEach((locale, message) -> {
                LcdpModulePageI18nBean i18nMessage = new LcdpModulePageI18nBean();
                i18nMessage.setModulePageId(resourceHistory.getResourceId());
                i18nMessage.setModulePageHistoryId(resourceHistory.getId());
                i18nMessage.setId(ApplicationContextHelper.getNextIdentity());
                i18nMessage.setCode(code);
                i18nMessage.setI18nConfigId(locale);
                i18nMessage.setMessage(message);

                modulePageI18nBeanList.add(i18nMessage);
            });
        });

        List<LcdpModulePageI18nBean> selectedModulePageI18nList = pageI18nService.selectListByFilter(SearchFilter.instance()
                .match("MODULEPAGEHISTORYID", resourceHistory.getId()).filter(MatchPattern.SEQ));

        List<Long> deleteIdList = new ArrayList<>();
        List<LcdpModulePageI18nBean> insertList = new ArrayList<>();

        for (LcdpModulePageI18nBean pageI18n : selectedModulePageI18nList) {
            if (modulePageI18nBeanList.stream().noneMatch(c -> equalsPageI18n(pageI18n, c))) {
                deleteIdList.add(pageI18n.getId());
            }
        }

        for (LcdpModulePageI18nBean pageI18n : modulePageI18nBeanList) {
            if (selectedModulePageI18nList.stream().noneMatch(c -> equalsPageI18n(pageI18n, c))) {
                insertList.add(pageI18n);
            }
        }

        pageI18nService.getDao().deleteByIdList(deleteIdList);
        pageI18nService.getDao().fastInsert(insertList);
    }

    private void savePageDependentI18n(LcdpResourceHistoryBean resourceHistory, List<String> pageDependentI18n) {
        List<LcdpPageI18nCodeBean> newPageI18nCodeList = new ArrayList<>();

        List<Long> deletePageI18nCodeIdList = new ArrayList<>();

        List<LcdpPageI18nCodeBean> oldPageI18nCodeList = pageI18nCodeService.selectListByFilter(SearchFilter.instance()
                .match("MODULEPAGEHISTORYID", resourceHistory.getId()).filter(MatchPattern.EQ));

        //找出需要删除的code
        oldPageI18nCodeList.forEach(code -> {
            if (pageDependentI18n.contains(code.getCode())) {
                pageDependentI18n.remove(code.getCode());
            } else {
                deletePageI18nCodeIdList.add(code.getId());
            }
        });

        pageDependentI18n.forEach(code -> {
            LcdpPageI18nCodeBean lcdpPageI18nCodeBean = new LcdpPageI18nCodeBean();
            lcdpPageI18nCodeBean.setId(ApplicationContextHelper.getNextIdentity());
            lcdpPageI18nCodeBean.setCode(code);
            lcdpPageI18nCodeBean.setModulePageHistoryId(resourceHistory.getId());
            lcdpPageI18nCodeBean.setModulePageId(resourceHistory.getResourceId());
            newPageI18nCodeList.add(lcdpPageI18nCodeBean);
        });

        pageI18nCodeService.getDao().deleteByIdList(deletePageI18nCodeIdList);
        pageI18nCodeService.getDao().fastInsert(newPageI18nCodeList);
    }

    private boolean equalsPageComp(LcdpModulePageCompBean first, LcdpModulePageCompBean second) {
        if (!Objects.equals(first.getId(), second.getId())) {
            return false;
        }

        if (!Objects.equals(first.getParentId(), second.getParentId())) {
            return false;
        }

        if (!Objects.equals(first.getType(), second.getType())) {
            return false;
        }

        String firstConfig = Optional.ofNullable(first.getConfig()).orElse("");

        String secondConfig = Optional.ofNullable(second.getConfig()).orElse("");

        if (!Objects.equals(firstConfig, secondConfig)) {
            return false;
        }

        return true;
    }

    private boolean equalsPageI18n(LcdpModulePageI18nBean first, LcdpModulePageI18nBean second) {
        if (!Objects.equals(first.getCode(), second.getCode())) {
            return false;
        }

        if (!Objects.equals(first.getI18nConfigId(), second.getI18nConfigId())) {
            return false;
        }

        if (!Objects.equals(first.getMessage(), second.getMessage())) {
            return false;
        }

        return true;
    }

    private LcdpResultDTO javaSave(LcdpResourceBean resource, boolean compile) {
        resource.setContent(GzipUtils.decompress(resource.getContent()));
        // 特殊处理用LcdpBaseService接口进行脚本类注入
        List<String> lcdpBaseServiceFieldNameList = LcdpJavaCodeResolverUtils.getAuwowiredLcdpBaseServiceFieldNameList(resource.getContent());
        if (!lcdpBaseServiceFieldNameList.isEmpty()) {
            return LcdpResultDTO.fail(I18nHelper.getMessage("LCDP.EXCEPTION.UNABLE_AUTOWIRE_LCDPBASESERVICE",
                    lcdpBaseServiceFieldNameList.stream().collect(Collectors.joining(","))));
        }

        LcdpResultDTO result = LcdpResultDTO.sucess();

        List<String> importClassFullNameList = LcdpJavaCodeResolverUtils.getImportClassFullNameList(resource.getContent());
        if (!importClassFullNameList.isEmpty()) {
            resource.setContent(LcdpJavaCodeResolverUtils.getImportedSourceCode(resource.getContent(), importClassFullNameList));

            result.setData(importClassFullNameList);
        }

        LcdpResourceHistoryBean resourceHistory = resourceHistoryService.selectUnsubmittedResourceHistory(LocalContextHelper.getLoginUserId(), resource.getId());
        // 后端脚本保存，保存版本+1
        Long newModifyVersion = resourceHistory.getModifyVersion() + 1;
        resourceHistory.setContent(resource.getContent());
        resourceHistory.setModifyVersion(newModifyVersion);

        // 替换脚本类名
        String classContent = LcdpJavaCodeResolverUtils.getClassContent(resourceHistory.getContent(),
                resourceHistory.getResourceName(),
                resourceHistory.getVersion(),
                resourceHistory.getModifyVersion(),
                resourceHistory.getVersionOffset());

        // 替换脚本内容实现开发平台脚本之间互相调用
        classContent = resourceService.replaceScriptContent(classContent);
        resourceHistory.setClassContent(classContent);

        // 保存历史表
        String content = resourceHistory.getContent();
        List<String> filePathList = new ArrayList<>();

        Matcher filePathMatcher = FILE_PATTERN.matcher(content);
        while (filePathMatcher.find()) {
            filePathList.add(filePathMatcher.group(1));
        }
        if (!filePathList.isEmpty()) {
            filePathList = filePathList.stream().distinct().collect(Collectors.toList());
            String filePath = JSONObject.toJSONString(filePathList);
            resourceHistory.setLcdpFilePath(filePath);
        }
        resourceHistory.setContent(LcdpJavaCodeResolverUtils.updatePackage(resourceHistory.getContent(), resourceHistory.getPath()));
        resourceHistory.setClassContent(LcdpJavaCodeResolverUtils.updatePackage(resourceHistory.getClassContent(), resourceHistory.getPath()));

        String[] targertContent = resourceHistory.getContent().trim().split("\n");
        if (StringUtils.isEmpty(resourceHistory.getContent()) || targertContent.length < 2) {
            log.error("============>资源内容异常：" + resourceHistory.getResourceName() + "（" + resourceHistory.getId() + "）的源代码为空");
            log.error("============>资源内容异常：" + resourceHistory.getResourceName() + "原内容" + content);
            throw new CheckedException("LCDP.EXCEPTION.JAVA.SAVE.EMPTY_SOURCE_CODE");
        }
        resourceHistoryService.getDao().update(resourceHistory, "CONTENT", "MODIFYVERSION", "CLASSCONTENT", "LCDPFILEPATH");

        if (compile) {
            try {
                LcdpJavaCodeResolverUtils.loadAndRegisterSourceCode(resourceHistory);

                // 当编译时，需要同步数据
                LcdpResourceSyncManager.sync(resource.getId());
            } catch (Exception ce) {
                log.error(ce.getMessage(), ce);

                result.setMessage(I18nHelper.getMessage("LCDP.EXCEPTION.COMPILE_FAIL"));
                result.setFail();

                return result;
            }

            if (StringUtils.isEmpty(resource.getResourceName())) {
                resource.setResourceName(resourceHistory.getResourceName());
            }

            LcdpJavaCodeResolverUtils.removePreLoadedDevClass(resource);
        }

        return result;
    }

    /**
     * 是否是不可见的资源
     */
    private boolean isInvisible(LcdpResourceBean resourceBean) {
        return LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(resourceBean.getResourceCategory())
                && (ObjectUtils.isEmpty(resourceBean.getEffectVersion())
                || resourceBean.getEffectVersion() == 0L)
                && !LocalContextHelper.getLoginUserId().equals(resourceBean.getCreatedById());

    }

    /**
     * 资源复制 复制历史表数据
     *
     * @param oldResourceId  源资源ID
     * @param newResourceId  复制后的ID
     * @param path           资源路径
     * @param copyName       复制后名称
     * @param desc           复制后描述
     * @param scriptResource 是否只复制脚本
     */
    private void copyHistoryResource(Long oldResourceId, Long newResourceId, String path, String copyName, String desc, boolean scriptResource) {
        //资源历史表新老ID映射
        Map<Long, Long> resourceIdMapping = new HashMap<>();
        resourceIdMapping.put(oldResourceId, newResourceId);

        List<LcdpResourceHistoryBean> oldHistoryList = resourceHistoryService.selectListByFilter(SearchFilter.instance()
                        .match("RESOURCEID", oldResourceId).filter(MatchPattern.SEQ),
                Order.desc("ID"));

        // 首先判断有无正在编辑的数据
        List<LcdpResourceHistoryBean> sourceResourceHistoryList = oldHistoryList.stream().filter(history -> LocalContextHelper.getLoginUserId().equals(history.getCreatedById())
                && LcdpConstant.SUBMIT_FLAG_NO.equals(history.getSubmitFlag())).collect(Collectors.toList());

        // 如果有自己编辑的，就复制这个，否则就复制最后生效的
        LcdpResourceHistoryBean oldHistory = sourceResourceHistoryList.isEmpty() ? oldHistoryList.stream()
                .filter(history -> LcdpConstant.EFFECT_FLAG_YES.equals(history.getEffectFlag()))
                .findFirst().get()
                : sourceResourceHistoryList.get(0);

        LcdpResourceHistoryBean newHistory = new LcdpResourceHistoryBean();
        BeanUtils.copyProperties(oldHistory, newHistory, LcdpConstant.COPY_IGNORE_PROPERTIES);
        newHistory.setId(ApplicationContextHelper.getNextIdentity());
        newHistory.setResourceId(newResourceId);
        newHistory.setResourceName(copyName);
        newHistory.setResourceDesc(desc);
        newHistory.setPath(path);
        newHistory.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);
        newHistory.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_NO);
        newHistory.setVersion(1L);
        newHistory.setClassContent(null);

        ScriptOperator.updateContent(newHistory, oldHistory);

        if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_JAVA, oldHistory.getResourceCategory())) {
            // 替换脚本类名
            String classContent = LcdpJavaCodeResolverUtils.getClassContent(newHistory.getContent(),
                    newHistory.getResourceName(),
                    1L, 1L, newHistory.getVersionOffset());

            // 替换脚本内容实现开发平台脚本之间互相调用
            classContent = resourceService.replaceScriptContent(classContent);

            newHistory.setClassContent(classContent);
        }

        EntityHelper.assignCreatedElement(newHistory);
        resourceHistoryService.getDao().insert(newHistory);

        resourceIdMapping.put(oldHistory.getId(), newHistory.getId());

        // 如果是页面资源,还需复制组件数据
        if (LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(newHistory.getResourceCategory())) {
            Map<String, String> pathMap = new HashMap<>();
            pathMap.put(oldHistory.getPath(), newHistory.getPath());
            resourceService.copyPageComps(Arrays.asList(oldHistory), resourceIdMapping, pathMap, scriptResource);
        }
    }

    /**
     * 撤销检出，对于脚本资源有如下操作：<br>
     * 1、历史表删除检出数据<br>
     * 2、删除资源检出记录表<br>
     * 3、对于Java源代码，删除后加载资源表的源代码（会更新script method）<br>
     * 4、对于Mapper，删除开发中的Mapper<br>
     * 5、解锁资源
     */
    private void cancelCheckout(List<Long> resourceIdList, List<String> tableNameList, List<String> viewNameList, boolean fromOverview) {
        if (CollectionUtils.isEmpty(resourceIdList)
                && CollectionUtils.isEmpty(tableNameList)
                && CollectionUtils.isEmpty(viewNameList)) {
            return;
        }

        LcdpCheckImportDataDTO checkImportDataDTO = new LcdpCheckImportDataDTO();
        checkImportDataDTO.setOperation("cancel");
        List<String> unLockResourceIdList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(resourceIdList)) {
            List<LcdpResourceBean> resourceList = resourceService.getDao().selectListByIds(resourceIdList);
            checkImportDataDTO.setResourceList(resourceList);
            // 没有提交的不能撤销
            resourceList.removeIf(r -> r.getEffectVersion() == null || r.getEffectVersion() == 0L);

            if (!resourceList.isEmpty()) {
                List<Long> checkoutResourceIdList = resourceList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());

                SearchFilter filter = SearchFilter.instance()
                        .match("RESOURCEID", checkoutResourceIdList).filter(MatchPattern.OR)
                        .match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.SEQ)
                        .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ);

                // 超管不需要控制人员，用户类型为sysAdmin的账号，在检出概览也不需要控制
                if (!StringUtils.equals(LcdpConstant.SUPER_ADMIN_ID, LocalContextHelper.getLoginUserId())
                        && !(fromOverview && LocalContextHelper.isAdminLogin())) {
                    filter.match("CREATEDBYID", LocalContextHelper.getLoginUserId()).filter(MatchPattern.SEQ);
                }
                List<LcdpResourceHistoryBean> resourceHistoryList = resourceHistoryService.selectListByFilter(filter, Order.desc("ID"));

                if (!resourceHistoryList.isEmpty()) {
                    // 更新modifyVersion
                    resourceHistoryList.forEach(h -> {
                        LcdpResourceBean resource = resourceList.stream().filter(r -> r.getId().equals(h.getResourceId())).findAny().get();

                        resource.setModifyVersion(Math.max(Optional.ofNullable(resource.getModifyVersion()).orElse(0L), h.getModifyVersion()));
                    });
                    resourceService.getDao().update(resourceList, "MODIFYVERSION");

                    resourceHistoryService.getDao().deleteByIdList(resourceHistoryList.stream().map(e -> e.getId()).collect(Collectors.toList()));

                    unLockResourceIdList.addAll(resourceHistoryList.stream().map(resourceHistory -> String.valueOf(resourceHistory.getResourceId())).collect(Collectors.toList()));

                    List<LcdpResourceBean> checkoutResourceList = resourceList.stream()
                            .filter(r -> resourceHistoryList.stream().anyMatch(h -> h.getResourceId().equals(r.getId())))
                            .collect(Collectors.toList());

                    checkoutRecordService.removeCheckout(checkoutResourceList);

                    // 删除已加载的开发中的类
                    TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> {
                        checkoutResourceList.forEach(r -> LcdpJavaCodeResolverUtils.removeLoadedDevClass(r));
                    });

                    // 加载原来的类，并更新Script Method
                    List<LcdpResourceBean> javaResourceList = checkoutResourceList.stream()
                            .filter(r -> LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(r.getResourceCategory()))
                            .collect(Collectors.toList());
                    LcdpJavaCodeResolverUtils.updateProScriptMethod(javaResourceList);

                    // 卸载开发mapper
                    checkoutResourceList.stream().filter(r -> LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(r.getResourceCategory()))
                            .forEach(r -> LcdpMapperUtils.unloadMapper(r.getPath(), false));

                    // 同步数据
                    checkoutResourceList.forEach(r -> LcdpResourceSyncManager.sync(r.getId()));
                }
            }
        }

        if (!CollectionUtils.isEmpty(tableNameList)) {
            unLockResourceIdList.addAll(tableService.revert(tableNameList));
            List<LcdpTableBean> tableList = tableNameList.stream().map(name -> {
                LcdpTableBean table = new LcdpTableBean();
                table.setTableName(name);
                return table;
            }).collect(Collectors.toList());
            checkImportDataDTO.setTableList(tableList);

        }
        if (!CollectionUtils.isEmpty(viewNameList)) {
            unLockResourceIdList.addAll(viewService.revert(viewNameList));
            List<LcdpViewBean> viewList = viewNameList.stream().map(name -> {
                LcdpViewBean view = new LcdpViewBean();
                view.setViewName(name);
                return view;
            }).collect(Collectors.toList());
            checkImportDataDTO.setViewList(viewList);
        }

        resourceLockService.unLock(unLockResourceIdList);
        resourceImportRecordService.checkImportRecord(checkImportDataDTO);
    }

    private LoginUser buildLoginUser(String userId) {
        CoreUserBean user = coreUserService.selectById(userId);
        if (user == null) {
            throw new ApplicationWarningException("操作用户不存在");
        }

        LoginUser loginUser = new LoginUser(user.getId(), user.getPassword());
        loginUser.setOrgId(user.getOrgId());
        loginUser.setOrgName(user.getOrgName());
        loginUser.setTenantId(user.getTenantId());
        loginUser.setTenantName(user.getTenantName());
        return loginUser;
    }

    private void submitScriptHistoryList(List<LcdpResourceHistoryBean> submitHistoryList, LcdpCheckImportDataDTO checkImportRecord, List<LcdpResourceBean> pageList) {
        if (submitHistoryList.isEmpty()) {
            return;
        }

        List<Long> resourceIdList = submitHistoryList.stream().map(LcdpResourceHistoryBean::getResourceId).collect(Collectors.toList());

        List<LcdpResourceBean> submitResourceList = submitHistoryList.stream().map(history -> {
            LcdpResourceBean resource = new LcdpResourceBean();
            resource.setId(history.getResourceId());
            resource.setResourceName(history.getResourceName());
            resource.setEffectVersion(history.getVersion());
            resource.setPath(history.getPath());
            resource.setResourceCategory(history.getResourceCategory());
            resource.setLcdpFilePath(history.getLcdpFilePath());
            resource.setModifyVersion(history.getModifyVersion());

            String content = ObjectUtils.isEmpty(history.getContent()) ? "" : history.getContent();
            if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(history.getResourceCategory())) {
                // 替换脚本类名，保证正式版本按当前资源版本注册
                String classContent = history.getClassContent();
                classContent = LcdpJavaCodeResolverUtils.getClassContent(classContent, history.getResourceName(),
                        history.getVersion(), null, history.getVersionOffset());
                resource.setClassContent(classContent);
                resource.setClassName(ClassManager.getClassFullName(classContent));
            }

            resource.setContent(content);
            if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_VIEW, resource.getResourceCategory())) {
                pageList.add(resource);
            }
            return resource;
        }).collect(Collectors.toList());

        resourceService.getDao().update(submitResourceList, "CONTENT", "CLASSCONTENT", "EFFECTVERSION", "MODIFYVERSION",
                "CLASSNAME", "LCDPFILEPATH");

        List<LcdpResourceHistoryBean> updateHistoryList = new ArrayList<>();
        List<LcdpResourceHistoryBean> submittedResourceHistoryList = resourceHistoryService.selectListByFilter(SearchFilter.instance()
                .match("RESOURCEID", resourceIdList).filter(MatchPattern.OR)
                .match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_YES).filter(MatchPattern.EQ));
        submittedResourceHistoryList.forEach(h -> {
            h.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_YES);
            h.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);
        });
        updateHistoryList.addAll(submittedResourceHistoryList);

        submitHistoryList.forEach(h -> {
            h.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_YES);
            h.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
        });
        updateHistoryList.addAll(submitHistoryList);

        if (!updateHistoryList.isEmpty()) {
            resourceHistoryService.getDao().update(updateHistoryList, "SUBMITFLAG", "EFFECTFLAG");
        }

        List<LcdpResourceBean> javaResourceList = submitResourceList.stream()
                .filter(r -> LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(r.getResourceCategory()))
                .collect(Collectors.toList());
        LcdpJavaCodeResolverUtils.loadAndRegisterResourceist(javaResourceList);

        javaResourceList.forEach(r -> {
            String beanName = LcdpJavaCodeResolverUtils.getBeanName(r);

            Object bean = SpringUtils.getBean(beanName);
            if (LcdpBaseService.class.isAssignableFrom(ClassUtils.getRawType(bean.getClass()))) {
                String tableName = ((LcdpBaseService) bean).getTable();
                ApplicationContextHelper.setLcdpServiceNameByTable(tableName, beanName);
                RedisHelper.put(LcdpConstant.SCRIPT_PATH_TABLE_MAPPING_CACHE, r.getPath(), tableName);
            }
        });

        TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> javaResourceList.forEach(r -> {
            LcdpJavaCodeResolverUtils.removeLoadedDevClass(r);
            LcdpJavaCodeResolverUtils.removePreLoadedProClass(r);
        }));

        List<LcdpResourceBean> mapperResourceList = submitResourceList.stream()
                .filter(r -> LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(r.getResourceCategory())
                        && StringUtils.endsWithIgnoreCase(r.getResourceName(), database + "mapper"))
                .collect(Collectors.toList());

        if (!mapperResourceList.isEmpty()) {
            TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> mapperResourceList.forEach(r -> {
                LcdpMapperUtils.loadMapper(r.getPath(), true, r.getContent());
                LcdpMapperUtils.unloadMapper(r.getPath(), false);
            }));
        }

        checkoutRecordService.removeCheckout(submitResourceList);
        clearModuleCheckoutIfNoCheckedOutChildren(
                submitResourceList.stream()
                        .map(LcdpResourceBean::getParentId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList()));
        resourceIdList.forEach(LcdpResourceSyncManager::sync);
        checkImportRecord.setResourceList(submitResourceList);
    }

    private void clearModuleCheckoutIfNoCheckedOutChildren(List<Long> moduleIdList) {
        if (CollectionUtils.isEmpty(moduleIdList)) {
            return;
        }

        String loginUserId = LocalContextHelper.getLoginUserId();
        List<Long> normalizedModuleIdList = moduleIdList.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (normalizedModuleIdList.isEmpty()) {
            return;
        }

        List<LcdpResourceBean> moduleList = resourceService.getDao().selectListByIds(normalizedModuleIdList,
                Arrays.asList("ID", "RESOURCECATEGORY", "CHECKOUTUSERID"));
        if (moduleList.isEmpty()) {
            return;
        }

        List<LcdpResourceBean> moduleChildren = resourceService.getDao().selectListByOneColumnValues(normalizedModuleIdList,
                "MODULEID", Arrays.asList("ID", "MODULEID", "RESOURCECATEGORY", "CHECKOUTUSERID", "DELETEFLAG"));

        List<Long> remainingCheckedOutModuleIdList = moduleChildren.stream()
                .filter(r -> LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(r.getResourceCategory()))
                .filter(r -> LcdpConstant.RESOURCE_DELETED_NO.equals(r.getDeleteFlag()))
                .filter(r -> !StringUtils.isBlank(r.getCheckoutUserId()))
                .map(LcdpResourceBean::getModuleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<LcdpResourceBean> clearableModuleList = moduleList.stream()
                .filter(r -> LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(r.getResourceCategory()))
                .filter(r -> !StringUtils.isBlank(r.getCheckoutUserId()))
                .filter(r -> LocalContextHelper.isAdminLogin() || Objects.equals(loginUserId, r.getCheckoutUserId()))
                .filter(r -> !remainingCheckedOutModuleIdList.contains(r.getId()))
                .collect(Collectors.toList());

        if (!clearableModuleList.isEmpty()) {
            checkoutRecordService.removeCheckout(clearableModuleList);
        }
    }

    private LcdpResourceVersionBean historyToVersion(LcdpSubmitLogBean submitLog, LcdpResourceHistoryBean history) {
        LcdpResourceVersionBean resourceVersion = new LcdpResourceVersionBean();
        resourceVersion.setId(ApplicationContextHelper.getNextIdentity());
        resourceVersion.setLogId(submitLog.getId());
        resourceVersion.setResourceId(history.getResourceId().toString());
        resourceVersion.setResourceName(history.getResourceName());
        resourceVersion.setResourceCategory(history.getResourceCategory());
        resourceVersion.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
        resourceVersion.setVersion(history.getVersion());
        resourceVersion.setCommit(submitLog.getCommit());
        resourceVersion.setEditTime(history.getCreatedTime());
        resourceVersion.setSubmitTime(LocalDateTime.now());

        String[] path = StringUtils.split(history.getPath(), ".");
        resourceVersion.setCategoryName(path[0]);
        resourceVersion.setModuleName(path[1]);

        resourceVersion.setResourceAction(history.getVersion() == 1L ? LcdpConstant.RESOURCE_SUBMIT_ACTION_NEW : LcdpConstant.RESOURCE_SUBMIT_ACTION_UPDATE);
        resourceVersion.setResourcePath(history.getPath().replaceAll("\\.", "/"));
        EntityHelper.assignCreatedElement(resourceVersion);

        return resourceVersion;
    }

    private LcdpResourceVersionBean tableToVersion(LcdpSubmitLogBean submitLog, LcdpTableBean table) {
        LcdpResourceVersionBean resourceVersion = new LcdpResourceVersionBean();
        resourceVersion.setId(ApplicationContextHelper.getNextIdentity());
        resourceVersion.setLogId(submitLog.getId());
        resourceVersion.setResourceId(table.getTableName());
        resourceVersion.setResourceName(table.getTableName());
        resourceVersion.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_TABLE);
        resourceVersion.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
        resourceVersion.setVersion(table.getVersion());
        resourceVersion.setCommit(submitLog.getCommit());
        resourceVersion.setEditTime(table.getCreatedTime());
        resourceVersion.setSubmitTime(LocalDateTime.now());
        resourceVersion.setResourceAction(table.getVersion() == 1L ? LcdpConstant.RESOURCE_SUBMIT_ACTION_NEW : LcdpConstant.RESOURCE_SUBMIT_ACTION_UPDATE);
        resourceVersion.setResourcePath(table.getTableName());
        EntityHelper.assignCreatedElement(resourceVersion);

        return resourceVersion;
    }

    private LcdpResourceVersionBean viewToVersion(LcdpSubmitLogBean submitLog, LcdpViewBean view) {
        LcdpResourceVersionBean resourceVersion = new LcdpResourceVersionBean();
        resourceVersion.setId(ApplicationContextHelper.getNextIdentity());
        resourceVersion.setLogId(submitLog.getId());
        resourceVersion.setResourceId(view.getViewName());
        resourceVersion.setResourceName(view.getViewName());
        resourceVersion.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_DB_VIEW);
        resourceVersion.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
        resourceVersion.setVersion(view.getVersion());
        resourceVersion.setCommit(submitLog.getCommit());
        resourceVersion.setEditTime(view.getCreatedTime());
        resourceVersion.setSubmitTime(LocalDateTime.now());
        resourceVersion.setResourceAction(view.getVersion() == 1L ? LcdpConstant.RESOURCE_SUBMIT_ACTION_NEW : LcdpConstant.RESOURCE_SUBMIT_ACTION_UPDATE);
        resourceVersion.setResourcePath(view.getViewName());
        EntityHelper.assignCreatedElement(resourceVersion);

        return resourceVersion;
    }

    private LcdpResourceVersionBean globalConfigToVersion(LcdpSubmitLogBean submitLog, LcdpGlobalConfigBean config) {
        LcdpResourceVersionBean resourceVersion = new LcdpResourceVersionBean();
        resourceVersion.setId(ApplicationContextHelper.getNextIdentity());
        resourceVersion.setLogId(submitLog.getId());
        resourceVersion.setResourceId(config.getConfigCode());
        resourceVersion.setResourceName(I18nHelper.getMessage("T_LCDP_GLOBAL_CONFIG." + config.getConfigCode() + ".NAME"));
        resourceVersion.setResourcePath(resourceVersion.getResourceName());
        resourceVersion.setResourceCategory(config.getConfigCode());
        resourceVersion.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
        resourceVersion.setVersion(config.getVersion());
        resourceVersion.setCommit(submitLog.getCommit());
        resourceVersion.setEditTime(config.getCreatedTime());
        resourceVersion.setSubmitTime(LocalDateTime.now());
        resourceVersion.setResourceAction(config.getVersion() == 1L ? LcdpConstant.RESOURCE_SUBMIT_ACTION_NEW : LcdpConstant.RESOURCE_SUBMIT_ACTION_UPDATE);
        EntityHelper.assignCreatedElement(resourceVersion);

        return resourceVersion;
    }


    private void dealJavaListForDataClean(List<LcdpResourceBean> javaList) {
        javaList.stream().filter(java -> StringUtils.isNotBlank(java.getClassContent())).forEach(resource -> {
            String classContent = resource.getClassContent();
            //替换脚本类名
            classContent = LcdpJavaCodeResolverUtils.getClassContent(classContent, resource.getResourceName(),
                    1L, null, null);
            resource.setClassContent(classContent);
            resource.setClassName(ClassManager.getClassFullName(classContent));
            resource.setModifyVersion(null);
        });
        resourceService.getDao().update(javaList, "classContent", "modifyVersion", "CLASSNAME");
    }


    //---------------------------------------------------------------------------------
    // 私有类
    //---------------------------------------------------------------------------------

    /**
     * 操作模块时（复制，移动），所有资源内容中 分类.模块.目录. 全部替换掉
     */
    private static class ModuleOperator {
        private static void updateContent(LcdpResourceBean newResource, LcdpResourceBean oldResource) {
            String content = oldResource.getContent();

            if (StringUtils.isBlank(content)) {
                return;
            }

            newResource.setContent(getUpdatedContent(content, newResource.getPath(), oldResource.getPath()));
        }

        private static void updateContent(LcdpResourceHistoryBean newHistory, LcdpResourceHistoryBean oldHistory) {
            String content = oldHistory.getContent();

            if (StringUtils.isBlank(content)) {
                return;
            }

            newHistory.setContent(getUpdatedContent(content, newHistory.getPath(), oldHistory.getPath()));
        }

        private static void updateClassContent(LcdpResourceHistoryBean newHistory, LcdpResourceHistoryBean oldHistory) {
            String content = oldHistory.getClassContent();

            if (StringUtils.isBlank(content)) {
                return;
            }

            newHistory.setClassContent(getUpdatedContent(content, newHistory.getPath(), oldHistory.getPath()));
        }


        private static String getUpdatedContent(String content, String newPath, String oldPath) {
            if (StringUtils.isBlank(content)) {
                return content;
            }

            String[] oldPathSplits = StringUtils.split(oldPath, ".");
            String oldPrefix = oldPathSplits[0] + "." + oldPathSplits[1];
            String[] newPathSplits = StringUtils.split(newPath, ".");
            String newPrefix = newPathSplits[0] + "." + newPathSplits[1];

            content = StringUtils.replace(content, oldPrefix + ".page.", newPrefix + ".page.");
            content = StringUtils.replace(content, oldPrefix + ".client.", newPrefix + ".client.");
            content = StringUtils.replace(content, oldPrefix + ".server.", newPrefix + ".server.");
            content = StringUtils.replace(content, oldPrefix + ".mapper.", newPrefix + ".mapper.");

            return content;
        }
    }

    /**
     * 操作脚本时（复制，移动），资源内容中 Path替换成新的，对于Mapper文件还要特殊处理
     */
    private static class ScriptOperator {
        private static void updateContent(LcdpResourceBean newResource, LcdpResourceBean oldResource) {
            String content = oldResource.getContent();

            if (StringUtils.isBlank(content)) {
                return;
            }

            newResource.setContent(getUpdatedContent(content, newResource.getPath(), oldResource.getPath()));
        }

        private static void updateContent(LcdpResourceHistoryBean newHistory, LcdpResourceHistoryBean oldHistory) {
            String content = oldHistory.getContent();

            if (StringUtils.isBlank(content)) {
                return;
            }

            newHistory.setContent(getUpdatedContent(content, newHistory.getPath(), oldHistory.getPath()));
        }

        private static String getUpdatedContent(String content, String newPath, String oldPath) {
            if (StringUtils.isBlank(content)) {
                return content;
            }

            content = StringUtils.replace(content, oldPath, newPath);

            // 替换类名
            String oldResourceName = oldPath.substring(oldPath.lastIndexOf(".") + 1);
            String newResourceName = newPath.substring(newPath.lastIndexOf(".") + 1);
            content = content.replaceAll("public\\sclass\\s" + oldResourceName, "public class " + newResourceName);

            // 特殊处理mybatis
            if (content.contains("<mapper ") && content.contains("</mapper>")) {
                String oldNamespace = LcdpConstant.MAPPER_TMPL_NAME_LIST.stream()
                        .filter(n -> StringUtils.endsWith(oldPath, n))
                        .map(n -> StringUtils.removeEnd(oldPath, n) + "Mapper")
                        .findAny().get();

                String newNamespace = LcdpConstant.MAPPER_TMPL_NAME_LIST.stream()
                        .filter(n -> StringUtils.endsWith(newPath, n))
                        .map(n -> StringUtils.removeEnd(newPath, n) + "Mapper")
                        .findAny().get();

                content = StringUtils.replace(content, "\"" + oldNamespace + "\"", "\"" + newNamespace + "\"");
            }

            return content;
        }
    }
}
