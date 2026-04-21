package com.sunwayworld.cloud.module.lcdp.appmarket.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.appmarket.bean.LcdpAppMarketDTO;
import com.sunwayworld.cloud.module.lcdp.appmarket.bean.LcdpAppMarketTableDTO;
import com.sunwayworld.cloud.module.lcdp.appmarket.service.LcdpAppMarketService;
import com.sunwayworld.cloud.module.lcdp.base.service.LcdpBaseService;
import com.sunwayworld.cloud.module.lcdp.checkoutrecord.bean.LcdpCheckoutRecordBean;
import com.sunwayworld.cloud.module.lcdp.importrecord.bean.LcdpResourceImportRecordBean;
import com.sunwayworld.cloud.module.lcdp.importrecord.service.LcdpResourceImportRecordService;
import com.sunwayworld.cloud.module.lcdp.resource.bean.*;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpJavaCodeResolverUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpScriptUtils;
import com.sunwayworld.cloud.module.lcdp.submitlog.bean.LcdpSubmitLogBean;
import com.sunwayworld.cloud.module.lcdp.submitlog.service.LcdpSubmitLogService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableBean;
import com.sunwayworld.cloud.module.lcdp.table.bean.LcdpTableDTO;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpDatabaseService;
import com.sunwayworld.cloud.module.lcdp.table.service.LcdpTableService;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.database.context.instance.EntityHelper;
import com.sunwayworld.framework.exception.checked.CheckedException;
import com.sunwayworld.framework.http.HttpClientManager;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.io.file.FilePathDTO;
import com.sunwayworld.framework.io.file.path.FilePathService;
import com.sunwayworld.framework.jdk.core.ClassManager;
import com.sunwayworld.framework.mybatis.mapper.MapperParameter;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.utils.*;
import com.sunwayworld.module.admin.config.bean.CoreAdminSelectConfigBean;
import com.sunwayworld.module.item.file.bean.CoreFileBean;
import com.sunwayworld.module.item.file.utils.CoreFileUtils;
import com.sunwayworld.module.sys.code.bean.CoreCodeBean;
import com.sunwayworld.module.sys.code.bean.CoreCodeCategoryBean;
import com.sunwayworld.module.sys.i18n.bean.CoreI18nMessageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Repository
public class LcdpAppMarketServiceImpl implements LcdpAppMarketService {
    private String current_environment_database = LcdpConstant.PROFILE_DB_LIST.stream().filter(profile -> ApplicationContextHelper.isProfileActivated(profile)).findFirst().get();
    private static HttpClientManager httpClient = HttpClientManager.getInstance();

    @Autowired
    private LcdpResourceService resourceService;
    @Autowired
    private LcdpDatabaseService databaseService;
    @Autowired
    private LcdpTableService tableService;
    @Autowired
    private FilePathService filePathService;
    @Autowired
    private LcdpResourceImportRecordService resourceImportRecordService;
    @Autowired
    private LcdpSubmitLogService submitLogService;


    @Value("${sunway.appmarket.url:}")
    private String appMarketServerUrl;

    @Value("${sunway.appmarket.token:}")
    private String appMarketToken;

    @Override
    public Object getCodeList(String codeCategoryId) {
        checkAppMarketUrl(appMarketServerUrl);
        return httpClient.sendHttpGet(appMarketServerUrl + "/codes/" + codeCategoryId + "/queries", null);
    }

    @Override
    public Object selectPagination(RestJsonWrapperBean wrapper) {
        checkAppMarketUrl(appMarketServerUrl);

        String paramJson = JSONObject.toJSONString(wrapper);
        return httpClient.sendHttpPost(appMarketServerUrl + "/queries", paramJson, null);
    }

