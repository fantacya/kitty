package com.fantacya.kitty.lock.adapter;

import com.fantacya.kitty.lock.DistributeLock;
import com.fantacya.kitty.lock.LockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
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
    public boolean lock(int expireTime, int timeout, int retryInterval) {
        String stamp = "Locked@" + SIMPLE_DATE_FORMAT.format(new Date());
        long now = System.currentTimeMillis();
        long deadline = now + timeout;
        while (now < deadline) {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, stamp, Duration.of(expireTime, ChronoUnit.MILLIS));
            if (Objects.equals(result, Boolean.TRUE)) {
                this.stamp = stamp;
                return true;
            }

            now = System.currentTimeMillis() + retryInterval;
            if (now < deadline) {
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException e) {
                    throw new LockException(e);
                }
            }
        }
        return false;
    }

    @Override
    public void release() {
        if (stamp == null) {
            throw new IllegalStateException("you haven't got lock " + key);
        }

        RedisScript<Integer> script = new DefaultRedisScript<>(
                "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
                Integer.class);
        Integer result = redisTemplate.execute(script, Collections.singletonList(key), stamp);
        if (!Objects.equals(result, 1)) {
            LOG.warn("lock is held by others, key={}", key);
        }
        stamp = null;
    }
}
