package com.sunwayworld.cloud.module.lcdp.table.persistent.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.spring.annotation.GikamBean;

/**
 * @author yangsz@sunway.com 2022-10-19
 */
@GikamBean
public interface LcdpDatabaseMapper {

    List<Map<String, Object>> selectFieldListByCondition(MapperParameter mapperParameter);

    List<Map<String, Object>> selectIndexListByCondition(MapperParameter mapperParameter);

    List<Map<String, Object>> selectTableNameListByCondition(MapperParameter mapperParameter);
    
    List<Map<String, Object>> selectTableInfoByCondition(MapperParameter mapperParameter);

    List<Map<String, Object>> selectViewInfoListByCondition(MapperParameter mapperParameter);

    Map<String, Object> selectTableInfoByTableName(@Param("tableName") String tableName);
    
    List<Map<String, Object>> selectIndexListByTableName(@Param("tableName") String tableName);
    
    List<Map<String, Object>> selectFieldListByTableName(@Param("tableName") String tableName);

    int countByTableName(@Param("tableName") String tablename);

    int countByViewName(@Param("viewName") String tablename);

    int countByIndexName(@Param("indexName") String indexName);

    List<Map<String, Object>> lockTableByTableName(@Param("tableName") String name);


    List<Map<String, Object>> selectTableOrViewNameListByCondition(MapperParameter mapperParameter);

}
