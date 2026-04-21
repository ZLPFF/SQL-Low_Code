package com.sunwayworld.cloud.module.lcdp.message.log.websocket;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.cloud.module.lcdp.message.log.LcdpScriptLogListener;
import com.sunwayworld.cloud.module.lcdp.message.log.bean.LcdpScriptLogDTO;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.context.concurrent.GikamConcurrentLocker;
import com.sunwayworld.framework.i18n.I18nHelper;
import com.sunwayworld.framework.security.authentication.AuthenticationHelper;
import com.sunwayworld.framework.utils.StringUtils;

@Controller
@ServerEndpoint(value = "/ws/script-log-websocket/{userToken}")
public class LcdpScriptLogWebSocket {
    private static final Logger log = LoggerFactory.getLogger(LcdpScriptLogWebSocket.class);
    
    // concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象
    public static ConcurrentHashMap<String, LcdpScriptLogWebSocket> webSocketSet = new ConcurrentHashMap<String, LcdpScriptLogWebSocket>();
    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session webSocketSession;
    // 当前WebSocket的用户token
    private String userToken;
    //当前线程脚本数据源名称
    private static final ThreadLocal<String> CONTEXT_SCRIPT_DATASOURCE_NAME_HOLDER = new ThreadLocal<>();

    private static StringRedisTemplate redisTemplate = ApplicationContextHelper.getBean(StringRedisTemplate.class); // redis模板

    /**
     * 连接建立成功后调用的方法
     * <p>
     * session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(@PathParam(value = "userToken") String param, Session webSocketSession, EndpointConfig config) {
        GikamConcurrentLocker.blockRun("LCDP_SCRIPT_LOG_WEBSOCKET", () -> {
            if (webSocketSet.containsKey(param)) {
                Session oldSession = webSocketSet.get(param).webSocketSession;
                if (webSocketSet.get(param).webSocketSession.isOpen()) {
                    try {
                        webSocketSet.get(param).sendMessage(JSONObject.toJSONString(LcdpScriptLogDTO.ofOffline("WARN", I18nHelper.getMessage("LCDP.RUN_LOGS.REPEATED_CONNECTION"))));
                        oldSession.close();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
            userToken = param;
            this.webSocketSession = webSocketSession;
            webSocketSet.put(param, this);
        });
    }

    /**
     * 连接关闭后调用的方法
     */
    @OnClose
    public void onClose() {
        GikamConcurrentLocker.blockRun("LCDP_SCRIPT_LOG_WEBSOCKET", () -> {
            if (!StringUtils.isEmpty(userToken)) {
                webSocketSet.remove(userToken);
            }
        });
    }

    /**
     * 收到消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        //心跳监测
        if ("ping".equals(message)) {
            sendMessage("pong");
        }
    }

    /**
     * 推送调试日志
     *
     * @param
     */
    public void pushLog(String type, String log) {
        String userToken = AuthenticationHelper.getLoginUserToken();

        String scriptName = StringUtils.isEmpty(CONTEXT_SCRIPT_DATASOURCE_NAME_HOLDER.get()) ? "" : CONTEXT_SCRIPT_DATASOURCE_NAME_HOLDER.get() + " ";
        LcdpScriptLogDTO scriptLog = LcdpScriptLogDTO.of(type, scriptName + log);

        if (!StringUtils.isEmpty(userToken) && webSocketSet.get(userToken) != null) {

            GikamConcurrentLocker.blockRun("LCDP_SCRIPT_LOG_WEBSOCKET_PUSH" + userToken, () -> {
                webSocketSet.get(userToken).sendMessage(JSONObject.toJSONString(scriptLog));
            });
        }else if(!StringUtils.isEmpty(userToken) && webSocketSet.get(userToken) == null) { //集群模式
            scriptLog.setUser(userToken);
            GikamConcurrentLocker.blockRun("LCDP_SCRIPT_LOG_WEBSOCKET_PUSH" + userToken, () -> {
                redisTemplate.convertAndSend(LcdpScriptLogListener.LCDP_SCRIPT_LOG_TOPIC, JSON.toJSONString(scriptLog));
            });
        }
    }

    public void sendMessage(String message) {

        try {
            synchronized (this.webSocketSession) {
                this.webSocketSession.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    public void pushErrorLog(String log) {
        pushLog("ERROR", log);
    }

    public void pushInfoLog(String log) {
        pushLog("INFO", log);
    }

    /**
     * 发生错误时调用
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) throws IOException {
        log.error(error.getMessage(), error);
        if (session.isOpen()) {
            session.close();
        }
    }

    public static void setContextScriptDatasourceName(String name) {
        CONTEXT_SCRIPT_DATASOURCE_NAME_HOLDER.set(name);
    }

}
