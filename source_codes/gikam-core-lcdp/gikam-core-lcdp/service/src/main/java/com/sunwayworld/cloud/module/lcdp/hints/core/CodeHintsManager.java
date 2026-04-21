package com.sunwayworld.cloud.module.lcdp.hints.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ResolvableType;

import com.sunwayworld.cloud.module.lcdp.hints.bean.CoreHintsChainingCallDTO;
import com.sunwayworld.cloud.module.lcdp.hints.bean.CoreHintsTypeDTO;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceClassInfoDTO;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpJavaCodeResolverUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpReflectionUtils;
import com.sunwayworld.framework.jdk.core.ClassManager;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.LcdpUtils;
import com.sunwayworld.framework.utils.ReflectionUtils;
import com.sunwayworld.framework.utils.SpringUtils;
import com.sunwayworld.framework.utils.StringUtils;

public class CodeHintsManager {
    private static final Logger logger = LogManager.getLogger(CodeHintsManager.class);
    
    public static CoreHintsChainingCallDTO callNext(CoreHintsChainingCallDTO chainingCall) {
        logger.warn("==============>>" + chainingCall.getCallType() + ":" + chainingCall.getChaining());
        
        if (chainingCall.getChaining() == null || chainingCall.getChaining().isEmpty()) {
            return chainingCall;
        }
        
        String next = chainingCall.getChaining().poll();
        
        logger.warn("------next>>>" + next);
        
        // this 一定只有第一级才有
        if ("this".equals(next)) {
            if (chainingCall.getLevel() > 0) {
                return null;
            }
        }
        
        // 调用默认方法
        if (chainingCall.getCallType().is(CodeHintsCallType.SUPER_DEFAULT)
                && !"super".equals(next)
                && !next.contains("(")) {
            return null;
        }
        
        if (chainingCall.getResolvableType() == null) {
            return callNextForSourceCode(chainingCall, next);
        } else { // 实际类型存在
            return callNextForClass(chainingCall, next);
        }
    }
    
    /**
     * 获取指定类里所有匹配的成员变量
     */
    public static List<Field> getMatchFieldList(Class<?> clazz, CodeHintsCallType calltype, Predicate<Field> predicate) {
        List<Field> fieldList = getFieldList(clazz);
        
        if (fieldList.isEmpty()) {
            return CollectionUtils.emptyList();
        }
        
        List<Field> returnList = new ArrayList<>();
        
        if (CodeHintsCallType.STATIC.equals(calltype)) {
            returnList.addAll(fieldList.stream()
                    .filter(f -> Modifier.isPublic(f.getModifiers())
                            && Modifier.isStatic(f.getModifiers())
                            && predicate.test(f))
                    .collect(Collectors.toList()));
        } else if (CodeHintsCallType.THIS.equals(calltype)) {
            returnList.addAll(fieldList.stream()
                    .filter(f -> (f.getDeclaringClass().equals(clazz)
                            || Modifier.isPublic(f.getModifiers())
                            || Modifier.isProtected(f.getModifiers()))
                            && predicate.test(f))
                    .collect(Collectors.toList()));
        } else if (CodeHintsCallType.SUPER.equals(calltype)) {
            returnList.addAll(fieldList.stream()
                    .filter(f -> (Modifier.isPublic(f.getModifiers())
                                    || Modifier.isProtected(f.getModifiers()))
                            && predicate.test(f))
                    .collect(Collectors.toList()));
        } else if (CodeHintsCallType.SUPER_DEFAULT.equals(calltype)) { // 不能调用成员变量
            return CollectionUtils.emptyList();
        } else {
            returnList.addAll(fieldList.stream()
                    .filter(f -> Modifier.isPublic(f.getModifiers())
                            && predicate.test(f))
                    .collect(Collectors.toList()));
        }
        
        // 删除相同名称，在父类中的成员变量
        returnList.removeIf(f -> returnList.stream().anyMatch(l -> !f.equals(l)
                && l.getName().equals(f.getName())
                && f.getDeclaringClass().isAssignableFrom(l.getDeclaringClass())));
        
        return returnList;
    }
    
    /**
     * 获取指定类里所有匹配的方法
     */
    public static List<Method> getMatchMethodList(Class<?> clazz, CodeHintsCallType calltype, Predicate<Method> methodPredicate) {

        List<Method> returnList = getAllMatchMethodList(clazz,calltype, methodPredicate);


        // 只保留最终的方法
        returnList.removeIf(m -> returnList.stream().anyMatch(n -> !m.equals(n)
                && m.getDeclaringClass().isAssignableFrom(n.getDeclaringClass())
                && ReflectionUtils.equals(m, n)));
        
        return returnList;
    }


