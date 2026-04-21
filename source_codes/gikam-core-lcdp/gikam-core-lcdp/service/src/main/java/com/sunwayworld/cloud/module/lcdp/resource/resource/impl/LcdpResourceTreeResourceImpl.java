package com.sunwayworld.cloud.module.lcdp.resource.resource.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.resource.resource.LcdpResourceTreeResource;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceTreeService;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@LogModule("低代码平台资源树")
@RestController
@GikamBean
public class LcdpResourceTreeResourceImpl implements LcdpResourceTreeResource {
    @Autowired
    private LcdpResourceTreeService resourceTreeService;

    @Override
    @Log(value = "查询低代码平台左侧和右侧检出项资源树", type = LogType.SELECT)
    @RequestMapping(value = {"/{parentId}", ""}, method = RequestMethod.POST)
    public List<LcdpResourceTreeNodeDTO> selectTree(@PathVariable(required = false) String parentId, RestJsonWrapperBean jsonWrapper) {
        return resourceTreeService.selectTree(parentId, jsonWrapper);
    }

    @Override
    @Log(value = "查询低代码平台左侧资源树从指定资源向上查询树", type = LogType.SELECT)
    @RequestMapping(value = "/from-resource/{resourceId}", method = RequestMethod.POST)
    public List<LcdpResourceTreeNodeDTO> selectTreeUpwardList(@PathVariable Long resourceId, RestJsonWrapperBean jsonWrapper) {
        return resourceTreeService.selectTreeUpwardList(resourceId, jsonWrapper);
    }

    @Override
    @Log(value = "查询低代码平台的检出概览树", type = LogType.SELECT)
    @RequestMapping(value = "/checkout-overview", method = RequestMethod.POST)
    public List<LcdpResourceTreeNodeDTO> selectCheckoutOverviewTree() {
        return resourceTreeService.selectCheckoutOverviewTree();
    }

    @Override
    @Log(value = "查询低代码平台分类和模块的树", type = LogType.SELECT)
    @RequestMapping(value = "/modules", method = RequestMethod.POST)
    public List<LcdpResourceTreeNodeDTO> selectModuleTree() {
        return resourceTreeService.selectModuleTree();
    }
}
