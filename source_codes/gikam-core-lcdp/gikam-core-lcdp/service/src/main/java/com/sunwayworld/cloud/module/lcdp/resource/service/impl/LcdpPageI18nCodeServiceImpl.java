package com.sunwayworld.cloud.module.lcdp.resource.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpPageI18nCodeBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.LcdpPageI18nCodeDao;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpPageI18nCodeService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceHistoryService;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.support.PersistableHelper;
import com.sunwayworld.framework.utils.BeanUtils;

@Repository
@GikamBean
public class LcdpPageI18nCodeServiceImpl implements LcdpPageI18nCodeService {

    @Autowired
    private LcdpPageI18nCodeDao lcdpPageI18nCodeDao;
    @Lazy
    @Autowired
    private LcdpResourceHistoryService resourceHistoryService;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpPageI18nCodeDao getDao() {
        return lcdpPageI18nCodeDao;
    }

    @Override
    @Transactional
    public void copy(Map<Long, Long> historyIdMapping) {
        if (historyIdMapping.isEmpty()) {
            return;
        }
        
        List<Long> historyIdList = new ArrayList<>(historyIdMapping.keySet());
        List<LcdpPageI18nCodeBean> pageI18nCodeList = selectListByFilter(SearchFilter.instance().match("MODULEPAGEHISTORYID",
                historyIdList).filter(MatchPattern.OR));
        
        if (pageI18nCodeList.isEmpty()) {
            return;
        }
        
        List<Long> newHistoryIdList = new ArrayList<>(historyIdMapping.values());
        
        List<LcdpResourceHistoryBean> newHistoryList = resourceHistoryService.getDao().selectListByIds(newHistoryIdList, Arrays.asList("ID", "RESOURCEID", "VERSION"));
        
        List<LcdpPageI18nCodeBean> insertPageI18nCodeList = new ArrayList<>();
        for (LcdpPageI18nCodeBean pageI18nCode : pageI18nCodeList) {
            Long newId = historyIdMapping.get(pageI18nCode.getModulePageHistoryId());
            LcdpResourceHistoryBean newHistory = newHistoryList.stream().filter(h -> h.getId().equals(newId)).findAny().get();
            
            LcdpPageI18nCodeBean insertPageI18nCode = new LcdpPageI18nCodeBean();
            BeanUtils.copyProperties(pageI18nCode, insertPageI18nCode, PersistableHelper.ignoreProperties());
            insertPageI18nCode.setId(ApplicationContextHelper.getNextIdentity());
            insertPageI18nCode.setModulePageId(newHistory.getResourceId());
            insertPageI18nCode.setModulePageHistoryId(newHistory.getId());
            
            insertPageI18nCodeList.add(insertPageI18nCode);
        }
        
        getDao().fastInsert(insertPageI18nCodeList);
    }

}
