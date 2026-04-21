package com.sunwayworld.cloud.module.lcdp.config.service;

import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpConfigCompareDTO;
import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpGlobalConfigBean;
import com.sunwayworld.cloud.module.lcdp.config.bean.LcdpGlobalConfigEditDTO;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpGlobalConfigService extends GenericService<LcdpGlobalConfigBean, Long> {

    LcdpGlobalConfigBean selectConfigContent(String configCode);

    void submit(RestJsonWrapperBean wrapper);

    void submit(LcdpGlobalConfigBean submitConfig, String commitLog);

    void activate(RestJsonWrapperBean wrapper);

    /**
     * 回滚
     */
    void revert(RestJsonWrapperBean wrapper);

    LcdpConfigCompareDTO compare(RestJsonWrapperBean wrapper);

    String selectSysClientCss();
    
    /**
     * 获取指定类型的编辑配置内容
     */
    LcdpGlobalConfigEditDTO selectEditContent(String configCode);
    
    /**
     * 新增或更新指定配置类型下编辑中的数据
     */
    Long saveEditData(String configCode, RestJsonWrapperBean wrapper);
}
