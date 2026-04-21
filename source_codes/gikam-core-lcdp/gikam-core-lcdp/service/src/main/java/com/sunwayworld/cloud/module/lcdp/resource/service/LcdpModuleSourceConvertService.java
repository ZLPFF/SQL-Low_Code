package com.sunwayworld.cloud.module.lcdp.resource.service;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModuleSourceConvertResultDTO;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

public interface LcdpModuleSourceConvertService {
    LcdpModuleSourceConvertResultDTO convert(Long moduleId, RestJsonWrapperBean wrapper);
}
