package com.sunwayworld.cloud.module.lcdp.resource.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sunwayworld.cloud.module.lcdp.base.annotation.Mapping;
import com.sunwayworld.cloud.module.lcdp.errorscript.service.LcdpErrorScriptService;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceHistoryBean;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpServerScriptMethodBean;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceHistoryService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpServerScriptMethodService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.LocalContextHelper;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.exception.checked.CheckedException;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.jdk.core.ClassManager;
import com.sunwayworld.framework.jdk.tools.DynamicClassLoader;
import com.sunwayworld.framework.jdk.tools.LoadMultipleResult;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.NumberUtils;
import com.sunwayworld.framework.utils.SpringUtils;
import com.sunwayworld.framework.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java代码解析工具类
 *
 * @author zhangjr@sunwayworld.com 2024年7月18日
 */
public class LcdpJavaCodeResolverUtils {
    private static final Logger logger = LoggerFactory.getLogger(LcdpJavaCodeResolverUtils.class);

    private static final List<String> REQUIRED_IMPORT_LIST = new ArrayList<>();

    static {
        REQUIRED_IMPORT_LIST.add("com.sunwayworld.cloud.module.lcdp.resource.support.LcdpScriptUtils");
        REQUIRED_IMPORT_LIST.add("com.sunwayworld.cloud.module.lcdp.resource.support.UtilsCaller");
        REQUIRED_IMPORT_LIST.add("com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant");
        REQUIRED_IMPORT_LIST.add("com.sunwayworld.framework.spring.SpringCaller");
    }

    // 替换注释的正则表达式
    private static final String blockCommentRegExp = "/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/";
    private static final String toggleCommentRegExp = "(?<=\\s|^|;|\\)|\\(|\\{)\\s*//.*?(?=\n|$)";

    // 替换字符串的正则表达式
    private static final String stringRegExp = "\"([^\"|\\n|\\r])+?\"";
    // 匹配括号
    private static final Pattern bracketPattern = Pattern.compile("\\(([^()]*)\\)");

    // 获取已导入的包
    private static final Pattern importPattern = Pattern.compile("(^|\\n|\\r|;)+\\s*(import)\\s+(?<package>(\\w|\\.|\\*)+);");
    // 低代码生成的导入的类的集合
    private static final Pattern lcdpPathImportPattern = Pattern.compile("@LcdpPathImport[^)]+\\)");
    // 低代码生成的类导入的类里获取字符串
    private static final Pattern lcdpPathImportStringPattern = Pattern.compile("\"(?<classFullName>[^\"]+)\"");

    // 获取所有类，第一个字母必需大写
    private static final Pattern classPattern = Pattern.compile("(\\(|,|\\s|\\<|@|!|-\\>)\\s*(?<Class>[A-Z][A-Za-z0-9_]+)");

    // 获取LcdpBaseService接口进行脚本类注入
    private static final Pattern lcdpBaseServiceAutowiredClassPattern = Pattern.compile("@Autowired\\s*(\\r|\\n)*\\s*(\\w|\\s)*\\s+LcdpBaseService\\s+(?<fieldName>\\w+);");

    // 校验类名是否是低代码平台开发的
    private static final Pattern lcdpClassSuffixPattern = Pattern.compile("v[1-9][0-9]*(m[1-9][0-9]*)?$");
    private static final Pattern lcdpProClassSuffixPattern = Pattern.compile("v(?<version>[1-9][0-9]*)$");
    private static final Pattern lcdpDevClassSuffixPattern = Pattern.compile("v(?<version>[1-9][0-9]*)m(?<modifyVersion>[1-9][0-9]*)$");

    // 获取低代码平台开发出来的调用静态变量或静态方法的类（也有可能是底层的类）直接调用静态变量或静态方法的
    private static final Pattern lcdpClassCalledStaticPattern = Pattern.compile("[^A-Za-z0-9_\\.\"](?<className>(Lcdp\\w+(Utils|Service|Listener)))\\.");

    // 或是由所有Autowired注解的成员变量
    public static final Pattern lcdpAutowiredBeanPatten = Pattern.compile("@Autowired\\s*([\\r\\n])*(@Lazy)*\\s*private\\s+(?<className>\\w+)\\s+(?<fieldName>\\w+);");
    // 匹配else if
    public static final Pattern elseIfPattern = Pattern.compile("else\\s+if");
    // 匹配if(但不是else if(
    public static final Pattern ifNotElseIfPattern = Pattern.compile("(?<!else\\s{1,5})if\\s*\\(");
    //获取package这一行

