package com.sunwayworld.cloud.module.lcdp.resource.resource.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceSearchDTO;
import com.sunwayworld.cloud.module.lcdp.resource.resource.LcdpResourceSearchResource;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceSearchService;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@GikamBean
@RestController
@LogModule("低代码检索")
public class LcdpResourceSearchResourceImpl implements LcdpResourceSearchResource {
    @Autowired
    private LcdpResourceSearchService searchService;

    @Override
    @Log(value = "检索查询", type = LogType.SELECT)
    public Page<LcdpResourceSearchDTO> selectPagination(RestJsonWrapperBean jsonWrapper) {
        return searchService.selectPagination(jsonWrapper);
    }

    @Override
    @Log(value = "检索中的脚本查询", type = LogType.SELECT)
    public String selectContent(@PathVariable String id) {
        return searchService.selectContent(id);
    }
}
