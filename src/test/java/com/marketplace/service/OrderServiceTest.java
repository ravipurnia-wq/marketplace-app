package com.marketplace.service;

import com.marketplace.model.*;
import com.marketplace.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserService userService;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Cart testCart;
    private Order testOrder;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setAddress("123 Test St");
        testUser.setPhoneNumber("555-1234");

        testCartItem = new CartItem(
            "1",
            "Test Product",
            new BigDecimal("99.99"),
            "http://test.com/image.jpg",
            2
        );

        testCart = new Cart("user-123");
        testCart.addItem(testCartItem);

        testOrder = new Order(
            "user-123",
            "test@example.com",
            "Test User",
            testCart.getItems(),
            testCart.getTotal()
        );
        testOrder.setId("order-123");
        testOrder.setShippingAddress("123 Test St");
        testOrder.setPhoneNumber("555-1234");
    }

    @Test
    void createOrderFromCart_withValidUserAndCart_shouldCreateOrder() {
        // Given
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(cartService.getCurrentUserCart()).thenReturn(testCart);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.createOrderFromCart();

        // Then
        assertNotNull(result);
        assertEquals("order-123", result.getId());
        verify(orderRepository).save(any(Order.class));
        verify(cartService).clearCurrentUserCart();
    }

    @Test
    void createOrderFromCart_withNoUser_shouldThrowException() {
        // Given
        when(userService.getCurrentUser()).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.createOrderFromCart());
        assertEquals("User must be logged in to place an order", exception.getMessage());
    }

    @Test
    void createOrderFromCart_withEmptyCart_shouldThrowException() {
        // Given
        Cart emptyCart = new Cart("user-123");
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(cartService.getCurrentUserCart()).thenReturn(emptyCart);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.createOrderFromCart());
        assertEquals("Cart is empty", exception.getMessage());
    }

    @Test
    void updateOrderStatus_withValidOrder_shouldUpdateStatus() {
        // Given
        when(orderRepository.findById("order-123")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.updateOrderStatus("order-123", OrderStatus.SHIPPED);

        // Then
        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_withInvalidOrder_shouldThrowException() {
        // Given
        when(orderRepository.findById("invalid")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.updateOrderStatus("invalid", OrderStatus.SHIPPED));
        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void addPaymentInfo_withValidOrder_shouldAddPaymentInfo() {
        // Given
        when(orderRepository.findById("order-123")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.addPaymentInfo("order-123", "txn-123", "payment-123");

        // Then
        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void addPaymentInfo_withInvalidOrder_shouldThrowException() {
        // Given
        when(orderRepository.findById("invalid")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.addPaymentInfo("invalid", "txn-123", "payment-123"));
        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void getCurrentUserOrders_withValidUser_shouldReturnOrders() {
        // Given
        when(userService.getCurrentUserId()).thenReturn("user-123");
        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user-123"))
            .thenReturn(Arrays.asList(testOrder));

        // When
        List<Order> result = orderService.getCurrentUserOrders();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("order-123", result.get(0).getId());
    }

    @Test
    void getCurrentUserOrders_withNoUser_shouldThrowException() {
        // Given
        when(userService.getCurrentUserId()).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.getCurrentUserOrders());
        assertEquals("User must be logged in", exception.getMessage());
    }

    @Test
    void getCurrentUserOrdersWithPageable_withValidUser_shouldReturnPagedOrders() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(Arrays.asList(testOrder));
        when(userService.getCurrentUserId()).thenReturn("user-123");
        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user-123", pageable))
            .thenReturn(page);

        // When
        Page<Order> result = orderService.getCurrentUserOrders(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("order-123", result.getContent().get(0).getId());
    }

    @Test
    void getCurrentUserOrdersWithPageable_withNoUser_shouldThrowException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(userService.getCurrentUserId()).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.getCurrentUserOrders(pageable));
        assertEquals("User must be logged in", exception.getMessage());
    }

    @Test
    void getAllOrders_shouldReturnAllOrders() {
        // Given
        when(orderRepository.findAll()).thenReturn(Arrays.asList(testOrder));

        // When
        List<Order> result = orderService.getAllOrders();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void getAllOrdersWithPageable_shouldReturnPagedOrders() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(Arrays.asList(testOrder));
        when(orderRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(page);

        // When
        Page<Order> result = orderService.getAllOrders(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(orderRepository).findAllByOrderByCreatedAtDesc(pageable);
    }

    @Test
    void getOrderById_withValidId_shouldReturnOrder() {
        // Given
        when(orderRepository.findById("order-123")).thenReturn(Optional.of(testOrder));

        // When
        Optional<Order> result = orderService.getOrderById("order-123");

        // Then
        assertTrue(result.isPresent());
        assertEquals("order-123", result.get().getId());
    }

    @Test
    void getOrdersByStatus_shouldReturnOrdersByStatus() {
        // Given
        when(orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.PENDING))
            .thenReturn(Arrays.asList(testOrder));

        // When
        List<Order> result = orderService.getOrdersByStatus(OrderStatus.PENDING);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findByStatusOrderByCreatedAtDesc(OrderStatus.PENDING);
    }

    @Test
    void getOrderCountByStatus_shouldReturnCount() {
        // Given
        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(5L);

        // When
        long result = orderService.getOrderCountByStatus(OrderStatus.PENDING);

        // Then
        assertEquals(5L, result);
        verify(orderRepository).countByStatus(OrderStatus.PENDING);
    }

    @Test
    void updateOrder_shouldUpdateOrder() {
        // Given
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.updateOrder(testOrder);

        // Then
        assertNotNull(result);
        verify(orderRepository).save(testOrder);
    }
}