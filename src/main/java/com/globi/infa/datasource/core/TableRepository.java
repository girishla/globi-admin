package com.globi.infa.datasource.core;

import java.util.List;

import com.globi.infa.metadata.src.InfaSourceColumnDefinition;

public interface TableRepository {

	
	public List<DataSourceTableDTO> accept(TableMetadataVisitor qV);
	
}