    @Override
    public Object publishFunc(RestJsonWrapperBean wrapper) {
        checkAppMarketUrl(appMarketServerUrl);
        /**
         * 1.调用发布接口增加业务数据
         * 2.调用上传接口上传迁移文件
         */
        String paramJson = JSONObject.toJSONString(wrapper);
        Object rntValue = httpClient.sendHttpPost(appMarketServerUrl + "/action/publish-func", paramJson, null);


        LcdpAppMarketDTO appMarketDTO = wrapper.parseUnique(LcdpAppMarketDTO.class);
        Long resourceId = NumberUtils.parseLong(wrapper.getParamValue("resourceId"));
        CoreFileBean coreFile = getExportFileUrl(resourceId);

        FilePathDTO filePath = CoreFileUtils.toFilePath(coreFile);
        File file = filePathService.getLocalPath(filePath).toFile();
        HttpClientManager.getInstance().upload(appMarketServerUrl + "/action/upload-publish-func-file", FileUtils.makeMultipartFile(file), appMarketDTO);
        return rntValue;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object applyFunc(RestJsonWrapperBean wrapper) {
        checkAppMarketUrl(appMarketServerUrl);
        String paramJson = JSONObject.toJSONString(wrapper);
        String sourceUrl = httpClient.sendHttpPost(appMarketServerUrl + "/action/apply-func", paramJson, null);

        List<LcdpAppMarketTableDTO> marketTableDTOList = wrapper.parse(LcdpAppMarketTableDTO.class);


        File tempFile = FileUtils.createTempFile("swdp");
        httpClient.download(sourceUrl, tempFile);
        Map<String, String> fileMap = resourceService.unZip(tempFile.toPath());
        if (fileMap.isEmpty()) {
            throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.FUNCS.APPLY.RESOURCE_FILE_NOT_EXIST"));
        }

        List<LcdpResourceTreeNodeDTO> treeNodeDTOList = getFuncApplyTreeNodeDTO(fileMap);

        return importData(fileMap, treeNodeDTOList, marketTableDTOList);
    }

    @Override
    public Object getFuncVersion(RestJsonWrapperBean wrapper) {
        checkAppMarketUrl(appMarketServerUrl);

        String paramJson = JSONObject.toJSONString(wrapper);
        return httpClient.sendHttpPost(appMarketServerUrl + "/func-version/queries", paramJson, null);
    }

    @Override
    public Object deleteFunc(RestJsonWrapperBean wrapper) {
        checkAppMarketUrl(appMarketServerUrl);

        String paramJson = JSONObject.toJSONString(wrapper);
        Object rntValue = httpClient.sendHttpPost(appMarketServerUrl + "/action/delete-func", paramJson, null);
        return rntValue;
    }

    @Override
    public Object getFuncProject() {
        checkAppMarketUrl(appMarketServerUrl);

        if (StringUtils.isEmpty(appMarketToken)) {
            return null;
        }

        return httpClient.sendHttpGet(appMarketServerUrl + "/func-projects/" + appMarketToken + "/queries", null);
    }

    @Override
    public Object getExistFuncTableList(RestJsonWrapperBean wrapper) {
        checkAppMarketUrl(appMarketServerUrl);

        String paramJson = JSONObject.toJSONString(wrapper);
        Object rntObj = httpClient.sendHttpPost(appMarketServerUrl + "/func-tables/queries", paramJson, null);

        if (rntObj != null) {
            List<String> rntList = new ArrayList<>();
            JSONArray arr = JSONArray.parseArray(rntObj.toString());
            for (int i = 0; i < arr.size(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String tableName = obj.getString("tableName");
                if (databaseService.isExistPhysicalTable(tableName)) {
                    rntList.add(tableName);
                }
            }
            return rntList;
        }
        return null;
    }

    @Override
    public Object getFuncPageResourceList(RestJsonWrapperBean wrapper) {
        checkAppMarketUrl(appMarketServerUrl);

        String paramJson = JSONObject.toJSONString(wrapper);
        Object rntObj = httpClient.sendHttpPost(appMarketServerUrl + "/func-resources/queries", paramJson, null);

        return rntObj;
    }

    @Override
    public Object getFuncResourceContent(Long resourceId) {
        checkAppMarketUrl(appMarketServerUrl);

        return httpClient.sendHttpGet(appMarketServerUrl + "/func-resources/" + resourceId + "/content", null);
    }

    //-------------------------------------------------------------------------------------------------------------
    // 私有方法
    //-------------------------------------------------------------------------------------------------------------

    private void checkAppMarketUrl(String appMarketServerUrl) {
        if (StringUtils.isEmpty(appMarketServerUrl)) {
            throw new CheckedException("[sunway.appmarket.url] not empty!");
        }
    }

    /**
     * 获取资源文件URL
     *
     * @param resourceId
     * @return
     */
    private CoreFileBean getExportFileUrl(Long resourceId) {
        LcdpResourceBean moduleResource = resourceService.selectByIdIfPresent(resourceId);
        if (moduleResource == null) {
            throw new CheckedException("module resource not exist!");
        }
        if (!LcdpConstant.RESOURCE_CATEGORY_MODULE.equals(moduleResource.getResourceCategory())) {
            throw new CheckedException("must be select module resource to publish!");
        }

        //查询resourceId的所有子资源
        SearchFilter childFilter = SearchFilter.instance()
                .match("PARENTID", resourceId).filter(MatchPattern.EQ)
                .match("PATH", null).filter(MatchPattern.DIFFER)
                .match("DELETEFLAG", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ);
        List<LcdpResourceBean> childResourceList = resourceService.selectListByFilter(childFilter);
        List<Long> resourceIdList = childResourceList.stream().map(e -> e.getId()).collect(Collectors.toList());

        LcdpCheckoutRecordBean checkoutRecord = getFuncPublishCheckoutRecord();
        LcdpResourceCheckoutConfigDTO checkoutConfigDTO = getFuncPublishCheckoutConfig();
        List<LcdpTableDTO> tableDTOList = getFuncPublishTableDTO(childResourceList);//导出表数据
        List<String> tableNameList = tableDTOList.stream().map(e -> e.getTableName()).collect(Collectors.toList());
        MapperParameter parameter = new MapperParameter();
        parameter.setFilter(SearchFilter.instance().match("TABLENAME", tableNameList).filter(MatchPattern.OCISEQ));

        List<LcdpTableBean> tableList = databaseService.selectPhysicalTableInfoList(parameter);

        List<LcdpResourceTreeNodeDTO> treeNodeDtoList = resourceService.bulidResourceTreeNodeDTOS(resourceIdList, tableList, CollectionUtils.emptyList());
        List<LcdpResourceBean> exportResourceList = resourceService.getExportedResources(resourceIdList);//导出资源数据

        LcdpResourceExportDTO exportDTO = new LcdpResourceExportDTO();
        exportDTO.setExportLog(checkoutRecord.getCheckoutNote());
        exportDTO.setTreeNodeDtoList(treeNodeDtoList);
        exportDTO.setExportResourceList(exportResourceList);
        exportDTO.setTableDTOList(tableDTOList);
        exportDTO.setViewList(CollectionUtils.emptyList());
        exportDTO.setLcdpResourceFileList(CollectionUtils.emptyList());

        return resourceService.getcreateExportFile(exportDTO, checkoutConfigDTO, checkoutRecord);
    }


    private LcdpCheckoutRecordBean getFuncPublishCheckoutRecord() {
        LcdpCheckoutRecordBean checkoutRecord = new LcdpCheckoutRecordBean();
        checkoutRecord.setCheckoutNote("func publish");
        checkoutRecord.setId(ApplicationContextHelper.getNextIdentity());
        checkoutRecord.setCheckoutNo(ApplicationContextHelper.getNextSequence("T_LCDP_CHECKOUT_RECORD"));
        EntityHelper.assignCreatedElement(checkoutRecord);
        return checkoutRecord;
    }

    private LcdpResourceCheckoutConfigDTO getFuncPublishCheckoutConfig() {
        LcdpResourceCheckoutConfigDTO checkoutConfigDTO = new LcdpResourceCheckoutConfigDTO();
        checkoutConfigDTO.setExportSysClientJsFlag(Constant.NO);// 默认非全局JS脚本
        checkoutConfigDTO.setExportSysClientCssFlag(Constant.NO);// 默认非全局CSS样式
        if (LcdpScriptUtils.validateCurrentDBMybatisMapper()) {
            checkoutConfigDTO.setExportEnvironmentDBType(current_environment_database);
        }
        return checkoutConfigDTO;
    }

    private List<LcdpTableDTO> getFuncPublishTableDTO(List<LcdpResourceBean> childResourceList) {
        List<String> tableNameList = new ArrayList<>();

        childResourceList.stream().filter(f -> LcdpConstant.RESOURCE_CATEGORY_JAVA.equals(f.getResourceCategory())).forEach(e -> {
            if (StringUtils.isNotEmpty(e.getClassContent())) {
                //解析获取LcdpBaseService中getXxxTable()方法的表名称
                Class<?> clazz = ClassManager.getClassByFullName(ClassManager.getClassFullName(e.getClassContent()));
                if (clazz != null && LcdpBaseService.class.isAssignableFrom(clazz)) {
                    String beanName = LcdpJavaCodeResolverUtils.getBeanName(e.getClassContent());
                    LcdpBaseService scriptService = SpringUtils.getBean(beanName);
                    String tableName = scriptService.getTable();
                    if (!StringUtils.isEmpty(tableName)) {
                        tableNameList.add(tableName);
                    }
                }

                //解析获取模块关联表的表名称
                if (!StringUtils.isEmpty(e.getDependentTable())) {
                    tableNameList.addAll(Arrays.asList(e.getDependentTable().split(",")));
                }

                //解析获取脚本中getXxxTable()方法的表名称
                Pattern pattern = Pattern.compile("private String get.*?Table\\(\\) \\{.*?return \"(.*?)\"\\;");
                Matcher matcher = pattern.matcher(e.getContent());
                while (matcher.find()) {
                    tableNameList.add(matcher.group(1));
                }
            }
        });

        List<LcdpTableDTO> list = tableService.selectPhysicalTableInfoList(tableNameList.stream().distinct().collect(Collectors.toList()));
        return list;
    }


    /**
     * 获取一键应用资源树
     *
     * @param fileMap 功能应用的资源文件
     * @return
     */
    private List<LcdpResourceTreeNodeDTO> getFuncApplyTreeNodeDTO(Map<String, String> fileMap) {
        String exportLog = fileMap.get("exportLog");

        JSONObject exportLogJson = JSON.parseObject(exportLog);

        List<LcdpResourceTreeNodeDTO> treeNodeDTOList = (List<LcdpResourceTreeNodeDTO>) exportLogJson.get("treeNodeDTOList");

        return treeNodeDTOList;
    }

    private LcdpSubmitLogBean getFuncApplyAutoSubmitLog() {
        LcdpSubmitLogBean submitLog = new LcdpSubmitLogBean();
        submitLog.setId(ApplicationContextHelper.getNextIdentity());
        submitLog.setCommit(I18nHelper.getMessage("T_LCDP_RESOURCE.FILE.IMPORT.AUTO.SUBMIT"));
        return submitLog;
    }

    private LcdpSubmitLogBean getFuncApplySubmitLog() {
        LcdpSubmitLogBean submitLog = new LcdpSubmitLogBean();
        submitLog.setId(ApplicationContextHelper.getNextIdentity());
        submitLog.setCommit("功能超市一键应用");
        submitLogService.getDao().insert(submitLog);
        return submitLog;
    }

    private void insertFuncApplyImportRecord(Map<String, String> fileMap, List<LcdpResourceTreeNodeDTO> treeNodeDTOList) {
        LcdpResourceImportRecordBean record = new LcdpResourceImportRecordBean();
        record.setId(ApplicationContextHelper.getNextIdentity());
        record.setImportLog("功能超市一键应用");
        record.setImportContent(JSON.toJSONString(treeNodeDTOList));
        String exportLog = fileMap.get("exportLog");
        JSONObject exportLogJson = JSON.parseObject(exportLog);
        String log = exportLogJson.getString("log");
        record.setExportLog(log);
        EntityHelper.assignCreatedElement(record);
        resourceImportRecordService.getDao().insert(record);
    }

    private List<String> getAllResourceIdList(JSONObject resourceNodeObj) {
        List<String> idList = new ArrayList<>();
        idList.add(resourceNodeObj.getString("id"));

        JSONArray children = resourceNodeObj.getJSONArray("children");
        if (children != null && children.size() > 0) {
            for (int i = 0; i < children.size(); i++) {
                JSONObject childObj = children.getJSONObject(i);
                //递归查询所有的resource
                idList.addAll(getAllResourceIdList(childObj));
            }
        }
        return idList;
    }

    private void parseTreeNodeDTO(List<LcdpResourceTreeNodeDTO> treeNodeDTOList, List<String> resourceIdList, List<String> tableNameList, List<LcdpAppMarketTableDTO> marketTableDTOList) {
        String content = JSONObject.toJSONString(treeNodeDTOList);
        JSONArray jsonArray = JSONObject.parseArray(content);
        if (jsonArray != null && jsonArray.size() > 0) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                if (LcdpConstant.RESOURCE_TABLE_TREE_ROOT_NAME.equals(obj.getString("id"))) {//table资源
                    JSONArray tableChildren = obj.getJSONArray("children");
                    if (tableChildren != null && tableChildren.size() > 0) {
                        for (int j = 0; j < tableChildren.size(); j++) {
                            JSONObject tableObj = tableChildren.getJSONObject(j);

                            String tableName = tableObj.getString("id");

                            //根据前端配置勾选的是否合并进行表的导入应用
                            LcdpAppMarketTableDTO marketTableDTO = marketTableDTOList.stream().filter(f -> ObjectUtils.equals(tableName, f.getTableName())).findFirst().orElse(null);
                            if (marketTableDTO != null) {
                                if (Constant.NO.equals(marketTableDTO.getMergeFlag())) {
                                    continue;
                                }
                            }
                            tableNameList.add(tableName);
                        }
                    }
                } else {//resource资源
                    resourceIdList.addAll(getAllResourceIdList(obj));
                }
            }
        }
    }

