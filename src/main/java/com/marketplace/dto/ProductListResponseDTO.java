package com.marketplace.dto;

import java.util.List;

public class ProductListResponseDTO {
    private String status;
    private String message;
    private List<ProductResponseDTO> products;
    private Integer totalCount;
    private Integer currentPage;
    private Integer totalPages;

    public ProductListResponseDTO() {}

    public ProductListResponseDTO(List<ProductResponseDTO> products) {
        this.status = "success";
        this.products = products;
        this.totalCount = products.size();
    }

    public ProductListResponseDTO(String status, String message) {
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

    public List<ProductResponseDTO> getProducts() {
        return products;
    }

    public void setProducts(List<ProductResponseDTO> products) {
        this.products = products;
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
}