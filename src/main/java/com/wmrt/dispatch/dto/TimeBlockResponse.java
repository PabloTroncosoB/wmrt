package com.wmrt.dispatch.dto;

import java.time.LocalDate;

/**
 * Response DTO for a single time block (ventana de despacho).
 */
public class TimeBlockResponse {

    private Long id;
    private LocalDate date;
    private String startTime;
    private String endTime;
    private double cost;
    private int availableSlots;
    private Long zoneId;
    private String zoneName;
    private boolean full;

    public TimeBlockResponse() {}

    public TimeBlockResponse(Long id, LocalDate date, String startTime, String endTime,
                             double cost, int availableSlots, Long zoneId, String zoneName, boolean full) {
        this.id = id;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.cost = cost;
        this.availableSlots = availableSlots;
        this.zoneId = zoneId;
        this.zoneName = zoneName;
        this.full = full;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    public int getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(int availableSlots) { this.availableSlots = availableSlots; }

    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }

    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }

    public boolean isFull() { return full; }
    public void setFull(boolean full) { this.full = full; }
}
