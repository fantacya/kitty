package com.fantacya.kitty.lock;

/**
 * @description:
 * @author: harri2012
 * @created: 2019-08-04 12:55
 */
public class LockException extends RuntimeException {
    public LockException() {
    }

    public LockException(String message) {
        super(message);
    }

    public LockException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockException(Throwable cause) {
        super(cause);
    }
}
