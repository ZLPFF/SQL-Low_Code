package com.sunwayworld.cloud.module.lcdp.index.workspace.controller.impl;

import org.springframework.stereotype.Controller;

import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.module.index.workspace.controller.impl.CoreWorkspaceControllerImpl;

@Controller
@GikamBean
public class LcdpWorkspaceControllerImpl extends CoreWorkspaceControllerImpl {
    @Override
    @Log(value = "工作空间页面", type = LogType.CONTROLLER)
    public String indexPage() {
        return "module/lcdp/index/workspace/index";
    }
}
