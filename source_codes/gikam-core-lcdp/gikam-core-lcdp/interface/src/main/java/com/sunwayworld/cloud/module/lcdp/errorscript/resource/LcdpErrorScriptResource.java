package com.sunwayworld.cloud.module.lcdp.errorscript.resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.errorscript.bean.LcdpErrorScriptBean;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;

@RequestMapping(LcdpPathConstant.ERROR_SCRIPT_PATH)
public interface LcdpErrorScriptResource extends GenericCloudResource<LcdpErrorScriptBean, Long> {
    @RequestMapping(value = "/queries/warning", method = RequestMethod.POST)
    Page<LcdpErrorScriptBean> selectWarningPagination(RestJsonWrapperBean wrapper);
    
    @RequestMapping(value = "/number-of-warnings", method = RequestMethod.GET)
    Long selectNumberOfWarnings();
}
