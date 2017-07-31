package com.globi.infa.datasource.fbm;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.datasource.core.SourceMetadataFactory;
import com.globi.infa.datasource.core.TableColumnMetadataVisitor;
import com.globi.infa.datasource.core.TableColumnRepository;
import com.globi.infa.datasource.core.TableMetadataVisitor;
import com.globi.infa.datasource.core.TableRepository;
import com.globi.infa.datasource.type.oracle.OracleTableColumnMetadataVisitor;
import com.globi.infa.datasource.type.oracle.OracleTableMetadataVisitor;
import com.globi.infa.datasource.type.oracle.OracleInfaSourceToInfaTargetTypeMapper;
import com.globi.infa.datasource.type.oracle.OracleInfaSourceToInfaXFormTypeMapper;
import com.globi.infa.datasource.type.oracle.OracleViewMetadataVisitor;
import com.globi.metadata.sourcesystem.SourceSystem;
import com.globi.metadata.sourcesystem.SourceSystemRepository;


@Component
public class FBMSourceMetadataFactory implements SourceMetadataFactory {
	
	@Autowired
	private FBMTableRepository fbmcrmRepo;
	
	@Autowired
	private FBMTableColumnRepository fbmTableColumnRepo;
	
	@Autowired
	private OracleViewMetadataVisitor fbmViewMetadataVisitor;
	
	@Autowired
	private OracleTableColumnMetadataVisitor fbmOracleTableColumnMetadataVisitor;
	
	@Autowired
	private OracleInfaSourceToInfaXFormTypeMapper fbmOracleToInfaDataTypeMapper;
	
	
	@Autowired
	private OracleInfaSourceToInfaTargetTypeMapper fbmOracleInfaSourceToInfaTargetTypeMapper;

	
	@Autowired
	private SourceSystemRepository sourceSystemRepo;
	


	@Override
	public SourceSystem getSourceSystem() {
		
		Optional<SourceSystem> source;

		source = sourceSystemRepo.findByName(getSourceName());
		
		if (!source.isPresent())
			throw new IllegalArgumentException("Cannot find Source System. Please ensure it is defined.");

		return source.get();
	}
	
	@Override
	public DataTypeMapper createSourceToTargetDatatypeMapper() {
		return fbmOracleInfaSourceToInfaTargetTypeMapper;
	}
	

	@Override
	public TableRepository createTableRepository() {
		
		return fbmcrmRepo;
	}

	@Override
	public TableColumnRepository createTableColumnRepository() {
		
		return fbmTableColumnRepo;
	}

	@Override
	public TableMetadataVisitor createTableMetadataVisitor() {
		
		return fbmViewMetadataVisitor;
	}

	@Override
	public TableColumnMetadataVisitor createTableColumnMetadataVisitor() {
		
		return fbmOracleTableColumnMetadataVisitor;
	}

	@Override
	public DataTypeMapper createDatatypeMapper() {
		
		return fbmOracleToInfaDataTypeMapper;
	}

	@Override
	public String getSourceName() {
		// 
		return "FBM";
	}

}
