package com.fantacya.kitty.aop;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.StaticMethodMatcher;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据注解识别的Pointcut
 * @author harri2012
 */
public class AnnotationPointcut<T extends Annotation> implements Pointcut {
    /**
     * 保存匹配方法的注解对象，方便拦截时获取
     */
    private ConcurrentHashMap<MethodAnnotationKey, Annotation> annotationMap = new ConcurrentHashMap<>(512);

    private Class<? extends Annotation> annotationType;

    private MethodMatcher methodMatcher;

    public AnnotationPointcut(Class<T> annotationType) {
        this(annotationType, false, false, null);
    }

    /**
     * 构造方法
     * @param annotationType 注解类型
     * @param checkInherited 是否检查父类或接口中的方法注解
     * @param checkClass 是否检查所在类的注解
     */
    public AnnotationPointcut(Class<T> annotationType, boolean checkInherited, boolean checkClass, AnnotationValidator<T> validator) {
        this.annotationType = annotationType;
        this.methodMatcher = new AnnotationMethodMatcher<>(annotationType, checkInherited, checkClass, validator);
    }

    @Override
    public ClassFilter getClassFilter() {
        return ClassFilter.TRUE;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return methodMatcher;
    }

    @SuppressWarnings("unchecked")
    public T getAnnotation(Class<?> targetClass, Method method) {
        return (T) annotationMap.get(new MethodAnnotationKey(targetClass, method, annotationType));
    }

    class AnnotationMethodMatcher<T extends Annotation> extends StaticMethodMatcher {
        private Class<T> annotationType;

        /**
         * 是否在父类或接口类中查找方法注解
         */
        private boolean checkInherited;

        /**
         * 是否在类的注解中查找注解
         */
        private boolean checkClass;

        /**
         * 注解属性校验器
         */
        private AnnotationValidator<T> annotationValidator;

        public AnnotationMethodMatcher(Class<T> annotationType, boolean checkInherited, boolean checkClass, AnnotationValidator<T> validator) {
            this.annotationType = annotationType;
            this.checkInherited = checkInherited;
            this.checkClass = checkClass;
            this.annotationValidator = validator;
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            // Proxy classes never have annotations on their redeclared methods.
            if (Proxy.isProxyClass(targetClass)) {
                return false;
            }

            T annotation = checkInherited ? AnnotationUtils.findAnnotation(method, annotationType)
                    : AnnotationUtils.getAnnotation(method, annotationType);
            if (annotation == null && checkClass) {
                annotation = checkInherited ? AnnotationUtils.findAnnotation(targetClass, annotationType)
                        : AnnotationUtils.getAnnotation(targetClass, annotationType);
            }

            if (annotation != null) {
                if (annotationValidator != null) {
                    annotationValidator.validate(targetClass, method, annotation);
                }
                annotationMap.put(new MethodAnnotationKey(targetClass, method, annotationType), annotation);
                return true;
            }
            return false;
        }

    }

    public static class MethodAnnotationKey {
        private final Class<?> invokeClass;
        private final Method invokeMethod;
        private final Class<?> annotationType;

        public MethodAnnotationKey(Class<?> invokeClass, Method invokeMethod, Class<?> annotationType) {
            this.invokeClass = invokeClass;
            this.invokeMethod = invokeMethod;
            this.annotationType = annotationType;
        }

        public Class<?> getInvokeClass() {
            return invokeClass;
        }

        public Method getInvokeMethod() {
            return invokeMethod;
        }

        public Class<?> getAnnotationType() {
            return annotationType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MethodAnnotationKey that = (MethodAnnotationKey) o;
            return invokeClass.equals(that.invokeClass) &&
                    invokeMethod.equals(that.invokeMethod) &&
                    annotationType.equals(that.annotationType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(invokeClass, invokeMethod, annotationType);
        }
    }
}
