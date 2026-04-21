package com.sunwayworld.cloud.module.lcdp.resource.service;

import com.sunwayworld.cloud.module.lcdp.base.LcdpResultDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModuleSourceConvertResultDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceTreeNodeDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.framework.data.ResultEntity;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

import java.util.List;

/**
 * 资源的增、删、改、复制、检出、提交、撤销检出、回滚、导入等核心操作
 */
public interface LcdpResourceManageService {
    /**
     * 新增资源
     */
    Long insert(RestJsonWrapperBean wrapper);
    
    /**
     * 根据模块模板新增资源
     */
    Long insertByModuleTmpl(RestJsonWrapperBean wrapper);
    
    /**
     * 根据页面模板新增资源
     */
    Long insertByPageTmpl(RestJsonWrapperBean wrapper);
    
    /**
     * 保存操作
     */
    LcdpResultDTO save(Long id, RestJsonWrapperBean wrapper);
    
    /**
     * 删除资源（做删除记号，不真删数据）
     */
    void delete(RestJsonWrapperBean wrapper);
    
    /**
     * 复制模块
     */
    Long copyModule(Long moduleId, RestJsonWrapperBean wrapper);
    
    /**
     * 复制脚本
     */
    Long copyScript(Long id, RestJsonWrapperBean wrapper);
    
    /**
     * 检出脚本
     */
    LcdpResourceDTO checkoutScript(Long id);
    
    /**
     * 检出模块
     */
    void checkoutModule(Long id);
    
    /**
     * 撤销模块的检出
     */
    void cancelCheckoutModule(Long id);
    
    /**
     * 撤销检出（来源于检出概览时在p里传overview为非空）
     */
    void cancelCheckout(RestJsonWrapperBean wrapper);
    
    /**
     * 提交（左侧树、右侧检出项提交）
     */
    void submitResource(RestJsonWrapperBean wrapper);

    /**
     * 根据资源历史ID提交
     */
    void submitResourceByHistory(RestJsonWrapperBean wrapper);
    
    /**
     * 回滚
     */
    void revertResource(RestJsonWrapperBean wrapper);

    /**
     * 创建表接口服务
     * @param tableDTO
     * @return
     */
    String createTableServer(LcdpTableDTO tableDTO);

    LcdpResultDTO convertPage(Long id, RestJsonWrapperBean wrapper);

    void dataClean(RestJsonWrapperBean wrapper);

    ResultEntity validateDataClean(RestJsonWrapperBean wrapper);

    LcdpModuleSourceConvertResultDTO convertModuleSource(Long moduleId, RestJsonWrapperBean wrapper);

    List<LcdpResourceTreeNodeDTO> selectConvertRecordTree(Long moduleId, RestJsonWrapperBean wrapper);

    List<LcdpResourceTreeNodeDTO> selectConvertRecordOverviewTree(RestJsonWrapperBean wrapper);
}
