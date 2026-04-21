package com.sunwayworld.cloud.module.lcdp.checkoutrecord.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCheckOutDTO;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCheckoutRecordBean;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCrdLogBean;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.resource.LcdpCheckoutRecordResource;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.service.LcdpCheckoutRecordService;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;

@LogModule("迁出记录表")
@RestController
@GikamBean
public class LcdpCheckoutRecordResourceImpl implements LcdpCheckoutRecordResource, AbstractGenericResource<LcdpCheckoutRecordService, LcdpCheckoutRecordBean, Long> {

    @Autowired
    private LcdpCheckoutRecordService lcdpCheckoutRecordService;

    @Override
    public LcdpCheckoutRecordService getService() {
        return lcdpCheckoutRecordService;
    }

    @Log(value = "新增迁出记录表", type = LogType.INSERT)
    @Override
    @RequestMapping(method = RequestMethod.POST)
    public Long insert(RestJsonWrapperBean wrapper) {
        return getService().insert(wrapper);
    }


    @Override
    @Log(value = "根据迁出记录表ID查询迁出记录日志表", type = LogType.SELECT)
    @RequestMapping(value = "/{id}/logs/queries", method = RequestMethod.POST)
    public Page<LcdpCrdLogBean> selectLogPaginationByCheckoutRecordId(@PathVariable Long id, RestJsonWrapperBean wrapper) {
        return getService().selectLogPaginationByCheckoutRecordId(id, wrapper);
    }

    @Override
    @Log(value = "确认(确认操作包括迁出和导出)", type = LogType.INSERT)
    public LcdpCheckOutDTO confirm(RestJsonWrapperBean jsonWrapper) {
        return getService().confirm(jsonWrapper);
    }

    @Override
    @Log(value = "资源导出", type = LogType.SELECT)
    public LcdpCheckOutDTO export(RestJsonWrapperBean jsonWrapper) {
        return getService().export(jsonWrapper);
    }

    @Override
    @Log(value = "资源迁出", type = LogType.UPDATE)
    public LcdpCheckOutDTO checkout(RestJsonWrapperBean jsonWrapper) {
        return getService().checkout(jsonWrapper);
    }

    @Override
    @Log(value = "资源迁出网络测试", type = LogType.UPDATE)
    public LcdpCheckOutDTO checkoutNetworkTest(RestJsonWrapperBean jsonWrapper) {
        return getService().checkoutNetworkTest(jsonWrapper);
    }
    
    @Override
    @Log(value = "查询资源迁出内容", type = LogType.UPDATE)
    public String selectContent(Long id) {
        return getService().selectColumnById(id, "CONTENT");
    }
}
