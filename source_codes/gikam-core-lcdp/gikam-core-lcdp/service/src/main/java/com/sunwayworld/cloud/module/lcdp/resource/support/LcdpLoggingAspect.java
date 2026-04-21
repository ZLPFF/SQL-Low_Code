package com.sunwayworld.cloud.module.lcdp.resource.support;

import java.lang.reflect.Modifier;

import com.sunwayworld.cloud.module.lcdp.base.annotation.Mapping;
import com.sunwayworld.cloud.module.lcdp.base.annotation.MappingType;
import com.sunwayworld.framework.log.LogHolder;
import com.sunwayworld.framework.log.annotation.LogModule;
import com.sunwayworld.module.sys.log.bean.CoreLogBean;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * @author yangsz@sunway.com 2023-12-07
 */
@Aspect
@Component
public class LcdpLoggingAspect {

    @Around("@annotation(mapping)")
    public Object handleResourceException(ProceedingJoinPoint pjp, Mapping mapping) throws Throwable {
        CoreLogBean logBean = LogHolder.popContextLog();

        if (logBean != null && mapping != null) {

            MethodSignature methodSignature = (MethodSignature) pjp.getSignature();

            String desc = mapping.value();

            MappingType type = mapping.type();

            logBean.setName(desc);

            // 查找目标类上的 LogModule 注解
            LogModule logModule = pjp.getTarget().getClass().getAnnotation(LogModule.class);
            if (logModule != null) {
                logBean.setModule(logModule.value());
            } else {
                logBean.setModule(desc);
            }

            logBean.setType(type.name());

            logBean.setSignature(getSimpleSignature(pjp.getTarget(), methodSignature));
        }

        return pjp.proceed();
    }

    private String getSimpleSignature(Object target, MethodSignature methodSignature) {

        StringBuilder builder = new StringBuilder();
        builder.append(Modifier.toString(methodSignature.getModifiers())).append(" ")
                .append(methodSignature.getReturnType().getSimpleName()).append(" ")
                .append(target.getClass().getSimpleName()).append(".").append(methodSignature.getName()).append("(");

        if (methodSignature.getParameterNames() != null) {
            for (int i = 0, j = methodSignature.getParameterNames().length; i < j; i++) {
                if (i > 0) {
                    builder.append(", ");
                }

                String parameterName = methodSignature.getParameterNames()[i];
                Class<?> parameterType = methodSignature.getParameterTypes()[i];

                builder.append(parameterType.getSimpleName()).append(" ").append(parameterName);
            }
            builder.append(")");
        }

        return builder.toString();
    }
}
