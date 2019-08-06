package com.fantacya.kitty.lock;

import java.util.function.Consumer;

/**
 * @description: 分布式锁提供者
 * @author: harri2012
 * @created: 2019-07-07 23:13
 */
public interface DistributeLock {

    int DEFAULT_RETRY_TIMEOUT = 20;

    /**
     * 获取锁的key
     * @return
     */
    String key();

    /**
     * 获取锁
     * @param expireTime 锁自动失效时长，单位：毫秒
     * @param timeout 获取锁操作超时时长，单位：毫秒
     * @param retryInterval 重试时间间隔，单位：毫秒
     * @return 锁定成功时返回锁的戳记 <code>stamp</code>，用于释放锁。锁定失败时，返回null
     */
    boolean lock(int expireTime, int timeout, int retryInterval);

    /**
     * 获取锁
     * @param expireTime 锁自动失效时长，单位：毫秒
     * @param timeout 获取锁操作超时时长，单位：毫秒
     * @return
     */
    default boolean lock(int expireTime, int timeout) {
        return lock(expireTime, timeout, DEFAULT_RETRY_TIMEOUT);
    }

    /**
     * 释放锁
     */
    void release();

    /**
     * 获取锁并执行方法
     * @param expireTime 自动失效时长，毫秒
     * @param timeout 获取锁操作超时时长，毫秒
     * @param run 执行函数，在本线程内执行
     */
    default void doWithLock(int expireTime, int timeout, Runnable run) {
        Object stamp = lock(expireTime, timeout);
        if (stamp != null) {
            try {
                run.run();
            } finally {
                release();
            }
        } else {
            throw new RuntimeException("require lock failed, key=" + key());
        }
    }

    /**
     * 获取锁并执行方法
     * @param expireTime 超时时长，毫秒
     * @param timeout 获取锁操作超时时长，毫秒
     * @param callback 执行方法，在本线程内执行，获取锁成功时参数为true，失败时参数为false
     */
    default void doWithLock(int expireTime, int timeout, Consumer<Boolean> callback) {
        Object stamp = lock(expireTime, timeout);
        try {
            callback.accept(stamp == null);
        } finally {
            if (stamp != null) {
                release();
            }
        }
    }
}
