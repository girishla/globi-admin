package com.globi.infa.generator.ptp;

import static com.globi.infa.generator.builder.InfaObjectMother.getDataSourceNumIdMappingVariable;
import static com.globi.infa.generator.builder.InfaObjectMother.getEtlProcWidMappingVariable;
import static com.globi.infa.generator.builder.InfaObjectMother.getInitialExtractDateMappingVariable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.xml.sax.SAXException;

import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.generator.AbstractMappingGenerator;
import com.globi.infa.generator.GeneratorContext;
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
import com.globi.infa.metadata.core.SourceTableAbbreviationMap;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.metadata.src.InfaSourceDefinition;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;

public class PTPExtractMappingGenerator extends AbstractMappingGenerator {

	private final GeneratorContext context;
	private final PTPWorkflow wfDefinition;
	private final Jaxb2Marshaller marshaller;
	private final SourceTableAbbreviationMap sourceTableAbbreviation;
	
	public PTPExtractMappingGenerator(GeneratorContext context,Jaxb2Marshaller marshaller,SourceTableAbbreviationMap sourceTableAbbreviation){
		
		this.context=context;
		this.wfDefinition=(PTPWorkflow) context.inputWF;
		this.marshaller=marshaller;
		this.sourceTableAbbreviation=sourceTableAbbreviation;
		
	}
	
	InfaMappingObject getExtractMapping() throws IOException, SAXException, JAXBException {

		InfaSourceDefinition sourceTableDef;

		Map<String, String> emptyValuesMap = new HashMap<>();
		Map<String, String> commonValuesMap = new HashMap<>();



		String tblName = wfDefinition.getSourceTableName();
		String dbName = wfDefinition.getSourceName();
		String sourceFilter = wfDefinition.getSourceFilter();
		String tableOwner = context.source.getOwnerName();

		List<InfaSourceColumnDefinition> allTableColumns = context.colRepository.accept(context.columnQueryVisitor, tblName);

		List<PTPWorkflowSourceColumn> inputSelectedColumns = wfDefinition.getColumns();

		List<InfaSourceColumnDefinition> matchedColumns = this
				.getFilteredSourceDefnColumns(context.colRepository.accept(context.columnQueryVisitor, tblName), inputSelectedColumns);


		
		//for SQL server each table can have a different owner so needs looking up
		//for Non-Siebel sources, each table can have a different owner so needs looking up
		if ((!(context.source.getName().equals("CUK")) && (!context.source.getName().equals("CGL")))) {

			List<DataSourceTableDTO> sourceTables = context.tableRepository.accept(context.tableQueryVisitor);
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
				.databaseName(context.source.getName())//
				.databaseType(context.source.getDbType())//
				.build();


		sourceTableDef.getColumns().addAll(matchedColumns);


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
						.mapper(context.sourceToTargetDatatypeMapper)//
						.addFields(matchedColumns)//
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
						.noMoreValues()//
						.loadSourceQualifierFromSeed("Seed_SourceQualifier")//
						.addFields(context.dataTypeMapper, matchedColumns)//
						.addFilter(sourceFilter)
						.addCCFilterFromColumns(inputSelectedColumns, tblName)
						.noMoreFilters()
						.name("SQ_ExtractData")//
						.build())//
				.transformation(ExpressionXformBuilder.newBuilder()//
						.expressionFromPrototype("ExpFromPrototype")//
						.expression("EXP_Resolve")//
						.mapper(context.dataTypeMapper).addEffectiveFromDateField()//
						.addEtlProcWidField()//
						.addDatasourceNumIdField()//
						.addIntegrationIdField(matchedColumns)//
						.addBUIDField(matchedColumns)//
						.addPGUIDField(dbName, tblName, sourceTableAbbreviation, matchedColumns)//
						.addMD5HashField(matchedColumns)//
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
						.mapper(context.dataTypeMapper).noMoreFields()//
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
				.mappingvariable(getDataSourceNumIdMappingVariable(Integer.toString(context.source.getSourceNum())))//
				.noMoreMappingVariables()//
				.build();

		return mappingObjExtract;

	}
	
	
	
}
