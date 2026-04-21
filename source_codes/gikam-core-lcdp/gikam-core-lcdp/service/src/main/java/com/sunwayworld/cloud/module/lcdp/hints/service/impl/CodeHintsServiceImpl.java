package com.sunwayworld.cloud.module.lcdp.hints.service.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Repository;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import com.sunwayworld.cloud.module.lcdp.errorscript.bean.LcdpErrorScriptBean;
import com.sunwayworld.cloud.module.lcdp.errorscript.service.LcdpErrorScriptService;
import com.sunwayworld.cloud.module.lcdp.hints.bean.CodeExecutableSourceCodeDTO;
import com.sunwayworld.cloud.module.lcdp.hints.bean.CodeHintsSourceCodeDTO;
import com.sunwayworld.cloud.module.lcdp.hints.bean.CodeHintsTextDTO;
import com.sunwayworld.cloud.module.lcdp.hints.bean.CodeSourceCodeDTO;
import com.sunwayworld.cloud.module.lcdp.hints.bean.CoreHintsChainingCallDTO;
import com.sunwayworld.cloud.module.lcdp.hints.core.CodeHintsCallType;
import com.sunwayworld.cloud.module.lcdp.hints.core.CodeHintsManager;
import com.sunwayworld.cloud.module.lcdp.hints.core.CoreSourceCodeHelper;
import com.sunwayworld.cloud.module.lcdp.hints.service.CodeHintsService;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceClassInfoDTO;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpJavaCodeResolverUtils;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpReflectionUtils;
import com.sunwayworld.cloud.module.lcdp.resourcelock.service.LcdpResourceLockService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.database.sql.Order;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.io.file.FileScope;
import com.sunwayworld.framework.jdk.core.ClassManager;
import com.sunwayworld.framework.mybatis.mapper.MatchPattern;
import com.sunwayworld.framework.mybatis.mapper.SearchFilter;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.utils.ArrayUtils;
import com.sunwayworld.framework.utils.ClassUtils;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.JsonUtils;
import com.sunwayworld.framework.utils.LcdpUtils;
import com.sunwayworld.framework.utils.ReflectionUtils;
import com.sunwayworld.framework.utils.StringUtils;
import com.sunwayworld.module.item.file.bean.CoreFileBean;
import com.sunwayworld.module.item.file.service.CoreFileService;
import com.sunwayworld.module.item.file.utils.CoreFileUtils;

@Repository
public class CodeHintsServiceImpl implements CodeHintsService {
    private static final Logger logger = LoggerFactory.getLogger(CodeHintsService.class);

    public static final Map<String, String> BUILTIN_IMPORT_MAP = new HashMap<>();

    static {
        BUILTIN_IMPORT_MAP.put("String", "java.lang.String");
        BUILTIN_IMPORT_MAP.put("StringBuilder", "java.lang.StringBuilder");
        BUILTIN_IMPORT_MAP.put("StringBuffer", "java.lang.StringBuffer");
        BUILTIN_IMPORT_MAP.put("Thread", "java.lang.Thread");
        BUILTIN_IMPORT_MAP.put("ThreadLocal", "java.lang.ThreadLocal");
        BUILTIN_IMPORT_MAP.put("System", "java.lang.System");
        BUILTIN_IMPORT_MAP.put("RuntimeException", "java.lang.RuntimeException");
        BUILTIN_IMPORT_MAP.put("Runnable", "java.lang.Runnable");
        BUILTIN_IMPORT_MAP.put("Object", "java.lang.Object");
        BUILTIN_IMPORT_MAP.put("Class", "java.lang.Class");
    }

    public static final List<String> IGNORE_IMPORT_NAME = new ArrayList<>();

    static {
        IGNORE_IMPORT_NAME.add("Override");
    }

    @Lazy
    @Autowired
    private LcdpResourceService resourceService;

    @Autowired
    private CoreFileService fileService;

    @Autowired
    private LcdpErrorScriptService lcdpErrorScriptService;


    @Autowired
    private LcdpResourceLockService lcdpResourceLockService;


