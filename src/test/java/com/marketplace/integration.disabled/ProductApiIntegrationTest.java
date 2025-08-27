package com.marketplace.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.model.Product;
import com.marketplace.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
class ProductApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        
        testProduct = new Product();
        testProduct.setName("Integration Test Product");
        testProduct.setPrice(new BigDecimal("199.99"));
        testProduct.setDescription("Integration test description");
        testProduct.setImageUrl("http://test.com/integration-image.jpg");
        testProduct.setPaypalButtonId("integration-button-123");
    }

    @Test
    void fullProductLifecycle_shouldWorkCorrectly() throws Exception {
        // 1. Create a product
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Product created successfully"));

        // Verify product was created in database
        assertEquals(1, productRepository.count());
        Product createdProduct = productRepository.findAll().get(0);
        assertNotNull(createdProduct.getId());
        assertEquals("Integration Test Product", createdProduct.getName());

        // 2. Get all products
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products[0].name").value("Integration Test Product"));

        // 3. Get product by ID
        String productId = createdProduct.getId();
        mockMvc.perform(get("/api/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.product.name").value("Integration Test Product"))
                .andExpect(jsonPath("$.product.price").value(199.99));

        // 4. Update the product
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Integration Product");
        updatedProduct.setPrice(new BigDecimal("299.99"));
        updatedProduct.setDescription("Updated description");
        updatedProduct.setImageUrl("http://test.com/updated-image.jpg");
        updatedProduct.setPaypalButtonId("updated-button-123");

        mockMvc.perform(put("/api/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Product updated successfully"));

        // Verify update in database
        Product dbProduct = productRepository.findById(productId).orElse(null);
        assertNotNull(dbProduct);
        assertEquals("Updated Integration Product", dbProduct.getName());
        assertEquals(new BigDecimal("299.99"), dbProduct.getPrice());

        // 5. Delete the product
        mockMvc.perform(delete("/api/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Product deleted successfully"));

        // Verify deletion in database
        assertEquals(0, productRepository.count());
        assertFalse(productRepository.existsById(productId));
    }

    @Test
    void getProductById_withNonExistentId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test
    void updateProduct_withNonExistentId_shouldReturn404() throws Exception {
        Product updateData = new Product();
        updateData.setName("Updated Product");
        updateData.setPrice(new BigDecimal("99.99"));

        mockMvc.perform(put("/api/products/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test
    void deleteProduct_withNonExistentId_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test
    void createProduct_withInvalidData_shouldReturnValidationError() throws Exception {
        Product invalidProduct = new Product();
        // Missing required fields

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void multipleProducts_shouldMaintainConsistency() throws Exception {
        // Create multiple products
        for (int i = 1; i <= 3; i++) {
            Product product = new Product();
            product.setName("Product " + i);
            product.setPrice(new BigDecimal(String.valueOf(100 * i)));
            product.setDescription("Description " + i);
            product.setImageUrl("http://test.com/image" + i + ".jpg");
            product.setPaypalButtonId("button-" + i);

            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(product)))
                    .andExpect(status().isCreated());
        }

        // Verify all products were created
        assertEquals(3, productRepository.count());

        // Get all products and verify order and content
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(3));

        // Test pagination behavior if implemented
        var products = productRepository.findAll();
        assertEquals(3, products.size());

        // Verify each product has correct data
        boolean foundProduct1 = false, foundProduct2 = false, foundProduct3 = false;
        for (Product p : products) {
            if ("Product 1".equals(p.getName())) foundProduct1 = true;
            if ("Product 2".equals(p.getName())) foundProduct2 = true;
            if ("Product 3".equals(p.getName())) foundProduct3 = true;
        }
        assertTrue(foundProduct1 && foundProduct2 && foundProduct3);
    }
}