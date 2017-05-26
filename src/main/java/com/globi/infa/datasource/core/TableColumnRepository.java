package com.globi.infa.datasource.core;

import java.util.List;

import com.globi.infa.DataSourceTableDTO;
import com.globi.infa.datasource.core.InfaSourceColumnDefinition;

public interface TableColumnRepository {

	
	public List<InfaSourceColumnDefinition> accept(TableColumnMetadataVisitor qV,String tableName);
	
}
