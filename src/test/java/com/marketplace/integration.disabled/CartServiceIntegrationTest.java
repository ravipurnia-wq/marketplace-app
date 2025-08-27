package com.marketplace.integration;

import com.marketplace.model.*;
import com.marketplace.repository.*;
import com.marketplace.service.CartService;
import com.marketplace.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Transactional
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
class CartServiceIntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Clear all data
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded-password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setAddress("123 Test St");
        testUser.setPhoneNumber("555-1234");
        
        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        testUser.setRoles(roles);
        testUser.setCreatedAt(LocalDateTime.now());
        
        testUser = userRepository.save(testUser);

        // Create test product
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setDescription("Test Description");
        testProduct.setImageUrl("http://test.com/image.jpg");
        testProduct.setPaypalButtonId("button-123");
        
        testProduct = productRepository.save(testProduct);
    }

    @Test
    @WithMockUser(username = "testuser")
    void addToCart_fullIntegrationFlow_shouldWorkCorrectly() {
        // Initially no cart should exist
        assertEquals(0, cartRepository.count());

        // Add item to cart
        Cart cart = cartService.addToCart(testProduct.getId(), 2);

        // Verify cart was created and saved
        assertNotNull(cart);
        assertEquals(testUser.getId(), cart.getUserId());
        assertEquals(1, cart.getItems().size());
        assertEquals(1, cartRepository.count());

        // Verify cart item details
        CartItem item = cart.getItems().get(0);
        assertEquals(testProduct.getId(), item.getProductId());
        assertEquals(testProduct.getName(), item.getProductName());
        assertEquals(testProduct.getPrice(), item.getProductPrice());
        assertEquals(2, item.getQuantity());

        // Verify total calculation
        BigDecimal expectedTotal = testProduct.getPrice().multiply(new BigDecimal("2"));
        assertEquals(expectedTotal, cart.getTotal());

        // Add another item to existing cart
        cart = cartService.addToCart(testProduct.getId(), 1);
        assertEquals(3, cart.getItems().get(0).getQuantity());

        // Verify cart is still saved in database
        Cart dbCart = cartRepository.findByUserId(testUser.getId()).orElse(null);
        assertNotNull(dbCart);
        assertEquals(3, dbCart.getItems().get(0).getQuantity());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateCartItemQuantity_shouldUpdateAndSave() {
        // First add item to cart
        cartService.addToCart(testProduct.getId(), 2);

        // Update quantity
        Cart updatedCart = cartService.updateCartItemQuantity(testProduct.getId(), 5);

        // Verify update
        assertEquals(1, updatedCart.getItems().size());
        assertEquals(5, updatedCart.getItems().get(0).getQuantity());

        // Verify in database
        Cart dbCart = cartRepository.findByUserId(testUser.getId()).orElse(null);
        assertNotNull(dbCart);
        assertEquals(5, dbCart.getItems().get(0).getQuantity());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateCartItemQuantity_toZero_shouldRemoveItemAndDeleteCart() {
        // Add item to cart
        cartService.addToCart(testProduct.getId(), 2);
        assertEquals(1, cartRepository.count());

        // Update quantity to 0
        Cart updatedCart = cartService.updateCartItemQuantity(testProduct.getId(), 0);

        // Verify cart is empty but not null
        assertNotNull(updatedCart);
        assertTrue(updatedCart.isEmpty());

        // Verify cart was deleted from database
        assertEquals(0, cartRepository.count());
    }

    @Test
    @WithMockUser(username = "testuser")
    void removeFromCart_shouldRemoveItemAndDeleteEmptyCart() {
        // Add item to cart
        cartService.addToCart(testProduct.getId(), 2);
        assertEquals(1, cartRepository.count());

        // Remove item
        Cart updatedCart = cartService.removeFromCart(testProduct.getId());

        // Verify cart is empty
        assertNotNull(updatedCart);
        assertTrue(updatedCart.isEmpty());

        // Verify cart was deleted from database
        assertEquals(0, cartRepository.count());
    }

    @Test
    @WithMockUser(username = "testuser")
    void clearCart_shouldDeleteCartFromDatabase() {
        // Add item to cart
        cartService.addToCart(testProduct.getId(), 2);
        assertEquals(1, cartRepository.count());

        // Clear cart
        cartService.clearCurrentUserCart();

        // Verify cart was deleted
        assertEquals(0, cartRepository.count());
        assertFalse(cartRepository.findByUserId(testUser.getId()).isPresent());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCurrentUserCart_withExistingCart_shouldReturnPersistedCart() {
        // Add item to create cart
        cartService.addToCart(testProduct.getId(), 3);

        // Get current cart
        Cart currentCart = cartService.getCurrentUserCart();

        // Verify it returns the persisted cart
        assertNotNull(currentCart);
        assertEquals(testUser.getId(), currentCart.getUserId());
        assertEquals(1, currentCart.getItems().size());
        assertEquals(3, currentCart.getItems().get(0).getQuantity());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCurrentUserCart_withNoExistingCart_shouldReturnNewEmptyCart() {
        // Get current cart when none exists
        Cart currentCart = cartService.getCurrentUserCart();

        // Verify it returns new empty cart
        assertNotNull(currentCart);
        assertEquals(testUser.getId(), currentCart.getUserId());
        assertTrue(currentCart.isEmpty());
    }

    @Test
    @WithMockUser(username = "testuser")
    void addToCart_withNonExistentProduct_shouldThrowException() {
        // Try to add non-existent product
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> cartService.addToCart("non-existent-id", 1));
        
        assertEquals("Product not found", exception.getMessage());
        
        // Verify no cart was created
        assertEquals(0, cartRepository.count());
    }

    @Test
    void addToCart_withNoUserLoggedIn_shouldThrowException() {
        // Try to add to cart without being logged in
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> cartService.addToCart(testProduct.getId(), 1));
        
        assertEquals("User must be logged in to add items to cart", exception.getMessage());
    }

    @Test
    @WithMockUser(username = "testuser")
    void multipleProducts_inSameCart_shouldWorkCorrectly() {
        // Create second product
        Product product2 = new Product();
        product2.setName("Second Product");
        product2.setPrice(new BigDecimal("149.99"));
        product2.setDescription("Second Description");
        product2.setImageUrl("http://test.com/image2.jpg");
        product2.setPaypalButtonId("button-456");
        product2 = productRepository.save(product2);

        // Add both products to cart
        cartService.addToCart(testProduct.getId(), 2);
        Cart cart = cartService.addToCart(product2.getId(), 1);

        // Verify cart contains both items
        assertEquals(2, cart.getItems().size());

        // Verify total calculation
        BigDecimal expectedTotal = testProduct.getPrice().multiply(new BigDecimal("2"))
                .add(product2.getPrice());
        assertEquals(expectedTotal, cart.getTotal());

        // Verify persistence
        Cart dbCart = cartRepository.findByUserId(testUser.getId()).orElse(null);
        assertNotNull(dbCart);
        assertEquals(2, dbCart.getItems().size());
        assertEquals(expectedTotal, dbCart.getTotal());
    }
}