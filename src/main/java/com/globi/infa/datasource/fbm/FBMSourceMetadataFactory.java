package com.globi.infa.datasource.fbm;

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
import com.globi.infa.datasource.type.oracle.OracleViewMetadataVisitor;


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
	private OracleToInfaDataTypeMapper fbmOracleToInfaDataTypeMapper;
	

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
