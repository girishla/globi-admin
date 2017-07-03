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
	private String sourceName;
	
	@NonNull
	private String tableOwner;
	
	@NonNull
	private String tableName;
	
	@NonNull
	private String colName;
	
	@NonNull
	private String dataType;

	@Builder.Default
	private int colOrder=0;
	

	private boolean integrationId;

	private boolean changeCaptureCol;
	
	private boolean pguidCol;
	
	private boolean buidCol;
	
	
}
