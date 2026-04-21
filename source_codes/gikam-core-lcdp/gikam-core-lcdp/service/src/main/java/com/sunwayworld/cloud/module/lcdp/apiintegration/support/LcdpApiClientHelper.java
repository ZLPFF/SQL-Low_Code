package com.sunwayworld.cloud.module.lcdp.apiintegration.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiFieldBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiReqBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiRequestDTO;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiResBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.service.LcdpApiIntegrationService;
import com.sunwayworld.cloud.module.lcdp.apiintegration.service.LcdpApiReqService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpScriptUtils;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.exception.NetworkException;
import com.sunwayworld.framework.exception.checked.CheckedException;
import com.sunwayworld.framework.executor.manager.TaskExecutorManager;
import com.sunwayworld.framework.http.HttpClientManager;
import com.sunwayworld.framework.http.HttpSimpleClient;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.utils.BeanUtils;
import com.sunwayworld.framework.utils.EncryptUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.util.UriComponentsBuilder;

import javax.xml.namespace.QName;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 低代码http、WebService客户端
 */
public class LcdpApiClientHelper {

    private static final long DEFAULT_CONNECTION_TIMEOUT = 1000 * 10;
    private static final long DEFAULT_RECEIVE_TIMEOUT = 1000 * 60 * 5;

    private static HttpClientManager httpClient = HttpClientManager.getInstance();

    private static ThreadLocal<Boolean> testFlag = new ThreadLocal<>();

    private static final ThreadLocal<LcdpApiRequestDTO> CURRENT_REQUEST = new ThreadLocal<>();


    private static LcdpApiIntegrationService lcdpApiIntegrationService = ApplicationContextHelper.getBean(LcdpApiIntegrationService.class);

