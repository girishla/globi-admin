package com.globi.infa.generator.sil;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.generator.AbstractMappingGenerator;
import com.globi.infa.generator.builder.ExpressionXformBuilder;
import com.globi.infa.generator.builder.FilterXformBuilder;
import com.globi.infa.generator.builder.InfaMappingObject;
import com.globi.infa.generator.builder.LookupXformBuilder;
import com.globi.infa.generator.builder.MappingBuilder;
import com.globi.infa.generator.builder.SourceDefinitionBuilder;
import com.globi.infa.generator.builder.SourceQualifierBuilder;
import com.globi.infa.generator.builder.UnionXformBuilder;
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

	public SILDimensionMappingGenerator(SILWorkflow wfDefinition, //
			List<InfaSourceColumnDefinition> allSourceColumns, //
			List<SILInfaSourceColumnDefinition> matchedColumnsSIL, //
			SourceSystem sourceSystem, //
			DataSourceTableDTO sourceTable, //
			Jaxb2Marshaller marshaller, //
			DataTypeMapper dataTypeMapper, //
			DataTypeMapper sourceToTargetDatatypeMapper) {

		this.wfDefinition = wfDefinition;
		this.allSourceColumns = allSourceColumns;
		this.sourceSystem = sourceSystem;
		this.sourceTable = sourceTable;
		this.marshaller = marshaller;
		this.dataTypeMapper = dataTypeMapper;
		this.sourceToTargetDatatypeMapper = sourceToTargetDatatypeMapper;
		this.matchedColumnsSIL = matchedColumnsSIL;

	}

	InfaMappingObject getMapping() throws Exception {

		String stageTableName = wfDefinition.getStageName();
		String dbName = sourceSystem.getDbName();
		String tableOwner = sourceSystem.getOwnerName();

		List<SILInfaSourceColumnDefinition> nonSysMatchedCols = matchedColumnsSIL.stream()//
				.filter(col -> !col.getColumnType().equals("System"))//
				.collect(Collectors.toList());
		
		List<SILInfaSourceColumnDefinition> attribCols = matchedColumnsSIL.stream()//
				.filter(col -> col.getColumnType().equals("Attribute"))//
				.collect(Collectors.toList());

		attribCols.stream().forEach(col->log.info("%%%%%%%" + col.getColumnName()));

		@SuppressWarnings("unchecked")
		InfaMappingObject mappingObjExtract = MappingBuilder//
				.newBuilder()//
				.simpleTableSyncClass("simpleTableSyncClass")//
				.sourceDefn(SourceDefinitionBuilder.newBuilder()//
						.sourceFromSeed("sourceFromSeedClass")//
						.marshaller(marshaller).loadSourceFromSeed("Seed_SIL_Source_UnspecifiedVirtual")//
						.noFields()//
						.nameAlreadySet()//
						.build())//
				.sourceDefn(SourceDefinitionBuilder.newBuilder()//
						.sourceDefnFromPrototype("SourceFromPrototype")//
						.sourceDefn(sourceSystem, stageTableName, tableOwner)//
						.addFields(allSourceColumns)//
						.name(stageTableName)//
						.build())//
				.noMoreSources()//
				.noMoreTargets()//
				.noMoreMapplets()//
				.reusableTransformation(LookupXformBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noInterpolationValues()//
						.loadLookupXformFromSeed("Seed_SIL_REUSE_LKP_Dim_PGUID")//
						.nameAlreadySet()//
						.build())
				.noMoreReusableXforms()
				.startMappingDefn("SIL_" + stageTableName + "_Dimension")
				.transformation(SourceQualifierBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noMoreValues().loadSourceQualifierFromSeed("Seed_SIL_Xform_SQ_Unspecified")//
						.noMoreFields()//
						.noMoreFilters()//
						.nameAlreadySet()//
						.build())
				.transformation(SourceQualifierBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noMoreValues()//
						.loadSourceQualifierFromSeed("Seed_CMN_SourceQualifier")//
						.addFields(dataTypeMapper, (List<InfaSourceColumnDefinition>) (List<?>) matchedColumnsSIL)//
						.noMoreFilters().name("SQ_ExtractData")//
						.build())
				.transformation(UnionXformBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noMoreValues()//
						.loadUnionXformFromSeed("Seed_SIL_Xform_UNION_Unspecified")//
						.addOutputFields(dataTypeMapper,
								(List<InfaSourceColumnDefinition>) (List<?>) matchedColumnsSIL.stream()//
										.filter(col -> col.getColumnType().equals("Foreign Key")
												|| col.getColumnType().equals("Attribute")
												|| col.getColumnType().equals("Measure Attribute"))
										.collect(Collectors.toList()))//
						.addInputFields("DATA", 1)//
						.addInputFields("UNSPEC", 2)//
						.noMoreInputFields()//
						.addFieldDependencies(1).addFieldDependencies(2).noMoreFieldDependencies().nameAlreadySet()//
						.build())
				.autoConnectByName(stageTableName, "SQ_ExtractData")//
				.autoConnectByName("VIRTUAL_EXP", "SQ_ExtractUnspecified")//
				.autoConnectByTransformedName("SQ_ExtractData", "UNION_UnspecifiedData", str -> str + 1)
				.connector("SQ_ExtractUnspecified", "UNSPEC_WID", "UNION_UnspecifiedData", "ROW_WID2")
				.connector("SQ_ExtractUnspecified", "UNSPEC_PGUID", "UNION_UnspecifiedData", "DATASOURCE_NUM_ID2")
				.connector("SQ_ExtractUnspecified", "UNSPEC_PGUID", "UNION_UnspecifiedData", "INTEGRATION_ID2")
				.connector("SQ_ExtractUnspecified", "UNSPEC_PGUID", "UNION_UnspecifiedData", "PGUID2")
				.connector("SQ_ExtractUnspecified", "UNSPEC_N_FLAG", "UNION_UnspecifiedData", "DELETE_FLG2")
				.connector("SQ_ExtractUnspecified", "UNSPEC_Y_FLAG", "UNION_UnspecifiedData", "CURRENT_FLG2")
				.connector("SQ_ExtractUnspecified", "UNSPEC_CREATED_DT", "UNION_UnspecifiedData", "S_CREATED_DT2")
				.connector("SQ_ExtractUnspecified", "UNSPEC_CREATED_DT", "UNION_UnspecifiedData", "S_UPDATED_DT2")
				.transformation(ExpressionXformBuilder.newBuilder()//
						.expressionFromPrototype("ExpFromPrototype")//
						.expression("EXP_CalculateHashes")//
						.mapper(dataTypeMapper)//
						.addFields((List<InfaSourceColumnDefinition>) (List<?>)nonSysMatchedCols)//
						.addMD5HashField("HASH_RECORD",(List<InfaSourceColumnDefinition>) (List<?>)nonSysMatchedCols)//
						.noMoreFields()//
						.nameAlreadySet()//
						.build())//
				.autoConnectByName("UNION_UnspecifiedData", "EXP_CalculateHashes")//
				.transformation(ExpressionXformBuilder.newBuilder()//
						.expressionFromPrototype("ExpFromPrototype")//
						.expression("EXP_ETL_Parameters")//
						.mapper(dataTypeMapper)//
						.addInputField("PGUID", "string", "100", "0")//
						.addEtlProcWidField("ETL_PROC_WID")//
						.noMoreFields()//
						.nameAlreadySet()//
						.build())//
				.connector("UNION_UnspecifiedData", "PGUID", "EXP_ETL_Parameters", "PGUID")
				.transformation(FilterXformBuilder.newBuilder()//
						.FilterFromSeed("SeedClass")//
						.marshaller(marshaller)//
						.noInterpolationValues()//
						.loadFilterXformFromSeed("Seed_SIL_Xform_FIL")//
						.addFields((List<InfaSourceColumnDefinition>) (List<?>)attribCols)//
						.noMoreFields()//
						.addCondition("(ISNULL(LKP_ETL_PROC_WID) OR LKP_ETL_PROC_WID != ETL_PROC_WID) AND (ISNULL(LKP_HASH_RECORD) OR LKP_HASH_RECORD != HASH_RECORD)")//
						.noMoreConditions()//
						.nameAlreadySet()//
						.build())
				.autoConnectByTransformedName("LKP_SYS_Dimension_PGUID", "FIL_ExcludeRecords", name -> "LKP_" + name)
				.autoConnectByName("UNION_UnspecifiedData", "FIL_ExcludeRecords")//
				.connector("EXP_CalculateHashes", "HASH_RECORD", "FIL_ExcludeRecords", "HASH_RECORD")
				.connector("EXP_ETL_Parameters", "ETL_PROC_WID", "FIL_ExcludeRecords", "ETL_PROC_WID")

				//Seed_SIL_MPL_GenerateCCAttributes
				.noMoreTransformations()//
				.noMoreConnectors()//
				.noMoreTargetLoadOrders()//
				.noMoreMappingVariables()//
				.build();

		return mappingObjExtract;

	}

}
