package com.sunwayworld.cloud.module.lcdp.resource.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpModulePageCompBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpViewButtonRoleBean;
import com.sunwayworld.cloud.module.lcdp.resource.persistent.dao.LcdpViewButtonRoleDao;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpModulePageI18nService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceHistoryService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpViewButtonRoleService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.data.page.Pagination;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.ServletUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.sys.menu.bean.CoreMenuBean;
import com.sunwayworld.module.sys.menu.service.CoreMenuService;

@Repository
@GikamBean
public class LcdpViewButtonRoleServiceImpl implements LcdpViewButtonRoleService {

    @Autowired
    private LcdpViewButtonRoleDao lcdpViewButtonRoleDao;

    @Autowired
    private LcdpModulePageCompServiceImpl modulePageCompService;

    @Autowired
    private LcdpResourceHistoryService resourceHistoryService;

    @Autowired
    private CoreMenuService menuService;

    @Autowired
    @Lazy
    private LcdpResourceService resourceService;

    @Autowired
    private LcdpModulePageI18nService i18nService;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpViewButtonRoleDao getDao() {
        return lcdpViewButtonRoleDao;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        List<LcdpViewButtonRoleBean> buttonRoleList = jsonWrapper.parse(LcdpViewButtonRoleBean.class);

        int qty = buttonRoleList.size();
        List<LcdpResourceHistoryBean> filter = buttonRoleList.stream().map(e -> {
            LcdpResourceHistoryBean resourceHistoryBean = new LcdpResourceHistoryBean();
            resourceHistoryBean.setResourceId(e.getResourceId());
            resourceHistoryBean.setVersion(e.getEffectVersion());
            return resourceHistoryBean;
        }).collect(Collectors.toList());


        List<Long> idList = ApplicationContextHelper.getNextIdentityList(qty);
        for (int i = 0; i < qty; i++) {
            LcdpViewButtonRoleBean buttonRole = buttonRoleList.get(i);
            buttonRole.setId(idList.get(i));
        }
        getDao().insert(buttonRoleList);


        return buttonRoleList.get(0).getId();
    }


