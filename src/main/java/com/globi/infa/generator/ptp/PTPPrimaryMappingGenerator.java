package com.globi.infa.generator.ptp;

import static com.globi.infa.generator.builder.InfaObjectMother.getDataSourceNumIdMappingVariable;
import static com.globi.infa.generator.builder.InfaObjectMother.getEtlProcWidMappingVariable;
import static com.globi.infa.generator.builder.InfaObjectMother.getInitialExtractDateMappingVariable;
import static com.globi.infa.generator.builder.InfaObjectMother.getTablenameMappingVariable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.xml.sax.SAXException;

import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.generator.AbstractMappingGenerator;
import com.globi.infa.generator.builder.ExpressionXformBuilder;
import com.globi.infa.generator.builder.InfaMappingObject;
import com.globi.infa.generator.builder.LookupXformBuilder;
import com.globi.infa.generator.builder.MappingBuilder;
import com.globi.infa.generator.builder.SourceDefinitionBuilder;
import com.globi.infa.generator.builder.SourceQualifierBuilder;
import com.globi.infa.generator.builder.TargetDefinitionBuilder;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.metadata.src.PTPInfaSourceColumnDefinition;
import com.globi.infa.workflow.ptp.PTPWorkflow;
import com.globi.infa.workflow.ptp.PTPWorkflowSourceColumn;
import com.globi.metadata.sourcesystem.SourceSystem;

public class PTPPrimaryMappingGenerator extends AbstractMappingGenerator {

	private final PTPWorkflow wfDefinition;
	private final List<InfaSourceColumnDefinition> allSourceColumns;
	private final List<PTPInfaSourceColumnDefinition> matchedColumnsPTP;
	private final SourceSystem sourceSystem;
	private final DataSourceTableDTO sourceTable;
	private final Jaxb2Marshaller marshaller;
	private final DataTypeMapper dataTypeMapper;
	private final DataTypeMapper sourceToTargetDatatypeMapper;
	
	
	public PTPPrimaryMappingGenerator(PTPWorkflow wfDefinition,//
			List<InfaSourceColumnDefinition> allSourceColumns,//
			List<PTPInfaSourceColumnDefinition> matchedColumnsPTP,//
			SourceSystem sourceSystem,//
			DataSourceTableDTO sourceTable,//
			Jaxb2Marshaller marshaller,//
			DataTypeMapper dataTypeMapper,//
			DataTypeMapper sourceToTargetDatatypeMapper){
		
		this.wfDefinition=wfDefinition;
		this.allSourceColumns=allSourceColumns;
		this.sourceSystem=sourceSystem;
		this.sourceTable=sourceTable;
		this.marshaller=marshaller;
		this.dataTypeMapper=dataTypeMapper;
		this.sourceToTargetDatatypeMapper=sourceToTargetDatatypeMapper;
		this.matchedColumnsPTP=matchedColumnsPTP;

		
	}
	