    public static String sendHttp(String apiCode, String body) {
        return sendHttp(apiCode, null, null, body);
    }
    /**
     * 发送Http的请求，请求内容为json字符串
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static String sendHttp(String apiCode, Map<String, Object> queryParams, Map<String, String> header, String body,Map<String, String> replaceParams) {
        LcdpApiBean api = findApi(apiCode);

        // 创建新的API对象避免修改原始配置
        LcdpApiBean modifiedApi = new LcdpApiBean();
        // 复制原有属性
        BeanUtils.copyProperties(api, modifiedApi);

        // 处理路径参数替换
        String apiUrl = api.getApiUrl();
        if (replaceParams != null && !replaceParams.isEmpty()) {
            for (Map.Entry<String, String> entry : replaceParams.entrySet()) {
                apiUrl = StringUtils.replace(apiUrl, "{" + entry.getKey() + "}", entry.getValue());
            }
        }
        modifiedApi.setApiUrl(apiUrl);

        return sendHttp(modifiedApi, queryParams, header, body);
    }

    /**
     * 发送Http的请求，请求内容为json字符串
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static String sendHttp(String apiCode, Map<String, Object> queryParams, Map<String, String> header, String body) {
        LcdpApiBean api = findApi(apiCode);
        return sendHttp(api, queryParams, header, body);
    }

    @Nullable
    private static String sendHttp(LcdpApiBean api, Map<String, Object> queryParams, Map<String, String> header, String body) {

        //接口鉴权
        String authentType = api.getAuthentType(); //鉴权方式
        if (StringUtils.isNotEmpty(authentType)) {
            String timestamp = Long.toString(System.currentTimeMillis() / 1000); //时间戳
            String clientId = api.getClientId(); //ak(鉴权clientId)
            String secret = api.getSecret(); //sk(鉴权secret)

            if (StringUtils.isEmpty(clientId) || StringUtils.isEmpty(secret)) {
                throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.CLIENTID_SECRET_NOT_EMPTY", api.getRestfulMethod()));
            }

            if (header == null) {
                header = new HashMap<>();
            }

            //鉴权脚本
            String authentScript = api.getAuthentScript();
            //如果配置了鉴权脚本，则解析脚本封装的header参数，否则走默认封装
            if (StringUtils.isNotEmpty(authentScript)) {
                LcdpApiRequestDTO requestDTO = new LcdpApiRequestDTO();
                requestDTO.setApi(api);
                requestDTO.setBody(body);
                requestDTO.setQueryParams(queryParams);
                requestDTO.setHeaders(header);
                CURRENT_REQUEST.set(requestDTO);


                String returnValue = null;
                Map<String, String> returnMap = null;
                try {
                    returnValue = LcdpScriptUtils.callScriptMethodNoConvert(authentScript, new Object[]{body});
                    returnMap = JSONObject.parseObject(returnValue, Map.class);
                } catch (Exception e) {
                    throw new CheckedException(api.getAuthentScript() + "execute error :" + e.getMessage());
                } finally {
                    CURRENT_REQUEST.remove();
                }

                if (returnMap != null) {
                    for (String key : returnMap.keySet()) {
                        header.put(key, returnMap.get(key));
                    }
                }
            } else {
                //时间戳+ak+sk+MD5加密
                if (LcdpConstant.API_AUTHENTTYPE_AUTHENT.equals(authentType)) {
                    header.put("token", EncryptUtils.MD5Encrypt(timestamp + clientId + secret));
                } else if (LcdpConstant.API_AUTHENTTYPE_AUTHENTCHECK.equals(authentType)) {
                    //时间戳+ak+sk+body+MD5加密
                    header.put("token", EncryptUtils.MD5Encrypt(timestamp + clientId + secret + body));
                }
                header.put("timestamp", timestamp);
                header.put("ak", clientId);
            }
        }

        String result = null;

        Consumer headerConsumer = parseHeader(api, header);

        boolean isMultipart = validateMultipart(api, header);
        boolean isFormUrlEncoded = validateFormUrlEncoded(api, header);

        String url = parseUrl(api, queryParams);

        String finalBody = parseBody(api, body);

        LcdpApiReqBean request = new LcdpApiReqBean();
        request.setApiCode(api.getApiCode());
        request.setRequestBody(finalBody);
        request.setRequestHeader(parseHeaderContent(api, header));
        request.setRequestDateTime(LocalDateTime.now());
        request.setStatus(Constant.REMOTE_CALL_STATUS_PENDING);
        request.setApiId(api.getId());
        request.setRequestUrl(url);
        LcdpApiResBean insertResponse = new LcdpApiResBean();

        try {

            switch (api.getRestfulMethod()) {
                case "GET":
                    result = httpClient.sendHttpGet(url, headerConsumer);
                    break;
                case "POST":
                    if (isMultipart) {
                        result = sendMultiPart(url, finalBody, headerConsumer);
                    } else if (isFormUrlEncoded) {
                        result = sendFormUrlEncoded(url, finalBody, headerConsumer);
                    } else {
                        result = httpClient.sendHttpPost(url, finalBody, headerConsumer);
                    }
                    break;
                case "PUT":
                    result = httpClient.sendHttpPut(url, finalBody, headerConsumer);
                    break;
                case "DELETE":
                    result = httpClient.sendHttpDelete(url, headerConsumer);
                    break;
                case "PATCH":
                    result = httpClient.sendHttpPatch(url, finalBody, headerConsumer);
                    break;
                default:
                    throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.REQUEST_METHOD_NOT_SUPPORT", api.getRestfulMethod()));
            }

            String returnValue = (String) resultMapping(result, api);

            if (returnValue != null) {

                insertResponse.setStatus(Constant.YES);
                insertResponse.setResponseBody(returnValue);

            } else {
                insertResponse.setStatus(Constant.YES);
            }

            return returnValue;
        } catch (Exception ex) {
            insertResponse.setStatus(Constant.NO); // 失败
            insertResponse.setLog(ObjectUtils.getStackTrace(ex));

            throw ex;
        } finally {
            insertResponse.setResponseDateTime(LocalDateTime.now());

            //计算耗时
            LocalDateTime startTime = request.getRequestDateTime();
            LocalDateTime endTime = insertResponse.getResponseDateTime();
            long times = ChronoUnit.MILLIS.between(startTime, endTime);
            insertResponse.setTimes(times);

            TaskExecutorManager.getDefaultRunner().submit(() -> {
                ApplicationContextHelper.getBean(LcdpApiReqService.class).record(request, insertResponse);
            });
        }
    }

    /**
     * 发送WebService
     */
    public static Object sendWebService(String apiCode, String operation, Object... args) {
        LcdpApiBean api = findApi(apiCode);

        LcdpApiReqBean request = new LcdpApiReqBean();
        request.setApiCode(apiCode);
        request.setRequestBody(JSON.toJSONString(args));
        request.setRequestDateTime(LocalDateTime.now());
        request.setStatus(Constant.REMOTE_CALL_STATUS_PENDING);
        request.setApiId(api.getId());
        request.setWsOperation(operation);

        LcdpApiResBean insertResponse = new LcdpApiResBean();

        try {

            Object returnValue = resultMapping(getData(api.getApiUrl(), operation, args)[0], api);

            if (returnValue != null) {

                insertResponse.setStatus(Constant.YES);
                insertResponse.setResponseBody(JSON.toJSONString(returnValue));

            } else {
                insertResponse.setStatus(Constant.YES);
            }

            return returnValue;
        } catch (Exception ex) {
            insertResponse.setStatus(Constant.NO); // 失败
            insertResponse.setLog(ObjectUtils.getStackTrace(ex));

            throw ex;
        } finally {
            insertResponse.setResponseDateTime(LocalDateTime.now());

            //计算耗时
            LocalDateTime startTime = request.getRequestDateTime();
            LocalDateTime endTime = insertResponse.getResponseDateTime();
            long times = ChronoUnit.MILLIS.between(startTime, endTime);
            insertResponse.setTimes(times);

            TaskExecutorManager.getDefaultRunner().submit(() -> {
                ApplicationContextHelper.getBean(LcdpApiReqService.class).record(request, insertResponse);
            });
        }
    }


