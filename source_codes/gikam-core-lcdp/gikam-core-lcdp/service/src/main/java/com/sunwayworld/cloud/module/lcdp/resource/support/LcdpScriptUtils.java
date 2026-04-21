package com.sunwayworld.cloud.module.lcdp.resource.support;

import java.lang.reflect.InvocationTargetException;
import java.sql.Blob;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.base.service.LcdpBaseService;
import com.sunwayworld.cloud.module.lcdp.configparam.service.LcdpConfigParamService;
import com.sunwayworld.cloud.module.lcdp.message.log.LcdpScriptLogConfig;
import com.sunwayworld.cloud.module.lcdp.message.log.websocket.LcdpScriptLogWebSocket;
import com.sunwayworld.cloud.module.lcdp.resource.bean.LcdpResourceBean;
import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.cloud.module.lcdp.support.constant.LcdpConstant;
import com.sunwayworld.framework.cache.redis.RedisHelper;
import com.sunwayworld.framework.constant.Constant;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.data.page.Page;
import com.sunwayworld.framework.exception.UnexpectedException;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.tenant.TenantContext;
import com.sunwayworld.framework.tenant.TenantManager;
import com.sunwayworld.framework.utils.ConvertUtils;
import com.sunwayworld.framework.utils.ExceptionUtils;
import com.sunwayworld.framework.utils.LcdpUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.SpringUtils;
import com.sunwayworld.framework.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.LocalDateTime;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;


public class LcdpScriptUtils {

    private static final Logger logger = LogManager.getLogger(LcdpScriptUtils.class);

    private static final boolean designCenterTenantFlag = ApplicationContextHelper.getEnvironment().getProperty("sunway.design-center-tenant.enabled", Boolean.class, false);


    /**
     * 调用脚本方法
     *
     * @param scriptPath:请求脚本路径
     * @return
     * @throws Throwable * @throws Throwable
     */
    public static String callScriptMethod(String scriptPath, Object... args) {
        int lastDotIndex = scriptPath.lastIndexOf(".");

        //从前端传过来的调用路径中截取脚本路径和调用方法名
        String path = scriptPath.substring(0, lastDotIndex);
        String methodName = scriptPath.substring(lastDotIndex + 1);

        // 根据前端传递的header值判断是否开发环境
        if (LcdpUtils.isDebugRequest()) {
            return devCallScriptMethod(path, methodName, args);
        }

        //正式环境脚本调用
        return proCallScriptMethod(path, methodName, args);
    }

    /**
     * LcdpApi  调用脚本方法，且不做key转小写
     *
     * @param scriptPath
     * @param args
     * @return
     */
    public static String callScriptMethodNoConvert(String scriptPath, Object... args) {
        int lastDotIndex = scriptPath.lastIndexOf(".");

        //从前端传过来的调用路径中截取脚本路径和调用方法名
        String path = scriptPath.substring(0, lastDotIndex);
        String methodName = scriptPath.substring(lastDotIndex + 1);

        Object result = proCallScriptMethodHandle(path, methodName, args);
        return objectToJSONStringNoConvert(result);
    }

    /**
     * 正式环境脚本调用
     */
    public static String proCallScriptMethod(String path, String methodName, Object[] args) {
        Object result = proCallScriptMethodHandle(path, methodName, args);
        return objectToJSONString(result);
    }


