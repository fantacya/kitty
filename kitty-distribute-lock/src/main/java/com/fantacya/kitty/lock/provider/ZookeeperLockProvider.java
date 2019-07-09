package com.fantacya.kitty.lock.provider;

/**
 * @description:
 * @author: harri2012
 * @created: 2019-07-07 23:34
 */
public class ZookeeperLockProvider implements DistributeLockProvider {

    @Override
    public Object lock(String key, long expireTime) {
        return null;
    }

    @Override
    public boolean release(String key, Object stamp) {
        return false;
    }
}
