package com.sunwayworld.cloud.module.lcdp.databasemanager.persistent.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

import com.sunwayworld.framework.spring.annotation.GikamBean;

@GikamBean
public interface DatabaseManagerMapper {
    void update(@Param("sql") String sql);

    List<Map<String, Object>> selectList(@Param("sql") String sql);
}
