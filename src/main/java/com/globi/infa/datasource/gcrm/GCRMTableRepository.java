package com.globi.infa.datasource.gcrm;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.globi.infa.DataSourceTableDTO;
import com.globi.infa.datasource.core.OracleTableRepository;
import com.globi.infa.datasource.core.DBMetadataVisitor;
import com.globi.infa.datasource.core.TableRepository;
import com.globi.metadata.sourcesystem.SourceSystem;
import com.globi.metadata.sourcesystem.SourceSystemRepository;

@Repository
public class GCRMTableRepository implements TableRepository {

	@Autowired
	@Qualifier("jdbcOracleGCRM")
	protected JdbcTemplate jdbcOracleGCRM;
	
	@Autowired
	protected SourceSystemRepository sourceSystemRepo;

	@Override
	public List<DataSourceTableDTO> accept(DBMetadataVisitor qV) {
		
		Optional<SourceSystem> source=sourceSystemRepo.findByName("CGL");
		// TODO Auto-generated method stub
		return qV.getAllTables(jdbcOracleGCRM, source.get().getOwnerName());
	}

}
