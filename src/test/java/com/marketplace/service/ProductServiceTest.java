package com.marketplace.service;

import com.marketplace.config.DatabaseConfig;
import com.marketplace.model.Product;
import com.marketplace.model.ProductMongo;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.ProductMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private DatabaseConfig databaseConfig;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMongoRepository productMongoRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductMongo testProductMongo;
    private ProductService.ProductDto testProductDto;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId("1");
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setDescription("Test Description");
        testProduct.setImageUrl("http://test.com/image.jpg");
        testProduct.setPaypalButtonId("button-123");

        testProductMongo = new ProductMongo();
        testProductMongo.setId("mongo-1");
        testProductMongo.setName("Test Mongo Product");
        testProductMongo.setPrice(new BigDecimal("199.99"));
        testProductMongo.setDescription("Test Mongo Description");
        testProductMongo.setImageUrl("http://test.com/mongo-image.jpg");
        testProductMongo.setPaypalButtonId("mongo-button-123");

        testProductDto = new ProductService.ProductDto();
        testProductDto.setId("1");
        testProductDto.setName("Test Product DTO");
        testProductDto.setPrice(new BigDecimal("299.99"));
        testProductDto.setDescription("Test DTO Description");
        testProductDto.setImageUrl("http://test.com/dto-image.jpg");
        testProductDto.setPaypalButtonId("dto-button-123");
    }

    @Test
    void findAll_withH2Database_shouldReturnProductList() {
        // Given
        when(databaseConfig.isMongoEnabled()).thenReturn(false);
        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct));

        // When
        List<ProductService.ProductDto> result = productService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        verify(productRepository).findAll();
        verify(productMongoRepository, never()).findAll();
    }

    @Test
    void findAll_withMongoDB_shouldReturnProductList() {
        // Given
        when(databaseConfig.isMongoEnabled()).thenReturn(true);
        when(productMongoRepository.findAll()).thenReturn(Arrays.asList(testProductMongo));

        // When
        List<ProductService.ProductDto> result = productService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Mongo Product", result.get(0).getName());
        verify(productMongoRepository).findAll();
        verify(productRepository, never()).findAll();
    }

    @Test
    void findById_withH2Database_shouldReturnProduct() {
        // Given
        when(databaseConfig.isMongoEnabled()).thenReturn(false);
        when(productRepository.findById("1")).thenReturn(Optional.of(testProduct));

        // When
        Optional<ProductService.ProductDto> result = productService.findById("1");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Product", result.get().getName());
        verify(productRepository).findById("1");
    }

    @Test
    void findById_withMongoDB_shouldReturnProduct() {
        // Given
        when(databaseConfig.isMongoEnabled()).thenReturn(true);
        when(productMongoRepository.findById("mongo-1")).thenReturn(Optional.of(testProductMongo));

        // When
        Optional<ProductService.ProductDto> result = productService.findById("mongo-1");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Mongo Product", result.get().getName());
        verify(productMongoRepository).findById("mongo-1");
    }

    @Test
    void findById_withInvalidId_shouldReturnEmpty() {
        // Given
        when(databaseConfig.isMongoEnabled()).thenReturn(false);
        when(productRepository.findById("invalid")).thenThrow(new NumberFormatException());

        // When
        Optional<ProductService.ProductDto> result = productService.findById("invalid");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void save_withH2Database_shouldSaveProduct() {
        // Given
        when(databaseConfig.isMongoEnabled()).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        ProductService.ProductDto result = productService.save(testProductDto);

        // Then
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void save_withMongoDB_shouldSaveProduct() {
        // Given
        when(databaseConfig.isMongoEnabled()).thenReturn(true);
        when(productMongoRepository.save(any(ProductMongo.class))).thenReturn(testProductMongo);

        // When
        ProductService.ProductDto result = productService.save(testProductDto);

        // Then
        assertNotNull(result);
        assertEquals("Test Mongo Product", result.getName());
        verify(productMongoRepository).save(any(ProductMongo.class));
    }

    @Test
    void deleteById_withH2Database_shouldDeleteProduct() {
        // Given
        when(databaseConfig.isMongoEnabled()).thenReturn(false);

        // When
        productService.deleteById("1");

        // Then
        verify(productRepository).deleteById("1");
    }

    @Test
    void deleteById_withMongoDB_shouldDeleteProduct() {
        // Given
        when(databaseConfig.isMongoEnabled()).thenReturn(true);

        // When
        productService.deleteById("mongo-1");

        // Then
        verify(productMongoRepository).deleteById("mongo-1");
    }

    @Test
    void deleteById_withInvalidId_shouldHandleException() {
        // Given
        when(databaseConfig.isMongoEnabled()).thenReturn(false);
        doThrow(new NumberFormatException()).when(productRepository).deleteById("invalid");

        // When & Then
        assertDoesNotThrow(() -> productService.deleteById("invalid"));
    }

    @Test
    void existsById_withH2Database_shouldReturnTrue() {
        // Given
        when(databaseConfig.isMongoEnabled()).thenReturn(false);
        when(productRepository.existsById("1")).thenReturn(true);

        // When
        boolean result = productService.existsById("1");

        // Then
        assertTrue(result);
        verify(productRepository).existsById("1");
    }

    @Test
    void existsById_withMongoDB_shouldReturnTrue() {
        // Given
        when(databaseConfig.isMongoEnabled()).thenReturn(true);
        when(productMongoRepository.existsById("mongo-1")).thenReturn(true);

        // When
        boolean result = productService.existsById("mongo-1");

        // Then
        assertTrue(result);
        verify(productMongoRepository).existsById("mongo-1");
    }

    @Test
    void existsById_withInvalidId_shouldReturnFalse() {
        // Given
        when(databaseConfig.isMongoEnabled()).thenReturn(false);
        when(productRepository.existsById("invalid")).thenThrow(new NumberFormatException());

        // When
        boolean result = productService.existsById("invalid");

        // Then
        assertFalse(result);
    }

    @Test
    void productDto_gettersAndSetters_shouldWorkCorrectly() {
        // Given
        ProductService.ProductDto dto = new ProductService.ProductDto();

        // When
        dto.setId("test-id");
        dto.setName("test-name");
        dto.setPrice(new BigDecimal("123.45"));
        dto.setDescription("test-description");
        dto.setImageUrl("test-image-url");
        dto.setPaypalButtonId("test-button-id");

        // Then
        assertEquals("test-id", dto.getId());
        assertEquals("test-name", dto.getName());
        assertEquals(new BigDecimal("123.45"), dto.getPrice());
        assertEquals("test-description", dto.getDescription());
        assertEquals("test-image-url", dto.getImageUrl());
        assertEquals("test-button-id", dto.getPaypalButtonId());
    }
}