    public static void setTestFlag(boolean testFlag) {
        LcdpApiClientHelper.testFlag.set(testFlag);
    }

    public static LcdpApiRequestDTO getCurrentRequest() {
        return CURRENT_REQUEST.get();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object sendRestHttp(LcdpApiReqBean lcdpApiReqBean, Object... args) {
        LcdpApiBean api = lcdpApiIntegrationService.selectApiByCode(lcdpApiReqBean.getApiCode());
        validateInvokeApi(lcdpApiReqBean.getApiCode(), api, false);

        String result = null;
        LcdpApiBean newApi = new LcdpApiBean();
        newApi.setRestfulHeader(lcdpApiReqBean.getRequestHeader());
        newApi.setRestfulQueryParam(lcdpApiReqBean.getRequestBody());
        newApi.setApiUrl(api.getApiUrl());


        String body = "";
        if (StringUtils.isNotEmpty(lcdpApiReqBean.getRequestBody())) {
            body = lcdpApiReqBean.getRequestBody();
        }
        Map<String, String> header = new HashMap<String, String>();
        header = JSONObject.parseObject(lcdpApiReqBean.getRequestHeader(), Map.class);
        Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams = JSONObject.parseObject(body, Map.class);

        //接口鉴权
        String authentType = api.getAuthentType(); //鉴权方式
        if (StringUtils.isNotEmpty(authentType)) {
            String timestamp = Long.toString(System.currentTimeMillis() / 1000); //时间戳
            String clientId = api.getClientId(); //ak(鉴权clientId)
            String secret = api.getSecret(); //sk(鉴权secret)

            if (StringUtils.isEmpty(clientId) || StringUtils.isEmpty(secret)) {
                throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.CLIENTID_SECRET_NOT_EMPTY", api.getRestfulMethod()));
            }

            //鉴权脚本
            String authentScript = api.getAuthentScript();
            //如果配置了鉴权脚本，则解析脚本封装的header参数，否则走默认封装
            if (StringUtils.isNotEmpty(authentScript)) {
                LcdpApiRequestDTO requestDTO = new LcdpApiRequestDTO();
                requestDTO.setApi(api);
                requestDTO.setBody(body);
                requestDTO.setQueryParams(queryParams);
                requestDTO.setHeaders(header);
                CURRENT_REQUEST.set(requestDTO);

                String returnValue = null;
                Map<String, String> returnMap = null;
                try {
                    returnValue = LcdpScriptUtils.callScriptMethodNoConvert(authentScript, new Object[]{body});
                    returnMap = JSONObject.parseObject(returnValue, Map.class);
                } catch (Exception e) {
                    throw new CheckedException(api.getAuthentScript() + "execute error :" + e.getMessage());
                } finally {
                    CURRENT_REQUEST.remove();
                }

                if (returnMap != null) {
                    for (String key : returnMap.keySet()) {
                        header.put(key, returnMap.get(key));
                    }
                }
            } else {
                //时间戳+ak+sk+MD5加密
                if (LcdpConstant.API_AUTHENTTYPE_AUTHENT.equals(authentType)) {
                    header.put("token", EncryptUtils.MD5Encrypt(timestamp + clientId + secret));
                } else if (LcdpConstant.API_AUTHENTTYPE_AUTHENTCHECK.equals(authentType)) {
                    //时间戳+ak+sk+body+MD5加密
                    header.put("token", EncryptUtils.MD5Encrypt(timestamp + clientId + secret + body));
                }
                header.put("timestamp", timestamp);
                header.put("ak", clientId);
            }
        }

        Consumer headerConsumer = parseHeader(newApi, header);


        boolean isMultipart = validateMultipart(newApi, header);
        String url = api.getApiUrl();

        String finalBody = parseBody(api, body);

        LcdpApiResBean insertResponse = new LcdpApiResBean();

        try {
            switch (api.getRestfulMethod()) {
                case "GET":
                    result = httpClient.sendHttpGet(url, headerConsumer);
                    break;
                case "POST":
                    result = isMultipart ? sendMultiPart(url, finalBody, headerConsumer) : httpClient.sendHttpPost(url, finalBody, headerConsumer);
                    break;
                case "PUT":
                    result = httpClient.sendHttpPut(url, finalBody, headerConsumer);
                    break;
                case "DELETE":
                    result = httpClient.sendHttpDelete(url, headerConsumer);
                    break;
                case "PATCH":
                    result = httpClient.sendHttpPatch(url, finalBody, headerConsumer);
                    break;
                default:
                    throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.REQUEST_METHOD_NOT_SUPPORT", api.getRestfulMethod()));
            }

            String returnValue = (String) resultMapping(result, api);

            if (returnValue != null) {
                insertResponse.setStatus(Constant.YES);
                insertResponse.setResponseBody(returnValue);

            } else {
                insertResponse.setStatus(Constant.YES);
            }
            lcdpApiReqBean.setStatus(Constant.YES);
            return returnValue;
        } catch (Exception ex) {
            lcdpApiReqBean.setStatus(Constant.NO);
            insertResponse.setStatus(Constant.NO); // 失败
            insertResponse.setLog(ObjectUtils.getStackTrace(ex));

            throw ex;
        } finally {
            insertResponse.setResponseDateTime(LocalDateTime.now());

            //计算耗时
            LocalDateTime startTime = lcdpApiReqBean.getRequestDateTime();
            LocalDateTime endTime = insertResponse.getResponseDateTime();
            long times = ChronoUnit.MILLIS.between(startTime, endTime);
            insertResponse.setTimes(times);

            ApplicationContextHelper.getBean(LcdpApiReqService.class).record(lcdpApiReqBean, insertResponse);
        }
    }

    //-------------------------------------------------------------------------------
    // 私有方法
    //-------------------------------------------------------------------------------

    /**
     * 获取对应接口配置
     */
    private static LcdpApiBean findApi(String apiCode) {
        LcdpApiBean api = lcdpApiIntegrationService.selectApiByCode(apiCode);
        validateInvokeApi(apiCode, api, true);
        return api;
    }

    private static void validateInvokeApi(String apiCode, LcdpApiBean api, boolean allowInactiveWhenTest) {
        if (ObjectUtils.isEmpty(api)) {
            throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.NOT_EXIST", apiCode));
        } else if (!StringUtils.equals(api.getCallType(), LcdpConstant.API_INVOKE_TYPE)) {
            throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.CAN_NOT_INVOKE", apiCode));
        } else if (StringUtils.isBlank(api.getApiUrl())) {
            throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.EMPTY_URL", apiCode));
        } else if (!StringUtils.equals(api.getActivatedFlag(), Constant.ACTIVATED_STATUS_YES)
                && (!allowInactiveWhenTest || testFlag.get() == null || !testFlag.get())) {
            throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.CAN_NOT_INVOKE", apiCode));
        }
    }

    /**
     * 结果映射
     */
    private static Object resultMapping(Object result, LcdpApiBean api) {
        if (result instanceof String && JSONValidator.from((String) result).validate()) {

            Object resultJsonObject = JSON.parse((String) result);

            List<LcdpApiFieldBean> lcdpApiFieldList = lcdpApiIntegrationService.selectCacheableApiFieldList(api.getId());

            Map<String, String> fieldMapping = lcdpApiFieldList.stream().collect(Collectors.toMap(LcdpApiFieldBean::getApiField, LcdpApiFieldBean::getSystemField));

            if (resultJsonObject instanceof JSONObject) {
                replaceKeyByMapping((JSONObject) resultJsonObject, fieldMapping);
                return JSONObject.toJSONString(resultJsonObject);
            } else if (resultJsonObject instanceof JSONArray) {
                ((JSONArray) resultJsonObject).forEach(resultObject -> replaceKeyByMapping(resultObject, fieldMapping));
                return JSONObject.toJSONString(resultJsonObject);
            }
        }
        return result;
    }

    private static void replaceKeyByMapping(Object target, Map<String, String> fieldMapping) {
        if (target instanceof JSONObject) {
            JSONObject targetImpl = (JSONObject) target;

            fieldMapping.forEach((apiField, systemFiled) -> {
                if (targetImpl.containsKey(apiField)) {
                    targetImpl.put(systemFiled, targetImpl.remove(apiField));
                }
            });
        }
    }

    /**
     * 解析生成header
     */
    private static Consumer<HttpRequestBase> parseHeader(LcdpApiBean api, Map<String, String> header) {
        Object parse = JSON.parse(StringUtils.isBlank(api.getRestfulHeader()) ? "{}" : api.getRestfulHeader());

        if (parse instanceof JSONObject) {

            return request -> {
                //配置header
                ((JSONObject) parse).forEach((k, v) -> request.addHeader(k, v.toString()));
                //调用header
                if (!ObjectUtils.isEmpty(header)) {
                    header.forEach((k, v) -> request.addHeader(k, v));
                }
            };
        } else {
            throw new CheckedException("LCDP.MODULE.API_INTEGRATION.RESTFUL_HEADER.TIP.PARSE_ERROR");
        }
    }

    /**
     * 解析生成header
     */
    private static String parseHeaderContent(LcdpApiBean api, Map<String, String> header) {
        Object parse = JSON.parse(StringUtils.isBlank(api.getRestfulHeader()) ? "{}" : api.getRestfulHeader());

        if (parse instanceof JSONObject) {

            JSONObject finalHeader = (JSONObject) parse;

            if (!ObjectUtils.isEmpty(header)) {
                header.forEach((k, v) -> finalHeader.put(k, v));
            }

            return finalHeader.toJSONString();
        } else {
            throw new CheckedException("LCDP.MODULE.API_INTEGRATION.RESTFUL_HEADER.TIP.PARSE_ERROR");
        }
    }

    /**
     * 解析生成url
     */
    private static String parseUrl(LcdpApiBean api, Map<String, Object> params) {
        String apiUrl = api.getApiUrl();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(apiUrl);

        if (!StringUtils.isBlank(api.getRestfulQueryParam())) {
            JSONObject jsonObject = JSONObject.parseObject(api.getRestfulQueryParam());
            jsonObject.forEach((k, v) -> uriComponentsBuilder.queryParam(k, v));
        }

        if (!ObjectUtils.isEmpty(params)) {
            params.forEach((k, v) -> uriComponentsBuilder.queryParam(k, v));
        }

        return uriComponentsBuilder.build().toString();
    }

    private static String parseBody(LcdpApiBean api, String body) {

        if (!StringUtils.isBlank(api.getRestfulReqBody())) {
            if (StringUtils.isBlank(body)) {
                return api.getRestfulReqBody();
            }
            try {
                return jsonMerge(JSONObject.parseObject(body), JSONObject.parseObject(api.getRestfulReqBody())).toJSONString();
            } catch (Exception e) {
                return body;
            }
        }

        return body;
    }


    /**
     * 合并JSON对象，用source覆盖target，返回覆盖后的JSON对象。
     *
     * @param source JSONObject
     * @param target JSONObject
     * @return JSONObject
     */
    private static JSONObject jsonMerge(JSONObject source, JSONObject target) {
        // 覆盖目标JSON为空，直接返回覆盖源
        if (target == null) {
            return source;
        }

        for (String key : source.keySet()) {
            Object value = source.get(key);
            if (!target.containsKey(key)) {
                target.put(key, value);
            } else {
                if (value instanceof JSONObject) {
                    JSONObject valueJson = (JSONObject) value;
                    JSONObject targetValue = jsonMerge(valueJson, target.getJSONObject(key));
                    target.put(key, targetValue);
                } else if (value instanceof JSONArray) {
                    JSONArray valueArray = (JSONArray) value;
                    for (int i = 0; i < valueArray.size(); i++) {
                        JSONObject obj = (JSONObject) valueArray.get(i);
                        JSONObject targetValue = jsonMerge(obj, (JSONObject) target.getJSONArray(key).get(i));
                        target.getJSONArray(key).set(i, targetValue);
                    }
                } else {
                    target.put(key, value);
                }
            }
        }
        return target;
    }


    //-----------------------------------------------------------
    // cxf实现逻辑
    //-----------------------------------------------------------
    private static final Object[] getData(String wsdlUrl, String operation, Object... args) {

        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();

        try (Client client = dcf.createClient(new URL(wsdlUrl))) {
            HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
            HTTPClientPolicy policy = new HTTPClientPolicy();
            // 连接超时时间
            policy.setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT);
            // 请求超时时间
            policy.setReceiveTimeout(DEFAULT_RECEIVE_TIMEOUT);
            httpConduit.setClient(policy);

            QName qname = getOperateQName(client, operation);

            Object[] realArgs = getRealArgs(client, qname, args);

            return client.invoke(qname, realArgs);
        } catch (Exception ex) {
            throw new NetworkException(ex);
        }
    }

