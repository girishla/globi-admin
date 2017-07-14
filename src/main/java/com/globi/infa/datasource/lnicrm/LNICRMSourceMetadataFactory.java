package com.globi.infa.datasource.lnicrm;

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


@Component
public class LNICRMSourceMetadataFactory implements SourceMetadataFactory {
	
	@Autowired
	private LNICRMTableRepository lnicrmRepo;
	
	@Autowired
	private LNICRMTableColumnRepository lniTableColumnRepo;
	
	@Autowired
	private OracleTableMetadataVisitor lniTableMetadataVisitor;
	
	@Autowired
	private OracleTableColumnMetadataVisitor lniOracleTableColumnMetadataVisitor;
	
	@Autowired
	private OracleInfaSourceToInfaXFormTypeMapper lniOracleToInfaDataTypeMapper;
	
	@Autowired
	private OracleInfaSourceToInfaTargetTypeMapper lniOracleInfaSourceToInfaTargetTypeMapper;

	@Override
	public DataTypeMapper createSourceToTargetDatatypeMapper() {
		return lniOracleInfaSourceToInfaTargetTypeMapper;
	}
	
	@Override
	public TableRepository createTableRepository() {
		
		return lnicrmRepo;
	}

	@Override
	public TableColumnRepository createTableColumnRepository() {
		
		return lniTableColumnRepo;
	}

	@Override
	public TableMetadataVisitor createTableMetadataVisitor() {
		
		return lniTableMetadataVisitor;
	}

	@Override
	public TableColumnMetadataVisitor createTableColumnMetadataVisitor() {
		
		return lniOracleTableColumnMetadataVisitor;
	}

	@Override
	public DataTypeMapper createDatatypeMapper() {
		
		return lniOracleToInfaDataTypeMapper;
	}

	@Override
	public String getSourceName() {
		// 
		return "CUK";
	}



}
