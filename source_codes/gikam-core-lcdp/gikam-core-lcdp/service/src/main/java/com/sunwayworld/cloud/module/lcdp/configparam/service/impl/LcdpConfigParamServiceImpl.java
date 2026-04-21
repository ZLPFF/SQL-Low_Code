package com.sunwayworld.cloud.module.lcdp.configparam.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.configparam.bean.LcdpConfigParamBean;
import com.sunwayworld.cloud.module.lcdp.configparam.persistent.dao.LcdpConfigParamDao;
import com.sunwayworld.cloud.module.lcdp.configparam.service.LcdpConfigParamService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.at.annotation.AuditTrailEntry;
import com.sunwayworld.framework.at.annotation.AuditTrailType;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.ArrayUtils;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;

@Repository
@GikamBean
public class LcdpConfigParamServiceImpl implements LcdpConfigParamService {

    @Autowired
    private LcdpConfigParamDao lcdpConfigParamDao;
    @Lazy
    @Autowired
    private LcdpConfigParamService proxyInstance;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpConfigParamDao getDao() {
        return lcdpConfigParamDao;
    }

    @Override
    @Transactional
    @AuditTrailEntry(AuditTrailType.INSERT)
    @Audit(AuditConstant.INSERT)
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        List<LcdpConfigParamBean> configParamList = jsonWrapper.parse(LcdpConfigParamBean.class);

        configParamList.stream().forEach(e -> {
            e.setUserId(LocalContextHelper.getLoginUserId());
        });
        List<LcdpConfigParamBean> existConfigParamList = getDao().selectList(configParamList, ArrayUtils.asList("USERID", "PARAMCODE"), CollectionUtils.emptyList());


        List<LcdpConfigParamBean> insertList = new ArrayList<>();
        List<LcdpConfigParamBean> updateList = new ArrayList<>();

        for (LcdpConfigParamBean configPram : configParamList) {
            LcdpConfigParamBean existConfigParam = existConfigParamList.stream().filter(f -> ObjectUtils.equals(f.getParamCode(), configPram.getParamCode())).findFirst().orElse(null);
            if (existConfigParam == null) {//新增
                configPram.setId(ApplicationContextHelper.getNextIdentity());
                insertList.add(configPram);
            } else {//修改
                existConfigParam.setParamValue(configPram.getParamValue());
                updateList.add(existConfigParam);
            }
        }

        if (insertList.size() > 0) {
            getDao().insert(insertList);
        }

        if (updateList.size() > 0) {
            getDao().update(updateList, "PARAMVALUE");
        }

        return 1L;
    }

    @Override
    @Cacheable(value = "T_LCDP_CONFIG_PARAM.BY_LOGINUSER", keyGenerator = "loginUserKeyGenerator", unless="#result == null")
    public JSONObject selectUserConfigParam() {
        List<LcdpConfigParamBean> configList = getDao().selectListByOneColumnValue(LocalContextHelper.getLoginUserId(), "USERID");

        JSONObject obj = new JSONObject();
        configList.stream().forEach(e -> {
            obj.put(e.getParamCode(), e.getParamValue());
            obj.put("id", e.getId());
        });

        return obj;
    }

    @Override
    public LcdpConfigParamBean selectSubmitConfigParam(RestJsonWrapperBean wrapper) {
        LcdpConfigParamBean filter = new LcdpConfigParamBean();
        filter.setParamCode(LcdpConstant.SUBMIT_TMPL_PARAM_CONFIG);

        LcdpConfigParamBean configParam = getDao().selectFirstIfPresent(filter);
        return configParam;
    }

    @Override
    public void hintsConfig(RestJsonWrapperBean wrapper) {
        String tmplContent = proxyInstance.getParamValue(LcdpConstant.LCDP_HINTS_PARAM_CODE);
        
        if (!StringUtils.isEmpty(tmplContent)) {
            tmplContent = tmplContent.replaceAll("\r", "");
            String[] tmplContents = tmplContent.split("\n");
            // CodeHintsManager.hintsConfig(Arrays.asList(tmplContents));
        }
    }
    
    @Override
    @Cacheable(value = "T_LCDP_CONFIG_PARAM.PARAM_VALUE", key = "#paramCode", unless="#result == null")
    public String getParamValue(String paramCode) {
        LcdpConfigParamBean filter = new LcdpConfigParamBean();
        filter.setParamCode(paramCode);
        
        LcdpConfigParamBean param = getDao().selectFirstIfPresent(filter);
        return param == null ? null : param.getParamValue();
    }

    @Override
    public String getCurrentDBMybatisMapperParam() {
        return proxyInstance.getParamValue("currentDBMybatisMapper");
    }

    @Override
    public boolean allowRepetitiveFileName() {
        String paramValue = proxyInstance.getParamValue("allowRepetitiveFileName");
        
        return StringUtils.equals(paramValue, Constant.YES);
    }
}
