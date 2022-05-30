package com.nttdata.transactionmanagement.Model;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Product {
    @Id
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
    //Lista en posición 0 será la cuenta principal
    private List<String> associatedAccounts;
}
