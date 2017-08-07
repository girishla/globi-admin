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
import com.globi.infa.generator.builder.MappletBuilder;
import com.globi.infa.generator.builder.SequenceXformBuilder;
import com.globi.infa.generator.builder.SourceDefinitionBuilder;
import com.globi.infa.generator.builder.SourceQualifierBuilder;
import com.globi.infa.generator.builder.TargetDefinitionBuilder;
import com.globi.infa.generator.builder.UnionXformBuilder;
import com.globi.infa.generator.builder.UpdateStrategyXformBuilder;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.metadata.src.SILInfaSourceColumnDefinition;
import com.globi.infa.workflow.SILWorkflow;
import com.globi.metadata.sourcesystem.SourceSystem;

import lombok.extern.slf4j.Slf4j;

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

		List<SILInfaSourceColumnDefinition> nonSysMatchedCols = matchedColumnsSIL.stream()//
				.filter(col -> !col.getColumnType().equals("System"))//
				.collect(Collectors.toList());
		
		

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
				.reusableTransformation(LookupXformBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noInterpolationValues()//
						.loadLookupXformFromSeed("Seed_SIL_REUSE_LKP_Dim_PGUID")//
						.nameAlreadySet()//
						.build())
				.noMoreReusableXforms()
				.startMappingDefn("SIL_" + tableName + "_Dimension")
				.transformation(SourceQualifierBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noMoreValues()//
						.loadSourceQualifierFromSeed("Seed_SIL_Xform_SQ_Fact")//
						.addFields(sourceToXformDataTypeMapper, (List<InfaSourceColumnDefinition>) (List<?>) matchedColumnsSIL)//
						.noMoreFilters().name("SQ_ExtractData")//
						.build())
				.transformation(UnionXformBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noMoreValues()//
						.loadUnionXformFromSeed("Seed_SIL_Xform_UNION_Unspecified")//
						.addOutputFields(sourceToXformDataTypeMapper,
								(List<InfaSourceColumnDefinition>) (List<?>) matchedColumnsSIL.stream()//
										.filter(col -> col.getColumnType().equals("Foreign Key")
												|| col.getColumnType().equals("Attribute")
												|| col.getColumnType().equals("Measure Attribute"))
										.collect(Collectors.toList()))//
						.addInputFields("DATA", 1)//
						.addInputFields("UNSPEC", 2)//
						.noMoreInputFields()//
						.addFieldDependencies(1)//
						.addFieldDependencies(2)//
						.noMoreFieldDependencies()//
						.nameAlreadySet()//
						.build())
				.autoConnectByName(stageTableName, "SQ_ExtractData")//
				.connector("VIRTUAL_EXP", "NUMERIC_EXPRESSION", "SQ_ExtractUnspecified", "UNSPEC_WID")
				.connector("VIRTUAL_EXP", "VARCHAR_EXPRESSION", "SQ_ExtractUnspecified", "UNSPEC_PGUID")
				.connector("VIRTUAL_EXP", "VARCHAR_EXPRESSION", "SQ_ExtractUnspecified", "UNSPEC_N_FLAG")
				.connector("VIRTUAL_EXP", "VARCHAR_EXPRESSION", "SQ_ExtractUnspecified", "UNSPEC_Y_FLAG")
				.connector("VIRTUAL_EXP", "DATE_EXPRESSION", "SQ_ExtractUnspecified", "UNSPEC_CREATED_DT")
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
						.mapper(sourceToXformDataTypeMapper)//
						.addFields((List<InfaSourceColumnDefinition>) (List<?>)nonSysMatchedCols)//
						.addMD5HashField("HASH_RECORD",(List<InfaSourceColumnDefinition>) (List<?>)nonSysMatchedCols)//
						.noMoreFields()//
						.nameAlreadySet()//
						.build())//
				.autoConnectByName("UNION_UnspecifiedData", "EXP_CalculateHashes")//
				.transformation(ExpressionXformBuilder.newBuilder()//
						.expressionFromPrototype("ExpFromPrototype")//
						.expression("EXP_ETL_Parameters")//
						.mapper(sourceToXformDataTypeMapper)//
						.addInputField("PGUID", "string", "100", "0")//
						.addEtlProcWidField("ETL_PROC_WID")//
						.noMoreFields()//
						.nameAlreadySet()//
						.build())//
				.connector("UNION_UnspecifiedData", "PGUID", "EXP_ETL_Parameters", "PGUID")
				.connector("UNION_UnspecifiedData", "PGUID", "LKP_SYS_Dimension_PGUID", "IN_PGUID")
				.autoConnectByTransformedName("LKP_SYS_Dimension_PGUID", "FIL_ExcludeRecords", name -> "LKP_" + name)
				.autoConnectByName("UNION_UnspecifiedData", "FIL_ExcludeRecords")//
				.connector("EXP_CalculateHashes", "HASH_RECORD", "FIL_ExcludeRecords", "HASH_RECORD")
				.connector("EXP_ETL_Parameters", "ETL_PROC_WID", "FIL_ExcludeRecords", "ETL_PROC_WID")
				.transformation(SequenceXformBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noInterpolationValues()
						.loadExpressionXformFromSeed("Seed_PTP_WidSequence")//
						.name("SEQ_" + tableName)
						.build())
				.connector("SEQ_" + tableName, "NEXTVAL", "MPL_Resolve_ChangeCapture", "ROW_WID_NEW")
				.autoConnectByName("FIL_ExcludeRecords", "EXP_Collect")
				.connector("MPL_Resolve_ChangeCapture", "UPDATE_FLG", "EXP_Collect", "UPDATE_FLG")
				.connector("MPL_Resolve_ChangeCapture", "ROW_WID", "EXP_Collect", "ROW_WID")
				.connector("MPL_Resolve_ChangeCapture", "W_CREATED_DT", "EXP_Collect", "W_CREATED_DT")
				.connector("MPL_Resolve_ChangeCapture", "W_UPDATED_DT", "EXP_Collect", "W_UPDATED_DT")
				.connector("MPL_Resolve_ChangeCapture", "EFF_START_DT", "EXP_Collect", "EFF_START_DT")
				.connector("MPL_Resolve_ChangeCapture", "EFF_END_DT", "EXP_Collect", "EFF_END_DT")

				
				.connector("FIL_ExcludeRecords", "LKP_CREATED_DT", "MPL_Resolve_ChangeCapture", "W_CREATED_DT_LKP")
				.connector("FIL_ExcludeRecords", "LKP_HASH_SCD", "MPL_Resolve_ChangeCapture", "SCD_HASH_LKP")
				.connector("FIL_ExcludeRecords", "UNSPEC_ROW_WID", "MPL_Resolve_ChangeCapture", "ROW_WID_UNSPEC")
				.connector("FIL_ExcludeRecords", "HASH_SCD", "MPL_Resolve_ChangeCapture", "SCD_HASH_IN")
				.connector("FIL_ExcludeRecords", "S_UPDATED_DT", "MPL_Resolve_ChangeCapture", "S_UPDATED_DT_IN")
				.connector("FIL_ExcludeRecords", "LKP_ROW_WID", "MPL_Resolve_ChangeCapture", "ROW_WID_LKP")

				
				.transformation(UpdateStrategyXformBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noInterpolationValues()
						.loadUpdateStrategyXformFromSeed("Seed_SIL_Xform_UPD_Strategy")//
						.nameAlreadySet()//
						.build())
					.autoConnectByName("UPD_Dimension", "D_" + tableName)
				.connector("UPD_Dimension", "RECORD_HASH", "D_" + tableName, "HASH_RECORD")
	
				.noMoreTransformations()//
				.noMoreConnectors()//
				.noMoreTargetLoadOrders()//
				.noMoreMappingVariables()//
				.build();

		return mappingObjExtract;

	}

}
