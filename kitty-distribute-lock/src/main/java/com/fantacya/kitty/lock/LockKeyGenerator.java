package com.fantacya.kitty.lock;

import java.lang.reflect.Method;

/**
 * @description:
 * @author: harri2012
 * @created: 2019-07-07 23:26
 */
@FunctionalInterface
public interface LockKeyGenerator {

    /**
     * Generate a key for the given method and its parameters.
     * @param target the target instance
     * @param method the method being called
     * @param params the method parameters (with any var-args expanded)
     * @return a generated key
     */
    String generate(Object target, Method method, Object... params);
}