    private static Object[] getRealArgs(Client client, QName bindName, Object[] args) {

        Endpoint endpoint = client.getEndpoint();

        ServiceInfo serviceInfo = endpoint.getService().getServiceInfos().get(0);
        BindingInfo binding = serviceInfo.getBindings().stream().findAny().get();

        BindingOperationInfo boi = binding.getOperation(bindName);
        BindingMessageInfo inputMessageInfo = null;
        if (!boi.isUnwrapped()) {
            //OrderProcess uses document literal wrapped style.
            inputMessageInfo = boi.getWrappedOperation().getInput();
        } else {
            inputMessageInfo = boi.getUnwrappedOperation().getInput();
        }

        List<MessagePartInfo> parts = inputMessageInfo.getMessageParts();

        /***********************以下是初始化参数，组装参数；处理返回结果的过程******************************************/
        Object[] parameters = new Object[parts.size()];
        for (int m = 0; m < parts.size(); m++) {
            MessagePartInfo part = parts.get(m);
            // 取得对象实例
            Class<?> partClass = part.getTypeClass();//OrderInfo.class;
            //实例化对象
            Object initDomain = null;
            //普通参数的形参，不需要fastJson转换直接赋值即可
            if ("java.lang.String".equalsIgnoreCase(partClass.getCanonicalName())
                    || "int".equalsIgnoreCase(partClass.getCanonicalName())) {
                initDomain = args[m].toString();
            }
            //如果是数组
            else if (partClass.getCanonicalName().indexOf("[]") > -1) {
                //转换数组
                initDomain = JSON.parseArray(args[m].toString(), partClass.getComponentType());
            } else {
                initDomain = JSON.parseObject(args[m].toString(), partClass);
            }
            parameters[m] = initDomain;

        }
        return parameters;
    }

