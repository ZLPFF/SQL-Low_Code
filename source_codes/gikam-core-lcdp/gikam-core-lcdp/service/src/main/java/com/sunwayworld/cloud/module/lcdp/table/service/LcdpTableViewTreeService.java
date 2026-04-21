package com.sunwayworld.cloud.module.lcdp.table.service;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableViewTreeNodeDTO;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

public interface LcdpTableViewTreeService {
    List<LcdpTableViewTreeNodeDTO> selectTree(RestJsonWrapperBean wrapper);
}
