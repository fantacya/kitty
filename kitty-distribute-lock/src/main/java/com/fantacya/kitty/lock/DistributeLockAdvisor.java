package com.fantacya.kitty.lock;

import com.fantacya.kitty.aop.AbstractAnnotationMethodAdvisor;
import org.springframework.aop.framework.ReflectiveMethodInvocation;

/**
 * @description:
 * @author: harri2012
 * @date: 2019-07-07 23:28
 */
public class DistributeLockAdvisor extends AbstractAnnotationMethodAdvisor<DistributeLocked> {

    @Override
    public Object invoke(ReflectiveMethodInvocation invocation, DistributeLocked annotation) throws Throwable {
        return null;
    }
}
