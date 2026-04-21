package com.sunwayworld.cloud.module.lcdp.base.event;

import java.util.Map;

import org.springframework.context.ApplicationEvent;

/**
 * 低代码标准的通用事件类
 *
 * @author zhangjr@sunwayworld.com 2024年8月2日
 */
public class LcdpApplicationEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1627321410262825016L;
    
    private final Map<String, Object> param;
    
    public LcdpApplicationEvent(String key, Map<String, Object> param) {
        super(key);
        
        this.param = param;
    }
    
    public Map<String, Object> getParam() {
        return param;
    }
}
