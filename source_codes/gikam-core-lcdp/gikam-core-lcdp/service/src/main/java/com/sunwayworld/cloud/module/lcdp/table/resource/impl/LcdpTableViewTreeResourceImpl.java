package com.sunwayworld.cloud.module.lcdp.table.resource.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableViewTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.table.resource.LcdpTableViewTreeResource;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableViewTreeService;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@GikamBean
@RestController
@LogModule("低代码数据库表和视图的树")
public class LcdpTableViewTreeResourceImpl implements LcdpTableViewTreeResource {
    @Autowired
    private LcdpTableViewTreeService treeService;

    @Override
    public List<LcdpTableViewTreeNodeDTO> selectTree(RestJsonWrapperBean wrapper) {
        return treeService.selectTree(wrapper);
    }
}
