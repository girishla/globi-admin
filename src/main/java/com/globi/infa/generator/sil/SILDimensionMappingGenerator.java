package com.globi.infa.generator.sil;

import java.util.List;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.generator.AbstractMappingGenerator;
import com.globi.infa.generator.builder.InfaMappingObject;
import com.globi.infa.generator.builder.MappingBuilder;
import com.globi.infa.generator.builder.SourceDefinitionBuilder;
import com.globi.infa.generator.builder.SourceQualifierBuilder;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.metadata.src.SILInfaSourceColumnDefinition;
import com.globi.infa.workflow.SILWorkflow;
import com.globi.metadata.sourcesystem.SourceSystem;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class SILDimensionMappingGenerator extends AbstractMappingGenerator {

	
	private final SILWorkflow wfDefinition;
	private final List<InfaSourceColumnDefinition> allSourceColumns;
	private final List<SILInfaSourceColumnDefinition> matchedColumnsSIL;
	private final SourceSystem sourceSystem;
	private final DataSourceTableDTO sourceTable;
	private final Jaxb2Marshaller marshaller;
	private final DataTypeMapper dataTypeMapper;
	private final DataTypeMapper sourceToTargetDatatypeMapper;
	
	public SILDimensionMappingGenerator(SILWorkflow wfDefinition,//
			List<InfaSourceColumnDefinition> allSourceColumns,//
			List<SILInfaSourceColumnDefinition> matchedColumnsSIL,//
			SourceSystem sourceSystem,//
			DataSourceTableDTO sourceTable,//
			Jaxb2Marshaller marshaller,//
			DataTypeMapper dataTypeMapper,//
			DataTypeMapper sourceToTargetDatatypeMapper){
		
		this.wfDefinition=wfDefinition;
		this.allSourceColumns=allSourceColumns;
		this.sourceSystem=sourceSystem;
		this.sourceTable=sourceTable;
		this.marshaller=marshaller;
		this.dataTypeMapper=dataTypeMapper;
		this.sourceToTargetDatatypeMapper=sourceToTargetDatatypeMapper;
		this.matchedColumnsSIL=matchedColumnsSIL;
		
		
	}
	
	
	
	InfaMappingObject getMapping() throws Exception {
		
		String stageTableName = wfDefinition.getStageName();
		String dbName = sourceSystem.getDbName();
		String tableOwner=sourceSystem.getOwnerName();
		
		log.info(matchedColumnsSIL.toString());
		
		InfaMappingObject mappingObjExtract = MappingBuilder//
				.newBuilder()//
				.simpleTableSyncClass("simpleTableSyncClass")//
				.sourceDefn(SourceDefinitionBuilder.newBuilder()//
						.sourceFromSeed("sourceFromSeedClass")
						.marshaller(marshaller)
						.loadSourceFromSeed("Seed_SIL_Source_UnspecifiedVirtual")
						.noFields()
						.nameAlreadySet()
						.build())//
				.sourceDefn(SourceDefinitionBuilder.newBuilder()//
						.sourceDefnFromPrototype("SourceFromPrototype")//
						.sourceDefn(sourceSystem, stageTableName, tableOwner)//
						.addFields(allSourceColumns)//
						.name(stageTableName)//
						.build())//
				.noMoreSources()//
				.noMoreTargets()
				.noMoreMapplets()
				.startMappingDefn("SIL_"+ stageTableName + "Dimension")
				.transformation(SourceQualifierBuilder.newBuilder()//
						.marshaller(marshaller)
						.noMoreValues()
						.loadSourceQualifierFromSeed("Seed_SIL_Xform_SQ_Unspecified")//
						.noMoreFields()//
						.noMoreFilters()//
						.nameAlreadySet()//
						.build())
				.transformation(SourceQualifierBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noMoreValues()//
						.loadSourceQualifierFromSeed("Seed_CMN_SourceQualifier")//
						.addFields(dataTypeMapper, (List<InfaSourceColumnDefinition>)(List<?>)matchedColumnsSIL)//
						.noMoreFilters()
						.name("SQ_ExtractData")//
						.build())
				.noMoreTransformations()
				.noMoreConnectors()
				.noMoreTargetLoadOrders()
				.noMoreMappingVariables()
				.build();
		
		
		return mappingObjExtract;
		
	}
		
}
