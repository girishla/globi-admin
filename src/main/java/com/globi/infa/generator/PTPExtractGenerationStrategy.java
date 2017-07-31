package com.globi.infa.generator;

import static com.globi.infa.generator.builder.InfaObjectMother.getDataSourceNumIdMappingVariable;
import static com.globi.infa.generator.builder.InfaObjectMother.getEtlProcWidMappingVariable;
import static com.globi.infa.generator.builder.InfaObjectMother.getFolderFor;
import static com.globi.infa.generator.builder.InfaObjectMother.getInitialExtractDateMappingVariable;
import static com.globi.infa.generator.builder.InfaObjectMother.getRepository;
import static com.globi.infa.generator.builder.InfaObjectMother.getTablenameMappingVariable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.datasource.core.ObjectNameNormaliser;
import com.globi.infa.datasource.core.SourceMetadataFactoryMapper;
import com.globi.infa.generator.builder.ExpressionXformBuilder;
import com.globi.infa.generator.builder.FilterXformBuilder;
import com.globi.infa.generator.builder.InfaMappingObject;
import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.generator.builder.LookupXformBuilder;
import com.globi.infa.generator.builder.MappingBuilder;
import com.globi.infa.generator.builder.MappletBuilder;
import com.globi.infa.generator.builder.PowermartObjectBuilder;
import com.globi.infa.generator.builder.SequenceXformBuilder;
import com.globi.infa.generator.builder.SourceDefinitionBuilder;
import com.globi.infa.generator.builder.SourceQualifierBuilder;
import com.globi.infa.generator.builder.TargetDefinitionBuilder;
import com.globi.infa.generator.builder.WorkflowDefinitionBuilder;
import com.globi.infa.metadata.core.SourceTableAbbreviationMap;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.metadata.src.InfaSourceDefinition;
import com.globi.infa.metadata.src.InfaSourceDefinitionRepository;
import com.globi.infa.notification.messages.WorkflowMessageNotifier;
import com.globi.infa.workflow.GeneratedWorkflow;
import com.globi.infa.workflow.InfaPTPWorkflowRepository;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;
import com.globi.metadata.sourcesystem.SourceSystem;
import com.globi.metadata.sourcesystem.SourceSystemRepository;


@Service
@Scope("prototype")

public class PTPExtractGenerationStrategy extends AbstractGenerationStrategy implements InfaGenerationStrategy {

	private PTPWorkflow wfDefinition;
	private SourceTableAbbreviationMap sourceTableAbbreviation;

	PTPExtractGenerationStrategy(Jaxb2Marshaller marshaller, SourceSystemRepository sourceSystemRepo,
			SourceMetadataFactoryMapper metadataFactoryMapper, InfaSourceDefinitionRepository sourceDefnRepo,
			SourceTableAbbreviationMap sourceTableAbbreviation, WorkflowMessageNotifier socketNotifier,
			InfaPTPWorkflowRepository wfRepo) {

		super(marshaller, sourceSystemRepo, metadataFactoryMapper, socketNotifier);
		this.sourceTableAbbreviation = sourceTableAbbreviation;

	}

	private List<InfaSourceColumnDefinition> getFilteredSourceDefnColumns(
			List<InfaSourceColumnDefinition> allTableColumns, List<PTPWorkflowSourceColumn> inputSelectedColumns) {

		Map<String, InfaSourceColumnDefinition> allColsMap = allTableColumns.stream()
				.collect(Collectors.toMap(InfaSourceColumnDefinition::getColumnName, Function.identity()));

		inputSelectedColumns.stream().forEach(inputColumn -> {
			if (allColsMap.containsKey(inputColumn.getSourceColumnName())) {
				allColsMap.get(inputColumn.getSourceColumnName())
						.setIntegrationIdFlag(inputColumn.isIntegrationIdColumn());
				allColsMap.get(inputColumn.getSourceColumnName()).setColumnSequence(inputColumn.getColumnSequence());
				allColsMap.get(inputColumn.getSourceColumnName()).setBuidFlag(inputColumn.isBuidColumn());
				allColsMap.get(inputColumn.getSourceColumnName()).setCcFlag(inputColumn.isChangeCaptureColumn());
				allColsMap.get(inputColumn.getSourceColumnName()).setPguidFlag(inputColumn.isPguidColumn());
				allColsMap.get(inputColumn.getSourceColumnName()).setSelected(true);
			}
		});

		List<InfaSourceColumnDefinition> matchedColumns = allColsMap.values()//
				.stream()//
				.filter(column -> column.getSelected()).collect(Collectors.toList());

		return matchedColumns;

	}

