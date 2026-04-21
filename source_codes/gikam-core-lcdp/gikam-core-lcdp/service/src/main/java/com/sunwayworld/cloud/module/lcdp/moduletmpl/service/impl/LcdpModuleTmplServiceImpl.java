package com.sunwayworld.cloud.module.lcdp.moduletmpl.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.configparam.service.LcdpConfigParamService;
import com.sunwayworld.cloud.module.lcdp.message.sync.LcdpResourceSyncManager;
import com.sunwayworld.cloud.module.lcdp.moduletmpl.bean.*;
import com.sunwayworld.cloud.module.lcdp.moduletmpl.persistent.dao.LcdpModuleTmplDao;
import com.sunwayworld.cloud.module.lcdp.moduletmpl.service.*;
import com.sunwayworld.cloud.module.lcdp.moduletmpl.support.LcdpModuleTempHelper;
import com.sunwayworld.cloud.module.lcdp.resource.bean.*;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpModulePageCompService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpPageI18nCodeService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceHistoryService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpJavaCodeResolverUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpMapperUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpScriptUtils;
import com.sunwayworld.cloud.module.lcdp.support.LcdpScriptTemplateHelper;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableFieldDTO;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableService;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.database.context.instance.EntityHelper;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Repository
@GikamBean
public class LcdpModuleTmplServiceImpl implements LcdpModuleTmplService {

    @Autowired
    private LcdpModuleTmplDao lcdpModuleTmplDao;

    @Autowired
    private LcdpModuleTmplResourceService lcdpModuleTmplResourceService;

    @Autowired
    private LcdpModuleTmplPageCompService lcdpModuleTmplPageCompService;

    @Autowired
    private LcdpModuleTmplConfigService lcdpModuleTmplConfigService;

    @Autowired
    @Lazy
    private LcdpResourceService resourceService;

    @Autowired
    private LcdpTableService lcdpTableService;

    @Autowired
    private LcdpResourceHistoryService resourceHistoryService;

    @Autowired
    private LcdpModulePageCompService modulePageCompService;

    @Autowired
    private LcdpCustomTmplResourceService customTmplResourceService;

    @Autowired
    private LcdpCustomTmplPageCompService customTmplPageCompService;

    @Autowired
    private LcdpPageI18nCodeService pageI18nCodeService;

    @Autowired
    private LcdpConfigParamService lcdpConfigParamService;

    public static List<String> compList = ArrayUtils.asList("TabPanel", "GridToolbar", "Grid", "Button");

    private String current_environment_database = LcdpConstant.PROFILE_DB_LIST.stream().filter(profile -> ApplicationContextHelper.isProfileActivated(profile)).findFirst().get();


    @Override
    @SuppressWarnings("unchecked")
    public LcdpModuleTmplDao getDao() {
        return lcdpModuleTmplDao;
    }


    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public List<LcdpResourceBean> insertByModuleTmpl(RestJsonWrapperBean wrapper) {
        LcdpModuleTmplDTO moduleTmplDTO = wrapper.parseUnique(LcdpModuleTmplDTO.class);

        LcdpResourceBean module = new LcdpResourceBean();
        module.setParentId(moduleTmplDTO.getParentId());
        module.setResourceDesc(moduleTmplDTO.getResourceDesc());
        module.setResourceName(moduleTmplDTO.getResourceName());
        module.setResourceCategory(moduleTmplDTO.getResourceCategory());
        module.setId(ApplicationContextHelper.getNextIdentity());

        resourceService.updatePath(module);

        List<String> dependentTableNameList = new ArrayList<>();
        if (!StringUtils.isEmpty(moduleTmplDTO.getMasterTableName())) {
            dependentTableNameList.add(moduleTmplDTO.getMasterTableName());
        }
        if (!ObjectUtils.isEmpty(moduleTmplDTO.getChildTableList())) {
            dependentTableNameList.addAll(moduleTmplDTO.getChildTableList().stream().map(LcdpModuleTmplChildTableDTO::getTableName).collect(Collectors.toList()));
        }
        module.setDependentTable(StringUtils.join(dependentTableNameList, ","));

        resourceService.getDao().insert(module);

        List<LcdpResourceBean> resourceList = new ArrayList<>();
        resourceList.add(module);

        if ("sys".equals(moduleTmplDTO.getTmplClassId())) {
            resourceList.addAll(createBySysModuleTmpl(moduleTmplDTO, module));
        } else {
            resourceList.addAll(createByCustomModuleTmpl(moduleTmplDTO, module));
        }

        return resourceList;
    }


    @Override
    @Transactional
    @Audit(AuditConstant.DELETE)
    public void delete(RestJsonWrapperBean wrapper) {
        List<LcdpModuleTmplBean> deleteList = wrapper.parse(this.getDao().getType());
        if (deleteList.isEmpty()) {
            return;
        }
        List<Long> idList = deleteList.stream().map(LcdpModuleTmplBean::getId).collect(Collectors.toList());
        List<LcdpModuleTmplBean> moduleTmplList = getDao().selectListByIds(idList).stream().filter(tmpl -> LcdpConstant.MODULE_TMPL_CLASS_SYS.equals(tmpl.getTmplClassId())).collect(Collectors.toList());
        if (!moduleTmplList.isEmpty()) {
            ServletUtils.responseError(HttpStatus.BAD_REQUEST.value(), I18nHelper.getMessage("LCDP.MODULETEMP.DELETE.VALIDATE.SYS_NOT_ALLOWED_DELETE"));
            return;
        }
        List<LcdpCustomTmplResourceBean> customTmplResourceList = customTmplResourceService.selectListByFilter(SearchFilter.instance().match("moduleTmplId", idList).filter(MatchPattern.OR));
        getDao().deleteByIdList(idList);
        if (!customTmplResourceList.isEmpty()) {
            List<Long> customTmplResourceIdList = customTmplResourceList.stream().map(LcdpCustomTmplResourceBean::getId).collect(Collectors.toList());
            List<LcdpCustomTmplPageCompBean> customTmplPageCompList = customTmplPageCompService.selectListByFilter(SearchFilter.instance().match("customTmplPageId", customTmplResourceIdList).filter(MatchPattern.OR));
            customTmplResourceService.getDao().deleteByIdList(customTmplResourceIdList);
            customTmplPageCompService.getDao().deleteBy(customTmplPageCompList);
        }

    }

    @Override
    public void saveCustomTemplate(RestJsonWrapperBean wrapper) {
        Map<String, String> paramMap = wrapper.getParamMap();
        String resourceIdStr = paramMap.get("resourceId");
        String customTmplName = paramMap.get("customTmplName");
        //创建模板数据
        LcdpModuleTmplBean moduleTmpl = new LcdpModuleTmplBean();
        moduleTmpl.setId(ApplicationContextHelper.getNextIdentity());
        moduleTmpl.setModuleTmplName(customTmplName);
        moduleTmpl.setTmplClassId("biz");
        moduleTmpl.setTmplType("module");

        getDao().insert(moduleTmpl);

        saveCustomTmplData(resourceIdStr, moduleTmpl.getId());
    }

    @Override
    public Page<LcdpModuleTmplBean> selectCustomTemplatePagination(RestJsonWrapperBean wrapper) {
        return selectPaginationByFilter(SearchFilter.instance().match("TMPLCLASSID", "biz").filter(MatchPattern.EQ), wrapper);
    }

    //---------------------------------------------------------------------------------------------------------
    // 私有方法
    //---------------------------------------------------------------------------------------------------------
    private void saveCustomTmplData(String resourceIdStr, Long tmplId) {
        Long resourceId = Long.valueOf(resourceIdStr);
        LcdpResourceBean module = resourceService.selectById(resourceId);
        List<LcdpResourceBean> childResourceList = resourceService.selectListByFilter(SearchFilter.instance().match("PARENTID", module.getId()).filter(MatchPattern.EQ).match("RESOURCECATEGORY", LcdpConstant.RESOURCE_CATEGORY_VIEW).filter(MatchPattern.EQ).match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));
        if (childResourceList.isEmpty()) {
            return;
        }
        childResourceList = childResourceList.stream().filter(r -> !noReturnDataFilter(r)).collect(Collectors.toList());


