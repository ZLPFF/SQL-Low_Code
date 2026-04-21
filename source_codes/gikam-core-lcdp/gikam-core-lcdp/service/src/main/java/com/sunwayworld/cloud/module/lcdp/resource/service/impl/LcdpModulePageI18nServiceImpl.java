package com.sunwayworld.cloud.module.lcdp.resource.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.resource.bean.*;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.LcdpModulePageI18nDao;
import com.sunwayworld.cloud.module.lcdp.resource.service.*;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.cache.redis.RedisHelper;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.data.CaseInsensitiveLinkedMap;
import com.sunwayworld.framework.data.ListChunkIterator;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.data.page.Pagination;
import com.sunwayworld.framework.exception.FileException;
import com.sunwayworld.framework.exception.checked.CheckedException;
import com.sunwayworld.framework.exception.core.HttpErrorMessage;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.io.excel.ExcelWriter;
import com.sunwayworld.framework.io.excel.SheetWriter;
import com.sunwayworld.framework.io.excel.impl.DefaultExcelWriter;
import com.sunwayworld.framework.io.excel.impl.DefaultSheetWriter;
import com.sunwayworld.framework.io.excel.support.PropertyContext;
import com.sunwayworld.framework.io.excel.support.SheetContext;
import com.sunwayworld.framework.io.excel.support.impl.DefaultPropertyContextImpl;
import com.sunwayworld.framework.io.file.FilePathDTO;
import com.sunwayworld.framework.io.file.FileScope;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.mybatis.page.MybatisPageHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.extractor.DownloadParamExtractor;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.PersistableHelper;
import com.sunwayworld.framework.utils.*;
import com.sunwayworld.module.item.file.manager.CoreFileManager;
import com.sunwayworld.module.item.file.utils.CoreFileUtils;
import com.sunwayworld.module.sys.i18n.CoreI18nCache;
import com.sunwayworld.module.sys.i18n.bean.CoreI18nBean;
import com.sunwayworld.module.sys.i18n.bean.CoreI18nConfigBean;
import com.sunwayworld.module.sys.i18n.bean.CoreI18nMessageBean;
import com.sunwayworld.module.sys.i18n.service.CoreI18nConfigService;
import com.sunwayworld.module.sys.i18n.service.CoreI18nMessageService;
import com.sunwayworld.module.sys.i18n.service.CoreI18nService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@GikamBean
public class LcdpModulePageI18nServiceImpl implements LcdpModulePageI18nService {

    private static final Logger logger = LogManager.getLogger(LcdpModulePageI18nService.class);

    @Autowired
    private LcdpModulePageI18nDao lcdpModulePageI18nDao;

    @Autowired
    private LcdpResourceService lcdpResourceService;

    @Autowired
    private LcdpResourceHistoryService lcdpResourceHistoryService;

    @Autowired
    private LcdpModulePageCompService lcdpModulePageCompService;

    @Autowired
    private LcdpPageI18nCodeService lcdpPageI18nCodeService;

    @Autowired
    private CoreI18nMessageService coreI18nMessageService;

    @Autowired
    private CoreI18nService coreI18nService;

    @Autowired
    private CoreI18nConfigService coreI18nConfigService;

    @Autowired
    @Lazy
    private CoreFileManager fileManager;

    @Lazy
    @Autowired
    private LcdpModulePageI18nService proxyInstance;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpModulePageI18nDao getDao() {
        return lcdpModulePageI18nDao;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        LcdpModulePageI18nBean lcdpModulePageI18n = jsonWrapper.parseUnique(LcdpModulePageI18nBean.class);
        lcdpModulePageI18n.setId(ApplicationContextHelper.getNextIdentity());
        getDao().insert(lcdpModulePageI18n);
        return lcdpModulePageI18n.getId();
    }

    @Override
    public Map<String, String> selectI18nMessageByCode(RestJsonWrapperBean jsonWrapper) {
        String code = jsonWrapper.getParamValue("code");
        String resourceId = jsonWrapper.getParamValue("resourceId");

        List<CoreI18nConfigBean> coreI18nConfigList = coreI18nConfigService.selectAll();

        Map<String, String> i18nMessage = coreI18nConfigList.stream().collect(Collectors.toMap(CoreI18nConfigBean::getId, p -> ""));

        if (!StringUtils.isEmpty(resourceId)) {

            LcdpResourceBean resource = lcdpResourceService.selectById(Long.valueOf(resourceId));

            List<LcdpResourceHistoryBean> resourceHistoryList = lcdpResourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", resource.getId()).filter(MatchPattern.SEQ));

            LcdpResourceHistoryBean currentUserEditHistoryResource = null;
            //超级管理员sysAdmin 在点开查看资源时可以查看到最新的的数据
            if (StringUtils.equals(LcdpConstant.SUPER_ADMIN_ID, LocalContextHelper.getLoginUserId())) {
                currentUserEditHistoryResource = resourceHistoryList.stream().filter(re -> LcdpConstant.SUBMIT_FLAG_NO.equals(re.getSubmitFlag())).findAny().orElse(null);
            } else {
                currentUserEditHistoryResource = resourceHistoryList.stream().filter(re -> LcdpConstant.SUBMIT_FLAG_NO.equals(re.getSubmitFlag()) && LocalContextHelper.getLoginUserId().equals(re.getCreatedById())).findAny().orElse(null);
            }

            Long historyId = null;

            if (!ObjectUtils.isEmpty(currentUserEditHistoryResource)) {

                historyId = currentUserEditHistoryResource.getId();

            } else {
                LcdpResourceHistoryBean submitedResource = resourceHistoryList.stream().filter(re -> resource.getEffectVersion().equals(re.getVersion())).findAny().get();

                historyId = submitedResource.getId();
            }

            List<LcdpModulePageI18nBean> pageI18nMessageList = selectListByFilter(SearchFilter.instance().match("CODE", code).filter(MatchPattern.SEQ).match("MODULEPAGEHISTORYID", historyId).filter(MatchPattern.EQ));

            //页面国际化为空，则查询核心国际化
            if (!pageI18nMessageList.isEmpty()) {
                pageI18nMessageList.forEach(pageI18nMessage -> {
                    if (i18nMessage.containsKey(pageI18nMessage.getI18nConfigId())) {
                        i18nMessage.put(pageI18nMessage.getI18nConfigId(), pageI18nMessage.getMessage());
                    }
                });

                return i18nMessage;
            }
        }

        //查询全局国际化缓存
        Set<String> i18nConfigSet = i18nMessage.keySet();
        for (String i18nConfig : i18nConfigSet) {
            Map<String, String> i18nLocaleMap = CoreI18nCache.instance().getI18nLocaleMap(i18nConfig);
            if (!ObjectUtils.isEmpty(i18nLocaleMap)) {

                String message = CoreI18nCache.instance().getI18nLocaleMap(i18nConfig).get(code);
                if (!StringUtils.isEmpty(message)) {
                    i18nMessage.put(i18nConfig, message);
                }
            }
        }

        return i18nMessage;
    }

