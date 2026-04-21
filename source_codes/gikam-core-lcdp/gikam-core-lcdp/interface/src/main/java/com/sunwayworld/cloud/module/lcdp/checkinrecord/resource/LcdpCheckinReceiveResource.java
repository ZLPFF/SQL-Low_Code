package com.sunwayworld.cloud.module.lcdp.checkinrecord.resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogType;

@RequestMapping(LcdpPathConstant.CHECKIN_RECORD_RECEIVE_PATH)
public interface LcdpCheckinReceiveResource {
    @RequestMapping(value = "/action/receive", method = RequestMethod.POST)
    void receive(MultipartFile file, String checkoutRecord);


    @RequestMapping(value = "/action/network-test", method = RequestMethod.POST)
    @Log(value = "网络测试", type = LogType.INSERT)
    void networkTest();
}
