package com.sunwayworld.cloud.module.lcdp.importrecord.service;

import com.sunwayworld.cloud.module.lcdp.importrecord.bean.LcdpCheckImportDataDTO;
import com.sunwayworld.cloud.module.lcdp.importrecord.bean.LcdpResourceImportRecordBean;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpResourceImportRecordService extends GenericService<LcdpResourceImportRecordBean, Long> {

    void revertCheckIn(RestJsonWrapperBean jsonWrapper);

    void checkImportRecord(LcdpCheckImportDataDTO dto);
}
