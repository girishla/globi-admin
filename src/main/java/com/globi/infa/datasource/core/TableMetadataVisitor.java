package com.globi.infa.datasource.core;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

public interface TableMetadataVisitor {
	
	
	public List<DataSourceTableDTO> getAllTables(JdbcTemplate jdbcT,String ownerName);

}
