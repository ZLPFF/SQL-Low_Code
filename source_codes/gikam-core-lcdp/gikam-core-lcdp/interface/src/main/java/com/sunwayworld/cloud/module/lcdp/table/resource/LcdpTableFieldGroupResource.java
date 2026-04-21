package com.sunwayworld.cloud.module.lcdp.table.resource;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldGroupBean;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;
import com.sunwayworld.framework.support.tree.resource.GenericTreeCloudResource;

@RequestMapping("/secure/cloud/module/lcdp/table-field-groups")
public interface LcdpTableFieldGroupResource extends GenericCloudResource<LcdpTableFieldGroupBean, Long>, GenericTreeCloudResource<LcdpTableFieldGroupBean, Long> {
    List<LcdpTableFieldGroupBean> insertTableFieldGroupList(RestJsonWrapperBean wrapper);
}
