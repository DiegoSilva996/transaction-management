package com.nttdata.transactionmanagement.Controller;

import com.nttdata.transactionmanagement.Dto.ProductDto;
import com.nttdata.transactionmanagement.Service.productService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

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
@RequestMapping("/product")
public class ProductController {

    @Autowired
	private productService service;

    @GetMapping("/getAll")
    @TimeLimiter(name="createTransactionTime")
    @CircuitBreaker(name="createTransactionCircuit")
	public Flux<ProductDto> getAllProducts(){
		return service.getAll();
	}

    @GetMapping("/getById/{id}")
    @TimeLimiter(name="createTransactionTime")
    @CircuitBreaker(name="createTransactionCircuit")
	public Mono<ProductDto> getProduct(@PathVariable String id){
		return service.getProduct(id);
	}

	@PostMapping("/save")
    @TimeLimiter(name="createTransactionTime")
    @CircuitBreaker(name="createTransactionCircuit")
	public Mono<ProductDto> saveProduct(@RequestBody Mono<ProductDto> productObj){
		return service.saveProduct(productObj);
	}
	
	@PutMapping("/update/{id}")
    @TimeLimiter(name="createTransactionTime")
    @CircuitBreaker(name="createTransactionCircuit")
	public Mono<ProductDto> updateProduct(@RequestBody Mono<ProductDto> productObj,@PathVariable String id){
		return service.updateProduct(productObj, id);
	}
	
	@DeleteMapping("/delete/{id}")
    @TimeLimiter(name="createTransactionTime")
    @CircuitBreaker(name="createTransactionCircuit")
	public Mono<Void> deleteProduct(@PathVariable String id){
		return service.deleteProduct(id);
	}
}
