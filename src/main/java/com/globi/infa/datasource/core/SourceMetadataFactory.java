package com.globi.infa.datasource.core;

public interface SourceMetadataFactory {

	public abstract TableRepository createTableRepository();
	public abstract TableColumnRepository createTableColumnRepository();
	public abstract TableMetadataVisitor createTableMetadataVisitor();
	public abstract TableColumnMetadataVisitor createTableColumnMetadataVisitor();
	public abstract DataTypeMapper createDatatypeMapper();
	
	public abstract String getSourceName();
	
	
}
