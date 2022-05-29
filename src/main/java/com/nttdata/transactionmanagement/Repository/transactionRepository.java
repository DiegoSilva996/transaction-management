package com.nttdata.transactionmanagement.Repository;

import java.util.List;

import com.nttdata.transactionmanagement.Model.Transaction;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface transactionRepository extends ReactiveMongoRepository <Transaction, String>{
    List<Transaction> findByIdProduct (String IdProduct);
    List<Transaction> findByTransactionTypeAndIdProduct (String transactionType, String IdProduct);
}
