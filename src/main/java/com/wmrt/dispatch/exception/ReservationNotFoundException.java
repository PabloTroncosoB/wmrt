package com.wmrt.dispatch.exception;

/**
 * Thrown when a requested reservation does not exist.
 */
public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(String message) {
        super(message);
    }
}
