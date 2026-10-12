package com.wmrt.dispatch.service;

import com.wmrt.dispatch.dto.ReservationRequest;
import com.wmrt.dispatch.dto.ReservationResponse;
import com.wmrt.dispatch.dto.TimeBlockResponse;
import com.wmrt.dispatch.exception.NoCapacityException;
import com.wmrt.dispatch.exception.PastTimeBlockException;
import com.wmrt.dispatch.exception.ReservationNotFoundException;
import com.wmrt.dispatch.model.Reservation;
import com.wmrt.dispatch.model.ReservationStatus;
import com.wmrt.dispatch.model.TimeBlock;
import com.wmrt.dispatch.model.Zone;
import com.wmrt.dispatch.repository.ReservationRepository;
import com.wmrt.dispatch.repository.TimeBlockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService")
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TimeBlockRepository timeBlockRepository;

    @Mock
    private ZoneService zoneService;

    @Mock
    private AvailabilityService availabilityService;

    @InjectMocks
    private ReservationService reservationService;

    private TimeBlock availableBlock;
    private TimeBlock fullBlock;
    private Reservation confirmedReservation;
    private TimeBlockResponse blockDto;
    private ReservationRequest request;

    @BeforeEach
    void setUp() {
        LocalDate futureDate = LocalDate.now().plusDays(2);

        availableBlock = new TimeBlock(5L, 2L, futureDate,
                "09:00", "12:00", 10, 3, 5990.0);

        fullBlock = new TimeBlock(6L, 2L, futureDate,
                "12:00", "14:00", 10, 10, 4990.0);

        confirmedReservation = new Reservation(
                1L, "ORD-001", 5L,
                "Juan Pérez", "Av. Providencia 1234",
                ReservationStatus.CONFIRMED,
                LocalDateTime.of(2026, 10, 9, 15, 0)
        );

        blockDto = new TimeBlockResponse(
                5L, futureDate,
                "09:00", "12:00", 5990.0, 7, 2L, "Centro", false
        );

        request = new ReservationRequest();
        request.setOrderId("ORD-001");
        request.setTimeBlockId(5L);
        request.setCustomerName("Juan Pérez");
        request.setAddress("Av. Providencia 1234");
    }

    // ── createReservation() ───────────────────────────────────────────────────

    @Test
    @DisplayName("createReservation() persists reservation and returns response when block has capacity")
    void createReservation_success() {
        when(reservationRepository.existsByOrderId("ORD-001")).thenReturn(false);
        when(timeBlockRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(availableBlock));
        when(reservationRepository.save(any())).thenReturn(confirmedReservation);
        when(availabilityService.getTimeBlockById(5L)).thenReturn(blockDto);

        ReservationResponse result = reservationService.createReservation(request);

        assertNotNull(result);
        assertEquals("ORD-001",    result.getOrderId());
        assertEquals("CONFIRMED",  result.getStatus());
        assertEquals(5L,           result.getTimeBlockId());

        verify(timeBlockRepository).incrementReservedCount(5L);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("createReservation() throws IllegalStateException when orderId already reserved")
    void createReservation_throwsWhenDuplicateOrder() {
        when(reservationRepository.existsByOrderId("ORD-001")).thenReturn(true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> reservationService.createReservation(request)
        );

        assertTrue(ex.getMessage().contains("ORD-001"));
        verify(timeBlockRepository, never()).findByIdForUpdate(any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("createReservation() throws PastTimeBlockException when time block is in the past")
    void createReservation_throwsWhenBlockIsInThePast() {
        TimeBlock pastBlock = new TimeBlock(7L, 2L, LocalDate.now().minusDays(1),
                "09:00", "12:00", 10, 0, 5990.0);

        when(reservationRepository.existsByOrderId("ORD-001")).thenReturn(false);
        when(timeBlockRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(pastBlock));

        PastTimeBlockException ex = assertThrows(PastTimeBlockException.class,
                () -> reservationService.createReservation(request));

        assertTrue(ex.getMessage().contains("Cannot reserve a time block in the past"));
        verify(timeBlockRepository, never()).incrementReservedCount(any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("createReservation() throws NoCapacityException when time block is full")
    void createReservation_throwsWhenBlockFull() {
        when(reservationRepository.existsByOrderId("ORD-001")).thenReturn(false);
        when(timeBlockRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(fullBlock));

        assertThrows(NoCapacityException.class,
                () -> reservationService.createReservation(request));

        verify(timeBlockRepository, never()).incrementReservedCount(any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("createReservation() throws NoCapacityException when time block not found")
    void createReservation_throwsWhenBlockNotFound() {
        when(reservationRepository.existsByOrderId("ORD-001")).thenReturn(false);
        when(timeBlockRepository.findByIdForUpdate(5L)).thenReturn(Optional.empty());

        assertThrows(NoCapacityException.class,
                () -> reservationService.createReservation(request));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("createReservation() increments reservedCount exactly once")
    void createReservation_incrementsCountOnce() {
        when(reservationRepository.existsByOrderId("ORD-001")).thenReturn(false);
        when(timeBlockRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(availableBlock));
        when(reservationRepository.save(any())).thenReturn(confirmedReservation);
        when(availabilityService.getTimeBlockById(5L)).thenReturn(blockDto);

        reservationService.createReservation(request);

        verify(timeBlockRepository, times(1)).incrementReservedCount(5L);
    }

    // ── cancelReservation() ───────────────────────────────────────────────────

    @Test
    @DisplayName("cancelReservation() updates status to CANCELLED and decrements count")
    void cancelReservation_success() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(confirmedReservation));
        when(availabilityService.getTimeBlockById(5L)).thenReturn(blockDto);

        ReservationResponse result = reservationService.cancelReservation(1L);

        assertEquals("CANCELLED", result.getStatus());
        verify(reservationRepository).updateStatus(1L, ReservationStatus.CANCELLED);
        verify(timeBlockRepository).decrementReservedCount(5L);
    }

    @Test
    @DisplayName("cancelReservation() throws ReservationNotFoundException when id does not exist")
    void cancelReservation_throwsWhenNotFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReservationNotFoundException.class,
                () -> reservationService.cancelReservation(99L));

        verify(reservationRepository, never()).updateStatus(any(), any());
        verify(timeBlockRepository, never()).decrementReservedCount(any());
    }

    @Test
    @DisplayName("cancelReservation() throws IllegalStateException when already cancelled")
    void cancelReservation_throwsWhenAlreadyCancelled() {
        Reservation cancelled = new Reservation(
                1L, "ORD-001", 5L, "Juan Pérez", "Av. Providencia 1234",
                ReservationStatus.CANCELLED,
                LocalDateTime.of(2026, 10, 9, 15, 0)
        );
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(cancelled));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> reservationService.cancelReservation(1L)
        );

        assertTrue(ex.getMessage().contains("already cancelled"));
        verify(reservationRepository, never()).updateStatus(any(), any());
        verify(timeBlockRepository, never()).decrementReservedCount(any());
    }

    @Test
    @DisplayName("cancelReservation() decrements reservedCount exactly once")
    void cancelReservation_decrementsCountOnce() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(confirmedReservation));
        when(availabilityService.getTimeBlockById(5L)).thenReturn(blockDto);

        reservationService.cancelReservation(1L);

        verify(timeBlockRepository, times(1)).decrementReservedCount(5L);
    }

    // ── getReservation() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getReservation() returns response when reservation exists")
    void getReservation_returnsResponse() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(confirmedReservation));
        when(availabilityService.getTimeBlockById(5L)).thenReturn(blockDto);

        ReservationResponse result = reservationService.getReservation(1L);

        assertEquals(1L,        result.getId());
        assertEquals("ORD-001", result.getOrderId());
        assertEquals("CONFIRMED", result.getStatus());
    }

    @Test
    @DisplayName("getReservation() throws ReservationNotFoundException when not found")
    void getReservation_throwsWhenNotFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        ReservationNotFoundException ex = assertThrows(
                ReservationNotFoundException.class,
                () -> reservationService.getReservation(99L)
        );

        assertTrue(ex.getMessage().contains("99"));
    }

    // ── getReservationByOrderId() ─────────────────────────────────────────────

    @Test
    @DisplayName("getReservationByOrderId() returns response when orderId exists")
    void getReservationByOrderId_returnsResponse() {
        when(reservationRepository.findByOrderId("ORD-001")).thenReturn(Optional.of(confirmedReservation));
        when(availabilityService.getTimeBlockById(5L)).thenReturn(blockDto);

        ReservationResponse result = reservationService.getReservationByOrderId("ORD-001");

        assertEquals("ORD-001", result.getOrderId());
    }

    @Test
    @DisplayName("getReservationByOrderId() throws ReservationNotFoundException when orderId not found")
    void getReservationByOrderId_throwsWhenNotFound() {
        when(reservationRepository.findByOrderId("ORD-XXX")).thenReturn(Optional.empty());

        assertThrows(ReservationNotFoundException.class,
                () -> reservationService.getReservationByOrderId("ORD-XXX"));
    }

    // ── getAllReservations() ──────────────────────────────────────────────────

    @Test
    @DisplayName("getAllReservations() returns list of responses for all reservations")
    void getAllReservations_returnsList() {
        Reservation second = new Reservation(
                2L, "ORD-002", 5L, "María López", "Calle Falsa 123",
                ReservationStatus.CONFIRMED, LocalDateTime.now()
        );
        when(reservationRepository.findAll()).thenReturn(List.of(confirmedReservation, second));
        when(availabilityService.getTimeBlockById(5L)).thenReturn(blockDto);

        List<ReservationResponse> result = reservationService.getAllReservations();

        assertEquals(2, result.size());
        assertEquals("ORD-001", result.get(0).getOrderId());
        assertEquals("ORD-002", result.get(1).getOrderId());
    }

    @Test
    @DisplayName("getAllReservations() returns empty list when no reservations exist")
    void getAllReservations_returnsEmptyList() {
        when(reservationRepository.findAll()).thenReturn(List.of());

        List<ReservationResponse> result = reservationService.getAllReservations();

        assertTrue(result.isEmpty());
    }
}
