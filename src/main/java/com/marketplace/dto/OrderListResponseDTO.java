package com.marketplace.dto;

import java.util.List;

public class OrderListResponseDTO {
    private String status;
    private String message;
    private List<OrderResponseDTO> orders;
    private Integer totalCount;
    private Integer currentPage;
    private Integer totalPages;
    private Long pendingCount;
    private Long confirmedCount;
    private Long shippedCount;

    public OrderListResponseDTO() {}

    public OrderListResponseDTO(List<OrderResponseDTO> orders) {
        this.status = "success";
        this.orders = orders;
        this.totalCount = orders.size();
    }

    public OrderListResponseDTO(String status, String message) {
        this.status = status;
        this.message = message;
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

    public List<OrderResponseDTO> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderResponseDTO> orders) {
        this.orders = orders;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public Long getConfirmedCount() {
        return confirmedCount;
    }

    public void setConfirmedCount(Long confirmedCount) {
        this.confirmedCount = confirmedCount;
    }

    public Long getShippedCount() {
        return shippedCount;
    }

    public void setShippedCount(Long shippedCount) {
        this.shippedCount = shippedCount;
    }
}