    /**
     * 获取待引入包的行数据，如：<code>import com.sunwayworld.frameworkd.utils.StirngUtils;</code>
     */
    public static List<String> getImportClassFullNameList(String sourceCode) {
        if (!ClassManager.classPattern.asPredicate().test(sourceCode)) {
            return CollectionUtils.emptyList();
        }

        long start = System.currentTimeMillis();
        long stageTime = start;

        List<String> importPackageList = new ArrayList<>();
        List<String> importedPackageList = getImportedPackageList(sourceCode);
        List<String> importedLcdpPackageList = importedPackageList.stream().filter(e -> e.startsWith("@")).collect(Collectors.toList());
        logger.info("解析所有已导入的包{}项，耗时：{}毫秒",
                importedPackageList.size(),
                (System.currentTimeMillis() - stageTime));
        stageTime = System.currentTimeMillis();

        String resolvableJavaCode = getResolvableJavaCode(sourceCode);

        logger.info("转换成可解析的java源代码耗时：{}毫秒",
                (System.currentTimeMillis() - stageTime));
        stageTime = System.currentTimeMillis();

        List<String> suspectedToImportClassNameList = getSuspectedToImportClassNameList(resolvableJavaCode);
        logger.info("解析疑似要导入类的简称耗时：{}毫秒",
                (System.currentTimeMillis() - stageTime));

        for (String className : suspectedToImportClassNameList) {
            // 过滤泛型<ID>
            if ("ID".equals(className)) {
                continue;
            }
            List<String> classFullNameList = ClassManager.getClassFullNameList(className);

            logger.info("类简称{}对应的实际类名有{}项", className, classFullNameList.size());

            if (classFullNameList.isEmpty()) {
                // 注释掉该段代码，兼容项目上低代码脚本不是以Lcdp打头的
//                if (!isPossiableLcdpClass(className)) {
//                    continue;
//                }

                // 对于已经导入的低代码包直接判断，不再查询，避免低代码脚本重名问题
                if (importedLcdpPackageList.stream().anyMatch(e -> e.endsWith(className))) {
                    continue;
                }


                // 向下兼容
                String path = SpringUtils.getBean(LcdpResourceService.class).getPathByClassName(className);

                logger.info("类简称{}对应的低代码类的路径：[{}]", className, path);

                if (!StringUtils.isBlank(path)
                        && !importedPackageList.contains("@" + path)) {
                    importPackageList.add("@" + path);
                }

                continue;
            }

            if (classFullNameList.stream().anyMatch(n -> isImported(importedPackageList, n))) {
                continue;
            }

            String classFullName = classFullNameList.stream().filter(n -> !StringUtils.isBlank(n)
                    && !StringUtils.contains(n, "$$EnhancerBySpringCGLIB$$")
                    && StringUtils.contains(n, ".")).findFirst().orElse(null);

            if (StringUtils.isBlank(classFullName)
                    || "java.lang.Override".equals(classFullName)) {
                continue;
            }

            if (classFullName.contains("$")) {
                classFullName = StringUtils.replace(classFullName, "$", ".");
            }

            if (importPackageList.contains(classFullName)) { // 已导入
                continue;
            }

            importPackageList.add(classFullName);
        }

        logger.info("解析源代码并获取引入的包{}项，总耗时：{}毫秒",
                importPackageList.size(), (System.currentTimeMillis() - start));

        return importPackageList;
    }

    /**
     * 通过源代码获取优化后导入的包
     */
    public static List<String> getOptimizedImportList(String sourceCode) {
        if (!ClassManager.classPattern.asPredicate().test(sourceCode)) {
            return CollectionUtils.emptyList();
        }

        Map<String, String> cache = new HashMap<>();

        List<String> importedPackageList = getImportedPackageList(sourceCode);

        // 通配符的导入的包，去掉最后的通配符
        List<String> wildcardImportedPackageList = importedPackageList.stream()
                .filter(p -> p.endsWith(".*"))
                .collect(Collectors.toList());

        String resolvableJavaCode = getResolvableJavaCode(sourceCode);

        // 获取所有类
        Matcher classMatcher = classPattern.matcher(resolvableJavaCode);
        while (classMatcher.find()) {
            String className = classMatcher.group("Class");

            if (cache.containsKey(className)) {
                continue;
            }

            String matchPackage = importedPackageList.stream().filter(p -> p.endsWith("." + className))
                    .findFirst().orElse(null);

            if (!StringUtils.isBlank(matchPackage)) {
                cache.put(className, matchPackage);
            } else {
                if (wildcardImportedPackageList.isEmpty()) {
                    continue;
                }

                List<String> classFullNameList = ClassManager.getClassFullNameList(className);
                if (classFullNameList.isEmpty()) {
                    continue;
                }

                for (String classFullName : classFullNameList) {
                    String wildcardImportedPackage = wildcardImportedPackageList.stream().filter(p -> classFullName.startsWith(p)
                            && !StringUtils.removeStart(classFullName, p).contains(".")).findFirst().orElse(null);

                    if (!StringUtils.isBlank(wildcardImportedPackage)) {
                        cache.put(className, wildcardImportedPackage);

                        break;
                    }
                }
            }
        }

        return cache.values().stream().sorted().collect(Collectors.toList());
    }

    /**
     * 获取所有注入了<code>LcdpBaseService</code>的成员变量名称
     */
    public static List<String> getAuwowiredLcdpBaseServiceFieldNameList(String sourceCode) {
        List<String> fieldNameList = new ArrayList<>();

        Matcher matcher = lcdpBaseServiceAutowiredClassPattern.matcher(sourceCode);
        while (matcher.find()) {
            fieldNameList.add(matcher.group("fieldName"));
        }

        return fieldNameList;
    }

    /**
     * 获取低代码类的最终实际类名的前缀
     */
    public static String getLcdpClassNamePrefix(String lcdpPath, String lcdpClassName) {
        if (StringUtils.isBlank(lcdpPath)) {
            return lcdpClassName;
        }

        String[] values = StringUtils.split(lcdpPath, ".");

        return StringUtils.capitalize(values[0]) + lcdpClassName;
    }

    /**
     * 验证当前类可能是低代码平台开发的
     */
    public static boolean isPossiableLcdpClass(String className) {
        if (StringUtils.isBlank(className)) {
            return false;
        }

        return className.startsWith("Lcdp");
    }

    /**
     * 对于类名，如果是低代码的，就删除低代码类的后缀和前面的包，否则直接返回
     */
    public static String removeLcdpClassSuffixIfNecessary(String className) {
        if (StringUtils.isBlank(className)) {
            return className;
        }

        Matcher matcher = lcdpClassSuffixPattern.matcher(className);
        if (matcher.find()) {
            className = matcher.replaceAll("");
        }

        return className;
    }

    /**
     * 获取源代码中所有低代码平台开发的调用静态变量或静态方法的类（也有可能是底层的类）
     */
    public static List<String> getLcdpCalledStaticClassNameList(String sourceCode) {
        String nocommentSourceCode = LcdpJavaCodeResolverUtils.getNocommentJavaCode(sourceCode);

        List<String> classNameList = new ArrayList<>();

        Matcher lcdpClassMatcher = lcdpClassCalledStaticPattern.matcher(nocommentSourceCode);
        while (lcdpClassMatcher.find()) {
            String className = lcdpClassMatcher.group("className");

            if (!classNameList.contains(className)) {
                classNameList.add(className);
            }
        }

        return classNameList;
    }

