package com.sunwayworld.cloud.module.lcdp.resourcefile.resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import com.sunwayworld.cloud.module.lcdp.resourcefile.bean.LcdpResourceFileBean;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;
import com.sunwayworld.framework.support.choosable.resource.GenericChoosableCloudResource;
import com.sunwayworld.module.item.file.bean.CoreFileBean;

@RequestMapping(LcdpPathConstant.RESOURCE_FILE_PATH)
public interface LcdpResourceFileResource extends GenericCloudResource<LcdpResourceFileBean, Long> {

    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    LcdpResourceFileBean upload(LcdpResourceFileBean resourceFile, MultipartFile file);
}
