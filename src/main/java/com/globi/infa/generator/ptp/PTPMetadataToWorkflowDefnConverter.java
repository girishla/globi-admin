package com.globi.infa.generator.ptp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.globi.infa.datasource.core.PTPDataSourceTableColumnDTO;
import com.globi.infa.datasource.core.DataSourceTableDTO;
import com.globi.infa.workflow.ptp.PTPWorkflow;
import com.globi.infa.workflow.ptp.PTPWorkflowSourceColumn;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PTPMetadataToWorkflowDefnConverter {

	private final List<PTPDataSourceTableColumnDTO> columns;

	public PTPMetadataToWorkflowDefnConverter(List<PTPDataSourceTableColumnDTO> columns) {
		this.columns = columns;
	}

	private Map<String, DataSourceTableDTO> getDistinctTableMapFrom(List<PTPDataSourceTableColumnDTO> columns) {

		
		return columns.stream()//
				.map(column -> {
					return DataSourceTableDTO.builder()//
							.sourceName(column.getSourceName())//
							.tableName(column.getTableName())//
							.tableOwner(column.getTableOwner()).build();
				}).collect(Collectors.toMap(DataSourceTableDTO::getTableName, p -> p, (p, q) -> p));

	}

	private Map<String, PTPDataSourceTableColumnDTO> getDistinctColumnMapFor(String sourceName, String tableName,
			List<PTPDataSourceTableColumnDTO> columns) {

		return columns.stream().filter(
				column -> (column.getTableName().equals(tableName) && column.getSourceName().equals(sourceName)))//
				.collect(Collectors.toMap(PTPDataSourceTableColumnDTO::getColName, p -> p, (p, q) -> p));

	}

	private PTPWorkflowSourceColumn buildSourceColumnDefnFrom(PTPDataSourceTableColumnDTO column) {

		String colName = column.getColName();

		// Ugly hack - please remove as soon as this info is captured elsewhere
		if (colName.equals("ROW_ID")) {
			column.setIntegrationId(true);
		}

		return (PTPWorkflowSourceColumn.builder()//
				.changeCaptureColumn(column.isChangeCaptureCol())//
				.integrationIdColumn(column.isIntegrationId())//
				.pguidColumn(column.isPguidCol())//
				.buidColumn(column.isBuidCol())
				.columnSequence(column.getColOrder())
				.sourceColumnName(colName)//
				.build());

	}
	

	private PTPWorkflow getExtractWorkflowDefinitionFor(DataSourceTableDTO table) {

		String generatedWFName = "PTP_" + table.getSourceName() + "_" + table.getTableName();
		String generatedWFUri = "/GeneratedWorkflows/Repl/" + generatedWFName + ".xml";

		List<PTPWorkflowSourceColumn> workflowSourceColumnList = new ArrayList<>();

		Map<String, PTPDataSourceTableColumnDTO> distinctCols = getDistinctColumnMapFor(table.getSourceName(),
				table.getTableName(), columns);

		// all columns in the metadata become input columns to the generator
		distinctCols.entrySet().stream()//
				.filter(column -> column.getValue().getTableName().equals(table.getTableName()))//
				.forEach(column -> workflowSourceColumnList.add(buildSourceColumnDefnFrom(column.getValue())));

		
		//hack to set row_id as integration id for siebel sources if none specified. To be eventually removed.
		if((table.getSourceName().equals("CUK")) || (table.getSourceName().equals("CGL"))){
			if(!(distinctCols.entrySet().stream().anyMatch(column->column.getValue().isIntegrationId()))){	
				workflowSourceColumnList.add(PTPWorkflowSourceColumn.builder()//
						.changeCaptureColumn(false)//
						.integrationIdColumn(true)//
						.pguidColumn(false)//
						.sourceColumnName("ROW_ID")//
						.build());
			}
			
		}
		
			return PTPWorkflow.builder()//
					.sourceName(table.getSourceName())//
					.sourceFilter("")
					.sourceTableName(table.getTableName())
					.columns(workflowSourceColumnList)
					.workflowName(generatedWFName)
					.workflowUri(generatedWFUri)
					.targetTableName(table.getSourceName() + "_" +table.getTableName())
					.build();
		
		
					
		}


	public List<PTPWorkflow> getExtractWorkflowDefinitionObjects() {
		List<PTPWorkflow> ptpWorkflows = new ArrayList<>();
		columns.stream().map(column -> column.getSourceName()).distinct().forEach(sourceName -> {

			Map<String, DataSourceTableDTO> tables = getDistinctTableMapFrom(columns.stream()//
					.filter(column -> column.getSourceName().equals(sourceName))//
					.collect(Collectors.toList()));

			tables.keySet()//
					.stream()//
					.filter(table -> tables.get(table).getSourceName().equals(sourceName))
					.forEach(table -> ptpWorkflows.add(getExtractWorkflowDefinitionFor(tables.get(table))));


		});

		return ptpWorkflows;

	}
	


}
