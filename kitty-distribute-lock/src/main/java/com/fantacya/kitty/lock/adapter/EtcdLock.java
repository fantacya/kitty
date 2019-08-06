package com.fantacya.kitty.lock.adapter;

import com.fantacya.kitty.lock.DistributeLock;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.lock.LockResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @description:
 * @author: harri2012
 * @created: 2019-08-04 01:23
 */
public class EtcdLock implements DistributeLock {
    private static final Logger LOG = LoggerFactory.getLogger(EtcdLock.class);

    private final Client client;

    private final String lockKey;

    private LockResponse response;

    public EtcdLock(Client client, String lockKey) {
        this.client = client;
        this.lockKey = lockKey;
    }

    @Override
    public String key() {
        return lockKey;
    }

    @Override
    public boolean lock(int expireTime, int timeout, int retryInterval) {
        int seconds = (int) Math.ceil(expireTime / 1000.0);
        try {
            long leaseId = client.getLeaseClient().grant(seconds).get().getID();
            this.response = client.getLockClient().lock(ByteSequence.from(lockKey, StandardCharsets.UTF_8), leaseId).get(timeout, TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            LOG.warn("acquire lock failed, lock key: {}", lockKey, e);
            return false;
        }
    }

    @Override
    public void release() {
        if (response == null) {
            throw new IllegalStateException("you haven't got lock " + lockKey);
        }

        try {
            client.getLockClient().unlock(ByteSequence.from(lockKey, StandardCharsets.UTF_8)).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("failed to release lock, lockKey = {}", lockKey, e);
        }
        response = null;
    }
}
