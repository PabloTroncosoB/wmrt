package com.wmrt.dispatch.service;

import com.wmrt.dispatch.dto.AvailableDateResponse;
import com.wmrt.dispatch.dto.TimeBlockResponse;
import com.wmrt.dispatch.exception.TimeBlockNotFoundException;
import com.wmrt.dispatch.exception.ZoneNotFoundException;
import com.wmrt.dispatch.model.TimeBlock;
import com.wmrt.dispatch.model.Zone;
import com.wmrt.dispatch.repository.TimeBlockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AvailabilityService")
class AvailabilityServiceTest {

    @Mock
    private ZoneService zoneService;

    @Mock
    private TimeBlockRepository timeBlockRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private Zone zoneCentro;
    private TimeBlock blockAvailable;
    private TimeBlock blockFull;

    @BeforeEach
    void setUp() {
        zoneCentro = new Zone(2L, "Metropolitana Centro", "RM Centro", "Santiago, Providencia");

        blockAvailable = new TimeBlock(5L, 2L, LocalDate.of(2026, 10, 13),
                "09:00", "12:00", 10, 3, 5990.0);  // 7 slots left

        blockFull = new TimeBlock(6L, 2L, LocalDate.of(2026, 10, 13),
                "12:00", "14:00", 10, 10, 4990.0); // full
    }

    // ── getAvailableDates() ───────────────────────────────────────────────────

    @Test
    @DisplayName("getAvailableDates() returns dates with at least one non-full block")
    void getAvailableDates_returnsDatesWithAvailableBlocks() {
        LocalDate date = LocalDate.of(2026, 10, 13);

        when(zoneService.getZoneById(2L)).thenReturn(zoneCentro);
        when(timeBlockRepository.findAvailableDatesByZone(eq(2L), any(), any()))
                .thenReturn(List.of(date));
        when(timeBlockRepository.findByZoneAndDate(2L, date))
                .thenReturn(List.of(blockAvailable, blockFull));

        List<AvailableDateResponse> result = availabilityService.getAvailableDates(2L, "2026-10-13", "2026-10-14");

        assertEquals(1, result.size());
        assertEquals(date, result.get(0).getDate());
        assertEquals(1, result.get(0).getAvailableBlockCount()); // only blockAvailable is non-full
        assertEquals(2L, result.get(0).getZoneId());
        assertEquals("Centro", result.get(0).getZoneName());
    }

