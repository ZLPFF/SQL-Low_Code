package com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.soap.SOAPBinding;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.util.proxy.DefinePackageHelper;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.SoapHelper;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.base.SoapDynamicDTO;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.util.DynamicCreateObjectUtil;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.util.EsbXmlUtils;
import com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.util.JsonToSoapEntityUtil;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;

/**
 * @author wangda@sunwayworld.com
 * @date 2022/5/7 13:19
 * @description 动态soap服务建造者
 */

public abstract class AbstractSoapDynamicBuilder {

    private ClassPool classPool;
    private CtClass ctClass;
    private ClassFile classFile;
    private ConstPool constPool;
    private String packageName;
    private Long soapId;
    private String apiCode;
    private SoapDynamicDTO soapDynamicDTO;

    public AbstractSoapDynamicBuilder(Class<?> clazz, SoapDynamicDTO soapDynamicDTO, String apiCode) throws CannotCompileException {
        this.classPool = DynamicCreateObjectUtil.pair.getFirst();
        this.packageName = clazz.getPackage().getName();
        this.apiCode = apiCode;
        this.soapId = soapDynamicDTO.getServiceId();
        this.soapDynamicDTO = soapDynamicDTO;
        //服务名称
        try {
            this.classPool.importPackage(DynamicCreateObjectUtil.DYNAMIC_SOAP_ENTITY_PREFIX + this.soapId);
            this.ctClass = classPool.getCtClass(DynamicCreateObjectUtil.DYNAMIC_SOAP_ENTITY_PREFIX + this.soapId + ".SoapClass" + this.soapId);
            // 停止class精简
            this.ctClass.stopPruning(Boolean.TRUE);
            //解冻
            if (ctClass.isFrozen()) {
                this.ctClass.defrost();
            }
        } catch (NotFoundException e) {
            this.ctClass = classPool.makeClass(this.packageName + this.soapId + ".SoapClass" + this.soapId);
            DefinePackageHelper.definePackage(this.packageName + this.soapId, DynamicCreateObjectUtil.classLoader);
        }
        //获取类文件
        this.classFile = ctClass.getClassFile();
        //获取常量池
        this.constPool = classFile.getConstPool();
    }

    protected AnnotationsAttribute createAnnotationAttribute() {
        //可视的注解
        return new AnnotationsAttribute(this.getConstPool(), AnnotationsAttribute.visibleTag);
    }

    // 类文件webservice注解
    public AbstractSoapDynamicBuilder createClassAnnotation() {
        //给类添加webservice注解
        Map<String, Object> webServiceAnnotationMap = new HashMap<>();
        webServiceAnnotationMap.put("name", "lcdpWebServiceName" + apiCode);
        webServiceAnnotationMap.put("portName", "lcdpWebServicePortName" + apiCode);
        webServiceAnnotationMap.put("serviceName", "lcdpWebServiceServiceName" + apiCode);
        webServiceAnnotationMap.put("targetNamespace", "lcdpwebservice" + apiCode + ".ws.lcdp.sunwayworld.com");
        AnnotationsAttribute annotationsAttribute = createAnnotationAttribute();
        Annotation classAnnotation = DynamicCreateObjectUtil.addSingleAnnotation(this.getConstPool(), WebService.class.getName(), webServiceAnnotationMap);

        Annotation bindTypeAnnotation = createSoapBindingTypeAnnotation();
        if (null == bindTypeAnnotation) {
            annotationsAttribute.setAnnotation(classAnnotation);
        } else {
            annotationsAttribute.setAnnotations(new Annotation[]{classAnnotation, bindTypeAnnotation});
        }
        this.getClassFile().addAttribute(annotationsAttribute);
        return this;
    }

    public Annotation createMethodAnnotation(SoapDynamicDTO.WebMethod webMethod, Class<?> annotationClass) {
        Map<String, Object> annotationMap = new HashMap<>();
        Annotation annotation = null;
        if (WebMethod.class.isAssignableFrom(annotationClass)) {
            annotationMap.put("operationName", webMethod.getOperationName());
            annotation = DynamicCreateObjectUtil.addSingleAnnotation(this.getConstPool(), WebMethod.class.getName(), annotationMap);
        } else if (WebResult.class.isAssignableFrom(annotationClass)) {
            annotationMap.put("name", webMethod.getWebResult().getName());
            annotationMap.put("partName", webMethod.getWebResult().getPartName());
            annotation = DynamicCreateObjectUtil.addSingleAnnotation(this.getConstPool(), WebResult.class.getName(), annotationMap);
        } else if (RequestWrapper.class.isAssignableFrom(annotationClass)) {
            String name = "Request" + new Random().nextInt();
            annotationMap.put("className", "com.sunwayworld.cloud.common.webservice.support." + name);
            annotationMap.put("localName", name);
            annotation = DynamicCreateObjectUtil.addSingleAnnotation(this.getConstPool(), RequestWrapper.class.getName(), annotationMap);
        } else if (ResponseWrapper.class.isAssignableFrom(annotationClass)) {
            String name = "Request" + new Random().nextInt();
            annotationMap.put("className", "com.sunwayworld.cloud.common.webservice.support.Response" + name);
            annotationMap.put("localName", name);
            annotation = DynamicCreateObjectUtil.addSingleAnnotation(this.getConstPool(), ResponseWrapper.class.getName(), annotationMap);
        }
        return annotation;
    }

