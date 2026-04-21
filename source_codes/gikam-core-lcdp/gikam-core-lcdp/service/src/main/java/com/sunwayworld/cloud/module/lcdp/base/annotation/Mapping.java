package com.sunwayworld.cloud.module.lcdp.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于区分系统方法的注解，在系统生成的方法上标注
 * 自定义方法标注后可以将方法存入方法表中用于数据源的选择
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Mapping {

    String value() default "";

    MappingType type() default MappingType.DEFAULT;
}
