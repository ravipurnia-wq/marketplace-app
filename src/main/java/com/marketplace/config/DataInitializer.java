package com.marketplace.config;

import com.marketplace.model.Product;
import com.marketplace.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() == 0) {
            Product headphones = new Product(
                "Premium Headphones",
                new BigDecimal("99.99"),
                "High-quality wireless headphones with noise cancellation",
                "https://via.placeholder.com/300x200",
                "962SY9YFS2WD4"
            );
            
            Product smartWatch = new Product(
                "Smart Watch",
                new BigDecimal("199.99"),
                "Feature-rich smartwatch with health monitoring",
                "https://via.placeholder.com/300x200",
                "962SY9YFS2WD4"
            );
            
            Product laptopBag = new Product(
                "Laptop Bag",
                new BigDecimal("49.99"),
                "Durable and stylish laptop bag for professionals",
                "https://via.placeholder.com/300x200",
                "962SY9YFS2WD4"
            );
            
            productRepository.save(headphones);
            productRepository.save(smartWatch);
            productRepository.save(laptopBag);
        }
    }
}