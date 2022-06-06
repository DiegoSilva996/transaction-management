package com.nttdata.transactionmanagement.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nttdata.transactionmanagement.Dto.ProductDto;
import com.nttdata.transactionmanagement.Dto.TransactionDto;
import com.nttdata.transactionmanagement.Model.MasterValues;
import com.nttdata.transactionmanagement.Model.Product;
import com.nttdata.transactionmanagement.Model.Transaction;
import com.nttdata.transactionmanagement.Producer.KafkaProducer;
import com.nttdata.transactionmanagement.Repository.productRepository;
import com.nttdata.transactionmanagement.Repository.transactionRepository;
import com.nttdata.transactionmanagement.Util.AppUtils;
import com.nttdata.transactionmanagement.api.MasterValuesApiClient;
import com.nttdata.transactionmanagement.redis.model.MasterValuesCache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

  MasterValuesApiClient client; 

  private masterValuesService service;

  @Autowired
  private KafkaProducer kafkaProducer;

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
		map.put("id_product", pro.getId());
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

  //Clase interna para validar el saldo de las cuentas asociadas a las tarjetas de debito
  public String validateBalance(@RequestBody List<String> associatedAccounts, Double amount ){

      String idEnabled = "ID NOT ENABLED";
      List <String> enabledAccounts = new ArrayList<>();      
      associatedAccounts.stream().forEach((id_account) -> {
        Mono <Product> op_destination = productRepository.findById(id_account);          
        Product aux  = (Product) op_destination.map(value -> { return value; }).subscribe();
        HashMap<String, Object> validate =   validateNumberOfFreeTransactions(aux) ;
        String use_comission = validate.get("use_comission").toString();
        Double value_comission = Double.parseDouble(validate.get("value_comission").toString());
        Double totalAmount = amount+value_comission;
        Double realAmount = aux.getAmount();
        if (realAmount - totalAmount > 0) {
          String account = aux.getId() +"-"+ use_comission+"-"+value_comission;
          enabledAccounts.add(account); 
        }; 
      });
      if (enabledAccounts.size() > 0) { idEnabled = enabledAccounts.get(0); }; 
      return idEnabled;
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
        //Validar el saldo para la transacción
        Double current_amount =product.getAmount(); 
        Double new_amount = current_amount - amount - comission;

        //Se debe asignar el producto bancario del cual se realizará el retiro
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
  
  //Clase interna para crear transaccion -> retiro (BANK_WHITDRAWALL) con tarjeta de debito (DEBIT_CARD)
  public HashMap<String, Object> createBankWithdrawallwithDebitCard(@RequestBody Product product, Double amount, Transaction transaction  ){
    HashMap<String, Object> map = new HashMap<>();
    Double comission = 0.00;
    try{

      String accountEnabled =  validateBalance(product.getAssociatedAccounts(), amount);

      if(accountEnabled.equals("ID NOT ENABLED")){
        map.put("message", "Saldo insuficiente en las cuentas registradas para la transacción");
      } else{
        String[] parts = accountEnabled.split("-");
        String idProduct = parts[0];
        String flagComission = parts[1];
        comission = Double.parseDouble(parts[2]);

        //Obtener producto
        Mono <Product> op_destination = productRepository.findById(idProduct);          
        Product aux_product  = (Product) op_destination.map(value -> { return value; }).subscribe();

        //Actualizar transaccion antes de crear
        if(flagComission.equals("YES")){
          transaction.setFlagWithCommission(true);
          transaction.setTransactionCommission(comission);
        }

        //Actualizar el producto
        if(aux_product.getProductType().equals("FIXED_TERM_ACCOUNT") && aux_product.getTransactionDate() != null){
          DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); 
          Date current_date = new Date();            
          String s_current_date = dateFormat.format(current_date).toString();        
          String transactionDate = aux_product.getTransactionDate();           

          if(s_current_date.equals(transactionDate) == false){
            log.info("entrada 2");
            map.put("message", "No se encuentra en la fecha de transacción registrada en la cuenta de plazo fijo.");
          }else{
            log.info("comission al crear 1.0 ");
            log.info(comission.toString());
            aux_product.setAmount(aux_product.getAmount() - comission - amount);
            productRepository.save(aux_product);
            //Crear transacción y actualizar balance diario
            transaction.setNewDailyBalance(aux_product.getAmount());
            map.put("transaction", transactionRepository.save(transaction));
          }
        }else{
          log.info("comission al crear 2.0 ");
          log.info(comission.toString());
          aux_product.setAmount(aux_product.getAmount() - comission - amount);
          productRepository.save(aux_product);
          //Crear transacción y actualizar balance diario
          transaction.setNewDailyBalance(aux_product.getAmount());
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

          Mono <Product> op_destination = productRepository.findById(transaction.getIdDestinationProduct());          
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
    
  public ResponseEntity<Map<String, Object>> createTransaction(@RequestBody Transaction new_trans){

      log.info("entrando a método createTransaction");
      Map<String, Object> salida = new HashMap<>();      
      HashMap<String, Object> product_data = validateProduct(new_trans.getIdProduct());  
      String message = (product_data.get("message")).toString();
      

      if(message == "Id de producto no encontrado"){
          log.info("id incorrecto");
          salida.put("message", "Id de producto no encontrado");  
      }else{         
          Product current_product = Product.class.cast(product_data.get("product"));
          log.info("entro al else");
          String transactionType = new_trans.getTransactionType();
          log.info(transactionType);
          
          //Cantidad de transacciones
          List <Transaction> Q_transactions = transactionRepository.findByIdProduct(new_trans.getIdProduct());
          int q_transactions = Q_transactions.size();
          log.info("cantidad de tran::::: "+ q_transactions);

          //Asignar fecha de creación
          java.util.Date date = new java.util.Date();
          new_trans.setRegisterDate(date);
          //Asignar status
          new_trans.setStatus("ACTIVE");

          if(current_product.getProductType().equals("SAVING_ACCOUNT") &&  current_product.getMaximumTransactionLimit() <= q_transactions)
          {
            salida.put("ouput", "El número de transacciones permitidas para la cuenta fue alcanzada");
          }else{
            if(transactionType.equals("DEPOSIT" )){
              log.info("1");
              HashMap<String, Object> create_trans_a = createDeposit(  current_product, new_trans.getAmount(), new_trans );
              salida.put("ouput", create_trans_a);
            }else if(transactionType.equals("PAYMENT")){
                log.info("2");
                HashMap<String, Object> create_trans_b = createPayment(  current_product, new_trans.getAmount(), new_trans );
                salida.put("ouput", create_trans_b);
            }else if(transactionType.equals("CONSUMPTION")){
                log.info("3");
                HashMap<String, Object> create_trans_c = createConsumption(  current_product, new_trans.getAmount(), new_trans );
                salida.put("ouput", create_trans_c);
            }else if(transactionType.equals("BANK_WHITDRAWALL")){
              log.info("4");
              HashMap<String, Object> create_trans_d = createBankWithdrawall(  current_product, new_trans.getAmount(), new_trans );
              salida.put("ouput", create_trans_d);
            }else if(transactionType.equals("WIRE_TRANSFER")){
              log.info("5");
              HashMap<String, Object> create_trans_e = createWireTransfer(  current_product, new_trans.getAmount(), new_trans );
              salida.put("ouput", create_trans_e);
            }else if(transactionType.equals("BANK_WHITDRAWALL") && current_product.getProductType().equals("DEBIT_CARD")){
              log.info("6");
              HashMap<String, Object> create_trans_f = createBankWithdrawallwithDebitCard(  current_product, new_trans.getAmount(), new_trans );
              salida.put("ouput", create_trans_f);
            }
            
          }  
      }          
      log.info("imprime nomas");
      return ResponseEntity.ok(salida);
  }

  //Clase interna para rertornar la siguiente info por billetera virtual: 
  // id de la billetera -> eWalletId
  // monto de la billetera -> eWalletAmount
  // id de la cuenta principal asociada a la tarjeta de debito de la billetera -> idAccount
  // monto de la cuenta principal ->accountAmount
  public HashMap<String, Object>  getDataByWallet(String phone){

    log.info("entrando a método getDataByWallet");
    log.info(phone);
    HashMap<String, Object> map = new HashMap<>();
    //data de la billetera
    //Mono <Product> walletMono = productRepository.findByPhoneNumber(phone);          
    List<String> AssociatedAccounts  = productRepository.findByPhoneNumber(phone).block().getAssociatedAccounts(); //(Product) walletMono.map(value -> { return value; }).subscribe();
    //data de la cuenta
    String idAccount = AssociatedAccounts.get(0);
    //Mono <Product> accountMono = productRepository.findById(idAccount);          
    //Product account  = productRepository.findById(idAccount); // (Product) accountMono.map(value -> { return value; }).subscribe();
    //armar hashmap
    map.put("eWalletId", productRepository.findByPhoneNumber(phone).block().getId());
    map.put("eWalletAmount", productRepository.findByPhoneNumber(phone).block().getAmount());
    //map.put("idAccount", account.getId());
    //map.put("accountAmount", account.getId());
    map.put("idAccount",productRepository.findById(idAccount).block().getId()  );       
    map.put("accountAmount",productRepository.findById(idAccount).block().getAmount()  );
        
    return map;
  }


  //Funciones con programación reactiva para actualizar montos  
  public Mono<String> saveAmount(String id, Double amount) {
    return productRepository.findById(id)
            .map(pro -> transform(pro, amount))   
            .flatMap(productRepository::save)          
            .map(Product::getId);                       
  }
  private Product transform(Product toBeSaved, Double amount) {
      toBeSaved.setAmount(amount);
      return toBeSaved;
  }


  //public ResponseEntity<Map<String, Object>> transferByYanki(@RequestBody JSONObject new_trans){
  //public Mono  <Transaction> transferByYanki(@RequestBody JSONObject new_trans){  
  public Mono  <Transaction> transferByYanki(String phoneOrigin,  String phoneDestination,  Double amount){  
    log.info("entrando a método transferByYanki");
    log.info(phoneOrigin);
    log.info(phoneDestination);
    
    Map<String, Object> salida = new HashMap<>();    
    //validar numeros de telefono
    //String phoneOrigin = new_trans.getString("phoneOrigin");
    //String phoneDestination = new_trans.getString("phoneDestination");
    //Double amount = new_trans.getDouble("amount");
    //String transactionType = new_trans.getString("transactionType");

    HashMap<String, Object> eWalletOrigin = getDataByWallet(phoneOrigin);  
    HashMap<String, Object> eWalletDestination = getDataByWallet(phoneDestination);  
    
    //validar que el ewallet tenga saldo suficiente para la transaccion 
    Double eWalletOriginAmount = (Double) eWalletOrigin.get("eWalletAmount");
    Double eWalletDestinationAmount = (Double) eWalletDestination.get("eWalletAmount");
    if(eWalletOriginAmount - amount < 0 ){
      salida.put("message", "Saldo insuficiente");  
      return null;

    }else{
      Double newAmount1 = eWalletOriginAmount - amount;
      Double newAmount2 = eWalletDestinationAmount + amount;
      String idWalletOrigin = (String) eWalletOrigin.get("eWalletId");
      String idWalletDestination = (String) eWalletDestination.get("eWalletId");
      //Actualizar montos de cada ewallet
      saveAmount(idWalletOrigin, newAmount1).subscribe(id -> System.out.println("Update Product with id: " + id));
      saveAmount(idWalletDestination, newAmount2).subscribe(id -> System.out.println("Update Product with id: " + id));

      //Registrar transacción
      java.util.Date date = new java.util.Date();
      Transaction newTransaction = new Transaction();
      newTransaction.setAmount(amount);
      newTransaction.setIdProduct(idWalletOrigin);
      newTransaction.setStatus("ACTIVE");
      newTransaction.setRegisterDate(date);
      newTransaction.setTransactionType("TRANSFER_BY_YANKEE");
      newTransaction.setIdDestinationProduct(idWalletDestination);
      //enviar a kafka
      kafkaProducer.publishMessage(String.valueOf(newTransaction));

      return transactionRepository.save(newTransaction);
    }

 
  }

  
  public Mono  <Transaction> transferBootCoin(String origin,  String destination,  Double amount, String paymentMethod) throws InterruptedException{  
      log.info("entrando a método transferByYanki");
      log.info(origin);
      log.info(destination);

      String IdProductOrigin = origin;
      String IdProductDestination = destination;


      if(paymentMethod.equals("TRANSFER_BY_YANKI")){
        HashMap<String, Object> eWalletOrigin = getDataByWallet(origin);  
        HashMap<String, Object> eWalletDestination = getDataByWallet(destination);  
        IdProductOrigin =  (String) eWalletOrigin.get("eWalletId");
        IdProductDestination = (String) eWalletDestination.get("eWalletId");
      }

      if (service.getAll().isEmpty()) {
        service.storageMasterValueList(
          client.getList()
            .stream()
            .map(MasterValuesCache::fromMVResponse)
            .map(AppUtils::masterValuesCacheToMasterValues)
            .collect(Collectors.toList())
        );
      }

      List<MasterValues> masterValues =  service.getAll();
      MasterValues purschaseRate = masterValues.stream().filter(m -> "PURCHASE_RATE".equals(m.getCode())).findAny().orElse(null);
      MasterValues sellingeRate = masterValues.stream().filter(m -> "SELLING_RATE".equals(m.getCode())).findAny().orElse(null);

      Transaction newTransaction = new Transaction();

  
        //Registrar transacción
        java.util.Date date = new java.util.Date();
        newTransaction.setAmount(amount);
        newTransaction.setIdProduct(IdProductOrigin);
        newTransaction.setStatus("ACTIVE");
        newTransaction.setRegisterDate(date);
        newTransaction.setTransactionType("BOOTCOIN_TRANSFER");
        newTransaction.setIdDestinationProduct(IdProductDestination);
        newTransaction.setOperationStatus("IN_PROCESS");


        //enviar a kafka
        kafkaProducer.publishMessage(String.valueOf(newTransaction)+"%%"+ purschaseRate.getValue() + "%%"+ sellingeRate.getValue());
  
        return transactionRepository.save(newTransaction);
      }
  
   
    
  
  
}