    @Override
    public Map<String, Map<String, String>> selectPageI18nMessage(Long resourceHistoryId) {
        List<LcdpModulePageI18nBean> pageI18nList = proxyInstance.selectListByModulePageHistoryId(resourceHistoryId);

        Map<String, Map<String, String>> map = new HashMap<>();

        List<CoreI18nConfigBean> coreI18nConfigList = coreI18nConfigService.selectAll();

        Map<String, String> i18nMessage = coreI18nConfigList.stream().collect(Collectors.toMap(CoreI18nConfigBean::getId, p -> ""));

        for (LcdpModulePageI18nBean message : pageI18nList) {
            String localeId = message.getI18nConfigId();
            String code = message.getCode();

            Map<String, String> i18nMap = map.get(code);

            if (i18nMap == null) {
                i18nMap = new HashMap<>();
                i18nMap.putAll(i18nMessage);
                map.put(code, i18nMap);
            }

            i18nMap.put(localeId, message.getMessage());
        }

        return map;
    }

    @Override
    public Page<Map<String, Object>> selectAllI18nMessage(RestJsonWrapperBean jsonWrapper) {

        parseParam(jsonWrapper);

        Page<Map<String, Object>> i18nPage = MybatisPageHelper.get(jsonWrapper.extractPageRowBounds(), () -> getDao().selectAllI18nMessage(jsonWrapper.extractMapFilter()));
        List<LcdpI18nDTO> rows = i18nPage.getRows().parallelStream().map(m -> PersistableHelper.mapToPersistable(m, LcdpI18nDTO.class)).collect(Collectors.toList());

        List<Long> i18nIdList = rows.stream().filter(i18n -> i18n.getId() != 0).map(i18n -> i18n.getId()).collect(Collectors.toList());

        List<LcdpModulePageI18nBean> conditionList = rows.stream().filter(i18n -> i18n.getId() == 0).map(i18n -> {
            LcdpModulePageI18nBean condition = new LcdpModulePageI18nBean();
            condition.setCode(i18n.getCode());
            condition.setModulePageHistoryId(i18n.getModulePageHistoryId());
            return condition;
        }).collect(Collectors.toList());

        List<CoreI18nMessageBean> messageList = coreI18nMessageService.selectListByFilter(SearchFilter.instance().match("I18NID", i18nIdList).filter(MatchPattern.OR));

        List<LcdpModulePageI18nBean> pageI18nMessageList = getDao().selectList(conditionList, Arrays.asList("CODE", "MODULEPAGEHISTORYID"), Arrays.asList("CODE", "MODULEPAGEHISTORYID", "I18NCONFIGID", "MESSAGE"));

        List<Map<String, Object>> i18nRowList = new ArrayList<>();

        rows.forEach(row -> {
            if (row.getId() != 0) {

                Map<String, Object> i18nRow = JSON.parseObject(JSON.toJSONString(row));
                Map<String, String> i18nMessageMap = new HashMap<>();

                messageList.stream()
                        .filter(i18n -> ObjectUtils.equals(row.getId(), i18n.getI18nId()) && i18n.getMessage() != null)
                        .forEach(i18nMessage -> {
                            i18nMessageMap.put(i18nMessage.getI18nConfigId(), i18nMessage.getMessage());
                        });

                i18nRow.putAll(i18nMessageMap);

                i18nRowList.add(i18nRow);
            } else {
                Map<String, String> i18nMessageMap = new HashMap<>();
                pageI18nMessageList.stream()
                        .filter(i18n -> ObjectUtils.equals(row.getModulePageHistoryId(), i18n.getModulePageHistoryId())
                                && StringUtils.equals(i18n.getCode(), row.getCode()) && i18n.getMessage() != null)
                        .forEach(pageI18nMessage -> {
                            i18nMessageMap.put(pageI18nMessage.getI18nConfigId(), pageI18nMessage.getMessage());
                        });

                Map<String, Object> i18nRow = JSON.parseObject(JSON.toJSONString(row));

                i18nRow.putAll(i18nMessageMap);

                i18nRowList.add(i18nRow);
            }
        });

        return new Pagination<>(i18nPage, i18nRowList);
    }

