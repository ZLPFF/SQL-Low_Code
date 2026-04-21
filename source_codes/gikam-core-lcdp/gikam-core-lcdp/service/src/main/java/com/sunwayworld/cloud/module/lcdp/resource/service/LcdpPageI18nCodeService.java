package com.sunwayworld.cloud.module.lcdp.resource.service;

import java.util.Map;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpPageI18nCodeBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpPageI18nCodeService extends GenericService<LcdpPageI18nCodeBean, Long> {
    /**
     * 复制历史资源对应的国际化关联信息到新的历史资源中
     */
    void copy(Map<Long, Long> historyIdMapping);
}
