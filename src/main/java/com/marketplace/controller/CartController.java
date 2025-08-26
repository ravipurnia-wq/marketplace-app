package com.marketplace.controller;

import com.marketplace.dto.*;
import com.marketplace.dto.mapper.DTOMapper;
import com.marketplace.model.Cart;
import com.marketplace.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @GetMapping
    public String viewCart(Model model) {
        Cart cart = cartService.getCurrentUserCart();
        model.addAttribute("cart", cart);
        return "cart";
    }
    
    @PostMapping("/add")
    public String addToCart(@RequestParam String productId, 
                           @RequestParam(defaultValue = "1") int quantity) {
        try {
            cartService.addToCart(productId, quantity);
            return "redirect:/cart";
        } catch (RuntimeException e) {
            return "redirect:/login";
        }
    }
    
    @PostMapping("/update")
    public String updateCartItem(@RequestParam String productId, 
                                @RequestParam int quantity) {
        try {
            cartService.updateCartItemQuantity(productId, quantity);
        } catch (RuntimeException e) {
            // Handle error
        }
        return "redirect:/cart";
    }
    
    @PostMapping("/remove")
    public String removeFromCart(@RequestParam String productId) {
        try {
            cartService.removeFromCart(productId);
        } catch (RuntimeException e) {
            // Handle error
        }
        return "redirect:/cart";
    }
    
    @PostMapping("/clear")
    public String clearCart() {
        cartService.clearCurrentUserCart();
        return "redirect:/cart";
    }
}

@RestController
@RequestMapping("/api/cart")
class CartRestController {
    
    @Autowired
    private CartService cartService;
    
    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart() {
        try {
            Cart cart = cartService.getCurrentUserCart();
            CartResponseDTO response = DTOMapper.toCartResponseDTO(cart);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CartResponseDTO errorResponse = new CartResponseDTO("error", "Failed to fetch cart: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping("/add")
    public ResponseEntity<CartResponseDTO> addToCart(@RequestParam String productId, 
                         @RequestParam(defaultValue = "1") int quantity) {
        try {
            Cart cart = cartService.addToCart(productId, quantity);
            CartResponseDTO response = DTOMapper.toCartResponseDTO(cart);
            response.setStatus("success");
            response.setMessage("Item added to cart successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CartResponseDTO errorResponse = new CartResponseDTO("error", "Failed to add item to cart: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PutMapping("/update")
    public ResponseEntity<CartResponseDTO> updateCartItem(@RequestParam String productId, 
                              @RequestParam int quantity) {
        try {
            Cart cart = cartService.updateCartItemQuantity(productId, quantity);
            CartResponseDTO response = DTOMapper.toCartResponseDTO(cart);
            response.setStatus("success");
            response.setMessage("Cart item updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CartResponseDTO errorResponse = new CartResponseDTO("error", "Failed to update cart item: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @DeleteMapping("/remove")
    public ResponseEntity<CartResponseDTO> removeFromCart(@RequestParam String productId) {
        try {
            Cart cart = cartService.removeFromCart(productId);
            CartResponseDTO response = DTOMapper.toCartResponseDTO(cart);
            response.setStatus("success");
            response.setMessage("Item removed from cart successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CartResponseDTO errorResponse = new CartResponseDTO("error", "Failed to remove item from cart: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @DeleteMapping("/clear")
    public ResponseEntity<CartResponseDTO> clearCart() {
        try {
            cartService.clearCurrentUserCart();
            CartResponseDTO response = new CartResponseDTO("success", "Cart cleared successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CartResponseDTO errorResponse = new CartResponseDTO("error", "Failed to clear cart: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}