    // 类文件BindingType注解
    protected abstract Annotation createSoapBindingTypeAnnotation();

    protected CtMethod createCtMethod(SoapDynamicDTO.WebMethod webMethod, CtClass returnType) throws CannotCompileException, NotFoundException {
        //方法参数
        List<SoapDynamicDTO.WebParam> webParamList = webMethod.getWebParam();

        CtMethod ctMethod = null;
        try {
            ctMethod = this.ctClass.getDeclaredMethod(webMethod.getOperationName());
            // 移除原先的方法,防止方法重复
            this.ctClass.removeMethod(ctMethod);
        } catch (NotFoundException e) {

        }
        StringBuilder stringBuilder = new StringBuilder("{com.sunwayworld.cloud.module.lcdp.apiintegration.service.LcdpApiIntegrationService service = com.sunwayworld.framework.context.ApplicationContextHelper.getBean(com.sunwayworld.cloud.module.lcdp.apiintegration.service.LcdpApiIntegrationService.class);\n");

        if (CollectionUtils.isEmpty(webParamList)) {
            //无参数
            ctMethod = new CtMethod(returnType, webMethod.getOperationName(), null, this.getCtClass());
            stringBuilder.append("Object obj = service.callApiScript(\"" + apiCode + "\");");
        } else {
            //有参数
            ctMethod = SoapHelper.createCtMethod(returnType, webMethod.getOperationName(), webParamList, this);
            stringBuilder.append("java.util.Map map = new java.util.HashMap();");

            for (int i = 0; i < webParamList.size(); i++) {
                if (StringUtils.equalsIgnoreCase(webParamList.get(i).getParamType(), "simple")) {
                    stringBuilder.append("map.put(\"").append(webParamList.get(i).getName()).append("\", $").append(i + 1).append(");");
                } else {
                    stringBuilder.append("map.put(\"").append(webParamList.get(i).getName()).append("\", com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.SoapHelper.fastJsonBean2Map($").append(i + 1).append("));");
                }
            }
            stringBuilder.append("Object obj = service.callApiScript(\"" + apiCode + "\",new Object[]{map});");
        }

        stringBuilder.append("String resultStr = String.valueOf(obj);");
        SoapDynamicDTO.WebParam webResult = webMethod.getWebResult();
        Annotation webResultAnno = null;
        if (null != webResult) {
            if ("simple".equalsIgnoreCase(webResult.getParamType())) {
                stringBuilder.append("return resultStr;");
            } else {
                stringBuilder.append("return ($r)com.alibaba.fastjson.JSON.parseObject(resultStr,$type);");
            }
            webResultAnno = createMethodAnnotation(webMethod, WebResult.class);
        }
        stringBuilder.append("}");

        AnnotationsAttribute methodAnno = createAnnotationAttribute();
        //给方法添加webMethod注解
        Annotation webMethodAnno = createMethodAnnotation(webMethod, WebMethod.class);

        methodAnno.setAnnotations(new Annotation[]{webMethodAnno, webResultAnno});
        ctMethod.getMethodInfo().addAttribute(methodAnno);
        ctMethod.setBody(stringBuilder.toString());
        //添加异常处理
        ctMethod.addCatch("{System.out.println($e);throw $e;}", ClassPool.getDefault().get(ApplicationRuntimeException.class.getName()));
        return ctMethod;
    }

    //方法
    public AbstractSoapDynamicBuilder createClassMethod() {
        ClassPool classPool = DynamicCreateObjectUtil.pair.getFirst();
        SoapDynamicDTO.WebMethod webMethod = this.getSoapDynamicDTO().getWebService().getWebMethod();
        if (ObjectUtils.isEmpty(webMethod)) {
            throw new ApplicationRuntimeException("LCDP.MODULE.API_INTEGRATION.TIP.SOAP_METHOD_IS_NOT_NULL");
        }

        try {
            CtClass returnType;
            SoapDynamicDTO.WebParam webResult = webMethod.getWebResult();
            // 无返回值
            if (null == webResult || "void".equals(webResult.getParamType())) {
                returnType = CtClass.voidType;
            } else {
                String paramType = webResult.getParamType();

                // 简单类型
                if ("simple".equalsIgnoreCase(paramType)) {
                    returnType = classPool.get(webMethod.getWebResult().getParamDefinition());
                } else {
                    returnType = classPool.getCtClass(DynamicCreateObjectUtil.DYNAMIC_SOAP_ENTITY_PREFIX + this.soapId + ".SoapClass" + this.soapId + webMethod.getWebResult().getParamDefinition());
                }
            }

            CtMethod ctMethod = createCtMethod(webMethod, returnType);
            this.getCtClass().addMethod(ctMethod);
        } catch (Exception e) {
            throw new ApplicationRuntimeException(e);
        }

        return this;
    }

