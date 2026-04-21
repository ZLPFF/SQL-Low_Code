package com.sunwayworld.cloud.module.lcdp.message.log;

public class LcdpScriptLogConfig {
    private static final ThreadLocal<Boolean> MYBATIS_MESSAGE_ENABLE_HOLDER = new ThreadLocal<>();
    
    public static void enable() {
        MYBATIS_MESSAGE_ENABLE_HOLDER.set(true);
    }
    
    public static void disable() {
        MYBATIS_MESSAGE_ENABLE_HOLDER.remove();
    }
    
    public static boolean isEnabled() {
        return Boolean.TRUE.equals(MYBATIS_MESSAGE_ENABLE_HOLDER.get());
    }
}
