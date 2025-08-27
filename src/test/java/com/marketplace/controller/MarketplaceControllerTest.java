package com.marketplace.controller;

import com.marketplace.model.Product;
import com.marketplace.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MarketplaceController.class)
@ActiveProfiles("test")
class MarketplaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository;

    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        testProduct1 = new Product();
        testProduct1.setId("1");
        testProduct1.setName("Test Product 1");
        testProduct1.setPrice(new BigDecimal("99.99"));
        testProduct1.setDescription("Test Description 1");
        testProduct1.setImageUrl("http://test.com/image1.jpg");

        testProduct2 = new Product();
        testProduct2.setId("2");
        testProduct2.setName("Test Product 2");
        testProduct2.setPrice(new BigDecimal("199.99"));
        testProduct2.setDescription("Test Description 2");
        testProduct2.setImageUrl("http://test.com/image2.jpg");
    }

    @Test
    void home_shouldReturnMarketplaceViewWithProducts() throws Exception {
        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct1, testProduct2));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("marketplace"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attribute("products", Arrays.asList(testProduct1, testProduct2)));

        verify(productRepository).findAll();
    }

    @Test
    void home_withEmptyProductList_shouldReturnMarketplaceViewWithEmptyList() throws Exception {
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("marketplace"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attribute("products", Collections.emptyList()));

        verify(productRepository).findAll();
    }

    @Test
    void products_shouldReturnProductsViewWithProducts() throws Exception {
        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct1, testProduct2));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attribute("products", Arrays.asList(testProduct1, testProduct2)));

        verify(productRepository).findAll();
    }

    @Test
    void products_withEmptyProductList_shouldReturnProductsViewWithEmptyList() throws Exception {
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attribute("products", Collections.emptyList()));

        verify(productRepository).findAll();
    }

    @Test
    void about_shouldReturnAboutView() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"));

        verifyNoInteractions(productRepository);
    }

    @Test
    void contact_shouldReturnContactView() throws Exception {
        mockMvc.perform(get("/contact"))
                .andExpect(status().isOk())
                .andExpect(view().name("contact"));

        verifyNoInteractions(productRepository);
    }

    @Test
    void admin_shouldReturnAdminViewWithProducts() throws Exception {
        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct1, testProduct2));

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attribute("products", Arrays.asList(testProduct1, testProduct2)));

        verify(productRepository).findAll();
    }

    @Test
    void admin_withEmptyProductList_shouldReturnAdminViewWithEmptyList() throws Exception {
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attribute("products", Collections.emptyList()));

        verify(productRepository).findAll();
    }

    @Test
    void paymentSuccess_shouldReturnPaymentSuccessView() throws Exception {
        mockMvc.perform(get("/payment-success"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment-success"));

        verifyNoInteractions(productRepository);
    }

    @Test
    void paymentCancelled_shouldReturnPaymentCancelledView() throws Exception {
        mockMvc.perform(get("/payment-cancelled"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment-cancelled"));

        verifyNoInteractions(productRepository);
    }

    @Test
    void home_whenRepositoryThrowsException_shouldPropagateException() throws Exception {
        when(productRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/"))
                .andExpect(status().is5xxServerError());

        verify(productRepository).findAll();
    }
}