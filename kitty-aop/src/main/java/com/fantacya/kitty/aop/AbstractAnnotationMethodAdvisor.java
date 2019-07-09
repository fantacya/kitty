package com.fantacya.kitty.aop;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.ResolvableType;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * 基于方法注解切面的抽象类
 * @param <T> 需要拦截的方法注解类型
 * @author harri2012
 */
public abstract class AbstractAnnotationMethodAdvisor<T extends Annotation> implements AnnotationMethodAdvisor<T>, InitializingBean {
    /**
     * 需要查找的注解类型
     */
    private Class<T> annotationType;

    private AnnotationPointcut<T> pointcut;

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

    public AbstractAnnotationMethodAdvisor() {
        this(false, false, null);
    }

    public AbstractAnnotationMethodAdvisor(boolean checkInherited, boolean checkClass, @Nullable AnnotationValidator<T> validator) {
        this.checkInherited = checkInherited;
        this.checkClass = checkClass;
        this.annotationValidator = validator;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ResolvableType type = ResolvableType.forClass(this.getClass()).as(AbstractAnnotationMethodAdvisor.class);
        @SuppressWarnings("unchecked")
        Class<T> annType = (Class<T>) type.getGeneric(0).getRawClass();
        if (annType == null) {
            throw new IllegalStateException("Class " + getClass().getCanonicalName()
                    + " must assign generic type parameter for AbstractAnnotationMethodAdvisor");
        }
        this.annotationType = annType;
        this.pointcut = new AnnotationPointcut<>(annotationType, checkInherited, checkClass, annotationValidator);
    }

    @Override
    public final Object invoke(MethodInvocation invocation) throws Throwable {
        T annotation = pointcut.getAnnotation(invocation.getThis().getClass(), invocation.getMethod());
        return invoke((ReflectiveMethodInvocation) invocation, annotation);
    }

    /**
     * 对切面进行处理的具体方法
     * @param invocation 方法调用
     * @param annotation 方法注解
     * @return
     * @throws Throwable
     */
    public abstract Object invoke(ReflectiveMethodInvocation invocation, T annotation) throws Throwable;

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    public Class<T> getAnnotationType() {
        return annotationType;
    }

    public boolean isCheckInherited() {
        return checkInherited;
    }

    public void setCheckInherited(boolean checkInherited) {
        this.checkInherited = checkInherited;
    }

    public void setCheckClass(boolean checkClass) {
        this.checkClass = checkClass;
    }

    public void setAnnotationValidator(AnnotationValidator<T> annotationValidator) {
        this.annotationValidator = annotationValidator;
    }
}

