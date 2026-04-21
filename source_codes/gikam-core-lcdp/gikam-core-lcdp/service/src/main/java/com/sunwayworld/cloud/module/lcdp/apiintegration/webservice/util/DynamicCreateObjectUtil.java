package com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.util;


import java.util.Map;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.Loader;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import com.sunwayworld.framework.data.Pair;
import com.sunwayworld.framework.utils.ObjectUtils;


public class DynamicCreateObjectUtil {

    public static final String DYNAMIC_SOAP_ENTITY_PREFIX = DynamicCreateObjectUtil.class.getPackage().getName();

    public static ClassLoader classLoader;
    // 保证Loader的唯一性,防止加载class报重复性异常
    public static Pair<ClassPool, Loader> pair = null;

    static {
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
            ClassPool classPool = ClassPool.getDefault();
            classPool.insertClassPath(new ClassClassPath(DynamicCreateObjectUtil.class));
            pair = Pair.of(classPool, null);
        } catch (Exception ignore) {

        }
    }

    public static Annotation addSingleAnnotation(ConstPool constpool, String annotationClassName, Map<String, Object> annotationMap) {
        Annotation annotation = new Annotation(annotationClassName, constpool);

        for (Map.Entry<String, Object> fieldValue : annotationMap.entrySet()) {
            if (!ObjectUtils.isEmpty(fieldValue.getValue())) {
                if (fieldValue.getValue() instanceof Enum) {
                    annotation.addMemberValue(fieldValue.getKey(), new EnumMemberValue(constpool));
                } else if (fieldValue.getValue() instanceof Boolean) {
                    annotation.addMemberValue(fieldValue.getKey(), new BooleanMemberValue((Boolean) fieldValue.getValue(), constpool));
                } else if (fieldValue.getValue() instanceof Integer) {
                    annotation.addMemberValue(fieldValue.getKey(), new StringMemberValue((Integer) fieldValue.getValue(), constpool));
                } else {
                    annotation.addMemberValue(fieldValue.getKey(), new StringMemberValue((String) fieldValue.getValue(), constpool));
                }
            }

        }
        return annotation;
    }

}
