package com.sunwayworld.cloud.module.lcdp.apiintegration.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiReqBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiResBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.persistent.dao.LcdpApiReqDao;
import com.sunwayworld.cloud.module.lcdp.apiintegration.service.LcdpApiIntegrationService;
import com.sunwayworld.cloud.module.lcdp.apiintegration.service.LcdpApiReqService;
import com.sunwayworld.cloud.module.lcdp.apiintegration.service.LcdpApiResService;
import com.sunwayworld.cloud.module.lcdp.apiintegration.support.LcdpApiClientHelper;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpScriptUtils;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.SunwayAopContext;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.exception.checked.CheckedException;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.spring.annotation.GikamBean;
import com.sunwayworld.framework.utils.EncryptUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@GikamBean
public class LcdpApiReqServiceImpl implements LcdpApiReqService {

    @Autowired
    private LcdpApiReqDao lcdpApiReqDao;

    @Autowired
    @Lazy
    private LcdpApiIntegrationService lcdpApiIntegrationService;

    @Autowired
    private LcdpApiResService lcdpApiResService;

    @Override
    @SuppressWarnings("unchecked")
    public LcdpApiReqDao getDao() {
        return lcdpApiReqDao;
    }

    @Override
    public Page<LcdpApiResBean> selectResPaginationByApiReqId(Long id, RestJsonWrapperBean wrapper) {
        Page<LcdpApiResBean> apiResPage = lcdpApiResService.selectPaginationByFilter(SearchFilter.instance().match("REQUESTID", id).filter(MatchPattern.EQ), wrapper);

        if (apiResPage.getNumberOfElements() > 0) {
            List<Long> idList = apiResPage.getRows().stream().map(LcdpApiResBean::getId).collect(Collectors.toList());

            List<LcdpApiResBean> apiResList = lcdpApiResService.getDao().selectListByIds(idList, Arrays.asList("ID", "RESPONSEBODY", "LOG"));

            apiResPage.getRows().forEach(r -> {
                LcdpApiResBean apiRes = apiResList.stream().filter(sr -> sr.getId().equals(r.getId())).findAny().orElse(null);

                if (apiRes != null) {
                    r.setResponseBody(apiRes.getResponseBody());
                    r.setLog(apiRes.getLog());
                }
            });
        }

        return apiResPage;
    }

