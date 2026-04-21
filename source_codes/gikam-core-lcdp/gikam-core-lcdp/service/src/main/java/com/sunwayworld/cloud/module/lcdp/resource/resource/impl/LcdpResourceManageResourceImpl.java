package com.sunwayworld.cloud.module.lcdp.resource.resource.impl;

import com.sunwayworld.cloud.module.lcdp.base.LcdpResultDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModuleSourceConvertResultDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.resource.resource.LcdpResourceManageResource;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceManageService;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.framework.data.ResultEntity;
import com.sunwayworld.framework.log.annotation.Log;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.framework.log.annotation.LogType;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@GikamBean
@RestController
@LogModule("低代码资源管理")
public class LcdpResourceManageResourceImpl implements LcdpResourceManageResource {
    @Autowired
    private LcdpResourceManageService resourceManageService;

    @Override
    @Log(value = "新增资源", type = LogType.INSERT)
    public Long insert(RestJsonWrapperBean wrapper) {
        return resourceManageService.insert(wrapper);
    }

    @Override
    @Log(value = "通过模块模板新增资源", type = LogType.INSERT)
    public Long insertByModuleTmpl(RestJsonWrapperBean wrapper) {
        return resourceManageService.insertByModuleTmpl(wrapper);
    }

    @Override
    @Log(value = "通过页面模板新增资源", type = LogType.INSERT)
    public Long insertByPageTmpl(RestJsonWrapperBean wrapper) {
        return resourceManageService.insertByPageTmpl(wrapper);
    }

    @Override
    @Log(value = "保存资源（对于类，如果传compile为非空时，也会编译操作）", type = LogType.UPDATE)
    public LcdpResultDTO save(@PathVariable Long id, RestJsonWrapperBean wrapper) {
        return resourceManageService.save(id, wrapper);
    }

    @Override
    @Log(value = "删除资源", type = LogType.DELETE)
    public void delete(RestJsonWrapperBean wrapper) {
        resourceManageService.delete(wrapper);
    }

    @Override
    @Log(value = "复制模块", type = LogType.COPY)
    public Long copyModule(@PathVariable Long id, RestJsonWrapperBean wrapper) {
        return resourceManageService.copyModule(id, wrapper);
    }

    @Override
    @Log(value = "复制脚本", type = LogType.COPY)
    public Long copyScript(@PathVariable Long id, RestJsonWrapperBean wrapper) {
        return resourceManageService.copyScript(id, wrapper);
    }

    @Override
    @Log(value = "脚本检出", type = LogType.INSERT)
    public LcdpResourceDTO checkoutScript(@PathVariable Long id) {
        return resourceManageService.checkoutScript(id);
    }

    @Override
    @Log(value = "模块检出", type = LogType.INSERT)
    public void checkoutModule(@PathVariable Long id) {
        resourceManageService.checkoutModule(id);
    }

    @Override
    @Log(value = "撤销模块检出", type = LogType.DELETE)
    public void cancelCheckoutModule(@PathVariable Long id) {
        resourceManageService.cancelCheckoutModule(id);
    }

    @Override
    @Log(value = "撤销资源（包括库表和视图）的检出", type = LogType.DELETE)
    public void cancelCheckout(RestJsonWrapperBean wrapper) {
        resourceManageService.cancelCheckout(wrapper);
    }

    @Override
    @Log(value = "资源提交", type = LogType.UPDATE)
    public void submitResource(RestJsonWrapperBean wrapper) {
        resourceManageService.submitResource(wrapper);
    }

    @Override
    @Log(value = "根据资源历史提交资源", type = LogType.UPDATE)
    public void submitResourceByHistory(RestJsonWrapperBean wrapper) {
        resourceManageService.submitResourceByHistory(wrapper);
    }

    @Override
    @Log(value = "资源回滚", type = LogType.UPDATE)
    public void revertResource(RestJsonWrapperBean wrapper) {
        resourceManageService.revertResource(wrapper);
    }

    @Override
    @Log(value = "创建表接口服务", type = LogType.INSERT)
    public String createTableServer(LcdpTableDTO tableDTO) {
        return resourceManageService.createTableServer(tableDTO);
    }

    @Override
    @Log(value = "V12页面转换低代码页面", type = LogType.UPDATE)
    public LcdpResultDTO convertPage(Long id, RestJsonWrapperBean wrapper) {
        return resourceManageService.convertPage(id, wrapper);
    }

    @Override
    @Log(value = "数据一键瘦身", type = LogType.DELETE)
    public void dataClean(RestJsonWrapperBean wrapper) {
        resourceManageService.dataClean(wrapper);
    }


    @Override
    @Log(value = "数据一键瘦身校验", type = LogType.SELECT)
    public ResultEntity validateDataClean(RestJsonWrapperBean wrapper) {
        return resourceManageService.validateDataClean(wrapper);
    }

    @Override
    @Log(value = "模块源码转换", type = LogType.UPDATE)
    public LcdpModuleSourceConvertResultDTO convertModuleSource(@PathVariable Long id, RestJsonWrapperBean wrapper) {
        return resourceManageService.convertModuleSource(id, wrapper);
    }

    @Override
    @Log(value = "查询模块转换记录树", type = LogType.SELECT)
    public List<LcdpResourceTreeNodeDTO> selectConvertRecordTree(@PathVariable Long id, RestJsonWrapperBean wrapper) {
        return resourceManageService.selectConvertRecordTree(id, wrapper);
    }

    @Override
    @Log(value = "查询转换记录概览树", type = LogType.SELECT)
    public List<LcdpResourceTreeNodeDTO> selectConvertRecordOverviewTree(RestJsonWrapperBean wrapper) {
        return resourceManageService.selectConvertRecordOverviewTree(wrapper);
    }
}
