package com.sunwayworld.cloud.module.lcdp.resource.support;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.sunwayworld.cloud.module.lcdp.resource.service.LcdpResourceService;
import com.sunwayworld.framework.exception.checked.CheckedException;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.utils.ArrayUtils;
import com.sunwayworld.framework.utils.ClassUtils;
import com.sunwayworld.framework.utils.ReflectionUtils;
import com.sunwayworld.framework.utils.SpringUtils;
import com.sunwayworld.framework.utils.StringUtils;

/**
 * 用于低代码平台开发出来的类调用静态成员变量或静态方法的工具类
 *
 * @author liuxai@sunwayworld.com 2023年8月2日
 */
public class UtilsCaller {
    private final String path;
    private final String fieldOrMethodName;

    private UtilsCaller(String path, String fieldOrMethodName) {
        // 向下兼容，path里如果没有.，说明是类名
        if (StringUtils.contains(path, ".")) {
            this.path = path;
        } else {
            this.path = SpringUtils.getBean(LcdpResourceService.class).getPathByClassName(path);
        }
        this.fieldOrMethodName = fieldOrMethodName;
    }

    public static UtilsCaller of(String utilName, String fieldOrMethodName) {
        return new UtilsCaller(utilName, fieldOrMethodName);
    }

    @SuppressWarnings("unchecked")
    public <T> T call(Object... args) {
        Class<?> clazz = LcdpScriptUtils.getActiveClassByPath(path);
        if (args.length == 0) {
            try {
                return (T) ReflectionUtils.getUniqueMethodByName(clazz, fieldOrMethodName).invoke(clazz);
            } catch (Exception e) {
                throw new CheckedException(e);
            }
        } else {
            List<ReflectionUtils.MethodWrapper> methodWrapperList = ReflectionUtils.getMethodWrapperListByName(clazz, fieldOrMethodName);

            if (methodWrapperList.isEmpty()) {
                throw new ApplicationRuntimeException("GIKAM.EXCEPTION.REFLECTION.NO_METHOD_FOR_NAME_AND_PARAMS",
                        ClassUtils.getRawType(clazz).toString(),
                        fieldOrMethodName, getParamSignature(args));
            }

            if (methodWrapperList.size() == 1) {
                ReflectionUtils.MethodWrapper methodWrapper = methodWrapperList.get(0);

                try {
                    Method method = methodWrapper.getMethod();
                    if (ArrayUtils.isEmpty(methodWrapper.getParamTypes())) {
                        return (T) method.invoke(null);
                    }

                    // 检查是否为可变参数方法并调整参数
                    Object[] adjustedArgs = adjustArgumentsForVarArgs(method, args);

                    return (T) method.invoke(null, adjustedArgs);
                } catch (Exception e) {
                    throw new CheckedException(e);
                }

            }

            //继承产品原型Service时有很多重名的方法，只是方法参数不同，如selectPagination方法，调用时需根据方法参数查找对应方法
            for (ReflectionUtils.MethodWrapper methodWrapper : methodWrapperList) {
                Class<?>[] paramTypes = methodWrapper.getMethod().getParameterTypes();

                //当最后一个参数是可变参数时，参数数量和参数类型数量不一致
                if (paramTypes.length != args.length && !paramTypes[paramTypes.length - 1].isArray()) {
                    continue;
                }

                boolean flag = true;
                for (int i = 0; i < paramTypes.length; i++) {
                    Class<?> type = paramTypes[i];
                    // 处理最后一个参数是可变参数，没传递参数问题
                    if (i >= args.length) {
                        List<Object> argList = Arrays.stream(args).collect(Collectors.toList());
                        argList.add(ArrayUtils.emptyArray(type.getComponentType()));

                        args = argList.toArray();
                        break;
                    }

                    Object param = args[i];
                    //最后一个参数是可变参数
                    if (i == paramTypes.length - 1 && type.isArray()) {
                        Class<?> genericClass = type.getComponentType();
                        List<?> variableArgList = Arrays.stream(args).skip(i).collect(Collectors.toList());

                        if (variableArgList.stream().allMatch(e -> genericClass.isAssignableFrom(e.getClass()))) {
                            args[i] = variableArgList.toArray(ArrayUtils.emptyArray(genericClass));
                        } else {
                            flag = false;
                        }

                        break;
                    }

                    if (param != null) {
                        Class<?> paramType = param.getClass();
                        // 处理基本类型与包装类型的双向匹配
                        if (!isAssignable(type, paramType)) {
                            flag = false;
                            break;
                        }
                    }
                }


                if (flag) {
                    Method method = methodWrapper.getMethod();
                    try {
                        return (T) method.invoke(null, args);
                    } catch (Exception e) {
                        throw new CheckedException(e);
                    }
                }
            }

            throw new ApplicationRuntimeException("GIKAM.EXCEPTION.REFLECTION.DUPLICATED_METHOD_FOR_NAME_AND_PARAMS",
                    ClassUtils.getRawType(clazz).toString(),
                    fieldOrMethodName, getParamSignature(args));
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getConstant() {
        try {
            Class<?> clazz = LcdpScriptUtils.getActiveClassByPath(path);

            Field field = ReflectionUtils.findField(clazz, fieldOrMethodName);

            if (!Modifier.isPublic(field.getModifiers())
                    || !Modifier.isStatic(field.getModifiers())) {
                throw new IllegalAccessException();
            }

            return (T) ReflectionUtils.getFieldValue(null, field);
        } catch (Exception e) {
            throw new CheckedException(e);
        }
    }

    //------------------------------------------------------------------------------------------
    // 私有方法
    //------------------------------------------------------------------------------------------
    /**
     * 调整参数以兼容可变参数方法
     */
    private Object[] adjustArgumentsForVarArgs(Method method, Object[] originalArgs) {
        Class<?>[] paramTypes = method.getParameterTypes();

        // 如果不是可变参数方法，直接返回原参数
        if (!method.isVarArgs() || paramTypes.length == 0 ||
                originalArgs.length <= paramTypes.length - 1) {
            return originalArgs;
        }

        // 可变参数位置
        int varArgsIndex = paramTypes.length - 1;
        Class<?> varArgComponentType = paramTypes[varArgsIndex].getComponentType();

        // 创建新的参数数组
        Object[] newArgs = new Object[paramTypes.length];

        // 复制固定参数
        for (int i = 0; i < varArgsIndex; i++) {
            newArgs[i] = originalArgs[i];
        }

        // 处理可变参数部分
        int varArgsLength = originalArgs.length - varArgsIndex;
        Object varArgsArray = Array.newInstance(varArgComponentType, varArgsLength);

        for (int i = 0; i < varArgsLength; i++) {
            Array.set(varArgsArray, i, originalArgs[varArgsIndex + i]);
        }

        newArgs[varArgsIndex] = varArgsArray;

        return newArgs;
    }


    private static String getParamSignature(Object... params) {
        if (ArrayUtils.isEmpty(params)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];

            if (i > 0) {
                sb.append(", ");
            }

            if (param == null) {
                sb.append("NIL");
            } else {
                sb.append(ClassUtils.getRawType(param.getClass()).getSimpleName());
            }
        }

        return sb.toString();
    }

    /**
     * 检查源类型是否可赋值给目标类型（支持基本类型与包装类型的双向转换）
     */
    private static boolean isAssignable(Class<?> targetType, Class<?> sourceType) {
        // 直接可赋值的情况
        if (targetType.isAssignableFrom(sourceType)) {
            return true;
        }

        // 基本类型与包装类型的双向映射
        if (targetType.isPrimitive()) {
            return isPrimitiveWrapperMatch(targetType, sourceType);
        } else if (sourceType.isPrimitive()) {
            return isPrimitiveWrapperMatch(getPrimitiveType(sourceType), targetType);
        }

        return false;
    }

    /**
     * 检查基本类型与其包装类型是否匹配
     */
    private static boolean isPrimitiveWrapperMatch(Class<?> primitiveType, Class<?> wrapperType) {
        if (primitiveType == int.class && wrapperType == Integer.class) return true;
        if (primitiveType == long.class && wrapperType == Long.class) return true;
        if (primitiveType == double.class && wrapperType == Double.class) return true;
        if (primitiveType == float.class && wrapperType == Float.class) return true;
        if (primitiveType == boolean.class && wrapperType == Boolean.class) return true;
        if (primitiveType == char.class && wrapperType == Character.class) return true;
        if (primitiveType == byte.class && wrapperType == Byte.class) return true;
        if (primitiveType == short.class && wrapperType == Short.class) return true;
        return primitiveType == void.class && wrapperType == Void.class;
    }

    /**
     * 获取包装类型对应的基本类型
     */
    private static Class<?> getPrimitiveType(Class<?> wrapperType) {
        if (wrapperType == Integer.class) return int.class;
        if (wrapperType == Long.class) return long.class;
        if (wrapperType == Double.class) return double.class;
        if (wrapperType == Float.class) return float.class;
        if (wrapperType == Boolean.class) return boolean.class;
        if (wrapperType == Character.class) return char.class;
        if (wrapperType == Byte.class) return byte.class;
        if (wrapperType == Short.class) return short.class;
        if (wrapperType == Void.class) return void.class;
        return wrapperType;
    }

}
