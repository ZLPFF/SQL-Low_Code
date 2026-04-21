package com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.base;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.sunwayworld.cloud.module.lcdp.apiintegration.bean.LcdpApiBean;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.util.JsonToSoapEntityUtil;
import com.sunwayworld.framework.data.annotation.NotNull;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.utils.BeanUtils;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.ObjectUtils;

/**
 * @author wangda@sunwayworld.com
 * @description 动态soap实体, 根据这个生成webservice服务
 */

public class SoapDynamicDTO implements Serializable {

    private static final long serialVersionUID = -4093559938608266869L;
    //soap版本 只使用1.1
    private String version;
    //soap服务定义
    private WebService webService;
    //通用实体定义
    private Map<String, String> paramConfig;
    //通用实体定义
    private List<Map<String, String>> entityConfig;
    //服务ID
    private Long serviceId;

    public SoapDynamicDTO() {

    }

    public SoapDynamicDTO(LcdpApiBean api) {
        this.serviceId = Long.valueOf(api.getId() + "" + api.getEffectVersion());

        SoapDynamicDTO soapDynamicDTO = JSON.parseObject(api.getSoapConfig(), getClass());

        BeanUtils.copyProperties(soapDynamicDTO, this, "serviceId");

        this.webService.setName(api.getApiName());

        Map<String, String> param = new HashMap<>();

        if (!ObjectUtils.isEmpty(entityConfig)) {
            entityConfig.forEach(entity -> {
                param.put(entity.get("entityName"), entity.get("entityExample"));
            });

            setParamConfig(param);
        }
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public WebService getWebService() {
        return webService;
    }

    public void setWebService(WebService webService) {
        this.webService = webService;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Map<String, String> getParamConfig() {
        return paramConfig;
    }

    public void setParamConfig(Map<String, String> paramConfig) {
        if (CollectionUtils.isEmpty(paramConfig)) {
            this.paramConfig = paramConfig;
        } else {
            this.paramConfig = paramConfig.entrySet().stream().collect(Collectors.toMap(k -> {
                if (StringUtils.isEmpty(k.getKey()) || !k.getKey().matches("^[a-zA-Z]*")) {
                    throw new ApplicationRuntimeException("LCDP.MODULE.API_INTEGRATION.TIP.SOAP_DYNAMIC_ENTITY_NOT_ALLOWED", k.getKey());
                }
                //首字母转大写
                return JsonToSoapEntityUtil.getFirstUppercase(k.getKey());
            }, Map.Entry::getValue));
        }
    }

    public List<Map<String, String>> getEntityConfig() {
        return entityConfig;
    }

    public void setEntityConfig(List<Map<String, String>> entityConfig) {
        this.entityConfig = entityConfig;
    }

    public static class WebService implements Serializable {

        private static final long serialVersionUID = -2934400999433853254L;
        private String name = "";
        @NotNull
        private String targetNamespace = "";
        @NotNull
        private String serviceName = "";
        private String portName = "";
        private String wsdlLocation = "";
        private String endpointInterface = "";

        //服务方法
        @NotNull
        private WebMethod webMethod;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTargetNamespace() {
            return targetNamespace;
        }

        public void setTargetNamespace(String targetNamespace) {
            this.targetNamespace = targetNamespace;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getPortName() {
            return portName;
        }

        public void setPortName(String portName) {
            this.portName = portName;
        }

        public String getWsdlLocation() {
            return wsdlLocation;
        }

        public void setWsdlLocation(String wsdlLocation) {
            this.wsdlLocation = wsdlLocation;
        }

        public String getEndpointInterface() {
            return endpointInterface;
        }

        public void setEndpointInterface(String endpointInterface) {
            this.endpointInterface = endpointInterface;
        }

        public WebMethod getWebMethod() {
            return webMethod;
        }

        public void setWebMethod(WebMethod webMethod) {
            this.webMethod = webMethod;
        }
    }

    public static class WebMethod implements Serializable {

        private static final long serialVersionUID = 8341719709332060095L;
        @NotNull
        private String operationName = "";
        private String action = "";
        private Boolean exclude = Boolean.FALSE;
        private WebParam webResult;
        private List<WebParam> webParam;

        public String getOperationName() {
            return operationName;
        }

        public void setOperationName(String operationName) {
            // 后续需要
            if (StringUtils.isEmpty(operationName) || !operationName.matches("^[a-zA-Z]*")) {
                throw new ApplicationRuntimeException("LCDP.MODULE.API_INTEGRATION.TIP.SOAP_DYNAMIC_METHOD_NOT_ALLOWED", operationName);
            }
            this.operationName = operationName;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public Boolean isExclude() {
            return exclude;
        }

        public void setExclude(Boolean exclude) {
            this.exclude = exclude;
        }

        public WebParam getWebResult() {
            return webResult;
        }

        public void setWebResult(WebParam webResult) {
            this.webResult = webResult;
        }

        public List<WebParam> getWebParam() {
            return webParam;
        }

        public void setWebParam(List<WebParam> webParam) {
            this.webParam = webParam;
        }
    }

    public static class WebResult implements Serializable {

        private static final long serialVersionUID = -6711738994278614452L;
        private String name = "";
        private String partName = "";
        private String targetNamespace = "";
        private Boolean header = Boolean.FALSE;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPartName() {
            return partName;
        }

        public void setPartName(String partName) {
            this.partName = partName;
        }

        public String getTargetNamespace() {
            return targetNamespace;
        }

        public void setTargetNamespace(String targetNamespace) {
            this.targetNamespace = targetNamespace;
        }

        public Boolean getHeader() {
            return header;
        }

        public void setHeader(Boolean header) {
            this.header = header;
        }
    }

    public static class WebParam extends WebResult implements Serializable {

        private static final long serialVersionUID = -1248520557839258484L;
        //参数定义, 简单类型 例：java.lang.Integer  复杂类型: 对应paramConfig的key
        @NotNull
        private String paramDefinition;
        // 参数类型, 简单类型:simple , 复杂类型 : bean
        @NotNull
        private String paramType;
        // 是否为数组
        private Boolean arrayFlag = Boolean.FALSE;

        public String getParamDefinition() {
            return paramDefinition;
        }

        public void setParamDefinition(String paramDefinition) {
            //首字母转大写
            if (!paramDefinition.contains(".")) {
                this.paramDefinition = JsonToSoapEntityUtil.getFirstUppercase(paramDefinition);
            } else {
                this.paramDefinition = paramDefinition;
            }

        }

        public String getParamType() {
            return paramType;
        }

        public void setParamType(String paramType) {
            this.paramType = paramType;
        }

        public Boolean getArrayFlag() {
            return arrayFlag;
        }

        public void setArrayFlag(Boolean arrayFlag) {
            this.arrayFlag = arrayFlag;
        }
    }
}
