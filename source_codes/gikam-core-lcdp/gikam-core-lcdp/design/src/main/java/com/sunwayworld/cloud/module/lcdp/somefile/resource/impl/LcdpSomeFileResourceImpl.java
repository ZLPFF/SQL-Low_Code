package com.sunwayworld.cloud.module.lcdp.somefile.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.somefile.resource.LcdpSomeFileResource;
import com.sunwayworld.cloud.module.lcdp.somefile.service.LcdpSomeFileService;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@LogModule("文件测试")
@RestController
@GikamBean
public class LcdpSomeFileResourceImpl implements LcdpSomeFileResource {
    @Autowired
    private LcdpSomeFileService someFileService;

    public LcdpSomeFileService getService() {
        return someFileService;
    }
    @Override
    public String selectFiles() {
        return getService().selectFiles();
    }
}
