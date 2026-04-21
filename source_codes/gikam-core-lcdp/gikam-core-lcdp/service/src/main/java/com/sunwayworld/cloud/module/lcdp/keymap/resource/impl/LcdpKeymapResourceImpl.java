package com.sunwayworld.cloud.module.lcdp.keymap.resource.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.sunwayworld.cloud.module.lcdp.keymap.bean.LcdpKeymapBean;
import com.sunwayworld.cloud.module.lcdp.keymap.resource.LcdpKeymapResource;
import com.sunwayworld.cloud.module.lcdp.keymap.service.LcdpKeymapService;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.resource.AbstractGenericResource;

/**
 * @author yangsz@sunway.com 2024-08-07
 */
@LogModule("低代码平台快捷键")
@RestController
@GikamBean
public class LcdpKeymapResourceImpl implements LcdpKeymapResource, AbstractGenericResource<LcdpKeymapService, LcdpKeymapBean, Long> {

    @Autowired
    private LcdpKeymapService lcdpKeymapService;

    @Override
    public LcdpKeymapService getService() {
        return lcdpKeymapService;
    }

    @Override
    public List<String> optimazeImports(RestJsonWrapperBean jsonWrapper) {
        return getService().optimazeImports(jsonWrapper);
    }

    @Override
    public String liveTemplate(Long id, RestJsonWrapperBean jsonWrapper) {
        return getService().liveTemplate(id);
    }

    @Override
    public List<LcdpKeymapBean> selectAll() {
        return getService().selectAll();
    }
}
