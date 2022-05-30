package com.nttdata.transactionmanagement.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.nttdata.transactionmanagement.Dto.ProductDto;
import com.nttdata.transactionmanagement.Dto.TransactionDto;
import com.nttdata.transactionmanagement.Model.Product;
import com.nttdata.transactionmanagement.Model.Transaction;
import com.nttdata.transactionmanagement.Repository.productRepository;
import com.nttdata.transactionmanagement.Repository.transactionRepository;
import com.nttdata.transactionmanagement.Util.AppUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class transactionService {
    @Autowired
    private transactionRepository transactionRepository; 

	@Autowired
	private productRepository productRepository;


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


	// Crear Servicio para Registrar transacciones: depósito, retiro, pago , consumo
	// Campos Obligatorios: Id cliente (valido) , tipo de cuenta, monto

	//Clase interna para validar producto bancario
	public HashMap<String, Object> validateProduct(String id) {        
		HashMap<String, Object> map = new HashMap<>();
		//Optional<Product> doc = productRepository.findById(id);
		Mono<Product> doc = productRepository.findById(id);
		if (doc.hasElement() != null) {   
			Mono<ProductDto> current_pro =doc.map(AppUtils::productEntitytoDto);
			//Armar hashmap
			map.put("message", "Id de producto encontrado");
			map.put("product", current_pro);
		}else{
			map.put("message", "Id de producto no encontrado");
		}
		return map;
	}


	// Todas las cuentas bancarias tendrán un número máximo de transacciones (depósitos y retiros)
	// Clase interna para validar si se cobrará comision
	public HashMap<String, Object> validateNumberOfFreeTransactions(Product pro) {      
		HashMap<String, Object> map = new HashMap<>();
		List <Transaction> deposits = transactionRepository.findByTransactionTypeAndIdProduct("DEPOSIT",pro.getId());
		List <Transaction> whitdrawall = transactionRepository.findByTransactionTypeAndIdProduct("BANK_WHITDRAWALL",pro.getId());
		int total_transactions = deposits.size() + whitdrawall.size();
		if(total_transactions >= pro.getNumberOfFreeTransactions()){
		map.put("use_comission", "YES");
		map.put("value_comission", "5.00");
		}else{
		map.put("use_comission", "NO");
		}
		return map;
	}

  //Clase interna para crear transaccion -> depósito (DEPOSIT)
  public HashMap<String, Object> createDeposit(@RequestBody Product product, Double amount, Transaction transaction  ){
      HashMap<String, Object> map = new HashMap<>();
      Double comission = 0.00;
      try{

          //validar si será una transaccion con comision
          transaction.setFlagWithCommission(false);
          HashMap<String, Object> validate = validateNumberOfFreeTransactions(product);
          String use_comission = validate.get("use_comission").toString();
          Double value_comission = Double.parseDouble(validate.get("value_comission").toString());
          if(use_comission.equals("YES")){
            transaction.setFlagWithCommission(true);
            transaction.setTransactionCommission(value_comission);
            comission= value_comission;
          }

          log.info("createDeposit:::::");  
          if(product.getProductType().equals("FIXED_TERM_ACCOUNT") && product.getTransactionDate() != null){
            log.info("entrada 1");

            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); //yyyy/MM/dd
            Date current_date = new Date();            
            String s_current_date = dateFormat.format(current_date).toString();         


            String transactionDate = product.getTransactionDate(); //new SimpleDateFormat("dd/MM/yyyy").parse(product.getTransactionDate());            
            log.info(s_current_date);
            log.info("entrada 1.1");
            log.info(transactionDate);
            log.info("entrada 1.2");

            if(s_current_date.equals(transactionDate) == false){
              log.info("entrada 2");
              map.put("message", "No se encuentra en la fecha de transacción registrada en la cuenta de plazo fijo.");
            }else{
              log.info("entrada 3.0");
              log.info("comision");
              log.info(comission.toString());
              //Actualizar producto
              Double New_amount = product.getAmount() + amount - comission;
              product.setAmount(New_amount);
              productRepository.save(product);

              //Crear transacción y actualizar balance diario
              transaction.setNewDailyBalance(New_amount);
              map.put("transaction", transactionRepository.save(transaction));
            }

          }else{
            log.info("entrada 3");
            log.info("comision");
            log.info(comission.toString());
            //Actualizar producto
            Double New_amount = product.getAmount() + amount -comission;
            product.setAmount(New_amount);
            productRepository.save(product);
            //Crear transacción y actualizar balance diario
            transaction.setNewDailyBalance(New_amount);
            map.put("transaction", transactionRepository.save(transaction));
          
          }

      } catch(Exception e) {
          e.printStackTrace();
          map.put("message", "error");
      }                    
      return map;
  }

  //Clase interna para crear transaccion -> pago (PAYMENT)
  //Un cliente puede hacer el pago de cualquier producto de crédito de terceros.
  public HashMap<String, Object> createPayment(@RequestBody Product product, Double amount, Transaction transaction  ){
    HashMap<String, Object> map = new HashMap<>();
    try{

        //Actualizar producto
        Double New_amount = product.getAmount() + amount;
        product.setAmount(New_amount);
        productRepository.save(product);
        //Crear transacción y actualizar balance diario
        transaction.setNewDailyBalance(New_amount);
        map.put("transaction", transactionRepository.save(transaction));

    }catch(Exception e) {
        e.printStackTrace();
        map.put("message", "error");
    }                    
    return map;
  }

  //Clase interna para crear transaccion -> consumo (CONSUMPTION)
  public HashMap<String, Object> createConsumption(@RequestBody Product product, Double amount, Transaction transaction  ){
    HashMap<String, Object> map = new HashMap<>();
    try{
        //Validar el saldo para la transacción
        Double current_amount = product.getAmount(); 
        Double new_amount = current_amount - amount;

        if( new_amount < 0){
          map.put("message", "Saldo insuficiente para la transacción");
        }else{
          product.setAmount(new_amount);
          productRepository.save(product);
          //Crear transacción y actualizar balance diario
          transaction.setNewDailyBalance(new_amount);
          map.put("transaction", transactionRepository.save(transaction));
        }       

    }catch(Exception e) {
        e.printStackTrace();
        map.put("message", "error");
    }                    
    return map;
  }

  //Clase interna para crear transaccion -> retiro (BANK_WHITDRAWALL)
  public HashMap<String, Object> createBankWithdrawall(@RequestBody Product product, Double amount, Transaction transaction  ){
    HashMap<String, Object> map = new HashMap<>();
    Double comission = 0.00;
    try{
        Product aux = new Product();
        //Si es tarjeta de debito, se usará el saldo de la cuenta principal 
        if(product.getProductType().equals("DEBIT_CARD")){
          //usar saldo de la cuenta principal
          String principal_id = product.getAssociatedAccounts().get(0);
          Mono <Product> op_destination = productRepository.findById(principal_id);          
          aux  = (Product) op_destination.map(value -> { return value; }).subscribe();
        }

        //validar si será una transaccion con comision
        transaction.setFlagWithCommission(false);
        HashMap<String, Object> validate =  product.getProductType().equals("DEBIT_CARD") ? validateNumberOfFreeTransactions(aux) : validateNumberOfFreeTransactions(product);
                
        String use_comission = validate.get("use_comission").toString();
        Double value_comission = Double.parseDouble(validate.get("value_comission").toString());
        if(use_comission.equals("YES")){
          transaction.setFlagWithCommission(true);
          transaction.setTransactionCommission(value_comission);
          comission= value_comission;
        }
        //Validar el saldo para la transacción
        Double current_amount =product.getProductType().equals("DEBIT_CARD") ? aux.getAmount(): product.getAmount(); 
        Double new_amount = current_amount - amount - comission;

        if( new_amount < 0){
          map.put("message", "Saldo insuficiente para la transacción");
        }else{

          if(product.getProductType().equals("FIXED_TERM_ACCOUNT") && product.getTransactionDate() != null){
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); 
            Date current_date = new Date();            
            String s_current_date = dateFormat.format(current_date).toString();         


            String transactionDate = product.getTransactionDate();           

            if(s_current_date.equals(transactionDate) == false){
              log.info("entrada 2");
              map.put("message", "No se encuentra en la fecha de transacción registrada en la cuenta de plazo fijo.");
            }else{
              log.info("comission al crear 1.0 ");
              log.info(comission.toString());
              product.setAmount(new_amount);
              productRepository.save(product);
              //Crear transacción y actualizar balance diario
              transaction.setNewDailyBalance(new_amount);
              map.put("transaction", transactionRepository.save(transaction));
            }
          }else{
            log.info("comission al crear 2.0 ");
            log.info(comission.toString());
            product.setAmount(new_amount);
            productRepository.save(product);
            //Crear transacción y actualizar balance diario
            transaction.setNewDailyBalance(new_amount);
            map.put("transaction", transactionRepository.save(transaction));
          }

        }       

    }catch(Exception e) {
        e.printStackTrace();
        map.put("message", "error");
    }                    
    return map;
  }

  //Clase interna para crear transaccion -> transferencia bancaria (WIRE_TRANSFER)
  public HashMap<String, Object> createWireTransfer(@RequestBody Product product, Double amount, Transaction transaction  ){
    HashMap<String, Object> map = new HashMap<>();
    try{
        //Validar el saldo para la transacción
        Double current_amount = product.getAmount(); 
        Double new_amount = current_amount - amount;

        if( new_amount < 0){
          map.put("message", "Saldo insuficiente para la transacción");
        }else{
          //Actualizar cuenta de origen
          Double New_amount = product.getAmount() - amount;
          product.setAmount(New_amount);
          productRepository.save(product);
          //Actualizar cuenta de destino

          Mono <Product> op_destination = productRepository.findById(transaction.getIdDestinationAccount());          
          Product destination  = (Product) op_destination.map(value -> { return value; }).subscribe();
          Double New_amount_destination = destination.getAmount() + amount;
          destination.setAmount(New_amount_destination);
          productRepository.save(product);
          //Crear transacción y actualizar balance diario
          transaction.setNewDailyBalance(New_amount);
          map.put("transaction", transactionRepository.save(transaction));

        }



    }catch(Exception e) {
        e.printStackTrace();
        map.put("message", "error");
    }                    
    return map;
  }

}
