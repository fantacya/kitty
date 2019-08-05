package com.fantacya.kitty.concurrent.retry;

import com.fantacya.kitty.concurrent.exception.UncheckedTimeoutException;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * @author Jason Dunkelberger (dirkraft)
 */
public class AttemptTimeLimiterTest {

    Retryer<Void> r = RetryerBuilder.<Void>newBuilder()
            .withAttemptTimeLimiter(AttemptTimeLimiters.<Void>fixedTimeLimit(Duration.ofSeconds(1)))
            .build();

    @Test
    public void testAttemptTimeLimit() throws ExecutionException, RetryException {
        try {
            r.call(new SleepyOut(0L));
        } catch (ExecutionException e) {
            Assert.fail("Should not timeout");
        }

        try {
            r.call(new SleepyOut(10 * 1000L));
            Assert.fail("Expected timeout exception");
        } catch (ExecutionException e) {
            // expected
            Assert.assertEquals(UncheckedTimeoutException.class, e.getCause().getClass());
        }
    }

    static class SleepyOut implements Callable<Void> {

        final long sleepMs;

        SleepyOut(long sleepMs) {
            this.sleepMs = sleepMs;
        }

        @Override
        public Void call() throws Exception {
            Thread.sleep(sleepMs);
            System.out.println("I'm awake now");
            return null;
        }
    }
}
