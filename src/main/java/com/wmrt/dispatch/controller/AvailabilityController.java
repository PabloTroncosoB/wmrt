package com.wmrt.dispatch.controller;

import com.wmrt.dispatch.dto.AvailableDateResponse;
import com.wmrt.dispatch.dto.TimeBlockResponse;
import com.wmrt.dispatch.service.AvailabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for dispatch window availability endpoints.
 */
@RestController
@RequestMapping("/api/v1/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    /**
     * GET /api/v1/availability/zones/{zoneId}/dates?from=YYYY-MM-DD&to=YYYY-MM-DD
     * Returns dates that have at least one available (non-full) time block for the given zone.
     */
    @GetMapping("/zones/{zoneId}/dates")
    public ResponseEntity<List<AvailableDateResponse>> getAvailableDates(
            @PathVariable Long zoneId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(availabilityService.getAvailableDates(zoneId, from, to));
    }

    /**
     * GET /api/v1/availability/zones/{zoneId}/blocks?date=YYYY-MM-DD
     * Returns all time blocks for the given zone and date.
     */
    @GetMapping("/zones/{zoneId}/blocks")
    public ResponseEntity<List<TimeBlockResponse>> getTimeBlocksByZoneAndDate(
            @PathVariable Long zoneId,
            @RequestParam String date) {
        return ResponseEntity.ok(availabilityService.getTimeBlocksByZoneAndDate(zoneId, date));
    }

    /**
     * GET /api/v1/availability/blocks/{blockId}
     * Returns a single time block by its ID.
     */
    @GetMapping("/blocks/{blockId}")
    public ResponseEntity<TimeBlockResponse> getTimeBlockById(@PathVariable Long blockId) {
        return ResponseEntity.ok(availabilityService.getTimeBlockById(blockId));
    }
}
