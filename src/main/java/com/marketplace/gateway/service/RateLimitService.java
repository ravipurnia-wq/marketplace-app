package com.marketplace.gateway.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);

    // Rate limits per minute
    private static final int API_RATE_LIMIT = 100;
    private static final int ADMIN_RATE_LIMIT = 20;
    private static final int PAYMENT_RATE_LIMIT = 10;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    // Fallback in-memory cache if Redis is not available
    private final Cache<String, AtomicInteger> inMemoryCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    private boolean redisAvailable = false;

    public boolean isAllowed(String clientId, String serviceType) {
        int limit = getRateLimit(serviceType);
        String key = "rate_limit:" + serviceType + ":" + clientId;

        try {
            if (redisTemplate != null && !redisAvailable) {
                // Test Redis connectivity
                redisTemplate.opsForValue().get("test");
                redisAvailable = true;
            }

            if (redisAvailable && redisTemplate != null) {
                return checkRateLimitWithRedis(key, limit);
            } else {
                return checkRateLimitInMemory(key, limit);
            }
        } catch (Exception e) {
            logger.warn("Rate limiting error, falling back to in-memory: {}", e.getMessage());
            redisAvailable = false;
            return checkRateLimitInMemory(key, limit);
        }
    }

    private boolean checkRateLimitWithRedis(String key, int limit) {
        try {
            Long currentCount = redisTemplate.opsForValue().increment(key);
            
            if (currentCount == 1) {
                // First request in this window, set expiration
                redisTemplate.expire(key, Duration.ofMinutes(1));
            }

            boolean allowed = currentCount <= limit;
            
            if (!allowed) {
                logger.debug("Rate limit exceeded for key: {} (count: {}, limit: {})", key, currentCount, limit);
            }
            
            return allowed;
        } catch (Exception e) {
            logger.error("Redis rate limiting error: {}", e.getMessage());
            redisAvailable = false;
            throw e;
        }
    }

    private boolean checkRateLimitInMemory(String key, int limit) {
        try {
            AtomicInteger counter = inMemoryCache.get(key, AtomicInteger::new);
            int currentCount = counter.incrementAndGet();
            
            boolean allowed = currentCount <= limit;
            
            if (!allowed) {
                logger.debug("In-memory rate limit exceeded for key: {} (count: {}, limit: {})", key, currentCount, limit);
            }
            
            return allowed;
        } catch (Exception e) {
            logger.error("In-memory rate limiting error: {}", e.getMessage());
            // If all else fails, allow the request
            return true;
        }
    }

    private int getRateLimit(String serviceType) {
        switch (serviceType.toLowerCase()) {
            case "admin":
                return ADMIN_RATE_LIMIT;
            case "payment":
            case "payments":
                return PAYMENT_RATE_LIMIT;
            case "api":
            default:
                return API_RATE_LIMIT;
        }
    }

    public boolean isHealthy() {
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForValue().get("health_check");
                return true;
            }
            // In-memory cache is always healthy
            return true;
        } catch (Exception e) {
            logger.debug("Rate limiting service health check failed: {}", e.getMessage());
            return inMemoryCache != null; // Return true if in-memory cache is available
        }
    }

    public String getStatus() {
        if (redisAvailable && redisTemplate != null) {
            return "Redis-backed rate limiting active";
        } else {
            return "In-memory rate limiting active (Redis unavailable)";
        }
    }
}