    @Override
    public List<String> getImportList(RestJsonWrapperBean jsonWrapper) {
        String className = jsonWrapper.getParamValue("className");

        if (IGNORE_IMPORT_NAME.contains(className)) {
            return CollectionUtils.emptyList();
        }

        String classFullName = BUILTIN_IMPORT_MAP.get(className);
        if (!StringUtils.isBlank(classFullName)) {
            return ArrayUtils.asList(classFullName);
        }

        List<String> classFullNameList = ClassManager.getClassFullNameList(className);

        if (!classFullNameList.isEmpty()) {
            return classFullNameList
                    .stream()
                    .map(n -> {
                        if (n.contains("$")) { // 内部类
                            return StringUtils.replace(n, "$", ".");
                        } else {
                            return n;
                        }
                    })
                    .sorted()
                    .collect(Collectors.toList());
        }

        return resourceService.getPathListByClassName(className)
                .stream()
                .map(n -> "@" + n)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<CodeHintsTextDTO> getHintsClassList(RestJsonWrapperBean jsonWrapper) {
        String line = jsonWrapper.getParamValue("line");

        if (StringUtils.isBlank(line)) {
            return null;
        }

        CoreHintsChainingCallDTO chainingCall = new CoreHintsChainingCallDTO();

        String sourceCode = jsonWrapper.getParamValue("content");
        if (!StringUtils.isBlank(sourceCode)) {
            chainingCall.setSourceCode(sourceCode);
        } else {
            String className = jsonWrapper.getParamValue("className");
            String packages = jsonWrapper.getParamValue("packages");

            List<String> packageList = (StringUtils.isBlank(packages)
                    ? CollectionUtils.emptyList()
                    : JsonUtils.parseList(packages, String.class));

            List<ClassFullNameDTO> classFullNameList = getClassFullNameList(packageList, className);

            if (classFullNameList.size() != 1) {
                return null;
            }

            ClassFullNameDTO classFullName = classFullNameList.get(0);

            if (classFullName.isLcdpPath()) {
                LcdpResourceClassInfoDTO classInfo = resourceService.getActiveClassInfoByPath(classFullName.getName(), LcdpUtils.isDebugRequest());

                if (classInfo.getClazz() == null) {
                    chainingCall.setSourceCode(classInfo.getSourceCode());
                } else {
                    chainingCall.setResolvableType(ResolvableType.forClass(classInfo.getClazz()));
                }
            } else {
                chainingCall.setResolvableType(LcdpReflectionUtils.getResolvableType(className, packageList));

                // 私有类
                if (chainingCall.getResolvableType() == null
                        || Modifier.isPrivate(chainingCall.getResolvableType().resolve().getModifiers())) {
                    return null;
                }
            }
        }

        CodeHintsManager.splitChainingCall(line).forEach(c -> chainingCall.addNextChain(c));
        chainingCall.setCallType(getCallType(line));

        CoreHintsChainingCallDTO finalChainingCall = CodeHintsManager.callNext(chainingCall);

        if (finalChainingCall == null) {
            return null;
        }

        List<CodeHintsTextDTO> hintsTextList = new ArrayList<>();
        if (finalChainingCall.getResolvableType() != null) { // 已编译
            getCodeHintsListByClass(finalChainingCall.getResolvableType(), finalChainingCall.getCallType()).forEach(t -> hintsTextList.add(t));
        } else { // 没有编译，只有源代码
            if (!StringUtils.isBlank(finalChainingCall.getSourceCode())) {
                getCodeHintsListBySourceCode(finalChainingCall.getSourceCode(),
                        finalChainingCall.getCallType()).forEach(t -> hintsTextList.add(t));
            }
        }

        hintsTextList.sort(textComparator);

        return hintsTextList;
    }

    @Override
    public List<String> getOptimizedImportList(RestJsonWrapperBean jsonWrapper) {
        String sourceCode = jsonWrapper.getParamValue("content");

        return LcdpJavaCodeResolverUtils.getOptimizedImportList(sourceCode);
    }

    @Override
    public List<String> getHintsClassFullNameListByStartText(RestJsonWrapperBean jsonWrapper) {
        String startText = jsonWrapper.getParamValue("startText");

        if (StringUtils.isBlank(startText) || startText.length() <= 2) {
            return CollectionUtils.emptyList();
        }

        List<String> matchedClassFullNameList = ClassManager.getMatchedClassFullNameList(n -> StringUtils.startsWithIgnoreCase(n, startText));

        matchedClassFullNameList.removeIf(c -> {
            Class<?> clazz = ClassManager.getClassByFullName(c);

            return clazz == null || !Modifier.isPublic(clazz.getModifiers());
        });

        List<String> highestPriorityList = Arrays.asList(ClassManager.HIGHEST_PRIORITY_CLASS_FULLNAMES);
        Comparator<String> comparator = (n1, n2) -> {
            String s1 = ClassManager.getSimpleName(n1);
            String s2 = ClassManager.getSimpleName(n2);

            if (startText.equals(s1)) {
                if (startText.equals(s2)) {
                    if (highestPriorityList.contains(n1)) {
                        return -1;
                    } else if (highestPriorityList.contains(n2)) {
                        return 1;
                    }
                }

                return 1;
            }

            if (startText.equals(s2)) {
                return -1;
            }

            if (highestPriorityList.contains(n1)) {
                return -1;
            } else if (highestPriorityList.contains(n2)) {
                return 1;
            }

            if (Objects.equals(s1, s2)) {
                return n1.compareTo(n2);
            }

            return s1.compareTo(s2);
        };

        if (matchedClassFullNameList.size() > 100) {
            matchedClassFullNameList = matchedClassFullNameList.stream().sorted(comparator).limit(100).collect(Collectors.toList());
        }

        return matchedClassFullNameList.stream()
                .sorted(comparator)
                .map(n -> {
                    if (LcdpReflectionUtils.isLcdpClass(n)) { // 如果是低代码开发的类，去掉只留path
                        return "l#@" + LcdpReflectionUtils.getLcdpPath(n);
                    } else {
                        try {
                            Class<?> clazz = ClassManager.getClassByFullName(n);

                            if (clazz == null) {
                                logger.debug("类名[{}]对应的类不存在", n);

                                return null;
                            }

                            if (n.contains("$")) { // 内部类
                                n = StringUtils.replace(n, "$", ".");
                            }

                            int modifiers = clazz.getModifiers();

                            if (clazz.isAnnotation()) {
                                return "an#" + n;
                            } else if (clazz.isEnum()) {
                                return "e#" + n;
                            } else if (Modifier.isInterface(modifiers)) {
                                return "i#" + n;
                            } else if (Modifier.isAbstract(modifiers)) {
                                return "ab#" + n;
                            }

                            return n;
                        } catch (Exception ex) {
                            logger.debug("类名[{}]对应的类不存在", n, ex);

                            return null;
                        }
                    }
                })
                .filter(n -> n != null)
                .collect(Collectors.toList());
    }

    @Override
    public CodeHintsSourceCodeDTO getSourceCode(RestJsonWrapperBean jsonWrapper) {
        String className = jsonWrapper.getParamValue("className");

        // 获取已导入的依赖包，包括低代码中开发的类
        String packages = jsonWrapper.getParamValue("packages");

        List<String> packageList = (StringUtils.isBlank(packages)
                ? CollectionUtils.emptyList()
                : JsonUtils.parseList(packages, String.class));

        List<ClassFullNameDTO> classFullNameList = getClassFullNameList(packageList, className);

        if (classFullNameList.isEmpty()) {
            throw new ApplicationRuntimeException("LCDP.EXCEPTION.CLASS_FOR_CLASSNAME_NOTEXISTS_OR_UNIMPORTED", className);
        } else if (classFullNameList.size() > 1) {
            throw new ApplicationRuntimeException("LCDP.EXCEPTION.MORE_THAN_ONE_CLASS_FOR_CLASSNAME", className);
        }

        ClassFullNameDTO dto = classFullNameList.get(0);

        CodeHintsSourceCodeDTO hintsSourceCode = new CodeHintsSourceCodeDTO();

        if (dto.isLcdpPath()) {
            LcdpResourceClassInfoDTO classInfo = resourceService.getActiveClassInfoByPath(dto.getName(), LcdpUtils.isDebugRequest());

            hintsSourceCode.setResourceId(classInfo.getResourceId());
        } else {
            String jarName = ClassManager.getJarNameIfPresent(dto.getName());

            if (jarName == null) {
                throw new ApplicationRuntimeException("LCDP.EXCEPTION.NOT_FOUND_JAR_FOR_CLASSNAME", dto.getName());
            }

            //替换掉jarName可能存在的感叹号（!用来分隔包路径和包内路径
            jarName = jarName.replace("!", "");

            //获取源代码的文件
            SearchFilter jarFilter = SearchFilter.instance().match("NAME", jarName).filter(MatchPattern.SEQ)
                    .match("SCOPE", FileScope.src.name()).filter(MatchPattern.SEQ)
                    .match("FILEEXT", "jar").filter(MatchPattern.SEQ);

            CoreFileBean jarFileBean = fileService.selectFirstByFilter(jarFilter, Order.desc("VERSION"));

            if (jarFileBean == null) {
                logger.warn("找不到源代码jar包[" + jarName + "]");
                throw new ApplicationRuntimeException("LCDP.EXCEPTION.NEED_SOURCE_CODE", jarName);
            }

            String jarFilePath = CoreFileUtils.getLocalPath(jarFileBean).toString();

            hintsSourceCode.setSourceCode(ClassManager.getJavaSourceCode(jarFilePath, dto.getName()));
        }

        return hintsSourceCode;
    }

    @Override
    public CodeExecutableSourceCodeDTO getExecutableSourceCode(RestJsonWrapperBean jsonWrapper) {
        String path = jsonWrapper.getParamValue("path");

        LcdpResourceClassInfoDTO classInfo = resourceService.getActiveClassInfoByPath(path, LcdpUtils.isDebugRequest(), false);

        CodeExecutableSourceCodeDTO executableSourceCodeDTO = new CodeExecutableSourceCodeDTO();
        executableSourceCodeDTO.setExecutableSourceCode(classInfo.getClassSourceCode());

        // 根据path获取
        LcdpResourceBean resource = resourceService.getByPath(path);

        String scriptStatus = "submit";

        if (Constant.YES.equals(lcdpResourceLockService.getLockStatus(LocalContextHelper.getLoginUserId(), resource.getId().toString(), LcdpConstant.RESOURCE_CATEGORY_JAVA))) {
            scriptStatus = "checkout";
        }

        LcdpErrorScriptBean errorScript = lcdpErrorScriptService.selectFirstByFilter(SearchFilter.instance().match("SERVERSCRIPTID", resource.getId()).filter(MatchPattern.EQ).match("SCRIPTSTATUS", scriptStatus).filter(MatchPattern.EQ), Order.desc("ID"));

        if (errorScript != null) {
            executableSourceCodeDTO.setErrorLog(errorScript.getErrorLog());
        }


        return executableSourceCodeDTO;
    }

    @Override
    public String getFormattedSourceCode(RestJsonWrapperBean jsonWrapper) {
        String sourceCode = jsonWrapper.getParamValue("content");

        if (StringUtils.isEmpty(sourceCode)) {
            return null;
        }

        try {
            sourceCode = new Formatter(JavaFormatterOptions.builder().style(JavaFormatterOptions.Style.AOSP).build()).formatSource(sourceCode);

            return sourceCode.replaceAll("@Autowired", "@Autowired\n    ");
        } catch (FormatterException e) {
            return sourceCode;
        }
    }

    @Override
    public List<CodeHintsTextDTO> getSourceCodeHintsByMethodOrField(RestJsonWrapperBean jsonWrapper) {
        String line = jsonWrapper.getParamValue("line");

        if (StringUtils.isBlank(line)) {
            return null;
        }

        CoreHintsChainingCallDTO chainingCall = new CoreHintsChainingCallDTO();

        String sourceCode = jsonWrapper.getParamValue("content");
        if (!StringUtils.isBlank(sourceCode)) {
            chainingCall.setSourceCode(sourceCode);
        } else {
            String className = jsonWrapper.getParamValue("className");
            String packages = jsonWrapper.getParamValue("packages");

            List<String> packageList = (StringUtils.isBlank(packages)
                    ? CollectionUtils.emptyList()
                    : JsonUtils.parseList(packages, String.class));

            List<ClassFullNameDTO> classFullNameList = getClassFullNameList(packageList, className);

            if (classFullNameList.size() != 1) {
                return null;
            }

            ClassFullNameDTO classFullName = classFullNameList.get(0);

            if (classFullName.isLcdpPath()) {
                LcdpResourceClassInfoDTO classInfo = resourceService.getActiveClassInfoByPath(classFullName.getName(), LcdpUtils.isDebugRequest());

                if (classInfo.getClazz() == null) {
                    chainingCall.setSourceCode(classInfo.getSourceCode());
                } else {
                    chainingCall.setResolvableType(ResolvableType.forClass(classInfo.getClazz()));
                }
            } else {
                chainingCall.setResolvableType(LcdpReflectionUtils.getResolvableType(className, packageList));

                // 私有类
                if (chainingCall.getResolvableType() == null
                        || Modifier.isPrivate(chainingCall.getResolvableType().resolve().getModifiers())) {
                    return null;
                }
            }

        }

        int dotLastIndex = line.lastIndexOf('.');

        String lastStepLine = line.substring(0, dotLastIndex);

        String methodOrFieldName = line.substring(dotLastIndex + 1);

        CodeHintsManager.splitChainingCall(lastStepLine).forEach(c -> chainingCall.addNextChain(c));
        chainingCall.setCallType(getCallType(line));

        CoreHintsChainingCallDTO finalChainingCall = CodeHintsManager.callNext(chainingCall);

        if (finalChainingCall == null) {
            return null;
        }

        List<CodeHintsTextDTO> hintsTextList = new ArrayList<>();
        if (finalChainingCall.getResolvableType() != null) { // 已编译
            getSourceCodeHintsListByClass(finalChainingCall.getResolvableType(), finalChainingCall.getCallType(), methodOrFieldName).forEach(t -> hintsTextList.add(t));
        } else { // 没有编译，只有源代码
            if (!StringUtils.isBlank(finalChainingCall.getSourceCode())) {
                getSourceCodeHintsListBySourceCode(finalChainingCall.getSourceCode(),
                        finalChainingCall.getCallType(), methodOrFieldName).forEach(t -> hintsTextList.add(t));
            }
        }

        hintsTextList.sort(textComparator);

        //禅道60488452  特殊处理
        for (CodeHintsTextDTO hintsText : hintsTextList) {
            String text = hintsText.getHintsText();
            if (StringUtils.isNotEmpty(text) && text.indexOf("@") > 0) {
                String path = text.split("@")[1];
                LcdpResourceClassInfoDTO classInfo = resourceService.getActiveClassInfoByPath(path, LcdpUtils.isDebugRequest());
                if (classInfo != null) {
                    hintsText.setResourceId(classInfo.getResourceId());
                }
            }
        }

        return hintsTextList;

    }


    @Override
    public CodeSourceCodeDTO getSourceCodeByHintText(RestJsonWrapperBean jsonWrapper) {

        CodeHintsTextDTO codeHintsTextDTO = jsonWrapper.parseUnique(CodeHintsTextDTO.class);

        String hintText = codeHintsTextDTO.getHintsText();

        String[] hintTextSplitStr = hintText.split("-");

        if (hintTextSplitStr.length != 2) {
            return null;
        }

        String classFullName = hintTextSplitStr[1].trim();

        String sourceCode = getSourceCodeByClassFullName(classFullName);

        if (!StringUtils.isBlank(sourceCode)) {

            String methodOrFieldText = hintTextSplitStr[0].split(":")[0].trim();
            // 根据提示文本获取在源码中的行数返回给前端
            int lineNumber = LcdpJavaCodeResolverUtils.getLineNumber(sourceCode, methodOrFieldText, codeHintsTextDTO.getName(), codeHintsTextDTO.isField());

            // 如果没有匹配到代码行数，可能是方法声明部分换行了，单独处理一下
            if (lineNumber == -1) {
                sourceCode = CoreSourceCodeHelper.compressParenthesesToSingleLine(sourceCode);
                lineNumber = LcdpJavaCodeResolverUtils.getLineNumber(sourceCode, methodOrFieldText, codeHintsTextDTO.getName(), codeHintsTextDTO.isField());
            }


            return CodeSourceCodeDTO.of(lineNumber, sourceCode);
        }


        return null;
    }

    private String getSourceCodeByClassFullName(String classFullName) {

        if (classFullName.startsWith("@")) {
            LcdpResourceClassInfoDTO classInfo = resourceService.getActiveClassInfoByPath(StringUtils.removeStart(classFullName, "@"), LcdpUtils.isDebugRequest());
            return classInfo.getSourceCode();
        } else {
            String jarName = ClassManager.getJarNameIfPresent(classFullName);


            if (jarName == null) {
                throw new ApplicationRuntimeException("LCDP.EXCEPTION.NOT_FOUND_JAR_FOR_CLASSNAME", classFullName);
            }

            //替换掉jarName可能存在的感叹号（!用来分隔包路径和包内路径
            jarName = jarName.replace("!", "");

            //获取源代码的文件
            SearchFilter jarFilter = SearchFilter.instance().match("NAME", jarName).filter(MatchPattern.SEQ)
                    .match("SCOPE", FileScope.src.name()).filter(MatchPattern.SEQ)
                    .match("FILEEXT", "jar").filter(MatchPattern.SEQ);

            CoreFileBean jarFileBean = fileService.selectFirstByFilter(jarFilter, Order.desc("VERSION"));

            if (jarFileBean == null) {
                logger.warn("找不到源代码jar包[" + jarName + "]");
                throw new ApplicationRuntimeException("LCDP.EXCEPTION.NEED_SOURCE_CODE", jarName);
            }

            String jarFilePath = CoreFileUtils.getLocalPath(jarFileBean).toString();

            return ClassManager.getJavaSourceCode(jarFilePath, classFullName);
        }


    }


    //--------------------------------------------------------------------------------------
    // 私有方法
    //--------------------------------------------------------------------------------------
    private CodeHintsCallType getCallType(String line) {
        if (line.equals("this")
                || line.startsWith("this.")) {
            return CodeHintsCallType.THIS;
        }

        if (line.equals("super")) {
            return CodeHintsCallType.SUPER;
        }

        return CodeHintsCallType.REGULAR;
    }

    private static final Comparator<CodeHintsTextDTO> textComparator = (t1, t2) -> { // field和static优先级高
        if (t1.isField()) {
            if (t2.isField()) {
                if (t1.isStaticModifier()) {
                    if (t2.isStaticModifier()) {
                        return t1.getHintsText().compareTo(t2.getHintsText());
                    } else {
                        return -1;
                    }
                }

                if (t2.isStaticModifier()) {
                    return 1;
                }

                return t1.getHintsText().compareTo(t2.getHintsText());
            } else {
                return -1;
            }
        }

        if (t2.isField()) {
            return 1;
        }

        if (t1.isStaticModifier()) {
            if (t2.isStaticModifier()) {
                return t1.getHintsText().compareTo(t2.getHintsText());
            } else {
                return -1;
            }
        }

        if (t2.isStaticModifier()) {
            return 1;
        }

        return t1.getHintsText().compareTo(t2.getHintsText());
    };

    private static List<CodeHintsTextDTO> getCodeHintsListByClass(ResolvableType resolvableType, CodeHintsCallType callType) {
        if (resolvableType == null) {
            return CollectionUtils.emptyList();
        }

        List<CodeHintsTextDTO> hintsTextList = new ArrayList<>();

        Class<?> clazz = resolvableType.resolve();

        if (clazz == null) {
            return CollectionUtils.emptyList();
        }

        String classFullName = LcdpReflectionUtils.isLcdpClass(clazz.getName())
                ? "@" + LcdpReflectionUtils.getLcdpPath(clazz.getName())
                : clazz.getName();

        List<Field> fieldList = CodeHintsManager.getMatchFieldList(clazz, callType, m -> true);
        if (!fieldList.isEmpty()) {
            fieldList.stream().forEach(f -> {
                CodeHintsTextDTO hintsText = new CodeHintsTextDTO();
                hintsText.setName(f.getName());

                if (Modifier.isStatic(f.getModifiers())) {
                    StringBuilder sb = new StringBuilder(f.getName())
                            .append(" : ")
                            .append(LcdpReflectionUtils.removePackage(f.getGenericType().getTypeName()))
                            .append(" - ").append(classFullName);
                    hintsText.setHintsText(sb.toString());
                } else {
                    ResolvableType fieldResolvableType = ReflectionUtils.getActualResolvableType(resolvableType, f.getGenericType());

                    StringBuilder sb = new StringBuilder(f.getName())
                            .append(" : ")
                            .append(LcdpReflectionUtils.removePackage(ReflectionUtils.resolvableTypeToString(fieldResolvableType)))
                            .append(" - ").append(classFullName);
                    hintsText.setHintsText(sb.toString());
                }

                hintsText.setField(true);
                hintsText.setStaticModifier(Modifier.isStatic(f.getModifiers()));

                if (!hintsTextList.contains(hintsText)) {
                    hintsTextList.add(hintsText);
                }
            });
        }

        List<Method> methodList = CodeHintsManager.getMatchMethodList(clazz, callType, m -> true);
        if (!methodList.isEmpty()) {
            methodList.forEach(m -> {
                CodeHintsTextDTO hintsText = new CodeHintsTextDTO();
                hintsText.setName(m.getName());

                StringBuilder sb = new StringBuilder(m.getName()).append("(");
                int count = m.getParameterCount();
                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        if (i > 0) {
                            sb.append(", ");
                        }

                        if (Modifier.isStatic(m.getModifiers())) {
                            sb.append(LcdpReflectionUtils.removePackage(m.getGenericParameterTypes()[i].getTypeName()));
                        } else {
                            ResolvableType parameterResolvableType = ReflectionUtils.getActualResolvableType(resolvableType, m.getGenericParameterTypes()[i]);
                            sb.append(LcdpReflectionUtils.removePackage(ReflectionUtils.resolvableTypeToString(parameterResolvableType)));
                        }
                    }
                }

                sb.append(") : ");


                if (Modifier.isStatic(m.getModifiers())) {
                    sb.append(LcdpReflectionUtils.removePackage(m.getGenericReturnType().getTypeName()));
                } else {
                    ResolvableType returnResolvableType = ReflectionUtils.getActualResolvableType(resolvableType, m.getGenericReturnType());
                    sb.append(LcdpReflectionUtils.removePackage(ReflectionUtils.resolvableTypeToString(returnResolvableType)));
                }

                sb.append(" - ").append(classFullName);
                hintsText.setHintsText(sb.toString());

                hintsText.setField(false);
                hintsText.setStaticModifier(Modifier.isStatic(m.getModifiers()));

                if (!hintsTextList.contains(hintsText)) {
                    hintsTextList.add(hintsText);
                }
            });
        }

