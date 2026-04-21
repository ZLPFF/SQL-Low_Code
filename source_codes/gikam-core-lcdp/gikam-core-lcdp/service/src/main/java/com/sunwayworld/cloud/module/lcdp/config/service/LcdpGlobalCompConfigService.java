package com.sunwayworld.cloud.module.lcdp.config.service;

import java.util.List;
import java.util.Map;

import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpGlobalCompConfigBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpGlobalCompConfigService extends GenericService<LcdpGlobalCompConfigBean, Long> {

    Map<String, List<LcdpGlobalCompConfigBean>> selectConfigList();
}
