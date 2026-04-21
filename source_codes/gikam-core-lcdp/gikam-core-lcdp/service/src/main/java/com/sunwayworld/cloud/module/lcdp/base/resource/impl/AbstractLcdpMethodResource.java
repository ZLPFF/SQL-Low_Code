package com.sunwayworld.cloud.module.lcdp.base.resource.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sunwayworld.cloud.module.lcdp.base.resource.LcdpMethodResource;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.utils.ClassUtils;
import com.sunwayworld.framework.utils.ConvertUtils;
import com.sunwayworld.framework.utils.ReflectionUtils;
import com.sunwayworld.framework.utils.StringUtils;

public interface AbstractLcdpMethodResource<S> extends LcdpMethodResource<S> {

    default Object invokeServiceMethod(String methodName, RestJsonWrapperBean wrapper) {
        Object serviceBean = getService();
        if (serviceBean == null) {
            throw new ApplicationRuntimeException("未找到低代码服务实例");
        }
        String actualMethodName = normalizeMethodName(methodName);
        List<Object> rawArgs = extractInvokeArgs(wrapper == null ? null : wrapper.getParamMap().get("args"));
        Class<?> targetClass = ClassUtils.getRawType(serviceBean.getClass());
        for (Method method : targetClass.getMethods()) {
            if (!StringUtils.equals(method.getName(), actualMethodName) || method.getDeclaringClass() == Object.class) {
                continue;
            }
            Object[] invokeArgs = buildInvokeArgs(method, rawArgs, wrapper);
            if (invokeArgs == null) {
                continue;
            }
            return ReflectionUtils.invokeMethod(method, serviceBean, invokeArgs);
        }
        throw new ApplicationRuntimeException("未找到匹配的低代码方法：" + actualMethodName);
    }

    default String normalizeMethodName(String methodName) {
        if (StringUtils.isBlank(methodName)) {
            return methodName;
        }
        while (methodName.startsWith(".")) {
            methodName = methodName.substring(1);
        }
        return methodName;
    }

    @SuppressWarnings("unchecked")
    default List<Object> extractInvokeArgs(Object rawArgs) {
        if (rawArgs == null) {
            return Collections.emptyList();
        }
        if (rawArgs instanceof List) {
            return new ArrayList<>((List<Object>) rawArgs);
        }
        if (rawArgs.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(rawArgs);
            List<Object> arrayArgs = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                arrayArgs.add(java.lang.reflect.Array.get(rawArgs, i));
            }
            return arrayArgs;
        }
        return new ArrayList<>(Collections.singletonList(rawArgs));
    }

    default Object[] buildInvokeArgs(Method method, List<Object> rawArgs, RestJsonWrapperBean wrapper) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            return rawArgs.isEmpty() ? new Object[0] : null;
        }
        if (parameterTypes.length == 1 && RestJsonWrapperBean.class.isAssignableFrom(parameterTypes[0])) {
            return new Object[]{wrapper};
        }
        if (parameterTypes.length != rawArgs.size()) {
            return null;
        }
        Object[] invokeArgs = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Object argument = convertInvokeArg(rawArgs.get(i), parameterTypes[i], wrapper);
            if (argument == InvalidArgument.INSTANCE) {
                return null;
            }
            invokeArgs[i] = argument;
        }
        return invokeArgs;
    }

    default Object convertInvokeArg(Object value, Class<?> targetType, RestJsonWrapperBean wrapper) {
        if (RestJsonWrapperBean.class.isAssignableFrom(targetType)) {
            return wrapper;
        }
        if (value == null) {
            return null;
        }
        if (targetType.isInstance(value)) {
            return value;
        }
        if ((targetType == Map.class || Map.class.isAssignableFrom(targetType)) && value instanceof Map) {
            return value;
        }
        if ((targetType == List.class || List.class.isAssignableFrom(targetType)) && value instanceof List) {
            return value;
        }
        try {
            return ConvertUtils.convert(value, targetType);
        } catch (Exception e) {
            return InvalidArgument.INSTANCE;
        }
    }

    enum InvalidArgument {
        INSTANCE
    }
}
