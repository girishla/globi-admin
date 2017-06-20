package com.globi.infa.generator;

import static com.globi.infa.generator.builder.InfaObjectMother.getDataSourceNumIdMappingVariable;
import static com.globi.infa.generator.builder.InfaObjectMother.getEtlProcWidMappingVariable;
import static com.globi.infa.generator.builder.InfaObjectMother.getFolderFor;
import static com.globi.infa.generator.builder.InfaObjectMother.getInitialExtractDateMappingVariable;
import static com.globi.infa.generator.builder.InfaObjectMother.getMappingFrom;
import static com.globi.infa.generator.builder.InfaObjectMother.getRepository;

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
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.globi.infa.datasource.core.SourceMetadataFactoryMapper;
import com.globi.infa.generator.builder.ExpressionXformBuilder;
import com.globi.infa.generator.builder.FilterXformBuilder;
import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.generator.builder.LookupXformBuilder;
import com.globi.infa.generator.builder.MappletBuilder;
import com.globi.infa.generator.builder.PowermartObjectBuilder;
import com.globi.infa.generator.builder.SequenceXformBuilder;
import com.globi.infa.generator.builder.SourceDefinitionBuilder;
import com.globi.infa.generator.builder.SourceQualifierBuilder;
import com.globi.infa.generator.builder.TargetDefinitionBuilder;
import com.globi.infa.generator.builder.WorkflowDefinitionBuilder;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.metadata.src.InfaSourceDefinition;
import com.globi.infa.workflow.GeneratedWorkflow;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;
import com.globi.metadata.sourcesystem.SourceSystem;
import com.globi.metadata.sourcesystem.SourceSystemRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Scope("prototype")
@Slf4j
public class PTPExtractGenerationStrategy extends AbstractGenerationStrategy implements InfaGenerationStrategy {

	private PTPWorkflow wfDefinition;

	PTPExtractGenerationStrategy(Jaxb2Marshaller marshaller,SourceSystemRepository sourceSystemRepo, SourceMetadataFactoryMapper metadataFactoryMapper) {

		super(marshaller,sourceSystemRepo,metadataFactoryMapper);

	}

