package com.nttdata.transactionmanagement.Controller;

import com.nttdata.transactionmanagement.Dto.TransactionDto;
import com.nttdata.transactionmanagement.Service.transactionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
	private transactionService service;

    @GetMapping("/getAll")
	public Flux<TransactionDto> getAll(){
		return service.getAll();
	}

    @GetMapping("/getById/{id}")
	public Mono<TransactionDto> getTransaction(@PathVariable String id){
		return service.getTransaction(id);
	}

	@PostMapping("/save")
	public Mono<TransactionDto> saveTransaction(@RequestBody Mono<TransactionDto> TransactionObj){
		return service.saveTransaction(TransactionObj);
	}
	
	@PutMapping("/update/{id}")
	public Mono<TransactionDto> saveTransaction(@RequestBody Mono<TransactionDto> TransactionObj,@PathVariable String id){
		return service.updateTransaction(TransactionObj, id);
	}
	
	@DeleteMapping("/delete/{id}")
	public Mono<Void> deleteTransaction(@PathVariable String id){
		return service.deleteTransaction(id);
	}
}
