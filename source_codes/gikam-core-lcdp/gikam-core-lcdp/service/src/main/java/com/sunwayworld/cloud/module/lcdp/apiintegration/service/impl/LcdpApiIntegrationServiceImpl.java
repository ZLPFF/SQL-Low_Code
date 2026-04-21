package com.sunwayworld.cloud.module.lcdp.apiintegration.service.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.sun.xml.ws.api.server.WebServiceContextDelegate;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiFieldBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiReqBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiResBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.persistent.dao.LcdpApiDao;
import com.sunwayworld.cloud.module.lcdp.apiintegration.service.LcdpApiFieldService;
import com.sunwayworld.cloud.module.lcdp.apiintegration.service.LcdpApiIntegrationService;
import com.sunwayworld.cloud.module.lcdp.apiintegration.service.LcdpApiReqService;
import com.sunwayworld.cloud.module.lcdp.apiintegration.support.LcdpApiClientHelper;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.LcdpWebServiceContext;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.SoapHelper;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.base.SoapConnection;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.base.SoapDynamicDTO;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.base.SoapRequestUrl;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.builder.AbstractSoapDynamicBuilder;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.util.DynamicCreateObjectUtil;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.util.EsbXmlUtils;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.submitlog.bean.LcdpSubmitLogBean;
import com.sunwayworld.cloud.module.lcdp.submitlog.service.LcdpSubmitLogService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.audit.aunnotation.Audit;
import com.sunwayworld.framework.audit.constant.AuditConstant;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.exception.checked.CheckedException;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.io.file.FilePathDTO;
import com.sunwayworld.framework.io.file.FileScope;
import com.sunwayworld.framework.io.file.path.FilePathService;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.restful.data.RestValidationResultBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.ArchiveUtils;
import com.sunwayworld.framework.utils.ArrayUtils;
import com.sunwayworld.framework.utils.EncryptUtils;
import com.sunwayworld.framework.utils.FileUtils;
import com.sunwayworld.framework.utils.JsonUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.ServletUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.item.file.bean.CoreFileBean;
import com.sunwayworld.module.item.file.manager.CoreFileManager;
import com.sunwayworld.module.item.file.service.CoreFileService;
import com.sunwayworld.module.item.file.utils.CoreFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@GikamBean
public class LcdpApiIntegrationServiceImpl implements LcdpApiIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(LcdpApiIntegrationServiceImpl.class);

    @Autowired
    private LcdpApiDao lcdpApiDao;

    @Autowired
    private LcdpApiFieldService lcdpApiFieldService;

    @Autowired
    private LcdpApiReqService lcdpApiReqService;

    @Autowired
    private CoreFileService coreFileService;

    @Autowired
    private FilePathService filePathService;

    @Autowired
    private CoreFileManager fileManager;

    @Autowired
    private LcdpResourceService lcdpResourceService;


    @Autowired
    private LcdpSubmitLogService submitLogService;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpApiDao getDao() {
        return lcdpApiDao;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long insert(RestJsonWrapperBean jsonWrapper) {
        LcdpApiBean lcdpApiIntegration = jsonWrapper.parseUnique(LcdpApiBean.class);
        lcdpApiIntegration.setId(ApplicationContextHelper.getNextIdentity());
        getDao().insert(lcdpApiIntegration);
        return lcdpApiIntegration.getId();
    }

    @Override
    public Page<LcdpApiFieldBean> selectFieldPaginationByApiIntegrationId(Long id, RestJsonWrapperBean wrapper) {
        return lcdpApiFieldService.selectPaginationByFilter(SearchFilter.instance().match("APIID", id).filter(MatchPattern.EQ), wrapper);
    }

    @Override
    public Page<LcdpApiReqBean> selectRequestPaginationByApiIntegrationId(Long id, RestJsonWrapperBean wrapper) {
        Page<LcdpApiReqBean> apiReqPage = lcdpApiReqService.selectPaginationByFilter(SearchFilter.instance().match("APIID", id).filter(MatchPattern.EQ), wrapper);

        // 单独查询clob字段
        if (apiReqPage.getNumberOfElements() > 0) {
            List<Long> idList = apiReqPage.getRows().stream().map(LcdpApiReqBean::getId).collect(Collectors.toList());

            List<LcdpApiReqBean> apiReqList = lcdpApiReqService.getDao().selectListByIds(idList, Arrays.asList("ID", "REQUESTBODY", "REQUESTHEADER"));

            apiReqPage.getRows().forEach(r -> {
                LcdpApiReqBean apiReq = apiReqList.stream().filter(sr -> sr.getId().equals(r.getId())).findAny().orElse(null);

                if (apiReq != null) {
                    r.setRequestBody(apiReq.getRequestBody());
                    r.setRequestHeader(apiReq.getRequestHeader());
                }
            });
        }

        return apiReqPage;
    }

    @Override
    public Page<LcdpApiResBean> selectResponsePaginationByApiRequestId(Long id, RestJsonWrapperBean wrapper) {
        return lcdpApiReqService.selectResPaginationByApiReqId(id, wrapper);
    }

    @Override
    @Transactional
    @Audit(AuditConstant.INSERT)
    public Long insertField(Long id, RestJsonWrapperBean wrapper) {

        List<LcdpApiFieldBean> lcdpApiFieldList = wrapper.parse(LcdpApiFieldBean.class);

        lcdpApiFieldList.forEach(e -> {
            e.setId(ApplicationContextHelper.getNextIdentity());
            e.setApiId(id);
        });

        lcdpApiFieldService.getDao().insert(lcdpApiFieldList);

        return lcdpApiFieldList.get(0).getId();
    }

    @Override
    @Transactional
    @Audit(AuditConstant.DELETE)
    public void deleteField(Long id, RestJsonWrapperBean wrapper) {
        lcdpApiFieldService.delete(wrapper);
    }

    @Override
    @Cacheable(value = "T_LCDP_API", key = "#apiCode", unless = "#result == null")
    public LcdpApiBean selectApiByCode(String apiCode) {
        List<LcdpApiBean> apiList = selectListByFilter(SearchFilter.instance().match("APICODE", apiCode).filter(MatchPattern.SEQ));

        if (apiList.isEmpty()) {
            return null;
        }
        return apiList.get(0);
    }

    @Override
    @Cacheable(value = "T_LCDP_API_URL", key = "#url", unless = "#result == null")
    public LcdpApiBean selectActivatedApiByUrl(String url) {

        List<LcdpApiBean> apiList = selectListByFilter(SearchFilter.instance()
                .match("APIURL", url.toUpperCase()).filter(MatchPattern.CISB)
                .match("ACTIVATEDFLAG", Constant.ACTIVATED_STATUS_YES).filter(MatchPattern.SEQ)
                .match("CALLTYPE", LcdpConstant.API_OUTER_TYPE).filter(MatchPattern.SEQ));

        if (apiList.isEmpty()) {
            return null;
        }
        return apiList.get(0);
    }

    @Override
    @Cacheable(value = "T_LCDP_API_FIELD", key = "#apiId", unless = "#result == null")
    public List<LcdpApiFieldBean> selectCacheableApiFieldList(Long apiId) {
        return lcdpApiFieldService.selectListByFilter(SearchFilter.instance().match("APIID", apiId).filter(MatchPattern.EQ)
                .match("APIFIELD", null).filter(MatchPattern.ISNOTNULL)
                .match("SYSTEMFIELD", null).filter(MatchPattern.ISNOTNULL));
    }

    @Override
    @Transactional
    @Audit(AuditConstant.ACTIVATE)
    public void activate(RestJsonWrapperBean wrapper) {
        LcdpApiIntegrationService.super.activate(wrapper);

        LcdpApiBean parse = wrapper.parseUnique(LcdpApiBean.class);

        LcdpApiBean lcdpApiBean = selectById(parse.getId());

        lcdpApiBean.setEffectVersion(ObjectUtils.isEmpty(lcdpApiBean.getEffectVersion()) ? 1 : lcdpApiBean.getEffectVersion() + 1);

        if (StringUtils.equals(lcdpApiBean.getApiType(), LcdpConstant.API_WEBSERVICE_TYPE) && StringUtils.equals(lcdpApiBean.getCallType(), LcdpConstant.API_OUTER_TYPE)) {
            Map<String, Object> mapping = new HashMap<>();
            try {
                SoapDynamicDTO soapDynamicDTO = new SoapDynamicDTO(lcdpApiBean);

                if (!ObjectUtils.isEmpty(soapDynamicDTO.getParamConfig())) {
                    soapDynamicDTO.getParamConfig().values().forEach(config -> {
                        if (!JSONValidator.from(config).validate() && !EsbXmlUtils.isXml(config)) {
                            throw new CheckedException("LCDP.MODULE.API_INTEGRATION.TIP.ENTITY_EXAMPLE_FORMAT");
                        }
                    });
                }

                AbstractSoapDynamicBuilder.Soap11DynamicBuilder soap11DynamicBuilder = new AbstractSoapDynamicBuilder.Soap11DynamicBuilder(DynamicCreateObjectUtil.class, soapDynamicDTO, lcdpApiBean.getApiCode());
                Class<?> soapClass = soap11DynamicBuilder.createSoapClass();
                mapping.put(String.valueOf(lcdpApiBean.getId()), soapClass.newInstance());
            } catch (Exception e) {
                throw new CheckedException(e);
            }
            LcdpWebServiceContext.endpointAdapterMapping(mapping);
        }

        getDao().update(lcdpApiBean, "EFFECTVERSION");
    }

    @Transactional
    @Audit(AuditConstant.DEACTIVATE)
    public void deactivate(RestJsonWrapperBean wrapper) {
        LcdpApiIntegrationService.super.deactivate(wrapper);

        List<LcdpApiBean> parse = wrapper.parse(LcdpApiBean.class);

        List<String> mapping = parse.stream().map(LcdpApiBean::getId).map(String::valueOf).collect(Collectors.toList());

        LcdpWebServiceContext.removeEndpointAdapter(mapping);
    }

    @Override
    public void resetRequest(Long id) {
        lcdpApiReqService.reset(id);
    }

    @Override
    @Transactional
    @Audit(AuditConstant.SAVE)
    public Object testApi(String apiCode, JSONObject paramMap) {
        LcdpApiBean api = selectApiByCode(apiCode);

        String body = paramMap.getString("body");
        String operation = paramMap.getString("operation");
        JSONArray args = paramMap.getJSONArray("args");

        try {
            LcdpApiClientHelper.setTestFlag(true);
            if (StringUtils.equals(LcdpConstant.API_INVOKE_TYPE, api.getCallType())) {
                if (StringUtils.equals(LcdpConstant.API_RESTFUL_TYPE, api.getApiType())) {

                    return LcdpApiClientHelper.sendHttp(apiCode, body);
                } else {
                    return LcdpApiClientHelper.sendWebService(apiCode, operation, ObjectUtils.isEmpty(args) ? null : args.toArray());
                }
            } else {
                if (StringUtils.equals(LcdpConstant.API_RESTFUL_TYPE, api.getApiType())) {
                    return callApiScript(apiCode, new Object[]{body});

                } else {
                    return callApiScript(apiCode, transformJsonToParams(api, args));
                }
            }
        } catch (Exception exception) {
            throw new CheckedException(ObjectUtils.getStackTrace(exception));
        } finally {
            LcdpApiClientHelper.setTestFlag(false);
        }
    }

    @Override
    @Transactional
    @Audit(AuditConstant.SAVE)
    public Object callRestfulApi(String url, String requestBody) {
        LcdpApiBean lcdpApiBean = selectActivatedApiByUrl(url);

        if (ObjectUtils.isEmpty(lcdpApiBean) || !StringUtils.equals(lcdpApiBean.getApiType(), LcdpConstant.API_RESTFUL_TYPE)) {
            ServletUtils.responseError(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.name());
            return null;
        }

        return callApiScript(lcdpApiBean.getApiCode(), new Object[]{requestBody});
    }

    @Override
    @Transactional
    @Audit(AuditConstant.SAVE)
    public void callSoapApi(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LcdpApiBean lcdpApiBean = selectActivatedApiByUrl(ServletUtils.getRequestUri(request));

        if (ObjectUtils.isEmpty(lcdpApiBean) || !StringUtils.equals(lcdpApiBean.getApiType(), LcdpConstant.API_WEBSERVICE_TYPE)) {
            ServletUtils.responseError(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.name());
            return;
        }

        SoapRequestUrl soapRequestUrl = SoapRequestUrl.newInstance(request);

        HttpAdapter endpointAdapter = LcdpWebServiceContext.getEndpointAdapter(String.valueOf(lcdpApiBean.getId()));

        WebServiceContextDelegate delegate = SoapHelper.createDelegate(endpointAdapter, soapRequestUrl);

        SoapConnection connection = new SoapConnection(request, response, soapRequestUrl, delegate);

        if (SoapHelper.isWsdlRequest(soapRequestUrl.queryString)) {
            endpointAdapter.publishWSDL(connection);
        } else {
            endpointAdapter.handle(connection);
        }
    }

    @Override
    @Transactional
    @Audit(AuditConstant.SAVE)
    public Object callApiScript(String apiCode, Object... args) {
        LcdpApiBean api = selectApiByCode(apiCode);
        if (ObjectUtils.isEmpty(api)) {
            throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.NOT_EXIST", apiCode));
        }
        if (!StringUtils.equals(api.getActivatedFlag(), Constant.ACTIVATED_STATUS_YES)) {
            throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.CAN_NOT_INVOKE", apiCode));
        }
        String requestBody = StringUtils.equals(LcdpConstant.API_RESTFUL_TYPE, api.getApiType()) ? (String) args[0] : JSON.toJSONString(ArrayUtils.asList(args));

        LcdpApiReqBean request = new LcdpApiReqBean();
        request.setApiCode(apiCode);
        request.setRequestBody(requestBody);
        request.setRequestDateTime(LocalDateTime.now());
        request.setStatus(Constant.REMOTE_CALL_STATUS_PENDING);//请求待处理
        request.setApiId(api.getId());
        request.setNotifyFlag(Constant.NO);
        if (StringUtils.equals(LcdpConstant.API_RESTFUL_TYPE, api.getApiType())) {
            HttpServletRequest currentRequest = ServletUtils.getCurrentRequest();
            Map<String, String> headerMap = new HashMap<>();
            Enumeration<String> enumeration = currentRequest.getHeaderNames();
            while (enumeration.hasMoreElements()) {
                String name = enumeration.nextElement();
                String value = currentRequest.getHeader(name);
                headerMap.put(name, value);
            }
            request.setRequestHeader(JSON.toJSONString(headerMap));


            //60540308  兼容get请求，requestBody无法获取参数情况
            Map<String, String[]> paramArrayMap = currentRequest.getParameterMap();
            if (!paramArrayMap.isEmpty()) {
                Map<String, String> paramMap = paramArrayMap.entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().length > 0 ? entry.getValue()[0] : ""
                ));

                String json = JSON.toJSONString(paramMap);
                request.setRequestBody(json);
                args = new Object[]{json};
            }
        }

        if (!StringUtils.equals(api.getCallType(), LcdpConstant.API_OUTER_TYPE)) {
            throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.NOT_EXIST", apiCode));
        }

        return lcdpApiReqService.doProcessRequest(request, args);
    }

    @Override
    public String generateExportFile(RestJsonWrapperBean wrapper) {
        List<Long> exportIdList = wrapper.parseId(Long.class);
        List<LcdpApiBean> apiList = selectListByIds(exportIdList);

        //导出的接口字段
        List<LcdpApiFieldBean> apiFieldList = lcdpApiFieldService.selectListByFilter(SearchFilter.instance()
                .match("APIID", apiList.stream().map(LcdpApiBean::getId).collect(Collectors.toList())).filter(MatchPattern.OR));


        //导出接口集成相关的脚本
        List<LcdpResourceBean> resourceList = new ArrayList<>();

        //1.导出的接口关联脚本
        List<String> scriptPathList = apiList.stream()
                .filter(apiBean -> !StringUtils.isEmpty(apiBean.getScriptMethodPath()))
                .map(apiBean -> apiBean.getScriptMethodPath().substring(0, apiBean.getScriptMethodPath().lastIndexOf(".")))
                .collect(Collectors.toList());
        List<LcdpResourceBean> scriptList = lcdpResourceService.selectListByFilter(SearchFilter.instance()
                .match("PATH", scriptPathList).filter(MatchPattern.OR).match("deleteFlag", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));
        List<LcdpResourceBean> moduleList = lcdpResourceService.selectListByFilter(SearchFilter.instance()
                .match("id", scriptList.stream().map(LcdpResourceBean::getParentId).collect(Collectors.toList())).filter(MatchPattern.OR));
        List<LcdpResourceBean> categoryList = lcdpResourceService.selectListByFilter(SearchFilter.instance()
                .match("id", moduleList.stream().map(LcdpResourceBean::getParentId).collect(Collectors.toList())).filter(MatchPattern.OR));
        resourceList.addAll(scriptList);
        resourceList.addAll(moduleList);
        resourceList.addAll(categoryList);

        //2.导出的接口关联鉴权脚本
        List<String> authentScriptPathList = apiList.stream()
                .filter(apiBean -> !StringUtils.isEmpty(apiBean.getAuthentScript()))
                .map(apiBean -> apiBean.getAuthentScript().substring(0, apiBean.getAuthentScript().lastIndexOf(".")))
                .collect(Collectors.toList());
        List<LcdpResourceBean> authentScriptList = lcdpResourceService.selectListByFilter(SearchFilter.instance()
                .match("PATH", authentScriptPathList).filter(MatchPattern.OR).match("deleteFlag", LcdpConstant.RESOURCE_DELETED_NO).filter(MatchPattern.EQ));
        List<LcdpResourceBean> authentModuleList = lcdpResourceService.selectListByFilter(SearchFilter.instance()
                .match("id", authentScriptList.stream().map(LcdpResourceBean::getParentId).collect(Collectors.toList())).filter(MatchPattern.OR));
        List<LcdpResourceBean> authentCategoryList = lcdpResourceService.selectListByFilter(SearchFilter.instance()
                .match("id", authentModuleList.stream().map(LcdpResourceBean::getParentId).collect(Collectors.toList())).filter(MatchPattern.OR));
        resourceList.addAll(authentScriptList);
        resourceList.addAll(authentModuleList);
        resourceList.addAll(authentCategoryList);

        //去重
        resourceList = resourceList.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(LcdpResourceBean::getId))), ArrayList::new));


        LocalDateTime now = LocalDateTime.now();
        String uuid = StringUtils.randomUUID(16);
        String zipName = "export_lcdp_api_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")) + "_" + LocalContextHelper.getLoginUserId() + ".swdp";

        FilePathDTO zipFilePath = FilePathDTO.of(FileScope.temp.name(), now, uuid, zipName);

        String apiFilePath = filePathService.getLocalPath(FilePathDTO.of(FileScope.temp.name(), now, uuid, "api")).toString() + File.separator;
        String apiFieldFilePath = filePathService.getLocalPath(FilePathDTO.of(FileScope.temp.name(), now, uuid, "apiField")).toString() + File.separator;
        String resourceFilePath = filePathService.getLocalPath(FilePathDTO.of(FileScope.temp.name(), now, uuid, "resource")).toString() + File.separator;

        FileUtils.write(new File(apiFilePath), EncryptUtils.base64Encode(JSONObject.toJSONString(apiList)));
        FileUtils.write(new File(apiFieldFilePath), EncryptUtils.base64Encode(JSONObject.toJSONString(apiFieldList)));
        FileUtils.write(new File(resourceFilePath), EncryptUtils.base64Encode(JSONObject.toJSONString(resourceList)));

        File zip = filePathService.getLocalPath(zipFilePath).toFile();

        ArchiveUtils.zip(zip, new File(apiFilePath), new File(apiFieldFilePath), new File(resourceFilePath));

        fileManager.upload(zipFilePath, zip.toPath());

        return fileManager.getDownloadUrl(zipFilePath);
    }

    @Override
    public RestValidationResultBean validateUrl(Long id, RestJsonWrapperBean wrapper) {
        String url = wrapper.getParamMap().get("url");

        if (!StringUtils.isEmpty(url)) {
            int index = url.indexOf('?');
            if (index != -1) {
                url = url.substring(0, index);
            }
        }

        List<LcdpApiBean> apiBeanList = selectListByFilter(SearchFilter.instance()
                .match("ID", id).filter(MatchPattern.DIFFER)
                .match("ACTIVATEDFLAG", Constant.ACTIVATED_STATUS_YES).filter(MatchPattern.SEQ)
                .match("APIURL", url.toUpperCase()).filter(MatchPattern.CISB));


        if (apiBeanList.isEmpty()) {
            return new RestValidationResultBean(true);
        } else {
            return new RestValidationResultBean(false, I18nHelper.getMessage("core.validator.column.not-unique"));
        }
    }

    @Override
    public void importByExportFile(Long fileId, RestJsonWrapperBean wrapper) {
        CoreFileBean importFileBean = coreFileService.selectDetail(fileId);

        Path importPath = CoreFileUtils.getLocalPath(importFileBean);

        File importFile = importPath.toFile();

        List<LcdpApiBean> apiBeanList = new ArrayList<>();
        List<LcdpApiFieldBean> apiFieldBeanList = new ArrayList<>();
        List<LcdpResourceBean> resourceBeanList = new ArrayList<>();

        unzip(importFile, apiBeanList, apiFieldBeanList, resourceBeanList);

        if(apiBeanList.isEmpty()) {
            throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATIONS.TIP.IMPORTED_FILE_ERROR"));
        }

        //导入接口
        List<String> apiCodeList = apiBeanList.stream().map(LcdpApiBean::getApiCode).collect(Collectors.toList());

        List<LcdpApiBean> existedApiList = selectListByFilter(SearchFilter.instance().match("APICODE", apiCodeList).filter(MatchPattern.OR));

        List<LcdpApiBean> updateApiList = new ArrayList<>();
        List<LcdpApiBean> insertApiList = new ArrayList<>();
        List<LcdpApiBean> initWSApiList = new ArrayList<>();

        for (LcdpApiBean importApi : apiBeanList) {

            LcdpApiBean existedApi = existedApiList.stream().filter(api -> StringUtils.equals(api.getApiCode(), importApi.getApiCode())).findAny().orElse(null);

            if (existedApi != null) {
                //修改存在的api
                importApi.setId(existedApi.getId());
                importApi.setEffectVersion(ObjectUtils.isEmpty(existedApi.getEffectVersion()) ? 1 : existedApi.getEffectVersion() + 1);

                updateApiList.add(importApi);
            } else {
                //插入新的api
                importApi.setId(ApplicationContextHelper.getNextIdentity());
                importApi.setEffectVersion(1l);

                insertApiList.add(importApi);
            }

            //需要初始化加载的webservice接口
            if (StringUtils.equals(importApi.getApiType(), LcdpConstant.API_WEBSERVICE_TYPE) && StringUtils.equals(importApi.getCallType(), LcdpConstant.API_OUTER_TYPE)) {
                initWSApiList.add(importApi);
            }
        }

        getDao().insert(insertApiList);
        getDao().update(updateApiList);

        //初始化webservice接口
        if (!initWSApiList.isEmpty()) {
            Map<String, Object> mapping = new HashMap<>();

            initWSApiList.forEach(activatedApi -> {
                try {
                    SoapDynamicDTO soapDynamicDTO = new SoapDynamicDTO(activatedApi);
                    AbstractSoapDynamicBuilder.Soap11DynamicBuilder soap11DynamicBuilder = new AbstractSoapDynamicBuilder.Soap11DynamicBuilder(DynamicCreateObjectUtil.class, soapDynamicDTO, activatedApi.getApiCode());
                    Class<?> soapClass = soap11DynamicBuilder.createSoapClass();
                    mapping.put(String.valueOf(activatedApi.getId()), soapClass.newInstance());
                } catch (Exception e) {
                    log.error("============>webservice加载异常：apiCode为" + activatedApi.getApiCode() + "，异常日志如下：" + e.getMessage(), e);
                }
            });

            LcdpWebServiceContext.endpointAdapterMapping(mapping);
        }

        //导入接口关联脚本
        LcdpSubmitLogBean autoSubmitLog = new LcdpSubmitLogBean();
        autoSubmitLog.setId(ApplicationContextHelper.getNextIdentity());
        autoSubmitLog.setCommit("接口集成迁入自动升版");

        LcdpSubmitLogBean submitLog = new LcdpSubmitLogBean();
        submitLog.setId(ApplicationContextHelper.getNextIdentity());
        submitLog.setCommit("接口集成迁入");
        submitLogService.getDao().insert(submitLog);
        lcdpResourceService.importResourceData(resourceBeanList, null, null, null, submitLog, autoSubmitLog, new StringBuilder());


    }

    private void unzip(File importFile, List<LcdpApiBean> apiBeanList, List<LcdpApiFieldBean> apiFieldBeanList, List<LcdpResourceBean> resourceBeanList) {

        try (FileInputStream input = new FileInputStream(importFile);
             ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(input), Charset.forName("GBK"))) {

            ZipEntry ze = null;

            while ((ze = zipInputStream.getNextEntry()) != null) {
                int i = ze.getName().indexOf("\\");
                if (i < 0) {
                    i = ze.getName().indexOf("/");
                }
                String fileName = ze.getName().substring(i + 1);

                InputStream is = null;

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream();) {

                    //读取
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zipInputStream.read(buffer)) > -1) {
                        baos.write(buffer, 0, len);
                    }
                    baos.flush();

                    is = new ByteArrayInputStream(baos.toByteArray());
                    String fileContent = StringUtils.read(is);

                    if ("api".equals(fileName)) {
                        List<LcdpApiBean> importApiList = JsonUtils.parseList(EncryptUtils.base64Decode(fileContent), LcdpApiBean.class);
                        apiBeanList.addAll(importApiList);
                    } else if ("apiField".equals(fileName)) {
                        List<LcdpApiFieldBean> importApiFieldList = JsonUtils.parseList(EncryptUtils.base64Decode(fileContent), LcdpApiFieldBean.class);
                        apiFieldBeanList.addAll(importApiFieldList);
                    } else if ("resource".equals(fileName)) {
                        List<LcdpResourceBean> importResourceList = JsonUtils.parseList(EncryptUtils.base64Decode(fileContent), LcdpResourceBean.class);
                        resourceBeanList.addAll(importResourceList);
                    }
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            /* ignore */
                        }
                    }
                }


            }
        } catch (IOException io) {
            throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.RESOUCES.TIP.EXCETION.FILE.ANALYSE.EXCETION"));
        }

    }


    private Map<String, Object> transformJsonToParams(LcdpApiBean api, JSONArray args) {
        SoapDynamicDTO soapDynamicDTO = new SoapDynamicDTO(api);

        List<SoapDynamicDTO.WebParam> webParamList = soapDynamicDTO.getWebService().getWebMethod().getWebParam();

        Map<String, Object> parameters = new HashMap<>();

        for (int i = 0; i < webParamList.size(); i++) {
            parameters.put(webParamList.get(i).getName(), args.get(i));
        }

        return parameters;
    }

    @Override
    @Transactional
    @Audit(AuditConstant.SAVE)
    public void editResetRequest(Long id ,RestJsonWrapperBean wrapper) {
        String testReqBody = wrapper.getParamValue("testReqBody");
        String requestHeader = wrapper.getParamValue("requestHeader");
        
        LcdpApiReqBean request = lcdpApiReqService.selectById(id);
        LcdpApiReqBean entity= new LcdpApiReqBean();
        entity.setId(ApplicationContextHelper.getNextIdentity());
        entity.setApiId(request.getApiId());
        entity.setApiCode(request.getApiCode());
        entity.setRequestDateTime(LocalDateTime.now());
        entity.setStatus("0");
        entity.setNotifyFlag(request.getNotifyFlag());
        entity.setWsOperation(request.getWsOperation());
        entity.setRequestBody(testReqBody);
        entity.setRequestHeader(requestHeader);
        
        lcdpApiReqService.getDao().insert(entity);
        lcdpApiReqService.reset(entity.getId());
    }
}
