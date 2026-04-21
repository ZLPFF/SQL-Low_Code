package com.sunwayworld.cloud.module.lcdp.scriptblock.resource.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.scriptblock.bean.LcdpScriptBlockBean;
import com.sunwayworld.cloud.module.lcdp.scriptblock.bean.LcdpScriptBlockTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.scriptblock.resource.LcdpScriptBlockResource;
import com.sunwayworld.cloud.module.lcdp.scriptblock.service.LcdpScriptBlockService;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;

@LogModule("脚本代码块表")
@RestController
@GikamBean
public class LcdpScriptBlockResourceImpl implements LcdpScriptBlockResource, AbstractGenericResource<LcdpScriptBlockService, LcdpScriptBlockBean, Long> {

    @Autowired
    private LcdpScriptBlockService lcdpScriptBlockService;

    @Override
    public LcdpScriptBlockService getService() {
        return lcdpScriptBlockService;
    }

    @Log(value = "新增脚本代码块表", type = LogType.INSERT)
    @Override
    @RequestMapping(method = RequestMethod.POST)
    public Long insert(RestJsonWrapperBean wrapper) {
        return getService().insert(wrapper);
    }

    @Log(value = "查询代码块树", type = LogType.SELECT)
    @Override
    public List<LcdpScriptBlockTreeNodeDTO> selectTree(RestJsonWrapperBean wrapper) {
        return getService().selectTree(wrapper);
    }

    @Log(value = "拖拽代码块树", type = LogType.SELECT)
    @Override
    public void drag(RestJsonWrapperBean wrapper) {
        getService().drag(wrapper);
    }

    @Log(value = "导出代码块", type = LogType.EXPORT)
    @Override
    public String export(RestJsonWrapperBean wrapper) {
        return getService().export(wrapper);
    }



}
