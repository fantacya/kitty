package com.fantacya.kitty.concurrent.retry;

/**
 * This listener provides callbacks for several events that occur when running
 * code through a {@link Retryer} instance.
 */
public interface RetryListener {

    /**
     * This method with fire no matter what the result is and before the
     * rejection predicate and stop strategies are applied.
     *
     * @param attempt the current {@link Attempt}
     * @param <V>     the type returned by the retryer callable
     */
    <V> void onRetry(Attempt<V> attempt);
}
