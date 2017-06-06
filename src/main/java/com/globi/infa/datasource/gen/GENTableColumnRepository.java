package com.globi.infa.datasource.gen;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.globi.infa.datasource.core.TableColumnMetadataVisitor;
import com.globi.infa.datasource.core.TableColumnRepository;
import com.globi.infa.metadata.source.InfaSourceColumnDefinition;

@Repository
public class GENTableColumnRepository implements TableColumnRepository {

	@Autowired
	@Qualifier("jdbcOracleGEN")
	protected JdbcTemplate jdbcOracleGEN;

	@Override
	public List<InfaSourceColumnDefinition> accept(TableColumnMetadataVisitor qV,String tableName) {
		return qV.getAllColumnsFor(jdbcOracleGEN, tableName);
	}
	
	
	


}