    public static List<Method> getAllMatchMethodList(Class<?> clazz, CodeHintsCallType calltype, Predicate<Method> methodPredicate) {
        List<Method> methodList = getMethodList(clazz);

        if (methodList.isEmpty()) {
            return CollectionUtils.emptyList();
        }

        List<Method> returnList = new ArrayList<>();

        if (CodeHintsCallType.STATIC.equals(calltype)) {
            returnList.addAll(methodList.stream()
                    .filter(m -> Modifier.isPublic(m.getModifiers())
                            && Modifier.isStatic(m.getModifiers())
                            && methodPredicate.test(m))
                    .collect(Collectors.toList()));
        } else if (CodeHintsCallType.THIS.equals(calltype)) {
            returnList.addAll(methodList.stream()
                    .filter(m -> (m.getDeclaringClass().equals(clazz)
                            || Modifier.isPublic(m.getModifiers())
                            || Modifier.isProtected(m.getModifiers()))
                            && methodPredicate.test(m))
                    .collect(Collectors.toList()));
        } else if (CodeHintsCallType.SUPER.equals(calltype)) {
            returnList.addAll(methodList.stream()
                    .filter(m -> !(m.getDeclaringClass().equals(clazz)
                            || Modifier.isPublic(m.getModifiers())
                            || Modifier.isProtected(m.getModifiers()))
                            && methodPredicate.test(m))
                    .collect(Collectors.toList()));
        } else if (CodeHintsCallType.SUPER_DEFAULT.equals(calltype)) {
            returnList.addAll(methodList.stream()
                    .filter(m -> m.getDeclaringClass().isInterface()
                            && ReflectionUtils.isDefaultMethod(m)
                            && methodPredicate.test(m))
                    .collect(Collectors.toList()));
        } else {
            returnList.addAll(methodList.stream()
                    .filter(m -> Modifier.isPublic(m.getModifiers())
                            && methodPredicate.test(m))
                    .collect(Collectors.toList()));
        }

        return returnList;
    }




    private static final Pattern classNamePattern = Pattern.compile("[A-Z][A-Za-z0-9_]+");
    
    /**
     * 对于字符串类型，完善其中泛型的实现类的全称
     */
    public static String fullfillTypeName(String actualClassName, List<String> importedPackageList) {
        logger.info("----aaa>>>" + actualClassName);
        
        Matcher genericMatcher = ClassManager.classGenericPattern.matcher(actualClassName);
        while (genericMatcher.find()) { // 有泛型
            String genericPart = genericMatcher.group("genericPart");
            
            logger.info("----bbb>>>" + genericPart);
            
            StringBuffer sb = new StringBuffer();
            
            Matcher classNameMatcher = classNamePattern.matcher(genericPart);
            while(classNameMatcher.find()) {
                String group = classNameMatcher.group();
                

                logger.info("----cc>>>" + group);
                
                String classFullName = LcdpReflectionUtils.getClassFullName(importedPackageList, group);
                
                logger.info("----dd>>>" + classFullName);
                
                if (StringUtils.startsWith(classFullName, "@")) {
                    classFullName = classFullName.substring(1);
                }
                
                classNameMatcher.appendReplacement(sb, classFullName);

                logger.info("----ee>>>" + sb.toString());
                
                
            }
            

            logger.info("----xx>>>" + sb.toString());
            
            if (sb.length() > 0) {
                classNameMatcher.appendTail(sb);
                
                actualClassName = genericMatcher.replaceAll("<" + sb.toString() + ">");
            }
        }
        
        return actualClassName;
    }
    
    /**
     * 拆分调用链
     */
    public static List<String> splitChainingCall(String chainingCallLine) {
        if (StringUtils.isBlank(chainingCallLine)) {
            return CollectionUtils.emptyList();
        }
        
        List<String> chainingCallList = new ArrayList<>();

        int quotation = 0; // 双引号
        int bracketLeft = 0; // 左括号
        int bracketRight = 0; // 右括号
        int angleBracketLeft = 0; // 左尖括号
        int angleBracketRight = 0; // 右尖括号
        String str = "";
        
        for (char c : chainingCallLine.trim().toCharArray()) {
            if (quotation % 2 == 1) {
                str += Character.toString(c);
                
                if (c == '"') {
                    quotation++;
                }
            } else {
                if (c == '"') { // 双引号
                    quotation++;
                } else if (c == '(') {
                    bracketLeft++;
                } else if (c == ')') {
                    bracketRight++;
                } else if (c == '<') {
                    angleBracketLeft++;
                } else if (c == '>') {
                    angleBracketRight++;
                } else if (c == '.'
                        && (bracketLeft == bracketRight)
                        && (angleBracketLeft == angleBracketRight)) {
                    chainingCallList.add(str.trim());
                    
                    bracketLeft = 0;
                    bracketRight = 0;
                    angleBracketLeft = 0;
                    angleBracketRight = 0;
                    str = "";
                    
                    continue;
                }
                
                str += Character.toString(c);
            }
        }
        
        if (!StringUtils.isBlank(str)) {
            chainingCallList.add(str.trim());
        }
        
        return chainingCallList;
    }
    
