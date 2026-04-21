package com.sunwayworld.cloud.module.lcdp.resource.service;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpViewButtonRoleBean;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.service.GenericService;
import com.sunwayworld.framework.support.choosable.service.GenericChoosableService;
import com.sunwayworld.module.sys.menu.bean.CoreMenuBean;

public interface LcdpViewButtonRoleService extends GenericService<LcdpViewButtonRoleBean, Long>, GenericChoosableService<LcdpViewButtonRoleBean, Long> {

    Page<LcdpViewButtonRoleBean> selectPaginationByViewInfo(RestJsonWrapperBean wrapper);

    Page<LcdpViewButtonRoleBean> selectDistinctRawPagination(RestJsonWrapperBean wrapper);


    void initMenuPageData();

    void updatePageInfo(List<LcdpResourceBean> pageList);

    void insertViewInfo( CoreMenuBean coreMenu);
}
