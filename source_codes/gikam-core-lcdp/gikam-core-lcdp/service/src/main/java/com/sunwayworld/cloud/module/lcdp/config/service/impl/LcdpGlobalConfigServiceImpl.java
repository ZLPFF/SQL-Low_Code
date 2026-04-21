package com.sunwayworld.cloud.module.lcdp.config.service.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sunwayworld.cloud.module.lcdp.importrecord.bean.LcdpCheckImportDataDTO;
import com.sunwayworld.cloud.module.lcdp.importrecord.service.LcdpResourceImportRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpConfigCompareDTO;
import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpGlobalConfigBean;
import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpGlobalConfigEditDTO;
import com.sunwayworld.cloud.module.lcdp.config.dao.LcdpGlobalConfigDao;
import com.sunwayworld.cloud.module.lcdp.config.service.LcdpGlobalConfigService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.concurrent.GikamConcurrentLocker;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.data.page.Pagination;
import com.sunwayworld.framework.database.context.TableContext;
import com.sunwayworld.framework.database.sql.Order;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.mybatis.page.PageRowBounds;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.PersistableHelper;
import com.sunwayworld.framework.utils.ArrayUtils;
import com.sunwayworld.framework.utils.BeanUtils;
import com.sunwayworld.framework.utils.NumberUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;

@Repository
@GikamBean
public class LcdpGlobalConfigServiceImpl implements LcdpGlobalConfigService {

    @Autowired
    private LcdpGlobalConfigDao lcdpGlobalConfigDao;
    @Lazy
    @Autowired
    private LcdpGlobalConfigService proxy;

    @Lazy
    @Autowired
    private LcdpResourceImportRecordService lcdpResourceImportRecordService;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpGlobalConfigDao getDao() {
        return lcdpGlobalConfigDao;
    }

    @Override
    public Page<LcdpGlobalConfigBean> selectPagination(RestJsonWrapperBean wrapper) {
        MapperParameter parameter = wrapper.extractMapFilter();
        PageRowBounds rowBounds = wrapper.extractPageRowBounds();

        TableContext context = TableContext.of("T_LCDP_GLOBAL_CONFIG");

        // 不查询clob字段
        String columns = context.getColumnContextList().stream()
                .filter(c -> !"content".equalsIgnoreCase(c.getColumnName()))
                .map(c -> c.getColumnName()).collect(Collectors.joining(", "));
        parameter.put("_columns_", columns);

        return (Pagination<LcdpGlobalConfigBean>) this.selectPagination(parameter, rowBounds);
    }

    @Override
    @Cacheable(value = "T_LCDP_GLOBAL_CONFIG.BY_CONFIGCODE", key = "#configCode", unless = "#result == null")
    public LcdpGlobalConfigBean selectConfigContent(String configCode) {
        LcdpGlobalConfigBean lcdpGlobalConfigBean = selectFirstByFilter(SearchFilter.instance()
                .match("CONFIGCODE", configCode).filter(MatchPattern.SEQ)
                .match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.SEQ));
        return lcdpGlobalConfigBean == null ? new LcdpGlobalConfigBean() : lcdpGlobalConfigBean;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public void submit(RestJsonWrapperBean wrapper) {
        LcdpGlobalConfigBean submitConfig = wrapper.parseUnique(getDao().getType());
        submit(submitConfig, wrapper.getParamValue("commitLog"));
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public void submit(LcdpGlobalConfigBean submitConfig, String commitLog) {
        //查询生效配置
        List<LcdpGlobalConfigBean> effectiveConfigList = selectListByFilter(SearchFilter.instance()
                .match("CONFIGCODE", submitConfig.getConfigCode()).filter(MatchPattern.SEQ)
                .match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.SEQ));
        //查询最高版本配置
        LcdpGlobalConfigBean version = selectFirstByFilter(SearchFilter.instance()
                .match("CONFIGCODE", submitConfig.getConfigCode()).filter(MatchPattern.SEQ), Order.desc("VERSION"));
        //失效原配置
        effectiveConfigList.forEach(config -> config.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO));
        getDao().update(effectiveConfigList, "EFFECTFLAG");

