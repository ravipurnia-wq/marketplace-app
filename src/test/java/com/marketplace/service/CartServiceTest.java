package com.marketplace.service;

import com.marketplace.model.Cart;
import com.marketplace.model.CartItem;
import com.marketplace.model.Product;
import com.marketplace.repository.CartRepository;
import com.marketplace.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CartService cartService;

    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId("1");
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setDescription("Test Description");
        testProduct.setImageUrl("http://test.com/image.jpg");
        testProduct.setPaypalButtonId("button-123");

        testCart = new Cart("user-123");
        
        testCartItem = new CartItem(
            "1",
            "Test Product",
            new BigDecimal("99.99"),
            "http://test.com/image.jpg",
            2
        );
    }

    @Test
    void getOrCreateCart_withExistingCart_shouldReturnExistingCart() {
        // Given
        when(cartRepository.findByUserId("user-123")).thenReturn(Optional.of(testCart));

        // When
        Cart result = cartService.getOrCreateCart("user-123");

        // Then
        assertNotNull(result);
        assertEquals("user-123", result.getUserId());
        verify(cartRepository).findByUserId("user-123");
    }

    @Test
    void getOrCreateCart_withNoExistingCart_shouldCreateNewCart() {
        // Given
        when(cartRepository.findByUserId("user-123")).thenReturn(Optional.empty());

        // When
        Cart result = cartService.getOrCreateCart("user-123");

        // Then
        assertNotNull(result);
        assertEquals("user-123", result.getUserId());
        assertTrue(result.getItems().isEmpty());
        verify(cartRepository).findByUserId("user-123");
    }

    @Test
    void getCurrentUserCart_withLoggedInUser_shouldReturnCart() {
        // Given
        when(userService.getCurrentUserId()).thenReturn("user-123");
        when(cartRepository.findByUserId("user-123")).thenReturn(Optional.of(testCart));

        // When
        Cart result = cartService.getCurrentUserCart();

        // Then
        assertNotNull(result);
        assertEquals("user-123", result.getUserId());
    }

    @Test
    void getCurrentUserCart_withNoLoggedInUser_shouldReturnNewCart() {
        // Given
        when(userService.getCurrentUserId()).thenReturn(null);

        // When
        Cart result = cartService.getCurrentUserCart();

        // Then
        assertNotNull(result);
        assertNull(result.getUserId());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void addToCart_withValidProductAndUser_shouldAddItemToCart() {
        // Given
        when(userService.getCurrentUserId()).thenReturn("user-123");
        when(productRepository.findById("1")).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId("user-123")).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // When
        Cart result = cartService.addToCart("1", 2);

        // Then
        assertNotNull(result);
        verify(cartRepository).save(any(Cart.class));
        verify(productRepository).findById("1");
    }

    @Test
    void addToCart_withNoUser_shouldThrowException() {
        // Given
        when(userService.getCurrentUserId()).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> cartService.addToCart("1", 2));
        assertEquals("User must be logged in to add items to cart", exception.getMessage());
    }

    @Test
    void addToCart_withInvalidProduct_shouldThrowException() {
        // Given
        when(userService.getCurrentUserId()).thenReturn("user-123");
        when(productRepository.findById("invalid")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> cartService.addToCart("invalid", 2));
        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void updateCartItemQuantity_withValidUser_shouldUpdateQuantity() {
        // Given
        testCart.addItem(testCartItem); // Ensure cart is not empty after update
        when(userService.getCurrentUserId()).thenReturn("user-123");
        when(cartRepository.findByUserId("user-123")).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // When
        Cart result = cartService.updateCartItemQuantity("1", 3);

        // Then
        assertNotNull(result);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void updateCartItemQuantity_withNoUser_shouldThrowException() {
        // Given
        when(userService.getCurrentUserId()).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> cartService.updateCartItemQuantity("1", 3));
        assertEquals("User must be logged in", exception.getMessage());
    }

    @Test
    void updateCartItemQuantity_withEmptyCartAfterUpdate_shouldDeleteCart() {
        // Given
        Cart emptyCart = new Cart("user-123");
        when(userService.getCurrentUserId()).thenReturn("user-123");
        when(cartRepository.findByUserId("user-123")).thenReturn(Optional.of(emptyCart));

        // When
        Cart result = cartService.updateCartItemQuantity("1", 0);

        // Then
        assertNotNull(result);
        verify(cartRepository).deleteByUserId("user-123");
    }

    @Test
    void removeFromCart_withValidUser_shouldRemoveItem() {
        // Given
        testCart.addItem(testCartItem); // Add item first
        testCart.addItem(new CartItem("2", "Another Product", new BigDecimal("50.00"), "image2.jpg", 1)); // Add another item so cart isn't empty
        when(userService.getCurrentUserId()).thenReturn("user-123");
        when(cartRepository.findByUserId("user-123")).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // When
        Cart result = cartService.removeFromCart("1");

        // Then
        assertNotNull(result);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void removeFromCart_withNoUser_shouldThrowException() {
        // Given
        when(userService.getCurrentUserId()).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> cartService.removeFromCart("1"));
        assertEquals("User must be logged in", exception.getMessage());
    }

    @Test
    void removeFromCart_withEmptyCartAfterRemoval_shouldDeleteCart() {
        // Given
        Cart emptyCart = new Cart("user-123");
        when(userService.getCurrentUserId()).thenReturn("user-123");
        when(cartRepository.findByUserId("user-123")).thenReturn(Optional.of(emptyCart));

        // When
        Cart result = cartService.removeFromCart("1");

        // Then
        assertNotNull(result);
        verify(cartRepository).deleteByUserId("user-123");
    }

    @Test
    void clearCart_shouldDeleteCart() {
        // When
        cartService.clearCart("user-123");

        // Then
        verify(cartRepository).deleteByUserId("user-123");
    }

    @Test
    void clearCurrentUserCart_withLoggedInUser_shouldDeleteCart() {
        // Given
        when(userService.getCurrentUserId()).thenReturn("user-123");

        // When
        cartService.clearCurrentUserCart();

        // Then
        verify(cartRepository).deleteByUserId("user-123");
    }

    @Test
    void clearCurrentUserCart_withNoUser_shouldNotDeleteCart() {
        // Given
        when(userService.getCurrentUserId()).thenReturn(null);

        // When
        cartService.clearCurrentUserCart();

        // Then
        verify(cartRepository, never()).deleteByUserId(any());
    }
}