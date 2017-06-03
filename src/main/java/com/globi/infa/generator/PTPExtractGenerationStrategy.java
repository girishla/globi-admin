package com.globi.infa.generator;

import static com.globi.infa.generator.builder.InfaObjectStaticFactory.getDataSourceNumIdMappingVariable;
import static com.globi.infa.generator.builder.InfaObjectStaticFactory.getEtlProcWidMappingVariable;
import static com.globi.infa.generator.builder.InfaObjectStaticFactory.getFolderFor;
import static com.globi.infa.generator.builder.InfaObjectStaticFactory.getInitialExtractDateMappingVariable;
import static com.globi.infa.generator.builder.InfaObjectStaticFactory.getMappingFrom;
import static com.globi.infa.generator.builder.InfaObjectStaticFactory.getRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.globi.infa.datasource.core.InfaSourceColumnDefinition;
import com.globi.infa.datasource.core.InfaSourceDefinition;
import com.globi.infa.datasource.core.SourceMetadataFactoryMapper;
import com.globi.infa.generator.builder.ExpressionXformBuilder;
import com.globi.infa.generator.builder.FilterXformBuilder;
import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.generator.builder.LookupXformBuilder;
import com.globi.infa.generator.builder.PowermartObjectBuilder;
import com.globi.infa.generator.builder.SequenceXformBuilder;
import com.globi.infa.generator.builder.SourceDefinitionBuilder;
import com.globi.infa.generator.builder.SourceQualifierBuilder;
import com.globi.infa.generator.builder.TargetDefinitionBuilder;
import com.globi.infa.generator.builder.WorkflowDefinitionBuilder;
import com.globi.infa.workflow.GeneratedWorkflow;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;
import com.globi.metadata.sourcesystem.SourceSystem;

@Component
public class PTPExtractGenerationStrategy extends AbstractGenerationStrategy implements InfaGenerationStrategy {

	PTPExtractGenerationStrategy(Jaxb2Marshaller marshaller, SourceMetadataFactoryMapper metadataFactoryMapper) {
		this.marshaller = marshaller;
		this.metadataFactoryMapper = metadataFactoryMapper;

	}

	private InfaPowermartObject generateWorkflow() throws IOException, SAXException, JAXBException {

		InfaSourceDefinition sourceTableDef;

		Map<String, String> emptyValuesMap = new HashMap<>();
		Map<String, String> lookupXformValuesMap = new HashMap<>();

		Optional<SourceSystem> source = sourceSystemRepo.findByName(wfDefinition.getSourceName());

		if (!source.isPresent())
			throw new IllegalArgumentException("Source System not recognised");

		if (wfDefinition == null)
			throw new IllegalArgumentException("Workflow Definition Must be set before invoking generate");

		sourceTableDef = InfaSourceDefinition.builder()//
				.sourceTableName(wfDefinition.getSourceTableName())//
				.ownerName(source.get().getOwnerName())//
				.databaseName(source.get().getName())//
				.databaseType(source.get().getDbType())//
				.build();

		List<InfaSourceColumnDefinition> allTableColumns = colRepository.accept(columnQueryVisitor,
				wfDefinition.getSourceTableName());

		List<PTPWorkflowSourceColumn> inputSelectedColumns = wfDefinition.getColumns();

		// Filter for selected columns for which generation must take place.
		// Also tag the integration Id
		List<InfaSourceColumnDefinition> matchedColumns = allTableColumns.stream().filter(column -> {
			return inputSelectedColumns.stream()//
					.anyMatch(selectedCol -> {//
						return selectedCol.getSourceColumnName().equals(column.getColumnName());
					});
		}).map(column -> {

			if (inputSelectedColumns.stream().anyMatch(selectedCol -> {
				return selectedCol.getSourceColumnName().equals(column.getColumnName())
						&& selectedCol.isIntegrationIdColumn();
			})) {
				column.setIntegrationIdFlag(true);
			}
			
			if (inputSelectedColumns.stream().anyMatch(selectedCol -> {
				return selectedCol.getSourceColumnName().equals(column.getColumnName())
						&& selectedCol.isPguidColumn();
			})) {
				column.setPguidFlag(true);
			}
			

			return column;

		}).collect(Collectors.toList());

		// Find and set the sourceQualifier filter column
		Optional<PTPWorkflowSourceColumn> sourceQualifierFilterClauseColumn = inputSelectedColumns.stream()//
				.filter(column -> column.isChangeCaptureColumn())//
				.findAny();
		
		String sourceFilter="";
		
		if(sourceQualifierFilterClauseColumn.isPresent()){
			sourceFilter=sourceTableDef.getSourceTableName() + "." + sourceQualifierFilterClauseColumn.get().getSourceColumnName()
					+ " >= TO_DATE('$$INITIAL_EXTRACT_DATE','dd/MM/yyyy HH24:mi:ss')";
				
		}
		
		sourceTableDef.getColumns().addAll(matchedColumns);

		lookupXformValuesMap.put("targetTableName",
				sourceTableDef.getDatabaseName() + "_" + sourceTableDef.getSourceTableName());

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
				.mappingDefn(getMappingFrom("PTP_" + sourceTableDef.getDatabaseName() + "_" + sourceTableDef.getSourceTableName() + "_Extract"))//
				.transformation(SourceQualifierBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setValue("sourceFilter",sourceFilter)
						.noMoreValues().loadSourceQualifierFromSeed("Seed_SourceQualifier")//
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
						.setInterpolationValues(lookupXformValuesMap)//
						.loadLookupXformFromSeed("Seed_LKPRecordInstanceViaHash")//
						.nameAlreadySet()//
						.build())
				.transformation(FilterXformBuilder.newBuilder()//
						.filterFromPrototype("FilterFromPrototype")//
						.filter("FIL_ChangesOnly")//
						.noMoreFields()//
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
				.noMoreConnectors()//
				.noMoreTargetLoadOrders()//
				.mappingvariable(getEtlProcWidMappingVariable())//
				.mappingvariable(getInitialExtractDateMappingVariable())//
				.mappingvariable(getDataSourceNumIdMappingVariable())
				.noMoreMappingVariables()//
				.setdefaultConfigFromSeed("Seed_DefaultSessionConfig")//
				.workflow(WorkflowDefinitionBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setValue("phasePrefix", "PTP")//
						.setValue("primaryName", sourceTableDef.getDatabaseName() + "_"+ sourceTableDef.getSourceTableName())//
						.setValue("suffix", "Extract")//
						.setValue("sourceShortCode", sourceTableDef.getDatabaseName())//
						.setValue("TargetShortCode", "PDL")//
						.noMoreValues()//
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