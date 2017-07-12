package com.globi.infa.datasource.gcrm;

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
import com.globi.infa.datasource.type.oracle.OracleToInfaDataTypeMapper;


@Component
public class GCRMSourceMetadataFactory implements SourceMetadataFactory {
	
	@Autowired
	private GCRMTableRepository gcrmcrmRepo;
	
	@Autowired
	private GCRMTableColumnRepository gcrmTableColumnRepo;
	
	@Autowired
	private OracleTableMetadataVisitor gcrmTableMetadataVisitor;
	
	@Autowired
	private OracleTableColumnMetadataVisitor gcrmOracleTableColumnMetadataVisitor;
	
	@Autowired
	private OracleToInfaDataTypeMapper gcrmOracleToInfaDataTypeMapper;
	

	@Override
	public TableRepository createTableRepository() {
		
		return gcrmcrmRepo;
	}

	@Override
	public TableColumnRepository createTableColumnRepository() {
		
		return gcrmTableColumnRepo;
	}

	@Override
	public TableMetadataVisitor createTableMetadataVisitor() {
		
		return gcrmTableMetadataVisitor;
	}

	@Override
	public TableColumnMetadataVisitor createTableColumnMetadataVisitor() {
		
		return gcrmOracleTableColumnMetadataVisitor;
	}

	@Override
	public DataTypeMapper createDatatypeMapper() {
		
		return gcrmOracleToInfaDataTypeMapper;
	}

	@Override
	public String getSourceName() {
		// 
		return "CGL";
	}

}