    // 创建通用实体
    public AbstractSoapDynamicBuilder createEntityClass() {
        Map<String, String> paramConfig = this.getSoapDynamicDTO().getParamConfig();
        if (!CollectionUtils.isEmpty(paramConfig)) {
            for (Map.Entry<String, String> entry : paramConfig.entrySet()) {
                try {
                    if (JsonToSoapEntityUtil.isJson(entry.getValue())) {
                        JsonToSoapEntityUtil.fromJson(entry.getValue(), DynamicCreateObjectUtil.DYNAMIC_SOAP_ENTITY_PREFIX + this.getSoapId() + ".SoapClass" + this.getSoapId() + entry.getKey());
                    } else if (EsbXmlUtils.isXml(entry.getValue())) {
                        Map<String, Object> map = EsbXmlUtils.xmlToMap(entry.getValue());
                        JsonToSoapEntityUtil.fromJson(JacksonUtils.toJson(map), DynamicCreateObjectUtil.DYNAMIC_SOAP_ENTITY_PREFIX + this.getSoapId() + ".SoapClass" + this.getSoapId() + entry.getKey());
                    } else {
                        throw new RuntimeException("LCDP.MODULE.API_INTEGRATION.TIP.SOAP_DYNAMIC_ENTITY_DEFINE_NOT_ALLOWED");
                    }
                } catch (Exception e) {
                    throw new ApplicationRuntimeException(e);
                }
            }
        }
        return this;
    }

    public Class<?> createSoapClass() {
        this.createEntityClass().createClassAnnotation().createClassMethod().writeClassFile();
        //加载
        try {
            // 使用父类加载器
            return this.ctClass.toClass(DynamicCreateObjectUtil.classLoader, this.getClass().getProtectionDomain());
        } catch (Exception e) {
            throw new ApplicationRuntimeException(e);
        }
    }

    // 保存class文件
    public void writeClassFile() {
        //后续看情况,是否需要保存该class文件
    }

    public ClassPool getClassPool() {
        return classPool;
    }

    public void setClassPool(ClassPool classPool) {
        this.classPool = classPool;
    }

    public CtClass getCtClass() {
        return ctClass;
    }

    public void setCtClass(CtClass ctClass) {
        this.ctClass = ctClass;
    }

    public ClassFile getClassFile() {
        return classFile;
    }

    public void setClassFile(ClassFile classFile) {
        this.classFile = classFile;
    }

    public ConstPool getConstPool() {
        return constPool;
    }

    public void setConstPool(ConstPool constPool) {
        this.constPool = constPool;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Long getSoapId() {
        return soapId;
    }

    public void setSoapId(Long soapId) {
        this.soapId = soapId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public SoapDynamicDTO getSoapDynamicDTO() {
        return soapDynamicDTO;
    }

    public void setSoapDynamicDTO(SoapDynamicDTO soapDynamicDTO) {
        this.soapDynamicDTO = soapDynamicDTO;
    }

    // soap1.1
    public static class Soap11DynamicBuilder extends AbstractSoapDynamicBuilder {

        public Soap11DynamicBuilder(Class<?> clazz, SoapDynamicDTO soapDynamicDTO, String apiCode) throws CannotCompileException {
            super(clazz, soapDynamicDTO, apiCode);
        }

        @Override
        protected Annotation createSoapBindingTypeAnnotation() {
            return null;
        }
    }

    //soap1.2
    public static class Soap12DynamicBuilder extends AbstractSoapDynamicBuilder {

        public Soap12DynamicBuilder(Class<?> clazz, SoapDynamicDTO soapDynamicDTO, String apiCode) throws CannotCompileException {
            super(clazz, soapDynamicDTO, apiCode);
        }

        @Override
        protected Annotation createSoapBindingTypeAnnotation() {
            Map<String, Object> bindTypeMap = new HashMap<>();
            bindTypeMap.put("value", SOAPBinding.SOAP12HTTP_BINDING);
            return DynamicCreateObjectUtil.addSingleAnnotation(this.getConstPool(), BindingType.class.getName(), bindTypeMap);
        }
    }
}