	private Optional<SourceSystem> setupSourceSystemDefn() {

		Optional<SourceSystem> source;

		if (wfDefinition == null)
			throw new IllegalArgumentException("Workflow Definition Must be set before invoking generate");

		source = sourceSystemRepo.findByName(wfDefinition.getSourceName());

		if (!source.isPresent())
			throw new IllegalArgumentException("Source System not recognised");

		return source;

	}

	private String getSourceFilterString(String sourceFilter, List<PTPWorkflowSourceColumn> inputSelectedColumns,
			String tableName) {

		// Find and set the sourceQualifier filter column
		Optional<PTPWorkflowSourceColumn> sourceQualifierFilterClauseColumn = inputSelectedColumns.stream()//
				.filter(column -> column.isChangeCaptureColumn())//
				.findAny();

		String ccFilter = "";
		String combinedFilter = "";
		sourceFilter = ObjectUtils.defaultIfNull(sourceFilter, "");

		if (sourceQualifierFilterClauseColumn.isPresent()) {
			ccFilter = tableName + "." + sourceQualifierFilterClauseColumn.get().getSourceColumnName()
					+ " >= TO_DATE('$$INITIAL_EXTRACT_DATE','dd/MM/yyyy HH24:mi:ss')";
		}

		if (!ccFilter.isEmpty() && !sourceFilter.isEmpty()) {
			combinedFilter = ccFilter + " AND " + sourceFilter;
		} else {
			combinedFilter = ObjectUtils.firstNonNull(ccFilter, combinedFilter);
		}

		return combinedFilter == null ? "" : combinedFilter;
	}


