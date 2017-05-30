package com.globi.infa.datasource.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Data
@RequiredArgsConstructor
@Getter
@Setter
@Builder
public class DataSourceTableColumnDTO  {

	@JsonIgnore
	private Integer  id;
	
	@NonNull
	private String tableOwner;
	
	@NonNull
	private String tableName;
	
	@NonNull
	private String name;
	
	@NonNull
	private String dataType;

	private String integrationId;

	private String changeCaptureCol;
	
	
}
