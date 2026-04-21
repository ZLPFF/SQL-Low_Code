package com.sunwayworld.cloud.module.lcdp.importrecord.resource;

import com.sunwayworld.cloud.module.lcdp.importrecord.bean.LcdpResourceImportRecordBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceMoveoutDataDTO;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(LcdpPathConstant.RESOURCE_IMPORT_RECORD_PATH)
public interface LcdpResourceImportRecordResource extends GenericCloudResource<LcdpResourceImportRecordBean, Long> {


    @RequestMapping(value = "/revert-checkin", method = RequestMethod.POST)
    void revertCheckIn(RestJsonWrapperBean jsonWrapper);

}
