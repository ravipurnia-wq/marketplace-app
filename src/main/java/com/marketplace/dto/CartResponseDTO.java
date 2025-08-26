package com.marketplace.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponseDTO {
    private String status;
    private String message;
    private String cartId;
    private String userId;
    private List<CartItemDTO> items;
    private BigDecimal total;
    private Integer totalItems;
    private Boolean empty;

    public CartResponseDTO() {}

    public CartResponseDTO(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public CartResponseDTO(String cartId, String userId, List<CartItemDTO> items, 
                          BigDecimal total, Integer totalItems, Boolean empty) {
        this.status = "success";
        this.cartId = cartId;
        this.userId = userId;
        this.items = items;
        this.total = total;
        this.totalItems = totalItems;
        this.empty = empty;
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

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CartItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemDTO> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public Boolean getEmpty() {
        return empty;
    }

    public void setEmpty(Boolean empty) {
        this.empty = empty;
    }
}