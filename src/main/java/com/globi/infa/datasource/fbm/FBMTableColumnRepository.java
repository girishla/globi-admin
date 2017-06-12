package com.globi.infa.datasource.fbm;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.globi.infa.datasource.core.TableColumnMetadataVisitor;
import com.globi.infa.datasource.core.TableColumnRepository;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;

@Repository
public class FBMTableColumnRepository implements TableColumnRepository {

	@Autowired
	@Qualifier("jdbcOracleFBM")
	protected JdbcTemplate jdbcOracleFBM;

	@Override
	public List<InfaSourceColumnDefinition> accept(TableColumnMetadataVisitor qV,String tableName) {
		return qV.getAllColumnsFor(jdbcOracleFBM, tableName);
	}
	
	
	


}
