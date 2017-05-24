package com.globi.infa.datasource.gcrm;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.globi.infa.datasource.core.OracleTableColumnRepository;
import com.globi.infa.sourcedefinition.InfaSourceColumnDefinition;

@Repository
public class GCRMTableColumnRepository extends OracleTableColumnRepository {

	@Autowired
	@Qualifier("jdbcOracleGCRM")
	protected JdbcTemplate jdbcOracleGCRM;

	public List<InfaSourceColumnDefinition> getAllColumnsFor(String tableName) {
		return getAllColumnsFor(jdbcOracleGCRM, tableName);

	}

}
