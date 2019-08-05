package com.fantacya.kitty.concurrent.retry;

import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StopStrategiesTest {

    @Test
    public void testNeverStop() {
        assertFalse(StopStrategies.neverStop().shouldStop(failedAttempt(3, 6546L)));
    }

    @Test
    public void testStopAfterAttempt() {
        assertFalse(StopStrategies.stopAfterAttempt(3).shouldStop(failedAttempt(2, 6546L)));
        assertTrue(StopStrategies.stopAfterAttempt(3).shouldStop(failedAttempt(3, 6546L)));
        assertTrue(StopStrategies.stopAfterAttempt(3).shouldStop(failedAttempt(4, 6546L)));
    }

    @Test
    public void testStopAfterDelayWithMilliseconds() {
        assertFalse(StopStrategies.stopAfterDelay(Duration.ofMillis(1000)).shouldStop(failedAttempt(2, 999L)));
        assertTrue(StopStrategies.stopAfterDelay(Duration.ofMillis(1000)).shouldStop(failedAttempt(2, 1000L)));
        assertTrue(StopStrategies.stopAfterDelay(Duration.ofMillis(1000)).shouldStop(failedAttempt(2, 1001L)));
    }

    @Test
    public void testStopAfterDelayWithTimeUnit() {
        assertFalse(StopStrategies.stopAfterDelay(Duration.ofSeconds(1)).shouldStop(failedAttempt(2, 999L)));
        assertTrue(StopStrategies.stopAfterDelay(Duration.ofSeconds(1)).shouldStop(failedAttempt(2, 1000L)));
        assertTrue(StopStrategies.stopAfterDelay(Duration.ofSeconds(1)).shouldStop(failedAttempt(2, 1001L)));
    }

    public Attempt<Boolean> failedAttempt(long attemptNumber, long delaySinceFirstAttempt) {
        return new Retryer.ExceptionAttempt<Boolean>(new RuntimeException(), attemptNumber, delaySinceFirstAttempt);
    }
}
