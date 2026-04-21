package com.sunwayworld.cloud.module.lcdp.checkoutrecord.service;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCheckOutDTO;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCheckoutRecordBean;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCrdLogBean;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpCheckoutRecordService extends GenericService<LcdpCheckoutRecordBean, Long> {

    Page<LcdpCrdLogBean> selectLogPaginationByCheckoutRecordId(Long id, RestJsonWrapperBean wrapper);

    LcdpCheckOutDTO confirm(RestJsonWrapperBean jsonWrapper);

    LcdpCheckOutDTO export(RestJsonWrapperBean jsonWrapper);

    LcdpCheckOutDTO checkout(RestJsonWrapperBean jsonWrapper);

    void doCheckout(List<LcdpCrdLogBean> checkoutList, LcdpCheckOutDTO checkOutDTO, LcdpCheckoutRecordBean checkoutRecord);

    LcdpCheckOutDTO checkoutNetworkTest(RestJsonWrapperBean jsonWrapper);
}
