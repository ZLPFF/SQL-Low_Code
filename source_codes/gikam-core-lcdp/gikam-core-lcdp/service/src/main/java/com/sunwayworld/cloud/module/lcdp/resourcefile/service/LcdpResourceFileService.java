package com.sunwayworld.cloud.module.lcdp.resourcefile.service;

import org.springframework.web.multipart.MultipartFile;

import com.sunwayworld.cloud.module.lcdp.resourcefile.bean.LcdpResourceFileBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpResourceFileService extends GenericService<LcdpResourceFileBean, Long> {

    LcdpResourceFileBean upload(LcdpResourceFileBean resourceFile, MultipartFile file);
}
