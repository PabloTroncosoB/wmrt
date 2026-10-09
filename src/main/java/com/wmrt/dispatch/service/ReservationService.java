package com.wmrt.dispatch.service;

import com.wmrt.dispatch.dto.ReservationRequest;
import com.wmrt.dispatch.dto.ReservationResponse;
import com.wmrt.dispatch.dto.TimeBlockResponse;
import com.wmrt.dispatch.exception.NoCapacityException;
import com.wmrt.dispatch.exception.ReservationNotFoundException;
import com.wmrt.dispatch.model.Reservation;
import com.wmrt.dispatch.model.ReservationStatus;
import com.wmrt.dispatch.model.TimeBlock;
import com.wmrt.dispatch.model.Zone;
import com.wmrt.dispatch.repository.ReservationRepository;
import com.wmrt.dispatch.repository.TimeBlockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing dispatch window reservations.
 * Uses a synchronized lock to prevent over-booking in single-process deployments.
 */
@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    /** Lock object used to serialize the check-then-act sequence for reservation creation. */
    private final Object lock = new Object();

    private final ReservationRepository reservationRepository;
    private final TimeBlockRepository timeBlockRepository;
    private final ZoneService zoneService;
    private final AvailabilityService availabilityService;

    public ReservationService(ReservationRepository reservationRepository,
                              TimeBlockRepository timeBlockRepository,
                              ZoneService zoneService,
                              AvailabilityService availabilityService) {
        this.reservationRepository = reservationRepository;
        this.timeBlockRepository = timeBlockRepository;
        this.zoneService = zoneService;
        this.availabilityService = availabilityService;
    }

    /**
     * Creates a new reservation.
     * <p>
     * Steps:
     * 1. Check that the orderId has no existing reservation (idempotency guard).
     * 2. Inside a synchronized block: re-fetch the time block and verify capacity,
     *    increment reserved_count, then persist the reservation.
     * </p>
     */
    public ReservationResponse createReservation(ReservationRequest req) {
        // Step 1: Idempotency check (outside lock is fine for early rejection)
        if (reservationRepository.existsByOrderId(req.getOrderId())) {
            throw new IllegalStateException("Order already has a reservation: " + req.getOrderId());
        }

        Reservation saved;

        // Step 2: Synchronized check-then-act to prevent over-booking
        synchronized (lock) {
            TimeBlock block = timeBlockRepository.findByIdForUpdate(req.getTimeBlockId())
                    .orElseThrow(() -> new NoCapacityException(
                            "Time block not found with id: " + req.getTimeBlockId()));

            if (block.isFull()) {
                throw new NoCapacityException(
                        "Time block " + req.getTimeBlockId() + " has no available capacity");
            }

            timeBlockRepository.incrementReservedCount(block.getId());

            Reservation reservation = new Reservation(
                    null,
                    req.getOrderId(),
                    req.getTimeBlockId(),
                    req.getCustomerName(),
                    req.getAddress(),
                    ReservationStatus.CONFIRMED,
                    LocalDateTime.now()
            );
            saved = reservationRepository.save(reservation);
        }

        log.debug("Created reservation id={} for orderId={}", saved.getId(), saved.getOrderId());
        return buildReservationResponse(saved);
    }

    /**
     * Cancels an existing reservation and frees the time block slot.
     */
    public ReservationResponse cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + id));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Reservation " + id + " is already cancelled");
        }

        reservationRepository.updateStatus(id, ReservationStatus.CANCELLED);
        timeBlockRepository.decrementReservedCount(reservation.getTimeBlockId());

        reservation.setStatus(ReservationStatus.CANCELLED);
        log.debug("Cancelled reservation id={}", id);
        return buildReservationResponse(reservation);
    }

    /**
     * Returns a reservation response by its ID.
     */
    public ReservationResponse getReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + id));
        return buildReservationResponse(reservation);
    }

    /**
     * Returns a reservation response by its order ID.
     */
    public ReservationResponse getReservationByOrderId(String orderId) {
        Reservation reservation = reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found for orderId: " + orderId));
        return buildReservationResponse(reservation);
    }

    /**
     * Returns all reservations.
     */
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::buildReservationResponse)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private ReservationResponse buildReservationResponse(Reservation r) {
        TimeBlockResponse timeBlockResponse = availabilityService.getTimeBlockById(r.getTimeBlockId());
        return new ReservationResponse(
                r.getId(),
                r.getOrderId(),
                r.getTimeBlockId(),
                r.getCustomerName(),
                r.getAddress(),
                r.getStatus().name(),
                r.getCreatedAt().toString(),
                timeBlockResponse
        );
    }
}
