package com.wmrt.dispatch.dto;

import java.time.LocalDate;

/**
 * Response DTO for a date that has at least one available (non-full) time block.
 */
public class AvailableDateResponse {

    private LocalDate date;
    private int availableBlockCount;
    private Long zoneId;
    private String zoneName;

    public AvailableDateResponse() {}

    public AvailableDateResponse(LocalDate date, int availableBlockCount, Long zoneId, String zoneName) {
        this.date = date;
        this.availableBlockCount = availableBlockCount;
        this.zoneId = zoneId;
        this.zoneName = zoneName;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getAvailableBlockCount() { return availableBlockCount; }
    public void setAvailableBlockCount(int availableBlockCount) {
        this.availableBlockCount = availableBlockCount;
    }

    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }

    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }
}
