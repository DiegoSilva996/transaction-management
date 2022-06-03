package com.nttdata.transactionmanagement.Repository;

import java.util.List;

import com.nttdata.transactionmanagement.Model.Product;

import reactor.core.publisher.Mono;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface productRepository extends ReactiveMongoRepository <Product, String> {
    Mono<Product> findByPhoneNumber(String phoneNumber);
    Mono<Product> findById(String Id);
    List<Product> findByClientId(String clientId);
    List<Product> findByProductTypeAndStatus(String ProductType, String Status);
    List<Product> findByProductTypeAndClientId (String ProductType, String clientId);    

}
