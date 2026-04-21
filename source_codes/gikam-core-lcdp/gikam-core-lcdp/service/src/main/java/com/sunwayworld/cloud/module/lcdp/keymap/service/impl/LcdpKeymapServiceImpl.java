package com.sunwayworld.cloud.module.lcdp.keymap.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.cloud.module.lcdp.keymap.bean.LcdpKeymapBean;
import com.sunwayworld.cloud.module.lcdp.keymap.persistent.dao.LcdpKeymapDao;
import com.sunwayworld.cloud.module.lcdp.keymap.service.LcdpKeymapService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpJavaCodeResolverUtils;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.ArrayUtils;

/**
 * @author yangsz@sunway.com 2024-08-07
 */
@Repository
@GikamBean
public class LcdpKeymapServiceImpl implements LcdpKeymapService{

    @Autowired
    private LcdpKeymapDao lcdpKeymapDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpKeymapDao getDao() {
        return lcdpKeymapDao;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        LcdpKeymapBean keymap = jsonWrapper.parseUnique(LcdpKeymapBean.class);
        Long id = ApplicationContextHelper.getNextIdentity();
        keymap.setId(id);
        getDao().insert(keymap);
        return id;
    }

    @Override
    public List<String> optimazeImports(RestJsonWrapperBean jsonWrapper) {
        String sourceCode = jsonWrapper.getParamValue("content");
        return LcdpJavaCodeResolverUtils.getOptimizedImportList(sourceCode);
    }

    @Override
    public String liveTemplate(Long id) {
        LcdpKeymapBean lcdpKeymapBean = getDao().selectByIdIfPresent(id);
        return lcdpKeymapBean != null ? lcdpKeymapBean.getTemplate() : null;
    }

    @Override
    public RestValidationResultBean validateUnique(Long aLong, Map<String, Object> columnMap) {
        String binding = String.valueOf(columnMap.get("binding"));

        List<String> keyList = ArrayUtils.asList(binding.split(" \\+ "));

        List<LcdpKeymapBean> bindingList = selectListByFilter(SearchFilter.instance().match("BINDING", keyList).filter(MatchPattern.SC));

        if(bindingList.isEmpty()){
            return new RestValidationResultBean(true);
        }

        return new RestValidationResultBean(false, I18nHelper.getMessage("core.validator.column.not-unique"));
    }
}
