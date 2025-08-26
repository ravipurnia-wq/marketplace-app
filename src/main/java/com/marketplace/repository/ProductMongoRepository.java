package com.marketplace.repository;

import com.marketplace.model.ProductMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductMongoRepository extends MongoRepository<ProductMongo, String> {
    List<ProductMongo> findByNameContainingIgnoreCase(String name);
    List<ProductMongo> findByPriceLessThanEqual(Double price);
}