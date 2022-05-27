package com.nttdata.transactionmanagement.Dto;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class ProductDto {
    private String id;
    private String customerId;
    private Date creationDate;
    private String transactionDate; 
    private int maximumTransactionLimit;
    private int numberOfFreeTransactions;
    private Double maintenanceCommission;
    private Double amount;
    private String productType;
    private String status;
    private List<String> owners;
    private List<String> authorizedSigner;   
    private Boolean hasDebt;
    private List<String> associatedAccounts;
}