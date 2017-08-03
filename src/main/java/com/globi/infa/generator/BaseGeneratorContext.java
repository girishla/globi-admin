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
public class BaseGeneratorContext implements GeneratorContext{

	protected TableRepository tableRepository;
	protected TableColumnRepository colRepository;
	protected TableMetadataVisitor tableQueryVisitor;
	protected TableColumnMetadataVisitor columnQueryVisitor;
	protected DataTypeMapper dataTypeMapper;
	protected DataTypeMapper sourceToTargetDatatypeMapper;
	protected SourceSystem source;
	protected InfaWorkflow inputWF;
	protected Jaxb2Marshaller marshaller;
	protected List<InfaSourceColumnDefinition> allSourceColumns;
	protected DataSourceTableDTO sourceTable;
	
	
	
	
}
