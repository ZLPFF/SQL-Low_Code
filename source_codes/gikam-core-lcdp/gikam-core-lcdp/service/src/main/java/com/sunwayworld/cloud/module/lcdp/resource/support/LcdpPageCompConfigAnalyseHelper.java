package com.sunwayworld.cloud.module.lcdp.resource.support;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class LcdpPageCompConfigAnalyseHelper {
    private static final List<String> REQUIRED_DATA_TYPE_LIST = Arrays.asList("select", "checkboxgroup", "radiogroup", "combobox");

    /**
     * 对组件的配置项进行整体解析
     * 目前帮助拿到系统编码，未来可在此基础上扩展
     * 目标是按照配置项的key进行分组
     *
     * @param configList
     */
    public static Map<String, List<String>> analyseConfig2Select(List<String> configList) {
        return analyseConfigByCategory(configList);
    }


    public static Map<String, List<String>> analyseConfig2File(List<String> configList) {
        return analyseConfigByCategory(configList);
    }

    private static Map<String, List<String>> analyseConfigByCategory(List<String> configList) {
        List<JSONObject> jsonObjectList = configList.stream().map(JSON::parseObject).collect(Collectors.toList());

        return jsonObjectList.stream()
                .filter(jo -> {
                    String type = jo.getString("type");
                    String category = jo.getString("category");
                    return type != null
                            && REQUIRED_DATA_TYPE_LIST.contains(type.toLowerCase())
                            && category != null
                            && !category.trim().isEmpty();
                })
                .collect(Collectors.groupingBy(jo -> jo.getString("type").toLowerCase(),
                        Collectors.mapping(jo -> jo.getString("category"), Collectors.toList())));
    }
}
