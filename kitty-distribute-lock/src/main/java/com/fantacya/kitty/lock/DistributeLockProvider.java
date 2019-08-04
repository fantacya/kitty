package com.fantacya.kitty.lock;

/**
 * @description:
 * @author: harri2012
 * @created: 2019-08-04 03:27
 */
public interface DistributeLockProvider {

    DistributeLock createLock(String key);
}
