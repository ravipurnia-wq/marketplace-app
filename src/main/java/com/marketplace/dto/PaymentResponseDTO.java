package com.marketplace.dto;

public class PaymentResponseDTO {
    private String status;
    private String message;
    private String paypalUrl;
    private String orderId;
    private String productId;
    private String productName;
    private String productPrice;
    private String productDescription;

    public PaymentResponseDTO() {}

    public PaymentResponseDTO(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public PaymentResponseDTO(String status, String message, String paypalUrl, String orderId) {
        this.status = status;
        this.message = message;
        this.paypalUrl = paypalUrl;
        this.orderId = orderId;
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

    public String getPaypalUrl() {
        return paypalUrl;
    }

    public void setPaypalUrl(String paypalUrl) {
        this.paypalUrl = paypalUrl;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }
}