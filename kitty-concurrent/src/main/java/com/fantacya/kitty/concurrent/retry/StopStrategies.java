package com.fantacya.kitty.concurrent.retry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Factory class for {@link StopStrategy} instances.
 *
 * @author JB
 */
public final class StopStrategies {
    private static final StopStrategy NEVER_STOP = new NeverStopStrategy();

    private StopStrategies() {
    }

    /**
     * Returns a stop strategy which never stops retrying. It might be best to
     * try not to abuse services with this kind of behavior when small wait
     * intervals between retry attempts are being used.
     *
     * @return a stop strategy which never stops
     */
    public static StopStrategy neverStop() {
        return NEVER_STOP;
    }

    /**
     * Returns a stop strategy which stops after N failed attempts.
     *
     * @param attemptNumber the number of failed attempts before stopping
     * @return a stop strategy which stops after {@code attemptNumber} attempts
     */
    public static StopStrategy stopAfterAttempt(int attemptNumber) {
        return new StopAfterAttemptStrategy(attemptNumber);
    }

    /**
     * Returns a stop strategy which stops after a given delay. If an
     * unsuccessful attempt is made, this {@link StopStrategy} will check if the
     * amount of time that's passed from the first attempt has exceeded the
     * given delay amount. If it has exceeded this delay, then using this
     * strategy causes the retrying to stop.
     *
     * @param duration the delay, starting from first attempt
     * @return a stop strategy which stops after {@code delayInMillis} time in milliseconds
     */
    public static StopStrategy stopAfterDelay(Duration duration) {
        Objects.requireNonNull(duration, "The duration may not be null");
        return new StopAfterDelayStrategy(duration.toMillis());
    }

    @Immutable
    private static final class NeverStopStrategy implements StopStrategy {
        @Override
        public boolean shouldStop(Attempt failedAttempt) {
            return false;
        }
    }

    @Immutable
    private static final class StopAfterAttemptStrategy implements StopStrategy {
        private final int maxAttemptNumber;

        public StopAfterAttemptStrategy(int maxAttemptNumber) {
            checkArgument(maxAttemptNumber >= 1, "maxAttemptNumber must be >= 1 but is %d", maxAttemptNumber);
            this.maxAttemptNumber = maxAttemptNumber;
        }

        @Override
        public boolean shouldStop(Attempt failedAttempt) {
            return failedAttempt.getAttemptNumber() >= maxAttemptNumber;
        }
    }

    @Immutable
    private static final class StopAfterDelayStrategy implements StopStrategy {
        private final long maxDelay;

        public StopAfterDelayStrategy(long maxDelay) {
            checkArgument(maxDelay >= 0L, "maxDelay must be >= 0 but is %d", maxDelay);
            this.maxDelay = maxDelay;
        }

        @Override
        public boolean shouldStop(Attempt failedAttempt) {
            return failedAttempt.getDelaySinceFirstAttempt() >= maxDelay;
        }
    }

    private static void checkArgument(boolean expression,
                                     @Nullable String errorMessageTemplate,
                                     @Nullable Object... errorMessageArgs) {
        if (!expression) {
            throw new IllegalArgumentException(
                    String.format(errorMessageTemplate, errorMessageArgs));
        }
    }
}
