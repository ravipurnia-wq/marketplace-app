package com.marketplace.controller;

import com.marketplace.model.Cart;
import com.marketplace.model.CartItem;
import com.marketplace.model.Product;
import com.marketplace.repository.ProductRepository;
import com.marketplace.service.CartService;
import com.marketplace.service.PayPalSellerService;
import com.marketplace.service.PayPalService;
import com.paypal.orders.LinkDescription;
import com.paypal.orders.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@ActiveProfiles("test")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private PayPalService payPalService;

    @MockBean
    private CartService cartService;

    @MockBean
    private PayPalSellerService payPalSellerService;

    private Product testProduct;
    private Cart testCart;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId("1");
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setDescription("Test Description");

        CartItem cartItem = new CartItem("1", "Test Product", new BigDecimal("99.99"), "image.jpg", 2);
        testCart = new Cart("user-123");
        testCart.addItem(cartItem);

        // Mock PayPal Order
        mockOrder = mock(Order.class);
        when(mockOrder.id()).thenReturn("paypal-order-123");
        when(mockOrder.status()).thenReturn("CREATED");

        LinkDescription approveLink = mock(LinkDescription.class);
        when(approveLink.rel()).thenReturn("approve");
        when(approveLink.href()).thenReturn("https://www.sandbox.paypal.com/checkoutnow?token=ABC123");

        when(mockOrder.links()).thenReturn(Arrays.asList(approveLink));
    }

    @Test
    void processPayPalPayment_withValidProduct_shouldReturnPaymentUrl() throws Exception {
        when(productRepository.findById("1")).thenReturn(Optional.of(testProduct));
        when(payPalSellerService.isSellerEligibleForCheckout("default-seller")).thenReturn(true);
        when(payPalService.createOrder(testProduct)).thenReturn(mockOrder);

        mockMvc.perform(post("/api/payments/paypal")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": \"1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Payment initiated"))
                .andExpect(jsonPath("$.paypalUrl").value("https://www.sandbox.paypal.com/checkoutnow?token=ABC123"))
                .andExpect(jsonPath("$.orderId").value("paypal-order-123"));

        verify(productRepository).findById("1");
        verify(payPalService).createOrder(testProduct);
    }

    @Test
    void processPayPalPayment_withMissingProductId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/payments/paypal")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Missing required field: productId"));

        verifyNoInteractions(productRepository, payPalService);
    }

    @Test
    void processPayPalPayment_withInvalidProduct_shouldReturnBadRequest() throws Exception {
        when(productRepository.findById("999")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/payments/paypal")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": \"999\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Product not found"));

        verify(productRepository).findById("999");
        verifyNoInteractions(payPalService);
    }

    @Test
    void processPayPalPayment_withIneligibleSeller_shouldReturnBadRequest() throws Exception {
        when(productRepository.findById("1")).thenReturn(Optional.of(testProduct));
        when(payPalSellerService.isSellerEligibleForCheckout("default-seller")).thenReturn(false);
        
        Map<String, Object> sellerValidation = new HashMap<>();
        sellerValidation.put("warning", "Seller account not verified");
        when(payPalSellerService.validateSellerAccount("default-seller")).thenReturn(sellerValidation);

        mockMvc.perform(post("/api/payments/paypal")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": \"1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Seller account not verified"));

        verify(productRepository).findById("1");
        verify(payPalSellerService).isSellerEligibleForCheckout("default-seller");
        verifyNoInteractions(payPalService);
    }

    @Test
    void processPayPalPayment_withPayPalServiceFailure_shouldFallbackToDemoUrl() throws Exception {
        when(productRepository.findById("1")).thenReturn(Optional.of(testProduct));
        when(payPalSellerService.isSellerEligibleForCheckout("default-seller")).thenReturn(true);
        when(payPalService.createOrder(testProduct)).thenThrow(new RuntimeException("PayPal service unavailable"));

        mockMvc.perform(post("/api/payments/paypal")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": \"1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Payment initiated"))
                .andExpect(jsonPath("$.paypalUrl").value(org.hamcrest.Matchers.containsString("sandbox.paypal.com")))
                .andExpect(jsonPath("$.orderId").value(org.hamcrest.Matchers.startsWith("demo-order-")));

        verify(payPalService).createOrder(testProduct);
    }

    @Test
    void capturePayPalOrder_withValidOrderId_shouldReturnSuccess() throws Exception {
        when(mockOrder.status()).thenReturn("COMPLETED");
        when(payPalService.captureOrder("paypal-order-123")).thenReturn(mockOrder);

        mockMvc.perform(post("/api/payments/paypal/capture/paypal-order-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Payment captured successfully"))
                .andExpect(jsonPath("$.orderId").value("paypal-order-123"))
                .andExpect(jsonPath("$.orderStatus").value("COMPLETED"));

        verify(payPalService).captureOrder("paypal-order-123");
    }

    @Test
    void capturePayPalOrder_withServiceFailure_shouldReturnError() throws Exception {
        when(payPalService.captureOrder("invalid-order")).thenThrow(new RuntimeException("Order not found"));

        mockMvc.perform(post("/api/payments/paypal/capture/invalid-order"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Payment capture failed: Order not found"));
    }

    @Test
    void processCartPayPalPayment_withValidCart_shouldReturnPaymentUrl() throws Exception {
        when(cartService.getCurrentUserCart()).thenReturn(testCart);
        when(payPalService.createCartOrder(eq(testCart), any(BigDecimal.class))).thenReturn(mockOrder);

        mockMvc.perform(post("/api/payments/paypal/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.paypalUrl").value("https://www.sandbox.paypal.com/checkoutnow?token=ABC123"))
                .andExpect(jsonPath("$.cartTotal").exists())
                .andExpect(jsonPath("$.tax").exists())
                .andExpect(jsonPath("$.finalTotal").exists())
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.orderId").value("paypal-order-123"));

        verify(cartService).getCurrentUserCart();
        verify(payPalService).createCartOrder(eq(testCart), any(BigDecimal.class));
    }

    @Test
    void processCartPayPalPayment_withEmptyCart_shouldReturnBadRequest() throws Exception {
        Cart emptyCart = new Cart("user-123");
        when(cartService.getCurrentUserCart()).thenReturn(emptyCart);

        mockMvc.perform(post("/api/payments/paypal/cart"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Cart is empty"));

        verify(cartService).getCurrentUserCart();
        verifyNoInteractions(payPalService);
    }

    @Test
    void processCartPayPalPayment_withPayPalServiceFailure_shouldFallbackToDemoUrl() throws Exception {
        when(cartService.getCurrentUserCart()).thenReturn(testCart);
        when(payPalService.createCartOrder(eq(testCart), any(BigDecimal.class)))
                .thenThrow(new RuntimeException("PayPal service unavailable"));

        mockMvc.perform(post("/api/payments/paypal/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.paypalUrl").value(org.hamcrest.Matchers.containsString("sandbox.paypal.com")))
                .andExpect(jsonPath("$.orderId").value(org.hamcrest.Matchers.startsWith("demo-cart-")));

        verify(cartService).getCurrentUserCart();
        verify(payPalService).createCartOrder(eq(testCart), any(BigDecimal.class));
    }

    @Test
    void handlePayPalSuccess_shouldReturnCompletedStatus() throws Exception {
        mockMvc.perform(post("/api/payments/paypal/success")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\": \"paypal-order-123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("completed"))
                .andExpect(jsonPath("$.message").value("Payment completed successfully"));
    }

    @Test
    void handlePayPalCancel_shouldReturnCancelledStatus() throws Exception {
        mockMvc.perform(post("/api/payments/paypal/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\": \"paypal-order-123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("cancelled"))
                .andExpect(jsonPath("$.message").value("Payment was cancelled"));
    }

    @Test
    void processPayPalPayment_withGeneralException_shouldReturnInternalServerError() throws Exception {
        when(productRepository.findById("1")).thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(post("/api/payments/paypal")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": \"1\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("Payment processing failed:")));
    }

    @Test
    void processCartPayPalPayment_withGeneralException_shouldReturnInternalServerError() throws Exception {
        when(cartService.getCurrentUserCart()).thenThrow(new RuntimeException("Cart service unavailable"));

        mockMvc.perform(post("/api/payments/paypal/cart"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("Cart payment failed:")));
    }
}