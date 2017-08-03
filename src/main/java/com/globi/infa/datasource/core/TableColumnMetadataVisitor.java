package com.globi.infa.datasource.core;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import com.globi.infa.metadata.src.InfaSourceColumnDefinition;

public interface TableColumnMetadataVisitor {
	
	public List<InfaSourceColumnDefinition>  getAllColumnsFor(JdbcTemplate jdbcT,String tableName);

	
	
}
