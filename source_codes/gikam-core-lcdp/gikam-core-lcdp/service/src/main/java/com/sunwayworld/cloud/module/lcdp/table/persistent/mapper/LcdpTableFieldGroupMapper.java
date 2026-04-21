package com.sunwayworld.cloud.module.lcdp.table.persistent.mapper;

import java.util.List;
import java.util.Map;

import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.base.mapper.GenericMapper;

@GikamBean
public interface LcdpTableFieldGroupMapper extends GenericMapper<Long> {
	List<Map<String, Object>> selectTreeNodeList(MapperParameter parameter);
}
