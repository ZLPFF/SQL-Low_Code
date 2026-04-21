package com.sunwayworld.cloud.module.lcdp.resourcefile.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sunwayworld.cloud.module.lcdp.resourcefile.bean.LcdpResourceFileBean;
import com.sunwayworld.cloud.module.lcdp.resourcefile.resource.LcdpResourceFileResource;
import com.sunwayworld.cloud.module.lcdp.resourcefile.service.LcdpResourceFileService;
import com.sunwayworld.framework.data.page.Pagination;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;

@LogModule("低代码资源文件表")
@RestController
@GikamBean
public class LcdpResourceFileResourceImpl implements LcdpResourceFileResource,AbstractGenericResource<LcdpResourceFileService, LcdpResourceFileBean, Long> {

    @Autowired
    private LcdpResourceFileService lcdpResourceFileService;

    @Override
    public LcdpResourceFileService getService() {
        return lcdpResourceFileService;
    }

    @Log(value = "新增低代码资源文件表", type = LogType.INSERT)
    @Override
    @RequestMapping(method = RequestMethod.POST)
    public Long insert(RestJsonWrapperBean wrapper) {
        return getService().insert(wrapper);
    }

    @Log(value = "资源文件上传", type = LogType.INSERT)
    @Override
    public LcdpResourceFileBean upload(LcdpResourceFileBean resourceFile, MultipartFile file) {
        return getService().upload(resourceFile,file);
    }
}