    /**
     * 替换源代码中的类名
     */
    public static String replaceClassSimpleName(String sourceCode, String newClassSimpleName) {
        if (StringUtils.isBlank(sourceCode)
                || StringUtils.isBlank(newClassSimpleName)) {
            return sourceCode;
        }

        String className = ClassManager.getClassName(sourceCode);
        if (StringUtils.isBlank(className)) {
            return sourceCode;
        }

        StringBuffer sb = new StringBuffer();

        Pattern pattern = Pattern.compile("[^A-Za-z0-9_\\.\"]" + className + "[^A-Za-z0-9_\"]");
        Matcher matcher = pattern.matcher(sourceCode);
        while (matcher.find()) {
            String group = matcher.group();

            matcher.appendReplacement(sb, group.replace(className, newClassSimpleName));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 获取添加导入的包后的源代码
     */
    public static String getImportedSourceCode(String sourceCode, List<String> importClassFullNameList) {
        if (importClassFullNameList.isEmpty()) {
            return sourceCode;
        }

        StringBuilder regularImport = new StringBuilder();
        StringBuilder lcdpImport = new StringBuilder();

        // 已加载的低代码的path
        List<String> lcdpImportedPathList = getLcdpImportedPathList(sourceCode);

        for (String importClassFullName : importClassFullNameList) {
            if (StringUtils.startsWith(importClassFullName, "@")) { // 低代码类
                importClassFullName = StringUtils.removeStart(importClassFullName, "@");

                if (!lcdpImportedPathList.contains(importClassFullName)) {
                    if (lcdpImport.length() > 0) {
                        lcdpImport.append(",");
                    }

                    lcdpImport.append("\"").append(importClassFullName).append("\"");
                }
            } else {
                if (regularImport.length() > 0) {
                    regularImport.append("\n");
                }

                regularImport.append("import ").append(importClassFullName).append(";");
            }
        }

        // 处理低代码导入的类
        if (lcdpImport.length() > 0) {
            Matcher pathImportMatcher = lcdpPathImportPattern.matcher(sourceCode);
            if (pathImportMatcher.find()) {
                String group = pathImportMatcher.group();

                // 没有{}时，需要添加
                if (!group.contains("{")) {
                    group = StringUtils.replaceFirst(group, "(", "({");
                    group = StringUtils.replaceLast(group, ")", "})");
                }

                if (group.contains("\"")) {
                    int lastIndex = group.lastIndexOf("\"");

                    String replace = group.substring(0, lastIndex + 1) + "," + lcdpImport.toString() + group.substring(lastIndex + 1);

                    sourceCode = StringUtils.replaceFirst(sourceCode, group, replace);
                } else if (group.contains("}")) {
                    int lastIndex = group.lastIndexOf("}");

                    String replace = group.substring(0, lastIndex) + lcdpImport.toString() + group.substring(lastIndex);

                    sourceCode = StringUtils.replaceFirst(sourceCode, group, replace);
                } else {
                    int lastIndex = group.lastIndexOf(")");

                    String replace = group.substring(0, lastIndex) + lcdpImport.toString() + group.substring(lastIndex);

                    sourceCode = StringUtils.replaceFirst(sourceCode, group, replace);
                }
            } else {
                Matcher publicClassMatcher = ClassManager.classPattern.matcher(sourceCode);
                if (publicClassMatcher.find()) {
                    String group = publicClassMatcher.group();

                    String replace = "@LcdpPathImport({" + lcdpImport.toString() + "})\n" + group;

                    sourceCode = StringUtils.replaceFirst(sourceCode, group, replace);
                }
            }
        }

        if (ClassManager.packagePattern.matcher(sourceCode).find()) { // 源代码有包
            return sourceCode.replaceFirst(";", ";\n" + regularImport.toString());
        } else {
            return regularImport.toString() + "\n" + sourceCode;
        }
    }

    /**
     * 获取可编译的源代码，主要是添加默认必要导入的包并用于向下兼容
     */
    public static String getCompilableSourceCode(String sourceCode) {
        List<String> requiredImportList = new ArrayList<>(REQUIRED_IMPORT_LIST);

        Matcher importMatcher = importPattern.matcher(sourceCode);
        while (importMatcher.find()) {
            String importPackage = importMatcher.group("package");
            if (requiredImportList.contains(importPackage)) {
                requiredImportList.remove(importPackage);
            }
        }

        if (!requiredImportList.isEmpty()) {
            sourceCode = getImportedSourceCode(sourceCode, requiredImportList);
        }

        if (StringUtils.contains(sourceCode, "com.sunwayworld.framework.io.file.path.FilePathManager")) {
            sourceCode = StringUtils.replace(sourceCode, "FilePathManager", "FilePathService");
        }

        return sourceCode;
    }

    /**
     * 编译源代码，编译前会添加必须要导入的包
     */
    public static Class<?> loadSourceCode(LcdpResourceBean resource) {
        Class<?> clazz = null;

        try {
            String className = ClassManager.getClassFullName(resource.getClassContent());

            if (ClassManager.isClassPresent(className)) {
                clazz = ClassManager.getClassByFullName(className);

                SpringUtils.getBean(LcdpErrorScriptService.class).deleteByResourceId(resource.getId());
            } else {
                long start = System.currentTimeMillis();
                clazz = DynamicClassLoader.getInstance().loadSourceCode(getCompilableSourceCode(resource.getClassContent()));
                logger.info("从资源表中编译资源ID={}, 资源名称={}的源代码，耗时：{}毫秒",
                        resource.getId(),
                        resource.getResourceName(),
                        (System.currentTimeMillis() - start));

                SpringUtils.getBean(LcdpErrorScriptService.class).deleteByResourceId(resource.getId());
            }
        } catch (Exception ex) {
            SpringUtils.getBean(LcdpErrorScriptService.class).insertByResource(resource, ex.getMessage());

            throw new ApplicationRuntimeException(ex, "LCDP.EXCEPTION.SCRIPT_COMPILE_ERROR", resource.getPath());
        }

        // 更新脚本方法表
        updateProScriptMethod(Arrays.asList(resource));

        return clazz;
    }

    /**
     * 编译源代码，编译前会添加必须要导入的包
     */
    public static Class<?> loadSourceCode(LcdpResourceHistoryBean resourceHistory) {
        try {
            String className = ClassManager.getClassFullName(resourceHistory.getClassContent());

            if (ClassManager.isClassPresent(className)) {
                Class<?> clazz = ClassManager.getClassByFullName(className);

                SpringUtils.getBean(LcdpErrorScriptService.class).deleteByResourceId(resourceHistory.getResourceId(), "checkout");

                return clazz;
            } else {
                long start = System.currentTimeMillis();
                Class<?> clazz = DynamicClassLoader.getInstance().loadSourceCode(getCompilableSourceCode(resourceHistory.getClassContent()));
                logger.info("从历史表中编译资源ID={}, 资源名称={}的源代码，耗时：{}毫秒",
                        resourceHistory.getResourceId(),
                        resourceHistory.getResourceName(),
                        (System.currentTimeMillis() - start));

                SpringUtils.getBean(LcdpErrorScriptService.class).deleteByResourceId(resourceHistory.getResourceId(), "checkout");

                // 更新脚本方法表
                updateDevScriptMethod(Arrays.asList(resourceHistory));

                return clazz;
            }
        } catch (Exception ex) {
            SpringUtils.getBean(LcdpErrorScriptService.class).insertByHistory(resourceHistory, ex.getMessage());

            throw new ApplicationRuntimeException(ex);
        }
    }

    /**
     * 编译资源的源代码，并注册到spring容器中
     */
    public static void loadAndRegisterResourceist(List<LcdpResourceBean> resourceList) {
        if (resourceList.isEmpty()) {
            return;
        }

        List<Long> allResourceIdList = resourceList.stream()
                .map(LcdpResourceBean::getId)
                .collect(Collectors.toList());

        // 已编译过的
        List<Long> compiledResourceIdList = resourceList.stream()
                .filter(r -> ClassManager.isClassPresent(ClassManager.getClassFullName(r.getClassContent())))
                .map(LcdpResourceBean::getId)
                .collect(Collectors.toList());

        // 删除已编译过的
        resourceList.removeIf(r -> compiledResourceIdList.contains(r.getId()));

        if (!resourceList.isEmpty()) {
            Map<String, String> sourceMap = new HashMap<>();
            resourceList.forEach(r -> {
                sourceMap.put(ClassManager.getClassFullName(r.getClassContent()), r.getClassContent());
            });

            LoadMultipleResult result = DynamicClassLoader.getInstance().loadSourceCode(sourceMap);

            // 有异常
            if (!CollectionUtils.isEmpty(result.getErrorMessageMap())) {
                StringBuilder sb = new StringBuilder();

                for (Entry<String, String> error : result.getErrorMessageMap().entrySet()) {
                    LcdpResourceBean resource = resourceList.stream()
                            .filter(r -> error.getKey().equals(ClassManager.getClassFullName(r.getClassContent())))
                            .findAny().get();

                    SpringUtils.getBean(LcdpErrorScriptService.class).insertByResource(resource, error.getValue());

                    if (sb.length() > 0) {
                        sb.append("\n");
                    }

                    sb.append("类名：").append(error.getKey()).append(" --- 异常日志：").append(error.getValue());
                }

                logger.error(sb.toString());

                throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.RESOURCES.TIP.BACKEND_SCRIPT_REGISTRATION_SPRING_ERROR") + sb.toString());
            }

            // 注册到spring
            result.getClassMap().forEach((n, c) -> registerBean(c));

            // 更新脚本方法表
            updateProScriptMethod(resourceList);
        }

        SpringUtils.getBean(LcdpErrorScriptService.class).deleteByScriptIdList(allResourceIdList, "submit");
    }

    /**
     * 编译历史资源的源代码，并注册到spring容器中
     */
    public static void loadAndRegisterResourceHistoryList(List<LcdpResourceHistoryBean> resourceHistoryList) {
        if (resourceHistoryList.isEmpty()) {
            return;
        }

        List<Long> allResourceIdList = resourceHistoryList.stream()
                .map(LcdpResourceHistoryBean::getResourceId)
                .collect(Collectors.toList());

        // 已编译过的
        List<Long> compiledResourceIdList = resourceHistoryList.stream()
                .filter(h -> ClassManager.isClassPresent(ClassManager.getClassFullName(h.getClassContent())))
                .map(LcdpResourceHistoryBean::getResourceId)
                .collect(Collectors.toList());

        // 删除已编译过的
        resourceHistoryList.removeIf(h -> compiledResourceIdList.contains(h.getResourceId()));

        if (!resourceHistoryList.isEmpty()) {
            Map<String, String> sourceMap = new HashMap<>();
            resourceHistoryList.forEach(h -> {
                sourceMap.put(ClassManager.getClassFullName(h.getClassContent()), h.getClassContent());
            });

            LoadMultipleResult result = DynamicClassLoader.getInstance().loadSourceCode(sourceMap);

            // 有异常
            if (!CollectionUtils.isEmpty(result.getErrorMessageMap())) {
                StringBuilder sb = new StringBuilder();

                for (Entry<String, String> error : result.getErrorMessageMap().entrySet()) {
                    LcdpResourceHistoryBean history = resourceHistoryList.stream()
                            .filter(h -> error.getKey().equals(ClassManager.getClassFullName(h.getClassContent())))
                            .findAny().get();

                    SpringUtils.getBean(LcdpErrorScriptService.class).insertByHistory(history, error.getValue());

                    if (sb.length() > 0) {
                        sb.append("\n");
                    }

                    sb.append("类名：").append(error.getKey()).append(" --- 异常日志：").append(error.getValue());
                }

                logger.error(sb.toString());

                throw new CheckedException(I18nHelper.getMessage("LCDP.MODULE.RESOURCES.TIP.BACKEND_SCRIPT_REGISTRATION_SPRING_ERROR") + sb.toString());
            }

            resourceHistoryList.forEach(r -> r.setCompiledVersion(r.getModifyVersion()));
            SpringUtils.getBean(LcdpResourceHistoryService.class).getDao().update(resourceHistoryList, "COMPILEDVERSION");

            // 注册到spring
            result.getClassMap().forEach((n, c) -> registerBean(c));

            // 更新脚本方法表
            updateDevScriptMethod(resourceHistoryList);
        }

        SpringUtils.getBean(LcdpErrorScriptService.class).deleteByScriptIdList(allResourceIdList, "checkout");
    }

    public static void updateProScriptMethod(List<LcdpResourceBean> resourceList) {
        LcdpServerScriptMethodService methodService = SpringUtils.getBean(LcdpServerScriptMethodService.class);

        List<Long> resourceIdList = resourceList.stream().map(LcdpResourceBean::getId).collect(Collectors.toList());

        // 查询所有已存在的脚本方法
        List<LcdpServerScriptMethodBean> selectedScriptMethodList = methodService.getDao().selectListByOneColumnValues(resourceIdList, "SERVERSCRIPTID");

        List<Long> deleteScriptMethodIdList = new ArrayList<>();
        List<LcdpServerScriptMethodBean> insertScriptMethodList = new ArrayList<>();


        for (LcdpResourceBean resource : resourceList) {
            Class<?> clazz = ClassManager.getClassByFullName(ClassManager.getClassFullName(resource.getClassContent()));
            if (clazz == null) {
                continue;
            }

            List<Method> methodList = Arrays.stream(clazz.getMethods())
                    .filter(m -> m.getAnnotation(Mapping.class) != null)
                    .collect(Collectors.toList());

            selectedScriptMethodList.forEach(s -> {
                if (methodList.stream().noneMatch(m -> {
                    Mapping annotation = m.getAnnotation(Mapping.class);
                    String methodName = m.getName();
                    String methodPath = resource.getPath() + "." + methodName;

                    return Objects.equals(s.getMethodName(), methodName)
                            && Objects.equals(s.getMethodPath(), methodPath)
                            && Objects.equals(s.getMethodDesc(), annotation.value())
                            && Objects.equals(s.getMappingType(), annotation.type().name());
                })) {
                    deleteScriptMethodIdList.add(s.getId());
                }
            });

            for (Method method : methodList) {
                Mapping annotation = method.getAnnotation(Mapping.class);
                String methodName = method.getName();
                String methodPath = resource.getPath() + "." + methodName;

                // 已存在
                if (selectedScriptMethodList.stream().anyMatch(m -> Objects.equals(m.getMethodName(), methodName)
                        && Objects.equals(m.getMethodPath(), methodPath)
                        && Objects.equals(m.getMethodDesc(), annotation.value())
                        && Objects.equals(m.getMappingType(), annotation.type().name()))) {
                    continue;
                }

                LcdpServerScriptMethodBean serverScriptMethod = new LcdpServerScriptMethodBean();
                serverScriptMethod.setId(ApplicationContextHelper.getNextIdentity());
                serverScriptMethod.setServerScriptId(resource.getId());
                serverScriptMethod.setMethodName(methodName);
                serverScriptMethod.setMethodPath(methodPath);
                serverScriptMethod.setMethodDesc(annotation.value());
                serverScriptMethod.setMappingType(annotation.type().name());
                serverScriptMethod.setMethodFlag("normal");
                serverScriptMethod.setMethodCreatedById(LocalContextHelper.getLoginUserId());
                if (Page.class.isAssignableFrom(method.getReturnType())
                        && method.getParameterCount() == 1
                        && RestJsonWrapperBean.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    serverScriptMethod.setTodoFlag(Constant.YES);
                } else {
                    serverScriptMethod.setTodoFlag(Constant.NO);
                }

                insertScriptMethodList.add(serverScriptMethod);
            }
        }

        methodService.getDao().deleteByIdList(deleteScriptMethodIdList);
        methodService.getDao().fastInsert(insertScriptMethodList);
    }

    public static void updateDevScriptMethod(List<LcdpResourceHistoryBean> resourceHistoryList) {
        LcdpServerScriptMethodService methodService = SpringUtils.getBean(LcdpServerScriptMethodService.class);

        List<Long> resourceIdList = resourceHistoryList.stream().map(LcdpResourceHistoryBean::getResourceId).collect(Collectors.toList());

        // 查询所有已存在的脚本方法
        List<LcdpServerScriptMethodBean> selectedScriptMethodList = methodService.getDao().selectListByOneColumnValues(resourceIdList, "SERVERSCRIPTID");

        List<Long> deleteScriptMethodIdList = new ArrayList<>();
        List<LcdpServerScriptMethodBean> insertScriptMethodList = new ArrayList<>();


        for (LcdpResourceHistoryBean history : resourceHistoryList) {
            Class<?> clazz = ClassManager.getClassByFullName(ClassManager.getClassFullName(history.getClassContent()));

            List<Method> methodList = Arrays.stream(clazz.getMethods())
                    .filter(m -> m.getAnnotation(Mapping.class) != null)
                    .collect(Collectors.toList());

            selectedScriptMethodList.forEach(s -> {
                if (methodList.stream().noneMatch(m -> {
                    Mapping annotation = m.getAnnotation(Mapping.class);
                    String methodName = m.getName();
                    String methodPath = history.getPath() + "." + methodName;

                    return Objects.equals(s.getMethodName(), methodName)
                            && Objects.equals(s.getMethodPath(), methodPath)
                            && Objects.equals(s.getMethodDesc(), annotation.value())
                            && Objects.equals(s.getMappingType(), annotation.type().name());
                })) {
                    deleteScriptMethodIdList.add(s.getId());
                }
            });

            for (Method method : methodList) {
                Mapping annotation = method.getAnnotation(Mapping.class);
                String methodName = method.getName();
                String methodPath = history.getPath() + "." + methodName;

                // 已存在
                if (selectedScriptMethodList.stream().anyMatch(m -> Objects.equals(m.getMethodName(), methodName)
                        && Objects.equals(m.getMethodPath(), methodPath)
                        && Objects.equals(m.getMethodDesc(), annotation.value())
                        && Objects.equals(m.getMappingType(), annotation.type().name()))) {
                    continue;
                }

                LcdpServerScriptMethodBean serverScriptMethod = new LcdpServerScriptMethodBean();
                serverScriptMethod.setId(ApplicationContextHelper.getNextIdentity());
                serverScriptMethod.setServerScriptId(history.getResourceId());
                serverScriptMethod.setMethodName(methodName);
                serverScriptMethod.setMethodPath(methodPath);
                serverScriptMethod.setMethodDesc(annotation.value());
                serverScriptMethod.setMappingType(annotation.type().name());
                serverScriptMethod.setMethodFlag("normal");
                serverScriptMethod.setMethodCreatedById(LocalContextHelper.getLoginUserId());
                if (StringUtils.equals(history.getEffectFlag(), LcdpConstant.EFFECT_FLAG_NO)
                        && history.getVersion() == 1) {
                    serverScriptMethod.setMethodFlag("draft");
                }
                if (Page.class.isAssignableFrom(method.getReturnType())
                        && method.getParameterCount() == 1
                        && RestJsonWrapperBean.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    serverScriptMethod.setTodoFlag(Constant.YES);
                } else {
                    serverScriptMethod.setTodoFlag(Constant.NO);
                }

                insertScriptMethodList.add(serverScriptMethod);
            }
        }

        methodService.getDao().deleteByIdList(deleteScriptMethodIdList);
        methodService.getDao().fastInsert(insertScriptMethodList);
    }

    /**
     * 编译源代码并注册到Spring容器中
     */
    public static Class<?> loadAndRegisterSourceCode(LcdpResourceBean resource) {
        Class<?> clazz = loadSourceCode(resource);

        registerBean(clazz);

        return clazz;
    }

    /**
     * 编译源代码并注册到Spring容器中
     */
    public static Class<?> loadAndRegisterSourceCode(LcdpResourceHistoryBean resourceHistory) {
        Class<?> clazz = loadSourceCode(resourceHistory);

        registerBean(clazz);

        return clazz;
    }

    /**
     * 编译源代码，编译前会添加必须要导入的包
     */
    public static Class<?> loadSourceCode(String sourceCode) {
        String className = ClassManager.getClassFullName(sourceCode);

        if (ClassManager.isClassPresent(className)) {
            return ClassManager.getClassByFullName(className);
        } else {
            Class<?> clazz = null;
            try {
                clazz = DynamicClassLoader.getInstance().loadSourceCode(getCompilableSourceCode(sourceCode));
            } catch (Error e) {
                logger.error(e.getMessage(), e);
                return null;
            }
            return clazz;
        }
    }

    /**
     * 删除已加载的开发中的类
     */
    public static void removeLoadedDevClass(LcdpResourceBean resource) {
        String prefix = resource.getPath() + "." + resource.getResourceName();

        Predicate<String> prediate = n -> StringUtils.startsWith(n, prefix)
                && lcdpDevClassSuffixPattern.matcher(StringUtils.removeStart(n, prefix)).find();

        List<String> classFullNameList = ClassManager.getClassFullNameList(prediate);

        if (!classFullNameList.isEmpty()) {
            classFullNameList.forEach(n -> {
                Class<?> clazz = ClassManager.getClassByFullName(n);

                ClassManager.remove(n);

                SpringUtils.removeBean(clazz);
            });
        }
    }

    /**
     * 删除已加载正式的类
     */
    public static void removeLoadedProClass(LcdpResourceBean resource) {
        String prefix = resource.getPath() + "." + resource.getResourceName();

        Predicate<String> prediate = n -> StringUtils.startsWith(n, prefix)
                && lcdpProClassSuffixPattern.matcher(StringUtils.removeStart(n, prefix)).find();

        List<String> classFullNameList = ClassManager.getClassFullNameList(prediate);

        if (!classFullNameList.isEmpty()) {
            classFullNameList.forEach(n -> {
                Class<?> clazz = ClassManager.getClassByFullName(n);

                ClassManager.remove(n);

                SpringUtils.removeBean(clazz);
            });
        }
    }

    /**
     * 删除之前已加载的开发中的类，保留版本最大的
     */
    public static void removePreLoadedDevClass(LcdpResourceBean resource) {
        String prefix = resource.getPath() + "." + resource.getResourceName();

        Predicate<String> prediate = n -> StringUtils.startsWith(n, prefix)
                && lcdpDevClassSuffixPattern.matcher(StringUtils.removeStart(n, prefix)).find();

        List<String> classFullNameList = ClassManager.getClassFullNameList(prediate);

        if (!classFullNameList.isEmpty()) {
            long maxVersion = 0L;
            long maxModifyVersion = 0L;

            for (String classFullName : classFullNameList) {
                Matcher matcher = lcdpDevClassSuffixPattern.matcher(classFullName);
                if (matcher.find()) {
                    long version = NumberUtils.parseLong(matcher.group("version"));
                    long modifyVersion = NumberUtils.parseLong(matcher.group("modifyVersion"));

                    if (version > maxVersion) {
                        maxVersion = version;
                    }
                    if (modifyVersion > maxModifyVersion) {
                        maxModifyVersion = modifyVersion;
                    }
                }
            }

            String latestNameSuffix = "v" + maxVersion + "m" + maxModifyVersion;

            classFullNameList.forEach(n -> {
                if (!StringUtils.endsWith(n, latestNameSuffix)) {
                    Class<?> clazz = ClassManager.getClassByFullName(n);

                    ClassManager.remove(n);

                    SpringUtils.removeBean(clazz);
                }
            });
        }
    }

    /**
     * 删除之前已加载的开发中的类，保留版本最大的
     */
    public static void removePreLoadedProClass(LcdpResourceBean resource) {
        String prefix = resource.getPath() + "." + resource.getResourceName();

        Predicate<String> prediate = n -> StringUtils.startsWith(n, prefix)
                && lcdpProClassSuffixPattern.matcher(StringUtils.removeStart(n, prefix)).find();

        List<String> classFullNameList = ClassManager.getClassFullNameList(prediate);

        if (!classFullNameList.isEmpty()) {
            long maxVersion = 0L;

            for (String classFullName : classFullNameList) {
                Matcher matcher = lcdpProClassSuffixPattern.matcher(classFullName);
                if (matcher.find()) {
                    long version = NumberUtils.parseLong(matcher.group("version"));

                    if (version > maxVersion) {
                        maxVersion = version;
                    }
                }
            }

            String latestNameSuffix = "v" + maxVersion;

            classFullNameList.forEach(n -> {
                if (!StringUtils.endsWith(n, latestNameSuffix)) {
                    Class<?> clazz = ClassManager.getClassByFullName(n);

                    ClassManager.remove(n);

                    SpringUtils.removeBean(clazz);
                }
            });
        }
    }

    /**
     * 获取所有已导入的包
     */
    public static List<String> getImportedPackageList(String sourceCode) {
        List<String> importedPackageList = new ArrayList<>();

        sourceCode = getNocommentJavaCode(sourceCode);

        Matcher importMatcher = importPattern.matcher(sourceCode);
        while (importMatcher.find()) {
            importedPackageList.add(importMatcher.group("package"));
        }

        List<String> lcdpImportedPathList = getLcdpImportedPathList(sourceCode);
        if (!lcdpImportedPathList.isEmpty()) {
            lcdpImportedPathList.forEach(p -> importedPackageList.add("@" + p));
        }

        return importedPackageList;
    }

    /**
     * 获取所有低代码导入的包
     */
    public static List<String> getLcdpImportedPathList(String sourceCode) {
        sourceCode = getNocommentJavaCode(sourceCode);

        List<String> importedPackageList = new ArrayList<>();

        Matcher lcdpImportMatcher = lcdpPathImportPattern.matcher(sourceCode);
        if (lcdpImportMatcher.find()) {
            String group = lcdpImportMatcher.group();

            Matcher stringMatcher = lcdpPathImportStringPattern.matcher(group);
            while (stringMatcher.find()) {
                importedPackageList.add(stringMatcher.group("classFullName"));
            }
        }

        return importedPackageList;
    }

    /**
     * 通过路径前缀
     */
    public static boolean isLcdpClassPresent(String lcdpPath, String className) {
        String lcdpClassNamePrefix = LcdpJavaCodeResolverUtils.getLcdpClassNamePrefix(lcdpPath, className);

        List<String> classFullNameList = ClassManager.getMatchedClassFullNameList(n -> StringUtils.startsWith(n, lcdpClassNamePrefix));

        if (!classFullNameList.isEmpty()) {
            if (classFullNameList.stream().anyMatch(n -> LcdpReflectionUtils.isLcdpClass(n))) {
                return true;
            }
        }

        // 代码可能还没有编译
        String path = getFullPath(lcdpPath, className);

        return Constant.YES.equals(SpringUtils.getBean(LcdpResourceHistoryService.class).isExists(path));
    }

    /**
     * 根据LcdpPath注解里的值和类名获取全路径
     */
    public static String getFullPath(String lcdpPath, String className) {
        if (StringUtils.isBlank(lcdpPath)) { // 向下兼容
            return SpringUtils.getBean(LcdpResourceService.class).getPathByClassName(className);
        } else if (StringUtils.endsWith(lcdpPath, ".service")) {
            return lcdpPath + ".service." + className;
        } else if (!StringUtils.endsWith(lcdpPath, "." + className)) {
            return lcdpPath + "." + className;
        }

        return lcdpPath;
    }

    /**
     * 验证源代码是否已注册到Spring容器中
     */
    public static boolean isBeanExists(String sourceCode) {
        return SpringUtils.isBeanExists(getBeanName(sourceCode));
    }

    /**
     * 获取源代码编译后注册到Spring容器时bean的名称
     */
    public static String getBeanName(String sourceCode) {
        String packageName = ClassManager.getPackageName(sourceCode);
        String className = ClassManager.getClassName(sourceCode);

        return (StringUtils.isBlank(packageName) ? "<empty>" : packageName) + "$" + className;
    }

    /**
     * 获取类注册到Spring容器时bean的名称
     */
    public static String getBeanName(LcdpResourceBean resource) {
        return getBeanName(resource.getClassContent());
    }

    /**
     * 获取类注册到Spring容器时bean的名称
     */
    public static String getBeanName(Class<?> clazz) {
        String className = clazz.getName();

        if (StringUtils.contains(className, ".")) {
            return StringUtils.replaceLast(className, ".", "$");
        } else {
            return "<empty>$" + className;
        }
    }

    /**
     * 注册指定的类到spring容器中，bean的名称为 包名 + "$" + 类简称
     */
    public static void registerBean(Class<?> clazz) {
        SpringUtils.registerBean(getBeanName(clazz), clazz);
    }

    /**
     * 源代码没有包的话，在第一行加入包，如果包内容和指定的
     */
    public static String updatePackage(String sourceCode, String packageName) {
        Matcher packageMatcher = ClassManager.packagePattern.matcher(sourceCode);
        if (packageMatcher.find()) {
            sourceCode = packageMatcher.replaceAll("");

            sourceCode = "\n" + StringUtils.trim(sourceCode);
        }

        return "package " + packageName + ";\n" + sourceCode;
    }

    /**
     * 获取删除注释后的源代码
     */
    public static String getNocommentJavaCode(String javaCode) {
        return Pattern.compile(toggleCommentRegExp, Pattern.MULTILINE).matcher(javaCode.replaceAll(blockCommentRegExp, "")).replaceAll("");
    }

    /**
     * 获取生效待编译的源代码（类名已替换）
     */
    public static String getClassContent(String sourceCode, String className, Long v, Long m, Long versionOffset) {
        Long version = Optional.ofNullable(v).orElse(0L) + Optional.ofNullable(versionOffset).orElse(0L);

        String finalClassName = className + "v" + version + (m == null ? "" : "m" + m);

        return LcdpJavaCodeResolverUtils.replaceClassSimpleName(sourceCode, finalClassName);
    }

    /**
     * 根据页面的资源名称获取默认的服务名称
     */
    public static String getDefaultServiceName(String pageResourceName) {
        return "Lcdp" + StringUtils.capitalize(StringUtils.removeStart(StringUtils.removeEndIgnoreCase(pageResourceName, "Page"), "Lcdp")) + "Service";
    }

    /**
     * 根据源码和方法描述获取方法所在行号
     */
    public static int getLineNumber(String sourceCode, String methodOrFieldText, String methodOrFieldName, boolean fieldFlag) {

        String matchRegex = null;

        if (fieldFlag) {
            matchRegex = methodOrFieldName;
        } else {
            matchRegex = methodOrFieldName + "\\(";
            Matcher matcher = bracketPattern.matcher(methodOrFieldText);

            // 获取方法参数字符串
            if (matcher.find()) {
                String methodParamStr = matcher.group(1);

                List<String> methodParamTypeList = StringUtils.splitIgnoringAngleBrackets(methodParamStr);

                int methodParamTypeSize = methodParamTypeList.size();

                for (int i = 0; i < methodParamTypeSize; i++) {


                    String methodParamType = methodParamTypeList.get(i).trim();

                    logger.info("methodParamType: {}", methodParamType);

                    if (StringUtils.isEmpty(methodParamType)) {
                        continue;
                    }

                    // 当为泛型时源码里边可能也是？,可能是V这种, 匹配的时候匹配？或者字符
                    if (methodParamType.contains("?")) {
                        methodParamType = methodParamType.replaceAll("\\?", "\\\\??\\\\w*");
                    }

                    // 处理源码中getString(final Map<String, ?> map, final String key)的final这种修饰情况
                    matchRegex += "\\w*\\s*";

                    if (i == methodParamTypeSize - 1) {
                        // 最后一个可能是可变参数，替换掉[],匹配...
                        matchRegex += methodParamType.replace("[", "").replace("]", "") + "\\s*\\.*" + "\\s+\\w+";
                    } else {
                        // 匹配参数名和空格
                        matchRegex += methodParamType.replace("[", "\\[").replace("]", "\\]") + "\\s+\\w+";
                    }


                    if (i != methodParamTypeSize - 1) {
                        matchRegex += "\\,\\s*";
                    }
                }


            }

            matchRegex += "\\)";


        }

        // 按行分割字符串
        String[] lines = sourceCode.split("\n");

        logger.info("matchRegex: {}", matchRegex);


        Pattern methodPattern = Pattern.compile(matchRegex);

        // 遍历每一行，检查是否匹配目标内容
        for (int i = 0; i < lines.length; i++) {
            Matcher methodMatcher = methodPattern.matcher(lines[i]);
            if (methodMatcher.find()) {
                return i + 1; // 行号从 1 开始
            }
        }

        // 如果没有找到目标内容，返回 -1
        return -1;
    }

    /**
     * 判断名称是否是开发模式的，名称为Bean的名称或类名
     */
    public static boolean isDevName(String name) {
        return lcdpDevClassSuffixPattern.matcher(name).find();
    }

    /**
     * 获取开发名称中的“版本”，开发名称为xxxv版本m变更版本，如：xxxv1m1<br>
     * 返回的“版本”为 v * 10000 + m
     */
    public static int getDevVersion(String devName) {
        Matcher matcher = lcdpDevClassSuffixPattern.matcher(devName);

        int version = NumberUtils.parseInt(matcher.group("version"));
        int modifyVersion = NumberUtils.parseInt(matcher.group("modifyVersion"));

        return version * 10000 + modifyVersion;
    }

    /**
     * 判断名称是否是正式模式的，名称为Bean的名称或类名
     */
    public static boolean isProName(String name) {
        return lcdpProClassSuffixPattern.matcher(name).find();
    }

    /**
     * 获取正式名称中的版本，正式名称为xxxv版本，如：xxxv1<br>
     * 返回的版本为 1
     */
    public static int getProVersion(String devName) {
        Matcher matcher = lcdpProClassSuffixPattern.matcher(devName);

        int version = NumberUtils.parseInt(matcher.group("version"));

        return version;
    }

    //------------------------------------------------------------------------------
    // 私有方法
    //------------------------------------------------------------------------------


    /**
     * 获取所有疑似要导入的类
     */
    private static List<String> getSuspectedToImportClassNameList(String sourceCode) {
        Set<String> classNameSet = new HashSet<>();

        String sourceCodeClassName = ClassManager.getClassName(sourceCode);

        long stageTime = System.currentTimeMillis();

        // 获取所有类
        Matcher classMatcher = classPattern.matcher(sourceCode);
        while (classMatcher.find()) {
            String className = classMatcher.group("Class");

            if (!Objects.equals(sourceCodeClassName, className)) {
                classNameSet.add(className);
            }
        }

        logger.info("解析所有类耗时：{}毫秒",
                (System.currentTimeMillis() - stageTime));
        stageTime = System.currentTimeMillis();

        return new ArrayList<>(classNameSet);
    }

    private static boolean isImported(List<String> importedPackageList, String classFullName) {
        if (StringUtils.isBlank(classFullName)) {
            return true;
        }

        for (String importedPackage : importedPackageList) {
            importedPackage = StringUtils.removeStart(importedPackage, "@"); // 低代码导入的包前缀是@

            if (StringUtils.endsWith(importedPackage, ".*")) {
                String prefix = StringUtils.removeEnd(importedPackage, "*");

                if (StringUtils.startsWith(classFullName, prefix)
                        && !StringUtils.removeStart(classFullName, prefix).contains(".")) {
                    return true;
                }
            } else {
                if (importedPackage.equals(classFullName)) {
                    return true;
                }

                if (classFullName.contains(".")) {
                    if (importedPackage.endsWith(classFullName.substring(classFullName.lastIndexOf(".")))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 获取可解析的java源代码，去掉注释并把字符串转为空字符串
     */
    private static String getResolvableJavaCode(String javaCode) {
        String resolvableJavaCode = getNocommentJavaCode(javaCode).replaceAll(stringRegExp, "null");
        Pattern pattern = Pattern.compile("^package\\s+[\\w.]+;\\s*$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(resolvableJavaCode);
        if (matcher.find()) {
            resolvableJavaCode = resolvableJavaCode.replaceAll(matcher.group(), "");
        }
        return resolvableJavaCode;
    }


}
