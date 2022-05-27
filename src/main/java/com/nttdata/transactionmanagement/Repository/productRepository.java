package com.nttdata.transactionmanagement.Repository;

import com.nttdata.transactionmanagement.Model.Product;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface productRepository extends ReactiveMongoRepository <Product, String> {
    
}