        //添加新配置
        submitConfig.setId(ApplicationContextHelper.getNextIdentity());
        submitConfig.setCommitLog(commitLog);
        submitConfig.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
        submitConfig.setVersion(ObjectUtils.isEmpty(version.getVersion()) ? 1L : version.getVersion() + 1L);
        getDao().insert(submitConfig);
    }

    @Override
    @Transactional
    @Audit(AuditConstant.ACTIVATE)
    public void activate(RestJsonWrapperBean wrapper) {
        LcdpGlobalConfigBean activateConfig = wrapper.parseUnique(getDao().getType());
        activateConfig = selectById(activateConfig.getId());
        //查询生效配置
        List<LcdpGlobalConfigBean> effectiveConfig = selectListByFilter(SearchFilter.instance()
                .match("CONFIGCODE", activateConfig.getConfigCode()).filter(MatchPattern.SEQ)
                .match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.SEQ));
        //失效原配置
        effectiveConfig.forEach(config -> config.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO));
        updateIfChanged(effectiveConfig);
        //激活配置
        activateConfig.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
        updateIfChanged(activateConfig);
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public void revert(RestJsonWrapperBean wrapper) {
        List<Long> idList = wrapper.parseId(Long.class);
        LcdpCheckImportDataDTO importData = new LcdpCheckImportDataDTO();

        if (idList.size() != 1) {
            return;
        }

        String commitLog = wrapper.getParamValue("commitLog"); // 提交日志

        Long id = idList.get(0);

        LcdpGlobalConfigBean revertToConfig = selectById(id);

        // 查询生效配置
        List<LcdpGlobalConfigBean> effectiveConfig = selectListByFilter(SearchFilter.instance()
                .match("CONFIGCODE", revertToConfig.getConfigCode()).filter(MatchPattern.SEQ)
                .match("EFFECTFLAG", LcdpConstant.EFFECT_FLAG_YES).filter(MatchPattern.SEQ));

        // 失效原配置
        effectiveConfig.forEach(config -> config.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO));
        getDao().update(effectiveConfig, "EFFECTFLAG");

        LcdpGlobalConfigBean filter = new LcdpGlobalConfigBean();
        filter.setConfigCode(revertToConfig.getConfigCode());
        LcdpGlobalConfigBean latestConfig = getDao().selectFirst(filter, Order.desc("VERSION"));

        LcdpGlobalConfigBean insertConfig = new LcdpGlobalConfigBean();
        BeanUtils.copyProperties(revertToConfig, insertConfig, PersistableHelper.ignoreProperties());
        insertConfig.setId(ApplicationContextHelper.getNextIdentity());
        insertConfig.setVersion(latestConfig.getVersion() + 1L);
        insertConfig.setEffectFlag(LcdpConstant.EFFECT_FLAG_YES);
        insertConfig.setCommitLog(Optional.ofNullable(commitLog).orElse("")
                + "(" + I18nHelper.getMessage("LCDP.GLOBAL_CONFIG.DEFAULT_REVERT_COMMIT_LOG") + ")");

        getDao().insert(insertConfig);



        if (StringUtils.equalsIgnoreCase(revertToConfig.getConfigCode(), LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_CSS)) {
            importData.setCssOperation(LcdpConstant.OPERATION_OF_ROLLBACK_REVERT);
            if (latestConfig.getVersion() > 1) {
                importData.setSysClientCssVersion(latestConfig.getVersion());

            }
        } else {
            importData.setCssOperation(LcdpConstant.OPERATION_OF_ROLLBACK_REVERT);
            if (latestConfig.getVersion() > 1) {
                importData.setSysClientJsVersion(latestConfig.getVersion());
            }


        }
        importData.setOperation(LcdpConstant.OPERATION_OF_ROLLBACK_REVERT);
        lcdpResourceImportRecordService.checkImportRecord(importData);
    }

    @Override
    public LcdpConfigCompareDTO compare(RestJsonWrapperBean wrapper) {
        //版本配置id
        Long id = Long.valueOf(wrapper.getParamMap().get("resourceId"));

        LcdpConfigCompareDTO compareDTO = new LcdpConfigCompareDTO();
        //当前版本配置
        LcdpGlobalConfigBean currentConfig = selectById(id);
        //全部历史版本配置
        LcdpGlobalConfigBean condition = new LcdpGlobalConfigBean();
        condition.setConfigCode(currentConfig.getConfigCode());
        List<LcdpGlobalConfigBean> historyConfigList = getDao().selectList(condition, ArrayUtils.asList("VERSION", "ID"));
        historyConfigList.sort(Comparator.comparing(LcdpGlobalConfigBean::getVersion).reversed());
        //前一个版本配置
        LcdpGlobalConfigBean previousConfig = historyConfigList.stream().filter(config -> config.getVersion() == currentConfig.getVersion() - 1).findFirst().orElse(null);

        compareDTO.setCurrentVersionHistory(currentConfig);
        compareDTO.setHistoryList(historyConfigList);
        if (!ObjectUtils.isEmpty(previousConfig)) {
            compareDTO.setPreviousVersionHistory(selectById(previousConfig.getId()));
        }

        return compareDTO;
    }

    @Override
    public String selectSysClientCss() {
        String sysClientCSS = selectConfigContent("SYS_CLIENT_CSS").getContent();

        if (StringUtils.isEmpty(sysClientCSS)) {
            return "";
        }


        // 替换所有空格、换行符、制表符
        return sysClientCSS.replaceAll("[\\s\u0000]+", "");
    }

    @Override
    public LcdpGlobalConfigEditDTO selectEditContent(String configCode) {
        LcdpGlobalConfigEditDTO dto = new LcdpGlobalConfigEditDTO();

        LcdpGlobalConfigBean effectConfig = proxy.selectConfigContent(configCode);
        if (effectConfig.getId() != null) { // 有生效的配置
            dto.setEffectId(effectConfig.getId());
            dto.setEffectContent(effectConfig.getContent());
        }

        LcdpGlobalConfigBean latestConfig = selectFirstByFilter(SearchFilter.instance()
                        .match("CONFIGCODE", configCode).filter(MatchPattern.SEQ),
                Order.desc("VERSION"));
        if (latestConfig == null) { // 最新的配置不存在
            dto.setEditContent(dto.getEffectContent());
        } else { // 最新的配置存在
            if (!Objects.equals(latestConfig.getId(), effectConfig.getId())) { // 最新的配置不是生效的配置
                dto.setEditId(latestConfig.getId());
                dto.setEditContent(latestConfig.getContent());
            } else {
                dto.setEditContent(latestConfig.getContent());
            }
        }

        return dto;
    }

    @Override
    @Transactional
    public Long saveEditData(String configCode, RestJsonWrapperBean wrapper) {
        String editId = wrapper.getParamValue("editId");
        String editContent = wrapper.getParamValue("editContent");
        LcdpCheckImportDataDTO importData = new LcdpCheckImportDataDTO();

        // 加锁防止并发
        GikamConcurrentLocker.block("SYSTEM_CONFIG$" + configCode);

        LcdpGlobalConfigBean latestFilter = new LcdpGlobalConfigBean();
        latestFilter.setConfigCode(configCode);
        LcdpGlobalConfigBean latestConfig = getDao().selectFirstIfPresent(latestFilter, Arrays.asList("ID", "EFFECTFLAG", "VERSION"), Order.desc("VERSION"));
        if (latestConfig != null) {
            if (!StringUtils.isBlank(editId)) {
                if (!latestConfig.getId().equals(NumberUtils.parseLong(editId))) {
                    throw new ApplicationRuntimeException("LCDP.EXCEPTION.CONCURRENT_MODIFICATION");
                }
            }

            if (LcdpConstant.EFFECT_FLAG_NO.equals(latestConfig.getEffectFlag())) {
                latestConfig.setContent(editContent);

                getDao().update(latestConfig, "CONTENT");

                if (StringUtils.equalsIgnoreCase(configCode, LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_CSS)) {
                    importData.setCssOperation("checkout");
                    if (latestConfig.getVersion() > 1) {
                        importData.setSysClientCssVersion(latestConfig.getVersion() - 1);

                    }
                } else {
                    importData.setJsOperation("checkout");
                    if (latestConfig.getVersion() > 1) {
                        importData.setSysClientJsVersion(latestConfig.getVersion() - 1);
                    }


                }
                importData.setOperation("checkout");
                lcdpResourceImportRecordService.checkImportRecord(importData);

                return latestConfig.getId();
            }
        }

        LcdpGlobalConfigBean insertConfig = new LcdpGlobalConfigBean();
        insertConfig.setId(ApplicationContextHelper.getNextIdentity());
        insertConfig.setConfigCode(configCode);
        insertConfig.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);
        insertConfig.setContent(editContent);
        if (latestConfig == null) {
            insertConfig.setVersion(1L);
        } else {
            insertConfig.setVersion(latestConfig.getVersion() + 1);
        }

        getDao().insert(insertConfig);
        if (latestConfig != null) {
            if (StringUtils.equalsIgnoreCase(configCode, LcdpConstant.RESOURCE_CATEGORY_GLOBAL_SYSTEM_CSS)) {
                if(importData.getSysClientCssVersion()==null){
                    importData.setSysClientCssVersion(latestConfig.getVersion());
                }
                importData.setCssOperation("checkout");
            } else {
                if(importData.getSysClientJsVersion()==null){
                    importData.setSysClientJsVersion(latestConfig.getVersion());
                }
                importData.setJsOperation("checkout");
            }
            importData.setOperation("checkout");
            lcdpResourceImportRecordService.checkImportRecord(importData);
        }


        return insertConfig.getId();
    }
}
