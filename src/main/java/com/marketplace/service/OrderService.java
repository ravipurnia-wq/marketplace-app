package com.marketplace.service;

import com.marketplace.model.*;
import com.marketplace.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CartService cartService;
    
    public Order createOrderFromCart() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User must be logged in to place an order");
        }
        
        Cart cart = cartService.getCurrentUserCart();
        if (cart.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        Order order = new Order(
            currentUser.getId(),
            currentUser.getEmail(),
            currentUser.getFullName(),
            cart.getItems(),
            cart.getTotal()
        );
        
        order.setShippingAddress(currentUser.getAddress());
        order.setPhoneNumber(currentUser.getPhoneNumber());
        
        Order savedOrder = orderRepository.save(order);
        
        cartService.clearCurrentUserCart();
        
        return savedOrder;
    }
    
    public Order updateOrderStatus(String orderId, OrderStatus status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("Order not found");
        }
        
        Order order = orderOpt.get();
        order.setStatus(status);
        return orderRepository.save(order);
    }
    
    public Order addPaymentInfo(String orderId, String paypalTransactionId, String paypalPaymentId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("Order not found");
        }
        
        Order order = orderOpt.get();
        order.setPaypalTransactionId(paypalTransactionId);
        order.setPaypalPaymentId(paypalPaymentId);
        order.setStatus(OrderStatus.CONFIRMED);
        return orderRepository.save(order);
    }
    
    public List<Order> getCurrentUserOrders() {
        String userId = userService.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User must be logged in");
        }
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public Page<Order> getCurrentUserOrders(Pageable pageable) {
        String userId = userService.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User must be logged in");
        }
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    public Optional<Order> getOrderById(String orderId) {
        return orderRepository.findById(orderId);
    }
    
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    public long getOrderCountByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }
    
    public Order updateOrder(Order order) {
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }
}