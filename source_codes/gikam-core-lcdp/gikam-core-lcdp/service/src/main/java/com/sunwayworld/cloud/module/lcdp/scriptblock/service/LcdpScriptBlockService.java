package com.sunwayworld.cloud.module.lcdp.scriptblock.service;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.scriptblock.bean.LcdpScriptBlockBean;
import com.sunwayworld.cloud.module.lcdp.scriptblock.bean.LcdpScriptBlockTreeNodeDTO;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpScriptBlockService extends GenericService<LcdpScriptBlockBean, Long> {

    List<LcdpScriptBlockTreeNodeDTO> selectTree(RestJsonWrapperBean wrapper);

    void drag(RestJsonWrapperBean wrapper);

    String export(RestJsonWrapperBean wrapper);

    void importData(List<LcdpScriptBlockBean> scriptBlockList);

    List<LcdpScriptBlockTreeNodeDTO> buildTreeNodeDTOList(List<LcdpScriptBlockBean> scriptBlockList);
}
