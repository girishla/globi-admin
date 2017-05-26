package com.globi.infa.generator.ptp;

import static com.globi.infa.generator.builder.RawStaticFactory.getEtlProcWidMappingVariable;
import static com.globi.infa.generator.builder.RawStaticFactory.getFolderFor;
import static com.globi.infa.generator.builder.RawStaticFactory.getInitialExtractDateMappingVariable;
import static com.globi.infa.generator.builder.RawStaticFactory.getMappingFrom;
import static com.globi.infa.generator.builder.RawStaticFactory.getRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.globi.infa.DataTypeMapper;
import com.globi.infa.datasource.core.TableColumnMetadataVisitor;
import com.globi.infa.datasource.core.TableColumnRepository;
import com.globi.infa.generator.InfaPowermartObject;
import com.globi.infa.generator.InfaRepoObjectBuilder;
import com.globi.infa.generator.builder.ExpressionXformBuilder;
import com.globi.infa.generator.builder.FilterXformBuilder;
import com.globi.infa.generator.builder.LookupXformBuilder;
import com.globi.infa.generator.builder.SequenceXformBuilder;
import com.globi.infa.generator.builder.SourceDefinitionBuilder;
import com.globi.infa.generator.builder.SourceQualifierBuilder;
import com.globi.infa.generator.builder.TargetDefinitionBuilder;
import com.globi.infa.generator.builder.WorkflowDefinitionBuilder;
import com.globi.infa.sourcedefinition.InfaSourceDefinition;
import com.globi.infa.workflow.InfaGenerationStrategy;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.metadata.sourcesystem.SourceSystem;
import com.globi.metadata.sourcesystem.SourceSystemRepository;

import lombok.Setter;

@Component
public class PTPInfaGenerationStrategy implements InfaGenerationStrategy {

	@Autowired
	Jaxb2Marshaller marshaller;

	@Autowired
	protected SourceSystemRepository sourceSystemRepo;
	

	@Setter
	private PTPWorkflow wfDefinition;
	@Setter
	DataTypeMapper dataTypeMapper;
	@Setter
	private TableColumnRepository colRepository;
	@Setter
	private TableColumnMetadataVisitor columnQueryVisitor;
	
	

	PTPInfaGenerationStrategy(Jaxb2Marshaller marshaller) {
		this.marshaller = marshaller;

	}

	private InfaPowermartObject generateWorkflow() throws IOException, SAXException, JAXBException {

		InfaSourceDefinition sourceTableDef;

		Map<String, String> emptyValuesMap = new HashMap<>();
		Map<String, String> lookupXformValuesMap = new HashMap<>();

		final String sourceQualifierFilterClauseColumn = "LAST_UPD";

		Optional<SourceSystem> source = sourceSystemRepo.findByName(wfDefinition.getSourceName());

		if (!source.isPresent())
			throw new IllegalArgumentException("Source System not recognised");

		sourceTableDef = InfaSourceDefinition.builder()//
				.sourceTableName(wfDefinition.getSourceTableName())//
				.ownerName(source.get().getOwnerName())//
				.databaseName(source.get().getName())//
				.databaseType(source.get().getDbType())//
				.build();

		// TODO Filter this list by the user selected list of columns
		sourceTableDef.getColumns().addAll(colRepository.accept(columnQueryVisitor,wfDefinition.getSourceTableName()));
		sourceTableDef.getColumns().forEach(column -> {
			if (column.getColumnName().equals("ROW_ID")) {
				column.setIntegrationIdFlag(true);
			}
		});

		lookupXformValuesMap.put("targetTableName",
				sourceTableDef.getDatabaseName() + "_" + sourceTableDef.getSourceTableName());

		InfaPowermartObject pmObj = InfaRepoObjectBuilder//
				.newBuilder()//
				.powermartObject().repository(getRepository())//
				.marshaller(marshaller)//
				.folder(getFolderFor("LAW_PTP_" + sourceTableDef.getDatabaseName(), "Pull to puddle folder"))//
				.simpleTableSyncClass("simpleTableSyncClass")//
				.sourceDefn(SourceDefinitionBuilder.newBuilder()//
						.sourceDefnFromPrototype("SourceFromPrototype")//
						.sourceDefn(sourceTableDef)//
						.addFields(sourceTableDef.getColumns())//
						.name(sourceTableDef.getSourceTableName())//
						.build())
				.noMoreSources()//
				.targetDefn(TargetDefinitionBuilder.newBuilder()//
						.marshaller(marshaller)//
						.loadTargetFromSeed("Seed_PTPTargetTableSystemCols")//
						.addFields(sourceTableDef.getColumns())//
						.name(sourceTableDef.getDatabaseName() + "_" + sourceTableDef.getSourceTableName())//
						.build())//
				.noMoreTargets()//
				.mappingDefn(getMappingFrom(sourceTableDef))//
				.transformation(SourceQualifierBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setValue("sourceFilter",
								sourceTableDef.getSourceTableName() + "." + sourceQualifierFilterClauseColumn
										+ " >= TO_DATE('$$INITIAL_EXTRACT_DATE','dd/MM/yyyy HH24:mi:ss')")
						.noMoreValues().loadSourceQualifierFromSeed("Seed_SourceQualifier")//
						.addFields(dataTypeMapper, sourceTableDef.getColumns())//
						.name("SQ_ExtractData")//
						.build())//
				.transformation(ExpressionXformBuilder.newBuilder()//
						.expressionFromPrototype("ExpFromPrototype")//
						.expression("EXP_Resolve")//
						.addEffectiveFromDateField()//
						.addEtlProcWidField()//
						.addIntegrationIdField(sourceTableDef.getColumns())//
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
						.loadExpressionXformFromSeed("Seed_LKPRecordInstanceViaHash")//
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
				.mappingvariable(getInitialExtractDateMappingVariable()).noMoreMappingVariables()//
				.setdefaultConfigFromSeed("Seed_DefaultSessionConfig")//
				.workflow(WorkflowDefinitionBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setValue("phasePrefix", "PTP")//
						.setValue("primaryName", sourceTableDef.getSourceTableName())//
						.setValue("sourceShortCode", sourceTableDef.getDatabaseName())//
						.setValue("TargetShortCode", "LAW")//
						.noMoreValues()//
						.loadWorkflowFromSeed("Seed_SimpleWorkflow")//
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
		} catch (IOException | SAXException | JAXBException e) {
			e.printStackTrace();
		}

		return pmObj;

	}
}