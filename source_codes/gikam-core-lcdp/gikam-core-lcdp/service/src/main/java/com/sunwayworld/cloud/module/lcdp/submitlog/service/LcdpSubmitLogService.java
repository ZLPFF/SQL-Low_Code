package com.sunwayworld.cloud.module.lcdp.submitlog.service;

import com.sunwayworld.cloud.module.lcdp.resourceversion.bean.LcdpResourceVersionBean;
import com.sunwayworld.cloud.module.lcdp.submitlog.bean.LcdpSubmitLogBean;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpSubmitLogService extends GenericService<LcdpSubmitLogBean, Long> {
    Page<LcdpResourceVersionBean> selectVersionPaginationByLogId(Long id, RestJsonWrapperBean wrapper);

    Page<LcdpResourceVersionBean> viewResource(RestJsonWrapperBean wrapper);

}