    private static final QName getOperateQName(Client client, String operation) {
        Endpoint endpoint = client.getEndpoint();
        QName opName = new QName(endpoint.getService().getName().getNamespaceURI(), operation);
        BindingInfo bindingInfo = endpoint.getEndpointInfo().getBinding();

        if (bindingInfo.getOperation(opName) == null) {
            for (BindingOperationInfo operationInfo : bindingInfo.getOperations()) {
                if (operation.equals(operationInfo.getName().getLocalPart())) {
                    return operationInfo.getName();
                }
            }
        }

        return opName;
    }

    private static boolean validateMultipart(LcdpApiBean api, Map<String, String> header) {

        Object parse = JSON.parse(StringUtils.isBlank(api.getRestfulHeader()) ? "{}" : api.getRestfulHeader());

        boolean isMultipart = false;

        if (parse instanceof JSONObject) {

            //配置header
            isMultipart = ((JSONObject) parse).keySet().stream()
                    .anyMatch(k -> StringUtils.equalsIgnoreCase("content-type", k)
                            && StringUtils.containsIgnoreCase(String.valueOf(((JSONObject) parse).get(k)), "multipart"));

            //调用header
            if (!isMultipart && !ObjectUtils.isEmpty(header)) {
                isMultipart = header.keySet().stream()
                        .anyMatch(k -> StringUtils.equalsIgnoreCase("content-type", k)
                                && StringUtils.containsIgnoreCase(String.valueOf(header.get(k)), "multipart"));
            }

            return isMultipart;
        } else {
            throw new CheckedException("LCDP.MODULE.API_INTEGRATION.RESTFUL_HEADER.TIP.PARSE_ERROR");
        }

    }


