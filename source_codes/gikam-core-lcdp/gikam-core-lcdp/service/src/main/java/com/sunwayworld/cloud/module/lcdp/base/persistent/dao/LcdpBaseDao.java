package com.sunwayworld.cloud.module.lcdp.base.persistent.dao;

import com.sunwayworld.framework.mybatis.mapper.MapperParameter;

import java.util.List;
import java.util.Map;

public interface LcdpBaseDao {

    void insert(String tableName, Map<String, Object> map);

    void insert(String tableName, List<Map<String, Object>> list);

    List<Map<String, Object>> selectByCondition(MapperParameter parameter);

    void delete(String tableName, List<String> idList);

    void update(String tableName, Map<String, Object> map);

    void update(String tableNaem, List<Map<String, Object>> list, String... updateColumns);

    void updateIfChanged(String tableName, List<Map<String, Object>> list);
}