    /**
     * 开发环境脚本调用
     */
    public static String devCallScriptMethod(String path, String methodName, Object[] args) {
        // 开发环境下需要向前端推送脚本调用日志
        LcdpScriptLogWebSocket webSocket = ApplicationContextHelper.getBean(LcdpScriptLogWebSocket.class);

        try {
            webSocket.pushInfoLog("脚本调用 ==>" + path + "." + methodName);

            Class<?> clazz = ApplicationContextHelper.getBean(LcdpResourceService.class).getActiveClassByPath(path, true);

            //设置线程上下文数据源名称
            LcdpScriptLogWebSocket.setContextScriptDatasourceName(path + "@" + methodName);

            //开启mybatis日志向运行日志推送
            LcdpScriptLogConfig.enable();

            String beanName = LcdpJavaCodeResolverUtils.getBeanName(clazz);

            synchronized (beanName.intern()) {
                if (!ApplicationContextHelper.getApplicationContext().containsBean(beanName)) {
                    LcdpJavaCodeResolverUtils.registerBean(clazz);

                    // 脚本类对象,表名建立映射
                    if (LcdpBaseService.class.isAssignableFrom(clazz)) {
                        LcdpBaseService scriptService = SpringUtils.getBean(beanName);
                        String tableName = scriptService.getTable();
                        ApplicationContextHelper.setLcdpServiceNameByTable(tableName, beanName);

                        //脚本路径关联表名
                        RedisHelper.put(LcdpConstant.SCRIPT_PATH_TABLE_MAPPING_CACHE, path, tableName);
                    }
                }
            }

            Object result = SpringUtils.invoke(beanName, methodName, args);

            // 统一处理来源数据源时获取数据源字段和类型，兼容项目上自定义数据源方法
            dealSourceDataSourceFields(result, args);

            return objectToJSONString(result);
        } catch (Throwable e) {
            Throwable cause = e;

            //执行目标异常则抛出内部原因
            InvocationTargetException invocationTargetException = ExceptionUtils.getCause(e, InvocationTargetException.class);

            while (!ObjectUtils.isEmpty(invocationTargetException)) {
                cause = invocationTargetException.getTargetException();
                invocationTargetException = ExceptionUtils.getCause(cause, InvocationTargetException.class);
            }

            validateTipException(cause);


            String exceptionLog = I18nHelper.getMessage("LCDP.RUN_LOGS.SCRIPT_INVOKE_ERROR", path, methodName, ObjectUtils.getStackTrace(cause));
            webSocket.pushErrorLog(exceptionLog);

            throw new UnexpectedException(exceptionLog);
        } finally {
            LcdpScriptLogConfig.disable();
        }

    }

    /**
     * 通过类名和@LcdpPath注解中的路径前缀，获取spring的bean
     */
    public static Object getBean(String path) {

        String originalPath = path;

        // 兼容低版本写法，直接使用ServiceName来调用
        if (!path.contains(".")) {
            path = ApplicationContextHelper.getBean(LcdpResourceService.class).getPathByClassName(path);
        }

        if (StringUtils.isEmpty(path)) {
            throw new ApplicationRuntimeException("LCDP.EXCEPTION.PATH_NOT_FOUND", originalPath);
        }

        Class<?> clazz = getActiveClassByPath(path);

        String beanName = LcdpJavaCodeResolverUtils.getBeanName(clazz);

        if (!SpringUtils.isBeanExists(path)) {
            SpringUtils.registerBean(beanName, clazz);
        }

        return SpringUtils.getBean(beanName);
    }

    public static <T> T getBean(Class<T> interfaceClass) {
        ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();

        try {
            Map<String, T> map = ((ConfigurableApplicationContext) applicationContext).getBeanFactory().getBeansOfType(interfaceClass);

            if (map == null || map.isEmpty()) {
                return null;
            }

            if (LcdpUtils.isDebugRequest()) { // DEBUG模式
                int maxVersion = 0;
                T maxVersionBean = null;

                for (Map.Entry<String, T> entry : map.entrySet()) {
                    if (LcdpJavaCodeResolverUtils.isDevName(entry.getKey())) {
                        int version = LcdpJavaCodeResolverUtils.getDevVersion(entry.getKey());

                        if (version > maxVersion) {
                            maxVersion = version;

                            maxVersionBean = entry.getValue();
                        }
                    }
                }

                return maxVersionBean;
            } else {
                int maxVersion = 0;
                T maxVersionBean = null;

                for (Map.Entry<String, T> entry : map.entrySet()) {
                    if (LcdpJavaCodeResolverUtils.isProName(entry.getKey())) {
                        int version = LcdpJavaCodeResolverUtils.getProVersion(entry.getKey());

                        if (version > maxVersion) {
                            maxVersion = version;

                            maxVersionBean = entry.getValue();
                        }
                    }
                }

                return maxVersionBean;
            }
        } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);

