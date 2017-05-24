package com.globi.infa.datasource.gen;

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
public class GENTableRepository extends OracleTableRepository {

	@Autowired
	@Qualifier("jdbcOracleGEN")
	protected JdbcTemplate jdbcOracleGEN;

	public List<DataSourceTable> getAllTables() {
		Optional<SourceSystem> source = sourceSystemRepo.findByName("GEN");

		return getAllTables(jdbcOracleGEN, source.get().getOwnerName());
	}

}
