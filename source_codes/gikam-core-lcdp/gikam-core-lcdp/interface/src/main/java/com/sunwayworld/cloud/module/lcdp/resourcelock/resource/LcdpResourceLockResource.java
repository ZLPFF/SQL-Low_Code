package com.sunwayworld.cloud.module.lcdp.resourcelock.resource;

import com.sunwayworld.cloud.module.lcdp.resourcelock.bean.LcdpResourceLockBean;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.support.base.resource.GenericCloudResource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@RequestMapping(LcdpPathConstant.RESOURCE_LOCK_PATH)
public interface LcdpResourceLockResource extends GenericCloudResource<LcdpResourceLockBean, Long>{

    @RequestMapping(value = {"/resources/{resourceId}/action/validate-editable"}, method = {RequestMethod.POST})
    RestValidationResultBean validateResourceEditable(@PathVariable String resourceId);

}
