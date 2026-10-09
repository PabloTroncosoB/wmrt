package com.wmrt.dispatch.model;

import java.time.LocalDate;

/**
 * Represents a time block (ventana de despacho) for a given zone and date.
 * Tracks capacity and current reservation count.
 */
public class TimeBlock {

    private Long id;
    private Long zoneId;
    private LocalDate availableDate;
    private String startTime;
    private String endTime;
    private int capacity;
    private int reservedCount;
    private double cost;

    public TimeBlock() {}

    public TimeBlock(Long id, Long zoneId, LocalDate availableDate, String startTime,
                     String endTime, int capacity, int reservedCount, double cost) {
        this.id = id;
        this.zoneId = zoneId;
        this.availableDate = availableDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
        this.reservedCount = reservedCount;
        this.cost = cost;
    }

    /**
     * Returns the number of slots still available for reservation.
     */
    public int availableSlots() {
        return Math.max(0, capacity - reservedCount);
    }

    /**
     * Returns true if no capacity remains.
     */
    public boolean isFull() {
        return reservedCount >= capacity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }

    public LocalDate getAvailableDate() { return availableDate; }
    public void setAvailableDate(LocalDate availableDate) { this.availableDate = availableDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getReservedCount() { return reservedCount; }
    public void setReservedCount(int reservedCount) { this.reservedCount = reservedCount; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    @Override
    public String toString() {
        return "TimeBlock{id=" + id + ", zoneId=" + zoneId + ", date=" + availableDate
                + ", " + startTime + "-" + endTime + ", slots=" + availableSlots() + "/" + capacity + "}";
    }
}