	InfaMappingObject getPrimaryMapping() throws IOException, SAXException, JAXBException {

		String tblName = wfDefinition.getSourceTableName();
		String dbName = wfDefinition.getSourceName();
		String sourceFilter = wfDefinition.getSourceFilter();
		String tableOwner = sourceTable.getTableOwner();
		String targetTableName = wfDefinition.getTargetTableName();
		String targetTableDefnName = targetTableName.isEmpty() ? dbName + "_" + tblName + "_P" : targetTableName + "_P";

		List<PTPWorkflowSourceColumn> inputSelectedColumns = wfDefinition.getColumns();

//		List<PTPInfaSourceColumnDefinition> matchedColumnsPTP = this
//				.getFilteredSourceDefnColumns(allSourceColumns, inputSelectedColumns);



		//Keep only Integration Id columns
		List<PTPInfaSourceColumnDefinition> columnsList = matchedColumnsPTP//
				.stream()//
				.filter(column -> column.getIntegrationIdFlag())//
				.collect(Collectors.toList());



		InfaMappingObject mappingObjPrimary = MappingBuilder//
				.newBuilder()//
				.simpleTableSyncClass("simpleTableSyncClass")//
				.sourceDefn(SourceDefinitionBuilder.newBuilder()//
						.sourceDefnFromPrototype("SourceFromPrototype")//
						.sourceDefn(sourceSystem,tblName,tableOwner)//
						.addFields((List<InfaSourceColumnDefinition>)(List<?>)allSourceColumns)//
						.name(tblName)//
						.build())
				.noMoreSources()//
				.targetDefn(TargetDefinitionBuilder.newBuilder()//
						.marshaller(marshaller)//
						.loadTargetFromSeed("Seed_PTP_PTPPrimaryExtractTargetTable")//
						.mapper(sourceToTargetDatatypeMapper).noMoreFields()//
						.name(targetTableDefnName)//
						.build())//
				.noMoreTargets()//
				.noMoreMapplets()//
				.noMoreReusableXforms()
				.startMappingDefn("PTP_" + dbName + "_" + tblName + "_Primary")//
				.transformation(SourceQualifierBuilder.newBuilder()//
						.marshaller(marshaller)//
						.noMoreValues()
						.loadSourceQualifierFromSeed("Seed_PTP_SourceQualifier")//
						.addFields(dataTypeMapper, (List<InfaSourceColumnDefinition>)(List<?>)columnsList)//
						.addCCFilterFromColumns(inputSelectedColumns, tblName)
						.addFilter(sourceFilter)
						.noMoreFilters()
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
						.noMoreInterpolationValues()//
						.loadLookupXformFromSeed("Seed_PTP_LKPPTPPrimaryRecordKeys")//
						.nameAlreadySet()//
						.build())
				.autoConnectByName(tblName, "SQ_PrimaryData")//
				.autoConnectByName("LKP_RecordKeys", targetTableDefnName)//
				.autoConnectByName("EXP_Resolve", targetTableDefnName)//
				.connector("EXP_Resolve", "SYS_INTEGRATION_ID", "LKP_RecordKeys", "SYS_INTEGRATION_ID_IN")//
				.noMoreTransformations()//
				.noMoreConnectors()//
				.noMoreTargetLoadOrders()//
				.mappingvariable(getEtlProcWidMappingVariable())//
				.mappingvariable(getInitialExtractDateMappingVariable())//
				.mappingvariable(getDataSourceNumIdMappingVariable(Integer.toString(sourceSystem.getSourceNum())))//
				.mappingvariable(getTablenameMappingVariable(
						targetTableName.isEmpty() ? dbName + "_" + tblName : targetTableName))//
				.noMoreMappingVariables()//
				.build();

		return mappingObjPrimary;

	}
	
	
	
//	private List<PTPInfaSourceColumnDefinition> getFilteredSourceDefnColumns(
//			List<PTPInfaSourceColumnDefinition> allTableColumns, List<PTPWorkflowSourceColumn> inputSelectedColumns) {
//
//		Map<String, PTPInfaSourceColumnDefinition> allColsMap = allTableColumns.stream()
//				.collect(Collectors.toMap(PTPInfaSourceColumnDefinition::getColumnName, Function.identity()));
//
//		inputSelectedColumns.stream().forEach(inputColumn -> {
//			if (allColsMap.containsKey(inputColumn.getSourceColumnName())) {
//				allColsMap.get(inputColumn.getSourceColumnName())
//						.setIntegrationIdFlag(inputColumn.isIntegrationIdColumn());
//				allColsMap.get(inputColumn.getSourceColumnName()).setColumnSequence(inputColumn.getColumnSequence());
//				allColsMap.get(inputColumn.getSourceColumnName()).setBuidFlag(inputColumn.isBuidColumn());
//				allColsMap.get(inputColumn.getSourceColumnName()).setCcFlag(inputColumn.isChangeCaptureColumn());
//				allColsMap.get(inputColumn.getSourceColumnName()).setPguidFlag(inputColumn.isPguidColumn());
//				allColsMap.get(inputColumn.getSourceColumnName()).setSelected(true);
//			}
//		});
//
//		List<PTPInfaSourceColumnDefinition> matchedColumnsPTP = allColsMap.values()//
//				.stream()//
//				.filter(column -> column.getSelected()).collect(Collectors.toList());
//
//		return matchedColumnsPTP;
//
//	}
	
	
}
