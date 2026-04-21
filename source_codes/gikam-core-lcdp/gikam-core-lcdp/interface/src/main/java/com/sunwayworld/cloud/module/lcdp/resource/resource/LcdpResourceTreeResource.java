package com.sunwayworld.cloud.module.lcdp.resource.resource;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

@RequestMapping(LcdpPathConstant.RESOURCE_TREE_PATH)
public interface LcdpResourceTreeResource {
    @RequestMapping(value = {"/{parentId}", ""}, method = RequestMethod.POST)
    List<LcdpResourceTreeNodeDTO> selectTree(@PathVariable(required = false) String parentId, RestJsonWrapperBean jsonWrapper);

    @RequestMapping(value = "/from-resource/{resourceId}", method = RequestMethod.POST)
    List<LcdpResourceTreeNodeDTO> selectTreeUpwardList(@PathVariable Long resourceId, RestJsonWrapperBean jsonWrapper);
    
    @RequestMapping(value = "/checkout-overview", method = RequestMethod.POST)
    List<LcdpResourceTreeNodeDTO> selectCheckoutOverviewTree();
    
    @RequestMapping(value = "/modules", method = RequestMethod.POST)
    List<LcdpResourceTreeNodeDTO> selectModuleTree();
}
