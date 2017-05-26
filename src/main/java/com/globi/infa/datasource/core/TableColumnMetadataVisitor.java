package com.globi.infa.datasource.core;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import com.globi.infa.DataSourceTableDTO;
import com.globi.infa.datasource.core.InfaSourceColumnDefinition;

public interface TableColumnMetadataVisitor {
	
	public List<InfaSourceColumnDefinition>  getAllColumnsFor(JdbcTemplate jdbcT,String tableName);

}
