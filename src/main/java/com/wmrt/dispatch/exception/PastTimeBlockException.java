package com.wmrt.dispatch.exception;

/**
 * Thrown when attempting to create a reservation for a time block that is in the past.
 */
public class PastTimeBlockException extends RuntimeException {

    public PastTimeBlockException(String message) {
        super(message);
    }
}
