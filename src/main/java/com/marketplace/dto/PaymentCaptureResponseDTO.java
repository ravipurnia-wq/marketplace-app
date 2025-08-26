package com.marketplace.dto;

public class PaymentCaptureResponseDTO {
    private String status;
    private String message;
    private String orderId;
    private String orderStatus;

    public PaymentCaptureResponseDTO() {}

    public PaymentCaptureResponseDTO(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public PaymentCaptureResponseDTO(String status, String message, String orderId, String orderStatus) {
        this.status = status;
        this.message = message;
        this.orderId = orderId;
        this.orderStatus = orderStatus;
    }

    // Getters and setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}