package com.nttdata.transactionmanagement.Repository;

import com.nttdata.transactionmanagement.Model.Transaction;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface transactionRepository extends ReactiveMongoRepository <Transaction, String>{
    
}
