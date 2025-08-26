package com.marketplace.service;

import com.marketplace.model.Product;
import com.marketplace.model.Cart;
import com.marketplace.model.CartItem;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.orders.*;
import com.paypal.orders.AmountBreakdown;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PayPalService {
    
    private static final Logger logger = LoggerFactory.getLogger(PayPalService.class);
    private final PayPalHttpClient payPalClient;
    
    @Value("${paypal.client.id:sb-demo-client-id}")
    private String clientId;
    
    @Value("${paypal.client.secret:sb-demo-client-secret}")
    private String clientSecret;
    
    @Value("${paypal.mode:sandbox}")
    private String mode;
    
    @Value("${paypal.return.success.url:http://localhost:8081/payment-success}")
    private String successUrl;
    
    @Value("${paypal.return.cancel.url:http://localhost:8081/payment-cancelled}")
    private String cancelUrl;
    
    public PayPalService(@Value("${paypal.client.id:sb-demo-client-id}") String clientId,
                         @Value("${paypal.client.secret:sb-demo-client-secret}") String clientSecret,
                         @Value("${paypal.mode:sandbox}") String mode) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.mode = mode;
        
        PayPalEnvironment environment;
        if ("live".equals(mode)) {
            environment = new PayPalEnvironment.Live(clientId, clientSecret);
        } else {
            environment = new PayPalEnvironment.Sandbox(clientId, clientSecret);
        }
        this.payPalClient = new PayPalHttpClient(environment);
    }
    
    public Order createOrder(Product product) throws IOException {
        logger.info("Creating PayPal order for product: {} with client ID: {}", product.getName(), clientId.substring(0, 10) + "...");
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");
        
        ApplicationContext applicationContext = new ApplicationContext()
            .returnUrl(successUrl)
            .cancelUrl(cancelUrl);
        orderRequest.applicationContext(applicationContext);
        
        List<PurchaseUnitRequest> purchaseUnits = new ArrayList<>();
        purchaseUnits.add(new PurchaseUnitRequest()
            .referenceId("PUHF")
            .description(product.getDescription())
            .customId(product.getId().toString())
            .softDescriptor("TechMarket Pro")
            .amountWithBreakdown(new AmountWithBreakdown()
                .currencyCode("USD")
                .value(product.getPrice().toString())
            )
        );
        orderRequest.purchaseUnits(purchaseUnits);
        
        OrdersCreateRequest request = new OrdersCreateRequest();
        request.header("prefer", "return=representation");
        request.requestBody(orderRequest);
        
        logger.info("Sending PayPal order creation request...");
        try {
            Order result = payPalClient.execute(request).result();
            logger.info("PayPal order created successfully: {}", result.id());
            return result;
        } catch (Exception e) {
            logger.error("PayPal order creation failed: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    public Order captureOrder(String orderId) throws IOException {
        OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
        request.requestBody(new OrderActionRequest());
        
        return payPalClient.execute(request).result();
    }
    
    public Order getOrder(String orderId) throws IOException {
        OrdersGetRequest request = new OrdersGetRequest(orderId);
        return payPalClient.execute(request).result();
    }
    
    public Order createCartOrder(Cart cart, BigDecimal finalTotal) throws IOException {
        logger.info("Creating PayPal cart order for {} items, total: {}", cart.getItems().size(), finalTotal);
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");
        
        ApplicationContext applicationContext = new ApplicationContext()
            .returnUrl(successUrl)
            .cancelUrl(cancelUrl);
        orderRequest.applicationContext(applicationContext);
        
        // Build description from cart items
        StringBuilder description = new StringBuilder("TechMarket Pro Cart: ");
        for (CartItem item : cart.getItems()) {
            description.append(item.getProductName())
                      .append(" (x")
                      .append(item.getQuantity())
                      .append("), ");
        }
        if (description.length() > 2) {
            description.setLength(description.length() - 2);
        }
        
        List<PurchaseUnitRequest> purchaseUnits = new ArrayList<>();
        purchaseUnits.add(new PurchaseUnitRequest()
            .referenceId("CART")
            .description(description.toString())
            .customId("cart-" + System.currentTimeMillis())
            .softDescriptor("TechMarket Pro")
            .amountWithBreakdown(new AmountWithBreakdown()
                .currencyCode("USD")
                .value(finalTotal.toString())
            )
        );
        orderRequest.purchaseUnits(purchaseUnits);
        
        OrdersCreateRequest request = new OrdersCreateRequest();
        request.header("prefer", "return=representation");
        request.requestBody(orderRequest);
        
        logger.info("Sending PayPal cart order creation request...");
        try {
            Order result = payPalClient.execute(request).result();
            logger.info("PayPal cart order created successfully: {}", result.id());
            return result;
        } catch (Exception e) {
            logger.error("PayPal cart order creation failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}