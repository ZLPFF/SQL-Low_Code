package com.sunwayworld.cloud.module.lcdp.appmarket.resource;

import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(LcdpPathConstant.APPMARKETS_PATH)
public interface LcdpAppMarketResource {
    Object getCodeList(String codeCategoryId);

    Object selectPagination(RestJsonWrapperBean wrapper);

    Object publishFunc(RestJsonWrapperBean wrapper);

    Object applyFunc(RestJsonWrapperBean wrapper);

    Object getFuncVersion(RestJsonWrapperBean wrapper);

    Object deleteFunc(RestJsonWrapperBean wrapper);

    Object getFuncProject();

    Object getExistFuncTableList(RestJsonWrapperBean wrapper);

    Object getFuncPageResourceList(RestJsonWrapperBean wrapper);
}