            return null;
        }
    }

    public static Object getBean(String beanName, String path) {
        return getBean(beanName);
    }


    /**
     * 根据路径获取当前生效的类
     */
    public static Class<?> getActiveClassByPath(String path) {
        return ApplicationContextHelper.getBean(LcdpResourceService.class).getActiveClassByPath(path, LcdpUtils.isDebugRequest());
    }


    public static boolean validateCurrentDBMybatisMapper() {
        String environmentGlobConfig = ApplicationContextHelper.getBean(LcdpConfigParamService.class).getCurrentDBMybatisMapperParam();
        return !StringUtils.isEmpty(environmentGlobConfig) && StringUtils.equals(environmentGlobConfig, Constant.YES);
    }

    public static boolean requiredMapperOnly() {
        String environmentGlobConfig = ApplicationContextHelper.getBean(LcdpConfigParamService.class).getCurrentDBMybatisMapperParam();
        return !StringUtils.isEmpty(environmentGlobConfig) && StringUtils.equals(environmentGlobConfig, Constant.YES);
    }

    //---------------------------------------------------------------
    // 私有方法
    //---------------------------------------------------------------
    private static String objectToJSONString(Object result) {
        if (result == null) {
            return "";
        }
        // 过滤Blob字段
        filterBlobField(result);

        // 统一处理日期时间格式化
        Object resultJsonObject = JSON.parse(JSONObject.toJSONStringWithDateFormat(result, "yyyy-MM-dd HH:mm:ss"));

        if (resultJsonObject instanceof JSONObject) {
            return JSONObject.toJSONString(keyToLowerCase((JSONObject) JSONObject.toJSON(resultJsonObject)));
        } else if (resultJsonObject instanceof JSONArray) {
            return JSONObject.toJSONString(keyToLowerCase((JSONArray) JSONObject.toJSON(resultJsonObject)));
        } else {
            return result.toString();
        }

    }

    private static String objectToJSONStringNoConvert(Object result) {
        if (result == null) {
            return "";
        }
        // 过滤Blob字段
        filterBlobField(result);

        // 统一处理日期时间格式化
        Object resultJsonObject = JSON.parse(JSONObject.toJSONStringWithDateFormat(result, "yyyy-MM-dd HH:mm:ss"));

        if (resultJsonObject instanceof JSONObject || resultJsonObject instanceof JSONArray) {
            return JSONObject.toJSONString(resultJsonObject);
        } else {
            return result.toString();
        }

    }

    /**
     * 处理返回值，过滤blob字段
     **/
    @SuppressWarnings({"unchecked"})
    private static void filterBlobField(Object javaObject) {

        if (javaObject instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) javaObject;

            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof Blob) {
                    entry.setValue(null);
                } else if (entry.getValue() instanceof byte[]) {
                    String text = ConvertUtils.convert(entry.getValue(), String.class);
                    entry.setValue(text);
                }
            }
            return;
        }

        if (javaObject instanceof Collection) {
            Collection<Object> collection = (Collection<Object>) javaObject;

            for (Object item : collection) {
                filterBlobField(item);
            }
            return;
        }

        if (javaObject instanceof Page) {
            Page<?> page = (Page<?>) javaObject;
            filterBlobField(page.getRows());
        }
    }

    private static JSONObject keyToLowerCase(JSONObject o1) {
        JSONObject o2 = new JSONObject();
        Set<String> keys = o1.keySet();
        for (String key : keys) {
            Object object = o1.get(key);
            if (object == null) {
                continue;
            }

            //根据value值的类型,调用不同的方法
            if (object.getClass().toString().endsWith("JSONObject")) {
                o2.put(key.toLowerCase(), keyToLowerCase((JSONObject) object));
            } else if (object.getClass().toString().endsWith("JSONArray")) {
                JSONArray arr = (JSONArray) object;
                o2.put(key.toLowerCase(), keyToLowerCase(arr));
            } else {
                if (key.equalsIgnoreCase("id") && object.toString().length() > 50) {
                    object = object.toString();
                }
                o2.put(key.toLowerCase(), object);
            }
        }
        return o2;
    }

    private static JSONArray keyToLowerCase(JSONArray o1) {
        JSONArray o2 = new JSONArray();

        for (int i = 0; i < o1.size(); i++) {
            Object item = o1.get(i);

            if (item == null) {
                continue;
            }

            if (item instanceof JSONObject) {
                o2.add(keyToLowerCase((JSONObject) item));
            } else if (item instanceof JSONArray) {
                o2.add(keyToLowerCase((JSONArray) item));
            } else {
                o2.add(item);
            }
        }

        return o2;
    }

    private static void validateTipException(Throwable cause) {

        logger.error(cause.getMessage(), cause);

        if (StringUtils.startsWith(cause.getClass().getName(), "com.sunwayworld")) {
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }


    }

    private static void dealSourceDataSourceFields(Object result, Object[] args) {

        if (args.length > 0 && args[0] instanceof RestJsonWrapperBean && (result instanceof Map || result instanceof Page)) {
            RestJsonWrapperBean wrapper = (RestJsonWrapperBean) args[0];

            //选择数据源查询
            if (StringUtils.equals(wrapper.getParamValue("queryFieldListFlag"), "1")) {

                if (result instanceof Map) {
                    Map<String, Object> resultMap = (Map<String, Object>) result;

                    if (resultMap.get("columnTypeMap") == null) {

                        Map<String, Object> columnTypeMap = new HashMap<>();

                        for (String key : resultMap.keySet()) {
                            columnTypeMap.put(key, getFrontEndRequiredColumnTypeByValue(resultMap.get(key)));
                        }


                        resultMap.put("columnTypeMap", columnTypeMap);

                    }

                } else {
                    Page page = (Page) result;

                    Map columnTypeMap = page.getColumnTypeMap();

                    if (columnTypeMap.isEmpty() && !page.getRows().isEmpty() && page.getRows().get(0) instanceof Map) {
                        Map rowMap = (Map) page.getRows().get(0);

                        for (Object key : rowMap.keySet()) {
                            columnTypeMap.put(key.toString(), getFrontEndRequiredColumnTypeByValue(rowMap.get(key)));
                        }


                        page.setColumnTypeMap(columnTypeMap);
                    }


                }


            }

        }


    }

    private static String getFrontEndRequiredColumnTypeByValue(Object o) {

        if (o instanceof LocalDateTime) {
            return "dateTime";
        } else if (o instanceof LocalDate) {
            return "date";
        } else if (o instanceof Number) {
            return "number";
        } else if (o instanceof Integer) {
            return "number";
        } else if (o instanceof Double) {
            return "number";
        } else if (o instanceof Long) {
            return "number";
        } else {
            return "string";
        }
    }

    private static Object proCallScriptMethodHandle(String path, String methodName, Object[] args) {
        LcdpResourceBean resource = null;
        if (designCenterTenantFlag) {
            //缓存中获取脚本资源
            resource = ApplicationContextHelper.getBean(TenantManager.class).call("lcdpResource", "LCDP_DESIGN_TENANT",
                    () -> {
                        return ApplicationContextHelper.getBean(LcdpResourceService.class).getLatestActivatedResourceByPath(path);
                    }
            );
        } else {
            resource = ApplicationContextHelper.getBean(LcdpResourceService.class).getLatestActivatedResourceByPath(path);
        }


        if (resource == null
                || StringUtils.isEmpty(resource.getClassName())) {
            throw new ApplicationRuntimeException("LCDP.EXCEPTION.PATH_NOT_FOUND", path);
        }

        //#60359052 本地服务器混合开发，如果没有，就直接注册
        String beanName = LcdpJavaCodeResolverUtils.getBeanName(resource);
        synchronized (beanName.intern()) {
            if (!ApplicationContextHelper.getApplicationContext().containsBean(beanName)) {
                Class<?> clazz = LcdpJavaCodeResolverUtils.loadSourceCode(resource);
                LcdpJavaCodeResolverUtils.registerBean(clazz);

                //脚本类对象,表名建立映射
                if (LcdpBaseService.class.isAssignableFrom(clazz)) {
                    LcdpBaseService scriptService = SpringUtils.getBean(beanName);
                    String tableName = scriptService.getTable();
                    ApplicationContextHelper.setLcdpServiceNameByTable(tableName, beanName);
                    //脚本路径关联表名
                    RedisHelper.put(LcdpConstant.SCRIPT_PATH_TABLE_MAPPING_CACHE, path, tableName);
                }
            }
        }
        try {
            Object result = null;
            if (designCenterTenantFlag) {
                // 切换数据源
                AtomicReference<Object> resultObj = new AtomicReference<>(null);
                TenantManager tenantManager = ApplicationContextHelper.getBean(TenantManager.class);
                tenantManager.tenantCall("LIMS_INVOKE_LCDP", TenantContext.getTenant(), () -> {
                    resultObj.set(SpringUtils.invoke(beanName, methodName, args));
                });
                result = resultObj.get();
            } else {
                result = SpringUtils.invoke(beanName, methodName, args);
            }
            return result;
        } catch (Exception e) {

            logger.error(e.getMessage(),e);

            Throwable cause = e;

            InvocationTargetException invocationTargetException = ExceptionUtils.getCause(e, InvocationTargetException.class);
            //执行目标异常则抛出内部原因
            while (!ObjectUtils.isEmpty(invocationTargetException)) {
                cause = invocationTargetException.getTargetException();
                invocationTargetException = ExceptionUtils.getCause(cause, InvocationTargetException.class);
            }
            validateTipException(cause);

            throw new RuntimeException(I18nHelper.getMessage("LCDP.RUN_LOGS.SCRIPT_INVOKE_ERROR", path, methodName), cause);
        }
    }


}