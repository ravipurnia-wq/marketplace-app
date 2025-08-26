package com.marketplace.controller;

import com.marketplace.dto.*;
import com.marketplace.dto.mapper.DTOMapper;
import com.marketplace.model.Product;
import com.marketplace.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductRestController {
    
    @Autowired
    private ProductRepository productRepository;
    
    @GetMapping
    public ResponseEntity<ProductListResponseDTO> getAllProducts() {
        try {
            List<Product> products = productRepository.findAll();
            ProductListResponseDTO response = DTOMapper.toProductListResponseDTO(products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ProductListResponseDTO errorResponse = new ProductListResponseDTO("error", "Failed to fetch products: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable String id) {
        try {
            Optional<Product> product = productRepository.findById(id);
            if (product.isPresent()) {
                ProductResponseDTO response = DTOMapper.toProductResponseDTO(product.get());
                return ResponseEntity.ok(response);
            } else {
                ProductResponseDTO errorResponse = new ProductResponseDTO("error", "Product not found");
                return ResponseEntity.status(404).body(errorResponse);
            }
        } catch (Exception e) {
            ProductResponseDTO errorResponse = new ProductResponseDTO("error", "Failed to fetch product: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody Product product) {
        try {
            Product savedProduct = productRepository.save(product);
            ProductResponseDTO response = DTOMapper.toProductResponseDTO(savedProduct);
            response.setStatus("success");
            response.setMessage("Product created successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ProductResponseDTO errorResponse = new ProductResponseDTO("error", "Failed to create product: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable String id, @Valid @RequestBody Product productDetails) {
        try {
            Optional<Product> optionalProduct = productRepository.findById(id);
            
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                product.setName(productDetails.getName());
                product.setPrice(productDetails.getPrice());
                product.setDescription(productDetails.getDescription());
                product.setImageUrl(productDetails.getImageUrl());
                product.setPaypalButtonId(productDetails.getPaypalButtonId());
                
                Product updatedProduct = productRepository.save(product);
                ProductResponseDTO response = DTOMapper.toProductResponseDTO(updatedProduct);
                response.setStatus("success");
                response.setMessage("Product updated successfully");
                return ResponseEntity.ok(response);
            } else {
                ProductResponseDTO errorResponse = new ProductResponseDTO("error", "Product not found");
                return ResponseEntity.status(404).body(errorResponse);
            }
        } catch (Exception e) {
            ProductResponseDTO errorResponse = new ProductResponseDTO("error", "Failed to update product: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> deleteProduct(@PathVariable String id) {
        try {
            if (productRepository.existsById(id)) {
                productRepository.deleteById(id);
                ProductResponseDTO response = new ProductResponseDTO("success", "Product deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                ProductResponseDTO errorResponse = new ProductResponseDTO("error", "Product not found");
                return ResponseEntity.status(404).body(errorResponse);
            }
        } catch (Exception e) {
            ProductResponseDTO errorResponse = new ProductResponseDTO("error", "Failed to delete product: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}