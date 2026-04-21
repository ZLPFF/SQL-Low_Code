package com.sunwayworld.cloud.module.lcdp.resource.controller.impl;

import org.springframework.stereotype.Controller;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.cloud.module.lcdp.resource.controller.LcdpViewButtonRoleController;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogType;

@Controller
@GikamBean
public class LcdpViewButtonRoleControllerImpl implements LcdpViewButtonRoleController {

    @Log(value = "页面按钮权限编制列表页", type = LogType.CONTROLLER)
    @Override
    public String editListPage() {
        return "module/lcdp/view-button-roles/lcdp-view-button-role-edit-list";
    }

    @Log(value = "页面按钮权限选择列表页", type = LogType.CONTROLLER)
    @Override
    public String choosePage() {
        return "module/lcdp/view-button-roles/lcdp-view-button-role-choose-list";
    }

}
