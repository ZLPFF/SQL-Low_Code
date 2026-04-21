package com.sunwayworld.cloud.module.lcdp.resource.support;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.framework.database.dialect.DialectRepository;
import com.sunwayworld.framework.exception.checked.CheckedException;
import com.sunwayworld.framework.exception.core.ApplicationWarningException;
import com.sunwayworld.framework.mybatis.MybatisHelper;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.ReflectionUtils;
import com.sunwayworld.framework.utils.StringUtils;

/**
 * @author yangsz@sunway.com 2023-02-07
 * 实现mybatis对低代码mapper加载、卸载
 */
public class LcdpMapperUtils {
    private static final Logger logger = LoggerFactory.getLogger(LcdpMapperUtils.class);

    //检出mapper对应的namespace后缀
    public static final String NAMESPACE_DEV_SUFFIX = "$DEV";

    //低代码热加载mapper的resource前缀
    private static final String RESOURCE_PREFIX = "LCDP_MAPPER_HOT_RELOAD:";

    //方言Mapper名后缀
    public static final String DIALECT_MAPPER_SUFFIX = DialectRepository.getDialect().getDatabase().substring(0, 1).toUpperCase() + DialectRepository.getDialect().getDatabase().substring(1) + "Mapper";

    /**
     * 包含mapper
     *
     * @return
     */
    public static boolean containMapper(String mapperId) {
        Configuration configuration = MybatisHelper.getSqlSessionFactory().getConfiguration();

        @SuppressWarnings("unchecked")
        Set<String> loadedResourcesSet = (Set<String>) ReflectionUtils.getFieldValue(configuration, "loadedResources");
        return loadedResourcesSet.contains(mapperId);
    }