	private InfaMappingObject getPrimaryMapping() throws IOException, SAXException, JAXBException {

		InfaSourceDefinition sourceTableDef;

		Map<String, String> commonValuesMap = new HashMap<>();

		Optional<SourceSystem> source = setupSourceSystemDefn();

		String tblName = wfDefinition.getSourceTableName();
		String dbName = wfDefinition.getSourceName();
		String sourceFilter = wfDefinition.getSourceFilter();
		String tableOwner = source.get().getOwnerName();

		List<InfaSourceColumnDefinition> allTableColumns = colRepository.accept(columnQueryVisitor, tblName);
		List<PTPWorkflowSourceColumn> inputSelectedColumns = wfDefinition.getColumns();

		List<InfaSourceColumnDefinition> matchedColumns = this
				.getFilteredSourceDefnColumns(colRepository.accept(columnQueryVisitor, tblName), inputSelectedColumns);

		
		//for Non-Siebel sources, each table can have a different owner so needs looking up
		if ((!(source.get().getName().equals("CUK")) && (!source.get().getName().equals("CGL")))) {

			List<DataSourceTableDTO> sourceTables = tableRepository.accept(tableQueryVisitor);
			Optional<DataSourceTableDTO> sourceTable = sourceTables.stream()//
					.filter(table -> table.getTableName().equals(tblName))
					.findFirst();

			if (sourceTable.isPresent()) {
				tableOwner = sourceTable.get().getTableOwner();
			}
		}
		

		sourceTableDef = InfaSourceDefinition.builder()//
				.sourceTableName(tblName)//
				.ownerName(tableOwner)//
				.databaseName(source.get().getName())//
				.databaseType(source.get().getDbType())//
				.sourceTableUniqueName(source.get().getName() + "_" + tblName)//
				.build();

		String combinedFilter = getSourceFilterString(sourceFilter, inputSelectedColumns, tblName);

		sourceTableDef.getColumns().addAll(matchedColumns);

		commonValuesMap.put("targetTableName", dbName + "_" + tblName);
		commonValuesMap.put("sourceName", dbName);

		List<InfaSourceColumnDefinition> columnsList = sourceTableDef.getColumns()//
				.stream()//
				.filter(column -> column.getIntegrationIdFlag())//
				.collect(Collectors.toList());

		String targetTableName = wfDefinition.getTargetTableName();

		String targetTableDefnName = targetTableName.isEmpty() ? dbName + "_" + tblName + "_P" : targetTableName + "_P";

		InfaMappingObject mappingObjPrimary = MappingBuilder//
				.newBuilder()//
				.simpleTableSyncClass("simpleTableSyncClass")//
				.sourceDefn(SourceDefinitionBuilder.newBuilder()//
						.sourceDefnFromPrototype("SourceFromPrototype")//
						.sourceDefn(sourceTableDef)//
						.addFields(allTableColumns)//
						.name(tblName)//
						.build())
				.noMoreSources()//
				.targetDefn(TargetDefinitionBuilder.newBuilder()//
						.marshaller(marshaller)//
						.loadTargetFromSeed("Seed_PTPPrimaryExtractTargetTable")//
						.mapper(this.sourceToTargetDataTypeMapper).noMoreFields()//
						.name(targetTableDefnName)//
						.build())//
				.noMoreTargets()//
				.noMoreMapplets()//
				.startMappingDefn("PTP_" + dbName + "_" + tblName + "_Primary")//
				.transformation(SourceQualifierBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setValue("sourceFilter", combinedFilter).noMoreValues()
						.loadSourceQualifierFromSeed("Seed_SourceQualifier")//
						.addFields(dataTypeMapper, columnsList)//
						.name("SQ_PrimaryData")//
						.build())//
				.transformation(ExpressionXformBuilder.newBuilder()//
						.expressionFromPrototype("ExpFromPrototype")//
						.expression("EXP_Resolve")//
						.mapper(dataTypeMapper).addIntegrationIdField(columnsList)//
						.addDatasourceNumIdField()//
						.noMoreFields()//
						.nameAlreadySet()//
						.build())//
				.transformationCopyConnectAllFields("SQ_PrimaryData", "EXP_Resolve")//
				.transformation(LookupXformBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noInterpolationValues()//
						.loadLookupXformFromSeed("Seed_LKPPTPPrimaryRecordKeys")//
						.nameAlreadySet()//
						.build())
				.noMoreTransformations()//
				.autoConnectByName(tblName, "SQ_PrimaryData")//
				.autoConnectByName("LKP_RecordKeys", targetTableDefnName)//
				.autoConnectByName("EXP_Resolve", targetTableDefnName)//
				.connector("EXP_Resolve", "SYS_INTEGRATION_ID", "LKP_RecordKeys", "SYS_INTEGRATION_ID_IN")//
				.noMoreConnectors()//
				.noMoreTargetLoadOrders()//
				.mappingvariable(getEtlProcWidMappingVariable())//
				.mappingvariable(getInitialExtractDateMappingVariable())//
				.mappingvariable(getDataSourceNumIdMappingVariable(Integer.toString(source.get().getSourceNum())))//
				.mappingvariable(getTablenameMappingVariable(
						targetTableName.isEmpty() ? dbName + "_" + tblName : targetTableName))//
				.noMoreMappingVariables()//
				.build();

		return mappingObjPrimary;

	}

