package com.globi.infa.datasource.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
@RequiredArgsConstructor
@Builder
public class DataSourceTableDTO  {

	@JsonIgnore
	private Integer  id;
	
	@NonNull
	private String tableOwner;
	
	@NonNull
	private String tableName;
	
	

	
}
