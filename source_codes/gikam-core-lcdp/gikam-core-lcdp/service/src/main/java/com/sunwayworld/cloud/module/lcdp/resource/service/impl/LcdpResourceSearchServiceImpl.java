package com.sunwayworld.cloud.module.lcdp.resource.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceSearchDTO;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpModulePageCompService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceHistoryService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceSearchService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpScriptUtils;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.data.page.Pagination;
import com.sunwayworld.framework.database.dialect.Dialect;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.ClassUtils;
import com.sunwayworld.framework.utils.NumberUtils;
import com.sunwayworld.framework.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@GikamBean
@Repository
public class LcdpResourceSearchServiceImpl implements LcdpResourceSearchService {
    @Autowired
    private LcdpResourceService resourceService;
    @Autowired
    private LcdpResourceHistoryService resourceHistoryService;
    @Autowired
    private LcdpModulePageCompService pageCompService;

    private static final long MAX_RETURN_SIZE = 200;

    private String database = ClassUtils.getPredicatedClasses("com.sunwayworld", c -> !c.isInterface() && Dialect.class.isAssignableFrom(c))
            .stream()
            .map(c -> ((Dialect) ClassUtils.newInstance(c)).getDatabase())
            .filter(d -> ApplicationContextHelper.isProfileActivated(d))
            .findAny()
            .get();

    @Override
    public Page<LcdpResourceSearchDTO> selectPagination(RestJsonWrapperBean wrapper) {
        String resourceCategory = wrapper.getParamValue("resourceCategory"); // 查询类型
        String searchKey = wrapper.getParamValue("searchKey"); // 查询关键字
        String matchCase = wrapper.getParamValue("matchCase"); // 是否匹配大小写

        List<LcdpResourceSearchDTO> searchList = new ArrayList<>();

        if (StringUtils.isBlank(resourceCategory)
                || LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(resourceCategory)) {
            searchList.addAll(selectJava(searchKey, matchCase));
        }

        if (searchList.size() > MAX_RETURN_SIZE) {
            return new Pagination<>();
        }

        if (StringUtils.isBlank(resourceCategory)
                || LcdpConstant.RESOURCE_CATEGORY_JS.equals(resourceCategory)) {
            searchList.addAll(selectJavascript(searchKey, matchCase));
        }

        if (searchList.size() > MAX_RETURN_SIZE) {
            return new Pagination<>();
        }

        if (StringUtils.isBlank(resourceCategory)
                || LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(resourceCategory)) {
            searchList.addAll(selectMapper(searchKey, matchCase));
        }

        if (searchList.size() > MAX_RETURN_SIZE) {
            return new Pagination<>();
        }

        if (StringUtils.isBlank(resourceCategory)
                || LcdpConstant.RESOURCE_CATEGORY_COMP.equals(resourceCategory)) {
            searchList.addAll(selectComp(searchKey, matchCase));
        }

        if (searchList.size() > MAX_RETURN_SIZE) {
            return new Pagination<>();
        }

        searchList.forEach(s -> s.removeExt$());
        searchList.sort((s1, s2) -> s1.getPath().toLowerCase().compareTo(s2.getPath().toLowerCase()));

        return new Pagination<>(searchList);
    }

    @Override
    public String selectContent(String id) {
        if (NumberUtils.isNumber(id)) {
            return resourceHistoryService.selectColumnById(NumberUtils.parseLong(id), "CONTENT", String.class);
        }

        return pageCompService.selectColumnById(id, "CONFIG", String.class);
    }

    //--------------------------------------------------------------------------
    // 私有方法
    //--------------------------------------------------------------------------
    private List<LcdpResourceSearchDTO> selectJava(String searchKey, String matchCase) {
        return resourceService.selectJavaByKeyword(LocalContextHelper.getLoginUserId(), searchKey, matchCase);
    }

    private List<LcdpResourceSearchDTO> selectJavascript(String searchKey, String matchCase) {
        return resourceService.selectJavascriptByKeyword(LocalContextHelper.getLoginUserId(), searchKey, matchCase);
    }

    private List<LcdpResourceSearchDTO> selectMapper(String searchKey, String matchCase) {
        String mapperSuffix = null;
        if (LcdpScriptUtils.requiredMapperOnly()) {
            mapperSuffix = StringUtils.capitalize(database) + "Mapper";
        }

        return resourceService.selectMapperByKeyword(LocalContextHelper.getLoginUserId(), mapperSuffix, searchKey, matchCase);
    }

    private List<LcdpResourceSearchDTO> selectComp(String searchKey, String matchCase) {
        List<LcdpResourceSearchDTO> searchList = resourceService.selectCompByKeyword(LocalContextHelper.getLoginUserId(), searchKey, matchCase);

        searchList.forEach(s -> {
            String content = s.getExt$Item("content");


            if (StringUtils.isNotEmpty(content)) {
                try {
                    if (JSONValidator.from(content).validate()) {
                        JSONObject jo = JSON.parseObject(content);
                        s.setPath(s.getPath() + "." + jo.getString("id"));
                    } else {
                        s.setPath(s.getPath());
                    }
                } catch (Exception e) {
                    // 当JSON验证或解析失败时，保持原有路径
                    s.setPath(s.getPath());
                }
            } else {
                s.setPath(s.getPath());
            }

        });

        return searchList;
    }
}
