package com.nttdata.transactionmanagement.Dto;

import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransactionDto {
    private String id;
    private Date registerDate;
    private String idProduct;
    private Double amount;
    private Double transactionCommission;
    private boolean flagWithCommission;
    private String idDestinationAccount;
    private String transactionType;
    private String status;
    private Double newDailyBalance; 
    private String idCustomer;
}
