package com.sunwayworld.cloud.module.lcdp.table.resource;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableViewTreeNodeDTO;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

@RequestMapping(LcdpPathConstant.TABLEVIEW_TREE_PATH)
public interface LcdpTableViewTreeResource {
    @RequestMapping(method = RequestMethod.POST)
    List<LcdpTableViewTreeNodeDTO> selectTree(RestJsonWrapperBean wrapper);
}
