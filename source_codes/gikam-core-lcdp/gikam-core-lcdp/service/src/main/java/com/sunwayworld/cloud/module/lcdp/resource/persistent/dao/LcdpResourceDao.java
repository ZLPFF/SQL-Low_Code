package com.sunwayworld.cloud.module.lcdp.resource.persistent.dao;

import java.util.List;
import java.util.Map;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceSearchDTO;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.support.base.dao.GenericDao;

public interface LcdpResourceDao extends GenericDao<LcdpResourceBean, Long> {

    List<Map<String, Object>> selectTreeChildQtyByParentId(List<Long> parentIdList);

    List<Map<String, Object>> selectPageByCondition(MapperParameter parameter);
    
    List<String> selectPathListByResourceName(String resourceName);
    
    List<LcdpResourceBean> selectReferencedList(String path);
    
    List<LcdpResourceBean> selectTreeSearchList(String resourceName, String columns);
    
    List<LcdpResourceBean> selectTreeCheckoutLeafNodeList(List<Long> categoryIdList);
    
    List<LcdpResourceBean> selectTreeErrorLeafNodeList(List<Long> categoryIdList);
    
    List<LcdpResourceBean> selectPageServiceList();
    
    List<LcdpResourceBean> selectVisibleModuleList(boolean excludeDeleted);
    
    List<LcdpResourceBean> selectCheckoutableModuleList(List<Long> categoryIdList);
    
    List<LcdpResourceBean> selectCheckoutableResourceList(List<Long> moduleIdList);
    
    List<LcdpResourceBean> selectSubmittableResourceList(String userId, String columns);
    
    List<LcdpResourceBean> selectToBeCopiedMapperList(String fromSuffix, String toSuffix);
    
    List<LcdpResourceBean> selectCheckoutedList();
    
    List<LcdpResourceSearchDTO> selectJavaByKeyword(String userId, String keyword, String matchCase);
    
    List<LcdpResourceSearchDTO> selectJavascriptByKeyword(String userId, String keyword, String matchCase);
    
    List<LcdpResourceSearchDTO> selectMapperByKeyword(String userId, String mapperSuffix, String keyword, String matchCase);
    
    List<LcdpResourceSearchDTO> selectCompByKeyword(String userId, String keyword, String matchCase);
    
    List<LcdpResourceBean> selectMoveOutModuleList(List<Long> categoryIdList);
    
    List<LcdpResourceBean> selectIdAndParentIdList(MapperParameter parameter);

    List<LcdpResourceBean> selectEffectIdList(MapperParameter mapperParameter);
}
