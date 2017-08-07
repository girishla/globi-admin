package com.globi.infa.generator.sil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.generator.AbstractMappingGenerator;
import com.globi.infa.generator.builder.ExpressionXformBuilder;
import com.globi.infa.generator.builder.FilterXformBuilder;
import com.globi.infa.generator.builder.InfaMappingObject;
import com.globi.infa.generator.builder.InfaTransformationObject;
import com.globi.infa.generator.builder.LookupXformBuilder;
import com.globi.infa.generator.builder.MappingBuilder;
import com.globi.infa.generator.builder.MappletBuilder;
import com.globi.infa.generator.builder.SequenceXformBuilder;
import com.globi.infa.generator.builder.SourceDefinitionBuilder;
import com.globi.infa.generator.builder.SourceQualifierBuilder;
import com.globi.infa.generator.builder.TargetDefinitionBuilder;
import com.globi.infa.generator.builder.UnionXformBuilder;
import com.globi.infa.generator.builder.UpdateStrategyXformBuilder;
import com.globi.infa.generator.builder.MappingBuilder.ReusableTransformationStep;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.metadata.src.SILInfaSourceColumnDefinition;
import com.globi.infa.workflow.SILWorkflow;
import com.globi.metadata.sourcesystem.SourceSystem;

import lombok.extern.slf4j.Slf4j;
import xjc.TRANSFORMATION;

@Slf4j
public class SILFactMappingGenerator extends AbstractMappingGenerator {

	private final SILWorkflow wfDefinition;
	private final List<InfaSourceColumnDefinition> allSourceColumns;
	private final List<SILInfaSourceColumnDefinition> matchedColumnsSIL;
	private final List<SILInfaSourceColumnDefinition> allTargetColumns;
	private final List<SILInfaSourceColumnDefinition> allOneToOneDimColumns;
	private final SourceSystem sourceSystem;
	private final DataSourceTableDTO sourceTable;
	private final Jaxb2Marshaller marshaller;
	private final DataTypeMapper sourceToXformDataTypeMapper;
	private final DataTypeMapper sourceToTargetDatatypeMapper;

	public SILFactMappingGenerator(SILWorkflow wfDefinition, //
			List<InfaSourceColumnDefinition> allSourceColumns, //
			List<SILInfaSourceColumnDefinition> allOneToOneDimColumns, //
			List<SILInfaSourceColumnDefinition> allTargetColumns, //
			List<SILInfaSourceColumnDefinition> matchedColumnsSIL, //
			SourceSystem sourceSystem, //
			DataSourceTableDTO sourceTable, //
			Jaxb2Marshaller marshaller, //
			DataTypeMapper sourceToXformDataTypeMapper, //
			DataTypeMapper sourceToTargetDatatypeMapper) {

		this.wfDefinition = wfDefinition;
		this.allSourceColumns = allSourceColumns;
		this.sourceSystem = sourceSystem;
		this.sourceTable = sourceTable;
		this.marshaller = marshaller;
		this.sourceToXformDataTypeMapper = sourceToXformDataTypeMapper;
		this.sourceToTargetDatatypeMapper = sourceToTargetDatatypeMapper;
		this.matchedColumnsSIL = matchedColumnsSIL;
		this.allTargetColumns=allTargetColumns;
		this.allOneToOneDimColumns=allOneToOneDimColumns;

	}

