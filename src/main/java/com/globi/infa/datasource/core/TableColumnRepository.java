package com.globi.infa.datasource.core;

import java.util.List;

import com.globi.infa.metadata.src.InfaSourceColumnDefinition;

public interface TableColumnRepository {

	
	public List<InfaSourceColumnDefinition> accept(TableColumnMetadataVisitor qV,String tableName);
	
}
