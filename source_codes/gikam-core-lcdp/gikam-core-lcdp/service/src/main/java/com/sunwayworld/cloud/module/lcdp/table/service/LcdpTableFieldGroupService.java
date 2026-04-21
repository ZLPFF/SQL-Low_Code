package com.sunwayworld.cloud.module.lcdp.table.service;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldGroupBean;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.service.GenericService;
import com.sunwayworld.framework.support.tree.service.GenericTreeService;

public interface LcdpTableFieldGroupService extends GenericService<LcdpTableFieldGroupBean, Long>, GenericTreeService<LcdpTableFieldGroupBean, Long> {
    List<LcdpTableFieldGroupBean> insertTableFieldGroupList(RestJsonWrapperBean wrapper);
}
