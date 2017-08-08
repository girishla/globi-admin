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
import com.rits.cloning.Cloner;

import lombok.extern.slf4j.Slf4j;
import xjc.TRANSFORMATION;
import xjc.TRANSFORMFIELD;

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
		this.allTargetColumns = allTargetColumns;
		this.allOneToOneDimColumns = allOneToOneDimColumns;

	}

	InfaMappingObject getMapping() throws Exception {

		String stageTableName = wfDefinition.getStageName();
		String tableName = wfDefinition.getTableBaseName();

		String tableOwner = sourceSystem.getOwnerName();

		List<SILInfaSourceColumnDefinition> widColumns = allTargetColumns.stream()//
				.filter(col -> col.getColumnType().equals("Foreign Key"))//
				.collect(Collectors.toList());

		widColumns.stream().forEach(col -> log.debug("**************" + col.getColumnName()));

		@SuppressWarnings("unchecked")
		InfaMappingObject mappingObjExtract = MappingBuilder//
				.newBuilder()//
				.simpleTableSyncClass("simpleTableSyncClass")//
				.sourceDefn(SourceDefinitionBuilder.newBuilder()//
						.sourceFromSeed("seedClass").marshaller(marshaller)//
						.loadSourceFromSeed("Seed_SIL_Fact_SRC_Dim")
						.addFields((List<InfaSourceColumnDefinition>) (List<?>) allOneToOneDimColumns)//
						.name("D_" + tableName)//
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
						.addFields((List<InfaSourceColumnDefinition>) (List<?>) allTargetColumns)//
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
				.reusableTransformation(hasAmountFields() ? LookupXformBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noInterpolationValues()//
						.loadLookupXformFromSeed("Seed_SIL_Xform_LKP_FX")//
						.nameAlreadySet()//
						.build() : null)
				.noMoreReusableXforms().startMappingDefn("SIL_" + tableName + "_Fact")
				.transformation(SourceQualifierBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noMoreValues()//
						.loadSourceQualifierFromSeed("Seed_SIL_Xform_SQ_Fact")//
						.addFields(sourceToXformDataTypeMapper,
								(List<InfaSourceColumnDefinition>) (List<?>) matchedColumnsSIL)//
						.noMoreFilters().name("SQ_ExtractData")//
						.build())
				.transformation(ExpressionXformBuilder.newBuilder()//
						.expressionFromPrototype("ExpFromPrototype")//
						.expression("EXP_FK_Resolution")//
						.mapper(sourceToXformDataTypeMapper)//
						.addTransformFields(getWidResolveExpressionInputFields())
						.addTransformFields(getWidResolveExpressionOutputFieldsExceptDates())
						.addTransformFields(getWidResolveExpressionOutputFieldsForDates())//
						.noMoreFields()//
						.nameAlreadySet()//
						.build())//
				.transformation(hasAmountFields() ? ExpressionXformBuilder.newBuilder()//
						.expressionFromSeed("seedClass").marshaller(marshaller).noInterpolationValues()
						.loadExpressionXformFromSeed("Seed_SIL_Xform_EXP_Currency").mapper(sourceToXformDataTypeMapper)
						.addTransformFields(getAmountsExpressionForCurrencyColumns())
						.addTransformFields(getGlobalAmountsExpressionForCurrencyColumns()).noMoreFields()//
						.name("EXP_Amounts").build() : null)//
				.transformation(ExpressionXformBuilder.newBuilder()//
						.expressionFromSeed("seedClass")//
						.marshaller(marshaller)//
						.noInterpolationValues()
						.loadExpressionXformFromSeed("Seed_SIL_Xform_EXP_Fact_Collect")
						.mapper(sourceToXformDataTypeMapper)//
						.noMoreFields()//
						.name("EXP_Collect")//
						.build())//
				.transformation(ExpressionXformBuilder.newBuilder()//
						.expressionFromSeed("seedClass")//
						.marshaller(marshaller)//
						.noInterpolationValues()
						.loadExpressionXformFromSeed("Seed_SIL_Xform_US_Fact")//
						.mapper(sourceToXformDataTypeMapper)
						.noMoreFields()//
						.name("UPD_Fact").build())//
				.transformationCopyConnectAllFields("EXP_FK_Resolution", "EXP_Collect")
				.transformationCopyConnectAllFields("EXP_Amounts", "EXP_Collect")
				.transformationCopyMapConnectAllFields("EXP_Collect", "UPD_Fact", this::mapToUpdateStrategyField)
				.autoConnectByName(stageTableName, "SQ_ExtractData")//
				.connector("D_" + tableName, "ROW_WID", "SQ_ExtractData", "ROW_WID")//
				.autoConnectByName("UPD_Fact", "F_" + tableName)
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

	private boolean hasAmountFields() {

		matchedColumnsSIL.stream()
				.forEach(col -> log.debug("^^^^^^^^" + col.getColumnType() + ":" + col.getColumnDataType()));

		return matchedColumnsSIL.stream()//
				.anyMatch(col -> col.getColumnName().startsWith("DOC_") && col.getColumnType().equals("Measure"));

	}

	private List<TRANSFORMFIELD> getWidResolveExpressionInputFields() {

		return matchedColumnsSIL.stream()//
				.filter(col -> col.getColumnType().equals("Foreign Key")).map(col -> {

					TRANSFORMFIELD xformExpressionField = new TRANSFORMFIELD();
					xformExpressionField.setDATATYPE(sourceToXformDataTypeMapper.mapType(col.getColumnDataType()));
					xformExpressionField.setDEFAULTVALUE("");
					xformExpressionField.setDESCRIPTION("");
					xformExpressionField.setEXPRESSION("");
					xformExpressionField.setEXPRESSIONTYPE("GENERAL");
					xformExpressionField.setNAME(col.getColumnName());
					xformExpressionField.setPICTURETEXT("");
					xformExpressionField.setPORTTYPE("INPUT");
					xformExpressionField.setPRECISION(Integer.toString(col.getPrecision()));
					xformExpressionField.setSCALE(Integer.toString(col.getScale()));

					return xformExpressionField;

				}).collect(Collectors.toList());

	}

	private List<TRANSFORMFIELD> getWidResolveExpressionOutputFieldsExceptDates() {

		return matchedColumnsSIL.stream()//
				.filter(col -> col.getColumnType().equals("Foreign Key") && !col.getColumnDataType().equals("date"))
				.map(col -> {

					TRANSFORMFIELD xformExpressionField = new TRANSFORMFIELD();
					xformExpressionField.setDATATYPE("decimal");
					xformExpressionField.setDEFAULTVALUE("");
					xformExpressionField.setDESCRIPTION("");
					xformExpressionField.setEXPRESSION(
							String.format(":LKP.LKP_D_%s(%s)", col.getDimTableName(), col.getColumnName()));
					xformExpressionField.setEXPRESSIONTYPE("GENERAL");
					xformExpressionField.setNAME(col.getColumnName().replace("PGUID", "WID"));
					xformExpressionField.setPICTURETEXT("");
					xformExpressionField.setPORTTYPE("OUTPUT");
					xformExpressionField.setPRECISION("10");
					xformExpressionField.setSCALE("0");

					return xformExpressionField;

				}).collect(Collectors.toList());

	}

	private List<TRANSFORMFIELD> getWidResolveExpressionOutputFieldsForDates() {

		return matchedColumnsSIL.stream()//
				.filter(col -> col.getColumnType().equals("Foreign Key") && col.getColumnDataType().equals("date"))
				.map(col -> {

					TRANSFORMFIELD xformExpressionField = new TRANSFORMFIELD();
					xformExpressionField.setDATATYPE("decimal");
					xformExpressionField.setDEFAULTVALUE("");
					xformExpressionField.setDESCRIPTION("");
					xformExpressionField
							.setEXPRESSION(String.format("TO_INTEGER(TO_CHAR(%s,'YYYYMMDD'))", col.getColumnName()));
					xformExpressionField.setEXPRESSIONTYPE("GENERAL");
					xformExpressionField.setNAME(col.getColumnName() + "_WID");
					xformExpressionField.setPICTURETEXT("");
					xformExpressionField.setPORTTYPE("OUTPUT");
					xformExpressionField.setPRECISION("10");
					xformExpressionField.setSCALE("0");

					return xformExpressionField;

				}).collect(Collectors.toList());

	}

	private List<TRANSFORMFIELD> getAmountsExpressionForCurrencyColumns() {

		return matchedColumnsSIL.stream()//
				.filter(col -> col.getColumnName().startsWith("DOC_") && col.getColumnType().equals("Measure"))
				.map(col -> {

					TRANSFORMFIELD xformExpressionField = new TRANSFORMFIELD();
					xformExpressionField.setDATATYPE("decimal");
					xformExpressionField.setDEFAULTVALUE("");
					xformExpressionField.setDESCRIPTION("");
					xformExpressionField.setEXPRESSION(col.getColumnName());
					xformExpressionField.setEXPRESSIONTYPE("GENERAL");
					xformExpressionField.setNAME(col.getColumnName());
					xformExpressionField.setPICTURETEXT("");
					xformExpressionField.setPORTTYPE("INPUT/OUTPUT");
					xformExpressionField.setPRECISION("22");
					xformExpressionField.setSCALE("7");

					return xformExpressionField;

				}).collect(Collectors.toList());

	}

	private List<TRANSFORMFIELD> getGlobalAmountsExpressionForCurrencyColumns() {

		return matchedColumnsSIL.stream()//
				.filter(col -> col.getColumnName().startsWith("DOC_") && col.getColumnType().equals("Measure"))
				.map(col -> {

					TRANSFORMFIELD xformExpressionField = new TRANSFORMFIELD();
					xformExpressionField.setDATATYPE("decimal");
					xformExpressionField.setDEFAULTVALUE("");
					xformExpressionField.setDESCRIPTION("");
					xformExpressionField.setEXPRESSION(String.format("%s*GBL_RATE_V", col.getColumnName()));
					xformExpressionField.setEXPRESSIONTYPE("GENERAL");
					xformExpressionField.setNAME(col.getColumnName().replace("DOC_", "GBL_"));
					xformExpressionField.setPICTURETEXT("");
					xformExpressionField.setPORTTYPE("OUTPUT");
					xformExpressionField.setPRECISION("22");
					xformExpressionField.setSCALE("7");

					return xformExpressionField;

				}).collect(Collectors.toList());

	}

	private TRANSFORMFIELD mapToUpdateStrategyField(TRANSFORMFIELD field) {
		Cloner cloner = Cloner.shared();
		TRANSFORMFIELD toField = cloner.deepClone(field);

		if (toField.getDATATYPE().equals("decimal") && toField.getNAME().endsWith("_WID")) {
			toField.setDEFAULTVALUE("$$UNSPEC_NUM");
		} else if (toField.getDATATYPE().equals("string") && toField.getPRECISION().equals("1")) {
			toField.setDEFAULTVALUE("$$UNSPEC_FLG");
		}

		toField.setPORTTYPE("INPUT/OUTPUT");

		return toField;

	}

}
