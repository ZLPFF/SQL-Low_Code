package com.sunwayworld.cloud.module.lcdp.table.persistent.dao.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableIndexBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableIndexDTO;
import com.sunwayworld.cloud.module.lcdp.table.persistent.dao.LcdpDatabaseDao;
import com.sunwayworld.cloud.module.lcdp.table.persistent.mapper.LcdpDatabaseMapper;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.PersistableHelper;

/**
 * @author yangsz@sunway.com 2022-10-26
 */
@Repository
@GikamBean
public class LcdpDatabaseDaoImpl implements LcdpDatabaseDao {

    @Autowired
    private LcdpDatabaseMapper lcdpDatabaseMapper;

    @Override
    public List<LcdpTableFieldBean> selectFieldListByCondition(MapperParameter mapperParameter) {
        return lcdpDatabaseMapper.selectFieldListByCondition(mapperParameter)
                .stream().map(m -> PersistableHelper.mapToPersistable(m, LcdpTableFieldBean.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<LcdpTableIndexBean> selectIndexListByCondition(MapperParameter mapperParameter) {
        return lcdpDatabaseMapper.selectIndexListByCondition(mapperParameter)
                .stream().map(m -> PersistableHelper.mapToPersistable(m, LcdpTableIndexBean.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> selectTableNameListByCondition(MapperParameter mapperParameter) {
        return lcdpDatabaseMapper.selectTableNameListByCondition(mapperParameter);
    }

    @Override
    public List<LcdpTableBean> selectTableInfoByCondition(MapperParameter mapperParameter) {
        return lcdpDatabaseMapper.selectTableInfoByCondition(mapperParameter)
                .stream().map(m -> PersistableHelper.mapToPersistable(m, LcdpTableBean.class))
                .collect(Collectors.toList());
    }

    @Override
    public LcdpTableDTO selectTableInfoByTableName(String tableName) {
        Map<String, Object> map = lcdpDatabaseMapper.selectTableInfoByTableName(tableName);
        
        if (map == null) {
            return null;
        }
        
        return PersistableHelper.mapToPersistable(map, LcdpTableDTO.class);
    }

    @Override
    public List<LcdpTableIndexDTO> selectIndexListByTableName(String tableName) {
        return lcdpDatabaseMapper.selectIndexListByTableName(tableName)
                .stream().map(m -> PersistableHelper.mapToPersistable(m, LcdpTableIndexDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<LcdpTableFieldDTO> selectFieldListByTableName(String tableName) {
        return lcdpDatabaseMapper.selectFieldListByTableName(tableName)
                .stream().map(m ->
                {
                    LcdpTableFieldDTO lcdpTableFieldDTO = PersistableHelper.mapToPersistable(m, LcdpTableFieldDTO.class);
                    if (lcdpTableFieldDTO.getDefaultValue().indexOf("::") > 0) {
                        lcdpTableFieldDTO.setDefaultValue(lcdpTableFieldDTO.getDefaultValue().split("::")[0]);
                    }else {
                        lcdpTableFieldDTO.setDefaultValue(lcdpTableFieldDTO.getDefaultValue());
                    }
                    return lcdpTableFieldDTO;
                })
                .collect(Collectors.toList());
    }
    @Override
    public List<Map<String, Object>> selectViewInfoListByCondition(MapperParameter mapperParameter) {
        return lcdpDatabaseMapper.selectViewInfoListByCondition(mapperParameter);
    }

    @Override
    public int countByTableName(String name) {
        return lcdpDatabaseMapper.countByTableName(name);
    }

    @Override
    public int countByViewName(String name) {
        return lcdpDatabaseMapper.countByViewName(name);
    }

    @Override
    public int countByIndexName(String name) {
        return lcdpDatabaseMapper.countByIndexName(name);
    }

    @Override
    public void lockTableByTableName(String name) {
        lcdpDatabaseMapper.lockTableByTableName(name);
    }


    @Override
    public List<Map<String, Object>> selectTableOrViewNameListByCondition(MapperParameter mapperParameter) {
        return lcdpDatabaseMapper.selectTableOrViewNameListByCondition(mapperParameter);
    }
}