    @Override
    public Page<LcdpViewButtonRoleBean> selectPaginationByViewInfo(RestJsonWrapperBean wrapper) {


        Object str = wrapper.extractMapFilter().get("resourceId");
        if (str == null) {
            return new Pagination<>();
        }
        Long resourceId = Long.valueOf(wrapper.extractMapFilter().get("resourceId").toString());

        LcdpResourceBean resourceBean = resourceService.selectById(resourceId);
        String resourceName = wrapper.getParamValue("resourceName");
        LcdpResourceHistoryBean resourceHistory = resourceHistoryService.selectFirstByFilter(SearchFilter.instance().match("resourceId", resourceId).filter(MatchPattern.EQ).match("version", resourceBean.getEffectVersion()).filter(MatchPattern.EQ));

        List<LcdpModulePageCompBean> modulePageCompList = modulePageCompService.selectByModulePageHistoryId(resourceHistory.getId());
        List<LcdpModulePageCompBean> tabPanelList = modulePageCompList.stream().filter(e -> StringUtils.equals("TabPanel", e.getType())).collect(Collectors.toList());
        Map<String, LcdpModulePageCompBean> tablePanleId2TablePanelMap
                = tabPanelList.stream().collect(Collectors.toMap(LcdpModulePageCompBean::getId, Function.identity()));
        List<LcdpModulePageCompBean> buttonList = modulePageCompList.stream().filter(e -> StringUtils.equals("Button", e.getType())).collect(Collectors.toList());
        Map<String, String> id2ParentIdMap = modulePageCompList.stream().map(page -> {
            if (page.getParentId() == null) {
                page.setParentId("root");
            }
            return page;
        }).collect(Collectors.toMap(LcdpModulePageCompBean::getId, LcdpModulePageCompBean::getParentId));
        List<LcdpViewButtonRoleBean> roleList = new ArrayList<>();

        Map<String, Map<String, String>> i18nMessageMap = i18nService.selectPageI18nMessage(resourceHistory.getId());
        Map<String, String> i18nMap = i18nMessageMap.get(I18nHelper.getLocal());
        buttonList.forEach(button -> {
            if (resourceBean.getEffectVersion() == null) {
                return;
            }
            LcdpViewButtonRoleBean role = new LcdpViewButtonRoleBean();
            role.setId(ApplicationContextHelper.getNextIdentity());
            role.setButtonDataId(button.getId());
            role.setResourceId(resourceId);
            role.setResourceName(resourceName);
            role.setResourceHistoryId(resourceHistory.getId());
            role.setEffectVersion(resourceBean.getEffectVersion());
            JSONObject configObject = JSON.parseObject(button.getConfig());
            JSONObject title = configObject.getJSONObject("text");
            String buttonName = I18nHelper.getMessage(title.getString("i18nCode"));
            if (StringUtils.equals(buttonName, title.getString("i18nCode"))) {
                buttonName = i18nMessageMap == null ? title.getString("i18nCode") : i18nMessageMap.get(title.getString("i18nCode")).get(I18nHelper.getLocal().toLanguageTag());
            }
            role.setButtonName(buttonName);
            JSONArray visibleRoles = configObject.getJSONArray("visibleRoles");
            if (!ObjectUtils.isEmpty(visibleRoles)) {
                role.setVisibleRoles(String.join(",", visibleRoles.stream().map(String::valueOf).collect(Collectors.toList())));
            }
            findTabPanel(role, modulePageCompList, tablePanleId2TablePanelMap, id2ParentIdMap, button, i18nMessageMap);
            roleList.add(role);
        });
        HttpServletResponse response = ServletUtils.getCurrentResponse();
        if (response != null) {
            response.setHeader("GIKAM-INSTANT-SAVE-PATH", "/secure/core/module/lcdp/view-button-roles/instant");
        }

        List<LcdpViewButtonRoleBean> sortList = roleList.stream()
                .sorted(Comparator.comparing(LcdpViewButtonRoleBean::getButtonParentName,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        return new Pagination<>(sortList);

    }

    @Override
    public Page<LcdpViewButtonRoleBean> selectDistinctRawPagination(RestJsonWrapperBean wrapper) {
//        Page<LcdpViewButtonRoleBean> roleBeanPage = LcdpViewButtonRoleService.super.selectPagination(wrapper);
//        List<LcdpViewButtonRoleBean> rows = roleBeanPage.getRows();
//        rows.stream().distinct()
        return null;
    }

    @Override
    public Page<LcdpViewButtonRoleBean> selectRawPagination(RestJsonWrapperBean wrapper) {
        HttpServletResponse response = ServletUtils.getCurrentResponse();
        if (response != null) {
            response.setHeader("GIKAM-INSTANT-SAVE-PATH", "/secure/core/module/lcdp/view-button-roles/instant");
        }
        Page<LcdpViewButtonRoleBean> lcdpViewButtonRoleBeanPage = LcdpViewButtonRoleService.super.selectRawPagination(wrapper);
        List<LcdpViewButtonRoleBean> rows = lcdpViewButtonRoleBeanPage.getRows();

        ArrayList<LcdpViewButtonRoleBean> distinctRows = rows.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(LcdpViewButtonRoleBean::getResourceId))), ArrayList::new));
        Page<LcdpViewButtonRoleBean> page = new Pagination<>(distinctRows);


        return page;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.SAVE)
    public void instantSave(RestJsonWrapperBean wrapper) {
        LcdpViewButtonRoleBean viewButtonRole = wrapper.parseUnique(LcdpViewButtonRoleBean.class);


        String buttonDataId = viewButtonRole.getButtonDataId();
        LcdpModulePageCompBean button = modulePageCompService.selectById(buttonDataId);
        JSONObject configObject = JSON.parseObject(button.getConfig());
        JSONObject title = configObject.getJSONObject("text");
        JSONArray visibleRoles = new JSONArray();

        if (!StringUtils.isEmpty(viewButtonRole.getVisibleRoles())) {
            String[] visiableRoleArray = viewButtonRole.getVisibleRoles().split(",");
            for (String role : visiableRoleArray) {
                visibleRoles.add(Long.valueOf(role));
            }
        }
        configObject.put("visibleRoles", visibleRoles);
        button.setConfig(configObject.toJSONString());
        modulePageCompService.getDao().update(button, "config");
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public void initMenuPageData() {
        List<CoreMenuBean> menuList = menuService.selectListByFilter(SearchFilter.instance().match("resourceId", null).filter(MatchPattern.DIFFER));
        Map<Long, List<CoreMenuBean>> resourceId2MenuIdMap = menuList.stream().collect(Collectors.groupingBy(CoreMenuBean::getResourceId));
        List<Long> resourceIdList = menuList.stream().map(CoreMenuBean::getResourceId).collect(Collectors.toList());
        List<LcdpResourceBean> resourceList = resourceService.selectListByIds(resourceIdList);

        List<LcdpViewButtonRoleBean> buttonRoleList = new ArrayList<>();

        for (LcdpResourceBean menuResource : resourceList) {
            List<LcdpResourceBean> pageList = new ArrayList<>();
            List<CoreMenuBean> menuDataList = resourceId2MenuIdMap.get(menuResource.getId());
            menuDataList.forEach(menu -> {
                LcdpViewButtonRoleBean view = new LcdpViewButtonRoleBean();
                view.setId(ApplicationContextHelper.getNextIdentity());
                view.setMenuId(menu.getId());
                view.setResourceId(menuResource.getId());
                view.setResourceName(menuResource.getResourceName());
                view.setResourceDesc(menuResource.getResourceDesc());
                view.setPath(menuResource.getPath());
                buttonRoleList.add(view);
                pageList.add(menuResource);
                getPageResourceList(menuResource, pageList, menu.getId(), buttonRoleList);

            });
        }
//        Map<String, List<LcdpViewButtonRoleBean>> menuId2ButtonRoleListMap = buttonRoleList.stream().collect(Collectors.groupingBy(LcdpViewButtonRoleBean::getMenuId));
//        List<LcdpViewButtonRoleBean> dealList = new ArrayList<>();
//        menuId2ButtonRoleListMap.forEach((menuId, roleList) -> {
//            Map<Long, List<LcdpViewButtonRoleBean>> resourceId2RoleListMap = roleList.stream().collect(Collectors.groupingBy(LcdpViewButtonRoleBean::getResourceId));
//            resourceId2RoleListMap.forEach((resourceId, roleList2) -> {
//                if (roleList2.size() > 1) {
//                    List<String> parentIdList = roleList2.stream().map(LcdpViewButtonRoleBean::getParentId).collect(Collectors.toList());
//                    String parentIdStr = String.join(",", parentIdList);
//                    if (StringUtils.isNotEmpty(parentIdStr)) {
//                        roleList2.get(0).setParentId(parentIdStr);
//                    }
//                    dealList.add(roleList2.get(0));
//                }
//                buttonRoleList.removeIf(r -> r.getResourceId() == resourceId);
//            });
//        });
//        buttonRoleList.addAll(dealList);
        getDao().insert(buttonRoleList);
    }

    @Override
    @Transactional
    @Audit(AuditConstant.SAVE)
    public void updatePageInfo(List<LcdpResourceBean> resourceList) {
        List<LcdpViewButtonRoleBean> viewButtonRoleList = new ArrayList<>();
        List<Long> resourceIdList = resourceList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());


        List<LcdpViewButtonRoleBean> exitButtonRoleList = selectListByFilter(SearchFilter.instance().match("RESOURCEID", resourceIdList).filter(MatchPattern.OR));
        List<LcdpViewButtonRoleBean> exitChildButtonRoleList = selectListByFilter(SearchFilter.instance().match("PARENTID", resourceIdList).filter(MatchPattern.OR));
        Map<Long, List<LcdpViewButtonRoleBean>> resourceId2RoleListMap = exitChildButtonRoleList.stream().collect(Collectors.groupingBy(LcdpViewButtonRoleBean::getParentId));
        List<LcdpResourceBean> pageList = new ArrayList<>();
        List<LcdpViewButtonRoleBean> buttonRoleList = new ArrayList<>();
        List<LcdpViewButtonRoleBean> deleteList = new ArrayList<>();
        List<LcdpViewButtonRoleBean> insertList = new ArrayList<>();
        exitButtonRoleList.forEach(buttonRole -> {
            LcdpResourceBean menuResource = resourceList.stream().filter(r -> r.getId().equals(buttonRole.getResourceId())).findFirst().get();
            pageList.add(menuResource);
            getPageResourceList(menuResource, pageList, buttonRole.getMenuId(), buttonRoleList);
            List<LcdpViewButtonRoleBean> roleList = resourceId2RoleListMap.get(buttonRole.getResourceId());
            if (roleList != null) {
                Set<Long> roleResourceIds = roleList.stream()
                        .map(LcdpViewButtonRoleBean::getResourceId)
                        .collect(Collectors.toSet());

                Set<Long> buttonRoleResourceIds = buttonRoleList.stream()
                        .map(LcdpViewButtonRoleBean::getResourceId)
                        .collect(Collectors.toSet());

                Set<Long> toAdd = buttonRoleResourceIds.stream()
                        .filter(id -> !roleResourceIds.contains(id))
                        .collect(Collectors.toSet());

                Set<Long> toRemove = roleResourceIds.stream()
                        .filter(id -> !buttonRoleResourceIds.contains(id))
                        .collect(Collectors.toSet());

                List<LcdpViewButtonRoleBean> toAddBeans = buttonRoleList.stream()
                        .filter(bean -> toAdd.contains(bean.getResourceId()))
                        .collect(Collectors.toList());

                List<LcdpViewButtonRoleBean> toRemoveBeans = roleList.stream()
                        .filter(bean -> toRemove.contains(bean.getResourceId()))
                        .collect(Collectors.toList());

                if (!toAddBeans.isEmpty()) {
                    insertList.addAll(toAddBeans);
                }

                if (!toRemoveBeans.isEmpty()) {
                    deleteList.addAll(toRemoveBeans);
                }
            }
        });

        getDao().deleteBy(deleteList);

        List<LcdpViewButtonRoleBean> distinctList = insertList.stream().distinct().collect(Collectors.toList());

        distinctList.forEach(bean -> {
            bean.setId(ApplicationContextHelper.getNextIdentity());
        });
        getDao().insert(distinctList);


    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public void insertViewInfo(CoreMenuBean coreMenu) {
        LcdpResourceBean resource = resourceService.selectById(coreMenu.getResourceId());
        List<LcdpResourceBean> pageList = new ArrayList<>();
        List<LcdpViewButtonRoleBean> buttonRoleList = new ArrayList<>();
        LcdpViewButtonRoleBean view = new LcdpViewButtonRoleBean();
        view.setId(ApplicationContextHelper.getNextIdentity());
        view.setMenuId(coreMenu.getId());
        view.setResourceId(resource.getId());
        view.setResourceName(resource.getResourceName());

        view.setResourceDesc(resource.getResourceDesc());
        view.setPath(resource.getPath());

        buttonRoleList.add(view);
        pageList.add(resource);
        getPageResourceList(resource, pageList, coreMenu.getId(), buttonRoleList);

//        Map<String, List<LcdpViewButtonRoleBean>> menuId2ButtonRoleListMap = buttonRoleList.stream().collect(Collectors.groupingBy(LcdpViewButtonRoleBean::getMenuId));
//        List<LcdpViewButtonRoleBean> dealList = new ArrayList<>();
//        menuId2ButtonRoleListMap.forEach((menuId, roleList) -> {
//            Map<Long, List<LcdpViewButtonRoleBean>> resourceId2RoleListMap = roleList.stream().collect(Collectors.groupingBy(LcdpViewButtonRoleBean::getResourceId));
//            resourceId2RoleListMap.forEach((resourceId, roleList2) -> {
//                if (roleList2.size() > 1) {
//                    List<String> parentIdList = roleList2.stream().map(LcdpViewButtonRoleBean::getParentId).collect(Collectors.toList());
//                    String parentIdStr = String.join(",", parentIdList);
//                    if (StringUtils.isNotEmpty(parentIdStr)) {
//                        roleList2.get(0).setParentId(parentIdStr);
//                    }
//                    dealList.add(roleList2.get(0));
//                }
//                buttonRoleList.removeIf(r -> r.getResourceId() == resourceId);
//            });
//        });
//        buttonRoleList.addAll(dealList);
        getDao().insert(buttonRoleList);


    }

    //-------------------------------------------------------------------------------------------------------------
    // 私有方法
    //-------------------------------------------------------------------------------------------------------------
    private final static Pattern pageResourcePathPattern = Pattern.compile("(\\w+\\.\\w+\\.page\\.\\w+)");

    /**
     * 获取pageResource所有的引用page页面
     *
     * @param pageResource
     * @return
     */
    private void getPageResourceList(LcdpResourceBean pageResource, List<LcdpResourceBean> list, String menuId, List<LcdpViewButtonRoleBean> buttonRoleList) {
        String content = pageResource.getContent();
        if (StringUtils.isEmpty(content)) {
            return;
        }
        Matcher matcher = pageResourcePathPattern.matcher(content);
        while (matcher.find()) {
            String path = matcher.group();
            LcdpResourceBean resource = resourceService.getByPath(path);
            if (resource == null || LcdpConstant.RESOURCE_DELETED_YES.equals(resource.getDeleteFlag())) {
                continue;
            }


            Long effectVersion = resource.getEffectVersion();
            if (effectVersion == null) {
                effectVersion = 1L;
            }
            LcdpResourceHistoryBean history = resourceHistoryService.selectFirstByFilter(SearchFilter.instance().match("resourceId", resource.getId()).filter(MatchPattern.SEQ).match("version", effectVersion).filter(MatchPattern.SEQ));
            List<LcdpModulePageCompBean> modulePageCompList = modulePageCompService.selectByModulePageHistoryId(history.getId());
            List<LcdpModulePageCompBean> chooseList = modulePageCompList.stream().filter(e -> StringUtils.equalsIgnoreCase(e.getType(), "Choose")).collect(Collectors.toList());
            List<LcdpModulePageCompBean> gridChooseList = modulePageCompList.stream().filter(e -> StringUtils.equalsIgnoreCase(e.getType(), "GridColumn")
                    && StringUtils.equalsIgnoreCase(JSON.parseObject(e.getConfig()).getString("type"), "choose")).collect(Collectors.toList());
            List<String> pathList = new ArrayList<>();

            if (!chooseList.isEmpty()) {
                chooseList.forEach(choose -> {
                    String config = choose.getConfig();
                    JSONObject configObject = JSON.parseObject(config);
                    if (StringUtils.isNotEmpty(configObject.getString("url"))) {
                        pathList.add(configObject.getString("url"));
                    }
                });
            }
            if (!gridChooseList.isEmpty()) {
                gridChooseList.forEach(choose -> {
                    String config = choose.getConfig();
                    JSONObject configObject = JSON.parseObject(config);
                    if (StringUtils.isNotEmpty(configObject.getString("url"))) {
                        pathList.add(configObject.getString("url"));
                    }
                });
            }
            if (!pathList.isEmpty()) {
                for (String choosePath : pathList) {
                    LcdpResourceBean chooseResouce = resourceService.getByPath(choosePath);
                    if (chooseResouce == null) {
                        continue;
                    }
                    if (list.stream().noneMatch(e -> ObjectUtils.equals(e.getId(), chooseResouce.getId()))) {
                        list.add(chooseResouce);
                        LcdpViewButtonRoleBean chooseView = new LcdpViewButtonRoleBean();
                        chooseView.setId(ApplicationContextHelper.getNextIdentity());
                        chooseView.setMenuId(menuId);
                        chooseView.setResourceId(chooseResouce.getId());
                        chooseView.setResourceName(chooseResouce.getResourceName());
                        chooseView.setPath(chooseResouce.getPath());
                        chooseView.setResourceDesc(chooseResouce.getResourceDesc());
                        chooseView.setParentId(pageResource.getId());
                        buttonRoleList.add(chooseView);
                        getPageResourceList(chooseResouce, list, menuId, buttonRoleList);
                    }

                }
            }


            if (list.stream().noneMatch(e -> ObjectUtils.equals(e.getId(), resource.getId()))) {
                list.add(resource);
                LcdpViewButtonRoleBean view = new LcdpViewButtonRoleBean();
                view.setId(ApplicationContextHelper.getNextIdentity());
                view.setMenuId(menuId);
                view.setResourceId(resource.getId());
                view.setResourceName(resource.getResourceName());
                view.setPath(resource.getPath());
                view.setResourceDesc(resource.getResourceDesc());
                view.setParentId(pageResource.getId());
                buttonRoleList.add(view);


                getPageResourceList(resource, list, menuId, buttonRoleList);

            }


        }
    }


    private void findTabPanel(LcdpViewButtonRoleBean role, List<LcdpModulePageCompBean> modulePageCompList, Map<String, LcdpModulePageCompBean> tablePanleId2TablePanelMap, Map<String, String> id2ParentIdMap, LcdpModulePageCompBean button, Map<String, Map<String, String>> i18nMessageMap) {
        String parentId = id2ParentIdMap.get(button.getId());
        if (tablePanleId2TablePanelMap.get(parentId) != null) {
            role.setButtonParentId(parentId);
            JSONObject configObject = JSON.parseObject(tablePanleId2TablePanelMap.get(parentId).getConfig());
            JSONObject title = configObject.getJSONObject("title");
            String buttonParentName = I18nHelper.getMessage(title.getString("i18nCode"));
            if (StringUtils.equals(buttonParentName, title.getString("i18nCode"))) {
                String i18nCode = title.getString("i18nCode");

                if (i18nMessageMap == null) {
                    buttonParentName = i18nCode;
                } else {
                    if (i18nMessageMap.get(i18nCode) != null) {
                        buttonParentName = i18nMessageMap.get(i18nCode).get(I18nHelper.getLocal().toLanguageTag());
                    }
                }
            }
            role.setButtonParentName(buttonParentName);
        } else {
            if (parentId.equals("root")) {
                return;
            }
            LcdpModulePageCompBean pageComp = modulePageCompList.stream().filter(e -> StringUtils.equals(parentId, e.getId())).findAny().orElse(null);
            findTabPanel(role, modulePageCompList, tablePanleId2TablePanelMap, id2ParentIdMap, pageComp, i18nMessageMap);
        }

    }


}
