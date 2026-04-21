package com.sunwayworld.cloud.module.lcdp.table.persistent.dao.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.item.admarea.bean.CoreAdmAreaBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldGroupBean;
import com.sunwayworld.cloud.module.lcdp.table.persistent.dao.LcdpTableFieldGroupDao;
import com.sunwayworld.cloud.module.lcdp.table.persistent.mapper.LcdpTableFieldGroupMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.PersistableHelper;

@Repository
@GikamBean
public class LcdpTableFieldGroupDaoImpl extends MybatisDaoSupport<LcdpTableFieldGroupBean, Long> implements LcdpTableFieldGroupDao {

    @Autowired
    private LcdpTableFieldGroupMapper lcdpTableFieldGroupMapper;

    @Override
    public LcdpTableFieldGroupMapper getMapper() {
        return lcdpTableFieldGroupMapper;
    }

	@Override
	public List<LcdpTableFieldGroupBean> selectTreeNodeList(MapperParameter parameter) {
        return getMapper().selectTreeNodeList(parameter).stream().map(e -> PersistableHelper.mapToPersistable(e, LcdpTableFieldGroupBean.class)).collect(Collectors.toList());
    }
}
