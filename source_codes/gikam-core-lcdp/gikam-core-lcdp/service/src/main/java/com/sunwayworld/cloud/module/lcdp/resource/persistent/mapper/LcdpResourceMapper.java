package com.sunwayworld.cloud.module.lcdp.resource.persistent.mapper;

import java.util.List;
import java.util.Map;

import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.mapper.GenericMapper;
import org.apache.ibatis.annotations.Param;

@GikamBean
public interface LcdpResourceMapper extends GenericMapper<Long> {

    List<Map<String, Object>> selectTreeNodeList(Map<String, Object> parameter);

    List<Map<String, Object>> selectTreeChildQtyByParentId(@Param("parentIdList") List<Long> parentIdList);

    List<Map<String, Object>> selectPageByCondition(MapperParameter parameter);
    
    List<String> selectPathListByResourceName(@Param("resourceName") String resourceName);
    
    List<Map<String, Object>> selectReferencedList(@Param("path") String path);
    
    List<Map<String, Object>> selectTreeSearchList(@Param("resourceName") String resourceName, @Param("columns") String columns);
    
    List<Map<String, Object>> selectTreeCheckoutLeafNodeList(@Param("categoryIdList") List<Long> categoryIdList);
    
    List<Map<String, Object>> selectTreeErrorLeafNodeList(@Param("categoryIdList") List<Long> categoryIdList);
    
    List<Map<String, Object>> selectPageServiceList(@Param("userId") String userId);
    
    List<Map<String, Object>> selectVisibleModuleList(@Param("userId") String userId, @Param("excludeDeleted") boolean excludeDeleted);
    
    List<Map<String, Object>> selectCheckoutableModuleList(@Param("categoryIdList") List<Long> categoryIdList);
    
    List<Map<String, Object>> selectCheckoutableResourceList(@Param("moduleIdList") List<Long> moduleIdList);
    
    List<Map<String, Object>> selectSubmittableResourceList(@Param("userId") String userId, @Param("columns") String columns);
    
    List<Map<String, Object>> selectToBeCopiedMapperList(@Param("fromSuffix") String fromSuffix, @Param("toSuffix") String toSuffix);
    
    List<Map<String, Object>> selectCheckoutedList();
    
    List<Map<String, Object>> selectJavaByKeyword(@Param("userId") String userId, @Param("keyword") String keyword, @Param("matchCase") String matchCase);
    
    List<Map<String, Object>> selectJavascriptByKeyword(@Param("userId") String userId, @Param("keyword") String keyword, @Param("matchCase")  String matchCase);
    
    List<Map<String, Object>> selectMapperByKeyword(@Param("userId") String userId, @Param("mapperSuffix") String mapperSuffix, @Param("keyword") String keyword, @Param("matchCase")  String matchCase);
    
    List<Map<String, Object>> selectCompByKeyword(@Param("userId") String userId, @Param("keyword") String keyword, @Param("matchCase")  String matchCase);
    
    List<Map<String, Object>> selectMoveOutModuleList(@Param("categoryIdList") List<Long> categoryIdList);
    
    List<Map<String, Object>> selectIdAndParentIdList(MapperParameter parameter);

    List<Map<String, Object>> selectEffectIdList(MapperParameter parameter);
}
