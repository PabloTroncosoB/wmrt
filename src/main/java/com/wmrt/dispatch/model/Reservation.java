package com.wmrt.dispatch.model;

import java.time.LocalDateTime;

/**
 * Represents a customer reservation for a dispatch time block.
 */
public class Reservation {

    private Long id;
    private String orderId;
    private Long timeBlockId;
    private String customerName;
    private String address;
    private ReservationStatus status;
    private LocalDateTime createdAt;

    public Reservation() {}

    public Reservation(Long id, String orderId, Long timeBlockId, String customerName,
                       String address, ReservationStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.timeBlockId = timeBlockId;
        this.customerName = customerName;
        this.address = address;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Long getTimeBlockId() { return timeBlockId; }
    public void setTimeBlockId(Long timeBlockId) { this.timeBlockId = timeBlockId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Reservation{id=" + id + ", orderId='" + orderId + "', status=" + status + "}";
    }
}
