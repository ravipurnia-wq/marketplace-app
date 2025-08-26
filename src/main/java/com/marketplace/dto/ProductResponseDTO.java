package com.marketplace.dto;

import java.math.BigDecimal;

public class ProductResponseDTO {
    private String id;
    private String name;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    private String category;
    private String paypalButtonId;
    private String status;
    private String message;

    public ProductResponseDTO() {}

    public ProductResponseDTO(String id, String name, BigDecimal price, String description, 
                             String imageUrl, String category, String paypalButtonId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.paypalButtonId = paypalButtonId;
        this.status = "success";
    }

    public ProductResponseDTO(String status, String message) {
        this.status = status;
        this.message = message;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPaypalButtonId() {
        return paypalButtonId;
    }

    public void setPaypalButtonId(String paypalButtonId) {
        this.paypalButtonId = paypalButtonId;
    }

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
}