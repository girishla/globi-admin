package com.globi.infa.generator.sil;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.datasource.core.MetadataFactoryMapper;
import com.globi.infa.datasource.core.SourceMetadataFactory;
import com.globi.infa.generator.BaseGeneratorContext;
import com.globi.infa.metadata.src.InfaSourceColumnDefinition;
import com.globi.infa.metadata.src.SILInfaSourceColumnDefinition;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.infa.workflow.SILWorkflow;
import com.globi.infa.workflow.SILWorkflowSourceColumn;

import lombok.Getter;

public class SILGeneratorContext extends BaseGeneratorContext {

	@Getter
	List<SILInfaSourceColumnDefinition> matchedColumnsSIL;

	@Getter
	SILWorkflow silWorkflow;

	
	public static SILGeneratorContext contextFor(String sourceName, String tblName, MetadataFactoryMapper mapper,
			InfaWorkflow inputWF) {

		SILGeneratorContext context = new SILGeneratorContext();
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
		context.silWorkflow = (SILWorkflow) inputWF;
		context.allSourceColumns = context.getColRepository().accept(context.getColumnQueryVisitor(), tblName);
		context.matchedColumnsSIL = getFilteredSourceDefnColumns(context.allSourceColumns,
				context.silWorkflow.getColumns());

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

	public static SILInfaSourceColumnDefinition getSILColDefinitionFrom(InfaSourceColumnDefinition column,
			List<SILWorkflowSourceColumn> inputSelectedColumns) {

		SILInfaSourceColumnDefinition silCol = SILInfaSourceColumnDefinition.builder()//
				.autoColumnFlag(false)
				.domainLookupColumnFlag(false)
				.legacyColumnFlag(false)
				.targetColumnFlag(false)
				.miniDimColumnFlag(false)
				.columnType("")
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
				.filter(selectedCol -> selectedCol.getColumnName().equals(silCol.getColumnName()))
				.forEach(selectedCol -> {

					silCol.setAutoColumnFlag(selectedCol.isAutoColumn());
					silCol.setColumnType(selectedCol.getColumnType());
					silCol.setDomainLookupColumnFlag(selectedCol.isDomainLookupColumn());
					silCol.setLegacyColumnFlag(selectedCol.isLegacyColumn());
					silCol.setMiniDimColumnFlag(selectedCol.isMiniDimColumn());
					silCol.setTargetColumnFlag(selectedCol.isTargetColumn());
					silCol.setSelected(true);
				    silCol.setDimTableName(selectedCol.getDimTableName());

				});

		return silCol;

	}

	public static List<SILInfaSourceColumnDefinition> getFilteredSourceDefnColumns(
			List<InfaSourceColumnDefinition> allTableColumns, List<SILWorkflowSourceColumn> inputSelectedColumns) {

		List<SILInfaSourceColumnDefinition> matchedColumnsSIL = allTableColumns//
				.stream()//
				.filter(col -> inputSelectedColumns.stream()//
						.anyMatch(selectedCol -> selectedCol.getColumnName().equals(col.getColumnName())))
				.map(column -> {

					return getSILColDefinitionFrom(column, inputSelectedColumns);

				}).collect(Collectors.toList());

		return matchedColumnsSIL;

	}

}
