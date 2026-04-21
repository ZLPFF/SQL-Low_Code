package com.sunwayworld.cloud.module.lcdp.table.persistent.dao;

import java.util.List;
import java.util.Map;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableIndexBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableIndexDTO;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;

/**
 * @author yangsz@sunway.com 2022-10-26
 */
public interface LcdpDatabaseDao {
    List<LcdpTableFieldBean> selectFieldListByCondition(MapperParameter mapperParameter);

    List<LcdpTableIndexBean> selectIndexListByCondition(MapperParameter mapperParameter);

    List<Map<String, Object>> selectTableNameListByCondition(MapperParameter mapperParameter);

    List<LcdpTableBean> selectTableInfoByCondition(MapperParameter mapperParameter);

    List<Map<String, Object>> selectViewInfoListByCondition(MapperParameter mapperParameter);

    LcdpTableDTO selectTableInfoByTableName(String tableName);
    
    List<LcdpTableIndexDTO> selectIndexListByTableName(String tableName);

    List<LcdpTableFieldDTO> selectFieldListByTableName(String tableName);

    int countByTableName(String name);

    int countByViewName(String name);

    int countByIndexName(String name);

    void lockTableByTableName(String name);

    List<Map<String, Object>> selectTableOrViewNameListByCondition(MapperParameter mapperParameter);
}
