package com.sunwayworld.cloud.module.lcdp.resource.support;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

import com.gexin.fastjson.JSON;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceClassInfoDTO;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.support.bean.LcdpClassDescriptor;
import com.sunwayworld.cloud.module.lcdp.resource.support.bean.LcdpFieldDescriptor;
import com.sunwayworld.cloud.module.lcdp.resource.support.bean.LcdpMethodDescriptor;
import com.sunwayworld.cloud.module.lcdp.resource.support.bean.LcdpParamDescriptor;
import com.sunwayworld.framework.data.Pair;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.jdk.core.ClassManager;
import com.sunwayworld.framework.utils.ClassUtils;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.LcdpUtils;
import com.sunwayworld.framework.utils.ReflectionUtils;
import com.sunwayworld.framework.utils.ReflectionUtils.FieldWrapper;
import com.sunwayworld.framework.utils.ReflectionUtils.MethodWrapper;
import com.sunwayworld.framework.utils.SpringUtils;
import com.sunwayworld.framework.utils.StringUtils;

public abstract class LcdpReflectionUtils {
    private static final Logger logger = LoggerFactory.getLogger(LcdpReflectionUtils.class);
    
    // 校验类名是否是低代码平台开发的
    private static final Pattern lcdpClassPattern = Pattern.compile("^([A-Za-z0-9_]+\\.){2}server\\.(?<packageLastName>[A-Za-z0-9_]+)\\.(?<lcdpClassName>[A-Za-z0-9_]+)(v[1-9][0-9]*(m[1-9][0-9]*)?)?$");
    
    private static final Pattern packagePattern = Pattern.compile("([A-Za-z0-9_]+\\.)+");
    
    /**
     * 获取类中的方法
     */
    public static final List<LcdpMethodDescriptor> getMethod(Class<?> clazz, String methodName) {
        List<MethodWrapper> methodList = ReflectionUtils.getMethodWrapperListByName(clazz, methodName);
        
        return methodList.stream().map(m -> {
            LcdpMethodDescriptor descriptor = new LcdpMethodDescriptor();
            descriptor.setName(methodName);
            descriptor.setModifiers(m.getMethod().getModifiers());
            descriptor.setReturnTypeName(m.getMethod().getReturnType().getName());
            
            LcdpClassDescriptor returnTypeDescriptor = new LcdpClassDescriptor();
            returnTypeDescriptor.setClassName(m.getMethod().getReturnType().getSimpleName());
            returnTypeDescriptor.setClassFullName(m.getMethod().getReturnType().getName());
            returnTypeDescriptor.setLcdpClass(false);
            descriptor.setReturnType(returnTypeDescriptor);
            
            if (m.getMethod().getParameterCount() > 0) {
                List<LcdpParamDescriptor> paramList = new ArrayList<>();
                for (int i = 0, j = m.getMethod().getParameterCount(); i < j; i++) {
                    Parameter parameter = m.getMethod().getParameters()[i];
                    
                    LcdpParamDescriptor paramDescriptor = new LcdpParamDescriptor();
                    paramDescriptor.setName(parameter.getName());
                    paramDescriptor.setTypeName(parameter.getType().getName());
                    paramDescriptor.setType(parameter.getType());
                    paramList.add(paramDescriptor);
                }
                descriptor.setParamList(paramList);
            }
            
            return descriptor;
        }).distinct().collect(Collectors.toList());
    }
    
