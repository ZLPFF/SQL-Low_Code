package com.sunwayworld.cloud.module.lcdp.configparam.service;

import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.configparam.bean.LcdpConfigParamBean;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpConfigParamService extends GenericService<LcdpConfigParamBean, Long> {

    JSONObject selectUserConfigParam();

    String getCurrentDBMybatisMapperParam();

    boolean allowRepetitiveFileName();

    LcdpConfigParamBean selectSubmitConfigParam(RestJsonWrapperBean wrapper);

    void hintsConfig(RestJsonWrapperBean wrapper);
    
    String getParamValue(String paramCode);
}
