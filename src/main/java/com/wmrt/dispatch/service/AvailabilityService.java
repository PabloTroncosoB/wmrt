package com.wmrt.dispatch.service;

import com.wmrt.dispatch.dto.AvailableDateResponse;
import com.wmrt.dispatch.dto.TimeBlockResponse;
import com.wmrt.dispatch.exception.TimeBlockNotFoundException;
import com.wmrt.dispatch.model.TimeBlock;
import com.wmrt.dispatch.model.Zone;
import com.wmrt.dispatch.repository.TimeBlockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for querying dispatch window availability.
 */
@Service
public class AvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(AvailabilityService.class);

    private final ZoneService zoneService;
    private final TimeBlockRepository timeBlockRepository;

    public AvailabilityService(ZoneService zoneService, TimeBlockRepository timeBlockRepository) {
        this.zoneService = zoneService;
        this.timeBlockRepository = timeBlockRepository;
    }

    /**
     * Returns dates with available (non-full) blocks for the given zone within the date range.
     * Defaults to today + 30 days if from/to are null.
     */
    public List<AvailableDateResponse> getAvailableDates(Long zoneId, String fromDate, String toDate) {
        Zone zone = zoneService.getZoneById(zoneId);

        LocalDate from = (fromDate != null) ? LocalDate.parse(fromDate) : LocalDate.now();
        LocalDate to   = (toDate   != null) ? LocalDate.parse(toDate)   : from.plusDays(30);

        log.debug("Querying available dates for zone={} from={} to={}", zoneId, from, to);

        List<LocalDate> availableDates = timeBlockRepository.findAvailableDatesByZone(zoneId, from, to);

        List<AvailableDateResponse> response = new ArrayList<>();
        for (LocalDate date : availableDates) {
            List<TimeBlock> blocks = timeBlockRepository.findByZoneAndDate(zoneId, date);
            long nonFullCount = blocks.stream().filter(b -> !b.isFull()).count();
            if (nonFullCount > 0) {
                response.add(new AvailableDateResponse(date, (int) nonFullCount, zone.getId(), zone.getName()));
            }
        }

        return response;
    }

    /**
     * Returns all time blocks for a given zone and date as DTOs.
     */
    public List<TimeBlockResponse> getTimeBlocksByZoneAndDate(Long zoneId, String date) {
        Zone zone = zoneService.getZoneById(zoneId);
        LocalDate localDate = LocalDate.parse(date);

        log.debug("Querying time blocks for zone={} date={}", zoneId, date);

        List<TimeBlock> blocks = timeBlockRepository.findByZoneAndDate(zoneId, localDate);
        return blocks.stream()
                .map(b -> toTimeBlockResponse(b, zone.getName()))
                .toList();
    }

    /**
     * Returns a single time block by its ID as a DTO, or throws TimeBlockNotFoundException.
     */
    public TimeBlockResponse getTimeBlockById(Long id) {
        log.debug("Fetching time block with id={}", id);
        TimeBlock block = timeBlockRepository.findById(id)
                .orElseThrow(() -> new TimeBlockNotFoundException("Time block not found with id: " + id));

        Zone zone = zoneService.getZoneById(block.getZoneId());
        return toTimeBlockResponse(block, zone.getName());
    }

    /**
     * Converts a TimeBlock domain object to a TimeBlockResponse DTO.
     */
    public TimeBlockResponse toTimeBlockResponse(TimeBlock block, String zoneName) {
        return new TimeBlockResponse(
                block.getId(),
                block.getAvailableDate(),
                block.getStartTime(),
                block.getEndTime(),
                block.getCost(),
                block.availableSlots(),
                block.getZoneId(),
                zoneName,
                block.isFull()
        );
    }
}
