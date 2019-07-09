package com.fantacya.kitty.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @description:
 * @author: harri2012
 * @created: 2019-07-07 13:28
 */
public interface AnnotationValidator<T extends Annotation> {

    /**
     * 对注解的属性进行校验。不符合校验规则时，抛出 {@link AnnotationValidateException } 异常
     * @param targetClass 目标类型
     * @param method 注解的方法
     * @param annotation 注解对象
     * @throws AnnotationValidateException
     */
    void validate(Class<?> targetClass, Method method, T annotation) throws AnnotationValidateException;
}
