package com.marketplace.repository;

import com.marketplace.model.Order;
import com.marketplace.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
    
    Page<Order> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
    
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    long countByStatus(OrderStatus status);
}