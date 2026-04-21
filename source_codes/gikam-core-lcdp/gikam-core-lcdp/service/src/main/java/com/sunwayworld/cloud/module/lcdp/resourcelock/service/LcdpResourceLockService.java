package com.sunwayworld.cloud.module.lcdp.resourcelock.service;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resourcelock.bean.LcdpResourceLockBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpResourceLockService extends GenericService<LcdpResourceLockBean, Long> {

    RestValidationResultBean validateResourceEditable(String resourceId);

    RestValidationResultBean validateResourceEditable(List<String> resourceIdList);

    void lock(String resourceId,String resourceCategory);

    void unLock(List<String> resourceIdList);

    void lockResource(List<LcdpResourceBean> resourceList);

    List<LcdpResourceLockBean> selectResourceLockListByCategory(String lockUserId, String resourceCategory);
    
    /**
     * 获取资源的锁定列表
     */
    List<LcdpResourceLockBean> selectResourceLockList(String resourceId, String resourceCategory);
    
    /**
     * 获取指定用户对于资源锁定的状态（0-没有锁定 1-指定用户锁定 2-其它人锁定）
     */
    String getLockStatus(String userId, String resourceId, String resourceCategory);
}
