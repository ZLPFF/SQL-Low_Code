package com.sunwayworld.cloud.boot.listener;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.boot.filter.ScriptRegisterFilter;
import com.sunwayworld.cloud.boot.listener.service.LcdpHistoricalDebtService;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.LcdpWebServiceContext;
import com.sunwayworld.cloud.module.lcdp.base.service.LcdpBaseService;
import com.sunwayworld.cloud.module.lcdp.configparam.bean.LcdpConfigParamBean;
import com.sunwayworld.cloud.module.lcdp.configparam.service.LcdpConfigParamService;
import com.sunwayworld.cloud.module.lcdp.errorscript.bean.LcdpErrorScriptBean;
import com.sunwayworld.cloud.module.lcdp.errorscript.service.LcdpErrorScriptService;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModulePageCompBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.ServerScriptType;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpModulePageCompService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpModulePageI18nService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceCheckoutRecordService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceHistoryService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpViewButtonRoleService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpJavaCodeResolverUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpMapperUtils;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.cache.memory.MemoryCacheManager;
import com.sunwayworld.framework.cache.redis.RedisHelper;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.filter.ContextServletRequestFilter;
import com.sunwayworld.framework.data.ChunkIterator;
import com.sunwayworld.framework.data.ListChunkIterator;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.executor.manager.TaskExecutorManager;
import com.sunwayworld.framework.jdk.core.ClassManager;
import com.sunwayworld.framework.jdk.tools.DynamicClassLoader;
import com.sunwayworld.framework.jdk.tools.LoadMultipleResult;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.spring.filter.GikamFilterRegistry;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.EncryptUtils;
import com.sunwayworld.framework.utils.FileUtils;
import com.sunwayworld.framework.utils.SpringUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.admin.config.service.CoreAdminSelectConfigService;
import com.sunwayworld.module.sys.constant.bean.CoreConstantBean;
import com.sunwayworld.module.sys.constant.service.CoreConstantService;
import com.sunwayworld.module.sys.i18n.bean.CoreI18nMessageBean;
import com.sunwayworld.module.sys.i18n.service.CoreI18nMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

/**
 * 应用加载完毕后执行
 * --Java脚本注册到spring容器
 * --脚本智能提醒初始化
 */
@Service
@ConditionalOnProperty(prefix = "sunway.lcdp", name = "enable", havingValue = "true", matchIfMissing = true)
public class LcdpReadyListener implements ApplicationListener<ApplicationPreparedEvent> {
    private static final String STARTUP_COMPILE_CACHE_ENABLED = "sunway.lcdp.startup.compile-cache.enabled";
    private static final String STARTUP_COMPILE_CACHE_VERSION = "1";
    private static final String STARTUP_COMPILE_CACHE_FOLDER = "sunway/lcdp-startup-compile-cache";

    @Autowired
    private LcdpResourceService lcdpResourceService;

    @Autowired
    private LcdpResourceHistoryService lcdpResourceHistoryService;

    @Autowired
    private LcdpModulePageCompService lcdpModulePageCompService;

    @Autowired
    private LcdpModulePageI18nService lcdpModulePageI18nService;

    @Autowired
    private CoreAdminSelectConfigService coreAdminSelectConfigService;

    @Autowired
    private CoreI18nMessageService coreI18nMessageService;

    @Autowired
    private CoreConstantService coreConstantService;

    @Autowired
    private LcdpConfigParamService lcdpConfigParamService;

    @Autowired
    private LcdpModulePageCompService modulePageCompService;

    @Autowired
    private LcdpResourceCheckoutRecordService resourceCheckoutRecordService;

    @Autowired
    private LcdpErrorScriptService errorScriptService;

    @Autowired
    private LcdpHistoricalDebtService historicalDebtService;

    @Autowired
    private LcdpViewButtonRoleService viewButtonRoleService;

    private static final Logger log = LoggerFactory.getLogger(LcdpReadyListener.class);

    static {
        GikamFilterRegistry.INSTANCE.registerAfter(new ScriptRegisterFilter(), ContextServletRequestFilter.class);
    }

    /**
     * 应用加载完毕后执行的方法
     */
    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        long now = System.currentTimeMillis();

        //初始化应用uuid
        initAppUUID();

        // 更新资源里的分类和模块的ID，处理历史数据
        historicalDebtService.updateResourceModuleAndCategoryId();

