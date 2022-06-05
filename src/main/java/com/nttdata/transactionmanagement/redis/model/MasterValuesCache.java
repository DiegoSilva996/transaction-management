package com.nttdata.transactionmanagement.redis.model;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.redis.core.RedisHash;

import com.nttdata.transactionmanagement.Model.MasterValues;
import com.nttdata.transactionmanagement.api.MasterValuesResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RedisHash("MasterValue")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class MasterValuesCache implements Serializable{
    private String id;
    private Date creationDate;
    private String status;
    private String description;
    private String code;
    private double value; 
    private String masterType;

    public static MasterValuesCache fromMVResponse(MasterValuesResponse objResponse) {
		return MasterValuesCache.builder()
				.id(objResponse.getId())
				.creationDate(objResponse.getCreationDate())
				.description(objResponse.getDescription())
				.code(objResponse.getCode())
				.value(objResponse.getValue())
				.masterType(objResponse.getMasterType())
				.build();
				
	}
}