    /**
     * mapper加载
     *
     * @param lcdpMapperPath   低代码mapper资源路径
     * @param isPro            mapper是否生产使用
     * @param mapperXmlContent mapper.xml文本
     */
    public static void loadMapper(String lcdpMapperPath, boolean isPro, String mapperXmlContent) {
        Configuration configuration = MybatisHelper.getSqlSessionFactory().getConfiguration();

        //根据路径名与环境生成对应resource
        String resource = getResource(lcdpMapperPath, isPro);

        //根据路径名与环境生成对应的namespace
        String namespace = getNamespace(lcdpMapperPath, isPro);

        //mapper的namespace替换为生成的namespace
        String content = changeMapperNamespace(mapperXmlContent, namespace);

        //加载并覆盖mapper
        @SuppressWarnings("unchecked")
        Set<String> loadedResourcesSet = (Set<String>) ReflectionUtils.getFieldValue(configuration, "loadedResources");
        boolean contains = loadedResourcesSet.contains(resource);
        try {
            //删除resource才能使用XMLMapperBuilder重新加载
            loadedResourcesSet.remove(resource);

            //校验并找出mapperId集合
            List<String> mapperIdList = parseMapperIdListByXml(content, configuration, namespace);

            //只加载对应数据库的mapper
            if (!StringUtils.endsWith(lcdpMapperPath, DIALECT_MAPPER_SUFFIX)) {
                return;
            }
            synchronized (configuration) {
                //加载xml
                XMLMapperBuilder builder = new XMLMapperBuilder(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), configuration, resource, configuration.getSqlFragments());
                builder.parse();

                //删除namespace中应该删除的mappedStatements及相关资源
                clearStrictMap(namespace, mapperId -> !mapperIdList.contains(mapperId));
            }

        } catch (Exception e) {
            logger.error("============>mapper加载异常：path为" + lcdpMapperPath + "，异常日志如下：" + e.getMessage());
            //加载异常还原resource
            if (contains) {
                loadedResourcesSet.add(resource);
            }
            if(isPro){
                throw new CheckedException(e);
            }
        }
    }

    //系统启动时，批量加载mapper
    public static void batchLoadMapper(List<LcdpResourceBean> mapperResourceList) {
        //过滤重复命名空间mapper,映射为<namespace,mapperContent>的键值对
        List<String> existedResourceList = new ArrayList<>();
        Map<String, String> map = mapperResourceList.stream().filter(mapper -> {
            if (!StringUtils.endsWith(mapper.getPath(), DIALECT_MAPPER_SUFFIX)) {
                return false;
            }
            String namespace = getNamespace(mapper.getPath(), true);
            if (existedResourceList.contains(namespace)) {
                return false;
            }
            return existedResourceList.add(namespace);
        }).collect(Collectors.toMap(mapper -> getNamespace(mapper.getPath(), true), LcdpResourceBean::getContent));
        //记录已加载过的mapper
        Set<String> loadedMapperSet = new HashSet<>();

        //迭代器遍历加载
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            try {
                loadMapper(next.getKey(), next.getValue(), map, loadedMapperSet, new HashSet<>());
            } catch (Exception e) {
                logger.error("============>加载mapper：" + next.getKey() + "异常，异常日志如下：" + e.getMessage());
            }

            if (loadedMapperSet.contains(next.getKey())) {
                iterator.remove();
            }
        }
    }

    private static void loadMapper(String namespace, String mapperContent, Map<String, String> mapperMap, Set<String> loadedMapperSet, Set<String> loadingMapperSet) {
        if (loadingMapperSet.contains(namespace)) {
            throw new CheckedException(loadingMapperSet + " exist circular reference");
        }

        if (loadedMapperSet.contains(namespace)) {
            return;
        }

        //mapper的namespace替换为生成的namespace
        String content = changeMapperNamespace(mapperContent, namespace);

        loadingMapperSet.add(namespace);

        Configuration configuration = MybatisHelper.getSqlSessionFactory().getConfiguration();

        XPathParser xPathParser = new XPathParser(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), true, configuration.getVariables(), new XMLMapperEntityResolver());
        XNode mapperNode = xPathParser.evalNode("/mapper");

        //找出select|insert|update|delete|sql|parameterMap|resultMap节点
        List<XNode> statementNodeList = mapperNode.evalNodes("/mapper/select|update|insert|sql|parameterMap|resultMap");

        List<String> sqlList = statementNodeList.stream().filter(node -> StringUtils.equalsIgnoreCase(node.getName(), "sql"))
                .map(node -> node.getStringAttribute("id")).collect(Collectors.toList());

        List<String> dependentMapperList = new ArrayList<>();

        statementNodeList.stream().forEach(node -> analyzeMapper(node, configuration, sqlList, namespace, dependentMapperList));

        //首先加载依赖mapper
        dependentMapperList.stream().distinct().forEach(dependentMapper -> {
            if (!mapperMap.containsKey(dependentMapper)) {
                throw new CheckedException("dependent reference:" + dependentMapper + " not exist");
            }

            loadMapper(dependentMapper, mapperMap.get(dependentMapper), mapperMap, loadedMapperSet, loadingMapperSet);
        });

        //加载xml
        synchronized (configuration) {
            String resource = namespace.replace(DIALECT_MAPPER_SUFFIX, "Mapper");

            XMLMapperBuilder builder = new XMLMapperBuilder(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), configuration, resource, configuration.getSqlFragments());

            builder.parse();
        }
    }

    private static void analyzeMapper(XNode node, Configuration configuration, List<String> sqlList, String namespace, List<String> dependentMapperList) {
        if (!ObjectUtils.isEmpty(node)) {

            for (XNode childen : node.getChildren()) {
                if (StringUtils.equals("include", childen.getName())) {
                    Map<String, XNode> sqlFragments = configuration.getSqlFragments();
                    String refId = childen.getStringAttribute("refid");
                    if (!sqlFragments.containsKey(refId) && !sqlList.contains(refId) && !sqlList.contains(refId.replace(namespace + ".", ""))) {
                        String dependentNamespace = refId.substring(0, refId.lastIndexOf("."));
                        if (!StringUtils.isBlank(dependentNamespace)) {
                            dependentMapperList.add(dependentNamespace);
                        }
                    }
                } else {
                    analyzeMapper(childen, configuration, sqlList, namespace, dependentMapperList);
                }
            }
        }
    }


    /**
     * mapper卸载
     *
     * @param lcdpMapperPath 低代码mapper资源路径
     * @param isPro          mapper是否生产使用
     */
    public static void unloadMapper(String lcdpMapperPath, boolean isPro) {
        //只删除当前数据库类型的mapper
        if (!StringUtils.endsWith(lcdpMapperPath, DIALECT_MAPPER_SUFFIX)) {
            return;
        }
        Configuration configuration = MybatisHelper.getSqlSessionFactory().getConfiguration();

        //根据路径名与环境生成对应resource
        String resource = getResource(lcdpMapperPath, isPro);
        //根据路径名与环境生成对应的namespace
        String namespace = getNamespace(lcdpMapperPath, isPro);
        //清除原配置
        @SuppressWarnings("unchecked")
        Set<String> loadedResourcesSet = (Set<String>) ReflectionUtils.getFieldValue(configuration, "loadedResources");
        loadedResourcesSet.remove(resource);

        //删除namespace中所有的mappedStatements及相关资源
        clearStrictMap(namespace, mapperId -> true);
    }


    /**
     * mapper校验
     *
     * @param lcdpMapperPath   低代码mapper资源路径
     * @param isPro            mapper是否生产使用
     * @param mapperXmlContent mapper.xml文本
     */
    public static void validateMapper(String lcdpMapperPath, boolean isPro, String mapperXmlContent) {
        Configuration configuration = MybatisHelper.getSqlSessionFactory().getConfiguration();

        //根据路径名与环境生成对应的namespace
        String namespace = getNamespace(lcdpMapperPath, isPro);

        //mapper的namespace替换为生成的namespace
        String content = changeMapperNamespace(mapperXmlContent, namespace);

        try {
            //校验
            XPathParser xPathParser = new XPathParser(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), true, configuration.getVariables(), new XMLMapperEntityResolver());
            XNode mapperNode = xPathParser.evalNode("/mapper");
            String mapperNamespace = mapperNode.getStringAttribute("namespace");
            //校验namespace是否一致
            if (!StringUtils.equals(mapperNamespace, namespace)) {
                throw new ApplicationWarningException("mapper namespace error");
            }

        } catch (Exception e) {
            throw new ApplicationWarningException(e);
        }
    }
    
    /**
     * 重新加载mapper文件
     */
    public static void reloadMapper(String mapperPath, boolean isPro, String mapperContent) {
        unloadMapper(mapperPath, isPro);
        
        loadMapper(mapperPath, isPro, mapperContent);
    }

    /**
     * 根据namespace与mapperId过滤器，删除mybatis的mappedStatement、sqlFragment等资源
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void clearStrictMap(String namespace, Predicate<String> mapperIdFilter) {
        Configuration configuration = MybatisHelper.getSqlSessionFactory().getConfiguration();

        for (String field : new String[]{"mappedStatements", "caches", "resultMaps", "parameterMaps", "keyGenerators", "sqlFragments"}) {
            Object fieldValue = ReflectionUtils.getFieldValue(configuration, field);
            if (fieldValue instanceof Map) {
                Map<String, ?> propertyMap = (Map) fieldValue;
                //找出namespace下并且可通过mapperFilter的mapperId
                List<String> list = propertyMap.keySet().stream().filter(o -> o.startsWith(namespace + ".") && !o.replace(namespace + ".", "").contains(".") && mapperIdFilter.test(o)).collect(Collectors.toList());
                list.forEach(propertyMap::remove);
            }
        }
    }

    /**
     * 替换mapper xml的namespace
     */
    private static String changeMapperNamespace(String mapperXmlContent, String namespace) {
        Pattern pattern = Pattern.compile("namespace\\s*=\\s*\"(\\S+)\"");
        Matcher matcher = pattern.matcher(mapperXmlContent);
        if (matcher.find()) {
            String originNamespace = matcher.group(1);

            return mapperXmlContent.replace(originNamespace, namespace);
        }
        throw new ApplicationWarningException("namespace parse error");

    }

    /**
     * 根据低代码路径与环境生成resource标识
     * 格式：LCDP_MAPPER_HOT_RELOAD: + mapper资源路径 + 环境标志
     */
    private static String getResource(String lcdpMapperPath, boolean isPro) {
        return RESOURCE_PREFIX + lcdpMapperPath + (isPro ? "" : NAMESPACE_DEV_SUFFIX);
    }

    /**
     * mapper资源路径转为namespace
     * 格式：xxx.xxxMysqlMapper -> xxx.xxxMapper + 环境标志
     */
    private static String getNamespace(String lcdpMapperPath, boolean isPro) {
        return lcdpMapperPath.replace(DIALECT_MAPPER_SUFFIX, "Mapper") + (isPro ? "" : NAMESPACE_DEV_SUFFIX);
    }

    /**
     * 根据mapper xml内容解析出所有的mapperId，用于筛选出应该删除的mapperId
     */
    private static List<String> parseMapperIdListByXml(String mapperXmlContent, Configuration configuration, String namespace) {
        XPathParser xPathParser = new XPathParser(new ByteArrayInputStream(mapperXmlContent.getBytes(StandardCharsets.UTF_8)), true, configuration.getVariables(), new XMLMapperEntityResolver());
        XNode mapperNode = xPathParser.evalNode("/mapper");
        String mapperNamespace = mapperNode.getStringAttribute("namespace");
        //校验namespace是否一致
        if (!StringUtils.equals(mapperNamespace, namespace)) {
            throw new CheckedException("mapper namespace error");
        }
        //找出select|insert|update|delete|sql|parameterMap|resultMap节点
        List<XNode> statementNodeList = mapperNode.evalNodes("/mapper/select|update|insert|sql|parameterMap|resultMap");

        List<String> sqlList = statementNodeList.stream().filter(node -> StringUtils.equalsIgnoreCase(node.getName(), "sql"))
                .map(node -> node.getStringAttribute("id")).collect(Collectors.toList());

        statementNodeList.stream().forEach(node -> validateInclude(node, configuration, sqlList, namespace));

        //拿出所有节点的mapperId
        return statementNodeList.stream().map(node -> namespace + "." + node.getStringAttribute("id")).collect(Collectors.toList());
    }

    private static void validateInclude(XNode node, Configuration configuration, List<String> sqlList, String namespace) {
        if (!ObjectUtils.isEmpty(node)) {

            for (XNode childen : node.getChildren()) {
                if (StringUtils.equals("include", childen.getName())) {
                    Map<String, XNode> sqlFragments = configuration.getSqlFragments();
                    String refId = childen.getStringAttribute("refid");
                    if (!sqlFragments.containsKey(refId) && !sqlList.contains(refId) && !sqlList.contains(refId.replace(namespace + ".", ""))) {
                        throw new CheckedException(childen.getStringAttribute("refid") + " sql fragment is not exist");
                    }
                } else {
                    validateInclude(childen, configuration, sqlList, namespace);
                }
            }
        }
    }
}
