package com.marketplace.controller;

import com.marketplace.dto.*;
import com.marketplace.dto.mapper.DTOMapper;
import com.marketplace.model.Order;
import com.marketplace.model.OrderStatus;
import com.marketplace.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping
    public String viewOrders(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderService.getCurrentUserOrders(pageable);
        model.addAttribute("orders", orders);
        return "orders";
    }
    
    @GetMapping("/{orderId}")
    public String viewOrderDetails(@PathVariable String orderId, Model model) {
        Optional<Order> orderOpt = orderService.getOrderById(orderId);
        if (!orderOpt.isPresent()) {
            return "redirect:/orders";
        }
        
        model.addAttribute("order", orderOpt.get());
        return "order-details";
    }
    
    @PostMapping("/create")
    public String createOrder(Model model) {
        try {
            Order order = orderService.createOrderFromCart();
            return "redirect:/orders/" + order.getId();
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String adminOrders(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size,
                             Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderService.getAllOrders(pageable);
        model.addAttribute("orders", orders);
        
        // Add order statistics
        model.addAttribute("pendingCount", orderService.getOrderCountByStatus(OrderStatus.PENDING));
        model.addAttribute("confirmedCount", orderService.getOrderCountByStatus(OrderStatus.CONFIRMED));
        model.addAttribute("shippedCount", orderService.getOrderCountByStatus(OrderStatus.SHIPPED));
        
        return "admin-orders";
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/{orderId}/status")
    public String updateOrderStatus(@PathVariable String orderId, 
                                  @RequestParam OrderStatus status) {
        orderService.updateOrderStatus(orderId, status);
        return "redirect:/orders/admin";
    }
}

@RestController
@RequestMapping("/api/orders")
class OrderRestController {
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping
    public ResponseEntity<OrderListResponseDTO> getUserOrders() {
        try {
            List<Order> orders = orderService.getCurrentUserOrders();
            OrderListResponseDTO response = DTOMapper.toOrderListResponseDTO(orders);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            OrderListResponseDTO errorResponse = new OrderListResponseDTO("error", "Failed to fetch orders: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable String orderId) {
        try {
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isPresent()) {
                OrderResponseDTO response = DTOMapper.toOrderResponseDTO(orderOpt.get());
                return ResponseEntity.ok(response);
            } else {
                OrderResponseDTO errorResponse = new OrderResponseDTO("error", "Order not found");
                return ResponseEntity.status(404).body(errorResponse);
            }
        } catch (Exception e) {
            OrderResponseDTO errorResponse = new OrderResponseDTO("error", "Failed to fetch order: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder() {
        try {
            Order order = orderService.createOrderFromCart();
            OrderResponseDTO response = DTOMapper.toOrderResponseDTO(order);
            response.setStatus("success");
            response.setMessage("Order created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            OrderResponseDTO errorResponse = new OrderResponseDTO("error", "Failed to create order: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<OrderListResponseDTO> getAllOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();
            OrderListResponseDTO response = DTOMapper.toOrderListResponseDTO(orders);
            
            // Add order statistics
            response.setPendingCount(orderService.getOrderCountByStatus(OrderStatus.PENDING));
            response.setConfirmedCount(orderService.getOrderCountByStatus(OrderStatus.CONFIRMED));
            response.setShippedCount(orderService.getOrderCountByStatus(OrderStatus.SHIPPED));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            OrderListResponseDTO errorResponse = new OrderListResponseDTO("error", "Failed to fetch admin orders: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(@PathVariable String orderId, 
                                  @RequestBody OrderStatus status) {
        try {
            Order order = orderService.updateOrderStatus(orderId, status);
            OrderResponseDTO response = DTOMapper.toOrderResponseDTO(order);
            response.setStatus("success");
            response.setMessage("Order status updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            OrderResponseDTO errorResponse = new OrderResponseDTO("error", "Failed to update order status: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}