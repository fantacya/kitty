package com.fantacya.kitty.aop.advisor;

import com.fantacya.kitty.aop.AbstractAnnotationMethodAdvisor;
import com.fantacya.kitty.aop.AnnotationValidateException;
import com.fantacya.kitty.aop.annotation.RandomAdvised;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * @description:
 * @author: huangdachao
 * @date: 2019-07-07
 * @time: 22:56
 * Copyright (C) 2018 MTDP
 * All rights reserved
 */
@Order(100)
@Component
public class RandomAdvisor extends AbstractAnnotationMethodAdvisor<RandomAdvised> {

    public RandomAdvisor() {
        setAnnotationValidator((targetClass, method, annotation) -> {
            if (Integer.class == method.getReturnType() || int.class == method.getReturnType()) {
                throw new AnnotationValidateException(String.format("注解%s只可用在返回整数类型的方法上", annotation.annotationType()));
            }
        });
    }

    @Override
    public Object invoke(ReflectiveMethodInvocation invocation, RandomAdvised annotation) throws Throwable {
        Integer r = (Integer) invocation.proceed();
        return r + new Random().nextInt(annotation.bound());
    }
}
