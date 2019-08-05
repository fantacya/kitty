package com.fantacya.kitty.concurrent.retry;

import com.fantacya.kitty.concurrent.SimpleTimeLimiter;
import com.fantacya.kitty.concurrent.TimeLimiter;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Factory class for instances of {@link AttemptTimeLimiter}
 *
 * @author Jason Dunkelberger (dirkraft)
 */
public class AttemptTimeLimiters {

    private AttemptTimeLimiters() {
    }

    /**
     * @param <V> The type of the computation result.
     * @return an {@link AttemptTimeLimiter} impl which has no time limit
     */
    public static <V> AttemptTimeLimiter<V> noTimeLimit() {
        return new NoAttemptTimeLimit<V>();
    }

    /**
     * For control over thread management, it is preferable to offer an {@link ExecutorService} through the other
     * factory method, {@link #fixedTimeLimit(Duration, ExecutorService)}. See the note on
     * {@link SimpleTimeLimiter#create(ExecutorService)}, which this AttemptTimeLimiter uses.
     *
     * @param timeout that an attempt may persist before being circumvented
     * @param <V>      the type of the computation result
     * @return an {@link AttemptTimeLimiter} with a fixed time limit for each attempt
     */
    public static <V> AttemptTimeLimiter<V> fixedTimeLimit(@Nonnull Duration timeout) {
        Objects.requireNonNull(timeout);
        return new FixedAttemptTimeLimit<V>(timeout);
    }

    /**
     * @param timeout        that an attempt may persist before being circumvented
     * @param executorService used to enforce time limit
     * @param <V>             the type of the computation result
     * @return an {@link AttemptTimeLimiter} with a fixed time limit for each attempt
     */
    public static <V> AttemptTimeLimiter<V> fixedTimeLimit(@Nonnull Duration timeout, @Nonnull ExecutorService executorService) {
        Objects.requireNonNull(timeout);
        return new FixedAttemptTimeLimit<V>(timeout, executorService);
    }

    @Immutable
    private static final class NoAttemptTimeLimit<V> implements AttemptTimeLimiter<V> {
        @Override
        public V call(Callable<V> callable) throws Exception {
            return callable.call();
        }
    }

    @Immutable
    private static final class FixedAttemptTimeLimit<V> implements AttemptTimeLimiter<V> {

        private final TimeLimiter timeLimiter;
        private final Duration timeout;

        public FixedAttemptTimeLimit(@Nonnull Duration timeout) {
            this(SimpleTimeLimiter.create(Executors.newCachedThreadPool()), timeout);
        }

        public FixedAttemptTimeLimit(@Nonnull Duration timeout, @Nonnull ExecutorService executorService) {
            this(SimpleTimeLimiter.create(executorService), timeout);
        }

        private FixedAttemptTimeLimit(@Nonnull TimeLimiter timeLimiter, @Nonnull Duration timeout) {
            Objects.requireNonNull(timeLimiter);
            Objects.requireNonNull(timeout);
            this.timeLimiter = timeLimiter;
            this.timeout = timeout;
        }

        @Override
        public V call(Callable<V> callable) throws Exception {
            return timeLimiter.callWithTimeout(callable, timeout);
        }
    }
}
