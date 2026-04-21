package com.sunwayworld.cloud.module.lcdp.scriptsource.service;

import com.sunwayworld.cloud.module.lcdp.scriptsource.bean.LcdpScriptSourceBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpScriptSourceService extends GenericService<LcdpScriptSourceBean, Long> {

    LcdpScriptSourceBean selectByFullName(String fullName);
}
