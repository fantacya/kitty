package com.fantacya.kitty.concurrent.exception;

import javax.annotation.Nullable;

/**
 * Unchecked version of {@link java.util.concurrent.TimeoutException}.
 *
 * @author Kevin Bourrillion
 * @since 1.0
 */
public class UncheckedTimeoutException extends RuntimeException {
  public UncheckedTimeoutException() {}

  public UncheckedTimeoutException(@Nullable String message) {
    super(message);
  }

  public UncheckedTimeoutException(@Nullable Throwable cause) {
    super(cause);
  }

  public UncheckedTimeoutException(@Nullable String message, @Nullable Throwable cause) {
    super(message, cause);
  }

  private static final long serialVersionUID = 0;
}
