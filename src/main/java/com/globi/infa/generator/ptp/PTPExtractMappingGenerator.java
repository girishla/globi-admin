package com.globi.infa.generator.ptp;

import static com.globi.infa.generator.builder.InfaObjectMother.getDataSourceNumIdMappingVariable;
import static com.globi.infa.generator.builder.InfaObjectMother.getEtlProcWidMappingVariable;
import static com.globi.infa.generator.builder.InfaObjectMother.getInitialExtractDateMappingVariable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.xml.sax.SAXException;

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
import com.globi.infa.metadata.core.StringMap;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;
import com.globi.metadata.sourcesystem.SourceSystem;

public class PTPExtractMappingGenerator extends AbstractMappingGenerator {

	private final PTPWorkflow wfDefinition;
	private final List<InfaSourceColumnDefinition> allSourceColumns;
	private final SourceSystem sourceSystem;
	private final DataSourceTableDTO sourceTable;
	private final StringMap sourceTableAbbreviation;
	private final Jaxb2Marshaller marshaller;
	private final DataTypeMapper dataTypeMapper;
	private final DataTypeMapper sourceToTargetDatatypeMapper;
	
	public PTPExtractMappingGenerator(PTPWorkflow wfDefinition,//
			List<InfaSourceColumnDefinition> allSourceColumns,//
			SourceSystem sourceSystem,//
			DataSourceTableDTO sourceTable,//
			StringMap sourceTableAbbreviation,//
			Jaxb2Marshaller marshaller,//
			DataTypeMapper dataTypeMapper,//
			DataTypeMapper sourceToTargetDatatypeMapper){
		
		this.wfDefinition=wfDefinition;
		this.allSourceColumns=allSourceColumns;
		this.sourceSystem=sourceSystem;
		this.sourceTable=sourceTable;
		this.marshaller=marshaller;
		this.sourceTableAbbreviation=sourceTableAbbreviation;
		this.dataTypeMapper=dataTypeMapper;
		this.sourceToTargetDatatypeMapper=sourceToTargetDatatypeMapper;
		
		
	}
	
	InfaMappingObject getExtractMapping() throws IOException, SAXException, JAXBException {

		String tblName = wfDefinition.getSourceTableName();
		String dbName = wfDefinition.getSourceName();
		String sourceFilter = wfDefinition.getSourceFilter();
		String tableOwner = sourceTable.getTableOwner();
		String targetTableName = wfDefinition.getTargetTableName();
		String targetTableDefnName = targetTableName.isEmpty() ? dbName + "_" + tblName : targetTableName;
		Map<String, String> emptyValuesMap = new HashMap<>();
		Map<String, String> commonValuesMap = new HashMap<>();
		commonValuesMap.put("targetTableName", targetTableDefnName);
		commonValuesMap.put("sourceName", dbName);
		
		
		List<PTPWorkflowSourceColumn> inputSelectedColumns = wfDefinition.getColumns();

		List<InfaSourceColumnDefinition> matchedColumns = this
				.getFilteredSourceDefnColumns(allSourceColumns, inputSelectedColumns);


		InfaMappingObject mappingObjExtract = MappingBuilder//
				.newBuilder()//
				.simpleTableSyncClass("simpleTableSyncClass")//
				.sourceDefn(SourceDefinitionBuilder.newBuilder()//
						.sourceDefnFromPrototype("SourceFromPrototype")//
						.sourceDefn(sourceSystem,tblName,tableOwner)//
						.addFields(allSourceColumns)//
						.name(tblName)//
						.build())//
				.noMoreSources()//
				.targetDefn(TargetDefinitionBuilder.newBuilder()//
						.marshaller(marshaller)//
						.loadTargetFromSeed("Seed_PTP_PTPTargetTableSystemCols")//
						.mapper(sourceToTargetDatatypeMapper)//
						.addFields(matchedColumns)//
						.noMoreFields()//
						.name(targetTableDefnName)//
						.build())//
				.noMoreTargets()//
				.mappletDefn(MappletBuilder.newBuilder()//
						.marshaller(marshaller)//
						.loadMappletFromSeed("Seed_PTP_MPLDomainLookup")//
						.nameAlreadySet()//
						.build())//
				.noMoreMapplets()//
				.startMappingDefn("PTP_" + dbName + "_" + tblName + "_Extract")//
				.transformation(SourceQualifierBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noMoreValues()//
						.loadSourceQualifierFromSeed("Seed_PTP_SourceQualifier")//
						.addFields(dataTypeMapper, matchedColumns)//
						.addFilter(sourceFilter)
						.addCCFilterFromColumns(inputSelectedColumns, tblName)
						.noMoreFilters()
						.name("SQ_ExtractData")//
						.build())//
				.transformation(ExpressionXformBuilder.newBuilder()//
						.expressionFromPrototype("ExpFromPrototype")//
						.expression("EXP_Resolve")//
						.mapper(dataTypeMapper).addEffectiveFromDateField()//
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
						.loadExpressionXformFromSeed("Seed_PTP_WidSequence")//
						.nameAlreadySet()//
						.build())
				.transformation(LookupXformBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setInterpolationValues(commonValuesMap)//
						.loadLookupXformFromSeed("Seed_PTP_LKPRecordInstanceViaHash")//
						.nameAlreadySet()//
						.build())
				.transformation(LookupXformBuilder.newBuilder()//
						.marshaller(marshaller)//
						.setInterpolationValues(commonValuesMap)//
						.loadLookupXformFromSeed("Seed_PTP_LKPBU_" + dbName)//
						.nameAlreadySet()//
						.build())
				.transformation(ExpressionXformBuilder.newBuilder()//
						.ExpressionFromSeed("Prepare BU Domain Lookup")//
						.marshaller(marshaller)//
						.setInterpolationValues(commonValuesMap)//
						.loadExpressionXformFromSeed("Seed_PTP_EXPPrepDomLookup")//
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
				.mappingvariable(getDataSourceNumIdMappingVariable(Integer.toString(sourceSystem.getSourceNum())))//
				.noMoreMappingVariables()//
				.build();

		return mappingObjExtract;

	}
	
	
	
}
