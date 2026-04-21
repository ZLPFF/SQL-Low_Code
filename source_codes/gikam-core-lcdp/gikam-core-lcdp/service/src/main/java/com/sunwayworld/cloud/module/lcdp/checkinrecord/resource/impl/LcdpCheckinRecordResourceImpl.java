package com.sunwayworld.cloud.module.lcdp.checkinrecord.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.checkinrecord.bean.LcdpCheckinRecordBean;
import com.sunwayworld.cloud.module.lcdp.checkinrecord.resource.LcdpCheckinRecordResource;
import com.sunwayworld.cloud.module.lcdp.checkinrecord.service.LcdpCheckinRecordService;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCheckOutDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpExportLogFileDTO;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;

@LogModule("迁入记录表")
@RestController
@GikamBean
public class LcdpCheckinRecordResourceImpl implements LcdpCheckinRecordResource, AbstractGenericResource<LcdpCheckinRecordService, LcdpCheckinRecordBean, Long> {

    @Autowired
    private LcdpCheckinRecordService lcdpCheckinRecordService;

    @Override
    public LcdpCheckinRecordService getService() {
        return lcdpCheckinRecordService;
    }

    @Log(value = "新增迁入记录表", type = LogType.INSERT)
    @Override
    @RequestMapping(method = RequestMethod.POST)
    public Long insert(RestJsonWrapperBean wrapper) {
        return getService().insert(wrapper);
    }


    @Log(value = "资源导入", type = LogType.INSERT)
    @Override
    public void importFile(RestJsonWrapperBean jsonWrapper) {
        getService().importFile(jsonWrapper);
    }

    @Log(value = "迁入资源分析", type = LogType.SELECT)
    @Override
    public LcdpExportLogFileDTO checkinAnalyse(RestJsonWrapperBean jsonWrapper) {
       return getService().checkinAnalyse(jsonWrapper);
    }

    @Log(value = "资源迁入", type = LogType.UPDATE)
    @Override
    public String checkin(RestJsonWrapperBean jsonWrapper) {
        return getService().checkin(jsonWrapper);
    }

    @Log(value = "迁入资源再导出", type = LogType.UPDATE)
    @Override
    public LcdpCheckOutDTO export(RestJsonWrapperBean jsonWrapper) {
        return getService().export(jsonWrapper);
    }

    @Log(value = "迁入资源再迁出", type = LogType.UPDATE)
    @Override
    public LcdpCheckOutDTO checkout(RestJsonWrapperBean jsonWrapper) {
        return getService().checkout(jsonWrapper);
    }

    @Log(value = "迁入信息忽略", type = LogType.UPDATE)
    @Override
    public void ignore(RestJsonWrapperBean jsonWrapper) {
        getService().ignore(jsonWrapper);
    }

    @Log(value = "迁入信息校验数量", type = LogType.SELECT)
    @Override
    public RestValidationResultBean checkinValidator(RestJsonWrapperBean jsonWrapper) {
        return getService().checkinValidator(jsonWrapper);
    }
}
