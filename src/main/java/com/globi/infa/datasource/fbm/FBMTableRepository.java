package com.globi.infa.datasource.fbm;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.globi.infa.DataSourceTableDTO;
import com.globi.infa.datasource.core.TableMetadataVisitor;
import com.globi.infa.datasource.core.TableRepository;
import com.globi.metadata.sourcesystem.SourceSystem;
import com.globi.metadata.sourcesystem.SourceSystemRepository;

@Repository
public class FBMTableRepository implements TableRepository {

	
	@Autowired
	@Qualifier("jdbcOracleFBM")
	protected JdbcTemplate jdbcOracleFBM;
	
	@Autowired
	protected SourceSystemRepository sourceSystemRepo;

	
	@Override
	public List<DataSourceTableDTO> accept(TableMetadataVisitor qV) {
		
		Optional<SourceSystem> source=sourceSystemRepo.findByName("FBM");
		return qV.getAllTables(jdbcOracleFBM, source.get().getOwnerName());
	}

	

}
