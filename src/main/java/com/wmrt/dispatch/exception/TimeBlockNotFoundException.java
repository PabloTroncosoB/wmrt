package com.wmrt.dispatch.exception;

/**
 * Thrown when a requested time block does not exist.
 */
public class TimeBlockNotFoundException extends RuntimeException {
    public TimeBlockNotFoundException(String message) {
        super(message);
    }
}
