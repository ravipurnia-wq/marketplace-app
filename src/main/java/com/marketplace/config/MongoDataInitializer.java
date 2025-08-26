package com.marketplace.config;

import com.marketplace.model.ProductMongo;
import com.marketplace.repository.ProductMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(name = "app.database.type", havingValue = "mongodb")
public class MongoDataInitializer implements CommandLineRunner {
    
    @Autowired
    private ProductMongoRepository productMongoRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (productMongoRepository.count() == 0) {
            ProductMongo headphones = new ProductMongo(
                "Premium Headphones",
                new BigDecimal("99.99"),
                "High-quality wireless headphones with noise cancellation",
                "https://via.placeholder.com/300x200",
                "962SY9YFS2WD4"
            );
            
            ProductMongo smartWatch = new ProductMongo(
                "Smart Watch",
                new BigDecimal("199.99"),
                "Feature-rich smartwatch with health monitoring",
                "https://via.placeholder.com/300x200",
                "962SY9YFS2WD4"
            );
            
            ProductMongo laptopBag = new ProductMongo(
                "Laptop Bag",
                new BigDecimal("49.99"),
                "Durable and stylish laptop bag for professionals",
                "https://via.placeholder.com/300x200",
                "962SY9YFS2WD4"
            );
            
            ProductMongo wirelessMouse = new ProductMongo(
                "Wireless Mouse",
                new BigDecimal("29.99"),
                "Ergonomic wireless mouse with long battery life",
                "https://via.placeholder.com/300x200",
                "962SY9YFS2WD4"
            );
            
            productMongoRepository.save(headphones);
            productMongoRepository.save(smartWatch);
            productMongoRepository.save(laptopBag);
            productMongoRepository.save(wirelessMouse);
            
            System.out.println("✅ MongoDB initialized with " + productMongoRepository.count() + " products");
        }
    }
}