    /**
     * 获取源代码中的方法，如果不存在向上找抽象类和接口类
     */
    public static final List<LcdpMethodDescriptor> getMethod(String sourceCode, String methodName) {
        // 删除注释
        sourceCode = LcdpJavaCodeResolverUtils.getNocommentJavaCode(sourceCode);
        
        List<LcdpMethodDescriptor> descriptorList = new ArrayList<>();
        
        List<String> importedPackageList = LcdpJavaCodeResolverUtils.getImportedPackageList(sourceCode);
        
        // 提取源代码中的方法
        Matcher methodMatcher = ClassManager.classMethodPattern.matcher(sourceCode);
        while(methodMatcher.find()) {
            if (methodName.equals(methodMatcher.group("methodName"))) {
                String group = methodMatcher.group();
                
                LcdpMethodDescriptor descriptor = new LcdpMethodDescriptor();
                descriptor.setName(methodName);
                descriptor.setModifiers(getModifiers(group));
                
                String returnType = methodMatcher.group("returnType");
                if (StringUtils.equals("void", returnType)) {
                    descriptor.setReturnTypeName("void");
                } else {
                    LcdpClassDescriptor returnTypeDescriptor = getClassDescriptor(importedPackageList, returnType);
                    if (returnTypeDescriptor == null) {
                        throw new ApplicationRuntimeException("LCDP.EXCEPTION.METHOD_RETURN_TYPE_NOT_SPECIFIED", methodName, returnType);
                    }

                    descriptor.setReturnTypeName(returnTypeDescriptor.getClassFullName());

                    descriptor.setReturnType(getClassDescriptor(importedPackageList, returnType));
                }
                String methodParam = methodMatcher.group("methodParam");
                if (!StringUtils.isBlank(methodParam)) {
                    List<LcdpParamDescriptor> paramList = new ArrayList<>();
                    
                    String[] methodParams = methodParam.trim().split("[\\s\\r\\n]*,[\\s\\r\\n]*");
                    
                    for (String param : methodParams) {
                        Matcher paramMatcher = ClassManager.classMethodParamPattern.matcher(param);
                        if (paramMatcher.find()) {
                            LcdpParamDescriptor paramDescriptor = new LcdpParamDescriptor();
                            paramDescriptor.setName(paramMatcher.group("paramName"));
                            
                            LcdpClassDescriptor paramTypeDescriptor = getClassDescriptor(importedPackageList, paramMatcher.group("paramType"));
                            if (paramTypeDescriptor == null) {
                                throw new ApplicationRuntimeException("LCDP.EXCEPTION.METHOD_PARAM_TYPE_NOT_SPECIFIED", methodName, paramDescriptor.getName());
                            }
                            
                            paramDescriptor.setTypeName(paramTypeDescriptor.getClassFullName());
                            if (isLcdpClass(paramTypeDescriptor.getClassFullName())) {
                                Class<?> clazz = SpringUtils.getBean(LcdpResourceService.class).getActiveClassByPath(paramTypeDescriptor.getClassFullName(), LcdpUtils.isDebugRequest());
                                
                                paramDescriptor.setType(clazz);
                            } else {
                                paramDescriptor.setType(ClassManager.getClassByFullName(paramTypeDescriptor.getClassFullName()));
                            }
                            
                            paramList.add(paramDescriptor);
                        }
                    }
                    
                    descriptor.setParamList(paramList);
                }
                
                descriptorList.add(descriptor);
            }
        }
        
        // 提取父类中的成员变量
        String extendClassFullName = getSuperClassFullName(sourceCode);
        if (!StringUtils.isBlank(extendClassFullName)) {
            Class<?> clazz = ClassManager.getClassByFullName(extendClassFullName);
            
            if (clazz == null) {
                throw new ApplicationRuntimeException("LCDP.EXCEPTION.CLASS_NOT_EXISTS", extendClassFullName);
            }
            
            List<Method> methodList = ReflectionUtils.getMethodList(clazz, m -> m.getName().equals(methodName) && !Modifier.isPrivate(m.getModifiers()));
            for (Method method : methodList) {
                LcdpMethodDescriptor descriptor = new LcdpMethodDescriptor();
                descriptor.setName(methodName);
                descriptor.setModifiers(method.getModifiers());
                descriptor.setReturnTypeName(method.getReturnType().getName());
                
                LcdpClassDescriptor returnTypeDescriptor = new LcdpClassDescriptor();
                returnTypeDescriptor.setClassName(method.getReturnType().getSimpleName());
                returnTypeDescriptor.setClassFullName(method.getReturnType().getName());
                returnTypeDescriptor.setLcdpClass(false);
                descriptor.setReturnType(returnTypeDescriptor);
                
                if (method.getParameterCount() > 0) {
                    List<LcdpParamDescriptor> paramList = new ArrayList<>();
                    for (int i = 0, j = method.getParameterCount(); i < j; i++) {
                        Parameter parameter = method.getParameters()[i];
                        
                        LcdpParamDescriptor paramDescriptor = new LcdpParamDescriptor();
                        paramDescriptor.setName(parameter.getName());
                        paramDescriptor.setTypeName(parameter.getType().getName());
                        paramDescriptor.setType(parameter.getType());
                        paramList.add(paramDescriptor);
                    }
                    descriptor.setParamList(paramList);
                }
                
                if (descriptorList.stream().noneMatch(d -> d.equals(descriptor))) {
                    descriptorList.add(descriptor);
                }
            }
        }
        
        // 提取接口类中的成员变量
        List<String> implementedClassFullNameList = getImplementedClassFullNameList(sourceCode);
        for (String implementedClassFullName : implementedClassFullNameList) {
            Class<?> clazz = ClassManager.getClassByFullName(implementedClassFullName);
            
            if (clazz == null) {
                throw new ApplicationRuntimeException("LCDP.EXCEPTION.CLASS_NOT_EXISTS", implementedClassFullName);
            }
            
            List<Method> methodList = ReflectionUtils.getMethodList(clazz, m -> m.getName().equals(methodName) && !Modifier.isPrivate(m.getModifiers()));
            for (Method method : methodList) {
                LcdpMethodDescriptor descriptor = new LcdpMethodDescriptor();
                descriptor.setName(methodName);
                descriptor.setModifiers(method.getModifiers());
                descriptor.setReturnTypeName(method.getReturnType().getName());
                
                LcdpClassDescriptor returnTypeDescriptor = new LcdpClassDescriptor();
                returnTypeDescriptor.setClassName(method.getReturnType().getSimpleName());
                returnTypeDescriptor.setClassFullName(method.getReturnType().getName());
                returnTypeDescriptor.setLcdpClass(false);
                descriptor.setReturnType(returnTypeDescriptor);
                
                if (method.getParameterCount() > 0) {
                    List<LcdpParamDescriptor> paramList = new ArrayList<>();
                    for (int i = 0, j = method.getParameterCount(); i < j; i++) {
                        Parameter parameter = method.getParameters()[i];
                        
                        LcdpParamDescriptor paramDescriptor = new LcdpParamDescriptor();
                        paramDescriptor.setName(parameter.getName());
                        paramDescriptor.setTypeName(parameter.getType().getName());
                        paramDescriptor.setType(parameter.getType());
                        paramList.add(paramDescriptor);
                    }
                    descriptor.setParamList(paramList);
                }
                
                if (descriptorList.stream().noneMatch(d -> d.equals(descriptor))) {
                    descriptorList.add(descriptor);
                }
            }
        }
        
        return descriptorList;
    }
    /**
     * 获取类中的成员变量
     */
    public static final LcdpFieldDescriptor getField(Class<?> clazz, String fieldName) {
        FieldWrapper fieldWrapper = ReflectionUtils.getFieldWrapperList(clazz).stream().filter(f -> f.getField().getName().equals(fieldName)).findAny().orElse(null);
        
        if (fieldWrapper == null) {
            return null;
        }
        
        LcdpFieldDescriptor descriptor = new LcdpFieldDescriptor();
        descriptor.setName(fieldName);
        descriptor.setTypeName(fieldWrapper.getField().getType().getName());
        descriptor.setModifiers(fieldWrapper.getField().getModifiers());
        
        return descriptor;
    }
    
