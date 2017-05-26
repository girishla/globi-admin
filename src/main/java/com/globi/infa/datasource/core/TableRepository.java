package com.globi.infa.datasource.core;

import java.util.List;

import com.globi.infa.DataSourceTableDTO;
import com.globi.infa.sourcedefinition.InfaSourceColumnDefinition;

public interface TableRepository {

	
	public List<DataSourceTableDTO> accept(TableMetadataVisitor qV);
	
}
