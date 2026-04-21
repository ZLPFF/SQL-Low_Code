package com.sunwayworld.cloud.module.lcdp.errorscript.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sunwayworld.cloud.module.lcdp.errorscript.bean.LcdpErrorScriptBean;
import com.sunwayworld.cloud.module.lcdp.errorscript.persistent.dao.LcdpErrorScriptDao;
import com.sunwayworld.cloud.module.lcdp.errorscript.service.LcdpErrorScriptService;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.mybatis.page.MybatisPageHelper;
import com.sunwayworld.framework.mybatis.page.PageRowBounds;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.StringUtils;

@Repository
@GikamBean
public class LcdpErrorScriptServiceImpl implements LcdpErrorScriptService {

    @Autowired
    private LcdpErrorScriptDao lcdpErrorScriptDao;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpErrorScriptDao getDao() {
        return lcdpErrorScriptDao;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertByResource(LcdpResourceBean resource, String errorLog) {
        deleteByResourceId(resource.getId()); // 删除该资源下的所有错误信息
        
        LcdpErrorScriptBean errorScript = new LcdpErrorScriptBean();
        errorScript.setId(ApplicationContextHelper.getNextIdentity());
        errorScript.setServerScriptId(resource.getId());
        errorScript.setScriptName(resource.getResourceName());
        errorScript.setScriptContent(resource.getContent());
        errorScript.setScriptPath(resource.getPath());
        errorScript.setErrorLog(errorLog);
        errorScript.setScriptStatus("submit");
        errorScript.setCreatedById(resource.getCreatedById());
        errorScript.setCreatedByName(resource.getCreatedByName());
        errorScript.setCreatedTime(LocalDateTime.now());
        getDao().insert(errorScript);
    
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertByHistory(LcdpResourceHistoryBean history, String errorLog) {
        deleteByResourceId(history.getResourceId(), "checkout"); // 删除该资源下的已检测的错误信息
        
        LcdpErrorScriptBean errorScript = new LcdpErrorScriptBean();
        errorScript.setId(ApplicationContextHelper.getNextIdentity());
        errorScript.setServerScriptId(history.getResourceId());
        errorScript.setScriptName(history.getResourceName());
        errorScript.setScriptContent(history.getContent());
        errorScript.setScriptPath(history.getPath());
        errorScript.setErrorLog(errorLog);
        errorScript.setScriptStatus("checkout");
        errorScript.setCreatedById(history.getCreatedById());
        errorScript.setCreatedByName(history.getCreatedByName());
        errorScript.setCreatedTime(LocalDateTime.now());
        getDao().insert(errorScript);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteByResourceId(Long resourceId, String scriptStatus) {
        LcdpErrorScriptBean filter = new LcdpErrorScriptBean();
        filter.setServerScriptId(resourceId);
        
        if (StringUtils.isBlank(scriptStatus)) {
            getDao().deleteBy(filter,"SERVERSCRIPTID");
        } else {
            filter.setScriptStatus(scriptStatus);
            
            getDao().deleteBy(filter,"SERVERSCRIPTID", "SCRIPTSTATUS");
        }
    }

    @Override
    @Transactional
    public void deleteByScriptIdList(List<Long> javaScriptIdList, String scriptStatus) {
        if(javaScriptIdList.isEmpty()){
            return;
        }
        
        SearchFilter searchFiliter = SearchFilter.instance().match("SERVERSCRIPTID",javaScriptIdList).filter(MatchPattern.OR);
        if (!StringUtils.isBlank(scriptStatus)) {
            searchFiliter.match("SCRIPTSTATUS", scriptStatus);
        }
        
        List<LcdpErrorScriptBean> errorScriptList = selectListByFilter(searchFiliter);
        if(errorScriptList.isEmpty()){
            return;
        }
        List<Long> deleteIdList = errorScriptList.stream().map(LcdpErrorScriptBean::getId).collect(Collectors.toList());
        getDao().deleteByIdList(deleteIdList);
    }

    @Override
    public Page<LcdpErrorScriptBean> selectWarningPagination(RestJsonWrapperBean wrapper) {
        MapperParameter parameter = wrapper.extractMapFilter();
        PageRowBounds rowBounds = wrapper.extractPageRowBounds();
        
        return selectPagination(() -> getDao().selectWarningListByCondition(parameter), rowBounds);
    }

    @Override
    @Transactional
    public void deleteAbnormalRecord() {
        List<Long> abnormalIdList = getDao().selectAbnormalIdList();
        
        getDao().deleteByIdList(abnormalIdList);
    }
    
    @Override
    public Long selectNumberOfWarnings() {
        try {
            MybatisPageHelper.setCountOnly();
            
            Page<LcdpErrorScriptBean> page = selectPagination(() -> getDao().selectWarningListByCondition(new MapperParameter()), null);
            
            return 0L + page.getNumberOfElements();
        } finally {
            MybatisPageHelper.clear();
        }
    }
}
