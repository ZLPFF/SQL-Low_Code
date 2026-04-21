package com.sunwayworld.cloud.module.lcdp.checkoutrecord.resource;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCheckOutDTO;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCheckoutRecordBean;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCrdLogBean;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;

@RequestMapping(LcdpPathConstant.CHECKOUT_RECORD_PATH)
public interface LcdpCheckoutRecordResource extends GenericCloudResource<LcdpCheckoutRecordBean, Long> {
    Page<LcdpCrdLogBean> selectLogPaginationByCheckoutRecordId(Long id, RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/action/confirm", method = RequestMethod.POST)
    LcdpCheckOutDTO confirm(RestJsonWrapperBean jsonWrapper);

    @RequestMapping(value = "/action/export", method = RequestMethod.POST)
    LcdpCheckOutDTO export(RestJsonWrapperBean jsonWrapper);


    @RequestMapping(value = "/action/checkout", method = RequestMethod.POST)
    LcdpCheckOutDTO checkout(RestJsonWrapperBean jsonWrapper);


    @RequestMapping(value = "/action/checkout-network-test", method = RequestMethod.POST)
    LcdpCheckOutDTO checkoutNetworkTest(RestJsonWrapperBean jsonWrapper);
    

    @RequestMapping(value = "/{id}/contents", method = RequestMethod.GET)
    String selectContent(@PathVariable Long id);
}
