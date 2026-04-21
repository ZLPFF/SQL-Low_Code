package com.sunwayworld.cloud.module.lcdp.resource.service;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceConvertRecordBean;
import com.sunwayworld.framework.support.base.service.GenericService;

import java.util.List;

public interface LcdpResourceConvertRecordService extends GenericService<LcdpResourceConvertRecordBean, Long> {
    void saveConvertedRecords(List<LcdpResourceConvertRecordBean> recordList);
}
