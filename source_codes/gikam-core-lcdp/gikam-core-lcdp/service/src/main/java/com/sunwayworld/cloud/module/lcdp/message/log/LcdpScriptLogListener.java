package com.sunwayworld.cloud.module.lcdp.message.log;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONValidator;
import com.sunwayworld.cloud.module.lcdp.message.log.bean.LcdpScriptLogDTO;
import com.sunwayworld.cloud.module.lcdp.message.log.websocket.LcdpScriptLogWebSocket;
import com.sunwayworld.framework.context.ApplicationContextHelper;

public class LcdpScriptLogListener implements MessageListener {
    // 脚本日志redis订阅主题
    public static final String LCDP_SCRIPT_LOG_TOPIC = "_event@" + ApplicationContextHelper.getEnvironment().getProperty("spring.redis.database") + "_:lcdp-log";
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = new String(message.getBody());
        if (JSONValidator.from(body).validate()){
            LcdpScriptLogDTO scriptLog = JSON.parseObject(body, LcdpScriptLogDTO.class);
            LcdpScriptLogWebSocket webSocket = LcdpScriptLogWebSocket.webSocketSet.get(scriptLog.getUser());

            if (webSocket != null) {
                scriptLog.setUser(null);
                webSocket.sendMessage(JSON.toJSONString(scriptLog));
            }
        }
    }
}
