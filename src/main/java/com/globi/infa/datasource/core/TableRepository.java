package com.globi.infa.datasource.core;

import java.util.List;

import com.globi.infa.DataSourceTableDTO;

public interface TableRepository {

	
	public List<DataSourceTableDTO> accept(DBMetadataVisitor qV);
	
}
