package com.globi.infa.datasource.vpt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.datasource.core.SourceMetadataFactory;
import com.globi.infa.datasource.core.TableColumnMetadataVisitor;
import com.globi.infa.datasource.core.TableColumnRepository;
import com.globi.infa.datasource.core.TableMetadataVisitor;
import com.globi.infa.datasource.core.TableRepository;
import com.globi.infa.datasource.type.sqlserver.SQLServerTableColumnMetadataVisitor;
import com.globi.infa.datasource.type.sqlserver.SQLServerTableMetadataVisitor;
import com.globi.infa.datasource.type.sqlserver.SQLServerInfaSourceToInfaXFormTypeMapper;

@Component
public class VPTSourceMetadataFactory implements SourceMetadataFactory {

	@Autowired
	private VPTTableRepository vptRepo;

	@Autowired
	private VPTTableColumnRepository vptTableColumnRepo;

	@Autowired
	private SQLServerTableMetadataVisitor vptTableMetadataVisitor;

	@Autowired
	private SQLServerTableColumnMetadataVisitor vptSQLServerTableColumnMetadataVisitor;

	@Autowired
	private SQLServerInfaSourceToInfaXFormTypeMapper vptSQLServerToInfaDataTypeMapper;

	@Override
	public TableRepository createTableRepository() {

		return vptRepo;
	}

	@Override
	public TableColumnRepository createTableColumnRepository() {

		return vptTableColumnRepo;
	}

	@Override
	public TableMetadataVisitor createTableMetadataVisitor() {

		return vptTableMetadataVisitor;
	}

	@Override
	public TableColumnMetadataVisitor createTableColumnMetadataVisitor() {

		return vptSQLServerTableColumnMetadataVisitor;
	}

	@Override
	public DataTypeMapper createDatatypeMapper() {

		return vptSQLServerToInfaDataTypeMapper;
	}

	@Override
	public String getSourceName() {
		//
		return "VPT";
	}

}
