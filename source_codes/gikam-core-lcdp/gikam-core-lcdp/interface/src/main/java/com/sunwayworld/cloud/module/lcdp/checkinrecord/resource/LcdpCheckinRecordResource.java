package com.sunwayworld.cloud.module.lcdp.checkinrecord.resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.checkinrecord.bean.LcdpCheckinRecordBean;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCheckOutDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpExportLogFileDTO;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;

@RequestMapping(LcdpPathConstant.CHECKIN_RECORD_PATH)
public interface LcdpCheckinRecordResource extends GenericCloudResource<LcdpCheckinRecordBean, Long> {
    @RequestMapping(value = "/action/import", method = RequestMethod.POST)
    void importFile(RestJsonWrapperBean jsonWrapper);


    @RequestMapping(value = "/action/checkin-analyse", method = RequestMethod.POST)
    LcdpExportLogFileDTO checkinAnalyse(RestJsonWrapperBean jsonWrapper);

    @RequestMapping(value = "/action/checkin", method = RequestMethod.POST)
    String checkin(RestJsonWrapperBean jsonWrapper);

    @RequestMapping(value = "/action/export", method = RequestMethod.POST)
    LcdpCheckOutDTO export(RestJsonWrapperBean jsonWrapper);

    @RequestMapping(value = "/action/checkout", method = RequestMethod.POST)
    LcdpCheckOutDTO checkout(RestJsonWrapperBean jsonWrapper);

    @RequestMapping(value = "/action/ignore", method = RequestMethod.POST)
    void ignore(RestJsonWrapperBean jsonWrapper);

    @RequestMapping(value = "/action/checkin-validator", method = RequestMethod.POST)
    RestValidationResultBean checkinValidator(RestJsonWrapperBean jsonWrapper);
}
