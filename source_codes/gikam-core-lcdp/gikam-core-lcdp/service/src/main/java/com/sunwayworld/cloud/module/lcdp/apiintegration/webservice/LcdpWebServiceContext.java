package com.sunwayworld.cloud.module.lcdp.apiintegration.webservice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.service.LcdpApiIntegrationService;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.base.SoapDynamicDTO;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.builder.AbstractSoapDynamicBuilder;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.util.DynamicCreateObjectUtil;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;

/**
 * @author yangsz@sunway.com 2023-06-12
 */
public class LcdpWebServiceContext {

    private static final Logger log = LoggerFactory.getLogger(LcdpWebServiceContext.class);

    // soap端点适配, 程序启动得时候,需要初始化.服务停用时,需要删除该映射,路径与HttpAdatper对应关系
    private static Map<String, HttpAdapter> mappings = new ConcurrentHashMap<>();

    public static HttpAdapter getEndpointAdapter(String url) {
        return mappings.get(url);
    }

    // 添加适配
    public static void endpointAdapterMapping(Map<String, Object> mapping) {
        mapping.forEach((k, v) -> mappings.put(k, SoapHelper.createEndpointAdapter(v)));
    }

    public static void removeEndpointAdapter(List<String> mapping) {
        mapping.forEach(serviceId -> mappings.remove(serviceId));
    }

    public static void init() {
        LcdpApiIntegrationService apiIntegrationService = ApplicationContextHelper.getBean(LcdpApiIntegrationService.class);

        List<LcdpApiBean> activatedApiList = apiIntegrationService.selectListByFilter(SearchFilter.instance()
                .match("ACTIVATEDFLAG", Constant.ACTIVATED_STATUS_YES).filter(MatchPattern.SEQ)
                .match("APITYPE", LcdpConstant.API_WEBSERVICE_TYPE).filter(MatchPattern.SEQ)
                .match("CALLTYPE", LcdpConstant.API_OUTER_TYPE).filter(MatchPattern.SEQ));

        Map<String, Object> mapping = new HashMap<>();

        activatedApiList.forEach(activatedApi -> {
            try {
                SoapDynamicDTO soapDynamicDTO = new SoapDynamicDTO(activatedApi);
                AbstractSoapDynamicBuilder.Soap11DynamicBuilder soap11DynamicBuilder = new AbstractSoapDynamicBuilder.Soap11DynamicBuilder(DynamicCreateObjectUtil.class, soapDynamicDTO, activatedApi.getApiCode());
                Class<?> soapClass = soap11DynamicBuilder.createSoapClass();
                mapping.put(String.valueOf(activatedApi.getId()), soapClass.newInstance());
            } catch (Exception e) {
                log.error("============>webservice加载异常：apiCode为" + activatedApi.getApiCode() + "，异常日志如下：" + e.getMessage());
            }
        });

        endpointAdapterMapping(mapping);
    }
}
