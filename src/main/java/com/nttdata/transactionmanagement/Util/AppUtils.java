package com.nttdata.transactionmanagement.Util;

import com.nttdata.transactionmanagement.Dto.ProductDto;
import com.nttdata.transactionmanagement.Dto.TransactionDto;
import com.nttdata.transactionmanagement.Model.MasterValues;
import com.nttdata.transactionmanagement.Model.Product;
import com.nttdata.transactionmanagement.Model.Transaction;
import com.nttdata.transactionmanagement.redis.model.MasterValuesCache;

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
	public static Transaction DtoTotransactionEntity(TransactionDto transdto) {
		Transaction transaction=new Transaction();
		BeanUtils.copyProperties(transdto, transaction);
		return transaction;
	}
	
	public static MasterValuesCache masterValuesToMasterValuesCache(MasterValues masterValues) {
		MasterValuesCache mvc =new MasterValuesCache();
		BeanUtils.copyProperties(masterValues, mvc);
		return mvc;
	}

	public static MasterValues masterValuesCacheToMasterValues(MasterValuesCache masterValuesCache) {
		MasterValues mv =new MasterValues();
		BeanUtils.copyProperties(masterValuesCache, mv);
		return mv;
	}
	
}
