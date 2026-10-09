package com.wmrt.dispatch.controller;

import com.wmrt.dispatch.dto.ReservationRequest;
import com.wmrt.dispatch.dto.ReservationResponse;
import com.wmrt.dispatch.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for reservation management endpoints.
 */
@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * POST /api/v1/reservations
     * Creates a new reservation. Returns 201 Created.
     */
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/reservations
     * Returns all reservations.
     */
    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    /**
     * GET /api/v1/reservations/{id}
     * Returns a reservation by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservation(id));
    }

    /**
     * GET /api/v1/reservations/order/{orderId}
     * Returns a reservation by its order ID.
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ReservationResponse> getReservationByOrderId(@PathVariable String orderId) {
        return ResponseEntity.ok(reservationService.getReservationByOrderId(orderId));
    }

    /**
     * DELETE /api/v1/reservations/{id}
     * Cancels a reservation. Returns 200 OK with the updated reservation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }
}
