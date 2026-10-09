package com.wmrt.dispatch.dto;

/**
 * Response DTO for a reservation, including embedded time block details.
 */
public class ReservationResponse {

    private Long id;
    private String orderId;
    private Long timeBlockId;
    private String customerName;
    private String address;
    private String status;
    private String createdAt;
    private TimeBlockResponse timeBlock;

    public ReservationResponse() {}

    public ReservationResponse(Long id, String orderId, Long timeBlockId, String customerName,
                               String address, String status, String createdAt, TimeBlockResponse timeBlock) {
        this.id = id;
        this.orderId = orderId;
        this.timeBlockId = timeBlockId;
        this.customerName = customerName;
        this.address = address;
        this.status = status;
        this.createdAt = createdAt;
        this.timeBlock = timeBlock;
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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public TimeBlockResponse getTimeBlock() { return timeBlock; }
    public void setTimeBlock(TimeBlockResponse timeBlock) { this.timeBlock = timeBlock; }
}
