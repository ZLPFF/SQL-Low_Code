package com.sunwayworld.cloud.module.lcdp.apiintegration.webservice;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebParam;
import javax.xml.namespace.QName;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;

import com.alibaba.fastjson.JSON;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.api.server.PortAddressResolver;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.WebServiceContextDelegate;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.base.SoapDynamicDTO;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.base.SoapRequestUrl;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.builder.AbstractSoapDynamicBuilder;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.util.DynamicCreateObjectUtil;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;

/**
 * @author yangsz@sunway.com 2023-06-12
 */
public class SoapHelper {


    /**
     * 使用WebService类对象创建对应HttpAdapter
     */
    public static HttpAdapter createEndpointAdapter(Object implementor) {

        SDDocumentSource primaryWsdl = null;

        WSEndpoint<?> endpoint = WSEndpoint.create(implementor.getClass(), true,
                InstanceResolver.createSingleton(implementor).createInvoker(),
                null, null, null,
                BindingImpl.create(BindingID.parse(implementor.getClass())),
                primaryWsdl,
                null, null, true);
        return HttpAdapter.createAlone(endpoint);
    }

    /**
     * 判断queryString是否为wsdl请求
     */
    public static boolean isWsdlRequest(String queryString) {
        return queryString != null && (queryString.equalsIgnoreCase("wsdl") || queryString.startsWith("xsd="));
    }


    public static CtMethod createCtMethod(CtClass returnType, String methodName, List<SoapDynamicDTO.WebParam> webParamList, AbstractSoapDynamicBuilder abstractSoapDynamicBuilder) {
        List<CtClass> parameters = new ArrayList<>();
        for (SoapDynamicDTO.WebParam webParam : webParamList) {
            try {
                CtClass ctClass;
                if ("simple".equalsIgnoreCase(webParam.getParamType())) {
                    String className = webParam.getArrayFlag() ? webParam.getParamDefinition() + "[]" : webParam.getParamDefinition();
                    ctClass = ClassPool.getDefault().getCtClass(className);
                } else {
                    Long soapId = abstractSoapDynamicBuilder.getSoapDynamicDTO().getServiceId();
                    String className = webParam.getArrayFlag() ? webParam.getParamDefinition() + "[]" : webParam.getParamDefinition();
                    ctClass = DynamicCreateObjectUtil.pair.getFirst().get(DynamicCreateObjectUtil.DYNAMIC_SOAP_ENTITY_PREFIX + soapId + ".SoapClass" + soapId + className);
                }
                parameters.add(ctClass);
            } catch (Exception e) {
                throw new ApplicationRuntimeException(e);
            }

        }
        CtMethod ctMethod = new CtMethod(returnType, methodName, parameters.toArray(new CtClass[0]), abstractSoapDynamicBuilder.getCtClass());
        ParameterAnnotationsAttribute parameterAttribute = new ParameterAnnotationsAttribute(abstractSoapDynamicBuilder.getConstPool(), ParameterAnnotationsAttribute.visibleTag);
        Annotation[][] paramArrays = new Annotation[webParamList.size()][1];
        for (int i = 0; i < webParamList.size(); i++) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("name", webParamList.get(i).getName());
            paramMap.put("partName", webParamList.get(i).getPartName());
            paramMap.put("header", webParamList.get(i).getHeader());
            paramMap.put("targetNamespace", webParamList.get(i).getTargetNamespace());
            Annotation annotation3 = DynamicCreateObjectUtil.addSingleAnnotation(abstractSoapDynamicBuilder.getConstPool(), WebParam.class.getName(), paramMap);
            paramArrays[i][0] = annotation3;
        }
        parameterAttribute.setAnnotations(paramArrays);
        ctMethod.getMethodInfo().getAttributes().add(parameterAttribute);
        return ctMethod;
    }


    public static WebServiceContextDelegate createDelegate(final HttpAdapter adapter, final SoapRequestUrl soapRequestUrl) {
        return new WebServiceContextDelegate() {
            @Override
            public Principal getUserPrincipal(@NotNull Packet request) {
                return null;
            }

            @Override
            public boolean isUserInRole(@NotNull Packet request, String role) {
                return false;
            }

            @Override
            @SuppressWarnings("rawtypes")
            public @NotNull
            String getEPRAddress(@NotNull Packet request, @NotNull WSEndpoint endpoint) {
                PortAddressResolver resolver = adapter.owner.createPortAddressResolver(soapRequestUrl.baseAddress, endpoint.getImplementationClass());
                QName portName = endpoint.getPortName();
                String address = resolver.getAddressFor(endpoint.getServiceName(), portName.getLocalPart());
                if (address == null) {
                    throw new ApplicationRuntimeException("LCDP.MODULE.API_INTEGRATION.TIP.SOAP_SERVLET_NO_ADDRESS_AVAILABLE", portName.getLocalPart());
                }
                return address;
            }

            @Override
            @SuppressWarnings("rawtypes")
            public String getWSDLAddress(@NotNull Packet request, @NotNull WSEndpoint endpoint) {
                return getEPRAddress(request, endpoint) + "?wsdl";
            }
        };
    }

    public static Object fastJsonBean2Map(Object bean) {
        return JSON.parse(JSON.toJSONString(bean));
    }
}
