package com.globi.infa.generator;

import java.util.List;
import java.util.Optional;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.datasource.core.MetadataFactoryMapper;
import com.globi.infa.datasource.core.SourceMetadataFactory;
import com.globi.infa.datasource.core.TableColumnMetadataVisitor;
import com.globi.infa.datasource.core.TableColumnRepository;
import com.globi.infa.datasource.core.TableMetadataVisitor;
import com.globi.infa.datasource.core.TableRepository;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.metadata.sourcesystem.SourceSystem;

import lombok.Getter;


@Getter
public class DefaultGeneratorContext implements GeneratorContext{

	private TableRepository tableRepository;
	private TableColumnRepository colRepository;
	private TableMetadataVisitor tableQueryVisitor;
	private TableColumnMetadataVisitor columnQueryVisitor;
	private DataTypeMapper dataTypeMapper;
	private DataTypeMapper sourceToTargetDatatypeMapper;
	private SourceSystem source;
	private InfaWorkflow inputWF;
	private Jaxb2Marshaller marshaller;
	List<InfaSourceColumnDefinition> allSourceColumns;
	private DataSourceTableDTO sourceTable;
	
	
	public static DefaultGeneratorContext contextFor(String sourceName, String tblName, MetadataFactoryMapper mapper,InfaWorkflow inputWF){
		
		DefaultGeneratorContext context=new DefaultGeneratorContext();
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

		
		context.allSourceColumns = context.getColRepository().accept(context.getColumnQueryVisitor(), tblName);
		List<DataSourceTableDTO> sourceTables = context.getTableRepository().accept(context.getTableQueryVisitor());
		Optional<DataSourceTableDTO> sourceTableOptional = sourceTables.stream()//
				.filter(table -> table.getTableName().equals(tblName))
				.findFirst();
		
		if (!sourceTableOptional.isPresent()) {
			throw new IllegalArgumentException("Cannot find Source Table. Please ensure it is valid.");
		}else{
			context.sourceTable=sourceTableOptional.get();
		}
		
		return context;
		
	}
	
	

	
	
}
