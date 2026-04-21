package com.sunwayworld.cloud.module.lcdp.resource.service;

import java.util.List;
import java.util.Map;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpI18nDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModulePageI18nBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.service.GenericService;

public interface LcdpModulePageI18nService extends GenericService<LcdpModulePageI18nBean, Long> {

    Map<String, String> selectI18nMessageByCode(RestJsonWrapperBean jsonWrapper);

    Map<String, Map<String, String>> selectPageI18nMessage(Long resourceHistoryId);

    Page<Map<String, Object>> selectAllI18nMessage(RestJsonWrapperBean jsonWrapper);

    List<LcdpModulePageI18nBean> selectEffectivePageI18nMessage(MapperParameter parameter);

    void alterMessage(RestJsonWrapperBean wrapper);

    String export(RestJsonWrapperBean wrapper);

    void batchDealHistoryPageCompI18n(List<LcdpResourceHistoryBean> resourceHistoryList, Map<String, String> i18nMessageMap);

    void batchDealHistoryPageCompI18nForDuplicateId(List<LcdpResourceHistoryBean> chunkItemList);
    
    List<LcdpModulePageI18nBean> selectListByModulePageHistoryId(Long modulePageHistoryId);
    
    /**
     * 复制历史资源对应的页面国际化信息到新的历史资源中
     */
    void copy(Map<Long, Long> historyIdMapping);

    void batchDealHistoryPageCompI18nGridAndForm(List<LcdpResourceHistoryBean> chunkItemList, Map<String, String> i18nMessageMap,List<LcdpI18nDTO>i18nDTOList);

    void refreshI18nMessage(RestJsonWrapperBean jsonWrapper);
}
