package com.globi.infa.datasource.law;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.datasource.core.TableMetadataVisitor;
import com.globi.infa.datasource.core.TableRepository;
import com.globi.metadata.sourcesystem.SourceSystem;
import com.globi.metadata.sourcesystem.SourceSystemRepository;

@Repository
public class LAWTableRepository implements TableRepository {

	
	@Autowired
	@Qualifier("jdbcOracleLAW")
	protected JdbcTemplate jdbcOracleLAW;
	
	@Autowired
	protected SourceSystemRepository sourceSystemRepo;

	
	@Override
	public List<DataSourceTableDTO> accept(TableMetadataVisitor qV) {
		
		Optional<SourceSystem> source=sourceSystemRepo.findByName("LAW");
		return qV.getAllTables(jdbcOracleLAW, source.get().getOwnerName());
	}

	

}
