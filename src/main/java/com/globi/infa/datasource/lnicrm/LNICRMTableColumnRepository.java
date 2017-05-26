package com.globi.infa.datasource.lnicrm;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.globi.infa.datasource.core.TableColumnMetadataVisitor;
import com.globi.infa.datasource.core.TableColumnRepository;
import com.globi.infa.datasource.core.InfaSourceColumnDefinition;

@Repository
public class LNICRMTableColumnRepository implements TableColumnRepository {

	@Autowired
	@Qualifier("jdbcOracleLNICRM")
	protected JdbcTemplate jdbcOracleLNICRM;

	@Override
	public List<InfaSourceColumnDefinition> accept(TableColumnMetadataVisitor qV,String tableName) {
		return qV.getAllColumnsFor(jdbcOracleLNICRM, tableName);
	}
	
	
	


}