    /**
     * 获取源代码中的成员变量，如果不存在向上找抽象类也接口类
     */
    public static final LcdpFieldDescriptor getField(String sourceCode, String fieldName) {
        // 删除注释
        sourceCode = LcdpJavaCodeResolverUtils.getNocommentJavaCode(sourceCode);
        
        List<String> importedPackageList = LcdpJavaCodeResolverUtils.getImportedPackageList(sourceCode);
        
        // 提取源代码中的成员变量
        Matcher fieldMatcher = ClassManager.classFieldPattern.matcher(sourceCode);
        while(fieldMatcher.find()) {
            if (fieldName.equals(fieldMatcher.group("fieldName"))) {
                String group = fieldMatcher.group();
                
                logger.warn("11111111>>>" + group);
                
                LcdpFieldDescriptor descriptor = new LcdpFieldDescriptor();
                descriptor.setName(fieldName);
                descriptor.setModifiers(getModifiers(group));
                
                String fieldType = fieldMatcher.group("fieldType");
                
                logger.warn("22222222>>>" + fieldType);
                
                LcdpClassDescriptor fieldClassDescriptor = getClassDescriptor(importedPackageList, fieldType);
                
                logger.warn("333333333>>>" + JSON.toJSONString(fieldClassDescriptor));
                
                if (fieldClassDescriptor == null) {
                    throw new ApplicationRuntimeException("LCDP.EXCEPTION.FIELD_TYPE_NOT_SPECIFIED", fieldName, fieldType);
                }
                
                if (CollectionUtils.isEmpty(fieldClassDescriptor.getGenericMap())) { // 没有泛型，获取全路径
                    descriptor.setTypeName(fieldClassDescriptor.getClassFullName());
                } else { // 有泛型获取类名
                    descriptor.setTypeName(fieldClassDescriptor.getClassName());
                }
                descriptor.setType(fieldClassDescriptor);
                
                return descriptor;
            }
        }
        
        // 提取父类中的成员变量
        String extendClassFullName = getSuperClassFullName(sourceCode);
        if (!StringUtils.isBlank(extendClassFullName)) {
            Class<?> clazz = ClassManager.getClassByFullName(extendClassFullName);
            
            if (clazz == null) {
                throw new ApplicationRuntimeException("LCDP.EXCEPTION.CLASS_NOT_EXISTS", extendClassFullName);
            }
            
            Field field = ReflectionUtils.findField(clazz, fieldName);
            if (field != null) {
                LcdpFieldDescriptor descriptor = new LcdpFieldDescriptor();
                descriptor.setName(fieldName);
                descriptor.setType(getClassDescriptor(importedPackageList, field.getType().getName()));
                descriptor.setTypeName(field.getType().getName());
                descriptor.setModifiers(field.getModifiers());
                
                return descriptor;
            }
        }
        
        // 提取接口类中的成员变量
        List<String> implementedClassFullNameList = getImplementedClassFullNameList(sourceCode);
        for (String implementedClassFullName : implementedClassFullNameList) {
            Class<?> clazz = ClassManager.getClassByFullName(implementedClassFullName);
            
            if (clazz == null) {
                throw new ApplicationRuntimeException("LCDP.EXCEPTION.CLASS_NOT_EXISTS", implementedClassFullName);
            }
            
            Field field = ReflectionUtils.findField(clazz, fieldName);
            if (field != null) {
                LcdpFieldDescriptor descriptor = new LcdpFieldDescriptor();
                descriptor.setName(fieldName);
                descriptor.setType(getClassDescriptor(importedPackageList, field.getType().getName()));
                descriptor.setTypeName(field.getType().getName());
                descriptor.setModifiers(field.getModifiers());
                
                return descriptor;
            }
        }
        
        throw new ApplicationRuntimeException("LCDP.EXCEPTION.FIELD_NOT_EXISTS", fieldName, ClassManager.getClassName(sourceCode));
    }
    
