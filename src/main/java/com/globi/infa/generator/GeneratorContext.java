package com.globi.infa.generator;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.datasource.core.TableColumnMetadataVisitor;
import com.globi.infa.datasource.core.TableColumnRepository;
import com.globi.infa.datasource.core.TableMetadataVisitor;
import com.globi.infa.datasource.core.TableRepository;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.metadata.sourcesystem.SourceSystem;

public interface GeneratorContext {

	public TableRepository getTableRepository() ;
	public TableColumnRepository getColRepository() ;
	public TableMetadataVisitor getTableQueryVisitor() ;
	public TableColumnMetadataVisitor getColumnQueryVisitor() ;
	public DataTypeMapper getDataTypeMapper() ;
	public DataTypeMapper getSourceToTargetDatatypeMapper();
	public SourceSystem getSource() ;
	public InfaWorkflow getInputWF();
	public Jaxb2Marshaller getMarshaller();
	
	
}
