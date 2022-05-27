package com.nttdata.transactionmanagement.Util;

import com.nttdata.transactionmanagement.Dto.ProductDto;
import com.nttdata.transactionmanagement.Dto.TransactionDto;
import com.nttdata.transactionmanagement.Model.Product;
import com.nttdata.transactionmanagement.Model.Transaction;

import org.springframework.beans.BeanUtils;

public class AppUtils {
    public static ProductDto productEntitytoDto(Product product) {
		ProductDto productDto=new ProductDto();
		BeanUtils.copyProperties(product, productDto);
		return productDto;
	}
	
	public static Product DtoToproductEntity(ProductDto productDto) {
		Product product=new Product();
		BeanUtils.copyProperties(productDto, product);
		return product;
	}
	
	public static TransactionDto transactionEntitytoDto(Transaction trans) {
		TransactionDto transactionDto=new TransactionDto();
		BeanUtils.copyProperties(trans, transactionDto);
		return transactionDto;
	}
	
	public static Transaction DtoTotransactionEntity(TransactionDto transactionDto) {
		Transaction trans =new Transaction();
		BeanUtils.copyProperties(transactionDto, trans);
		return trans;
	}
}
