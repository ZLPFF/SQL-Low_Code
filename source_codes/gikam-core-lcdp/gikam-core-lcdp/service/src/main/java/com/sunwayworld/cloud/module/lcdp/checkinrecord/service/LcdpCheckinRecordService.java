package com.sunwayworld.cloud.module.lcdp.checkinrecord.service;

import org.springframework.web.multipart.MultipartFile;

import com.sunwayworld.cloud.module.lcdp.checkinrecord.bean.LcdpCheckinRecordBean;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCheckOutDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpExportLogFileDTO;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpCheckinRecordService extends GenericService<LcdpCheckinRecordBean, Long> {

    void receive(MultipartFile file, String checkoutRecord);

    void importFile(RestJsonWrapperBean jsonWrapper);

    LcdpExportLogFileDTO checkinAnalyse(RestJsonWrapperBean jsonWrapper);

    String checkin(RestJsonWrapperBean jsonWrapper);


    LcdpCheckOutDTO export(RestJsonWrapperBean jsonWrapper);

    LcdpCheckOutDTO checkout(RestJsonWrapperBean jsonWrapper);

    void ignore(RestJsonWrapperBean jsonWrapper);

    RestValidationResultBean checkinValidator(RestJsonWrapperBean jsonWrapper);

    void networkTest();
}