    @Override
    public List<LcdpModulePageI18nBean> selectEffectivePageI18nMessage(MapperParameter parameter) {
        List<Map<String, Object>> mapList = getDao().selectEffectiveByCondition(parameter);
        if (mapList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        return mapList.parallelStream().map(m -> PersistableHelper.mapToPersistable(m, getDao().getType())).collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Audit(AuditConstant.SAVE)
    public void alterMessage(RestJsonWrapperBean wrapper) {
        List<LcdpI18nDTO> alterationMessageList = wrapper.parse(LcdpI18nDTO.class);

        List<LcdpI18nDTO> coreI18nList = alterationMessageList.stream().filter(i18n -> i18n.getId() != 0).collect(Collectors.toList());

        List<LcdpI18nDTO> lcdpI18nList = alterationMessageList.stream().filter(i18n -> i18n.getId() == 0).collect(Collectors.toList());

        Map<String, String> configMap = new CaseInsensitiveLinkedMap<>();

        coreI18nConfigService.selectAll().forEach(config -> {
            configMap.put(config.getId(), config.getId());
        });


        if (!coreI18nList.isEmpty()) {
            List<Long> coreI18nIdList = coreI18nList.stream().map(LcdpI18nDTO::getId).collect(Collectors.toList());

            List<CoreI18nMessageBean> oldMessageList = coreI18nMessageService.selectListByFilter(SearchFilter.instance().match("I18NID", coreI18nIdList).filter(MatchPattern.OR));

            List<CoreI18nMessageBean> newMessageList = new ArrayList<>();

            List<CoreI18nBean> coreI18nBeanList = coreI18nService.selectListByIds(coreI18nIdList);

            coreI18nList.forEach(coreI18n -> {
                CoreI18nBean i18nBean = coreI18nBeanList.stream().filter(i18n -> ObjectUtils.equals(i18n.getId(), coreI18n.getId())).findFirst().orElse(null);
                if (!ObjectUtils.isEmpty(i18nBean)) {
                    i18nBean.setDescription(coreI18n.getDescription());
                }

                coreI18n.getExt$().forEach((k, v) -> {
                    CoreI18nMessageBean oldMessage = oldMessageList.stream().filter(i18n -> ObjectUtils.equals(i18n.getI18nId(), coreI18n.getId()) && StringUtils.equalsIgnoreCase(i18n.getI18nConfigId(), k)).findFirst().orElse(null);

                    if (ObjectUtils.isEmpty(oldMessage) && !StringUtils.isEmpty(v)) {
                        CoreI18nMessageBean coreI18nMessageBean = new CoreI18nMessageBean();
                        coreI18nMessageBean.setId(ApplicationContextHelper.getNextIdentity());
                        coreI18nMessageBean.setI18nId(coreI18n.getId());
                        coreI18nMessageBean.setI18nConfigId(configMap.get(k));
                        coreI18nMessageBean.setMessage(v);

                        newMessageList.add(coreI18nMessageBean);
                    } else if (!ObjectUtils.isEmpty(oldMessage)) {
                        oldMessage.setMessage(v);
                    }
                });
            });

            coreI18nService.updateIfChanged(coreI18nBeanList);
            coreI18nMessageService.updateIfChanged(oldMessageList);
            coreI18nMessageService.getDao().insert(newMessageList);
        }

        if (!lcdpI18nList.isEmpty()) {
            List<LcdpModulePageI18nBean> newMessageList = new ArrayList<>();

            List<LcdpModulePageI18nBean> conditionList = lcdpI18nList.stream().map(lcdpI18n -> {
                LcdpModulePageI18nBean condition = new LcdpModulePageI18nBean();
                condition.setCode(lcdpI18n.getCode());
                condition.setModulePageHistoryId(lcdpI18n.getModulePageHistoryId());
                return condition;
            }).collect(Collectors.toList());

            List<LcdpModulePageI18nBean> pageI18nMessageList = getDao().selectList(conditionList, Arrays.asList("CODE", "MODULEPAGEHISTORYID"), Arrays.asList("ID", "CODE", "MODULEPAGEHISTORYID", "I18NCONFIGID", "MESSAGE"));

            lcdpI18nList.forEach(lcdpI18nDTO -> {
                lcdpI18nDTO.getExt$().forEach((k, v) -> {
                    LcdpModulePageI18nBean oldMessage = pageI18nMessageList.stream().filter(i18n -> StringUtils.equals(i18n.getCode(), lcdpI18nDTO.getCode()) && ObjectUtils.equals(lcdpI18nDTO.getModulePageHistoryId(), i18n.getModulePageHistoryId()) && StringUtils.equalsIgnoreCase(i18n.getI18nConfigId(), k)).findFirst().orElse(null);

                    if (ObjectUtils.isEmpty(oldMessage) && !StringUtils.isEmpty(v)) {
                        LcdpModulePageI18nBean lcdpModulePageI18nBean = new LcdpModulePageI18nBean();
                        lcdpModulePageI18nBean.setId(ApplicationContextHelper.getNextIdentity());
                        lcdpModulePageI18nBean.setModulePageHistoryId(lcdpI18nDTO.getModulePageHistoryId());
                        lcdpModulePageI18nBean.setCode(lcdpI18nDTO.getCode());
                        lcdpModulePageI18nBean.setI18nConfigId(configMap.get(k));
                        lcdpModulePageI18nBean.setMessage(v);

                        newMessageList.add(lcdpModulePageI18nBean);
                    } else if (!ObjectUtils.isEmpty(oldMessage)) {
                        oldMessage.setMessage(v);
                    }
                });
            });
            updateIfChanged(pageI18nMessageList);
            getDao().insert(newMessageList);
        }

        CoreI18nCache.instance().reloadMessage();

    }

    @Override
    public String export(RestJsonWrapperBean wrapper) {
        List<CoreI18nBean> allCoreI18nList = coreI18nService.selectAll();

        List<CoreI18nMessageBean> allCoreI18nMessageList = coreI18nMessageService.selectList(new MapperParameter());

        List<LcdpModulePageI18nBean> allPageI18nMessageList = selectEffectivePageI18nMessage(new MapperParameter());

        Map<String, LcdpI18nDTO> allI18nMap = new HashMap<>();

        List<LcdpI18nDTO> allI18nList = new ArrayList<>();

        allCoreI18nList.forEach(coreI18n -> {
            LcdpI18nDTO lcdpI18nDTO = new LcdpI18nDTO();
            lcdpI18nDTO.setCode(coreI18n.getCode());
            lcdpI18nDTO.setDescription(coreI18n.getDescription());
            lcdpI18nDTO.setDefaultMessage(coreI18n.getDefaultMessage());

            allI18nList.add(lcdpI18nDTO);
            allI18nMap.put(coreI18n.getCode(), lcdpI18nDTO);
        });

        //核心国际化信息
        allCoreI18nMessageList.forEach(coreI18nMessageBean -> {
            String code = coreI18nMessageBean.getExt$Item("CODE");
            LcdpI18nDTO i18nDTO = allI18nMap.get(code);

            if (!ObjectUtils.isEmpty(i18nDTO)) {
                i18nDTO.setExt$Item(coreI18nMessageBean.getExt$Item("LOCALENAME"), coreI18nMessageBean.getMessage());
            }
        });

        //页面国际化信息
        allPageI18nMessageList.forEach(pageI18nBean -> {
            String code = pageI18nBean.getCode();
            LcdpI18nDTO i18nDTO = allI18nMap.get(code);

            if (ObjectUtils.isEmpty(i18nDTO)) {
                i18nDTO = new LcdpI18nDTO();
                i18nDTO.setCode(pageI18nBean.getCode());
                allI18nList.add(i18nDTO);
                allI18nMap.put(code, i18nDTO);
            }

            i18nDTO.setExt$Item(pageI18nBean.getExt$Item("LOCALENAME"), pageI18nBean.getMessage());
        });

        List<PropertyContext> columns = generateExcelColumn();

        String topic = I18nHelper.getMessage("T_CORE_I18N_MESSAGE.SERVICE_NAME");

        SheetContext sheetContext = new SheetContext(topic, topic, columns);

        SheetWriter sheetWriter = new DefaultSheetWriter(sheetContext, allI18nList);

        ExcelWriter excelWriter = new DefaultExcelWriter(sheetWriter);

        String fileName = new StringBuilder(topic)
                .append("-")
                .append(LocalContextHelper.getLoginUser().getUserName())
                .append("-")
                .append(DateTimeUtils.formatLocalDateTime(LocalDateTime.now(), "yyyyMMddHHmmss"))
                .append(".xlsx").toString();

        FilePathDTO filePath = CoreFileUtils.toFilePath(FileScope.export, fileName);

        File exportFile = null;
        try {
            exportFile = FileUtils.createTempFile();

            try (FileOutputStream fos = new FileOutputStream(exportFile);) {
                excelWriter.write(fos);
            }

            fileManager.upload(filePath, exportFile);
        } catch (IOException ioe) {
            throw new FileException(ioe);
        } finally {
            FileUtils.deleteQuietly(exportFile);
        }

        return fileManager.getDownloadUrl(filePath);
    }

    @Override
    @Transactional
    public void batchDealHistoryPageCompI18n(List<LcdpResourceHistoryBean> resourceHistoryList, Map<String, String> i18nMessageMap) {
        if (ObjectUtils.isEmpty(i18nMessageMap)) {
            i18nMessageMap = new HashMap<>();
            //按钮相关系统国际化
            List<CoreI18nMessageBean> coreI18nMessageList = coreI18nMessageService.selectListByFilter(SearchFilter.instance()
                    .match("CODE", "GIKAM").filter(MatchPattern.SB)
                    .match("I18NCONFIGID", "zh-CN").filter(MatchPattern.SEQ));
            for (CoreI18nMessageBean coreI18nMessageBean : coreI18nMessageList) {
                i18nMessageMap.put(coreI18nMessageBean.getMessage(), coreI18nMessageBean.getExt$Item("CODE"));
            }
        }
        List<Long> resourceHistoryIdList = resourceHistoryList.stream().map(LcdpResourceHistoryBean::getId).collect(Collectors.toList());
        //所有页面的页面组件
        List<LcdpModulePageCompBean> pageCompList = lcdpModulePageCompService.selectListByFilter(SearchFilter.instance().match("modulePageHistoryId", resourceHistoryIdList).filter(MatchPattern.OR));
        //待更新的组件数据、与国际化数据
        List<LcdpModulePageCompBean> dealPageCompList = new ArrayList<>();
        List<LcdpPageI18nCodeBean> pageI18nCodeList = new ArrayList<>();
        List<LcdpModulePageI18nBean> pageI18nList = new ArrayList<>();
        //所有grid、form的字段组件类型
        List<String> specialCompType = Arrays.asList("GridColumn", "Date", "Number", "Cron", "Text", "Month", "CheckboxGroup", "CodeEditor", "Time", "Choose", "Year", "DateTime", "Ckeditor4", "Formula", "Textarea", "Image", "Link", "ComboBox", "SimpleCheckbox", "Empty", "CascadeSelect", "RichText", "Ketcher", "RadioGroup", "Select", "Script", "Sign");

        Map<String, String> finalI18nMessageMap = i18nMessageMap;
        resourceHistoryList.forEach(resourceHistory -> {
            //找出当前页面的所有组件
            List<LcdpModulePageCompBean> thisPageCompList = pageCompList.stream().filter(pageComp -> ObjectUtils.equals(pageComp.getModulePageHistoryId(), resourceHistory.getId())).collect(Collectors.toList());
            //国际化依赖关系
            List<String> thisPageI18nCodeList = new ArrayList<>();
            //当前页面的待更新国际化数据
            List<LcdpModulePageI18nBean> thisPageI18nList = new ArrayList<>();
            //遍历当前页面组件配置找出需要修改配置与新增的国际化
            thisPageCompList.forEach(comp -> {
                JSONObject configObject = JSON.parseObject(comp.getConfig());

                String pageCompi18nCodePrefix = "";
                //生成页面国际化前缀，不处理grid、form的字段组件
                if (!specialCompType.contains(comp.getType())) {
                    pageCompi18nCodePrefix = new StringBuilder(resourceHistory.getPath()).append(".").append(configObject.get("id")).append(".").toString();
                }


                if (handlePageCompJsonI18n(configObject, pageCompi18nCodePrefix, finalI18nMessageMap, comp, thisPageI18nCodeList, thisPageI18nList)) {
                    comp.setConfig(configObject.toJSONString());
                    dealPageCompList.add(comp);
                }

            });
            //国际化依赖关系
            thisPageI18nCodeList.forEach(code -> {
                LcdpPageI18nCodeBean lcdpPageI18nCodeBean = new LcdpPageI18nCodeBean();
                lcdpPageI18nCodeBean.setId(ApplicationContextHelper.getNextIdentity());
                lcdpPageI18nCodeBean.setModulePageId(resourceHistory.getResourceId());
                lcdpPageI18nCodeBean.setModulePageHistoryId(resourceHistory.getId());
                lcdpPageI18nCodeBean.setCode(code);

                pageI18nCodeList.add(lcdpPageI18nCodeBean);
            });
            pageI18nList.addAll(thisPageI18nList);
        });

        //更新数据
        ListChunkIterator<LcdpModulePageCompBean> chunkIterator = ListChunkIterator.of(dealPageCompList, 400);
        while (chunkIterator.hasNext()) {
            List<LcdpModulePageCompBean> chunkItemList = chunkIterator.nextChunk();
            lcdpModulePageCompService.getDao().update(chunkItemList, "CONFIG");
        }
        getDao().fastInsert(pageI18nList);
        lcdpPageI18nCodeService.getDao().fastInsert(pageI18nCodeList);
    }


    @Override
    @Transactional
    public void batchDealHistoryPageCompI18nGridAndForm(List<LcdpResourceHistoryBean> resourceHistoryList, Map<String, String> i18nMessageMap, List<LcdpI18nDTO> i18nDTOList) {
        Map<Long, List<String>> historyId2CodeList = i18nDTOList.stream().filter(i18nDTO -> !ObjectUtils.isEmpty(i18nDTO.getModulePageHistoryId())).collect(Collectors.groupingBy(LcdpI18nDTO::getModulePageHistoryId, Collectors.mapping(LcdpI18nDTO::getCode, Collectors.toList())));
        if (ObjectUtils.isEmpty(i18nMessageMap)) {
            i18nMessageMap = new HashMap<>();
            //按钮相关系统国际化
            List<CoreI18nMessageBean> coreI18nMessageList = coreI18nMessageService.selectListByFilter(SearchFilter.instance()
                    .match("CODE", "GIKAM").filter(MatchPattern.SB)
                    .match("I18NCONFIGID", "zh-CN").filter(MatchPattern.SEQ));
            Map<String, String> finalI18nMessageMap = i18nMessageMap;
            coreI18nMessageList.forEach(coreI18nMessageBean -> {
                finalI18nMessageMap.put(coreI18nMessageBean.getMessage(), coreI18nMessageBean.getExt$Item("CODE"));
            });
        }
        Map<String, String> finalI18nMessageMap = i18nMessageMap;
        List<Long> resourceHistoryIdList = resourceHistoryList.stream().map(LcdpResourceHistoryBean::getId).collect(Collectors.toList());
//        List<Long> resourceHistoryIdList = new ArrayList<>();
        //所有页面的页面组件
        List<LcdpModulePageCompBean> pageCompList = lcdpModulePageCompService.selectListByFilter(SearchFilter.instance().match("modulePageHistoryId", resourceHistoryIdList).filter(MatchPattern.OR));
        //待更新的组件数据、与国际化数据
        List<LcdpModulePageCompBean> dealPageCompList = new ArrayList<>();
        List<LcdpPageI18nCodeBean> pageI18nCodeList = new ArrayList<>();
        List<LcdpModulePageI18nBean> pageI18nList = new ArrayList<>();
        //所有grid、form的字段组件类型
        List<String> specialCompType = Arrays.asList("GridColumn", "Date", "Number", "Cron", "Text", "Month", "CheckboxGroup", "CodeEditor", "Time", "Choose", "Year", "DateTime", "Ckeditor4", "Formula", "Textarea", "Image", "Link", "ComboBox", "SimpleCheckbox", "Empty", "CascadeSelect", "RichText", "Ketcher", "RadioGroup", "Select", "Script", "Sign");

        resourceHistoryList.stream().forEach(resourceHistory -> {
            //找出当前页面的所有组件
            List<LcdpModulePageCompBean> thisPageCompList = pageCompList.stream().filter(pageComp -> ObjectUtils.equals(pageComp.getModulePageHistoryId(), resourceHistory.getId())).collect(Collectors.toList());
            //映射下
            Map<String, String> id2CompIdMap = thisPageCompList.stream().collect(Collectors.toMap(comp -> comp.getId(), comp -> {
                String config = comp.getConfig() + (StringUtils.isEmpty(comp.getConfigExtend()) ? "" : comp.getConfigExtend());
                JSONObject configObject = JSON.parseObject(config);
                return configObject.getString("id");
            }));
            //国际化依赖关系
            List<String> thisPageI18nCodeList = new ArrayList<>();
            //当前页面的待更新国际化数据
            List<LcdpModulePageI18nBean> thisPageI18nList = new ArrayList<>();
            //遍历当前页面组件配置找出需要修改配置与新增的国际化
            thisPageCompList.forEach(comp -> {
                String config = comp.getConfig() + (StringUtils.isEmpty(comp.getConfigExtend()) ? "" : comp.getConfigExtend());
                JSONObject configObject = JSON.parseObject(config);

                String pageCompi18nCodePrefix = "";
                //生成页面国际化前缀，不处理grid、form的字段组件
                if (!specialCompType.contains(comp.getType())) {
                    pageCompi18nCodePrefix = new StringBuilder(resourceHistory.getPath()).append(".").append(configObject.get("id")).append(".").toString();
                }
                //只处理grid、form的字段组件
                if (specialCompType.contains(comp.getType())) {
                    pageCompi18nCodePrefix = new StringBuilder(resourceHistory.getPath()).append(".").append(id2CompIdMap.get(comp.getParentId())).append(".").append(configObject.get("field")).append(".").toString();

                    if (handlePageCompJsonI18nGridAndForm(configObject, pageCompi18nCodePrefix, finalI18nMessageMap, comp, thisPageI18nCodeList, thisPageI18nList, historyId2CodeList)) {
                        comp.setConfig(configObject.toJSONString());
                        dealPageCompList.add(comp);
                    }
                }




            });
            //国际化依赖关系
            thisPageI18nCodeList.forEach(code -> {
                LcdpPageI18nCodeBean lcdpPageI18nCodeBean = new LcdpPageI18nCodeBean();
                lcdpPageI18nCodeBean.setId(ApplicationContextHelper.getNextIdentity());
                lcdpPageI18nCodeBean.setModulePageId(resourceHistory.getResourceId());
                lcdpPageI18nCodeBean.setModulePageHistoryId(resourceHistory.getId());
                lcdpPageI18nCodeBean.setCode(code);

                pageI18nCodeList.add(lcdpPageI18nCodeBean);
            });
            pageI18nList.addAll(thisPageI18nList);
        });

        //更新数据
        ListChunkIterator<LcdpModulePageCompBean> chunkIterator = ListChunkIterator.of(dealPageCompList, 200);
        while (chunkIterator.hasNext()) {
            List<LcdpModulePageCompBean> chunkItemList = chunkIterator.nextChunk();
            lcdpModulePageCompService.getDao().update(chunkItemList, "CONFIG");
        }
        getDao().insert(pageI18nList);
        lcdpPageI18nCodeService.getDao().insert(pageI18nCodeList);
    }

    @Override
    public void refreshI18nMessage(RestJsonWrapperBean jsonWrapper) {
        if (RedisHelper.get("I18NMESSAGE_REFRESH", "PENDING") != null) {
            ServletUtils.responseError(HttpErrorMessage.of(HttpStatus.BAD_REQUEST, "国际化信息正在刷新中"));
            return;
        }
        RedisHelper.put("I18NMESSAGE_REFRESH", "PENDING", "1");

        try {


            List<CoreI18nBean> allCoreI18nList = coreI18nService.selectAll();

            List<CoreI18nMessageBean> allCoreI18nMessageList = coreI18nMessageService.selectList(new MapperParameter());

            List<LcdpModulePageI18nBean> allPageI18nMessageList = selectEffectivePageI18nMessage(new MapperParameter());

            Map<String, LcdpI18nDTO> allI18nMap = new HashMap<>();

            List<LcdpI18nDTO> allI18nList = new ArrayList<>();

            allCoreI18nList.forEach(coreI18n -> {
                LcdpI18nDTO lcdpI18nDTO = new LcdpI18nDTO();
                lcdpI18nDTO.setCode(coreI18n.getCode());
                lcdpI18nDTO.setDescription(coreI18n.getDescription());
                lcdpI18nDTO.setDefaultMessage(coreI18n.getDefaultMessage());

                allI18nList.add(lcdpI18nDTO);
                allI18nMap.put(coreI18n.getCode(), lcdpI18nDTO);
            });

            //核心国际化信息
            allCoreI18nMessageList.forEach(coreI18nMessageBean -> {
                String code = coreI18nMessageBean.getExt$Item("CODE");
                LcdpI18nDTO i18nDTO = allI18nMap.get(code);

                if (!ObjectUtils.isEmpty(i18nDTO)) {
                    i18nDTO.setExt$Item(coreI18nMessageBean.getExt$Item("LOCALENAME"), coreI18nMessageBean.getMessage());
                }
            });

            Map<Long, List<String>> historyId2CodeList = allPageI18nMessageList.stream().collect(Collectors.groupingBy(LcdpModulePageI18nBean::getModulePageHistoryId, Collectors.mapping(LcdpModulePageI18nBean::getCode, Collectors.toList())));
            //页面国际化信息
            allPageI18nMessageList.forEach(pageI18nBean -> {

                String code = pageI18nBean.getCode();
                LcdpI18nDTO i18nDTO = allI18nMap.get(code);

                if (ObjectUtils.isEmpty(i18nDTO)) {
                    i18nDTO = new LcdpI18nDTO();
                    i18nDTO.setCode(pageI18nBean.getCode());
                    i18nDTO.setModulePageHistoryId(pageI18nBean.getModulePageHistoryId());
                    allI18nList.add(i18nDTO);
                    allI18nMap.put(code, i18nDTO);
                }
            });


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
                batchDealHistoryPageCompI18nGridAndForm(chunkItemList, i18nMessageMap, allI18nList);
            }
            RedisHelper.evict("I18NMESSAGE_REFRESH", "PENDING");
        } catch (Exception e) {
            logger.error("刷新国际化信息异常", e);
            throw new CheckedException(e);
        } finally {
            RedisHelper.evict("I18NMESSAGE_REFRESH", "PENDING");
        }

    }


    @Override
    @Transactional
    public void batchDealHistoryPageCompI18nForDuplicateId(List<LcdpResourceHistoryBean> resourceHistoryList) {
        List<LcdpModulePageI18nBean> oldI118nList = new ArrayList<>();


        List<LcdpModulePageI18nBean> insertPageI18nList = new ArrayList<>();

        List<Long> resourceHistoryIdList = resourceHistoryList.stream().map(LcdpResourceHistoryBean::getId).collect(Collectors.toList());
        Map<Long, String> resourceHistoryId2PathMap = resourceHistoryList.stream().collect(Collectors.toMap(LcdpResourceHistoryBean::getId, LcdpResourceHistoryBean::getPath));
        //所有页面的页面组件
        List<LcdpModulePageCompBean> pageCompList = lcdpModulePageCompService.selectListByFilter(SearchFilter.instance().match("modulePageHistoryId", resourceHistoryIdList).filter(MatchPattern.OR));

        List<LcdpModulePageCompBean> dealPageCompList = new ArrayList<>();

        List<LcdpModulePageCompBean> tabPageCompList = pageCompList.stream().filter(comp -> StringUtils.equals("Tab", comp.getType())).collect(Collectors.toList());
        List<LcdpModulePageCompBean> tabPanelPageCompList = pageCompList.stream().filter(comp -> StringUtils.equals("TabPanel", comp.getType())).collect(Collectors.toList());
        tabPageCompList.forEach(tab -> {
            JSONObject tabConfig = JSON.parseObject(tab.getConfig());
            JSONArray tabChildrenWidgetId = (JSONArray) tabConfig.get("childrenWidgetId");
            List<String> tabPanelIdList = tabChildrenWidgetId.toJavaList(String.class);
            List<LcdpModulePageCompBean> tabPanelList = tabPanelPageCompList.stream().filter(tabPanel -> tabPanelIdList.contains(tabPanel.getId())).collect(Collectors.toList());

            List<String> tabPanelCompIdList = tabPanelList.stream().map(tabPanel -> {
                JSONObject tabPanelConfig = JSON.parseObject(tabPanel.getConfig());
                String tabPanelCompId = tabPanelConfig.getString("id");
                if (!tabPanelCompId.startsWith("TabPanel_")) {
                    return null;
                }
                return tabPanelCompId;
            }).collect(Collectors.toList());
            List<String> duplicateCompIdList = tabPanelCompIdList.stream()
                    .filter(id -> !StringUtils.isEmpty(id) && Collections.frequency(tabPanelCompIdList, id) > 1)
                    .collect(Collectors.toList());

            List<LcdpModulePageCompBean> duplicates = tabPanelList.stream()
                    .filter(tabPanel -> {
                        JSONObject tabPanelConfig = JSON.parseObject(tabPanel.getConfig());
                        String tabPanelCompId = tabPanelConfig.getString("id");
                        if (duplicateCompIdList.contains(tabPanelCompId)) {
                            return true;
                        }
                        return false;
                    }).collect(Collectors.toList());


            List<String> idIndexList = tabPanelList.stream().map(tabPanel -> {
                JSONObject panelConfig = JSON.parseObject(tabPanel.getConfig());
                if (panelConfig.getString("id").startsWith("TabPanel_") && panelConfig.getString("id").split("_").length > 1) {
                    return panelConfig.getString("id").split("_")[1];
                }
                return null;
            }).filter(index -> !StringUtils.isEmpty(index) && NumberUtils.isNumber(index)).collect(Collectors.toList());
            Long index = 0L;
            if (!idIndexList.isEmpty()) {
                index = idIndexList.stream().map(Long::valueOf).max(Comparator.comparing(Function.identity())).get();
            }
            for (int i = 0; i < duplicates.size(); i++) {
                String tabPanelCompId = "TabPanel_" + (index + (i + 1));
                LcdpModulePageCompBean tablePanel = duplicates.get(i);
                Long resourceHistoryId = tablePanel.getModulePageHistoryId();
                JSONObject tabPanelConfig = JSON.parseObject(tablePanel.getConfig());
                @SuppressWarnings("unchecked")
                Map<String, Object> titleMap = tabPanelConfig.getObject("title", Map.class);

                String oldI118nCode = ObjectUtils.isEmpty(titleMap.get("i18nCode")) ? "" : titleMap.get("i18nCode").toString();
                LcdpModulePageI18nBean deletePageI18nBean = new LcdpModulePageI18nBean();
                deletePageI18nBean.setCode(oldI118nCode);
                deletePageI18nBean.setModulePageHistoryId(tablePanel.getModulePageHistoryId());
                deletePageI18nBean.setModulePageId(tablePanel.getModulePageId());
                oldI118nList.add(deletePageI18nBean);

                titleMap.put("i18nCode", resourceHistoryId2PathMap.get(resourceHistoryId) + "." + tabPanelCompId + ".title");
                tabPanelConfig.put("title", titleMap);
                tabPanelConfig.put("id", tabPanelCompId);

                tablePanel.setConfig(tabPanelConfig.toJSONString());

                LcdpModulePageI18nBean lcdpModulePageI18nBean = new LcdpModulePageI18nBean();
                lcdpModulePageI18nBean.setId(ApplicationContextHelper.getNextIdentity());
                lcdpModulePageI18nBean.setCode(resourceHistoryId2PathMap.get(resourceHistoryId) + "." + tabPanelCompId + ".title");
                lcdpModulePageI18nBean.setModulePageHistoryId(tablePanel.getModulePageHistoryId());
                lcdpModulePageI18nBean.setModulePageId(tablePanel.getModulePageId());
                lcdpModulePageI18nBean.setMessage(ObjectUtils.isEmpty(titleMap.get("zh-CN")) ? "" : titleMap.get("zh-CN") + "");
                lcdpModulePageI18nBean.setI18nConfigId("zh-CN");
                insertPageI18nList.add(lcdpModulePageI18nBean);
            }
            dealPageCompList.addAll(duplicates);
        });


        //更新数据
        ListChunkIterator<LcdpModulePageCompBean> chunkIterator = ListChunkIterator.of(dealPageCompList, 400);
        while (chunkIterator.hasNext()) {
            List<LcdpModulePageCompBean> chunkItemList = chunkIterator.nextChunk();
            lcdpModulePageCompService.getDao().update(chunkItemList, "CONFIG");
        }
        getDao().fastInsert(insertPageI18nList);
    }

    @Override
    @Cacheable(value = "T_LCDP_MODULE_PAGE_I18N.BY_MODULEPAGEHISTORYID", key = "'' + #modulePageHistoryId", unless = "#result == null")
    public List<LcdpModulePageI18nBean> selectListByModulePageHistoryId(Long modulePageHistoryId) {
        return selectListByFilter(SearchFilter.instance().match("MODULEPAGEHISTORYID", modulePageHistoryId).filter(MatchPattern.EQ));
    }

    @Override
    @Transactional
    public void copy(Map<Long, Long> historyIdMapping) {
        if (historyIdMapping.isEmpty()) {
            return;
        }

        List<Long> historyIdList = new ArrayList<>(historyIdMapping.keySet());
        List<LcdpModulePageI18nBean> pageI18nList = selectListByFilter(SearchFilter.instance().match("MODULEPAGEHISTORYID",
                historyIdList).filter(MatchPattern.OR));

        if (pageI18nList.isEmpty()) {
            return;
        }

        List<Long> newHistoryIdList = new ArrayList<>(historyIdMapping.values());

        List<LcdpResourceHistoryBean> newHistoryList = lcdpResourceHistoryService.getDao().selectListByIds(newHistoryIdList, Arrays.asList("ID", "RESOURCEID", "VERSION"));

        List<LcdpModulePageI18nBean> insertPageI18nList = new ArrayList<>();
        for (LcdpModulePageI18nBean pageI18n : pageI18nList) {
            Long newId = historyIdMapping.get(pageI18n.getModulePageHistoryId());
            LcdpResourceHistoryBean newHistory = newHistoryList.stream().filter(h -> h.getId().equals(newId)).findAny().get();

            LcdpModulePageI18nBean insertPageI18n = new LcdpModulePageI18nBean();
            BeanUtils.copyProperties(pageI18n, insertPageI18n, PersistableHelper.ignoreProperties());
            insertPageI18n.setId(ApplicationContextHelper.getNextIdentity());
            insertPageI18n.setModulePageId(newHistory.getResourceId());
            insertPageI18n.setModulePageHistoryId(newHistory.getId());

            insertPageI18nList.add(insertPageI18n);
        }

        getDao().fastInsert(insertPageI18nList);
    }

    //---------------------------------------------------------------------------------------------
    // 私有方法
    //---------------------------------------------------------------------------------------------
    private boolean handlePageCompJsonI18n(JSONObject configObject, String pageCompi18nCodePrefix, Map<String, String> i18nMessageMap, LcdpModulePageCompBean comp, List<String> thisPageI18nCodeList, List<LcdpModulePageI18nBean> thisPageI18nList) {
        //是否更改配置标志
        AtomicBoolean handledFlag = new AtomicBoolean(false);

        //遍历该JsonObject
        configObject.forEach((k, v) -> {
            if (!(v instanceof JSONObject)) {
                return;
            }
            JSONObject propertyObject = (JSONObject) v;
            Iterator<String> iterator = propertyObject.keySet().iterator();

            while (iterator.hasNext()) {
                String property = iterator.next();

                Object value = propertyObject.get(property);

                //若存在zh-CN属性，则认为该JsonObject为国际化属性值
                if (StringUtils.equals(property, "zh-CN") && !propertyObject.containsKey("type")) {

                    propertyObject.put("type", "i18n");
                    handledFlag.set(true);
                    String sysCode = i18nMessageMap.get(value);
                    if (!StringUtils.isEmpty(sysCode)) {
                        propertyObject.put("i18nCode", sysCode);
                        if (!thisPageI18nCodeList.contains(sysCode)) {
                            thisPageI18nCodeList.add(sysCode);
                        }
                    } else if (!StringUtils.isEmpty(pageCompi18nCodePrefix)) {
                        propertyObject.put("i18nCode", pageCompi18nCodePrefix + k);

                        if (!StringUtils.isEmpty(String.valueOf(value))) {
                            LcdpModulePageI18nBean lcdpModulePageI18nBean = new LcdpModulePageI18nBean();
                            lcdpModulePageI18nBean.setId(ApplicationContextHelper.getNextIdentity());
                            lcdpModulePageI18nBean.setCode(pageCompi18nCodePrefix + k);
                            lcdpModulePageI18nBean.setModulePageHistoryId(comp.getModulePageHistoryId());
                            lcdpModulePageI18nBean.setModulePageId(comp.getModulePageId());
                            lcdpModulePageI18nBean.setMessage(String.valueOf(value));
                            lcdpModulePageI18nBean.setI18nConfigId("zh-CN");

                            thisPageI18nList.add(lcdpModulePageI18nBean);
                        }

                        if (!StringUtils.isEmpty(String.valueOf(propertyObject.get("en-US")))) {
                            LcdpModulePageI18nBean lcdpModulePageI18nBean = new LcdpModulePageI18nBean();
                            lcdpModulePageI18nBean.setId(ApplicationContextHelper.getNextIdentity());
                            lcdpModulePageI18nBean.setCode(pageCompi18nCodePrefix + k);
                            lcdpModulePageI18nBean.setModulePageHistoryId(comp.getModulePageHistoryId());
                            lcdpModulePageI18nBean.setModulePageId(comp.getModulePageId());
                            lcdpModulePageI18nBean.setMessage(propertyObject.getString("en-US"));
                            lcdpModulePageI18nBean.setI18nConfigId("en-US");

                            thisPageI18nList.add(lcdpModulePageI18nBean);
                        }

                    }
                }
            }
        });

        return handledFlag.get();
    }


    private boolean handlePageCompJsonI18nGridAndForm(JSONObject configObject, String pageCompi18nCodePrefix, Map<String, String> i18nMessageMap, LcdpModulePageCompBean comp, List<String> thisPageI18nCodeList, List<LcdpModulePageI18nBean> thisPageI18nList, Map<Long, List<String>> historyId2CodeList) {
        //是否更改配置标志
        AtomicBoolean handledFlag = new AtomicBoolean(false);

        //遍历该JsonObject
        configObject.forEach((k, v) -> {
            if (!(v instanceof JSONObject)) {
                return;
            }
            JSONObject propertyObject = (JSONObject) v;
            Iterator<String> iterator = propertyObject.keySet().iterator();

            while (iterator.hasNext()) {
                String property = iterator.next();

                Object value = propertyObject.get(property);

                //若存在zh-CN属性，则认为该JsonObject为国际化属性值
                if (StringUtils.equals(property, "zh-CN") && propertyObject.containsKey("type")&&StringUtils.equals(propertyObject.getString("type"), "i18n")&&!propertyObject.containsKey("i18nCode")) {
                    propertyObject.put("type", "i18n");
                    handledFlag.set(true);
                    String sysCode = i18nMessageMap.get(value);
                    if (!StringUtils.isEmpty(sysCode)) {
                        propertyObject.put("i18nCode", sysCode);
                        if (!thisPageI18nCodeList.contains(sysCode)) {
                            thisPageI18nCodeList.add(sysCode);
                        }
                    } else if (!StringUtils.isEmpty(pageCompi18nCodePrefix)) {
                        propertyObject.put("i18nCode", pageCompi18nCodePrefix + k);

                        if (!StringUtils.isEmpty(String.valueOf(value))) {
                            LcdpModulePageI18nBean lcdpModulePageI18nBean = new LcdpModulePageI18nBean();
                            lcdpModulePageI18nBean.setId(ApplicationContextHelper.getNextIdentity());
                            lcdpModulePageI18nBean.setCode(pageCompi18nCodePrefix + k);
                            lcdpModulePageI18nBean.setModulePageHistoryId(comp.getModulePageHistoryId());
                            lcdpModulePageI18nBean.setModulePageId(comp.getModulePageId());
                            lcdpModulePageI18nBean.setMessage(String.valueOf(value));
                            lcdpModulePageI18nBean.setI18nConfigId("zh-CN");

                            thisPageI18nList.add(lcdpModulePageI18nBean);
                        }

                        if (!StringUtils.isEmpty(String.valueOf(propertyObject.get("en-US")))) {
                            LcdpModulePageI18nBean lcdpModulePageI18nBean = new LcdpModulePageI18nBean();
                            lcdpModulePageI18nBean.setId(ApplicationContextHelper.getNextIdentity());
                            lcdpModulePageI18nBean.setCode(pageCompi18nCodePrefix + k);
                            lcdpModulePageI18nBean.setModulePageHistoryId(comp.getModulePageHistoryId());
                            lcdpModulePageI18nBean.setModulePageId(comp.getModulePageId());
                            lcdpModulePageI18nBean.setMessage(propertyObject.getString("en-US"));
                            lcdpModulePageI18nBean.setI18nConfigId("en-US");

                            thisPageI18nList.add(lcdpModulePageI18nBean);
                        }

                    }
                }


            }
        });

        return handledFlag.get();
    }


    private List<PropertyContext> generateExcelColumn() {

        List<DownloadParamExtractor.TitleDef> titleDefList = new ArrayList<>();

        DownloadParamExtractor.TitleDef code = new DownloadParamExtractor.TitleDef();
        code.setId("code");
        code.setName(I18nHelper.getMessage("T_CORE_I18N.CODE"));
        titleDefList.add(code);

        DownloadParamExtractor.TitleDef description = new DownloadParamExtractor.TitleDef();
        description.setId("description");
        description.setName(I18nHelper.getMessage("T_CORE_I18N.DESCRIPTION"));
        titleDefList.add(description);

        List<CoreI18nConfigBean> coreI18nConfigList = coreI18nConfigService.selectAll();
        coreI18nConfigList.forEach(config -> {
            DownloadParamExtractor.TitleDef configDef = new DownloadParamExtractor.TitleDef();
            configDef.setId(config.getLocaleName());
            configDef.setName(config.getLocaleName());
            titleDefList.add(configDef);
        });

        List<PropertyContext> propertyContextList = new ArrayList<>();
        titleDefList.forEach(t -> {
            String title = t.getName();
            String name = StringUtils.removeStart(t.getId(), "ext$.");

            Function<Object, Object> function = null;

            HorizontalAlignment alignment = HorizontalAlignment.LEFT;

            PropertyContext propertyContext = DefaultPropertyContextImpl.of(title, name, function, alignment);

            propertyContextList.add(propertyContext);
        });

        return propertyContextList;
    }

    private void parseParam(RestJsonWrapperBean jsonWrapper) {
        Map<String, String> paramMap = jsonWrapper.getParamMap();
        String qf = paramMap.get("qf");
        if (!StringUtils.isEmpty(qf)) {
            JSONObject jsonObject = JSON.parseObject(qf);
            Set<String> paramSet = new HashSet<>(Arrays.asList("description", "code", "path"));
            jsonObject.forEach((k, v) -> {
                String paramKey = k.replace("_CISC", "");
                if (!paramSet.contains(paramKey)) {
                    jsonWrapper.setFilterValue("localeId", paramKey);
                    jsonWrapper.setFilterValue("message", v);
                } else {
                    jsonWrapper.setFilterValue(paramKey, v);
                }
            });
        }
    }
}
