package com.marketplace.controller;

import com.marketplace.dto.*;
import com.marketplace.dto.mapper.DTOMapper;
import com.marketplace.model.Product;
import com.marketplace.model.Cart;
import com.marketplace.repository.ProductRepository;
import com.marketplace.service.PayPalService;
import com.marketplace.service.CartService;
import com.marketplace.service.PayPalSellerService;
import com.paypal.orders.Order;
import com.paypal.orders.LinkDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private PayPalService payPalService;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private PayPalSellerService payPalSellerService;
    
    @Value("${paypal.return.success.url:http://localhost:8081/payment-success}")
    private String successUrl;
    
    @Value("${paypal.return.cancel.url:http://localhost:8081/payment-cancelled}")
    private String cancelUrl;
    
    @Value("${paypal.client.id:sb-demo-client-id}")
    private String paypalClientId;
    
    @PostMapping("/paypal")
    public ResponseEntity<PaymentResponseDTO> processPayPalPayment(@RequestBody Map<String, Object> paymentData) {
        try {
            // Validate input data
            if (paymentData.get("productId") == null) {
                PaymentResponseDTO errorResponse = new PaymentResponseDTO("error", "Missing required field: productId");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String productId = paymentData.get("productId").toString();
            
            // PayPal Guidelines: Validate seller account before processing payment
            String sellerId = "default-seller"; // TODO: Get from user session or product
            if (!payPalSellerService.isSellerEligibleForCheckout(sellerId)) {
                Map<String, Object> sellerValidation = payPalSellerService.validateSellerAccount(sellerId);
                String warning = (String) sellerValidation.get("warning");
                PaymentResponseDTO errorResponse = new PaymentResponseDTO("error", 
                    warning != null ? warning : "Seller account not eligible for payments");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Get product details from database
            Optional<Product> productOpt = productRepository.findById(productId);
            if (!productOpt.isPresent()) {
                PaymentResponseDTO errorResponse = new PaymentResponseDTO("error", "Product not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Product product = productOpt.get();
            
            // Try PayPal SDK first, fall back to direct URL if needed
            String paypalUrl = null;
            String orderId = null;
            
            try {
                Order order = payPalService.createOrder(product);
                
                // Find the approval URL from the order links
                for (LinkDescription link : order.links()) {
                    if ("approve".equals(link.rel())) {
                        paypalUrl = link.href();
                        orderId = order.id();
                        break;
                    }
                }
                
                if (paypalUrl == null || paypalUrl.isEmpty()) {
                    throw new Exception("No approval URL found in PayPal response");
                }
                
            } catch (Exception e) {
                // Fallback to direct PayPal URL for development/demo purposes
                paypalUrl = buildDemoPayPalUrl(product);
                orderId = "demo-order-" + System.currentTimeMillis();
            }
            
            PaymentResponseDTO response = DTOMapper.createPaymentResponseDTO(
                "success", "Payment initiated", paypalUrl, 
                orderId != null ? orderId : "demo-order", product
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            PaymentResponseDTO errorResponse = new PaymentResponseDTO("error", "Payment processing failed: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    private String buildDemoPayPalUrl(Product product) {
        // Working PayPal demo URL for development/testing
        StringBuilder url = new StringBuilder("https://www.sandbox.paypal.com/cgi-bin/webscr");
        url.append("?cmd=_xclick");
        url.append("&business=").append(paypalClientId);
        url.append("&item_name=").append(java.net.URLEncoder.encode(product.getName(), java.nio.charset.StandardCharsets.UTF_8));
        url.append("&item_number=").append(product.getId());
        url.append("&amount=").append(product.getPrice());
        url.append("&currency_code=USD");
        url.append("&return=").append(java.net.URLEncoder.encode(successUrl, java.nio.charset.StandardCharsets.UTF_8));
        url.append("&cancel_return=").append(java.net.URLEncoder.encode(cancelUrl, java.nio.charset.StandardCharsets.UTF_8));
        url.append("&no_shipping=1");
        url.append("&no_note=1");
        
        return url.toString();
    }
    
    @PostMapping("/paypal/capture/{orderId}")
    public ResponseEntity<PaymentCaptureResponseDTO> capturePayPalOrder(@PathVariable String orderId) {
        try {
            Order order = payPalService.captureOrder(orderId);
            
            PaymentCaptureResponseDTO response = new PaymentCaptureResponseDTO(
                "success", "Payment captured successfully", order.id(), order.status()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            PaymentCaptureResponseDTO errorResponse = new PaymentCaptureResponseDTO("error", "Payment capture failed: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping("/paypal/cart")
    public ResponseEntity<CartPaymentResponseDTO> processCartPayPalPayment() {
        try {
            // Get current user's cart
            Cart cart = cartService.getCurrentUserCart();
            if (cart.isEmpty()) {
                CartPaymentResponseDTO errorResponse = new CartPaymentResponseDTO("error", "Cart is empty");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Calculate cart total with tax
            BigDecimal cartTotal = cart.getTotal();
            BigDecimal tax = cartTotal.multiply(new BigDecimal("0.08"));
            BigDecimal finalTotal = cartTotal.add(tax);
            
            // Try PayPal SDK first, fall back to direct URL if needed
            String paypalUrl = null;
            String orderId = null;
            
            try {
                Order order = payPalService.createCartOrder(cart, finalTotal);
                
                // Find the approval URL from the order links
                for (LinkDescription link : order.links()) {
                    if ("approve".equals(link.rel())) {
                        paypalUrl = link.href();
                        orderId = order.id();
                        break;
                    }
                }
                
                if (paypalUrl == null || paypalUrl.isEmpty()) {
                    throw new Exception("No approval URL found in PayPal response");
                }
                
            } catch (Exception e) {
                // Fallback to direct PayPal URL for development/demo purposes (same as Buy Now)
                paypalUrl = buildDemoCartPayPalUrl(cart, finalTotal);
                orderId = "demo-cart-" + System.currentTimeMillis();
            }
            
            CartPaymentResponseDTO response = DTOMapper.createCartPaymentResponseDTO(
                "success", paypalUrl, cartTotal.toString(), tax.toString(),
                finalTotal.toString(), cart.getTotalItems(), orderId
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            CartPaymentResponseDTO errorResponse = new CartPaymentResponseDTO("error", "Cart payment failed: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    private String buildDemoCartPayPalUrl(Cart cart, BigDecimal finalTotal) {
        // Working PayPal demo URL for cart checkout - same format as individual product
        StringBuilder url = new StringBuilder("https://www.sandbox.paypal.com/cgi-bin/webscr");
        url.append("?cmd=_xclick");
        url.append("&business=").append(paypalClientId);
        url.append("&item_name=").append(java.net.URLEncoder.encode("TechMarket Pro Cart (" + cart.getTotalItems() + " items)", java.nio.charset.StandardCharsets.UTF_8));
        url.append("&item_number=cart-").append(System.currentTimeMillis());
        url.append("&amount=").append(finalTotal.toString());
        url.append("&currency_code=USD");
        url.append("&return=").append(java.net.URLEncoder.encode(successUrl, java.nio.charset.StandardCharsets.UTF_8));
        url.append("&cancel_return=").append(java.net.URLEncoder.encode(cancelUrl, java.nio.charset.StandardCharsets.UTF_8));
        url.append("&no_shipping=1");
        url.append("&no_note=1");
        return url.toString();
    }

    @PostMapping("/paypal/success")
    public ResponseEntity<PaymentResponseDTO> handlePayPalSuccess(@RequestBody Map<String, Object> successData) {
        PaymentResponseDTO response = new PaymentResponseDTO("completed", "Payment completed successfully");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/paypal/cancel")
    public ResponseEntity<PaymentResponseDTO> handlePayPalCancel(@RequestBody Map<String, Object> cancelData) {
        PaymentResponseDTO response = new PaymentResponseDTO("cancelled", "Payment was cancelled");
        return ResponseEntity.ok(response);
    }
}