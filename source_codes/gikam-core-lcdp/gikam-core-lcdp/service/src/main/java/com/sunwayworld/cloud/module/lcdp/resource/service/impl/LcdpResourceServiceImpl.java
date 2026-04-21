package com.sunwayworld.cloud.module.lcdp.resource.service.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.sunwayworld.cloud.module.lcdp.base.service.LcdpBaseService;
import com.sunwayworld.cloud.module.lcdp.appmarket.service.LcdpAppMarketService;
import com.sunwayworld.cloud.module.lcdp.base.annotation.Mapping;
import com.sunwayworld.cloud.module.lcdp.base.annotation.MappingType;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCheckoutRecordBean;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.service.LcdpCheckoutRecordService;
import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpGlobalConfigBean;
import com.sunwayworld.cloud.module.lcdp.config.service.LcdpGlobalConfigService;
import com.sunwayworld.cloud.module.lcdp.configparam.service.LcdpConfigParamService;
import com.sunwayworld.cloud.module.lcdp.hints.bean.CoreHintsChainingCallDTO;
import com.sunwayworld.cloud.module.lcdp.hints.core.CodeHintsCallType;
import com.sunwayworld.cloud.module.lcdp.hints.core.CodeHintsManager;
import com.sunwayworld.cloud.module.lcdp.importrecord.bean.LcdpCheckImportDataDTO;
import com.sunwayworld.cloud.module.lcdp.importrecord.bean.LcdpRImportRecordDetailBean;
import com.sunwayworld.cloud.module.lcdp.importrecord.bean.LcdpResourceImportRecordBean;
import com.sunwayworld.cloud.module.lcdp.importrecord.service.LcdpRImportRecordDetailService;
import com.sunwayworld.cloud.module.lcdp.importrecord.service.LcdpResourceImportRecordService;
import com.sunwayworld.cloud.module.lcdp.message.log.LcdpScriptLogConfig;
import com.sunwayworld.cloud.module.lcdp.message.sync.LcdpResourceSyncManager;
import com.sunwayworld.cloud.module.lcdp.moduletmpl.service.LcdpModuleTmplService;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpAnalysisResultDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpExportLogFileDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpJavaStructureDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModulePageCompBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModulePageI18nBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpPageI18nCodeBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceCheckoutConfigDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceCheckoutRecordBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceClassInfoDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceCompareDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceComparisonContentDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceComparisonDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceComparisonItemDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceComparisonVersionDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceContentDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceExportDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceMoveoutDataDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceSearchDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpServerScriptMethodBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpWorkspaceDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.ServerScriptType;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.LcdpResourceDao;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpModulePageCompService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpModulePageI18nService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpPageI18nCodeService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceCheckoutRecordService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceHistoryService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceTreeService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpServerScriptMethodService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpViewButtonRoleService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpJavaCodeResolverUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpMapperUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpPageCompConfigAnalyseHelper;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpReflectionUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpScriptUtils;
import com.sunwayworld.cloud.module.lcdp.resourcefile.bean.LcdpResourceFileBean;
import com.sunwayworld.cloud.module.lcdp.resourcefile.service.LcdpResourceFileService;
import com.sunwayworld.cloud.module.lcdp.resourcelock.bean.LcdpResourceLockBean;
import com.sunwayworld.cloud.module.lcdp.resourcelock.service.LcdpResourceLockService;
import com.sunwayworld.cloud.module.lcdp.resourceversion.bean.LcdpResourceVersionBean;
import com.sunwayworld.cloud.module.lcdp.resourceversion.service.LcdpResourceVersionService;
import com.sunwayworld.cloud.module.lcdp.scriptblock.bean.LcdpScriptBlockBean;
import com.sunwayworld.cloud.module.lcdp.scriptblock.bean.LcdpScriptBlockTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.scriptblock.service.LcdpScriptBlockService;
import com.sunwayworld.cloud.module.lcdp.submitlog.bean.LcdpSubmitLogBean;
import com.sunwayworld.cloud.module.lcdp.submitlog.service.LcdpSubmitLogService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableIndexBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpViewDTO;
import com.sunwayworld.cloud.module.lcdp.table.helper.LcdpTableUtils;
import com.sunwayworld.cloud.module.lcdp.table.persistent.dao.LcdpDatabaseDao;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpDatabaseService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableFieldService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableIndexService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpViewService;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.cache.memory.MemoryCacheManager;
import com.sunwayworld.framework.cache.redis.RedisHelper;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.context.concurrent.GikamConcurrentLocker;
import com.sunwayworld.framework.data.CaseInsensitiveLinkedMap;
import com.sunwayworld.framework.data.ListChunkIterator;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.data.page.PageRequest;
import com.sunwayworld.framework.data.page.Pagination;
import com.sunwayworld.framework.database.context.instance.EntityHelper;
import com.sunwayworld.framework.database.core.DatabaseManager;
import com.sunwayworld.framework.database.dialect.Dialect;
import com.sunwayworld.framework.database.dialect.DialectRepository;
import com.sunwayworld.framework.database.sql.Order;
import com.sunwayworld.framework.exception.FileException;
import com.sunwayworld.framework.exception.checked.CheckedException;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.exception.core.ApplicationWarningException;
import com.sunwayworld.framework.executor.manager.TaskExecutorManager;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.io.file.FilePathDTO;
import com.sunwayworld.framework.io.file.FileScope;
import com.sunwayworld.framework.io.file.path.FilePathService;
import com.sunwayworld.framework.jdk.core.ClassManager;
import com.sunwayworld.framework.jdk.tools.DynamicClassLoader;
import com.sunwayworld.framework.jdk.tools.LoadMultipleResult;
import com.sunwayworld.framework.mybatis.MybatisTimeZoneHelper;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.mybatis.page.MybatisPageHelper;
import com.sunwayworld.framework.mybatis.page.PageRowBounds;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.security.bean.LoginUser;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.PersistableHelper;
import com.sunwayworld.framework.support.base.service.GenericService;
import com.sunwayworld.framework.support.tree.TreeDescriptor;
import com.sunwayworld.framework.support.tree.TreeHelper;
import com.sunwayworld.framework.support.tree.bean.AbstractTreeNode;
import com.sunwayworld.framework.tenant.TenantConstant;
import com.sunwayworld.framework.tenant.TenantContext;
import com.sunwayworld.framework.tenant.TenantManager;
import com.sunwayworld.framework.utils.ArchiveUtils;
import com.sunwayworld.framework.utils.ArrayUtils;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.ConvertUtils;
import com.sunwayworld.framework.utils.EncryptUtils;
import com.sunwayworld.framework.utils.FileUtils;
import com.sunwayworld.framework.utils.JsonUtils;
import com.sunwayworld.framework.utils.LcdpUtils;
import com.sunwayworld.framework.utils.NumberUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.ServletUtils;
import com.sunwayworld.framework.utils.SpringUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.admin.config.bean.CoreAdminSelectConfigBean;
import com.sunwayworld.module.admin.config.service.CoreAdminSelectConfigService;
import com.sunwayworld.module.admin.request.bean.CoreRequestUrlBean;
import com.sunwayworld.module.admin.request.bean.CoreRequestUrlWhitelistBean;
import com.sunwayworld.module.admin.request.service.CoreRequestUrlService;
import com.sunwayworld.module.admin.request.service.CoreRequestUrlWhitelistService;
import com.sunwayworld.module.design.studio.bean.CoreStudioAdvSearchConfigBean;
import com.sunwayworld.module.design.studio.service.CoreStudioAdvSearchConfigService;
import com.sunwayworld.module.item.file.bean.CoreFileBean;
import com.sunwayworld.module.item.file.manager.CoreFileManager;
import com.sunwayworld.module.item.file.service.CoreFileService;
import com.sunwayworld.module.item.file.utils.CoreFileUtils;
import com.sunwayworld.module.mdm.user.bean.CoreUserBean;
import com.sunwayworld.module.mdm.user.service.CoreUserService;
import com.sunwayworld.module.sys.code.bean.CoreCodeBean;
import com.sunwayworld.module.sys.code.bean.CoreCodeCategoryBean;
import com.sunwayworld.module.sys.code.service.CoreCodeCategoryService;
import com.sunwayworld.module.sys.code.service.CoreCodeService;
import com.sunwayworld.module.sys.constant.bean.CoreConstantBean;
import com.sunwayworld.module.sys.constant.service.CoreConstantService;
import com.sunwayworld.module.sys.i18n.bean.CoreI18nBean;
import com.sunwayworld.module.sys.i18n.bean.CoreI18nMessageBean;
import com.sunwayworld.module.sys.i18n.service.CoreI18nMessageService;
import com.sunwayworld.module.sys.i18n.service.CoreI18nService;
import com.sunwayworld.module.sys.menu.bean.CoreMenuBean;
import com.sunwayworld.module.sys.menu.service.CoreMenuService;
import com.sunwayworld.module.sys.page.bean.CorePageConfigDTO;
import com.sunwayworld.module.sys.page.service.CorePageConfigService;
import com.sunwayworld.module.sys.role.bean.CoreRolePermissionBean;
import com.sunwayworld.module.sys.role.service.CoreRolePermissionService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@GikamBean
public class LcdpResourceServiceImpl implements LcdpResourceService {
    private static final Logger log = LoggerFactory.getLogger(LcdpResourceServiceImpl.class);

    private static final boolean designCenterTenantFlag = ApplicationContextHelper.getEnvironment().getProperty("sunway.design-center-tenant.enabled", Boolean.class, false);

    private static final Pattern startDotPattern = Pattern.compile("^\\s*\\.\\s*[A-Za-z0-9_]");

    private static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<>();

    private static final String REGISTER_PRD = "REGISTER_PRD";

    private static final String COPY_MAPPER_DELETE_SOURCE_CONSTANT = "LCDP_COPY_MAPPER_DELETE_SOURCE";

    private volatile Map<Long, LcdpResourceBean> committedAspectCache;
    private Map<String, Class<?>> committedAspectClassCache;

    private volatile Map<String, Class<?>> unCommittedScriptAspects;

    private volatile Map<Long, LcdpResourceBean> committedFilterCache;
    private CopyOnWriteArrayList<String> committedFilterNameCache;

    @Autowired
    private LcdpResourceDao lcdpResourceDao;

    @Autowired
    @Lazy
    private LcdpViewButtonRoleService viewButtonRoleService;

    @Autowired
    private LcdpResourceHistoryService lcdpResourceHistoryService;

    @Autowired
    private LcdpModulePageCompService lcdpModulePageCompService;

    @Autowired
    @Lazy
    private LcdpModulePageI18nService lcdpModulePageI18nService;

    @Autowired
    private LcdpPageI18nCodeService lcdpPageI18nCodeService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CoreI18nMessageService coreI18nMessageService;

    @Autowired
    private CoreI18nService coreI18nService;

    @Autowired
    private LcdpResourceLockService lcdpResourceLockService;

    @Autowired
    private LcdpTableService lcdpTableService;

    @Autowired
    private LcdpViewService lcdpViewService;

    @Autowired
    private LcdpDatabaseService lcdpDatabaseService;

    @Autowired
    private LcdpServerScriptMethodService serverScriptMethodService;

    @Autowired
    private CoreFileManager fileManager;

    @Autowired
    private CoreFileService coreFileService;

    @Autowired
    private CoreUserService coreUserService;

    @Autowired
    private TenantManager tenantManager;

    @Autowired
    @Lazy
    private LcdpResourceVersionService lcdpResourceVersionService;

    @Autowired
    @Lazy
    private LcdpAppMarketService lcdpAppMarketService;


    @Autowired
    private LcdpResourceImportRecordService resourceImportRecordService;

    @Autowired
    @Lazy
    private LcdpModuleTmplService moduleTmplService;

    @Autowired
    private CoreMenuService coreMenuService;

    @Autowired
    private CoreCodeCategoryService coreCodeCategoryService;

    @Autowired
    private CoreCodeService coreCodeService;

    @Autowired
    private LcdpTableFieldService lcdpTableFieldService;

    @Autowired
    private LcdpTableIndexService lcdpTableIndexService;

    @Lazy
    @Autowired
    private LcdpResourceService proxyInstance;

    @Lazy
    @Autowired
    private LcdpSubmitLogService submitLogService;

    @Autowired
    private Dialect dialect;

    @Autowired
    private LcdpDatabaseDao lcdpDatabaseDao;

    @Autowired
    @Lazy
    private LcdpCheckoutRecordService checkoutRecordService;

    @Autowired
    @Lazy
    private LcdpScriptBlockService scriptBlockService;

    @Autowired
    @Lazy
    private LcdpResourceCheckoutRecordService resourceCheckoutRecordService;

    @Autowired
    @Lazy
    private CoreRolePermissionService rolePermissionService;

    @Autowired
    private CoreAdminSelectConfigService coreAdminSelectConfigService;

    @Autowired
    private LcdpGlobalConfigService lcdpGlobalConfigService;

    @Autowired
    private CorePageConfigService corePageConfigService;

    @Autowired
    private CoreStudioAdvSearchConfigService coreStudioAdvSearchConfigService;

    @Autowired
    private LcdpResourceFileService resourceFileService;

    @Autowired
    private FilePathService filePathService;

    @Autowired
    private CoreConstantService coreConstantService;

    @Autowired
    private CoreRequestUrlService coreRequestUrlService;

    @Autowired
    private CoreRequestUrlWhitelistService requestUrlWhitelistService;

    @Autowired
    private LcdpConfigParamService lcdpConfigParamService;

    @Lazy
    @Autowired
    private LcdpResourceTreeService resourceTreeService;

    @Lazy
    @Autowired
    private LcdpRImportRecordDetailService resourceImportRecordDetailService;

    private String current_environment_database = LcdpConstant.PROFILE_DB_LIST.stream().filter(profile -> ApplicationContextHelper.isProfileActivated(profile)).findFirst().get();

    @Override
    @SuppressWarnings("unchecked")
    public LcdpResourceDao getDao() {
        return lcdpResourceDao;
    }

    @Override
    public void save(Long id, RestJsonWrapperBean wrapper) {
        throw new ApplicationRuntimeException("找到你了，你是做啥操作了？？？");
    }

    /**
     * 用于common待办调用低代码脚本
     */
    @Override
    public Page<?> callTodoScript(String path, RestJsonWrapperBean jsonWrapper) {
        String result = LcdpScriptUtils.callScriptMethod(path, jsonWrapper);

        Page<?> page = JSON.parseObject(result, Page.class);

        return page;
    }

    @Override
    public Map<String, Class<?>> getAspectClassMap() {
        if (null == committedAspectCache) {
            synchronized (this) {
                if (null == committedAspectCache) {
                    committedAspectCache = new ConcurrentHashMap<>();
                    committedAspectClassCache = new ConcurrentHashMap<>();
                    List<LcdpResourceBean> aspectScriptList = selectListByFilter(SearchFilter.instance()
                            .match("SCRIPTTYPE", ServerScriptType.aspect.name()).filter(MatchPattern.EQ)
                            .match("EFFECTVERSION", null).filter(MatchPattern.DIFFER)
                            .match("CLASSCONTENT", null).filter(MatchPattern.DIFFER)
                            .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));
                    aspectScriptList.forEach(resource -> {
                        committedAspectCache.put(resource.getId(), resource);
                        Class<?> clazz = ClassManager.getClassByFullName(ClassManager.getClassFullName(resource.getClassContent()));

                        if (clazz != null) {
                            String beanName = LcdpJavaCodeResolverUtils.getBeanName(resource.getClassContent());
                            committedAspectClassCache.put(beanName, clazz);
                        }
                    });
                }
            }
        }

        Map<String, Class<?>> map = new HashMap<>(committedAspectClassCache);

        //应用启动时，注册未提交的资源使用最新保存的切面，并缓存该结果
        if (Constant.YES.equals(MemoryCacheManager.get(LcdpConstant.LCDP_ASPECT_UNCOMMITTED_FLAG))) {
            if (null == unCommittedScriptAspects) {
                synchronized (this) {
                    if (null == unCommittedScriptAspects) {
                        List<LcdpResourceHistoryBean> unCommittedAspects = lcdpResourceHistoryService.selectDevScriptList(ServerScriptType.aspect.name());

                        for (LcdpResourceHistoryBean unCommittedAspect : unCommittedAspects) {
                            Long resourceId = unCommittedAspect.getResourceId();
                            if (committedAspectCache.containsKey(resourceId)) {
                                String invalidAspectBeanName = LcdpJavaCodeResolverUtils.getBeanName(committedAspectCache.get(resourceId));
                                map.remove(invalidAspectBeanName);
                            }

                            Class<?> clazz = ClassManager.getClassByFullName(ClassManager.getClassFullName(unCommittedAspect.getClassContent()));

                            if (clazz == null) {
                                continue;
                            }

                            String beanName = LcdpJavaCodeResolverUtils.getBeanName(clazz);

                            // 加入最新保存版
                            map.put(beanName, clazz);
                        }
                        unCommittedScriptAspects = new ConcurrentHashMap<>(map);
                    }
                }
            } else {
                map = new HashMap<>(unCommittedScriptAspects);
            }
        }

        String callType = LcdpUtils.isDebugRequest() ? LcdpConstant.SCRIPT_CALL_DEV : LcdpConstant.SCRIPT_CALL_PRO;
        //避免提交时使用未提交的切面
        Boolean isRegisterPrd = REGISTER_PRD.equals(THREAD_LOCAL.get());
        if (LcdpConstant.SCRIPT_CALL_DEV.equals(callType) && !isRegisterPrd) {
            //处理检出的和新建的切面
            List<LcdpResourceHistoryBean> unCommittedAspects = lcdpResourceHistoryService.selectDevScriptList(ServerScriptType.aspect.name());

            List<LcdpResourceLockBean> aspectScriptLockList = lcdpResourceLockService.selectResourceLockListByCategory(LocalContextHelper.getLoginUserId(),
                    LcdpConstant.RESOURCE_CATEGORY_JAVA);

            for (LcdpResourceHistoryBean resource : unCommittedAspects) {
                Long resourceId = resource.getResourceId();
                if (aspectScriptLockList.stream().anyMatch(e -> e.getResourceId().equals(resourceId.toString()))) {
                    if (committedAspectCache.containsKey(resourceId)) {
                        //如果已经提交过，先移除，再添加当前版本
                        String invalidAspectFullName = ClassManager.getClassFullName(committedAspectCache.get(resourceId).getClassContent());
                        map.remove(invalidAspectFullName);
                    }
                    String beanName = LcdpJavaCodeResolverUtils.getBeanName(resource.getClassContent());
                    Class<?> clazz = ClassManager.getClassByFullName(ClassManager.getClassFullName(resource.getClassContent()));

                    // 加入最新保存版
                    if (clazz != null) {
                        map.put(beanName, clazz);
                    }
                }
            }
        }

        return map;
    }

    @Override
    public List<String> getFilterBeanNameList() {
        if (null == committedFilterCache) {
            synchronized (this) {
                if (null == committedFilterCache) {
                    List<LcdpResourceBean> filterResources = selectListByFilter(SearchFilter.instance()
                            .match("SCRIPTTYPE", ServerScriptType.filter.name()).filter(MatchPattern.EQ)
                            .match("EFFECTVERSION", null).filter(MatchPattern.DIFFER)
                            .match("CLASSCONTENT", null).filter(MatchPattern.DIFFER)
                            .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));
                    committedFilterCache = filterResources.stream().collect(Collectors.toMap(LcdpResourceBean::getId, Function.identity()));
                    committedFilterNameCache = committedFilterCache.values().stream()
                            .filter(filter -> LcdpJavaCodeResolverUtils.isBeanExists(filter.getClassContent()))
                            .map(filter -> LcdpJavaCodeResolverUtils.getBeanName(filter.getClassContent()))
                            .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
                }
            }
        }
        List<String> filterBeanNameList = new ArrayList<>(committedFilterNameCache);
        String callType = LcdpUtils.isDebugRequest() ? LcdpConstant.SCRIPT_CALL_DEV : LcdpConstant.SCRIPT_CALL_PRO;
        if (LcdpConstant.SCRIPT_CALL_DEV.equals(callType)) {
            List<LcdpResourceHistoryBean> unCommittedFilters = lcdpResourceHistoryService.selectDevScriptList(ServerScriptType.filter.name());

            List<LcdpResourceLockBean> FilterScriptLockList = lcdpResourceLockService.selectResourceLockListByCategory(LocalContextHelper.getLoginUserId(),
                    LcdpConstant.RESOURCE_CATEGORY_JAVA);

            for (LcdpResourceHistoryBean resource : unCommittedFilters) {
                Long resourceId = resource.getResourceId();
                if (FilterScriptLockList.stream().anyMatch(e -> e.getResourceId().equals(resourceId.toString()))) {
                    //处理检出的过滤器，剔除发布版本
                    if (committedFilterCache.containsKey(resourceId)) {
                        String invalidAspectBeanName = LcdpJavaCodeResolverUtils.getBeanName(committedFilterCache.get(resourceId));
                        filterBeanNameList.remove(invalidAspectBeanName);
                    }

                    String filterBeanName = LcdpJavaCodeResolverUtils.getBeanName(resource.getClassContent());
                    // 加入最新保存版
                    if (!SpringUtils.isBeanExists(filterBeanName)) {
                        Class<?> clazz = LcdpJavaCodeResolverUtils.loadSourceCode(resource);

                        LcdpJavaCodeResolverUtils.registerBean(clazz);

                        filterBeanNameList.add(filterBeanName);
                    }
                }
            }
        }
        return filterBeanNameList;
    }

    @Override
    public Long selectPageIdByPath(RestJsonWrapperBean wrapper) {
        String resourcePath = wrapper.getParamValue("resourcePath");
        if (StringUtils.isEmpty(resourcePath)) {
            return null;
        }
        LcdpResourceBean view = selectFirstByFilter(SearchFilter.instance().match("path", resourcePath).filter(MatchPattern.EQ).match("deleteFlag", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));
        if (view == null) {
            return null;
        }
        return view.getId();
    }

    @Override
    public Long selectByPath(RestJsonWrapperBean wrapper) {
        String resourcePath = wrapper.getParamValue("resourcePath");
        int lastDotIndex = resourcePath.lastIndexOf(".");

        //从前端传过来的调用路径中截取脚本路径和调用方法名
        String scriptPath = resourcePath.substring(0, lastDotIndex);
        String path = StringUtils.endsWithIgnoreCase(scriptPath, LcdpConstant.RESOURCE_CATEGORY_MAPPER) ? StringUtils.replaceLast(scriptPath, "Mapper", LcdpMapperUtils.DIALECT_MAPPER_SUFFIX) : scriptPath;

        LcdpResourceBean resource = selectFirstByFilter(SearchFilter.instance().match("path", path).filter(MatchPattern.EQ));
        if (ObjectUtils.isEmpty(resource)) {
            return null;
        }
        if (resource.getEffectVersion() != null || StringUtils.equals(LocalContextHelper.getLoginUserId(), resource.getCreatedById())) {
            return resource.getId();
        }

        return null;
    }

    /**
     * 根据前端传来的资源ID来查询资源信息
     */
    @Override
    public LcdpResourceDTO selectResourceContent(Long id) {
        // 预览请求，直接调用应用超市接口获取资源信息
        if (LcdpUtils.isPreviewRequest()) {
            String rntJsonString = (String) lcdpAppMarketService.getFuncResourceContent(id);
            return JsonUtils.parse(rntJsonString, LcdpResourceDTO.class);
        }

        LcdpResourceBean condition = new LcdpResourceBean();
        condition.setId(id);
        List<String> queryColumnList = ArrayUtils.asList("ID", "EFFECTVERSION");
        LcdpResourceBean resource = getDao().selectFirstIfPresent(condition, ArrayUtils.asList("ID"), queryColumnList);

        if (resource == null) {
            return new LcdpResourceDTO();
        }

        return getDesignResourceDTO(resource);
    }

    @Override
    public List<LcdpResourceDTO> selectCategorySelectableList() {

        List<LcdpResourceBean> resourceList = selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_CATEGORY).filter(MatchPattern.SEQ).match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ), Order.asc("RESOURCENAME"));
        List<LcdpResourceDTO> resourceDTOList = resourceList.stream().map(resource -> {
            LcdpResourceDTO resourceDTO = new LcdpResourceDTO();
            resourceDTO.setId(resource.getId());
            resourceDTO.setName(resource.getResourceName());
            resourceDTO.setType(resource.getResourceCategory());
            return resourceDTO;
        }).collect(Collectors.toList());
        return resourceDTOList;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.SAVE)
    public void renameResource(Long id, RestJsonWrapperBean wrapper) {
        String newName = wrapper.getParamValue("newName");

        if (!StringUtils.startsWith(newName, "Lcdp")) {
            throw new ApplicationRuntimeException("LCDP.MODULE.RESOUCES.TIP.INCALID_RESOURCENAME_PREFIX", newName);
        }

        List<LcdpResourceHistoryBean> resourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", id).filter(MatchPattern.SEQ));

        List<LcdpResourceHistoryBean> filterResourceHistoryList = resourceHistoryList.stream().filter(history -> LocalContextHelper.getLoginUserId().equals(history.getCreatedById())
                && LcdpConstant.SUBMIT_FLAG_NO.equals(history.getSubmitFlag())).collect(Collectors.toList());

        if (!filterResourceHistoryList.isEmpty()) {
            LcdpResourceHistoryBean resourceHistory = filterResourceHistoryList.get(0);

            rename(Arrays.asList(resourceHistory), newName);
        }
    }


    @Override
    @Transactional
    public RestValidationResultBean preRenameResourceValidate(Long id, RestJsonWrapperBean wrapper) {
        String newName = wrapper.getParamValue("newName");

        if (!StringUtils.startsWith(newName, "Lcdp")) {
            return new RestValidationResultBean(false, I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.INVALID_RESOURCENAME_PREFIX", newName));
        }

        List<LcdpResourceHistoryBean> resourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", id).filter(MatchPattern.SEQ));

        List<LcdpResourceHistoryBean> filterResourceHistoryList = resourceHistoryList.stream().filter(history -> LocalContextHelper.getLoginUserId().equals(history.getCreatedById())
                && LcdpConstant.SUBMIT_FLAG_NO.equals(history.getSubmitFlag())).collect(Collectors.toList());

        if (!filterResourceHistoryList.isEmpty()) {
            LcdpResourceHistoryBean resourceHistory = filterResourceHistoryList.get(0);

            String path = resourceHistory.getPath();
            String newPath = path.substring(0, path.lastIndexOf(".") + 1) + newName;

            LcdpResourceBean filter = new LcdpResourceBean();
            filter.setPath(newPath);
            if (getDao().countBy(filter) > 0) {
                return new RestValidationResultBean(false, I18nHelper.getMessage("core.validator.column.not-unique"));
            }

            List<LcdpResourceBean> referenceResourceList = this.selectReferencedList(resourceHistory.getPath());

            if (!referenceResourceList.isEmpty()) {
                return new RestValidationResultBean(true, JSON.toJSONString(referenceResourceList));
            }

            return new RestValidationResultBean(true);
        }

        return new RestValidationResultBean(false, I18nHelper.getMessage("GIKAM.TIP.NOT_PERMITTED"));
    }

    @Override
    public Page<LcdpResourceBean> selectPageChoosablePagination(RestJsonWrapperBean wrapper) {
        MapperParameter parameter = wrapper.extractMapFilter();

        // 选择查询
        parameter.setChoosableQueries();

        parameter.putAll(wrapper.getExtFilter());
        Page<Map<String, Object>> page = new Pagination<>();
        PageRowBounds rowBounds = wrapper.extractPageRowBounds();
        if (designCenterTenantFlag && StringUtils.isNotEmpty(TenantContext.getTenant())) {
            page = tenantManager.call("lcdpChoose", TenantConstant.TENANT_LCDP_DESIGN_NAME, () -> {
                return MybatisPageHelper.get(rowBounds, () -> getDao().selectPageByCondition(parameter));
            });
        } else {
            page = MybatisPageHelper.get(rowBounds, () -> getDao().selectPageByCondition(parameter));
        }

        if (page.getRows().isEmpty()) {
            return new Pagination<>(page, CollectionUtils.emptyList());
        }

        List<LcdpResourceBean> itemList = page.getRows().parallelStream().map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceBean.class)).collect(Collectors.toList());

        return new Pagination<>(page, itemList);
    }

    @Override
    public Page<LcdpResourceBean> selectChoosablePagination(RestJsonWrapperBean wrapper) {
        MapperParameter parameter = wrapper.extractMapFilter();
        parameter.setFilter(SearchFilter.instance().match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));


        parameter.setChoosableQueries();

        parameter.putAll(wrapper.getExtFilter());
        Page<Map<String, Object>> page = new Pagination<>();
        PageRowBounds rowBounds = wrapper.extractPageRowBounds();
        if (designCenterTenantFlag && StringUtils.isNotEmpty(TenantContext.getTenant())) {
            page = tenantManager.call("lcdpChoose", TenantConstant.TENANT_LCDP_DESIGN_NAME, () -> {
                return MybatisPageHelper.get(rowBounds, () -> getDao().selectByCondition(parameter));
            });
        } else {
            page = MybatisPageHelper.get(rowBounds, () -> getDao().selectByCondition(parameter));
        }


        if (page.getRows().isEmpty()) {
            return new Pagination<>(page, CollectionUtils.emptyList());
        }

        List<LcdpResourceBean> itemList = page.getRows().parallelStream().map(m -> PersistableHelper.mapToPersistable(m, LcdpResourceBean.class)).collect(Collectors.toList());

        return new Pagination<>(page, itemList);
    }

    @Override
    public List<LcdpJavaStructureDTO> selectJavaScriptStructure(RestJsonWrapperBean wrapper) {
        String idStr = wrapper.getParamValue("resourceId");
        Long resourceId = Long.valueOf(idStr);
        LcdpResourceBean resource = selectById(resourceId);
        List<LcdpResourceHistoryBean> resourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", resourceId).filter(MatchPattern.EQ));
        LcdpResourceHistoryBean resourceHistory = resourceHistoryList.stream().filter(r -> StringUtils.equals(r.getSubmitFlag(), LcdpConstant.SUBMIT_FLAG_NO) && StringUtils.equals(r.getCreatedById(), LocalContextHelper.getLoginUserId())).findFirst().orElse(null);
        String classContent = null;
        if (resourceHistory == null) {
            //说明当前登录人看到的数据是已生效的 那么为了避免是刚启动历史表的数据并没有加载到spring里面  直接从resource表里拿
            classContent = resource.getClassContent();
        } else {
            classContent = resourceHistory.getClassContent();
        }
        String classFullName = ClassManager.getClassFullName(classContent);
        Class<?> javaClass = ClassManager.getClassByFullName(classFullName);
        return this.getClassStructureInfo(javaClass, classContent);
    }

    @Override
    public String callScript(Object... args) {
        //获取资源路径,方法名称
        String scriptPath = ServletUtils.getCurrentRequest().getHeader(LcdpConstant.REQUEST_HEADER_SCRIPTPATH);

        if (StringUtils.isEmpty(scriptPath)) {
            RestJsonWrapperBean wrapper = (RestJsonWrapperBean) Arrays.stream(args).filter(a -> a != null && RestJsonWrapperBean.class.isAssignableFrom(a.getClass())).findFirst().orElse(null);

            if (wrapper != null) {
                scriptPath = wrapper.getParamValue("scriptPath");
            }
        }


        if (!validateScriptCallable(scriptPath)) {
            return "";
        }

        //配置参数后访问脚本可初始化路径配置
        if (!LcdpUtils.isDebugRequest()
                && StringUtils.equalsIgnoreCase(Constant.YES, ApplicationContextHelper.getConstantValue("CHECK_ACCESS_PERMISSION_AUTO_INSERT"))) {
            initAccessPermission(scriptPath);
        }

        //检查接口权限
        if (!LcdpUtils.isDebugRequest()
                && ApplicationContextHelper.getEnvironment().getProperty("sunway.api.check-access-permission", boolean.class, false)) {
            checkAccessPermission(scriptPath);
        }

        try {
            return LcdpScriptUtils.callScriptMethod(scriptPath, args);
        } finally {
            HttpServletResponse response = ServletUtils.getCurrentResponse();
            //返回数据时设置header低代码默认的保存数据源
            if (response != null) {
                String path = scriptPath.substring(0, scriptPath.lastIndexOf("."));
                response.setHeader("GIKAM-INSTANT-SAVE-PATH", path + ".updateData");
            }
        }
    }

    @Override
    public String callScheduleScript(String path, Object... args) {
        return LcdpScriptUtils.callScriptMethod(path, args);
    }

    @Override
    public void callDownloadScript(String scriptPath, String env) {
        //校验脚本路径
        if (!validateScriptCallable(scriptPath)) {
            return;
        }
        int lastDotIndex = scriptPath.lastIndexOf(".");

        //从前端传过来的调用路径中截取脚本路径和调用方法名
        String path = scriptPath.substring(0, lastDotIndex);
        String methodName = scriptPath.substring(lastDotIndex + 1);

        // 根据前端传递的env值判断是否开发环境
        if (StringUtils.equals(env, LcdpConstant.REQUEST_HEADER_LCDPENV_DEVELOPMENT)) {
            LcdpScriptUtils.devCallScriptMethod(path, methodName, ArrayUtils.emptyArray(Object.class));
            return;
        }

        //正式环境脚本调用
        LcdpScriptUtils.proCallScriptMethod(path, methodName, ArrayUtils.emptyArray(Object.class));
    }

    @Override
    @Transactional
    public void manualSave(RestJsonWrapperBean wrapper) {
        //重新解析请求json
        String requestJson = wrapper.getRequestJson();
        //已解析的paramMap
        Map<String, String> paramMap = wrapper.getParamMap();
        //已解析的serviceList
        List<GenericService<?, ?>> serviceList = wrapper.getBodyServiceList();
        //已解析的bodyList
        List<List<String>> parsedBodyList = wrapper.getBodyList();

        //解析低代码接口路径
        List<String> pathList = new ArrayList<>();
        JSONObject jo = JSONObject.parseObject(requestJson);
        int index = 0;
        while (true) {
            JSONArray array = jo.getJSONArray("b" + ((index > 0) ? index : ""));

            if (array == null || array.isEmpty()) {
                break;
            } else {
                String path = array.getString(0);
                JSONArray contentArray = array.getJSONArray(1);
                //跳过body为空的path
                if (contentArray != null && !contentArray.isEmpty()) {
                    pathList.add(path);
                }
            }

            index++;
        }

        int size = serviceList.size();

        for (int i = 0; i < size; i++) {

            GenericService<?, ?> service = serviceList.get(i);

            List<String> contentList = parsedBodyList.get(i);

            //body数据
            List<List<String>> bodyList = new ArrayList<>();
            bodyList.add(contentList);
            //方法参数
            RestJsonWrapperBean restJsonWrapperBean = new RestJsonWrapperBean(requestJson, paramMap, bodyList, ArrayUtils.asList(service));

            if (service != null) {
                //service不为空，调用V12原逻辑保存
                instantSave(restJsonWrapperBean);
            } else {
                LcdpScriptUtils.callScriptMethod(pathList.get(i), restJsonWrapperBean);
            }
        }

    }

    @Override
    public LcdpResourceComparisonDTO compare(RestJsonWrapperBean wrapper) {
        LcdpResourceComparisonDTO comparison = new LcdpResourceComparisonDTO();
        Long version = StringUtils.isBlank(wrapper.getParamValue("version")) ? null : NumberUtils.parseLong(wrapper.getParamValue("version"));

        // 表
        if (!StringUtils.isBlank(wrapper.getParamValue("tableName"))) {
            comparison.setType("table");

            String table = wrapper.getParamValue("tableName");

            LcdpTableBean tableFilter = new LcdpTableBean();
            tableFilter.setTableName(table);
            tableFilter.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_YES);
            List<LcdpTableBean> tableList = lcdpTableService.selectList(tableFilter, Order.desc("VERSION"));
            if (!tableList.isEmpty()) {
                // 赋值所有版本信息
                comparison.setVersionList(tableList.stream().map(t -> {
                    LcdpResourceComparisonVersionDTO comparisonVersion = new LcdpResourceComparisonVersionDTO();
                    comparisonVersion.setId(t.getId());
                    comparisonVersion.setVersion(t.getVersion());

                    return comparisonVersion;
                }).collect(Collectors.toList()));
            }

            LcdpTableBean currentTableFilter = new LcdpTableBean();
            currentTableFilter.setTableName(table);
            currentTableFilter.setVersion(version);
            LcdpTableBean currentTable = lcdpTableService.getDao().selectFirstIfPresent(currentTableFilter, Order.desc("VERSION"));
            if (currentTable != null) {
                LcdpResourceComparisonItemDTO currentItem = new LcdpResourceComparisonItemDTO();
                currentItem.setId(currentTable.getId());
                currentItem.setVersion(currentTable.getVersion());
                currentItem.setContent(currentTable.getSql());

                comparison.setCurrentItem(currentItem);
            }

            if (version != null
                    && version > 1) {
                LcdpTableBean preTableFilter = new LcdpTableBean();
                preTableFilter.setTableName(table);
                preTableFilter.setVersion(version - 1);
                preTableFilter.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_YES);
                LcdpTableBean preTable = lcdpTableService.getDao().selectFirstIfPresent(preTableFilter, Order.desc("VERSION"));
                if (preTable != null) {
                    LcdpResourceComparisonItemDTO previousItem = new LcdpResourceComparisonItemDTO();
                    previousItem.setId(preTable.getId());
                    previousItem.setVersion(preTable.getVersion());
                    previousItem.setContent(preTable.getSql());

                    comparison.setPreviousItem(previousItem);
                }
            }

            return comparison;
        }

        // 视图
        if (!StringUtils.isBlank(wrapper.getParamValue("viewName"))) {
            comparison.setType("view");

            String view = wrapper.getParamValue("viewName");

            LcdpViewBean viewFilter = new LcdpViewBean();
            viewFilter.setViewName(view);
            viewFilter.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_YES);
            List<LcdpViewBean> viewList = lcdpViewService.selectList(viewFilter, Order.desc("VERSION"));
            if (!viewList.isEmpty()) {
                // 赋值所有版本信息
                comparison.setVersionList(viewList.stream().map(v -> {
                    LcdpResourceComparisonVersionDTO comparisonVersion = new LcdpResourceComparisonVersionDTO();
                    comparisonVersion.setId(v.getId());
                    comparisonVersion.setVersion(v.getVersion());

                    return comparisonVersion;
                }).collect(Collectors.toList()));
            }

            LcdpViewBean currentViewFilter = new LcdpViewBean();
            currentViewFilter.setViewName(view);
            currentViewFilter.setVersion(version);
            LcdpViewBean currentView = lcdpViewService.getDao().selectFirstIfPresent(currentViewFilter, Order.desc("VERSION"));
            if (currentView != null) {
                LcdpResourceComparisonItemDTO currentItem = new LcdpResourceComparisonItemDTO();
                currentItem.setId(currentView.getId());
                currentItem.setVersion(currentView.getVersion());
                currentItem.setContent(currentView.getSelectStatement());

                comparison.setCurrentItem(currentItem);
            }

            if (version != null
                    && version > 1) {
                LcdpViewBean preViewFilter = new LcdpViewBean();
                preViewFilter.setViewName(view);
                preViewFilter.setVersion(version - 1);
                preViewFilter.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_YES);
                LcdpViewBean preView = lcdpViewService.getDao().selectFirstIfPresent(preViewFilter, Order.desc("VERSION"));
                if (preView != null) {
                    LcdpResourceComparisonItemDTO previousItem = new LcdpResourceComparisonItemDTO();
                    previousItem.setId(preView.getId());
                    previousItem.setVersion(preView.getVersion());
                    previousItem.setContent(preView.getSelectStatement());

                    comparison.setPreviousItem(previousItem);
                }
            }

            return comparison;
        }

        String resourceId = wrapper.getParamValue("resourceId");

        // 全局js脚本和css样式
        if (LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_JS.equals(resourceId)
                || LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_CSS.equals(resourceId)
                || LcdpConstant.SYS_CLIENT_JS_ID.equals(resourceId)
                || LcdpConstant.SYS_CLIENT_CSS_ID.equals(resourceId)) {
            // 类型是全局
            comparison.setType("global");

            LcdpGlobalConfigBean configFilter = new LcdpGlobalConfigBean();
            configFilter.setConfigCode(resourceId);
            List<LcdpGlobalConfigBean> configList = lcdpGlobalConfigService.getDao().selectList(configFilter,
                    Arrays.asList("ID", "VERSION", "EFFECTFLAG"), Order.desc("VERSION"));
            if (!configList.isEmpty()) {
                // 如果最后的配置未提交时，去掉，版本比较时，只比较已生效过的数据
                LcdpGlobalConfigBean latestConfig = configList.get(0);
                if (LcdpConstant.EFFECT_FLAG_NO.equals(latestConfig.getEffectFlag())) {
                    configList.remove(0);
                }

                // 赋值所有版本信息
                comparison.setVersionList(configList.stream().map(c -> {
                    LcdpResourceComparisonVersionDTO comparisonVersion = new LcdpResourceComparisonVersionDTO();
                    comparisonVersion.setId(c.getId());
                    comparisonVersion.setVersion(c.getVersion());

                    return comparisonVersion;
                }).collect(Collectors.toList()));
            }

            LcdpGlobalConfigBean currentConfigFilter = new LcdpGlobalConfigBean();
            currentConfigFilter.setConfigCode(resourceId);
            currentConfigFilter.setVersion(version);
            LcdpGlobalConfigBean currentConfig = lcdpGlobalConfigService.getDao().selectFirstIfPresent(currentConfigFilter, Order.desc("VERSION"));
            if (currentConfig != null) {
                if (configList.stream().anyMatch(c -> c.getId().equals(currentConfig.getId()))) {
                    LcdpResourceComparisonItemDTO currentItem = new LcdpResourceComparisonItemDTO();
                    currentItem.setId(currentConfig.getId());
                    currentItem.setVersion(currentConfig.getVersion());
                    currentItem.setContent(currentConfig.getContent());

                    comparison.setCurrentItem(currentItem);
                }
            }

            if (version != null
                    && version > 1) {
                LcdpGlobalConfigBean preConfigFilter = new LcdpGlobalConfigBean();
                preConfigFilter.setConfigCode(resourceId);
                preConfigFilter.setVersion(version - 1);
                LcdpGlobalConfigBean preConfig = lcdpGlobalConfigService.getDao().selectFirstIfPresent(preConfigFilter, Order.desc("VERSION"));
                if (preConfig != null) {
                    if (configList.stream().anyMatch(c -> c.getId().equals(preConfig.getId()))) {
                        LcdpResourceComparisonItemDTO previousItem = new LcdpResourceComparisonItemDTO();
                        previousItem.setId(preConfig.getId());
                        previousItem.setVersion(preConfig.getVersion());
                        previousItem.setContent(preConfig.getContent());

                        comparison.setPreviousItem(previousItem);
                    }
                }
            }

            return comparison;
        }

        String compareSource = wrapper.getParamValue("source");
        Long id = Long.valueOf(resourceId);

        LcdpResourceHistoryBean historyFilter = new LcdpResourceHistoryBean();
        historyFilter.setResourceId(id);
        List<LcdpResourceHistoryBean> resourceHistoryBriefList = lcdpResourceHistoryService.getDao().selectList(historyFilter,
                Arrays.asList("ID", "VERSION", "SUBMITFLAG"), Order.desc("VERSION"));

        if (resourceHistoryBriefList.isEmpty()) {
            throw new CheckedException("LCDP.MODULE.RESOUCES.TIP.RESOURCE_DELETED");
        }

        LcdpResourceHistoryBean currentResourceHistory = null;
        //查询当前版本的数据
        if (StringUtils.equals("checkout", compareSource)) {
            LcdpResourceHistoryBean resourceHistoryBrief = resourceHistoryBriefList.stream().filter(item -> LcdpConstant.SUBMIT_FLAG_NO.equals(item.getSubmitFlag())).findFirst().orElse(null);
            if (resourceHistoryBrief == null) {
                return comparison;
            }
            currentResourceHistory = lcdpResourceHistoryService.selectById(resourceHistoryBrief.getId());
        } else {
            LcdpResourceHistoryBean resourceHistoryBrief = (version == null
                    ? resourceHistoryBriefList.get(0)
                    : resourceHistoryBriefList.stream().filter(item -> version.equals(item.getVersion())).findFirst().get());

            currentResourceHistory = lcdpResourceHistoryService.selectById(resourceHistoryBrief.getId());

            resourceHistoryBriefList.removeIf(item -> !LcdpConstant.SUBMIT_FLAG_YES.equals(item.getSubmitFlag()));
        }

        if (!resourceHistoryBriefList.isEmpty()) {
            // 赋值所有版本信息
            comparison.setVersionList(resourceHistoryBriefList.stream().map(h -> {
                LcdpResourceComparisonVersionDTO compareVersion = new LcdpResourceComparisonVersionDTO();
                compareVersion.setId(h.getId());
                compareVersion.setVersion(h.getVersion());

                return compareVersion;
            }).collect(Collectors.toList()));
        }

        LcdpResourceComparisonItemDTO currentItem = new LcdpResourceComparisonItemDTO();
        currentItem.setId(currentResourceHistory.getId());
        currentItem.setVersion(currentResourceHistory.getVersion());
        currentItem.setContent(currentResourceHistory.getContent());
        comparison.setCurrentItem(currentItem);

        if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_VIEW, currentResourceHistory.getResourceCategory())) {
            List<LcdpModulePageCompBean> currentModulePageCompList = lcdpModulePageCompService.selectByModulePageHistoryId(currentResourceHistory.getId());

            comparison.setCurrentPageCompList(currentModulePageCompList);
        }

        // 赋值类型
        if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_VIEW, currentResourceHistory.getResourceCategory())) {
            // 类型是页面
            comparison.setType("page");
        } else {
            // 类型是资源
            comparison.setType("resource");
        }

        if (currentResourceHistory.getVersion() == null
                || currentResourceHistory.getVersion() <= 1L) {
            return comparison;
        }

        // 查询上一版本的数据用于对比
        Long previousVersion = currentResourceHistory.getVersion() - 1;
        LcdpResourceHistoryBean previousResourceHistoryBrief = resourceHistoryBriefList.stream().filter(item -> previousVersion.equals(item.getVersion())).findFirst().get();

        LcdpResourceHistoryBean previousResourceHistory = lcdpResourceHistoryService.selectById(previousResourceHistoryBrief.getId());
        LcdpResourceComparisonItemDTO previousItem = new LcdpResourceComparisonItemDTO();
        previousItem.setId(previousResourceHistory.getId());
        previousItem.setVersion(previousResourceHistory.getVersion());
        previousItem.setContent(previousResourceHistory.getContent());
        comparison.setPreviousItem(previousItem);

        if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_VIEW, previousResourceHistory.getResourceCategory())) {
            List<LcdpModulePageCompBean> previousModulePageCompList = lcdpModulePageCompService.selectByModulePageHistoryId(previousResourceHistory.getId());

            comparison.setPreviousPageCompList(previousModulePageCompList);
        }
        return comparison;
    }

    @Override
    public String export(RestJsonWrapperBean wrapper, LcdpCheckoutRecordBean checkoutRecord) {
        LcdpResourceCheckoutConfigDTO checkoutConfigDTO = new LcdpResourceCheckoutConfigDTO();
        checkoutConfigDTO.setExportSysClientJsFlag(wrapper.getParamValue("exportSysClientJsFlag"));
        checkoutConfigDTO.setExportSysClientCssFlag(wrapper.getParamValue("exportSysClientCssFlag"));
        if (LcdpScriptUtils.validateCurrentDBMybatisMapper()) {
            checkoutConfigDTO.setExportEnvironmentDBType(current_environment_database);
        }

        List<LcdpResourceBean> exportResourceList = new ArrayList<>();//导出资源数据

        List<LcdpTableDTO> tableDTOList = new ArrayList<>();//导出表数据

        List<LcdpResourceVersionBean> resourceVersionList = wrapper.parse(LcdpResourceVersionBean.class);
        List<String> resourceIdStrList = new ArrayList<>();
        List<String> tableNameList = new ArrayList<>();
        List<String> viewNameList = new ArrayList<>();
        resourceVersionList.forEach(version -> {
            if (LcdpConstant.RESOURCE_CATEGORY_TABLE.equals(version.getResourceCategory())) {
                tableNameList.add(version.getResourceId());
            } else if (LcdpConstant.RESOURCE_CATEGORY_DB_VIEW.equals(version.getResourceCategory())) {
                viewNameList.add(version.getResourceId());
            } else {
                resourceIdStrList.add(version.getResourceId());
            }
        });
        List<Long> resourceIdList = resourceIdStrList.stream().map(Long::valueOf).collect(Collectors.toList());

        MapperParameter parameter = new MapperParameter();
        parameter.setFilter(SearchFilter.instance().match("TABLENAME", tableNameList).filter(MatchPattern.OCISEQ));

        List<LcdpTableBean> tableList = lcdpDatabaseDao.selectTableInfoByCondition(parameter);
        List<LcdpViewBean> viewList = lcdpDatabaseService.selectPhysicalViewInfoList(viewNameList);

        List<LcdpResourceTreeNodeDTO> treeNodeDtoList = bulidResourceTreeNodeDTOS(resourceIdList, tableList, viewList);

        if (!resourceIdList.isEmpty()) {
            //得到导出的资源
            exportResourceList = getExportedResources(resourceIdList);

        }
        if (!tableNameList.isEmpty()) {
            tableDTOList = lcdpTableService.selectPhysicalTableInfoList(tableNameList);
        }

        List<LcdpScriptBlockBean> scriptBlockList = wrapper.parse(LcdpScriptBlockBean.class);
        List<LcdpResourceFileBean> resourceFileList = wrapper.parse(LcdpResourceFileBean.class);

        if (resourceIdList.isEmpty()
                && tableNameList.isEmpty()
                && viewNameList.isEmpty()
                && scriptBlockList.isEmpty()
                && resourceFileList.isEmpty()
                && !Constant.YES.equals(checkoutConfigDTO.getExportSysClientJsFlag())
                && !Constant.YES.equals(checkoutConfigDTO.getExportSysClientCssFlag())) {
            return null;
        }

        List<String> lcdpResourceFilePathList = resourceFileList.stream().filter(item -> !StringUtils.isEmpty(item.getRelativePath())).map(LcdpResourceFileBean::getRelativePath).collect(Collectors.toList());
        List<LcdpResourceFileBean> lcdpResourceFileList = resourceFileService.selectListByFilter(SearchFilter.instance().match("relativePath", lcdpResourceFilePathList).filter(MatchPattern.OR));
        if (!lcdpResourceFileList.isEmpty()) {
            //记录导出信息
            LcdpResourceTreeNodeDTO fileRoot = new LcdpResourceTreeNodeDTO();
            fileRoot.setName("文件");
            fileRoot.setId(ApplicationContextHelper.getNextIdentity().toString());
            fileRoot.setType(LcdpConstant.RESOURCE_CATEGORY_FILE);
            treeNodeDtoList.add(fileRoot);
            lcdpResourceFileList.forEach(file -> {
                LcdpResourceTreeNodeDTO treeNodeDTO = new LcdpResourceTreeNodeDTO();
                treeNodeDTO.setParentId(fileRoot.getId());
                treeNodeDTO.setName(file.getFileName());
                fileRoot.addChild(treeNodeDTO);
            });
            TreeHelper.updateChildQty(treeNodeDtoList);
        }

        LcdpResourceExportDTO exportDTO = new LcdpResourceExportDTO();
        exportDTO.setExportLog(checkoutRecord.getCheckoutNote());
        exportDTO.setTreeNodeDtoList(treeNodeDtoList);
        exportDTO.setExportResourceList(exportResourceList);
        exportDTO.setTableDTOList(tableDTOList);
        exportDTO.setViewList(viewList);
        exportDTO.setLcdpResourceFileList(lcdpResourceFileList);

        scriptBlockList = scriptBlockList.stream().filter(e -> e.getId() != null).collect(Collectors.toList());
        // 代码块导出并记录导出信息
        if (!scriptBlockList.isEmpty()) {
            scriptBlockList = scriptBlockService.selectListByIds(scriptBlockList.stream().map(e -> e.getId()).collect(Collectors.toList()));

            TreeDescriptor<LcdpScriptBlockBean> descriptor = new TreeDescriptor<>("id", "parentId", "name", "orderNo");
            descriptor.setParseTreeNodeFunction(t -> {
                LcdpResourceTreeNodeDTO resourceDTO = new LcdpResourceTreeNodeDTO();
                BeanUtils.copyProperties(t, resourceDTO);
                return resourceDTO;
            });
            List<LcdpResourceTreeNodeDTO> scriptBlockTreeNodeList = TreeHelper.parseTreeNode(scriptBlockList, descriptor, LcdpResourceTreeNodeDTO.class);

            //构建根节点
            LcdpResourceTreeNodeDTO blockRoot = new LcdpResourceTreeNodeDTO();
            blockRoot.setName("代码块");
            blockRoot.setId(ApplicationContextHelper.getNextIdentity().toString());
            blockRoot.setType(LcdpConstant.RESOURCE_CATEGORY_CODEBLOCK);
            treeNodeDtoList.add(blockRoot);

            scriptBlockTreeNodeList.forEach(node -> {
                node.setParentId(blockRoot.getId());
                node.setId(blockRoot.getId());
                blockRoot.addChild(node);
            });
            TreeHelper.updateChildQty(treeNodeDtoList);
        }


        exportDTO.setLcdpScriptBlockList(scriptBlockList);

        if (StringUtils.equals(Constant.YES, checkoutConfigDTO.getExportSysClientCssFlag())) {
            LcdpResourceTreeNodeDTO systemCss = new LcdpResourceTreeNodeDTO();
            systemCss.setName(LcdpConstant.SYS_CLIENT_CSS);
            systemCss.setDesc(LcdpConstant.SYS_CLIENT_CSS);
            systemCss.setId(LcdpConstant.SYS_CLIENT_CSS_ID);
            systemCss.setType(LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_CSS);
            treeNodeDtoList.add(0, systemCss);
        }

        if (StringUtils.equals(Constant.YES, checkoutConfigDTO.getExportSysClientJsFlag())) {
            LcdpResourceTreeNodeDTO clientJS = new LcdpResourceTreeNodeDTO();
            clientJS.setName(LcdpConstant.SYS_CLIENT_JS);
            clientJS.setDesc(LcdpConstant.SYS_CLIENT_JS);
            clientJS.setId(LcdpConstant.SYS_CLIENT_JS_ID);
            clientJS.setType(LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_JS);
            treeNodeDtoList.add(0, clientJS);
        }

        checkoutRecord.setContent(JSONObject.toJSONString(treeNodeDtoList));

        return createExportFile(exportDTO, checkoutConfigDTO, checkoutRecord);

    }

    @Override
    public LcdpExportLogFileDTO importAnalyse(RestJsonWrapperBean wrapper) {
        Map<String, String> paramMap = wrapper.getParamMap();
        CoreFileBean coreFileBean = null;
        if (paramMap.get("fileId") != null) {
            Long fileId = Long.valueOf(paramMap.get("fileId"));
            coreFileBean = coreFileService.selectById(fileId);
        }

        //获取文件ID 查询文件

        if (paramMap.get("checkinId") != null) {
            coreFileBean = coreFileService.selectFileListByTargetIds("T_LCDP_CHECKIN_RECORD$" + paramMap.get("checkinId")).stream().findFirst().get();
        }

        Path filePath = CoreFileUtils.getLocalPath(coreFileBean);

        //拿到这次需要分析的资源
        List<LcdpResourceVersionBean> resourceVersionList = wrapper.parse(LcdpResourceVersionBean.class);
        List<String> resourceIdStrList = new ArrayList<>();
        List<String> tableNameList = new ArrayList<>();
        List<String> viewNameList = new ArrayList<>();
        resourceVersionList.forEach(version -> {
            if (LcdpConstant.RESOURCE_CATEGORY_TABLE.equals(version.getResourceCategory())) {
                tableNameList.add(version.getResourceId());
            } else if (LcdpConstant.RESOURCE_CATEGORY_DB_VIEW.equals(version.getResourceCategory())) {
                viewNameList.add(version.getResourceId());
            } else if (LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_JS.equals(version.getResourceCategory())) {
                resourceIdStrList.add("sysClientJs");
            } else if (LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_CSS.equals(version.getResourceCategory())) {
                resourceIdStrList.add("sysClientCss");
            } else {
                resourceIdStrList.add(version.getResourceId());
            }
        });

        //拿到文件对应文件数据集合
        Map<String, String> fileMap = unZip(filePath);
        Map<String, LcdpAnalysisResultDTO> tableResult = new HashMap<>();
        Map<String, LcdpAnalysisResultDTO> viewResult = new HashMap<>();
        if (!tableNameList.isEmpty()) {
            tableResult = lcdpTableService.analysisTableInfo(tableNameList, fileMap);
        }
        if (!viewNameList.isEmpty()) {
            viewResult = lcdpViewService.analysisViewInfo(viewNameList, fileMap);
        }

        Map<String, Object> importDataMap = analysisFileContent(resourceIdStrList, tableNameList, viewNameList, fileMap);

        @SuppressWarnings("unchecked") List<LcdpResourceBean> resourceList = (List<LcdpResourceBean>) importDataMap.get("resource");

        for (int i = 0; i < resourceList.size(); i++) {
            LcdpResourceBean resource = resourceList.get(i);
            //在模块下构造三个文件夹放不同的文件
            if (LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(resource.getResourceCategory())) {
                bulidFolder(resourceList, resource);
            }
        }


        LcdpExportLogFileDTO exportLogFileDTO = new LcdpExportLogFileDTO();

        @SuppressWarnings("unchecked")
        List<LcdpResourceTreeNodeDTO> resourceDTOList = bulidAnalysisTreeNodeDTOS(tableResult,
                viewResult,
                resourceList,
                (List<LcdpTableDTO>) importDataMap.get("table"),
                (List<LcdpViewBean>) importDataMap.get("db-view"));

        exportLogFileDTO.setTreeNodeDTOList(resourceDTOList);
        //获取提交日志
        String exportLog = fileMap.get("exportLog");
        JSONObject exportLogJson = JSON.parseObject(exportLog);

        String checkoutConfig = fileMap.get("checkoutConfig");
        if (!StringUtils.isEmpty(checkoutConfig)) {
            LcdpResourceCheckoutConfigDTO checkoutConfigDTO = JsonUtils.parse(checkoutConfig, LcdpResourceCheckoutConfigDTO.class);
            exportLogFileDTO.setResourceCheckoutConfigDTO(checkoutConfigDTO);
        }


        String log = exportLogJson.getString("log");

        exportLogFileDTO.setLog(log);
        exportLogFileDTO.setCheckoutRecordNo(exportLogJson.getString("checkoutRecordNo"));

        //将新生成的数据生成树
        String tree = JSONObject.toJSONString(resourceDTOList);
        JSONArray analysisTreeArray = JSON.parseArray(tree);
        //添加提交日志
        analysisTreeArray.add(log);

        //添加文件ID
        exportLogFileDTO.setFileId(coreFileBean.getId());
        Map<String, Long> fileIdMap = new HashMap<>();
        fileIdMap.put("fileId", coreFileBean.getId());
        analysisTreeArray.add(fileIdMap);
        if (!ObjectUtils.isEmpty(fileMap.get("lcdpFile"))) {
            List<LcdpResourceFileBean> fileList = JSON.parseArray(fileMap.get("lcdpFile")).toJavaList(LcdpResourceFileBean.class);
            exportLogFileDTO.setResourceFileList(fileList);
        }

        if (!ObjectUtils.isEmpty(fileMap.get("scriptBlock"))) {
            List<LcdpScriptBlockBean> scriptBlockList = JSON.parseArray(fileMap.get("scriptBlock")).toJavaList(LcdpScriptBlockBean.class);
            List<LcdpScriptBlockTreeNodeDTO> scriptBlockTreeNodeDTOList = scriptBlockService.buildTreeNodeDTOList(scriptBlockList);

            exportLogFileDTO.setScriptBlockTreeNodeDTOList(scriptBlockTreeNodeDTOList);
        }


        //添加分析结果是否可以正常导入
        boolean allowImportFlag = true;
        Map<String, Boolean> allowImportMap = new HashMap<>();
        allowImportMap.put("allowImport", true);
        if (!tableResult.isEmpty()) {
            Set<String> tableNameSet = tableResult.keySet();
            for (String tableName : tableNameSet) {
                Boolean allowImport = tableResult.get(tableName).getEnable();
                if (!allowImport) {
                    allowImportFlag = false;
                    allowImportMap.put("allowImport", allowImport);
                    break;
                }
            }
        }
        analysisTreeArray.add(allowImportMap);
        exportLogFileDTO.setAllowImport(allowImportFlag);
        return exportLogFileDTO;
    }


    @Override
    public LcdpResourceComparisonDTO importableCompare(RestJsonWrapperBean wrapper) {
        LcdpResourceComparisonDTO comparison = new LcdpResourceComparisonDTO();

        Long fileId = NumberUtils.parseLong(wrapper.getParamValue("fileId"));
        CoreFileBean importFile = coreFileService.selectById(fileId);
        Path importFilePath = CoreFileUtils.getLocalPath(importFile);

        Map<String, String> dataMap = unZip(importFilePath);

        String resourceId = wrapper.getParamValue("resourceId"); // 要比较资源的ID（导入文件中的ID，而非系统中的ID）
        String resourceCategory = wrapper.getParamValue("resourceCategory"); // 要比较资源的类型

        if (LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_JS.equals(resourceCategory)) { // 系统JS
            String json = dataMap.get("sysClientJs");
            LcdpGlobalConfigBean jsConfig = JsonUtils.parse(json, LcdpGlobalConfigBean.class);

            LcdpResourceComparisonItemDTO currentItem = new LcdpResourceComparisonItemDTO();
            currentItem.setContent(jsConfig.getContent());
            comparison.setCurrentItem(currentItem);

            LcdpGlobalConfigBean previousJsConfig = lcdpGlobalConfigService.selectConfigContent("SYS_CLIENT_JS");
            LcdpResourceComparisonItemDTO previousItem = new LcdpResourceComparisonItemDTO();
            previousItem.setId(previousJsConfig.getId());
            previousItem.setVersion(previousJsConfig.getVersion());
            previousItem.setContent(previousJsConfig.getContent());
            comparison.setPreviousItem(previousItem);
        } else if (LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_CSS.equals(resourceCategory)) { // 系统CSS
            String json = dataMap.get("sysClientCss");
            LcdpGlobalConfigBean cssConfig = JsonUtils.parse(json, LcdpGlobalConfigBean.class);

            LcdpResourceComparisonItemDTO currentItem = new LcdpResourceComparisonItemDTO();
            currentItem.setContent(cssConfig.getContent());
            comparison.setCurrentItem(currentItem);

            LcdpGlobalConfigBean activeCssConfig = lcdpGlobalConfigService.selectConfigContent("SYS_CLIENT_CSS");
            LcdpResourceComparisonItemDTO previousItem = new LcdpResourceComparisonItemDTO();
            previousItem.setId(activeCssConfig.getId());
            previousItem.setVersion(activeCssConfig.getVersion());
            previousItem.setContent(activeCssConfig.getContent());
            comparison.setPreviousItem(previousItem);
        } else if (LcdpConstant.RESOURCE_CATEGORY_TABLE.equals(resourceCategory)) { // 库表
            String json = dataMap.get(resourceId);
            LcdpTableDTO tableDTO = JsonUtils.parse(json, LcdpTableDTO.class);
            String sql = lcdpTableService.generateCreateSql(tableDTO);

            LcdpResourceComparisonItemDTO currentItem = new LcdpResourceComparisonItemDTO();
            currentItem.setContent(sql);
            comparison.setCurrentItem(currentItem);

            LcdpTableBean activeTable = lcdpTableService.selectFirstByFilter(SearchFilter.instance()
                            .match("TABLENAME", tableDTO.getTableName()).filter(MatchPattern.SEQ)
                            .match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_YES).filter(MatchPattern.SEQ),
                    Order.desc("VERSION"));
            if (activeTable != null) {
                LcdpResourceComparisonItemDTO previousItem = new LcdpResourceComparisonItemDTO();
                previousItem.setId(activeTable.getId());
                previousItem.setVersion(activeTable.getVersion());
                previousItem.setContent(activeTable.getSql());
                comparison.setPreviousItem(previousItem);
            }
        } else if (LcdpConstant.RESOURCE_CATEGORY_DB_VIEW.equals(resourceCategory)) { // 视图
            String json = dataMap.get(resourceId);
            LcdpViewDTO viewDTO = JsonUtils.parse(json, LcdpViewDTO.class);

            LcdpResourceComparisonItemDTO currentItem = new LcdpResourceComparisonItemDTO();
            currentItem.setContent(viewDTO.getSelectStatement());
            comparison.setCurrentItem(currentItem);

            LcdpViewBean activeView = lcdpViewService.selectFirstByFilter(SearchFilter.instance()
                            .match("VIEWNAME", viewDTO.getViewName()).filter(MatchPattern.SEQ)
                            .match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_YES).filter(MatchPattern.SEQ),
                    Order.desc("VERSION"));
            if (activeView != null) {
                LcdpResourceComparisonItemDTO previousItem = new LcdpResourceComparisonItemDTO();
                previousItem.setId(activeView.getId());
                previousItem.setVersion(activeView.getVersion());
                previousItem.setContent(activeView.getSelectStatement());
                comparison.setPreviousItem(previousItem);
            }
        } else { // 资源
            String resourceJson = dataMap.get(resourceId);
            LcdpResourceBean resource = JsonUtils.parse(resourceJson, LcdpResourceBean.class);

            LcdpResourceComparisonItemDTO currentItem = new LcdpResourceComparisonItemDTO();
            currentItem.setContent(resource.getContent());
            comparison.setCurrentItem(currentItem);

            if (LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(resourceCategory)) { // 页面
                String pageCompJson = dataMap.get(resourceId + "view");
                if (!StringUtils.isEmpty(pageCompJson)) {
                    List<LcdpModulePageCompBean> pageCompList = JSON.parseArray(pageCompJson).toJavaList(LcdpModulePageCompBean.class);
                    comparison.setCurrentPageCompList(pageCompList);
                }
            }

            LcdpResourceHistoryBean filter = new LcdpResourceHistoryBean();
            filter.setPath(resource.getPath());
            filter.setDeleteFlag(LcdpConstant.RESOURCE_DELETED_NO);
            filter.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
            LcdpResourceHistoryBean activeHistory = lcdpResourceHistoryService.getDao().selectFirstIfPresent(filter,
                    Arrays.asList("ID", "VERSION", "CONTENT"),
                    Order.desc("VERSION"));
            if (activeHistory != null) {
                LcdpResourceComparisonItemDTO previousItem = new LcdpResourceComparisonItemDTO();
                previousItem.setId(activeHistory.getId());
                previousItem.setVersion(activeHistory.getVersion());
                previousItem.setContent(activeHistory.getContent());
                comparison.setPreviousItem(previousItem);

                if (LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(resourceCategory)) { // 页面
                    List<LcdpModulePageCompBean> pageCompList = lcdpModulePageCompService.selectByModulePageHistoryId(activeHistory.getId());
                    comparison.setPreviousPageCompList(pageCompList);
                }
            }
        }

        return comparison;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.SAVE)
    public void dealPageComps(RestJsonWrapperBean wrapper) {
        List<LcdpResourceBean> resourceList = selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_VIEW).filter(MatchPattern.EQ).match("EFFECTVERSION", null).filter(MatchPattern.DIFFER).match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ));
        List<Long> resourceIdList = resourceList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
        List<LcdpResourceHistoryBean> resourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", resourceIdList).filter(MatchPattern.OR).match("EFFECTFLAG", Constant.YES).filter(MatchPattern.EQ));
        List<Long> resourceHistoryIdList = resourceHistoryList.stream().map(LcdpResourceHistoryBean::getId).collect(Collectors.toList());

        List<LcdpModulePageCompBean> pageCompList = lcdpModulePageCompService.selectListByFilter(SearchFilter.instance().match("modulePageHistoryId", resourceHistoryIdList).filter(MatchPattern.OR));

        List<String> dealTypeList = Arrays.asList("Grid", "Uploader", "WindowToolbar", "GridToolbar");
        List<LcdpModulePageCompBean> dealPageCompList = pageCompList.stream().filter(comp -> dealTypeList.contains(comp.getType())).collect(Collectors.toList());
        dealPageCompList.forEach(comp -> {
            JSONObject configObject = JSON.parseObject(comp.getConfig());
            switch (comp.getType()) {
                case "Grid":
                    configObject.remove("filter");
                    configObject.remove("toolbarAlign");
                    configObject.remove("editorInvisible");
                    configObject.remove("columnsFill");
                    configObject.remove("contentAlign");
                case "Uploader":
                    configObject.remove("toolbarAlign");
                    break;
                case "WindowToolbar":
                    configObject.remove("toolbarAlign");
                    break;
                case "GridToolbar":
                    configObject.remove("toolbarAlign");
                    break;
            }

            comp.setConfig(configObject.toJSONString());
        });
        lcdpModulePageCompService.getDao().update(dealPageCompList, "CONFIG");
    }

    @Override
    @Transactional
    @Audit(AuditConstant.SAVE)
    public void dealResourceHistories(RestJsonWrapperBean wrapper) {
        String userId = wrapper.getParamValue("userId");
        if (StringUtils.isNotBlank(userId)) {
            LoginUser loginUser = buildLoginUser(userId);
            try {
                LocalContextHelper.setUserLogin(loginUser);
                doDealResourceHistories(wrapper);
            } finally {
                LocalContextHelper.removeUserLogin();
            }
            return;
        }

        doDealResourceHistories(wrapper);
    }

    @Override
    @Transactional
    public void clearResourceCache(RestJsonWrapperBean wrapper) {
        List<Long> resourceHistoryIdList = wrapper.parseId(Long.class);
        if (resourceHistoryIdList.isEmpty()) {
            return;
        }

        Map<Long, LcdpResourceHistoryBean> historyId2HistoryMap = lcdpResourceHistoryService.selectListByIds(resourceHistoryIdList).stream()
                .collect(Collectors.toMap(LcdpResourceHistoryBean::getId, Function.identity(), (left, right) -> left));
        if (historyId2HistoryMap.isEmpty()) {
            return;
        }

        List<Long> resourceIdList = historyId2HistoryMap.values().stream()
                .map(LcdpResourceHistoryBean::getResourceId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (resourceIdList.isEmpty()) {
            return;
        }

        Map<Long, LcdpResourceBean> resourceId2ResourceMap = selectListByIds(resourceIdList).stream()
                .collect(Collectors.toMap(LcdpResourceBean::getId, Function.identity(), (left, right) -> left));

        for (Long resourceId : resourceIdList) {
            LcdpResourceBean resource = resourceId2ResourceMap.get(resourceId);
            if (resource == null) {
                continue;
            }

            clearResourceCache(resource);
        }
    }

    private void doDealResourceHistories(RestJsonWrapperBean wrapper) {
        List<Long> resourceHistoryIdList = wrapper.parseId(Long.class);
        if (resourceHistoryIdList.isEmpty()) {
            return;
        }

        Map<Long, LcdpResourceHistoryBean> historyId2HistoryMap = lcdpResourceHistoryService.selectListByIds(resourceHistoryIdList).stream()
                .collect(Collectors.toMap(LcdpResourceHistoryBean::getId, Function.identity(), (left, right) -> left));
        if (historyId2HistoryMap.isEmpty()) {
            return;
        }

        List<Long> resourceIdList = historyId2HistoryMap.values().stream()
                .map(LcdpResourceHistoryBean::getResourceId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, LcdpResourceBean> resourceId2ResourceMap = selectListByIds(resourceIdList).stream()
                .collect(Collectors.toMap(LcdpResourceBean::getId, Function.identity(), (left, right) -> left));

        for (Long resourceHistoryId : resourceHistoryIdList) {
            LcdpResourceHistoryBean resourceHistory = historyId2HistoryMap.get(resourceHistoryId);
            if (resourceHistory == null || resourceHistory.getResourceId() == null) {
                continue;
            }

            LcdpResourceBean resource = resourceId2ResourceMap.get(resourceHistory.getResourceId());
            if (resource == null) {
                continue;
            }

            switch (resourceHistory.getResourceCategory()) {
                case LcdpConstant.RESOURCE_CATEGORY_VIEW:
                    clearPageCaches(resource, resourceHistory);
                    break;
                case LcdpConstant.RESOURCE_CATEGORY_JAVA:
                    dealJavaHistory(resource, resourceHistory);
                    break;
                case LcdpConstant.RESOURCE_CATEGORY_MAPPER:
                    dealMapperHistory(resourceHistory);
                    break;
                default:
                    break;
            }
        }
    }

    private void clearResourceCache(LcdpResourceBean resource) {
        clearResourceBaseCaches(resource);

        switch (resource.getResourceCategory()) {
            case LcdpConstant.RESOURCE_CATEGORY_VIEW:
                evictCache("T_LCDP_MODULE_PAGE_COMP.BY_MODULEPAGEID", String.valueOf(resource.getId()));
                break;
            case LcdpConstant.RESOURCE_CATEGORY_JAVA:
                refreshJavaResourceCache(resource);
                break;
            case LcdpConstant.RESOURCE_CATEGORY_MAPPER:
                refreshMapperResourceCache(resource);
                break;
            default:
                break;
        }
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


    @Override
    public LcdpExportLogFileDTO importByFile(Long fileId, RestJsonWrapperBean wrapper) {
        LcdpExportLogFileDTO exportLogFileDTO = new LcdpExportLogFileDTO();
        CoreFileBean coreFileBean = coreFileService.selectById(fileId);
        Path filePath = CoreFileUtils.getLocalPath(coreFileBean);
        Map<String, Long> fileIdMap = new HashMap<>();
        fileIdMap.put("fileId", fileId);
        Map<String, String> fileMap = unZip(filePath);

        String exportLog = fileMap.get("exportLog");
        String checkoutConfig = fileMap.get("checkoutConfig");
        if (!StringUtils.isEmpty(checkoutConfig)) {
            LcdpResourceCheckoutConfigDTO checkoutConfigDTO = JsonUtils.parse(checkoutConfig, LcdpResourceCheckoutConfigDTO.class);
            exportLogFileDTO.setResourceCheckoutConfigDTO(checkoutConfigDTO);
        }
        JSONObject exportLogJson = JSON.parseObject(exportLog);
        @SuppressWarnings("unchecked")
        List<LcdpResourceTreeNodeDTO> treeNodeDTOList = (List<LcdpResourceTreeNodeDTO>) exportLogJson.get("treeNodeDTOList");

        String log = exportLogJson.getString("log");
        if (!ObjectUtils.isEmpty(fileMap.get("lcdpFile"))) {
            List<LcdpResourceFileBean> fileList = JSON.parseArray(fileMap.get("lcdpFile")).toJavaList(LcdpResourceFileBean.class);
            exportLogFileDTO.setResourceFileList(fileList);
        }
        if (!ObjectUtils.isEmpty(fileMap.get("scriptBlock"))) {
            List<LcdpScriptBlockBean> scriptBlockList = JSON.parseArray(fileMap.get("scriptBlock")).toJavaList(LcdpScriptBlockBean.class);

            List<LcdpScriptBlockTreeNodeDTO> scriptBlockTreeNodeDTOList = scriptBlockService.buildTreeNodeDTOList(scriptBlockList);

            exportLogFileDTO.setScriptBlockTreeNodeDTOList(scriptBlockTreeNodeDTOList);
        }
        exportLogFileDTO.setFileId(fileId);
        exportLogFileDTO.setLog(log);
        exportLogFileDTO.setTreeNodeDTOList(treeNodeDTOList);
        exportLogFileDTO.setCheckoutRecordNo(exportLogJson.getString("checkoutRecordNo"));
        return exportLogFileDTO;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importData(RestJsonWrapperBean wrapper) {
        String checkinIdStr = wrapper.getParamValue("checkinId");

        LcdpResourceCheckoutConfigDTO checkoutConfigDTO = new LcdpResourceCheckoutConfigDTO();
        String exportSysClientJsFlag = wrapper.getParamValue("exportSysClientJsFlag");
        String exportSysClientCssFlag = wrapper.getParamValue("exportSysClientCssFlag");
        checkoutConfigDTO.setExportSysClientJsFlag(exportSysClientJsFlag);
        checkoutConfigDTO.setExportSysClientCssFlag(exportSysClientCssFlag);

        CoreFileBean coreFileBean = coreFileService.selectFileListByTargetIds("T_LCDP_CHECKIN_RECORD$" + checkinIdStr).stream().findFirst().get();
        Path filePath = CoreFileUtils.getLocalPath(coreFileBean);
        //获取文件ID 查询文件
        Map<String, String> paramMap = wrapper.getParamMap();

        String importLog = paramMap.get("importLog");

        StringBuilder importJavaRecord = new StringBuilder();

        LcdpSubmitLogBean autoSubmitLog = new LcdpSubmitLogBean();
        autoSubmitLog.setId(ApplicationContextHelper.getNextIdentity());
        autoSubmitLog.setCommit(I18nHelper.getMessage("T_LCDP_RESOURCE.FILE.IMPORT.AUTO.SUBMIT") + ":" + importLog);

        LcdpSubmitLogBean submitLog = new LcdpSubmitLogBean();
        submitLog.setId(ApplicationContextHelper.getNextIdentity());
        submitLog.setCommit(importLog);
        submitLogService.getDao().insert(submitLog);


        //拿到这次需要分析的资源
        List<LcdpResourceVersionBean> resourceVersionList = wrapper.parse(LcdpResourceVersionBean.class);
        List<String> resourceIdStrList = new ArrayList<>();
        List<String> tableNameList = new ArrayList<>();
        List<String> viewNameList = new ArrayList<>();
        resourceVersionList.forEach(version -> {
            if (LcdpConstant.RESOURCE_CATEGORY_TABLE.equals(version.getResourceCategory())) {
                tableNameList.add(version.getResourceId());
            } else if (LcdpConstant.RESOURCE_CATEGORY_DB_VIEW.equals(version.getResourceCategory())) {
                viewNameList.add(version.getResourceId());
            } else if (LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_JS.equals(version.getResourceCategory())) {
                resourceIdStrList.add("sysClientJs");
            } else if (LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_CSS.equals(version.getResourceCategory())) {
                resourceIdStrList.add("sysClientCss");
            } else {
                resourceIdStrList.add(version.getResourceId());
            }
        });
        //拿到文件对应文件数据集合
        Map<String, String> fileMap = unZip(filePath);

        //通过对文件分析得到要导入的数据
        Map<String, Object> importDataMap = analysisFileContent(resourceIdStrList, tableNameList, viewNameList, fileMap);

        List<LcdpResourceBean> resourceList = (List<LcdpResourceBean>) importDataMap.get("resource");
        List<LcdpTableDTO> tableDTOList = (List<LcdpTableDTO>) importDataMap.get("table");
        List<LcdpViewBean> importViewList = (List<LcdpViewBean>) importDataMap.get("db-view");
        List<LcdpModulePageCompBean> pageCompList = (List<LcdpModulePageCompBean>) importDataMap.get("pageComps");
        List<LcdpModulePageI18nBean> pageI18nList = (List<LcdpModulePageI18nBean>) importDataMap.get("pageI18n");
        List<LcdpPageI18nCodeBean> pageDependentI18nList = (List<LcdpPageI18nCodeBean>) importDataMap.get("pageDependentI18n");

        List<CoreCodeCategoryBean> coreCodeCategoryList = (List<CoreCodeCategoryBean>) importDataMap.get("codeCategory");//系统编码分类集合
        List<CoreCodeBean> codeList = (List<CoreCodeBean>) importDataMap.get("code");//系统编码分类集合
        List<CoreAdminSelectConfigBean> coreAdminSelectConfigList = (List<CoreAdminSelectConfigBean>) importDataMap.get("adminSelectConfig");//系统编码分类集合
        List<CoreI18nMessageBean> coreI18nMessageList = (List<CoreI18nMessageBean>) importDataMap.get("i18n");//系统国际化编码集合
        List<LcdpResourceFileBean> lcdpResourceFileList = (List<LcdpResourceFileBean>) importDataMap.get("lcdpFile");

        Map<Long, List<LcdpModulePageCompBean>> resourceId2PageCompMap = pageCompList.stream().collect((Collectors.groupingBy(LcdpModulePageCompBean::getModulePageId)));
        Map<Long, List<LcdpModulePageI18nBean>> resourceId2PageI18nMap = pageI18nList.stream().collect((Collectors.groupingBy(LcdpModulePageI18nBean::getModulePageId)));
        Map<Long, List<LcdpPageI18nCodeBean>> resourceId2PageDependentI18nMap = pageDependentI18nList.stream().collect((Collectors.groupingBy(LcdpPageI18nCodeBean::getModulePageId)));
        //资源部分导入 脚本 页面等数据
        List<LcdpResourceBean> importResourceDataList = new ArrayList<>();


        //
        LcdpCheckImportDataDTO checkImportDataDTO = new LcdpCheckImportDataDTO();
        if (!resourceList.isEmpty()) {
            ListChunkIterator<LcdpResourceBean> chunkIterator = ListChunkIterator.of(resourceList, 400);
            while (chunkIterator.hasNext()) {
                importResourceDataList.addAll(importResourceData(chunkIterator.nextChunk(), resourceId2PageCompMap, resourceId2PageI18nMap, resourceId2PageDependentI18nMap, submitLog, autoSubmitLog, importJavaRecord));
            }
            checkImportDataDTO.setResourceList(importResourceDataList);
        }

        List<LcdpTableBean> tableList = new ArrayList<>();
        List<LcdpViewBean> viewList = new ArrayList<>();
        if (!tableDTOList.isEmpty()) {
            tableList = importTableData(tableNameList, fileMap, submitLog, autoSubmitLog);
            checkImportDataDTO.setTableList(tableList);
        }

        if (!importViewList.isEmpty()) {
            viewList = importViewData(viewNameList, fileMap, submitLog, autoSubmitLog);
            checkImportDataDTO.setViewList(viewList);
        }

        LcdpExportLogFileDTO importContentLogDto = new LcdpExportLogFileDTO();
        importContentLogDto.setResourceCheckoutConfigDTO(checkoutConfigDTO);

        List<Long> idList = importResourceDataList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
        List<LcdpResourceTreeNodeDTO> treeNodeDTOList = bulidResourceTreeNodeDTOS(idList, tableList, viewList);
        importContentLogDto.setTreeNodeDTOList(treeNodeDTOList);

        LcdpResourceImportRecordBean record = new LcdpResourceImportRecordBean();

        //导入系统编码
        importSysCode(coreCodeCategoryList, codeList, coreAdminSelectConfigList);
        //导入系统国际化
        if (!ObjectUtils.isEmpty(coreI18nMessageList)) {
            importSysI18n(coreI18nMessageList);
        }
        // 导入系统JS脚本
        if (StringUtils.equals(exportSysClientJsFlag, Constant.YES)) {
            LcdpGlobalConfigBean sysClentJs = importSysClientJs(fileMap);
            if (sysClentJs != null) {
                record.setSysClientJsVersion(sysClentJs.getVersion());
                checkImportDataDTO.setSysClientJsVersion(sysClentJs.getVersion() - 1);
                checkImportDataDTO.setJsOperation(LcdpConstant.OPERATION_OF_ROLLBACK_CHECKIN_SUBMIT);
            }
        }
        // 导入系统CSS样式
        if (StringUtils.equals(exportSysClientCssFlag, Constant.YES)) {
            LcdpGlobalConfigBean sysClentCss = importSysClientCss(fileMap);
            if (sysClentCss != null) {
                record.setSysClientCssVersion(sysClentCss.getVersion());
                checkImportDataDTO.setCssOperation(LcdpConstant.OPERATION_OF_ROLLBACK_CHECKIN_SUBMIT);
                checkImportDataDTO.setSysClientCssVersion(sysClentCss.getVersion() - 1);
            }
        }
        List<LcdpResourceFileBean> resourceFileList = wrapper.parse(LcdpResourceFileBean.class);
        //导入文件
        if (!ObjectUtils.isEmpty(lcdpResourceFileList)) {
            importLcdpRsourceFileList(lcdpResourceFileList, resourceFileList);

            importContentLogDto.setResourceFileList(lcdpResourceFileList);
        }

        List<LcdpScriptBlockBean> lcdpScriptBlockList = wrapper.parse(LcdpScriptBlockBean.class);
        //导入代码块
        if (!ObjectUtils.isEmpty(lcdpScriptBlockList)) {
            List<LcdpScriptBlockBean> importScriptBlockList = (List<LcdpScriptBlockBean>) importDataMap.get("scriptBlock");
            List<LcdpScriptBlockBean> selectedImportScriptBlockList = importScriptBlockList.stream().filter(scriptBlock -> lcdpScriptBlockList.stream().anyMatch(selectedScriptBlock -> StringUtils.equals(selectedScriptBlock.getExt$Item("title"), scriptBlock.getName()))).collect(Collectors.toList());
            scriptBlockService.importData(selectedImportScriptBlockList);

            List<LcdpScriptBlockTreeNodeDTO> scriptBlockTreeNodeDTOList = scriptBlockService.buildTreeNodeDTOList(selectedImportScriptBlockList);
            importContentLogDto.setScriptBlockTreeNodeDTOList(scriptBlockTreeNodeDTOList);
        }


        record.setId(ApplicationContextHelper.getNextIdentity());
        record.setRollbackable(Constant.YES);
        record.setHasRollbackFlag(Constant.NO);
        record.setImportLog(importLog);
        record.setSubmitLogId(submitLog.getId());

        record.setImportContent(JSON.toJSONString(importContentLogDto));
        String exportLog = fileMap.get("exportLog");
        JSONObject exportLogJson = JSON.parseObject(exportLog);
        String log = exportLogJson.getString("log");
        record.setExportLog(log);
        EntityHelper.assignCreatedElement(record);
        record.setCreatedTime(submitLog.getCreatedTime());
        resourceImportRecordService.getDao().insert(record);

        dealImportRecord(record, checkImportDataDTO);


        if (StringUtils.isEmpty(importJavaRecord.toString())) {
            return I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.RESOURCE_IMPORT_SUCCESS");
        }

        return I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.RESOURCE_IMPORT_SUCCESS") + I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.JAVA.COMPILER.ERROR") + "：" + "/r/n" + importJavaRecord;


    }

    private void dealImportRecord(LcdpResourceImportRecordBean record, LcdpCheckImportDataDTO checkImportDataDTO) {
        List<LcdpResourceBean> resourceList = checkImportDataDTO.getResourceList();
        List<LcdpRImportRecordDetailBean> detailList = new ArrayList<>();

        if (!ObjectUtils.isEmpty(resourceList)) {
            List<LcdpRImportRecordDetailBean> resourceDetailList = resourceList.stream().map(resource -> {
                LcdpRImportRecordDetailBean detail = new LcdpRImportRecordDetailBean();
                detail.setResourceId(resource.getId().toString());
                detail.setRecordId(record.getId());
                detail.setResourceCategory(resource.getResourceCategory());
                detail.setResourceVersion(resource.getEffectVersion());
                detail.setDeleteFlag(resource.getDeleteFlag());
                detail.setId(ApplicationContextHelper.getNextIdentity());
                detail.setRollbackable(Constant.YES);
                return detail;
            }).collect(Collectors.toList());
            detailList.addAll(resourceDetailList);
        }
        List<LcdpTableBean> tableList = checkImportDataDTO.getTableList();
        if (!ObjectUtils.isEmpty(tableList)) {
            List<LcdpRImportRecordDetailBean> resourceTableList = tableList.stream().map(table -> {
                LcdpRImportRecordDetailBean detail = new LcdpRImportRecordDetailBean();
                detail.setResourceId(table.getTableName());
                detail.setRecordId(record.getId());
                detail.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_TABLE);
                detail.setResourceVersion(table.getVersion());
                detail.setId(ApplicationContextHelper.getNextIdentity());
                detail.setRollbackable(Constant.YES);

                return detail;
            }).collect(Collectors.toList());
            detailList.addAll(resourceTableList);
        }

        List<LcdpViewBean> viewList = checkImportDataDTO.getViewList();
        if (!ObjectUtils.isEmpty(viewList)) {
            List<LcdpRImportRecordDetailBean> resourceViewList = viewList.stream().map(view -> {
                LcdpRImportRecordDetailBean detail = new LcdpRImportRecordDetailBean();
                detail.setResourceId(view.getViewName());
                detail.setRecordId(record.getId());
                detail.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_DB_VIEW);
                detail.setResourceVersion(view.getVersion());
                detail.setId(ApplicationContextHelper.getNextIdentity());
                detail.setRollbackable(Constant.YES);
                return detail;
            }).collect(Collectors.toList());
            detailList.addAll(resourceViewList);
        }


        //需要特殊对待下 迁入后检出又被迁入升版提交的

        if (!ObjectUtils.isEmpty(resourceList)) {
            List<LcdpResourceBean> importSubmit = resourceList.stream().filter(resource -> StringUtils.equals(resource.getExt$Item("importSubmit"), Constant.YES)).collect(Collectors.toList());
            if (!ObjectUtils.isEmpty(importSubmit)) {
                Map<Long, LcdpResourceBean> resourceId2ResourceMap = importSubmit.stream().collect(Collectors.toMap(LcdpResourceBean::getId, resource -> resource));
                List<Long> importSubmitIdList = importSubmit.stream().map(resource -> resource.getId()).collect(Collectors.toList());
                List<LcdpRImportRecordDetailBean> needUpdateList = resourceImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("resourceOperations", LcdpConstant.OPERATION_OF_ROLLBACK_CHECKOUT).filter(MatchPattern.EQ).match("resourceId", importSubmitIdList).filter(MatchPattern.OIN));
                if (!needUpdateList.isEmpty()) {
                    needUpdateList.forEach(detail -> {
                        detail.setResourceOperations(LcdpConstant.OPERATION_OF_ROLLBACK_SUBMIT);
                        detail.setRollbackable(Constant.NO);
                        detail.setOperationVersion(Long.valueOf(resourceId2ResourceMap.get(Long.valueOf(detail.getResourceId())).getExt$Item("importSubmitVersion")));
                    });
                    List<LcdpResourceImportRecordBean> updateList = needUpdateList.stream().map(LcdpRImportRecordDetailBean::getRecordId).distinct().map(recordId -> {
                        LcdpResourceImportRecordBean update = new LcdpResourceImportRecordBean();
                        update.setId(recordId);
                        update.setRollbackable(Constant.NO);
                        return update;
                    }).collect(Collectors.toList());
                    resourceImportRecordDetailService.getDao().update(needUpdateList, "resourceOperations", "rollbackable", "operationVersion");
                    resourceImportRecordService.getDao().update(updateList);
                }
            }
        }

        List<String> resourceIdList = detailList.stream().map(re -> re.getResourceId()).collect(Collectors.toList());
        List<LcdpRImportRecordDetailBean> exitResourceList = resourceImportRecordDetailService.selectListByFilter(SearchFilter.instance().match("resourceId", resourceIdList).filter(MatchPattern.OIN).match("rollbackable", Constant.YES).filter(MatchPattern.EQ));
        if (!ObjectUtils.isEmpty(exitResourceList)) {
            Map<String, String> resourceDeleteMap = detailList.stream().filter(re -> LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(re.getResourceCategory())).collect(Collectors.toMap(re -> re.getResourceId(), re -> re.getDeleteFlag()));
            List<LcdpRImportRecordDetailBean> updateList = exitResourceList.stream().map(re -> {
                re.setResourceOperations(StringUtils.equals(resourceDeleteMap.get(re.getResourceId()), Constant.YES) ? LcdpConstant.OPERATION_OF_ROLLBACK_CHECKIN_DELETE : LcdpConstant.OPERATION_OF_ROLLBACK_CHECKIN_SUBMIT);
                re.setDeleteFlag(StringUtils.equals(resourceDeleteMap.get(re.getResourceId()), Constant.YES) ? LcdpConstant.RESOURCE_DELETED_YES : LcdpConstant.RESOURCE_DELETED_NO);
//                re.setRollbackable(Constant.NO);
                return re;
            }).collect(Collectors.toList());
            resourceImportRecordDetailService.getDao().update(updateList);
            List<Long> recordList = exitResourceList.stream().map(re -> re.getRecordId()).distinct().collect(Collectors.toList());
            List<LcdpResourceImportRecordBean> updateRecordList = resourceImportRecordService.selectListByIds(recordList);
            updateRecordList.forEach(re -> {
                re.setRollbackable(Constant.NO);
            });
            resourceImportRecordService.getDao().update(updateRecordList);

        }


        if (StringUtils.isNotEmpty(checkImportDataDTO.getJsOperation())) {
            LcdpResourceImportRecordBean jsImport = resourceImportRecordService.selectFirstByFilter(SearchFilter.instance().match("sysClientJsVersion", checkImportDataDTO.getSysClientJsVersion()).filter(MatchPattern.EQ));
            if (jsImport != null) {
                jsImport.setRollbackable(Constant.NO);
                jsImport.setJsOperation(checkImportDataDTO.getJsOperation());
                resourceImportRecordService.getDao().update(jsImport);
            }
        }

        if (StringUtils.isNotEmpty(checkImportDataDTO.getCssOperation())) {
            LcdpResourceImportRecordBean cssImport = resourceImportRecordService.selectFirstByFilter(SearchFilter.instance().match("sysClientCssVersion", checkImportDataDTO.getSysClientCssVersion()).filter(MatchPattern.EQ));
            if (cssImport != null) {
                cssImport.setRollbackable(Constant.NO);
                cssImport.setCssOperation(checkImportDataDTO.getCssOperation());
                resourceImportRecordService.getDao().update(cssImport);
            }
        }
        resourceImportRecordDetailService.getDao().insert(detailList);


    }

    @Override
    public LcdpResourceCompareDTO selectHistoryDetail(Long historyId, RestJsonWrapperBean wrapper) {
        LcdpResourceCompareDTO compareDTO = new LcdpResourceCompareDTO();
        String category = wrapper.getParamValue("category");
        if (StringUtils.equals(category, LcdpConstant.RESOURCE_CATEGORY_TABLE)) {
            List<LcdpTableBean> tableList = lcdpTableService.selectListByFilter(SearchFilter.instance().match("ID", historyId).filter(MatchPattern.EQ));
            compareDTO.setCurrentTable(tableList.stream().findFirst().orElse(null));
            return compareDTO;

        } else if (StringUtils.equals(category, LcdpConstant.RESOURCE_CATEGORY_DB_VIEW)) {
            List<LcdpViewBean> viewList = lcdpViewService.selectListByFilter(SearchFilter.instance().match("ID", historyId).filter(MatchPattern.EQ));
            compareDTO.setCurrentView(viewList.stream().findFirst().orElse(null));
            return compareDTO;
        }
        LcdpResourceHistoryBean currentHistory = lcdpResourceHistoryService.selectById(Long.valueOf(historyId));
        compareDTO.setCurrentVersionHistory(currentHistory);
        if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_VIEW, currentHistory.getResourceCategory())) {
            List<LcdpModulePageCompBean> currentModulePageCompList = lcdpModulePageCompService.selectListByFilter(SearchFilter.instance().match("MODULEPAGEHISTORYID", currentHistory.getId()).filter(MatchPattern.EQ));

            compareDTO.setCurrentModulePageCompList(currentModulePageCompList);
        }
        return compareDTO;
    }

    @Override
    public LcdpResourceDTO selectContentByPath(RestJsonWrapperBean wrapper) {

        //获取资源路径,方法名称
        Map<String, String> paramMap = wrapper.getParamMap();
        String resourcePath = paramMap.get("resourcePath");
        String lcdpEnv = ServletUtils.getCurrentRequest().getHeader(LcdpConstant.REQUEST_HEADER_LCDPENV);

        List<String> queryColumnList = ArrayUtils.asList("ID", "EFFECTVERSION");

        LcdpResourceBean condition = new LcdpResourceBean();
        condition.setPath(resourcePath);
        condition.setDeleteFlag(LcdpConstant.RESOURCE_DELETED_NO);
        LcdpResourceDTO resourceDTO = new LcdpResourceDTO();
        if (designCenterTenantFlag && StringUtils.isNotEmpty(TenantContext.getTenant())) {
            if (StringUtils.equalsIgnoreCase(TenantContext.getTenant(), "LCDP_DESIGN_TENANT") || TenantContext.isMaster(TenantContext.getTenant())) {
                resourceDTO = getContentLcdpResourceDTO(resourcePath, lcdpEnv, queryColumnList, condition);
            } else {
                resourceDTO = tenantManager.call("LCDP_DESIGN_TENANT", "LCDP_DESIGN_TENANT", () -> {
                    return getContentLcdpResourceDTO(resourcePath, lcdpEnv, queryColumnList, condition);
                });
            }
        } else {
            resourceDTO = getContentLcdpResourceDTO(resourcePath, lcdpEnv, queryColumnList, condition);
        }

        return resourceDTO;
    }

    @NotNull
    private LcdpResourceDTO getContentLcdpResourceDTO(String resourcePath, String lcdpEnv, List<String> queryColumnList, LcdpResourceBean condition) {
        LcdpResourceBean resource = getDao().selectFirstIfPresent(condition, ArrayUtils.asList("PATH", "DELETEFLAG"), queryColumnList, Order.asc("RESOURCENAME"));


        //判断是否设计器调用
        if (LcdpConstant.REQUEST_HEADER_LCDPENV_DEVELOPMENT.equals(lcdpEnv)) {
            if (resource == null) {
                SearchFilter filter = SearchFilter.instance().match("PATH", resourcePath).filter(MatchPattern.EQ)
                        .match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.SEQ)
                        .match("CREATEDBYID", LocalContextHelper.getLoginUserId()).filter(MatchPattern.SEQ)
                        .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ);
                LcdpResourceHistoryBean history = lcdpResourceHistoryService.selectFirstByFilter(filter);

                if (history == null) {
                    return new LcdpResourceDTO();
                }
                LcdpResourceDTO resourceDTO = buildLcdpResourceDTO(history);
                return resourceDTO;
            }
            return getDesignResourceDTO(resource);
        }

        if (resource == null) {
            return new LcdpResourceDTO();
        }

        LcdpResourceHistoryBean historyCondition = new LcdpResourceHistoryBean();
        historyCondition.setResourceId(resource.getId());
        historyCondition.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);

        LcdpResourceHistoryBean resourceHistory = lcdpResourceHistoryService.getDao().selectFirst(historyCondition, Arrays.asList("ID", "RESOURCEID", "RESOURCENAME", "RESOURCECATEGORY", "CONTENT", "PATH"));
        LcdpResourceDTO resourceDTO = buildLcdpResourceDTO(resourceHistory);

        return resourceDTO;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.SAVE)
    @CacheEvict(value = "T_CORE_MENU", key = "'ALL'")
    public void publishMenu(RestJsonWrapperBean wrapper) {
        CoreMenuBean coreMenu = wrapper.parseUnique(CoreMenuBean.class);
        if (StringUtils.isEmpty(coreMenu.getId())) {
            return;
        }
        coreMenu.setPublishTime(LocalDateTime.now());
        coreMenu.setPublishUserId(LocalContextHelper.getLoginUserId());
        coreMenu.setPublishUserName(LocalContextHelper.getLoginUserName());
        if (designCenterTenantFlag && StringUtils.isNotEmpty(TenantContext.getTenant())) {
            if (TenantContext.isMaster(TenantContext.getTenant()) || StringUtils.equals(TenantContext.getTenant(), "LCDP_DESIGN_TENANT")) {
                String publishTenantId = coreMenu.getExt$Item("publishtenantid");

                if (StringUtils.equalsIgnoreCase(publishTenantId, "LCDP_DESIGN_TENANT")) {
                    tenantManager.call("T_CORE_MENU", () -> {
                        coreMenuService.getDao().update(coreMenu, "PUBLISHTIME", "PUBLISHUSERID", "PUBLISHUSERNAME", "RESOURCEID", "RESOURCENAME", "RESOURCEPATH");
                    });
                } else {
                    tenantManager.tenantCall("T_CORE_MENU", publishTenantId, () -> {
                        coreMenuService.getDao().update(coreMenu, "PUBLISHTIME", "PUBLISHUSERID", "PUBLISHUSERNAME", "RESOURCEID", "RESOURCENAME", "RESOURCEPATH");
                    });
                }

            }
        } else {
            coreMenuService.getDao().update(coreMenu, "PUBLISHTIME", "PUBLISHUSERID", "PUBLISHUSERNAME", "RESOURCEID", "RESOURCENAME", "RESOURCEPATH");
        }
        viewButtonRoleService.insertViewInfo(coreMenu);

        //菜单发布后，同步数据至越权配置(先删后增)
        List<CoreRequestUrlBean> deleteList = coreRequestUrlService.getDao().selectListByOneColumnValue(coreMenu.getId(), "MENUID");
        if (deleteList != null && deleteList.size() > 0) {
            coreRequestUrlService.getDao().deleteByIdList(deleteList.stream().map(e -> e.getId()).collect(Collectors.toList()));
        }

        Long resourceId = coreMenu.getResourceId();
        LcdpResourceBean pageResource = this.getDao().selectByIdIfPresent(resourceId);
        if (pageResource != null) {
            List<CoreRequestUrlBean> list = new ArrayList<>();

            //获取菜单的所有page页面
            List<LcdpResourceBean> pageList = new ArrayList<>();
            pageList.add(pageResource);
            getPageResourceList(pageResource, pageList);

            //获取所有page页面调用的url
            List<String> urlList = getRequestUrlList(pageList);
            List<LcdpServerScriptMethodBean> scriptMethodList = serverScriptMethodService.getDao().selectListByOneColumnValues(urlList, "METHODPATH");
            for (String url : urlList) {
                LcdpServerScriptMethodBean scriptMethod = scriptMethodList.stream().filter(f -> url.equals(f.getMethodPath())).findFirst().orElse(null);
                CoreRequestUrlBean requestUrl = new CoreRequestUrlBean();
                requestUrl.setId(ApplicationContextHelper.getNextIdentity());
                requestUrl.setMenuId(coreMenu.getId());
                requestUrl.setMethod("POST");
                requestUrl.setUrl(url);
                requestUrl.setNote(scriptMethod == null ? "" : scriptMethod.getMethodDesc());
                requestUrl.setDataSource(Constant.NO); //自动扫描
                list.add(requestUrl);
            }
            coreRequestUrlService.getDao().insert(list);
        }
    }


    @Override
    public List<LcdpServerScriptMethodBean> selectServerScriptMethodSelectableList(RestJsonWrapperBean wrapper) {
        Map<String, String> paramMap = wrapper.getParamMap();
        String methodPath = paramMap.get("methodPath");
        String mappingType = paramMap.get("mappingType");
        String methodDesc = paramMap.get("methodDesc");
        String[] queryParam = methodPath.split("\\*");
        SearchFilter filter = SearchFilter.instance();
        for (String param : queryParam) {
            filter.match("METHODPATH", param.toUpperCase()).filter(MatchPattern.CISC).match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ);
        }
        if (!StringUtils.isEmpty(mappingType)) {
            filter.match("MAPPINGTYPE", Arrays.asList(mappingType, MappingType.DEFAULT.name())).filter(MatchPattern.OR);
        }
        if (!StringUtils.isEmpty(methodDesc)) {
            filter.match("METHODDESC", methodDesc).filter(MatchPattern.SC);
        }
        List<LcdpServerScriptMethodBean> backEndScriptMethodList = serverScriptMethodService.selectListByFilter(filter, Order.asc("METHODPATH"));
        List<LcdpServerScriptMethodBean> removeList = backEndScriptMethodList.stream().filter(bs -> StringUtils.equals(bs.getMethodFlag(), "draft") && !StringUtils.equals(LocalContextHelper.getLoginUserId(), bs.getMethodCreatedById())).collect(Collectors.toList());
        backEndScriptMethodList.removeAll(removeList);
        if (backEndScriptMethodList.size() > 150) {
            backEndScriptMethodList = backEndScriptMethodList.subList(0, 150);//重新给list赋值
        }

        return backEndScriptMethodList;
    }

    @Override
    public Page<LcdpServerScriptMethodBean> selectServerScriptApiMethodList(RestJsonWrapperBean wrapper) {
        SearchFilter filter = SearchFilter.instance().match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.SEQ);
        return serverScriptMethodService.selectPaginationByFilter(filter, wrapper);
    }

    /**
     * 功能需求：在页面数据检出 复制 移动时需要将组件数据进行更新入库。更新的数据有：组件的ID 组件的配置config
     * 因为组件数据的id是前端生成的uuid 所以后台也要以uuid作为新生成的组件id。同时前端组件数据是具有父子层级关系的。
     * 组件ID生成规则：
     * 组件id:uuid  实现要点：根据组件层级父子关系，进行逐条更新。 更新后及时写入到父组件的config配置childrenWidgetId和结尾是_childrenWidgetId
     * 中维持好组件之间的父子关系。同时前端组件要求组件id和config配置里面的uuid一致，所以在组件数据更新后进行统一替换
     *
     * @param modulePageCompList 原始组件集合
     * @return 新生成的组件集合
     */
    @Override
    public List<LcdpModulePageCompBean> generateModulePageComps(List<LcdpModulePageCompBean> modulePageCompList) {
        if (ObjectUtils.isEmpty(modulePageCompList)) {
            return new ArrayList<>();
        }
        List<LcdpModulePageCompBean> pageCompDataList = modulePageCompList.stream().map(page -> {
            LcdpModulePageCompBean pageComp = new LcdpModulePageCompBean();
            BeanUtils.copyProperties(page, pageComp);
            return pageComp;
        }).collect(Collectors.toList());

        //找到根节点
        List<LcdpModulePageCompBean> rootCompList = modulePageCompList.stream().filter(comp -> ObjectUtils.isEmpty(comp.getParentId())).collect(Collectors.toList());
        Map<String, LcdpModulePageCompBean> parentCompMap = rootCompList.stream().collect(Collectors.toMap(LcdpModulePageCompBean::getId, Function.identity()));
        List<String> rootIdList = rootCompList.stream().map(LcdpModulePageCompBean::getId).collect(Collectors.toList());

        Map<String, LcdpModulePageCompBean> rootId2RootCompMap = rootCompList.stream().collect(Collectors.toMap(LcdpModulePageCompBean::getId, Function.identity()));
        //逻辑调整代码
        Map<String, List<LcdpModulePageCompBean>> parentId2PageCompListMap = modulePageCompList.stream().filter(comp -> null != comp.getParentId()).collect(Collectors.groupingBy(LcdpModulePageCompBean::getParentId));
        List<LcdpModulePageCompBean> childrenCompList = new ArrayList<>();
        List<String> singleRootIdList = new ArrayList<>(); //下面无子节点的root组件
        for (String rootId : rootIdList) {
            List<LcdpModulePageCompBean> pageCompBeanList = parentId2PageCompListMap.get(rootId);
            if (!ObjectUtils.isEmpty(pageCompBeanList)) {
                childrenCompList.addAll(pageCompBeanList);
            } else {
                singleRootIdList.add(rootId);
            }
        }
        List<LcdpModulePageCompBean> targetPageCompList = new ArrayList<>();//复制的页面配置集合

        //如果是单一组件 set值 判断条件：rootList循环下无子组件数据 则修改组件iD即可
        if (!singleRootIdList.isEmpty()) {
            for (String rootId : singleRootIdList) {
                LcdpModulePageCompBean singleComp = rootId2RootCompMap.get(rootId);
                singleComp.setId(StringUtils.randomUUID());
                targetPageCompList.add(singleComp);
            }
        }
        //结束
        //查询根节点下的子节点
        Map<String, List<LcdpModulePageCompBean>> compListByParentIdMap = childrenCompList.stream().collect(Collectors.groupingBy(LcdpModulePageCompBean::getParentId));
        Map<String, String> idMap = new HashMap<>();
        //处理根节点及根节点下的数据
        compListByParentIdMap.forEach((parentId, childrenList) -> {
            LcdpModulePageCompBean parentComp = parentCompMap.get(parentId);
            parentComp.setId(StringUtils.randomUUID());
            JSONObject config = JSON.parseObject(parentComp.getConfig());
            childrenList.forEach(child -> {
                String id = StringUtils.randomUUID();
                idMap.put(child.getId(), id);
                child.setId(id);
                child.setParentId(parentComp.getId());
            });

            Set<String> keySet = config.keySet();
            //需要将config中的childrenWidgetId和结尾是_childrenWidgetId 进行替换
            List<String> childrenWidgetIdList = keySet.stream().filter(key -> key.equals("childrenWidgetId") || key.endsWith("_childrenWidgetId")).collect(Collectors.toList());
            for (String key : childrenWidgetIdList) {
                JSONArray childrenWidgetId = (JSONArray) config.get(key);
                if (!ObjectUtils.isEmpty(childrenWidgetId)) {
                    for (int i = 0; i < childrenWidgetId.size(); i++) {
                        childrenWidgetId.set(i, idMap.get(childrenWidgetId.getString(i)));
                    }
                    config.put(key, childrenWidgetId);
                }
            }
            String configJson = JSONObject.toJSONString(config);
            parentComp.setConfig(configJson);
            targetPageCompList.add(parentComp);
            targetPageCompList.addAll(childrenList);
        });
        // 递归处理子节点的页面控件
        updateChildrenComp(idMap, targetPageCompList, parentId2PageCompListMap, pageCompDataList);
        for (LcdpModulePageCompBean pageComp : targetPageCompList) {
            String configStr = pageComp.getConfig();
            if (!StringUtils.isEmpty(configStr)) {
                JSONObject config = JSON.parseObject(configStr);

                //处理uuid 将新生成组件中config的uuid做到与组件的id 一致
                config.put("uuid", pageComp.getId());
                String configJson = JSONObject.toJSONString(config);
                pageComp.setConfig(configJson);
            }

        }
        List<LcdpModulePageCompBean> shuttleFrameList = targetPageCompList.stream().filter(comp -> "ShuttleFrame".equals(comp.getType())).collect(Collectors.toList());
        if (!shuttleFrameList.isEmpty()) {
            shuttleFrameList.forEach(shuttleFrame -> {
                JSONObject config = JSON.parseObject(shuttleFrame.getConfig());
                JSONArray childrenWidgetId = config.getJSONArray("childrenWidgetId");
                String leftGridId = childrenWidgetId.getString(0);
                String rightGridId = childrenWidgetId.getString(1);
                LcdpModulePageCompBean leftGrid = targetPageCompList.stream().filter(comp -> StringUtils.equals(leftGridId, comp.getId())).findFirst().orElse(null);
                LcdpModulePageCompBean rightGrid = targetPageCompList.stream().filter(comp -> StringUtils.equals(rightGridId, comp.getId())).findFirst().orElse(null);
                if (leftGrid != null) {
                    JSONObject leftGridConfig = JSON.parseObject(leftGrid.getConfig());
                    config.put("columns_childrenWidgetId", leftGridConfig.get("childrenWidgetId"));
                    config.put("leftToolbar_childrenWidgetId", leftGridConfig.get("toolbar_childrenWidgetId"));
                }
                if (rightGrid != null) {
                    JSONObject rightGridConfig = JSON.parseObject(rightGrid.getConfig());
                    config.put("rightColumns_childrenWidgetId", rightGridConfig.get("childrenWidgetId"));
                    config.put("rightToolbar_childrenWidgetId", rightGridConfig.get("toolbar_childrenWidgetId"));
                }
                String configJson = JSONObject.toJSONString(config);

                shuttleFrame.setConfig(configJson);
            });
        }
        return targetPageCompList;
    }

    @Override
    public RestValidationResultBean validateUnique(RestJsonWrapperBean wrapper) {
        String validateUniqueFieldJson = wrapper.getParamValue("vu");
        String resourceCategory = wrapper.getParamValue("resourceCategory");
        String parentId = wrapper.getParamValue("parentId");
        if (null == validateUniqueFieldJson) {
            return new RestValidationResultBean(true);
        } else {
            Map<String, Object> validateUniqueFieldMap = (Map<String, Object>) JSONObject.parseObject(validateUniqueFieldJson, new TypeReference<Map<String, Object>>() {
            }, new Feature[0]);
            Map<String, Object> columnMap = new CaseInsensitiveLinkedMap<>(validateUniqueFieldMap);
            Long id = ConvertUtils.convert(columnMap.get("id"), Long.class);
            columnMap.remove("id");
            if (!StringUtils.isEmpty(resourceCategory) && !StringUtils.isEmpty(parentId)) {
                //mapper单独校验
                if (StringUtils.equals(LcdpConstant.RESOURCE_CATEGORY_MAPPER, resourceCategory)) {

                    List<String> mapperNameList = new ArrayList<>();

                    LcdpConstant.MAPPER_TMPL_NAME_LIST.forEach(mapperName -> {
                        mapperNameList.add(columnMap.get("resourceName").toString() + mapperName);
                    });

                    List<LcdpResourceBean> mapperResourceList = selectListByFilter(SearchFilter.instance()
                            .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ)
                            .match("PARENTID", Long.valueOf(parentId)).filter(MatchPattern.EQ)
                            .match("RESOURCENAME", mapperNameList).filter(MatchPattern.OR));

                    if (!mapperResourceList.isEmpty()) {
                        return new RestValidationResultBean(false, I18nHelper.getMessage("core.validator.column.not-unique"));
                    } else {
                        return new RestValidationResultBean(true);
                    }
                }

                List<LcdpResourceBean> resourceList = selectListByFilter(SearchFilter.instance().match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ).match("RESOURCENAME", columnMap.get("resourceName").toString()).filter(MatchPattern.EQ));
                List<LcdpResourceBean> sameNameResourceList = new ArrayList<>();
                List<LcdpResourceBean> resourceListInSameFolder = new ArrayList<>();
                if (LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(resourceCategory) || LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(resourceCategory)) {
                    //筛选同名的模块或者脚本资源
                    sameNameResourceList = resourceList.stream()
                            .filter(resource -> LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(resource.getResourceCategory())
                                    || (LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(resourceCategory) && LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(resource.getResourceCategory())))
                            .collect(Collectors.toList());
                    resourceListInSameFolder = resourceList.stream().filter(re -> ObjectUtils.equals(re.getParentId(), Long.valueOf(parentId)) && StringUtils.equals(re.getResourceCategory(), resourceCategory)).collect(Collectors.toList());
                }
                //筛选出同名中的模块，因为模块没有路径，需要查询父分类，用于拼接路径
                List<Long> categoryIdList = sameNameResourceList.stream().filter(resource -> LcdpConstant.RESOURCE_CATEGORY_MODULE
                        .equals(resource.getResourceCategory())).map(LcdpResourceBean::getParentId).collect(Collectors.toList());
                List<LcdpResourceBean> categoryResources = this.selectListByIds(categoryIdList);
                Map<Long, LcdpResourceBean> categoryMap = categoryResources.stream().collect(Collectors.toMap(LcdpResourceBean::getId, bean -> bean));

                String pathStr = sameNameResourceList.stream().map(resourceBean -> {
                    if (LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(resourceBean.getResourceCategory())) {
                        LcdpResourceBean category = categoryMap.get(resourceBean.getParentId());
                        return category.getResourceName() + "." + resourceBean.getResourceName();
                    } else {
                        return resourceBean.getPath();
                    }
                }).collect(Collectors.joining(";"));


                boolean allowRepetitiveFileName = lcdpConfigParamService.allowRepetitiveFileName();

                //重命名特殊处理
                String resourceId = wrapper.getParamValue("resourceId");
                if (!StringUtils.isEmpty(resourceId)) {
                    boolean flag = resourceListInSameFolder.stream().allMatch(e -> ObjectUtils.equals(e.getId(), Long.valueOf(resourceId)));
                    if (flag) {
                        if (allowRepetitiveFileName || resourceList.isEmpty()) {
                            return new RestValidationResultBean(true, StringUtils.isEmpty(pathStr) ? null : pathStr);
                        } else {
                            return new RestValidationResultBean(false, StringUtils.isEmpty(pathStr) ? null : pathStr);
                        }
                    }
                }


                if (!resourceListInSameFolder.isEmpty()) {
                    return new RestValidationResultBean(false, I18nHelper.getMessage("core.validator.column.not-unique"));
                } else {
                    if (allowRepetitiveFileName || resourceList.isEmpty()) {
                        return new RestValidationResultBean(true, StringUtils.isEmpty(pathStr) ? null : pathStr);
                    } else {
                        return new RestValidationResultBean(false, StringUtils.isEmpty(pathStr) ? null : pathStr);
                    }
                }
            }
            return this.validateUnique(id, columnMap);
        }
    }

    @Override
    public RestValidationResultBean validateUnique(Long id, Map<String, Object> columnMap) {
        if (!ObjectUtils.isEmpty(columnMap.get("resourceName"))) {
            String resourceName = columnMap.get("resourceName").toString();
            List<LcdpResourceBean> resourceList = selectListByFilter(SearchFilter.instance().match("RESOURCENAME", resourceName).filter(MatchPattern.EQ).match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));
            if (!resourceList.isEmpty()) {
                return new RestValidationResultBean(false, I18nHelper.getMessage("core.validator.column.not-unique"));
            } else {
                return new RestValidationResultBean(true);
            }
        }
        return LcdpResourceService.super.validateUnique(id, columnMap);
    }

    @Override
    public LcdpWorkspaceDTO selectWorkSpaceData(RestJsonWrapperBean wrapper) {
        LcdpWorkspaceDTO workspaceDTO = new LcdpWorkspaceDTO();

        List<LcdpResourceBean> moduleList = selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_MODULE).filter(MatchPattern.EQ).match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));
        List<String> moduleIdList = moduleList.stream().map(LcdpResourceBean::getId).map(String::valueOf).collect(Collectors.toList());
        workspaceDTO.setModuleIdList(moduleIdList);

        return workspaceDTO;
    }

    /**
     * 替换脚本内容，实现开发平台脚本之间互相调用：
     * 1.将 @Autowired
     * private XxxService xxxService;
     * 替换为private Object getXxxService() { return LcdpScriptUtils.getBean("XxxService");}
     * 2.将 xxxService.selectPagination(id, wrapper) 替换成 SpringCaller.of(getXxxService(), "selectPagination").call(id, wrapper)
     */
    @Override
    public String replaceScriptContent(String content) {
        String sourceCode = LcdpJavaCodeResolverUtils.getNocommentJavaCode(content);

        List<String> importedPathList = LcdpJavaCodeResolverUtils.getImportedPackageList(sourceCode);

        Matcher matcher = LcdpJavaCodeResolverUtils.lcdpAutowiredBeanPatten.matcher(sourceCode);

        while (matcher.find()) {
            String className = matcher.group("className");

            String path = LcdpReflectionUtils.getClassFullName(importedPathList, className);

            log.info("=========>>>" + className + "--" + path);

            if (StringUtils.isBlank(path)) {
                continue;
            }

            if (!StringUtils.startsWith(path, "@")) { // 不是低代码平台开发的
                continue;
            }

            path = path.substring(1); // 去掉@

            log.info("类名=[{}] 匹配 路径=[{}]", className, path);

            String fieldName = matcher.group("fieldName");

            // 只有当注入的类为开发平台中新建的类时才需要替换
            String getMethodName = "get" + StringUtils.capitalize(fieldName);

            sourceCode = StringUtils.replaceFirst(sourceCode, matcher.group(),
                    String.format("private Object %s() { return LcdpScriptUtils.getBean(\"%s\");}",
                            getMethodName, path));

            String paramNamePrefix = "vp" + StringUtils.randomUUID(8);
            int paramNameIndex = 1;

            StringBuffer buffer = new StringBuffer();

            LcdpResourceClassInfoDTO classInfo = getActiveClassInfoByPath(path, LcdpUtils.isDebugRequest());

            // 匹配 =或空格 +工具类名.方法名(  这种格式内容
            Pattern calledPattern = Pattern.compile("(?<prefix>(\\r|\\n)[^\\r\\n]*[^A-Za-z0-9_\\.])(?<startText>(this\\.)?" + fieldName + ")\\.(?<methodName>[A-Za-z0-9]+)(?<nextText>\\([^:;]+)(?<suffix>\\s*:|;)");
            Matcher calledMatcher = calledPattern.matcher(sourceCode);
            while (calledMatcher.find()) {
                String prefix = calledMatcher.group("prefix");
                String startText = calledMatcher.group("startText");
                String methodName = calledMatcher.group("methodName");
                String nextText = calledMatcher.group("nextText");
                String suffix = calledMatcher.group("suffix");

                List<String> callList = CodeHintsManager.splitChainingCall(startText + "." + methodName + nextText);

                if (callList.get(0).equals("this")) {
                    callList.remove(0);
                }

                log.warn("---------11>" + path);
                log.warn("---------22>" + JSON.toJSONString(callList));

                if (classInfo.getClazz() != null) {
                    if (CollectionUtils.isEmpty(LcdpReflectionUtils.getMethod(classInfo.getClazz(), methodName))) {
                        throw new ApplicationRuntimeException("LCDP.EXCEPTION.METHOD_NOT_EXISTS", methodName, classInfo.getClazz().getName());
                    }
                } else {
                    if (CollectionUtils.isEmpty(LcdpReflectionUtils.getMethod(classInfo.getSourceCode(), methodName))) {
                        throw new ApplicationRuntimeException("LCDP.EXCEPTION.METHOD_NOT_EXISTS", methodName, ClassManager.getClassFullName(classInfo.getSourceCode()));
                    }
                }

                if (callList.size() <= 2) {
                    calledMatcher.appendReplacement(buffer, prefix + "SpringCaller.of(" + getMethodName + "(), \"" + methodName + "\").call" + nextText + suffix);
                } else {
                    CoreHintsChainingCallDTO chainingCall = new CoreHintsChainingCallDTO();
                    if (classInfo.getClazz() != null) {
                        chainingCall.setResolvableType(ResolvableType.forClass(classInfo.getClazz()));
                    } else {
                        chainingCall.setSourceCode(classInfo.getSourceCode());
                    }

                    chainingCall.setCallType(CodeHintsCallType.THIS);
                    chainingCall.setLevel(0);
                    chainingCall.addNextChain(callList.get(1));

                    CoreHintsChainingCallDTO nextChainingCall = CodeHintsManager.callNext(chainingCall);

                    if (nextChainingCall == null
                            || nextChainingCall.getResolvableType() == null) {
                        calledMatcher.appendReplacement(buffer, prefix + "SpringCaller.of(" + getMethodName + "(), \"" + methodName + "\").call" + nextText + suffix);
                    } else {
                        paramNameIndex++;
                        String paramName = paramNamePrefix + paramNameIndex;

                        String methodDesc = callList.get(1);

                        String typeName = nextChainingCall.getResolvableType().toString();

                        StringBuilder sb = new StringBuilder("\n    ")
                                .append(typeName)
                                .append(" ")
                                .append(paramName)
                                .append(" = (")
                                .append(typeName)
                                .append(") SpringCaller.of(")
                                .append(getMethodName)
                                .append("(), \"")
                                .append(methodName)
                                .append("\").call")
                                .append(methodDesc.substring(methodDesc.indexOf("(")))
                                .append(";")
                                .append(prefix)
                                .append(paramName);

                        for (int i = 2; i < callList.size(); i++) {
                            sb.append(".").append(callList.get(i));
                        }

                        sb.append(suffix);

                        calledMatcher.appendReplacement(buffer, sb.toString());
                    }
                }

                calledMatcher.appendTail(buffer);

                sourceCode = buffer.toString();
                buffer.setLength(0);
                calledMatcher = calledPattern.matcher(sourceCode);
            }

            matcher = LcdpJavaCodeResolverUtils.lcdpAutowiredBeanPatten.matcher(sourceCode);
        }

        // 替换，低代码平台开发的调用静态变量或静态方法的类
        List<String> lcdpImportedPathList = LcdpJavaCodeResolverUtils.getLcdpImportedPathList(sourceCode);
        if (!lcdpImportedPathList.isEmpty()) {
            for (String lcdpImportedPath : lcdpImportedPathList) {
                log.warn("------------>[" + lcdpImportedPath + "]");

                String className = lcdpImportedPath.substring(lcdpImportedPath.lastIndexOf(".") + 1);

                String paramNamePrefix = "vs" + StringUtils.randomUUID(8);
                int paramNameIndex = 1;

                LcdpResourceClassInfoDTO classInfo = getActiveClassInfoByPath(lcdpImportedPath, LcdpUtils.isDebugRequest());

                StringBuffer buffer = new StringBuffer();

                // 匹配 类名.方法名(  这种格式内容
                Pattern staticMethodPattern = Pattern.compile("(?<prefix>(\\r|\\n)[^\\r\\n]*[^A-Za-z0-9_\\.])" + className + "\\.(?<methodName>[A-Za-z0-9_]+)(?<nextText>\\([^;]+)");
                Matcher staticMethodMatcher = staticMethodPattern.matcher(sourceCode);

                int loopMethodCount = 0;
                while (loopMethodCount++ < 5) { // 正则匹配的一行信息中，含多个className，这种要特殊处理，最多循环5次
                    boolean loop = false;

                    while (staticMethodMatcher.find()) {
                        String prefix = staticMethodMatcher.group("prefix");
                        String methodName = staticMethodMatcher.group("methodName");
                        String nextText = staticMethodMatcher.group("nextText");

                        List<String> callList = CodeHintsManager.splitChainingCall(className + "." + methodName + nextText);

                        if (StringUtils.contains(prefix, className)
                                || StringUtils.contains(nextText, className)) {
                            loop = true;
                        }

                        log.warn("=======callList>" + JSON.toJSONString(callList));

                        log.warn("=======>" + prefix + "---" + methodName + "---" + nextText);

                        if (classInfo.getClazz() != null) {
                            if (CollectionUtils.isEmpty(LcdpReflectionUtils.getMethod(classInfo.getClazz(), methodName))) {
                                throw new ApplicationRuntimeException("LCDP.EXCEPTION.METHOD_NOT_EXISTS", methodName, classInfo.getClazz().getName());
                            }
                        } else {
                            if (CollectionUtils.isEmpty(LcdpReflectionUtils.getMethod(classInfo.getSourceCode(), methodName))) {
                                throw new ApplicationRuntimeException("LCDP.EXCEPTION.METHOD_NOT_EXISTS", methodName, ClassManager.getClassFullName(classInfo.getSourceCode()));
                            }
                        }

                        if (callList.size() <= 2) {
                            staticMethodMatcher.appendReplacement(buffer, prefix + "UtilsCaller.of(\"" + lcdpImportedPath + "\",\"" + methodName + "\").call" + nextText);
                        } else {
                            CoreHintsChainingCallDTO chainingCall = new CoreHintsChainingCallDTO();
                            if (classInfo.getClazz() != null) {
                                chainingCall.setResolvableType(ResolvableType.forClass(classInfo.getClazz()));
                            } else {
                                chainingCall.setSourceCode(classInfo.getSourceCode());
                            }

                            chainingCall.setCallType(CodeHintsCallType.STATIC);
                            chainingCall.setLevel(0);
                            chainingCall.addNextChain(callList.get(1));

                            CoreHintsChainingCallDTO nextChainingCall = CodeHintsManager.callNext(chainingCall);

                            if (nextChainingCall == null
                                    || nextChainingCall.getResolvableType() == null) {
                                staticMethodMatcher.appendReplacement(buffer, prefix + "UtilsCaller.of(\"" + lcdpImportedPath + "\",\"" + methodName + "\").call" + nextText);
                            } else {
                                paramNameIndex++;
                                String paramName = paramNamePrefix + paramNameIndex;

                                String methodDesc = callList.get(1);

                                StringBuilder sb = new StringBuilder("\n    ")
                                        .append(nextChainingCall.getResolvableType().toString())
                                        .append(" ")
                                        .append(paramName)
                                        .append(" = (")
                                        .append(nextChainingCall.getResolvableType().toString())
                                        .append(") UtilsCaller.of(\"")
                                        .append(lcdpImportedPath)
                                        .append("\",\"")
                                        .append(methodName)
                                        .append("\").call")
                                        .append(methodDesc.substring(methodDesc.indexOf("(")))
                                        .append(";")
                                        .append(prefix)
                                        .append(paramName);

                                for (int i = 2; i < callList.size(); i++) {
                                    sb.append(".").append(callList.get(i));
                                }

                                if (StringUtils.endsWith(nextText, ";")
                                        && !StringUtils.endsWith(sb.toString(), ";")) {
                                    sb.append(";");
                                }

                                staticMethodMatcher.appendReplacement(buffer, sb.toString());
                            }
                        }
                    }
                    staticMethodMatcher.appendTail(buffer);
                    sourceCode = buffer.toString();
                    buffer.setLength(0);


                    if (!loop) {
                        break;
                    }

                    staticMethodMatcher = staticMethodPattern.matcher(sourceCode);
                }

                // 匹配 类名.静态变量  这种格式内容
                Pattern staticFieldPattern = Pattern.compile("(?<prefix>(\\r|\\n)[^\\r\\n]*[^A-Za-z0-9_\\.])" + className + "\\.(?<fieldName>[A-Za-z0-9_]+)(?<nextText>[^A-Za-z0-9_][^\\r|\\n]*)");
                Matcher staticFieldMatcher = staticFieldPattern.matcher(sourceCode);

                int loopCount = 0;
                while (loopCount++ < 5) { // 正则匹配的一行信息中，含多个className，这种要特殊处理，最多循环5次
                    boolean loop = false;

                    while (staticFieldMatcher.find()) {
                        String prefix = staticFieldMatcher.group("prefix");
                        String fieldName = staticFieldMatcher.group("fieldName");
                        String nextText = staticFieldMatcher.group("nextText");

                        String chainingCallLine = className + "." + fieldName;

                        boolean nextStartDot = startDotPattern.matcher(nextText).find();

                        if (nextStartDot) {
                            chainingCallLine += nextText;
                        }

                        List<String> callList = CodeHintsManager.splitChainingCall(chainingCallLine);

                        log.warn("=======callList>" + JSON.toJSONString(callList));

                        log.warn("=======static field>>>" + prefix + "---" + fieldName + "---" + nextText);

                        if (StringUtils.contains(prefix, className)
                                || StringUtils.contains(nextText, className)) {
                            loop = true;
                        }

                        if (classInfo.getClazz() != null) {
                            if (LcdpReflectionUtils.getField(classInfo.getClazz(), fieldName) == null) {
                                throw new ApplicationRuntimeException("LCDP.EXCEPTION.FIELD_NOT_EXISTS", fieldName, classInfo.getClazz().getName());
                            }
                        } else {
                            if (LcdpReflectionUtils.getField(classInfo.getSourceCode(), fieldName) == null) {
                                throw new ApplicationRuntimeException("LCDP.EXCEPTION.FIELD_NOT_EXISTS", fieldName, ClassManager.getClassFullName(classInfo.getSourceCode()));
                            }
                        }

                        if (StringUtils.isBlank(prefix)) {
                            staticFieldMatcher.appendReplacement(buffer, prefix + "UtilsCaller.of(\"" + lcdpImportedPath + "\",\"" + fieldName + "\").getConstant()" + nextText);
                        } else {
                            CoreHintsChainingCallDTO chainingCall = new CoreHintsChainingCallDTO();
                            if (classInfo.getClazz() != null) {
                                chainingCall.setResolvableType(ResolvableType.forClass(classInfo.getClazz()));
                            } else {
                                chainingCall.setSourceCode(classInfo.getSourceCode());
                            }

                            chainingCall.setCallType(CodeHintsCallType.STATIC);
                            chainingCall.setLevel(0);
                            chainingCall.addNextChain(fieldName);

                            CoreHintsChainingCallDTO nextChainingCall = CodeHintsManager.callNext(chainingCall);

                            log.warn("--11------->" + nextChainingCall.getLevel() + "--" + nextChainingCall.getResolvableType() + "---" + nextChainingCall.getCallType());

                            if (nextChainingCall == null
                                    || nextChainingCall.getResolvableType() == null) {
                                staticFieldMatcher.appendReplacement(buffer, prefix + "UtilsCaller.of(\"" + lcdpImportedPath + "\",\"" + fieldName + "\").getConstant()" + nextText);
                            } else {
                                paramNameIndex++;
                                String paramName = paramNamePrefix + paramNameIndex;


                                StringBuilder sb = new StringBuilder();

                                StringBuilder utilClassConstantVariablesSb = new StringBuilder("\n    ")
                                        .append(nextChainingCall.getResolvableType().toString())
                                        .append(" ")
                                        .append(paramName)
                                        .append(" = (")
                                        .append(nextChainingCall.getResolvableType().toString())
                                        .append(") UtilsCaller.of(\"")
                                        .append(lcdpImportedPath)
                                        .append("\",\"")
                                        .append(fieldName)
                                        .append("\").getConstant();")
                                        .append("\n    ");


                                if (LcdpJavaCodeResolverUtils.elseIfPattern.matcher(prefix).find()) {
                                    String modifiedString = insertBeforeIf(buffer.toString(), utilClassConstantVariablesSb.toString());
                                    buffer = new StringBuffer(modifiedString);
                                } else {
                                    sb.append(utilClassConstantVariablesSb.toString());
                                }


                                sb.append(prefix)
                                        .append(paramName);


                                if (nextStartDot) {
                                    for (int i = 2; i < callList.size(); i++) {
                                        sb.append(".").append(callList.get(i));
                                    }
                                } else {
                                    sb.append(nextText);
                                }

                                if (StringUtils.endsWith(nextText, ";")
                                        && !StringUtils.endsWith(sb.toString(), ";")) {
                                    sb.append(";");
                                }

                                staticFieldMatcher.appendReplacement(buffer, sb.toString());
                            }
                        }
                    }
                    staticFieldMatcher.appendTail(buffer);
                    sourceCode = buffer.toString();
                    buffer.setLength(0);

                    if (!loop) {
                        break;
                    }

                    staticFieldMatcher = staticFieldPattern.matcher(sourceCode);
                }
            }
        }

        return LcdpJavaCodeResolverUtils.getCompilableSourceCode(sourceCode);
    }

    @Override
    public List<String> generatePackageImport(RestJsonWrapperBean wrapper) {
        return LcdpJavaCodeResolverUtils.getImportClassFullNameList(wrapper.getParamValue("content"));
    }

    @Override
    @Cacheable(value = "T_LCDP_RESOURCE.BY_PATH", key = "#path", unless = "#result == null")
    public LcdpResourceBean getByPath(String path) {
        if (designCenterTenantFlag && StringUtils.isNotBlank(TenantContext.getTenant())) {
            return tenantManager.call("RESOURCE.BY_PATH", TenantConstant.TENANT_LCDP_DESIGN_NAME, () -> {
                return selectFirstByFilter(SearchFilter.instance()
                        .match("PATH", path).filter(MatchPattern.EQ));
            });

        } else {
            return selectFirstByFilter(SearchFilter.instance()
                    .match("PATH", path).filter(MatchPattern.EQ));
        }

    }

    @Override
    @Cacheable(value = "T_LCDP_RESOURCE.LATEST_EXECUTED_BY_PATH", key = "#path", unless = "#result == null")
    public LcdpResourceBean getLatestActivatedResourceByPath(String path) {
        if (designCenterTenantFlag && StringUtils.isNotBlank(TenantContext.getTenant())) {
            return tenantManager.call("EXECUTED.BY_PATH", TenantConstant.TENANT_LCDP_DESIGN_NAME, () -> {
                LcdpResourceBean resource = selectFirstByFilter(SearchFilter.instance()
                                .match("PATH", path).filter(MatchPattern.EQ)
                                .match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ)
                                .match("EFFECTVERSION", null).filter(MatchPattern.DIFFER),
                        Order.desc("ID"));

                if (resource != null) {
                    return resource;
                }

                return selectFirstByFilter(SearchFilter.instance()
                                .match("PATH", path).filter(MatchPattern.EQ)
                                .match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ),
                        Order.desc("ID"));

            });

        } else {
            LcdpResourceBean resource = selectFirstByFilter(SearchFilter.instance()
                            .match("PATH", path).filter(MatchPattern.EQ)
                            .match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ)
                            .match("EFFECTVERSION", null).filter(MatchPattern.DIFFER),
                    Order.desc("ID"));

            if (resource != null) {
                return resource;
            }

            return selectFirstByFilter(SearchFilter.instance()
                            .match("PATH", path).filter(MatchPattern.EQ)
                            .match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ),
                    Order.desc("ID"));
        }
    }

    @Override
    @Transactional
    public Class<?> getActiveClassByPath(String path, boolean debug) {
        LcdpResourceClassInfoDTO classInfo = getActiveClassInfoByPath(path, debug);

        if (classInfo.getClazz() != null) {
            return classInfo.getClazz();
        }

        if (classInfo.getResourceHistoryId() != null) {
            if (!Objects.equals(classInfo.getModifyVersion(), classInfo.getCompiledVersion())) {
                LcdpResourceHistoryBean resourceHistory = new LcdpResourceHistoryBean();
                resourceHistory.setId(classInfo.getResourceHistoryId());
                resourceHistory.setCompiledVersion(classInfo.getModifyVersion());
                lcdpResourceHistoryService.getDao().update(resourceHistory, "COMPILEDVERSION");
            }
        }

        LcdpResourceHistoryBean history = lcdpResourceHistoryService.selectById(classInfo.getResourceHistoryId());

        return LcdpJavaCodeResolverUtils.loadSourceCode(history);
    }

    @Override
    public LcdpResourceClassInfoDTO getActiveClassInfoByPath(String path, boolean debug) {
        return getActiveClassInfoByPath(path, debug, false, true);
    }

    @Override
    public LcdpResourceClassInfoDTO getActiveClassInfoByPath(String path, boolean debug, boolean nullIfMissing, boolean requireClassIfPossiable) {
        // 获取脚本资源
        LcdpResourceBean resource = ApplicationContextHelper.getBean(LcdpResourceService.class).getLatestActivatedResourceByPath(path);

        if (resource == null) { // 文件被重命名并提交
            if (nullIfMissing) {
                return null;
            }

            throw new ApplicationRuntimeException("LCDP.EXCEPTION.PATH_NOT_FOUND", path);
        }

        if (!debug) {
            if (StringUtils.isBlank(resource.getClassContent())) { // 没有生效版本
                throw new ApplicationRuntimeException("LCDP.EXCEPTION.UNABLE_USE_NOT_PUBLISHED_CLASS", resource.getPath());
            }

            LcdpResourceClassInfoDTO classInfo = new LcdpResourceClassInfoDTO();
            classInfo.setResourceId(resource.getId());
            classInfo.setClassName(resource.getClassName());
            classInfo.setClassFullName(ClassManager.getClassFullName(resource.getClassContent()));
            classInfo.setClassSourceCode(resource.getClassContent());
            classInfo.setSourceCode(resource.getContent());
            if (requireClassIfPossiable) {
                Class<?> clazz = ClassManager.getClassByFullName(classInfo.getClassFullName());
                if (clazz == null) {
                    clazz = LcdpJavaCodeResolverUtils.loadSourceCode(resource);
                }
                classInfo.setClazz(clazz);
            }

            return classInfo;
        }

        boolean myLockResourceExists = false;
        //超级管理员sysAdmin 在运行页面时为了确保运行的是最新数据 所以查询脚本文件时不做人员控制
        if (StringUtils.equals(LcdpConstant.SUPER_ADMIN_ID, LocalContextHelper.getLoginUserId())) {
            myLockResourceExists = !"0".equals(lcdpResourceLockService.getLockStatus(LocalContextHelper.getLoginUserId(),
                    String.valueOf(resource.getId()), LcdpConstant.RESOURCE_CATEGORY_JAVA));
        } else {
            //查询资源锁定表,判断当前用户是否编辑
            myLockResourceExists = "1".equals(lcdpResourceLockService.getLockStatus(LocalContextHelper.getLoginUserId(),
                    String.valueOf(resource.getId()), LcdpConstant.RESOURCE_CATEGORY_JAVA));
        }

        if (!myLockResourceExists) { // 如果未被锁定，则使用当前生效版本
            LcdpResourceClassInfoDTO classInfo = new LcdpResourceClassInfoDTO();
            classInfo.setResourceId(resource.getId());
            classInfo.setClassName(resource.getClassName());

            if (StringUtils.isBlank(resource.getClassContent())) { // 没有生效版本
                throw new ApplicationRuntimeException("LCDP.EXCEPTION.UNABLE_USE_NOT_PUBLISHED_CLASS", resource.getPath());
            }

            classInfo.setClassFullName(ClassManager.getClassFullName(resource.getClassContent()));
            classInfo.setClassSourceCode(resource.getClassContent());
            classInfo.setSourceCode(resource.getContent());

            if (requireClassIfPossiable) {
                Class<?> clazz = ClassManager.getClassByFullName(classInfo.getClassFullName());
                if (clazz == null) {
                    clazz = LcdpJavaCodeResolverUtils.loadSourceCode(resource);
                }
                classInfo.setClazz(clazz);
            }

            return classInfo;
        } else { // 如果已经被锁定，则使用历史最高版本
            LcdpResourceHistoryBean resourceHistory = lcdpResourceHistoryService.selectUnsubmittedResourceHistory(LocalContextHelper.getLoginUserId(), resource.getId());

            LcdpResourceClassInfoDTO classInfo = new LcdpResourceClassInfoDTO();
            classInfo.setResourceId(resource.getId());
            classInfo.setClassName(ClassManager.getClassName(resourceHistory.getClassContent()));
            classInfo.setClassFullName(ClassManager.getClassFullName(resourceHistory.getClassContent()));
            classInfo.setClassSourceCode(resourceHistory.getClassContent());
            classInfo.setSourceCode(resourceHistory.getContent());
            classInfo.setResourceHistoryId(resourceHistory.getId());
            classInfo.setModifyVersion(resourceHistory.getModifyVersion());
            classInfo.setCompiledVersion(resourceHistory.getCompiledVersion());

            if (requireClassIfPossiable) {
                if (Objects.equals(resourceHistory.getCompiledVersion(), resourceHistory.getModifyVersion())) {
                    Class<?> clazz = ClassManager.getClassByFullName(classInfo.getClassFullName());
                    if (clazz == null) {
                        clazz = LcdpJavaCodeResolverUtils.loadSourceCode(resourceHistory);
                    }
                    classInfo.setClazz(clazz);
                }
            }

            return classInfo;
        }
    }

    @Override
    @Cacheable(value = "T_LCDP_RESOURCE.GET_PATH_BY_CLASS_NAME", key = "#className", unless = "#result == null")
    public String getPathByClassName(String className) {
        if (designCenterTenantFlag && StringUtils.isNotBlank(TenantContext.getTenant())) {
            return tenantManager.call("GET_PATH_BY_CLASS_NAME", TenantConstant.TENANT_LCDP_DESIGN_NAME, () -> {
                LcdpResourceBean filter = new LcdpResourceBean();
                filter.setResourceName(className);
                filter.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_JAVA);
                filter.setDeleteFlag(Constant.NO);

                LcdpResourceBean resource = getDao().selectFirstIfPresent(filter, Arrays.asList("ID", "PATH"));

                if (resource == null) {
                    return "";
                }

                return resource.getPath();
            });

        } else {
            LcdpResourceBean filter = new LcdpResourceBean();
            filter.setResourceName(className);
            filter.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_JAVA);
            filter.setDeleteFlag(Constant.NO);

            LcdpResourceBean resource = getDao().selectFirstIfPresent(filter, Arrays.asList("ID", "PATH"));

            if (resource == null) {
                return "";
            }

            return resource.getPath();
        }


    }

    @Override
    public List<String> getPathListByClassName(String className) {
        return getDao().selectPathListByResourceName(className);
    }

    @Override
    public List<LcdpResourceBean> selectReferencedList(String path) {
        return getDao().selectReferencedList(path);
    }

    @Override
    public List<LcdpResourceBean> selectTreeLeafNodeList(List<Long> categoryIdList) {
        List<LcdpResourceBean> checkoutLeafNodeList = getDao().selectTreeCheckoutLeafNodeList(categoryIdList);

        List<LcdpResourceBean> erorLeafNodeList = getDao().selectTreeErrorLeafNodeList(categoryIdList);

        List<LcdpResourceBean> resourceList = new ArrayList<>(checkoutLeafNodeList);

        for (LcdpResourceBean errorLeafNode : erorLeafNodeList) {
            LcdpResourceBean checkoutLeafNode = resourceList.stream().filter(r -> r.getId().equals(errorLeafNode.getId())).findAny().orElse(null);

            if (checkoutLeafNode == null) {
                resourceList.add(errorLeafNode);
            } else {
                String errorStatus = checkoutLeafNode.getExt$Item("errorstatus");

                if (StringUtils.isBlank(errorStatus)) {
                    checkoutLeafNode.setExt$Item("errorstatus", errorLeafNode.getExt$Item("errorstatus"));
                } else {
                    checkoutLeafNode.setExt$Item("errorstatus", errorStatus + "," + errorLeafNode.getExt$Item("errorstatus"));
                }
            }
        }

        return resourceList;
    }

    @Override
    public List<LcdpResourceBean> selectTreeSearchList(String resourceName, String columns) {
        return getDao().selectTreeSearchList(resourceName, columns);
    }

    @Override
    public LcdpResourceContentDTO selectActiveContent(Long resourceId, String scriptStatus) {
        boolean debug = LcdpUtils.isDebugRequest();

        // 获取脚本资源
        LcdpResourceBean resource = this.selectById(resourceId);

        // 已提交或正式环境上查询资源表的数据
        if ("submit".equals(scriptStatus)
                || !debug) {
            LcdpResourceContentDTO resourceContent = new LcdpResourceContentDTO();
            resourceContent.setContent(resource.getContent());
            resourceContent.setClassContent(resource.getClassContent());

            return resourceContent;
        }

        boolean myLockResourceExists = false;
        //超级管理员sysAdmin 在运行页面时为了确保运行的是最新数据 所以查询脚本文件时不做人员控制
        if (StringUtils.equals(LcdpConstant.SUPER_ADMIN_ID, LocalContextHelper.getLoginUserId())) {
            myLockResourceExists = !"0".equals(lcdpResourceLockService.getLockStatus(LocalContextHelper.getLoginUserId(),
                    String.valueOf(resource.getId()), LcdpConstant.RESOURCE_CATEGORY_JAVA));
        } else {
            //查询资源锁定表,判断当前用户是否编辑
            myLockResourceExists = "1".equals(lcdpResourceLockService.getLockStatus(LocalContextHelper.getLoginUserId(),
                    String.valueOf(resource.getId()), LcdpConstant.RESOURCE_CATEGORY_JAVA));
        }

        if (!myLockResourceExists) { // 如果未被锁定，则使用当前生效版本
            LcdpResourceContentDTO resourceContent = new LcdpResourceContentDTO();
            resourceContent.setContent(resource.getContent());
            resourceContent.setClassContent(resource.getClassContent());

            return resourceContent;
        } else { // 如果已经被锁定，则使用历史最高版本
            LcdpResourceHistoryBean resourceHistory = lcdpResourceHistoryService.selectUnsubmittedResourceHistory(LocalContextHelper.getLoginUserId(), resource.getId());

            LcdpResourceContentDTO resourceContent = new LcdpResourceContentDTO();
            resourceContent.setContent(resourceHistory.getContent());
            resourceContent.setClassContent(resourceHistory.getClassContent());

            return resourceContent;
        }
    }

    @Override
    public String getActiveMapperId(String mapperId) {
        if (LcdpUtils.isDebugRequest()) {
            try {
                //关闭sql推送,屏蔽mapper检出情况查询的日志推送
                LcdpScriptLogConfig.disable();

                // 截取当前mapperId对应的namespace
                String namespace = mapperId.substring(0, mapperId.lastIndexOf("."));

                String path = namespace;

                String mapperSuffix = StringUtils.capitalize(LcdpConstant.PROFILE_DB_LIST.stream()
                        .filter(profile -> ApplicationContextHelper.isProfileActivated(profile))
                        .findFirst()
                        .get()) + "Mapper";
                if (!StringUtils.endsWith(path, mapperSuffix)) {
                    path = StringUtils.removeEnd(path, "Mapper") + mapperSuffix;
                }


                // 根据路径查询mapper
                LcdpResourceBean mapperResource = getLatestActivatedResourceByPath(path);

                if (mapperResource == null) {
                    return mapperId;
                }

                LcdpResourceLockBean lockResource = null;
                // 超级管理员sysAdmin 在运行页面时为了确保运行的是最新数据 所以查询脚本文件时不做人员控制
                if (StringUtils.equals(LcdpConstant.SUPER_ADMIN_ID, LocalContextHelper.getLoginUserId())) {
                    List<LcdpResourceLockBean> lockList = lcdpResourceLockService.selectListByFilter(SearchFilter.instance()
                            .match("RESOURCEID", "" + mapperResource.getId()).filter(MatchPattern.EQ)
                            .match("LOCKUSERID", null).filter(MatchPattern.DIFFER));

                    lockResource = lockList.stream().findFirst().orElse(null);
                } else {
                    //查询资源锁定表,判断当前用户是否编辑
                    LcdpResourceLockBean lockFilter = new LcdpResourceLockBean();
                    lockFilter.setResourceId("" + mapperResource.getId());
                    lockFilter.setLockUserId(LocalContextHelper.getLoginUserId());
                    lockResource = ApplicationContextHelper.getBean(LcdpResourceLockService.class).getDao().selectFirstIfPresent(lockFilter);
                }

                if (lockResource != null) {
                    return namespace + LcdpMapperUtils.NAMESPACE_DEV_SUFFIX + mapperId.substring(mapperId.lastIndexOf("."));
                }

            } finally {
                //重新开启sql推送
                LcdpScriptLogConfig.enable();
            }
        }

        return mapperId;
    }

    @Override
    public List<LcdpResourceBean> selectPageServiceList() {
        return getDao().selectPageServiceList();
    }

    @Override
    public void updatePath(LcdpResourceBean scriptResource) {
        if (!LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(scriptResource.getResourceCategory())) {
            return;
        }

        LcdpResourceBean moduleResource = selectById(scriptResource.getParentId());
        LcdpResourceBean categoryResource = selectById(moduleResource.getParentId());

        // 赋值分类和模块的ID
        scriptResource.setModuleId(moduleResource.getId());
        scriptResource.setCategoryId(categoryResource.getId());

        //根据文件类型进行路径拼接  路径： 分类名.模块名.文件夹.文件名
        String path = categoryResource.getResourceName() + "." + moduleResource.getResourceName() + ".";

        switch (scriptResource.getResourceCategory()) {
            case LcdpConstant.RESOURCE_CATEGORY_VIEW:
                scriptResource.setPath(path + "page." + scriptResource.getResourceName());
                break;
            case LcdpConstant.RESOURCE_CATEGORY_JS:
                scriptResource.setPath(path + "client." + scriptResource.getResourceName());
                break;
            case LcdpConstant.RESOURCE_CATEGORY_JAVA:
                scriptResource.setPath(path + "server." + scriptResource.getResourceName());
                break;
            case LcdpConstant.RESOURCE_CATEGORY_MAPPER:
                scriptResource.setPath(path + "mapper." + scriptResource.getResourceName());
                break;
        }
    }

    /**
     * 初始化资源历史表
     */
    @Override
    public void initResourceHistory(LcdpResourceHistoryBean resourceHistory) {
        resourceHistory.setVersion(1L);
        resourceHistory.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_NO);
        resourceHistory.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);
        resourceHistory.setModifyVersion(LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(resourceHistory.getResourceCategory()) ? 1L : null);
        resourceHistory.setCompiledVersion(LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(resourceHistory.getResourceCategory()) ? 0L : null);
    }

    @Override
    public List<LcdpResourceBean> selectVisibleModuleList(boolean excludeDeleted) {
        return getDao().selectVisibleModuleList(excludeDeleted);
    }

    @Override
    public List<LcdpResourceBean> selectCheckoutableModuleList(List<Long> categoryIdList) {
        return getDao().selectCheckoutableModuleList(CollectionUtils.isEmpty(categoryIdList) ? null : categoryIdList);
    }

    @Override
    public List<LcdpResourceBean> selectCheckoutableResourceList(List<Long> moduleIdList) {
        return getDao().selectCheckoutableResourceList(CollectionUtils.isEmpty(moduleIdList) ? null : moduleIdList);
    }

    @Override
    public List<LcdpResourceBean> selectSubmittableResourceList(String userId, String columns) {
        return getDao().selectSubmittableResourceList(userId, columns);
    }

    @Override
    @Transactional
    public void copyMapper(String source, String database) {
        String fromSuffix = StringUtils.capitalize(source) + "Mapper";
        String toSuffix = StringUtils.capitalize(database) + "Mapper";

        List<LcdpResourceBean> mapperList = getDao().selectToBeCopiedMapperList(fromSuffix, toSuffix);

        if (mapperList.isEmpty()) {
            return;
        }

        List<LcdpResourceBean> sourceMapperList = mapperList.stream().map(mapper -> {
            LcdpResourceBean sourceMapper = new LcdpResourceBean();
            BeanUtils.copyProperties(mapper, sourceMapper);
            return sourceMapper;
        }).collect(Collectors.toList());

        List<Long> resourceIdList = mapperList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());

        SearchFilter searchFilter = SearchFilter.instance()
                .match("RESOURCEID", resourceIdList).filter(MatchPattern.OR)
                .match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.EQ);
        List<LcdpResourceHistoryBean> effectHistoryList = lcdpResourceHistoryService.selectListByFilter(searchFilter);

        List<LcdpResourceBean> insertResourceList = new ArrayList<>();
        List<LcdpResourceHistoryBean> insertResourceHistoryList = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        mapperList.forEach(m -> {
            LcdpResourceHistoryBean effectHistory = effectHistoryList.stream()
                    .filter(h -> h.getResourceId().equals(m.getId())).findAny().orElse(null);

            if (effectHistory == null) {
                return;
            }

            m.setId(ApplicationContextHelper.getNextIdentity());
            m.setResourceName(StringUtils.removeEndIgnoreCase(m.getResourceName(), fromSuffix) + toSuffix);
            m.setPath(StringUtils.removeEndIgnoreCase(m.getPath(), fromSuffix) + toSuffix);
            m.setEffectVersion(0L);
            m.setCheckoutTime(null);
            m.setCheckoutUserId(null);
            m.setCreatedById(LocalContextHelper.getLoginUserId());
            m.setCreatedByName(LocalContextHelper.getLoginUserName());
            m.setCreatedTime(now);
            m.setCreatedByOrgId(LocalContextHelper.getLoginOrgId());
            m.setCreatedByOrgName(LocalContextHelper.getLoginOrgName());
            insertResourceList.add(m);

            effectHistory.setId(ApplicationContextHelper.getNextIdentity());
            effectHistory.setResourceName(m.getResourceName());
            effectHistory.setPath(m.getPath());
            effectHistory.setResourceId(m.getId());

            effectHistory.setCreatedById(LocalContextHelper.getLoginUserId());
            effectHistory.setCreatedByName(LocalContextHelper.getLoginUserName());
            effectHistory.setCreatedTime(now);
            effectHistory.setCreatedByOrgId(LocalContextHelper.getLoginOrgId());
            effectHistory.setCreatedByOrgName(LocalContextHelper.getLoginOrgName());
            initResourceHistory(effectHistory);
            insertResourceHistoryList.add(effectHistory);
        });

        getDao().fastInsert(insertResourceList);
        lcdpResourceHistoryService.getDao().fastInsert(insertResourceHistoryList);

        insertResourceHistoryList.forEach(h -> {
            TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> {
                LcdpMapperUtils.loadMapper(h.getPath(), false, h.getContent());
            });
        });

        // 资源锁定
        lcdpResourceLockService.lockResource(insertResourceList);

        // 添加检出记录表
        resourceCheckoutRecordService.checkoutResource(insertResourceList);

        if (StringUtils.equals(ApplicationContextHelper.getConstantValue(COPY_MAPPER_DELETE_SOURCE_CONSTANT, Constant.NO), Constant.YES)) {
            physicalDeleteCopiedSourceMapper(sourceMapperList);
        }

        // 同步数据
        resourceIdList.forEach(LcdpResourceSyncManager::sync);
    }

    private void physicalDeleteCopiedSourceMapper(List<LcdpResourceBean> sourceMapperList) {
        if (CollectionUtils.isEmpty(sourceMapperList)) {
            return;
        }

        List<Long> sourceResourceIdList = sourceMapperList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
        List<String> sourceResourceIdStringList = sourceResourceIdList.stream().map(String::valueOf).collect(Collectors.toList());

        TaskExecutorManager.getDefaultRunner().runAfterCommitTransaction(() -> sourceMapperList.forEach(sourceMapper -> {
            LcdpMapperUtils.unloadMapper(sourceMapper.getPath(), false);
            LcdpMapperUtils.unloadMapper(sourceMapper.getPath(), true);
        }));

        List<LcdpResourceHistoryBean> sourceHistoryList = lcdpResourceHistoryService.getDao()
                .selectListByOneColumnValues(sourceResourceIdList, "RESOURCEID", Arrays.asList("ID"));
        if (!CollectionUtils.isEmpty(sourceHistoryList)) {
            lcdpResourceHistoryService.getDao().deleteByIdList(sourceHistoryList.stream().map(LcdpResourceHistoryBean::getId).collect(Collectors.toList()));
        }

        List<LcdpResourceVersionBean> sourceVersionList = lcdpResourceVersionService.selectListByFilter(SearchFilter.instance()
                .match("RESOURCEID", sourceResourceIdStringList).filter(MatchPattern.OR), Arrays.asList("ID"));
        if (!CollectionUtils.isEmpty(sourceVersionList)) {
            lcdpResourceVersionService.getDao().deleteByIdList(sourceVersionList.stream().map(LcdpResourceVersionBean::getId).collect(Collectors.toList()));
        }

        List<LcdpResourceLockBean> sourceLockList = lcdpResourceLockService.selectListByFilter(SearchFilter.instance()
                .match("RESOURCEID", sourceResourceIdStringList).filter(MatchPattern.OR), Arrays.asList("ID"));
        if (!CollectionUtils.isEmpty(sourceLockList)) {
            lcdpResourceLockService.getDao().deleteByIdList(sourceLockList.stream().map(LcdpResourceLockBean::getId).collect(Collectors.toList()));
        }

        List<LcdpResourceCheckoutRecordBean> sourceCheckoutRecordList = resourceCheckoutRecordService.selectListByFilter(SearchFilter.instance()
                .match("RESOURCEID", sourceResourceIdList).filter(MatchPattern.OR), Arrays.asList("ID"));
        if (!CollectionUtils.isEmpty(sourceCheckoutRecordList)) {
            resourceCheckoutRecordService.getDao().deleteByIdList(sourceCheckoutRecordList.stream().map(LcdpResourceCheckoutRecordBean::getId).collect(Collectors.toList()));
        }

        getDao().deleteByIdList(sourceResourceIdList);
    }

    @Override
    public List<LcdpResourceBean> selectCheckoutedList() {
        return getDao().selectCheckoutedList();
    }

    @Override
    public List<LcdpModulePageCompBean> copyPageComps(List<LcdpResourceHistoryBean> oldPageList,
                                                      Map<Long, Long> resourceIdMapping, Map<String, String> pathMapping, boolean scriptResource) {
        // 取原始页面配置
        List<Long> pageHistroyIdList = oldPageList.stream().map(LcdpResourceHistoryBean::getId).collect(Collectors.toList());

        List<LcdpModulePageCompBean> oldPageCompList = lcdpModulePageCompService.selectListByFilter(SearchFilter.instance()
                .match("MODULEPAGEHISTORYID", pageHistroyIdList).filter(MatchPattern.OR));

        // 删除有parentId，但是不存在对应父节点组件的
        oldPageCompList.removeIf(c -> {
            if (!StringUtils.isBlank(c.getParentId())) {
                if (oldPageCompList.stream().noneMatch(lc -> lc.getId().equals(c.getParentId()))) {
                    return true;
                }
            }

            return false;
        });

        List<LcdpModulePageI18nBean> oldPageI18nList = lcdpModulePageI18nService.selectListByFilter(SearchFilter.instance()
                .match("modulePageHistoryId", pageHistroyIdList).filter(MatchPattern.OR));

        List<LcdpModulePageCompBean> insertPageCompList = new ArrayList<>();
        List<LcdpModulePageI18nBean> insertPageI18nList = new ArrayList<>();

        Map<String, String> pageCompIdMapping = new HashMap<>();

        for (LcdpModulePageCompBean oldPageComp : oldPageCompList) {
            LcdpResourceHistoryBean oldPage = oldPageList.stream().filter(p -> p.getId().equals(oldPageComp.getModulePageHistoryId())).findAny().get();

            LcdpModulePageCompBean insertPageComp = new LcdpModulePageCompBean();
            BeanUtils.copyProperties(oldPageComp, insertPageComp, LcdpConstant.COPY_IGNORE_PROPERTIES);

            insertPageComp.setId(StringUtils.randomUUID());
            pageCompIdMapping.put(oldPageComp.getId(), insertPageComp.getId());

            Long modulePageId = resourceIdMapping.get(oldPageComp.getModulePageId());
            Long modulePageHistoryId = resourceIdMapping.get(oldPageComp.getModulePageHistoryId());

            insertPageComp.setModulePageId(modulePageId);
            insertPageComp.setModulePageHistoryId(modulePageHistoryId);

            String config = insertPageComp.getConfig();

            // 替换前缀
            if (!CollectionUtils.isEmpty(pathMapping)) {
                String oldPath = oldPage.getPath();
                String newPath = pathMapping.get(oldPath);

                config = scriptResource ? ScriptOperator.getUpdatedContent(config, newPath, oldPath)
                        : ModuleOperator.getUpdatedContent(config, newPath, oldPath);
            }

            // 替换UUID
            config = StringUtils.replace(config, oldPageComp.getId(), insertPageComp.getId());

            insertPageComp.setConfig(config);

            insertPageCompList.add(insertPageComp);
        }

        /**
         * 更新parentId和config
         */
        for (LcdpModulePageCompBean insertPageComp : insertPageCompList) {
            // 更新parentId
            insertPageComp.setParentId(pageCompIdMapping.get(insertPageComp.getParentId()));

            String config = insertPageComp.getConfig();

            for (Entry<String, String> entry : pageCompIdMapping.entrySet()) {
                config = StringUtils.replace(config, entry.getKey(), entry.getValue());
            }

            insertPageComp.setConfig(config);
        }

        for (LcdpModulePageI18nBean oldPageI18n : oldPageI18nList) {
            LcdpResourceHistoryBean oldPage = oldPageList.stream().filter(p -> p.getId().equals(oldPageI18n.getModulePageHistoryId())).findAny().get();

            LcdpModulePageI18nBean insertPageI18n = new LcdpModulePageI18nBean();
            BeanUtils.copyProperties(oldPageI18n, insertPageI18n, LcdpConstant.COPY_IGNORE_PROPERTIES);

            insertPageI18n.setId(ApplicationContextHelper.getNextIdentity());

            Long modulePageId = resourceIdMapping.get(oldPageI18n.getModulePageId());
            Long modulePageHistoryId = resourceIdMapping.get(oldPageI18n.getModulePageHistoryId());

            insertPageI18n.setModulePageId(modulePageId);
            insertPageI18n.setModulePageHistoryId(modulePageHistoryId);


            if (!CollectionUtils.isEmpty(pathMapping)) {
                String oldPath = oldPage.getPath();
                String newPath = pathMapping.get(oldPath);

                String code = insertPageI18n.getCode();

                // 替换前缀
                code = StringUtils.replace(code, oldPath, newPath);

                insertPageI18n.setCode(code);
            }

            insertPageI18nList.add(insertPageI18n);
        }

        lcdpModulePageCompService.getDao().fastInsert(insertPageCompList);
        lcdpModulePageI18nService.getDao().fastInsert(insertPageI18nList);

        return insertPageCompList;
    }

    @Override
    public List<LcdpResourceSearchDTO> selectJavaByKeyword(String userId, String keyword, String matchCase) {
        return getDao().selectJavaByKeyword(userId, keyword, matchCase);
    }

    @Override
    public List<LcdpResourceSearchDTO> selectJavascriptByKeyword(String userId, String keyword, String matchCase) {
        return getDao().selectJavascriptByKeyword(userId, keyword, matchCase);
    }

    @Override
    public List<LcdpResourceSearchDTO> selectMapperByKeyword(String userId, String mapperSuffix, String keyword, String matchCase) {
        return getDao().selectMapperByKeyword(userId, mapperSuffix, keyword, matchCase);
    }

    @Override
    public List<LcdpResourceSearchDTO> selectCompByKeyword(String userId, String keyword, String matchCase) {
        return getDao().selectCompByKeyword(userId, keyword, matchCase);
    }

    @Override
    public LcdpResourceMoveoutDataDTO selectMoveoutData(RestJsonWrapperBean wrapper) {
        LcdpResourceMoveoutDataDTO moveoutData = new LcdpResourceMoveoutDataDTO();

        List<LcdpResourceTreeNodeDTO> treeNodeList = resourceTreeService.selectMoveOutTree(wrapper);
        moveoutData.setResourceTree(treeNodeList);

        List<Long> scriptIdList = new ArrayList<>();
        treeNodeList.forEach(n -> { // 分类
            if (n.getChildren() != null) {
                for (AbstractTreeNode moduleNode : n.getChildren()) { // 模块
                    if (moduleNode.getChildren() != null) {
                        for (AbstractTreeNode folderNode : moduleNode.getChildren()) { // 文件夹
                            if (folderNode.getChildren() != null) {
                                for (AbstractTreeNode scriptNode : folderNode.getChildren()) { // 脚本
                                    scriptIdList.add(NumberUtils.parseLong(scriptNode.getId()));
                                }
                            }
                        }
                    }
                }
            }
        });

        if (!scriptIdList.isEmpty()) {
            List<LcdpResourceBean> scriptResourceList = getDao().selectListByIds(scriptIdList, Arrays.asList("ID", "LCDPFILEPATH"));

            List<String> pathList = scriptResourceList.stream()
                    .filter(r -> !StringUtils.isEmpty(r.getLcdpFilePath()))
                    .map(r -> JsonUtils.parseList(r.getLcdpFilePath(), String.class))
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());

            if (!pathList.isEmpty()) {
                List<LcdpResourceFileBean> fileList = resourceFileService.selectListByFilter(SearchFilter.instance()
                        .match("RELATIVEPATH", pathList).filter(MatchPattern.OR));

                moveoutData.setFileList(fileList);
            }
        }

        return moveoutData;
    }

    @Override
    public List<LcdpResourceBean> selectMoveOutModuleList(List<Long> categoryIdList) {
        return getDao().selectMoveOutModuleList(CollectionUtils.isEmpty(categoryIdList) ? null : categoryIdList);
    }

    @Override
    public LcdpResourceComparisonContentDTO selectComparisonContent(String type, Long id) {
        LcdpResourceComparisonContentDTO comparisonContent = new LcdpResourceComparisonContentDTO();

        if ("global".equals(type)) { // 全局资源
            comparisonContent.setContent(lcdpGlobalConfigService.selectColumnById(id, "CONTENT"));
        } else if ("table".equals(type)) {
            comparisonContent.setContent(lcdpTableService.selectColumnById(id, "SQL"));
        } else if ("view".equals(type)) {
            comparisonContent.setContent(lcdpViewService.selectColumnById(id, "SELECTSTATEMENT"));
        } else {
            comparisonContent.setContent(lcdpResourceHistoryService.selectColumnById(id, "CONTENT"));

            if ("page".equals(type)) { // 页面
                comparisonContent.setPageCompList(lcdpModulePageCompService.selectByModulePageHistoryId(id));
            }
        }

        return comparisonContent;
    }

    @Override
    public void updateVersionOffset(LcdpResourceBean javaScriptResource, LcdpResourceHistoryBean javaScriptHistoryResource) {
        // 获取最新删除的源代码，赋值版本偏差
        LcdpResourceBean resourceFilter = new LcdpResourceBean();
        resourceFilter.setPath(javaScriptResource.getPath());
        resourceFilter.setDeleteFlag(LcdpConstant.RESOURCE_DELETED_YES);
        LcdpResourceBean latestDeletedResource = getDao().selectFirstIfPresent(resourceFilter,
                Arrays.asList("ID", "EFFECTVERSION", "VERSIONOFFSET"),
                Order.desc("ID"));
        if (latestDeletedResource != null) {
            javaScriptResource.setVersionOffset(Optional.ofNullable(latestDeletedResource.getEffectVersion()).orElse(0L)
                    + Optional.ofNullable(latestDeletedResource.getVersionOffset()).orElse(0L)
                    + 1L);

            if (javaScriptHistoryResource != null) {
                javaScriptHistoryResource.setVersionOffset(javaScriptResource.getVersionOffset());
            }
        }
    }

    @Override
    public void initLcdpRequestUrls(RestJsonWrapperBean wrapper) {
        RedisHelper.put("REQUEST_URL_INIT", "REQUEST_URL_INIT_KEY", Constant.NO);
        //查询所有低代码菜单
        SearchFilter filter = SearchFilter.instance().match("RESOURCEID", null).filter(MatchPattern.DIFFER);
        List<CoreMenuBean> menuList = coreMenuService.selectListByFilter(filter);

        List<Long> resourceIdList = menuList.stream().map(e -> e.getResourceId()).distinct().collect(Collectors.toList());
        List<LcdpResourceBean> resourceList = this.selectListByIds(resourceIdList);

        try {
            for (LcdpResourceBean menuResource : resourceList) {
                List<CoreRequestUrlBean> list = new ArrayList<>();

                //获取菜单的所有page页面
                List<LcdpResourceBean> pageList = new ArrayList<>();
                pageList.add(menuResource);
                getPageResourceList(menuResource, pageList);

                //获取所有page页面调用的url
                List<String> urlList = getRequestUrlList(pageList);
                List<LcdpServerScriptMethodBean> scriptMethodList = serverScriptMethodService.getDao().selectListByOneColumnValues(urlList, "METHODPATH");
                for (String url : urlList) {
                    //处理多个菜单配置相同url情况
                    List<CoreMenuBean> filterMenuList = menuList.stream().filter(f -> ObjectUtils.equals(f.getResourceId(), menuResource.getId())).collect(Collectors.toList());

                    LcdpServerScriptMethodBean scriptMethod = scriptMethodList.stream().filter(f -> url.equals(f.getMethodPath())).findFirst().orElse(null);
                    for (CoreMenuBean menu : filterMenuList) {
                        CoreRequestUrlBean requestUrl = new CoreRequestUrlBean();
                        requestUrl.setId(ApplicationContextHelper.getNextIdentity());
                        requestUrl.setMenuId(menu.getId());
                        requestUrl.setMethod("POST");
                        requestUrl.setUrl(url);
                        requestUrl.setNote(scriptMethod == null ? "" : scriptMethod.getMethodDesc());
                        requestUrl.setDataSource(Constant.NO); //自动扫描
                        list.add(requestUrl);
                    }
                }
                coreRequestUrlService.getDao().insert(list);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            RedisHelper.put("REQUEST_URL_INIT", "REQUEST_URL_INIT_KEY", Constant.YES);
            throw new RuntimeException("Unexpected code " + ex.getMessage());
        }

        RedisHelper.put("REQUEST_URL_INIT", "REQUEST_URL_INIT_KEY", Constant.YES);
    }

    @Override
    public String getLcdpServiceNameByTable(String table) {
        String beanName = ApplicationContextHelper.getLcdpServiceNameByTable(table);
        if (StringUtils.isEmpty(beanName)) {
            return null;
        }

        if (beanName.indexOf("$") > 0) {
            String path = beanName.split("\\$")[0];

            Class<?> clazz = null;
            try {
                clazz = getActiveClassByPath(path, LcdpUtils.isDebugRequest());
            } catch (Exception e) {
                /* ignore */
                return null;
            }

            if (clazz != null) {
                String realBeanName = LcdpJavaCodeResolverUtils.getBeanName(clazz);
                return realBeanName;
            }
        }
        return beanName;
    }

    //-------------------------------------------------------------------------------------------------------------
    // 私有方法
    //-------------------------------------------------------------------------------------------------------------
    private final static Pattern pageResourcePathPattern = Pattern.compile("(\\w+\\.\\w+\\.page\\.\\w+)");
    private final static Pattern requestUrlPattern = Pattern.compile("\\w+\\.\\w+\\.server\\.Lcdp\\w+Service\\.\\w+");

    /**
     * 获取pageResource所有的引用page页面
     *
     * @param pageResource
     * @return
     */
    private void getPageResourceList(LcdpResourceBean pageResource, List<LcdpResourceBean> list) {
        String content = pageResource.getContent();
        // 添加空值检查
        if (StringUtils.isEmpty(content)) {
            return;
        }


        Matcher matcher = pageResourcePathPattern.matcher(content);
        while (matcher.find()) {
            String path = matcher.group();
            LcdpResourceBean resource = getByPath(path);
            if (resource != null && list.stream().noneMatch(e -> ObjectUtils.equals(e.getId(), resource.getId()))) {
                list.add(resource);
                getPageResourceList(resource, list);
            }
        }
    }

    /**
     * 获取pageResource所有引用的requestUrl
     *
     * @param pageList
     * @return
     */
    private List<String> getRequestUrlList(List<LcdpResourceBean> pageList) {
        List<String> requestUrlList = new ArrayList<>();

        //1.获取页面组件配置的requestUrl
        List<String> typeList = Arrays.asList("Grid", "Uploader", "Form", "Select", "Choose"); //存在url请求的组件类型
        List<Long> resourceIdList = pageList.stream().map(e -> e.getId()).collect(Collectors.toList());
        SearchFilter searchFilter = SearchFilter.instance().match("MODULEPAGEID", resourceIdList).filter(MatchPattern.OR);
        searchFilter.match("TYPE", typeList).filter(MatchPattern.OR);
        List<LcdpModulePageCompBean> pageCompList = lcdpModulePageCompService.selectListByFilter(searchFilter);
        for (LcdpModulePageCompBean pageComp : pageCompList) {
            if (StringUtils.isEmpty(pageComp.getConfig())) {
                continue;
            }
            String content = pageComp.getConfig() + (pageComp.getConfigExtend() == null ? "" : pageComp.getConfigExtend());
            Matcher matcher = requestUrlPattern.matcher(content);
            while (matcher.find()) {
                String requestUrl = matcher.group();
                if (!requestUrlList.contains(requestUrl)) {
                    requestUrlList.add(requestUrl);
                }
            }

            //处理新版本url拆分后情况
            JSONObject contentObj = JSONObject.parseObject(content);
            String service = contentObj.getString("service");
            if (StringUtils.isNotEmpty(service)) {
                for (String key : contentObj.keySet()) {
                    if (key.endsWith("Method")) {
                        String method = contentObj.getString(key);
                        if (StringUtils.isNotEmpty(method) && !requestUrlList.contains(service + "." + method)) {
                            requestUrlList.add(service + "." + method);
                        }
                    }
                }
            }
        }

        //2.获取页面脚本中的requestUrl
        for (LcdpResourceBean page : pageList) {
            String content = page.getContent();
            if (StringUtils.isEmpty(content)) {
                continue;
            }
            //匹配直接写全路径的情况
            Matcher matcher = requestUrlPattern.matcher(content);
            while (matcher.find()) {
                String requestUrl = matcher.group();
                if (!requestUrlList.contains(requestUrl)) {
                    requestUrlList.add(requestUrl);
                }
            }

            //匹配通过变量拼接的情况，如： baseUrl + ".insert"
            String varAssignRegex = "var\\s+([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*=\\s*['\"]([^'\"]*\\.server\\.[^'\"]+)['\"]";
            String concatRegex = "([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*\\+\\s*['\"]\\.(.*?)['\"]";

            Pattern varAssignPattern = Pattern.compile(varAssignRegex);
            Pattern concatPattern = Pattern.compile(concatRegex);

            Matcher varAssignMatcher = varAssignPattern.matcher(content);
            Matcher concatMatcher = concatPattern.matcher(content);

            // 查找所有变量赋值
            while (varAssignMatcher.find()) {
                String varName = varAssignMatcher.group(1);
                String varValue = varAssignMatcher.group(2);
                concatMatcher.reset(content);
                while (concatMatcher.find()) {
                    String concatVarName = concatMatcher.group(1);
                    String concatString = concatMatcher.group(2);

                    if (concatVarName.equals(varName)) {
                        String requestUrl = varValue + "." + concatString;
                        if (!requestUrlList.contains(requestUrl)) {
                            requestUrlList.add(requestUrl);
                        }
                    }
                }
            }
        }
        return requestUrlList.stream().sorted().collect(Collectors.toList());
    }

    /**
     * 校验脚本是否可以被调用
     *
     * @param scriptPath 资源路径
     * @return
     */
    private boolean validateScriptCallable(String scriptPath) {

        if (StringUtils.isEmpty(scriptPath)) {
            return false;
        }

        if (ServletUtils.getCurrentResponse() != null) {
            ServletUtils.getCurrentResponse().addHeader(LcdpConstant.REQUEST_HEADER_SCRIPTPATH, scriptPath);
        }

        //附件的内置脚本直接过滤
        if (scriptPath.indexOf("lcdp.file.server.FileScript") >= 0) {
            return true;
        }

        int lastDotIndex = scriptPath.lastIndexOf(".");
        //从前端传过来的调用路径中截取脚本路径和调用方法名
        String path = scriptPath.substring(0, lastDotIndex);
        if (designCenterTenantFlag && StringUtils.isNotEmpty(TenantContext.getTenant())) {
            tenantManager.tenantCall("validate", "LCDP_DESIGN_TENANT", () -> {
                // 是否存在
                if (Constant.NO.equals(lcdpResourceHistoryService.isExists(path))) {
                    throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.METHOD_NOT_EXITS") + ":" + scriptPath);
                }

                // 是否被删除
                if (Constant.NO.equals(lcdpResourceHistoryService.isActive(path))) {
                    throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.HAS_BEEN_DELETED") + ":" + scriptPath);
                }
            });
        } else {
            if (Constant.NO.equals(lcdpResourceHistoryService.isExists(path))) {
                throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.METHOD_NOT_EXITS") + ":" + scriptPath);
            }

            // 是否被删除
            if (Constant.NO.equals(lcdpResourceHistoryService.isActive(path))) {
                throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.HAS_BEEN_DELETED") + ":" + scriptPath);
            }
        }

        return true;
    }

    private void checkAccessPermission(String scriptPath) {
        List<CoreRequestUrlBean> requestUrlList = coreRequestUrlService.selectListByRequestUrl(scriptPath);

        if (CollectionUtils.isEmpty(requestUrlList)) {
            return;
        }

        //白名单过滤
        CoreRequestUrlWhitelistBean filter = new CoreRequestUrlWhitelistBean();
        filter.setUrl(scriptPath);
        filter.setActivatedFlag(Constant.YES);
        CoreRequestUrlWhitelistBean whitelist = requestUrlWhitelistService.getDao().selectFirstIfPresent(filter);
        if (whitelist != null) {
            return;
        }

        // 当前人员是否有权限访问该请求路径
        List<CoreMenuBean> currentUserMenuList = coreMenuService.selectCurrentUserMenuList();
        if (requestUrlList.stream().noneMatch(u -> currentUserMenuList.stream()
                .anyMatch(m -> m.getId().equals(u.getMenuId())))) {
            throw new ApplicationWarningException("GIKAM.PERMISSION.UNAUTHORIZED");
        }
    }

    private void initAccessPermission(String scriptPath) {
        HttpServletRequest currentRequest = ServletUtils.getCurrentRequest();

        String menuId = currentRequest.getHeader(Constant.REQUEST_HEADER_MENUID);

        if (!StringUtils.isBlank(menuId)) {

            List<String> searchColNames = Arrays.asList("menuId", "url");
            List<String> selectColNames = Arrays.asList("id");

            CoreRequestUrlBean urlBean = new CoreRequestUrlBean();
            urlBean.setMenuId(menuId);
            urlBean.setNote("auto insert");
            urlBean.setUrl(scriptPath);

            GikamConcurrentLocker.blockRun("AUTO_INSERT_ACCESS::" + menuId + scriptPath, () -> {
                List<CoreRequestUrlBean> requestUrlList = coreRequestUrlService.getDao().selectList(urlBean, searchColNames, selectColNames);

                if (CollectionUtils.isEmpty(requestUrlList)) {
                    urlBean.setId(ApplicationContextHelper.getNextIdentity());
                    coreRequestUrlService.getDao().insert(urlBean);
                }
            });

        }

    }

    /**
     * 获取设计器脚本或页面数据
     */
    private LcdpResourceDTO getDesignResourceDTO1(LcdpResourceBean resource) {
        boolean isSuperAdmin = Objects.equals(LcdpConstant.SUPER_ADMIN_ID, LocalContextHelper.getLoginUserId());

        LcdpResourceHistoryBean historyFilter = new LcdpResourceHistoryBean();
        historyFilter.setResourceId(resource.getId());
        LcdpResourceHistoryBean latestHistory = lcdpResourceHistoryService.getDao().selectFirst(historyFilter, Order.desc("ID"));

        LcdpResourceDTO resourceDTO = buildLcdpResourceDTO(latestHistory);
        resourceDTO.setEffectVersion(resource.getEffectVersion()); // 生效版本


        // 新增的资源
        if (resource.getEffectVersion() == null
                || resource.getEffectVersion().equals(0L)) {
            // 当前人员是超管或检出人时才能编辑
            resourceDTO.setEditable(isSuperAdmin || LocalContextHelper.getLoginUserId().equals(resource.getCheckoutUserId()));
            resourceDTO.setResourceStatus(LcdpConstant.RESOURCE_STATUS_NEW);
            return resourceDTO;
        }

        // 未检出
        if (StringUtils.isBlank(resource.getCheckoutUserId())) {
            resourceDTO.setEditable(false);
            resourceDTO.setResourceStatus(LcdpConstant.RESOURCE_STATUS_VALID);
            return resourceDTO;
        }

        // 已检出，当前人员是超管或检出人时才能编辑
        resourceDTO.setEditable(isSuperAdmin || LocalContextHelper.getLoginUserId().equals(resource.getCheckoutUserId()));
        resourceDTO.setResourceStatus(LcdpConstant.RESOURCE_STATUS_VALID);
        return resourceDTO;
    }


    private LcdpResourceDTO getDesignResourceDTO(LcdpResourceBean resource) {
        LcdpResourceHistoryBean noSubmitHistory = lcdpResourceHistoryService.selectFirstByFilter(SearchFilter.instance().match("resourceId", resource.getId()).filter(MatchPattern.SEQ).match("submitFlag", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.SEQ));
        LcdpResourceHistoryBean effectHistory = lcdpResourceHistoryService.selectFirstByFilter(SearchFilter.instance().match("resourceId", resource.getId()).filter(MatchPattern.SEQ).match("effectFlag", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.SEQ));


        // 如果是新增数据点开
        if (resource.getEffectVersion() == null) {
            LcdpResourceDTO resourceDTO = new LcdpResourceDTO();

            resourceDTO.setEditable(true);
            resourceDTO = buildLcdpResourceDTO(noSubmitHistory);
            resourceDTO.setResourceStatus(LcdpConstant.RESOURCE_STATUS_NEW);
            return resourceDTO;
        }

        LcdpResourceHistoryBean viewResource = null;
        //超级管理员sysAdmin 在点开查看资源时可以查看到最新的的数据
        if (StringUtils.equals(LcdpConstant.SUPER_ADMIN_ID, LocalContextHelper.getLoginUserId())) {
            viewResource = noSubmitHistory;
        } else {
            if (noSubmitHistory == null || (noSubmitHistory != null && !StringUtils.equals(noSubmitHistory.getCreatedById(), LocalContextHelper.getLoginUserId()))) {
                viewResource = null;
            } else {
                viewResource = noSubmitHistory;
            }
        }

        if (viewResource != null) {
            LcdpResourceDTO resourceDTO = buildLcdpResourceDTO(viewResource);
            resourceDTO.setEditable(true);
            resourceDTO.setResourceStatus(LcdpConstant.RESOURCE_STATUS_VALID);
            return resourceDTO;
        } else {
            LcdpResourceDTO resourceDTO = buildLcdpResourceDTO(effectHistory);
            resourceDTO.setEditable(false);
            return resourceDTO;
        }
    }

    /**
     * 复制时更新组件
     *
     * @param idMap              新老组件ID映射
     * @param targetPageCompList 目标组件集合
     */
    private void updateChildrenComp(Map<String, String> idMap, List<LcdpModulePageCompBean> targetPageCompList,
                                    Map<String, List<LcdpModulePageCompBean>> parentId2PageCompListMap, List<LcdpModulePageCompBean> pageCompList) {

        List<LcdpModulePageCompBean> pageCompDataList = pageCompList.stream().map(page -> {
            LcdpModulePageCompBean pageComp = new LcdpModulePageCompBean();
            BeanUtils.copyProperties(page, pageComp);
            return pageComp;
        }).collect(Collectors.toList());

        Set<String> parentIdSet = idMap.keySet();
        //父节点
        List<String> parentIdList = new ArrayList<>(parentIdSet);
        List<String> childrenIdList = new ArrayList<>();
        List<LcdpModulePageCompBean> parentCompList = pageCompList.stream().filter(comp -> null != idMap.get(comp.getId())).collect(Collectors.toList());
        List<LcdpModulePageCompBean> childrenCompList = new ArrayList<>();
        for (String parentId : parentIdList) {
            List<LcdpModulePageCompBean> pageCompBeanList = parentId2PageCompListMap.get(parentId);
            if (null != pageCompBeanList) {
                childrenCompList.addAll(pageCompBeanList);
            }
        }

        Map<String, LcdpModulePageCompBean> parentCompMap = parentCompList.stream().collect(Collectors.toMap(LcdpModulePageCompBean::getId, Function.identity()));
        //子节点根据parentId分组
        Map<String, List<LcdpModulePageCompBean>> compListByParentIdMap = childrenCompList.stream().collect(Collectors.groupingBy(LcdpModulePageCompBean::getParentId));
        Map<String, String> id2Map = new HashMap<>();
        compListByParentIdMap.forEach((parentId, childrenList) -> {
            LcdpModulePageCompBean parentComp = parentCompMap.get(parentId);
            JSONObject config = JSON.parseObject(parentComp.getConfig());
            childrenList.forEach(child -> {
                String id = StringUtils.randomUUID();
                id2Map.put(child.getId(), id);
                childrenIdList.add(child.getId());
                child.setId(id);
                child.setParentId(idMap.get(parentComp.getId()));
            });
            Set<String> keySet = config.keySet();
            List<String> childrenWidgetIdList = keySet.stream().filter(key -> key.equals("childrenWidgetId") || key.endsWith("_childrenWidgetId")).collect(Collectors.toList());
            for (String key : childrenWidgetIdList) {
                JSONArray childrenWidgetId = (JSONArray) config.get(key);
                if (!ObjectUtils.isEmpty(childrenWidgetId)) {
                    for (int i = 0; i < childrenWidgetId.size(); i++) {
                        childrenWidgetId.set(i, id2Map.get(childrenWidgetId.getString(i)));
                    }
                    config.put(key, childrenWidgetId);
                }
            }

            String configJson = JSONObject.toJSONString(config);
            parentComp.setConfig(configJson);
            targetPageCompList.stream().filter(page -> idMap.get(parentComp.getId()).equals(page.getId())).forEach(page -> {
                page.setConfig(configJson);
            });
            targetPageCompList.addAll(childrenList);
        });
        List<LcdpModulePageCompBean> modulePageCompList = pageCompDataList.stream().filter(comp -> childrenIdList.contains(comp.getParentId())).collect(Collectors.toList());
        if (!modulePageCompList.isEmpty()) {
            updateChildrenComp(id2Map, targetPageCompList, parentId2PageCompListMap, pageCompDataList);
        }
    }

    /**
     * 构建资源内容DTO
     *
     * @param
     * @return
     */
    private LcdpResourceDTO buildLcdpResourceDTO(LcdpResourceHistoryBean resourceHistory) {

        LcdpResourceDTO resourceDTO = LcdpResourceDTO.of(resourceHistory);
        if (LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(resourceHistory.getResourceCategory())) {
            List<LcdpModulePageCompBean> modulePageCompList = lcdpModulePageCompService.selectByModulePageHistoryId(resourceHistory.getId());
            List<LcdpModulePageCompBean> dealPageCompList = modulePageCompList.stream().map(e -> {
                LcdpModulePageCompBean pageComp = new LcdpModulePageCompBean();
                BeanUtils.copyProperties(e, pageComp);
                return pageComp;
            }).collect(Collectors.toList());

            resourceDTO.setComponents(dealPageCompList);

            if (StringUtils.equals(LcdpConstant.EFFECT_FLAG_YES, resourceHistory.getEffectFlag())) {
                resourceDTO.setI18n(lcdpModulePageI18nService.selectPageI18nMessage(resourceHistory.getId()));
            } else {
                resourceDTO.setI18n(lcdpModulePageI18nService.selectPageI18nMessage(resourceHistory.getId()));
                List<LcdpPageI18nCodeBean> pageI18nCodeList = lcdpPageI18nCodeService.selectListByFilter(SearchFilter.instance().match("MODULEPAGEHISTORYID", resourceHistory.getId()).filter(MatchPattern.EQ));
                resourceDTO.setDependentI18n(pageI18nCodeList.stream().map(LcdpPageI18nCodeBean::getCode).collect(Collectors.toList()));
            }

            if (designCenterTenantFlag && StringUtils.isNotEmpty(TenantContext.getTenant())) {
                HttpServletRequest currentRequest = ServletUtils.getCurrentRequest();
                if (currentRequest != null) {
                    String tenantId = currentRequest.getHeader(TenantConstant.TENANT_REQUEST_HEADER);
                    if (StringUtils.isNotEmpty(tenantId)) {
                        tenantManager.tenantCall("SET_PAGE_CONFIG", tenantId, () -> {
                            resourceDTO.setPageConfig(buildPageConfig(resourceHistory.getPath()));
                        });
                    }
                } else {
                    resourceDTO.setPageConfig(buildPageConfig(resourceHistory.getPath()));
                }
            } else {
                //设置页面配置
                resourceDTO.setPageConfig(buildPageConfig(resourceHistory.getPath()));
            }

        }

        return resourceDTO;
    }


    /**
     * 在模块后构建文件夹并将脚本归类
     *
     * @param resourceList 资源集合
     * @param resource     资源
     */
    private void bulidFolder(List<LcdpResourceBean> resourceList, LcdpResourceBean resource) {

        //页面
        LcdpResourceBean pageFloder = new LcdpResourceBean();
        pageFloder.setId(Long.valueOf(resource.getId() + LcdpConstant.FOLDER_PAGE_SUFFIX));
        pageFloder.setOrderNo(1l);
        pageFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_PAGE);
        pageFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_PAGE);
        pageFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
        pageFloder.setParentId(resource.getId());
        resourceList.add(pageFloder);
        resourceList.stream().filter(re -> LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(re.getResourceCategory()) && re.getParentId().equals(resource.getId())).forEach(re -> re.setParentId(pageFloder.getId()));
        //前端脚本
        LcdpResourceBean frontFloder = new LcdpResourceBean();
        frontFloder.setId(Long.valueOf(resource.getId() + LcdpConstant.FOLDER_CLIENT_SUFFIX));
        frontFloder.setOrderNo(1l);
        frontFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_CLIENT);
        frontFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_CLIENT);
        frontFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
        frontFloder.setParentId(resource.getId());
        resourceList.add(frontFloder);
        resourceList.stream().filter(re -> LcdpConstant.RESOURCE_CATEGORY_JS.equals(re.getResourceCategory()) && re.getParentId().equals(resource.getId())).forEach(re -> re.setParentId(frontFloder.getId()));
        //后端脚本
        LcdpResourceBean backFloder = new LcdpResourceBean();
        backFloder.setId(Long.valueOf(resource.getId() + LcdpConstant.FOLDER_SERVER_SUFFIX));
        backFloder.setOrderNo(1l);
        backFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_SERVER);
        backFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_SERVER);
        backFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
        backFloder.setParentId(resource.getId());
        resourceList.add(backFloder);
        resourceList.stream().filter(re -> LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(re.getResourceCategory()) && re.getParentId().equals(resource.getId())).forEach(re -> re.setParentId(backFloder.getId()));

        LcdpResourceBean mapperFloder = new LcdpResourceBean();
        mapperFloder.setId(Long.valueOf(resource.getId() + LcdpConstant.FOLDER_MAPPER_SUFFIX));
        mapperFloder.setOrderNo(1l);
        mapperFloder.setResourceDesc(LcdpConstant.FOLDERS_UNDER_MODULE_MAPPER);
        mapperFloder.setResourceName(LcdpConstant.FOLDERS_UNDER_MODULE_MAPPER);
        mapperFloder.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_FOLDER);
        mapperFloder.setParentId(resource.getId());
        resourceList.add(mapperFloder);
        resourceList.stream().filter(re -> LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(re.getResourceCategory()) && re.getParentId().equals(resource.getId())).forEach(re -> re.setParentId(mapperFloder.getId()));

    }

    /**
     * 用于提交和导出时树的构建
     *
     * @param resourceIdList 资源ID集合
     * @param tableList      树资源集合
     * @return
     */
    public List<LcdpResourceTreeNodeDTO> bulidResourceTreeNodeDTOS(List<Long> resourceIdList, List<LcdpTableBean> tableList,
                                                                   List<LcdpViewBean> viewList, String... operationType) {

        List<LcdpResourceBean> resourceList = new ArrayList<>();
        //提前准备resorceDtoList  预防只有提交的表资源之说
        List<LcdpResourceTreeNodeDTO> resourceDTOList = new ArrayList<>();
        if (!resourceIdList.isEmpty()) {
            List<LcdpResourceBean> scriptList = getDao().selectListByIds(resourceIdList, ArrayUtils.asList("ID", "RESOURCENAME", "RESOURCEDESC", "RESOURCECATEGORY", "PARENTID", "ORDERNO"), Order.asc("RESOURCENAME"));

            //查询模块和分类用来构造提交树
            List<Long> moduleIdList = scriptList.stream().map(LcdpResourceBean::getParentId).collect(Collectors.toList());
            moduleIdList = moduleIdList.stream().distinct().collect(Collectors.toList());
            List<LcdpResourceBean> mooduleList = selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_MODULE).filter(MatchPattern.EQ).match("ID", moduleIdList).filter(MatchPattern.OR), Order.asc("RESOURCENAME"));
            List<Long> categoryIdList = mooduleList.stream().map(LcdpResourceBean::getParentId).collect(Collectors.toList());
            categoryIdList = categoryIdList.stream().distinct().collect(Collectors.toList());
            List<LcdpResourceBean> categoryList = selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_CATEGORY).filter(MatchPattern.EQ).match("ID", categoryIdList).filter(MatchPattern.OR), Order.asc("RESOURCENAME"));
            resourceList.addAll(scriptList);
            resourceList.addAll(mooduleList);
            resourceList.addAll(categoryList);

            for (int i = 0; i < resourceList.size(); i++) {
                LcdpResourceBean resource = resourceList.get(i);
                //在模块下构造三个文件夹放不同的文件
                if (LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(resource.getResourceCategory())) {

                    bulidFolder(resourceList, resource);
                }
            }
            //为了不破坏构建文件夹方法的纯粹性，在本方法内做判断
            if (!ObjectUtils.isEmpty(operationType)) {
                List<LcdpResourceHistoryBean> historyResourceList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", resourceIdList).filter(MatchPattern.OR).match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.SEQ));
                List<LcdpResourceHistoryBean> currentUserCheckout = historyResourceList.stream().filter(his -> StringUtils.equals(his.getCreatedById(), LocalContextHelper.getLoginUserId()) && StringUtils.equals(his.getDeleteFlag(), LcdpConstant.RESOURCE_DELETED_NO)).collect(Collectors.toList());

                currentUserCheckout.stream().forEach(e -> {
                    LcdpResourceHistoryBean history = historyResourceList.stream().filter(f -> ObjectUtils.equals(e.getId(), f.getResourceId())).findFirst().orElse(null);
                    if (history != null) {
                        e.setResourceName(history.getResourceName());
                    }
                });
                //待提交时如果文件夹下没文件 则不展示文件夹
                List<LcdpResourceBean> waitDeleteFolderList = new ArrayList<>();
                List<LcdpResourceBean> folderList = resourceList.stream().filter(resource -> LcdpConstant.RESOURCE_CATEGORY_FOLDER.equals(resource.getResourceCategory())).collect(Collectors.toList());
                Map<Long, LcdpResourceBean> folderMap = folderList.stream().collect(Collectors.toMap(LcdpResourceBean::getId, Function.identity(), (v1, v2) -> v1));
                List<Long> folderIdList = folderList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
                List<LcdpResourceBean> childList = resourceList.stream().filter(resource -> folderIdList.contains(resource.getParentId())).collect(Collectors.toList());
                Map<Long, List<LcdpResourceBean>> childMap = childList.stream().collect(Collectors.groupingBy(LcdpResourceBean::getParentId));
                folderMap.forEach((k, v) -> {
                    if (ObjectUtils.isEmpty(childMap.get(k))) {
                        waitDeleteFolderList.add(v);
                    }
                });
                resourceList.removeAll(waitDeleteFolderList);

                Map<Long, String> resourceId2UserMap = historyResourceList.stream().collect(Collectors.toMap(LcdpResourceHistoryBean::getResourceId, LcdpResourceHistoryBean::getCreatedByName, (v1, v2) -> v1));
                scriptList.forEach(script -> script.setExt$Item("checkoutuser", resourceId2UserMap.get(script.getId())));
            }
            TreeDescriptor<LcdpResourceBean> descriptor = new TreeDescriptor<>("id", "parentId", "resourceName", "orderNo");
            descriptor.setParseTreeNodeFunction(t -> {
                LcdpResourceTreeNodeDTO resourceDTO = LcdpResourceTreeNodeDTO.of(t);
                return resourceDTO;
            });
            resourceDTOList = TreeHelper.parseTreeNode(resourceList, descriptor, LcdpResourceTreeNodeDTO.class);
        }

        if (!tableList.isEmpty()) {
            //构建表根节点
            LcdpResourceTreeNodeDTO tableRoot = new LcdpResourceTreeNodeDTO();
            tableRoot.setName(LcdpConstant.RESOURCE_CATEGORY_TABLE);
            tableRoot.setDesc(LcdpConstant.RESOURCE_CATEGORY_TABLE);
            tableRoot.setId(LcdpConstant.RESOURCE_TABLE_TREE_ROOT_NAME);
            tableRoot.setType(LcdpConstant.RESOURCE_CATEGORY_CATEGORY);
            resourceDTOList.add(tableRoot);
            tableList.forEach(table -> {
                LcdpResourceTreeNodeDTO treeNodeDTO = LcdpResourceTreeNodeDTO.of(table);
                treeNodeDTO.setType(LcdpConstant.RESOURCE_CATEGORY_TABLE);
                treeNodeDTO.setParentId(tableRoot.getId());
                treeNodeDTO.setCheckoutUser(table.getCreatedByName());
                if (null != table.getVersion()) {
                    treeNodeDTO.setResourceStatus(1L == table.getVersion() && StringUtils.equals(LcdpConstant.SUBMIT_FLAG_NO, table.getSubmitFlag()) ? LcdpConstant.RESOURCE_STATUS_NEW : LcdpConstant.RESOURCE_STATUS_VALID);
                }
                tableRoot.addChild(treeNodeDTO);
            });
            TreeHelper.updateChildQty(resourceDTOList);
        }
        if (!viewList.isEmpty()) {

            //构建表根节点
            LcdpResourceTreeNodeDTO viewRoot = new LcdpResourceTreeNodeDTO();
            viewRoot.setName(LcdpConstant.RESOURCE_CATEGORY_DB_VIEW);
            viewRoot.setDesc(LcdpConstant.RESOURCE_CATEGORY_DB_VIEW);
            viewRoot.setId(LcdpConstant.RESOURCE_VIEW_TREE_ROOT_NAME);
            viewRoot.setType(LcdpConstant.RESOURCE_CATEGORY_CATEGORY);
            resourceDTOList.add(viewRoot);
            viewList.forEach(view -> {
                LcdpResourceTreeNodeDTO treeNodeDTO = LcdpResourceTreeNodeDTO.of(view);
                treeNodeDTO.setType(LcdpConstant.RESOURCE_CATEGORY_DB_VIEW);
                treeNodeDTO.setParentId(viewRoot.getId());
                treeNodeDTO.setCheckoutUser(view.getCreatedByName());
                if (null != view.getVersion()) {
                    treeNodeDTO.setResourceStatus(1L == view.getVersion() && StringUtils.equals(LcdpConstant.SUBMIT_FLAG_NO, view.getSubmitFlag()) ? LcdpConstant.RESOURCE_STATUS_NEW : LcdpConstant.RESOURCE_STATUS_VALID);
                }
                viewRoot.addChild(treeNodeDTO);
            });
            TreeHelper.updateChildQty(resourceDTOList);
        }

        return resourceDTOList;
    }

    public Map<String, Object> analysisFileContent
            (List<String> resourceIdStrList, List<String> tableNameList, List<String> viewNameList, Map<String, String> fileMap) {
        //资源集合
        List<LcdpResourceBean> resourceList = new ArrayList<>();

        List<LcdpResourceBean> scriptList = new ArrayList<>();//脚本
        List<LcdpResourceBean> moduleList = new ArrayList<>();//模块
        List<LcdpResourceBean> categoryList = new ArrayList<>();//分类

        List<CoreCodeCategoryBean> coreCodeCategoryList = new ArrayList<>();//系统编码分类集合
        List<CoreCodeBean> codeList = new ArrayList<>();//系统编码表
        List<CoreMenuBean> coreMenuList = new ArrayList<>();//系统编码表
        List<CoreI18nMessageBean> coreI18nMessageList = new ArrayList<>();//系统国际化信息表
        List<CoreAdminSelectConfigBean> coreAdminSelectConfigList = new ArrayList<>();//系统下拉框配置表
        List<LcdpResourceFileBean> lcdpFileList = new ArrayList<>();//低代码资源文件
        List<LcdpScriptBlockBean> scriptBlockList = new ArrayList<>();//代码块文件

        List<LcdpModulePageCompBean> modulePageCompList = new ArrayList<>();//页面组件
        List<LcdpModulePageI18nBean> modulePageI18nList = new ArrayList<>();//页面国际化
        List<LcdpPageI18nCodeBean> modulePageDependentI18nList = new ArrayList<>();//页面系统国际化依赖
        //查询模块和分类用来构造提交树
        for (String reourceId : resourceIdStrList) {
            String resourceJson = fileMap.get(reourceId);
            if (StringUtils.isEmpty(resourceJson)) {
                continue;
            }
            LcdpResourceBean resource = JsonUtils.parse(resourceJson, LcdpResourceBean.class);
            scriptList.add(resource);
        }
        scriptList = scriptList.stream().filter(resource -> LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(resource.getResourceCategory())).collect(Collectors.toList());


        for (LcdpResourceBean scrip : scriptList) {
            String resourceJson = fileMap.get(scrip.getParentId().toString());
            LcdpResourceBean resource = JsonUtils.parse(resourceJson, LcdpResourceBean.class);
            List<Long> moduleIdList = moduleList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
            if (!moduleIdList.contains(resource.getId())) {
                moduleList.add(resource);
            }
        }
        for (LcdpResourceBean module : moduleList) {
            String resourceJson = fileMap.get(module.getParentId().toString());
            LcdpResourceBean resource = JsonUtils.parse(resourceJson, LcdpResourceBean.class);
            List<Long> categoryIdList = categoryList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
            if (!categoryIdList.contains(resource.getId())) {
                categoryList.add(resource);
            }
        }
        resourceList.addAll(scriptList);
        resourceList.addAll(moduleList);
        resourceList.addAll(categoryList);


        List<LcdpTableDTO> tableDTOList = new ArrayList<>();
        for (String tableName : tableNameList) {
            String tableJson = fileMap.get(tableName);
            LcdpTableDTO tableDTO = JsonUtils.parse(tableJson, LcdpTableDTO.class);
            tableDTOList.add(tableDTO);
        }

        List<LcdpViewBean> viewList = new ArrayList<>();
        for (String viewName : viewNameList) {
            String viewJson = fileMap.get(viewName);
            LcdpViewBean view = JsonUtils.parse(viewJson, LcdpViewBean.class);
            viewList.add(view);
        }
        scriptList.stream().filter(resource -> LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(resource.getResourceCategory())).forEach(sc -> {
            String viewId = sc.getId() + "view";
            String i18nId = sc.getId() + "i18n";
            String sysI18nDependenceId = sc.getId() + "sysI18nDependence";

            String modulePageCompJson = fileMap.get(viewId);
            if (!StringUtils.isEmpty(modulePageCompJson)) {
                List<LcdpModulePageCompBean> modulePageCompBeans = JSON.parseArray(modulePageCompJson).toJavaList(LcdpModulePageCompBean.class);
                modulePageCompList.addAll(modulePageCompBeans);
            }

            String modulePageI18nJson = fileMap.get(i18nId);
            if (!StringUtils.isEmpty(modulePageI18nJson)) {
                modulePageI18nList.addAll(JSON.parseArray(modulePageI18nJson).toJavaList(LcdpModulePageI18nBean.class));
            }

            String modulePageI18nDependenceJson = fileMap.get(sysI18nDependenceId);
            if (!StringUtils.isEmpty(modulePageI18nDependenceJson)) {
                modulePageDependentI18nList.addAll(JSON.parseArray(modulePageI18nDependenceJson).toJavaList(LcdpPageI18nCodeBean.class));
            }
        });

        if (!ObjectUtils.isEmpty(fileMap.get("codeCategory"))) {
            List<CoreCodeCategoryBean> codeCategorys = JSON.parseArray(fileMap.get("codeCategory")).toJavaList(CoreCodeCategoryBean.class);
            coreCodeCategoryList.addAll(codeCategorys);
        }

        if (!ObjectUtils.isEmpty(fileMap.get("code"))) {
            List<CoreCodeBean> codes = JSON.parseArray(fileMap.get("code")).toJavaList(CoreCodeBean.class);
            codeList.addAll(codes);
        }

        if (!ObjectUtils.isEmpty(fileMap.get("i18n"))) {
            List<CoreI18nMessageBean> i18n = JSON.parseArray(fileMap.get("i18n")).toJavaList(CoreI18nMessageBean.class);
            coreI18nMessageList.addAll(i18n);
        }

        if (!ObjectUtils.isEmpty(fileMap.get("adminSelectConfig"))) {
            List<CoreAdminSelectConfigBean> adminSelectConfigList = JSON.parseArray(fileMap.get("adminSelectConfig")).toJavaList(CoreAdminSelectConfigBean.class);
            coreAdminSelectConfigList.addAll(adminSelectConfigList);
        }

        if (!ObjectUtils.isEmpty(fileMap.get("menu"))) {
            List<CoreMenuBean> menuList = JSON.parseArray(fileMap.get("menu")).toJavaList(CoreMenuBean.class);
            coreMenuList.addAll(menuList);
        }

        if (!ObjectUtils.isEmpty(fileMap.get("lcdpFile"))) {
            List<LcdpResourceFileBean> fileList = JSON.parseArray(fileMap.get("lcdpFile")).toJavaList(LcdpResourceFileBean.class);
            lcdpFileList.addAll(fileList);
        }

        if (!ObjectUtils.isEmpty(fileMap.get("scriptBlock"))) {
            List<LcdpScriptBlockBean> lcdpScriptBlockBeanList = JSON.parseArray(fileMap.get("scriptBlock")).toJavaList(LcdpScriptBlockBean.class);
            scriptBlockList.addAll(lcdpScriptBlockBeanList);
        }

        Map<String, Object> importDataMap = new HashMap<>();
        importDataMap.put("resource", resourceList);
        importDataMap.put("pageComps", modulePageCompList);
        importDataMap.put("pageI18n", modulePageI18nList);
        importDataMap.put("pageDependentI18n", modulePageDependentI18nList);
        importDataMap.put("table", tableDTOList);
        importDataMap.put("db-view", viewList);
        importDataMap.put("codeCategory", coreCodeCategoryList);
        importDataMap.put("code", codeList);
        importDataMap.put("i18n", coreI18nMessageList);
        importDataMap.put("menu", coreMenuList);
        importDataMap.put("adminSelectConfig", coreAdminSelectConfigList);
        importDataMap.put("lcdpFile", lcdpFileList);
        importDataMap.put("scriptBlock", scriptBlockList);

        return importDataMap;
    }

    private List<LcdpResourceTreeNodeDTO> bulidAnalysisTreeNodeDTOS(Map<String, LcdpAnalysisResultDTO> tableResult,
                                                                    Map<String, LcdpAnalysisResultDTO> viewResult,
                                                                    List<LcdpResourceBean> resourceList,
                                                                    List<LcdpTableDTO> tableDTOList,
                                                                    List<LcdpViewBean> viewList) {
        List<LcdpResourceTreeNodeDTO> resourceDTOList = new ArrayList<>();

        if (!resourceList.isEmpty()) {
            TreeDescriptor<LcdpResourceBean> descriptor = new TreeDescriptor<>("id", "parentId", "resourceName", "orderNo");
            descriptor.setParseTreeNodeFunction(t -> {
                LcdpResourceTreeNodeDTO resourceDTO = LcdpResourceTreeNodeDTO.of(t);
                resourceDTO.setAllowImport(true);
                resourceDTO.setResourceTag(StringUtils.isEmpty(t.getDeleteFlag()) || StringUtils.equals(LcdpConstant.RESOURCE_DELETED_NO, t.getDeleteFlag()) ? "update" : "delete");
                return resourceDTO;
            });
            resourceDTOList = TreeHelper.parseTreeNode(resourceList, descriptor, LcdpResourceTreeNodeDTO.class);
        }
        Boolean allowImport = true;
        if (!tableResult.isEmpty()) {
            Set<String> tableNameSet = tableResult.keySet();
            for (String tableName : tableNameSet) {
                allowImport = tableResult.get(tableName).getEnable();
                if (!allowImport) {
                    break;
                }
            }
        }
        if (!tableDTOList.isEmpty()) {
            //构建表根节点
            LcdpResourceTreeNodeDTO tableRoot = new LcdpResourceTreeNodeDTO();
            tableRoot.setName(LcdpConstant.RESOURCE_CATEGORY_TABLE);
            tableRoot.setDesc(LcdpConstant.RESOURCE_CATEGORY_TABLE);
            tableRoot.setId(ApplicationContextHelper.getNextIdentity().toString());
            tableRoot.setType(LcdpConstant.RESOURCE_CATEGORY_CATEGORY);
            tableRoot.setAllowImport(allowImport);
            resourceDTOList.add(tableRoot);
            for (LcdpTableDTO table : tableDTOList) {
                LcdpResourceTreeNodeDTO treeNodeDTO = new LcdpResourceTreeNodeDTO();
                treeNodeDTO.setId(table.getTableName());
                treeNodeDTO.setName(table.getTableName());
                treeNodeDTO.setDesc(table.getTableDesc());
                treeNodeDTO.setOrderNo(1L);
                treeNodeDTO.setType(LcdpConstant.RESOURCE_CATEGORY_TABLE);
                treeNodeDTO.setParentId(tableRoot.getId());
                treeNodeDTO.setAllowImport(tableResult.get(table.getTableName()).getEnable());
                treeNodeDTO.setAnalysisResult(tableResult.get(table.getTableName()).getAnalysisResultList().toString());
                tableRoot.addChild(treeNodeDTO);
            }

            TreeHelper.updateChildQty(resourceDTOList);
        }

        if (!viewList.isEmpty()) {
            //构建表根节点
            LcdpResourceTreeNodeDTO viewRoot = new LcdpResourceTreeNodeDTO();
            viewRoot.setName(LcdpConstant.RESOURCE_CATEGORY_DB_VIEW);
            viewRoot.setDesc(LcdpConstant.RESOURCE_CATEGORY_DB_VIEW);
            viewRoot.setId(ApplicationContextHelper.getNextIdentity().toString());
            viewRoot.setType(LcdpConstant.RESOURCE_CATEGORY_CATEGORY);
            resourceDTOList.add(viewRoot);
            viewRoot.setAllowImport(true);
            for (LcdpViewBean view : viewList) {
                LcdpResourceTreeNodeDTO treeNodeDTO = new LcdpResourceTreeNodeDTO();
                treeNodeDTO.setId(view.getViewName());
                treeNodeDTO.setName(view.getViewName());
                treeNodeDTO.setDesc(view.getViewName());
                treeNodeDTO.setOrderNo(1L);
                treeNodeDTO.setType(LcdpConstant.RESOURCE_CATEGORY_DB_VIEW);
                treeNodeDTO.setParentId(viewRoot.getId());
                treeNodeDTO.setAllowImport(viewResult.get(view.getViewName()).getEnable());
                treeNodeDTO.setAnalysisResult(viewResult.get(view.getViewName()).getAnalysisResultList().toString());
                viewRoot.addChild(treeNodeDTO);
                if (viewRoot.getAllowImport() && !treeNodeDTO.getAllowImport()) {
                    viewRoot.setAllowImport(false);
                }
            }

            TreeHelper.updateChildQty(resourceDTOList);
        }
        return resourceDTOList;
    }

    public List<LcdpTableBean> importTableData
            (List<String> tableNameList, Map<String, String> fileMap, LcdpSubmitLogBean submitLog, LcdpSubmitLogBean
                    autoSubmitLog) {
        //查出当前系统对应表信息
        List<LcdpTableDTO> currentLcdpTableDTOS = lcdpTableService.selectPhysicalTableInfoList(tableNameList);

        List<LcdpTableBean> tableList = lcdpTableService.selectListByFilter(SearchFilter.instance().match("TABLENAME", tableNameList).filter(MatchPattern.OCISEQ));

        Map<String, Optional<LcdpTableBean>> table2VersionMap = tableList.stream().collect(Collectors.groupingBy(LcdpTableBean::getTableName, Collectors.maxBy(Comparator.comparing(LcdpTableBean::getVersion))));

        List<LcdpTableBean> unCommitTableList = tableList.stream().filter(table -> LcdpConstant.SUBMIT_FLAG_NO.equals(table.getSubmitFlag())).collect(Collectors.toList());
        List<String> unCommitTableNameList = unCommitTableList.stream().map(LcdpTableBean::getTableName).collect(Collectors.toList());
        lcdpResourceLockService.unLock(unCommitTableNameList);


        List<LcdpResourceVersionBean> insertVersionList = unCommitTableList.stream().map(table -> {
            LcdpResourceVersionBean resourceVersion = new LcdpResourceVersionBean();
            resourceVersion.setId(ApplicationContextHelper.getNextIdentity());
            resourceVersion.setResourceId(table.getTableName());
            resourceVersion.setResourceName(table.getTableName());
            resourceVersion.setLogId(autoSubmitLog.getId());
            resourceVersion.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_TABLE);
            resourceVersion.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);
            resourceVersion.setVersion(table.getVersion());
            resourceVersion.setCommit(autoSubmitLog.getCommit());
            resourceVersion.setEditTime(table.getCreatedTime());
            resourceVersion.setResourcePath(table.getTableName());
            EntityHelper.assignCreatedElement(resourceVersion);
            return resourceVersion;
        }).collect(Collectors.toList());
        lcdpResourceVersionService.getDao().insert(insertVersionList);

        List<LcdpTableBean> insertTableList = new ArrayList<>();

        List<LcdpTableFieldBean> insertFieldList = new ArrayList<>();

        List<LcdpTableIndexBean> insertIndexList = new ArrayList<>();
        //对比生成表操作bean并校验
        for (String tableName : tableNameList) {

            LcdpTableDTO importTable = JsonUtils.parse(fileMap.get(tableName), LcdpTableDTO.class);

            //PostgreSQL数据库默认值出现类型转换，导致字段长度超长，例如：'1'::character varying
            importTable.getFieldList().stream().forEach(e -> {
                if (e.getDefaultValue() != null && e.getDefaultValue().indexOf("::") > 0) {
                    e.setDefaultValue(e.getDefaultValue().split("::")[0]);
                }
            });

            boolean isExist = currentLcdpTableDTOS.stream().anyMatch(table -> StringUtils.equals(table.getTableName(), tableName));

            if (isExist) {
                //更新表
                modifyTableData(currentLcdpTableDTOS, table2VersionMap, insertTableList, insertFieldList, insertIndexList, tableName, importTable);
            } else {
                //新增
                addImportTableData(insertTableList, insertFieldList, insertIndexList, importTable);
            }
        }
        lcdpTableService.getDao().insert(insertTableList);
        lcdpResourceVersionService.insertByTableList(insertTableList, submitLog);
        lcdpTableIndexService.getDao().insert(insertIndexList);
        lcdpTableFieldService.getDao().insert(insertFieldList);
        LcdpSubmitLogBean querySubmitLog = submitLogService.selectByIdIfPresent(autoSubmitLog.getId());
        if (!unCommitTableList.isEmpty() && querySubmitLog == null) {
            submitLogService.getDao().insert(autoSubmitLog);
        }
        return insertTableList;
    }

    private List<LcdpViewBean> importViewData
            (List<String> viewNameList, Map<String, String> fileMap, LcdpSubmitLogBean submitLog, LcdpSubmitLogBean
                    autoSubmitLog) {

        List<LcdpViewBean> physicalViewInfoList = lcdpDatabaseService.selectPhysicalViewInfoList(viewNameList);

        List<LcdpViewBean> viewVersionList = lcdpViewService.selectListByFilter(SearchFilter.instance().match("VIEWNAME", viewNameList).filter(MatchPattern.OCISEQ));

        Map<String, Optional<LcdpViewBean>> view2VersionMap = viewVersionList.stream().collect(Collectors.groupingBy(LcdpViewBean::getViewName, Collectors.maxBy(Comparator.comparing(LcdpViewBean::getVersion))));

        //未提交的表解锁并自动假提交（不生效只修改版本信息）
        List<LcdpViewBean> unCommitViewList = viewVersionList.stream().filter(view -> LcdpConstant.SUBMIT_FLAG_NO.equals(view.getSubmitFlag())).collect(Collectors.toList());
        List<String> unCommitViewNameList = unCommitViewList.stream().map(LcdpViewBean::getTableName).collect(Collectors.toList());
        lcdpResourceLockService.unLock(unCommitViewNameList);

        List<LcdpResourceVersionBean> insertVersionList = unCommitViewList.stream().map(view -> {
            LcdpResourceVersionBean resourceVersion = new LcdpResourceVersionBean();
            resourceVersion.setId(ApplicationContextHelper.getNextIdentity());
            resourceVersion.setResourceId(view.getViewName());
            resourceVersion.setResourceName(view.getViewName());
            resourceVersion.setLogId(autoSubmitLog.getId());
            resourceVersion.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_TABLE);
            resourceVersion.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);
            view.setSubmitFlag(LcdpConstant.EFFECT_FLAG_YES);
            resourceVersion.setVersion(view.getVersion());
            resourceVersion.setCommit(autoSubmitLog.getCommit());
            resourceVersion.setEditTime(view.getCreatedTime());
            resourceVersion.setResourcePath(view.getViewName());
            EntityHelper.assignCreatedElement(resourceVersion);
            return resourceVersion;
        }).collect(Collectors.toList());
        lcdpResourceVersionService.getDao().insert(insertVersionList);
        lcdpViewService.updateIfChanged(unCommitViewList);


        List<LcdpViewBean> viewList = new ArrayList<>();
        List<LcdpResourceVersionBean> versionList = new ArrayList<>();
        viewNameList.forEach(viewName -> {
            LcdpViewBean matchPhysicalView = physicalViewInfoList.stream().filter(physicalView -> StringUtils.equalsIgnoreCase(viewName, physicalView.getViewName())).findFirst().orElse(null);

            LcdpViewBean matchSubmitView = JsonUtils.parse(fileMap.get(viewName), LcdpViewBean.class);

            if (ObjectUtils.isEmpty(matchSubmitView)) {
                return;
            }

            //清除TableContext缓存
            RedisHelper.evict(DatabaseManager.TABLE_CONTEXT_CACHE_NAME, viewName);
            RedisHelper.evict(DatabaseManager.ENTITY_CONTEXT_CACHE_NAME, viewName);

            if (ObjectUtils.isEmpty(matchPhysicalView)) {
                lcdpDatabaseService.createPhysicalView(matchSubmitView);
            } else {
                lcdpDatabaseService.alterPhysicalView(matchSubmitView);
            }
            matchSubmitView.setId(ApplicationContextHelper.getNextIdentity());
            matchSubmitView.setSubmitFlag(LcdpConstant.EFFECT_FLAG_YES);
            matchSubmitView.setVersion(ObjectUtils.isEmpty(view2VersionMap.get(viewName)) ? 2L : view2VersionMap.get(viewName).get().getVersion() + 1);
            viewList.add(matchSubmitView);

            LcdpResourceVersionBean resourceVersion = new LcdpResourceVersionBean();
            resourceVersion.setId(ApplicationContextHelper.getNextIdentity());
            resourceVersion.setLogId(submitLog.getId());
            resourceVersion.setResourceId(matchSubmitView.getViewName());
            resourceVersion.setResourceName(matchSubmitView.getViewName());
            resourceVersion.setResourcePath(matchSubmitView.getViewName());
            resourceVersion.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_DB_VIEW);
            resourceVersion.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
            resourceVersion.setVersion(matchSubmitView.getVersion());
            resourceVersion.setCommit(submitLog.getCommit());
            EntityHelper.assignCreatedElement(resourceVersion);
            resourceVersion.setEditTime(matchSubmitView.getCreatedTime());
            versionList.add(resourceVersion);
        });


        lcdpResourceVersionService.getDao().insert(versionList);
        lcdpViewService.getDao().insert(viewList);
        LcdpSubmitLogBean querySubmitLog = submitLogService.selectByIdIfPresent(autoSubmitLog.getId());
        if (!unCommitViewNameList.isEmpty() && querySubmitLog == null) {
            submitLogService.getDao().insert(autoSubmitLog);
        }
        return viewList;
    }

    private void addImportTableData(List<LcdpTableBean> insertTableList, List<LcdpTableFieldBean> insertFieldList,
                                    List<LcdpTableIndexBean> insertIndexList, LcdpTableDTO importTable) {
        LcdpTableBean table = new LcdpTableBean();
        table.setId(ApplicationContextHelper.getNextIdentity());
        table.setTableName(importTable.getTableName());
        table.setTableDesc(importTable.getTableDesc());
        table.setMasterTableName(importTable.getMasterTableName());
        table.setReferColumn(importTable.getReferColumn());

        List<LcdpTableFieldBean> fields = importTable.getFieldList().stream().map(dto -> {
            LcdpTableFieldBean field = new LcdpTableFieldBean();
            BeanUtils.copyProperties(dto, field);
            field.setId(ApplicationContextHelper.getNextIdentity());
            field.setTableId(table.getId());
            field.setFieldOperationType(LcdpConstant.FIELD_INDEX_OPS_ADD);
            EntityHelper.assignCreatedElement(field);
            return field;
        }).collect(Collectors.toList());

        insertFieldList.addAll(fields);

        List<LcdpTableIndexBean> indexes = importTable.getIndexList().stream().map(dto -> {
            LcdpTableIndexBean index = new LcdpTableIndexBean();
            BeanUtils.copyProperties(dto, index);
            index.setId(ApplicationContextHelper.getNextIdentity());
            index.setTableId(table.getId());
            index.setIndexOperationType(LcdpConstant.FIELD_INDEX_OPS_ADD);
            if (StringUtils.equals(dialect.getDatabase(), "oracle")) {
                index.setIndexName(generateValidIndexName(index.getIndexName()));
            }
            EntityHelper.assignCreatedElement(index);
            return index;
        }).collect(Collectors.toList());

        if (DialectRepository.isDameng() && !indexes.isEmpty()) {
            String regex = "^INDEX\\d+$";
            indexes = indexes.stream()
                    .filter(index -> !index.getIndexName().toUpperCase().matches("^INDEX\\d+$"))
                    .collect(Collectors.toList());
        }
        insertIndexList.addAll(indexes);

        String sql = lcdpTableService.createPhysicalTable(table, fields, indexes);
        table.setSql(sql);
        table.setVersion(1L);
        table.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_YES);
        EntityHelper.assignCreatedElement(table);
        insertTableList.add(table);
    }

    private void modifyTableData(List<LcdpTableDTO> currentLcdpTableDTOS, Map<String, Optional<LcdpTableBean>> table2VersionMap,
                                 List<LcdpTableBean> insertTableList, List<LcdpTableFieldBean> insertFieldList, List<LcdpTableIndexBean> insertIndexList,
                                 String tableName, LcdpTableDTO importTable) {
        LcdpTableDTO currentTable = currentLcdpTableDTOS.stream().filter(currentLcdpTableDTO -> StringUtils.equals(tableName, currentLcdpTableDTO.getTableName())).findAny().get();

        //分析出字段操作
        List<LcdpTableFieldBean> analyzedFieldList = LcdpTableUtils.analyzeFieldOps(importTable.getFieldList(), currentTable.getFieldList());

        //分析出索引操作
        List<LcdpTableIndexBean> analyzedIndexList = LcdpTableUtils.analyzeIndexOps(importTable.getIndexList(), currentTable.getIndexList());

        LcdpTableBean table = new LcdpTableBean();
        table.setId(ApplicationContextHelper.getNextIdentity());
        table.setTableName(currentTable.getTableName());
        table.setTableDesc(importTable.getTableDesc());
        table.setMasterTableName(importTable.getMasterTableName());
        table.setReferColumn(importTable.getReferColumn());

        analyzedFieldList.forEach(field -> {
            field.setId(ApplicationContextHelper.getNextIdentity());
            field.setTableId(table.getId());
        });
        insertFieldList.addAll(analyzedFieldList);

        analyzedIndexList.forEach(index -> {
            if (StringUtils.equals(dialect.getDatabase(), "oracle")) {
                index.setIndexName(generateValidIndexName(index.getIndexName()));
            }
            index.setId(ApplicationContextHelper.getNextIdentity());
            index.setTableId(table.getId());
        });
        insertIndexList.addAll(analyzedIndexList);

        //清除TableContext缓存
        RedisHelper.evict(DatabaseManager.TABLE_CONTEXT_CACHE_NAME, table.getTableName());
        RedisHelper.evict(DatabaseManager.ENTITY_CONTEXT_CACHE_NAME, table.getTableName());
        //达梦库不处理默认索引 比如index3345633 这种索引
        if (DialectRepository.isDameng() && !analyzedIndexList.isEmpty()) {
            String regex = "^INDEX\\d+$";
            analyzedIndexList = analyzedIndexList.stream()
                    .filter(index -> !index.getIndexName().toUpperCase().matches("^INDEX\\d+$"))
                    .collect(Collectors.toList());
        }
        lcdpTableService.alterPhysicalTable(currentTable, table, analyzedFieldList, analyzedIndexList);

        String sql = lcdpTableService.generateCreateSql(importTable);

        table.setSql(sql);
        LcdpTableBean exitTable = null == table2VersionMap.get(table.getTableName()) ? null : table2VersionMap.get(table.getTableName()).get();
        table.setVersion(ObjectUtils.isEmpty(exitTable) ? 2L : exitTable.getVersion() + 1);
        table.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_YES);
        EntityHelper.assignCreatedElement(table);
        insertTableList.add(table);
    }

    public List<LcdpResourceBean> importResourceData(List<LcdpResourceBean> resourceList, Map<Long, List<LcdpModulePageCompBean>> resourceId2PageCompMap,
                                                     Map<Long, List<LcdpModulePageI18nBean>> resourceId2PageI18nMap, Map<Long, List<LcdpPageI18nCodeBean>> resourceId2PageDependentI18nMap,
                                                     LcdpSubmitLogBean submitLog, LcdpSubmitLogBean autoSubmitLog, StringBuilder importJavaRecord) {
        List<LcdpResourceBean> importDataList = new ArrayList<>();

        //拿到资源脚本页面等信息信息
        List<LcdpResourceBean> scriptList = resourceList.stream().filter(resource -> LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(resource.getResourceCategory())
                && LcdpConstant.RESOURCE_DELETED_NO.equals(resource.getDeleteFlag())).collect(Collectors.toList());
        //从脚本信息里面获取到路径  用于之后的资源比对
        List<String> pathList = scriptList.stream().map(LcdpResourceBean::getPath).collect(Collectors.toList());

        //根据路径查询库中是否有数据
        List<LcdpResourceBean> currentResourceList = getDao().selectListByOneColumnValues(pathList, "PATH",
                Arrays.asList("ID", "PARENTID", "PATH", "RESOURCENAME", "DELETEFLAG", "EFFECTVERSION", "RESOURCEDESC", "RESOURCECATEGORY"));
        currentResourceList = currentResourceList.stream().filter(resource -> StringUtils.equals(resource.getDeleteFlag(), LcdpConstant.RESOURCE_DELETED_NO)).collect(Collectors.toList());

        Map<String, LcdpResourceBean> path2ResourceMap = currentResourceList.stream().collect(Collectors.toMap(LcdpResourceBean::getPath, Function.identity(), (v1, v2) -> v2));

        //库中有数据需要重写的数据
        List<LcdpResourceBean> overwriteList = new ArrayList<>();

        //库中没有需要添加的数据
        List<LcdpResourceBean> needAddList = new ArrayList<>();

        for (LcdpResourceBean resource : scriptList) {
            if (path2ResourceMap.get(resource.getPath()) != null) {
                overwriteList.add(resource);
            } else {
                needAddList.add(resource);
            }
        }

        //处理需要覆盖的数据
        if (!overwriteList.isEmpty()) {

            List<LcdpResourceBean> overwritedResourceList = dealOverwriteResourceData(resourceId2PageCompMap, resourceId2PageI18nMap, resourceId2PageDependentI18nMap, currentResourceList, overwriteList, submitLog, autoSubmitLog, importJavaRecord);
            importDataList.addAll(overwritedResourceList.stream().filter(resource -> LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(resource.getResourceCategory())).collect(Collectors.toList()));
            List<LcdpResourceBean> mapperList = overwritedResourceList.stream().filter(resource -> LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(resource.getResourceCategory())).collect(Collectors.toList());
            //如果是mapper加载到mybatis中
            for (LcdpResourceBean resource : mapperList) {
                if (LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(resource.getResourceCategory())) {
                    //加载生成mapper
                    LcdpMapperUtils.loadMapper(resource.getPath(), true, resource.getContent());
                    //  LcdpMapperUtils.syncMapper(resource.getPath(), true, resource.getContent());
                    //卸载开发mapper
                    LcdpMapperUtils.unloadMapper(resource.getPath(), false);
                }
            }
        }

        // 处理库中没有需要新增的数据
        if (!needAddList.isEmpty()) {
            List<LcdpResourceBean> addResourceList = dealNewAddResourceData(resourceList, resourceId2PageCompMap, resourceId2PageI18nMap, resourceId2PageDependentI18nMap, needAddList, submitLog, importJavaRecord);

            importDataList.addAll(addResourceList.stream().filter(resource -> LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(resource.getResourceCategory())).collect(Collectors.toList()));

            List<LcdpResourceBean> mapperList = addResourceList.stream().filter(resource -> LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(resource.getResourceCategory())).collect(Collectors.toList());
            for (LcdpResourceBean resource : mapperList) {
                //加载生成mapper
                LcdpMapperUtils.loadMapper(resource.getPath(), true, resource.getContent());
                //  LcdpMapperUtils.syncMapper(resource.getPath(), true, resource.getContent());
            }
        }
        List<LcdpResourceBean> deleteResourceList = resourceList.stream().filter(r -> LcdpConstant.RESOURCE_DELETED_YES.equals(r.getDeleteFlag())).collect(Collectors.toList());
        if (!deleteResourceList.isEmpty()) {
            List<String> deletePathList = deleteResourceList.stream().map(LcdpResourceBean::getPath)
                    .filter(p -> !StringUtils.isBlank(p))
                    .collect(Collectors.toList());

            List<LcdpResourceBean> toDeleteResourceList = selectListByFilter(SearchFilter.instance()
                    .match("PATH", deletePathList).filter(MatchPattern.OR)
                    .match("deleteFlag", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));

            toDeleteResourceList.forEach(r -> {
                r.setDeleteFlag(LcdpConstant.RESOURCE_DELETED_YES);
                r.setDeleteTime(LocalDateTime.now());
                r.setDeletedById(LocalContextHelper.getLoginUserId());
            });
            getDao().update(toDeleteResourceList, "deleteFlag", "deleteTime", "deletedById");

            importDataList.addAll(toDeleteResourceList);

            List<Long> resourceIdList = toDeleteResourceList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
            List<LcdpResourceHistoryBean> toDeleteHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("resourceId", resourceIdList).filter(MatchPattern.OR));

            toDeleteHistoryList.forEach(r -> {
                r.setDeleteFlag(LcdpConstant.RESOURCE_DELETED_YES);
                r.setDeleteTime(submitLog.getCreatedTime());
                r.setDeletedById(LocalContextHelper.getLoginUserId());
            });
            lcdpResourceHistoryService.getDao().update(toDeleteHistoryList, "deleteFlag", "deleteTime", "deletedById");
            List<Long> javaIdList = toDeleteResourceList.stream().filter(r -> LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(r.getResourceCategory())).map(LcdpResourceBean::getId).collect(Collectors.toList());
            List<LcdpServerScriptMethodBean> deleteServerScriptMethod = serverScriptMethodService.selectListByFilter(SearchFilter.instance().match("serverScriptId", javaIdList).filter(MatchPattern.OR));
            serverScriptMethodService.getDao().deleteBy(deleteServerScriptMethod);
            lcdpResourceVersionService.insertByDeleteResourceList(toDeleteHistoryList, submitLog);

            List<LcdpResourceVersionBean> versionList = lcdpResourceVersionService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", javaIdList.stream().map(e -> String.valueOf(e)).collect(Collectors.toList())).filter(MatchPattern.OR));
            versionList.forEach(version -> {
                version.setResourceDeleteFlag(LcdpConstant.RESOURCE_DELETED_YES);
            });

            lcdpResourceVersionService.getDao().update(versionList, "RESOURCEDELETEFLAG");

        }

        return importDataList;
    }

    private List<LcdpResourceBean> dealNewAddResourceData(List<LcdpResourceBean> resourceList, Map<Long, List<LcdpModulePageCompBean>> resourceId2PageCompMap,
                                                          Map<Long, List<LcdpModulePageI18nBean>> resourceId2PageI18nMap, Map<Long, List<LcdpPageI18nCodeBean>> resourceId2PageDependentI18nMap,
                                                          List<LcdpResourceBean> needAddList, LcdpSubmitLogBean submitLog, StringBuilder importJavaRecord) {
    /*
    先把数据的父ID恢复到正常的数据：去掉文件夹这一层
     */
        List<LcdpResourceBean> actualImportResourceList = needAddList.stream().collect(Collectors.toList());
            /*
            处理数据 1.先查有没有分类 如果没有分类 则要从分类开始构建 分类 模块 随后插入脚本
            如果有分类则再查有没有对应的模块：如果有模块则插入数据  如果没有则构建模块 插入脚本
             */
        //准备好三个数据集合： 资源表集合  资源历史表集合  页面组件表集合
        List<LcdpResourceBean> insertResourceList = new ArrayList<>();
        List<LcdpResourceHistoryBean> insertResourceHistoryList = new ArrayList<>();
        List<LcdpModulePageCompBean> insertMoudlePageCompList = new ArrayList<>();
        List<LcdpModulePageI18nBean> insertPageI18nList = new ArrayList<>();
        List<LcdpPageI18nCodeBean> insertPageDependentI18nList = new ArrayList<>();
        //根据脚本路径获取分类名称  脚本路径 分类.模块.文件夹.文件
        List<String> categoryNameList = actualImportResourceList.stream().map(resource -> {
            return resource.getPath().split("\\.")[0];
        }).collect(Collectors.toList());
        //提前准备要构建分类的集合
        List<String> prepareAddCategoryNameList = new ArrayList<>();
        prepareAddCategoryNameList.addAll(categoryNameList);
        //查询库中是否有分类
        List<LcdpResourceBean> categoryList = selectListByFilter(SearchFilter.instance().match("RESOURCENAME", categoryNameList).filter(MatchPattern.OR).match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_CATEGORY).filter(MatchPattern.SEQ).match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));
        List<String> currentCategoryNameList = categoryList.stream().map(LcdpResourceBean::getResourceName).collect(Collectors.toList());

        prepareAddCategoryNameList.removeAll(currentCategoryNameList);

        if (!prepareAddCategoryNameList.isEmpty()) {
            //如果没有对应的分类 开始构建分类 模块
            //准备ID映射集合 存储新的ID 和 导入数据的ID  导入数据ID用作资源查询
            Map<Long, Long> categoryIdMap = new HashMap<>();
            Map<Long, Long> moduleIdMap = new HashMap<>();
            //构建分类数据
            List<LcdpResourceBean> prepareCategoryList = resourceList.stream().filter(resource -> prepareAddCategoryNameList.contains(resource.getResourceName())).collect(Collectors.toList());
            List<Long> categoryIdList = prepareCategoryList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
            prepareCategoryList.forEach(category -> {
                Long id = ApplicationContextHelper.getNextIdentity();
                categoryIdMap.put(category.getId(), id);
                category.setId(id);
                EntityHelper.assignCreatedElement(category);
            });
            insertResourceList.addAll(prepareCategoryList);
            //构建模块数据
            List<LcdpResourceBean> prepareModuleList = resourceList.stream().filter(resource -> categoryIdList.contains(resource.getParentId())).collect(Collectors.toList());
            List<Long> moduleIdList = prepareModuleList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
            prepareModuleList.forEach(module -> {
                Long id = ApplicationContextHelper.getNextIdentity();
                moduleIdMap.put(module.getId(), id);
                module.setId(id);
                module.setParentId(categoryIdMap.get(module.getParentId()));
                EntityHelper.assignCreatedElement(module);
            });
            insertResourceList.addAll(prepareModuleList);

            List<LcdpResourceBean> prepareScriptList = actualImportResourceList.stream().filter(resource -> moduleIdList.contains(resource.getParentId())).collect(Collectors.toList());
            //构建脚本数据
            prepareScriptList.forEach(script -> {
                Long resourceId = script.getId();
                Long id = ApplicationContextHelper.getNextIdentity();
                script.setId(id);
                script.setParentId(moduleIdMap.get(script.getParentId()));
                script.setEffectVersion(1L);
                EntityHelper.assignCreatedElement(script);

                //处理历史表及组件数据
                dealHistoryAndPageCompData(script, insertResourceHistoryList, resourceId2PageCompMap, resourceId2PageI18nMap, resourceId2PageDependentI18nMap, resourceId, insertMoudlePageCompList, insertPageI18nList, insertPageDependentI18nList);
            });
            insertResourceList.addAll(prepareScriptList);

        }

        //库中分类名称和映射
        Map<String, Long> categoryNameIdMap = categoryList.stream().collect(Collectors.toMap(LcdpResourceBean::getResourceName, LcdpResourceBean::getId));
        //导入数据的模块和分类做映射 为了处理库中有分类的情况 对模块的判断
        List<LcdpResourceBean> tempResourceList = actualImportResourceList.stream().filter(resource -> currentCategoryNameList.contains(resource.getPath().split("\\.")[0])).collect(Collectors.toList());
        Map<String, String> module2CategoryMap = new HashMap<>();
        for (LcdpResourceBean resource : tempResourceList) {
            String[] pathArray = resource.getPath().split("\\.");
            String categoryName = pathArray[0];
            String moduleName = pathArray[1];
            module2CategoryMap.put(moduleName, categoryName);
        }

        Set<String> moduleNameSet = module2CategoryMap.keySet();
        List<String> moduleNameList = moduleNameSet.stream().collect(Collectors.toList());

        //参照是否有分类来进行模块数据的一个查询 判断是否有模块
        List<String> prepareAddModuleNameList = new ArrayList<>();
        prepareAddModuleNameList.addAll(moduleNameList);
        List<LcdpResourceBean> moduleList = selectListByFilter(SearchFilter.instance()
                .match("RESOURCENAME", moduleNameList).filter(MatchPattern.OR)
                .match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_MODULE).filter(MatchPattern.SEQ)
                .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));
        List<String> currentmoduleNameList = moduleList.stream().map(LcdpResourceBean::getResourceName).collect(Collectors.toList());
        prepareAddModuleNameList.removeAll(currentmoduleNameList);

        if (!prepareAddModuleNameList.isEmpty()) {
            //如果有库中没有的模块则需要构建模块并插入脚本数据
            Map<Long, Long> ModuleIdMap = new HashMap<>();
            List<LcdpResourceBean> prepareModuleList = resourceList.stream().filter(resource -> prepareAddModuleNameList.contains(resource.getResourceName())).collect(Collectors.toList());
            List<Long> moduleIdList = prepareModuleList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
            //处理模块数据
            prepareModuleList.forEach(module -> {
                Long id = ApplicationContextHelper.getNextIdentity();
                ModuleIdMap.put(module.getId(), id);
                module.setId(id);
                module.setParentId(categoryNameIdMap.get(module2CategoryMap.get(module.getResourceName())));
                EntityHelper.assignCreatedElement(module);
            });
            List<LcdpResourceBean> prepareScriptList = actualImportResourceList.stream().filter(resource -> moduleIdList.contains(resource.getParentId())).collect(Collectors.toList());

            insertResourceList.addAll(prepareModuleList);

            List<String> prepareJavaScriptPathList = prepareScriptList.stream().filter(e -> LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(e.getResourceCategory())).map(e -> e.getPath()).collect(Collectors.toList());

            List<LcdpResourceBean> existDeleteResourceList = selectListByFilter(SearchFilter.instance()
                    .match("PATH", prepareJavaScriptPathList).filter(MatchPattern.OR)
                    .match("DELETEFLAG", Constant.YES).filter(MatchPattern.EQ), Order.desc("ID"));

            prepareScriptList.forEach(script -> {
                Long resourceId = script.getId();
                Long id = ApplicationContextHelper.getNextIdentity();
                script.setId(id);
                script.setParentId(ModuleIdMap.get(script.getParentId()));
                script.setEffectVersion(1l);
                EntityHelper.assignCreatedElement(script);

                //处理历史表及组件数据
                dealHistoryAndPageCompData(script, insertResourceHistoryList, resourceId2PageCompMap, resourceId2PageI18nMap, resourceId2PageDependentI18nMap, resourceId, insertMoudlePageCompList, insertPageI18nList, insertPageDependentI18nList);
            });
            insertResourceList.addAll(prepareScriptList);
        }

        actualImportResourceList.removeIf(resource -> prepareAddCategoryNameList.contains(resource.getPath().split("\\.")[0]));
        actualImportResourceList.removeIf(resource -> prepareAddModuleNameList.contains(resource.getPath().split("\\.")[1]));
        if (!actualImportResourceList.isEmpty()) {
            // 给module赋值Path
            List<Long> categoryIdList = moduleList.stream().map(e -> e.getParentId()).collect(Collectors.toList());

            Map<Long, String> categoryId2NameMap = selectListByIds(categoryIdList).stream().collect(Collectors.toMap(LcdpResourceBean::getId, LcdpResourceBean::getResourceName));

            //分类和模块都有  我们只需要插入脚本数据即可
            Map<String, Long> modulePath2IdMap = moduleList.stream().collect(Collectors.toMap(e -> {
                return categoryId2NameMap.get(e.getParentId()) + "." + e.getResourceName();
            }, LcdpResourceBean::getId));
            actualImportResourceList.forEach(script -> {
                Long resourceId = script.getId();
                script.setId(ApplicationContextHelper.getNextIdentity());
                script.setEffectVersion(1L);

                String path = script.getPath();
                // 截取分类.模块
                String modulePath = path.substring(0, path.indexOf(".", path.indexOf(".") + 1));


                script.setParentId(modulePath2IdMap.get(modulePath));
                //处理历史表及组件数据
                dealHistoryAndPageCompData(script, insertResourceHistoryList, resourceId2PageCompMap, resourceId2PageI18nMap, resourceId2PageDependentI18nMap, resourceId, insertMoudlePageCompList, insertPageI18nList, insertPageDependentI18nList);
            });
            insertResourceList.addAll(actualImportResourceList);
        }

        insertResourceList.stream().filter(resource -> LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(resource.getResourceCategory())).forEach(resource -> {
            //脚本导入，脚本类替换
            // 为：类名+v(生效版本)
            String content = resource.getClassContent();
            if (!StringUtils.isEmpty(content)) {
                String classContent = LcdpJavaCodeResolverUtils.getClassContent(content,
                        resource.getResourceName(),
                        resource.getEffectVersion(), null,
                        resource.getVersionOffset());

                resource.setClassContent(classContent);
                resource.setClassName(ClassManager.getClassFullName(classContent));
            }
        });

        getDao().insert(insertResourceList);
        lcdpResourceHistoryService.getDao().insert(insertResourceHistoryList);
        lcdpModulePageCompService.getDao().fastInsert(insertMoudlePageCompList);
        lcdpModulePageI18nService.getDao().insert(insertPageI18nList);
        lcdpPageI18nCodeService.getDao().insert(insertPageDependentI18nList);
        lcdpResourceVersionService.insertByHistoryResourceList(insertResourceHistoryList, submitLog);

        insertResourceList.forEach(r -> {
            updatePath(r);
            updateVersionOffset(r);
        });

        getDao().update(insertResourceList, "PATH", "MODULEID", "CATEGORYID", "VERSIONOFFSET");

        List<LcdpResourceBean> javaHistoryList = insertResourceList.stream().filter(resource -> LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(resource.getResourceCategory())).collect(Collectors.toList());
        List<JavaSourceConfig> sourceConfigList = new ArrayList<>();
        javaHistoryList.forEach(java -> {
            String classFullName = ClassManager.getClassFullName(java.getClassContent());
            String hintName = java.getResourceName();
            String sourceCode = java.getClassContent();

            sourceConfigList.add(new JavaSourceConfig(classFullName, hintName, sourceCode, java));
        });
        insertServerScriptMethodAndRegister(sourceConfigList, importJavaRecord);

        return insertResourceList;

    }

    private void insertServerScriptMethodAndRegister(List<JavaSourceConfig> sourceConfigList, StringBuilder importJavaRecord) {
        if (sourceConfigList.isEmpty()) {
            return;
        }
        Map<String, String> sourceMap = new HashMap<>();

        sourceConfigList.stream().filter(s -> !StringUtils.isEmpty(s.getClassFullName()) && !ClassManager.isClassPresent(s.getClassFullName())).forEach(s -> sourceMap.put(s.getClassFullName(), s.getSourceCode()));

        if (sourceMap.isEmpty()) {
            return;
        }


        LoadMultipleResult result = DynamicClassLoader.getInstance().loadSourceCode(sourceMap);

        // 有异常
        if (!CollectionUtils.isEmpty(result.getErrorMessageMap())) {
            result.getErrorMessageMap().forEach((k, v) -> {

                String classFullName = StringUtils.replace(k, "/", ".");

                JavaSourceConfig sourceConfig = sourceConfigList.stream().filter(t -> classFullName.equals(t.getClassFullName())).findAny().get();
                importJavaRecord.append("脚本加载异常：类名为" + sourceConfig.getHintName() + "，异常日志如下：" + v + "/r/n");

                log.error("============>脚本加载异常：类名为" + sourceConfig.getClassFullName() + "，异常日志如下：" + v);

                sourceConfigList.removeIf(t -> StringUtils.isEmpty(t.getClassFullName()) || t.getClassFullName().equals(sourceConfig.getClassFullName()));

            });

            insertServerScriptMethodAndRegister(sourceConfigList, importJavaRecord);

            return;
        }

        List<LcdpServerScriptMethodBean> serverScriptMethodList = new ArrayList<>();

        List<Long> resourceIdList = new ArrayList<>();
        Map<Long, Method[]> methodMap = sourceConfigList.stream().filter(his -> StringUtils.isNotBlank(ClassManager.getClassFullName(his.getSourceCode()))).collect(Collectors.toMap(JavaSourceConfig::getResourceId,
                (his -> LcdpJavaCodeResolverUtils.loadSourceCode(his.getSourceCode()) == null ? new Method[0] : LcdpJavaCodeResolverUtils.loadSourceCode(his.getSourceCode()).getMethods())));
        for (JavaSourceConfig java : sourceConfigList) {
            if (methodMap.get(java.getResourceId()) == null || methodMap.get(java.getResourceId()).length == 0) {
                continue;
            }
            List<Method> methodList = Arrays.stream(methodMap.get(java.getResourceId())).filter(method -> method.getAnnotation(Mapping.class) != null).collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Method::getName))), ArrayList::new));
            for (Method method : methodList) {
                LcdpServerScriptMethodBean serverScriptMethod = new LcdpServerScriptMethodBean();
                Mapping annotation = method.getAnnotation(Mapping.class);
                String methodName = method.getName();
                String methodPath = java.getPath() + "." + methodName;
                serverScriptMethod.setId(ApplicationContextHelper.getNextIdentity());
                serverScriptMethod.setServerScriptId(java.getResourceId());
                serverScriptMethod.setMethodName(methodName);
                serverScriptMethod.setMethodPath(methodPath);
                serverScriptMethod.setMethodDesc(annotation.value());
                serverScriptMethod.setMappingType(annotation.type().name());
                serverScriptMethod.setMethodFlag("normal");
                serverScriptMethod.setMethodCreatedById(LocalContextHelper.getLoginUserId());
                resourceIdList.add(java.getResourceId());
                if (Page.class.isAssignableFrom(method.getReturnType()) && method.getParameterCount() == 1 && RestJsonWrapperBean.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    serverScriptMethod.setTodoFlag(Constant.YES);
                } else {
                    serverScriptMethod.setTodoFlag(Constant.NO);
                }
                serverScriptMethodList.add(serverScriptMethod);
            }
        }
        result.getClassMap().forEach((k, c) -> {
            JavaSourceConfig sourceConfig = sourceConfigList.stream().filter(t -> k.equals(t.getClassFullName())).findAny().orElse(null);
            if (sourceConfig == null) {
                return;
            }

            //为了避免提交时使用未提交的切面，通过设置变量做处理
            THREAD_LOCAL.set(REGISTER_PRD);
            LcdpJavaCodeResolverUtils.registerBean(c);
            THREAD_LOCAL.remove();

            //    stringRedisTemplate.convertAndSend(LcdpMapperUtils.LCDP_SYNC_SCRIPT_TOPIC, sourceConfig.getSourceCode());
            LcdpResourceBean resource = sourceConfig.getResource();

            String beanName = LcdpJavaCodeResolverUtils.getBeanName(c);

            if (ServerScriptType.aspect.name().equals(resource.getScriptType())) {
                //更新切面缓存
                if (committedAspectCache.containsKey(sourceConfig.getResourceId())) {
                    LcdpResourceBean expiredAspect = committedAspectCache.get(sourceConfig.getResourceId());
                    committedAspectClassCache.remove(expiredAspect.getClassName());
                }
                committedAspectClassCache.put(beanName, c);
                committedAspectCache.put(sourceConfig.getResourceId(), sourceConfig.getResource());
            } else if (ServerScriptType.filter.name().equals(sourceConfig.resource.getScriptType())) {
                //更新过滤器缓存
                if (committedFilterCache.containsKey(sourceConfig.getResourceId())) {
                    LcdpResourceBean expiredFilter = committedFilterCache.get(sourceConfig.getResourceId());
                    committedFilterNameCache.remove(expiredFilter.getClassName());
                }
                committedFilterNameCache.add(beanName);
                committedFilterCache.put(sourceConfig.getResourceId(), sourceConfig.getResource());
            }

            if (StringUtils.equals(ServerScriptType.eventListener.name(), resource.getScriptType())) {
                SimpleApplicationEventMulticaster applicationEventMulticaster = new SimpleApplicationEventMulticaster();
                applicationEventMulticaster.addApplicationListener(SpringUtils.getBean(beanName));
            }
        });

        resourceIdList = resourceIdList.stream().distinct().collect(Collectors.toList());
        int batchSize = dialect.getMaxParamQty();
        List<LcdpServerScriptMethodBean> deleteServerScriptMethodList = new ArrayList<>();
        int numBatches = (int) Math.ceil((double) resourceIdList.size() / batchSize);
        for (int i = 0; i < numBatches; i++) {
            int start = i * batchSize;
            int end = Math.min(start + batchSize, resourceIdList.size());
            List<Long> batchIds = resourceIdList.subList(start, end);
            List<LcdpServerScriptMethodBean> methodList = serverScriptMethodService.getDao().selectListByOneColumnValues(batchIds, "SERVERSCRIPTID", Arrays.asList("ID"));
            deleteServerScriptMethodList.addAll(methodList);
        }

        if (!deleteServerScriptMethodList.isEmpty()) {
            List<Long> deleteIdList = deleteServerScriptMethodList.stream().map(LcdpServerScriptMethodBean::getId).collect(Collectors.toList());
            serverScriptMethodService.getDao().deleteByIdList(deleteIdList);
        }

        serverScriptMethodService.getDao().fastInsert(serverScriptMethodList);
    }

    private void dealHistoryAndPageCompData(LcdpResourceBean script, List<LcdpResourceHistoryBean> insertResourceHistoryList,
                                            Map<Long, List<LcdpModulePageCompBean>> resourceId2PageCompMap, Map<Long, List<LcdpModulePageI18nBean>> resourceId2PageI18nMap,
                                            Map<Long, List<LcdpPageI18nCodeBean>> resourceId2PageDependentI18nMap, Long resourceId,
                                            List<LcdpModulePageCompBean> insertMoudlePageCompList, List<LcdpModulePageI18nBean> insertPageI18nList,
                                            List<LcdpPageI18nCodeBean> insertPageDependentI18nList) {
        LcdpResourceHistoryBean resourceHistory = new LcdpResourceHistoryBean();
        BeanUtils.copyProperties(script, resourceHistory, LcdpConstant.COPY_IGNORE_PROPERTIES);
        resourceHistory.setId(ApplicationContextHelper.getNextIdentity());
        resourceHistory.setResourceId(script.getId());
        resourceHistory.setVersion(script.getEffectVersion());
        resourceHistory.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_YES);
        resourceHistory.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
        EntityHelper.assignCreatedElement(resourceHistory);
        insertResourceHistoryList.add(resourceHistory);

        if (LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(script.getResourceCategory())) {
            //处理页面组件数据
            List<LcdpModulePageCompBean> modulePageCompList = resourceId2PageCompMap.get(resourceId);
            List<LcdpModulePageI18nBean> modulePageI18nList = resourceId2PageI18nMap.get(resourceId);
            List<LcdpPageI18nCodeBean> modulePageDependentI18nList = resourceId2PageDependentI18nMap.get(resourceId);
            if (!ObjectUtils.isEmpty(modulePageCompList)) {
                modulePageCompList.forEach(pageComp -> {
                    if (!StringUtils.isEmpty(pageComp.getConfigExtend())) {
                        // 判断pageComp.getConfig()是否已经是完整的JSON字符串
                        if (JsonUtils.isObject(pageComp.getConfig())) {
                            // 如果已经是完整的JSON对象，就不需要拼接
                            pageComp.setConfig(pageComp.getConfig());
                        } else {
                            // 否则进行拼接
                            pageComp.setConfig(pageComp.getConfig() + pageComp.getConfigExtend());
                        }
                    }
                });
                List<LcdpModulePageCompBean> insertPageCompList = generatePageComps(modulePageCompList);
                insertPageCompList.forEach(comp -> {
                    comp.setModulePageId(script.getId());
                    comp.setModulePageHistoryId(resourceHistory.getId());
                });
                insertMoudlePageCompList.addAll(insertPageCompList);
            }

            if (!ObjectUtils.isEmpty(modulePageI18nList)) {
                modulePageI18nList.forEach(i18n -> {
                    i18n.setId(ApplicationContextHelper.getNextIdentity());
                    i18n.setModulePageId(script.getId());
                    i18n.setModulePageHistoryId(resourceHistory.getId());
                    EntityHelper.assignCreatedElement(i18n);
                });
                insertPageI18nList.addAll(modulePageI18nList);
            }

            if (!ObjectUtils.isEmpty(modulePageDependentI18nList)) {
                modulePageDependentI18nList.forEach(dependence -> {
                    dependence.setId(ApplicationContextHelper.getNextIdentity());
                    dependence.setModulePageId(script.getId());
                    dependence.setModulePageHistoryId(resourceHistory.getId());
                    EntityHelper.assignCreatedElement(dependence);
                });
                insertPageDependentI18nList.addAll(modulePageDependentI18nList);
            }
        }
    }

    private List<LcdpResourceBean> dealOverwriteResourceData(Map<Long, List<LcdpModulePageCompBean>> resourceId2PageCompMap,
                                                             Map<Long, List<LcdpModulePageI18nBean>> resourceId2PageI18nMap, Map<Long, List<LcdpPageI18nCodeBean>> resourceId2PageDependentI18nMap,
                                                             List<LcdpResourceBean> currentResourceList, List<LcdpResourceBean> overwriteList, LcdpSubmitLogBean submitLog,
                                                             LcdpSubmitLogBean autoSubmitLog, StringBuilder importJavaRecord) {
        /**
         * 历史表数据如果有未提交的 则进行提交操作  释放锁
         * 提交后 插入导入的数据 对现有版本+1  导入的数据记录到提交表里面
         *
         */
        Map<String, LcdpResourceBean> path2OverwriteResourceMap = overwriteList.stream().collect(Collectors.toMap(LcdpResourceBean::getPath, Function.identity(), (v1, v2) -> v2));
        List<Long> currentResourceIdList = currentResourceList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
        //

        List<LcdpResourceHistoryBean> currentResourceHistoryList = lcdpResourceHistoryService.getDao().selectListByOneColumnValues(currentResourceIdList, "RESOURCEID", Arrays.asList("ID", "RESOURCEID", "VERSION", "SUBMITFLAG", "EFFECTFLAG", "PATH", "CREATEDTIME", "RESOURCENAME", "RESOURCECATEGORY", "RESOURCEDESC"));

        List<LcdpResourceHistoryBean> currentNoSubmitHisList = currentResourceHistoryList.stream().filter(his -> LcdpConstant.SUBMIT_FLAG_NO.equals(his.getSubmitFlag())).collect(Collectors.toList());

        //修改生效状态
        List<LcdpResourceHistoryBean> currentEffectList = currentResourceHistoryList.stream().filter(his -> LcdpConstant.EFFECT_FLAG_YES.equals(his.getEffectFlag())).collect(Collectors.toList());
        currentEffectList.forEach(his -> his.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO));
        lcdpResourceHistoryService.getDao().update(currentEffectList, "EFFECTFLAG");

        //修改提交状态
        currentNoSubmitHisList.forEach(his -> {
            his.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_YES);
        });
        List<Long> importSubmitResourceIdList = currentResourceHistoryList.stream().map(his -> his.getResourceId()).collect(Collectors.toList());


        Map<Long, Optional<LcdpResourceHistoryBean>> resourceId2VersionMap = currentResourceHistoryList.stream().collect(Collectors.groupingBy(LcdpResourceHistoryBean::getResourceId, Collectors.maxBy(Comparator.comparing(LcdpResourceHistoryBean::getVersion))));
        //将历史表的版本写入到主表中  这步可有可无 做一次记录

        currentResourceList.forEach(resource -> {
            if (resourceId2VersionMap.get(resource.getId()) != null) {
                resource.setEffectVersion(resourceId2VersionMap.get(resource.getId()).get().getVersion());
            }
        });
        //区分开 这个是为了 迁入撤销加的判断
        currentResourceList.forEach(resource -> {
            if (importSubmitResourceIdList.contains(resource.getId())) {
                resource.setExt$Item("importSubmit", "1");
                resource.setExt$Item("importSubmitVersion", resource.getEffectVersion().toString());
            }
        });


        //更新数据并释放锁以及插入版本记录表
        getDao().update(currentResourceList, "EFFECTVERSION");
        resourceCheckoutRecordService.removeCheckout(currentResourceList);
        lcdpResourceHistoryService.getDao().update(currentNoSubmitHisList, "SUBMITFLAG");
        lcdpResourceLockService.unLock(currentResourceIdList.stream().map(String::valueOf).collect(Collectors.toList()));
        List<LcdpResourceVersionBean> hisResourceVersionList = currentNoSubmitHisList.stream().map(his -> {
            LcdpResourceVersionBean resourceVersion = new LcdpResourceVersionBean();
            resourceVersion.setId(ApplicationContextHelper.getNextIdentity());
            resourceVersion.setResourceId(his.getResourceId().toString());
            resourceVersion.setVersion(his.getVersion());
            resourceVersion.setLogId(autoSubmitLog.getId());
            resourceVersion.setEditTime(his.getCreatedTime());
            resourceVersion.setSubmitTime(LocalDateTime.now());
            resourceVersion.setResourceName(his.getResourceName());
            resourceVersion.setResourceCategory(his.getResourceCategory());
            resourceVersion.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);
            resourceVersion.setCommit(autoSubmitLog.getCommit());
            resourceVersion.setResourceAction(his.getVersion() == 1 ? LcdpConstant.RESOURCE_STATUS_NEW : LcdpConstant.RESOURCE_SUBMIT_ACTION_UPDATE);
            resourceVersion.setResourcePath(his.getPath().replaceAll("\\.", "/"));
            String[] path = his.getPath().split("\\.");
            resourceVersion.setCategoryName(path[0]);
            resourceVersion.setModuleName(path[1]);
            return resourceVersion;
        }).collect(Collectors.toList());
        List<LcdpResourceVersionBean> updateResourceVersionList = lcdpResourceVersionService.getDao().selectListByOneColumnValues(currentResourceIdList.stream().map(String::valueOf).collect(Collectors.toList()), "RESOURCEID", Arrays.asList("ID"));

        updateResourceVersionList.forEach(version -> {
            version.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);
        });
        lcdpResourceVersionService.getDao().update(updateResourceVersionList, "EFFECTFLAG");
        if (!hisResourceVersionList.isEmpty()) {
            submitLogService.getDao().insert(autoSubmitLog);
        }
        lcdpResourceVersionService.getDao().insert(hisResourceVersionList);

        //处理导入进来的数据
        List<LcdpResourceHistoryBean> insertHistoryList = new ArrayList<>();
        List<LcdpModulePageCompBean> insertPageCompsList = new ArrayList<>();
        List<LcdpModulePageI18nBean> insertPageI18nList = new ArrayList<>();
        List<LcdpPageI18nCodeBean> insertPageDependentI18nList = new ArrayList<>();
        //对数据库中主表数据进行更新 主要是更新生效版本及脚本内容
        for (LcdpResourceBean resource : currentResourceList) {
            LcdpResourceBean overWriteResource = path2OverwriteResourceMap.get(resource.getPath());
            resource.setEffectVersion(resource.getEffectVersion() + 1);

            String content = overWriteResource.getContent();
            resource.setContent(content);
            resource.setResourceDesc(overWriteResource.getResourceDesc());
            if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(resource.getResourceCategory())) {
                if (!StringUtils.isEmpty(overWriteResource.getClassContent())) {
                    //替换脚本类名
                    String classContent = LcdpJavaCodeResolverUtils.getClassContent(overWriteResource.getClassContent(), resource.getResourceName(),
                            resource.getEffectVersion(), null,
                            overWriteResource.getVersionOffset());
                    resource.setClassContent(classContent);
                    resource.setClassName(ClassManager.getClassFullName(classContent));
                }
            }
            //处理历史表数据
            dealHistoryAndPageCompData(resource, insertHistoryList, resourceId2PageCompMap, resourceId2PageI18nMap, resourceId2PageDependentI18nMap, overWriteResource.getId(), insertPageCompsList, insertPageI18nList, insertPageDependentI18nList);
        }

        getDao().update(currentResourceList, "EFFECTVERSION", "CONTENT", "CLASSNAME", "CLASSCONTENT", "RESOURCEDESC");
        lcdpResourceHistoryService.getDao().insert(insertHistoryList);
        lcdpModulePageCompService.getDao().fastInsert(insertPageCompsList);
        lcdpModulePageI18nService.getDao().insert(insertPageI18nList);
        lcdpPageI18nCodeService.getDao().insert(insertPageDependentI18nList);
        lcdpResourceVersionService.insertByHistoryResourceList(insertHistoryList, submitLog);
        List<LcdpResourceBean> javaList = currentResourceList.stream().filter(resource -> LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(resource.getResourceCategory())).collect(Collectors.toList());
        List<JavaSourceConfig> sourceConfigList = new ArrayList<>();
        javaList.forEach(java -> {
            if (StringUtils.isNotEmpty(java.getClassContent())) {
                String classFullName = ClassManager.getClassFullName(java.getClassContent());
                String hintName = java.getResourceName();
                String sourceCode = java.getClassContent();

                sourceConfigList.add(new JavaSourceConfig(classFullName, hintName, sourceCode, java));
            }
        });
        insertServerScriptMethodAndRegister(sourceConfigList, importJavaRecord);
        return currentResourceList;
    }

    public void importSysCode(List<CoreCodeCategoryBean> coreCodeCategoryList, List<CoreCodeBean> codeList,
                              List<CoreAdminSelectConfigBean> coreAdminSelectConfigList) {
        CoreConstantBean queryCoreConstant = coreConstantService.selectByIdIfPresent("LCDP_CODE_NAME_OVERRIDE");
        if (coreCodeCategoryList.isEmpty()) {
            return;
        }
        List<CoreCodeCategoryBean> insertCodeCategoryList = new ArrayList<>();

        List<CoreCodeCategoryBean> existCategoryList = new ArrayList<>();

        List<CoreCodeCategoryBean> notExistCategoryList = new ArrayList<>();

        List<CoreAdminSelectConfigBean> insertAdminSelectConfigList = new ArrayList<>();

        List<CoreCodeBean> insertCodeList = new ArrayList<>();

        List<CoreCodeBean> updateCodeList = new ArrayList<>();
        List<String> categoryIdList = coreCodeCategoryList.stream().map(CoreCodeCategoryBean::getId).collect(Collectors.toList());

        List<CoreCodeCategoryBean> currentCodeCategoryList = coreCodeCategoryService.selectListByIds(categoryIdList);
        Map<String, CoreCodeCategoryBean> currentCategoryMap = currentCodeCategoryList.stream().collect(Collectors.toMap(CoreCodeCategoryBean::getId, Function.identity()));
        coreCodeCategoryList.forEach(cc -> {
            if (currentCategoryMap.get(cc.getId()) != null) {
                existCategoryList.add(currentCategoryMap.get(cc.getId()));
            } else {
                notExistCategoryList.add(cc);
            }
        });
        Map<String, List<CoreCodeBean>> codeMap = codeList.stream().collect(Collectors.groupingBy(CoreCodeBean::getCodeCategoryId));
        if (!notExistCategoryList.isEmpty()) {
            insertCodeCategoryList.addAll(notExistCategoryList);
            notExistCategoryList.forEach(nc -> {
                if (!ObjectUtils.isEmpty(codeMap.get(nc.getId()))) {
                    insertCodeList.addAll(codeMap.get(nc.getId()));
                }
            });
        }
        if (!existCategoryList.isEmpty()) {
            List<String> existCategoryIdList = existCategoryList.stream().map(CoreCodeCategoryBean::getId).collect(Collectors.toList());
            List<CoreCodeBean> existCodeList = coreCodeService.selectListByFilter(SearchFilter.instance().match("CODECATEGORYID", existCategoryIdList).filter(MatchPattern.OR));
            Map<String, List<CoreCodeBean>> existCodeMap = existCodeList.stream().collect(Collectors.groupingBy(CoreCodeBean::getCodeCategoryId));
            existCodeMap.forEach((existCategory, existCode) -> {
                Map<String, List<CoreCodeBean>> tempExistCodeMap = existCode.stream().filter(code -> !StringUtils.isEmpty(code.getCode())).collect(Collectors.groupingBy(CoreCodeBean::getCode));
                List<CoreCodeBean> sourceCodeList = codeMap.get(existCategory);
                if (!ObjectUtils.isEmpty(sourceCodeList)) {
                    sourceCodeList.forEach(source -> {
                        if (tempExistCodeMap.get(source.getCode()) == null) {
                            insertCodeList.add(source);
                        } else {
                            if (queryCoreConstant == null || StringUtils.equals(queryCoreConstant.getConstantValue(), Constant.YES)) {
                                if (!StringUtils.equals(tempExistCodeMap.get(source.getCode()).get(0).getCodeName(), source.getCodeName())) {
                                    tempExistCodeMap.get(source.getCode()).forEach(c -> c.setCodeName(source.getCodeName()));
                                    updateCodeList.addAll(tempExistCodeMap.get(source.getCode()));
                                }
                            }

                        }
                    });
                }

            });
        }
        List<String> categoryList = coreAdminSelectConfigList.stream().map(CoreAdminSelectConfigBean::getCategory).collect(Collectors.toList());
        Map<String, CoreAdminSelectConfigBean> category2AdminSelectConfigMap = coreAdminSelectConfigList.stream().collect(Collectors.toMap(CoreAdminSelectConfigBean::getCategory, Function.identity(), (v1, v2) -> v2));
        List<CoreAdminSelectConfigBean> exitAdminSelectConfigList = coreAdminSelectConfigService.selectListByFilter(SearchFilter.instance().match("category", categoryList).filter(MatchPattern.OR));
        category2AdminSelectConfigMap.forEach((category, selectConfig) -> {
            if (exitAdminSelectConfigList.stream().noneMatch(select -> StringUtils.equals(select.getCategory(), category))) {
                insertAdminSelectConfigList.add(selectConfig);
            } else {
                if (queryCoreConstant == null || StringUtils.equals(queryCoreConstant.getConstantValue(), Constant.YES)) {
                    exitAdminSelectConfigList.stream().filter(select -> StringUtils.equals(select.getCategory(), category)).forEach(select -> select.setName(selectConfig.getName()));
                }

            }
        });
        insertCodeList.forEach(code -> code.setId(ApplicationContextHelper.getNextIdentity()));
        coreCodeCategoryService.getDao().insert(insertCodeCategoryList);
        coreCodeService.getDao().insert(insertCodeList);
        coreCodeService.getDao().update(updateCodeList, "CODENAME");
        coreAdminSelectConfigService.getDao().insert(insertAdminSelectConfigList);
        coreAdminSelectConfigService.getDao().update(exitAdminSelectConfigList, "name");

    }

    public void importSysI18n(List<CoreI18nMessageBean> coreI18nMessageList) {
        List<String> codeList = coreI18nMessageList.stream().map(coreI18nMessageBean -> coreI18nMessageBean.getExt$Item("CODE")).collect(Collectors.toList());

        List<CoreI18nMessageBean> coreI18nMessageBeanList = coreI18nMessageService.selectListByFilter(SearchFilter.instance().match("CODE", codeList).filter(MatchPattern.OR));

        Map<String, List<CoreI18nMessageBean>> coreI18nMap = new HashMap<>();

        List<CoreI18nBean> insertCoreI18nList = new ArrayList<>();
        List<CoreI18nMessageBean> insertCoreI18nMessageList = new ArrayList<>();
        List<CoreI18nMessageBean> updateCoreI18nMessageList = new ArrayList<>();

        coreI18nMessageBeanList.forEach(message -> {
            List<CoreI18nMessageBean> thisI18nMessageList = coreI18nMap.get(message.getExt$Item("CODE"));

            if (thisI18nMessageList == null) {
                thisI18nMessageList = new ArrayList<>();
                coreI18nMap.put(message.getExt$Item("CODE"), thisI18nMessageList);
            }

            thisI18nMessageList.add(message);
        });

        coreI18nMessageList.forEach(message -> {
            String code = message.getExt$Item("CODE");
            List<CoreI18nMessageBean> thisI18nMessageList = coreI18nMap.get(code);

            if (thisI18nMessageList == null) {
                CoreI18nBean i18nBean = new CoreI18nBean();
                i18nBean.setId(ApplicationContextHelper.getNextIdentity());
                i18nBean.setCode(code);
                i18nBean.setDescription(message.getMessage());
                i18nBean.setDefaultMessage(message.getMessage());

                insertCoreI18nList.add(i18nBean);

                thisI18nMessageList = new ArrayList<>();
                coreI18nMap.put(code, thisI18nMessageList);

                CoreI18nMessageBean coreI18nMessageBean = new CoreI18nMessageBean();
                coreI18nMessageBean.setId(ApplicationContextHelper.getNextIdentity());
                coreI18nMessageBean.setI18nConfigId(message.getI18nConfigId());
                coreI18nMessageBean.setI18nId(i18nBean.getId());
                coreI18nMessageBean.setMessage(message.getMessage());

                insertCoreI18nMessageList.add(coreI18nMessageBean);
            } else {
                CoreI18nMessageBean coreI18nMessage = thisI18nMessageList.stream().filter(thisMessage -> StringUtils.equals(thisMessage.getI18nConfigId(), message.getI18nConfigId())).findFirst().orElse(null);

                if (coreI18nMessage == null) {
                    CoreI18nMessageBean anotherCoreI18nMessage = thisI18nMessageList.stream().findAny().orElse(null);
                    if (anotherCoreI18nMessage != null) {
                        CoreI18nMessageBean coreI18nMessageBean = new CoreI18nMessageBean();
                        coreI18nMessageBean.setId(ApplicationContextHelper.getNextIdentity());
                        coreI18nMessageBean.setI18nConfigId(message.getI18nConfigId());
                        coreI18nMessageBean.setI18nId(anotherCoreI18nMessage.getI18nId());
                        coreI18nMessageBean.setMessage(message.getMessage());

                        insertCoreI18nMessageList.add(coreI18nMessageBean);
                    }
                } else {
                    coreI18nMessage.setMessage(message.getMessage());
                    updateCoreI18nMessageList.add(coreI18nMessage);
                }

            }
        });

        coreI18nService.getDao().insert(insertCoreI18nList);
        coreI18nMessageService.getDao().insert(insertCoreI18nMessageList);
        coreI18nMessageService.updateIfChanged(updateCoreI18nMessageList);
    }

    private LcdpGlobalConfigBean importSysClientJs(Map<String, String> fileMap) {
        String sysClientJs = fileMap.get("sysClientJs");
        if (!StringUtils.isEmpty(sysClientJs)) {
            LcdpGlobalConfigBean sysClientJsBean = JsonUtils.parse(sysClientJs, LcdpGlobalConfigBean.class);
            PersistableHelper.resetBasicProperties(sysClientJsBean);
            sysClientJsBean.setId(ApplicationContextHelper.getNextIdentity());
            lcdpGlobalConfigService.submit(sysClientJsBean, I18nHelper.getMessage("LCDP.GLOBAL_CONFIG.DEFAULT_IMPORT_COMMIT_LOG"));
            return sysClientJsBean;
        }
        return null;
    }

    private LcdpGlobalConfigBean importSysClientCss(Map<String, String> fileMap) {
        String sysClientCss = fileMap.get("sysClientCss");
        if (!StringUtils.isEmpty(sysClientCss)) {
            LcdpGlobalConfigBean sysClientCssBean = JsonUtils.parse(sysClientCss, LcdpGlobalConfigBean.class);
            PersistableHelper.resetBasicProperties(sysClientCssBean);
            sysClientCssBean.setId(ApplicationContextHelper.getNextIdentity());
            lcdpGlobalConfigService.submit(sysClientCssBean, I18nHelper.getMessage("LCDP.GLOBAL_CONFIG.DEFAULT_IMPORT_COMMIT_LOG"));
            return sysClientCssBean;
        }
        return null;
    }

    /**
     * 导入文件解压
     *
     * @param filePath 文件路径
     * @param dataMap  数据映射
     * @return 数据映射结果
     */
    public Map<String, String> unZip(Path filePath) {
        Map<String, String> map = new HashMap<>();

        FileInputStream input = null;
        ZipInputStream zipInputStream = null;
        try {
            //获取文件输入流
            input = new FileInputStream(filePath.toString());
            //获取ZIP输入流(一定要指定字符集Charset.forName("GBK")否则会报java.lang.IllegalArgumentException: MALFORMED)
            zipInputStream = new ZipInputStream(new BufferedInputStream(input), Charset.forName("GBK"));

            //定义ZipEntry置为null,避免由于重复调用zipInputStream.getNextEntry造成的不必要的问题
            ZipEntry ze = null;
            //循环遍历
            while ((ze = zipInputStream.getNextEntry()) != null) {
                int i = ze.getName().indexOf("\\");
                if (i < 0) {
                    i = ze.getName().indexOf("/");
                }
                String fileName = ze.getName().substring(i + 1);
                if (!ze.isDirectory()) {

                    ByteArrayOutputStream baos = null;
                    InputStream is = null;

                    try {
                        baos = new ByteArrayOutputStream();

                        //读取
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zipInputStream.read(buffer)) > -1) {
                            baos.write(buffer, 0, len);
                        }
                        baos.flush();

                        is = new ByteArrayInputStream(baos.toByteArray());
                        String fileContent = StringUtils.read(is);
                        map.put(fileName, EncryptUtils.base64Decode(fileContent));
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e) {
                                /* ignore */
                            }
                        }

                        if (baos != null) {
                            try {
                                baos.close();
                            } catch (IOException e) {
                                /* ignore */
                            }
                        }
                    }
                }
            }
        } catch (IOException io) {
            log.error(io.getMessage(), io);

            throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.EXCETION.FILE.ANALYSE.EXCETION"));
        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.closeEntry();
                } catch (IOException e) {
                    /* ignore */
                }

                try {
                    zipInputStream.close();
                } catch (IOException e) {
                    /* ignore */
                }
            }

            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    /* ignore */
                }
            }
        }

        return map;
    }

    public CoreFileBean getcreateExportFile(LcdpResourceExportDTO exportDTO, LcdpResourceCheckoutConfigDTO checkoutConfigDTO, LcdpCheckoutRecordBean checkoutRecord) {
        LcdpExportLogFileDTO exportLogFileDTO = new LcdpExportLogFileDTO();
        exportLogFileDTO.setTreeNodeDTOList(exportDTO.getTreeNodeDtoList());
        exportLogFileDTO.setLog(exportDTO.getExportLog());
        exportLogFileDTO.setCheckoutRecordNo(checkoutRecord.getCheckoutNo());
        String treeStr = JSONObject.toJSONString(exportLogFileDTO);
        LocalDateTime now = MybatisTimeZoneHelper.timeZoneTransform(LocalDateTime.now());
        String uuid = StringUtils.randomUUID(16);
        String zipName = "export_" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")) + "_" + LocalContextHelper.getLoginUserId() + ".swdp";
        FilePathDTO folderFilePath = FilePathDTO.of(FileScope.temp.name(), now, uuid, "resource");

        FilePathDTO zipFilePath = FilePathDTO.of(FileScope.temp.name(), now, uuid, zipName);

        Path path = filePathService.getLocalPath(folderFilePath);
        String resourcePath = path.toString() + File.separator;


        String sysCodePath = filePathService.getLocalPath(FilePathDTO.of(FileScope.temp.name(), now, uuid, "sysCode")).toString() + File.separator;
        String menuPath = filePathService.getLocalPath(FilePathDTO.of(FileScope.temp.name(), now, uuid, "menu")).toString() + File.separator;
        String globalConfigPath = filePathService.getLocalPath(FilePathDTO.of(FileScope.temp.name(), now, uuid, "globalConfig")).toString() + File.separator;
        String checkoutConfigPath = filePathService.getLocalPath(FilePathDTO.of(FileScope.temp.name(), now, uuid, "checkoutConfig")).toString() + File.separator;
        String i18nPath = filePathService.getLocalPath(FilePathDTO.of(FileScope.temp.name(), now, uuid, "i18n")).toString() + File.separator;
        String resourceFilePath = filePathService.getLocalPath(FilePathDTO.of(FileScope.temp.name(), now, uuid, "lcdpFile")).toString() + File.separator;
        String scriptBlockPath = filePathService.getLocalPath(FilePathDTO.of(FileScope.temp.name(), now, uuid, "lcdpScriptBlock")).toString() + File.separator;


        File zip = filePathService.getLocalPath(zipFilePath).toFile();

        File exportLog = new File(path.getParent() + File.separator + "exportLog");

        //文件内容采用base64加密
        FileUtils.write(exportLog, EncryptUtils.base64Encode(treeStr));
        // 写入脚本文件zipName

        Map<Long, List<LcdpModulePageCompBean>> modulePageId2CompMap = new HashMap<>();//导出页面组件数据

        Map<Long, List<LcdpModulePageI18nBean>> modulePageId2I18nMap = null;//导出页面组件国际化数据
        Map<Long, List<LcdpPageI18nCodeBean>> modulePageId2I18nDependenceMap = null;//导出页面组件国际化数据

        List<CoreI18nMessageBean> coreI18nMessageList = new ArrayList<>();//核心国际化

        Map<String, List<String>> type2ParamMap = new HashMap<>();//组件数据解析Map
        //拿到页面类型的资源ID,从ext$里拿到历史资源ID
        List<LcdpResourceBean> viewList = exportDTO.getExportResourceList().stream().filter(his -> LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(his.getResourceCategory())).collect(Collectors.toList());
        List<Long> viewIdList = viewList.stream().map(his -> Long.valueOf(his.getExt$Item("resourcehistoryid"))).collect(Collectors.toList());

        List<CoreMenuBean> menuList = new ArrayList<>();

        if (!viewIdList.isEmpty()) {
            List<LcdpModulePageCompBean> modulePageCompList = lcdpModulePageCompService.selectListByFilter(SearchFilter.instance().match("MODULEPAGEHISTORYID", viewIdList).filter(MatchPattern.OR));

            List<String> configList = modulePageCompList.stream().map(c -> c.getConfig()).collect(Collectors.toList());

            type2ParamMap = LcdpPageCompConfigAnalyseHelper.analyseConfig2Select(configList);

            modulePageId2CompMap = modulePageCompList.stream().collect(Collectors.groupingBy(LcdpModulePageCompBean::getModulePageId));

            List<Long> resourceIdList = viewList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());

            menuList = coreMenuService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", resourceIdList).filter(MatchPattern.OR));

            List<LcdpModulePageI18nBean> pageI18nList = lcdpModulePageI18nService.selectListByFilter(SearchFilter.instance().match("MODULEPAGEHISTORYID", viewIdList).filter(MatchPattern.OR));

            modulePageId2I18nMap = pageI18nList.stream().collect(Collectors.groupingBy(LcdpModulePageI18nBean::getModulePageId));

            List<LcdpPageI18nCodeBean> pageI18nCodeList = lcdpPageI18nCodeService.selectListByFilter(SearchFilter.instance().match("MODULEPAGEHISTORYID", viewIdList).filter(MatchPattern.OR));

            modulePageId2I18nDependenceMap = pageI18nCodeList.stream().collect(Collectors.groupingBy(LcdpPageI18nCodeBean::getModulePageId));

            Set<String> codeSet = pageI18nCodeList.stream().map(LcdpPageI18nCodeBean::getCode).collect(Collectors.toSet());

            if (!codeSet.isEmpty()) {
                ArrayList<String> codeList = new ArrayList<>(codeSet);
                coreI18nMessageList.addAll(coreI18nMessageService.selectListByFilter(SearchFilter.instance().match("CODE", codeList).filter(MatchPattern.OR)));
            }


        }

        for (LcdpResourceBean resource : exportDTO.getExportResourceList()) {
            FileUtils.write(new File(resourcePath + resource.getId()), EncryptUtils.base64Encode(JSONObject.toJSONString(resource)));
            if (LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(resource.getResourceCategory())) {
                if (!ObjectUtils.isEmpty(modulePageId2CompMap.get(resource.getId()))) {
                    FileUtils.write(new File(resourcePath + resource.getId() + "view"), EncryptUtils.base64Encode(JSONObject.toJSONString(modulePageId2CompMap.get(resource.getId()))));
                }

                if (!ObjectUtils.isEmpty(modulePageId2I18nMap.get(resource.getId()))) {
                    FileUtils.write(new File(resourcePath + resource.getId() + "i18n"), EncryptUtils.base64Encode(JSONObject.toJSONString(modulePageId2I18nMap.get(resource.getId()))));
                }

                if (!ObjectUtils.isEmpty(modulePageId2I18nDependenceMap.get(resource.getId()))) {
                    FileUtils.write(new File(resourcePath + resource.getId() + "sysI18nDependence"), EncryptUtils.base64Encode(JSONObject.toJSONString(modulePageId2I18nDependenceMap.get(resource.getId()))));
                }
            }
        }
        // 写入表文件
        for (LcdpTableDTO tableDTO : exportDTO.getTableDTOList()) {
            FileUtils.write(new File(resourcePath + tableDTO.getTableName()), EncryptUtils.base64Encode(JSONObject.toJSONString(tableDTO)));
        }
        // 写入视图
        for (LcdpViewBean view : exportDTO.getViewList()) {
            FileUtils.write(new File(resourcePath + view.getViewName()), EncryptUtils.base64Encode(JSONObject.toJSONString(view)));
        }
        //写入系统编码文件
        List<String> selectParam = type2ParamMap.values().stream()
                .flatMap(List::stream)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (!ObjectUtils.isEmpty(selectParam)) {
            List<CoreCodeCategoryBean> coreCodeCategoryList = coreCodeCategoryService.selectListByIds(selectParam);

            List<CoreCodeBean> codeList = coreCodeService.selectListByFilter(SearchFilter.instance().match("codeCategoryId", selectParam).filter(MatchPattern.OR));

            List<CoreAdminSelectConfigBean> coreAdminSelectConfigList = coreAdminSelectConfigService.selectListByFilter(SearchFilter.instance().match("category", selectParam).filter(MatchPattern.OR));

            FileUtils.write(new File(sysCodePath + "codeCategory"), EncryptUtils.base64Encode(JSONObject.toJSONString(coreCodeCategoryList)));
            FileUtils.write(new File(sysCodePath + "code"), EncryptUtils.base64Encode(JSONObject.toJSONString(codeList)));
            FileUtils.write(new File(sysCodePath + "adminSelectConfig"), EncryptUtils.base64Encode(JSONObject.toJSONString(coreAdminSelectConfigList)));
        }
        if (!menuList.isEmpty()) {
            FileUtils.write(new File(menuPath + "menu"), EncryptUtils.base64Encode(JSONObject.toJSONString(menuList)));
        }

        // 全局JS脚本
        if (StringUtils.equals(checkoutConfigDTO.getExportSysClientJsFlag(), Constant.YES)) {
            LcdpGlobalConfigBean globalJs = lcdpGlobalConfigService.selectConfigContent("SYS_CLIENT_JS");

            if (globalJs != null) {
                FileUtils.write(new File(globalConfigPath + "sysClientJs"), EncryptUtils.base64Encode(JSONObject.toJSONString(globalJs)));
            }
        }

        // 全局CSS脚本
        if (StringUtils.equals(checkoutConfigDTO.getExportSysClientCssFlag(), Constant.YES)) {
            LcdpGlobalConfigBean globalCss = lcdpGlobalConfigService.selectConfigContent("SYS_CLIENT_CSS");

            if (globalCss != null) {
                FileUtils.write(new File(globalConfigPath + "sysClientCss"), EncryptUtils.base64Encode(JSONObject.toJSONString(globalCss)));
            }
        }

        //写入需要导入的国际化
        if (!coreI18nMessageList.isEmpty()) {
            FileUtils.write(new File(i18nPath + "i18n"), EncryptUtils.base64Encode(JSONObject.toJSONString(coreI18nMessageList)));
        }

        List<LcdpResourceFileBean> lcdpResourceFileList = exportDTO.getLcdpResourceFileList();
        if (!ObjectUtils.isEmpty(lcdpResourceFileList)) {
            FileUtils.write(new File(resourceFilePath + "lcdpFile"), EncryptUtils.base64Encode(JSONObject.toJSONString(lcdpResourceFileList)));
        }

        // 写入需要导入的代码块
        List<LcdpScriptBlockBean> lcdpScriptBlockList = exportDTO.getLcdpScriptBlockList();
        if (!ObjectUtils.isEmpty(lcdpScriptBlockList)) {
            FileUtils.write(new File(scriptBlockPath + "scriptBlock"), EncryptUtils.base64Encode(JSONObject.toJSONString(lcdpScriptBlockList)));
        }


        FileUtils.write(new File(checkoutConfigPath + "checkoutConfig"), EncryptUtils.base64Encode(JSONObject.toJSONString(checkoutConfigDTO)));

        ArchiveUtils.zip(zip, new File(resourcePath), new File(sysCodePath), exportLog, new File(globalConfigPath), new File(checkoutConfigPath), new File(i18nPath), new File(resourceFilePath), new File(scriptBlockPath));

        CoreFileBean coreFileBean = new CoreFileBean();
        Long key = ApplicationContextHelper.getNextIdentity();
        coreFileBean.setId(key);
        coreFileBean.setName(zipName);
        coreFileBean.setFileExt("swdp");
        coreFileBean.setTargetId(checkoutRecordService.getDao().getTable() + "$" + checkoutRecord.getId());
        coreFileBean.setScope(FileScope.temp.name());
        coreFileBean.setValidTimeFrom(null);
        coreFileBean.setValidTimeTo(null);
        coreFileBean.setMd5Name(uuid);
        // 上传人应该为提交人,这里取了制单人信息
        coreFileBean.setTimes(0L);
        coreFileBean.setCreatedTime(LocalDateTime.now());
        coreFileBean.setPermanent("1");
        coreFileBean.setVersion(1L);
        coreFileService.getDao().insert(coreFileBean);
        fileManager.upload(coreFileBean, zip.toPath());

        return coreFileBean;
    }

    private String createExportFile(LcdpResourceExportDTO exportDTO, LcdpResourceCheckoutConfigDTO checkoutConfigDTO, LcdpCheckoutRecordBean checkoutRecord) {

        CoreFileBean coreFileBean = getcreateExportFile(exportDTO, checkoutConfigDTO, checkoutRecord);

        String downloadUrl = fileManager.getAbsoluteDownloadUrl(coreFileBean);
        return downloadUrl;
    }

    public List<LcdpResourceBean> getExportedResources(List<Long> resourceIdList) {
        List<LcdpResourceBean> sourceResourceList = selectListByIds(resourceIdList);
        Map<Long, Long> parentIdMap = sourceResourceList.stream().collect(Collectors.toMap(LcdpResourceBean::getId, LcdpResourceBean::getParentId));
        List<LcdpResourceHistoryBean> resourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", resourceIdList).filter(MatchPattern.OR));
        Map<Long, List<LcdpResourceHistoryBean>> resourceId2HistoryMap = resourceHistoryList.stream().collect(Collectors.groupingBy(LcdpResourceHistoryBean::getResourceId));
        List<LcdpResourceHistoryBean> effectList = new ArrayList<>();

        sourceResourceList.forEach(resource -> {
            List<LcdpResourceHistoryBean> historyBeans = resourceId2HistoryMap.get(resource.getId());
            if (!CollectionUtils.isEmpty(historyBeans)) {
                historyBeans.stream().filter(history -> resource.getEffectVersion() != null && history.getVersion() != null
                                && resource.getEffectVersion().longValue() == history.getVersion().longValue())
                        .findFirst().ifPresent(effectList::add);
            }
        });
        //复制脚本数据
        List<LcdpResourceBean> resourceList = new ArrayList<>();
        List<LcdpResourceBean> scriptList = effectList.stream().map(his -> {
            LcdpResourceBean resource = new LcdpResourceBean();
            BeanUtils.copyProperties(his, resource);
            resource.setId(his.getResourceId());
            resource.setOrderNo(1L);
            resource.setExt$Item("resourcehistoryid", his.getId().toString());
            resource.setParentId(parentIdMap.get(his.getResourceId()));
            return resource;
        }).collect(Collectors.toList());
        //查询模块和分类用来构造提交树
        List<Long> moduleIdList = scriptList.stream().map(LcdpResourceBean::getParentId).collect(Collectors.toList());
        List<LcdpResourceBean> mooduleList = selectListByIds(moduleIdList);
        List<Long> categoryIdList = mooduleList.stream().map(LcdpResourceBean::getParentId).collect(Collectors.toList());
        List<LcdpResourceBean> categoryList = selectListByIds(categoryIdList);
        resourceList.addAll(scriptList);
        resourceList.addAll(mooduleList);
        resourceList.addAll(categoryList);

        return resourceList;
    }

    @Override
    public LocalDateTime getResourceLastModifiedTime(Long id) {
        String loginUserId = LocalContextHelper.getLoginUserId();

        LcdpResourceHistoryBean resourceHistory = lcdpResourceHistoryService.selectUnsubmittedResourceHistory(loginUserId, id);

        if (resourceHistory != null) {
            return resourceHistory.getLastUpdatedTime();
        }
        return null;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.DELETE)
    public void physicalDeleteResource(RestJsonWrapperBean wrapper) {
        LcdpResourceService.super.delete(wrapper);
        List<Long> idList = wrapper.parseId(Long.class);

        List<LcdpResourceBean> childResourceList = this.selectListByFilter(SearchFilter.instance().match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ)
                .match("PARENTID", idList).filter(MatchPattern.OR), Arrays.asList("ID"));
        if (childResourceList != null && childResourceList.size() > 0) {
            throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.CHILD_RESOURCE_EXITS"));
        }


        List<LcdpResourceHistoryBean> historyList = lcdpResourceHistoryService.getDao().selectListByOneColumnValues(idList, "RESOURCEID", Arrays.asList("ID"));
        if (historyList != null && historyList.size() > 0) {
            lcdpResourceHistoryService.getDao().deleteByIdList(historyList.stream().map(e -> e.getId()).collect(Collectors.toList()));
        }

        List<CoreMenuBean> menuList = coreMenuService.getDao().selectListByOneColumnValues(idList, "RESOURCEID", Arrays.asList("ID"));
        List<String> menuIdList = menuList.stream().map(e -> e.getId()).collect(Collectors.toList());
        if (menuList != null && menuList.size() > 0) {
            coreMenuService.getDao().deleteByIdList(menuIdList);
        }

        SearchFilter searchFilter = SearchFilter.instance().match("PERMISSIONTYPEID", "T_CORE_MENU").filter(MatchPattern.EQ)
                .match("TARGETID", menuIdList).filter(MatchPattern.OR);
        List<CoreRolePermissionBean> permissionList = rolePermissionService.selectListByFilter(searchFilter);
        if (permissionList != null && permissionList.size() > 0) {
            rolePermissionService.getDao().deleteByIdList(permissionList.stream().map(e -> e.getId()).collect(Collectors.toList()));
        }

        List<LcdpResourceCheckoutRecordBean> checkoutRecordList = resourceCheckoutRecordService.selectListByFilter(SearchFilter.instance()
                .match("RESOURCEID", idList).filter(MatchPattern.OR), Arrays.asList("ID"));
        if (checkoutRecordList != null && checkoutRecordList.size() > 0) {
            resourceCheckoutRecordService.getDao().deleteByIdList(checkoutRecordList.stream().map(e -> e.getId()).collect(Collectors.toList()));
        }
    }

    @Override
    public List<LcdpResourceBean> selectIdAndParentIdList() {
        int count = getDao().count();
        int loop = (int) Math.ceil(Double.valueOf("" + count) / 100);

        AtomicInteger atomic = new AtomicInteger();

        List<LcdpResourceBean> resourceList = new ArrayList<>();
        for (int i = 0; i < loop; i++) {
            atomic.incrementAndGet();
            final int j = i + 1;

            TaskExecutorManager.getDefaultRunner().submit(() -> {
                try {
                    // 只查询数据
                    MybatisPageHelper.setDataOnly();

                    PageRequest page = new PageRequest();
                    page.setPageNumber(j);
                    page.setPageSize(100);

                    PageRowBounds rowBounds = new PageRowBounds(page);
                    List<LcdpResourceBean> selectList = selectPagination(() -> {
                        return getDao().selectIdAndParentIdList(new MapperParameter());
                    }, rowBounds).getRows();

                    synchronized (resourceList) {
                        if (!CollectionUtils.isEmpty(selectList)) {
                            resourceList.addAll(selectList);
                        }
                    }
                } finally {
                    atomic.decrementAndGet();

                    MybatisPageHelper.clear();
                }
            });
        }

        while (atomic.get() > 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
        }

        return resourceList;
    }

    @Override
    public List<LcdpResourceBean> selectEffectIdList() {
        int count = getDao().count();
        int loop = (int) Math.ceil(Double.valueOf("" + count) / 100);

        AtomicInteger atomic = new AtomicInteger();

        List<LcdpResourceBean> resourceList = new ArrayList<>();
        for (int i = 0; i < loop; i++) {
            atomic.incrementAndGet();
            final int j = i + 1;

            TaskExecutorManager.getDefaultRunner().submit(() -> {
                try {
                    // 只查询数据
                    MybatisPageHelper.setDataOnly();

                    PageRequest page = new PageRequest();
                    page.setPageNumber(j);
                    page.setPageSize(100);

                    PageRowBounds rowBounds = new PageRowBounds(page);
                    List<LcdpResourceBean> selectList = selectPagination(() -> {
                        return getDao().selectEffectIdList(new MapperParameter());
                    }, rowBounds).getRows();

                    synchronized (resourceList) {
                        if (!CollectionUtils.isEmpty(selectList)) {
                            resourceList.addAll(selectList);
                        }
                    }
                } finally {
                    atomic.decrementAndGet();

                    MybatisPageHelper.clear();
                }
            });
        }

        while (atomic.get() > 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
        }

        return resourceList;
    }


    private int findCharCount(String content, char findChar) {
        int num = 0;
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == findChar) {
                num++;
            }
        }
        return num;
    }

    private List<LcdpJavaStructureDTO> getClassStructureInfo(Class<?> clazz, String classContent) {
        Map<String, Object> mapper = new HashMap<String, Object>();
        Arrays.stream(clazz.getDeclaredMethods()).forEach(method -> mapper.put(method.getName(), method));
        Arrays.stream(clazz.getDeclaredFields()).forEach(field -> mapper.put(field.getName(), field));
        String[] lines = classContent.split("\n");
        List<LcdpJavaStructureDTO> list = new ArrayList<LcdpJavaStructureDTO>();
        // 左花括号数量
        int leftBraceNum = 0;
        // 右花括号数量
        int rightBraceNum = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            leftBraceNum += this.findCharCount(line, '{');
            rightBraceNum += this.findCharCount(line, '}');
            if (leftBraceNum == 0) {
                continue;
            }
            // 处在某个块状作用域下，不属于class属性或方法，无须解析
            if (!(leftBraceNum == rightBraceNum + 1 || (line.trim().endsWith("{") && leftBraceNum == rightBraceNum + 2))) {
                continue;
            }
            String[] lineSplitParts = line.split("(\\s|\\()+");
            Object desc = null;
            for (String part : lineSplitParts) {
                if (mapper.get(part) != null) {
                    desc = mapper.get(part);
                    break;
                }
            }
            if (desc == null) {
                continue;
            }
            LcdpJavaStructureDTO structure = new LcdpJavaStructureDTO();
            if (desc instanceof Method) {
                Method methodDescription = (Method) desc;
                String returnType = methodDescription.getReturnType().getSimpleName();
                List<String> paramTypes = Arrays.stream(methodDescription.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.toList());
                structure.setType("method");
                structure.setName(String.format("%s(%s): %s", methodDescription.getName(), StringUtils.join(paramTypes, ","), returnType));
                structure.setLineNumber(i + 1);
            } else if (desc instanceof Field) {
                Field description = (Field) desc;
                String fieldTypeName = description.getType().getSimpleName();
                structure.setType("field");
                structure.setName(String.format("%s: %s", description.getName(), fieldTypeName));
                structure.setLineNumber(i + 1);
            }
            list.add(structure);
        }
        return list;
    }

    private void rename(List<LcdpResourceHistoryBean> list, String newName) {
        List<RestJsonWrapperBean> wrapperList = new ArrayList<>();

        List<LcdpResourceBean> updateResourceList = new ArrayList<>();

        for (LcdpResourceHistoryBean resourceHistory : list) {
            String path = resourceHistory.getPath();

            int lastDotIndex = path.lastIndexOf(".");
            String newPath = path.substring(0, lastDotIndex + 1) + newName;

            if (LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(resourceHistory.getResourceCategory())) {
                String content = resourceHistory.getContent();
                if (!StringUtils.isEmpty(content)) {
                    content = LcdpJavaCodeResolverUtils.updatePackage(content, newPath); // 更新包名

                    String newContent = LcdpJavaCodeResolverUtils.replaceClassSimpleName(content, newName);
                    resourceHistory.setContent(newContent);

                    // 替换脚本类名
                    String classContent = LcdpJavaCodeResolverUtils.getClassContent(newContent,
                            newName,
                            1L, 1L,
                            resourceHistory.getVersionOffset());
                    classContent = replaceScriptContent(classContent);
                    resourceHistory.setClassContent(classContent);

                    // 改名后编译
                    LcdpJavaCodeResolverUtils.loadSourceCode(resourceHistory);
                }

            }
            if (LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(resourceHistory.getResourceCategory())) {
                //处理国际化
                List<LcdpModulePageI18nBean> modulePageI18nList = lcdpModulePageI18nService.selectListByFilter(SearchFilter.instance().match("modulePageHistoryId",
                        resourceHistory.getId()).filter(MatchPattern.EQ));

                if (!ObjectUtils.isEmpty(modulePageI18nList)) {
                    List<LcdpModulePageI18nBean> newPageI18nList = modulePageI18nList.stream().map(i18nBean -> {
                        LcdpModulePageI18nBean newModulePageI18n = new LcdpModulePageI18nBean();
                        BeanUtils.copyProperties(i18nBean, newModulePageI18n);
                        newModulePageI18n.setId(ApplicationContextHelper.getNextIdentity());
                        newModulePageI18n.setCode(StringUtils.replace(i18nBean.getCode(), path, newPath));
                        return newModulePageI18n;
                    }).collect(Collectors.toList());

                    lcdpModulePageI18nService.getDao().deleteBy(modulePageI18nList);
                    lcdpModulePageI18nService.getDao().fastInsert(newPageI18nList);

                    List<LcdpModulePageCompBean> modulePageCompList = lcdpModulePageCompService.selectListByFilter(SearchFilter.instance().match("modulePageHistoryId",
                            resourceHistory.getId()).filter(MatchPattern.EQ));
                    dealRenamePage(path, newPath, modulePageCompList);
                    lcdpModulePageCompService.getDao().update(modulePageCompList, "CONFIG");

                }
            }

            resourceHistory.setResourceName(newName);
            resourceHistory.setPath(newPath);

            LcdpResourceBean resource = new LcdpResourceBean();
            resource.setId(resourceHistory.getResourceId());
            resource.setResourceName(newName);
            resource.setPath(newPath);

            updateResourceList.add(resource);

            RestJsonWrapperBean wrapper = new RestJsonWrapperBean();
            wrapper.setParamValue("searchKey", path);
            wrapper.setParamValue("newContent", newPath);
            wrapperList.add(wrapper);
        }

        lcdpResourceHistoryService.updateIfChanged(list);
        getDao().update(updateResourceList, "PATH", "RESOURCENAME");
    }

    /**
     * 根据资源路径构建页面表格配置与页面高级查询配置
     */
    private String buildPageConfig(String path) {
        StringBuilder sb = new StringBuilder();

        // 添加页面配置
        CorePageConfigDTO pageConfig = corePageConfigService.selectCurrentPageConfig(path);

        if (!pageConfig.getGrid().isEmpty()) {
            sb.append("Gikam.setCompParam('")
                    .append(JSON.toJSONString(pageConfig))
                    .append("');\n");
        }

        if (LocalContextHelper.isUserLogin()) {
            // 添加高级查询配置
            List<CoreStudioAdvSearchConfigBean> searchConfigList = coreStudioAdvSearchConfigService.getCurrentUserConfigListByUrl(path);
            if (!CollectionUtils.isEmpty(searchConfigList)) {
                sb.append("Gikam.component.Grid.setAdvSearchConfig([");

                for (int i = 0, j = searchConfigList.size(); i < j; i++) {
                    CoreStudioAdvSearchConfigBean searchConfig = searchConfigList.get(i);

                    if (i > 0) {
                        sb.append(",");
                    }

                    sb.append("{blockId:'")
                            .append(searchConfig.getBlockId())
                            .append("',url:'")
                            .append(searchConfig.getUrl())
                            .append("',json:'")
                            .append(searchConfig.getJson())
                            .append("'}");
                }

                sb.append("]);\n");
            }
        }
        return sb.toString();
    }

    private void dealRenamePage(String oldPath, String newPath, List<LcdpModulePageCompBean> newModulePageCompList) {
        newModulePageCompList.forEach(comp -> {
            String config = comp.getConfig();
            JSONObject configObject = JSON.parseObject(config);
            configObject.forEach((k, v) -> {
                if (!(v instanceof JSONObject)) {
                    return;
                }
                JSONObject propertyObject = (JSONObject) v;
                Iterator<String> iterator = propertyObject.keySet().iterator();
                while (iterator.hasNext()) {
                    String property = iterator.next();
                    if (StringUtils.equals(property, "type") && StringUtils.equals(propertyObject.getString(property), "i18n")) {
                        if (!StringUtils.isEmpty(propertyObject.getString("i18nCode")) && propertyObject.getString("i18nCode").startsWith(oldPath)) {
                            String oldI18nCode = propertyObject.getString("i18nCode");
                            String newI18nCode = oldI18nCode.replaceAll(oldPath, newPath);
                            propertyObject.put("i18nCode", newI18nCode);
                        }
                        break;
                    }

                }

            });

            comp.setConfig(configObject.toJSONString());
        });
    }

    private void importLcdpRsourceFileList(List<LcdpResourceFileBean> lcdpResourceFileList, List<LcdpResourceFileBean> importResourceFileList) {
        List<String> importFilePathList = importResourceFileList.stream().map(LcdpResourceFileBean::getRelativePath).collect(Collectors.toList());
        lcdpResourceFileList = lcdpResourceFileList.stream().filter(file -> importFilePathList.contains(file.getRelativePath())).collect(Collectors.toList());
        List<String> filePathList = lcdpResourceFileList.stream().map(LcdpResourceFileBean::getRelativePath).collect(Collectors.toList());
        Map<String, LcdpResourceFileBean> filePath2FileMap = lcdpResourceFileList.stream().collect(Collectors.toMap(LcdpResourceFileBean::getRelativePath, Function.identity()));
        List<LcdpResourceFileBean> resourceFileList = resourceFileService.selectListByFilter(SearchFilter.instance().match("relativePath", filePathList).filter(MatchPattern.OR));
        Map<String, LcdpResourceFileBean> exitFileMap = resourceFileList.stream().collect(Collectors.toMap(LcdpResourceFileBean::getRelativePath, Function.identity()));

        resourceFileList.forEach(file -> {
            file.setFileContent(filePath2FileMap.get(file.getRelativePath()).getFileContent());
        });
        resourceFileService.getDao().update(resourceFileList, "fileContent");

        lcdpResourceFileList.removeIf(importFile -> !ObjectUtils.isEmpty(exitFileMap.get(importFile.getRelativePath())));
        List<LcdpResourceFileBean> insertResourceFileList = lcdpResourceFileList.stream().map(importFile -> {
            LcdpResourceFileBean resourceFile = new LcdpResourceFileBean();
            BeanUtils.copyProperties(importFile, resourceFile);
            resourceFile.setFilePath(null);
            EntityHelper.assignCreatedElement(resourceFile);
            return resourceFile;
        }).collect(Collectors.toList());

        createLcdpResourceFile(lcdpResourceFileList);

        resourceFileService.getDao().insert(insertResourceFileList);
    }


    private void createLcdpResourceFile(List<LcdpResourceFileBean> lcdpResourceFileList) {
        lcdpResourceFileList.forEach(file -> {
            byte[] fileBytes = Base64.getDecoder().decode(file.getFileContent());

            try (InputStream in = new ByteArrayInputStream(fileBytes);) {
                FilePathDTO filePathDTO = filePathService.toFilePath(file.getRelativePath());
                Path localPath1 = filePathService.getLocalPath(filePathDTO);
                String absoluteDownloadUrl = fileManager.getAbsoluteDownloadUrl(filePathDTO);
                File file1 = localPath1.toFile();
                FileUtils.write(file1, in);
                fileManager.upload(filePathDTO, file1);
                file.setFilePath(absoluteDownloadUrl);
            } catch (Exception e) {
                throw new FileException(e);
            }
        });
    }

    private List<LcdpModulePageCompBean> generatePageComps(List<LcdpModulePageCompBean> modulePageCompList) {
        if (ObjectUtils.isEmpty(modulePageCompList)) {
            return new ArrayList<>();
        }
        List<LcdpModulePageCompBean> pageCompDataList = modulePageCompList.stream().filter(page -> !StringUtils.equals("remove", page.getParentId())).map(page -> {
            LcdpModulePageCompBean pageComp = new LcdpModulePageCompBean();
            BeanUtils.copyProperties(page, pageComp);
            return pageComp;
        }).collect(Collectors.toList());
        pageCompDataList.forEach(comp -> {
            if (StringUtils.isEmpty(comp.getParentId())) {
                comp.setParentId("root");
            }
        });
        LinkedHashMap<String, List<String>> parentId2IdListMap
                = pageCompDataList.stream().collect(Collectors.groupingBy(LcdpModulePageCompBean::getParentId, LinkedHashMap::new, Collectors.mapping(LcdpModulePageCompBean::getId, Collectors.toList())));
        Map<String, String> idMap = new LinkedHashMap<>();
        Map<String, String> revertIdMap = new LinkedHashMap<>();
        List<LcdpModulePageCompBean> targetPageCompList = pageCompDataList.stream().map(page -> {
            LcdpModulePageCompBean pageComp = new LcdpModulePageCompBean();
            BeanUtils.copyProperties(page, pageComp);
            return pageComp;
        }).collect(Collectors.toList());
        targetPageCompList.forEach(page -> {
            String id = StringUtils.randomUUID();
            idMap.put(page.getId(), id);
            revertIdMap.put(id, page.getId());
            page.setId(id);
        });
        Map<String, List<LcdpModulePageCompBean>> targetParentId2ChildListMap = targetPageCompList.stream().collect(Collectors.groupingBy(LcdpModulePageCompBean::getParentId));

        parentId2IdListMap.forEach((parentId, childIdList) -> {
            if (StringUtils.equals(parentId, "root")) {
                return;
            }
            targetParentId2ChildListMap.get(parentId).forEach(child -> child.setParentId(idMap.get(parentId)));
            LcdpModulePageCompBean parentComp = targetPageCompList.stream().filter(page -> StringUtils.equals(revertIdMap.get(page.getId()), parentId)).findFirst().orElse(null);
            if (parentComp == null) {
                targetPageCompList.removeIf(page -> StringUtils.isEmpty(page.getParentId()));
                return;
            }
            JSONObject config = JSON.parseObject(parentComp.getConfig());
            Set<String> keySet = config.keySet();
            //需要将config中的childrenWidgetId和结尾是_childrenWidgetId 进行替换
            List<String> childrenWidgetIdList = keySet.stream().filter(key -> key.equals("childrenWidgetId") || key.endsWith("_childrenWidgetId")).collect(Collectors.toList());
            for (String key : childrenWidgetIdList) {
                JSONArray childrenWidgetId = (JSONArray) config.get(key);
                if (!ObjectUtils.isEmpty(childrenWidgetId)) {
                    for (int i = 0; i < childrenWidgetId.size(); i++) {
                        childrenWidgetId.set(i, idMap.get(childrenWidgetId.getString(i)));
                    }
                    config.put(key, childrenWidgetId);
                }
            }
            String configJson = JSONObject.toJSONString(config);

            parentComp.setConfig(configJson);
        });

        for (LcdpModulePageCompBean pageComp : targetPageCompList) {
            if (StringUtils.equals(pageComp.getParentId(), "root")) {
                pageComp.setParentId(null);
            }
            String configStr = pageComp.getConfig();
            if (!StringUtils.isEmpty(configStr)) {
                JSONObject config = JSON.parseObject(configStr);

                //处理uuid 将新生成组件中config的uuid做到与组件的id 一致
                config.put("uuid", pageComp.getId());
                String configJson = JSONObject.toJSONString(config);

                pageComp.setConfig(configJson);
            }

        }
        List<LcdpModulePageCompBean> shuttleFrameList = targetPageCompList.stream().filter(comp -> "ShuttleFrame".equals(comp.getType())).collect(Collectors.toList());
        if (!shuttleFrameList.isEmpty()) {
            shuttleFrameList.forEach(shuttleFrame -> {
                JSONObject config = JSON.parseObject(shuttleFrame.getConfig());
                JSONArray childrenWidgetId = config.getJSONArray("childrenWidgetId");
                String leftGridId = childrenWidgetId.getString(0);
                String rightGridId = childrenWidgetId.getString(1);
                LcdpModulePageCompBean leftGrid = targetPageCompList.stream().filter(comp -> StringUtils.equals(leftGridId, comp.getId())).findFirst().orElse(null);
                LcdpModulePageCompBean rightGrid = targetPageCompList.stream().filter(comp -> StringUtils.equals(rightGridId, comp.getId())).findFirst().orElse(null);
                if (leftGrid != null) {
                    JSONObject leftGridConfig = JSON.parseObject(leftGrid.getConfig());
                    config.put("columns_childrenWidgetId", leftGridConfig.get("childrenWidgetId"));
                    config.put("leftToolbar_childrenWidgetId", leftGridConfig.get("toolbar_childrenWidgetId"));
                }
                if (rightGrid != null) {
                    JSONObject rightGridConfig = JSON.parseObject(rightGrid.getConfig());
                    config.put("rightColumns_childrenWidgetId", rightGridConfig.get("childrenWidgetId"));
                    config.put("rightToolbar_childrenWidgetId", rightGridConfig.get("toolbar_childrenWidgetId"));
                }
                String configJson = JSONObject.toJSONString(config);
                shuttleFrame.setConfig(configJson);
            });
        }

        return targetPageCompList;
    }


    private void dealJavaHistory(LcdpResourceBean resource, LcdpResourceHistoryBean resourceHistory) {
        if (StringUtils.isBlank(resourceHistory.getContent())) {
            return;
        }

        List<String> lcdpBaseServiceFieldNameList = LcdpJavaCodeResolverUtils.getAuwowiredLcdpBaseServiceFieldNameList(resourceHistory.getContent());
        if (!lcdpBaseServiceFieldNameList.isEmpty()) {
            throw new CheckedException(I18nHelper.getMessage("LCDP.EXCEPTION.UNABLE_AUTOWIRE_LCDPBASESERVICE",
                    lcdpBaseServiceFieldNameList.stream().collect(Collectors.joining(","))));
        }

        String content = resourceHistory.getContent();
        List<String> importClassFullNameList = LcdpJavaCodeResolverUtils.getImportClassFullNameList(content);
        if (!importClassFullNameList.isEmpty()) {
            content = LcdpJavaCodeResolverUtils.getImportedSourceCode(content, importClassFullNameList);
        }

        Long newModifyVersion = Optional.ofNullable(resourceHistory.getModifyVersion()).orElse(0L) + 1L;
        resourceHistory.setContent(content);
        resourceHistory.setModifyVersion(newModifyVersion);

        String classContent = LcdpJavaCodeResolverUtils.getClassContent(resourceHistory.getContent(),
                resourceHistory.getResourceName(),
                resourceHistory.getVersion(),
                resourceHistory.getModifyVersion(),
                resourceHistory.getVersionOffset());
        classContent = replaceScriptContent(classContent);

        resourceHistory.setContent(LcdpJavaCodeResolverUtils.updatePackage(resourceHistory.getContent(), resourceHistory.getPath()));
        resourceHistory.setClassContent(LcdpJavaCodeResolverUtils.updatePackage(classContent, resourceHistory.getPath()));
        lcdpResourceHistoryService.getDao().update(resourceHistory, "CONTENT", "CLASSCONTENT", "MODIFYVERSION");

        if (isEffectHistory(resourceHistory)) {
            LcdpJavaCodeResolverUtils.removeLoadedProClass(resource);
        } else {
            LcdpJavaCodeResolverUtils.removeLoadedDevClass(resource);
        }

        LcdpJavaCodeResolverUtils.loadAndRegisterSourceCode(resourceHistory);

        if (!Objects.equals(resourceHistory.getCompiledVersion(), resourceHistory.getModifyVersion())) {
            resourceHistory.setCompiledVersion(resourceHistory.getModifyVersion());
            lcdpResourceHistoryService.getDao().update(resourceHistory, "COMPILEDVERSION");
        }

        clearResourceHistoryCaches(resourceHistory);
    }

    private void dealMapperHistory(LcdpResourceHistoryBean resourceHistory) {
        if (StringUtils.isBlank(resourceHistory.getContent())
                || StringUtils.isBlank(resourceHistory.getPath())) {
            return;
        }

        String mapperPath = resourceHistory.getPath();
        if (StringUtils.endsWith(mapperPath, "Mapper")
                && !StringUtils.endsWith(mapperPath, LcdpMapperUtils.DIALECT_MAPPER_SUFFIX)) {
            mapperPath = StringUtils.removeEnd(mapperPath, "Mapper") + LcdpMapperUtils.DIALECT_MAPPER_SUFFIX;
        }

        LcdpMapperUtils.reloadMapper(mapperPath, isEffectHistory(resourceHistory), resourceHistory.getContent());
        clearResourceHistoryCaches(resourceHistory);
    }

    private boolean isEffectHistory(LcdpResourceHistoryBean resourceHistory) {
        return StringUtils.equals(LcdpConstant.EFFECT_FLAG_YES, resourceHistory.getEffectFlag());
    }

    private void clearResourceBaseCaches(LcdpResourceBean resource) {
        evictCache("T_LCDP_RESOURCE", String.valueOf(resource.getId()));
        evictCache("T_LCDP_RESOURCE.BY_PATH", resource.getPath());
        evictCache("T_LCDP_RESOURCE.LATEST_EXECUTED_BY_PATH", resource.getPath());
        evictCache("T_LCDP_RESOURCE.GET_PATH_BY_CLASS_NAME", resource.getResourceName());
    }

    private void refreshJavaResourceCache(LcdpResourceBean resource) {
        LcdpJavaCodeResolverUtils.removeLoadedProClass(resource);

        if (StringUtils.equals(LcdpConstant.RESOURCE_DELETED_NO, resource.getDeleteFlag())
                && StringUtils.isNotBlank(resource.getContent())
                && StringUtils.isNotBlank(resource.getClassContent())) {
            LcdpJavaCodeResolverUtils.loadAndRegisterSourceCode(resource);
            refreshJavaTableMapping(resource.getPath(), LcdpJavaCodeResolverUtils.getBeanName(resource));
        }
    }

    private void refreshJavaTableMapping(String path, String beanName) {
        if (StringUtils.isBlank(path) || StringUtils.isBlank(beanName)) {
            return;
        }

        Object bean = SpringUtils.getBean(beanName);
        if (bean instanceof LcdpBaseService) {
            String tableName = ((LcdpBaseService) bean).getTable();
            ApplicationContextHelper.setLcdpServiceNameByTable(tableName, beanName);
            RedisHelper.put(LcdpConstant.SCRIPT_PATH_TABLE_MAPPING_CACHE, path, tableName);
        }
    }

    private void refreshMapperResourceCache(LcdpResourceBean resource) {
        String mapperPath = normalizeMapperPath(resource.getPath());
        if (StringUtils.isBlank(mapperPath)) {
            return;
        }

        LcdpMapperUtils.unloadMapper(mapperPath, true);

        if (StringUtils.equals(LcdpConstant.RESOURCE_DELETED_NO, resource.getDeleteFlag())
                && StringUtils.isNotBlank(resource.getContent())) {
            LcdpMapperUtils.loadMapper(mapperPath, true, resource.getContent());
        }
    }

    private String normalizeMapperPath(String mapperPath) {
        if (StringUtils.isBlank(mapperPath)) {
            return mapperPath;
        }

        if (StringUtils.endsWith(mapperPath, "Mapper")
                && !StringUtils.endsWith(mapperPath, LcdpMapperUtils.DIALECT_MAPPER_SUFFIX)) {
            return StringUtils.removeEnd(mapperPath, "Mapper") + LcdpMapperUtils.DIALECT_MAPPER_SUFFIX;
        }

        return mapperPath;
    }

    private void clearPageCaches(LcdpResourceBean resource, LcdpResourceHistoryBean resourceHistory) {
        evictCache("T_LCDP_MODULE_PAGE_COMP.BY_MODULEPAGEID", String.valueOf(resource.getId()));
        evictCache("T_LCDP_MODULE_PAGE_I18N.BY_MODULEPAGEHISTORYID", String.valueOf(resourceHistory.getId()));
    }

    private void clearResourceHistoryCaches(LcdpResourceHistoryBean resourceHistory) {
        evictCache("T_LCDP_RESOURCE_HISTORY.DEV_SCRIPT", resourceHistory.getScriptType());
        evictCache("T_LCDP_RESOURCE_HISTORY.BY_RESOURCEID", String.valueOf(resourceHistory.getResourceId()));
        evictCache("T_LCDP_RESOURCE_HISTORY.EXISTS", resourceHistory.getPath());
        evictCache("T_LCDP_RESOURCE_HISTORY.ACTIVE", resourceHistory.getPath());

        if (StringUtils.isNotBlank(resourceHistory.getCreatedById())) {
            evictCache("T_LCDP_RESOURCE_HISTORY.USER_UNSUBMITTED_BY_RESOURCEID", resourceHistory.getCreatedById() + "$" + resourceHistory.getResourceId());
        }
    }

    private void evictCache(String cacheName, Object key) {
        if (key == null) {
            return;
        }

        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    private void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    //-------------------------------------------------------------------------------------------------------------
    // 私有类
    //-------------------------------------------------------------------------------------------------------------

    /**
     * 操作脚本时（复制，移动），资源内容中 Path替换成新的，对于Mapper文件还要特殊处理
     */
    private static class ScriptOperator {
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

    /**
     * 操作模块时（复制，移动），所有资源内容中 分类.模块.目录. 全部替换掉
     */
    private static class ModuleOperator {
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
     * 从大字符串文本中从后往前找到最近的 if( 片段（但不是 else if(），并将内容插入到该片段之前
     *
     * @param sourceCode      原始字符串
     * @param contentToInsert 需要插入的内容
     * @return 修改后的字符串
     */
    private String insertBeforeIf(String sourceCode, String contentToInsert) {
        Matcher matcher = LcdpJavaCodeResolverUtils.ifNotElseIfPattern.matcher(sourceCode);

        // 从后往前查找匹配的 if(
        int ifIndex = -1;
        while (matcher.find()) {
            ifIndex = matcher.start();
            break;
        }

        if (ifIndex == -1) {
            // 如果没有找到 if( 片段，则直接返回原字符串
            return sourceCode;
        }

        // 在 if( 片段之前插入内容
        String modifiedSourceCode = sourceCode.substring(0, ifIndex) + contentToInsert + sourceCode.substring(ifIndex);

        return modifiedSourceCode;
    }


    private String generateValidIndexName(String originalIndexName) {
        // Oracle索引名称最大长度为30个字符
        final int MAX_INDEX_NAME_LENGTH = 30;

        if (originalIndexName == null || originalIndexName.length() <= MAX_INDEX_NAME_LENGTH) {
            return originalIndexName;
        }

        // 方法1: 使用哈希值缩短名称
        String prefix = originalIndexName.substring(0, 20); // 保留前20个字符
        String suffix = String.valueOf(originalIndexName.hashCode()); // 添加哈希值

        String shortenedName = prefix + "_" + suffix;

        // 确保最终名称不超过30个字符
        if (shortenedName.length() > MAX_INDEX_NAME_LENGTH) {
            shortenedName = shortenedName.substring(0, MAX_INDEX_NAME_LENGTH);
        }

        return shortenedName;
    }

    private static class JavaSourceConfig {
        private Long resourceId;
        private String classFullName;
        private String hintName;
        private String sourceCode;
        private String path;
        private Long version;
        private LcdpResourceBean resource;

        public JavaSourceConfig(String classFullName, String hintName, String sourceCode, LcdpResourceBean resource) {
            super();
            this.resourceId = resource.getId();
            this.classFullName = classFullName;
            this.hintName = hintName;
            this.sourceCode = sourceCode;
            this.path = resource.getPath();
            this.version = resource.getEffectVersion();
            this.resource = resource;
        }

        public Long getResourceId() {
            return resourceId;
        }

        public String getPath() {
            return path;
        }

        @SuppressWarnings("unused")
        public Long getVersion() {
            return version;
        }

        public String getHintName() {
            return hintName;
        }

        public String getClassFullName() {
            return classFullName;
        }

        public String getSourceCode() {
            return sourceCode;
        }

        public LcdpResourceBean getResource() {
            return resource;
        }
    }
}
