package com.fantacya.kitty.aop.advisor;

import com.fantacya.kitty.aop.AbstractAnnotationMethodAdvisor;
import com.fantacya.kitty.aop.annotation.LogAdvised;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Order(200)
@Component
public class LogMethodAdvisor extends AbstractAnnotationMethodAdvisor<LogAdvised> {
    private static final Logger LOG = LoggerFactory.getLogger(LogMethodAdvisor.class);

    @Override
    public Object invoke(ReflectiveMethodInvocation invocation, LogAdvised logAdvised) throws Throwable {
        Method method = invocation.getMethod();

        LOG.info("Invoke method: {}, args={}", method, invocation.getArguments());
        try {
            Object result = invocation.proceed();
            LOG.info("Invocation result: {}", result);
            return result;
        } catch (Throwable t) {
            LOG.info("Invocation cause exception, method: {}", method, t);
            throw t;
        }
    }
}