	private InfaMappingObject getExtractMapping() throws IOException, SAXException, JAXBException {

		InfaSourceDefinition sourceTableDef;

		Map<String, String> emptyValuesMap = new HashMap<>();
		Map<String, String> commonValuesMap = new HashMap<>();

		Optional<SourceSystem> source = this.setupSourceSystemDefn();

		String tblName = wfDefinition.getSourceTableName();
		String dbName = wfDefinition.getSourceName();
		String sourceFilter = wfDefinition.getSourceFilter();
		String tableOwner = source.get().getOwnerName();

		List<InfaSourceColumnDefinition> allTableColumns = colRepository.accept(columnQueryVisitor, tblName);

		List<PTPWorkflowSourceColumn> inputSelectedColumns = wfDefinition.getColumns();

		List<InfaSourceColumnDefinition> matchedColumns = this
				.getFilteredSourceDefnColumns(colRepository.accept(columnQueryVisitor, tblName), inputSelectedColumns);

		
		
		
		//for SQL server each table can have a different owner so needs looking up
		//for Non-Siebel sources, each table can have a different owner so needs looking up
		if ((!(source.get().getName().equals("CUK")) && (!source.get().getName().equals("CGL")))) {

			List<DataSourceTableDTO> sourceTables = tableRepository.accept(tableQueryVisitor);
			Optional<DataSourceTableDTO> sourceTable = sourceTables.stream()//
					.filter(table -> table.getTableName().equals(tblName))
					.findFirst();
			
			if (sourceTable.isPresent()) {
				tableOwner = sourceTable.get().getTableOwner();

			}

		}
		
		
		sourceTableDef = InfaSourceDefinition.builder()//
				.sourceTableName(tblName)//
				.ownerName(tableOwner)//
				.databaseName(source.get().getName())//
				.databaseType(source.get().getDbType())//
				.build();

		String combinedFilter = getSourceFilterString(sourceFilter, inputSelectedColumns, tblName);

		sourceTableDef.getColumns().addAll(matchedColumns);

		List<InfaSourceColumnDefinition> columnsList = sourceTableDef.getColumns();

		// normalise columns removing any special chars spaces etc
		columnsList = columnsList.stream().map(column -> {
			column.setColumnName(ObjectNameNormaliser.normalise(column.getColumnName()));
			return column;
		}).collect(Collectors.toList());

		String targetTableName = wfDefinition.getTargetTableName();
		String targetTableDefnName = targetTableName.isEmpty() ? dbName + "_" + tblName : targetTableName;

		commonValuesMap.put("targetTableName", targetTableDefnName);
		commonValuesMap.put("sourceName", dbName);

		InfaMappingObject mappingObjExtract = MappingBuilder//
				.newBuilder()//
				.simpleTableSyncClass("simpleTableSyncClass")//
				.sourceDefn(SourceDefinitionBuilder.newBuilder()//
						.sourceDefnFromPrototype("SourceFromPrototype")//
						.sourceDefn(sourceTableDef)//
						.addFields(allTableColumns)//
						.name(tblName)//
						.build())//
				.noMoreSources()//
				.targetDefn(TargetDefinitionBuilder.newBuilder()//
						.marshaller(marshaller)//
						.loadTargetFromSeed("Seed_PTPTargetTableSystemCols")//
						.mapper(this.sourceToTargetDataTypeMapper).addFields(columnsList)//
						.noMoreFields()//
						.name(targetTableDefnName)//
						.build())//
				.noMoreTargets()//
				.mappletDefn(MappletBuilder.newBuilder()//
						.marshaller(marshaller)//
						.loadMappletFromSeed("Seed_MPLDomainLookup")//
						.nameAlreadySet()//
						.build())//
				.noMoreMapplets()//
				.startMappingDefn("PTP_" + dbName + "_" + tblName + "_Extract")//
				.transformation(SourceQualifierBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setValue("sourceFilter", combinedFilter)//
						.noMoreValues()//
						.loadSourceQualifierFromSeed("Seed_SourceQualifier")//
						.addFields(dataTypeMapper, columnsList)//
						.name("SQ_ExtractData")//
						.build())//
				.transformation(ExpressionXformBuilder.newBuilder()//
						.expressionFromPrototype("ExpFromPrototype")//
						.expression("EXP_Resolve")//
						.mapper(dataTypeMapper).addEffectiveFromDateField()//
						.addEtlProcWidField()//
						.addDatasourceNumIdField()//
						.addIntegrationIdField(columnsList)//
						.addBUIDField(columnsList)//
						.addPGUIDField(dbName, tblName, sourceTableAbbreviation, columnsList)//
						.addMD5HashField(columnsList)//
						.addRowWidField()//
						.noMoreFields()//
						.nameAlreadySet()//
						.build())//
				.transformationCopyConnectAllFields("SQ_ExtractData", "EXP_Resolve")//
				.transformation(SequenceXformBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setInterpolationValues(emptyValuesMap)//
						.loadExpressionXformFromSeed("Seed_WidSequence")//
						.nameAlreadySet()//
						.build())
				.transformation(LookupXformBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setInterpolationValues(commonValuesMap)//
						.loadLookupXformFromSeed("Seed_LKPRecordInstanceViaHash")//
						.nameAlreadySet()//
						.build())
				.transformation(LookupXformBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setInterpolationValues(commonValuesMap)//
						.loadLookupXformFromSeed("Seed_LKPBU_" + dbName)//
						.nameAlreadySet()//
						.build())
				.transformation(ExpressionXformBuilder.newBuilder()//
						.ExpressionFromSeed("Prepare BU Domain Lookup")//
						.marshaller(marshaller)//
						.setInterpolationValues(commonValuesMap)//
						.loadExpressionXformFromSeed("Seed_EXPPrepDomLookup")//
						.mapper(dataTypeMapper).noMoreFields()//
						.nameAlreadySet()//
						.build())
				.transformation(FilterXformBuilder.newBuilder()//
						.filterFromPrototype("FilterFromPrototype")//
						.filter("FIL_ChangesOnly")//
						.addPGUIDField()//
						.addHashLookupField().noMoreFields()//
						.addCondition("ISNULL(SYS_HASH_RECORD_LKP)")//
						.noMoreConditions()//
						.nameAlreadySet()//
						.build())
				.transformationCopyConnectAllFields("EXP_Resolve", "FIL_ChangesOnly")//
				.noMoreTransformations()//
				.autoConnectByName(tblName, "SQ_ExtractData")//
				.autoConnectByName("FIL_ChangesOnly", targetTableDefnName)//
				.connector("SEQ_WID", "NEXTVAL", "EXP_Resolve", "SYS_ROW_WID")
				.connector("EXP_Resolve", "SYS_HASH_RECORD", "LKP_RecordInstance", "SYS_HASH_RECORD")
				.connector("LKP_RecordInstance", "SYS_HASH_RECORD", "FIL_ChangesOnly", "SYS_HASH_RECORD")//
				.connector("LKP_RecordInstance", "SYS_HASH_RECORD_LKP", "FIL_ChangesOnly", "SYS_HASH_RECORD_LKP")//
				.connector("EXP_Resolve", "BU", "EXP_PrepBUDomLookup", "DL_01_SRC_VAL")//
				.connector("EXP_PrepBUDomLookup", "DL_01_DEFAULT", "MPL_DomainLookup", "IN_01_DEFAULT")//
				.connector("EXP_PrepBUDomLookup", "DL_01_DOMAIN_MAP", "MPL_DomainLookup", "IN_01_DOMAIN_MAP")//
				.connector("EXP_PrepBUDomLookup", "DL_01_SRC_VAL", "MPL_DomainLookup", "IN_01_SRC_VAL")//
				.connector("MPL_DomainLookup", "OUT_01_TGT_VAL", "FIL_ChangesOnly", "SYS_BU_PGUID")//
				.noMoreConnectors()//
				.noMoreTargetLoadOrders()//
				.mappingvariable(getEtlProcWidMappingVariable())//
				.mappingvariable(getInitialExtractDateMappingVariable())//
				.mappingvariable(getDataSourceNumIdMappingVariable(Integer.toString(source.get().getSourceNum())))//
				.noMoreMappingVariables()//
				.build();

		return mappingObjExtract;

	}

