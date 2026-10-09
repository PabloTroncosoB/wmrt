package com.wmrt.dispatch.controller;

import com.wmrt.dispatch.model.Zone;
import com.wmrt.dispatch.service.ZoneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for zone endpoints.
 */
@RestController
@RequestMapping("/api/v1/zones")
public class ZoneController {

    private final ZoneService zoneService;

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    /**
     * GET /api/v1/zones
     * Returns all dispatch zones.
     */
    @GetMapping
    public ResponseEntity<List<Zone>> getAllZones() {
        return ResponseEntity.ok(zoneService.getAllZones());
    }

    /**
     * GET /api/v1/zones/{id}
     * Returns a single zone by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Zone> getZoneById(@PathVariable Long id) {
        return ResponseEntity.ok(zoneService.getZoneById(id));
    }
}
