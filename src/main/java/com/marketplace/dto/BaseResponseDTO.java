package com.marketplace.dto;

import java.time.LocalDateTime;

public abstract class BaseResponseDTO {
    protected String status;
    protected String message;
    protected LocalDateTime timestamp;

    public BaseResponseDTO() {
        this.timestamp = LocalDateTime.now();
    }

    public BaseResponseDTO(String status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}