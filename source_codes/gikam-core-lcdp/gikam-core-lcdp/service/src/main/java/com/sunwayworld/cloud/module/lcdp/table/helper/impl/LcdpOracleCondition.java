package com.sunwayworld.cloud.module.lcdp.table.helper.impl;

import java.util.Arrays;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author yangsz@sunway.com 2023-08-31
 */
public class LcdpOracleCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return Arrays.asList(context.getEnvironment().getActiveProfiles()).contains("oracle") && !context.getEnvironment().getProperty("spring.datasource.driver-class-name").contains("kingbase");
    }
}
