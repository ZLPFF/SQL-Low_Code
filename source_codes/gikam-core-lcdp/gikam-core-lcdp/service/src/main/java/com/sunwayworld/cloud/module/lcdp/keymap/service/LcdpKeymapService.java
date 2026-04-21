package com.sunwayworld.cloud.module.lcdp.keymap.service;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.keymap.bean.LcdpKeymapBean;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.service.GenericService;

/**
 * @author yangsz@sunway.com 2024-08-07
 */
public interface LcdpKeymapService extends GenericService<LcdpKeymapBean, Long> {
    /***
     * 优化Java源码中的import
     * @return
     */
    List<String> optimazeImports(RestJsonWrapperBean jsonWrapper);

    /***
     * 通过id查询实时代码模板
     */
    String liveTemplate(Long id);
}
