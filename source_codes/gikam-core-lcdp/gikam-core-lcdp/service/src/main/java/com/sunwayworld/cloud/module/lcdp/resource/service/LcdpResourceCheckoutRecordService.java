package com.sunwayworld.cloud.module.lcdp.resource.service;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceCheckoutRecordBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpResourceCheckoutRecordService extends GenericService<LcdpResourceCheckoutRecordBean, Long> {
    void checkoutResource(List<LcdpResourceBean> resourceList);

    void removeCheckout(List<LcdpResourceBean> resourceList);

    void checkoutTableOrView(String name, String desc, String category);

    void removeCheckoutTableOrView(List<String> nameList);

    void dealResourceCheckoutRecord();
}
