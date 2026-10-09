package com.wmrt.dispatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a reservation.
 */
public class ReservationRequest {

    @NotBlank(message = "orderId is required")
    private String orderId;

    @NotNull(message = "timeBlockId is required")
    private Long timeBlockId;

    @NotBlank(message = "customerName is required")
    private String customerName;

    @NotBlank(message = "address is required")
    private String address;

    public ReservationRequest() {}

    public ReservationRequest(String orderId, Long timeBlockId, String customerName, String address) {
        this.orderId = orderId;
        this.timeBlockId = timeBlockId;
        this.customerName = customerName;
        this.address = address;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Long getTimeBlockId() { return timeBlockId; }
    public void setTimeBlockId(Long timeBlockId) { this.timeBlockId = timeBlockId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