    @Override
    public Object doProcessRequest(LcdpApiReqBean request, Object... args) {
        LcdpApiBean api = lcdpApiIntegrationService.selectApiByCode(request.getApiCode());

        if (ObjectUtils.isEmpty(api)) {
            throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.NOT_EXIST", request.getApiCode()));
        }
        if (!StringUtils.equals(api.getActivatedFlag(), Constant.ACTIVATED_STATUS_YES)) {
            throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.CAN_NOT_INVOKE", request.getApiCode()));
        }

        LcdpApiResBean insertResponse = new LcdpApiResBean();

        //接口集成鉴权
        String authentType = api.getAuthentType();
        if (StringUtils.isNotEmpty(authentType)) {
            String requestHeader = request.getRequestHeader();
            JSONObject headerObj = JSONObject.parseObject(requestHeader);

            //自定义header鉴权
            if (LcdpConstant.API_AUTHENTTYPE_HEADER.equals(authentType)) {
                String authentHeader = api.getAuthentHeader();
                if (StringUtils.isEmpty(authentHeader)) {
                    throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.HEADER_NOT_EMPTY", api.getRestfulMethod()));
                }
                JSONObject authentHeaderObj = JSONObject.parseObject(authentHeader);
                for (String key : authentHeaderObj.keySet()) {
                    if (!headerObj.containsKey(key)) {
                        throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.AUTHENT_HEADER_CHECK_ERROR", api.getRestfulMethod()));
                    }

                    if (!ObjectUtils.equals(authentHeaderObj.getString(key), headerObj.getString(key))) {
                        throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.AUTHENT_HEADER_CHECK_ERROR", api.getRestfulMethod()));
                    }
                }
            } else {
                //鉴权脚本
                String authentScript = api.getAuthentScript();
                //如果配置了鉴权脚本，则走脚本鉴权校验逻辑，否则走默认校验
                if (StringUtils.isNotEmpty(authentScript)) {
                    try {
                        String returnValue = LcdpScriptUtils.callScriptMethod(authentScript, args);
                        Map<String, String> returnMap = JSONObject.parseObject(returnValue, Map.class);
                        if (!"success".equals(returnMap.get("code"))) {
                            throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.AUTHENT_CHECK_ERROR", api.getRestfulMethod()));
                        }
                    } catch (Exception ex) {
                        throw ex;
                    }
                } else {
                    String clientId = api.getClientId(); //ak(鉴权clientId)
                    String secret = api.getSecret(); //sk(鉴权secret)
                    if (StringUtils.isEmpty(clientId) || StringUtils.isEmpty(secret)) {
                        throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.CLIENTID_SECRET_NOT_EMPTY", api.getRestfulMethod()));
                    }

                    String timestamp = headerObj.getString("timestamp");
                    String requestAk = headerObj.getString("ak");
                    String requestToken = headerObj.getString("token");
                    if (StringUtils.isEmpty(timestamp)) {
                        throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.HEADER_REQUEST_TIMESTAMP_NOT_EMPTY", api.getRestfulMethod()));
                    }
                    if (StringUtils.isEmpty(requestAk)) {
                        throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.HEADER_REQUEST_AK_NOT_EMPTY", api.getRestfulMethod()));
                    }
                    if (StringUtils.isEmpty(requestToken)) {
                        throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.HEADER_REQUEST_TOKEN_NOT_EMPTY", api.getRestfulMethod()));
                    }

                    String token = LcdpConstant.API_AUTHENTTYPE_AUTHENTCHECK.equals(authentType) ?
                            EncryptUtils.MD5Encrypt(timestamp + clientId + secret + request.getRequestBody()) :
                            EncryptUtils.MD5Encrypt(timestamp + clientId + secret);
                    if (!StringUtils.equals(token, requestToken)) {
                        throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.API_INTEGRATION.TIP.AUTHENT_CHECK_ERROR", api.getRestfulMethod()));
                    }
                }

            }
        }

        String returnValue;

        try {
            String convertFlag = ApplicationContextHelper.getConstantValue("LCDP_API_KEY_CONVERT", Constant.YES);

            if (Constant.YES.equals(convertFlag)) {
                returnValue = LcdpScriptUtils.callScriptMethod(api.getScriptMethodPath(), args);
            } else {
                returnValue = LcdpScriptUtils.callScriptMethodNoConvert(api.getScriptMethodPath(), args);
            }


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

            ((LcdpApiReqServiceImpl) SunwayAopContext.currentProxy()).record(request, insertResponse);
        }
    }

    @Override
    public void reset(Long id) {
        LcdpApiReqBean request = selectById(id);
        request.setRequestDateTime(LocalDateTime.now());

        //删除历史请求以及相应
        List<LcdpApiResBean> delResList = lcdpApiResService.getDao().selectListByOneColumnValue(id, "REQUESTID");
        this.getDao().delete(id);
        lcdpApiResService.getDao().deleteBy(delResList);


        LcdpApiBean api = lcdpApiIntegrationService.selectById(request.getApiId());

        String requestBody = request.getRequestBody();

        try {
            if (StringUtils.equals(LcdpConstant.API_OUTER_TYPE, api.getCallType())) {

                if (StringUtils.equals(LcdpConstant.API_RESTFUL_TYPE, api.getApiType())) {

                    doProcessRequest(request, new Object[]{request.getRequestBody()});
                } else {

                    JSONArray args = JSON.parseArray(requestBody);
                    doProcessRequest(request, args.get(0));
                }
            } else {
                LcdpApiClientHelper.setTestFlag(true);
                if (StringUtils.equals(LcdpConstant.API_RESTFUL_TYPE, api.getApiType())) {

                    LcdpApiClientHelper.sendRestHttp(request, new Object[]{request.getRequestBody()});
                } else {
                    JSONArray args = JSON.parseArray(requestBody);
                    doProcessRequest(request, args.get(0));
                }
            }
        } catch (Exception exception) {
            throw new CheckedException(ObjectUtils.getStackTrace(exception));
        }
    }

    @Override
    public void record(LcdpApiReqBean request, LcdpApiResBean insertResponse) {
        String requestStatus = Constant.NO.equals(insertResponse.getStatus()) ? Constant.REMOTE_CALL_STATUS_FAILED : Constant.REMOTE_CALL_STATUS_SUCCESS;
        request.setStatus(requestStatus);

        if(request.getId() == null) {
            request.setId(ApplicationContextHelper.getNextIdentity());
        }

        getDao().insert(request);

        insertResponse.setId(ApplicationContextHelper.getNextIdentity());
        insertResponse.setRequestId(request.getId());
        lcdpApiResService.getDao().insert(insertResponse);
    }
}
