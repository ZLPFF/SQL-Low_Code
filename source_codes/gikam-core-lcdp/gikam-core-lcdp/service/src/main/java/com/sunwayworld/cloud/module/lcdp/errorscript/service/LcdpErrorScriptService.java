package com.sunwayworld.cloud.module.lcdp.errorscript.service;

import java.util.List;

import com.sunwayworld.cloud.module.lcdp.errorscript.bean.LcdpErrorScriptBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpErrorScriptService extends GenericService<LcdpErrorScriptBean, Long> {
    void insertByResource(LcdpResourceBean resource, String errorLog);
    
    void insertByHistory(LcdpResourceHistoryBean history, String errorLog);
    
    void deleteByResourceId(Long resourceId, String scriptStatus);
    
    default void deleteByResourceId(Long resourceId) {
        deleteByResourceId(resourceId, null);
    }
    
    default void deleteByScriptIdList(List<Long> javaScriptIdList) {
        deleteByScriptIdList(javaScriptIdList, null);
    }
    
    void deleteByScriptIdList(List<Long> javaScriptIdList, String scriptStatus);
    
    /**
     * 源代码上面错误查询
     */
    Page<LcdpErrorScriptBean> selectWarningPagination(RestJsonWrapperBean wrapper);
    
    /**
     * 删除非正常的记录
     */
    void deleteAbnormalRecord();
    
    /**
     * 源代码编译错误数量
     */
    Long selectNumberOfWarnings();
}