    @Test
    @DisplayName("getAvailableDates() excludes dates where all blocks are full")
    void getAvailableDates_excludesFullDates() {
        LocalDate date = LocalDate.of(2026, 10, 13);

        when(zoneService.getZoneById(2L)).thenReturn(zoneCentro);
        when(timeBlockRepository.findAvailableDatesByZone(eq(2L), any(), any()))
                .thenReturn(List.of(date));
        when(timeBlockRepository.findByZoneAndDate(2L, date))
                .thenReturn(List.of(blockFull)); // only full block

        List<AvailableDateResponse> result = availabilityService.getAvailableDates(2L, "2026-10-13", "2026-10-14");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAvailableDates() uses today as default from-date when null")
    void getAvailableDates_defaultsFromDateToToday() {
        when(zoneService.getZoneById(2L)).thenReturn(zoneCentro);
        when(timeBlockRepository.findAvailableDatesByZone(eq(2L), eq(LocalDate.now()), any()))
                .thenReturn(List.of());

        List<AvailableDateResponse> result = availabilityService.getAvailableDates(2L, null, null);

        assertTrue(result.isEmpty());
        verify(timeBlockRepository).findAvailableDatesByZone(eq(2L), eq(LocalDate.now()), any());
    }

    @Test
    @DisplayName("getAvailableDates() throws ZoneNotFoundException for unknown zone")
    void getAvailableDates_throwsWhenZoneNotFound() {
        when(zoneService.getZoneById(99L)).thenThrow(new ZoneNotFoundException("Zone not found with id: 99"));

        assertThrows(ZoneNotFoundException.class,
                () -> availabilityService.getAvailableDates(99L, null, null));
    }

    @Test
    @DisplayName("getAvailableDates() returns empty list when no dates available")
    void getAvailableDates_returnsEmptyWhenNoDates() {
        when(zoneService.getZoneById(2L)).thenReturn(zoneCentro);
        when(timeBlockRepository.findAvailableDatesByZone(eq(2L), any(), any()))
                .thenReturn(List.of());

        List<AvailableDateResponse> result = availabilityService.getAvailableDates(2L, "2026-10-13", "2026-10-20");

        assertTrue(result.isEmpty());
    }

    // ── getTimeBlocksByZoneAndDate() ──────────────────────────────────────────

    @Test
    @DisplayName("getTimeBlocksByZoneAndDate() returns mapped DTOs for given zone and date")
    void getTimeBlocksByZoneAndDate_returnsMappedDtos() {
        when(zoneService.getZoneById(2L)).thenReturn(zoneCentro);
        when(timeBlockRepository.findByZoneAndDate(2L, LocalDate.of(2026, 10, 13)))
                .thenReturn(List.of(blockAvailable, blockFull));

        List<TimeBlockResponse> result = availabilityService
                .getTimeBlocksByZoneAndDate(2L, "2026-10-13");

        assertEquals(2, result.size());

        TimeBlockResponse first = result.get(0);
        assertEquals(5L,     first.getId());
        assertEquals("09:00", first.getStartTime());
        assertEquals("12:00", first.getEndTime());
        assertEquals(5990.0, first.getCost());
        assertEquals(7,      first.getAvailableSlots());
        assertFalse(first.isFull());

        TimeBlockResponse second = result.get(1);
        assertEquals(6L, second.getId());
        assertTrue(second.isFull());
        assertEquals(0, second.getAvailableSlots());
    }

    @Test
    @DisplayName("getTimeBlocksByZoneAndDate() returns empty list when no blocks for that date")
    void getTimeBlocksByZoneAndDate_returnsEmptyWhenNone() {
        when(zoneService.getZoneById(2L)).thenReturn(zoneCentro);
        when(timeBlockRepository.findByZoneAndDate(2L, LocalDate.of(2026, 10, 13)))
                .thenReturn(List.of());

        List<TimeBlockResponse> result = availabilityService
                .getTimeBlocksByZoneAndDate(2L, "2026-10-13");

        assertTrue(result.isEmpty());
    }

    // ── getTimeBlockById() ────────────────────────────────────────────────────

    @Test
    @DisplayName("getTimeBlockById() returns DTO when block exists")
    void getTimeBlockById_returnsDto() {
        when(timeBlockRepository.findById(5L)).thenReturn(Optional.of(blockAvailable));
        when(zoneService.getZoneById(2L)).thenReturn(zoneCentro);

        TimeBlockResponse result = availabilityService.getTimeBlockById(5L);

        assertEquals(5L,       result.getId());
        assertEquals("09:00",  result.getStartTime());
        assertEquals("Centro", result.getZoneName());
        assertEquals(7,        result.getAvailableSlots());
    }

    @Test
    @DisplayName("getTimeBlockById() throws TimeBlockNotFoundException when not found")
    void getTimeBlockById_throwsWhenNotFound() {
        when(timeBlockRepository.findById(999L)).thenReturn(Optional.empty());

        TimeBlockNotFoundException ex = assertThrows(
                TimeBlockNotFoundException.class,
                () -> availabilityService.getTimeBlockById(999L)
        );

        assertTrue(ex.getMessage().contains("999"));
    }

    // ── toTimeBlockResponse() ─────────────────────────────────────────────────

    @Test
    @DisplayName("toTimeBlockResponse() correctly maps all fields")
    void toTimeBlockResponse_mapsAllFields() {
        TimeBlockResponse dto = availabilityService.toTimeBlockResponse(blockAvailable, "Centro");

        assertEquals(5L,                               dto.getId());
        assertEquals(LocalDate.of(2026, 10, 13),       dto.getDate());
        assertEquals("09:00",                          dto.getStartTime());
        assertEquals("12:00",                          dto.getEndTime());
        assertEquals(5990.0,                           dto.getCost());
        assertEquals(7,                                dto.getAvailableSlots());
        assertEquals(2L,                               dto.getZoneId());
        assertEquals("Centro",                         dto.getZoneName());
        assertFalse(dto.isFull());
    }
}
