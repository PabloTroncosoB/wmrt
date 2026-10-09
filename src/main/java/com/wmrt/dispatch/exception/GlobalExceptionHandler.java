package com.wmrt.dispatch.exception;

import com.wmrt.dispatch.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Global exception handler that converts domain exceptions into standardized HTTP error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ZoneNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleZoneNotFound(ZoneNotFoundException ex,
                                                             HttpServletRequest request) {
        log.debug("ZoneNotFoundException: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Zone Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(TimeBlockNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTimeBlockNotFound(TimeBlockNotFoundException ex,
                                                                  HttpServletRequest request) {
        log.debug("TimeBlockNotFoundException: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Time Block Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReservationNotFound(ReservationNotFoundException ex,
                                                                    HttpServletRequest request) {
        log.debug("ReservationNotFoundException: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Reservation Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(NoCapacityException.class)
    public ResponseEntity<ErrorResponse> handleNoCapacity(NoCapacityException ex,
                                                          HttpServletRequest request) {
        log.debug("NoCapacityException: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "No Capacity Available", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex,
                                                            HttpServletRequest request) {
        log.debug("IllegalStateException: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        String fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.debug("Validation error: {}", fieldErrors);
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Failed", fieldErrors, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred", request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error,
                                                        String message, String path) {
        ErrorResponse body = new ErrorResponse(
                Instant.now().toString(),
                status.value(),
                error,
                message,
                path
        );
        return ResponseEntity.status(status).body(body);
    }
}
