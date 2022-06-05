package com.nttdata.transactionmanagement.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.nttdata.transactionmanagement.config.MasterValuesApiProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class MasterValuesApiClient {
    private final WebClient webClient;
	private final MasterValuesApiProperties mvProperties;
	
	public List<MasterValuesResponse> getList() throws InterruptedException{
		ExecutorService executor = Executors.newSingleThreadExecutor();
		List<MasterValuesResponse> result=new ArrayList<>();
		//cambiar url
		webClient.get().uri(mvProperties.getBaseUrl()+"/mastervalues/all")
		.accept(MediaType.TEXT_EVENT_STREAM)
		.retrieve()
		.bodyToFlux(MasterValuesResponse.class)
		.publishOn(Schedulers.fromExecutor(executor))
		.subscribe(response->result.add(response));
		executor.awaitTermination(1, TimeUnit.SECONDS);
		log.info("Master Values list"+ result);
		return result;
	}
}
