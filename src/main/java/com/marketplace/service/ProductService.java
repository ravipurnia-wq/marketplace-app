package com.marketplace.service;

import com.marketplace.config.DatabaseConfig;
import com.marketplace.model.Product;
import com.marketplace.model.ProductMongo;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.ProductMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    
    @Autowired
    private DatabaseConfig databaseConfig;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired(required = false)
    private ProductMongoRepository productMongoRepository;
    
    public List<ProductDto> findAll() {
        if (databaseConfig.isMongoEnabled() && productMongoRepository != null) {
            return productMongoRepository.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            return productRepository.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
    }
    
    public Optional<ProductDto> findById(String id) {
        if (databaseConfig.isMongoEnabled() && productMongoRepository != null) {
            return productMongoRepository.findById(id).map(this::convertToDto);
        } else {
            try {
                return productRepository.findById(id).map(this::convertToDto);
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }
    }
    
    public ProductDto save(ProductDto productDto) {
        if (databaseConfig.isMongoEnabled() && productMongoRepository != null) {
            ProductMongo product = convertToMongoEntity(productDto);
            return convertToDto(productMongoRepository.save(product));
        } else {
            Product product = convertToJpaEntity(productDto);
            return convertToDto(productRepository.save(product));
        }
    }
    
    public void deleteById(String id) {
        if (databaseConfig.isMongoEnabled() && productMongoRepository != null) {
            productMongoRepository.deleteById(id);
        } else {
            try {
                productRepository.deleteById(id);
            } catch (NumberFormatException e) {
                // Invalid ID format
            }
        }
    }
    
    public boolean existsById(String id) {
        if (databaseConfig.isMongoEnabled() && productMongoRepository != null) {
            return productMongoRepository.existsById(id);
        } else {
            try {
                return productRepository.existsById(id);
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
    
    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        dto.setPaypalButtonId(product.getPaypalButtonId());
        return dto;
    }
    
    private ProductDto convertToDto(ProductMongo product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        dto.setPaypalButtonId(product.getPaypalButtonId());
        return dto;
    }
    
    private Product convertToJpaEntity(ProductDto dto) {
        Product product = new Product();
        if (dto.getId() != null && !dto.getId().isEmpty()) {
            try {
                product.setId(dto.getId());
            } catch (NumberFormatException e) {
                // New entity
            }
        }
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());
        product.setImageUrl(dto.getImageUrl());
        product.setPaypalButtonId(dto.getPaypalButtonId());
        return product;
    }
    
    private ProductMongo convertToMongoEntity(ProductDto dto) {
        ProductMongo product = new ProductMongo();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());
        product.setImageUrl(dto.getImageUrl());
        product.setPaypalButtonId(dto.getPaypalButtonId());
        return product;
    }
    
    // DTO class for unified product representation
    public static class ProductDto {
        private String id;
        private String name;
        private BigDecimal price;
        private String description;
        private String imageUrl;
        private String paypalButtonId;
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public String getPaypalButtonId() { return paypalButtonId; }
        public void setPaypalButtonId(String paypalButtonId) { this.paypalButtonId = paypalButtonId; }
    }
}