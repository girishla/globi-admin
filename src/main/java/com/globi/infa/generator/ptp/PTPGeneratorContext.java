package com.globi.infa.generator.ptp;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.datasource.core.MetadataFactoryMapper;
import com.globi.infa.datasource.core.SourceMetadataFactory;
import com.globi.infa.generator.BaseGeneratorContext;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.metadata.src.PTPInfaSourceColumnDefinition;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;

import lombok.Getter;

public class PTPGeneratorContext extends BaseGeneratorContext {

	@Getter
	List<PTPInfaSourceColumnDefinition> matchedColumnsPTP;

	@Getter
	PTPWorkflow ptpWorkflow;

	
	public static PTPGeneratorContext contextFor(String sourceName, String tblName, MetadataFactoryMapper mapper,
			InfaWorkflow inputWF) {

		PTPGeneratorContext context = new PTPGeneratorContext();
		SourceMetadataFactory sourceMetadataFactory = mapper.getMetadataFactoryMap().get(sourceName);
		
		if(sourceMetadataFactory==null){
			throw new IllegalArgumentException(String.format("Invalid datasource %s. Please ensure the datasource is configured correctly",sourceName));
		}

		context.dataTypeMapper = sourceMetadataFactory.createDatatypeMapper();
		context.sourceToTargetDatatypeMapper = sourceMetadataFactory.createSourceToTargetDatatypeMapper();
		context.colRepository = sourceMetadataFactory.createTableColumnRepository();
		context.columnQueryVisitor = sourceMetadataFactory.createTableColumnMetadataVisitor();
		context.tableQueryVisitor = sourceMetadataFactory.createTableMetadataVisitor();
		context.tableRepository = sourceMetadataFactory.createTableRepository();
		context.source = sourceMetadataFactory.getSourceSystem();
		context.inputWF = inputWF;
		context.ptpWorkflow = (PTPWorkflow) inputWF;
		context.allSourceColumns = context.getColRepository().accept(context.getColumnQueryVisitor(), tblName);
		context.matchedColumnsPTP = getFilteredSourceDefnColumns(context.allSourceColumns,
				context.ptpWorkflow.getColumns());

		List<DataSourceTableDTO> sourceTables = context.getTableRepository().accept(context.getTableQueryVisitor());
		Optional<DataSourceTableDTO> sourceTableOptional = sourceTables.stream()//
				.filter(table -> table.getTableName().equals(tblName)).findFirst();

		if (!sourceTableOptional.isPresent()) {
			throw new IllegalArgumentException("Cannot find Source Table. Please ensure it is valid.");
		} else {
			context.sourceTable = sourceTableOptional.get();
		}

		return context;

	}

	public static PTPInfaSourceColumnDefinition getPTPColDefinitionFrom(InfaSourceColumnDefinition column,
			List<PTPWorkflowSourceColumn> inputSelectedColumns) {

		PTPInfaSourceColumnDefinition ptpCol = (PTPInfaSourceColumnDefinition) PTPInfaSourceColumnDefinition.builder()//
				.buidFlag(false)//
				.ccFlag(false)//
				.integrationIdFlag(false)//
				.pguidFlag(false)//
				.columnDataType(column.getColumnDataType())//
				.columnLength(column.getColumnLength())//
				.columnName(column.getColumnName())//
				.columnNumber(column.getColumnNumber())//
				.nullable(column.getNullable())//
				.offset(column.getOffset())//
				.physicalLength(column.getPhysicalLength())//
				.physicalOffset(column.getPhysicalOffset())//
				.precision(column.getPrecision())//
				.scale(column.getScale())//
				.selected(column.getSelected())//
				.build();

		inputSelectedColumns.stream()//
				.filter(selectedCol -> selectedCol.getSourceColumnName().equals(ptpCol.getColumnName()))
				.forEach(selectedCol -> {

					ptpCol.setIntegrationIdFlag(selectedCol.isIntegrationIdColumn());
					ptpCol.setColumnSequence(selectedCol.getColumnSequence());
					ptpCol.setBuidFlag(selectedCol.isBuidColumn());
					ptpCol.setCcFlag(selectedCol.isChangeCaptureColumn());
					ptpCol.setPguidFlag(selectedCol.isPguidColumn());
					ptpCol.setSelected(true);

				});

		return ptpCol;

	}

	public static List<PTPInfaSourceColumnDefinition> getFilteredSourceDefnColumns(
			List<InfaSourceColumnDefinition> allTableColumns, List<PTPWorkflowSourceColumn> inputSelectedColumns) {

		List<PTPInfaSourceColumnDefinition> matchedColumnsPTP = allTableColumns//
				.stream()//
				.filter(col -> inputSelectedColumns.stream()//
						.anyMatch(selectedCol -> selectedCol.getSourceColumnName().equals(col.getColumnName())))
				.map(column -> {

					return getPTPColDefinitionFrom(column, inputSelectedColumns);

				}).collect(Collectors.toList());

		return matchedColumnsPTP;

	}

}
