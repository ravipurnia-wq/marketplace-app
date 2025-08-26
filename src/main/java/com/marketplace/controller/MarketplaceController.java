package com.marketplace.controller;

import com.marketplace.model.Product;
import com.marketplace.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MarketplaceController {
    
    @Autowired
    private ProductRepository productRepository;
    
    @GetMapping("/")
    public String home(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "marketplace";
    }
    
    @GetMapping("/products")
    public String products(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "products";
    }
    
    @GetMapping("/about")
    public String about() {
        return "about";
    }
    
    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
    
    @GetMapping("/admin")
    public String admin(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "admin";
    }
    
    @GetMapping("/payment-success")
    public String paymentSuccess() {
        return "payment-success";
    }
    
    @GetMapping("/payment-cancelled")
    public String paymentCancelled() {
        return "payment-cancelled";
    }
}