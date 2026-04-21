package com.sunwayworld.cloud.module.lcdp.base.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.message.log.websocket.LcdpScriptLogWebSocket;
import com.sunwayworld.framework.context.ApplicationContextHelper;

public abstract class LcdpBaseUtils {
    private static final Logger logger = LogManager.getLogger(LcdpBaseUtils.class);

    public static void log(Object log) {
        String message = "";
        // 统一处理日期时间格式化
        Object resultJsonObject = JSON.parse(JSONObject.toJSONStringWithDateFormat(log, "yyyy-MM-dd HH:mm:ss"));

        if (resultJsonObject instanceof JSONObject) {
            message = JSONObject.toJSONString(resultJsonObject, true);
        } else if (resultJsonObject instanceof JSONArray) {
            message = JSONArray.toJSONString(resultJsonObject, true);
        } else {
            message = String.valueOf(log);
        }
        LcdpScriptLogWebSocket console = ApplicationContextHelper.getBean(LcdpScriptLogWebSocket.class);
        //向运行日志中推送log
        console.pushInfoLog(message);
        //后台日志
        logger.info(log);
    }
}