        // 1.已提交的java脚本注册
        SearchFilter resourceFilter = SearchFilter.instance()
                .match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_JAVA).filter(MatchPattern.EQ)
                .match("EFFECTVERSION", 0).filter(MatchPattern.NG)
                .match("CLASSCONTENT", null).filter(MatchPattern.DIFFER)
                .match("PATH", null).filter(MatchPattern.DIFFER)
                .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ);
        List<LcdpResourceBean> resourceList = lcdpResourceService.selectListByMultiThread(resourceFilter, 100);

        // 处理历史数据，添加包
        historicalDebtService.updateResourceIfNecessary(resourceList);

        List<SourceConfig> committedAspectList = new ArrayList<>(); //已提交的切面
        List<SourceConfig> committedList = new ArrayList<>(); // 已提交的数据
        resourceList.forEach(r -> {
            SourceConfig sc = new SourceConfig(r);

            if (!StringUtils.isBlank(sc.getClassName())) {
                if (ServerScriptType.aspect.name().equals(r.getScriptType())) {
                    committedAspectList.add(new SourceConfig(r));
                } else {
                    committedList.add(new SourceConfig(r));
                }
            }
        });

        SearchFilter historyFilter = SearchFilter.instance()
                .match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_JAVA).filter(MatchPattern.EQ)
                .match("CLASSCONTENT", null).filter(MatchPattern.DIFFER)
                .match("PATH", null).filter(MatchPattern.DIFFER)
                .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ)
                .match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.EQ);
        List<LcdpResourceHistoryBean> resourceHistoryList = lcdpResourceHistoryService.selectListByMultiThread(historyFilter, 100);

        // 处理历史数据，添加包
        historicalDebtService.updateResourceHistoryIfNecessary(resourceHistoryList);

        List<SourceConfig> uncommittedAspectList = new ArrayList<>(); // 未提交的切面
        List<SourceConfig> uncommittedList = new ArrayList<>(); // 未提交的数据
        resourceHistoryList.forEach(r -> {
            SourceConfig sc = new SourceConfig(r);

            if (!StringUtils.isBlank(sc.getClassName())) {
                if (ServerScriptType.aspect.name().equals(r.getScriptType())) {
                    uncommittedAspectList.add(sc);
                } else {
                    uncommittedList.add(sc);
                }
            }
        });

        List<LcdpErrorScriptBean> errorScriptList = new ArrayList<>();

        // 需要优先注册Aspect类型的脚本
        compileAndRegister(committedAspectList, errorScriptList, "submit");
        List<SourceConfig> committedAspectFailedList = committedAspectList.stream().filter(c -> !StringUtils.isBlank(c.getCompileErrorMessage())).collect(Collectors.toList());
        log.info("编译并注册已生效的Aspect源代码，成功{}项，失败{}项", committedAspectList.size() - committedAspectFailedList.size(), committedAspectFailedList.size());
        if (!committedAspectFailedList.isEmpty()) {
            printFailedCompileMessage("已生效的切面源代码编译失败详细信息如下，需要人工修复", committedAspectFailedList);
        }


        compileAndRegister(uncommittedAspectList, errorScriptList, "checkout");
        List<SourceConfig> uncommittedAspectFailedList = uncommittedAspectList.stream().filter(c -> !StringUtils.isBlank(c.getCompileErrorMessage())).collect(Collectors.toList());
        log.info("编译并注册未生效的Aspect源代码，成功{}项，失败{}项", uncommittedAspectList.size() - uncommittedAspectFailedList.size(), uncommittedAspectFailedList.size());

        //添加aspect注册完成的标识码，BeanFactoryAspectJAdvisorsBuilder中能够获取低代码的切面类
        MemoryCacheManager.put(LcdpConstant.LCDP_ASPECT_FLAG, Constant.YES);

        compileAndRegister(committedList, errorScriptList, "submit");
        List<SourceConfig> committedFailedList = committedList.stream().filter(c -> !StringUtils.isBlank(c.getCompileErrorMessage())).collect(Collectors.toList());
        log.info("编译并注册已生效的Java源代码，成功{}项，失败{}项", committedList.size() - committedFailedList.size(), committedFailedList.size());
        if (!committedFailedList.isEmpty()) {
            printFailedCompileMessage("已生效的Java源代码编译失败详细信息如下，需要人工修复", committedFailedList);
        }

        //注册未提交的java脚本，需要使用检出的切面
        MemoryCacheManager.put(LcdpConstant.LCDP_ASPECT_UNCOMMITTED_FLAG, Constant.YES);
        compileAndRegister(uncommittedList, errorScriptList, "checkout");
        List<SourceConfig> uncommittedFailedList = uncommittedList.stream().filter(c -> !StringUtils.isBlank(c.getCompileErrorMessage())).collect(Collectors.toList());
        log.info("编译并注册未生效的Java源代码，成功{}项，失败{}项", uncommittedList.size() - uncommittedFailedList.size(), uncommittedFailedList.size());
        MemoryCacheManager.remove(LcdpConstant.LCDP_ASPECT_UNCOMMITTED_FLAG);

        log.info("============>Java资源已加载完毕，耗时：" + ((System.currentTimeMillis() - now) / 1000) + "秒");

        long mapperStart = System.currentTimeMillis();

        //1.检出的和新建的mapper注册,查询未提交的mapper即可
        SearchFilter historyMapperFilter = SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_MAPPER).filter(MatchPattern.EQ)
                .match("CONTENT", null).filter(MatchPattern.DIFFER)
                .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ)
                .match("SUBMITFLAG", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.EQ)
                .match("PATH", LcdpMapperUtils.DIALECT_MAPPER_SUFFIX).filter(MatchPattern.SE);
        List<LcdpResourceHistoryBean> mapperHistoryList = lcdpResourceHistoryService.selectListByMultiThread(historyMapperFilter, 50);

        //2.已提交的mapper注册
        SearchFilter mapperFilter = SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_MAPPER).filter(MatchPattern.EQ)
                .match("EFFECTVERSION", 0).filter(MatchPattern.NG)
                .match("CONTENT", null).filter(MatchPattern.DIFFER)
                .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ)
                .match("PATH", LcdpMapperUtils.DIALECT_MAPPER_SUFFIX).filter(MatchPattern.SE);
        List<LcdpResourceBean> mapperList = lcdpResourceService.selectListByMultiThread(mapperFilter, 50);

        LcdpMapperUtils.batchLoadMapper(mapperList);

        for (LcdpResourceHistoryBean mapper : mapperHistoryList) {
            registerMapper(mapper.getPath(), false, mapper.getContent());
        }

        log.info("============>mapper已加载完毕，耗时：" + ((System.currentTimeMillis() - mapperStart) / 1000) + "秒");

        //低代码脚本注册完成
        MemoryCacheManager.put(LcdpConstant.REGISTER_FINISH_KEY, Constant.YES);

        //因全局配置调整处理历史页面数据
        dealHistoryPageCompsForGlobalConfig();

        resourceCheckoutRecordService.dealResourceCheckoutRecord();

        coreAdminSelectConfigService.supplementSelectConfig();

        //处理历史页面国际化数据
        dealHistoryPageCompI18n();


        dealHistoryPageCompI18nGridAndForm();

        //处理历史页面重复ID问题
        dealDuplicateIdCompsCompI18n();

        dealHistoryGridScroll();

        dealHistoryGridLoadingMode();

        dealHistoryGridBadgeCount();

        dealHistoryGridShowCheckedNum();

        dealHistoryGridCheckContinuous();

        dealHistoryShowCheckedNum2Null();

        //初始化接口集成webservice服务
        LcdpWebServiceContext.init();

        //插入加载错误信息
        insertScriptError(errorScriptList);

        initViewButtonRole();

    }


    private void insertScriptError(List<LcdpErrorScriptBean> errorScriptList) {
        // 启动的时候删除所有错误脚本
        List<Long> errorScriptIdList = errorScriptService.getDao().selectAll("ID").stream().map(e -> e.getId()).collect(Collectors.toList());
        errorScriptService.getDao().deleteByIdList(errorScriptIdList);

        ChunkIterator<LcdpErrorScriptBean> errorScriptIterator = ListChunkIterator.of(errorScriptList, 10);

        CountDownLatch latch = new CountDownLatch((int) Math.ceil(Double.valueOf("" + errorScriptList.size()) / 10));
        while (errorScriptIterator.hasNext()) {
            List<LcdpErrorScriptBean> nextChunk = errorScriptIterator.nextChunk();

            TaskExecutorManager.getDefaultRunner().submit(() -> {
                try {
                    errorScriptService.getDao().fastInsert(nextChunk);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException ie) {
            throw new ApplicationRuntimeException(ie);
        }
    }

    private void initAppUUID() {
        List<LcdpConfigParamBean> appUUIDList = lcdpConfigParamService.selectListByFilter(SearchFilter.instance().match("paramCode", "appuuid").filter(MatchPattern.EQ));

        if (appUUIDList.isEmpty()) {
            LcdpConfigParamBean AppUUID = new LcdpConfigParamBean();
            AppUUID.setId(ApplicationContextHelper.getNextIdentity());
            AppUUID.setParamCode("appuuid");
            AppUUID.setParamValue(StringUtils.randomUUID());

            lcdpConfigParamService.getDao().insert(AppUUID);
        }
    }


    private void dealHistoryGridLoadingMode() {
        long dealStart = System.currentTimeMillis();

        CoreConstantBean queryCoreConstant = coreConstantService.selectByIdIfPresent("LCDP_GRID_LOADING_MODE_DEAL");
        if (queryCoreConstant != null) {
            return;
        }
        List<LcdpResourceHistoryBean> effectResourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_VIEW).filter(MatchPattern.EQ).match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.EQ).match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ));
        List<LcdpResourceHistoryBean> unSubmitResourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_VIEW).filter(MatchPattern.EQ).match("submitFlag", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.EQ).match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ));
        List<Long> resoureIdList = unSubmitResourceHistoryList.stream().map(LcdpResourceHistoryBean::getResourceId).collect(Collectors.toList());
        effectResourceHistoryList.removeIf(effect -> resoureIdList.contains(effect.getResourceId()));
        effectResourceHistoryList.addAll(unSubmitResourceHistoryList);

        ListChunkIterator<LcdpResourceHistoryBean> chunkIterator = ListChunkIterator.of(effectResourceHistoryList, 100);
        while (chunkIterator.hasNext()) {
            List<LcdpResourceHistoryBean> chunkItemList = chunkIterator.nextChunk();
            modulePageCompService.dealLoadingMode(chunkItemList);
        }

        CoreConstantBean coreConstant = new CoreConstantBean();
        coreConstant.setId("LCDP_GRID_LOADING_MODE_DEAL");
        coreConstant.setConstantValue("1");
        coreConstant.setConstantDesc("低代码平台Grid加载中动画处理");
        coreConstant.setClassfication("系统");
        coreConstantService.getDao().insert(coreConstant);
        log.info("============>低代码平台Grid加载中动画处理，耗时：" + ((System.currentTimeMillis() - dealStart) / 1000) + "秒");
    }

    private void dealHistoryGridScroll() {
        long dealStart = System.currentTimeMillis();

        CoreConstantBean queryCoreConstant = coreConstantService.selectByIdIfPresent("LCDP_GRID_SCROLL_DEAL");
        if (queryCoreConstant != null) {
            return;
        }
        List<LcdpResourceHistoryBean> effectResourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_VIEW).filter(MatchPattern.EQ).match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.EQ).match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ));
        List<LcdpResourceHistoryBean> unSubmitResourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_VIEW).filter(MatchPattern.EQ).match("submitFlag", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.EQ).match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ));
        List<Long> resoureIdList = unSubmitResourceHistoryList.stream().map(LcdpResourceHistoryBean::getResourceId).collect(Collectors.toList());
        effectResourceHistoryList.removeIf(effect -> resoureIdList.contains(effect.getResourceId()));
        effectResourceHistoryList.addAll(unSubmitResourceHistoryList);

        ListChunkIterator<LcdpResourceHistoryBean> chunkIterator = ListChunkIterator.of(effectResourceHistoryList, 100);
        while (chunkIterator.hasNext()) {
            List<LcdpResourceHistoryBean> chunkItemList = chunkIterator.nextChunk();
            modulePageCompService.dealGridSroll(chunkItemList);
        }

        CoreConstantBean coreConstant = new CoreConstantBean();
        coreConstant.setId("LCDP_GRID_SCROLL_DEAL");
        coreConstant.setConstantValue("1");
        coreConstant.setConstantDesc("低代码平台Grid滚动条处理");
        coreConstant.setClassfication("系统");
        coreConstantService.getDao().insert(coreConstant);
        log.info("============>低代码平台Grid滚动条处理，耗时：" + ((System.currentTimeMillis() - dealStart) / 1000) + "秒");


    }


    private void dealHistoryGridBadgeCount() {
        long dealStart = System.currentTimeMillis();

        CoreConstantBean queryCoreConstant = coreConstantService.selectByIdIfPresent("LCDP_GRID_BADGE_COUNT_DEAL");
        if (queryCoreConstant != null) {
            return;
        }
        List<LcdpResourceHistoryBean> effectResourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_VIEW).filter(MatchPattern.EQ).match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.EQ).match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ));
        List<LcdpResourceHistoryBean> unSubmitResourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_VIEW).filter(MatchPattern.EQ).match("submitFlag", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.EQ).match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ));
        List<Long> resoureIdList = unSubmitResourceHistoryList.stream().map(LcdpResourceHistoryBean::getResourceId).collect(Collectors.toList());
        effectResourceHistoryList.removeIf(effect -> resoureIdList.contains(effect.getResourceId()));
        effectResourceHistoryList.addAll(unSubmitResourceHistoryList);

        ListChunkIterator<LcdpResourceHistoryBean> chunkIterator = ListChunkIterator.of(effectResourceHistoryList, 100);
        while (chunkIterator.hasNext()) {
            List<LcdpResourceHistoryBean> chunkItemList = chunkIterator.nextChunk();
            modulePageCompService.dealGridBadgeCount(chunkItemList);
        }

        CoreConstantBean coreConstant = new CoreConstantBean();
        coreConstant.setId("LCDP_GRID_BADGE_COUNT_DEAL");
        coreConstant.setConstantValue("1");
        coreConstant.setConstantDesc("低代码平台Grid最大显示数量处理");
        coreConstant.setClassfication("系统");
        coreConstantService.getDao().insert(coreConstant);
        log.info("============>低代码平台Grid最大显示数量处理，耗时：" + ((System.currentTimeMillis() - dealStart) / 1000) + "秒");


    }

    private void dealHistoryGridShowCheckedNum() {
        long dealStart = System.currentTimeMillis();

        CoreConstantBean queryCoreConstant = coreConstantService.selectByIdIfPresent("LCDP_GRID_SHOW_CHECKED_NUM");
        if (queryCoreConstant != null) {
            return;
        }
        List<LcdpResourceHistoryBean> effectResourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_VIEW).filter(MatchPattern.EQ).match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.EQ).match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ));
        List<LcdpResourceHistoryBean> unSubmitResourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_VIEW).filter(MatchPattern.EQ).match("submitFlag", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.EQ).match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ));
        List<Long> resoureIdList = unSubmitResourceHistoryList.stream().map(LcdpResourceHistoryBean::getResourceId).collect(Collectors.toList());
        effectResourceHistoryList.removeIf(effect -> resoureIdList.contains(effect.getResourceId()));
        effectResourceHistoryList.addAll(unSubmitResourceHistoryList);

        ListChunkIterator<LcdpResourceHistoryBean> chunkIterator = ListChunkIterator.of(effectResourceHistoryList, 100);
        while (chunkIterator.hasNext()) {
            List<LcdpResourceHistoryBean> chunkItemList = chunkIterator.nextChunk();
            modulePageCompService.dealGridShowCheckedNum(chunkItemList);
        }

        CoreConstantBean coreConstant = new CoreConstantBean();
        coreConstant.setId("LCDP_GRID_SHOW_CHECKED_NUM");
        coreConstant.setConstantValue("1");
        coreConstant.setConstantDesc("低代码平台Grid显示选中数量");
        coreConstant.setClassfication("系统");
        coreConstantService.getDao().insert(coreConstant);
        log.info("============>低代码平台Grid显示选中数量，耗时：" + ((System.currentTimeMillis() - dealStart) / 1000) + "秒");

    }

    private void dealHistoryShowCheckedNum2Null() {
        long dealStart = System.currentTimeMillis();

        CoreConstantBean queryCoreConstant = coreConstantService.selectByIdIfPresent("LCDP_GRID_CHECKED_NUM_TO_NULL");
        if (queryCoreConstant != null) {
            return;
        }
        List<LcdpResourceHistoryBean> effectResourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_VIEW).filter(MatchPattern.EQ).match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.EQ).match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ));
        List<LcdpResourceHistoryBean> unSubmitResourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_VIEW).filter(MatchPattern.EQ).match("submitFlag", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.EQ).match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ));
        List<Long> resoureIdList = unSubmitResourceHistoryList.stream().map(LcdpResourceHistoryBean::getResourceId).collect(Collectors.toList());
        effectResourceHistoryList.removeIf(effect -> resoureIdList.contains(effect.getResourceId()));
        effectResourceHistoryList.addAll(unSubmitResourceHistoryList);

        ListChunkIterator<LcdpResourceHistoryBean> chunkIterator = ListChunkIterator.of(effectResourceHistoryList, 100);
        while (chunkIterator.hasNext()) {
            List<LcdpResourceHistoryBean> chunkItemList = chunkIterator.nextChunk();
            modulePageCompService.dealHistoryShowCheckedNum2Null(chunkItemList);
        }

        CoreConstantBean coreConstant = new CoreConstantBean();
        coreConstant.setId("LCDP_GRID_CHECKED_NUM_TO_NULL");
        coreConstant.setConstantValue("1");
        coreConstant.setConstantDesc("低代码平台Grid显示选中数量");
        coreConstant.setClassfication("系统");
        coreConstantService.getDao().insert(coreConstant);
        log.info("============>低代码平台Grid显示选中数量，耗时：" + ((System.currentTimeMillis() - dealStart) / 1000) + "秒");

    }

    private void dealHistoryGridCheckContinuous() {
        long dealStart = System.currentTimeMillis();

        CoreConstantBean queryCoreConstant = coreConstantService.selectByIdIfPresent("LCDP_GRID_CHECK_CONTINUOUS");
        if (queryCoreConstant != null) {
            return;
        }

        List<LcdpResourceHistoryBean> effectResourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_VIEW).filter(MatchPattern.EQ).match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.EQ).match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ));
        List<LcdpResourceHistoryBean> unSubmitResourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_VIEW).filter(MatchPattern.EQ).match("submitFlag", LcdpConstant.SUBMIT_FLAG_NO).filter(MatchPattern.EQ).match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ));
        List<Long> resoureIdList = unSubmitResourceHistoryList.stream().map(LcdpResourceHistoryBean::getResourceId).collect(Collectors.toList());
        effectResourceHistoryList.removeIf(effect -> resoureIdList.contains(effect.getResourceId()));
        effectResourceHistoryList.addAll(unSubmitResourceHistoryList);

        ListChunkIterator<LcdpResourceHistoryBean> chunkIterator = ListChunkIterator.of(effectResourceHistoryList, 100);
        while (chunkIterator.hasNext()) {
            List<LcdpResourceHistoryBean> chunkItemList = chunkIterator.nextChunk();
            modulePageCompService.dealHistoryGridCheckContinuous(chunkItemList);
        }

        CoreConstantBean coreConstant = new CoreConstantBean();
        coreConstant.setId("LCDP_GRID_CHECK_CONTINUOUS");
        coreConstant.setConstantValue("1");
        coreConstant.setConstantDesc("低代码平台Grid保留选中数量");
        coreConstant.setClassfication("系统");
        coreConstantService.getDao().insert(coreConstant);
        log.info("============>低代码平台Grid保留选中数量，耗时：" + ((System.currentTimeMillis() - dealStart) / 1000) + "秒");
    }

    private void dealHistoryPageCompsForGlobalConfig() {
        long dealStart = System.currentTimeMillis();
        CoreConstantBean queryCoreConstant = coreConstantService.selectByIdIfPresent("LCDP_GLOBAL_CONFIG_DEAL");
        if (queryCoreConstant != null) {
            return;
        }
        List<LcdpResourceBean> resourceList = lcdpResourceService.selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_VIEW).filter(MatchPattern.EQ).match("EFFECTVERSION", null).filter(MatchPattern.DIFFER).match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ));
        List<Long> resourceIdList = resourceList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
        List<LcdpResourceHistoryBean> resourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", resourceIdList).filter(MatchPattern.OR).match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.EQ));
        List<Long> resourceHistoryIdList = resourceHistoryList.stream().map(LcdpResourceHistoryBean::getId).collect(Collectors.toList());

        List<LcdpModulePageCompBean> pageCompList = lcdpModulePageCompService.selectListByFilter(SearchFilter.instance().match("modulePageHistoryId", resourceHistoryIdList).filter(MatchPattern.OR));

        List<String> dealTypeList = Arrays.asList("Grid", "Uploader", "WindowToolbar", "GridToolbar");
        List<String> fieldTypeList = Arrays.asList("SELECT", "DATE", "DATETIME", "YEAR", "MONTH", "TIME");
        List<String> timeTypeList = Arrays.asList("DATE", "DATETIME", "YEAR", "MONTH", "TIME");
        List<LcdpModulePageCompBean> dealPageCompList = pageCompList.stream().filter(comp -> dealTypeList.contains(comp.getType())).collect(Collectors.toList());
        List<LcdpModulePageCompBean> dealFieldList = pageCompList.stream().filter(comp -> {
            String type = JSON.parseObject(comp.getConfig()).getString("type");
            if (StringUtils.isEmpty(type)) {
                return false;
            }
            if (fieldTypeList.contains(type.toUpperCase())) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        dealPageCompList.forEach(comp -> {
            JSONObject configObject = JSON.parseObject(comp.getConfig());
            switch (comp.getType()) {
                case "Grid":
                    configObject.remove("filterOpen");
                    configObject.remove("toolbarAlign");
                    configObject.remove("editorInvisible");
                    configObject.remove("columnsFill");
                    configObject.remove("contentAlign");
                    configObject.remove("checkOnActive");
                    configObject.remove("checkOneOnActive");
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
        dealFieldList.forEach(comp -> {
            JSONObject configObject = JSON.parseObject(comp.getConfig());
            String type = configObject.getString("type");
            if (StringUtils.equalsIgnoreCase(type, "select")) {
                configObject.remove("search");
                configObject.remove("firstBlank");
            }
            if (timeTypeList.contains(type.toUpperCase())) {
                configObject.remove("editable");
            }
            comp.setConfig(configObject.toJSONString());
        });
        dealPageCompList.addAll(dealFieldList);
        lcdpModulePageCompService.getDao().update(dealPageCompList, "CONFIG");

        CoreConstantBean coreConstant = new CoreConstantBean();
        coreConstant.setId("LCDP_GLOBAL_CONFIG_DEAL");
        coreConstant.setConstantValue("1");
        coreConstant.setConstantDesc("低代码平台历史数据处理");
        coreConstant.setClassfication("系统");
        coreConstantService.getDao().insert(coreConstant);
        log.info("============>全部配置属性历史数据已处理完毕，耗时：" + ((System.currentTimeMillis() - dealStart) / 1000) + "秒");

    }

    private void dealDuplicateIdCompsCompI18n() {

        CoreConstantBean queryCoreConstant = coreConstantService.selectByIdIfPresent("LCDP_PAGE_I18N_DUPLICATEID_DEAL");
        if (queryCoreConstant != null) {
            return;
        }
        long dealStart = System.currentTimeMillis();
        //所有页面resource
        List<LcdpResourceBean> resourceList = lcdpResourceService.selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_VIEW).filter(MatchPattern.EQ).match("EFFECTVERSION", null).filter(MatchPattern.DIFFER).match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ));
        List<Long> resourceIdList = resourceList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
        //所有页面生效历史resource
        List<LcdpResourceHistoryBean> resourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", resourceIdList).filter(MatchPattern.OR).match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.EQ));


        ListChunkIterator<LcdpResourceHistoryBean> chunkIterator = ListChunkIterator.of(resourceHistoryList, 100);
        while (chunkIterator.hasNext()) {
            List<LcdpResourceHistoryBean> chunkItemList = chunkIterator.nextChunk();
            lcdpModulePageI18nService.batchDealHistoryPageCompI18nForDuplicateId(chunkItemList);
        }


        CoreConstantBean coreConstant = new CoreConstantBean();
        coreConstant.setId("LCDP_PAGE_I18N_DUPLICATEID_DEAL");
        coreConstant.setConstantValue("1");
        coreConstant.setConstantDesc("低代码平台页面国际化组件重复ID历史数据处理");
        coreConstant.setClassfication("系统");
        coreConstantService.getDao().insert(coreConstant);
        log.info("============>低代码平台页面国际化历史数据处理已处理完毕，耗时：" + ((System.currentTimeMillis() - dealStart) / 1000) + "秒");
    }


    public void dealHistoryPageCompI18nGridAndForm() {
        CoreConstantBean queryCoreConstant = coreConstantService.selectByIdIfPresent("LCDP_PAGE_I18N_DEAL_GRID_FORM");
        if (queryCoreConstant != null) {
            return;
        }
        long dealStart = System.currentTimeMillis();
        //所有页面resource
        List<LcdpResourceBean> resourceList = lcdpResourceService.selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_VIEW).filter(MatchPattern.EQ).match("EFFECTVERSION", null).filter(MatchPattern.DIFFER).match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ));
        List<Long> resourceIdList = resourceList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
        //所有页面生效历史resource
        List<LcdpResourceHistoryBean> resourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", resourceIdList).filter(MatchPattern.OR).match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.EQ));
        //按钮相关系统国际化
        List<CoreI18nMessageBean> coreI18nMessageList = coreI18nMessageService.selectListByFilter(SearchFilter.instance()
                .match("CODE", "GIKAM").filter(MatchPattern.SB)
                .match("I18NCONFIGID", "zh-CN").filter(MatchPattern.SEQ));

        Map<String, String> i18nMessageMap = new HashMap<>();
        coreI18nMessageList.forEach(coreI18nMessageBean -> {
            i18nMessageMap.put(coreI18nMessageBean.getMessage(), coreI18nMessageBean.getExt$Item("CODE"));
        });

        ListChunkIterator<LcdpResourceHistoryBean> chunkIterator = ListChunkIterator.of(resourceHistoryList, 100);
        while (chunkIterator.hasNext()) {
            List<LcdpResourceHistoryBean> chunkItemList = chunkIterator.nextChunk();
            lcdpModulePageI18nService.batchDealHistoryPageCompI18nGridAndForm(chunkItemList, i18nMessageMap, new ArrayList<>());
        }


        CoreConstantBean coreConstant = new CoreConstantBean();
        coreConstant.setId("LCDP_PAGE_I18N_DEAL_GRID_FORM");
        coreConstant.setConstantValue("1");
        coreConstant.setConstantDesc("低代码平台页面国际化历史数据处理(GridForm)");
        coreConstant.setClassfication("系统");
        coreConstantService.getDao().insert(coreConstant);
        log.info("============>低代码平台页面国际化历史数据处理已处理完毕，耗时：" + ((System.currentTimeMillis() - dealStart) / 1000) + "秒");
    }

    public void dealHistoryPageCompI18n() {
        CoreConstantBean queryCoreConstant = coreConstantService.selectByIdIfPresent("LCDP_PAGE_I18N_DEAL");
        if (queryCoreConstant != null) {
            return;
        }
        long dealStart = System.currentTimeMillis();
        //所有页面resource
        List<LcdpResourceBean> resourceList = lcdpResourceService.selectListByFilter(SearchFilter.instance().match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_VIEW).filter(MatchPattern.EQ).match("EFFECTVERSION", null).filter(MatchPattern.DIFFER).match("DELETEFLAG", Constant.NO).filter(MatchPattern.EQ));
        List<Long> resourceIdList = resourceList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());
        //所有页面生效历史resource
        List<LcdpResourceHistoryBean> resourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", resourceIdList).filter(MatchPattern.OR).match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.EQ));
        //按钮相关系统国际化
        List<CoreI18nMessageBean> coreI18nMessageList = coreI18nMessageService.selectListByFilter(SearchFilter.instance()
                .match("CODE", "GIKAM").filter(MatchPattern.SB)
                .match("I18NCONFIGID", "zh-CN").filter(MatchPattern.SEQ));

        Map<String, String> i18nMessageMap = new HashMap<>();
        coreI18nMessageList.forEach(coreI18nMessageBean -> {
            i18nMessageMap.put(coreI18nMessageBean.getMessage(), coreI18nMessageBean.getExt$Item("CODE"));
        });

        ListChunkIterator<LcdpResourceHistoryBean> chunkIterator = ListChunkIterator.of(resourceHistoryList, 100);
        while (chunkIterator.hasNext()) {
            List<LcdpResourceHistoryBean> chunkItemList = chunkIterator.nextChunk();
            lcdpModulePageI18nService.batchDealHistoryPageCompI18n(chunkItemList, i18nMessageMap);
        }


        CoreConstantBean coreConstant = new CoreConstantBean();
        coreConstant.setId("LCDP_PAGE_I18N_DEAL");
        coreConstant.setConstantValue("1");
        coreConstant.setConstantDesc("低代码平台页面国际化历史数据处理");
        coreConstant.setClassfication("系统");
        coreConstantService.getDao().insert(coreConstant);
        log.info("============>低代码平台页面国际化历史数据处理已处理完毕，耗时：" + ((System.currentTimeMillis() - dealStart) / 1000) + "秒");
    }

    private void initViewButtonRole() {
        CoreConstantBean queryCoreConstant = coreConstantService.selectByIdIfPresent("LCDP_VIEW_BUTTON_ROLE");
        if (queryCoreConstant != null) {
            return;
        }
        long dealStart = System.currentTimeMillis();

        viewButtonRoleService.initMenuPageData();


        CoreConstantBean coreConstant = new CoreConstantBean();
        coreConstant.setId("LCDP_VIEW_BUTTON_ROLE");
        coreConstant.setConstantValue("1");
        coreConstant.setConstantDesc("低代码平台页面按钮权限控制初始化页面");
        coreConstant.setClassfication("系统");
        coreConstantService.getDao().insert(coreConstant);
        log.info("============>低代码平台页面按钮权限控制初始化页面处理已处理完毕，耗时：" + ((System.currentTimeMillis() - dealStart) / 1000) + "秒");
    }

    /**
     * 编译脚本并注册到Spring容器中，返回注册成功的数量
     */
    private void compileAndRegister(List<SourceConfig> sourceConfigList, List<LcdpErrorScriptBean> errorScriptList, String scriptStatus) {
        if (sourceConfigList.isEmpty()) {
            return;
        }

        Map<String, SourceConfig> sourceConfigMap = sourceConfigList.stream().collect(Collectors.toMap(
                SourceConfig::getClassFullName,
                sourceConfig -> sourceConfig,
                (left, right) -> left,
                LinkedHashMap::new
        ));

        LoadMultipleResult result = loadStartupCompileCache(sourceConfigList);
        if (result != null) {
            if (!CollectionUtils.isEmpty(result.getErrorMessageMap())) {
                markCompileError(result.getErrorMessageMap(), sourceConfigMap, errorScriptList, scriptStatus);
            }
        } else {
            result = compileWithoutCache(sourceConfigList, sourceConfigMap, errorScriptList, scriptStatus);
            saveStartupCompileCache(sourceConfigList, result);
        }

        List<Long> historyIdList = new ArrayList<>();
        if (result.getClassMap() != null) {
            result.getClassMap().forEach((k, c) -> {
                String classFullName = StringUtils.replace(k, "/", ".");

                SourceConfig sourceConfig = sourceConfigMap.get(classFullName);

                if (sourceConfig == null) {
                    return;
                }

                // 清除错误日志
                sourceConfig.setCompileErrorMessage(null);

                LcdpJavaCodeResolverUtils.registerBean(c);

                if (sourceConfig.isFromHistory()) {
                    historyIdList.add(sourceConfig.getResourceId());
                }

                String beanName = LcdpJavaCodeResolverUtils.getBeanName(c);

                //脚本类对象,表名建立映射
                if (LcdpBaseService.class.isAssignableFrom(c)) {
                    LcdpBaseService scriptService = SpringUtils.getBean(beanName);
                    String tableName = scriptService.getTable();
                    ApplicationContextHelper.setLcdpServiceNameByTable(tableName, beanName);

                    //脚本路径关联表名
                    RedisHelper.put(LcdpConstant.SCRIPT_PATH_TABLE_MAPPING_CACHE, sourceConfig.getPath(), tableName);
                }
            });

            // 更新编译版本
            lcdpResourceHistoryService.updateCompiledVersionIfNecessary(historyIdList);
        }
    }

    private LoadMultipleResult compileWithoutCache(List<SourceConfig> sourceConfigList, Map<String, SourceConfig> sourceConfigMap, List<LcdpErrorScriptBean> errorScriptList, String scriptStatus) {
        List<SourceConfig> runningSourceConfigList = new ArrayList<>(sourceConfigList);
        Map<String, String> compileErrorMessageMap = new LinkedHashMap<>();
        Map<String, byte[]> compiledBytesMap = new LinkedHashMap<>();

        LoadMultipleResult result = comile(runningSourceConfigList);
        collectCompiledBytes(result, compiledBytesMap);
        removeCompiledSource(runningSourceConfigList, result);

        // 有异常时，排除异常的类后重新编译
        while (!CollectionUtils.isEmpty(result.getErrorMessageMap())) {
            result.getErrorMessageMap().forEach((k, v) -> {
                String classFullName = StringUtils.replace(k, "/", ".");
                compileErrorMessageMap.put(classFullName, v);

                SourceConfig sourceConfig = sourceConfigMap.get(classFullName);
                if (sourceConfig != null) {
                    runningSourceConfigList.removeIf(t -> classFullName.equals(t.getClassFullName()));
                }
            });
            markCompileError(result.getErrorMessageMap(), sourceConfigMap, errorScriptList, scriptStatus);

            // 所有的源代码都报错
            if (runningSourceConfigList.isEmpty()) {
                break;
            }

            result = comile(runningSourceConfigList);
            collectCompiledBytes(result, compiledBytesMap);
            removeCompiledSource(runningSourceConfigList, result);
        }

        result.setBytesMap(compiledBytesMap);
        result.setClassMap(DynamicClassLoader.getInstance().loadCompiledBytes(compiledBytesMap));
        result.setErrorMessageMap(compileErrorMessageMap);
        return result;
    }

    private void collectCompiledBytes(LoadMultipleResult result, Map<String, byte[]> compiledBytesMap) {
        if (result == null || CollectionUtils.isEmpty(result.getBytesMap())) {
            return;
        }

        compiledBytesMap.putAll(result.getBytesMap());
    }

    private void removeCompiledSource(List<SourceConfig> runningSourceConfigList, LoadMultipleResult result) {
        if (CollectionUtils.isEmpty(runningSourceConfigList) || result == null || CollectionUtils.isEmpty(result.getBytesMap())) {
            return;
        }

        runningSourceConfigList.removeIf(sourceConfig -> result.getBytesMap().containsKey(sourceConfig.getClassFullName()));
    }

    private void markCompileError(Map<String, String> errorMessageMap, Map<String, SourceConfig> sourceConfigMap, List<LcdpErrorScriptBean> errorScriptList, String scriptStatus) {
        errorMessageMap.forEach((k, v) -> {
            String classFullName = StringUtils.replace(k, "/", ".");

            SourceConfig sourceConfig = sourceConfigMap.get(classFullName);
            if (sourceConfig == null || !StringUtils.isBlank(sourceConfig.getCompileErrorMessage())) {
                return;
            }

            sourceConfig.setCompileErrorMessage(v);

            LcdpErrorScriptBean errorScriptBean = new LcdpErrorScriptBean();
            errorScriptBean.setId(ApplicationContextHelper.getNextIdentity());
            errorScriptBean.setServerScriptId(sourceConfig.getResourceId());
            errorScriptBean.setScriptName(sourceConfig.getHintName());
            errorScriptBean.setScriptContent(sourceConfig.getScriptContent());
            errorScriptBean.setScriptPath(sourceConfig.getPath());
            errorScriptBean.setErrorLog(v);
            errorScriptBean.setScriptStatus(scriptStatus);
            errorScriptBean.setCreatedById(sourceConfig.getCreatedById());
            errorScriptBean.setCreatedByName(sourceConfig.getCreatedByName());
            errorScriptList.add(errorScriptBean);
        });
    }

    private static LoadMultipleResult comile(List<SourceConfig> sourceConfigList) {
        Map<String, String> sourceMap = new HashMap<>();
        sourceConfigList.forEach(s -> sourceMap.put(s.getClassFullName(), s.getSourceCode()));

        return DynamicClassLoader.getInstance().loadSourceCode(sourceMap);
    }

    private LoadMultipleResult loadStartupCompileCache(List<SourceConfig> sourceConfigList) {
        if (!ApplicationContextHelper.getEnvironment().getProperty(STARTUP_COMPILE_CACHE_ENABLED, Boolean.class, false)) {
            return null;
        }

        Path cacheFile = getStartupCompileCacheFile(sourceConfigList);
        if (!Files.exists(cacheFile)) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(cacheFile))) {
            StartupCompileCache cache = (StartupCompileCache) ois.readObject();

            LoadMultipleResult result = new LoadMultipleResult();
            result.setErrorMessageMap(cache.getErrorMessageMap());
            result.setBytesMap(cache.getBytesMap());
            result.setClassMap(DynamicClassLoader.getInstance().loadCompiledBytes(cache.getBytesMap()));

            log.info("启动编译缓存命中，脚本数量：{}，缓存文件：{}", sourceConfigList.size(), cacheFile);
            return result;
        } catch (Exception ex) {
            log.warn("加载启动编译缓存失败，缓存文件：{}，原因：{}", cacheFile, ex.getMessage());
            return null;
        }
    }

    private void saveStartupCompileCache(List<SourceConfig> sourceConfigList, LoadMultipleResult result) {
        if (!ApplicationContextHelper.getEnvironment().getProperty(STARTUP_COMPILE_CACHE_ENABLED, Boolean.class, false)) {
            return;
        }

        Path cacheFile = getStartupCompileCacheFile(sourceConfigList);
        StartupCompileCache cache = new StartupCompileCache();
        cache.setErrorMessageMap(result.getErrorMessageMap());
        cache.setBytesMap(result.getBytesMap());

        try {
            FileUtils.makeDirs(cacheFile);
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(cacheFile))) {
                oos.writeObject(cache);
            }
        } catch (IOException ex) {
            log.warn("保存启动编译缓存失败，缓存文件：{}，原因：{}", cacheFile, ex.getMessage());
        }
    }

    private Path getStartupCompileCacheFile(List<SourceConfig> sourceConfigList) {
        String key = EncryptUtils.MD5Encrypt(buildStartupCompileCacheSignature(sourceConfigList));
        return Paths.get(System.getProperty("java.io.tmpdir"), STARTUP_COMPILE_CACHE_FOLDER, key + ".bin");
    }

    private String buildStartupCompileCacheSignature(List<SourceConfig> sourceConfigList) {
        StringBuilder signature = new StringBuilder(STARTUP_COMPILE_CACHE_VERSION)
                .append('|').append(System.getProperty("java.version", ""))
                .append('|').append(buildPathSignature(System.getProperty("java.class.path", "")))
                .append('|').append(buildPathSignature(System.getProperty("sun.boot.class.path", "")));

        sourceConfigList.stream()
                .sorted(Comparator.comparing(SourceConfig::getClassFullName))
                .forEach(sourceConfig -> signature.append('|')
                        .append(sourceConfig.getClassFullName())
                        .append('=')
                        .append(EncryptUtils.MD5Encrypt(sourceConfig.getSourceCode())));

        return signature.toString();
    }

    private String buildPathSignature(String rawPath) {
        if (StringUtils.isBlank(rawPath)) {
            return "";
        }

        StringBuilder signature = new StringBuilder();
        String[] pathArray = rawPath.split(java.util.regex.Pattern.quote(File.pathSeparator));
        for (String pathText : pathArray) {
            if (StringUtils.isBlank(pathText)) {
                continue;
            }

            File pathFile = new File(pathText);
            signature.append(pathText)
                    .append('#')
                    .append(getPathModifiedSignature(pathFile))
                    .append(';');
        }

        return signature.toString();
    }

    private long getPathModifiedSignature(File pathFile) {
        if (!pathFile.exists()) {
            return -1L;
        }

        if (pathFile.isFile()) {
            return pathFile.lastModified();
        }

        final long[] latestModified = new long[]{pathFile.lastModified()};
        try {
            Files.walkFileTree(pathFile.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    latestModified[0] = Math.max(latestModified[0], attrs.lastModifiedTime().toMillis());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            log.warn("扫描启动编译缓存签名失败，路径：{}，原因：{}", pathFile.getAbsolutePath(), ex.getMessage());
        }

        return latestModified[0];
    }

    private static class SourceConfig {
        private final String className;
        private final String classFullName;
        private final String hintName;
        private final String sourceCode;
        private final String createdById;
        private final String createdByName;
        private final Long resourceId;
        private final String path;
        private final String scriptContent;
        private final boolean fromHistory;

        private String compileErrorMessage; // 编译失败时的错误信息

        public SourceConfig(LcdpResourceBean resource) {
            super();
            this.className = ClassManager.getClassName(resource.getClassContent());
            this.classFullName = ClassManager.getClassFullName(resource.getClassContent());
            this.hintName = resource.getResourceName();
            this.sourceCode = resource.getClassContent();
            this.createdByName = resource.getCreatedByName();
            this.createdById = resource.getCreatedById();
            this.resourceId = resource.getId();
            this.path = resource.getPath();
            this.scriptContent = resource.getContent();
            this.fromHistory = false;
        }

        public SourceConfig(LcdpResourceHistoryBean resourceHistory) {
            super();
            this.className = ClassManager.getClassName(resourceHistory.getClassContent());
            this.classFullName = ClassManager.getClassFullName(resourceHistory.getClassContent());
            this.hintName = resourceHistory.getResourceName();
            this.sourceCode = resourceHistory.getClassContent();
            this.createdByName = resourceHistory.getCreatedByName();
            this.createdById = resourceHistory.getCreatedById();
            this.resourceId = resourceHistory.getResourceId();
            this.path = resourceHistory.getPath();
            this.scriptContent = resourceHistory.getContent();
            this.fromHistory = true;
        }

        public String getHintName() {
            return hintName;
        }

        public String getSourceCode() {
            return sourceCode;
        }

        public String getCreatedById() {
            return createdById;
        }

        public String getCreatedByName() {
            return createdByName;
        }

        public Long getResourceId() {
            return resourceId;
        }

        public String getPath() {
            return path;
        }

        public String getScriptContent() {
            return scriptContent;
        }

        public String getClassFullName() {
            return classFullName;
        }

        public String getClassName() {
            return className;
        }

        public boolean isFromHistory() {
            return fromHistory;
        }

        public String getCompileErrorMessage() {
            return compileErrorMessage;
        }

        public void setCompileErrorMessage(String compileErrorMessage) {
            this.compileErrorMessage = compileErrorMessage;
        }
    }

    private static class StartupCompileCache implements Serializable {
        private static final long serialVersionUID = 1L;

        private Map<String, byte[]> bytesMap;
        private Map<String, String> errorMessageMap;

        public Map<String, byte[]> getBytesMap() {
            return bytesMap;
        }

        public void setBytesMap(Map<String, byte[]> bytesMap) {
            this.bytesMap = bytesMap;
        }

        public Map<String, String> getErrorMessageMap() {
            return errorMessageMap;
        }

        public void setErrorMessageMap(Map<String, String> errorMessageMap) {
            this.errorMessageMap = errorMessageMap;
        }
    }

    private void registerMapper(String path, boolean isPro, String content) {
        try {
            LcdpMapperUtils.loadMapper(path, isPro, content);
        } catch (Exception e) {
            log.error("============>加载mapper：" + path + "异常，异常日志如下：" + e.getMessage());
        }
    }

    private void printFailedCompileMessage(String message, List<SourceConfig> failedCompileList) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0, j = failedCompileList.size(); i < j; i++) {
            SourceConfig sourceConfig = failedCompileList.get(i);

            sb.append("\n").append((i + 1)).append("、")
                    .append("资源路径[").append(sourceConfig.getPath()).append("]\n")
                    .append(sourceConfig.getCompileErrorMessage());
        }

        sb.append("\n=================================================================================================");

        log.error("========================" + message + "========================" + sb.toString());
    }
}