    /**
     * 获取源代码中继承的类全称
     */
    public static String getSuperClassFullName(String sourceCode) {
        // 删除注释
        sourceCode = LcdpJavaCodeResolverUtils.getNocommentJavaCode(sourceCode);
        
        List<String> importedPackageList = LcdpJavaCodeResolverUtils.getImportedPackageList(sourceCode);
        
        Matcher extendsMatcher = ClassManager.classExtendPattern.matcher(sourceCode);
        while (extendsMatcher.find()) {
            String className = extendsMatcher.group("className");
            
            LcdpClassDescriptor classDescriptor = getClassDescriptor(importedPackageList, className);
            
            if (classDescriptor != null) {
                return classDescriptor.getClassFullName();
            }
        }
        
        return null;
    }
    
    /**
     * 获取源代码中继承的类中含泛型的全称
     */
    public static String getSuperClassGenericPart(String sourceCode, String superClass) {
        // 删除注释
        sourceCode = LcdpJavaCodeResolverUtils.getNocommentJavaCode(sourceCode);
        
        Pattern genericPartPattern = Pattern.compile("public\\s+class\\s+[A-Z][A-Za-z0-9]+\\s+extends\\s+" + superClass + "(?<genericPart>\\<[A-Za-z0-9_\\.\\,\\s]+\\>)");
        
        Matcher genericPartMatcher = genericPartPattern.matcher(sourceCode);
        if (genericPartMatcher.find()) {
            return genericPartMatcher.group("genericPart");
        }
        
        return null;
    }
    
    /**
     * 拆分实现的接口名称
     */
    public static List<String> splitInterfaceNames(String interfaceNames) {
        if (StringUtils.isBlank(interfaceNames)) {
            return CollectionUtils.emptyList();
        }
        
        if (!StringUtils.contains(interfaceNames, ",")) {
            return Arrays.asList(interfaceNames.trim());
        }
        
        List<String> interfaceNameList = new ArrayList<>();
        
        int left = 0;
        int right = 0;
        String str = "";
        
        for (char c : interfaceNames.trim().toCharArray()) {
            if (c == '<') {
                left++;
            } else if (c == '>') {
                right++;
            } else if (c == ',') {
                if (StringUtils.isBlank(str)) {
                    continue;
                }
                
                if (left == right) {
                    interfaceNameList.add(str.trim());
                    
                    str = "";
                    left = 0;
                    right = 0;
                    
                    continue;
                }
            } else if (Character.isWhitespace(c)) {
                if (StringUtils.isBlank(str)) {
                    continue;
                }
            }
            
            str += Character.toString(c);
        }
        
        if (!StringUtils.isBlank(str)
                && left == right) {
            interfaceNameList.add(str.trim());
        }
        
        return interfaceNameList;
    }
    
    /**
     * 获取源代码中所有接口的类全称
     */
    public static List<String> getImplementedClassFullNameList(String sourceCode) {
        List<String> importedPackageList = LcdpJavaCodeResolverUtils.getImportedPackageList(sourceCode);
        
        Matcher implementedMatcher = ClassManager.classImplementedPattern.matcher(sourceCode);
        if (implementedMatcher.find()) {
            String interfaceNames = StringUtils.trim(implementedMatcher.group("classNames"));
            
            List<String> interfaceNameList = splitInterfaceNames(interfaceNames);
            
            if (interfaceNameList.isEmpty()) {
                return CollectionUtils.emptyList();
            }

            // 接口可能是泛型，把泛型部分去掉
            return interfaceNameList.stream().map(n -> LcdpReflectionUtils.getClassFullName(importedPackageList, removeGenericPartIfPresent(n)))
                    .collect(Collectors.toList());
        }
        
        return CollectionUtils.emptyList();
    }

    /**
     * 获取源代码中接口的类中含泛型的全称
     */
    public static String getInterfacecClassGenericPart(String sourceCode, String interfaceClassName) {
        // 删除注释
        sourceCode = LcdpJavaCodeResolverUtils.getNocommentJavaCode(sourceCode);
        
        Matcher implementedMatcher = ClassManager.classImplementedPattern.matcher(sourceCode);
        if (implementedMatcher.find()) {
            String interfaceNames = StringUtils.trim(implementedMatcher.group("classNames"));
            
            List<String> interfaceNameList = splitInterfaceNames(interfaceNames);
            
            return interfaceNameList.stream().filter(n -> {
                String noPackageName = removePackage(n);
                return interfaceClassName.equals(noPackageName) || noPackageName.startsWith(interfaceClassName + "<");
            }).findAny().orElse(null);
        }
        
        return null;
    }
    
