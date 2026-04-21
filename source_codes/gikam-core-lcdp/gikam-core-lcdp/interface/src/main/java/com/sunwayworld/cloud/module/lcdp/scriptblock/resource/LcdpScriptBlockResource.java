package com.sunwayworld.cloud.module.lcdp.scriptblock.resource;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.scriptblock.bean.LcdpScriptBlockBean;
import com.sunwayworld.cloud.module.lcdp.scriptblock.bean.LcdpScriptBlockTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;

@RequestMapping(LcdpPathConstant.SCRIPT_BLOCK_PATH)
public interface LcdpScriptBlockResource extends GenericCloudResource<LcdpScriptBlockBean, Long> {

    @RequestMapping(value = "/tree", method = RequestMethod.POST)
    List<LcdpScriptBlockTreeNodeDTO> selectTree(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/action/drag", method = RequestMethod.PUT)
    void drag(RestJsonWrapperBean wrapper);


    @RequestMapping(value = "/action/export", method = RequestMethod.POST)
    String export(RestJsonWrapperBean wrapper);
}