    private String importData(Map<String, String> fileMap, List<LcdpResourceTreeNodeDTO> treeNodeDTOList, List<LcdpAppMarketTableDTO> marketTableDTOList) {
        StringBuilder importJavaRecord = new StringBuilder();
        LcdpSubmitLogBean autoSubmitLog = getFuncApplyAutoSubmitLog();
        LcdpSubmitLogBean submitLog = getFuncApplySubmitLog();

        //拿到这次需要分析的资源
        List<String> resourceIdList = new ArrayList<>();
        List<String> tableNameList = new ArrayList<>();
        List<String> viewNameList = new ArrayList<>();
        //解析资源树
        parseTreeNodeDTO(treeNodeDTOList, resourceIdList, tableNameList, marketTableDTOList);


        //通过对文件分析得到要导入的数据
        Map<String, Object> importDataMap = resourceService.analysisFileContent(resourceIdList, tableNameList, viewNameList, fileMap);

        List<LcdpResourceBean> resourceList = (List<LcdpResourceBean>) importDataMap.get("resource");
        List<LcdpTableDTO> tableDTOList = (List<LcdpTableDTO>) importDataMap.get("table");
        List<LcdpModulePageCompBean> pageCompList = (List<LcdpModulePageCompBean>) importDataMap.get("pageComps");
        List<LcdpModulePageI18nBean> pageI18nList = (List<LcdpModulePageI18nBean>) importDataMap.get("pageI18n");
        List<LcdpPageI18nCodeBean> pageDependentI18nList = (List<LcdpPageI18nCodeBean>) importDataMap.get("pageDependentI18n");

        List<CoreCodeCategoryBean> coreCodeCategoryList = (List<CoreCodeCategoryBean>) importDataMap.get("codeCategory");//系统编码分类集合
        List<CoreCodeBean> codeList = (List<CoreCodeBean>) importDataMap.get("code");//系统编码分类集合
        List<CoreAdminSelectConfigBean> coreAdminSelectConfigList = (List<CoreAdminSelectConfigBean>) importDataMap.get("adminSelectConfig");//系统编码分类集合
        List<CoreI18nMessageBean> coreI18nMessageList = (List<CoreI18nMessageBean>) importDataMap.get("i18n");//系统国际化编码集合

        Map<Long, List<LcdpModulePageCompBean>> resourceId2PageCompMap = pageCompList.stream().collect((Collectors.groupingBy(LcdpModulePageCompBean::getModulePageId)));
        Map<Long, List<LcdpModulePageI18nBean>> resourceId2PageI18nMap = pageI18nList.stream().collect((Collectors.groupingBy(LcdpModulePageI18nBean::getModulePageId)));
        Map<Long, List<LcdpPageI18nCodeBean>> resourceId2PageDependentI18nMap = pageDependentI18nList.stream().collect((Collectors.groupingBy(LcdpPageI18nCodeBean::getModulePageId)));

        //导入资源
        if (!resourceList.isEmpty()) {
            resourceService.importResourceData(resourceList, resourceId2PageCompMap, resourceId2PageI18nMap, resourceId2PageDependentI18nMap, submitLog, autoSubmitLog, importJavaRecord);
        }

        //导入表
        if (!tableDTOList.isEmpty()) {
            resourceService.importTableData(tableNameList, fileMap, submitLog, autoSubmitLog);
        }

        //导入系统编码
        resourceService.importSysCode(coreCodeCategoryList, codeList, coreAdminSelectConfigList);
        //导入系统国际化
        if (!ObjectUtils.isEmpty(coreI18nMessageList)) {
            resourceService.importSysI18n(coreI18nMessageList);
        }


        insertFuncApplyImportRecord(fileMap, treeNodeDTOList);
        if (StringUtils.isEmpty(importJavaRecord.toString())) {
            return I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.RESOURCE_IMPORT_SUCCESS");
        }
        return I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.RESOURCE_IMPORT_SUCCESS") + I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.JAVA.COMPILER.ERROR") + "：" + "/r/n" + importJavaRecord;
    }
}
