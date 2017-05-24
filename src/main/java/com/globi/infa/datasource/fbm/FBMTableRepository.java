package com.globi.infa.datasource.fbm;

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
public class FBMTableRepository extends OracleTableRepository {

	@Autowired
	@Qualifier("jdbcOracleFBM")
	protected JdbcTemplate jdbcOracleFBM;

	public List<DataSourceTable> getAllTables() {
		Optional<SourceSystem> source = sourceSystemRepo.findByName("FBM");

		return getAllTables(jdbcOracleFBM, source.get().getOwnerName());
	}

}
