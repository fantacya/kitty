package com.fantacya.kitty.lock.provider;

/**
 * @description: 分布式锁提供者
 * @author: harri2012
 * @created: 2019-07-07 23:13
 */
public interface DistributeLockProvider {

    /**
     * 获取锁
     * @param key 锁的key
     * @param expireTime 锁自动失效时长，单位：毫秒
     * @return 锁定成功时返回锁的戳记 <code>stamp</code>，用于释放锁。锁定失败时，返回null
     */
    Object lock(String key, long expireTime);

    /**
     * 释放锁
     * @param key 锁的key
     * @param stamp 锁的戳记，获取锁成功时的返回值。
     * @return 是否释放成功
     */
    boolean release(String key, Object stamp);
}
