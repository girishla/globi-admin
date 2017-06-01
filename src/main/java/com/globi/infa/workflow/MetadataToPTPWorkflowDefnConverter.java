package com.globi.infa.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.globi.infa.datasource.core.DataSourceTableColumnDTO;
import com.globi.infa.datasource.core.DataSourceTableDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetadataToPTPWorkflowDefnConverter {

	private final List<DataSourceTableColumnDTO> columns;

	MetadataToPTPWorkflowDefnConverter(PTPWorkflowRepository wfRepository, List<DataSourceTableColumnDTO> columns) {
		this.columns = columns;
	}

	private Map<String, DataSourceTableDTO> getDistinctTableMapFrom(List<DataSourceTableColumnDTO> columns) {

		return columns.stream()//
				.map(column -> {
					return DataSourceTableDTO.builder()//
							.sourceName(column.getSourceName())//
							.tableName(column.getTableName()).tableOwner(column.getTableOwner()).build();
				}).collect(Collectors.toMap(DataSourceTableDTO::getTableName, p -> p, (p, q) -> p));

	}

	private Map<String, DataSourceTableColumnDTO> getDistinctColumnMapFor(String tableName,
			List<DataSourceTableColumnDTO> columns) {

		return columns.stream().filter(column -> column.getTableName().equals(tableName))//
				.collect(Collectors.toMap(DataSourceTableColumnDTO::getColName, p -> p, (p, q) -> p));

	}

	private PTPWorkflowSourceColumn buildSourceColumnDefnFrom(DataSourceTableColumnDTO column) {

		if (column.getColName().equals("ROW_ID")) {
			column.setIntegrationId(true);
		}

		if (column.getColName().equals("LAST_UPD")) {
			column.setChangeCaptureCol(true);
		}

		return (PTPWorkflowSourceColumn.builder()//
				.changeCaptureColumn(column.isChangeCaptureCol())//
				.integrationIdColumn(column.isIntegrationId())//
				.sourceColumnName(column.getColName())//
				.build());

	}

	private PTPWorkflow getWorkflowDefinitionFor(DataSourceTableDTO table) {

		// Build input WF definition and run generator once for each table
		log.info("------------------------------------------*");
		log.info("**************beginning to process " + table);

		String generatedWFName = "PTP_" + table.getTableName() + "_Extract";
		String generatedWFUri = "/GeneratedWorkflows/Repl/" + "PTP_" + table.getTableName() + ".xml";

		List<PTPWorkflowSourceColumn> workflowSourceColumnList = new ArrayList<>();

		Map<String, DataSourceTableColumnDTO> distinctCols = getDistinctColumnMapFor(table.getTableName(), columns);

		// all columns in the metadata become input columns to the generator
		distinctCols.entrySet().stream()//
				.filter(column -> column.getValue().getTableName().equals(table.getTableName()))//
				.forEach(column -> workflowSourceColumnList.add(buildSourceColumnDefnFrom(column.getValue())));

		return PTPWorkflow.builder()//
				.sourceName(table.getSourceName())//
				.sourceTableName(table.getTableName()).columns(workflowSourceColumnList)
				.workflow(InfaWorkflow.builder()//
						.workflowUri(generatedWFUri)//
						.workflowName(generatedWFName)//
						.workflowType("PTP")//
						.build())
				.build();

	}

	public List<PTPWorkflow> getWorkflowDefinitionObjects() {
		List<PTPWorkflow> ptpWorkflows = new ArrayList<>();
		Map<String, DataSourceTableDTO> tables = getDistinctTableMapFrom(columns);

		tables.keySet()//
				.stream()//
				.forEach(table -> ptpWorkflows.add(getWorkflowDefinitionFor(tables.get(table))));

		return ptpWorkflows;

	}

}
