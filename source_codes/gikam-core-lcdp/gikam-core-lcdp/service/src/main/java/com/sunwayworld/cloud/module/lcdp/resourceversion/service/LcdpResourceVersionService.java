package com.sunwayworld.cloud.module.lcdp.resourceversion.service;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.resourceversion.bean.LcdpResourceVersionBean;
import com.sunwayworld.cloud.module.lcdp.submitlog.bean.LcdpSubmitLogBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpResourceVersionService extends GenericService<LcdpResourceVersionBean, Long> {
    void insertByHistoryResourceList(List<LcdpResourceHistoryBean> resourceHistoryList, LcdpSubmitLogBean submitLog);

    void insertByTableList(List<LcdpTableBean> tableList,LcdpSubmitLogBean submitLog);

    void dealHistoryData(RestJsonWrapperBean wrapper);

    void insertByDeleteResourceList(List<LcdpResourceHistoryBean> toDeleteHistoryList, LcdpSubmitLogBean submitLog);
}
