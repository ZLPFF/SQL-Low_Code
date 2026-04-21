package com.sunwayworld.cloud.module.lcdp.keymap.resource;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.keymap.bean.LcdpKeymapBean;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;

/**
 * @author yangsz@sunway.com 2024-08-07
 */
@RequestMapping(LcdpPathConstant.KEYMAP_PATH)
public interface LcdpKeymapResource extends GenericCloudResource<LcdpKeymapBean, Long> {

    @Log(value = "优化java代码的imports", type = LogType.SELECT)
    @RequestMapping(value = "/action/optimaze-imports", method = RequestMethod.POST)
    List<String> optimazeImports(RestJsonWrapperBean jsonWrapper);

    @Log(value = "实时代码模板", type = LogType.SELECT)
    @RequestMapping(value = "/action/live-template/{id}", method = RequestMethod.POST)
    String liveTemplate(@PathVariable("id") Long id, RestJsonWrapperBean jsonWrapper);

    @Log(value = "查询快捷键配置", type = LogType.SELECT)
    @RequestMapping(value = "/config/queries", method = RequestMethod.GET)
    List<LcdpKeymapBean> selectAll();

}
