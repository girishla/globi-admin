package com.globi.infa.datasource.chb;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.globi.infa.datasource.core.TableColumnMetadataVisitor;
import com.globi.infa.datasource.core.TableColumnRepository;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;

@Repository
public class CHBTableColumnRepository implements TableColumnRepository {

	@Autowired
	@Qualifier("jdbcOracleCHB")
	protected JdbcTemplate jdbcOracleCHB;

	@Override
	public List<InfaSourceColumnDefinition> accept(TableColumnMetadataVisitor qV,String tableName) {
		return qV.getAllColumnsFor(jdbcOracleCHB, tableName);
	}
	
	
	


}
