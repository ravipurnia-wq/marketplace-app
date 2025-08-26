package com.marketplace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.database")
public class DatabaseConfig {
    
    private String type = "h2"; // Default to H2, can be set to "mongodb"
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isMongoEnabled() {
        return "mongodb".equalsIgnoreCase(type);
    }
    
    public boolean isH2Enabled() {
        return "h2".equalsIgnoreCase(type);
    }
}