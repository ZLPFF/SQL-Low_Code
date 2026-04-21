package com.sunwayworld.cloud.module.lcdp.submitlog.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.resourceversion.bean.LcdpResourceVersionBean;
import com.sunwayworld.cloud.module.lcdp.resourceversion.service.LcdpResourceVersionService;
import com.sunwayworld.cloud.module.lcdp.submitlog.bean.LcdpSubmitLogBean;
import com.sunwayworld.cloud.module.lcdp.submitlog.persistent.dao.LcdpSubmitLogDao;
import com.sunwayworld.cloud.module.lcdp.submitlog.service.LcdpSubmitLogService;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.data.page.Pagination;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.mybatis.page.PageRowBounds;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.sys.code.bean.CoreCodeBean;
import com.sunwayworld.module.sys.code.service.CoreCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@GikamBean
public class LcdpSubmitLogServiceImpl implements LcdpSubmitLogService {

    @Autowired
    private LcdpSubmitLogDao lcdpSubmitLogDao;

    @Lazy
    @Autowired
    private LcdpResourceVersionService resourceVersionService;


    @Autowired
    private CoreCodeService codeService;


    @Override
    @SuppressWarnings("unchecked")
    public LcdpSubmitLogDao getDao() {
        return lcdpSubmitLogDao;
    }

    @Override
    @Transactional
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        LcdpSubmitLogBean lcdpSubmitLog = jsonWrapper.parseUnique(LcdpSubmitLogBean.class);
        lcdpSubmitLog.setId(ApplicationContextHelper.getNextIdentity());
        getDao().insert(lcdpSubmitLog);
        return lcdpSubmitLog.getId();
    }

    @Override
    public Page<LcdpResourceVersionBean> selectVersionPaginationByLogId(Long id, RestJsonWrapperBean wrapper) {
        Page<LcdpResourceVersionBean> resourceVersionPage = resourceVersionService.selectPaginationByFilter(SearchFilter.instance().match("LOGID", id).filter(MatchPattern.EQ), wrapper);

        if (!resourceVersionPage.getRows().isEmpty()) {

            List<CoreCodeBean> codeList = codeService.selectSelectableList("RESOURCE_CATEGORY");

            // 将codeList转换为code和codename对应的map，并处理国际化逻辑
            Map<String, String> codeMap = codeList.stream()
                    .collect(Collectors.toMap(
                            CoreCodeBean::getCode,
                            e -> {
                                // 如果i18nCode不为空，则取国际化值；如果国际化值为空则取codeName
                                if (!StringUtils.isEmpty(e.getI18nCode())) {
                                    String message = I18nHelper.getMessage(e.getI18nCode());

                                    if (!StringUtils.isEmpty(message)) {
                                        return message;
                                    }


                                }

                                return e.getCodeName();
                            },
                            (existing, replacement) -> existing // 处理key冲突的情况，保留原有的值
                    ));


            resourceVersionPage.getRows().forEach(r -> {
                r.setExt$Item("categoryi18n",codeMap.getOrDefault(r.getResourceCategory(), r.getResourceCategory()));
            });

        }


        return resourceVersionPage;
    }

    @Override
    public Page<LcdpResourceVersionBean> viewResource(RestJsonWrapperBean wrapper) {
        String logIdArrayStr = wrapper.getFilterValue("logId");
        if (StringUtils.isEmpty(logIdArrayStr)) {
            return null;
        }
        JSONArray array = JSONObject.parseArray(logIdArrayStr);
        List<String> LogIdStr = array.toJavaList(String.class);
        List<Long> logIdList = LogIdStr.stream().map(Long::valueOf).collect(Collectors.toList());

        MapperParameter parameter = wrapper.extractMapFilter();
        PageRowBounds rowBounds = wrapper.extractPageRowBounds();

        parameter.setRawQueries();
        parameter.setFilter(SearchFilter.instance().match("logId", logIdList).filter(MatchPattern.OR));

        if (!wrapper.getAuthorityList().isEmpty()) {
            wrapper.getAuthorityList().forEach(a -> parameter.setAuthorityParameter(a));
        }

        Page<LcdpResourceVersionBean> page = resourceVersionService.selectPagination(parameter, rowBounds);
        List<LcdpResourceVersionBean> resourceVersionList = page.getRows();

        //首先按 resourceCategory 升序排列，再按 resourceName 升序排列（空值排在最前）
        List<LcdpResourceVersionBean> sortResourceVersionSet = resourceVersionList.stream().sorted(Comparator.comparing(LcdpResourceVersionBean::getResourceCategory)
                .thenComparing(r -> r.getResourceName(), Comparator.nullsFirst(Comparator.naturalOrder()))).collect(Collectors.toList());
        page.setRows(sortResourceVersionSet);
        return page;
    }

    @Override
    public Page<LcdpSubmitLogBean> selectPagination(RestJsonWrapperBean wrapper) {
        MapperParameter parameter = wrapper.extractMapFilter();
        PageRowBounds rowBounds = wrapper.extractPageRowBounds();

        String resourceId = wrapper.getParamValue("resourceId");
        if (!StringUtils.isEmpty(resourceId)) {
            LcdpResourceVersionBean filter = new LcdpResourceVersionBean();
            filter.setResourceId(resourceId);

            List<Long> logIdList = resourceVersionService.getDao().selectColumnList(filter, "LOGID", Long.class);
            if (!logIdList.isEmpty()) {
                parameter.setFilter(SearchFilter.instance().match("ID", logIdList).filter(MatchPattern.OR));
            } else {
                return new Pagination<>();
            }
        }

        if (parameter.containsKey("resourceName")) {
            parameter.put("resourceName", StringUtils.upperCase((String) parameter.get("resourceName")));
        }

        return this.selectPagination(parameter, rowBounds);
    }
}