        List<Long> childResourceIdList = childResourceList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());

        List<LcdpResourceHistoryBean> resourceHistoryDataList = new ArrayList<>();

        List<LcdpResourceHistoryBean> resourceHistoryList = resourceHistoryService.selectListByFilter(SearchFilter.instance().match("RESOURCEID", childResourceIdList).filter(MatchPattern.OR));

        List<LcdpResourceHistoryBean> effectResourceList = resourceHistoryList.stream().filter(resource -> LcdpConstant.EFFECT_FLAG_YES.equals(resource.getEffectFlag())).collect(Collectors.toList());

        List<LcdpResourceHistoryBean> unSubmitResourceList = resourceHistoryList.stream().filter(resource -> LcdpConstant.SUBMIT_FLAG_NO.equals(resource.getSubmitFlag()) && LocalContextHelper.getLoginUserId().equals(resource.getCreatedById())).collect(Collectors.toList());

        if (!unSubmitResourceList.isEmpty()) {
            resourceHistoryDataList.addAll(unSubmitResourceList);
        }
        List<Long> editIdList = unSubmitResourceList.stream().map(LcdpResourceHistoryBean::getResourceId).collect(Collectors.toList());

        for (int i = 0; i < effectResourceList.size(); i++) {
            LcdpResourceHistoryBean effectData = effectResourceList.get(i);
            if (!editIdList.contains(effectData.getResourceId())) {
                resourceHistoryDataList.add(effectData);
            }
        }

        List<LcdpResourceHistoryBean> viewHistory = resourceHistoryDataList.stream().filter(history -> LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(history.getResourceCategory())).collect(Collectors.toList());
        List<Long> viewIdList = viewHistory.stream().map(LcdpResourceHistoryBean::getId).collect(Collectors.toList());

        List<LcdpModulePageCompBean> modulePageCompList = modulePageCompService.selectListByFilter(SearchFilter.instance().match("MODULEPAGEHISTORYID", viewIdList).filter(MatchPattern.OR));

        Map<Long, List<LcdpModulePageCompBean>> historyResourceId2PageCompListMap = modulePageCompList.stream().collect(Collectors.groupingBy(LcdpModulePageCompBean::getModulePageHistoryId));

        Map<Long, List<LcdpModulePageCompBean>> historyId2NewPageCompListMap = new HashMap<>();

        historyResourceId2PageCompListMap.forEach((historyId, pageCompList) -> {
            historyId2NewPageCompListMap.put(historyId, resourceService.generateModulePageComps(pageCompList));
        });


        //处理数据
        module.setId(ApplicationContextHelper.getNextIdentity());

        Map<Long, Long> id2Map = new HashMap<>();
        for (LcdpResourceHistoryBean resource : resourceHistoryDataList) {
            Long id = ApplicationContextHelper.getNextIdentity();
            id2Map.put(resource.getId(), id);
            resource.setId(id);
        }
        List<LcdpModulePageCompBean> modulePageCompDataList = new ArrayList<>();
        historyId2NewPageCompListMap.forEach((k, v) -> {
            for (LcdpModulePageCompBean pageComp : v) {
                pageComp.setModulePageHistoryId(id2Map.get(k));
            }
            modulePageCompDataList.addAll(v);
        });

        //生成数据并入库
        LcdpCustomTmplResourceBean customTmplResource = new LcdpCustomTmplResourceBean();
        BeanUtils.copyProperties(module, customTmplResource);
        customTmplResource.setModuleTmplId(tmplId);

        List<LcdpCustomTmplResourceBean> customTmplResourceList = resourceHistoryDataList.stream().map(resourceHistory -> {
            LcdpCustomTmplResourceBean childCustomTmplResource = new LcdpCustomTmplResourceBean();
            BeanUtils.copyProperties(resourceHistory, childCustomTmplResource);
            childCustomTmplResource.setModuleTmplId(tmplId);
            childCustomTmplResource.setParentId(module.getId());
            childCustomTmplResource.setModuleName(module.getResourceName());
            return childCustomTmplResource;
        }).collect(Collectors.toList());


        List<LcdpCustomTmplPageCompBean> customTmplPageCompList = modulePageCompDataList.stream().map(modulePageComp -> {
            LcdpCustomTmplPageCompBean customTmplPageComp = new LcdpCustomTmplPageCompBean();
            BeanUtils.copyProperties(modulePageComp, customTmplPageComp);
            customTmplPageComp.setCustomTmplPageId(modulePageComp.getModulePageHistoryId());
            return customTmplPageComp;
        }).collect(Collectors.toList());

        customTmplResourceService.getDao().insert(customTmplResourceList);
        customTmplPageCompService.getDao().insert(customTmplPageCompList);


    }

    private boolean noReturnDataFilter(LcdpResourceBean resourceBean) {

        return LcdpConstant.RESOURCE_SCRIPT_CATEGORY_LIST.contains(resourceBean.getResourceCategory()) && ObjectUtils.isEmpty(resourceBean.getEffectVersion()) && !LocalContextHelper.getLoginUserId().equals(resourceBean.getCreatedById());
    }


    private List<LcdpModuleTmplPageCompBean> generatePageComp(List<LcdpModuleTmplResourceBean> moduleTmplResourceList, LcdpModuleTmplDTO moduleTmplDTO, List<LcdpTableDTO> childTableDTOList) {

        List<Long> resourceIdList = moduleTmplResourceList.stream().map(LcdpModuleTmplResourceBean::getId).collect(Collectors.toList());
        List<LcdpModuleTmplPageCompBean> moduleTmplPageCompList = lcdpModuleTmplPageCompService.selectListByFilter(SearchFilter.instance().match("MODULETMPLPAGEID", resourceIdList).filter(MatchPattern.OR));
        List<LcdpModuleTmplPageCompBean> newCompList = new ArrayList<>();//用于存放新生成的组件数据
        Map<Long, List<LcdpModuleTmplPageCompBean>> tmplPageId2PageCompList = moduleTmplPageCompList.stream().collect(Collectors.groupingBy(LcdpModuleTmplPageCompBean::getModuleTmplPageId));
        tmplPageId2PageCompList.forEach((k, pageCompList) -> {
            buildPageComp(newCompList, pageCompList, moduleTmplDTO, childTableDTOList);
        });
        moduleTmplPageCompList.addAll(newCompList);

        List<LcdpModuleTmplPageCompBean> gridToolbarList = moduleTmplPageCompList.stream().filter(comp -> "GridToolbar".equals(comp.getType())).collect(Collectors.toList());
        if (!gridToolbarList.isEmpty()) {
            gridToolbarList.forEach(toolbar -> {
                JSONObject toolBarConfig = JSON.parseObject(toolbar.getConfig());
                toolBarConfig.put("toolbarWrap", false);
                toolBarConfig.remove("generalButtonGroup");
                toolBarConfig.remove("generalPanelTrigger");
                toolbar.setConfig(toolBarConfig.toJSONString());
            });
        }

        List<LcdpModuleTmplPageCompBean> buttonList = moduleTmplPageCompList.stream().filter(comp -> "Button".equals(comp.getType())).collect(Collectors.toList());
        if (!buttonList.isEmpty()) {
            for (int i = 0; i < buttonList.size(); i++) {
                JSONObject buttonConfig = JSON.parseObject(buttonList.get(i).getConfig());
                //
                if (buttonConfig.getString("id").startsWith("Button_")) {
                    buttonConfig.put("id", "Button_" + (i + 1));
                    if (StringUtils.equals(buttonConfig.getString("iconType"), "submit")) {
                        buttonConfig.put("iconType", "batch-submit");
                    }
                }

                if (buttonConfig.getString("id").startsWith("detail-button-submit")) {
                    if (StringUtils.equals(buttonConfig.getString("iconType"), "submit")) {
                        buttonConfig.put("iconType", "batch-submit");
                    }
                }

                dealButtonI18NCode(buttonConfig);

                buttonList.get(i).setConfig(buttonConfig.toJSONString());

            }
            ;

        }

        List<LcdpModuleTmplPageCompBean> tabPanelList = moduleTmplPageCompList.stream().filter(comp -> "TabPanel".equals(comp.getType())).collect(Collectors.toList());
        List<String> idIndexList = tabPanelList.stream().map(tabPanel -> {
            JSONObject panelConfig = JSON.parseObject(tabPanel.getConfig());
            return panelConfig.getString("id").split("_")[1];
        }).filter(index -> !StringUtils.isEmpty(index) && NumberUtils.isNumber(index)).collect(Collectors.toList());
        Long index = 0L;
        if (!idIndexList.isEmpty()) {
            index = idIndexList.stream().map(Long::valueOf).max(Comparator.comparing(Function.identity())).get();
        }
        for (int i = 0; i < tabPanelList.size(); i++) {
            LcdpModuleTmplPageCompBean tabPanel = tabPanelList.get(i);
            JSONObject panelConfig = JSON.parseObject(tabPanel.getConfig());
            if (panelConfig.getString("id").startsWith("TabPanel_")) {
                panelConfig.put("id", "TabPanel_" + (index + (i + 1)));
            }

            tabPanelList.get(i).setConfig(panelConfig.toJSONString());
        }


        return moduleTmplPageCompList;
    }

    private void dealButtonI18NCode(JSONObject buttonConfig) {
        String method = buttonConfig.getString("onClick__$S$__");
        String buttonI118NCode = LcdpConstant.BUTTON_I18N_MAP.get(method);
        if (method.startsWith("insert")) {
            buttonI118NCode = LcdpConstant.BUTTON_I18N_MAP.get("insert");
        }

        if (method.startsWith("delete")) {
            buttonI118NCode = LcdpConstant.BUTTON_I18N_MAP.get("delete");
        }

        JSONObject text = buttonConfig.getJSONObject("text");
        text.put("type", "i18n");
        text.put("i18nCode", buttonI118NCode);
        text.remove("en-US");
        buttonConfig.put("text", text);
    }

    private void buildPageComp(List<LcdpModuleTmplPageCompBean> newCompList, List<LcdpModuleTmplPageCompBean> pageCompList, LcdpModuleTmplDTO moduleTmplDTO, List<LcdpTableDTO> childTableDTOList) {
        //用来处理页面数据
        LcdpModuleTmplDTO dealTmplDTO = new LcdpModuleTmplDTO();

        BeanUtils.copyProperties(moduleTmplDTO, dealTmplDTO);

        String masterTableName = dealTmplDTO.getMasterTableName();

        if (moduleTmplDTO.getModuleTmplId() == 10001L) {
            LcdpModuleTmplPageCompBean layout = pageCompList.stream().filter(comp -> comp.getType().equals("Layout")).findFirst().orElse(null);


            JSONObject layoutConfig = JSON.parseObject(layout.getConfig());
            layoutConfig.put("layout", "wc");
            layout.setConfig(layoutConfig.toJSONString());
        }
        //查询主表信息
        LcdpTableDTO masterTableDTO = lcdpTableService.selectPhysicalTableInfo(masterTableName);
        List<LcdpTableFieldDTO> masterFieldList = masterTableDTO.getFieldList();
        //跳过ID字段并截取前50个字段用于生成数据 原因：如表中字段过多则会导致在存储grid配置时会导致数据过长插库报错  不解决的原因是因为CLOB字段在保存时效率过慢 体验极其不好
        masterFieldList = masterFieldList.stream().skip(1).limit(50).collect(Collectors.toList());

        //拿到主grid
        LcdpModuleTmplPageCompBean masterDataGrid = pageCompList.stream().filter(comp -> "Grid".equals(comp.getType()) && "master".equals(comp.getCompTag())).findFirst().orElse(null);

        //处理主gird
        if (!ObjectUtils.isEmpty(masterDataGrid)) {
            dealMasterGrid(dealTmplDTO, newCompList, masterFieldList, masterDataGrid);
        }

        LcdpModuleTmplPageCompBean formComp = pageCompList.stream().filter(comp -> "Form".equals(comp.getType()) && "master".equals(comp.getCompTag())).findFirst().orElse(null);

        //处理主form
        if (!ObjectUtils.isEmpty(formComp)) {
            dealMasterForm(dealTmplDTO, newCompList, masterFieldList, formComp);
        }

        //区分左右模板的tab和穿透模板tab
        //左右模板的tab仅有一个 穿透模板的则是有两个：第一个装form 第二个装子表和uploader  组件标记为child
        List<LcdpModuleTmplPageCompBean> tabList = pageCompList.stream().filter(comp -> "Tab".equals(comp.getType())).collect(Collectors.toList());
        LcdpModuleTmplPageCompBean tab = new LcdpModuleTmplPageCompBean();
        List<LcdpModuleTmplPageCompBean> childTabList = tabList.stream().filter(comp -> "child".equals(comp.getCompTag())).collect(Collectors.toList());
        if (childTabList.isEmpty() && !tabList.isEmpty()) {
            tab = tabList.get(0);
        } else if (!childTabList.isEmpty()) {
            tab = childTabList.get(0);
        }
        JSONObject tabConfig = new JSONObject();
        JSONArray tabChildrenWidgetId = new JSONArray();
        if (!ObjectUtils.isEmpty(tab.getConfig())) {
            tabConfig = JSON.parseObject(tab.getConfig());
            tabChildrenWidgetId = (JSONArray) tabConfig.get("childrenWidgetId");
        }

        //根据子表情况创建对应的tablepanel
        if (!childTableDTOList.isEmpty()) {
            Map<String, LcdpTableDTO> tableName2TableDTOMap = childTableDTOList.stream().collect(Collectors.toMap(LcdpTableDTO::getTableName, Function.identity()));

            List<LcdpModuleTmplPageCompBean> gridCompList = pageCompList.stream().filter(comp -> compList.contains(comp.getType()) && "child".equals(comp.getCompTag())).collect(Collectors.toList());

            if (!gridCompList.isEmpty()) {
                tableName2TableDTOMap.forEach((table, tableDTO) -> {
                    List<LcdpTableFieldDTO> fieldDTOList = tableDTO.getFieldList().stream().filter(field -> !StringUtils.equals("ID", field.getFieldName())).collect(Collectors.toList());
                    tableDTO.setFieldList(fieldDTOList);
                    //处理子表数据
                    copyChildPanelAndGrid(dealTmplDTO, gridCompList, tableDTO, newCompList);
                });
            }
            String tabId = tab.getId();
            //处理tab页装载数据
            gridCompList.stream().filter(comp -> "TabPanel".equals(comp.getType())).forEach(comp -> comp.setParentId("remove"));
            List<LcdpModuleTmplPageCompBean> newTabPanelList = newCompList.stream().filter(comp -> "TabPanel".equals(comp.getType()) && null == comp.getCompTag() && comp.getParentId().equals(tabId)).collect(Collectors.toList());
            List<String> newTabPanelIdList = newTabPanelList.stream().map(LcdpModuleTmplPageCompBean::getId).collect(Collectors.toList());

            //针对于uploader的一个处理  uploader的位置在左右模板中在tab的第三个页签上 穿透模板中在第二个页签上 同时对应模板中预设的子表则是一个是第二个页签和第一个页面
            if (!newTabPanelIdList.isEmpty()) {
                //先移除模板中原来的子表
                tabChildrenWidgetId.remove("child".equals(tab.getCompTag()) ? 0 : 1);
                Object uploaderId = null;
                int dealIndex = "child".equals(tab.getCompTag()) ? 0 : 1;
                if (tabChildrenWidgetId.size() > dealIndex) {
                    uploaderId = tabChildrenWidgetId.get(tabChildrenWidgetId.size() - 1);
                    tabChildrenWidgetId.remove(tabChildrenWidgetId.size() - 1);
                }
                for (String id : newTabPanelIdList) {
                    tabChildrenWidgetId.add(id);
                }
                //追加上附件
                if (null != uploaderId) {
                    tabChildrenWidgetId.add(uploaderId);
                }
            }
        } else {
            pageCompList.stream().filter(comp -> "child".equals(comp.getCompTag()) && "TabPanel".equals(comp.getType())).forEach(comp -> comp.setParentId("remove"));
            if (tabChildrenWidgetId.size() > 0) {
                if (!(moduleTmplDTO.getModuleTmplId() == LcdpConstant.MODULE_TMPL_UP_DOWN_ID.longValue())) {
                    tabChildrenWidgetId.remove("child".equals(tab.getCompTag()) ? 0 : 1);
                } else {
                    tabChildrenWidgetId.remove(0);
                }
            }

        }

        if (tabChildrenWidgetId.size() == 0) {
            tab.setParentId("remove");
            String tabId = tab.getId();
            pageCompList.stream().filter(comp -> "LayoutCenter".equals(comp.getType()) && !StringUtils.equals("master", comp.getCompTag())).forEach(comp -> {
                int removeId = 0;
                JSONObject layoutCenterConfig = JSON.parseObject(comp.getConfig());
                JSONArray childrenWidgetId = (JSONArray) layoutCenterConfig.get("childrenWidgetId");
                for (int i = 0; i < childrenWidgetId.size(); i++) {
                    if (childrenWidgetId.getString(i).equals(tabId)) {
                        removeId = i;
                    }
                }
                childrenWidgetId.remove(removeId);
                layoutCenterConfig.put("childrenWidgetId", childrenWidgetId);
                comp.setConfig(layoutCenterConfig.toJSONString());
            });

        } else {
            tabConfig.put("childrenWidgetId", tabChildrenWidgetId);
            tab.setConfig(tabConfig.toJSONString());
        }


        //处理uploader
        List<LcdpModuleTmplPageCompBean> uploaderList = pageCompList.stream().filter(comp -> "Uploader".equals(comp.getType())).collect(Collectors.toList());
        if (!uploaderList.isEmpty()) {
            LcdpModuleTmplPageCompBean uploaderComp = uploaderList.get(0);
            JSONObject uploaderConfig = JSON.parseObject(uploaderComp.getConfig());
            uploaderConfig.put("id", String.format(uploaderConfig.getString("id"), dealTmplDTO.getModuleName().toLowerCase()));
            uploaderConfig.put("dbTable", dealTmplDTO.getMasterTableName());
            uploaderConfig.put("preview", true);
            uploaderConfig.put("gridPage", true);
            uploaderConfig.remove("toolbarAlign");
            //添加依赖组件ID
            uploaderConfig.put("dependentWidgetId", dealTmplDTO.getDependentWidgetId());

            uploaderComp.setConfig(uploaderConfig.toJSONString());
        }


        List<LcdpModuleTmplPageCompBean> windowToolbarList = pageCompList.stream().filter(comp -> "WindowToolbar".equals(comp.getType())).collect(Collectors.toList());
        if (!windowToolbarList.isEmpty()) {
            windowToolbarList.forEach(toolbar -> {
                JSONObject toolBarConfig = JSON.parseObject(toolbar.getConfig());
                toolBarConfig.remove("toolbarAlign");
                toolbar.setConfig(toolBarConfig.toJSONString());
            });
        }

        List<LcdpModuleTmplPageCompBean> gridToolbarList = pageCompList.stream().filter(comp -> "GridToolbar".equals(comp.getType())).collect(Collectors.toList());
        List<LcdpModuleTmplPageCompBean> newGridToolBarList = newCompList.stream().filter(comp -> "GridToolbar".equals(comp.getType())).collect(Collectors.toList());
        if (!gridToolbarList.isEmpty()) {
            if (!newGridToolBarList.isEmpty()) {
                gridToolbarList.addAll(newGridToolBarList);
            }
            gridToolbarList.forEach(toolbar -> {
                JSONObject toolBarConfig = JSON.parseObject(toolbar.getConfig());
                toolBarConfig.remove("toolbarAlign");
                toolbar.setConfig(toolBarConfig.toJSONString());
            });
        }

        List<LcdpModuleTmplPageCompBean> tabPanelList = pageCompList.stream().filter(comp -> "TabPanel".equals(comp.getType())).collect(Collectors.toList());
        tabPanelList.forEach(tabPanel -> {
            JSONObject panelConfig = JSON.parseObject(tabPanel.getConfig());
            JSONObject title = panelConfig.getJSONObject("title");
            if (StringUtils.equals(title.getString("zh-CN"), "基本信息")) {
                title.put("type", "i18n");
                title.put("i18nCode", "GIKAM.FORM.BASE_INFO");
                title.remove("en-US");
                panelConfig.put("title", title);
                tabPanel.setConfig(panelConfig.toJSONString());
            }

        });
    }

    /**
     * 处理子表信息，子表信息在一个新的tabpanel页中，主要是grid数据。
     * 进行的处理 创建新的tabpanel GridToolbar button grid 逐级创建 并建立关联关系
     * 关系 tabpanel下面是grid  grid 的子组件为gird里面的列字段和GridToolbar GridToolbar里面放的是button
     *
     * @param moduleTmplDTO 模板数据DTO
     * @param gridCompList  子表信息集合 含 tabpanel GridToolbar button grid 信息
     * @param tableDTO      表信息
     * @param newCompList   新生成主键的集合
     */
    private void copyChildPanelAndGrid(LcdpModuleTmplDTO moduleTmplDTO, List<LcdpModuleTmplPageCompBean> gridCompList, LcdpTableDTO tableDTO, List<LcdpModuleTmplPageCompBean> newCompList) {

        //复制一个tabpanel出来承接gird
        LcdpModuleTmplPageCompBean tabPanelComp = gridCompList.stream().filter(comp -> "TabPanel".equals(comp.getType())).findFirst().get();
        LcdpModuleTmplPageCompBean copyTabPanel = new LcdpModuleTmplPageCompBean();
        BeanUtils.copyProperties(tabPanelComp, copyTabPanel);
        copyTabPanel.setId(StringUtils.randomUUID());
        //将组件标记置空
        copyTabPanel.setCompTag(null);

        //拿到Panelconfig，准备处理
        JSONObject panelConfig = JSON.parseObject(copyTabPanel.getConfig());
        @SuppressWarnings("unchecked")
        Map<String, Object> titleMap = panelConfig.getObject("title", Map.class);
        titleMap.put("en-US", tableDTO.getTableName());
        titleMap.put("zh-CN", tableDTO.getTableDesc());
        titleMap.put("type", "i18n");
        titleMap.put("i18nCode", tableDTO.getTableName() + ".SERVICE_NAME");
        titleMap.remove("en-US");
        panelConfig.put("title", titleMap);

        //复制grid
        LcdpModuleTmplPageCompBean gridComp = gridCompList.stream().filter(comp -> "Grid".equals(comp.getType())).findFirst().get();
        LcdpModuleTmplPageCompBean copyGrid = new LcdpModuleTmplPageCompBean();
        BeanUtils.copyProperties(gridComp, copyGrid);
        copyGrid.setId(StringUtils.randomUUID());
        copyGrid.setParentId(copyTabPanel.getId());

        //修改panelConfig 子节点字段
        panelConfig.put("childrenWidgetId", new String[]{copyGrid.getId()});
        copyTabPanel.setConfig(panelConfig.toJSONString());

        JSONObject gridConfig = JSON.parseObject(copyGrid.getConfig());

        String gridId = String.format(gridConfig.getString("id"), moduleTmplDTO.getModuleName().toLowerCase(), tableDTO.getTableName().toLowerCase().replaceFirst("t_", "").replaceAll("_", "-"));

        //截取前50个字段用于生成数据 原因：如表中字段过多则会导致在存储grid配置时会导致数据过长插库报错  不解决的原因是因为CLOB字段在保存时效率过慢 体验极其不好
        List<LcdpTableFieldDTO> fieldList = tableDTO.getFieldList().stream().limit(50).collect(Collectors.toList());
        //先拿到grid的列字段
        List<LcdpTableFieldDTO> createFieldList = fieldList.stream().filter(mf -> LcdpConstant.CREATE_COLUMN_LIST.contains(mf.getFieldName().toUpperCase())).collect(Collectors.toList());
        fieldList.removeAll(createFieldList);
        List<LcdpTableFieldDTO> columnConfigList = buildGridColumnConfigList(gridId, fieldList);
        columnConfigList.addAll(buildGridColumnConfigList(gridId, createFieldList));

        //将字段放置于grid中
        List<LcdpModuleTmplPageCompBean> gridColumnList = new ArrayList<>();
        gridColumnList.addAll(bulidCommonColumns(copyGrid.getId(), copyGrid.getPageType()));
        columnConfigList.forEach(field -> {
            LcdpModuleTmplPageCompBean gridColumn = new LcdpModuleTmplPageCompBean();
            gridColumn.setId(StringUtils.randomUUID());
            gridColumn.setParentId(copyGrid.getId());
            gridColumn.setType("GridColumn");
            JSONObject columnConfig = JSON.parseObject(field.getExt$Item("columnconfig"));

            //判断创建人字段
            if (LcdpConstant.CREATE_COLUMN_LIST.contains(field.getFieldName().toUpperCase())) {
                columnConfig.put("editor", false);
                field.setExt$Item("columnconfig", columnConfig.toJSONString());
            }
            //判断页面属性
            if (!StringUtils.equals(gridComp.getPageType(), "EditPage") && moduleTmplDTO.getModuleTmplId().longValue() != LcdpConstant.MODULE_TEPL_PENETRATION) {
                columnConfig.put("editor", false);
                field.setExt$Item("columnconfig", columnConfig.toJSONString());
            }
            gridColumn.setConfig(fieldToAddValidateConfig(field, "column"));
            gridColumn.setPageType(copyGrid.getPageType());
            gridColumnList.add(gridColumn);
        });
        gridColumnList.forEach(column -> {
            JSONObject columnConfig = JSON.parseObject(column.getConfig());
            if (StringUtils.equals(columnConfig.getString("gridColumnType"), "checkbox") || StringUtils.equals(columnConfig.getString("gridColumnType"), "index")) {
                return;
            }
            JSONObject title = columnConfig.getJSONObject("title");
            title.put("type", "i18n");
            title.put("i18nCode", (tableDTO.getTableName() + "." + columnConfig.getString("field")).toUpperCase());
            title.remove("en-US");
            columnConfig.put("title", title);
            column.setConfig(columnConfig.toJSONString());
        });
        List<String> gridColumnIdList = gridColumnList.stream().map(LcdpModuleTmplPageCompBean::getId).collect(Collectors.toList());
        gridConfig.put("childrenWidgetId", gridColumnIdList.toArray());
        gridConfig.put("id", gridId);
        String urlPath = moduleTmplDTO.getModulePath() + "server.Lcdp" + firstUppercase(tableDTO.getTableName().replaceFirst("T_", "").toLowerCase())[1] + "Service";
        gridConfig.put("url", String.format(gridConfig.getString("url"), urlPath));
        gridConfig.put("getRequestData__$S$__", "get" + firstUppercase(tableDTO.getTableName().replaceFirst("T_", "").toLowerCase())[1] + "RequestData");
        String instantSavePath = urlPath + ".updateData";
        gridConfig.put("instantSavePath", instantSavePath);

        //复制gridToolBar
        LcdpModuleTmplPageCompBean gridToolbarComp = gridCompList.stream().filter(comp -> "GridToolbar".equals(comp.getType())).findFirst().get();
        LcdpModuleTmplPageCompBean copyGridToolbar = new LcdpModuleTmplPageCompBean();
        BeanUtils.copyProperties(gridToolbarComp, copyGridToolbar);
        copyGridToolbar.setId(StringUtils.randomUUID());
        copyGridToolbar.setParentId(copyGrid.getId());
        gridConfig.put("toolbar_childrenWidgetId", new String[]{copyGridToolbar.getId()});
        gridConfig.put("confirmField", columnConfigList.stream().findFirst().get().getFieldName().toLowerCase());

        //添加依赖组件ID
        if (LcdpConstant.MODULE_TMPL_LEFT_RIGHT_ID.longValue() == moduleTmplDTO.getModuleTmplId() || LcdpConstant.MODULE_TMPL_UP_DOWN_ID.longValue() == moduleTmplDTO.getModuleTmplId()) {
            gridConfig.put("dependentWidgetId", moduleTmplDTO.getDependentWidgetId());
        }
        copyGrid.setConfig(dealGridConfig(gridConfig));

        JSONObject gridToobarConfig = JSON.parseObject(copyGridToolbar.getConfig());

        //复制button
        List<LcdpModuleTmplPageCompBean> buttonCompList = gridCompList.stream().filter(comp -> "Button".equals(comp.getType())).collect(Collectors.toList());
        Map<String, String> buttonIdMap = new HashMap<>();
        List<LcdpModuleTmplPageCompBean> copyButtonList = buttonCompList.stream().map(button -> {
            LcdpModuleTmplPageCompBean copyButton = new LcdpModuleTmplPageCompBean();
            BeanUtils.copyProperties(button, copyButton);
            copyButton.setId(StringUtils.randomUUID());
            buttonIdMap.put(button.getId(), copyButton.getId());
            copyButton.setParentId(copyGridToolbar.getId());
            copyButton.setConfig(String.format(button.getConfig(), firstUppercase(tableDTO.getTableName().replaceFirst("T_", "").toLowerCase())[1]));
            return copyButton;
        }).collect(Collectors.toList());
        JSONArray gridToolbarChildrenWidgetId = gridToobarConfig.getJSONArray("childrenWidgetId");
        for (int i = 0; i < gridToolbarChildrenWidgetId.size(); i++) {
            gridToolbarChildrenWidgetId.set(i, buttonIdMap.get(gridToolbarChildrenWidgetId.getString(i)));
        }
        gridToobarConfig.put("childrenWidgetId", gridToolbarChildrenWidgetId);
        copyGridToolbar.setConfig(gridToobarConfig.toJSONString());

        newCompList.add(copyTabPanel);
        newCompList.add(copyGrid);
        newCompList.add(copyGridToolbar);
        newCompList.addAll(copyButtonList);
        newCompList.addAll(gridColumnList);


    }

    private String fieldToAddValidateConfig(LcdpTableFieldDTO field, String type) {
        JSONObject fieldConfig = null;
        if (StringUtils.equals("column", type)) {
            fieldConfig = JSON.parseObject(field.getExt$Item("columnconfig"));
        } else if (StringUtils.equals("field", type)) {
            fieldConfig = JSON.parseObject(field.getExt$Item("fieldconfig"));
        }
        String fieldType = field.getFieldType();
        if (StringUtils.equalsIgnoreCase("text", fieldType)) {
            String fieldLength = field.getFieldLength();
            List<String> validateList = new ArrayList<>();

            if (fieldLength != null && ConvertUtils.convert(fieldLength, Long.class, 0L) > 3) {
                Long length = ConvertUtils.convert(fieldLength, Long.class);
                long validateLength = length / 3;
                validateList.add("strLength[0," + validateLength + "]");
            }
            fieldConfig.put("validators", validateList.toArray());
        }

        return fieldConfig.toJSONString();

    }

    private void dealMasterGrid(LcdpModuleTmplDTO moduleTmplDTO, List<LcdpModuleTmplPageCompBean> newCompList, List<LcdpTableFieldDTO> masterFieldList, LcdpModuleTmplPageCompBean masterDataGrid) {
        //拿到主Grid配置
        JSONObject masterGridConfig = JSON.parseObject(masterDataGrid.getConfig());
        String gridId = String.format(masterGridConfig.getString("id"), moduleTmplDTO.getModuleName().toLowerCase());

        //创建grid列
        LcdpModuleTmplPageCompBean processStatus = null;
        List<LcdpTableFieldDTO> dealFiledList = masterFieldList.stream().map(field -> {
            LcdpTableFieldDTO fieldDTO = new LcdpTableFieldDTO();
            BeanUtils.copyProperties(field, fieldDTO);
            return fieldDTO;
        }).collect(Collectors.toList());
        List<LcdpTableFieldDTO> createFieldList = dealFiledList.stream().filter(mf -> LcdpConstant.CREATE_COLUMN_LIST.contains(mf.getFieldName().toUpperCase())).collect(Collectors.toList());
        dealFiledList.removeAll(createFieldList);
        if (StringUtils.equals(Constant.WORKFLOW_TYPE_TRADITION, moduleTmplDTO.getBpFlag())) {
            dealFiledList = dealFiledList.stream().filter(field -> !StringUtils.equalsIgnoreCase("PROCESSSTATUS", field.getFieldName())).collect(Collectors.toList());
            String fieldTmpl = LcdpModuleTempHelper.FIELD_TMPL_MAP.get("processStatus");
            String fieldConfig = String.format(fieldTmpl, moduleTmplDTO.getMasterTableName());

            processStatus = new LcdpModuleTmplPageCompBean();
            processStatus.setId(StringUtils.randomUUID());
            processStatus.setParentId(masterDataGrid.getId());
            processStatus.setType("GridColumn");
            processStatus.setPageType(masterDataGrid.getPageType());
            processStatus.setConfig(fieldConfig);
        }

        List<LcdpTableFieldDTO> columnConfigList = buildGridColumnConfigList(gridId, dealFiledList);
        columnConfigList.addAll(buildGridColumnConfigList(gridId, createFieldList));
        if (LcdpConstant.MODULE_TEPL_PENETRATION.longValue() == moduleTmplDTO.getModuleTmplId() && !StringUtils.equals(masterDataGrid.getPageType(), "ChoosePage")) {
            LcdpTableFieldDTO field = columnConfigList.get(0);
            String fieldTmpl = LcdpModuleTempHelper.FIELD_TMPL_MAP.get("linkGridColumn");
            String fieldName = field.getFieldName().toLowerCase();
            String fieldConfig = String.format(fieldTmpl, fieldName, field.getFieldComment(), gridId + "_" + fieldName, fieldName);
            field.setExt$Item("columnconfig", fieldConfig);
        }
        List<LcdpModuleTmplPageCompBean> gridColumnList = new ArrayList<>();
        gridColumnList.addAll(bulidCommonColumns(masterDataGrid.getId(), masterDataGrid.getPageType()));

        if (processStatus != null) {
            gridColumnList.add(processStatus);
        }
        columnConfigList.forEach(filed -> {
            // 业务工作流特殊处理
            if ("wfnodename".equals(filed.getFieldName().toLowerCase()) && !StringUtils.equals(masterDataGrid.getPageType(), "ChoosePage")) {
                String fieldTmpl = LcdpModuleTempHelper.FIELD_TMPL_MAP.get("linkGridColumn");
                String fieldName = filed.getFieldName().toLowerCase();
                String fieldConfig = String.format(fieldTmpl, fieldName, filed.getFieldComment(), gridId + "_" + fieldName, fieldName);
                filed.setExt$Item("columnconfig", fieldConfig);
            }


            LcdpModuleTmplPageCompBean gridColumn = new LcdpModuleTmplPageCompBean();
            gridColumn.setId(StringUtils.randomUUID());
            gridColumn.setParentId(masterDataGrid.getId());
            gridColumn.setType("GridColumn");
            JSONObject columnConfig = JSON.parseObject(filed.getExt$Item("columnconfig"));
            columnConfig.put("editor", true);
            if (LcdpConstant.CREATE_COLUMN_LIST.contains(filed.getFieldName().toUpperCase())) {
                columnConfig.put("editor", false);
            }
            if (moduleTmplDTO.getModuleTmplId() != LcdpConstant.MODULE_TMPL_UP_DOWN_ID.longValue() || !StringUtils.equals(masterDataGrid.getPageType(), "EditPage")) {
                columnConfig.put("editor", false);
            }
            gridColumn.setConfig(columnConfig.toJSONString());
            gridColumn.setPageType(masterDataGrid.getPageType());
            gridColumnList.add(gridColumn);
        });
        if (StringUtils.equals("ChoosePage", masterDataGrid.getPageType())) {
            gridColumnList.forEach(column -> {
                JSONObject columnConfig = JSON.parseObject(column.getConfig());
                if (columnConfig.get("checkbox") != null && columnConfig.getBoolean("checkbox")) {
                    columnConfig.put("getSelectType__$S$__", moduleTmplDTO.getModuleName().toLowerCase() + "_choose_list_grid_selection_getselectiontype");
                    column.setConfig(columnConfig.toJSONString());
                }
            });
        }
        gridColumnList.forEach(column -> {
            JSONObject columnConfig = JSON.parseObject(column.getConfig());
            if (StringUtils.equals(columnConfig.getString("gridColumnType"), "checkbox") || StringUtils.equals(columnConfig.getString("gridColumnType"), "index")) {
                return;
            }
            JSONObject title = columnConfig.getJSONObject("title");
            title.put("type", "i18n");
            title.put("i18nCode", (moduleTmplDTO.getMasterTableName() + "." + columnConfig.getString("field")).toUpperCase());
            title.remove("en-US");
            columnConfig.put("title", title);
            column.setConfig(columnConfig.toJSONString());
        });


        List<String> gridColumnIdList = gridColumnList.stream().map(LcdpModuleTmplPageCompBean::getId).collect(Collectors.toList());
        //处理childrenWidgetId
        masterGridConfig.put("childrenWidgetId", gridColumnIdList.toArray());

        //将新增列添加到集合中
        newCompList.addAll(gridColumnList);

        masterGridConfig.put("id", gridId);

        String urlPath = moduleTmplDTO.getModulePath() + "server.Lcdp" + firstUppercase(moduleTmplDTO.getMasterTableName().replaceFirst("T_", "").toLowerCase())[1] + "Service";
        String gridUrl = String.format(masterGridConfig.getString("url"), urlPath);

        if (Constant.WORKFLOW_TYPE_TRADITION.equals(moduleTmplDTO.getBpFlag()) || Constant.WORKFLOW_TYPE_BUSINESS.equals(moduleTmplDTO.getBpFlag())) {
            String urlSuffix = "";

            switch (masterDataGrid.getPageType()) {
                case "EditPage":
                    urlSuffix = ".selectRawPagination";
                    break;
                case "AuditPage":
                    urlSuffix = ".selectAuditablePagination";
                    break;
                case "SearchPage":
                    urlSuffix = ".selectSearchablePagination";
                    break;
                case "ChoosePage":
                    urlSuffix = ".selectPaginationData";
                    break;
            }

            gridUrl = urlPath + urlSuffix;
        }

        masterGridConfig.put("url", gridUrl);

        String instantSavePath = urlPath + ".updateData";
        masterGridConfig.put("instantSavePath", instantSavePath);
        masterGridConfig.put("getRequestData__$S$__", "getGridRequestData");
        masterGridConfig.put("onRowActive__$S$__", "");
        masterGridConfig.put("onLoadSuccess__$S$__", "");
        masterGridConfig.put("confirmField", columnConfigList.stream().findFirst().get().getFieldName().toLowerCase());

        /**
         * 禅道号：20430642
         * 低代码自动生成前端页面时，生成查看业务工作流详情信息功能
         * 主要针对：(左右模板，上下模板) 编辑页面，审核页面，查询页面
         */
        if (Constant.WORKFLOW_TYPE_BUSINESS.equals(moduleTmplDTO.getBpFlag())) {
            List<Long> moduleTmplIdList = Arrays.asList(LcdpConstant.MODULE_TMPL_LEFT_RIGHT_ID, LcdpConstant.MODULE_TMPL_UP_DOWN_ID);
            List<String> pageTypeList = Arrays.asList("EditPage", "AuditPage", "SearchPage");

            if (moduleTmplIdList.contains(moduleTmplDTO.getModuleTmplId()) && pageTypeList.contains(masterDataGrid.getPageType())) {
                masterGridConfig.put("onCellClick__$S$__", "loadWfDetailPage");
            }
        }
        masterDataGrid.setConfig(dealGridConfig(masterGridConfig));
        moduleTmplDTO.setDependentWidgetId(gridId);
    }


    private void dealMasterForm(LcdpModuleTmplDTO moduleTmplDTO, List<LcdpModuleTmplPageCompBean> newCompList, List<LcdpTableFieldDTO> masterFieldList, LcdpModuleTmplPageCompBean formComp) {
        JSONObject formConfig = JSON.parseObject(formComp.getConfig());
        String formId = String.format(formConfig.getString("id"), moduleTmplDTO.getModuleName().toLowerCase());
        List<LcdpTableFieldDTO> fieldDTOList = buildFormFieldConfigList(formId, masterFieldList);
        List<LcdpModuleTmplPageCompBean> formFieldList = new ArrayList<>();
        fieldDTOList.forEach(fieldDTO -> {
            LcdpModuleTmplPageCompBean formField = new LcdpModuleTmplPageCompBean();
            formField.setId(StringUtils.randomUUID());
            formField.setParentId(formComp.getId());
            formField.setType(fieldDTO.getFieldType());
            formField.setConfig(fieldToAddValidateConfig(fieldDTO, "field"));
            JSONObject fieldConfig = JSON.parseObject(formField.getConfig());
            if (LcdpConstant.CREATE_COLUMN_LIST.contains(fieldDTO.getFieldName().toUpperCase())) {
                fieldConfig.put("readonly", true);
                formField.setConfig(fieldConfig.toJSONString());
            }
            JSONObject title = fieldConfig.getJSONObject("title");
            title.put("type", "i18n");
            title.put("i18nCode", (moduleTmplDTO.getMasterTableName() + "." + fieldConfig.getString("field")).toUpperCase());
            title.remove("en-US");
            fieldConfig.put("title", title);
            formField.setConfig(fieldConfig.toJSONString());
            formField.setPageType(formComp.getPageType());
            formFieldList.add(formField);
        });
        newCompList.addAll(formFieldList);
        List<String> formFieldIdList = formFieldList.stream().map(LcdpModuleTmplPageCompBean::getId).collect(Collectors.toList());
        formConfig.put("childrenWidgetId", formFieldIdList.toArray());

        formConfig.put("id", formId);

        String urlPath = moduleTmplDTO.getModulePath() + "server.Lcdp" + firstUppercase(moduleTmplDTO.getMasterTableName().replaceFirst("T_", "").toLowerCase())[1] + "Service";
        String formUrl = String.format(formConfig.getString("url"), urlPath);
        formConfig.put("url", formUrl);
        String formInstantSavePath = String.format(formConfig.getString("instantSavePath"), urlPath);
        formConfig.put("instantSavePath", formInstantSavePath);
        if (LcdpConstant.MODULE_TMPL_LEFT_RIGHT_ID.longValue() == moduleTmplDTO.getModuleTmplId()) {
            formConfig.put("dependentWidgetId", moduleTmplDTO.getDependentWidgetId());
            formConfig.put("onUpdated__$S$__", "onUpdateFormData");
        }

        formComp.setConfig(dealFormConfig(formConfig));
        if (StringUtils.isEmpty(moduleTmplDTO.getDependentWidgetId())) {
            moduleTmplDTO.setDependentWidgetId(formId);
        }
    }


    private List<LcdpTableFieldDTO> buildFormFieldConfigList(String moduleName, List<LcdpTableFieldDTO> masterFieldList) {
        for (LcdpTableFieldDTO tableFieldDTO : masterFieldList) {
            String fieldType = tableFieldDTO.getFieldType();
            if ("varchar".equalsIgnoreCase(fieldType) || "clob".equalsIgnoreCase(fieldType)) {
                tableFieldDTO.setFieldType("text");
            }

            tableFieldDTO.setFieldType(firstUppercase(tableFieldDTO.getFieldType())[1]);

            String fieldTmpl = LcdpModuleTempHelper.FIELD_TMPL_MAP.get(tableFieldDTO.getFieldType());
            String fieldName = tableFieldDTO.getFieldName().toLowerCase();

            String fieldConfig = String.format(fieldTmpl, fieldName, JsonUtils.escapeJsonValue(tableFieldDTO.getFieldComment()), fieldName, moduleName + "_" + fieldName);
            tableFieldDTO.setExt$Item("fieldconfig", fieldConfig);
        }
        return masterFieldList;
    }

    private List<LcdpTableFieldDTO> buildGridColumnConfigList(String moduleName, List<LcdpTableFieldDTO> masterFieldList) {
        for (LcdpTableFieldDTO tableFieldDTO : masterFieldList) {
            String fieldType = tableFieldDTO.getFieldType();
            if ("varchar".equalsIgnoreCase(fieldType) || "clob".equalsIgnoreCase(fieldType) || "number".equalsIgnoreCase(fieldType)) {
                tableFieldDTO.setFieldType("text");
            }
            String fieldTmpl = LcdpModuleTempHelper.FIELD_TMPL_MAP.get(tableFieldDTO.getFieldType() + "GridColumn");
            String fieldName = tableFieldDTO.getFieldName().toLowerCase();
            String fieldConfig = String.format(fieldTmpl, fieldName, JsonUtils.escapeJsonValue(tableFieldDTO.getFieldComment()), moduleName + "_" + fieldName, fieldName);
            tableFieldDTO.setExt$Item("columnconfig", fieldConfig);
        }
        return masterFieldList;
    }

    private List<LcdpModuleTmplPageCompBean> bulidCommonColumns(String parentId, String pageType) {
        List<LcdpModuleTmplPageCompBean> gridColumnList = new ArrayList<>();
        LcdpModuleTmplPageCompBean checkBox = new LcdpModuleTmplPageCompBean();
        checkBox.setId(StringUtils.randomUUID());
        checkBox.setParentId(parentId);
        checkBox.setType("GridColumn");
        checkBox.setPageType(pageType);
        checkBox.setConfig(LcdpModuleTempHelper.FIELD_TMPL_MAP.get("checkbox"));

        LcdpModuleTmplPageCompBean index = new LcdpModuleTmplPageCompBean();
        index.setId(StringUtils.randomUUID());
        index.setParentId(parentId);
        index.setType("GridColumn");
        index.setPageType(pageType);
        index.setConfig(LcdpModuleTempHelper.FIELD_TMPL_MAP.get("index"));


        gridColumnList.add(checkBox);
        gridColumnList.add(index);

        return gridColumnList;
    }

    private String generatePageContent(String pageType, LcdpModuleTmplDTO moduleTmplDTO) {
        //模板引擎
        TemplateEngine engine = new TemplateEngine();
        //读取磁盘中的模板文件
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        //路径
        if (LcdpConstant.MODULE_TMPL_LEFT_RIGHT_ID.longValue() == moduleTmplDTO.getModuleTmplId()) {
            resolver.setPrefix("thymeleaf/left-right/");
        } else if (LcdpConstant.MODULE_TEPL_PENETRATION.longValue() == moduleTmplDTO.getModuleTmplId()) {
            resolver.setPrefix("thymeleaf/penetration/");
            moduleTmplDTO.setDetailPagePath(moduleTmplDTO.getModulePath() + "page." + moduleTmplDTO.getModuleName() + "DetailPage");
        } else {
            resolver.setPrefix("thymeleaf/up-down/");
        }
        //后缀
        resolver.setSuffix(".js");
        //设置模板模式、默认是HTML
        resolver.setTemplateMode("TEXT");
        resolver.setCharacterEncoding("UTF-8");
        //设置引擎使用 resolve
        engine.setTemplateResolver(resolver);
        //准备数据 使用context
        Context context = new Context();
        //添加基本类型
        //准备数据
        LcdpModuleTmplDTO dealDTO = new LcdpModuleTmplDTO();
        BeanUtils.copyProperties(moduleTmplDTO, dealDTO);

        dealDTO.setMasterTableName(dealDTO.getMasterTableName().toLowerCase().replaceAll("_", "-"));

        List<LcdpModuleTmplChildTableDTO> childTableList = dealDTO.getChildTableList();
        if (!childTableList.isEmpty()) {
            for (LcdpModuleTmplChildTableDTO child : childTableList) {
                child.setScriptPath(moduleTmplDTO.getModulePath() + "server.Lcdp" + firstUppercase(child.getTableName().replaceFirst("T_", "").toLowerCase())[1] + "Service");
                child.setCompName(child.getTableName().toLowerCase().replaceFirst("t_", "").replaceAll("_", "-"));
                child.setFunctionName(firstUppercase(child.getTableName().replaceFirst("T_", "").toLowerCase())[1]);
                child.setAssociatedField(child.getAssociatedField().toLowerCase());
                child.setChildUrlName("Lcdp" + firstUppercase(child.getTableName().replaceFirst("T_", "").toLowerCase())[1]);
            }
        }
        dealDTO.setUploaderFlag(Constant.YES.equals(dealDTO.getUploaderFlag()) ? "true" : "false");
        dealDTO.setPreInsertFlag(Constant.YES.equals(dealDTO.getPreInsertFlag()) ? "true" : "false");
        dealDTO.setModuleName(dealDTO.getModuleName().toLowerCase());
        String masterTableName = moduleTmplDTO.getMasterTableName();
        LcdpTableDTO tableDTO = lcdpTableService.selectPhysicalTableInfo(masterTableName);
        LcdpTableFieldDTO preInsertDTO = tableDTO.getFieldList().size() > 1 ? tableDTO.getFieldList().stream().skip(1).findFirst().get() : tableDTO.getFieldList().stream().findFirst().get();
        LcdpTableFieldDTO linkDTO = tableDTO.getFieldList().size() > 1 ? tableDTO.getFieldList().stream().skip(1).findFirst().get() : tableDTO.getFieldList().stream().findFirst().get();
        linkDTO.setFieldName(linkDTO.getFieldName().toLowerCase());
        context.setVariable("moduleTmpl", dealDTO);
        context.setVariable("preInsert", preInsertDTO);
        context.setVariable("masterTableName", moduleTmplDTO.getMasterTableName());
        context.setVariable("link", linkDTO);
        if (LcdpConstant.MODULE_TMPL_LEFT_RIGHT_ID.longValue() == moduleTmplDTO.getModuleTmplId()) {
            context.setVariable("preInsertFormPath", dealDTO.getModulePath() + "page." + dealDTO.getResourceName() + pageType);
        } else {
            context.setVariable("preInsertFormPath", dealDTO.getModulePath() + "page." + dealDTO.getResourceName() + "DetailPage");

        }
        String out = engine.process(pageType, context);
        return out;
    }

    /**
     * newName :传入的字符串如果有"_",把下一个字符转化为大写 firstNewName
     * :传入的字符串如果有"_",把下一个字符转化为大写,第一个字符大写
     */
    private String[] firstUppercase(String tableName) {
        String newName = "";
        String firstNewName = "";
        for (int a = 0; a < tableName.length(); a++) {
            char chr = tableName.charAt(a);
            if (a == 0) {
                newName = newName + chr;
                firstNewName = firstNewName + String.valueOf(chr).toUpperCase();
            } else if ("_".equals(String.valueOf(chr))) {
                newName = newName + String.valueOf(tableName.charAt(a + 1)).toUpperCase();
                firstNewName = firstNewName + String.valueOf(tableName.charAt(a + 1)).toUpperCase();
                a++;
            } else {
                newName = newName + chr;
                firstNewName = firstNewName + chr;
            }
        }
        String[] arry = {newName, firstNewName};
        return arry;
    }

    /**
     * 因做出调整而导致Gird发生变化，应对调整 后期可将调整优化到数据库中避免产生资源消耗
     */
    private String dealGridConfig(JSONObject gridConfig) {
        gridConfig.put("sort", true);
        gridConfig.put("page", true);
        gridConfig.put("serverSearch", true);
        gridConfig.put("cellValueFillByDrag", false);
        gridConfig.put("showCheckedBadgeGt", 1);
        gridConfig.put("contextMenuConfig", false);
        gridConfig.put("showCheckedNum", true);
        gridConfig.remove("filterOpen");
        gridConfig.remove("toolbarAlign");
        gridConfig.remove("editorInvisible");
        gridConfig.remove("columnsFill");
        gridConfig.remove("contentAlign");
        gridConfig.remove("checkOnActive");
        gridConfig.remove("checkOneOnActive");
        gridConfig.remove("scroll");
        gridConfig.remove("pageSize");
        gridConfig.remove("pageList");
        gridConfig.remove("loadingMode");
        gridConfig.remove("badgeOverflowCount");
        gridConfig.remove("checkContinuous");
        return gridConfig.toJSONString();
    }

    /**
     * 因做出调整而导致Form发生变化，应对调整 后期可将调整优化到数据库中避免产生资源消耗
     */
    private String dealFormConfig(JSONObject formConfig) {
        formConfig.put("layout", "inline");
        formConfig.put("titleAlign", "right");
        return formConfig.toJSONString();
    }

    private List<LcdpResourceBean> createByCustomModuleTmpl(LcdpModuleTmplDTO moduleTmplDTO, LcdpResourceBean module) {
        moduleTmplDTO.setResourceId(module.getId());
        LcdpResourceBean category = resourceService.selectById(module.getParentId());
        Long moduleTmplId = moduleTmplDTO.getModuleTmplId();

        moduleTmplDTO.setModuleName(module.getResourceName());
        moduleTmplDTO.setCategoryName(category.getResourceName());
        moduleTmplDTO.setModulePath(category.getResourceName() + "." + module.getResourceName() + ".");

        List<LcdpCustomTmplResourceBean> customTmplResourceList = customTmplResourceService.selectListByFilter(SearchFilter.instance().match("MODULETMPLID", moduleTmplId).filter(MatchPattern.EQ));

        List<LcdpCustomTmplResourceBean> viewList = customTmplResourceList.stream().filter(resource -> LcdpConstant.RESOURCE_CATEGORY_VIEW.equals(resource.getResourceCategory())).collect(Collectors.toList());

        List<Long> viewIdList = viewList.stream().map(LcdpCustomTmplResourceBean::getId).collect(Collectors.toList());

        List<LcdpCustomTmplPageCompBean> customTmplPageCompList = customTmplPageCompService.selectListByFilter(SearchFilter.instance().match("CUSTOMTMPLPAGEID", viewIdList).filter(MatchPattern.OR));

        Map<Long, List<LcdpCustomTmplPageCompBean>> tmplPageId2PageCompList = customTmplPageCompList.stream().collect(Collectors.groupingBy(LcdpCustomTmplPageCompBean::getCustomTmplPageId));

        List<LcdpResourceBean> resourceList = new ArrayList<>();//存储新增的resource

        List<LcdpResourceHistoryBean> resourceHistoryList = new ArrayList<>(); //存储新增的resourceHistory

        List<LcdpModulePageCompBean> modulePageCompList = new ArrayList<>(); //存储新增的modulePageComp

        Map<Long, Long> id2Map = new HashMap<>();

        Map<Long, String> sourceId2PathMap = new HashMap<>();

        viewList.stream().forEach(customResource -> {
            LcdpResourceBean resource = new LcdpResourceBean();
            BeanUtils.copyProperties(customResource, resource, LcdpConstant.COPY_IGNORE_PROPERTIES);
            resource.setId(ApplicationContextHelper.getNextIdentity());
            sourceId2PathMap.put(resource.getId(), resource.getPath());
            id2Map.put(customResource.getId(), resource.getId());
            resource.setParentId(module.getId());
            resource.setResourceName(resource.getResourceName().replaceFirst(customResource.getModuleName(), module.getResourceName()));
            resource.setClassName(null);
            resource.setEffectVersion(null);
            EntityHelper.assignCreatedElement(resource);

            resourceService.updatePath(resource);
            resourceService.updateVersionOffset(resource);

            resourceList.add(resource);

            LcdpResourceHistoryBean resourceHistory = new LcdpResourceHistoryBean();
            BeanUtils.copyProperties(resource, resourceHistory);
            resourceHistory.setResourceId(resource.getId());
            resourceHistory.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_NO);
            resourceHistory.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);
            resourceHistory.setVersion(1L);
            resourceHistory.setModifyVersion(1L);
            EntityHelper.assignCreatedElement(resource);
            resourceHistoryList.add(resourceHistory);

            List<LcdpCustomTmplPageCompBean> sourceCustomTmplPageCompList = tmplPageId2PageCompList.get(customResource.getId());
            if (!sourceCustomTmplPageCompList.isEmpty()) {
                List<LcdpModulePageCompBean> originalModulePageCompList = sourceCustomTmplPageCompList.stream().map(source -> {
                    LcdpModulePageCompBean modulePageComp = new LcdpModulePageCompBean();
                    BeanUtils.copyProperties(source, modulePageComp);
                    modulePageComp.setModulePageId(resource.getId());
                    modulePageComp.setModulePageHistoryId(resourceHistory.getId());
                    return modulePageComp;
                }).collect(Collectors.toList());

                List<LcdpModulePageCompBean> newModulePageCompList = resourceService.generateModulePageComps(originalModulePageCompList);

                modulePageCompList.addAll(newModulePageCompList);
            }
        });

        List<String> tableNameList = moduleTmplDTO.getTableNameList();

        tableNameList.forEach(tableName -> {
            LcdpResourceBean javaResource = new LcdpResourceBean();
            javaResource.setId(ApplicationContextHelper.getNextIdentity());
            javaResource.setResourceName("Lcdp" + firstUppercase(tableName.replaceFirst("T_", "").toLowerCase())[1] + "Service");
            javaResource.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_JAVA);
            javaResource.setParentId(module.getId());

            resourceService.updatePath(javaResource);
            resourceService.updateVersionOffset(javaResource);

            resourceList.add(javaResource);

            LcdpConstant.MAPPER_TMPL_NAME_LIST.forEach(mapperName -> {
                if (LcdpScriptUtils.validateCurrentDBMybatisMapper()
                        && !StringUtils.containsIgnoreCase(mapperName, current_environment_database)) {
                    return;
                }

                LcdpResourceBean mapperResource = new LcdpResourceBean();
                mapperResource.setId(ApplicationContextHelper.getNextIdentity());
                mapperResource.setResourceName("Lcdp" + firstUppercase(tableName.replaceFirst("T_", "").toLowerCase())[1] + mapperName);
                mapperResource.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_MAPPER);
                mapperResource.setParentId(module.getId());

                resourceService.updatePath(mapperResource);

                resourceList.add(mapperResource);

                LcdpResourceHistoryBean mapperHistory = new LcdpResourceHistoryBean();
                mapperHistory.setId(ApplicationContextHelper.getNextIdentity());
                mapperHistory.setResourceName(mapperResource.getResourceName());
                mapperHistory.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_MAPPER);
                mapperHistory.setPath(mapperResource.getPath());
                mapperHistory.setResourceId(mapperResource.getId());
                mapperHistory.setVersion(1L);
                mapperHistory.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_NO);
                mapperHistory.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);
                mapperHistory.setModifyVersion(null);
                mapperHistory.setContent(LcdpScriptTemplateHelper.generateMapper(mapperResource.getPath(), tableName, mapperName, moduleTmplDTO.getBpFlag()));
                resourceHistoryList.add(mapperHistory);
            });

            LcdpResourceHistoryBean javaHistory = new LcdpResourceHistoryBean();
            BeanUtils.copyProperties(javaResource, javaHistory);
            javaHistory.setId(ApplicationContextHelper.getNextIdentity());
            javaHistory.setResourceId(javaResource.getId());
            javaHistory.setVersion(1L);
            javaHistory.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_NO);
            javaHistory.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);
            String content = LcdpScriptTemplateHelper.generateJavaScript(javaResource.getPath(),
                    javaHistory.getResourceName(),
                    tableName,
                    StringUtils.replaceLast(javaHistory.getPath().replace("server.", "mapper."), "Service", "Mapper"),
                    null);
            javaHistory.setContent(content);
            String classContent = LcdpJavaCodeResolverUtils.getClassContent(content,
                    javaHistory.getResourceName(),
                    1L, 1L,
                    javaHistory.getVersionOffset());
            javaHistory.setClassContent(classContent);
            resourceHistoryList.add(javaHistory);
        });

        resourceService.getDao().fastInsert(resourceList);
        resourceHistoryService.getDao().fastInsert(resourceHistoryList);
        modulePageCompService.getDao().fastInsert(modulePageCompList);
        List<LcdpResourceHistoryBean> javaHistoryList = resourceHistoryList.stream()
                .filter(resource -> LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(resource.getResourceCategory())).collect(Collectors.toList());

        // 加载并注册到spring容器中
        LcdpJavaCodeResolverUtils.loadAndRegisterResourceHistoryList(javaHistoryList);

        // 更新脚本方法表
        LcdpJavaCodeResolverUtils.updateDevScriptMethod(javaHistoryList);

        // 加载mapper
        List<LcdpResourceHistoryBean> mapperHistoryList = resourceHistoryList.stream()
                .filter(r -> LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(r.getResourceCategory())).collect(Collectors.toList());
        mapperHistoryList.forEach(h -> LcdpMapperUtils.loadMapper(h.getPath(), false, h.getContent()));

        // 同步
        resourceList.forEach(r -> LcdpResourceSyncManager.sync(r.getId()));

        return resourceList;
    }

    private List<LcdpResourceBean> createBySysModuleTmpl(LcdpModuleTmplDTO moduleTmplDTO, LcdpResourceBean module) {
        moduleTmplDTO.setResourceId(module.getId());
        LcdpResourceBean category = resourceService.selectById(module.getParentId());

        //获取moduleTmplDTO以及赋值为后面的使用铺垫
        Long moduleTmplId = moduleTmplDTO.getModuleTmplId();

        moduleTmplDTO.setModuleName(module.getResourceName());
        moduleTmplDTO.setCategoryName(category.getResourceName());
        moduleTmplDTO.setModulePath(category.getResourceName() + "." + module.getResourceName() + ".");
        moduleTmplDTO.setMasterScriptPath(category.getResourceName() + "." + module.getResourceName() + ".server." + "Lcdp" + firstUppercase(moduleTmplDTO.getMasterTableName().replaceFirst("T_", "").toLowerCase())[1] + "Service");

        String bpFlag = moduleTmplDTO.getBpFlag();
        // 业务工作流相关页面资源配置同传统工作流
        if (Constant.WORKFLOW_TYPE_BUSINESS.equals(bpFlag)) {
            bpFlag = Constant.WORKFLOW_TYPE_TRADITION;
        }

        String uploaderFlag = moduleTmplDTO.getUploaderFlag();
        //获取子表信息
        List<LcdpModuleTmplChildTableDTO> childTableList = moduleTmplDTO.getChildTableList();

        List<String> childTableNameList = childTableList.stream().map(LcdpModuleTmplChildTableDTO::getTableName).collect(Collectors.toList());

        List<LcdpTableDTO> childTableDTOList = lcdpTableService.selectPhysicalTableInfoList(childTableNameList);

        //拿到页面配置表数据
        List<LcdpModuleTmplConfigBean> moduleTmplConfigList = lcdpModuleTmplConfigService.selectListByFilter(SearchFilter.instance().match("MODULETMPLID", moduleTmplId).filter(MatchPattern.EQ).match("BPFLAG", bpFlag).filter(MatchPattern.EQ).match("UPLOADERFLAG", uploaderFlag).filter(MatchPattern.EQ));

        LcdpModuleTmplConfigBean moduleTmplConfig = moduleTmplConfigList.get(0);

        //根据页面配置表ID查询资源表数据
        SearchFilter filter = SearchFilter.instance();
        List<Long> moduleTmplConfigIdList = new ArrayList<>();
        moduleTmplConfigIdList.add(moduleTmplConfig.getId());
        if (Constant.YES.equals(moduleTmplDTO.getChooseFlag())) {
            moduleTmplConfigIdList.add(LcdpConstant.MODULE_TMPL_PUBLIC_CHOOSE_PAGE_ID);
        }
        filter.match("MODULETMPLCONFIGID", moduleTmplConfigIdList).filter(MatchPattern.OR);

        List<LcdpModuleTmplResourceBean> moduleTmplResourceList = lcdpModuleTmplResourceService.selectListByFilter(filter);

        //创建页面组件数据
        List<LcdpModuleTmplPageCompBean> moduleTmplPageCompList = generatePageComp(moduleTmplResourceList, moduleTmplDTO, childTableDTOList);

        //创建资源
        return createResource(moduleTmplDTO, moduleTmplPageCompList);
    }

    private List<LcdpResourceBean> createResource(LcdpModuleTmplDTO moduleTmplDTO, List<LcdpModuleTmplPageCompBean> moduleTmplPageCompList) {
        List<LcdpResourceBean> resourceList = new ArrayList<>();

        List<LcdpResourceHistoryBean> javaHistoryList = new ArrayList<>();

        List<LcdpResourceHistoryBean> resourceHistoryList = new ArrayList<>();

        List<LcdpModulePageCompBean> modulePageCompList = new ArrayList<>();

        List<LcdpPageI18nCodeBean> pageI18nCodeList = new ArrayList<>();
        //插入页面
        Map<String, List<LcdpModuleTmplPageCompBean>> pageTypeMap = moduleTmplPageCompList.stream().collect(Collectors.groupingBy(LcdpModuleTmplPageCompBean::getPageType));
        pageTypeMap.forEach((pageType, compList) -> {
            LcdpResourceBean page = new LcdpResourceBean();
            page.setId(ApplicationContextHelper.getNextIdentity());
            page.setParentId(moduleTmplDTO.getResourceId());
            page.setResourceName(moduleTmplDTO.getResourceName() + pageType);
            if (!StringUtils.isEmpty(moduleTmplDTO.getResourceDesc())) {
                page.setResourceDesc(moduleTmplDTO.getResourceDesc() + I18nHelper.getMessage(LcdpConstant.PAGE_TYPE_TO_PAGE_DESC_SUFFIX_MAP.get(pageType)));
            }
            page.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_VIEW);
            EntityHelper.assignCreatedElement(page);

            resourceService.updatePath(page);

            resourceList.add(page);

            LcdpResourceHistoryBean pageHistory = new LcdpResourceHistoryBean();
            BeanUtils.copyProperties(page, pageHistory);
            pageHistory.setId(ApplicationContextHelper.getNextIdentity());
            pageHistory.setResourceId(page.getId());
            pageHistory.setVersion(1L);
            pageHistory.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);
            pageHistory.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_NO);
            pageHistory.setContent(generatePageContent(pageType, moduleTmplDTO));
            resourceHistoryList.add(pageHistory);

            List<LcdpModulePageCompBean> tempPageCompList = compList.stream().map(comp -> {
                LcdpModulePageCompBean pageComp = new LcdpModulePageCompBean();
                BeanUtils.copyProperties(comp, pageComp);
                pageComp.setModulePageId(page.getId());
                pageComp.setModulePageHistoryId(pageHistory.getId());
                pageComp.setModulePageVersion(1L);
                return pageComp;
            }).collect(Collectors.toList());
            List<LcdpModulePageCompBean> pageCompList = resourceService.generateModulePageComps(tempPageCompList);
            List<LcdpPageI18nCodeBean> pageUseI18nCodeList = new ArrayList<>();
            for (LcdpModulePageCompBean pageComp : pageCompList) {
                JSONObject configObject = JSON.parseObject(pageComp.getConfig());
                JSONObject title = configObject.getJSONObject("title");
                JSONObject text = configObject.getJSONObject("text");
                if (!ObjectUtils.isEmpty(title)) {
                    if (StringUtils.equals(title.getString("type"), "i18n")) {
                        LcdpPageI18nCodeBean pageI18nCode = new LcdpPageI18nCodeBean();
                        pageI18nCode.setId(ApplicationContextHelper.getNextIdentity());
                        pageI18nCode.setModulePageId(pageComp.getModulePageId());
                        pageI18nCode.setModulePageHistoryId(pageComp.getModulePageHistoryId());
                        pageI18nCode.setCode(title.getString("i18nCode"));
                        pageUseI18nCodeList.add(pageI18nCode);
                    }
                }

                if (!ObjectUtils.isEmpty(text)) {
                    if (StringUtils.equals(text.getString("type"), "i18n")) {
                        LcdpPageI18nCodeBean pageI18nCode = new LcdpPageI18nCodeBean();
                        pageI18nCode.setId(ApplicationContextHelper.getNextIdentity());
                        pageI18nCode.setModulePageId(pageComp.getModulePageId());
                        pageI18nCode.setModulePageHistoryId(pageComp.getModulePageHistoryId());
                        pageI18nCode.setCode(text.getString("i18nCode"));
                        pageUseI18nCodeList.add(pageI18nCode);
                    }
                }
                configObject.put("versionCompare", StringUtils.randomUUID(18));
                pageComp.setConfig(configObject.toJSONString());
            }

            pageUseI18nCodeList = pageUseI18nCodeList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(LcdpPageI18nCodeBean::getCode))), ArrayList::new));
            pageI18nCodeList.addAll(pageUseI18nCodeList);

            modulePageCompList.addAll(pageCompList);
        });


        //先插入主表脚本
        LcdpResourceBean javaScript = new LcdpResourceBean();
        javaScript.setId(ApplicationContextHelper.getNextIdentity());
        javaScript.setParentId(moduleTmplDTO.getResourceId());
        javaScript.setResourceDesc(moduleTmplDTO.getResourceDesc());
        javaScript.setResourceName("Lcdp" + firstUppercase(moduleTmplDTO.getMasterTableName().replaceFirst("T_", "").toLowerCase())[1] + "Service");
        javaScript.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_JAVA);
        javaScript.setScriptType(ServerScriptType.service.name());
        EntityHelper.assignCreatedElement(javaScript);

        resourceService.updatePath(javaScript);
        resourceService.updateVersionOffset(javaScript);

        LcdpResourceHistoryBean javaScriptHistory = new LcdpResourceHistoryBean();
        BeanUtils.copyProperties(javaScript, javaScriptHistory);
        javaScriptHistory.setId(ApplicationContextHelper.getNextIdentity());
        javaScriptHistory.setResourceId(javaScript.getId());
        javaScriptHistory.setVersion(1L);
        javaScriptHistory.setCompiledVersion(1L);
        javaScriptHistory.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);
        javaScriptHistory.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_NO);

        String content = LcdpScriptTemplateHelper.generateJavaScript(javaScript.getPath(),
                javaScriptHistory.getResourceName(),
                moduleTmplDTO.getMasterTableName(),
                StringUtils.replaceLast(javaScriptHistory.getPath().replace("server.", "mapper."), "Service", "Mapper"),
                moduleTmplDTO.getBpFlag());

        //禅道60448310
        content = handleSequence(content, moduleTmplDTO);

        javaScriptHistory.setContent(content);
        //新增时JAVA类代码类名替换为：类名+v(生效版本)+m(修改版本)
        String classContent = LcdpJavaCodeResolverUtils.getClassContent(content,
                javaScriptHistory.getResourceName(),
                1L, 1L,
                javaScriptHistory.getVersionOffset());

        javaScriptHistory.setClassContent(classContent);

        EntityHelper.assignCreatedElement(javaScript);
        resourceList.add(javaScript);
        resourceHistoryList.add(javaScriptHistory);
        javaHistoryList.add(javaScriptHistory);
        //处理子表
        for (LcdpModuleTmplChildTableDTO childTableDTO : moduleTmplDTO.getChildTableList()) {
            LcdpResourceBean childJavaScript = new LcdpResourceBean();
            childJavaScript.setId(ApplicationContextHelper.getNextIdentity());
            childJavaScript.setParentId(moduleTmplDTO.getResourceId());
            childJavaScript.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_JAVA);
            childJavaScript.setResourceName("Lcdp" + firstUppercase(childTableDTO.getTableName().replaceFirst("T_", "").toLowerCase())[1] + "Service");
            childJavaScript.setResourceDesc(moduleTmplDTO.getResourceDesc());
            EntityHelper.assignCreatedElement(childJavaScript);

            resourceService.updatePath(childJavaScript);

            childJavaScript.setScriptType(ServerScriptType.service.name());

            resourceList.add(childJavaScript);

            LcdpResourceHistoryBean childJavaScriptHistory = new LcdpResourceHistoryBean();
            BeanUtils.copyProperties(childJavaScript, childJavaScriptHistory);
            childJavaScriptHistory.setId(ApplicationContextHelper.getNextIdentity());
            childJavaScriptHistory.setResourceId(childJavaScript.getId());
            childJavaScriptHistory.setVersion(1L);
            childJavaScriptHistory.setCompiledVersion(1L);
            childJavaScriptHistory.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);
            childJavaScriptHistory.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_NO);
            String childContent = LcdpScriptTemplateHelper.generateJavaScript(javaScript.getPath(),
                    childJavaScriptHistory.getResourceName(),
                    childTableDTO.getTableName(),
                    StringUtils.replaceLast(childJavaScriptHistory.getPath().replace("server.", "mapper."), "Service", "Mapper"),
                    null);
            childJavaScriptHistory.setContent(childContent);
            javaScriptHistory.setContent(content);
            //新增时JAVA类代码类名替换为：类名+v(生效版本)+m(修改版本)
            String childClassContent = childContent.replaceFirst(LcdpConstant.CLASS_KEY_EXPR + childJavaScriptHistory.getResourceName(), LcdpConstant.CLASS_KEY + StringUtils.capitalize(moduleTmplDTO.getModuleName()) + childJavaScriptHistory.getResourceName() + "v1m1");

            childJavaScriptHistory.setClassContent(childClassContent);

            EntityHelper.assignCreatedElement(childJavaScriptHistory);

            javaHistoryList.add(childJavaScriptHistory);
            resourceHistoryList.add(childJavaScriptHistory);
        }

        ////////
        //插入mapper
        LcdpConstant.MAPPER_TMPL_NAME_LIST.forEach(mapperName -> {
            if (LcdpScriptUtils.validateCurrentDBMybatisMapper()
                    && !StringUtils.containsIgnoreCase(mapperName, current_environment_database)) {
                return;
            }

            LcdpResourceBean mapper = new LcdpResourceBean();
            mapper.setId(ApplicationContextHelper.getNextIdentity());
            mapper.setResourceName("Lcdp" + firstUppercase(moduleTmplDTO.getMasterTableName().replaceFirst("T_", "").toLowerCase())[1] + mapperName);
            mapper.setResourceDesc(moduleTmplDTO.getResourceDesc());
            mapper.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_MAPPER);
            mapper.setParentId(moduleTmplDTO.getResourceId());
            EntityHelper.assignCreatedElement(mapper);

            resourceService.updatePath(mapper);

            resourceList.add(mapper);

            LcdpResourceHistoryBean mapperHistory = new LcdpResourceHistoryBean();
            mapperHistory.setId(ApplicationContextHelper.getNextIdentity());
            mapperHistory.setResourceName(mapper.getResourceName());
            mapperHistory.setResourceDesc(moduleTmplDTO.getResourceDesc());
            mapperHistory.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_MAPPER);
            mapperHistory.setPath(mapper.getPath());
            mapperHistory.setResourceId(mapper.getId());
            mapperHistory.setVersion(1L);
            mapperHistory.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_NO);
            mapperHistory.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);
            mapperHistory.setModifyVersion(null);
            mapperHistory.setContent(LcdpScriptTemplateHelper.generateMapper(mapper.getPath(), moduleTmplDTO.getMasterTableName(), mapperName, moduleTmplDTO.getBpFlag()));

            resourceHistoryList.add(mapperHistory);

            //处理子表
            for (LcdpModuleTmplChildTableDTO childTableDTO : moduleTmplDTO.getChildTableList()) {
                LcdpResourceBean childMapper = new LcdpResourceBean();
                childMapper.setId(ApplicationContextHelper.getNextIdentity());
                childMapper.setResourceName("Lcdp" + firstUppercase(childTableDTO.getTableName().replaceFirst("T_", "").toLowerCase())[1] + mapperName);
                childMapper.setResourceDesc(moduleTmplDTO.getResourceDesc());
                childMapper.setResourceCategory(LcdpConstant.RESOURCE_CATEGORY_MAPPER);
                childMapper.setParentId(moduleTmplDTO.getResourceId());
                EntityHelper.assignCreatedElement(childMapper);

                resourceService.updatePath(childMapper);

                resourceList.add(childMapper);

                LcdpResourceHistoryBean childMapperHistory = new LcdpResourceHistoryBean();
                BeanUtils.copyProperties(childMapper, childMapperHistory);
                childMapperHistory.setId(ApplicationContextHelper.getNextIdentity());
                childMapperHistory.setResourceId(childMapper.getId());
                childMapperHistory.setVersion(1L);
                childMapperHistory.setEffectFlag(LcdpConstant.EFFECT_FLAG_NO);
                childMapperHistory.setSubmitFlag(LcdpConstant.SUBMIT_FLAG_NO);
                childMapperHistory.setContent(LcdpScriptTemplateHelper.generateMapper(childMapper.getPath(), childTableDTO.getTableName(), mapperName, null));

                EntityHelper.assignCreatedElement(childMapperHistory);

                resourceHistoryList.add(childMapperHistory);
            }
        });

        resourceService.getDao().fastInsert(resourceList);
        resourceHistoryService.getDao().fastInsert(resourceHistoryList);
        modulePageCompService.getDao().fastInsert(modulePageCompList);
        pageI18nCodeService.getDao().fastInsert(pageI18nCodeList);

        // 加载并注册到spring容器中
        LcdpJavaCodeResolverUtils.loadAndRegisterResourceHistoryList(javaHistoryList);

        // 更新脚本方法表
        LcdpJavaCodeResolverUtils.updateDevScriptMethod(javaHistoryList);

        // 加载mapper
        List<LcdpResourceHistoryBean> mapperHistoryList = resourceHistoryList.stream()
                .filter(r -> LcdpConstant.RESOURCE_CATEGORY_MAPPER.equals(r.getResourceCategory())).collect(Collectors.toList());
        mapperHistoryList.forEach(h -> LcdpMapperUtils.loadMapper(h.getPath(), false, h.getContent()));

        // 同步
        resourceList.forEach(r -> LcdpResourceSyncManager.sync(r.getId()));

        return resourceList;
    }

    private String handleSequence(String content, LcdpModuleTmplDTO moduleTmplDTO) {
        if (!StringUtils.isEmpty(moduleTmplDTO.getPattern()) && !StringUtils.isEmpty(moduleTmplDTO.getColumn())) {
            String prefix = moduleTmplDTO.getPrefix() == null ? "" : moduleTmplDTO.getPrefix();
            String codeToInsert =
                    "        list.stream().forEach(e->{\n" +
                            "            e.put(\"" + moduleTmplDTO.getColumn() + "\", \"" + prefix + "\" + ApplicationContextHelper.getNextSequence(getTable()));\n" +
                            "        });";

            Pattern pattern = Pattern.compile("(.*List<Map<String, Object>> list = LcdpWrapperParseUtils.parseList\\(wrapper\\);)(\\s*)(\\n)");
            Matcher matcher = pattern.matcher(content);

            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String replacement = matcher.group(1) + "\n" + codeToInsert + matcher.group(2) + matcher.group(3);
                matcher.appendReplacement(sb, replacement);
            }

            matcher.appendTail(sb);
            return sb.toString();
        }

        return content;
    }
}
