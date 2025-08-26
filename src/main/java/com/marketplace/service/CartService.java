package com.marketplace.service;

import com.marketplace.model.Cart;
import com.marketplace.model.CartItem;
import com.marketplace.model.Product;
import com.marketplace.repository.CartRepository;
import com.marketplace.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserService userService;
    
    public Cart getOrCreateCart(String userId) {
        Optional<Cart> existingCart = cartRepository.findByUserId(userId);
        return existingCart.orElse(new Cart(userId));
    }
    
    public Cart getCurrentUserCart() {
        String userId = userService.getCurrentUserId();
        if (userId == null) {
            return new Cart();
        }
        return getOrCreateCart(userId);
    }
    
    public Cart addToCart(String productId, int quantity) {
        String userId = userService.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User must be logged in to add items to cart");
        }
        
        Optional<Product> productOpt = productRepository.findById(productId);
        if (!productOpt.isPresent()) {
            throw new RuntimeException("Product not found");
        }
        
        Product product = productOpt.get();
        Cart cart = getOrCreateCart(userId);
        
        CartItem cartItem = new CartItem(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getImageUrl(),
            quantity
        );
        
        cart.addItem(cartItem);
        return cartRepository.save(cart);
    }
    
    public Cart updateCartItemQuantity(String productId, int quantity) {
        String userId = userService.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User must be logged in");
        }
        
        Cart cart = getOrCreateCart(userId);
        cart.updateItemQuantity(productId, quantity);
        
        if (cart.isEmpty()) {
            cartRepository.deleteByUserId(userId);
            return new Cart(userId);
        }
        
        return cartRepository.save(cart);
    }
    
    public Cart removeFromCart(String productId) {
        String userId = userService.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("User must be logged in");
        }
        
        Cart cart = getOrCreateCart(userId);
        cart.removeItem(productId);
        
        if (cart.isEmpty()) {
            cartRepository.deleteByUserId(userId);
            return new Cart(userId);
        }
        
        return cartRepository.save(cart);
    }
    
    public void clearCart(String userId) {
        cartRepository.deleteByUserId(userId);
    }
    
    public void clearCurrentUserCart() {
        String userId = userService.getCurrentUserId();
        if (userId != null) {
            clearCart(userId);
        }
    }
}