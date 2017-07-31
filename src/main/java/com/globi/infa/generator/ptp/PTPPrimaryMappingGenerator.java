package com.globi.infa.generator.ptp;

import static com.globi.infa.generator.builder.InfaObjectMother.getDataSourceNumIdMappingVariable;
import static com.globi.infa.generator.builder.InfaObjectMother.getEtlProcWidMappingVariable;
import static com.globi.infa.generator.builder.InfaObjectMother.getInitialExtractDateMappingVariable;
import static com.globi.infa.generator.builder.InfaObjectMother.getTablenameMappingVariable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.xml.sax.SAXException;

import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.generator.AbstractMappingGenerator;
import com.globi.infa.generator.GeneratorContext;
import com.globi.infa.generator.builder.ExpressionXformBuilder;
import com.globi.infa.generator.builder.InfaMappingObject;
import com.globi.infa.generator.builder.LookupXformBuilder;
import com.globi.infa.generator.builder.MappingBuilder;
import com.globi.infa.generator.builder.SourceDefinitionBuilder;
import com.globi.infa.generator.builder.SourceQualifierBuilder;
import com.globi.infa.generator.builder.TargetDefinitionBuilder;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.metadata.src.InfaSourceDefinition;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;

public class PTPPrimaryMappingGenerator extends AbstractMappingGenerator {

	private final GeneratorContext context;
	private final PTPWorkflow wfDefinition;
	private final Jaxb2Marshaller marshaller;
	
	public PTPPrimaryMappingGenerator(GeneratorContext context,Jaxb2Marshaller marshaller){
		
		this.context=context;
		this.wfDefinition=(PTPWorkflow) context.inputWF;
		this.marshaller=marshaller;
		
	}
	
	InfaMappingObject getPrimaryMapping() throws IOException, SAXException, JAXBException {

		InfaSourceDefinition sourceTableDef;
		
		
		Map<String, String> commonValuesMap = new HashMap<>();


		String tblName = wfDefinition.getSourceTableName();
		String dbName = wfDefinition.getSourceName();
		String sourceFilter = wfDefinition.getSourceFilter();
		String tableOwner = context.source.getOwnerName();

		List<InfaSourceColumnDefinition> allTableColumns = context.colRepository.accept(context.columnQueryVisitor, tblName);
		List<PTPWorkflowSourceColumn> inputSelectedColumns = wfDefinition.getColumns();

		List<InfaSourceColumnDefinition> matchedColumns = this
				.getFilteredSourceDefnColumns(context.colRepository.accept(context.columnQueryVisitor, tblName), inputSelectedColumns);

		
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
				.sourceTableUniqueName(context.source.getName() + "_" + tblName)//
				.build();

		//String combinedFilter = getSourceFilterString(sourceFilter, inputSelectedColumns, tblName);

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
						.mapper(context.sourceToTargetDatatypeMapper).noMoreFields()//
						.name(targetTableDefnName)//
						.build())//
				.noMoreTargets()//
				.noMoreMapplets()//
				.startMappingDefn("PTP_" + dbName + "_" + tblName + "_Primary")//
				.transformation(SourceQualifierBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noMoreValues()
						.loadSourceQualifierFromSeed("Seed_SourceQualifier")//
						.addFields(context.dataTypeMapper, columnsList)//
						.addCCFilterFromColumns(inputSelectedColumns, tblName)
						.addFilter(sourceFilter)
						.noMoreFilters()
						.name("SQ_PrimaryData")//
						.build())//
				.transformation(ExpressionXformBuilder.newBuilder()//
						.expressionFromPrototype("ExpFromPrototype")//
						.expression("EXP_Resolve")//
						.mapper(context.dataTypeMapper).addIntegrationIdField(columnsList)//
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
				.mappingvariable(getDataSourceNumIdMappingVariable(Integer.toString(context.source.getSourceNum())))//
				.mappingvariable(getTablenameMappingVariable(
						targetTableName.isEmpty() ? dbName + "_" + tblName : targetTableName))//
				.noMoreMappingVariables()//
				.build();

		return mappingObjPrimary;

	}
	
	
	
}
