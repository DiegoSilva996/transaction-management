package com.nttdata.transactionmanagement.Service;

import com.nttdata.transactionmanagement.Dto.ProductDto;
import com.nttdata.transactionmanagement.Repository.productRepository;
import com.nttdata.transactionmanagement.Util.AppUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class productService {
    @Autowired
    private productRepository productRepository;

    public Flux<ProductDto> getAll(){
		return productRepository.findAll().map(AppUtils::productEntitytoDto);
	}

    public Mono<ProductDto> getProduct(String id){
		return productRepository.findById(id).map(AppUtils::productEntitytoDto);
	}
	
	public Mono<ProductDto> saveProduct(Mono<ProductDto> productDtoMono){
		return productDtoMono.map(AppUtils::DtoToproductEntity).flatMap(productRepository::insert).map(AppUtils::productEntitytoDto);		 
	}
	
	public Mono<ProductDto> updateProduct(Mono<ProductDto> productDtoMono, String id){
		return productRepository.findById(id)
		.flatMap(p->productDtoMono.map(AppUtils::DtoToproductEntity)
		.doOnNext(e->e.setId(id)))
		.flatMap(productRepository::save)
		.map(AppUtils::productEntitytoDto);
	}

	
	public Mono<Void> deleteProduct(String id){
		return productRepository.deleteById(id);
	}

}
