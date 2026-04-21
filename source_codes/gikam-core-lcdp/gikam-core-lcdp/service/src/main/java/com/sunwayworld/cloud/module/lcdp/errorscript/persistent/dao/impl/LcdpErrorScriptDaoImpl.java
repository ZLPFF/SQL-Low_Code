package com.sunwayworld.cloud.module.lcdp.errorscript.persistent.dao.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sunwayworld.cloud.module.lcdp.errorscript.bean.LcdpErrorScriptBean;
import com.sunwayworld.cloud.module.lcdp.errorscript.persistent.dao.LcdpErrorScriptDao;
import com.sunwayworld.cloud.module.lcdp.errorscript.persistent.mapper.LcdpErrorScriptMapper;
import com.sunwayworld.framework.mybatis.dao.MybatisDaoSupport;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.PersistableHelper;

@Repository
@GikamBean
public class LcdpErrorScriptDaoImpl extends MybatisDaoSupport<LcdpErrorScriptBean, Long> implements LcdpErrorScriptDao {

    @Autowired
    private LcdpErrorScriptMapper lcdpErrorScriptMapper;

    @Override
    public LcdpErrorScriptMapper getMapper() {
        return lcdpErrorScriptMapper;
    }

    @Override
    public List<LcdpErrorScriptBean> selectWarningListByCondition(MapperParameter parameter) {
        return getMapper().selectWarningListByCondition(parameter).stream()
                .map(m -> PersistableHelper.mapToPersistable(m, LcdpErrorScriptBean.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> selectAbnormalIdList() {
        return getMapper().selectAbnormalIdList();
    }
}
