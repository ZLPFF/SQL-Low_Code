package com.sunwayworld.cloud.module.lcdp.checkinrecord.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sunwayworld.cloud.module.lcdp.checkinrecord.resource.LcdpCheckinReceiveResource;
import com.sunwayworld.cloud.module.lcdp.checkinrecord.service.LcdpCheckinRecordService;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@LogModule("迁入记录表")
@RestController
@GikamBean
public class LcdpCheckinRecviveResourceImpl implements  LcdpCheckinReceiveResource {

    @Autowired
    private LcdpCheckinRecordService lcdpCheckinRecordService;

    public LcdpCheckinRecordService getService() {
        return lcdpCheckinRecordService;
    }

    @Override
    @Log(value = "接收迁入资源", type = LogType.INSERT)
    public void receive(MultipartFile file, String checkoutRecord) {
         getService().receive(file,checkoutRecord);
    }

    @Override
    public void networkTest() {
        getService().networkTest();
    }


}
