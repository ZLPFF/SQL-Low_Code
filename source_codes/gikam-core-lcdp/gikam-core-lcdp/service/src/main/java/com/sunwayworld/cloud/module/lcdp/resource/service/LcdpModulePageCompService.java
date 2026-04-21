package com.sunwayworld.cloud.module.lcdp.resource.service;

import java.util.List;
import java.util.Map;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModulePageCompBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpModulePageCompService extends GenericService<LcdpModulePageCompBean, String> {

    void dealGridSroll(List<LcdpResourceHistoryBean> chunkItemList);

    void dealLoadingMode(List<LcdpResourceHistoryBean> chunkItemList);

    void dealGridBadgeCount(List<LcdpResourceHistoryBean> chunkItemList);

    void dealGridShowCheckedNum(List<LcdpResourceHistoryBean> chunkItemList);

    void dealHistoryGridCheckContinuous(List<LcdpResourceHistoryBean> chunkItemList);

    void dealHistoryShowCheckedNum2Null(List<LcdpResourceHistoryBean> chunkItemList);

    List<LcdpModulePageCompBean> selectByModulePageId(Long modulePageId);
    
    List<LcdpModulePageCompBean> selectByModulePageHistoryId(Long modulePageHistoryId);
    
    /**
     * 复制历史资源对应的页面组件到新的历史资源中
     */
    void copy(Map<Long, Long> historyIdMapping);
}
