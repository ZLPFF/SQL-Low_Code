package com.sunwayworld.cloud.module.lcdp.moduletmpl.service;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.moduletmpl.bean.LcdpModuleTmplBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpModuleTmplService extends GenericService<LcdpModuleTmplBean, Long> {
    List<LcdpResourceBean> insertByModuleTmpl(RestJsonWrapperBean wrapper);

    void saveCustomTemplate(RestJsonWrapperBean wrapper);

    Page<LcdpModuleTmplBean> selectCustomTemplatePagination(RestJsonWrapperBean wrapper);
}
