package com.sunwayworld.cloud.module.lcdp.apiintegration.service;

import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiReqBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiResBean;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpApiReqService extends GenericService<LcdpApiReqBean, Long> {

    Page<LcdpApiResBean> selectResPaginationByApiReqId(Long id, RestJsonWrapperBean wrapper);

    Object doProcessRequest(LcdpApiReqBean request, Object... args);

    void reset(Long id);

    void record(LcdpApiReqBean request, LcdpApiResBean insertResponse);
}
