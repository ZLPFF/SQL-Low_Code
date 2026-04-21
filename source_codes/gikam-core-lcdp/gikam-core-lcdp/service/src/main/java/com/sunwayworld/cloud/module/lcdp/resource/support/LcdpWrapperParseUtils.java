package com.sunwayworld.cloud.module.lcdp.resource.support;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.mybatis.MybatisTimeZoneHelper;
import com.sunwayworld.framework.restful.data.RestJsonWrapperBean;
import com.sunwayworld.framework.support.base.service.GenericService;
import com.sunwayworld.framework.utils.ConvertUtils;
import com.sunwayworld.framework.utils.DateTimeUtils;
import com.sunwayworld.framework.utils.JsonUtils;
import com.sunwayworld.framework.utils.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LcdpWrapperParseUtils {

    /**
     * 解析为Map
     *
     * @param wrapper
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseMap(RestJsonWrapperBean wrapper) {
        List<List<String>> bodyList = wrapper.getBodyList();
        if (bodyList.isEmpty()) {
            return null;
        }
        
        List<String> list = bodyList.get(0);
        String objString = list.get(0);

        Map<String, Object> map = JsonUtils.parse(JSONObject.parseObject(objString), Map.class);

        handleZoneTime(map);
        return map;
    }

    public static Map<String, Object> parseMap(RestJsonWrapperBean wrapper, int index) {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = parseList(wrapper, index);
        if (!list.isEmpty()) {
            map = list.get(0);
        }

        handleZoneTime(map);
        return map;
    }

    /**
     * 解析为List
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> parseList(RestJsonWrapperBean wrapper) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<List<String>> bodyList = wrapper.getBodyList();
        if (bodyList.isEmpty()) {
            return mapList;
        }

        List<String> list = bodyList.get(0);
        for (String objString : list) {

            Map<String, Object> map = JsonUtils.parse(JSONObject.parseObject(objString), Map.class);
            handleZoneTime(map);
            mapList.add(map);
        }

        return mapList;
    }

    /**
     * 指定索引解析为List
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> parseList(RestJsonWrapperBean wrapper, int index) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<List<String>> bodyList = wrapper.getBodyList();

        if (bodyList.isEmpty() || bodyList.size() <= index) {
            return mapList;
        }

        List<String> list = bodyList.get(index);
        for (String objString : list) {
            Map<String, Object> map = JsonUtils.parse(JSONObject.parseObject(objString), Map.class);
            handleZoneTime(map);
            mapList.add(map);
        }

        return mapList;
    }

    /**
     * 解析为List<ID>
     * @return
     */
    public static <ID> List<ID> parseIdList(RestJsonWrapperBean wrapper, Class<ID> idType) {
        List<Map<String, Object>> list = parseList(wrapper);

        return list.stream().map(e -> ConvertUtils.convert(e.get("id"), idType)).collect(Collectors.toList());
    }

    /**
     * map的key全部转小写
     *
     * @param map
     * @return
     */
    public static Map<String, Object> lowerCaseKey(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        Map<String, Object> returnMap = new HashMap<>();

        map.forEach((key, value) -> {
            if (Timestamp.class.isInstance(value)) {
                returnMap.put(key.toLowerCase(), value.toString());
            } else {
                returnMap.put(key.toLowerCase(), value);
            }
        });

        return returnMap;
    }

    /**
     * map的key全部转小写
     */
    public static List<Map<String, Object>> lowerCaseKey(List<Map<String, Object>> list) {
        if (list == null) {
            return null;
        }

        List<Map<String, Object>> returnList = new ArrayList<>();

        for (Map<String, Object> map : list) {
            Map<String, Object> returnMap = lowerCaseKey(map);
            returnList.add(returnMap);
        }

        return returnList;
    }

    public static RestJsonWrapperBean toRestJsonWrapper(List<Map<String, Object>> list) {
        List<List<String>> bodyList = new ArrayList<>();
        List<String> jsonList = new ArrayList<>();
        for (Map<String, Object> map : list) {
            String json = JSONObject.toJSONString(map);
            jsonList.add(json);
        }
        bodyList.add(jsonList);
        RestJsonWrapperBean jsonWrapper = new RestJsonWrapperBean(null, bodyList, null);
        return jsonWrapper;
    }

    public static RestJsonWrapperBean toRestJsonWrapper(String requestJson) {
        if (StringUtils.isEmpty(requestJson)) {
            return new RestJsonWrapperBean();
        }
        JSONObject jo = JSONObject.parseObject(requestJson);
        String converterJson = jo.toJSONString();

        // 处理参数
        Map<String, String> paramMap = new HashMap<>();
        String paramStr = jo.getString("p");
        if (!StringUtils.isEmpty(paramStr)) {
            JSONObject paramObject = JSONObject.parseObject(paramStr);

            paramObject.entrySet().stream().forEach(e -> paramMap.put(e.getKey(), ConvertUtils.convert(e.getValue(), String.class)));

        }

        // 处理json格式的业务数据
        List<List<String>> bodyList = new ArrayList<>();
        List<GenericService<?, ?>> bodyServiceList = new ArrayList<>();

        int index = 0;
        while (true) {
            JSONArray array = jo.getJSONArray("b" + ((index > 0) ? index : ""));

            if (array == null || array.isEmpty()) {
                break;
            } else {
                String serviceName = array.getString(0);
                JSONArray contentArray = array.getJSONArray(1);

                if (contentArray != null && !contentArray.isEmpty()) {
                    List<String> contentList = contentArray.stream().map(a -> ((JSONObject) a).toJSONString()).collect(Collectors.toList());

                    bodyList.add(contentList);

                    bodyServiceList.add(RestJsonWrapperBean.resolveBodyService(serviceName));
                }
            }

            index++;
        }

        return new RestJsonWrapperBean(converterJson, paramMap, bodyList, bodyServiceList);
    }

    public static RestJsonWrapperBean toRestJsonWrapper(Map<String, String> paramMap, List<Map<String, Object>> list, List<GenericService<?, ?>> bodyServiceList) {
        List<List<String>> bodyList = new ArrayList<>();
        List<String> jsonList = new ArrayList<>();
        for (Map<String, Object> map : list) {
            String json = JSONObject.toJSONString(map);
            jsonList.add(json);
        }
        bodyList.add(jsonList);
        RestJsonWrapperBean jsonWrapper = new RestJsonWrapperBean(paramMap, bodyList, bodyServiceList);
        return jsonWrapper;
    }


    public static RestJsonWrapperBean toRestJsonWrapper(String requestjson, Map<String, String> paramMap, List<Map<String, Object>> list, List<GenericService<?, ?>> bodyServiceList) {
        List<List<String>> bodyList = new ArrayList<>();
        List<String> jsonList = new ArrayList<>();
        for (Map<String, Object> map : list) {
            String json = JSONObject.toJSONString(map);
            jsonList.add(json);
        }
        bodyList.add(jsonList);
        RestJsonWrapperBean jsonWrapper = new RestJsonWrapperBean(requestjson, paramMap, bodyList, bodyServiceList);
        return jsonWrapper;
    }


    /**
     * 处理时区
     * @param map
     */
    private static void handleZoneTime(Map<String, Object> map) {
        if(MybatisTimeZoneHelper.ZONE_ENABLED) {
            map.forEach((k,v)->{
                if(v != null) {
                    if(DateTimeUtils.isLocalDateTime(v.toString())) {
                        //前端时间字段，转换为服务器时区
                        map.put(k, MybatisTimeZoneHelper.timeZoneTransformToServer(DateTimeUtils.parseLocalDateTime(v.toString())));
                    }
                }
            });
        }
    }
}
