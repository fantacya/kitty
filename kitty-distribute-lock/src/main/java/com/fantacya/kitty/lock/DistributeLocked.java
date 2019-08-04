package com.fantacya.kitty.lock;

import javax.crypto.KeyGenerator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description:
 * @author: harri2012
 * @created: 2019-07-07 23:20
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributeLocked {

    /**
     * 分布式锁的key，EL表达式。
     * <p>The SpEL expression evaluates against a dedicated context that provides the
     * following meta-data:
     * <ul>
     * <li>{@code #result} for a reference to the result of the method invocation. For
     * supported wrappers such as {@code Optional}, {@code #result} refers to the actual
     * object, not the wrapper</li>
     * <li>{@code #root.method}, {@code #root.target}, and {@code #root.caches} for
     * references to the {@link java.lang.reflect.Method method}, target object, and
     * affected cache(s) respectively.</li>
     * <li>Shortcuts for the method name ({@code #root.methodName}) and target class
     * ({@code #root.targetClass}) are also available.
     * <li>Method arguments can be accessed by index. For instance the second argument
     * can be accessed via {@code #root.args[1]}, {@code #p1} or {@code #a1}. Arguments
     * can also be accessed by name if that information is available.</li>
     * </ul>
     * @return
     */
    String key() default "";

    /**
     * 锁自动释放时长，单位秒
     * @return
     */
    int autoExpireTime() default 5;

    /**
     * 获取锁超时时长，单位毫秒
     * @return
     */
    int timeout() default 500;

    /**
     * The bean name of the custom {@link LockKeyGenerator}
     * to use.
     * <p>Mutually exclusive with the {@link #key} attribute.
     * @see LockKeyGenerator#generate
     */
    String keyGenerator() default "";

    Class<? extends KeyGenerator> keyGeneratorClass() default KeyGenerator.class;
}
