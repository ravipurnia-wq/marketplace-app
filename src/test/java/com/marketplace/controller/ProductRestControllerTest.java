package com.marketplace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.model.Product;
import com.marketplace.repository.ProductRepository;
import com.marketplace.dto.mapper.DTOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductRestController.class)
@ActiveProfiles("test")
class ProductRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId("1");
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setDescription("Test Description");
        testProduct.setImageUrl("http://test.com/image.jpg");
        testProduct.setPaypalButtonId("button-123");
    }

    @Test
    void getAllProducts_shouldReturnProductList() throws Exception {
        // Given
        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct));

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products[0].name").value("Test Product"));

        verify(productRepository).findAll();
    }

    @Test
    void getAllProducts_whenExceptionThrown_shouldReturnError() throws Exception {
        // Given
        when(productRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Failed to fetch products: Database error"));
    }


    @Test
    void getProductById_withInvalidId_shouldReturnNotFound() throws Exception {
        // Given
        when(productRepository.findById("999")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test
    void getProductById_whenExceptionThrown_shouldReturnError() throws Exception {
        // Given
        when(productRepository.findById("1")).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Failed to fetch product: Database error"));
    }

    @Test
    void createProduct_withValidProduct_shouldCreateProduct() throws Exception {
        // Given
        Product newProduct = new Product();
        newProduct.setName("New Product");
        newProduct.setPrice(new BigDecimal("199.99"));
        newProduct.setDescription("New Description");
        newProduct.setImageUrl("http://test.com/new-image.jpg");
        newProduct.setPaypalButtonId("new-button-123");

        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Product created successfully"));

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_whenExceptionThrown_shouldReturnError() throws Exception {
        // Given
        Product newProduct = new Product();
        newProduct.setName("New Product");
        newProduct.setPrice(new BigDecimal("199.99"));

        when(productRepository.save(any(Product.class))).thenThrow(new RuntimeException("Save error"));

        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Failed to create product: Save error"));
    }

    @Test
    void updateProduct_withValidProduct_shouldUpdateProduct() throws Exception {
        // Given
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Product");
        updatedProduct.setPrice(new BigDecimal("299.99"));
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setImageUrl("http://test.com/updated-image.jpg");
        updatedProduct.setPaypalButtonId("updated-button-123");

        when(productRepository.findById("1")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When & Then
        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Product updated successfully"));

        verify(productRepository).findById("1");
        verify(productRepository).save(any(Product.class));
    }



    @Test
    void deleteProduct_withValidId_shouldDeleteProduct() throws Exception {
        // Given
        when(productRepository.existsById("1")).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Product deleted successfully"));

        verify(productRepository).existsById("1");
        verify(productRepository).deleteById("1");
    }

    @Test
    void deleteProduct_withInvalidId_shouldReturnNotFound() throws Exception {
        // Given
        when(productRepository.existsById("999")).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Product not found"));

        verify(productRepository).existsById("999");
        verify(productRepository, never()).deleteById("999");
    }

    @Test
    void deleteProduct_whenExceptionThrown_shouldReturnError() throws Exception {
        // Given
        when(productRepository.existsById("1")).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Failed to delete product: Database error"));
    }
}