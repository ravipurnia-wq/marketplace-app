package com.marketplace.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class PayPalConfig {
    
    @Value("${paypal.mode}")
    private String paypalMode;
    
    @Value("${app.environment}")
    private String appEnvironment;
    
    @Value("${app.paypal.environment}")
    private String paypalEnvironment;
    
    @Value("${spring.profiles.active}")
    private String activeProfile;
    
    @PostConstruct
    public void logPayPalConfiguration() {
        System.out.println("=== PayPal Configuration ===");
        System.out.println("Active Profile: " + activeProfile);
        System.out.println("App Environment: " + appEnvironment);
        System.out.println("PayPal Mode: " + paypalMode);
        System.out.println("PayPal Environment: " + paypalEnvironment);
        System.out.println("==============================");
    }
}