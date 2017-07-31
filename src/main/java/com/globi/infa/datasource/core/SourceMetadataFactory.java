package com.globi.infa.datasource.core;

import com.globi.metadata.sourcesystem.SourceSystem;

public interface SourceMetadataFactory {

	public abstract TableRepository createTableRepository();
	public abstract TableColumnRepository createTableColumnRepository();
	public abstract TableMetadataVisitor createTableMetadataVisitor();
	public abstract TableColumnMetadataVisitor createTableColumnMetadataVisitor();
	public abstract DataTypeMapper createDatatypeMapper();
	public abstract DataTypeMapper createSourceToTargetDatatypeMapper();
	public abstract SourceSystem getSourceSystem();
	
	public abstract String getSourceName();
	
	
}
