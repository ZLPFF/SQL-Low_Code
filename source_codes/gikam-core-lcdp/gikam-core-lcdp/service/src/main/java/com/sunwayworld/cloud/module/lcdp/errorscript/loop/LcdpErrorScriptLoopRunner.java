package com.sunwayworld.cloud.module.lcdp.errorscript.loop;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunwayworld.cloud.module.lcdp.errorscript.service.LcdpErrorScriptService;
import com.sunwayworld.framework.loop.registry.LoopRunner;
import com.sunwayworld.framework.utils.SpringUtils;

public class LcdpErrorScriptLoopRunner implements LoopRunner {
    private static final Logger logger = LoggerFactory.getLogger(LcdpErrorScriptLoopRunner.class);
    
    private LocalDateTime lastActivatedTime;
    
    @Override
    public void run() {
        logger.info("删除编译错误的异常日志-开始");
        lastActivatedTime = LocalDateTime.now();
        SpringUtils.getBean(LcdpErrorScriptService.class).deleteAbnormalRecord();
        logger.info("删除编译错误的异常日志-结束");
    }

    @Override
    public boolean active() {
        if (lastActivatedTime == null) {
            return true;
        }
        
        return LocalDateTime.now().isAfter(lastActivatedTime.plusMinutes(10));
    }
}
