package com.sunwayworld.cloud.module.lcdp.resource.service;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceTreeNodeDTO;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;

public interface LcdpResourceTreeService {
    List<LcdpResourceTreeNodeDTO> selectTree(String parentId, RestJsonWrapperBean jsonWrapper);
    
    List<LcdpResourceTreeNodeDTO> selectTreeUpwardList(Long resourceId, RestJsonWrapperBean jsonWrapper);
    
    /**
     * 查询检出概览
     */
    List<LcdpResourceTreeNodeDTO> selectCheckoutOverviewTree();
    
    /**
     * 查询分类和模块的树
     */
    List<LcdpResourceTreeNodeDTO> selectModuleTree();
    
    /**
     * 查询左侧树中多选迁出的树
     */
    List<LcdpResourceTreeNodeDTO> selectMoveOutTree(RestJsonWrapperBean jsonWrapper);
}
