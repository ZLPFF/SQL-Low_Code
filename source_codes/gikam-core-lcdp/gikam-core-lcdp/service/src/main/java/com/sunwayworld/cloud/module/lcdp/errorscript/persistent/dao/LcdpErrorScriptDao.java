package com.sunwayworld.cloud.module.lcdp.errorscript.persistent.dao;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.errorscript.bean.LcdpErrorScriptBean;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.support.base.dao.GenericDao;

public interface LcdpErrorScriptDao extends GenericDao<LcdpErrorScriptBean, Long> {
    List<LcdpErrorScriptBean> selectWarningListByCondition(MapperParameter parameter);
    
    List<Long> selectAbnormalIdList();
}
