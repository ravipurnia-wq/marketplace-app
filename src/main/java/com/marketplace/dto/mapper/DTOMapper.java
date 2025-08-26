package com.marketplace.dto.mapper;

import com.marketplace.dto.*;
import com.marketplace.model.*;
import java.util.List;
import java.util.stream.Collectors;

public class DTOMapper {

    // Product mapping
    public static ProductResponseDTO toProductResponseDTO(Product product) {
        if (product == null) return null;
        
        return new ProductResponseDTO(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getDescription(),
            product.getImageUrl(),
            null, // category field doesn't exist in Product model
            product.getPaypalButtonId()
        );
    }

    public static ProductListResponseDTO toProductListResponseDTO(List<Product> products) {
        List<ProductResponseDTO> productDTOs = products.stream()
                .map(DTOMapper::toProductResponseDTO)
                .collect(Collectors.toList());
        
        return new ProductListResponseDTO(productDTOs);
    }

    // Cart mapping
    public static CartItemDTO toCartItemDTO(CartItem item) {
        if (item == null) return null;
        
        return new CartItemDTO(
            item.getProductId(),
            item.getProductName(),
            item.getProductPrice(),
            item.getProductImageUrl(),
            item.getQuantity(),
            item.getSubtotal()
        );
    }

    public static CartResponseDTO toCartResponseDTO(Cart cart) {
        if (cart == null) return null;
        
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
                .map(DTOMapper::toCartItemDTO)
                .collect(Collectors.toList());
        
        return new CartResponseDTO(
            cart.getId(),
            cart.getUserId(),
            itemDTOs,
            cart.getTotal(),
            cart.getTotalItems(),
            cart.isEmpty()
        );
    }

    // Order mapping
    public static OrderResponseDTO toOrderResponseDTO(Order order) {
        if (order == null) return null;
        
        List<CartItemDTO> itemDTOs = order.getItems().stream()
                .map(DTOMapper::toCartItemDTO)
                .collect(Collectors.toList());
        
        return new OrderResponseDTO(
            order.getId(),
            order.getUserId(),
            itemDTOs,
            order.getTotalAmount(),
            order.getStatus(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            order.getShippingAddress(),
            "PayPal" // default payment method
        );
    }

    public static OrderListResponseDTO toOrderListResponseDTO(List<Order> orders) {
        List<OrderResponseDTO> orderDTOs = orders.stream()
                .map(DTOMapper::toOrderResponseDTO)
                .collect(Collectors.toList());
        
        return new OrderListResponseDTO(orderDTOs);
    }

    // Payment response mapping
    public static PaymentResponseDTO createPaymentResponseDTO(String status, String message, 
                                                             String paypalUrl, String orderId,
                                                             Product product) {
        PaymentResponseDTO dto = new PaymentResponseDTO(status, message, paypalUrl, orderId);
        if (product != null) {
            dto.setProductId(product.getId());
            dto.setProductName(product.getName());
            dto.setProductPrice(product.getPrice().toString());
            dto.setProductDescription(product.getDescription());
        }
        return dto;
    }

    public static CartPaymentResponseDTO createCartPaymentResponseDTO(String status, String paypalUrl, 
                                                                     String cartTotal, String tax,
                                                                     String finalTotal, Integer itemCount,
                                                                     String orderId) {
        CartPaymentResponseDTO dto = new CartPaymentResponseDTO(status, "success");
        dto.setPaypalUrl(paypalUrl);
        dto.setCartTotal(cartTotal);
        dto.setTax(tax);
        dto.setFinalTotal(finalTotal);
        dto.setItemCount(itemCount);
        dto.setOrderId(orderId);
        return dto;
    }
}