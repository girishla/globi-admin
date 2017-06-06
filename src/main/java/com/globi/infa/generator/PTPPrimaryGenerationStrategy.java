package com.globi.infa.generator;

import static com.globi.infa.generator.builder.InfaObjectMother.*;


import java.io.IOException;
import java.util.List;
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
import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.generator.builder.LookupXformBuilder;
import com.globi.infa.generator.builder.PowermartObjectBuilder;
import com.globi.infa.generator.builder.SourceDefinitionBuilder;
import com.globi.infa.generator.builder.SourceQualifierBuilder;
import com.globi.infa.generator.builder.TargetDefinitionBuilder;
import com.globi.infa.generator.builder.WorkflowDefinitionBuilder;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;
import com.globi.metadata.sourcesystem.SourceSystem;

@Component
public class PTPPrimaryGenerationStrategy extends AbstractGenerationStrategy implements InfaGenerationStrategy {

	PTPPrimaryGenerationStrategy(Jaxb2Marshaller marshaller, SourceMetadataFactoryMapper metadataFactoryMapper) {
		this.marshaller = marshaller;
		this.metadataFactoryMapper = metadataFactoryMapper;

	}

	private InfaPowermartObject generateWorkflow() throws IOException, SAXException, JAXBException {

		InfaSourceDefinition sourceTableDef;

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

			return column;

		}).collect(Collectors.toList());

		// Find and set the sourceQualifier filter column
		Optional<PTPWorkflowSourceColumn> sourceQualifierFilterClauseColumn = inputSelectedColumns.stream()//
				.filter(column -> column.isChangeCaptureColumn())//
				.findAny();

		sourceTableDef.getColumns().addAll(matchedColumns);

		List<InfaSourceColumnDefinition> integrationIdCols = sourceTableDef.getColumns()//
				.stream()//
				.filter(column -> column.getIntegrationIdFlag())//
				.collect(Collectors.toList());

		String targetTableDefnName = sourceTableDef.getDatabaseName() + "_" + sourceTableDef.getSourceTableName()
				+ "_P";

		InfaPowermartObject pmObj = PowermartObjectBuilder//
				.newBuilder()//
				.powermartObject().repository(getRepository())//
				.folder(getFolderFor("LAW_PTP_" + sourceTableDef.getDatabaseName(), "Pull to puddle folder"))//
				.marshaller(marshaller)//
				.primaryExtractClass("primaryExtractClass")//
				.sourceDefn(SourceDefinitionBuilder.newBuilder()//
						.sourceDefnFromPrototype("SourceFromPrototype")//
						.sourceDefn(sourceTableDef)//
						.addFields(allTableColumns)//
						.name(sourceTableDef.getSourceTableName())//
						.build())
				.noMoreSources()//
				.targetDefn(TargetDefinitionBuilder.newBuilder()//
						.marshaller(marshaller)//
						.loadTargetFromSeed("Seed_PTPPrimaryExtractTargetTable")//
						.noMoreFields().name(targetTableDefnName)//
						.build())//
				.noMoreTargets()//
				.noMoreMapplets()
				.mappingDefn(getMappingFrom("PTP_" + sourceTableDef.getDatabaseName() + "_" + sourceTableDef.getSourceTableName() + "_Primary"))//
				.transformation(SourceQualifierBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setValue("sourceFilter",
								sourceTableDef.getSourceTableName() + "."
										+ sourceQualifierFilterClauseColumn.get().getSourceColumnName()
										+ " >= TO_DATE('$$INITIAL_EXTRACT_DATE','dd/MM/yyyy HH24:mi:ss')")
						.noMoreValues().loadSourceQualifierFromSeed("Seed_SourceQualifier")//
						.addFields(dataTypeMapper, integrationIdCols)//
						.name("SQ_PrimaryData")//
						.build())//
				.transformation(ExpressionXformBuilder.newBuilder()//
						.expressionFromPrototype("ExpFromPrototype")//
						.expression("EXP_Resolve")//
						.addIntegrationIdField(integrationIdCols)//
						.addDatasourceNumIdField().noMoreFields()//
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
				.autoConnectByName(sourceTableDef.getSourceTableName(), "SQ_PrimaryData")//
				.autoConnectByName("LKP_RecordKeys", targetTableDefnName)//
				.autoConnectByName("EXP_Resolve", targetTableDefnName)//
				.connector("EXP_Resolve", "INTEGRATION_ID", "LKP_RecordKeys", "INTEGRATION_ID_IN")//
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
						.setValue("suffix", "Primary")//
						.setValue("primaryName", sourceTableDef.getSourceTableName())//
						.setValue("sourceShortCode", sourceTableDef.getDatabaseName())//
						.setValue("TargetShortCode", "LAW")//
						.noMoreValues()//
						.loadWorkflowFromSeed("Seed_PTPPrimaryWorkflow")//
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
			this.notifyListeners(pmObj,wfDefinition);
		} catch (IOException | SAXException | JAXBException e) {
			e.printStackTrace();
		}

		return pmObj;

	}
}