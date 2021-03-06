package com.globi.infa.datasource.gen;

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
import com.globi.infa.datasource.type.oracle.OracleTableColumnMetadataVisitor;
import com.globi.infa.datasource.type.oracle.OracleTableMetadataVisitor;
import com.globi.metadata.sourcesystem.SourceSystem;
import com.globi.metadata.sourcesystem.SourceSystemRepository;

@Component
public class GENSourceMetadataFactory implements SourceMetadataFactory {

	@Autowired
	private GENTableRepository gencrmRepo;

	@Autowired
	private GENTableColumnRepository genTableColumnRepo;

	@Autowired
	private OracleTableMetadataVisitor genTableMetadataVisitor;

	@Autowired
	private OracleTableColumnMetadataVisitor genOracleTableColumnMetadataVisitor;

	@Autowired
	private OracleInfaSourceToInfaXFormTypeMapper genOracleToInfaDataTypeMapper;

	
	@Autowired
	private OracleInfaSourceToInfaTargetTypeMapper genOracleInfaSourceToInfaTargetTypeMapper;

	
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
		return genOracleInfaSourceToInfaTargetTypeMapper;
	}
	
	@Override
	public TableRepository createTableRepository() {

		return gencrmRepo;
	}

	@Override
	public TableColumnRepository createTableColumnRepository() {

		return genTableColumnRepo;
	}

	@Override
	public TableMetadataVisitor createTableMetadataVisitor() {

		return genTableMetadataVisitor;
	}

	@Override
	public TableColumnMetadataVisitor createTableColumnMetadataVisitor() {

		return genOracleTableColumnMetadataVisitor;
	}

	@Override
	public DataTypeMapper createDatatypeMapper() {

		return genOracleToInfaDataTypeMapper;
	}

	@Override
	public String getSourceName() {
		//
		return "GEN";
	}

}
