package com.nttdata.transactionmanagement.Controller;

import java.util.Map;

import com.nttdata.transactionmanagement.Dto.TransactionDto;
import com.nttdata.transactionmanagement.Model.Transaction;
import com.nttdata.transactionmanagement.Service.transactionService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
	private transactionService service;

    @GetMapping("/getAll")
    @TimeLimiter(name="createTransactionTime")
    @CircuitBreaker(name="createTransactionCircuit")
	public Flux<TransactionDto> getAll(){
		return service.getAll();
	}

    @GetMapping("/getById/{id}")
    @TimeLimiter(name="createTransactionTime")
    @CircuitBreaker(name="createTransactionCircuit")
	public Mono<TransactionDto> getTransaction(@PathVariable String id){
		return service.getTransaction(id);
	}

	@PostMapping("/save")
    @TimeLimiter(name="createTransactionTime")
    @CircuitBreaker(name="createTransactionCircuit")
	public Mono<TransactionDto> saveTransaction(@RequestBody Mono<TransactionDto> TransactionObj){
		return service.saveTransaction(TransactionObj);
	}

	//Microservicio para la creacion de transacciones con las reglas de negocio
	@PostMapping("/createTransaction")
    @TimeLimiter(name="createTransactionTime")
    @CircuitBreaker(name="createTransactionCircuit")
	public ResponseEntity<Map<String, Object>> createTransaction(@RequestBody Transaction TransactionObj){
		return service.createTransaction(TransactionObj);
	}

	@PostMapping("/createEWalletTransaction")
    @TimeLimiter(name="createTransactionTime")
    @CircuitBreaker(name="createTransactionCircuit")
	//public Mono<Transaction> createEWalletTransaction(@RequestBody JSONObject new_trans){
	public Mono<Transaction> createEWalletTransaction(@RequestParam String phoneOrigin,@RequestParam  String phoneDestination,@RequestParam  Double amount){		
		return service.transferByYanki(phoneOrigin, phoneDestination,amount );
	}

	@PostMapping("/createBootCoinTransaction")
    @TimeLimiter(name="createTransactionTime")
    @CircuitBreaker(name="createTransactionCircuit")
	//public Mono<Transaction> createEWalletTransaction(@RequestBody JSONObject new_trans){
	public Transaction createBootCoinTransaction(@RequestParam String origin,@RequestParam  String destination,
	@RequestParam  Double amount, @RequestParam String paymentMethod) throws InterruptedException{		
		return service.transferBootCoin(origin, destination,amount, paymentMethod );
	}
	
	@PutMapping("/update/{id}")
    @TimeLimiter(name="createTransactionTime")
    @CircuitBreaker(name="createTransactionCircuit")
	public Mono<TransactionDto> saveTransaction(@RequestBody Mono<TransactionDto> TransactionObj,@PathVariable String id){
		return service.updateTransaction(TransactionObj, id);
	}
	
	@DeleteMapping("/delete/{id}")
    @TimeLimiter(name="createTransactionTime")
    @CircuitBreaker(name="createTransactionCircuit")
	public Mono<Void> deleteTransaction(@PathVariable String id){
		return service.deleteTransaction(id);
	}
}
