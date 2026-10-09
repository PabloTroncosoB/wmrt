package com.wmrt.dispatch.service;

import com.wmrt.dispatch.exception.ZoneNotFoundException;
import com.wmrt.dispatch.model.Zone;
import com.wmrt.dispatch.repository.ZoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for zone management.
 */
@Service
public class ZoneService {

    private static final Logger log = LoggerFactory.getLogger(ZoneService.class);

    private final ZoneRepository zoneRepository;

    public ZoneService(ZoneRepository zoneRepository) {
        this.zoneRepository = zoneRepository;
    }

    /**
     * Returns all zones.
     */
    public List<Zone> getAllZones() {
        log.debug("Fetching all zones");
        return zoneRepository.findAll();
    }

    /**
     * Returns a zone by its ID or throws ZoneNotFoundException.
     */
    public Zone getZoneById(Long id) {
        log.debug("Fetching zone with id={}", id);
        return zoneRepository.findById(id)
                .orElseThrow(() -> new ZoneNotFoundException("Zone not found with id: " + id));
    }
}