	private InfaPowermartObject generateWorkflow() throws IOException, SAXException, JAXBException {

		String tblName = wfDefinition.getSourceTableName();
		String dbName = wfDefinition.getSourceName();

		String targetTableName = wfDefinition.getTargetTableName();
		String targetTableDefnName = targetTableName.isEmpty() ? dbName + "_" + tblName : targetTableName;

		InfaPowermartObject pmObj = PowermartObjectBuilder//
				.newBuilder()//
				.powermartObject().repository(getRepository())//
				.folder(getFolderFor("LAW_PTP_" + dbName, "Pull to puddle folder"))//
				.marshaller(marshaller)//
				.mappingDefn(this.getExtractMapping())//
				.mappingDefn(this.getPrimaryMapping()).noMoreMappings()//
				.setdefaultConfigFromSeed("Seed_DefaultSessionConfig")//
				.workflow(WorkflowDefinitionBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setValue("phasePrefix", "PTP")//
						.setValue("sourceShortCode", dbName)//
						.setValue("TargetShortCode", "PDL")//
						.setValue("tableName", tblName)//
						.setValue("tgtTableName", targetTableDefnName).noMoreValues()//
						.loadWorkflowFromSeed("Seed_WFPTP")//
						.nameAlreadySet()//
						.build())//
				.build();

		pmObj.pmObjectName = "PTP_" + dbName + "_" + tblName;

		return pmObj;
	}

	@Override
	public InfaPowermartObject generate() {
		InfaPowermartObject pmObj = null;

		try {
			pmObj = this.generateWorkflow();
			this.notifyListeners(pmObj, wfDefinition);
		} catch (Exception e) {
			e.printStackTrace();

			throw new WorkflowGenerationException((GeneratedWorkflow) this.wfDefinition, e.getMessage());

		}

		return pmObj;

	}

	public void setWfDefinition(PTPWorkflow ptpWorkflow) {

		this.wfDefinition = ptpWorkflow;
		this.setContext(ptpWorkflow.getSourceName());

	}
}