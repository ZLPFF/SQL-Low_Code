package com.sunwayworld.cloud.module.lcdp.resource.resource;

import com.sunwayworld.cloud.module.lcdp.base.LcdpResultDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModuleSourceConvertResultDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpPathConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.framework.data.ResultEntity;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;


@RequestMapping(LcdpPathConstant.RESOURCE_MANAGE_PATH)
public interface LcdpResourceManageResource {
    @RequestMapping(method = RequestMethod.POST)
    Long insert(RestJsonWrapperBean wrapper);
    
    @RequestMapping(value = "/by-module-tmpl", method = RequestMethod.POST)
    Long insertByModuleTmpl(RestJsonWrapperBean wrapper);
    
    @RequestMapping(value = "/by-page-tmpl", method = RequestMethod.POST)
    Long insertByPageTmpl(RestJsonWrapperBean wrapper);
    
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    LcdpResultDTO save(@PathVariable Long id, RestJsonWrapperBean wrapper);
    
    @RequestMapping(method = RequestMethod.DELETE)
    void delete(RestJsonWrapperBean wrapper);
    
    @RequestMapping(value = "/modules/{id}/action/copy", method = RequestMethod.POST)
    Long copyModule(@PathVariable Long id, RestJsonWrapperBean wrapper);
    
    @RequestMapping(value = "/scripts/{id}/action/copy", method = RequestMethod.POST)
    Long copyScript(@PathVariable Long id, RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/{id}/action/checkout", method = RequestMethod.POST)
    LcdpResourceDTO checkoutScript(@PathVariable Long id);

    @RequestMapping(value = "/modules/{id}/action/checkout", method = RequestMethod.POST)
    void checkoutModule(@PathVariable Long id);

    @RequestMapping(value = "/modules/{id}/action/cancel-checkout", method = RequestMethod.POST)
    void cancelCheckoutModule(@PathVariable Long id);

    @RequestMapping(value = "/action/cancel-checkout", method = RequestMethod.POST)
    void cancelCheckout(RestJsonWrapperBean wrapper);
    
    @RequestMapping(value = "/action/submit", method = RequestMethod.POST)
    void submitResource(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/action/submit-by-history", method = RequestMethod.POST)
    void submitResourceByHistory(RestJsonWrapperBean wrapper);
    
    @RequestMapping(value = "/action/revert", method = RequestMethod.POST)
    void revertResource(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/action/create-table", method = RequestMethod.POST)
    String createTableServer(@RequestBody LcdpTableDTO tableDTO);

    @RequestMapping(value = "/action/convert-page/{id}", method = RequestMethod.PUT)
    LcdpResultDTO convertPage(@PathVariable Long id, RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/action/data-clean", method = RequestMethod.POST)
    void dataClean(RestJsonWrapperBean wrapper);


    @RequestMapping(value = "/action/validate/data-clean", method = RequestMethod.POST)
    ResultEntity validateDataClean(RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/modules/{id}/action/convert-source", method = RequestMethod.POST)
    LcdpModuleSourceConvertResultDTO convertModuleSource(@PathVariable Long id, RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/modules/{id}/action/convert-record-tree", method = RequestMethod.POST)
    List<LcdpResourceTreeNodeDTO> selectConvertRecordTree(@PathVariable Long id, RestJsonWrapperBean wrapper);

    @RequestMapping(value = "/action/convert-record-overview-tree", method = RequestMethod.POST)
    List<LcdpResourceTreeNodeDTO> selectConvertRecordOverviewTree(RestJsonWrapperBean wrapper);
}
