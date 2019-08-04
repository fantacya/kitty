package com.fantacya.kitty.lock.adapter;

import com.fantacya.kitty.lock.DistributeLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @description:
 * @author: harri2012
 * @created: 2019-07-07 23:33
 */
public class RedisLock implements DistributeLock {
    private static final Logger LOG = LoggerFactory.getLogger(RedisLock.class);
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private final StringRedisTemplate redisTemplate;

    private final String key;

    private String stamp;

    public RedisLock(@NonNull StringRedisTemplate redisTemplate, @NonNull String key) {
        Assert.hasLength(key, "key must not be empty");
        this.redisTemplate = redisTemplate;
        this.key = key;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public boolean lock(int expireTime, int timeout) {
        String stamp = "Locked@" + SIMPLE_DATE_FORMAT.format(new Date());
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, stamp, Duration.of(expireTime, ChronoUnit.MILLIS));
        if (Objects.equals(result, Boolean.TRUE)) {
            this.stamp = stamp;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void release() {
        if (stamp == null) {
            throw new IllegalStateException("you haven't got lock " + key);
        }

        final String[] val = new String[1];
        List<Object> result = redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                val[0] = (String) operations.opsForValue().get(key);
                if (key.equals(val[0])) {
                    operations.delete(key);
                }
                return operations.exec();
            }
        });

        if (result == null || result.size() != 2 || Objects.equals(result.get(1), Boolean.TRUE)) {
            LOG.warn("failed to release lock, lock key: {}, value: {}, expected value: {}", key, val[0], stamp);
        }
        stamp = null;
    }
}