    /**
     * 拆分方法的参数
     */
    public static List<String> splitMethodParams(String methodParams) {
        if (!StringUtils.contains(methodParams, ",")) {
            return Arrays.asList(methodParams);
        }
        
        List<String> methodParamList = new ArrayList<>();
        
        int left = 0;
        int right = 0;
        String str = "";
        
        for (char c : methodParams.trim().toCharArray()) {
            if (c == '<') {
                left++;
            } else if (c == '>') {
                right++;
            } else if (c == ',') {
                if (StringUtils.isBlank(str)) {
                    continue;
                }
                
                if (left == right) {
                    methodParamList.add(str.trim());
                    
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
            methodParamList.add(str.trim());
        }
        
        return methodParamList;
    }
    
    //------------------------------------------------------------------------------------------
    // 私有方法
    //------------------------------------------------------------------------------------------
    private static CoreHintsChainingCallDTO callNextForSourceCode(CoreHintsChainingCallDTO chainingCall, String next) {
        logger.warn("------->>>" + next + "---" + chainingCall.getCallType() + "---" + chainingCall.getLevel());
        
        if (next.equals("this")) {
            chainingCall.setCallType(CodeHintsCallType.THIS);
            chainingCall.setLevel(1);
            return callNext(chainingCall); 
        } else if (next.equals("super")) {
            if (chainingCall.getLevel() == 0) { // 调用父类的方法
                chainingCall.setCallType(CodeHintsCallType.SUPER);
                chainingCall.setLevel(1);
                
                return callNext(chainingCall);
            } else if (chainingCall.getLevel() == 1) { // 调用父类的默认方法
                if (chainingCall.getCallType().is(CodeHintsCallType.THIS)) {
                    return null;
                }
                chainingCall.setCallType(CodeHintsCallType.SUPER_DEFAULT);
                chainingCall.setLevel(2);
                
                return callNext(chainingCall);
            } else {
                return null;
            }
        } else if (next.startsWith("new ")) { // 不可能在这里
            return null;
        } else if (next.contains("(")) { // 调用方法
            int paramQty = LcdpReflectionUtils.getParamQty(next); // 参数数量
            
            List<String> importedPackageList = LcdpJavaCodeResolverUtils.getImportedPackageList(chainingCall.getSourceCode());
            
            String methodName = next.substring(0, next.indexOf("("));
            
            logger.warn("----->[" + methodName + "---" + paramQty + "]");
            
            if (chainingCall.getCallType().is(CodeHintsCallType.SUPER_DEFAULT)) { // 调用默认方法
                ResolvableType methodReturnType = null;
                
                String superClassFullName = LcdpReflectionUtils.getSuperClassFullName(chainingCall.getSourceCode());
                
                if (!StringUtils.isBlank(superClassFullName)) {
                    Class<?> superClass = ClassManager.getClassByFullName(superClassFullName);
                    
                    List<Method> superClassMethodList = getMatchMethodList(superClass,
                            CodeHintsCallType.SUPER_DEFAULT,
                            m -> methodName.equals(m.getName()) && LcdpReflectionUtils.matchParamQty(m, paramQty));
                    
                    // 如果返回值类型多于一条，不做提示
                    if (superClassMethodList.stream().map(m -> m.getReturnType()).distinct().count() > 1) {
                        return null;
                    }
                    
                    if (!superClassMethodList.isEmpty()) {
                        String superClassGenericPart = LcdpReflectionUtils.getSuperClassGenericPart(chainingCall.getSourceCode(), superClass.getSimpleName());
                        
                        ResolvableType superClassResolvableType = LcdpReflectionUtils.getResolvableType(superClass.getSimpleName()
                                + Optional.ofNullable(superClassGenericPart).orElse(""), importedPackageList);
                        
                        methodReturnType = ResolvableType.forType(superClassMethodList.get(0).getGenericReturnType(), superClassResolvableType);
                    }
                }
                
                List<String> interfaceClassFullNameList = LcdpReflectionUtils.getImplementedClassFullNameList(chainingCall.getSourceCode());
                for (String interfaceClassFullName : interfaceClassFullNameList) {
                    Class<?> interfaceClass = ClassManager.getClassByFullName(interfaceClassFullName);
                    
                    List<Method> interfaceDefaultMethodList = getMatchMethodList(interfaceClass,
                            CodeHintsCallType.SUPER_DEFAULT,
                            m -> methodName.equals(m.getName()) && LcdpReflectionUtils.matchParamQty(m, paramQty));
                    
                    // 如果返回值类型多于一条，不做提示
                    if (interfaceDefaultMethodList.stream().map(m -> m.getReturnType()).distinct().count() > 1) {
                        return null;
                    }
                    
                    if (!interfaceDefaultMethodList.isEmpty()) {
                        String interfaceGenericPart = LcdpReflectionUtils.getInterfacecClassGenericPart(chainingCall.getSourceCode(),
                                interfaceClass.getSimpleName());
                        
                        ResolvableType interfaceClassResolvableType = LcdpReflectionUtils.getResolvableType(interfaceClass.getSimpleName()
                                + Optional.ofNullable(interfaceGenericPart).orElse(""), importedPackageList);
                        
                        ResolvableType returnType = ResolvableType.forType(interfaceDefaultMethodList.get(0).getGenericReturnType(), interfaceClassResolvableType);
                        
                        // 返回值多于一条
                        if (methodReturnType != null && !methodReturnType.toString().equals(returnType.toString())) {
                            return null;
                        }
                        
                        if (methodReturnType == null) {
                            methodReturnType = returnType;
                        }
                    }
                }
                
                if (methodReturnType == null) { // 没有匹配的
                    return null;
                }
                
                CoreHintsChainingCallDTO nextChainingCall = new CoreHintsChainingCallDTO();
                nextChainingCall.setResolvableType(methodReturnType);
                nextChainingCall.setChaining(chainingCall.getChaining());
                nextChainingCall.setLevel(chainingCall.getLevel() + 1);
                nextChainingCall.setCallType(CodeHintsCallType.REGULAR);
                
                return callNext(nextChainingCall);
            } else if (chainingCall.getCallType().is(CodeHintsCallType.SUPER)) {  // 调用父类方法getSuperClassFullName
                String superClassFullName = LcdpReflectionUtils.getSuperClassFullName(chainingCall.getSourceCode());
                if (!StringUtils.isBlank(superClassFullName)) {
                    Class<?> superClass = ClassManager.getClassByFullName(superClassFullName);
                    
                    // 调用父类，不能是private
                    List<Method> superDefaultMethodList = getMatchMethodList(superClass,
                            CodeHintsCallType.REGULAR,
                            m -> methodName.equals(m.getName()) && LcdpReflectionUtils.matchParamQty(m, paramQty)
                            && !Modifier.isPrivate(m.getModifiers()));
                    
                    superDefaultMethodList.forEach(m -> logger.warn("============11>>>" + m));
                    
                    // 如果返回值类型多于一条，不做提示
                    if (superDefaultMethodList.stream().map(m -> m.getReturnType()).distinct().count() > 1) {
                        return null;
                    }
                    
                    if (superDefaultMethodList.size() == 1) {
                        String superClassGenericPart = LcdpReflectionUtils.getSuperClassGenericPart(chainingCall.getSourceCode(), superClass.getSimpleName());
                        
                        ResolvableType superClassResolvableType = LcdpReflectionUtils.getResolvableType(superClass.getSimpleName()
                                + Optional.ofNullable(superClassGenericPart).orElse(""), importedPackageList);
                        
                        CoreHintsChainingCallDTO nextChainingCall = new CoreHintsChainingCallDTO();
                        nextChainingCall.setResolvableType(superClassResolvableType);
                        nextChainingCall.setChaining(chainingCall.getChaining());
                        nextChainingCall.setLevel(chainingCall.getLevel() + 1);
                        nextChainingCall.setCallType(CodeHintsCallType.REGULAR);
                        
                        return callNext(nextChainingCall);
                    }
                }
                
                List<String> interfaceClassFullNameList = LcdpReflectionUtils.getImplementedClassFullNameList(chainingCall.getSourceCode());
                for (String interfaceClassFullName : interfaceClassFullNameList) {
                    Class<?> interfaceClass = ClassManager.getClassByFullName(interfaceClassFullName);
                    
                    List<Method> interfaceDefaultMethodList = getMatchMethodList(interfaceClass,
                            CodeHintsCallType.REGULAR,
                            m -> methodName.equals(m.getName()) && LcdpReflectionUtils.matchParamQty(m, paramQty));
                    
                    if (interfaceDefaultMethodList.isEmpty()) {
                        continue;
                    }
                    
                    // 如果返回值类型多于一条，不做提示
                    if (interfaceDefaultMethodList.stream().map(m -> m.getReturnType()).distinct().count() > 1) {
                        return null;
                    }
                    
                    if (interfaceDefaultMethodList.size() == 1) {
                        String interfaceGenericPart = LcdpReflectionUtils.getInterfacecClassGenericPart(chainingCall.getSourceCode(), interfaceClass.getSimpleName());
                        
                        ResolvableType interfaceResolvableType = LcdpReflectionUtils.getResolvableType(interfaceClass.getSimpleName()
                                + Optional.ofNullable(interfaceGenericPart).orElse(""), importedPackageList);
                        
                        CoreHintsChainingCallDTO nextChainingCall = new CoreHintsChainingCallDTO();
                        nextChainingCall.setResolvableType(interfaceResolvableType);
                        nextChainingCall.setChaining(chainingCall.getChaining());
                        nextChainingCall.setLevel(chainingCall.getLevel() + 1);
                        nextChainingCall.setCallType(CodeHintsCallType.REGULAR);
                        
                        return callNext(nextChainingCall);
                    }
                }
                
                return null;
            } else { // 调用其它方法
                Predicate<String> methodStringPredicate = null;
                if (chainingCall.getCallType().is(CodeHintsCallType.THIS)) {
                    methodStringPredicate = s -> s.contains(" " + methodName + "(");
                } else if (chainingCall.getCallType().is(CodeHintsCallType.STATIC)) {
                    methodStringPredicate = s -> s.startsWith("public ") && s.contains(" static ") && s.contains(" " + methodName + "(");
                } else {
                    methodStringPredicate = s -> s.startsWith("public ") && s.contains(" " + methodName + "(");
                }
                Predicate<Method> methodPredicate = null;
                if (chainingCall.getCallType().is(CodeHintsCallType.THIS)) {
                    methodPredicate = m -> m.getName().equals(methodName);
                } else if (chainingCall.getCallType().is(CodeHintsCallType.STATIC)) {
                    methodPredicate = m -> m.getName().equals(methodName) && Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers());
                } else {
                    methodPredicate = m -> m.getName().equals(methodName) && Modifier.isPublic(m.getModifiers());
                }
                
                List<ResolvableType> methodReturnTypeList = getLcdpMethodReturnTypeList(chainingCall.getSourceCode(), methodStringPredicate);
                
                logger.warn("------>>>" + methodReturnTypeList.size());
                
                // 如果返回值类型多于一条，不做提示
                if (methodReturnTypeList.stream().map(m -> m.toString()).distinct().count() > 1) {
                    return null;
                }
                
                // 被继承的类全称
                String superClassFullName = LcdpReflectionUtils.getSuperClassFullName(chainingCall.getSourceCode());
                if (!StringUtils.isBlank(superClassFullName)) {
                    Class<?> superClass = ClassManager.getClassByFullName(superClassFullName);
                    
                    List<Method> methodList = ReflectionUtils.getMethodList(superClass, methodPredicate);
                    if (!methodList.isEmpty()) {
                        if (methodList.stream().map(m -> m.getReturnType()).distinct().count() > 1) {
                            return null;
                        }
                        
                        String superClassGenericPart = LcdpReflectionUtils.getSuperClassGenericPart(chainingCall.getSourceCode(), superClass.getSimpleName());
                        
                        ResolvableType superClassResolvableType = LcdpReflectionUtils.getResolvableType(superClass.getSimpleName()
                                + Optional.ofNullable(superClassGenericPart).orElse(""), importedPackageList);
                        
                        ResolvableType returnType = ResolvableType.forType(methodList.get(0).getGenericReturnType(), superClassResolvableType);
                        
                        if (methodReturnTypeList.stream().noneMatch(r -> r.toString().equals(returnType.toString()))) {
                            methodReturnTypeList.add(returnType);
                            
                            // 如果返回值类型多于一条，不做提示
                            if (methodReturnTypeList.size() > 1) {
                                return null;
                            }
                        }
                    }
                }
                
                // 接口全称
                List<String> interfaceClassFullNameList = LcdpReflectionUtils.getImplementedClassFullNameList(chainingCall.getSourceCode());
                if (!interfaceClassFullNameList.isEmpty()) {
                    for (String interfaceClassFullName : interfaceClassFullNameList) {
                        Class<?> interfaceClass = ClassManager.getClassByFullName(interfaceClassFullName);
                        
                        List<Method> methodList = getMatchMethodList(interfaceClass,
                                chainingCall.getCallType(),
                                m -> methodName.equals(m.getName())
                                && LcdpReflectionUtils.matchParamQty(m, paramQty));
                        
                        if (!methodList.isEmpty()) {
                            if (methodList.stream().map(m -> m.getReturnType()).distinct().count() > 1) {
                                return null;
                            }
                            
                            String interfaceGenericPart = LcdpReflectionUtils.getInterfacecClassGenericPart(chainingCall.getSourceCode(), interfaceClass.getSimpleName());
                            
                            ResolvableType interfaceResolvableType = LcdpReflectionUtils.getResolvableType(Optional.ofNullable(interfaceGenericPart)
                                    .orElse(interfaceClass.getSimpleName()), importedPackageList);
                            
                            ResolvableType returnType = ResolvableType.forType(methodList.get(0).getGenericReturnType(), interfaceResolvableType);
                            
                            if (methodReturnTypeList.stream().noneMatch(r -> r.toString().equals(returnType.toString()))) {
                                methodReturnTypeList.add(returnType);
                                
                                // 如果返回值类型多于一条，不做提示
                                if (methodReturnTypeList.size() > 1) {
                                    return null;
                                }
                            }
                        }
                    }
                }
                
                if (methodReturnTypeList.isEmpty()) {
                    return null;
                }
                
                CoreHintsChainingCallDTO nextChainingCall = new CoreHintsChainingCallDTO();
                nextChainingCall.setResolvableType(methodReturnTypeList.get(0));
                nextChainingCall.setChaining(chainingCall.getChaining());
                nextChainingCall.setLevel(chainingCall.getLevel() + 1);
                nextChainingCall.setCallType(CodeHintsCallType.REGULAR);
                
                return callNext(nextChainingCall);
            }
        } else { // 变量
            if (chainingCall.getCallType().is(CodeHintsCallType.THIS)
                    || chainingCall.getCallType().is(CodeHintsCallType.SUPER)
                    || chainingCall.getCallType().is(CodeHintsCallType.STATIC)) {
                CoreHintsChainingCallDTO nextChainingCall = new CoreHintsChainingCallDTO();
                
                CoreHintsTypeDTO hintsType = getFieldTypeBySourceCode(chainingCall, next);
                
                if (hintsType == null) {
                    return null;
                }
                
                if (hintsType.getResolvableType() != null) {
                    nextChainingCall.setResolvableType(hintsType.getResolvableType());
                } else {
                    nextChainingCall.setSourceCode(hintsType.getSourceCode());
                }
                
                nextChainingCall.setChaining(chainingCall.getChaining());
                nextChainingCall.setLevel(chainingCall.getLevel() + 1);
                nextChainingCall.setCallType(CodeHintsCallType.REGULAR);
                
                return callNext(nextChainingCall);
            } else if (chainingCall.getCallType().is(CodeHintsCallType.SUPER_DEFAULT)) { // 不可能到这里
                return null;
            }
            
            if (chainingCall.getLevel() == 0) { // 局部变量或类名开头
                String classFullName = ClassManager.getClassFullName(chainingCall.getSourceCode());
                
                if (StringUtils.endsWith(classFullName, "." + next)) { // 类名开头
                    chainingCall.setLevel(chainingCall.getLevel() + 1);
                    return callNext(chainingCall);
                }
            }
            
            CoreHintsTypeDTO hintsType = getFieldTypeBySourceCode(chainingCall, next);
            if (hintsType == null) { // 类名开头
                chainingCall.setCallType(CodeHintsCallType.STATIC);
                chainingCall.setLevel(chainingCall.getLevel() + 1);
                return callNext(chainingCall);
            }
            
            CoreHintsChainingCallDTO nextChainingCall = new CoreHintsChainingCallDTO();
            
            if (hintsType.getResolvableType() != null) {
                nextChainingCall.setResolvableType(hintsType.getResolvableType());
            } else {
                nextChainingCall.setSourceCode(hintsType.getSourceCode());
            }
            
            nextChainingCall.setChaining(chainingCall.getChaining());
            nextChainingCall.setLevel(chainingCall.getLevel() + 1);
            nextChainingCall.setCallType(CodeHintsCallType.REGULAR);
            
            return callNext(nextChainingCall);
        }

    }
    
    private static CoreHintsChainingCallDTO callNextForClass(CoreHintsChainingCallDTO chainingCall, String next) {
        logger.warn("------->>>" + next + "---" + chainingCall.getCallType() + "---" + chainingCall.getLevel());
        
        if (next.equals("this")) { // 实际类时，不会有this开头的
            return null;
        } else if (next.equals("super")) {
            if (chainingCall.getLevel() == 1) { // 调用父类的默认方法
                if (chainingCall.getCallType().is(CodeHintsCallType.THIS)) {
                    return null;
                }
                
                chainingCall.setCallType(CodeHintsCallType.SUPER_DEFAULT);
                chainingCall.setLevel(2);
                
                return callNext(chainingCall);
            } else { // 其它都不应该被调用
                return null;
            }
        } else if (next.startsWith("new ")) {
            chainingCall.setCallType(CodeHintsCallType.REGULAR);
            chainingCall.setLevel(1);
            
            return callNext(chainingCall);
        } else if (next.contains("(")) { // 调用方法
            int paramQty = LcdpReflectionUtils.getParamQty(next); // 参数数量
            
            String methodName = next.substring(0, next.indexOf("("));
            
            logger.warn("---aa>" + methodName + "---" + paramQty);
            
            List<Method> methodList = getMatchMethodList(chainingCall.getResolvableType().resolve(),
                    chainingCall.getCallType(),
                    m -> m.getName().equals(methodName) && LcdpReflectionUtils.matchParamQty(m, paramQty));
            
            logger.warn("--->" + methodList.size());
            
            methodList.forEach(m -> logger.warn("===>" + m.toGenericString()));
            
            if (methodList.isEmpty()) {
                return null;
            }
            
            // 如果返回值类型多于一条，不做提示
            if (methodList.stream().map(m -> m.getReturnType()).distinct().count() > 1) {
                return null;
            }
            
            Method method = methodList.get(0);
            
            CoreHintsChainingCallDTO nextChainingCall = new CoreHintsChainingCallDTO();
            
            if (Modifier.isStatic(method.getModifiers())) {
                ResolvableType returnType = ResolvableType.forMethodReturnType(method);
                nextChainingCall.setResolvableType(returnType);;
            } else {
                ResolvableType returnType = ReflectionUtils.getActualResolvableType(chainingCall.getResolvableType(), method.getGenericReturnType());
                nextChainingCall.setResolvableType(returnType);;
            }
            
            nextChainingCall.setChaining(chainingCall.getChaining());
            nextChainingCall.setLevel(chainingCall.getLevel() + 1);
            nextChainingCall.setCallType(CodeHintsCallType.REGULAR);
            
            return callNext(nextChainingCall);
        } else { // 变量
            if (chainingCall.getLevel() == 0) {
                Class<?> clazz = chainingCall.getResolvableType().resolve();
                
                if (Objects.equals(clazz.getSimpleName(), next) // 变量名=类名
                        || (LcdpReflectionUtils.isLcdpClass(clazz.getName())
                                && StringUtils.endsWith(LcdpReflectionUtils.getLcdpPath(clazz.getName()), "." + next))) { // 低代码开发的类名
                    chainingCall.setCallType(CodeHintsCallType.STATIC);
                    chainingCall.setLevel(chainingCall.getLevel() + 1);
                    return callNext(chainingCall);
                } else {
                    if (!chainingCall.getCallType().is(CodeHintsCallType.STATIC)) { // 局部变量
                        chainingCall.setCallType(CodeHintsCallType.REGULAR);
                        chainingCall.setLevel(chainingCall.getLevel() + 1);
                        return callNext(chainingCall);
                    }
                }
            }
            
            List<Field> fieldList = getMatchFieldList(chainingCall.getResolvableType().resolve(), chainingCall.getCallType(), f -> f.getName().equals(next));
            
            fieldList.forEach(f -> logger.warn("---------->" + f));
            
            if (fieldList.size() != 1) {
                return null;
            }
            
            Field field = fieldList.get(0);
            
            ResolvableType fieldType = ResolvableType.forField(field, chainingCall.getResolvableType());
            
            CoreHintsChainingCallDTO nextChainingCall = new CoreHintsChainingCallDTO();
            nextChainingCall.setResolvableType(fieldType);
            nextChainingCall.setChaining(chainingCall.getChaining());
            nextChainingCall.setLevel(chainingCall.getLevel() + 1);
            nextChainingCall.setCallType(CodeHintsCallType.REGULAR);
            
            return callNext(nextChainingCall);
        }
    }
    
    private static List<Method> getMethodList(Class<?> clazz) {
        List<Method> methodList = new ArrayList<>();
        
        extractMethod(methodList, clazz);
        
        return methodList;
    }
    
    private static void extractMethod(List<Method> methodList, Class<?> clazz) {
        if (clazz == null
                || Object.class.equals(clazz)) {
            return;
        }
        
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().startsWith("lambda$")) {
                continue;
            }
            
            if (!methodList.contains(method)) {
                methodList.add(method);
            }
        }
        
