package com.sunwayworld.cloud.module.lcdp.resource.persistent.dao;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryDevParamDTO;
import com.sunwayworld.framework.support.base.dao.GenericDao;

public interface LcdpResourceHistoryDao extends GenericDao<LcdpResourceHistoryBean, Long> {
    List<String> selectDevResourceNameList(List<LcdpResourceHistoryDevParamDTO> devParamList);
    
    List<LcdpResourceHistoryBean> selectLatestActivatedListByResourceIdList(List<Long> resourceIdList);
    
    List<LcdpResourceHistoryBean> selectMaxVersionListByResourceIdList(List<Long> resourceIdList);
}