    /**
     * 通过类名简称和源代码中已导入的包获取类的描述
     */
    public static @Nullable LcdpClassDescriptor getClassDescriptor(List<String> importedPackageList, String className) {
        return getClassDescriptor(importedPackageList, className, CollectionUtils.emptyMap());
    }
    
    /**
     * 通过类名简称、泛型的Map和源代码中已导入的包获取类的描述
     */
    public static @Nullable LcdpClassDescriptor getClassDescriptor(List<String> importedPackageList, String className, Map<String, LcdpClassDescriptor> genericMap) {
        LcdpClassDescriptor classDescriptor = new LcdpClassDescriptor();
        classDescriptor.setClassName(className);
        
        if (ClassUtils.isPrimitiveType(className)) { // 基础类型
            classDescriptor.setClassFullName(className);
            classDescriptor.setLcdpClass(false);
            
            return classDescriptor;
        }
        
        String newClassName = className;
        
        // 泛型的签名，对于类名为非泛型，后缀为空
        String genericSignature = null;
        Matcher genericMatcher = ClassManager.classGenericPattern.matcher(className);
        if (genericMatcher.find()) {
            genericSignature = genericMatcher.group();
            
            newClassName = genericMatcher.replaceAll(""); // 删除泛型
            
            genericSignature = StringUtils.removeEnd(StringUtils.removeStart(genericSignature, "<"), ">");
        }
        
        logger.warn("genericSignature------>[" + genericSignature + "] -- " + newClassName + "---:" + className);
        
        String suffix = newClassName;
        String classFullName = importedPackageList
                .stream()
                .filter(p -> p.equals(suffix) || p.endsWith("." + suffix) || p.equals("@" + suffix))
                .map(p -> StringUtils.removeStart(p, "@"))
                .findAny()
                .orElse(null);
        
        if (!StringUtils.isBlank(classFullName)) {
            classDescriptor.setClassFullName(classFullName);
            classDescriptor.setLcdpClass(isLcdpClass(classFullName));
            
            if (!StringUtils.isBlank(genericSignature)) {
                classDescriptor.setGenericMap(getGenericTypeMap(classFullName, importedPackageList, genericSignature, genericMap));
            }
            
            return classDescriptor;
        }
        
        List<String> classFullNameList = ClassManager.getClassFullNameList(newClassName);
        
        logger.error("-----classFullNameList::" + classFullNameList.size());
        
        if (classFullNameList.isEmpty()) {
            Class<?> clazz = ClassManager.getClassByFullName(newClassName);
            if (clazz != null) {
                classDescriptor.setClassFullName(newClassName);
                classDescriptor.setLcdpClass(false);
                
                if (!StringUtils.isBlank(genericSignature)) {
                    classDescriptor.setGenericMap(getGenericTypeMap(newClassName, importedPackageList, genericSignature, genericMap));
                }
                
                return classDescriptor;
            }
            
            return null;
        }
        
        for (String importedPackage : importedPackageList) {
            if (importedPackage.endsWith(".*")) {
                String prefix = StringUtils.removeStart(importedPackage.substring(0, importedPackage.length() - 1), "@");
                
                classFullName = classFullNameList.stream().filter(n -> StringUtils.startsWith(n, prefix)
                        && !n.substring(prefix.length() + 1).contains(".")).findAny().orElse(null);
                
                if (!StringUtils.isBlank(classFullName)) {
                    classDescriptor.setClassFullName(classFullName);
                    
                    classDescriptor.setLcdpClass(ClassManager.isClassPresent(classFullName));
                    
                    return classDescriptor;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 验证当前类名（含路径）是否是低代码平台开发的，对于低代码
     */
    public static boolean isLcdpClass(String className) {
        if (StringUtils.isBlank(className)) {
            return false;
        }
        
        Matcher lcdpClassMatcher = lcdpClassPattern.matcher(className);
        if (lcdpClassMatcher.find()) {
            String packageLastName = lcdpClassMatcher.group("packageLastName");
            
            String lcdpClassName = lcdpClassMatcher.group("lcdpClassName");
            
            return lcdpClassName.startsWith(packageLastName);
        } else {
            return false;
        }
    }
    
    /**
     * 通过类名（含路径）获取低代码的路径
     */
    public static String getLcdpPath(String className) {
        if (StringUtils.isBlank(className)) {
            return className;
        }
        
        return className.substring(0, className.lastIndexOf("."));
    }
    
    /**
     * 从泛型签名中获取类名，如类List<String>, Map<String, Map<String, Sring>>中的
     * List<String> 和 Map<String, Map<String, Sring>>
     */
    public static List<String> getClassNameFromGenericSignature(String genericSignature) {
        if (genericSignature.startsWith("<")
                && genericSignature.endsWith("<")) {
            genericSignature = StringUtils.removeStart(genericSignature.trim(), "<");
            genericSignature = StringUtils.removeEnd(genericSignature.trim(), ">");
        }
        
        List<String> classNameList = new ArrayList<>();
        
        int left = 0;
        int right = 0;
        int comma = 0; // 是否有逗号
        String str = "";
        
        for (char c : genericSignature.trim().toCharArray()) {
            if (c == '<') {
                left++;
            } else if (c == '>') {
                right++;
            } else if (c == ',') {
                if (StringUtils.isBlank(str)) {
                    continue;
                }
                
                if (left == right) {
                    classNameList.add(str.trim());
                    
                    str = "";
                    left = 0;
                    right = 0;
                    comma = 0;
                    
                    continue;
                }
                
                comma++;
            } else if (Character.isWhitespace(c)) {
                if (StringUtils.isBlank(str)) {
                    continue;
                }
            }
            
            str += Character.toString(c);
            
            if (left == right
                    && (left > 0 || comma > 0)) {
                classNameList.add(str.trim());
                
                str = "";
                left = 0;
                right = 0;
                comma = 0;
            }
        }
        
        if (!StringUtils.isBlank(str)
                && left == right) {
            classNameList.add(str.trim());
        }
        
        return classNameList;
    }
    

    
    /**
     * 获取指定类名和已加载的包获取类的全称
     */
    public static String getClassFullName(List<String> importedPackageList, String className) {
        String classFullName = importedPackageList
                .stream()
                .filter(p -> p.endsWith("." + className) || p.equals(className) || p.equals("@" + className))
                .findAny()
                .orElse(null);
        
        if (!StringUtils.isBlank(classFullName)) {
            return classFullName;
        }
        
        List<String> classFullNameList = ClassManager.getClassFullNameList(className);
        
        if (classFullNameList.isEmpty()) {
            return null;
        }
        
        for (String importedPackage : importedPackageList) {
            if (importedPackage.endsWith(".*")) {
                String prefix = StringUtils.removeStart(importedPackage.substring(0, importedPackage.length() - 1), "@");
                
                classFullName = classFullNameList.stream().filter(n -> StringUtils.startsWith(n, prefix)
                        && !n.substring(prefix.length() + 1).contains(".")).findAny().orElse(null);
                
                if (!StringUtils.isBlank(classFullName)) {
                    return classFullName;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 通过类的路径获取启用的类信息
     */
    public static LcdpResourceClassInfoDTO getActiveClassInfoByPath(String path) {
        return SpringUtils.getBean(LcdpResourceService.class).getActiveClassInfoByPath(path, LcdpUtils.isDebugRequest());
    }
    
    /**
     * 获取调用方法的参数数量
     */
    public static int getParamQty(String method) {
        String params = method.substring(method.indexOf("(") + 1, method.lastIndexOf(")")).trim();
        if (!StringUtils.isBlank(params)) {
            if (!StringUtils.contains(params, ",")) {
                return 1;
            } else {
                if (params.contains("\\\"")) { // 引号里有引号
                    params = params.replaceAll("\\\"", "");
                }
                
                params = params.replaceAll("\"[^\"]*\"", "\"\"");
                
                return StringUtils.countOccurrencesOf(params, ",") + 1;
            }
        }
        
        return 0;
    }
    
    /**
     * 验证方法的参数数量是否与给定的数量匹配
     */
    public static boolean matchParamQty(Method method, int paramQty) {
        Parameter[] params = method.getParameters();
        
        if (paramQty == 0) {
            if (params == null
                    || params.length == 0) {
                return true;
            }
            
            if (params.length > 1) {
                return false;
            }
            
            Parameter parameter = params[0];
            
            return parameter.isVarArgs();
        }
        
        if (params.length == 0) {
            return false;
        }
        
        if (paramQty == method.getParameterCount()) {
            return true;
        }
        
        // 需要验证最后一个参数是否是泛型
        if (paramQty >= method.getParameterCount() - 1) {
            return params[params.length - 1].isVarArgs();
        }
        
        return false;
    }
    

    /**
     * 通过泛型类和实际类型的字符串，获取泛型类和实际类型的map
     */
    public static List<Pair<String, String>> getActualClassList(String genericString, String actualString) {
        Matcher genericMatcher = ClassManager.classGenericPattern.matcher(genericString);
        if (!genericMatcher.find()) {
            return CollectionUtils.emptyList();
        }
        String genericPart = genericMatcher.group("genericPart");
        
        logger.info("1-------->" + genericPart);
        
        List<String> genericPartList = getGenericPartList(genericPart);
        
        logger.info("[" + actualString + "]");
        
        Matcher actualMatcher = ClassManager.classGenericPattern.matcher(actualString);
        if (!actualMatcher.find()) {
            return CollectionUtils.emptyList();
        }
        String actualPart = actualMatcher.group("genericPart");
        
        logger.info("2-------->" + actualPart);
        
        List<String> actualPartList = getGenericPartList(actualPart);
        
        List<Pair<String, String>> pairList = new ArrayList<>();
        for (int i = 0, j = genericPartList.size(); i < j; i++) {
            pairList.add(Pair.of(genericPartList.get(i), actualPartList.get(i)));
        }
        
        return pairList;
    }
    /**
     * 替换含有泛型类的类名中替换实际的泛型值
     */
    public static String replaceGenericType(String classRawName, Map<String, String> actualClassMap) {
        for (Entry<String, String> entry : actualClassMap.entrySet()) {
            if (classRawName.contains("<" + entry.getKey() + ">")) {
                classRawName = StringUtils.replace(classRawName, "<" + entry.getKey() + ">", "<" + entry.getValue() + ">");
            }
            
            if (classRawName.contains("<" + entry.getKey() + ",")) {
                classRawName = StringUtils.replace(classRawName, "<" + entry.getKey() + ",", "<" + entry.getValue() + ",");
            }
            
            if (classRawName.contains(" " + entry.getKey() + ",")) {
                classRawName = StringUtils.replace(classRawName, " " + entry.getKey() + ",", " " + entry.getValue() + ",");
            }
            
            if (classRawName.contains(" " + entry.getKey() + ">")) {
                classRawName = StringUtils.replace(classRawName, " " + entry.getKey() + ">", " " + entry.getValue() + ">");
            }
        }
        
        return classRawName;
    }
    
    /**
     * 实际类名中删除包
     */
    public static String removePackage(String actualClassName) {
        Matcher packageMatcher = packagePattern.matcher(actualClassName);
        while(packageMatcher.find()) {
            actualClassName = packageMatcher.replaceAll("");
        }
        
        return actualClassName;
    }
    
    public static ResolvableType getResolvableType(String actualTypeName, List<String> importedPackageList) {
        if (!StringUtils.contains(actualTypeName, "<")) { // 不是泛型
            String classFullName = getClassFullName(importedPackageList, actualTypeName);
            
            if (StringUtils.isBlank(classFullName)) {
                return null;
            }
            
            logger.warn("-------11>" + classFullName);
            
            if (classFullName.startsWith("@") ) { // 低代码开发的类
                String path = classFullName.substring(1);
                
                logger.warn("-------22>" + path);
                
                Class<?> clazz = SpringUtils.getBean(LcdpResourceService.class).getActiveClassByPath(path, LcdpUtils.isDebugRequest());
                
                return ResolvableType.forClass(clazz);
            }
            
            return ResolvableType.forClass(ClassManager.getClassByFullName(classFullName));
        }
        
        String classFullName = getClassFullName(importedPackageList, actualTypeName.substring(0, actualTypeName.indexOf("<")));
        Class<?> actualType = ClassManager.getClassByFullName(classFullName);
        
        TypeContext context = getTypeContext(actualType, actualTypeName.substring(actualTypeName.indexOf("<")), importedPackageList);
        
        return context.toResolvableType();
    }


    //--------------------------------------------------------------------------------------------------
    // 私有方法
    //--------------------------------------------------------------------------------------------------
    private static String removeGenericPartIfPresent(String genericPart) {
        if (StringUtils.isBlank(genericPart)) {
            return genericPart;
        }

        if (genericPart.contains("<")) { // 去掉泛型
            return genericPart.substring(0, genericPart.indexOf("<")).trim();
        }

        return genericPart;
    }

    private static TypeContext getTypeContext(Class<?> type, String genericPart, List<String> importedPackageList) {
        TypeContext context = new TypeContext();
        context.setOwner(type);
        
        if (StringUtils.isBlank(genericPart)) {
            return context;
        }
        
        List<String> genericPartList = getGenericPartList(genericPart);
        for (String gp : genericPartList) {
            if (StringUtils.contains(gp, "<")) {
                String classFullName = getClassFullName(importedPackageList, gp.substring(0, gp.indexOf("<")));
                Class<?> actualType = ClassManager.getClassByFullName(classFullName);
                
                TypeContext childType = getTypeContext(actualType, gp.substring(gp.indexOf("<")), importedPackageList);
                context.addChildType(childType);
            } else {
                String classFullName = getClassFullName(importedPackageList, gp);
                Class<?> actualType = ClassManager.getClassByFullName(classFullName);
                
                TypeContext childType = new TypeContext();
                childType.setOwner(actualType);
                context.addChildType(childType);
            }
        }
        
        return context;
    }
    
    /**
     * 类的上下文
     */
    private static class TypeContext {
        private Class<?> owner;
        private List<TypeContext> childTypeList;
        
        public Class<?> getOwner() {
            return owner;
        }
        public void setOwner(Class<?> owner) {
            this.owner = owner;
        }
        public List<TypeContext> getChildTypeList() {
            return childTypeList;
        }
        public void setChildTypeList(List<TypeContext> childTypeList) {
            this.childTypeList = childTypeList;
        }
        public void addChildType(TypeContext childType) {
            if (getChildTypeList() == null) {
                setChildTypeList(new ArrayList<>());
            }
            
            getChildTypeList().add(childType);
        }
        public ResolvableType toResolvableType() {
            if (CollectionUtils.isEmpty(getChildTypeList())) {
                return ResolvableType.forClass(getOwner());
            }
            
            ResolvableType[] resolvableTypes = getChildTypeList().stream().map(t -> t.toResolvableType()).toArray(ResolvableType[]::new);
            
            logger.info("------>" + getOwner() + "---" + resolvableTypes.length);
            
            Arrays.stream(resolvableTypes).forEach(t -> logger.info("=======" + t));
            
            return ResolvableType.forClassWithGenerics(getOwner(), resolvableTypes);
        }
    }

    private static int getModifiers(String signature) {
        int modifier = 0;
        
        if (signature.startsWith("public ")) {
            modifier += Modifier.PUBLIC;
        }
        if (signature.startsWith("private ")) {
            modifier += Modifier.PRIVATE;
        }
        if (signature.startsWith("protected ")) {
            modifier += Modifier.PROTECTED;
        }
        if (signature.contains(" static ")) {
            modifier += Modifier.STATIC;
        }
        if (signature.contains(" final ")) {
            modifier += Modifier.FINAL;
        }
        
        return modifier;
    }
    
    private static Map<String, LcdpClassDescriptor> getGenericTypeMap(String classFullName, List<String> importedPackageList, String genericSignature, Map<String, LcdpClassDescriptor> genericMap) {
        if (isLcdpClass(classFullName)) { // 低代码开发的类，不会有泛型，只可能是泛型的实现类
            return getClassNameFromGenericSignature(genericSignature).stream().map(n -> {
                return getClassDescriptor(importedPackageList, n, genericMap);
            }).collect(Collectors.toMap(LcdpClassDescriptor::getClassName, d -> d));
        }
        
        Map<String, LcdpClassDescriptor> map = new HashMap<>();
        
        Class<?> clazz = ClassManager.getClassByFullName(classFullName);
        String str = clazz.toGenericString();
        
        logger.error("-----:::" + JSON.toJSONString(genericMap));
        
        logger.warn("getGenericTypeMap------>" + classFullName + "------->" + str + "---" + genericSignature);
        
        String genericString = StringUtils.substringsBetween(str, "<", ">").get(0).trim();
        
        String[] keys = genericString.split("\\s*,\\s*");
        
        logger.warn("============:::" + keys.length + "--->" + JSON.toJSONString(keys));
        
        List<String> actualClsssNameList = getClassNameFromGenericSignature(genericSignature);
        
        logger.warn("============actualClsssNameList:::" + JSON.toJSONString(actualClsssNameList));
        
        // 泛型的标识会改变，T、E等等
        for (int i = 0, j = keys.length; i < j; i++) {
            String key = keys[i];
            String actualClassName = actualClsssNameList.get(i);
            
            if (actualClassName.length() == 1) { // 泛型
                if (genericMap != null) {
                    if (genericMap.containsKey(actualClassName)) {
                        map.put(key, genericMap.get(actualClassName));
                    } else if (genericMap.containsKey(key)) {
                        map.put(key, genericMap.get(key));
                    }
                }
            } else {
                if (genericMap != null && genericMap.containsKey(key)) {
                    map.put(key, genericMap.get(key));
                } else {
                    map.put(key, getClassDescriptor(importedPackageList, actualClassName, genericMap));
                }
            }
        }
        
        return map;
    }
    

    
    /**
     * 从泛型签名中获取类名，如类List<String>, Map<String, Map<String, Sring>>中的
     * List<String> 和 Map<String, Map<String, Sring>>
     */
    private static List<String> getGenericPartList(String genericSignature) {
        if (genericSignature.startsWith("<")
                && genericSignature.endsWith(">")) {
            genericSignature = StringUtils.removeStart(genericSignature.trim(), "<");
            genericSignature = StringUtils.removeEnd(genericSignature.trim(), ">");
        }
        
        List<String> classNameList = new ArrayList<>();
        
        int left = 0;
        int right = 0;
        int comma = 0; // 是否有逗号
        String str = "";
        
        for (char c : genericSignature.trim().toCharArray()) {
            if (c == '<') {
                left++;
            } else if (c == '>') {
                right++;
            } else if (c == ',') {
                if (StringUtils.isBlank(str)) {
                    continue;
                }
                
                if (left == right) {
                    classNameList.add(str.trim());
                    
                    str = "";
                    left = 0;
                    right = 0;
                    comma = 0;
                    
                    continue;
                }
                
                comma++;
            } else if (Character.isWhitespace(c)) {
                if (StringUtils.isBlank(str)) {
                    continue;
                }
            }
            
            str += Character.toString(c);
            
            if (left == right
                    && (left > 0 || comma > 0)) {
                classNameList.add(str.trim());
                
                str = "";
                left = 0;
                right = 0;
                comma = 0;
            }
        }
        
        if (!StringUtils.isBlank(str)
                && left == right) {
            classNameList.add(str.trim());
        }
        
        return classNameList;
    }
}
