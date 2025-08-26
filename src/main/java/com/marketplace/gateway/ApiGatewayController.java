package com.marketplace.gateway;

import com.marketplace.gateway.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/gateway")
public class ApiGatewayController {

    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayController.class);

    @Autowired
    private RateLimitService rateLimitService;

    @RequestMapping(value = "/api/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyApiRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody(required = false) Object body) {
        
        String clientIp = getClientIp(request);
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String method = request.getMethod();
        
        logger.info("Gateway API Request: {} {} from {}", method, path, clientIp);

        // Rate limiting check
        if (!rateLimitService.isAllowed(clientIp, "api")) {
            logger.warn("Rate limit exceeded for IP: {}", clientIp);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Rate limit exceeded. Please try again later.");
            errorResponse.put("code", "RATE_LIMIT_EXCEEDED");
            
            // Add rate limit headers
            response.addHeader("X-Rate-Limit-Status", "exceeded");
            response.addHeader("X-Rate-Limit-Service", "api");
            
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
        }

        // Add gateway headers
        response.addHeader("X-Gateway-Route", "api");
        response.addHeader("X-Gateway-Response", "api-service");
        response.addHeader("X-Gateway-Version", "1.0");
        response.addHeader("X-Client-IP", clientIp);

        // Forward the request internally (no proxy needed for monolith)
        try {
            // Remove /gateway prefix and forward to the actual API
            String forwardPath = path.substring("/gateway".length());
            
            logger.info("Gateway forwarding to: {}", forwardPath);
            
            // Use RequestDispatcher to forward internally
            request.getRequestDispatcher(forwardPath).forward(request, response);
            
            logger.info("Gateway API Response: {} {} -> forwarded successfully", method, path);
            
            // Return null because the response has already been written by the forward
            return null;
            
        } catch (Exception e) {
            logger.error("Gateway error for {} {}: {}", method, path, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Gateway error: " + e.getMessage());
            errorResponse.put("code", "GATEWAY_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @RequestMapping(value = "/admin/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyAdminRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody(required = false) Object body) {
        
        String clientIp = getClientIp(request);
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String method = request.getMethod();
        
        logger.info("Gateway Admin Request: {} {} from {}", method, path, clientIp);

        // Stricter rate limiting for admin endpoints
        if (!rateLimitService.isAllowed(clientIp, "admin")) {
            logger.warn("Admin rate limit exceeded for IP: {}", clientIp);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Admin rate limit exceeded. Please try again later.");
            errorResponse.put("code", "ADMIN_RATE_LIMIT_EXCEEDED");
            
            response.addHeader("X-Rate-Limit-Status", "exceeded");
            response.addHeader("X-Rate-Limit-Service", "admin");
            
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
        }

        // Add gateway headers
        response.addHeader("X-Gateway-Route", "admin");
        response.addHeader("X-Gateway-Response", "admin-service");
        response.addHeader("X-Gateway-Version", "1.0");
        response.addHeader("X-Client-IP", clientIp);

        try {
            String forwardPath = path.substring("/gateway".length());
            logger.info("Gateway forwarding admin request to: {}", forwardPath);
            
            request.getRequestDispatcher(forwardPath).forward(request, response);
            
            logger.info("Gateway Admin Response: {} {} -> forwarded successfully", method, path);
            return null;
            
        } catch (Exception e) {
            logger.error("Gateway admin error: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Admin gateway error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getGatewayInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", "active");
        info.put("version", "1.0");
        info.put("type", "Custom Spring MVC Gateway");
        info.put("description", "TechMarket Pro API Gateway");
        info.put("architecture", "Monolithic with Internal Forwarding");
        info.put("features", new String[]{
            "Rate Limiting",
            "CORS Support", 
            "Security Headers",
            "Request Logging",
            "Internal Request Forwarding"
        });
        info.put("endpoints", new String[]{
            "/gateway/api/** - API endpoints with rate limiting",
            "/gateway/admin/** - Admin endpoints with strict rate limiting",
            "/gateway/info - Gateway information",
            "/gateway/health - Gateway health status"
        });
        info.put("rate_limiting", Map.of(
            "api_limit", "100 requests/minute",
            "admin_limit", "20 requests/minute",
            "payment_limit", "10 requests/minute"
        ));
        
        return ResponseEntity.ok(info);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getGatewayHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("gateway", "HEALTHY");
        health.put("timestamp", System.currentTimeMillis());
        health.put("rate_limiting", rateLimitService.isHealthy());
        health.put("rate_limiting_status", rateLimitService.getStatus());
        
        return ResponseEntity.ok(health);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}