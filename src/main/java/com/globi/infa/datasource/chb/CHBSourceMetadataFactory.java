package com.globi.infa.datasource.chb;

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
public class CHBSourceMetadataFactory implements SourceMetadataFactory {
	
	@Autowired
	private CHBTableRepository chbRepo;
	
	@Autowired
	private CHBTableColumnRepository chbTableColumnRepo;
	
	@Autowired
	private OracleTableMetadataVisitor chbTableMetadataVisitor;
	
	@Autowired
	private OracleTableColumnMetadataVisitor chbOracleTableColumnMetadataVisitor;
	
	@Autowired
	private OracleInfaSourceToInfaXFormTypeMapper chbOracleToInfaDataTypeMapper;
	
	
	@Autowired
	private OracleInfaSourceToInfaTargetTypeMapper chbOracleInfaSourceToInfaTargetTypeMapper;

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
