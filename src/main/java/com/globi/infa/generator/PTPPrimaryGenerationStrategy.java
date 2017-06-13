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
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.globi.infa.datasource.core.SourceMetadataFactoryMapper;
import com.globi.infa.generator.builder.ExpressionXformBuilder;
import com.globi.infa.generator.builder.InfaPowermartObject;
import com.globi.infa.generator.builder.LookupXformBuilder;
import com.globi.infa.generator.builder.PowermartObjectBuilder;
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

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PTPPrimaryGenerationStrategy extends AbstractGenerationStrategy implements InfaGenerationStrategy {

	
	private PTPWorkflow wfDefinition;

	PTPPrimaryGenerationStrategy(Jaxb2Marshaller marshaller, SourceMetadataFactoryMapper metadataFactoryMapper) {

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

		Map<String, String> commonValuesMap = new HashMap<>();

		Optional<SourceSystem> source=setupSourceSystemDefn();

		String tblName = wfDefinition.getSourceTableName();
		String dbName = wfDefinition.getSourceName();
		String sourceFilter = wfDefinition.getSourceFilter();
		
		List<InfaSourceColumnDefinition> allTableColumns = colRepository.accept(columnQueryVisitor, tblName);

		List<PTPWorkflowSourceColumn> inputSelectedColumns = wfDefinition.getColumns();

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

		List<InfaSourceColumnDefinition> columnsList = sourceTableDef.getColumns()//
				.stream()//
				.filter(column -> column.getIntegrationIdFlag())//
				.collect(Collectors.toList());
		
		String targetTableDefnName = dbName + "_" + tblName + "_P";
	

		InfaPowermartObject pmObj = PowermartObjectBuilder//
				.newBuilder()//
				.powermartObject().repository(getRepository())//
				.folder(getFolderFor("LAW_PTP_" + dbName, "Pull to puddle folder"))//
				.marshaller(marshaller)//
				.primaryExtractClass("primaryExtractClass")//
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
						.noMoreFields()//
						.name(targetTableDefnName)//
						.build())//
				.noMoreTargets()//
				.noMoreMapplets()
				.mappingDefn(getMappingFrom("PTP_" + dbName + "_" + tblName + "_Primary"))//
				.transformation(SourceQualifierBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setValue("sourceFilter",combinedFilter)
						.noMoreValues().loadSourceQualifierFromSeed("Seed_SourceQualifier")//
						.addFields(dataTypeMapper, columnsList)//
						.name("SQ_PrimaryData")//
						.build())//
				.transformation(ExpressionXformBuilder.newBuilder()//
						.expressionFromPrototype("ExpFromPrototype")//
						.expression("EXP_Resolve")//
						.addIntegrationIdField(columnsList)//
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
				.autoConnectByName(tblName, "SQ_PrimaryData")//
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
						.setValue("primaryName", tblName)//
						.setValue("sourceShortCode", dbName)//
						.setValue("TargetShortCode", "LAW")//
						.noMoreValues()//
						.loadWorkflowFromSeed("Seed_PTPPrimaryWorkflow")//
						.nameAlreadySet()//
						.build())//
				.build();

		pmObj.pmObjectName="PTP_" + dbName + "_" + tblName +"_Primary";

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