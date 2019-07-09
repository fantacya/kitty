package com.fantacya.kitty.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description:
 * @author: harri2012
 * @created: 2019-07-07 22:52
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RandomAdvised {

    /**
     * 随机值的范围
     * @return
     */
    int bound() default Integer.MAX_VALUE;
}
