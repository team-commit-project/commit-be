package com.receiptmate.auth.exception;

public class RedisOperationException extends RuntimeException {

    public RedisOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisOperationException(String message) {
        super(message);
    }
}