	InfaMappingObject getMapping() throws Exception {

		String stageTableName = wfDefinition.getStageName();
		String tableName = wfDefinition.getTableBaseName();

		String tableOwner = sourceSystem.getOwnerName();

		List<SILInfaSourceColumnDefinition> widColumns = allTargetColumns.stream()//
				.filter(col -> col.getColumnType().equals("Foreign Key"))//
				.collect(Collectors.toList());
				
		widColumns.stream().forEach(col->log.debug("**************" + col.getColumnName()));

		@SuppressWarnings("unchecked")
		InfaMappingObject mappingObjExtract = MappingBuilder//
				.newBuilder()//
				.simpleTableSyncClass("simpleTableSyncClass")//
				.sourceDefn(SourceDefinitionBuilder.newBuilder()//
						.sourceFromSeed("seedClass")
						.marshaller(marshaller)//
						.loadSourceFromSeed("Seed_SIL_Fact_SRC_Dim")
						.addFields((List<InfaSourceColumnDefinition>)(List<?>)allOneToOneDimColumns)//
						.name("D_" +tableName)//
						.build())//
				.sourceDefn(SourceDefinitionBuilder.newBuilder()//
						.sourceDefnFromPrototype("SourceFromPrototype")//
						.sourceDefn(sourceSystem, stageTableName, tableOwner)//
						.addFields(allSourceColumns)//
						.name(stageTableName)//
						.build())//
				.noMoreSources()//
				.targetDefn(TargetDefinitionBuilder.newBuilder()//
						.marshaller(marshaller)//
						.loadTargetFromSeed("Seed_SIL_Fact_TGT")//
						.mapper(sourceToTargetDatatypeMapper)//
						.addFields((List<InfaSourceColumnDefinition>)(List<?>)allTargetColumns)//
						.noMoreFields()//
						.name("F_" + tableName)//
						.build())//
				.noMoreTargets()//
				.mappletDefn(MappletBuilder.newBuilder()//
						.marshaller(marshaller)//
						.loadMappletFromSeed("Seed_SIL_MPL_GenerateCCAttributes")//
						.nameAlreadySet()//
						.build())//
				.noMoreMapplets()//
				.reusableTransformations(reusableLkpWidTransformationsFor(marshaller, widColumns))
				.reusableTransformation(LookupXformBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noInterpolationValues()//
						.loadLookupXformFromSeed("Seed_SIL_Xform_LKP_FactWid")//
						.nameAlreadySet()//
						.build())
				.reusableTransformation(LookupXformBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noInterpolationValues()//
						.loadLookupXformFromSeed("Seed_SIL_Xform_LKP_DT_WID")//
						.name("EXP_DT_WID_Generation")
						.build())
				.reusableTransformation(LookupXformBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noInterpolationValues()//
						.loadLookupXformFromSeed("Seed_SIL_Xform_LKP_FX")//
						.nameAlreadySet()//
						.build())
				.noMoreReusableXforms()
				.startMappingDefn("SIL_" + tableName + "_Fact")
				.transformation(SourceQualifierBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noMoreValues()//
						.loadSourceQualifierFromSeed("Seed_SIL_Xform_SQ_Fact")//
						.addFields(sourceToXformDataTypeMapper, (List<InfaSourceColumnDefinition>) (List<?>) matchedColumnsSIL)//
						.noMoreFilters().name("SQ_ExtractData")//
						.build())
				.autoConnectByName(stageTableName, "SQ_ExtractData")//
				.connector("D_" + tableName, "ROW_WID", "SQ_ExtractData", "ROW_WID")
				.noMoreTransformations()//
				.noMoreConnectors()//
				.noMoreTargetLoadOrders()//
				.noMoreMappingVariables()//
				.build();

		return mappingObjExtract;

	}
	
	
	private List<TRANSFORMATION> reusableLkpWidTransformationsFor(Jaxb2Marshaller marshaller,
			List<SILInfaSourceColumnDefinition> widColumns) throws Exception {

		return widColumns.stream()//
				.map(widCol -> {
					try {
						return getLkpWidXformFor(marshaller, widCol.getDimTableName());
					} catch (Exception e) {
						throw new RuntimeException("An unexpected error occured " + e.getStackTrace());
					}
				}).collect(Collectors.toList());

		
	}
	
	
	private TRANSFORMATION getLkpWidXformFor(Jaxb2Marshaller marshaller, String dimTableName)
			throws FileNotFoundException, IOException {

		TRANSFORMATION lkpXform = LookupXformBuilder.newBuilder()//
				.marshaller(marshaller)//
				.noInterpolationValues()//
				.loadLookupXformFromSeed("Seed_CMN_Xform_LKP_Wid_Standard")//
				.name("LKP_D_" + dimTableName).build();

		return lkpXform;

	}
	
	
	

}
