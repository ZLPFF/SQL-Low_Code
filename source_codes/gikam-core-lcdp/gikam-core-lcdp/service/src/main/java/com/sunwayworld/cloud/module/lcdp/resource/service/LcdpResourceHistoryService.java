package com.sunwayworld.cloud.module.lcdp.resource.service;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryDevParamDTO;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpResourceHistoryService extends GenericService<LcdpResourceHistoryBean, Long> {
    /**
     * 获取开发环境中的脚本（未提交的）
     */
    List<LcdpResourceHistoryBean> selectDevScriptList(String scriptType);

    List<LcdpResourceHistoryBean> selectListByResourceId(Long resourceId);
    
    List<String> selectDevResourceNameList(List<LcdpResourceHistoryDevParamDTO> devParamList);
    
    /**
     * 路径是否存在
     */
    String isExists(String path);
    
    /**
     * 路径是否生效（存在没有被删除的历史数据）
     */
    String isActive(String path);
    
    /**
     * 编译
     */
    void compile(Long id);
    
    /**
     * 更新编译版本
     */
    void updateCompiledVersionIfNecessary(List<Long> idList);
    
    /**
     * 获取指定用户编辑的未提交的历史资源，如果是超级管理员不控制
     */
    LcdpResourceHistoryBean selectUnsubmittedResourceHistory(String userId, Long resourceId);
    
    /**
     * 根据资源的ID获取最新生效的历史资源
     */
    List<LcdpResourceHistoryBean> selectLatestActivatedListByResourceIdList(List<Long> resourceIdList);
    
    /**
     * 根据资源的ID获取最大的版本的值
     */
    List<LcdpResourceHistoryBean> selectMaxVersionListByResourceIdList(List<Long> resourceIdList);
}