        return hintsTextList;
    }

    /**
     * 低代码开发的源代码中获取代码提醒
     */
    private List<CodeHintsTextDTO> getCodeHintsListBySourceCode(String sourceCode, CodeHintsCallType callType) {
        List<CodeHintsTextDTO> hintsTextList = new ArrayList<>();
        // 删除注释
        sourceCode = LcdpJavaCodeResolverUtils.getNocommentJavaCode(sourceCode);

        List<String> importedPackageList = LcdpJavaCodeResolverUtils.getImportedPackageList(sourceCode);

        String path = ClassManager.getPackageName(sourceCode); // package就是路径

        String classFullName = "@" + path;

        if (!CodeHintsCallType.SUPER.equals(callType)
                && !CodeHintsCallType.SUPER_DEFAULT.equals(callType)) {
            // 提取源代码中的成员变量
            Matcher fieldMatcher = ClassManager.classFieldPattern.matcher(sourceCode);
            while (fieldMatcher.find()) {
                String group = fieldMatcher.group();

                logger.warn("Field ------>[" + group + "]");

                String fieldType = fieldMatcher.group("fieldType");
                String fieldName = fieldMatcher.group("fieldName");

                if (CodeHintsCallType.STATIC.equals(callType)) {
                    if (!group.startsWith("public ")
                            || !group.contains(" static ")) {
                        continue;
                    }
                } else if (CodeHintsCallType.THIS.equals(callType)
                        || CodeHintsCallType.SUPER.equals(callType)
                        || CodeHintsCallType.SUPER_DEFAULT.equals(callType)) {
                    /* ignore */
                } else {
                    if (!group.startsWith("public ")) {
                        continue;
                    }
                }

                CodeHintsTextDTO hintsText = new CodeHintsTextDTO();
                hintsText.setName(fieldName);
                hintsText.setField(true);
                hintsText.setStaticModifier(group.contains(" static "));

                try {
                    ResolvableType fieldResolvableType = LcdpReflectionUtils.getResolvableType(fieldType, importedPackageList);

                    String actualFieldTypeName = fieldResolvableType.toString();
                    if (LcdpReflectionUtils.isLcdpClass(actualFieldTypeName)) {
                        hintsText.setHintsText(fieldName + " : " + LcdpReflectionUtils.removePackage(actualFieldTypeName.substring(0, actualFieldTypeName.lastIndexOf("."))) + " - " + classFullName);
                    } else {
                        hintsText.setHintsText(fieldName + " : " + LcdpReflectionUtils.removePackage(actualFieldTypeName) + " - " + classFullName);
                    }
                } catch (Exception ex) { // 类不存在，可能是编译失败
                    logger.warn(ex.getMessage(), ex);

                    hintsText.setHintsText(fieldName + " : " + fieldType + " - " + classFullName);
                }

                if (!hintsTextList.contains(hintsText)) {
                    hintsTextList.add(hintsText);
                }
            }

            // 提取源代码中的方法
            Matcher methodMetcher = ClassManager.classMethodPattern.matcher(sourceCode);
            while (methodMetcher.find()) {
                String group = methodMetcher.group();

                logger.warn("Method ------>[" + group + "]");

                String returnType = methodMetcher.group("returnType");
                String methodName = methodMetcher.group("methodName");
                String methodParam = methodMetcher.group("methodParam");

                if (CodeHintsCallType.STATIC.equals(callType)) {
                    if (!group.startsWith("public ")
                            || !group.contains(" static ")) {
                        continue;
                    }
                } else if (CodeHintsCallType.THIS.equals(callType)
                        || CodeHintsCallType.SUPER.equals(callType)
                        || CodeHintsCallType.SUPER_DEFAULT.equals(callType)) {
                    /* ignore */
                } else {
                    if (!group.startsWith("public ")) {
                        continue;
                    }
                }

                CodeHintsTextDTO hintsText = new CodeHintsTextDTO();
                hintsText.setName(methodName);
                hintsText.setField(false);
                hintsText.setStaticModifier(group.contains(" static "));

                StringBuilder sb = new StringBuilder(methodName).append("(");
                if (!StringUtils.isBlank(methodParam)) {
                    logger.warn("-----xxx>>>[" + methodParam + "]");

                    List<String> params = CodeHintsManager.splitMethodParams(methodParam);
                    for (int i = 0; i < params.size(); i++) {
                        if (i > 0) {
                            sb.append(", ");
                        }

                        String param = params.get(i).trim();

                        String paramType = param.substring(0, param.lastIndexOf(" "));

                        logger.warn("-----aaaa>>>[" + paramType + "]");

                        if (ClassUtils.isPrimitiveType(paramType)) {
                            sb.append(paramType);
                        } else {
                            ResolvableType paramResolvableType = LcdpReflectionUtils.getResolvableType(paramType, importedPackageList);
                            String actualParamTypeName = paramResolvableType.toString();
                            if (LcdpReflectionUtils.isLcdpClass(actualParamTypeName)) {
                                sb.append(LcdpReflectionUtils.removePackage(actualParamTypeName.substring(0, actualParamTypeName.lastIndexOf("."))));
                            } else {
                                sb.append(LcdpReflectionUtils.removePackage(actualParamTypeName));
                            }
                        }
                    }
                }
                sb.append(") : ");

                if (ClassUtils.isPrimitiveType(returnType)
                        || "void".equals(returnType)) {
                    sb.append(returnType);
                } else {
                    ResolvableType returnResolvableType = LcdpReflectionUtils.getResolvableType(returnType, importedPackageList);

                    if (returnResolvableType == null) {
                        continue;
                    }

                    String actualReturnTypeName = returnResolvableType.toString();
                    if (LcdpReflectionUtils.isLcdpClass(actualReturnTypeName)) {
                        sb.append(LcdpReflectionUtils.removePackage(actualReturnTypeName.substring(0, actualReturnTypeName.lastIndexOf("."))));
                    } else {
                        sb.append(LcdpReflectionUtils.removePackage(actualReturnTypeName));
                    }
                }

                sb.append(" - " + classFullName);

                hintsText.setHintsText(sb.toString());

                if (!hintsTextList.contains(hintsText)) {
                    hintsTextList.add(hintsText);
                }
            }
        }

        // 提取所有父类的静态变量和方法
        String superClassFullName = LcdpReflectionUtils.getSuperClassFullName(sourceCode);
        if (!StringUtils.isBlank(superClassFullName)) {
            Class<?> superClass = ClassManager.getClassByFullName(superClassFullName);

            String superClassGenericPart = LcdpReflectionUtils.getSuperClassGenericPart(sourceCode, superClass.getSimpleName());

            ResolvableType superClassResolvableType = LcdpReflectionUtils.getResolvableType(superClass.getSimpleName()
                    + Optional.ofNullable(superClassGenericPart).orElse(""), importedPackageList);

            List<CodeHintsTextDTO> superClassHintsTextList = getCodeHintsListByClass(superClassResolvableType, callType);

            superClassHintsTextList.forEach(t -> {
                if (!hintsTextList.contains(t)) {
                    hintsTextList.add(t);
                }
            });
        }
        List<String> implementedClassFullNameList = LcdpReflectionUtils.getImplementedClassFullNameList(sourceCode);
        if (!implementedClassFullNameList.isEmpty()) {
            for (String interfaceClassFullName : implementedClassFullNameList) {
                Class<?> interfaceClass = ClassManager.getClassByFullName(interfaceClassFullName);

                String interfaceClassGenericPart = LcdpReflectionUtils.getInterfacecClassGenericPart(sourceCode, interfaceClass.getSimpleName());

                logger.warn("=======interface11>>>[" + interfaceClassGenericPart + "]");

                logger.warn("=======interface22>>>" + interfaceClass.getSimpleName() + "---" + Optional.ofNullable(interfaceClassGenericPart).orElse(""));

                ResolvableType interfaceClassResolvableType = LcdpReflectionUtils.getResolvableType(Optional.ofNullable(interfaceClassGenericPart).orElse(interfaceClass.getSimpleName()), importedPackageList);

                List<CodeHintsTextDTO> interfaceClassHintsTextList = getCodeHintsListByClass(interfaceClassResolvableType, callType);

                interfaceClassHintsTextList.forEach(t -> {
                    if (!hintsTextList.contains(t)) {
                        hintsTextList.add(t);
                    }
                });
            }
        }

        return hintsTextList;
    }


    private static List<CodeHintsTextDTO> getSourceCodeHintsListByClass(ResolvableType resolvableType, CodeHintsCallType callType, String filterMethodOrFieldNameName) {
        if (resolvableType == null) {
            return CollectionUtils.emptyList();
        }

        List<CodeHintsTextDTO> hintsTextList = new ArrayList<>();

        Class<?> clazz = resolvableType.resolve();

        if (clazz == null) {
            return CollectionUtils.emptyList();
        }


        List<Field> fieldList = CodeHintsManager.getMatchFieldList(clazz, callType, m -> filterMethodOrFieldNameName.equals(m.getName()));
        if (!fieldList.isEmpty()) {
            fieldList.stream().forEach(f -> {
                CodeHintsTextDTO hintsText = new CodeHintsTextDTO();
                hintsText.setName(f.getName());

                if (Modifier.isStatic(f.getModifiers())) {
                    StringBuilder sb = new StringBuilder(f.getName())
                            .append(" : ")
                            .append(LcdpReflectionUtils.removePackage(f.getGenericType().getTypeName()))
                            .append(" - ").append(getClassFullName(f.getDeclaringClass()));
                    hintsText.setHintsText(sb.toString());
                } else {
                    ResolvableType fieldResolvableType = ReflectionUtils.getActualResolvableType(resolvableType, f.getGenericType());

                    StringBuilder sb = new StringBuilder(f.getName())
                            .append(" : ")
                            .append(LcdpReflectionUtils.removePackage(ReflectionUtils.resolvableTypeToString(fieldResolvableType)))
                            .append(" - ").append(getClassFullName(f.getDeclaringClass()));
                    hintsText.setHintsText(sb.toString());
                }

                hintsText.setField(true);
                hintsText.setStaticModifier(Modifier.isStatic(f.getModifiers()));

                if (!hintsTextList.contains(hintsText)) {
                    hintsTextList.add(hintsText);
                }
            });
        }

        List<Method> methodList = CodeHintsManager.getAllMatchMethodList(clazz, callType, m -> filterMethodOrFieldNameName.equals(m.getName()));


        if (!methodList.isEmpty()) {
            methodList.forEach(m -> {

                CodeHintsTextDTO hintsText = new CodeHintsTextDTO();
                hintsText.setName(m.getName());
                StringBuilder sb = new StringBuilder(m.getName()).append("(");
                int count = m.getParameterCount();
                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        if (i > 0) {
                            sb.append(", ");
                        }

                        if (Modifier.isStatic(m.getModifiers())) {
                            sb.append(LcdpReflectionUtils.removePackage(m.getGenericParameterTypes()[i].getTypeName()));
                        } else {
                            ResolvableType parameterResolvableType = ReflectionUtils.getActualResolvableType(resolvableType, m.getGenericParameterTypes()[i]);
                            sb.append(LcdpReflectionUtils.removePackage(ReflectionUtils.resolvableTypeToString(parameterResolvableType)));
                        }

                    }
                }

                sb.append(") : ");


                if (Modifier.isStatic(m.getModifiers())) {
                    sb.append(LcdpReflectionUtils.removePackage(m.getGenericReturnType().getTypeName()));
                } else {
                    ResolvableType returnResolvableType = ReflectionUtils.getActualResolvableType(resolvableType, m.getGenericReturnType());
                    sb.append(LcdpReflectionUtils.removePackage(ReflectionUtils.resolvableTypeToString(returnResolvableType)));
                }

                sb.append(" - ").append(getClassFullName(m.getDeclaringClass()));
                hintsText.setHintsText(sb.toString());

                hintsText.setField(false);
                hintsText.setStaticModifier(Modifier.isStatic(m.getModifiers()));

                if (!hintsTextList.contains(hintsText)) {
                    hintsTextList.add(hintsText);
                }
            });
        }

        return hintsTextList;
    }


    /**
     * 低代码开发的源代码中获取查看源代码提示
     */
    private List<CodeHintsTextDTO> getSourceCodeHintsListBySourceCode(String sourceCode, CodeHintsCallType callType, String filterMethodOrFieldName) {
        List<CodeHintsTextDTO> hintsTextList = new ArrayList<>();
        // 删除注释
        sourceCode = LcdpJavaCodeResolverUtils.getNocommentJavaCode(sourceCode);

        List<String> importedPackageList = LcdpJavaCodeResolverUtils.getImportedPackageList(sourceCode);

        String path = ClassManager.getPackageName(sourceCode); // package就是路径

        String classFullName = "@" + path;

        if (!CodeHintsCallType.SUPER.equals(callType)
                && !CodeHintsCallType.SUPER_DEFAULT.equals(callType)) {
            // 提取源代码中的成员变量
            Matcher fieldMatcher = ClassManager.classFieldPattern.matcher(sourceCode);
            while (fieldMatcher.find()) {
                String group = fieldMatcher.group();

                logger.warn("Field ------>[" + group + "]");

                String fieldType = fieldMatcher.group("fieldType");
                String fieldName = fieldMatcher.group("fieldName");

                if (!filterMethodOrFieldName.equals(fieldName)) {
                    continue;
                }


                if (CodeHintsCallType.STATIC.equals(callType)) {
                    if (!group.startsWith("public ")
                            || !group.contains(" static ")) {
                        continue;
                    }
                } else if (CodeHintsCallType.THIS.equals(callType)
                        || CodeHintsCallType.SUPER.equals(callType)
                        || CodeHintsCallType.SUPER_DEFAULT.equals(callType)) {
                    /* ignore */
                } else {
                    if (!group.startsWith("public ")) {
                        continue;
                    }
                }

                CodeHintsTextDTO hintsText = new CodeHintsTextDTO();
                hintsText.setName(fieldName);
                hintsText.setField(true);
                hintsText.setStaticModifier(group.contains(" static "));

                ResolvableType fieldResolvableType = LcdpReflectionUtils.getResolvableType(fieldType, importedPackageList);

                String actualFieldTypeName = fieldResolvableType.toString();
                if (LcdpReflectionUtils.isLcdpClass(actualFieldTypeName)) {
                    hintsText.setHintsText(fieldName + " : " + LcdpReflectionUtils.removePackage(actualFieldTypeName.substring(0, actualFieldTypeName.lastIndexOf("."))) + " - " + classFullName);
                } else {
                    hintsText.setHintsText(fieldName + " : " + LcdpReflectionUtils.removePackage(actualFieldTypeName) + " - " + classFullName);
                }

                if (!hintsTextList.contains(hintsText)) {
                    hintsTextList.add(hintsText);
                }
            }


            // 提取源代码中的方法
            Matcher methodMetcher = ClassManager.classMethodPattern.matcher(sourceCode);
            while (methodMetcher.find()) {
                String group = methodMetcher.group();

                logger.warn("Method ------>[" + group + "]");

                String returnType = methodMetcher.group("returnType");
                String methodName = methodMetcher.group("methodName");
                String methodParam = methodMetcher.group("methodParam");

                if (!filterMethodOrFieldName.equals(methodName)) {
                    continue;
                }


                if (CodeHintsCallType.STATIC.equals(callType)) {
                    if (!group.startsWith("public ")
                            || !group.contains(" static ")) {
                        continue;
                    }
                } else if (CodeHintsCallType.THIS.equals(callType)
                        || CodeHintsCallType.SUPER.equals(callType)
                        || CodeHintsCallType.SUPER_DEFAULT.equals(callType)) {
                    /* ignore */
                } else {
                    if (!group.startsWith("public ")) {
                        continue;
                    }
                }

                CodeHintsTextDTO hintsText = new CodeHintsTextDTO();
                hintsText.setName(methodName);
                hintsText.setField(false);
                hintsText.setStaticModifier(group.contains(" static "));

                StringBuilder sb = new StringBuilder(methodName).append("(");
                if (!StringUtils.isBlank(methodParam)) {
                    logger.warn("-----xxx>>>[" + methodParam + "]");

                    List<String> params = CodeHintsManager.splitMethodParams(methodParam);
                    for (int i = 0; i < params.size(); i++) {
                        if (i > 0) {
                            sb.append(", ");
                        }

                        String param = params.get(i).trim();

                        String paramType = param.substring(0, param.lastIndexOf(" "));

                        logger.warn("-----aaaa>>>[" + paramType + "]");

                        if (ClassUtils.isPrimitiveType(paramType)) {
                            sb.append(paramType);
                        } else {
                            ResolvableType paramResolvableType = LcdpReflectionUtils.getResolvableType(paramType, importedPackageList);
                            String actualParamTypeName = paramResolvableType.toString();
                            if (LcdpReflectionUtils.isLcdpClass(actualParamTypeName)) {
                                sb.append(LcdpReflectionUtils.removePackage(actualParamTypeName.substring(0, actualParamTypeName.lastIndexOf("."))));
                            } else {
                                sb.append(LcdpReflectionUtils.removePackage(actualParamTypeName));
                            }
                        }
                    }
                }
                sb.append(") : ");

                if (ClassUtils.isPrimitiveType(returnType)
                        || "void".equals(returnType)) {
                    sb.append(returnType);
                } else {
                    ResolvableType returnResolvableType = LcdpReflectionUtils.getResolvableType(returnType, importedPackageList);

                    if (returnResolvableType == null) {
                        continue;
                    }

                    String actualReturnTypeName = returnResolvableType.toString();
                    if (LcdpReflectionUtils.isLcdpClass(actualReturnTypeName)) {
                        sb.append(LcdpReflectionUtils.removePackage(actualReturnTypeName.substring(0, actualReturnTypeName.lastIndexOf("."))));
                    } else {
                        sb.append(LcdpReflectionUtils.removePackage(actualReturnTypeName));
                    }
                }

                sb.append(" - " + classFullName);

                hintsText.setHintsText(sb.toString());

                if (!hintsTextList.contains(hintsText)) {
                    hintsTextList.add(hintsText);
                }
            }
        }

        // 提取所有父类的静态变量和方法
        String superClassFullName = LcdpReflectionUtils.getSuperClassFullName(sourceCode);
        if (!StringUtils.isBlank(superClassFullName)) {
            Class<?> superClass = ClassManager.getClassByFullName(superClassFullName);

            String superClassGenericPart = LcdpReflectionUtils.getSuperClassGenericPart(sourceCode, superClass.getSimpleName());

            ResolvableType superClassResolvableType = LcdpReflectionUtils.getResolvableType(superClass.getSimpleName()
                    + Optional.ofNullable(superClassGenericPart).orElse(""), importedPackageList);

            List<CodeHintsTextDTO> superClassHintsTextList = getSourceCodeHintsListByClass(superClassResolvableType, callType, filterMethodOrFieldName);

            superClassHintsTextList.forEach(t -> {
                if (!hintsTextList.contains(t)) {
                    hintsTextList.add(t);
                }
            });
        }
        List<String> implementedClassFullNameList = LcdpReflectionUtils.getImplementedClassFullNameList(sourceCode);
        if (!implementedClassFullNameList.isEmpty()) {
            for (String interfaceClassFullName : implementedClassFullNameList) {
                Class<?> interfaceClass = ClassManager.getClassByFullName(interfaceClassFullName);

                String interfaceClassGenericPart = LcdpReflectionUtils.getInterfacecClassGenericPart(sourceCode, interfaceClass.getSimpleName());

                logger.warn("=======interface11>>>[" + interfaceClassGenericPart + "]");

                logger.warn("=======interface22>>>" + interfaceClass.getSimpleName() + "---" + Optional.ofNullable(interfaceClassGenericPart).orElse(""));

                ResolvableType interfaceClassResolvableType = LcdpReflectionUtils.getResolvableType(Optional.ofNullable(interfaceClassGenericPart).orElse(interfaceClass.getSimpleName()), importedPackageList);

                List<CodeHintsTextDTO> interfaceClassHintsTextList = getSourceCodeHintsListByClass(interfaceClassResolvableType, callType, filterMethodOrFieldName);

                interfaceClassHintsTextList.forEach(t -> {
                    if (!hintsTextList.contains(t)) {
                        hintsTextList.add(t);
                    }
                });
            }
        }

        return hintsTextList;
    }


    /**
     * 根据类名和可能的包名来获取类全称，如果包里没有匹配时，从所有类里查找匹配的
     */
    private List<ClassFullNameDTO> getClassFullNameList(List<String> packageList, String className) {
        // 去掉泛型
        String actualClassName = className.contains("<") ? className.substring(0, className.indexOf("<")) : className;

        List<String> matchList = packageList.stream().filter(p -> StringUtils.endsWith(p, "." + actualClassName)).collect(Collectors.toList());

        logger.warn("----------->" + className + "---" + matchList.size());

        if (!matchList.isEmpty()) {
            if (matchList.size() > 1) {
                throw new ApplicationRuntimeException("类名[" + className + "]引入的类不止一个，请检查引入的类");
            }

            return matchList.stream().map(m -> {
                if (m.startsWith("@")) {
                    return ClassFullNameDTO.lcdpOf(StringUtils.removeStart(m, "@"));
                } else {
                    return ClassFullNameDTO.of(m);
                }
            }).collect(Collectors.toList());
        }

        List<String> classFullNameList = ClassManager.getClassFullNameList(actualClassName);

        if (!classFullNameList.isEmpty()) {
            List<String> fullNameList = new ArrayList<>();

            // 处理引入的类中含有通配符的
            for (String packageName : packageList) {
                if (!StringUtils.endsWith(packageName, ".*")) {
                    continue;
                }

                String possibleFullName = StringUtils.removeEnd(packageName, "*") + actualClassName;

                if (classFullNameList.contains(possibleFullName)) {
                    fullNameList.add(possibleFullName);
                }
            }
            if (fullNameList.size() > 1) {
                throw new ApplicationRuntimeException("类名[" + className + "]引入的类不止一个，请检查引入通配符的类");
            }
            if (fullNameList.size() == 1) {
                return fullNameList.stream().map(ClassFullNameDTO::of).collect(Collectors.toList());
            }

            return classFullNameList.stream().map(ClassFullNameDTO::of).collect(Collectors.toList());
        }

        // 当前类是低代码平台开发的类
        return resourceService.getPathListByClassName(actualClassName).stream().map(ClassFullNameDTO::lcdpOf).collect(Collectors.toList());
    }

    private static String getClassFullName(Class<?> clazz) {
        return LcdpReflectionUtils.isLcdpClass(clazz.getName())
                ? "@" + LcdpReflectionUtils.getLcdpPath(clazz.getName())
                : clazz.getName();
    }


    private static class ClassFullNameDTO {
        private String name; // 类全称或路径
        private boolean lcdpPath; // 是否是低代码的路径

        private static ClassFullNameDTO of(String fullName) {
            ClassFullNameDTO instance = new ClassFullNameDTO();
            instance.name = fullName;
            instance.lcdpPath = false;
            return instance;
        }

        private static ClassFullNameDTO lcdpOf(String path) {
            ClassFullNameDTO instance = new ClassFullNameDTO();
            instance.name = path;
            instance.lcdpPath = true;
            return instance;
        }

        public String getName() {
            return name;
        }

        public boolean isLcdpPath() {
            return lcdpPath;
        }
    }
}
