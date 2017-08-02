package com.globi.infa.generator.sil;

import java.util.List;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.generator.AbstractMappingGenerator;
import com.globi.infa.generator.builder.InfaMappingObject;
import com.globi.infa.metadata.core.StringMap;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.workflow.SILWorkflow;
import com.globi.metadata.sourcesystem.SourceSystem;

public class SILDimensionMappingGenerator extends AbstractMappingGenerator {

	
	private final SILWorkflow wfDefinition;
	private final List<InfaSourceColumnDefinition> allSourceColumns;
	private final SourceSystem sourceSystem;
	private final DataSourceTableDTO sourceTable;
	private final StringMap sourceTableAbbreviation;
	private final Jaxb2Marshaller marshaller;
	private final DataTypeMapper dataTypeMapper;
	private final DataTypeMapper sourceToTargetDatatypeMapper;
	
	public SILDimensionMappingGenerator(SILWorkflow wfDefinition,//
			List<InfaSourceColumnDefinition> allSourceColumns,//
			SourceSystem sourceSystem,//
			DataSourceTableDTO sourceTable,//
			StringMap sourceTableAbbreviation,//
			Jaxb2Marshaller marshaller,//
			DataTypeMapper dataTypeMapper,//
			DataTypeMapper sourceToTargetDatatypeMapper){
		
		this.wfDefinition=wfDefinition;
		this.allSourceColumns=allSourceColumns;
		this.sourceSystem=sourceSystem;
		this.sourceTable=sourceTable;
		this.marshaller=marshaller;
		this.sourceTableAbbreviation=sourceTableAbbreviation;
		this.dataTypeMapper=dataTypeMapper;
		this.sourceToTargetDatatypeMapper=sourceToTargetDatatypeMapper;
		
		
	}
	
	
	
	InfaMappingObject getMapping() throws Exception {
		
		
		
		
		
		return null;
		
	}
		
}
