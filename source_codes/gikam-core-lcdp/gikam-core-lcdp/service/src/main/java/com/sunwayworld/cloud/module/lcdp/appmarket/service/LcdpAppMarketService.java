package com.sunwayworld.cloud.module.lcdp.appmarket.service;

import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

public interface LcdpAppMarketService {
    Object getCodeList(String codeCategoryId);

    Object selectPagination(RestJsonWrapperBean wrapper);

    Object publishFunc(RestJsonWrapperBean wrapper);

    Object applyFunc(RestJsonWrapperBean wrapper);

    Object getFuncVersion(RestJsonWrapperBean wrapper);

    Object deleteFunc(RestJsonWrapperBean wrapper);

    Object getFuncProject();

    Object getExistFuncTableList(RestJsonWrapperBean wrapper);

    Object getFuncPageResourceList(RestJsonWrapperBean wrapper);

    Object getFuncResourceContent(Long resourceId);
}
