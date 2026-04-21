package com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.util;

import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.SignatureAttribute;
import javassist.util.proxy.DefinePackageHelper;

import com.alibaba.fastjson.JSONValidator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.sunwayworld.framework.data.Pair;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.utils.StringUtils;

/**
 * @author wangda@sunwayworld.com
 * @date 2022/5/20 8:54
 * @description json 转soap请求参数,响应参数实体类
 */
public class JsonToSoapEntityUtil {

    public static void fromJson(String json, String classPackage) {
        JsonElement root = JsonParser.parseString(json);
        generateEntityClass(root, classPackage);
    }

    public static void generateEntityClass(JsonElement root, String classPackage) {
        int lastIndexDot = classPackage.lastIndexOf(".");
        String packageName = classPackage.substring(0, lastIndexDot);
        String className = classPackage.substring(lastIndexDot + 1);
        try {
            generateClass(packageName, className, root);
        } catch (Exception e) {
            throw new ApplicationRuntimeException(e);
        }
    }

    private static Pair<Boolean, Class<?>> generateClass(String packageName, String className, JsonElement jsonElement) throws Exception {
        Class<?> ctClass = null;
        boolean isArray = false;
        if (jsonElement.isJsonNull()) {
            ctClass = Object.class;
        } else if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
            ctClass = getClassForPrimitive(jsonPrimitive);
        } else if (jsonElement.isJsonArray()) {
            JsonArray array = jsonElement.getAsJsonArray();
            ctClass = getClassForArray(packageName, className, array);
            isArray = true;
        } else if (jsonElement.isJsonObject()) {
            JsonObject jsonObj = jsonElement.getAsJsonObject();
            ctClass = getClassForObject(packageName, className, jsonObj);
        }
        if (ctClass != null) {
            return Pair.of(isArray, ctClass);
        }
        throw new ApplicationRuntimeException("LCDP.MODULE.API_INTEGRATION.TIP.SOAP_ENTITY_JSON_NOT_SUPPORT");
    }

    private static Class<?> getClassForObject(String packageName, String className, JsonObject jsonObj) throws Exception {
        Map<String, Pair<Boolean, Class<?>>> fields = new LinkedHashMap<>();

        for (Map.Entry<String, JsonElement> element : jsonObj.entrySet()) {
            String fieldName = element.getKey();
            String fieldUppercase = getFirstUppercase(fieldName);
            Pair<Boolean, Class<?>> elementClass = generateClass(packageName, className + fieldUppercase, element.getValue());
            fields.put(fieldName, elementClass);
        }
        String classPackage = packageName + "." + className;
        return generatePojo(classPackage, fields);
    }

    private static Class<?> getClassForArray(String packageName, String className, JsonArray array) throws Exception {
        Class<?> ctClass = Object.class;
        if (array.size() > 0) {
            String elementName = className;
            if (className.endsWith("ies")) {
                elementName = elementName.substring(0, elementName.length() - 3) + "y";
            } else if (elementName.endsWith("s")) {
                elementName = elementName.substring(0, elementName.length() - 1);
            } else if (elementName.endsWith("List")) {
                elementName = elementName.substring(0, elementName.length() - 3);
            }
            ctClass = generateClass(packageName, elementName, array.get(0)).getSecond();
        }
        String narrowName = ctClass.getSimpleName();
        Class<?> boxedClass = null;
        if (narrowName.equalsIgnoreCase("int")) {
            boxedClass = Integer.class;
        } else if (narrowName.equalsIgnoreCase("long")) {
            boxedClass = Long.class;
        } else if (narrowName.equalsIgnoreCase("double")) {
            boxedClass = Double.class;
        } else if (narrowName.equalsIgnoreCase("string")) {
            boxedClass = String.class;
        }
        if (boxedClass != null) {
            return boxedClass;
        }
        return ctClass;
    }

    public static Class<?> generatePojo(String className, Map<String, Pair<Boolean, Class<?>>> fields) throws Exception {
        ClassPool pool = DynamicCreateObjectUtil.pair.getFirst();
        try {
            CtClass ctClass = null;
            try {
                ctClass = pool.getCtClass(className);
                // 停止class精简
                ctClass.stopPruning(Boolean.TRUE);
                //解冻
                if (ctClass.isFrozen()) {
                    ctClass.defrost();
                }
                modifyEntityClass(ctClass, fields);
                return ctClass.toClass(DynamicCreateObjectUtil.classLoader, JsonToSoapEntityUtil.class.getProtectionDomain());
                // 目前没找到好得方法去重新加载class。。回退服务或者升级服务生成新得class是一种解决办法
            } catch (NotFoundException e) {
                ctClass = pool.makeClass(className);
                DefinePackageHelper.definePackage(className.substring(0, className.lastIndexOf(".")), DynamicCreateObjectUtil.classLoader);
                ctClass.setInterfaces(new CtClass[]{ClassPool.getDefault().makeInterface("java.io.Serializable")});
                modifyEntityClass(ctClass, fields);
                return ctClass.toClass(DynamicCreateObjectUtil.classLoader, JsonToSoapEntityUtil.class.getProtectionDomain());
            } catch (CannotCompileException ec) {
                throw ec;
            }

        } catch (Exception e) {
            throw e;
        }
    }

    private static void modifyEntityClass(CtClass ctClass, Map<String, Pair<Boolean, Class<?>>> fields) throws CannotCompileException, NotFoundException {
        for (Map.Entry<String, Pair<Boolean, Class<?>>> field : fields.entrySet()) {
            Pair<Boolean, Class<?>> pair = field.getValue();
            CtField ctField = null;
            Class<?> fieldClass = pair.getSecond();
            try {
                ctField = ctClass.getDeclaredField(field.getKey());
                CtMethod getterMethod = ctClass.getDeclaredMethod(getGetterName(field.getKey()));
                CtMethod setterMethod = ctClass.getDeclaredMethod(getSetterName(field.getKey()));
                ctClass.removeField(ctField);
                ctClass.removeMethod(getterMethod);
                ctClass.removeMethod(setterMethod);
            } catch (Exception e) {

            }
            if (pair.getFirst()) {
                //array
                Class<?> aClass = fieldClass;
                ctField = new CtField(DynamicCreateObjectUtil.pair.getFirst().get(Array.newInstance(aClass, 0).getClass().getName()),
                        field.getKey(), ctClass);
                ctField.setGenericSignature(new SignatureAttribute.ArrayType(1,
                        new SignatureAttribute.ClassType(fieldClass.getName())).encode());
            } else {
                ctField = new CtField(DynamicCreateObjectUtil.pair.getFirst().get(fieldClass.getName()), field.getKey(), ctClass);
            }
            ctClass.addField(ctField);
            ctClass.addMethod(CtNewMethod.setter(getSetterName(field.getKey()), ctField));
            ctClass.addMethod(CtNewMethod.getter(getGetterName(field.getKey()), ctField));
        }
    }

    private static String getSetterName(String fieldName) {
        StringBuilder name = new StringBuilder();
        name.append("set");
        char[] chars = fieldName.toCharArray();
        if (Character.isLowerCase(chars[0])) {
            chars[0] = Character.toUpperCase(chars[0]);
        }
        name.append(chars);
        return name.toString();
    }

    private static String getGetterName(String fieldName) {
        StringBuilder name = new StringBuilder();
        name.append("get");
        char[] chars = fieldName.toCharArray();
        if (Character.isLowerCase(chars[0])) {
            chars[0] = Character.toUpperCase(chars[0]);
        }
        name.append(chars);
        return name.toString();

    }

    public static String getFirstUppercase(String word) {
        String firstLetterToUpperCase = word.substring(0, 1).toUpperCase();
        if (word.length() > 1) {
            firstLetterToUpperCase += word.substring(1, word.length());
        }
        return firstLetterToUpperCase;
    }

    private static Class<?> getClassForPrimitive(JsonPrimitive jsonPrimitive) throws Exception {
        Class<?> primitiveClass = null;
        if (jsonPrimitive.isNumber()) {
            Number number = jsonPrimitive.getAsNumber();
            double doubleValue = number.doubleValue();
            if (doubleValue != Math.round(doubleValue)) {
                primitiveClass = Double.class;
            } else {
                primitiveClass = Long.class;
            }
        } else if (jsonPrimitive.isBoolean()) {
            primitiveClass = Boolean.class;
        } else {
            primitiveClass = String.class;
        }
        return primitiveClass;
    }

    public static boolean isJson(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        return JSONValidator.from(str).validate();
    }
}
