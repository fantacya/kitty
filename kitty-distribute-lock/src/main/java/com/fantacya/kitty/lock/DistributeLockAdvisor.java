package com.fantacya.kitty.lock;

import com.fantacya.kitty.aop.AbstractAnnotationMethodAdvisor;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @author: harri2012
 * @date: 2019-07-07 23:28
 */
public class DistributeLockAdvisor extends AbstractAnnotationMethodAdvisor<DistributeLocked> implements ApplicationContextAware {

    private final DistributeLockProvider lockProvider;

    private final ExpressionEvaluator<String> evaluator = new ExpressionEvaluator<>();

    private ApplicationContext applicationContext;

    public DistributeLockAdvisor(DistributeLockProvider lockProvider) {
        this.lockProvider = lockProvider;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object invoke(ReflectiveMethodInvocation invocation, DistributeLocked annotation) throws Throwable {
        String key = annotation.key();
        if (StringUtils.hasLength(key)) {
            key = getKey(invocation.getThis(), invocation.getArguments(), invocation.getThis().getClass(),
                    invocation.getMethod(), key);
        } else if (StringUtils.hasLength(annotation.keyGenerator())) {
            LockKeyGenerator generator = applicationContext.getBean(annotation.keyGenerator(), LockKeyGenerator.class);
            key = generator.generate(invocation.getThis(), invocation.getMethod(), invocation.getArguments());
        } else if (annotation.keyGeneratorClass() != LockKeyGenerator.class) {
            LockKeyGenerator generator = (LockKeyGenerator) applicationContext.getBean(annotation.keyGeneratorClass());
            key = generator.generate(invocation.getThis(), invocation.getMethod(), invocation.getArguments());
        }

        String lockKey = key;
        Object[] result = new Object[1];
        Throwable[] throwable = new Throwable[1];
        lockProvider.createLock(key).doWithLock(annotation.autoExpireTime() * 1000, annotation.timeout(), locked -> {
            if (locked) {
                try {
                    result[0] = invocation.proceed();
                } catch (Throwable t) {
                    throwable[0] = t;
                }
            } else {
                throw new LockException("acquire lock '" + lockKey + "' failed");
            }
        });

        if (throwable[0] != null) {
            throw throwable[0];
        }
        return result;
    }

    private String getKey(Object object, Object[] args, Class clazz, Method method, String condition) {
        EvaluationContext evaluationContext = evaluator.createEvaluationContext(object, clazz, method, args);
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, clazz);
        return evaluator.condition(condition, methodKey, evaluationContext, String.class);
    }

    static class ExpressionEvaluator<T> extends CachedExpressionEvaluator {
        // shared param discoverer since it caches data internally
        private final ParameterNameDiscoverer paramNameDiscoverer = new DefaultParameterNameDiscoverer();

        private final Map<ExpressionKey, Expression> conditionCache = new ConcurrentHashMap<>(64);

        private final Map<AnnotatedElementKey, Method> targetMethodCache = new ConcurrentHashMap<>(64);

        /**
         * Create the suitable {@link EvaluationContext} for the specified event handling
         * on the specified method.
         */
        public EvaluationContext createEvaluationContext(Object object, Class<?> targetClass, Method method, Object[] args) {

            Method targetMethod = getTargetMethod(targetClass, method);
            ExpressionRootObject root = new ExpressionRootObject(object, args);
            return new MethodBasedEvaluationContext(root, targetMethod, args, this.paramNameDiscoverer);
        }

        /**
         * Specify if the condition defined by the specified expression matches.
         */
        public T condition(String conditionExpression, AnnotatedElementKey elementKey, EvaluationContext evalContext, Class<T> clazz) {
            return getExpression(this.conditionCache, elementKey, conditionExpression).getValue(evalContext, clazz);
        }

        private Method getTargetMethod(Class<?> targetClass, Method method) {
            AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);
            Method targetMethod = this.targetMethodCache.get(methodKey);
            if (targetMethod == null) {
                targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
                if (targetMethod == null) {
                    targetMethod = method;
                }
                this.targetMethodCache.put(methodKey, targetMethod);
            }
            return targetMethod;
        }
    }

    public static class ExpressionRootObject {
        private final Object object;

        private final Object[] args;

        public ExpressionRootObject(Object object, Object[] args) {
            this.object = object;
            this.args = args;
        }

        public Object getObject() {
            return object;
        }

        public Object[] getArgs() {
            return args;
        }
    }
}
