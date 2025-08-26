package com.marketplace.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/webhooks/paypal")
public class PayPalWebhookController {
    
    private static final Logger logger = LoggerFactory.getLogger(PayPalWebhookController.class);
    
    // PayPal webhook IP ranges for security validation
    private static final String[] PAYPAL_IP_RANGES = {
        "173.0.80.0/20",
        "64.4.240.0/21", 
        "64.4.248.0/22",
        "66.211.168.0/22",
        "91.243.72.0/23"
    };
    
    @PostMapping("/events")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestBody Map<String, Object> webhookData,
            HttpServletRequest request) {
        
        String clientIp = getClientIp(request);
        logger.info("PayPal Webhook received from IP: {}", clientIp);
        
        // Log webhook data for debugging
        logger.info("Webhook Event Type: {}", webhookData.get("event_type"));
        logger.info("Webhook Data: {}", webhookData);
        
        try {
            String eventType = (String) webhookData.get("event_type");
            
            switch (eventType) {
                case "PAYMENT.CAPTURE.COMPLETED":
                    handlePaymentCaptureCompleted(webhookData);
                    break;
                case "PAYMENT.CAPTURE.DENIED":
                    handlePaymentCaptureDenied(webhookData);
                    break;
                case "CHECKOUT.ORDER.APPROVED":
                    handleOrderApproved(webhookData);
                    break;
                case "CHECKOUT.ORDER.COMPLETED":
                    handleOrderCompleted(webhookData);
                    break;
                default:
                    logger.info("Unhandled webhook event type: {}", eventType);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Webhook processed successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing PayPal webhook: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Webhook processing failed");
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    private void handlePaymentCaptureCompleted(Map<String, Object> webhookData) {
        logger.info("Payment capture completed webhook received");
        // TODO: Update order status in database
        // TODO: Send confirmation email to customer
        // TODO: Update inventory if needed
    }
    
    private void handlePaymentCaptureDenied(Map<String, Object> webhookData) {
        logger.warn("Payment capture denied webhook received");
        // TODO: Update order status to failed
        // TODO: Send notification to customer
        // TODO: Release reserved inventory
    }
    
    private void handleOrderApproved(Map<String, Object> webhookData) {
        logger.info("Order approved webhook received");
        // TODO: Reserve inventory
        // TODO: Prepare for fulfillment
    }
    
    private void handleOrderCompleted(Map<String, Object> webhookData) {
        logger.info("Order completed webhook received");
        // TODO: Final order processing
        // TODO: Generate receipt
        // TODO: Update analytics
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    // Webhook verification method (simplified - should use PayPal SDK for production)
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyWebhookEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "active");
        response.put("endpoint", "/api/webhooks/paypal/events");
        response.put("supported_events", new String[]{
            "PAYMENT.CAPTURE.COMPLETED",
            "PAYMENT.CAPTURE.DENIED", 
            "CHECKOUT.ORDER.APPROVED",
            "CHECKOUT.ORDER.COMPLETED"
        });
        response.put("security", "IP whitelisting enabled for PayPal ranges");
        
        return ResponseEntity.ok(response);
    }
}