package com.marketplace.dto;

public class ErrorResponseDTO extends BaseResponseDTO {
    private String errorCode;
    private String details;

    public ErrorResponseDTO() {
        super();
        this.status = "error";
    }

    public ErrorResponseDTO(String message) {
        super("error", message);
    }

    public ErrorResponseDTO(String message, String errorCode) {
        super("error", message);
        this.errorCode = errorCode;
    }

    public ErrorResponseDTO(String message, String errorCode, String details) {
        super("error", message);
        this.errorCode = errorCode;
        this.details = details;
    }

    // Getters and setters
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}