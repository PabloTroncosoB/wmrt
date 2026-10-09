package com.wmrt.dispatch.exception;

/**
 * Thrown when a time block has no available capacity for a new reservation.
 */
public class NoCapacityException extends RuntimeException {
    public NoCapacityException(String message) {
        super(message);
    }
}
