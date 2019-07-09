package com.fantacya.kitty.aop;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;

import java.lang.annotation.Annotation;

/**
 * @description: 基于方法注解的Pointcut及advisor
 * @author: harri2012
 * @created: 2019-07-06 21:44
 *
 * @param <T> 注解类型
 */
public interface AnnotationMethodAdvisor<T extends Annotation> extends PointcutAdvisor, MethodInterceptor {

    @Override
    Object invoke(MethodInvocation invocation) throws Throwable;

    @Override
    Pointcut getPointcut();

    @Override
    default Advice getAdvice() {
        return this;
    }

    @Override
    default boolean isPerInstance() {
        return true;
    }
}
