package com.fantacya.kitty.lock.adapter;

import com.fantacya.kitty.lock.DistributeLock;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: harri2012
 * @created: 2019-07-07 23:34
 */
public class ZookeeperLock implements DistributeLock {

    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperLock.class);

    private final InterProcessMutex lock;

    private final String lockPath;

    private Date lockTime;

    public ZookeeperLock(CuratorFramework client, String lockPath) {
        this.lockPath = lockPath;
        this.lock = new InterProcessMutex(client, lockPath);
    }

    @Override
    public String key() {
        return lockPath;
    }

    @Override
    public boolean lock(int expireTime, int timeout, int retryInterval) {
        try {
            boolean result = lock.acquire(timeout, TimeUnit.MILLISECONDS);
            if (result) {
                lockTime = new Date();
            }
            return result;
        } catch (Exception e) {
            LOG.warn("acquire lock got exception, lock path: {}", lockPath, e);
            return false;
        }
    }

    @Override
    public void release() {
        if (lockTime == null) {
            throw new IllegalStateException("you haven't got lock " + lockPath);
        }

        try {
            lock.release();
            lockTime = null;
        } catch (Exception e) {
            LOG.warn("failed to release lock, lock path: {}", lockPath, e);
        }
    }
}
