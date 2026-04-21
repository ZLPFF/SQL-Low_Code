package com.sunwayworld.cloud.module.lcdp.submitlog.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.resourceversion.bean.LcdpResourceVersionBean;
import com.sunwayworld.cloud.module.lcdp.submitlog.bean.LcdpSubmitLogBean;
import com.sunwayworld.cloud.module.lcdp.submitlog.resource.LcdpSubmitLogResource;
import com.sunwayworld.cloud.module.lcdp.submitlog.service.LcdpSubmitLogService;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;

@LogModule("提交日志")
@RestController
@GikamBean
public class LcdpSubmitLogResourceImpl implements LcdpSubmitLogResource, AbstractGenericResource<LcdpSubmitLogService, LcdpSubmitLogBean, Long> {

    @Autowired
    private LcdpSubmitLogService lcdpSubmitLogService;

    @Override
    public LcdpSubmitLogService getService() {
        return lcdpSubmitLogService;
    }

    @Log(value = "新增提交日志", type = LogType.INSERT)
    @Override
    @RequestMapping(method = RequestMethod.POST)
    public Long insert(RestJsonWrapperBean wrapper) {
        return getService().insert(wrapper);
    }

    @Override
    @Log(value = "根据提交日志表ID查询提交信息", type = LogType.SELECT)
    public Page<LcdpResourceVersionBean> selectVersionPaginationByLogId(Long id, RestJsonWrapperBean wrapper) {
        return getService().selectVersionPaginationByLogId(id,wrapper);
    }

    @Override
    @Log(value = "选择日志信息查看提交的资源", type = LogType.SELECT)
    public Page<LcdpResourceVersionBean> viewResource(RestJsonWrapperBean wrapper) {
       return getService().viewResource(wrapper);
    }


}
