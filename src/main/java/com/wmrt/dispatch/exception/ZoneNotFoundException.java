package com.wmrt.dispatch.exception;

/**
 * Thrown when a requested zone does not exist.
 */
public class ZoneNotFoundException extends RuntimeException {
    public ZoneNotFoundException(String message) {
        super(message);
    }
}
