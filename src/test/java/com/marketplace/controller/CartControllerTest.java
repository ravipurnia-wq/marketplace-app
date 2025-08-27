package com.marketplace.controller;

import com.marketplace.model.Cart;
import com.marketplace.model.CartItem;
import com.marketplace.service.CartService;
import com.marketplace.dto.mapper.DTOMapper;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {CartController.class})
@ActiveProfiles("test")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    private Cart testCart;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testCartItem = new CartItem(
            "1",
            "Test Product",
            new BigDecimal("99.99"),
            "http://test.com/image.jpg",
            2
        );

        testCart = new Cart("user-123");
        testCart.addItem(testCartItem);
    }

    @Test
    @WithMockUser
    void viewCart_shouldReturnCartView() throws Exception {
        // Given
        when(cartService.getCurrentUserCart()).thenReturn(testCart);

        // When & Then
        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("cart"));

        verify(cartService).getCurrentUserCart();
    }

    @Test
    @WithMockUser
    void addToCart_withValidParams_shouldRedirectToCart() throws Exception {
        // Given
        when(cartService.addToCart("1", 2)).thenReturn(testCart);

        // When & Then
        mockMvc.perform(post("/cart/add")
                .param("productId", "1")
                .param("quantity", "2")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).addToCart("1", 2);
    }

    @Test
    @WithMockUser
    void addToCart_withDefaultQuantity_shouldAddOneItem() throws Exception {
        // Given
        when(cartService.addToCart("1", 1)).thenReturn(testCart);

        // When & Then
        mockMvc.perform(post("/cart/add")
                .param("productId", "1")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).addToCart("1", 1);
    }

    @Test
    @WithMockUser
    void addToCart_whenUserNotLoggedIn_shouldRedirectToLogin() throws Exception {
        // Given
        when(cartService.addToCart("1", 2)).thenThrow(new RuntimeException("User must be logged in"));

        // When & Then
        mockMvc.perform(post("/cart/add")
                .param("productId", "1")
                .param("quantity", "2")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser
    void updateCartItem_shouldRedirectToCart() throws Exception {
        // Given
        when(cartService.updateCartItemQuantity("1", 3)).thenReturn(testCart);

        // When & Then
        mockMvc.perform(post("/cart/update")
                .param("productId", "1")
                .param("quantity", "3")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).updateCartItemQuantity("1", 3);
    }

    @Test
    @WithMockUser
    void updateCartItem_whenExceptionThrown_shouldStillRedirectToCart() throws Exception {
        // Given
        when(cartService.updateCartItemQuantity("1", 3))
                .thenThrow(new RuntimeException("Update failed"));

        // When & Then
        mockMvc.perform(post("/cart/update")
                .param("productId", "1")
                .param("quantity", "3")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    @WithMockUser
    void removeFromCart_shouldRedirectToCart() throws Exception {
        // Given
        when(cartService.removeFromCart("1")).thenReturn(testCart);

        // When & Then
        mockMvc.perform(post("/cart/remove")
                .param("productId", "1")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).removeFromCart("1");
    }

    @Test
    @WithMockUser
    void removeFromCart_whenExceptionThrown_shouldStillRedirectToCart() throws Exception {
        // Given
        when(cartService.removeFromCart("1")).thenThrow(new RuntimeException("Remove failed"));

        // When & Then
        mockMvc.perform(post("/cart/remove")
                .param("productId", "1")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    @WithMockUser
    void clearCart_shouldRedirectToCart() throws Exception {
        // When & Then
        mockMvc.perform(post("/cart/clear")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).clearCurrentUserCart();
    }

}