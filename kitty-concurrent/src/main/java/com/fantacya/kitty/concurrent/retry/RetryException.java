package com.fantacya.kitty.concurrent.retry;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * An exception indicating that none of the attempts of the {@link Retryer}
 * succeeded. If the last {@link Attempt} resulted in an Exception, it is set as
 * the cause of the {@link RetryException}.
 *
 * @author JB
 */
public final class RetryException extends Exception {

    private final int numberOfFailedAttempts;
    private final Attempt<?> lastFailedAttempt;

    /**
     * If the last {@link Attempt} had an Exception, ensure it is available in
     * the stack trace.
     *
     * @param numberOfFailedAttempts times we've tried and failed
     * @param lastFailedAttempt      what happened the last time we failed
     */
    public RetryException(int numberOfFailedAttempts, @Nonnull Attempt<?> lastFailedAttempt) {
        this("Retrying failed to complete successfully after " + numberOfFailedAttempts + " attempts.", numberOfFailedAttempts, lastFailedAttempt);
    }

    /**
     * If the last {@link Attempt} had an Exception, ensure it is available in
     * the stack trace.
     *
     * @param message                Exception description to be added to the stack trace
     * @param numberOfFailedAttempts times we've tried and failed
     * @param lastFailedAttempt      what happened the last time we failed
     */
    public RetryException(String message, int numberOfFailedAttempts, Attempt<?> lastFailedAttempt) {
        super(message, Objects.requireNonNull(lastFailedAttempt, "Last attempt was null").hasException() ? lastFailedAttempt.getExceptionCause() : null);
        this.numberOfFailedAttempts = numberOfFailedAttempts;
        this.lastFailedAttempt = lastFailedAttempt;
    }

    /**
     * Returns the number of failed attempts
     *
     * @return the number of failed attempts
     */
    public int getNumberOfFailedAttempts() {
        return numberOfFailedAttempts;
    }

    /**
     * Returns the last failed attempt
     *
     * @return the last failed attempt
     */
    public Attempt<?> getLastFailedAttempt() {
        return lastFailedAttempt;
    }
}
