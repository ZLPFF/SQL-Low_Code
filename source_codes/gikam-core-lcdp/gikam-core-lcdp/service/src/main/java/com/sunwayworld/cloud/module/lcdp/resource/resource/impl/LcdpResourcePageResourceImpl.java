package com.sunwayworld.cloud.module.lcdp.resource.resource.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.base.LcdpResultDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpPageServiceColumnDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpPageServiceMethodDTO;
import com.sunwayworld.cloud.module.lcdp.resource.resource.LcdpResourcePageResource;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourcePageService;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldDTO;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;

@GikamBean
@RestController
@LogModule("低代码页面配置")
public class LcdpResourcePageResourceImpl implements LcdpResourcePageResource {
    @Autowired
    private LcdpResourcePageService resourcePageService;

    @Override
    @Log(value = "查询所有数据服务", type = LogType.SELECT)
    public List<String> selectServiceList() {
        return resourcePageService.selectServiceList();
    }

    @Override
    @Log(value = "校验SQL的有效性", type = LogType.VALIDATE)
    public LcdpResultDTO validateSql(RestJsonWrapperBean wrapper) {
        return resourcePageService.validateSql(wrapper);
    }

    @Override
    @Log(value = "查询指定数据服务下的所有Mapping方法", type = LogType.SELECT)
    public List<LcdpPageServiceMethodDTO> selectServiceMappingMethodList(RestJsonWrapperBean wrapper) {
        return resourcePageService.selectServiceMappingMethodList(wrapper);
    }

    @Override
    @Log(value = "查询数据服务下的列信息", type = LogType.SELECT)
    public List<LcdpTableFieldDTO> selectServiceColumnList(RestJsonWrapperBean wrapper) {
        return resourcePageService.selectServiceColumnList(wrapper);
    }
}
