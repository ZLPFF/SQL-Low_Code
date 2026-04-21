package com.sunwayworld.cloud.boot.listener.service;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;

/**
 * 用于处理系统启动时的历史债务
 *
 * @author zhangjr@sunwayworld.com 2024年8月9日
 */
public interface LcdpHistoricalDebtService {
    void updateResourceIfNecessary(List<LcdpResourceBean> resourceList);

    void updateResourceHistoryIfNecessary(List<LcdpResourceHistoryBean> resourceHistoryList);
    
    void updateResourceModuleAndCategoryId();
}
