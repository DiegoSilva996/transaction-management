package com.nttdata.transactionmanagement.Service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nttdata.transactionmanagement.Repository.masterValuesRepository;
import com.nttdata.transactionmanagement.redis.model.MasterValuesCache;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class masterValuesService {
    
    @Autowired
    private masterValuesRepository repository;

 
    public List<MasterValuesCache> getAll() {
        try {
          List<MasterValuesCache> assuranceCacheList = repository.findAll().collectList().block();          
          return assuranceCacheList;
        } catch (Exception e) {
          return Collections.EMPTY_LIST;
        }
    }
    
    public String storageMasterValueList(List<MasterValuesCache> masterValuesCacheList) {
        try {
            Iterable<MasterValuesCache> iterable = masterValuesCacheList;
            repository.saveAll(iterable);
            return "Master values list create successfully";
        } catch (Exception e) {
            return "Error saving master values cache list. " + e.getMessage();
        }
    }

}
