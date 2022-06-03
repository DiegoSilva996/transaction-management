package com.nttdata.transactionmanagement.Service;

import org.junit.jupiter.api.BeforeEach;

import com.nttdata.transactionmanagement.Model.Transaction;
import com.nttdata.transactionmanagement.Repository.transactionRepository;
import com.nttdata.transactionmanagement.Service.transactionService;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

public class TransactionServiceTest {

	@Mock
	private transactionRepository transRepo;
	@InjectMocks
	private transactionService transService;

	private List<Transaction> transList;
	private Transaction transExample = new Transaction();
	private Transaction transExampleCreate = new Transaction();

    @BeforeEach
    void setUp() {

        MockitoAnnotations.initMocks(this);
        transList = new ArrayList<>();
        Transaction trans1=Transaction.builder()
			.id("628296562738d249c56a5f01")
			.idProduct("628296562738d249c56a5fe0")
			.registerDate(null)
			.amount(229.00)
			.transactionCommission(5.00)
			.flagWithCommission(true)
			.transactionType("DEPOSIT")
			.status("ACTIVE")
			.newDailyBalance(null)
			.idDestinationProduct(null)
			.idCustomer(null)
			.build();
		transExample = trans1;
			
		Transaction trans2=Transaction.builder()
			.id("628296562738d249c56a5f02")
			.idProduct("628296562738d249c56a5fe0")
			.registerDate(null)
			.amount(20.00)
			.transactionCommission(0.00)
			.flagWithCommission(false)
			.transactionType("BANK_WHITDRAWALL")
			.status("ACTIVE")
			.newDailyBalance(null)
			.idDestinationProduct(null)
			.idCustomer(null)
			.build();

		Transaction trans3=Transaction.builder()
			.id("628296562738d249c56a5f03")
			.idProduct("628296562738d249c56a5fe6")
			.registerDate(null)
			.amount(300.00)
			.transactionCommission(0.00)
			.flagWithCommission(false)
			.transactionType("DEPOSIT")
			.status("ACTIVE")
			.newDailyBalance(null)
			.idDestinationProduct(null)
			.idCustomer(null)
			.build();

		transList.add(trans1);
		transList.add(trans2);
		transList.add(trans3);

		transExampleCreate=Transaction.builder()
			.idProduct("628296562738d249c56a5fe0")
			.registerDate(null)
			.amount(90.00)
			.transactionCommission(0.00)
			.flagWithCommission(false)
			.transactionType("DEPOSIT")
			.status("ACTIVE")
			.newDailyBalance(null)
			.idDestinationProduct(null)
			.idCustomer(null)
			.build();
    }


			
	@Test
	void createTransactionTest() {
		when(transRepo.save(transExampleCreate)).thenReturn(Mono.just(transExampleCreate));
		assertNotNull(transService.createTransaction(transExampleCreate));
	}

	@Test
    void findAllTest() {
        when(transRepo.findAll()).thenReturn(Flux.fromIterable(transList));
        assertNotNull(transService.getAll());
    }

	@Test
	void findTransactionSuccessfullyTest() {
		String idToTest = "628296562738d249c56a5f01";
		when(transRepo.findById(idToTest)).thenReturn(Mono.just(transExample));
		assertNotNull(transService.getTransaction(idToTest));
	}

	

}