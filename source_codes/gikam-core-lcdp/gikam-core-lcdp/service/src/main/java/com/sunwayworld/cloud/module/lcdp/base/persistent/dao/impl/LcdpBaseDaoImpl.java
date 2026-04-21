package com.sunwayworld.cloud.module.lcdp.base.persistent.dao.impl;

import com.sunwayworld.cloud.module.lcdp.base.persistent.dao.LcdpBaseDao;
import com.sunwayworld.cloud.module.lcdp.base.persistent.mapper.LcdpBaseMapper;
import com.sunwayworld.framework.database.context.TableContext;
import com.sunwayworld.framework.mybatis.MybatisHelper;
import com.sunwayworld.framework.mybatis.interceptor.MybatisColumnTypeHolder;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.dao.MapDao;
import com.sunwayworld.framework.utils.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@GikamBean
public class LcdpBaseDaoImpl implements LcdpBaseDao {
    @Autowired
    private LcdpBaseMapper baseMapper;


    @Autowired
    @Lazy
    private MapDao mapDao;

    @Override
    public List<Map<String, Object>> selectByCondition(MapperParameter parameter) {
        List<Map<String, Object>> mapList = baseMapper.selectByCondition(parameter);
        
        if (!CollectionUtils.isEmpty(mapList)) {
            MapDao.correctMap(mapList);
            
            Map<String, Class<?>> columnTypeMap = MybatisColumnTypeHolder.popColumnTypeMap();
            
            if (!CollectionUtils.isEmpty(columnTypeMap)) {
                mapList.forEach(m -> MybatisHelper.correctValue(columnTypeMap, m));
            }
            MybatisColumnTypeHolder.setColumnTypeMap(columnTypeMap);
        }
        
        return mapList;
    }


    @Override
    public void insert(String tableName, Map<String, Object> map) {
        mapDao.insert(TableContext.of(tableName), map);
    }

    @Override
    public void insert(String tableName, List<Map<String, Object>> list) {
        mapDao.insert(TableContext.of(tableName), list);
    }

    @Override
    public void delete(String tableName, List<String> idList) {
        mapDao.deleteByIdList(TableContext.of(tableName), idList);
    }

    @Override
    public void update(String tableName, Map<String, Object> map) {
        mapDao.update(TableContext.of(tableName), map);
    }

    @Override
    public void update(String tableName, List<Map<String, Object>> list, String... updateColumns) {
        mapDao.update(TableContext.of(tableName), list, updateColumns);
    }

    @Override
    public void updateIfChanged(String tableName, List<Map<String, Object>> list) {
        mapDao.updateIfChanged(TableContext.of(tableName), list);
    }

}
