package com.nttdata.transactionmanagement.Service;

import com.nttdata.transactionmanagement.Dto.TransactionDto;
import com.nttdata.transactionmanagement.Repository.transactionRepository;
import com.nttdata.transactionmanagement.Util.AppUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class transactionService {
    @Autowired
    private transactionRepository transactionRepository; 

    public Flux<TransactionDto> getAll(){
		return transactionRepository.findAll().map(AppUtils::transactionEntitytoDto);
	}

    public Mono<TransactionDto> getTransaction(String id){
		return transactionRepository.findById(id).map(AppUtils::transactionEntitytoDto);
	}
	
	public Mono<TransactionDto> saveTransaction(Mono<TransactionDto> transactionDtoMono){
		return transactionDtoMono.map(AppUtils::DtoTotransactionEntity).flatMap(transactionRepository::insert).map(AppUtils::transactionEntitytoDto);		 
	}
	
	public Mono<TransactionDto> updateTransaction(Mono<TransactionDto> transactionDtoMono, String id){
		return transactionRepository.findById(id).flatMap(p->transactionDtoMono.map(AppUtils::DtoTotransactionEntity)
		.doOnNext(e->e.setId(id)))
		.flatMap(transactionRepository::save)
		.map(AppUtils::transactionEntitytoDto);
	}
	
	public Mono<Void> deleteTransaction(String id){
		return transactionRepository.deleteById(id);
	}
}
