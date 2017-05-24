package com.globi.infa.datasource.gcrm;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.globi.infa.DataSourceTable;
import com.globi.infa.datasource.core.OracleTableRepository;
import com.globi.metadata.sourcesystem.SourceSystem;

@Repository
public class GCRMTableRepository extends OracleTableRepository {

	@Autowired
	@Qualifier("jdbcOracleGCRM")
	protected JdbcTemplate jdbcOracleGCRM;

	public List<DataSourceTable> getAllTables() {
		
		Optional<SourceSystem> source=sourceSystemRepo.findByName("CGL");
		
		return getAllTables(jdbcOracleGCRM,source.get().getOwnerName());
	}

}
