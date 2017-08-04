package com.globi.infa.datasource.law;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.datasource.core.SourceMetadataFactory;
import com.globi.infa.datasource.core.TableColumnMetadataVisitor;
import com.globi.infa.datasource.core.TableColumnRepository;
import com.globi.infa.datasource.core.TableMetadataVisitor;
import com.globi.infa.datasource.core.TableRepository;
import com.globi.infa.datasource.type.oracle.OracleInfaSourceToInfaTargetTypeMapper;
import com.globi.infa.datasource.type.oracle.OracleInfaSourceToInfaXFormTypeMapper;
import com.globi.infa.datasource.type.oracle.OracleNonOwnerTableMetadataVisitor;
import com.globi.infa.datasource.type.oracle.OracleTableColumnMetadataVisitor;
import com.globi.metadata.sourcesystem.SourceSystem;
import com.globi.metadata.sourcesystem.SourceSystemRepository;


@Component
public class LAWSourceMetadataFactory implements SourceMetadataFactory {
	
	@Autowired
	private LAWTableRepository lawRepo;
	
	@Autowired
	private LAWTableColumnRepository lawTableColumnRepo;
	
	@Autowired
	private OracleNonOwnerTableMetadataVisitor lawTableMetadataVisitor;
	
	@Autowired
	private OracleTableColumnMetadataVisitor lawOracleTableColumnMetadataVisitor;
	
	
	@Autowired
	private OracleInfaSourceToInfaXFormTypeMapper lawOracleToInfaDataTypeMapper;
	
	
	@Autowired
	private OracleInfaSourceToInfaTargetTypeMapper lawOracleInfaSourceToInfaTargetTypeMapper;

	
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
		return lawOracleInfaSourceToInfaTargetTypeMapper;
	}
	

	@Override
	public TableRepository createTableRepository() {
		
		return lawRepo;
	}

	@Override
	public TableColumnRepository createTableColumnRepository() {
		
		return lawTableColumnRepo;
	}

	@Override
	public TableMetadataVisitor createTableMetadataVisitor() {
		
		return lawTableMetadataVisitor;
	}

	@Override
	public TableColumnMetadataVisitor createTableColumnMetadataVisitor() {
		
		return lawOracleTableColumnMetadataVisitor;
	}

	@Override
	public DataTypeMapper createDatatypeMapper() {
		
		return lawOracleToInfaDataTypeMapper;
	}

	@Override
	public String getSourceName() {
		// 
		return "LAW";
	}



}
