package com.sunwayworld.cloud.module.lcdp.message.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import com.sunwayworld.cloud.module.lcdp.message.log.websocket.LcdpScriptLogWebSocket;
import com.sunwayworld.cloud.module.lcdp.resource.support.LcdpMapperUtils;
import com.sunwayworld.framework.context.ApplicationContextHelper;
import com.sunwayworld.framework.mybatis.log.MybatisLogger;
import com.sunwayworld.framework.utils.ObjectUtils;

/**
 * 将记录的mybatis日志推送到脚本日志websocket
 */
@Intercepts(value = {
        @Signature(type = Executor.class,
                method = "update",
                args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class LcdpScriptLogMybatisInterceptor implements Interceptor {
    private static final ThreadLocal<List<String>> MYBATIS_MESSAGE_HOLDER = new ThreadLocal<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 未开启时无操作
        if (!isLogEnabled()) {
            return invocation.proceed();
        }

        //获取mapperId
        Object[] args = invocation.getArgs();
        MappedStatement stat = (MappedStatement) args[0];
        String mapperId = stat.getId();

        //获取mapper的简单签名 yyy.mapper.xxxMapper$DEV.selectXXX -> xxxMapper.selectXXX
        mapperId = mapperId.replace(LcdpMapperUtils.NAMESPACE_DEV_SUFFIX, "");
        int dotSecondLastIndex = mapperId.substring(0, mapperId.lastIndexOf(".")).lastIndexOf(".") + 1;
        String simpleSignature = mapperId.substring(dotSecondLastIndex) + " ";

        //日志推送websocket
        LcdpScriptLogWebSocket console = ApplicationContextHelper.getBean(LcdpScriptLogWebSocket.class);

        //添加日志消费者，MybatisLogger调用后会销毁
        MybatisLogger.addConsumer(message -> {
            console.pushInfoLog(simpleSignature + message);

            //记录执行sql，用于后续sql拼接
            List<String> messageList = MYBATIS_MESSAGE_HOLDER.get();

            if(ObjectUtils.isEmpty(messageList)){
                messageList = new ArrayList<>();
                MYBATIS_MESSAGE_HOLDER.set(messageList);
            }

            messageList.add(message);
        });

        long now = System.currentTimeMillis();

        //执行目标
        Object result = invocation.proceed();

        long duration = System.currentTimeMillis() - now;

        // 推送耗时几秒
        double timeConsuming = (double) duration / 1000;
        console.pushInfoLog(simpleSignature + "<==      TimeConsuming: " + timeConsuming + "s");

        MYBATIS_MESSAGE_HOLDER.set(new ArrayList<>());

        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties prop) {
    }

    private boolean isLogEnabled() {
        if (!ApplicationContextHelper.isApplicationReady()) {
            return false;
        }
        
        return LcdpScriptLogConfig.isEnabled();
    }
}
