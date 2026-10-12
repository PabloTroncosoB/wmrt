package com.wmrt.dispatch.service;

import com.wmrt.dispatch.exception.ZoneNotFoundException;
import com.wmrt.dispatch.model.Zone;
import com.wmrt.dispatch.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ZoneService")
class ZoneServiceTest {

    @Mock
    private ZoneRepository zoneRepository;

    @InjectMocks
    private ZoneService zoneService;

    private Zone zoneNorte;
    private Zone zoneCentro;

    @BeforeEach
    void setUp() {
        zoneNorte  = new Zone(1L, "Metropolitana Norte",  "RM Norte",  "Huechuraba, Quilicura");
        zoneCentro = new Zone(2L, "Metropolitana Centro", "RM Centro", "Santiago, Providencia");
    }

    // ── getAllZones() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllZones() returns all zones from repository")
    void getAllZones_returnsAllZones() {
        when(zoneRepository.findAll()).thenReturn(List.of(zoneNorte, zoneCentro));

        List<Zone> result = zoneService.getAllZones();

        assertEquals(2, result.size());
        assertEquals("Metropolitana Norte",  result.get(0).getName());
        assertEquals("Metropolitana Centro", result.get(1).getName());
        verify(zoneRepository).findAll();
    }

    @Test
    @DisplayName("getAllZones() returns empty list when no zones exist")
    void getAllZones_returnsEmptyList() {
        when(zoneRepository.findAll()).thenReturn(List.of());

        List<Zone> result = zoneService.getAllZones();

        assertTrue(result.isEmpty());
        verify(zoneRepository).findAll();
    }

    // ── getZoneById() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getZoneById() returns zone when it exists")
    void getZoneById_returnsZone() {
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zoneNorte));

        Zone result = zoneService.getZoneById(1L);

        assertEquals(1L,      result.getId());
        assertEquals("Metropolitana Norte", result.getName());
        verify(zoneRepository).findById(1L);
    }

    @Test
    @DisplayName("getZoneById() throws ZoneNotFoundException when zone does not exist")
    void getZoneById_throwsWhenNotFound() {
        when(zoneRepository.findById(99L)).thenReturn(Optional.empty());

        ZoneNotFoundException ex = assertThrows(
                ZoneNotFoundException.class,
                () -> zoneService.getZoneById(99L)
        );

        assertTrue(ex.getMessage().contains("99"));
        verify(zoneRepository).findById(99L);
    }
}
