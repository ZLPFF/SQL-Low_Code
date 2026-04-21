package com.sunwayworld.cloud.module.lcdp.table.resource.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldGroupBean;
import com.sunwayworld.cloud.module.lcdp.table.resource.LcdpTableFieldGroupResource;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableFieldGroupService;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;
import com.sunwayworld.framework.support.tree.resource.AbstractGenericTreeResource;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

@LogModule("常用表字段分组")
@RestController
@GikamBean
public class LcdpTableFieldGroupResourceImpl implements LcdpTableFieldGroupResource, AbstractGenericResource<LcdpTableFieldGroupService, LcdpTableFieldGroupBean, Long>, AbstractGenericTreeResource<LcdpTableFieldGroupService, LcdpTableFieldGroupBean, Long> {

    @Autowired
    private LcdpTableFieldGroupService lcdpTableFieldGroupService;

    @Override
    public LcdpTableFieldGroupService getService() {
        return lcdpTableFieldGroupService;
    }

    @Log(value = "新增常用表字段分组", type = LogType.INSERT)
    @Override
    @RequestMapping(method = RequestMethod.POST)
    public Long insert(RestJsonWrapperBean wrapper) {
        return getService().insert(wrapper);
    }

    @Override
    @Log(value = "批量插入常用字段分组", type = LogType.INSERT)
    @RequestMapping(value = "/batchinsert", method = RequestMethod.POST)
    public List<LcdpTableFieldGroupBean> insertTableFieldGroupList(RestJsonWrapperBean wrapper) {
        return getService().insertTableFieldGroupList(wrapper);
    }
}