	private List<InfaSourceColumnDefinition> getFilteredSourceDefnColumns(
			List<InfaSourceColumnDefinition> allTableColumns, List<PTPWorkflowSourceColumn> inputSelectedColumns) {

		Map<String, InfaSourceColumnDefinition> allColsMap = allTableColumns.stream()
				.collect(Collectors.toMap(InfaSourceColumnDefinition::getColumnName, Function.identity()));

		inputSelectedColumns.stream().forEach(inputColumn -> {
			if (allColsMap.containsKey(inputColumn.getSourceColumnName())) {
				allColsMap.get(inputColumn.getSourceColumnName())
						.setIntegrationIdFlag(inputColumn.isIntegrationIdColumn());
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
		sourceFilter=ObjectUtils.defaultIfNull(sourceFilter,"");

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

	private InfaPowermartObject generateWorkflow() throws IOException, SAXException, JAXBException {

		
		
		InfaSourceDefinition sourceTableDef;

		Map<String, String> emptyValuesMap = new HashMap<>();
		Map<String, String> commonValuesMap = new HashMap<>();

		Optional<SourceSystem> source=this.setupSourceSystemDefn();

		String tblName = wfDefinition.getSourceTableName();
		String dbName = wfDefinition.getSourceName();
		String sourceFilter = wfDefinition.getSourceFilter();

		List<InfaSourceColumnDefinition> allTableColumns = colRepository.accept(columnQueryVisitor, tblName);

		List<PTPWorkflowSourceColumn> inputSelectedColumns =wfDefinition.getColumns();

		List<InfaSourceColumnDefinition> matchedColumns = this
				.getFilteredSourceDefnColumns(colRepository.accept(columnQueryVisitor, tblName), inputSelectedColumns);

		sourceTableDef = InfaSourceDefinition.builder()//
				.sourceTableName(tblName)//
				.ownerName(source.get().getOwnerName())//
				.databaseName(source.get().getName())//
				.databaseType(source.get().getDbType())//
				.build();

		String combinedFilter = getSourceFilterString(sourceFilter, inputSelectedColumns, tblName);

		sourceTableDef.getColumns().addAll(matchedColumns);

		commonValuesMap.put("targetTableName", dbName + "_" + tblName);
		commonValuesMap.put("sourceName", dbName);

		List<InfaSourceColumnDefinition> columnsList = sourceTableDef.getColumns();

		InfaPowermartObject pmObj = PowermartObjectBuilder//
				.newBuilder()//
				.powermartObject().repository(getRepository())//
				.folder(getFolderFor("LAW_PTP_" + dbName, "Pull to puddle folder"))//
				.marshaller(marshaller)//
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
						.loadTargetFromSeed("Seed_PTPTargetTableSystemCols")//
						.addFields(columnsList)//
						.noMoreFields().name(dbName + "_" + tblName)//
						.build())//
				.noMoreTargets()//
				.mappletDefn(MappletBuilder.newBuilder()//
						.marshaller(marshaller)//
						.loadMappletFromSeed("Seed_MPLDomainLookup")//
						.nameAlreadySet()//
						.build())//
				.noMoreMapplets().mappingDefn(getMappingFrom("PTP_" + dbName + "_" + tblName + "_Extract"))//
				.transformation(SourceQualifierBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setValue("sourceFilter", combinedFilter)//
						.noMoreValues().loadSourceQualifierFromSeed("Seed_SourceQualifier")//
						.addFields(dataTypeMapper, columnsList)//
						.name("SQ_ExtractData")//
						.build())//
				.transformation(ExpressionXformBuilder.newBuilder()//
						.expressionFromPrototype("ExpFromPrototype")//
						.expression("EXP_Resolve")//
						.addEffectiveFromDateField()//
						.addEtlProcWidField()//
						.addDatasourceNumIdField()//
						.addIntegrationIdField(columnsList)//
						.addBUIDField(columnsList).addPGUIDField(dbName, columnsList).addMD5HashField(columnsList)//
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
						.noMoreFields()//
						.nameAlreadySet()//
						.build())
				.transformation(FilterXformBuilder.newBuilder()//
						.filterFromPrototype("FilterFromPrototype")//
						.filter("FIL_ChangesOnly")//
						.addPGUIDField()//
						.addHashLookupField()
						.noMoreFields()//
						.addCondition("ISNULL(SYS_HASH_RECORD_LKP)")//
						.noMoreConditions()//
						.nameAlreadySet()//
						.build())
				.transformationCopyConnectAllFields("EXP_Resolve", "FIL_ChangesOnly")//
				.noMoreTransformations()//
				.autoConnectByName(tblName, "SQ_ExtractData")//
				.autoConnectByName("FIL_ChangesOnly", dbName + "_" + tblName)//
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
				.setdefaultConfigFromSeed("Seed_DefaultSessionConfig")//
				.workflow(WorkflowDefinitionBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setValue("phasePrefix", "PTP")//
						.setValue("primaryName", dbName + "_" + tblName)//
						.setValue("suffix", "Extract")//
						.setValue("sourceShortCode", dbName)//
						.setValue("TargetShortCode", "PDL")//
						.setValue("tableName", tblName)//
						.noMoreValues()//
						.loadWorkflowFromSeed("Seed_PTPExtractWorkflow")//
						.nameAlreadySet()//
						.build())//
				.build();

		pmObj.pmObjectName="PTP_" + dbName + "_" + tblName +"_Extract";
		
		return pmObj;
	}

	@Override
	public InfaPowermartObject generate() {
		InfaPowermartObject pmObj = null;

		try {
			pmObj = this.generateWorkflow();
			this.notifyListeners(pmObj, wfDefinition);
		} catch (IOException | SAXException | JAXBException e) {
			e.printStackTrace();
			throw new WorkflowGenerationException((GeneratedWorkflow) this.wfDefinition, e.getMessage());

		}

		return pmObj;

	}

	public void setWfDefinition(PTPWorkflow ptpWorkflow) {

		this.wfDefinition=ptpWorkflow;
		this.setContext(ptpWorkflow.getSourceName());
		
	}
}