    private static boolean validateFormUrlEncoded(LcdpApiBean api, Map<String, String> header) {
        Object parse = JSON.parse(StringUtils.isBlank(api.getRestfulHeader()) ? "{}" : api.getRestfulHeader());

        boolean isFormUrlEncoded = false;

        if (parse instanceof JSONObject) {
            // 检查配置header
            isFormUrlEncoded = ((JSONObject) parse).keySet().stream()
                    .anyMatch(k -> StringUtils.equalsIgnoreCase("content-type", k)
                            && StringUtils.containsIgnoreCase(String.valueOf(((JSONObject) parse).get(k)), "application/x-www-form-urlencoded"));

            // 检查调用header
            if (!isFormUrlEncoded && !ObjectUtils.isEmpty(header)) {
                isFormUrlEncoded = header.keySet().stream()
                        .anyMatch(k -> StringUtils.equalsIgnoreCase("content-type", k)
                                && StringUtils.containsIgnoreCase(String.valueOf(header.get(k)), "application/x-www-form-urlencoded"));
            }

            return isFormUrlEncoded;
        } else {
            throw new CheckedException("LCDP.MODULE.API_INTEGRATION.RESTFUL_HEADER.TIP.PARSE_ERROR");
        }
    }

    private static String sendFormUrlEncoded(String url, String finalBody, Consumer<HttpPost> headerConsumer) {
        try {
            // 将JSON对象转换为表单编码格式
            StringBuilder formParams = new StringBuilder();
            if (JSONValidator.from(finalBody).validate()) {
                JSONObject jsonObject = JSON.parseObject(finalBody);

                Map<String, String> map = jsonObject.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue() != null ? entry.getValue().toString() : ""
                        ));

                return httpClient.sendHttpPostForm(url, map, headerConsumer);
            }

            return null;
        } catch (Exception e) {
            throw new NetworkException(e);
        }
    }


    private static String sendMultiPart(String url, String finalBody, Consumer<HttpPost> headerConsumer) {
        try (CloseableHttpClient httpClient = HttpSimpleClient.getInstance().getHttpClient();) {

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(Charset.forName("UTF-8"));
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            finalBody = StringUtils.isBlank(finalBody) ? "{}" : finalBody;

            //表单中其他参数
            if (JSONValidator.from(finalBody).validate()) {
                JSONObject jsonObject = JSON.parseObject(finalBody);
                for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                    builder.addPart(entry.getKey(), new StringBody(String.valueOf(entry.getValue()), ContentType.create("text/plain", Consts.UTF_8)));
                }
            }


            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(builder.build());

            if (headerConsumer != null) {
                headerConsumer.accept(httpPost);
            }

            try (CloseableHttpResponse response = httpClient.execute(httpPost);) {
                HttpEntity body = response.getEntity();
                return EntityUtils.toString(body, "UTF-8");
            }
        } catch (IOException ioe) {
            throw new NetworkException(ioe);
        }
    }

}
