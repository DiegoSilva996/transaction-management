package com.nttdata.transactionmanagement.Repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.nttdata.transactionmanagement.Model.MasterValues;
import com.nttdata.transactionmanagement.redis.model.MasterValuesCache;

@Repository
public interface masterValuesRepository  extends ReactiveMongoRepository <MasterValues, String> {
    
}
