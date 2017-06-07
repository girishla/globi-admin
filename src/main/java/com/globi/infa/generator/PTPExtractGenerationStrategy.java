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

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
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
import com.globi.infa.metadata.source.InfaSourceColumnDefinition;
import com.globi.infa.metadata.source.InfaSourceDefinition;
import com.globi.infa.workflow.GeneratedWorkflow;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;
import com.globi.metadata.sourcesystem.SourceSystem;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PTPExtractGenerationStrategy extends AbstractGenerationStrategy implements InfaGenerationStrategy {

	
	Optional<SourceSystem> source;
	
	
	PTPExtractGenerationStrategy(Jaxb2Marshaller marshaller, SourceMetadataFactoryMapper metadataFactoryMapper) {
		
		this.marshaller = marshaller;
		this.metadataFactoryMapper = metadataFactoryMapper;

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
				.filter(column -> column.getSelected())
				.collect(Collectors.toList());

		return matchedColumns;

	}
	
	
	private void setupSourceSystemDefn(){
		
		if (wfDefinition == null)
			throw new IllegalArgumentException("Workflow Definition Must be set before invoking generate");
		
		this.source = sourceSystemRepo.findByName(wfDefinition.getSourceName());

		if (!source.isPresent())
			throw new IllegalArgumentException("Source System not recognised");
		
	}

	private InfaPowermartObject generateWorkflow() throws IOException, SAXException, JAXBException {

		InfaSourceDefinition sourceTableDef;

		Map<String, String> emptyValuesMap = new HashMap<>();
		Map<String, String> commonValuesMap = new HashMap<>();
		
		this.setupSourceSystemDefn();

		List<InfaSourceColumnDefinition> allTableColumns = colRepository.accept(columnQueryVisitor,
				wfDefinition.getSourceTableName());
		
		List<PTPWorkflowSourceColumn> inputSelectedColumns = wfDefinition.getColumns();
		
		List<InfaSourceColumnDefinition> matchedColumns = this.getFilteredSourceDefnColumns(colRepository.accept(columnQueryVisitor,
				wfDefinition.getSourceTableName()), inputSelectedColumns);

		sourceTableDef = InfaSourceDefinition.builder()//
				.sourceTableName(wfDefinition.getSourceTableName())//
				.ownerName(source.get().getOwnerName())//
				.databaseName(source.get().getName())//
				.databaseType(source.get().getDbType())//
				.build();

		// Find and set the sourceQualifier filter column
		Optional<PTPWorkflowSourceColumn> sourceQualifierFilterClauseColumn = inputSelectedColumns.stream()//
				.filter(column -> column.isChangeCaptureColumn())//
				.findAny();

		String sourceFilter = "";

		if (sourceQualifierFilterClauseColumn.isPresent()) {
			sourceFilter = sourceTableDef.getSourceTableName() + "."
					+ sourceQualifierFilterClauseColumn.get().getSourceColumnName()
					+ " >= TO_DATE('$$INITIAL_EXTRACT_DATE','dd/MM/yyyy HH24:mi:ss')";

		}

		//Save all columns for reference and then add back matched columns for processing
		sourceTableDef.getColumns().addAll(allTableColumns);
		sourceDefnRepo.save(sourceTableDef);

		sourceTableDef.getColumns().clear();
		sourceTableDef.getColumns().addAll(matchedColumns);
		

		commonValuesMap.put("targetTableName",
				sourceTableDef.getDatabaseName() + "_" + sourceTableDef.getSourceTableName());
		commonValuesMap.put("sourceName", sourceTableDef.getDatabaseName());

		InfaPowermartObject pmObj = PowermartObjectBuilder//
				.newBuilder()//
				.powermartObject().repository(getRepository())//
				.folder(getFolderFor("LAW_PTP_" + sourceTableDef.getDatabaseName(), "Pull to puddle folder"))//
				.marshaller(marshaller)//
				.simpleTableSyncClass("simpleTableSyncClass")//
				.sourceDefn(SourceDefinitionBuilder.newBuilder()//
						.sourceDefnFromPrototype("SourceFromPrototype")//
						.sourceDefn(sourceTableDef)//
						.addFields(allTableColumns)//
						.name(sourceTableDef.getSourceTableName())//
						.build())
				.noMoreSources()//
				.targetDefn(TargetDefinitionBuilder.newBuilder()//
						.marshaller(marshaller)//
						.loadTargetFromSeed("Seed_PTPTargetTableSystemCols")//
						.addFields(sourceTableDef.getColumns())//
						.noMoreFields()
						.name(sourceTableDef.getDatabaseName() + "_" + sourceTableDef.getSourceTableName())//
						.build())//
				.noMoreTargets()//
				.mappletDefn(MappletBuilder.newBuilder()//
						.marshaller(marshaller)//
						.loadMappletFromSeed("Seed_MPLDomainLookup")//
						.nameAlreadySet()//
						.build())//
				.noMoreMapplets()
				.mappingDefn(getMappingFrom("PTP_" + sourceTableDef.getDatabaseName() + "_"
						+ sourceTableDef.getSourceTableName() + "_Extract"))//
				.transformation(SourceQualifierBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setValue("sourceFilter", sourceFilter).noMoreValues()
						.loadSourceQualifierFromSeed("Seed_SourceQualifier")//
						.addFields(dataTypeMapper, sourceTableDef.getColumns())//
						.name("SQ_ExtractData")//
						.build())//
				.transformation(ExpressionXformBuilder.newBuilder()//
						.expressionFromPrototype("ExpFromPrototype")//
						.expression("EXP_Resolve")//
						.addEffectiveFromDateField()//
						.addEtlProcWidField()//
						.addDatasourceNumIdField()//
						.addIntegrationIdField(sourceTableDef.getColumns())//
						.addBUIDField(sourceTableDef.getColumns())
						.addPGUIDField(sourceTableDef.getDatabaseName(), sourceTableDef.getColumns())
						.addMD5HashField(sourceTableDef.getColumns())//
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
						.loadLookupXformFromSeed("Seed_LKPBU_" + sourceTableDef.getDatabaseName())//
						.nameAlreadySet()//
						.build())
				.transformation(ExpressionXformBuilder.newBuilder()//
						.ExpressionFromSeed("Prepare BU Domain Lookup").marshaller(marshaller)//
						.setInterpolationValues(commonValuesMap)//
						.loadExpressionXformFromSeed("Seed_EXPPrepDomLookup").noMoreFields().nameAlreadySet().build())
				.transformation(FilterXformBuilder.newBuilder()//
						.filterFromPrototype("FilterFromPrototype")//
						.filter("FIL_ChangesOnly")//
						.addPGUIDField().noMoreFields()//
						.addCondition("ISNULL(HASH_RECORD)")//
						.noMoreConditions()//
						.nameAlreadySet()//
						.build())
				.transformationCopyConnectAllFields("EXP_Resolve", "FIL_ChangesOnly")//
				.noMoreTransformations()//
				.autoConnectByName(sourceTableDef.getSourceTableName(), "SQ_ExtractData")//
				.autoConnectByName("FIL_ChangesOnly",
						sourceTableDef.getDatabaseName() + "_" + sourceTableDef.getSourceTableName())//
				.connector("SEQ_WID", "NEXTVAL", "EXP_Resolve", "ROW_WID")
				.connector("EXP_Resolve", "HASH_RECORD", "LKP_RecordInstance", "HASH_RECORD_IN")
				.connector("LKP_RecordInstance", "HASH_RECORD", "FIL_ChangesOnly", "HASH_RECORD")//
				.connector("EXP_Resolve", "BU", "EXP_PrepBUDomLookup", "DL_01_SRC_VAL")//
				.connector("EXP_PrepBUDomLookup", "DL_01_DEFAULT", "MPL_DomainLookup", "IN_01_DEFAULT")//
				.connector("EXP_PrepBUDomLookup", "DL_01_DOMAIN_MAP", "MPL_DomainLookup", "IN_01_DOMAIN_MAP")//
				.connector("EXP_PrepBUDomLookup", "DL_01_SRC_VAL", "MPL_DomainLookup", "IN_01_SRC_VAL")//
				.connector("MPL_DomainLookup", "OUT_01_TGT_VAL", "FIL_ChangesOnly", "BU_PGUID")//
				.noMoreConnectors()//
				.noMoreTargetLoadOrders()//
				.mappingvariable(getEtlProcWidMappingVariable())//
				.mappingvariable(getInitialExtractDateMappingVariable())//
				.mappingvariable(getDataSourceNumIdMappingVariable()).noMoreMappingVariables()//
				.setdefaultConfigFromSeed("Seed_DefaultSessionConfig")//
				.workflow(WorkflowDefinitionBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setValue("phasePrefix", "PTP")//
						.setValue("primaryName",
								sourceTableDef.getDatabaseName() + "_" + sourceTableDef.getSourceTableName())//
						.setValue("suffix", "Extract")//
						.setValue("sourceShortCode", sourceTableDef.getDatabaseName())//
						.setValue("TargetShortCode", "PDL")//
						.setValue("tableName", sourceTableDef.getSourceTableName()).noMoreValues()//
						.loadWorkflowFromSeed("Seed_PTPExtractWorkflow")//
						.nameAlreadySet()//
						.build())//
				.build();

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
}