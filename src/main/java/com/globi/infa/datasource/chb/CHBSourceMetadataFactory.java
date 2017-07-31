package com.globi.infa.datasource.chb;

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
public class CHBSourceMetadataFactory implements SourceMetadataFactory {
	
	@Autowired
	private CHBTableRepository chbRepo;
	
	@Autowired
	private CHBTableColumnRepository chbTableColumnRepo;
	
	@Autowired
	private OracleNonOwnerTableMetadataVisitor chbTableMetadataVisitor;
	
	@Autowired
	private OracleTableColumnMetadataVisitor chbOracleTableColumnMetadataVisitor;
	
	
	@Autowired
	private OracleInfaSourceToInfaXFormTypeMapper chbOracleToInfaDataTypeMapper;
	
	
	@Autowired
	private OracleInfaSourceToInfaTargetTypeMapper chbOracleInfaSourceToInfaTargetTypeMapper;

	
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
		return chbOracleInfaSourceToInfaTargetTypeMapper;
	}
	

	@Override
	public TableRepository createTableRepository() {
		
		return chbRepo;
	}

	@Override
	public TableColumnRepository createTableColumnRepository() {
		
		return chbTableColumnRepo;
	}

	@Override
	public TableMetadataVisitor createTableMetadataVisitor() {
		
		return chbTableMetadataVisitor;
	}

	@Override
	public TableColumnMetadataVisitor createTableColumnMetadataVisitor() {
		
		return chbOracleTableColumnMetadataVisitor;
	}

	@Override
	public DataTypeMapper createDatatypeMapper() {
		
		return chbOracleToInfaDataTypeMapper;
	}

	@Override
	public String getSourceName() {
		// 
		return "CHB";
	}



}
