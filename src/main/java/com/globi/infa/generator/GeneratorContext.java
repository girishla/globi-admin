package com.globi.infa.generator;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.datasource.core.SourceMetadataFactory;
import com.globi.infa.datasource.core.SourceMetadataFactoryMapper;
import com.globi.infa.datasource.core.TableColumnMetadataVisitor;
import com.globi.infa.datasource.core.TableColumnRepository;
import com.globi.infa.datasource.core.TableMetadataVisitor;
import com.globi.infa.datasource.core.TableRepository;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.metadata.sourcesystem.SourceSystem;

public class GeneratorContext {

	public TableRepository tableRepository;
	public TableColumnRepository colRepository;
	public TableMetadataVisitor tableQueryVisitor;
	public TableColumnMetadataVisitor columnQueryVisitor;
	public DataTypeMapper dataTypeMapper;
	public DataTypeMapper sourceToTargetDatatypeMapper;
	public SourceSystem source;
	public InfaWorkflow inputWF;
	public Jaxb2Marshaller marshaller;
	
	
	public static GeneratorContext contextFor(String sourceName,SourceMetadataFactoryMapper mapper,InfaWorkflow inputWF){
		
		GeneratorContext context=new GeneratorContext();
		SourceMetadataFactory sourceMetadataFactory= mapper.getMetadataFactoryMap()
				.get(sourceName);
		
		context.dataTypeMapper = sourceMetadataFactory.createDatatypeMapper();
		context.sourceToTargetDatatypeMapper=sourceMetadataFactory.createSourceToTargetDatatypeMapper();
		context.colRepository = sourceMetadataFactory.createTableColumnRepository();
		context.columnQueryVisitor = sourceMetadataFactory.createTableColumnMetadataVisitor();
		context.tableQueryVisitor=sourceMetadataFactory.createTableMetadataVisitor();
		context.tableRepository=sourceMetadataFactory.createTableRepository();
		context.source=sourceMetadataFactory.getSourceSystem();
		context.inputWF=inputWF;
		
		
		return context;
		
	}
	
	

	
	
}
