package com.globi.infa.datasource.core;

import java.util.List;

import com.globi.infa.datasource.core.InfaSourceColumnDefinition;

public interface TableRepository {

	
	public List<DataSourceTableDTO> accept(TableMetadataVisitor qV);
	
}
