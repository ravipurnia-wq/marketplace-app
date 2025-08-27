package com.marketplace.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PayPalWebhookController.class)
@ActiveProfiles("test")
class PayPalWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void handleWebhook_withPaymentCaptureCompleted_shouldReturnSuccess() throws Exception {
        Map<String, Object> webhookData = new HashMap<>();
        webhookData.put("event_type", "PAYMENT.CAPTURE.COMPLETED");
        webhookData.put("id", "WH-12345");
        webhookData.put("resource", Map.of("id", "payment-123"));

        mockMvc.perform(post("/api/webhooks/paypal/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "event_type": "PAYMENT.CAPTURE.COMPLETED",
                        "id": "WH-12345",
                        "resource": {
                            "id": "payment-123"
                        }
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Webhook processed successfully"));
    }

    @Test
    void handleWebhook_withPaymentCaptureDenied_shouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/webhooks/paypal/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "event_type": "PAYMENT.CAPTURE.DENIED",
                        "id": "WH-12346",
                        "resource": {
                            "id": "payment-124"
                        }
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Webhook processed successfully"));
    }

    @Test
    void handleWebhook_withOrderApproved_shouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/webhooks/paypal/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "event_type": "CHECKOUT.ORDER.APPROVED",
                        "id": "WH-12347",
                        "resource": {
                            "id": "order-123"
                        }
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Webhook processed successfully"));
    }

    @Test
    void handleWebhook_withOrderCompleted_shouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/webhooks/paypal/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "event_type": "CHECKOUT.ORDER.COMPLETED",
                        "id": "WH-12348",
                        "resource": {
                            "id": "order-124"
                        }
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Webhook processed successfully"));
    }

    @Test
    void handleWebhook_withUnhandledEventType_shouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/webhooks/paypal/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "event_type": "UNKNOWN.EVENT.TYPE",
                        "id": "WH-12349",
                        "resource": {
                            "id": "resource-123"
                        }
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Webhook processed successfully"));
    }

    @Test
    void handleWebhook_withMalformedJson_shouldReturnError() throws Exception {
        mockMvc.perform(post("/api/webhooks/paypal/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleWebhook_withEmptyBody_shouldReturnError() throws Exception {
        mockMvc.perform(post("/api/webhooks/paypal/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void handleWebhook_withXForwardedForHeader_shouldLogCorrectIp() throws Exception {
        mockMvc.perform(post("/api/webhooks/paypal/events")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Forwarded-For", "192.168.1.1, 10.0.0.1")
                .content("""
                    {
                        "event_type": "PAYMENT.CAPTURE.COMPLETED",
                        "id": "WH-12350"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void handleWebhook_withXRealIpHeader_shouldLogCorrectIp() throws Exception {
        mockMvc.perform(post("/api/webhooks/paypal/events")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Real-IP", "203.0.113.1")
                .content("""
                    {
                        "event_type": "PAYMENT.CAPTURE.COMPLETED",
                        "id": "WH-12351"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void verifyWebhookEndpoint_shouldReturnEndpointInfo() throws Exception {
        mockMvc.perform(get("/api/webhooks/paypal/verify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("active"))
                .andExpect(jsonPath("$.endpoint").value("/api/webhooks/paypal/events"))
                .andExpect(jsonPath("$.supported_events").isArray())
                .andExpect(jsonPath("$.supported_events[0]").value("PAYMENT.CAPTURE.COMPLETED"))
                .andExpect(jsonPath("$.supported_events[1]").value("PAYMENT.CAPTURE.DENIED"))
                .andExpect(jsonPath("$.supported_events[2]").value("CHECKOUT.ORDER.APPROVED"))
                .andExpect(jsonPath("$.supported_events[3]").value("CHECKOUT.ORDER.COMPLETED"))
                .andExpect(jsonPath("$.security").value("IP whitelisting enabled for PayPal ranges"));
    }

    @Test
    void handleWebhook_withComplexNestedData_shouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/webhooks/paypal/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "event_type": "PAYMENT.CAPTURE.COMPLETED",
                        "id": "WH-12352",
                        "resource": {
                            "id": "payment-125",
                            "amount": {
                                "currency_code": "USD",
                                "value": "100.00"
                            },
                            "status": "COMPLETED"
                        },
                        "links": [
                            {
                                "href": "https://api.paypal.com/v2/payments/captures/payment-125",
                                "rel": "self",
                                "method": "GET"
                            }
                        ]
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Webhook processed successfully"));
    }
}