        extractMethod(methodList, clazz.getSuperclass());
        
        if (clazz.getInterfaces() != null) {
            for (Class<?> iClazz : clazz.getInterfaces()) {
                extractMethod(methodList, iClazz);
            }
        }
    }
    
    private static List<Field> getFieldList(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        
        extractField(fieldList, clazz);
        
        return fieldList;
    }
    
    private static void extractField(List<Field> fieldList, Class<?> clazz) {
        if (clazz == null
                || Object.class.equals(clazz)) {
            return;
        }
        
        for (Field field : clazz.getDeclaredFields()) {
            if (!fieldList.contains(field)) {
                fieldList.add(field);
            }
        }
        
        extractField(fieldList, clazz.getSuperclass());
        
        if (clazz.getInterfaces() != null) {
            for (Class<?> iClazz : clazz.getInterfaces()) {
                extractField(fieldList, iClazz);
            }
        }
    }
    
    /**
     * 获取源代码中，低代码中开发的方法
     */
    private static final List<ResolvableType> getLcdpMethodReturnTypeList(String sourceCode, Predicate<String> predicate) {
        List<ResolvableType> returnTypeList = new ArrayList<>();
        
        List<String> importedPackageList = LcdpJavaCodeResolverUtils.getImportedPackageList(sourceCode);
        
        // 提取源代码中的方法
        Matcher methodMatcher = ClassManager.classMethodPattern.matcher(sourceCode);
        while(methodMatcher.find()) {
            String group = methodMatcher.group();
            
            logger.warn("---method>" + group);
            
            if (!predicate.test(group)) {
                continue;
            }
            
            String returnType = methodMatcher.group("returnType");
            
            returnTypeList.add(LcdpReflectionUtils.getResolvableType(returnType, importedPackageList));
        }
        
        return returnTypeList;
    }
    
    /**
     * 获取源代码中，低代码中开发的静态变量
     */
    private static final List<CoreHintsTypeDTO> getLcdpTypeList(String sourceCode, Predicate<String> predicate) {
        List<CoreHintsTypeDTO> returnTypeList = new ArrayList<>();
        
        List<String> importedPackageList = LcdpJavaCodeResolverUtils.getImportedPackageList(sourceCode);
        
        // 提取源代码中的方法
        Matcher fieldMatcher = ClassManager.classFieldPattern.matcher(sourceCode);
        while(fieldMatcher.find()) {
            String group = fieldMatcher.group();
            
            logger.warn("======[" + group + "]");
            
            if (!predicate.test(group)) {
                continue;
            }
            
            String fieldType = fieldMatcher.group("fieldType");
            
            logger.warn("------------->:" + fieldType);
            
            CoreHintsTypeDTO hintsType = new CoreHintsTypeDTO();
            try {
                ResolvableType resolvableType = LcdpReflectionUtils.getResolvableType(fieldType, importedPackageList);
                
                hintsType.setResolvableType(resolvableType);
                
                returnTypeList.add(hintsType);
            } catch (Exception ex) {
                String classFullName = LcdpReflectionUtils.getClassFullName(importedPackageList, fieldType);
                
                if (StringUtils.isBlank(classFullName)) {
                    continue;
                }
                
                if (classFullName.startsWith("@") ) { // 低代码开发的类
                    String path = classFullName.substring(1);
                    
                    LcdpResourceClassInfoDTO classInfo = SpringUtils.getBean(LcdpResourceService.class).getActiveClassInfoByPath(path,
                            LcdpUtils.isDebugRequest(), false);
                    
                    hintsType.setSourceCode(classInfo.getSourceCode());
                    
                    returnTypeList.add(hintsType);
                }
            }
        }
        
        return returnTypeList;
    }
    
    private static CoreHintsTypeDTO getFieldTypeBySourceCode(CoreHintsChainingCallDTO chainingCall, String next) {
        Predicate<String> predicate = null;
        if (chainingCall.getCallType().is(CodeHintsCallType.STATIC)) {
            predicate = f -> f.matches("public\\s+([A-Za-z0-9_]+\\s+)?static\\s+([A-Za-z0-9_]+\\s+)?([A-Za-z0-9_]+(\\<.*\\>)?)\\s+" + next + "\\s*(;|=)");
        } else if (chainingCall.getCallType().is(CodeHintsCallType.THIS)) {
            predicate = f -> f.matches("([A-Za-z0-9_]+\\s+){0,3}([A-Za-z0-9_]+(\\<.*\\>)?)\\s+" + next + "\\s*(;|=)");
        } else if (chainingCall.getCallType().is(CodeHintsCallType.SUPER)) {
            predicate = f -> false;
        } else if (chainingCall.getCallType().is(CodeHintsCallType.SUPER_DEFAULT)) {
            predicate = f -> false;
        } else {
            predicate = f -> f.matches("public\\s+([A-Za-z0-9_]+\\s+){0,3}" + next + "\\s*(;|=)");
        }
        List<CoreHintsTypeDTO> fieldTypeList = getLcdpTypeList(chainingCall.getSourceCode(), predicate);
        
        logger.warn("---------fieldTypeList.size()>>>" + fieldTypeList.size());
        
        // 如果返回值类型多于一条，不做提示
        if (fieldTypeList.stream().map(m -> m.toString()).distinct().count() > 1) {
            return null;
        }
        
        if (!fieldTypeList.isEmpty()) {
            return fieldTypeList.get(0);
        }
        
        List<String> importedPackageList = LcdpJavaCodeResolverUtils.getImportedPackageList(chainingCall.getSourceCode());
        
        // 被继承的类全称
        String superClassFullName = LcdpReflectionUtils.getSuperClassFullName(chainingCall.getSourceCode());
        if (!StringUtils.isBlank(superClassFullName)) {
            Class<?> superClass = ClassManager.getClassByFullName(superClassFullName);
            
            // 如果是调用super时，不能调用实现类的
            Predicate<Field> fieldPredicate = chainingCall.getCallType().is(CodeHintsCallType.SUPER)
                    ? f -> f.getName().equals(next)
                            && (superClass.equals(f.getDeclaringClass()) || !superClass.isAssignableFrom(f.getDeclaringClass()))
                            : f -> f.getName().equals(next);
            
            List<Field> fieldList = getMatchFieldList(superClass, chainingCall.getCallType(), fieldPredicate);
            
            if (fieldList.stream().map(f -> f.getType()).distinct().count() > 1) {
                return null;
            }
            
            if (!fieldList.isEmpty()) {
                Field field = fieldList.get(0);
                
                String superClassGenericPart = LcdpReflectionUtils.getSuperClassGenericPart(chainingCall.getSourceCode(), superClass.getSimpleName());
                
                ResolvableType superClassResolvableType = LcdpReflectionUtils.getResolvableType(superClass.getSimpleName()
                        + Optional.ofNullable(superClassGenericPart).orElse(""), importedPackageList);
                
                CoreHintsTypeDTO hintsType = new CoreHintsTypeDTO();
                hintsType.setResolvableType(ResolvableType.forType(field.getGenericType(), superClassResolvableType));
                return hintsType;
            }
        }
        
        // 接口全称
        List<String> interfaceClassFullNameList = LcdpReflectionUtils.getImplementedClassFullNameList(chainingCall.getSourceCode());
        if (!interfaceClassFullNameList.isEmpty()) {
            for (String interfaceClassFullName : interfaceClassFullNameList) {
                Class<?> interfaceClass = ClassManager.getClassByFullName(interfaceClassFullName);
                
                List<Field> fieldList = getMatchFieldList(interfaceClass, chainingCall.getCallType(), f -> f.getName().equals(next));
                
                if (fieldList.isEmpty()) {
                    continue;
                }
                
                // 如果返回值类型多于一条，不做提示
                if (fieldList.stream().map(f -> f.getType()).distinct().count() > 1) {
                    return null;
                }
                
                if (!fieldList.isEmpty()) {
                    String interfaceGenericPart = LcdpReflectionUtils.getInterfacecClassGenericPart(chainingCall.getSourceCode(), interfaceClass.getSimpleName());
                    
                    ResolvableType interfaceResolvableType = LcdpReflectionUtils.getResolvableType(Optional.ofNullable(interfaceGenericPart).orElse(interfaceClass.getSimpleName()), importedPackageList);
                    
                    CoreHintsTypeDTO hintsType = new CoreHintsTypeDTO();
                    hintsType.setResolvableType(ResolvableType.forType(fieldList.get(0).getGenericType(), interfaceResolvableType));
                    return hintsType;
                }
            }
        }
        
        return null;
    }
}
