package com.nttdata.transactionmanagement.api;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterValuesResponse {
    private String id;
    private Date creationDate;
    private String status;
    private String description;
    private String code;
    private double value; 
    private String masterType;
}
